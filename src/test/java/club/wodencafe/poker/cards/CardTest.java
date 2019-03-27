package club.wodencafe.poker.cards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static java.util.Objects.*;
import static club.wodencafe.poker.cards.Suit.*;
import static club.wodencafe.poker.cards.Card.*;
import static org.junit.Assert.*;
import static java.util.Arrays.*;
import org.junit.Assert;
import org.junit.Test;

import club.wodencafe.poker.cards.hands.Hand;

public class CardTest {
	
	private Hand getHand(HandType expectedHandType, List<Card> cardsInput) {

		Hand hand = HandUtil.getHand(cardsInput);
		
		boolean handsEqual = expectedHandType == hand.getHandType();
		
		if (!handsEqual) {
			Assert.fail("Hand Types are not equal." +
				System.lineSeparator() +
				"Expected " + expectedHandType + ", " +
				System.lineSeparator() + 
				"Got " + hand.getHandType());
		}
		
		List<Card> handCards = hand.getCards();
		
		Assert.assertEquals("Returned hand size is incorrect.", handCards.size(), 5);
		
		return hand;
	}

	@Test
	public void testHigh() throws Exception {

		Card aceSpades = getCard(SPADE, 1);
		Card nineClubs = getCard(CLUB, 9);
		Card fiveHearts = getCard(HEART, 5);
		Card threeClubs = getCard(CLUB, 3);
		Card twoSpades = getCard(SPADE, 2);
		
		List<Card> cardsInput = asList(
			twoSpades,
			fiveHearts,
			nineClubs,
			threeClubs,
			aceSpades);

		Hand hand = getHand(HandType.HIGH, cardsInput);
		
		List<Card> cardsOutput = hand.getCards();

		assertEquals(cardsOutput.get(0), aceSpades);
		assertEquals(cardsOutput.get(1), nineClubs);
		assertEquals(cardsOutput.get(2), fiveHearts);
		assertEquals(cardsOutput.get(3), threeClubs);
		assertEquals(cardsOutput.get(4), twoSpades);
	}
	
	@Test
	public void testRoyalFlush() throws Exception {

		Card aceSpades = getCard(SPADE, 1);
		Card kingSpades = getCard(SPADE, 13);
		Card queenSpades = getCard(SPADE, 12);
		Card jackSpades = getCard(SPADE, 11);
		Card tenSpades = getCard(SPADE, 10);
		
		List<Card> cardsInput = asList(
			jackSpades,
			queenSpades,
			kingSpades,
			tenSpades,
			aceSpades);

		Hand hand = getHand(HandType.ROYAL_FLUSH, cardsInput);
		
		List<Card> cardsOutput = hand.getCards();

		assertEquals(cardsOutput.get(0), aceSpades);
		assertEquals(cardsOutput.get(1), kingSpades);
		assertEquals(cardsOutput.get(2), queenSpades);
		assertEquals(cardsOutput.get(3), jackSpades);
		assertEquals(cardsOutput.get(4), tenSpades);
	}

	
	@Test
	public void testStraightFlush() throws Exception {

		Card tenSpades = getCard(SPADE, 10);
		Card nineSpades = getCard(SPADE, 9);
		Card eightSpades = getCard(SPADE, 8);
		Card sevenSpades = getCard(SPADE, 7);
		Card sixSpades = getCard(SPADE, 6);
		
		List<Card> cardsInput = asList(
			eightSpades,
			sevenSpades,
			tenSpades,
			nineSpades,
			sixSpades);

		Hand hand = getHand(HandType.STRAIGHT_FLUSH, cardsInput);
		
		List<Card> cardsOutput = hand.getCards();

		assertEquals(cardsOutput.get(0), tenSpades);
		assertEquals(cardsOutput.get(1), nineSpades);
		assertEquals(cardsOutput.get(2), eightSpades);
		assertEquals(cardsOutput.get(3), sevenSpades);
		assertEquals(cardsOutput.get(4), sixSpades);
	}

	
	@Test
	public void testStraight() throws Exception {

		Card tenSpades = getCard(SPADE, 10);
		Card nineClubs = getCard(CLUB, 9);
		Card eightHearts = getCard(HEART, 8);
		Card sevenSpades = getCard(SPADE, 7);
		Card sixSpades = getCard(SPADE, 6);
		
		List<Card> cardsInput = asList(
			eightHearts,
			sevenSpades,
			tenSpades,
			nineClubs,
			sixSpades);

		Hand hand = getHand(HandType.STRAIGHT, cardsInput);
		
		List<Card> cardsOutput = hand.getCards();

		assertEquals(cardsOutput.get(0), tenSpades);
		assertEquals(cardsOutput.get(1), nineClubs);
		assertEquals(cardsOutput.get(2), eightHearts);
		assertEquals(cardsOutput.get(3), sevenSpades);
		assertEquals(cardsOutput.get(4), sixSpades);
	}
	
	
	@Test
	public void testTwoPair() throws Exception {

		Card twoClubs = getCard(CLUB, 2);
		Card twoDiamonds = getCard(DIAMOND, 2);
		Card fiveClubs = getCard(CLUB, 5);
		Card fiveSpades = getCard(SPADE, 5);
		Card kingHearts = getCard(HEART, 13);
		
		List<Card> cardsInput = asList(
			twoClubs,
			fiveClubs,
			fiveSpades,
			twoDiamonds,
			kingHearts);

		Hand hand = getHand(HandType.TWO_PAIR, cardsInput);
		
		List<Card> cardsOutput = hand.getCards();

		assertEquals(cardsOutput.get(0).getValue(), 5);
		assertEquals(cardsOutput.get(1).getValue(), 5);
		assertEquals(cardsOutput.get(2).getValue(), 2);
		assertEquals(cardsOutput.get(3).getValue(), 2);
		assertEquals(cardsOutput.get(4), kingHearts);
	}

	@Test
	public void testQuads() throws Exception {

		Card twoClubs = getCard(CLUB, 2);
		Card twoDiamonds = getCard(DIAMOND, 2);
		Card twoHearts = getCard(HEART, 2);
		Card twoSpades = getCard(SPADE, 2);
		Card nineHearts = getCard(HEART, 9);
		
		List<Card> cardsInput = asList(
			twoClubs,
			nineHearts,
			twoHearts,
			twoSpades,
			twoDiamonds);

		Hand hand = getHand(HandType.FOUR, cardsInput);
		
		List<Card> cardsOutput = hand.getCards();

		assertEquals(cardsOutput.get(0).getValue(), 2);
		assertEquals(cardsOutput.get(1).getValue(), 2);
		assertEquals(cardsOutput.get(2).getValue(), 2);
		assertEquals(cardsOutput.get(3).getValue(), 2);
		assertEquals(cardsOutput.get(4).getValue(), 9);
	}
	
	@Test
	public void testFullHouse() throws Exception {

		Card twoClubs = getCard(CLUB, 2);
		Card twoDiamonds = getCard(DIAMOND, 2);
		Card twoHearts = getCard(HEART, 2);
		Card nineSpades = getCard(SPADE, 9);
		Card nineHearts = getCard(HEART, 9);
		
		List<Card> cardsInput = asList(
			twoClubs,
			twoHearts,
			nineSpades,
			twoDiamonds,
			nineHearts);

		Hand hand = getHand(HandType.FULL_HOUSE, cardsInput);
		
		List<Card> cardsOutput = hand.getCards();

		assertEquals(cardsOutput.get(0).getValue(), 2);
		assertEquals(cardsOutput.get(1).getValue(), 2);
		assertEquals(cardsOutput.get(2).getValue(), 2);
		assertEquals(cardsOutput.get(3).getValue(), 9);
		assertEquals(cardsOutput.get(4).getValue(), 9);
	}
	
	@Test
	public void testTrips() throws Exception {

		Card twoClubs = getCard(CLUB, 2);
		Card twoDiamonds = getCard(DIAMOND, 2);
		Card twoHearts = getCard(HEART, 2);
		Card nineSpades = getCard(SPADE, 9);
		Card kingHearts = getCard(HEART, 13);
		
		List<Card> cardsInput = asList(
			twoClubs,
			twoHearts,
			nineSpades,
			twoDiamonds,
			kingHearts);

		Hand hand = getHand(HandType.TRIPS, cardsInput);
		
		List<Card> cardsOutput = hand.getCards();

		assertEquals(cardsOutput.get(0).getValue(), 2);
		assertEquals(cardsOutput.get(1).getValue(), 2);
		assertEquals(cardsOutput.get(2).getValue(), 2);
		assertEquals(cardsOutput.get(3), kingHearts);
		assertEquals(cardsOutput.get(4), nineSpades);
	}
	
	
	@Test
	public void testPair() throws Exception {

		Card twoClubs = getCard(CLUB, 2);
		Card twoDiamonds = getCard(DIAMOND, 2);
		Card fiveClubs = getCard(CLUB, 5);
		Card nineSpades = getCard(SPADE, 9);
		Card kingHearts = getCard(HEART, 13);
		
		List<Card> cardsInput = asList(
			twoClubs,
			fiveClubs,
			nineSpades,
			twoDiamonds,
			kingHearts);

		Hand hand = getHand(HandType.PAIR, cardsInput);
		
		List<Card> cardsOutput = hand.getCards();

		assertEquals(cardsOutput.get(0).getValue(), 2);
		assertEquals(cardsOutput.get(1).getValue(), 2);
		assertEquals(cardsOutput.get(2), kingHearts);
		assertEquals(cardsOutput.get(3), nineSpades);
		assertEquals(cardsOutput.get(4), fiveClubs);
	}
	
	@Test
	public void testFlush() throws Exception {
		
		Card aceDiamonds = getCard(DIAMOND, 1);
		Card twoDiamonds = getCard(DIAMOND, 2);
		Card fiveDiamonds = getCard(DIAMOND, 5);
		Card nineDiamonds = getCard(DIAMOND, 9);
		Card kingDiamonds = getCard(DIAMOND, 13);

		List<Card> cardsInput = Arrays.asList(
			aceDiamonds,
			twoDiamonds,
			fiveDiamonds,
			nineDiamonds,
			kingDiamonds);
		
		List<Card> cardsExpected = Arrays.asList(
			aceDiamonds,
			kingDiamonds,
			nineDiamonds,
			fiveDiamonds,
			twoDiamonds);

		Hand hand = getHand(HandType.FLUSH, cardsInput);
		
		List<Card> cardsOutput = hand.getCards();

		for (int x = 0; x < cardsExpected.size(); x++) {
			assertEquals(cardsOutput.get(x), cardsExpected.get(x));
		}
	}
	
	@Test
	public void testFlushWithWilds() throws Exception {
		
		Card joker = getJoker();
		Card twoDiamonds = getCard(DIAMOND, 2);
		Card fiveDiamonds = getCard(DIAMOND, 5);
		Card nineDiamonds = getCard(DIAMOND, 9);
		Card kingDiamonds = getCard(DIAMOND, 13);

		List<Card> cardsInput = Arrays.asList(
			joker,
			twoDiamonds,
			fiveDiamonds,
			nineDiamonds,
			kingDiamonds);
		
		List<Card> cardsExpected = Arrays.asList(
			joker,
			kingDiamonds,
			nineDiamonds,
			fiveDiamonds,
			twoDiamonds);

		Hand hand = getHand(HandType.FLUSH, cardsInput);
		
		List<Card> cardsOutput = hand.getCards();

		for (int x = 0; x < cardsExpected.size(); x++) {
			assertEquals(cardsOutput.get(x), cardsExpected.get(x));
		}
	}
	
	@Test
	public void testStraightWithWilds() throws Exception {
		List<Card> cards = new ArrayList<>();
		cards.add(Card.getCard(Suit.DIAMOND, 2));
		cards.add(Card.getCard(Suit.DIAMOND, 3));
		cards.add(Card.getCard(Suit.CLUB, 4));
		cards.add(Card.getCard(Suit.DIAMOND, 5));
		cards.add(Card.getCard(Suit.HEART, 6));
		
		Hand hand = HandUtil.getHand(cards);
		
		Assert.assertTrue(hand.getHandType() == HandType.STRAIGHT);

		cards = new ArrayList<>();
		cards.add(Card.getCard(Suit.DIAMOND, 2));
		cards.add(Card.getCard(Suit.DIAMOND, 3));
		cards.add(Card.getCard(Suit.CLUB, 5));
		cards.add(Card.getCard(Suit.DIAMOND, 6));
		cards.add(Card.getCard(Suit.DIAMOND, 7));

		hand = HandUtil.getHand(cards);
		
		Assert.assertTrue(hand.getHandType() != HandType.STRAIGHT);

		cards = new ArrayList<>();
		cards.add(Card.getCard(Suit.DIAMOND, 2));
		cards.add(Card.getCard(Suit.DIAMOND, 3));
		cards.add(Card.getCard(Suit.DIAMOND, 5));
		cards.add(Card.getCard(Suit.DIAMOND, 6));
		cards.add(Card.getCard(Suit.DIAMOND, 7));
		cards.add(Card.getCard(Suit.DIAMOND, 9));
		cards.add(Card.getCard(Suit.DIAMOND, 10));
		cards.add(Card.getCard(Suit.DIAMOND, 11));

		hand = HandUtil.getHand(cards);
		
		Assert.assertTrue(hand.getHandType() != HandType.STRAIGHT);

		cards = new ArrayList<>();
		cards.add(Card.getCard(Suit.HEART, 2));
		cards.add(Card.getCard(Suit.DIAMOND, 3));
		cards.add(Card.getCard(Suit.SPADE, 5));
		cards.add(Card.getCard(Suit.SPADE, 6));
		cards.add(Card.getCard(Suit.DIAMOND, 7));
		cards.add(Card.getCard(Suit.JOKER, -1));
		cards.add(Card.getCard(Suit.CLUB, 9));
		cards.add(Card.getCard(Suit.DIAMOND, 10));
		cards.add(Card.getCard(Suit.CLUB, 11));


		hand = HandUtil.getHand(cards);
		
		Assert.assertTrue(hand.getHandType() == HandType.STRAIGHT);
		

		cards = new ArrayList<>();

		cards.add(Card.getCard(Suit.DIAMOND, 13));
		cards.add(Card.getCard(Suit.SPADE, 12));
		cards.add(Card.getCard(Suit.HEART, 6));
		cards.add(Card.getCard(Suit.CLUB, 5));
		cards.add(Card.getCard(Suit.DIAMOND, 4));
		cards.add(Card.getCard(Suit.JOKER, -1));
		cards.add(Card.getCard(Suit.JOKER, -1));

		hand = HandUtil.getHand(cards);
		
		Assert.assertTrue(hand.getHandType() == HandType.STRAIGHT);

		cards = new ArrayList<>();

		cards.add(Card.getCard(Suit.JOKER, -1));
		cards.add(Card.getCard(Suit.JOKER, -1));
		cards.add(Card.getCard(Suit.DIAMOND, 12));
		cards.add(Card.getCard(Suit.SPADE, 11));
		cards.add(Card.getCard(Suit.CLUB, 10));
		cards.add(Card.getCard(Suit.HEART, 5));
		cards.add(Card.getCard(Suit.DIAMOND, 4));

		hand = HandUtil.getHand(cards);
		
		Assert.assertTrue(hand.getHandType() == HandType.STRAIGHT);
		
		List<Card> handCards = hand.getCards();
		
		Assert.assertTrue(handCards.size() > 0);
		
		for (int x = 0; x < 5; x++) {
			Assert.assertTrue(handCards.get(x).equals(cards.get(x)));
			
		}
	}
}
