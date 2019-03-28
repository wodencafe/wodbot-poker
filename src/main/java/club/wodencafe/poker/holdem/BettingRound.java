package club.wodencafe.poker.holdem;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;

import club.wodencafe.data.Player;
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

	private PublishSubject<Void> roundComplete = PublishSubject.create();
	
	private Optional<PlayerRoundData> currentPlayer = Optional.empty();
	
	private Iterator<PlayerRoundData> playersIterator;

	public BettingRound(List<PlayerRoundData> players) {
		this.players = players;
		this.playersIterator = players.iterator();
	}
	
	@Override
	protected void runOneIteration() throws Exception {

		PlayerRoundData player = playersIterator.next();
		this.currentPlayer = Optional.of(player);
		
	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(0, 30, TimeUnit.SECONDS);

	}

	
}
