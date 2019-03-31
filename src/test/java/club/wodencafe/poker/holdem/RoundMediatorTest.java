package club.wodencafe.poker.holdem;

import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Test;

import club.wodencafe.data.Player;

public class RoundMediatorTest {

	// @Test
	public void testRoundMediator() throws Exception {
		Player alice = getAlice();

		Player bob = getBob();

		try (RoundMediator roundMediator = new RoundMediator(alice)) {

			roundMediator.onGeneralMessage().subscribe(System.out::println);

			roundMediator.onPlayerMessage()
					.subscribe(c -> System.out.println("[" + c.getKey().getIrcName() + "] " + c.getValue()));

			roundMediator.accept(new Command(CommandType.DEAL, bob));

			Assert.assertTrue(roundMediator.getPlayers().contains(bob) && roundMediator.getPlayers().contains(alice));

			// Manually skip to the next phase
			roundMediator.getPhaseManager().run();

			// Ensure we are in the Hole {@link Phase}
			Assert.assertTrue(roundMediator.getPhaseManager().get() == Phase.HOLE);

			roundMediator.accept(new Command(CommandType.CHECK, alice));

			roundMediator.accept(new Command(CommandType.CHECK, bob));

			Awaitility.await().atMost(5, TimeUnit.SECONDS)
					.until(() -> (roundMediator.getPhaseManager().get() == Phase.FLOP));

			roundMediator.accept(new Command(CommandType.CHECK, alice));

			roundMediator.accept(new Command(CommandType.CHECK, bob));

			Awaitility.await().atMost(5, TimeUnit.SECONDS)
					.until(() -> (roundMediator.getPhaseManager().get() == Phase.TURN));

			roundMediator.accept(new Command(CommandType.CHECK, alice));

			roundMediator.accept(new Command(CommandType.CHECK, bob));

			Awaitility.await().atMost(5, TimeUnit.SECONDS)
					.until(() -> (roundMediator.getPhaseManager().get() == Phase.RIVER));

			roundMediator.accept(new Command(CommandType.CHECK, alice));

			roundMediator.accept(new Command(CommandType.CHECK, bob));

			Awaitility.await().atMost(5, TimeUnit.SECONDS)
					.until(() -> (roundMediator.getPhaseManager().get() == Phase.SHOWDOWN));

			roundMediator.accept(new Command(CommandType.SHOW, alice));

			roundMediator.accept(new Command(CommandType.SHOW, bob));

			Entry<Collection<PlayerRoundData>, Long> winnerEntry = roundMediator.onRoundComplete().blockingFirst();

			Collection<PlayerRoundData> winners = winnerEntry.getKey();

		}
	}

	@Test
	public void testRoundMediatorWithBets() throws Exception {
		Player alice = getAlice();

		Player bob = getBob();

		try (RoundMediator roundMediator = new RoundMediator(alice)) {

			roundMediator.onGeneralMessage().subscribe(System.out::println);

			roundMediator.onPlayerMessage()
					.subscribe(c -> System.out.println("[" + c.getKey().getIrcName() + "] " + c.getValue()));

			roundMediator.accept(new Command(CommandType.DEAL, bob));

			Assert.assertTrue(roundMediator.getPlayers().contains(bob) && roundMediator.getPlayers().contains(alice));

			// Manually skip to the next phase
			roundMediator.getPhaseManager().run();

			// Ensure we are in the Hole {@link Phase}
			Assert.assertTrue(roundMediator.getPhaseManager().get() == Phase.HOLE);

			roundMediator.accept(new Command(CommandType.BET, 5, alice));

			roundMediator.accept(new Command(CommandType.CALL, bob));

			Assert.assertTrue(10 == roundMediator.getCurrentPotSize());

			Assert.assertTrue(alice.getMoney() == 95);
			Assert.assertTrue(bob.getMoney() == 95);

			Awaitility.await().atMost(5, TimeUnit.SECONDS)
					.until(() -> (roundMediator.getPhaseManager().get() == Phase.FLOP));

			roundMediator.accept(new Command(CommandType.CHECK, alice));

			roundMediator.accept(new Command(CommandType.BET, 5, bob));

			roundMediator.accept(new Command(CommandType.RAISE, 10, alice));

			roundMediator.accept(new Command(CommandType.CALL, bob));

			Assert.assertTrue(30 == roundMediator.getCurrentPotSize());

			Assert.assertTrue(alice.getMoney() == 85);

			Assert.assertTrue(bob.getMoney() == 85);

			Awaitility.await().atMost(5, TimeUnit.SECONDS)
					.until(() -> (roundMediator.getPhaseManager().get() == Phase.TURN));

			roundMediator.accept(new Command(CommandType.CHECK, alice));

			roundMediator.accept(new Command(CommandType.CHECK, bob));

			Awaitility.await().atMost(5, TimeUnit.SECONDS)
					.until(() -> (roundMediator.getPhaseManager().get() == Phase.RIVER));

			roundMediator.accept(new Command(CommandType.CHECK, alice));

			roundMediator.accept(new Command(CommandType.CHECK, bob));

			Awaitility.await().atMost(5, TimeUnit.SECONDS)
					.until(() -> (roundMediator.getPhaseManager().get() == Phase.SHOWDOWN));

			roundMediator.accept(new Command(CommandType.SHOW, alice));

			roundMediator.accept(new Command(CommandType.SHOW, bob));

			Entry<Collection<PlayerRoundData>, Long> winnerEntry = roundMediator.onRoundComplete().blockingFirst();

			Collection<PlayerRoundData> winners = winnerEntry.getKey();

			Assert.assertTrue(bob.getMoney() == 115);

			Assert.assertTrue(alice.getMoney() == 85);
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
