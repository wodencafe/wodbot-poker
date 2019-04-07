package club.wodencafe.poker.holdem;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

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

	private static final XLogger logger = XLoggerFactory.getXLogger(BettingRound.class);

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

	public long getPotSize() {
		logger.entry();
		long potSize = -1;
		try {
			potSize = potManager.getTotalPot();
			return potSize;
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(potSize);
		}
	}

	List<Command> getPreviousCommands() {
		logger.entry();
		try {
			return previousCommands;
		} finally {
			logger.exit(previousCommands);
		}
	}

	public Observable<Long> onBettingRoundComplete() {
		logger.entry();
		try {
			return bettingRoundComplete;
		} finally {
			logger.exit(bettingRoundComplete);
		}
	}

	public Observable<PlayerRoundData> onPlayerNewTurn() {
		logger.entry();
		try {
			return playerNewTurn;
		} finally {
			logger.exit(playerNewTurn);
		}
	}

	public Observable<Map.Entry<Player, Command>> onPlayerCommand() {
		logger.entry();
		try {
			return playerCommand;
		} finally {
			logger.exit(playerCommand);
		}
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

		logger.entry();
		List<Command> previousCommandsWithoutFolds = null;

		try {
			previousCommandsWithoutFolds = previousCommands.stream().filter(x -> x.getCommandType() != CommandType.FOLD)
					.collect(Collectors.toList());
			return previousCommandsWithoutFolds;
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(previousCommandsWithoutFolds);
		}
	}

	public Map.Entry<Player, Player> getCurrentAndPreviousPlayer() {

		logger.entry();

		Map.Entry<Player, Player> entry = null;
		try {

			logger.info("Get all the previous commands without folds.");
			List<Command> previousCommandsWithoutFolds = getPreviousCommandsWithoutFolds();
			logger.info("Got all the previous commands without folds: ", System.lineSeparator(),
					previousCommandsWithoutFolds);
			List<Player> playersWithoutFolds = players.stream().filter(x -> !x.isFolded()).map(x -> x.get())
					.collect(Collectors.toList());

			if (previousCommandsWithoutFolds.isEmpty()) {
				entry = new AbstractMap.SimpleEntry<>(players.iterator().next().get(), null);
			} else {

				Player previousPlayer = previousCommandsWithoutFolds.get(previousCommandsWithoutFolds.size() - 1)
						.getPlayer();

				logger.debug("It appears the previous player is", previousPlayer);

				if (isRoundComplete()) {
					logger.debug("Round is complete. There is no current player.");
					entry = new AbstractMap.SimpleEntry<>(null, previousPlayer);
				} else {
					logger.debug("Round is not complete.");
					Player currentPlayer;

					int previousPlayerIndex = playersWithoutFolds.indexOf(previousPlayer);

					// If this is the last player in the list
					if (previousPlayerIndex == (playersWithoutFolds.size() - 1)) {
						currentPlayer = playersWithoutFolds.get(0);
					} else {
						currentPlayer = playersWithoutFolds.get(previousPlayerIndex + 1);
					}

					logger.debug("It appears the current player is", currentPlayer);

					entry = new AbstractMap.SimpleEntry<>(currentPlayer, previousPlayer);
				}
			}
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(entry);
		}
		return entry;
	}

	public void handleCommand(Command command) {

		logger.entry(command);
		try {
			boolean found = false;
			if (Objects.equals(command.getPlayer(), getCurrentAndPreviousPlayer().getKey())) {
				found = true;
				CommandType commandType = command.getCommandType();

				logger.debug("commandType is: " + commandType.toString());
				List<Command> previousCommandsWithoutFolds = getPreviousCommandsWithoutFolds();

				logger.debug("previousCommandsWithoutFolds size is: " + previousCommandsWithoutFolds.size());

				if (commandType == CommandType.FOLD) {
					logger.debug("Folding.");
					fold(command.getPlayer());
				} else if (previousCommandsWithoutFolds.isEmpty()) {
					logger.debug("No previous commands.");
					// Applicable commands are Check, Bet, (Fold)
					if (commandType == CommandType.CHECK) {
						logger.debug("Checking.");
						check(command.getPlayer());
					} else if (commandType == CommandType.BET) {
						logger.debug("Betting.");
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
			if (!found) {
				logger.error("Can't find player for command.");
				throw new NoSuchElementException();
			}
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	private void turnComplete() {
		logger.entry();
		try {
			cancelCurrentTurn();
			if (isRoundComplete()) {
				stopAsync();
			} else {
				newTurn();
			}
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	private void cancelCurrentTurn() {
		logger.entry();
		try {
			if (future != null && !future.isCancelled() && !future.isDone()) {
				logger.info("Cancelling future", future);
				future.cancel(true);
			}
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	private void timerExpired(PlayerRoundData data) {
		logger.entry(data);
		try {
			fold(data.get());
			playerAutoFold.onNext(data);
			turnComplete();
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	private void newTurn() {
		logger.entry();
		try {
			Player player = getCurrentAndPreviousPlayer().getKey();
			PlayerRoundData data = getPlayerRoundData(player);
			playerNewTurn.onNext(data);
			logger.debug("Scheduling timer expired in " + betTimeout + " seconds.", player);
			future = service.schedule(() -> timerExpired(data), betTimeout, TimeUnit.SECONDS);
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	private PlayerRoundData getCurrentPlayerRoundData(Player player) {
		logger.entry(player);
		PlayerRoundData returnData = null;
		try {
			for (PlayerRoundData data : players) {
				logger.trace("Comparing " + data.get() + " with " + player);
				if (data.get() == player) {
					logger.trace("Match between " + data.get() + " and " + player);
					returnData = data;
					break;
				}
			}
			if (returnData == null) {
				NoSuchElementException e = new NoSuchElementException("Could not find data for Player " + player);
				logger.error(e.getMessage(), e);
				throw e;
			}
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(returnData);
		}
		return returnData;
	}

	private PlayerRoundData getPlayerRoundData(Player player) {
		logger.trace("getPlayerRoundData(" + player.toString() + ")");
		PlayerRoundData data = getCurrentPlayerRoundData(player);
		logger.trace("getPlayerRoundData(" + player.toString() + ") returning " + data);
		return data;
	}

	private void fold(Player player) {
		logger.entry(player);
		try {
			Command foldCommand = new Command(CommandType.FOLD, player);

			previousCommands.add(foldCommand);

			getPlayerRoundData(player).fold();

			logger.debug(player.getIrcName() + " folds.");

			playerCommand.onNext(new AbstractMap.SimpleEntry<>(player, foldCommand));

			turnComplete();
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	private void check(Player player) {
		logger.entry(player);
		try {
			Command checkCommand = new Command(CommandType.CHECK, player);

			previousCommands.add(checkCommand);

			potManager.check(getPlayerRoundData(player));

			logger.debug(player.getIrcName() + " checks.");

			playerCommand.onNext(new AbstractMap.SimpleEntry<>(player, checkCommand));

			turnComplete();
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	private void bet(final Player player, long amount) {
		logger.entry(player, amount);
		try {
			Command betCommand = new Command(CommandType.BET, amount, player);

			previousCommands.add(betCommand);

			PlayerRoundData data = getPlayerRoundData(player);

			potManager.bet(data, amount);

			logger.debug(player.getIrcName() + " bets " + amount + ".");

			playerCommand.onNext(new AbstractMap.SimpleEntry<>(player, betCommand));

			turnComplete();
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	private void call(Player player) {
		logger.entry(player);
		try {
			Command callCommand = new Command(CommandType.CALL, player);

			previousCommands.add(callCommand);

			potManager.call(getPlayerRoundData(player));

			logger.debug(player.getIrcName() + " calls.");

			playerCommand.onNext(new AbstractMap.SimpleEntry<>(player, callCommand));

			turnComplete();
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	private void raise(Player player, long amount) {
		logger.entry(player, amount);
		try {
			Command raiseCommand = new Command(CommandType.RAISE, amount, player);

			previousCommands.add(raiseCommand);

			potManager.raise(getPlayerRoundData(player), amount);

			logger.debug(player.getIrcName() + " raises to " + amount + ".");

			playerCommand.onNext(new AbstractMap.SimpleEntry<>(player, raiseCommand));

			turnComplete();
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	public List<PlayerRoundData> getPlayers() {
		return players;
	}

	public List<Player> getActivePlayers() {
		logger.entry();
		List<Player> activePlayers = null;
		try {
			activePlayers = players.stream().filter(x -> !x.isFolded()).filter(x -> !x.isShown()).map(x -> x.get())
					.collect(Collectors.toList());
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(activePlayers);
		}
		return activePlayers;
	}

	private List<Player> getPlayersFromCommands() {
		logger.entry();
		List<Player> players = null;
		try {
			players = previousCommands.stream().map(x -> x.getPlayer()).collect(Collectors.toList());

		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(players);
		}
		return players;
	}

	public boolean isRoundComplete() {
		logger.entry();
		boolean returnValue = false;
		try {
			returnValue = ((getPlayersFromCommands().containsAll(getActivePlayers()) && potManager.isPotSatisfied())
					|| getActivePlayers().size() == 1);
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(returnValue);
		}
		return returnValue;
	}

	@Override
	protected void startUp() throws Exception {
		logger.entry();
		try {
			newTurn();
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	@Override
	protected void shutDown() throws Exception {
		logger.entry();
		try {
			close();
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	@Override
	public void close() throws Exception {
		logger.entry();
		try {
			if (!closed) {
				logger.trace("Closing.");
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

			} else {
				logger.warn("Already closed.");
			}
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

}
