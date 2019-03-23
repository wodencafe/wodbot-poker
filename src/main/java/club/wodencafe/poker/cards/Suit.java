package club.wodencafe.poker.cards;

public enum Suit {
	SPADE("♤", Color.BLACK), HEART("♡", Color.RED), DIAMOND("♢", Color.RED), CLUB("♧", Color.BLACK), JOKER("🃏", Color.WILD);
	
	private String suitIcon;
	private Color color;
	private Suit(String suitIcon, Color color) {
		this.suitIcon = suitIcon;
		this.color = color;
	}
	public String getSuitIcon() {
		return suitIcon;
	}
	public Color getColor() {
		return color;
	}
}
