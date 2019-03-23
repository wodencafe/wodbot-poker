package club.wodencafe.poker.cards.hands;

import java.util.List;

import club.wodencafe.poker.cards.Card;
import club.wodencafe.poker.cards.HandType;

public class Hand {

	private List<Card> cards;
	
	private HandType handType;
	
	public Hand(List<Card> cards, HandType handType) {
		this.cards = cards;
		
		this.handType = handType;
	}
}
