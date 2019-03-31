package club.wodencafe.poker.holdem;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;

import club.wodencafe.bot.WodData;
import club.wodencafe.data.Player;
import club.wodencafe.poker.cards.Card;
import club.wodencafe.poker.cards.Deck;
import club.wodencafe.poker.cards.HandUtil;
import club.wodencafe.poker.cards.hands.Hand;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

/**
 * This class represents a single hand of poker.
 * 
 * @author wodencafe
 */
public class RoundMediator implements AutoCloseable, Consumer<Command> {
	private ScheduledFuture<?> future = null;
	private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	private List<PlayerRoundData> playerData = new ArrayList<>();

	private PhaseManager phaseManager;

	private PublishSubject<String> generalMessage = PublishSubject.create();

	private PublishSubject<Map.Entry<Player, String>> playerMessage = PublishSubject.create();

	private Deck deck;

	private Optional<BettingRound> bettingRound = Optional.empty();

	private ReplaySubject<Map.Entry<Collection<PlayerRoundData>, Long>> roundComplete = ReplaySubject.create();

	private List<Card> communityCards = new ArrayList<>();

	private AtomicLong pot = new AtomicLong();

	public long getCurrentPotSize() {
		long potSize = pot.get();

		if (bettingRound.isPresent()) {
			potSize += bettingRound.get().getPotSize();
		}
		return potSize;
	}

	// Keep track of these just in case
	private List<Card> burnCards = new ArrayList<>();

	public List<PlayerRoundData> getPlayerData() {
		return playerData;
	}

	public List<Player> getPlayers() {
		return playerData.stream().map(x -> x.get()).collect(Collectors.toList());
	}

	public Observable<Map.Entry<Collection<PlayerRoundData>, Long>> onRoundComplete() {
		return roundComplete;
	}

	public Observable<String> onGeneralMessage() {
		return generalMessage;
	}

	public Observable<Map.Entry<Player, String>> onPlayerMessage() {
		return playerMessage;
	}

	private void cancelFuture() {
		if (future != null && !future.isCancelled() && !future.isDone()) {
			future.cancel(true);
		}
	}

	public RoundMediator(Player initialPlayer) {
		phaseManager = new PhaseManager();

		phaseManager.onNewPhase().subscribe(this::handleNewPhase);

		deck = Deck.generateDeck(false);

		phaseManager.run();

		addPlayer(initialPlayer);
	}

	public PhaseManager getPhaseManager() {
		return phaseManager;
	}

	private boolean close = false;

	@Override
	public void close() throws Exception {
		if (!close) {
			close = true;
			cancelFuture();

			service.shutdown();

			roundComplete.onComplete();

			generalMessage.onComplete();

			playerMessage.onComplete();

			if (!bettingRound.isEmpty()) {
				bettingRound.get().close();
			}
		}
	}

	public List<Player> getActivePlayers() {
		return playerData.stream().filter(x -> !x.isFolded()).filter(x -> !x.isShown()).map(x -> x.get())
				.collect(Collectors.toList());
	}

	private void handleNewPhase(Phase newPhase) {

		cancelFuture();

		StringBuilder message = new StringBuilder();

		if (getActivePlayers().size() == 1) {
			PlayerRoundData data = getPlayerDataByPlayer(getActivePlayers().iterator().next());
			roundComplete.onNext(new AbstractMap.SimpleEntry<>(Arrays.asList(data), pot.get()));
		} else {
			if (newPhase == Phase.AWAITING_PLAYERS) {

				future = service.schedule(() -> phaseManager.run(), 30, TimeUnit.SECONDS);

				message.append("Awaiting players, type " + WodData.commandChar + CommandType.DEAL.getCommandName()
						+ " to be dealt into the match.");

			}
			if (newPhase == Phase.HOLE) {

				message.append("Dealing cards to " + playerData.size() + " players.");

				dealAllPlayersIn();
			} else if (newPhase == Phase.FLOP) {

				burn(1);
				addCommunityCards(3);

				message.append(getCommunityCardString() + " on the flop.");

			} else if (newPhase == Phase.TURN) {
				burn(1);
				addCommunityCards(1);

				message.append(getCommunityCardString() + " with the turn card.");

			} else if (newPhase == Phase.RIVER) {
				burn(1);
				addCommunityCards(1);

				message.append(getCommunityCardString() + " with the river.");
			}
			if (newPhase.isBetPhase()) {

				message.append(" Remaining players: " + playerData.size() + ". Pot Size: $" + pot.get() + ".");

				PlayerRoundData player = playerData.iterator().next();

				message.append(" " + player.get().getIrcName() + "'s turn.");

				BettingRound bettingRoundValue = new BettingRound(playerData);
				bettingRoundValue.startAsync();
				bettingRound = Optional.of(bettingRoundValue);
				bettingRoundValue.onPlayerCommand().subscribe(data ->
				{
					StringBuilder sb = new StringBuilder();
					sb.append(data.getKey().getIrcName() + " "
							+ WordUtils.capitalizeFully(data.getValue().getCommandType().toString()) + "s"
							+ (data.getValue().getData().isEmpty() ? "."
									: ((data.getValue().getCommandType() == CommandType.RAISE ? " to " : " ") + "$"
											+ String.valueOf(data.getValue().getData().get())) + "."));
					long potSize = getCurrentPotSize();

					if (potSize > 0) {
						sb.append(" Current pot size is $" + potSize + ".");
					}

					Entry<Player, Player> currentAndPreviousPlayer = bettingRoundValue.getCurrentAndPreviousPlayer();

					Player currentPlayer = currentAndPreviousPlayer.getKey();
					if (currentPlayer != null) {
						sb.append(" " + currentAndPreviousPlayer.getKey().getIrcName() + "'s turn.");
					} else {
						sb.append(" " + WordUtils.capitalizeFully(newPhase.toString()) + " round complete.");
					}

					generalMessage.onNext(sb.toString());
				});
				bettingRoundValue.onBettingRoundComplete().subscribe(potAmount ->
				{

					pot.addAndGet(potAmount);

					phaseManager.run();
				});
			}
			if (newPhase == Phase.SHOWDOWN) {
				future = service.schedule(() -> showdown(), 30, TimeUnit.SECONDS);
				StringBuilder sb = new StringBuilder(
						"Showdown, Remaining players: " + playerData.size() + ". Pot Size: $" + pot.get() + ". ");
				for (PlayerRoundData player : playerData) {
					sb.append(player.get().getIrcName() + ", ");
				}
				sb.append("you can " + WodData.commandChar + "show or " + WodData.commandChar + "fold.");
				message.append(sb.toString());
			}
		}
		if (message.length() > 0) {
			generalMessage.onNext(message.toString());
		}
	}

	private String getCommunityCardString() {
		StringBuilder sb = new StringBuilder();
		for (Card card : communityCards) {
			sb.append(card);
		}
		return sb.toString();
	}

	private void showdown() {
		cancelFuture();

		StringBuilder sb = new StringBuilder();

		Map<PlayerRoundData, Hand> highestHands = getHighHands();

		Set<PlayerRoundData> playerRoundData = highestHands.keySet();

		if (playerRoundData.size() == 1) {
			PlayerRoundData winningPlayerData = playerRoundData.iterator().next();

			sb.append("Winner: " + winningPlayerData.get().getIrcName() + " with " + highestHands.get(winningPlayerData)
					+ ", winning $" + getCurrentPotSize() + ".");

			winningPlayerData.get().addMoney(getCurrentPotSize());
		} else if (playerRoundData.size() > 1) {
			sb.append("Multiple Winners: ");
			boolean firstIteration = true;
			long potSplit = getCurrentPotSize() / playerRoundData.size();
			for (PlayerRoundData winningPlayerData : playerRoundData) {
				if (!firstIteration) {
					sb.append(", ");
				} else {
					firstIteration = false;
				}
				sb.append(winningPlayerData.get().getIrcName() + " with " + highestHands.get(winningPlayerData));
				winningPlayerData.get().addMoney(potSplit);
			}

		}

		if (sb.length() > 0) {
			generalMessage.onNext(sb.toString());
		}

		roundComplete.onNext(new AbstractMap.SimpleEntry<>(playerRoundData, pot.get()));

		try {
			close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Map<PlayerRoundData, Hand> getHighHands() {

		List<PlayerRoundData> shownPlayers = getShownPlayerData();

		Map<PlayerRoundData, Hand> highestHands = new HashMap<>();

		for (PlayerRoundData data : shownPlayers) {
			List<Card> combinedCards = new ArrayList<>(communityCards);
			combinedCards.addAll(data.getCards());

			Hand hand = HandUtil.getHand(combinedCards);

			if (highestHands.isEmpty()) {
				highestHands.put(data, hand);
			} else {
				Hand highHand = highestHands.values().iterator().next();

				int comparison = hand.compareTo(highHand);
				if (comparison > 0) {
					highestHands.clear();
					highestHands.put(data, hand);
				} else if (comparison == 0) {
					highestHands.put(data, hand);
				}
			}
		}
		return highestHands;
	}

	private List<PlayerRoundData> getShownPlayerData() {
		return getPlayerData().stream().filter(PlayerRoundData::isShown).collect(Collectors.toList());
	}

	/*
	 * @Override protected void runOneIteration() throws Exception { Phase phase =
	 * phaseManager.get();
	 * 
	 * if (phase == Phase.NOT_STARTED) { phaseManager.run();
	 * 
	 * phase = phaseManager.get(); } else {
	 * 
	 * } }
	 */

	private void addCommunityCards(int communityCardCount) {
		for (int x = 0; x < communityCardCount; x++) {
			Optional<Card> optionalCard = deck.get();

			if (optionalCard.isPresent()) {
				communityCards.add(optionalCard.get());
			} else {
				// TODO: Error, out of cards.
			}
		}
	}

	private void burn(int burnCardCount) {
		for (int x = 0; x < burnCardCount; x++) {
			Optional<Card> optionalCard = deck.get();

			if (optionalCard.isPresent()) {
				burnCards.add(optionalCard.get());
			} else {
				// TODO: Error, out of cards.
			}
		}
	}

	@Override
	public void accept(Command command) {
		CommandType commandType = command.getCommandType();
		Phase phase = phaseManager.get();
		Player player = command.getPlayer();
		PlayerRoundData data = getPlayerDataByPlayer(player);
		if (phase == Phase.AWAITING_PLAYERS) {
			// TODO: Make sure the player has money
			if (commandType == CommandType.DEAL) {
				if (!(playerData.size() > 12)) {
					addPlayer(player);
				} else {
					// TODO: Error Message, can't have more players.
				}
			} else {
				// TODO: Error Message, can't have perform other commands here.
			}
		} else if (phase == Phase.SHOWDOWN) {
			if (commandType == CommandType.SHOW) {
				StringBuilder sb = new StringBuilder(data.get().getIrcName() + " shows ");
				for (Card card : data.getCards()) {
					sb.append(card);
				}
				generalMessage.onNext(sb.toString());
				showPlayer(player);
				if (getActivePlayers().isEmpty()) {
					showdown();
				}
			} else if (commandType == CommandType.FOLD) {
				foldPlayer(player);

				if (getActivePlayers().isEmpty()) {
					showdown();
				}
			} else {
				// TODO: Error Message, bad command.
			}
		} else {
			if (commandType == CommandType.FOLD) {
				foldPlayer(player);
			}
			if (phase.isBetPhase()) {
				if (commandType.isBetCommand()) {
					BettingRound bettingRoundValue = bettingRound.get();

					bettingRoundValue.handleCommand(command);
				} else {
					// TODO: handle non bet commands
				}
			}
			/*
			 * else if (phase.isBetPhase() || phase == Phase.SHOWDOWN) { if
			 * (phase.isBetPhase()) { if (player.equals(obj)) switch (commandType) {
			 * 
			 * }
			 * 
			 * } }
			 */

		}
	}

	private void showPlayer(Player player) {
		PlayerRoundData data = getPlayerDataByPlayer(player);

		data.setShown(true);
	}

	private void peekPlayer(PlayerRoundData data) {
		StringBuilder sb = new StringBuilder();
		for (Card card : data.getCards()) {
			sb.append(card);
		}
		playerMessage.onNext(new AbstractMap.SimpleEntry<>(data.get(), "Your cards: " + sb.toString()));
	}

	private void dealAllPlayersIn() {
		for (PlayerRoundData playerRoundData : playerData) {
			Optional<Card> optionalCard = deck.get();
			Optional<Card> optionalCard2 = deck.get();
			if (optionalCard.isPresent() && optionalCard2.isPresent()) {
				playerRoundData.deal(optionalCard.get(), optionalCard2.get());

				peekPlayer(playerRoundData);
			} else {
				// TODO: Error, we are out of cards.
			}
		}
	}

	private PlayerRoundData getPlayerDataByPlayer(Player player) {
		return playerData.stream().filter(x -> x.get().equals(player)).findFirst().orElse(null);
	}

	private void addPlayer(Player player) {
		if (!isPlayerParticipating(player)) {
			// TODO: Should the cards be shown to the player here?
			PlayerRoundData data = new PlayerRoundData(player);

			playerData.add(data);
		}
	}

	private void foldPlayer(Player player) {
		try {
			PlayerRoundData playerData = getPlayerDataByPlayer(player);

			if (playerData != null) {
				if (!playerData.isFolded()) {
					playerData.close();
				} else {
					// Do nothing, player is already folded.
				}
			} else {
				// Do nothing, can't find player.
			}
		} catch (Exception e) {
			// TODO: Log this or something.
		}
	}

	private boolean isPlayerParticipating(Player player) {
		return playerData.stream().map(x -> x.get()).map(x -> x.getIrcName())
				.anyMatch(x -> x.equals(player.getIrcName()));
	}
}
