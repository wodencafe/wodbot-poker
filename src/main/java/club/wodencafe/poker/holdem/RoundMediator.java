package club.wodencafe.poker.holdem;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import club.wodencafe.bot.WodData;
import club.wodencafe.data.Player;
import club.wodencafe.data.PlayerService;
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
public class RoundMediator implements AutoCloseable, Predicate<Command> {
	private ScheduledFuture<?> future = null;
	private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	private List<PlayerRoundData> playerData = new ArrayList<>();

	private PhaseManager phaseManager;

	private PublishSubject<String> generalMessage = PublishSubject.create();

	private PublishSubject<Map.Entry<Player, String>> playerMessage = PublishSubject.create();

	private Deck deck;

	private Optional<BettingRound> bettingRound = Optional.empty();

	private ReplaySubject<Map<PlayerRoundData, Long>> roundComplete = ReplaySubject.create();

	private List<Card> communityCards = new ArrayList<>();

	private AtomicLong pot = new AtomicLong();

	private static final XLogger logger = XLoggerFactory.getXLogger(RoundMediator.class);

	public long getCurrentPotSize() {
		logger.entry();
		long potSize = -1;
		try {
			potSize = pot.get();

			if (bettingRound.isPresent()) {
				potSize += bettingRound.get().getPotSize();
			}
			return potSize;
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(potSize);
		}
	}

	public Optional<BettingRound> getBettingRound() {
		return bettingRound;
	}

	// Keep track of these just in case
	private List<Card> burnCards = new ArrayList<>();

	public List<PlayerRoundData> getPlayerData() {
		return playerData;
	}

	public List<Player> getPlayers() {
		return playerData.stream().map(x -> x.get()).collect(Collectors.toList());
	}

	public Observable<Map<PlayerRoundData, Long>> onRoundComplete() {
		return roundComplete;
	}

	public Observable<String> onGeneralMessage() {
		return generalMessage;
	}

	public Observable<Map.Entry<Player, String>> onPlayerMessage() {
		return playerMessage;
	}

	private void cancelFuture() {
		logger.entry();
		try {
			if (future != null && !future.isCancelled() && !future.isDone()) {
				future.cancel(true);
			}
		} catch (Throwable th) {
			logger.catching(th);
			throw th;
		} finally {
			logger.exit();
		}
	}

	public RoundMediator(Player initialPlayer) {
		logger.entry(initialPlayer);
		try {
			phaseManager = new PhaseManager();

			phaseManager.onNewPhase().subscribe(this::handleNewPhase, (e) ->
			{
				e.printStackTrace(System.err);
				System.out.println(e);
			});

			deck = Deck.generateDeck(false);

			phaseManager.run();

			PlayerService.save(initialPlayer);

			addPlayer(initialPlayer);
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	public PhaseManager getPhaseManager() {
		logger.entry();
		try {
			return phaseManager;
		} finally {
			logger.exit(phaseManager);
		}
	}

	private boolean close = false;

	@Override
	public void close() throws Exception {
		logger.entry();
		try {
			if (!close) {
				close = true;
				cancelFuture();

				service.shutdown();

				roundComplete.onComplete();

				generalMessage.onComplete();

				playerMessage.onComplete();

				if (bettingRound.isPresent()) {
					bettingRound.get().close();
				}
			} else {
				logger.warn("Already closed.");
			}
		} catch (Exception e) {
			logger.catching(e);
			throw e;
		} finally {
			logger.exit();
		}
	}

	public List<Player> getActivePlayers() {
		logger.entry();
		List<Player> players = null;
		try {
			players = playerData.stream().filter(x -> !x.isFolded()).filter(x -> !x.isShown()).map(x -> x.get())
					.collect(Collectors.toList());
			return players;
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(players);
		}

	}

	private void complete(Map<PlayerRoundData, Long> winners) {

		logger.entry(winners);
		try {
			StringBuilder sb = new StringBuilder("Game is complete!");
			for (Entry<PlayerRoundData, Long> entry : winners.entrySet()) {
				Player player = entry.getKey().get();

				player.addMoney(entry.getValue());

				PlayerService.save(player);
				sb.append(" Winner: " + player.getIrcName() + ", pot: $" + entry.getValue());
			}
			generalMessage.onNext(sb.toString());
			roundComplete.onNext(winners);
			close();
		} catch (Throwable th) {

			logger.catching(th);
			logger.error("Unable to complete RoundMediator", th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	private void complete(PlayerRoundData winner, Long winnings) {
		logger.entry(winner, winnings);
		try {
			Map<PlayerRoundData, Long> winners = new HashMap<>();
			winners.put(winner, winnings);
			complete(winners);
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	private void handleNewPhase(Phase newPhase) {

		logger.entry(newPhase);
		try {
			cancelFuture();

			StringBuilder message = new StringBuilder();

			if (getActivePlayers().size() == 1) {
				PlayerRoundData data = getPlayerDataByPlayer(getActivePlayers().iterator().next());

				complete(data, getCurrentPotSize());
			} else {
				if (newPhase == Phase.AWAITING_PLAYERS) {

					future = service.schedule(() -> phaseManager.run(), WodData.joinTimeout, TimeUnit.SECONDS);

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
								+ (!data.getValue().getData().isPresent() ? "."
										: ((data.getValue().getCommandType() == CommandType.RAISE ? " to " : " ") + "$"
												+ String.valueOf(data.getValue().getData().get())) + "."));
						long potSize = getCurrentPotSize();

						if (potSize > 0) {
							sb.append(" Current pot size is $" + potSize + ".");
						}

						Entry<Player, Player> currentAndPreviousPlayer = bettingRoundValue
								.getCurrentAndPreviousPlayer();

						Player currentPlayer = currentAndPreviousPlayer.getKey();
						if (currentPlayer != null) {
							sb.append(" " + currentAndPreviousPlayer.getKey().getIrcName() + "'s turn.");
						} else {
							sb.append(" " + WordUtils.capitalizeFully(newPhase.toString()) + " round complete.");
						}

						generalMessage.onNext(sb.toString());
					});
					bettingRoundValue.onPlayerNewTurn()
							.subscribe((e) -> generalMessage.onNext(e.get().getIrcName() + "'s turn."));
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
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
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
		logger.entry();
		try {
			cancelFuture();

			StringBuilder sb = new StringBuilder();

			Map<PlayerRoundData, Hand> highestHands = getHighHands();

			Set<PlayerRoundData> playerRoundData = highestHands.keySet();

			Map<PlayerRoundData, Long> winningPlayers = new HashMap<>();
			if (playerRoundData.size() == 1) {
				PlayerRoundData winningPlayerData = playerRoundData.iterator().next();

				sb.append("Winner: " + winningPlayerData.get().getIrcName() + " with "
						+ highestHands.get(winningPlayerData) + ", winning $" + getCurrentPotSize() + ".");

				winningPlayers.put(winningPlayerData, getCurrentPotSize());
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
					winningPlayers.put(winningPlayerData, potSplit);
				}

			}

			if (sb.length() > 0) {
				generalMessage.onNext(sb.toString());
			}

			complete(winningPlayers);
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	private Map<PlayerRoundData, Hand> getHighHands() {

		logger.entry();
		Map<PlayerRoundData, Hand> highestHands = new HashMap<>();

		try {
			List<PlayerRoundData> shownPlayers = getShownPlayerData();

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
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(highestHands);
		}
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
				NoSuchElementException nsee = new NoSuchElementException("Out of cards.");
				throw new RuntimeException(nsee);
			}
		}
	}

	private void burn(int burnCardCount) {
		for (int x = 0; x < burnCardCount; x++) {
			Optional<Card> optionalCard = deck.get();

			if (optionalCard.isPresent()) {
				burnCards.add(optionalCard.get());
			} else {
				NoSuchElementException nsee = new NoSuchElementException("Out of cards.");
				throw new RuntimeException(nsee);
			}
		}
	}

	private void showPlayer(Player player) {
		logger.entry(player);
		try {
			PlayerRoundData data = getPlayerDataByPlayer(player);

			data.setShown(true);
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	private void peekPlayer(PlayerRoundData data) {
		StringBuilder sb = new StringBuilder();
		for (Card card : data.getCards()) {
			sb.append(card);
		}
		playerMessage.onNext(new AbstractMap.SimpleEntry<>(data.get(), "Your cards: " + sb.toString()));
	}

	private void dealAllPlayersIn() {
		logger.entry();
		try {
			for (PlayerRoundData playerRoundData : playerData) {
				Optional<Card> optionalCard = deck.get();
				Optional<Card> optionalCard2 = deck.get();
				if (optionalCard.isPresent() && optionalCard2.isPresent()) {
					playerRoundData.deal(optionalCard.get(), optionalCard2.get());

					peekPlayer(playerRoundData);
				} else {
					NoSuchElementException nsee = new NoSuchElementException("Out of cards.");
					throw new RuntimeException(nsee);
				}
			}
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	private PlayerRoundData getPlayerDataByPlayer(Player player) {
		return playerData.stream().filter(x -> x.get().equals(player)).findFirst().orElse(null);
	}

	private void addPlayer(Player player) {
		logger.entry(player);
		try {
			PlayerService.save(player);

			if (!isPlayerParticipating(player)) {
				// TODO: Should the cards be shown to the player here?
				PlayerRoundData data = new PlayerRoundData(player);

				playerData.add(data);

				generalMessage.onNext(player.getIrcName() + " you have been added to the game.");
			} else {

				generalMessage.onNext(player.getIrcName() + " you are already in the game.");
			}
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
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
			logger.error("Unable to fold player.", e);
		}
	}

	private boolean isPlayerParticipating(Player player) {
		return playerData.stream().map(x -> x.get()).map(x -> x.getIrcName())
				.anyMatch(x -> x.equals(player.getIrcName()));
	}

	@Override
	public boolean test(Command command) {
		logger.entry(command);
		boolean returnValue = false;
		try {
			CommandType commandType = command.getCommandType();
			Phase phase = phaseManager.get();
			Player player = command.getPlayer();
			PlayerRoundData data = getPlayerDataByPlayer(player);
			if (phase == Phase.AWAITING_PLAYERS) {
				// TODO: Make sure the player has money
				if (commandType == CommandType.DEAL) {
					if (!(playerData.size() > 12)) {
						addPlayer(player);
						returnValue = true;
					} else {
						generalMessage.onNext(player.getIrcName() + " can't be added, too many players.");
					}
				} else {
					generalMessage.onNext(player.getIrcName() + " command invalid.");
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
					returnValue = true;
				} else if (commandType == CommandType.FOLD) {
					foldPlayer(player);

					if (getActivePlayers().isEmpty()) {
						showdown();
					}
					returnValue = true;
				} else {
					// TODO: Throw exception here?
					generalMessage.onNext(player.getIrcName() + " can't process this command here.");
				}
			} else {
				if (commandType == CommandType.FOLD) {
					foldPlayer(player);
					returnValue = true;
				}
				if (phase.isBetPhase()) {
					if (commandType.isBetCommand()) {
						BettingRound bettingRoundValue = bettingRound.get();

						bettingRoundValue.handleCommand(command);
					} else {
						// TODO: handle non bet commands
					}
					returnValue = true;
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
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(returnValue);
		}
		return returnValue;

	}
}
