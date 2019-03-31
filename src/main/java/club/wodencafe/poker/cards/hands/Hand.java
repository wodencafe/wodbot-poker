package club.wodencafe.poker.cards.hands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.text.WordUtils;

import club.wodencafe.poker.cards.Card;
import club.wodencafe.poker.cards.HandType;
import club.wodencafe.poker.cards.HandUtil;

public class Hand implements Comparable<Hand> {

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

	public List<Card> getNonHandCards() {
		List<Card> cards = new ArrayList<>(getCards());

		cards.removeAll(getHandTypeCards());

		return cards;
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
		case FOUR_OF_A_KIND: {
			count = 4;
		}
			break;
		case THREE_OF_A_KIND: {
			count = 3;
		}
			break;
		case PAIR: {
			count = 2;
		}
		case HIGH_CARD:
		default: {
			count = 1;
		}
			break;
		}

		return getCards().stream().limit(count).collect(Collectors.toList());
	}

	@Override
	public int compareTo(Hand arg0) {
		if (this.handType != arg0.handType) {
			return this.handType.getValue() - arg0.handType.getValue();
		} else {

			List<Card> sortedCards = HandUtil.getCardsSortedAceHigh(cards);
			List<Card> otherSortedCards = HandUtil.getCardsSortedAceHigh(arg0.getCards());
			for (int x = 0; x < getCards().size(); x++) {
				Card card = sortedCards.get(x);
				Card otherCard = otherSortedCards.get(x);
				int cardCompare = (card.getValue() == 1 ? 14 : card.getValue())
						- (otherCard.getValue() == 1 ? 14 : otherCard.getValue());
				if (cardCompare != 0) {
					return cardCompare;
				}
			}
		}
		return 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Card card : cards) {
			sb.append(card);
		}
		sb.append(" (" + WordUtils.capitalizeFully(String.valueOf(handType).replace("_", " ")) + ")");
		return sb.toString();
	}
}
