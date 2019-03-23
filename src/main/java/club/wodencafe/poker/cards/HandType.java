package club.wodencafe.poker.cards;

public enum HandType {
	HIGH(1), 
	PAIR(2), 
	TWO_PAIR(3), 
	TRIPS(4), 
	STRAIGHT(5), 
	FLUSH(6), 
	FULL_HOUSE(7),
	FOUR(8), 
	STRAIGHT_FLUSH(9), 
	ROYAL_FLUSH(10);
	
	private int handValue;
	
	private HandType(int handValue) {
		this.handValue = handValue;
	}
	
	public int getValue() {
		return handValue;
	}
}
