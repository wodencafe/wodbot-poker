package club.wodencafe.poker.holdem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import club.wodencafe.data.Player;
import club.wodencafe.poker.cards.Card;

public class PlayerRoundData implements AutoCloseable {
	private Player player;
	private AtomicLong totalBet = new AtomicLong();
	private Collection<Card> cards = new ArrayList<>();
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
	private boolean folded = false;
	public boolean isFolded() {
		return folded;
	}
	
	@Override
	public void close() throws Exception {
		folded = true;
	}
}
