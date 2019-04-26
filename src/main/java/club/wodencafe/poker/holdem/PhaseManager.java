package club.wodencafe.poker.holdem;

import java.util.Arrays;
import java.util.Iterator;
import java.util.function.Supplier;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * 
 * This class represents the different {@link Phase}s of a given round of poker,
 * for the {@link RoundMediator} class.
 * 
 * @author wodencafe
 */
public class PhaseManager implements AutoCloseable, Runnable, Supplier<Phase> {

	private static final XLogger logger = XLoggerFactory.getXLogger(PhaseManager.class);
	private PublishSubject<Phase> newPhase = PublishSubject.create();

	private Iterator<Phase> phases = Arrays.asList(Phase.values()).iterator();
	private Phase phase;

	public PhaseManager() {
		logger.entry();
		try {
			phase = phases.next();
		} catch (Throwable th) {
			RuntimeException e = new RuntimeException(th);
			logger.throwing(th);
			throw e;
		} finally {
			logger.exit(this);
		}

	}

	public Observable<Phase> onNewPhase() {
		logger.entry();
		try {
			return newPhase;
		} finally {
			logger.exit(newPhase);
		}
	}

	@Override
	public void close() throws Exception {
		logger.entry();
		try {
			// TODO Make idempotent
			newPhase.onComplete();
		} finally {
			logger.exit();
		}

	}

	@Override
	public Phase get() {
		logger.entry();
		try {
			return phase;
		} finally {
			logger.exit(phase);
		}
	}

	@Override
	public void run() {
		logger.entry();
		try {
			if (phases.hasNext()) {
				phase = phases.next();
				logger.debug("New Phase is: " + phase, phase);
				newPhase.onNext(phase);
			} else {
				logger.debug("No more phases.");
			}
		} catch (Throwable th) {
			RuntimeException e = new RuntimeException(th);
			logger.throwing(e);
			throw e;
		} finally {
			logger.exit();
		}
	}
}
