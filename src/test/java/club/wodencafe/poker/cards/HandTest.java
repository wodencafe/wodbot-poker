package club.wodencafe.poker.cards;

import static club.wodencafe.poker.cards.Suit.*;
import static club.wodencafe.poker.cards.Card.*;
import static club.wodencafe.poker.cards.HandUtil.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import club.wodencafe.poker.cards.hands.Hand;

import static java.util.Arrays.*;

public class HandTest {
	
	@Test
	public void testHands() throws Exception {

		Hand nothingHand = getNothingHand();
		
		Hand pairHand = getPairHand();
		
		Assert.assertTrue(pairHand.compareTo(nothingHand) > 0);
		
		Hand twoPairHand = getTwoPairHand();

		Assert.assertTrue(twoPairHand.compareTo(pairHand) > 0);
		
		Hand tripsHand = getTripsHand();
		
		Assert.assertTrue(tripsHand.compareTo(twoPairHand) > 0);
		
		Hand straightHand = getStraightHand();

		Assert.assertTrue(straightHand.compareTo(tripsHand) > 0);
		
		Hand flushHand = getFlushHand();
		
		Assert.assertTrue(flushHand.compareTo(straightHand) > 0);
		
		Hand fullHouseHand = getFullHouseHand();

		Assert.assertTrue(fullHouseHand.compareTo(flushHand) > 0);
		
		Hand quadsHand = getQuadsHand();

		Assert.assertTrue(quadsHand.compareTo(fullHouseHand) > 0);
		
		Hand straightFlushHand = getStraightFlushHand();
		
		Assert.assertTrue(straightFlushHand.compareTo(quadsHand) > 0);
		
		Hand royalFlushHand = getRoyalFlushHand();

		Assert.assertTrue(royalFlushHand.compareTo(straightFlushHand) > 0);
		
		
	}
	
	private Hand getHigherStraightHand() {
		Card jackSpades = getCard(SPADE, 11);
		Card tenSpades = getCard(SPADE, 10);
		Card nineClubs = getCard(CLUB, 9);
		Card eightHearts = getCard(HEART, 8);
		Card sevenSpades = getCard(SPADE, 7);
		
		List<Card> cardsInput = asList(
			eightHearts,
			sevenSpades,
			tenSpades,
			nineClubs,
			jackSpades);

		return getHand(cardsInput);
	}
	
	@Test
	public void testHighLowHands() throws Exception {
		Hand highHand;
		
		Hand lowHand;
		
		highHand = getHigherNothingHand();
		
		lowHand = getNothingHand();
		
		Assert.assertTrue(highHand.compareTo(lowHand) > 0);
				
		highHand = getHigherPairHand();
		
		lowHand = getPairHand();
		
		Assert.assertTrue(highHand.compareTo(lowHand) > 0);
		
		highHand = getHigherTwoPairHand();
		
		lowHand = getTwoPairHand();
		
		Assert.assertTrue(highHand.compareTo(lowHand) > 0);

		highHand = getHigherTripsHand();
		
		lowHand = getTripsHand();
		
		Assert.assertTrue(highHand.compareTo(lowHand) > 0);
		
		highHand = getHigherStraightHand();
		
		lowHand = getStraightHand();
		
		Assert.assertTrue(highHand.compareTo(lowHand) > 0);

		highHand = getHigherFlushHand();
		
		lowHand = getFlushHand();
		
		Assert.assertTrue(highHand.compareTo(lowHand) > 0);
		
		highHand = getHigherFullHouseHand();
		
		lowHand = getFullHouseHand();
		
		Assert.assertTrue(highHand.compareTo(lowHand) > 0);
		
		highHand = getHigherQuadsHand();
		
		lowHand = getQuadsHand();
		
		Assert.assertTrue(highHand.compareTo(lowHand) > 0);
		
		highHand = getHigherStraightFlushHand();
		
		lowHand = getStraightFlushHand();

		Assert.assertTrue(highHand.compareTo(lowHand) > 0);
		
	}
	
	private Hand getRoyalFlushHand() {
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

		return getHand(cardsInput);
	}
	private Hand getHigherStraightFlushHand() {
		Card jackSpades = getCard(SPADE, 11);
		Card tenSpades = getCard(SPADE, 10);
		Card nineSpades = getCard(SPADE, 9);
		Card eightSpades = getCard(SPADE, 8);
		Card sevenSpades = getCard(SPADE, 7);
		
		List<Card> cardsInput = asList(
			eightSpades,
			sevenSpades,
			tenSpades,
			nineSpades,
			jackSpades);

		return getHand(cardsInput);
	}
	private Hand getStraightFlushHand() {
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

		return getHand(cardsInput);
	}

	private Hand getHigherQuadsHand() {
		Card threeClubs = getCard(CLUB, 3);
		Card threeDiamonds = getCard(DIAMOND, 3);
		Card threeHearts = getCard(HEART, 3);
		Card threeSpades = getCard(SPADE, 3);
		Card nineHearts = getCard(HEART, 9);
		
		List<Card> cardsInput = asList(
			threeClubs,
			nineHearts,
			threeHearts,
			threeSpades,
			threeDiamonds);

		return getHand(cardsInput);
	}
	private Hand getQuadsHand() {
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

		return getHand(cardsInput);
	}

	private Hand getHigherFlushHand() {
		Card aceDiamonds = getCard(DIAMOND, 1);
		Card jackDiamonds = getCard(DIAMOND, 11);
		Card queenDiamonds = getCard(DIAMOND, 12);
		Card nineDiamonds = getCard(DIAMOND, 9);
		Card kingDiamonds = getCard(DIAMOND, 13);

		List<Card> cardsInput = Arrays.asList(
			aceDiamonds,
			jackDiamonds,
			queenDiamonds,
			nineDiamonds,
			kingDiamonds);

		return getHand(cardsInput);
	}
	private Hand getFlushHand() {
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

		return getHand(cardsInput);
	}
	private Hand getStraightHand() {
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

		return getHand(cardsInput);
	}

	private Hand getHigherFullHouseHand() {
		Card twoClubs = getCard(CLUB, 2);
		Card twoDiamonds = getCard(DIAMOND, 2);
		Card twoHearts = getCard(HEART, 2);
		Card tenSpades = getCard(SPADE, 10);
		Card tenHearts = getCard(HEART, 10);
		
		List<Card> cardsInput = asList(
			twoClubs,
			twoHearts,
			tenSpades,
			twoDiamonds,
			tenHearts);

		return HandUtil.getHand(cardsInput);
	}

	private Hand getFullHouseHand() {
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

		return HandUtil.getHand(cardsInput);
	}

	private Hand getTwoPairHand() {
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

		return getHand(cardsInput);
	}

	private Hand getHigherTwoPairHand() {
		Card threeClubs = getCard(CLUB, 3);
		Card threeDiamonds = getCard(DIAMOND, 3);
		Card fiveClubs = getCard(CLUB, 5);
		Card fiveSpades = getCard(SPADE, 5);
		Card kingHearts = getCard(HEART, 13);
		
		List<Card> cardsInput = asList(
			threeClubs,
			fiveClubs,
			fiveSpades,
			threeDiamonds,
			kingHearts);

		return getHand(cardsInput);
	}

	private Hand getHigherNothingHand() {
		Card aceSpades = getCard(SPADE, 1);
		Card tenClubs = getCard(CLUB, 10);
		Card fiveHearts = getCard(HEART, 5);
		Card threeClubs = getCard(CLUB, 3);
		Card twoSpades = getCard(SPADE, 2);
		

		List<Card> cardsInput = asList(
			twoSpades,
			fiveHearts,
			tenClubs,
			threeClubs,
			aceSpades);
		
		return HandUtil.getHand(cardsInput);
	}
	
	private Hand getNothingHand() {
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
		
		return HandUtil.getHand(cardsInput);
	}

	private Hand getHigherPairHand() {
		Card aceSpades = getCard(SPADE, 1);
		Card aceClubs = getCard(CLUB, 1);
		Card fiveHearts = getCard(HEART, 5);
		Card threeClubs = getCard(CLUB, 3);
		Card twoSpades = getCard(SPADE, 2);
		

		List<Card> cardsInput = asList(
			twoSpades,
			fiveHearts,
			aceClubs,
			threeClubs,
			aceSpades);
		
		return HandUtil.getHand(cardsInput);
	}
	
	private Hand getPairHand() {
		Card sixSpades = getCard(SPADE, 6);
		Card sixClubs = getCard(CLUB, 6);
		Card fiveHearts = getCard(HEART, 5);
		Card threeClubs = getCard(CLUB, 3);
		Card twoSpades = getCard(SPADE, 2);
		

		List<Card> cardsInput = asList(
			twoSpades,
			fiveHearts,
			sixClubs,
			threeClubs,
			sixSpades);
		
		return HandUtil.getHand(cardsInput);
	}

	private Hand getHigherTripsHand() {
		Card threeClubs = getCard(CLUB, 3);
		Card threeDiamonds = getCard(DIAMOND, 3);
		Card threeHearts = getCard(HEART, 3);
		Card nineSpades = getCard(SPADE, 9);
		Card kingHearts = getCard(HEART, 13);
		
		List<Card> cardsInput = asList(
			threeClubs,
			threeHearts,
			nineSpades,
			threeDiamonds,
			kingHearts);

		return HandUtil.getHand(cardsInput);
	}
	private Hand getTripsHand() {
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

		return HandUtil.getHand(cardsInput);
	}
}
