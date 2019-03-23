package club.wodencafe.poker.holdem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.common.util.concurrent.AbstractScheduledService;

import club.wodencafe.bot.WodData;
import club.wodencafe.poker.Player;
import io.reactivex.subjects.PublishSubject;

public class RoundMediator extends AbstractScheduledService implements AutoCloseable, Consumer<Command> {
	private List<PlayerData> players = new ArrayList<>();
 	
    private PhaseManager phaseManager;

	private PublishSubject<String> generalMessage = PublishSubject.create();

	private PublishSubject<Map.Entry<Player, String>> playerMessage = PublishSubject.create();
	
	private RoundMediator() {
		phaseManager = new PhaseManager();
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
		switch (phase) {
			case AWAITING_PLAYERS: {
				
			}
			break;
		}
	}
}
