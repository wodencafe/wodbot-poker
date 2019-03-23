package club.wodencafe.poker.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class Deck implements Supplier<Optional<Card>> {
	private List<Card> cards;
	private Deck() {
		cards = new ArrayList<>();
	}
	private void addCard(Card card) {
		cards.add(card);
	}
	public boolean hasCards() {
		return !cards.isEmpty();
	}
	public static Deck generateDeck(boolean includeJokers) {
		Deck deck = new Deck();
		
		deck.generate(includeJokers);
		
		deck.randomizeDeck();
		
		return deck;
	}
	
	private void generate(boolean includeJokers) {
		for (int x = 1; x < 14; x++) {
			for (Suit suit : Suit.values()) {
				if (suit != Suit.JOKER) {
					Card card = Card.getCard(suit, x);		
					addCard(card);
				}
			}
		}
		if (includeJokers) {
			Card card = Card.getCard(Suit.JOKER, -1);
			addCard(card);
		}
	}
	private void randomizeDeck() {
		Collections.shuffle(cards);
	}
	@Override
	public Optional<Card> get() {
		if (cards.isEmpty()) {
			return Optional.empty();
		}
		else {
			return Optional.of(cards.remove(0));
		}
	}
}
