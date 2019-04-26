package club.wodencafe.poker.holdem;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import club.wodencafe.data.Player;
import club.wodencafe.data.PlayerService;

public class PotManager {
	private static final XLogger logger = XLoggerFactory.getXLogger(PotManager.class);
	private List<Entry<PlayerRoundData, AtomicLong>> potFromPlayers = new ArrayList<>();
	private List<Command> previousCommands;
	private BettingRound bettingRound;

	public PotManager(BettingRound bettingRound) {
		logger.entry(bettingRound);
		try {
			this.previousCommands = bettingRound.getPreviousCommands();
			this.bettingRound = bettingRound;
			for (PlayerRoundData player : bettingRound.getPlayers()) {
				if (!player.isFolded()) {
					logger.info("Adding player " + player.get().getIrcName() + " to pot collection. Size is now "
							+ potFromPlayers.size() + ".");
					potFromPlayers.add(new AbstractMap.SimpleEntry<>(player, new AtomicLong(0)));
				} else {
					logger.warn("Player " + player.get().getIrcName() + " is already folded, can't add." + player);
				}
			}
		} catch (Throwable th) {
			logger.throwing(th);
			throw new RuntimeException(th);
		}
		logger.exit(this);
	}

	public boolean isPotSatisfied() {
		logger.entry();
		boolean returnValue = true;
		try {
			List<CommandType> previousCommandTypes = previousCommands.stream().map(Command::getCommandType)
					.collect(Collectors.toList());
			logger.trace("Previous command types: " + previousCommandTypes);

			int lastIndex = Math.max(Math.max(previousCommandTypes.lastIndexOf(CommandType.BET),
					previousCommandTypes.lastIndexOf(CommandType.RAISE)), 0);
			logger.trace("lastIndex of BET and RAISE: " + lastIndex);

			List<Player> previousCommandPlayers = previousCommands.subList(lastIndex, previousCommands.size()).stream()
					.map(Command::getPlayer).collect(Collectors.toList());
			logger.trace("Previous commands since last BET or RAISE: " + previousCommandPlayers);

			for (Player player : bettingRound.getActivePlayers()) {
				if (!previousCommandPlayers.contains(player)) {
					returnValue = false;
					logger.debug("Previous commands do not contain player " + player.getIrcName());
					break;
				}
			}

			if (returnValue) {
				Set<Long> values = new HashSet<Long>();
				for (Entry<PlayerRoundData, AtomicLong> playerData : potFromPlayers) {
					if (playerData.getKey().get().getMoney() > 0 && !playerData.getKey().isFolded()) {
						values.add(playerData.getValue().get());
						if (values.size() > 1) {
							returnValue = false;
							logger.debug("Pot is incorrect. " + values);
							break;
						}
					}
				}
			} else {
				logger.debug("Pot is already unsatisfied. Don't need to check the values.");
			}
		} catch (Throwable th) {
			logger.throwing(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(returnValue);
		}
		return returnValue;
	}

	public void check(PlayerRoundData player) {
		logger.entry(player);
		try {
			boolean foundPlayer = false;
			for (Entry<PlayerRoundData, AtomicLong> playerData : potFromPlayers) {
				if (Objects.equals(playerData.getKey(), player)) {
					// This isn't necessary.
					playerData.getValue().set(0);
					foundPlayer = true;
				}
			}
			if (!foundPlayer) {
				RuntimeException e = new RuntimeException("Player " + player.get() + " not found.");
				logger.throwing(e);
				throw e;
			}
		} finally {
			logger.exit();
		}
	}

	public void bet(PlayerRoundData player, long amount) {
		logger.entry(player, amount);
		try {
			for (Entry<PlayerRoundData, AtomicLong> playerData : potFromPlayers) {
				if (Objects.equals(playerData.getKey(), player)) {
					playerData.getKey().get().removeMoney(amount);

					PlayerService.save(playerData.getKey().get());

					playerData.getValue().set(amount);
					return;
				}
			}
			throw new NoSuchElementException("Player " + player.get() + " not found.");
		} catch (Throwable th) {
			RuntimeException e = new RuntimeException(th);
			logger.throwing(e);
			throw e;
		} finally {
			logger.exit();
		}
	}

	private long getLastBetCallOrRaiseAmount(Player player) {
		List<CommandType> previousCommandTypes = previousCommands.stream()
				.filter(x -> Objects.equals(x.getPlayer(), player)).map(Command::getCommandType)
				.collect(Collectors.toList());

		int lastIndex = Math.max(Math.max(
				Math.max(previousCommandTypes.lastIndexOf(CommandType.BET),
						previousCommandTypes.lastIndexOf(CommandType.RAISE)),
				previousCommandTypes.lastIndexOf(CommandType.CALL)), 0);
		if (lastIndex > -1) {
			Command command = previousCommands.stream().filter(x -> Objects.equals(x.getPlayer(), player))
					.collect(Collectors.toList()).get(lastIndex);
			if (command.getCommandType() == CommandType.CALL) {
				previousCommandTypes = previousCommands.stream().map(Command::getCommandType)
						.collect(Collectors.toList());
				lastIndex = Math.max(Math.max(previousCommandTypes.lastIndexOf(CommandType.BET),
						previousCommandTypes.lastIndexOf(CommandType.RAISE)), 0);
				command = previousCommands.get(lastIndex);

				return command.getData().get();

			} else {
				return command.getData().get();
			}
		} else {
			return lastIndex;
		}
	}

	public void call(PlayerRoundData player) {

		logger.entry(player);
		try {
			for (int x = 0; x < potFromPlayers.size(); x++) {
				Map.Entry<PlayerRoundData, AtomicLong> e = potFromPlayers.get(x);

				if (Objects.equals(e.getKey(), player)) {

					Map.Entry<PlayerRoundData, AtomicLong> prev;

					if (x > 0) {
						prev = potFromPlayers.get(x - 1);

					} else {
						prev = potFromPlayers.get(potFromPlayers.size() - 1);
					}

					long prevAmount = prev.getValue().get();

					e.getKey().get().removeMoney(prevAmount - e.getValue().get());

					PlayerService.save(e.getKey().get());

					e.getValue().set(prevAmount);

					return;
				}

			}

			throw new RuntimeException("Player " + player.get() + " not found.");
		} catch (Throwable th) {
			logger.throwing(th);
			throw th;
		} finally {
			logger.exit();
		}
	}

	public void raise(PlayerRoundData player, long amount) {
		logger.entry(player, amount);
		for (int x = 0; x < potFromPlayers.size(); x++) {
			Map.Entry<PlayerRoundData, AtomicLong> e = potFromPlayers.get(x);

			if (Objects.equals(e.getKey(), player)) {

				Map.Entry<PlayerRoundData, AtomicLong> prev;

				if (x > 0) {
					prev = potFromPlayers.get(x);

				} else {
					prev = potFromPlayers.get(potFromPlayers.size() - 1);
				}

				long prevAmount = prev.getValue().get();

				if (amount > prevAmount) {

					e.getKey().get().removeMoney(amount);

					PlayerService.save(e.getKey().get());

					e.getValue().set(amount);

					return;
				} else {
					throw new RuntimeException("Cannot raise to lower than previous amount.");
				}

			}

		}
		throw new RuntimeException("Player " + player.get() + " not found.");
	}

	public Long getTotalPot() {
		logger.entry();
		Long returnValue = -1L;
		try {
			returnValue = potFromPlayers.stream().map(x -> x.getValue().get()).mapToLong(Long::longValue).sum();
		} catch (Throwable th) {
			logger.throwing(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(returnValue);
		}
		return returnValue;
	}
}
