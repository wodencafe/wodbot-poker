package club.wodencafe.bot;

import java.util.Optional;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.wodencafe.data.Player;
import club.wodencafe.data.PlayerService;
import club.wodencafe.poker.holdem.RoundMediator;

public class WodBotListener extends ListenerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(WodBotListener.class);
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

	@Override
	public void onGenericMessage(GenericMessageEvent event) {

		try {
			if (event.getMessage().startsWith(WodData.commandChar + "startgame")) {
				if (!round.isPresent()) {

					String nick = event.getUser().getNick();

					Player player = PlayerService.load(nick);

					if (player == null) {
						player = new Player();
						player.setIrcName(event.getUser().getNick());
						player.setMoney(100L);
						PlayerService.save(player);
						logger.debug("Saving new player " + player);
					}

					RoundMediator roundMediator = new RoundMediator(player);
					round = Optional.of(roundMediator);
				} else {
					event.respond("Round is currently in progress.");
				}
			}
		} catch (Throwable e) {
			event.respond("Unexpected error occurred: " + e.getMessage());
			throw new RuntimeException(e);
		}

	}

}
