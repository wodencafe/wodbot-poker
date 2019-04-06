package club.wodencafe.bot;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

public class WodBotClass implements Runnable {
	public static void setLoggingLevel(ch.qos.logback.classic.Level level) {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(level);
	}

	public static void main(String[] args) throws Exception {
		setLoggingLevel(ch.qos.logback.classic.Level.TRACE);

		WodData.databaseName = "poker-prod";

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
			listener.setBot(bot);
			bot.startBot();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Finished");
	}
}
