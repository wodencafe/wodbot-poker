package club.wodencafe.bot;

import java.util.Optional;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

import club.wodencafe.poker.holdem.RoundMediator;

public class WodBotListener extends ListenerAdapter {
	private PircBotX bot = null;

	private Optional<RoundMediator> round = Optional.empty();

	public WodBotListener() {
	}

	public void setBot(PircBotX bot) {
		this.bot = bot;
	}

	public WodBotListener(PircBotX bot) {
		this.bot = bot;
	}

	public static void main(String[] args) {
		WodBotListener listener = new WodBotListener();
		listener.onGenericMessage(null);
	}

	@Override
	public void onGenericMessage(GenericMessageEvent event) {

		/*
		 * if (event.getMessage().startsWith(WodData.commandChar + "startgame")) { if
		 * (round.isEmpty()) {
		 * 
		 * Player player = PlayerService.load(event.getUser().getNick());
		 * 
		 * round = Optional.of(new RoundMediator(initialPlayer)); } else {
		 * event.respond("Game already in progress."); } }
		 */
		// String message = event.getMessage();
		// String message = "!weather 75019";
		// When someone says ?helloworld respond with "Hello World"
		/*
		 * if (message.startsWith("!weather")) { String[] sp = message.split(" ");
		 * event.respond("Format not correct for weather"); }
		 */
	}

}
