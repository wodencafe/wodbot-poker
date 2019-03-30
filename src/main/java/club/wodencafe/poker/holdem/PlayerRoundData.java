package club.wodencafe.poker.holdem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import club.wodencafe.data.Player;
import club.wodencafe.poker.cards.Card;

public class PlayerRoundData implements AutoCloseable {
	private Player player;
	public List<Card> getCards() {
		return cards;
	}
	private List<Card> cards = new ArrayList<>();
	public PlayerRoundData(Player player) {
		this.player = player;
	}
	public Player get() {
		return player;
	}
	public void deal(Card card, Card... cards) {
		this.cards.add(card);
		
		if (cards != null && cards.length > 0) {
			for (Card additionalCard : cards) {
				this.cards.add(additionalCard);
			}
		}
	}
	private boolean shown = false;
	public boolean isShown() {
		return shown;
	}
	private boolean folded = false;
	public boolean isFolded() {
		return folded;
	}
	@Override
	public String toString() {
		StringBuilder messageBuilder = new StringBuilder();
		for (Card card : cards) {
			messageBuilder.append(card);
		}
		if (!cards.isEmpty()) {
			messageBuilder.append(" - ");
		}
		messageBuilder.append(player);
		if (isFolded()) {
			messageBuilder.append(" (folded)");
		}
		messageBuilder.append("[" + System.identityHashCode(this) + "]");
		return messageBuilder.toString();
	}
	@Override
	public void close() throws Exception {
		folded = true;
	}
	
	public void fold() {
		try {
			close();
		}
		catch (Exception e) {
			
		}
	}
}
