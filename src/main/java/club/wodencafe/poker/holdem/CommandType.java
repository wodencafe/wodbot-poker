package club.wodencafe.poker.holdem;

import java.util.Objects;

public enum CommandType {
	DEAL("deal"), CHECK("check", true), BET("bet", true), CALL("call", true), RAISE("raise", true), FOLD("fold"),
	SHOW("show"), PEEK("peek");

	private String commandName;
	private boolean betCommand = false;

	private CommandType(String commandName) {
		this.commandName = commandName;
	}

	private CommandType(String commandName, boolean betCommand) {
		this.commandName = commandName;
		this.betCommand = betCommand;
	}

	public static CommandType get(String commandName) {
		for (CommandType type : values()) {
			if (Objects.equals(commandName, type.getCommandName())) {
				return type;
			}
		}
		return null;
	}

	public String getCommandName() {
		return commandName;
	}

	public boolean isBetCommand() {
		return betCommand;
	}
}
