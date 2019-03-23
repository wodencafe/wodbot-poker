package club.wodencafe.bot;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.pircbotx.PircBotX;

import com.google.common.util.concurrent.AbstractScheduledService;

public class WodBotService extends AbstractScheduledService {
	public WodBotService() {
		startAsync();
	}
	private PircBotX bot;
	
	public void setBot(PircBotX bot) {
		this.bot = bot;
	}
	@Override
	protected void runOneIteration() throws Exception {
		if (Objects.equals("foo", "foo")) {
			
		}
	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedDelaySchedule(0, 5, TimeUnit.SECONDS);
	}

}
