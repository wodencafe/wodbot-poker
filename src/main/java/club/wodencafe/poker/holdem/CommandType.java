package club.wodencafe.poker.holdem;

public enum CommandType {
	DEAL("deal"),
	CHECK("check", true),
	BET("bet", true),
	CALL("call", true),
	RAISE("raise", true),
	FOLD("fold"),
	SHOW("show");
	
	private String commandName;
	private boolean betCommand = false;
	private CommandType(String commandName) {
		this.commandName = commandName;
	}
	private CommandType(String commandName, boolean betCommand) {
		this.commandName = commandName;
		this.betCommand = betCommand;
	}
	public String getCommandName() {
		return commandName;
	}
	public boolean isBetCommand() {
		return betCommand;
	}
}
