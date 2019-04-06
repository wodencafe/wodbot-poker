package club.wodencafe.poker.holdem;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.Assert;
import org.junit.Test;

import club.wodencafe.data.Player;

public class RoundMediatorTest {

	@Test
	public void testRoundMediator() throws Exception {
		Player alice = getAlice();

		Player bob = getBob();

		try (RoundMediator roundMediator = new RoundMediator(alice)) {

			roundMediator.onGeneralMessage().subscribe(System.out::println);

			roundMediator.onPlayerMessage()
					.subscribe(c -> System.out.println("[" + c.getKey().getIrcName() + "] " + c.getValue()));

			roundMediator.test(new Command(CommandType.DEAL, bob));

			Assert.assertTrue(roundMediator.getPlayers().contains(bob) && roundMediator.getPlayers().contains(alice));

			// Manually skip to the next phase
			roundMediator.getPhaseManager().run();

			// Ensure we are in the Hole {@link Phase}
			Assert.assertTrue(roundMediator.getPhaseManager().get() == Phase.HOLE);

			Assert.assertEquals(roundMediator.getBettingRound().get().getCurrentAndPreviousPlayer().getKey(), alice);

			roundMediator.test(new Command(CommandType.CHECK, alice));

			Assert.assertEquals(roundMediator.getBettingRound().get().getCurrentAndPreviousPlayer().getKey(), bob);

			roundMediator.test(new Command(CommandType.CHECK, bob));

			Awaitility.await().atMost(5, TimeUnit.SECONDS)
					.until(() -> (roundMediator.getPhaseManager().get() == Phase.FLOP));

			roundMediator.test(new Command(CommandType.CHECK, alice));

			roundMediator.test(new Command(CommandType.CHECK, bob));

			Awaitility.await().atMost(5, TimeUnit.SECONDS)
					.until(() -> (roundMediator.getPhaseManager().get() == Phase.TURN));

			roundMediator.test(new Command(CommandType.CHECK, alice));

			roundMediator.test(new Command(CommandType.CHECK, bob));

			Awaitility.await().atMost(5, TimeUnit.SECONDS)
					.until(() -> (roundMediator.getPhaseManager().get() == Phase.RIVER));

			roundMediator.test(new Command(CommandType.CHECK, alice));

			roundMediator.test(new Command(CommandType.CHECK, bob));

			Awaitility.await().atMost(5, TimeUnit.SECONDS)
					.until(() -> (roundMediator.getPhaseManager().get() == Phase.SHOWDOWN));

			roundMediator.test(new Command(CommandType.SHOW, alice));

			roundMediator.test(new Command(CommandType.SHOW, bob));

			Map<PlayerRoundData, Long> winnerEntry = roundMediator.onRoundComplete().blockingFirst();

			// Collection<PlayerRoundData> winners = winnerEntry.getKey();

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

			roundMediator.test(new Command(CommandType.DEAL, bob));

			Assert.assertTrue(roundMediator.getPlayers().contains(bob) && roundMediator.getPlayers().contains(alice));

			// Manually skip to the next phase
			roundMediator.getPhaseManager().run();

			// Ensure we are in the Hole {@link Phase}
			Assert.assertTrue(roundMediator.getPhaseManager().get() == Phase.HOLE);

			roundMediator.test(new Command(CommandType.BET, 5, alice));

			roundMediator.test(new Command(CommandType.CALL, bob));

			Assert.assertTrue(10 == roundMediator.getCurrentPotSize());

			Assert.assertTrue(alice.getMoney() == 95);
			Assert.assertTrue(bob.getMoney() == 95);

			try {
				Awaitility.await().atMost(5, TimeUnit.SECONDS)
						.until(() -> (roundMediator.getPhaseManager().get() == Phase.FLOP));
			} catch (ConditionTimeoutException e) {
				// TODO: Investigate and print more info.
				throw e;
			}
			roundMediator.test(new Command(CommandType.CHECK, alice));

			roundMediator.test(new Command(CommandType.BET, 5, bob));

			roundMediator.test(new Command(CommandType.RAISE, 10, alice));

			roundMediator.test(new Command(CommandType.CALL, bob));

			Assert.assertTrue(30 == roundMediator.getCurrentPotSize());

			Assert.assertTrue(alice.getMoney() == 85);

			Assert.assertTrue(bob.getMoney() == 85);

			Awaitility.await().atMost(5, TimeUnit.SECONDS)
					.until(() -> (roundMediator.getPhaseManager().get() == Phase.TURN));

			roundMediator.test(new Command(CommandType.CHECK, alice));

			roundMediator.test(new Command(CommandType.CHECK, bob));

			Awaitility.await().atMost(5, TimeUnit.SECONDS)
					.until(() -> (roundMediator.getPhaseManager().get() == Phase.RIVER));

			roundMediator.test(new Command(CommandType.CHECK, alice));

			roundMediator.test(new Command(CommandType.CHECK, bob));

			Awaitility.await().atMost(5, TimeUnit.SECONDS)
					.until(() -> (roundMediator.getPhaseManager().get() == Phase.SHOWDOWN));

			roundMediator.test(new Command(CommandType.SHOW, alice));

			roundMediator.test(new Command(CommandType.SHOW, bob));

			Map<PlayerRoundData, Long> winnerEntry = roundMediator.onRoundComplete().blockingFirst();

			Collection<PlayerRoundData> winners = winnerEntry.keySet();

			if (winners.size() == 1) {
				Assert.assertTrue(winners.iterator().next().get().getMoney() == 115);
			} else if (winners.size() == 2) {
				Assert.assertTrue(winners.stream().filter(x -> x.get().getMoney() == 100).count() == 2);
			}

			Assert.assertTrue(bob.getMoney() == 115 || bob.getMoney() == 85);

			Assert.assertTrue(alice.getMoney() == 85 || alice.getMoney() == 115);

			Assert.assertEquals(alice.getMoney() + bob.getMoney(), 200);
		}
	}

	private Player getBob() {
		Player player2 = new Player();
		player2.setCreatedDate(new Date());
		player2.setModifiedDate(new Date());
		player2.setIrcName("Bob");
		player2.addMoney(100);
		return player2;
	}

	private Player getAlice() {
		Player player1 = new Player();
		player1.setCreatedDate(new Date());
		player1.setModifiedDate(new Date());
		player1.setIrcName("Alice");
		player1.addMoney(100);
		return player1;
	}
}
