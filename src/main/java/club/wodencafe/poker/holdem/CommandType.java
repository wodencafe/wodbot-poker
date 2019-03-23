package club.wodencafe.poker.holdem;

public enum CommandType {
	DEAL("deal"),
	CHECK("check"),
	BET("bet"),
	CALL("call"),
	RAISE("raise"),
	FOLD("fold"),
	SHOW("show");
	
	private String commandName;
	private CommandType(String commandName) {
		this.commandName = commandName;
	}
	public String getCommandName() {
		return commandName;
	}
}
