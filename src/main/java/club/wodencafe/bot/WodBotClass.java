package club.wodencafe.bot;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

public class WodBotClass implements Runnable {
	public static void main(String[] args) throws Exception {

		runBot();
	}

	private static void runBot() {
		WodBotClass wodBot = new WodBotClass();
		wodBot.run();
	}

	public WodBotListener listener = new WodBotListener();

	public WodBotService service = new WodBotService();

	PircBotX bot;

	@Override
	public void run() {
		// Configure what we want our bot to do
		Configuration configuration = new Configuration.Builder().setName("WodPokerBot") // Set the nick of the bot.
																							// CHANGE IN YOUR CODE
				.addServer("irc.freenode.net") // Join the freenode network
				.addAutoJoinChannel("#sgchan") // Join the official #pircbotx channel
				.addListener(listener) // Add our listener that will be called on Events
				.buildConfiguration();
		try {

			PircBotX bot = new PircBotX(configuration);
			this.bot = bot;
			service.setBot(bot);
			bot.startBot();
			listener.setBot(bot);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Finished");
	}
}
