package club.wodencafe.poker.holdem;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import club.wodencafe.data.Player;

public class BettingRoundTest {

	@Test
	public void testBettingRoundAutoFold() throws Exception {
		Player player1 = getAlice();

		Player player2 = getBob();

		PlayerRoundData player1Data = new PlayerRoundData(player1);
		PlayerRoundData player2Data = new PlayerRoundData(player2);

		List<PlayerRoundData> players = Arrays.asList(player1Data, player2Data);

		try (BettingRound bettingRound = new BettingRound(players, 1)) {

			bettingRound.startAsync();

			PlayerRoundData playerData = bettingRound.onPlayerNewTurn().blockingFirst();

			Assert.assertEquals(playerData, player1Data);

			Assert.assertEquals(bettingRound.getCurrentAndPreviousPlayer().getKey(), player1);

			long amount = bettingRound.onBettingRoundComplete().blockingFirst();

			Assert.assertEquals(amount, 0);

			Assert.assertTrue(bettingRound.isRoundComplete());
		}
	}

	@Test
	public void testBettingRoundBettingGame() throws Exception {

		Player player1 = getAlice();

		Player player2 = getBob();

		PlayerRoundData player1Data = new PlayerRoundData(player1);
		PlayerRoundData player2Data = new PlayerRoundData(player2);

		List<PlayerRoundData> players = Arrays.asList(player1Data, player2Data);

		try (BettingRound bettingRound = new BettingRound(players)) {

			bettingRound.startAsync();

			Assert.assertEquals(bettingRound.getCurrentAndPreviousPlayer().getKey(), player1);

			Command command = new Command(CommandType.CHECK, player1);

			bettingRound.handleCommand(command);

			Assert.assertEquals(bettingRound.getCurrentAndPreviousPlayer().getKey(), player2);

			Assert.assertFalse(bettingRound.isRoundComplete());

			command = new Command(CommandType.BET, 30, player2);

			bettingRound.handleCommand(command);

			Assert.assertFalse(bettingRound.isRoundComplete());

			Assert.assertEquals(bettingRound.getCurrentAndPreviousPlayer().getKey(), player1);

			command = new Command(CommandType.CALL, player1);

			bettingRound.handleCommand(command);

			long amount = bettingRound.onBettingRoundComplete().blockingFirst();

			Assert.assertEquals(amount, 60);

			Assert.assertTrue(bettingRound.isRoundComplete());
		}
	}

	@Test
	public void testBettingRoundSimpleGame() throws Exception {

		Player player1 = getAlice();

		Player player2 = getBob();

		PlayerRoundData player1Data = new PlayerRoundData(player1);
		PlayerRoundData player2Data = new PlayerRoundData(player2);

		List<PlayerRoundData> players = Arrays.asList(player1Data, player2Data);

		try (BettingRound bettingRound = new BettingRound(players)) {

			bettingRound.startAsync();

			Assert.assertEquals(bettingRound.getCurrentAndPreviousPlayer().getKey(), player1);

			Command command = new Command(CommandType.CHECK, player1);

			bettingRound.handleCommand(command);

			Assert.assertEquals(bettingRound.getCurrentAndPreviousPlayer().getKey(), player2);

			Assert.assertFalse(bettingRound.isRoundComplete());

			command = new Command(CommandType.CHECK, player2);

			bettingRound.handleCommand(command);

			Assert.assertTrue(bettingRound.isRoundComplete());

			Long potSize = bettingRound.onBettingRoundComplete().blockingFirst();

			Assert.assertTrue(potSize == 0);
		}
	}

	private Player getBob() {
		Player player2 = new Player();
		player2.setCreatedDate(new Date());
		player2.setModifiedDate(new Date());
		player2.setIrcName("Bob");
		player2.addMoney(100);
		player2.setId(2L);
		return player2;
	}

	private Player getAlice() {
		Player player1 = new Player();
		player1.setCreatedDate(new Date());
		player1.setModifiedDate(new Date());
		player1.setIrcName("Alice");
		player1.addMoney(100);
		player1.setId(1L);
		return player1;
	}
}
