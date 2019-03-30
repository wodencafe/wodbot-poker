package club.wodencafe.poker.holdem;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractIdleService;

import club.wodencafe.bot.WodData;
import club.wodencafe.data.Player;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

/**
 * This class should be instantiated for each round of betting, and will handle
 * the commands related to betting.
 * 
 * @author wodencafe
 *
 */
public class BettingRound extends AbstractIdleService implements AutoCloseable {
	private static Logger logger = LoggerFactory.getLogger(BettingRound.class);

	private boolean closed = false;

	private ScheduledFuture<?> future = null;

	private ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

	private List<PlayerRoundData> players;

	private PotManager potManager;

	private List<Command> previousCommands = new ArrayList<>();

	private PublishSubject<PlayerRoundData> playerAutoFold = PublishSubject.create();

	private PublishSubject<Map.Entry<Player, Command>> playerCommand = PublishSubject.create();

	private PublishSubject<PlayerRoundData> playerNewTurn = PublishSubject.create();

	private ReplaySubject<Long> bettingRoundComplete = ReplaySubject.create();

	private int betTimeout;

	List<Command> getPreviousCommands() {
		return previousCommands;
	}

	public Observable<Long> onBettingRoundComplete() {
		return bettingRoundComplete;
	}

	public Observable<PlayerRoundData> onPlayerNewTurn() {
		return playerNewTurn;
	}

	public Observable<Map.Entry<Player, Command>> onPlayerCommand() {
		return playerCommand;
	}

	public BettingRound(List<PlayerRoundData> players, int betTimeout) {
		this.players = players;

		this.betTimeout = betTimeout;

		potManager = new PotManager(this);
	}

	public BettingRound(List<PlayerRoundData> players) {
		this(players, WodData.betTimeout);
	}

	private List<Command> getPreviousCommandsWithoutFolds() {
		List<Command> previousCommandsWithoutFolds = previousCommands.stream()
				.filter(x -> x.getCommandType() != CommandType.FOLD).collect(Collectors.toList());
		return previousCommandsWithoutFolds;
	}

	public Map.Entry<Player, Player> getCurrentAndPreviousPlayer() {

		List<Command> previousCommandsWithoutFolds = getPreviousCommandsWithoutFolds();

		List<Player> playersWithoutFolds = players.stream().filter(x -> !x.isFolded()).map(x -> x.get())
				.collect(Collectors.toList());

		if (previousCommandsWithoutFolds.isEmpty()) {
			return new AbstractMap.SimpleEntry<>(players.iterator().next().get(), null);
		} else {

			Player previousPlayer = previousCommandsWithoutFolds.get(previousCommandsWithoutFolds.size() - 1)
					.getPlayer();

			Player currentPlayer;

			int previousPlayerIndex = playersWithoutFolds.indexOf(previousPlayer);

			// If this is the last player in the list
			if (previousPlayerIndex == (playersWithoutFolds.size() - 1)) {
				currentPlayer = playersWithoutFolds.get(0);
			} else {
				currentPlayer = playersWithoutFolds.get(previousPlayerIndex + 1);
			}

			return new AbstractMap.SimpleEntry<>(currentPlayer, previousPlayer);
		}
	}

	public void handleCommand(Command command) {

		if (Objects.equals(command.getPlayer(), getCurrentAndPreviousPlayer().getKey())) {
			CommandType commandType = command.getCommandType();

			List<Command> previousCommandsWithoutFolds = getPreviousCommandsWithoutFolds();

			if (commandType == CommandType.FOLD) {
				fold(command.getPlayer());
			} else if (previousCommandsWithoutFolds.isEmpty()) {
				// Applicable commands are Check, Bet, (Fold)
				if (commandType == CommandType.CHECK) {
					check(command.getPlayer());
				} else if (commandType == CommandType.BET) {
					bet(command.getPlayer(), command.getData().get());
				}

			} else {
				Command previousCommand = previousCommandsWithoutFolds.get(previousCommandsWithoutFolds.size() - 1);

				CommandType previousCommandType = previousCommand.getCommandType();

				switch (previousCommandType) {
				case CHECK: {
					// Applicable commands are Check, Bet, (Fold)
					if (commandType == CommandType.CHECK) {
						check(command.getPlayer());
					} else if (commandType == CommandType.BET) {
						bet(command.getPlayer(), command.getData().get());
					}
				}
					break;
				case RAISE:
				case BET: {
					// Applicable commands are Call, Raise, (Fold)
					if (commandType == CommandType.CALL) {
						call(command.getPlayer());
					} else if (commandType == CommandType.RAISE) {
						raise(command.getPlayer(), command.getData().get());
					}
				}
					break;
				case CALL: {
					if (commandType == CommandType.CALL) {
						call(command.getPlayer());
					} else if (commandType == CommandType.RAISE) {
						raise(command.getPlayer(), command.getData().get());
					}
				}
					break;
				}
			}
		}
	}

	private void turnComplete() {
		cancelCurrentTurn();
		if (isRoundComplete()) {
			stopAsync();
		} else {
			newTurn();
		}
	}

	private void cancelCurrentTurn() {
		if (future != null && !future.isCancelled() && !future.isDone()) {
			future.cancel(true);
		}
	}

	private void timerExpired(PlayerRoundData data) {
		fold(data.get());
		playerAutoFold.onNext(data);
		turnComplete();
	}

	private void newTurn() {
		Player player = getCurrentAndPreviousPlayer().getKey();
		PlayerRoundData data = getPlayerRoundData(player);
		playerNewTurn.onNext(data);
		future = service.schedule(() -> timerExpired(data), betTimeout, TimeUnit.SECONDS);
	}

	private PlayerRoundData getCurrentPlayerRoundData(Player player) {
		for (PlayerRoundData data : players) {
			logger.trace("Comparing " + data.get() + " with " + player);
			if (data.get() == player) {
				logger.trace("Match between " + data.get() + " and " + player);
				return data;
			}
		}
		return null;
	}

	private PlayerRoundData getPlayerRoundData(Player player) {
		logger.trace("getPlayerRoundData(" + player.toString() + ")");
		PlayerRoundData data = getCurrentPlayerRoundData(player);
		logger.trace("getPlayerRoundData(" + player.toString() + ") returning " + data);
		return data;
	}

	private void fold(Player player) {
		Command foldCommand = new Command(CommandType.FOLD, player);

		previousCommands.add(foldCommand);

		getPlayerRoundData(player).fold();

		logger.debug(player.getIrcName() + " folds.");

		playerCommand.onNext(new AbstractMap.SimpleEntry<>(player, foldCommand));

		turnComplete();
	}

	private void check(Player player) {
		Command checkCommand = new Command(CommandType.CHECK, player);

		previousCommands.add(checkCommand);

		potManager.check(getPlayerRoundData(player));

		logger.debug(player.getIrcName() + " checks.");

		playerCommand.onNext(new AbstractMap.SimpleEntry<>(player, checkCommand));

		turnComplete();
	}

	private void bet(final Player player, long amount) {
		Command betCommand = new Command(CommandType.BET, amount, player);

		previousCommands.add(betCommand);

		PlayerRoundData data = getPlayerRoundData(player);

		potManager.bet(data, amount);

		logger.debug(player.getIrcName() + " bets " + amount + ".");

		playerCommand.onNext(new AbstractMap.SimpleEntry<>(player, betCommand));

		turnComplete();
	}

	private void call(Player player) {
		Command callCommand = new Command(CommandType.CALL, player);

		previousCommands.add(callCommand);

		potManager.call(getPlayerRoundData(player));

		logger.debug(player.getIrcName() + " calls.");

		playerCommand.onNext(new AbstractMap.SimpleEntry<>(player, callCommand));

		turnComplete();
	}

	private void raise(Player player, long amount) {
		Command raiseCommand = new Command(CommandType.RAISE, amount, player);

		previousCommands.add(raiseCommand);

		potManager.raise(getPlayerRoundData(player), amount);

		logger.debug(player.getIrcName() + " raises to " + amount + ".");

		playerCommand.onNext(new AbstractMap.SimpleEntry<>(player, raiseCommand));

		turnComplete();
	}

	public List<PlayerRoundData> getPlayers() {
		return players;
	}

	public List<Player> getActivePlayers() {
		return players.stream().filter(x -> !x.isFolded()).filter(x -> !x.isShown()).map(x -> x.get())
				.collect(Collectors.toList());
	}

	private List<Player> getPlayersFromCommands() {
		return previousCommands.stream().map(x -> x.getPlayer()).collect(Collectors.toList());
	}

	public boolean isRoundComplete() {
		return ((getPlayersFromCommands().containsAll(getActivePlayers()) && potManager.isPotSatisfied())
				|| getActivePlayers().size() == 1);
	}

	@Override
	protected void startUp() throws Exception {
		newTurn();
	}

	@Override
	protected void shutDown() throws Exception {

		close();
	}

	@Override
	public void close() throws Exception {
		if (!closed) {
			closed = true;

			cancelCurrentTurn();

			if (!service.isShutdown()) {
				service.shutdown();
			}

			if (isRunning()) {
				stopAsync();
			}

			playerCommand.onComplete();

			bettingRoundComplete.onNext(potManager.getTotalPot());

			bettingRoundComplete.onComplete();

			playerAutoFold.onComplete();

			playerNewTurn.onComplete();

		}
	}

}
