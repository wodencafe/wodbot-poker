package club.wodencafe.poker.holdem;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import club.wodencafe.data.Player;

public class RoundMediatorTest {

	@Test
	public void testRoundMediator() {
		Player alice = getAlice();
		
		Player bob = getBob();
		
		RoundMediator roundMediator = new RoundMediator(alice);
		
		roundMediator.accept(new Command(CommandType.DEAL, bob));
		
		Assert.assertTrue(roundMediator.getPlayers().contains(bob) && roundMediator.getPlayers().contains(alice));

		
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
