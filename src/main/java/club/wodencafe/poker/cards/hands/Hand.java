package club.wodencafe.poker.cards.hands;

import java.util.List;
import java.util.stream.Collectors;

import club.wodencafe.poker.cards.Card;
import club.wodencafe.poker.cards.HandType;

public class Hand {

	private List<Card> cards;
	
	private HandType handType;
	
	public Hand(List<Card> cards, HandType handType) {
		this.cards = cards;
		
		this.handType = handType;
	}

	public List<Card> getCards() {
		return cards;
	}

	public HandType getHandType() {
		return handType;
	}
	
	public List<Card> getHandTypeCards() {
		int count;
		
		switch (getHandType()) {
			case STRAIGHT:
			case FLUSH:
			case STRAIGHT_FLUSH:
			case FULL_HOUSE:
			case ROYAL_FLUSH: {
				count = 5;
			}
			break;
			case TWO_PAIR:
			case FOUR: {
				count = 4;
			}
			break;
			case TRIPS: {
				count = 3;
			}
			break;
			case PAIR: {
				count = 2;
			}
			case HIGH: 
			default: {
				count = 1;
			}
			break;
		}
		
		return getCards().stream()
			.limit(count)
			.collect(Collectors.toList());
	}
}
