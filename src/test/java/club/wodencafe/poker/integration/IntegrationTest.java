package club.wodencafe.poker.integration;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.output.OutputIRC;
import org.pircbotx.output.OutputUser;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import club.wodencafe.bot.WodBotListener;
import club.wodencafe.bot.WodData;

@PowerMockIgnore({ "org.w3c.*", "javax.xml.*", "java.xml.*", "org.xml.sax.*", "org.w3c.dom.*",
		"com.sun.org.apache.xerces.*", "org.springframework.context.*", "org.apache.log4j.*", "javax.management.*",
		"java.lang.*", "javax.security.*" })
@RunWith(PowerMockRunner.class)
public class IntegrationTest {
	private static final Logger logger = LoggerFactory.getLogger(IntegrationTest.class);

	@Test
	public void test() throws Exception {

		// !startgame
		String userName1 = "wodencafe";

		PircBotX bot = EasyMock.createNiceMock(PircBotX.class);

		OutputIRC output = EasyMock.createNiceMock(OutputIRC.class);

		output.message(EasyMock.anyString(), EasyMock.anyString());

		EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {

			@Override
			public Object answer() throws Throwable {
				String channel = ((String) EasyMock.getCurrentArguments()[0]);
				String message = ((String) EasyMock.getCurrentArguments()[1]);
				logger.debug("Received from bot: " + channel + " - " + message);
				return null;
			}
		});

		EasyMock.expect(bot.sendIRC()).andStubReturn(output);

		MessageEvent wodencafe1StartGameMessage = EasyMock.createNiceMock(MessageEvent.class);

		EasyMock.expect(wodencafe1StartGameMessage.getMessage()).andStubReturn(WodData.commandChar + "startgame");

		User user1 = EasyMock.createNiceMock(User.class);

		OutputUser outputUser1 = EasyMock.createNiceMock(OutputUser.class);

		outputUser1.notice(EasyMock.anyString());

		EasyMock.expectLastCall().andStubAnswer(() ->
		{

			logger.debug("[NOTICE] -" + userName1 + ": " + ((String) EasyMock.getCurrentArguments()[0]) + "-");
			return null;
		});

		EasyMock.expect(user1.send()).andStubReturn(outputUser1);

		EasyMock.expect(wodencafe1StartGameMessage.getUser()).andStubReturn(user1);

		EasyMock.expect(user1.getNick()).andStubReturn(userName1);

		EasyMock.expect(wodencafe1StartGameMessage.getChannelSource()).andStubReturn("#poker");

		wodencafe1StartGameMessage.respond(EasyMock.anyString());

		EasyMock.expectLastCall().andStubAnswer(() ->
		{
			logger.debug(userName1 + ": " + ((String) EasyMock.getCurrentArguments()[0]));
			return null;
		});

		EasyMock.replay(outputUser1, bot, output, user1, wodencafe1StartGameMessage);

		WodBotListener listener = new WodBotListener(bot);

		listener.onGenericMessage(wodencafe1StartGameMessage);

		// !deal
		String userName2 = "wodencafe2";

		MessageEvent wodencafe2JoinGameMessageEvent = EasyMock.createNiceMock(MessageEvent.class);

		EasyMock.expect(wodencafe2JoinGameMessageEvent.getMessage()).andStubReturn(WodData.commandChar + "deal");

		User user2 = EasyMock.createNiceMock(User.class);

		OutputUser outputUser2 = EasyMock.createNiceMock(OutputUser.class);

		outputUser2.notice(EasyMock.anyString());

		EasyMock.expectLastCall().andStubAnswer(() ->
		{

			logger.debug("[NOTICE] -" + userName2 + ": " + ((String) EasyMock.getCurrentArguments()[0]) + "-");
			return null;
		});

		EasyMock.expect(user2.send()).andStubReturn(outputUser2);

		EasyMock.expect(wodencafe2JoinGameMessageEvent.getUser()).andStubReturn(user2);

		EasyMock.expect(user2.getNick()).andStubReturn(userName2);

		EasyMock.expect(wodencafe2JoinGameMessageEvent.getChannelSource()).andStubReturn("#poker");

		wodencafe2JoinGameMessageEvent.respond(EasyMock.anyString());

		EasyMock.expectLastCall().andStubAnswer(() ->
		{
			logger.debug(userName2 + ": " + ((String) EasyMock.getCurrentArguments()[0]));
			return null;
		});

		EasyMock.replay(user2, wodencafe2JoinGameMessageEvent);

		listener.onGenericMessage(wodencafe2JoinGameMessageEvent);

		listener.getRound().endPhaseEarly();

		// bet 10d

		MessageEvent wodencafe1BetGameMessageEvent = getMessageFromCommand(userName1, user1, BET + " 10");

		listener.onGenericMessage(wodencafe1BetGameMessageEvent);

		// call

		MessageEvent wodencafe2CallGameMessageEvent = getMessageFromCommand(userName2, user2, CALL);

		listener.onGenericMessage(wodencafe2CallGameMessageEvent);

		// check
		MessageEvent wodencafe1CheckGameMessageEvent = getMessageFromCommand(userName1, user1, CHECK);

		listener.onGenericMessage(wodencafe1CheckGameMessageEvent);

		// check 2
		MessageEvent wodencafe2CheckGameMessageEvent = getMessageFromCommand(userName2, user2, CHECK);

		listener.onGenericMessage(wodencafe2CheckGameMessageEvent);

		// check

		listener.onGenericMessage(getMessageFromCommand(userName1, user1, CHECK));

		// bet

		listener.onGenericMessage(getMessageFromCommand(userName2, user2, BET + " 10"));

		// raise

		listener.onGenericMessage(getMessageFromCommand(userName1, user1, RAISE + " 5"));

		// raise

		listener.onGenericMessage(getMessageFromCommand(userName2, user2, CALL));

		System.out.println("foobar");

	}

	private MessageEvent getMessageFromCommand(String userName2, User user2, String command) {
		MessageEvent messageEvent = EasyMock.createNiceMock(MessageEvent.class);

		EasyMock.expect(messageEvent.getMessage()).andStubReturn(command);

		EasyMock.expect(messageEvent.getUser()).andStubReturn(user2);

		EasyMock.expect(messageEvent.getChannelSource()).andStubReturn("#poker");

		messageEvent.respond(EasyMock.anyString());

		EasyMock.expectLastCall().andStubAnswer(() ->
		{
			logger.debug(userName2 + ": " + ((String) EasyMock.getCurrentArguments()[0]));
			return null;
		});

		EasyMock.replay(messageEvent);

		return messageEvent;
	}

	private static final String BET = WodData.commandChar + "bet";
	private static final String RAISE = WodData.commandChar + "raise";
	private static final String CHECK = WodData.commandChar + "check";
	private static final String CALL = WodData.commandChar + "call";
}
