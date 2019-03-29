package club.wodencafe.poker.holdem;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * 
 * This class represents the different {@link Phase}s of
 * a given round of poker, for the {@link RoundMediator} class.
 * 
 * @author wodencafe
 */
public class PhaseManager implements AutoCloseable, Runnable, Supplier<Phase> {

	private PublishSubject<Phase> newPhase = PublishSubject.create();

	private Iterator<Phase> phases = Arrays.asList(Phase.values()).iterator();
	private Phase phase;
	public PhaseManager() {
		phase = phases.next();
	}
	
	
	public Observable<Phase> onNewPhase() {
		return newPhase;
	}

	@Override
	public void close() throws Exception {
		newPhase.onComplete();
		
	}

	@Override
	public Phase get() {
		return phase;
	}

	@Override
	public void run() {
		if (phases.hasNext()) {
			phase = phases.next();
			newPhase.onNext(phase);
		}
	}
}
