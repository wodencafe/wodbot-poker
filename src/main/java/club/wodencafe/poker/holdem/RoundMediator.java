package club.wodencafe.poker.holdem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.common.util.concurrent.AbstractScheduledService;

import club.wodencafe.bot.WodData;
import club.wodencafe.data.Player;
import club.wodencafe.poker.cards.Deck;
import io.reactivex.subjects.PublishSubject;

public class RoundMediator extends AbstractScheduledService implements AutoCloseable, Consumer<Command> {
	private List<PlayerRoundData> players = new ArrayList<>();
 	
    private PhaseManager phaseManager;

	private PublishSubject<String> generalMessage = PublishSubject.create();

	private PublishSubject<Map.Entry<Player, String>> playerMessage = PublishSubject.create();
	
	private Deck deck;
	
	private RoundMediator() {
		phaseManager = new PhaseManager();
		
		deck = Deck.generateDeck(false);
	}
	
	public PhaseManager getPhaseManager() {
		return phaseManager;
	}
	
	@Override
	public void close() throws Exception {
		// Split the pot up here
	}

	@Override
	protected void runOneIteration() throws Exception {
		Phase phase = phaseManager.get();
		
		if (phase == Phase.NOT_STARTED) {
			phaseManager.run();
			
			phase = phaseManager.get();
			
			generalMessage.onNext("Awaiting players, type " + WodData.commandChar + CommandType.DEAL.getCommandName() + " to be dealt into the match");
		}
	}

	@Override
	protected Scheduler scheduler() {

        return Scheduler.newFixedRateSchedule(0, 30, TimeUnit.SECONDS);
	}

	@Override
	public void accept(Command command) {
		CommandType commandType = command.getCommandType();
		Phase phase = phaseManager.get();
		Player player = command.getPlayer();
		
		if (phase == Phase.AWAITING_PLAYERS) {
			// TODO: Make sure the player has money
			if (commandType == CommandType.DEAL) {
				if (!isPlayerParticipating(player)) {
					// TODO: Should the cards be shown to the player here?
					PlayerRoundData data = new PlayerRoundData(player);
					
					players.add(data);
				}
			}
		}
		else if (phase.isBetPhase() || phase == Phase.SHOWDOWN) {
			if (phase.isBetPhase()) {
				switch (commandType) {
					
				}
			}
			
		
		}
	}
	
	private boolean isPlayerParticipating(Player player) {
		return players
			.stream()
			.map(x -> x.get())
			.map(x -> x.getIrcName())
			.anyMatch(x -> x.equals(player.getIrcName()));
	}
}
