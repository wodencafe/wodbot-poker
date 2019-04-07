package club.wodencafe.poker.holdem;

import java.util.Optional;

import club.wodencafe.data.Player;

public class Command {
	private CommandType commandType;
	private Optional<Long> data;
	private Player player;

	public Command(CommandType commandType, long data, Player player) {
		super();
		this.commandType = commandType;
		this.data = Optional.of(data);
		this.player = player;
	}

	public Command(CommandType commandType, Player player) {
		super();
		this.commandType = commandType;
		this.data = Optional.empty();
		this.player = player;
	}

	public CommandType getCommandType() {
		return commandType;
	}

	public Optional<Long> getData() {
		return data;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public String toString() {
		return commandType.toString() + ", " + (data.isPresent() ? data.get() + " " : "") + player.getIrcName() + " ["
				+ System.identityHashCode(this) + "]";
	}
}
