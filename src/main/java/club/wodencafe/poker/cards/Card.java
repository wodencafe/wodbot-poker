package club.wodencafe.poker.cards;

public class Card implements Comparable<Card> {
	private Suit suit;
	private int value;
	private Card(Suit suit, int value) {
		this.suit = suit;
		this.value = value;
	}
	public Suit getSuit() {
		return suit;
	}
	public int getValue() {
		return value;
	}
	static Card getCard(Suit suit, int value) {
		return new Card(suit, value);
	}
	@Override
	public String toString() {
		if (suit == Suit.JOKER) {
			return suit.getSuitIcon();
		}
		else {
			String valueStr;
			if (value == 1) {
				valueStr = "A";
			}
			else if (value == 11) {
				valueStr = "J";
			}
			else if (value == 12) {
				valueStr = "Q";
			}
			else if (value == 13) {
				valueStr = "K";
			}
			else {
				valueStr = String.valueOf(value);
			}
			return "[" + valueStr + " " + suit.getSuitIcon() + "]";
		}
	}
	@Override
	public int compareTo(Card arg0) {
		return arg0.getValue() -getValue();
	}
}
