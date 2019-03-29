package club.wodencafe.poker.holdem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.common.util.concurrent.AbstractScheduledService;

import club.wodencafe.bot.WodData;
import club.wodencafe.data.Player;
import club.wodencafe.poker.cards.Card;
import club.wodencafe.poker.cards.Deck;
import io.reactivex.subjects.PublishSubject;

/**
 * This class represents a single hand of poker.
 * 
 * @author wodencafe
 */
public class RoundMediator implements AutoCloseable, Consumer<Command> {
	private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	private List<PlayerRoundData> players = new ArrayList<>();

	private PhaseManager phaseManager;

	private PublishSubject<String> generalMessage = PublishSubject.create();

	private PublishSubject<Map.Entry<Player, String>> playerMessage = PublishSubject.create();

	private Deck deck;

	private Optional<BettingRound> bettingRound = Optional.empty();

	private List<Card> communityCards = new ArrayList<>();

	// Keep track of these just in case
	private List<Card> burnCards = new ArrayList<>();

	public List<PlayerRoundData> getPlayers() {
		return players;
	}

	private RoundMediator(Player initialPlayer) {
		phaseManager = new PhaseManager();

		phaseManager.onNewPhase().subscribe(this::handleNewPhase);

		deck = Deck.generateDeck(false);

		addPlayer(initialPlayer);
	}

	public PhaseManager getPhaseManager() {
		return phaseManager;
	}

	@Override
	public void close() throws Exception {
		// TODO: Split the pot up here
	}

	private void handleNewPhase(Phase newPhase) {
		
		if (newPhase == Phase.AWAITING_PLAYERS) {
			generalMessage.onNext("Awaiting players, type " + WodData.commandChar + CommandType.DEAL.getCommandName()
					+ " to be dealt into the match");

		}
		if (newPhase == Phase.HOLE) {

			generalMessage.onNext("Dealing cards to " + players.size() + " players.");

			dealAllPlayersIn();
		} else if (newPhase == Phase.FLOP) {

			burn(1);
			addCommunityCards(3);

			String message = String.valueOf(communityCards.get(0)) + String.valueOf(communityCards.get(1))
					+ String.valueOf(communityCards.get(2)) + " on the flop. Remaining players: " + players.size();

			generalMessage.onNext(message);
		} else if (newPhase == Phase.TURN) {
			burn(1);
			addCommunityCards(1);

			String message = String.valueOf(communityCards.get(0)) + String.valueOf(communityCards.get(1))
					+ String.valueOf(communityCards.get(2)) + String.valueOf(communityCards.get(3))
					+ " with the turn card. Remaining players: " + players.size();

			generalMessage.onNext(message);

		} else if (newPhase == Phase.RIVER) {
			burn(1);
			addCommunityCards(1);

			String message = String.valueOf(communityCards.get(0)) + String.valueOf(communityCards.get(1))
					+ String.valueOf(communityCards.get(2)) + String.valueOf(communityCards.get(3))
					+ String.valueOf(communityCards.get(4)) + " with the river. Remaining players: " + players.size();

			generalMessage.onNext(message);
		}
		if (newPhase == Phase.SHOWDOWN) {

			StringBuilder sb = new StringBuilder("Showdown: ");
			for (PlayerRoundData player : players) {
				sb.append(player.get() + ", ");
			}
			sb.append("you can " + WodData.commandChar + "show or " + WodData.commandChar + " fold");
			generalMessage.onNext(sb.toString());
		}
	}

	/*@Override
	protected void runOneIteration() throws Exception {
		Phase phase = phaseManager.get();

		if (phase == Phase.NOT_STARTED) {
			phaseManager.run();

			phase = phaseManager.get();
		} else {

		}
	}*/

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
				if (!(players.size() > 12)) {
					addPlayer(player);
				} else {
					// TODO: Error Message, can't have more players.
				}
			} else {
				// TODO: Error Message, can't have perform other commands here.
			}
		} else if (phase == Phase.SHOWDOWN) {
			if (commandType == CommandType.SHOW) {
				generalMessage.onNext(data.toString());
				showPlayer(player);
			} else if (commandType == CommandType.FOLD) {
				foldPlayer(player);
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
		
		
	}
	private void dealAllPlayersIn() {
		for (PlayerRoundData playerRoundData : players) {
			Optional<Card> optionalCard = deck.get();
			Optional<Card> optionalCard2 = deck.get();
			if (optionalCard.isPresent() && optionalCard2.isPresent()) {
				playerRoundData.deal(optionalCard.get(), optionalCard2.get());
			} else {
				// TODO: Error, we are out of cards.
			}
		}
	}

	private PlayerRoundData getPlayerDataByPlayer(Player player) {
		return players.stream().filter(x -> x.get().equals(player)).findFirst().orElse(null);
	}

	private void addPlayer(Player player) {
		if (!isPlayerParticipating(player)) {
			// TODO: Should the cards be shown to the player here?
			PlayerRoundData data = new PlayerRoundData(player);

			players.add(data);
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
		return players.stream().map(x -> x.get()).map(x -> x.getIrcName()).anyMatch(x -> x.equals(player.getIrcName()));
	}
}
