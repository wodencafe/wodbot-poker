package club.wodencafe.poker.holdem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import club.wodencafe.data.Player;

public class BettingRoundTest {
	
	@Test
	public void testBettingRoundSimpleGame() {
	
		Player player1 = new Player();
		player1.setCreatedDate(new Date());
		player1.setModifiedDate(new Date());
		player1.setIrcName("Bob");
		player1.addMoney(100);
		player1.setId(1L);
		
		Player player2 = new Player();
		player2.setCreatedDate(new Date());
		player2.setModifiedDate(new Date());
		player2.setIrcName("Alice");
		player2.addMoney(100);
		player2.setId(2L);

		PlayerRoundData player1Data = new PlayerRoundData(player1);
		PlayerRoundData player2Data = new PlayerRoundData(player2);
		
		List<PlayerRoundData> players = Arrays.asList(player1Data, player2Data);
		
		BettingRound bettingRound = new BettingRound(players);
		
		bettingRound.startAsync();
		
		Assert.assertEquals(bettingRound.getCurrentPlayer(), player1);
		
		
		
	}
}
