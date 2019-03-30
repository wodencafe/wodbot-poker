package club.wodencafe.poker.holdem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import club.wodencafe.data.Player;

public class BettingRoundTest {
	
	@Test
	public void testBettingRoundAutoFold() throws Exception {
		Player player1 = new Player();
		player1.setCreatedDate(new Date());
		player1.setModifiedDate(new Date());
		player1.setIrcName("Alice");
		player1.addMoney(100);
		player1.setId(1L);
		
		Player player2 = new Player();
		player2.setCreatedDate(new Date());
		player2.setModifiedDate(new Date());
		player2.setIrcName("Bob");
		player2.addMoney(100);
		player2.setId(2L);

		PlayerRoundData player1Data = new PlayerRoundData(player1);
		PlayerRoundData player2Data = new PlayerRoundData(player2);
		
		List<PlayerRoundData> players = Arrays.asList(player1Data, player2Data);
		
		try (BettingRound bettingRound = new BettingRound(players, 1)) {
			
			bettingRound.startAsync();
			
			PlayerRoundData player = bettingRound.onPlayerNewTurn().blockingFirst();

			Assert.assertEquals(bettingRound.getCurrentPlayer(), player1);	
			
			long amount = bettingRound.onBettingRoundComplete().blockingFirst();
			
			Assert.assertEquals(amount, 0);
		}
	}
	
	@Test
	public void testBettingRoundSimpleGame() throws Exception {
	
		Player player1 = new Player();
		player1.setCreatedDate(new Date());
		player1.setModifiedDate(new Date());
		player1.setIrcName("Alice");
		player1.addMoney(100);
		player1.setId(1L);
		
		Player player2 = new Player();
		player2.setCreatedDate(new Date());
		player2.setModifiedDate(new Date());
		player2.setIrcName("Bob");
		player2.addMoney(100);
		player2.setId(2L);

		PlayerRoundData player1Data = new PlayerRoundData(player1);
		PlayerRoundData player2Data = new PlayerRoundData(player2);
		
		List<PlayerRoundData> players = Arrays.asList(player1Data, player2Data);
		
		try (BettingRound bettingRound = new BettingRound(players)) {
			
			bettingRound.startAsync();
			
			Assert.assertEquals(bettingRound.getCurrentPlayer(), player1);
			
			Command command = new Command(CommandType.CHECK, Optional.empty(), player1);
			
			bettingRound.handleCommand(command);
	
			Assert.assertEquals(bettingRound.getCurrentPlayer(), player2);
			
			Assert.assertFalse(bettingRound.isPotSatisfied());
			
			command = new Command(CommandType.CHECK, Optional.empty(), player2);
			
			bettingRound.handleCommand(command);
			
			Assert.assertTrue(bettingRound.isPotSatisfied());
			
			Long potSize = bettingRound.onBettingRoundComplete().blockingFirst();
			
			Assert.assertTrue(potSize == 0);
		}
	}
}
