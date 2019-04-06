package club.wodencafe.bot;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.types.GenericMessageEvent;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import club.wodencafe.data.DatabaseService;
import club.wodencafe.data.Player;
import club.wodencafe.data.PlayerService;
import club.wodencafe.poker.holdem.Command;
import club.wodencafe.poker.holdem.CommandType;
import club.wodencafe.poker.holdem.PlayerRoundData;
import club.wodencafe.poker.holdem.RoundMediator;

public class WodBotListener extends ListenerAdapter {

	private static final XLogger logger = XLoggerFactory.getXLogger(WodBotListener.class);
	private PircBotX bot = null;

	private Optional<Entry<RoundMediator, Collection<User>>> round = Optional.empty();

	public WodBotListener() {
		DatabaseService.SINGLETON.isRunning();
	}

	public void setBot(PircBotX bot) {
		this.bot = bot;
	}

	public WodBotListener(PircBotX bot) {
		this.bot = bot;
	}

	private void wireUpRound(RoundMediator roundMediator, String channel) {
		roundMediator.onGeneralMessage().subscribe(message -> bot.sendIRC().message(channel, message));
		roundMediator.onPlayerMessage().delay(100, TimeUnit.MILLISECONDS).subscribe(message ->
		{
			Player player = message.getKey();
			User user = round.get().getValue().stream().filter(x -> x.getNick().equals(player.getIrcName())).findAny()
					.orElse(null);
			try {
				if (user != null) {
					user.send().notice(message.getValue());
				}
			} catch (Throwable th) {
				logger.error("Error with sending a private message to user " + player.getIrcName()
						+ System.lineSeparator() + th.getMessage(), th);
				throw new RuntimeException(th);
			}
		}, error ->
		{
			logger.error("Error with Player Message", error);
		});
		roundMediator.onRoundComplete().subscribe(event ->
		{
			StringBuilder sb = new StringBuilder("Round complete.");
			Entry<PlayerRoundData, Long> entry = event.entrySet().iterator().next();
			if (event.size() > 0) {
				sb.append(" Winner is " + entry.getKey().get().getIrcName() + ", winning $"
						+ entry.getValue().toString());
			}
		});
	}

	@Override
	public void onGenericMessage(GenericMessageEvent genericEvent) {

		MessageEvent event = (MessageEvent) genericEvent;
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
					round = Optional.of(new AbstractMap.SimpleEntry<>(roundMediator,
							new ArrayList<>(Arrays.asList(event.getUser()))));
					String message = "New round initiated by " + nick + ". Type " + WodData.commandChar
							+ "deal to join the game.";
					logger.trace("Sending Message [" + message + "]");
					wireUpRound(roundMediator, event.getChannelSource());
					event.respond(message);
				} else {
					String message = "Round is currently in progress.";
					logger.trace("Sending Message [" + message + "]");
					event.respond(message);
				}
			} else if (event.getMessage().startsWith(WodData.commandChar + "deal")) {
				RoundMediator roundValue = round.get().getKey();
				if (roundValue != null) {
					String nick = event.getUser().getNick();
					Player player = PlayerService.load(nick);

					if (player == null) {
						player = new Player();
						player.setIrcName(event.getUser().getNick());
						player.setMoney(100L);
						PlayerService.save(player);
						logger.debug("Saving new player " + player);
					}

					if (!roundValue.getPlayers().contains(player)) {
						if (roundValue.test(new Command(CommandType.DEAL, player))) {
							round.get().getValue().add(event.getUser());
						}
					} else {
						String message = "You are already in the game.";
						logger.debug("Sending Message [" + message + "]");
						event.respond(message);
					}
				} else {
					String responseMessage = "No rounds, try " + WodData.commandChar + "deal";
					logger.debug("Sending Message [" + responseMessage + "]");
					event.respond(responseMessage);
				}
			} else {

				if (isLegitMessage(event.getMessage())) {
					RoundMediator roundValue = round.get().getKey();
					if (roundValue != null) {
						String nick = event.getUser().getNick();
						Player player = PlayerService.load(nick);
						Command command = getCommand(event.getMessage(), player);
						if (command != null) {
							roundValue.test(command);
						} else {
							// TODO: Some kind of error here
						}
					}
				}
			}

		} catch (Throwable e) {
			logger.error("Error processing message.", e);
			event.respond("Unexpected error occurred: " + e.getMessage());
			throw new RuntimeException(e);
		}

	}

	private Command getCommand(String message, Player player) {
		String[] messageSplit;
		long value = -1;
		message = message.substring(1);
		if (message.contains(" ")) {
			messageSplit = message.split(" ");
			message = messageSplit[0];
			if (messageSplit.length > 1) {
				try {
					value = Integer.parseInt(messageSplit[1]);
				} catch (Throwable th) {
					logger.debug("Can't parse integer.", th);
				}
			}
		}
		if (value > -1) {
			return new Command(CommandType.get(message), player);
		} else {

			return new Command(CommandType.get(message), value, player);
		}
	}

	private boolean isLegitMessage(String message) {
		if (message.contains(" ")) {
			message = message.split(" ")[0];
		}
		return CommandType.get(message.substring(1)) != null;
	}

}
