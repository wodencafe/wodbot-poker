package club.wodencafe.poker.holdem;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import club.wodencafe.data.Player;

public class PotManager {
	private List<Entry<PlayerRoundData, AtomicLong>> potFromPlayers = new ArrayList<>();
	private List<Command> previousCommands;
	private BettingRound bettingRound;

	public PotManager(BettingRound bettingRound) {
		this.previousCommands = bettingRound.getPreviousCommands();
		this.bettingRound = bettingRound;
		for (PlayerRoundData player : bettingRound.getPlayers()) {
			if (!player.isFolded()) {
				potFromPlayers.add(new AbstractMap.SimpleEntry<>(player, new AtomicLong(0)));
			}
		}
	}

	public boolean isPotSatisfied() {

		List<CommandType> previousCommandTypes = previousCommands.stream().map(Command::getCommandType)
				.collect(Collectors.toList());

		int lastIndex = Math.max(Math.max(previousCommandTypes.lastIndexOf(CommandType.BET),
				previousCommandTypes.lastIndexOf(CommandType.RAISE)), 0);

		List<Player> previousCommandPlayers = previousCommands.subList(lastIndex, previousCommands.size()).stream()
				.map(Command::getPlayer).collect(Collectors.toList());

		for (Player player : bettingRound.getActivePlayers()) {
			if (!previousCommandPlayers.contains(player)) {
				return false;
			}
		}

		Set<Long> values = new HashSet<Long>();
		for (Entry<PlayerRoundData, AtomicLong> playerData : potFromPlayers) {
			if (playerData.getKey().get().getMoney() > 0 && !playerData.getKey().isFolded()) {
				values.add(playerData.getValue().get());
				if (values.size() > 1) {
					return false;
				}
			}
		}
		return true;
	}

	public void check(PlayerRoundData player) {
		for (Entry<PlayerRoundData, AtomicLong> playerData : potFromPlayers) {
			if (Objects.equals(playerData.getKey(), player)) {
				// This isn't necessary.
				playerData.getValue().set(0);
				return;
			}
		}
		throw new RuntimeException("Player " + player.get() + " not found.");
	}

	public void bet(PlayerRoundData player, long amount) {
		for (Entry<PlayerRoundData, AtomicLong> playerData : potFromPlayers) {
			if (Objects.equals(playerData.getKey(), player)) {
				playerData.getKey().get().removeMoney(amount);

				playerData.getValue().set(amount);
				return;
			}
		}
		throw new RuntimeException("Player " + player.get() + " not found.");
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

				e.getValue().set(prevAmount);

				return;
			}

		}

		throw new RuntimeException("Player " + player.get() + " not found.");
	}

	public void raise(PlayerRoundData player, long amount) {
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
		return potFromPlayers.stream().map(x -> x.getValue().get()).mapToLong(Long::longValue).sum();
	}
}
