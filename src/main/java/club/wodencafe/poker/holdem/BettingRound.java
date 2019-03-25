package club.wodencafe.poker.holdem;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;

import io.reactivex.subjects.PublishSubject;

/**
 * This class should be instantiated for each round of betting,
 * and will handle the commands related to betting.
 * 
 * @author wodencafe
 *
 */
public class BettingRound extends AbstractScheduledService {
	private List<PlayerRoundData> players;

	private PublishSubject<PlayerRoundData> playerTurnEvent = PublishSubject.create();

	public BettingRound(List<PlayerRoundData> players) {
		this.players = players;
	}
	
	@Override
	protected void runOneIteration() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(0, 30, TimeUnit.SECONDS);

	}

	
}
