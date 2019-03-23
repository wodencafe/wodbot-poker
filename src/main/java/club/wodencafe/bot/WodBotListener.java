package club.wodencafe.bot;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.types.GenericMessageEvent;

import com.github.fedy2.weather.data.Channel;

public class WodBotListener extends ListenerAdapter {
	private PircBotX bot = null;
	public WodBotListener() {
	}
	public void setBot(PircBotX bot) {
		this.bot = bot;
	}
	public WodBotListener(PircBotX bot) {
		this.bot = bot;
	}
	public static void main(String[] args) {
		WodBotListener listener = new WodBotListener();
		listener.onGenericMessage(null);
	}
    @Override
    public void onGenericMessage(GenericMessageEvent event) {
    	//String message = event.getMessage();
    	//String message = "!weather 75019";
        //When someone says ?helloworld respond with "Hello World"
        /*if (message.startsWith("!weather"))
        {
        	String[] sp = message.split(" ");
        	event.respond("Format not correct for weather");
        }*/
    }
    
}
