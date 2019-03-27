package club.wodencafe.poker.cards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

public class CardTest {
	/*@Test
	public void test() throws Exception {
		Deck deck = Deck.generateDeck(false);
		
		while (deck.hasCards()) {
			Optional<Card> optionalCard = deck.get();
			
			Card card = optionalCard.get();
			
			System.out.println(card);
		}
	}*/
	
	@Test
	public void testStraight() throws Exception {
		List<Card> cards = new ArrayList<>();
		cards.add(Card.getCard(Suit.DIAMOND, 2));
		cards.add(Card.getCard(Suit.DIAMOND, 3));
		cards.add(Card.getCard(Suit.DIAMOND, 4));
		cards.add(Card.getCard(Suit.DIAMOND, 5));
		cards.add(Card.getCard(Suit.DIAMOND, 6));
		
		boolean isStraight = HandUtil.getStraight(cards).size() > 0;
		
		Assert.assertTrue(isStraight);

		cards = new ArrayList<>();
		cards.add(Card.getCard(Suit.DIAMOND, 2));
		cards.add(Card.getCard(Suit.DIAMOND, 3));
		cards.add(Card.getCard(Suit.DIAMOND, 5));
		cards.add(Card.getCard(Suit.DIAMOND, 6));
		cards.add(Card.getCard(Suit.DIAMOND, 7));

		isStraight = HandUtil.getStraight(cards).size() > 0;
		
		Assert.assertFalse(isStraight);

		cards = new ArrayList<>();
		cards.add(Card.getCard(Suit.DIAMOND, 2));
		cards.add(Card.getCard(Suit.DIAMOND, 3));
		cards.add(Card.getCard(Suit.DIAMOND, 5));
		cards.add(Card.getCard(Suit.DIAMOND, 6));
		cards.add(Card.getCard(Suit.DIAMOND, 7));
		cards.add(Card.getCard(Suit.DIAMOND, 9));
		cards.add(Card.getCard(Suit.DIAMOND, 10));
		cards.add(Card.getCard(Suit.DIAMOND, 11));

		isStraight = HandUtil.getStraight(cards).size() > 0;
		Assert.assertFalse(isStraight);
		

		cards = new ArrayList<>();
		cards.add(Card.getCard(Suit.DIAMOND, 2));
		cards.add(Card.getCard(Suit.DIAMOND, 3));
		cards.add(Card.getCard(Suit.DIAMOND, 5));
		cards.add(Card.getCard(Suit.DIAMOND, 6));
		cards.add(Card.getCard(Suit.DIAMOND, 7));
		cards.add(Card.getCard(Suit.JOKER, -1));
		cards.add(Card.getCard(Suit.DIAMOND, 9));
		cards.add(Card.getCard(Suit.DIAMOND, 10));
		cards.add(Card.getCard(Suit.DIAMOND, 11));

		isStraight = HandUtil.getStraight(cards).size() > 0;
		Assert.assertTrue(isStraight);
		

		cards = new ArrayList<>();

		cards.add(Card.getCard(Suit.DIAMOND, 13));
		cards.add(Card.getCard(Suit.DIAMOND, 12));
		cards.add(Card.getCard(Suit.DIAMOND, 6));
		cards.add(Card.getCard(Suit.DIAMOND, 5));
		cards.add(Card.getCard(Suit.DIAMOND, 4));
		cards.add(Card.getCard(Suit.JOKER, -1));
		cards.add(Card.getCard(Suit.JOKER, -1));
		isStraight = HandUtil.getStraight(cards).size() > 0;
		Assert.assertTrue(isStraight);

		cards = new ArrayList<>();

		cards.add(Card.getCard(Suit.JOKER, -1));
		cards.add(Card.getCard(Suit.JOKER, -1));
		cards.add(Card.getCard(Suit.DIAMOND, 12));
		cards.add(Card.getCard(Suit.DIAMOND, 11));
		cards.add(Card.getCard(Suit.DIAMOND, 10));
		cards.add(Card.getCard(Suit.DIAMOND, 5));
		cards.add(Card.getCard(Suit.DIAMOND, 4));
		List<Card> handCards = new ArrayList<>(HandUtil.getStraight(cards));
		Assert.assertTrue(handCards.size() > 0);
		
		for (int x = 0; x < 5; x++) {
			Assert.assertTrue(handCards.get(x).equals(cards.get(x)));
			
		}
	}
}
