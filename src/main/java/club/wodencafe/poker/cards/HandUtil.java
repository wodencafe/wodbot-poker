package club.wodencafe.poker.cards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import club.wodencafe.poker.cards.hands.Hand;

public class HandUtil {

	private static final XLogger logger = XLoggerFactory.getXLogger(HandUtil.class);
	private static Collection<HandType> handTypes = Arrays.asList(HandType.values()).stream()
			.sorted(Collections.reverseOrder()).collect(Collectors.toList());

	public static Hand getHand(Collection<Card> cards) {
		logger.entry(cards);
		Hand hand = null;
		try {
			List<Card> cardValues = new ArrayList<>(getCardsSortedAceHigh(cards));

			cards.stream().filter(card -> card.getSuit() == Suit.JOKER).filter(card -> !cardValues.contains(card))
					.forEach(cardValues::add);

			List<Card> handCards = new ArrayList<>();
			for (HandType handType : handTypes) {
				switch (handType) {
				case ROYAL_FLUSH: {
					handCards.addAll(getRoyalFlush(cardValues));
				}
					break;
				case STRAIGHT_FLUSH: {
					handCards.addAll(getStraightFlush(cardValues));
				}
					break;
				case FOUR_OF_A_KIND: {
					handCards.addAll(getQuads(cardValues));
				}
					break;
				case FULL_HOUSE: {
					handCards.addAll(getFullHouse(cardValues));
				}
					break;
				case FLUSH: {
					handCards.addAll(getFlush(cardValues));
				}
					break;
				case STRAIGHT: {
					handCards.addAll(getStraight(cardValues));
				}
					break;
				case THREE_OF_A_KIND: {
					handCards.addAll(getTrips(cardValues));
				}
					break;
				case TWO_PAIR: {
					handCards.addAll(getTwoPair(cardValues));
				}
					break;
				case PAIR: {
					handCards.addAll(getPair(cardValues));
				}
					break;
				}
				if (handCards.size() > 0) {
					if (handCards.size() < 5) {
						cardValues.removeAll(handCards);

						cardValues.stream().limit(5 - handCards.size()).forEach(handCards::add);

					}
					hand = new Hand(handCards, handType);
					break;
				}
			}
			if (hand == null) {
				handCards = cardValues.stream().limit(5).collect(Collectors.toList());
				hand = new Hand(handCards, HandType.HIGH_CARD);
			}
		} catch (Throwable th) {
			RuntimeException e = new RuntimeException(th);
			logger.throwing(e);
			throw e;
		} finally {
			logger.exit(hand);
		}
		return hand;
	}

	private static Collection<Card> getRoyalFlush(Collection<Card> cards) {

		Collection<Card> topFiveCards = getStraightFlush(cards);

		if (topFiveCards.size() == 5) {

			List<Integer> straightFlush = Arrays.asList(10, 11, 12, 13, 1);

			if (topFiveCards.stream().map(Card::getValue).filter(straightFlush::contains).count() == 5) {
				return getCardsSortedAceHigh(topFiveCards);
			}
		}
		return new ArrayList<>();

	}

	private static Collection<Card> getQuads(Collection<Card> cards) {

		return getDuplicates(cards, 4);
	}

	private static Collection<Card> getAndRemoveJokers(Collection<Card> cards) {
		List<Card> jokers = new ArrayList<>();

		List<Runnable> runnables = new ArrayList<>();

		for (Card card : cards) {
			if (card.getSuit() == Suit.JOKER) {
				runnables.add(() -> cards.remove(card));
				jokers.add(card);
			}
		}

		for (Runnable runnable : runnables) {
			runnable.run();
		}

		return jokers;
	}

	private static Map<Integer, Collection<Card>> getCardGroups(Collection<Card> cards) {

		Map<Integer, Collection<Card>> cardValuesFound = new HashMap<>();

		for (Card card : cards) {

			int cardValue = card.getValue();

			Collection<Card> collection = cardValuesFound.get(cardValue);

			if (collection == null) {
				collection = new ArrayList<Card>(Arrays.asList(card));

				cardValuesFound.put(cardValue, collection);
			} else {

				collection.add(card);
			}
		}

		return cardValuesFound;
	}

	private static Collection<Card> getDuplicates(Collection<Card> cards, int count) {

		List<Card> cardValues = new ArrayList<>(getCardsSortedAceHigh(cards));

		Collection<Card> jokers = getAndRemoveJokers(cardValues);

		List<Card> returnCards = new ArrayList<>();

		Map<Integer, Collection<Card>> cardGroups = new TreeMap<>(Collections.reverseOrder());

		cardGroups.putAll(getCardGroups((cardValues)));

		for (Collection<Card> collection : cardGroups.values()) {
			int collectionSize = collection.size();

			int adjCollectionSize = collectionSize + jokers.size();

			if (adjCollectionSize >= count) {

				returnCards.addAll(jokers);
				returnCards.addAll(collection);

				break;
			}
		}

		return returnCards;
	}

	private static Collection<Card> getTwoPair(Collection<Card> cards) {
		Collection<Card> pair = getPair(cards);

		if (pair.isEmpty()) {
			return pair;
		}

		List<Card> cardValues = cards.stream().filter(x -> !pair.contains(x)).collect(Collectors.toList());

		Collection<Card> pair2 = getPair(cardValues);

		if (pair2.isEmpty()) {
			return pair2;
		}

		Collection<Card> returnCards = new ArrayList<>(pair);
		returnCards.addAll(pair2);

		return returnCards;
	}

	private static Collection<Card> getStraightFlush(Collection<Card> cards) {
		Collection<Card> flush = getFlush(cards);

		if (!flush.isEmpty()) {
			flush = getStraight(cards);
			if (!flush.isEmpty()) {
				return flush;
			}
		}

		return new ArrayList<>();
	}

	private static Collection<Card> getFullHouse(Collection<Card> cards) {

		Collection<Card> trips = getTrips(cards);

		if (trips.isEmpty()) {
			return trips;
		}

		List<Card> cardValues = cards.stream().filter(x -> !trips.contains(x)).collect(Collectors.toList());

		Collection<Card> pair2 = getPair(cardValues);

		if (pair2.isEmpty()) {
			return pair2;
		}

		Collection<Card> returnCards = new ArrayList<>(trips);
		returnCards.addAll(pair2);

		return returnCards;
	}

	private static Collection<Card> getTrips(Collection<Card> cards) {

		return getDuplicates(cards, 3);
	}

	private static Collection<Card> getPair(Collection<Card> cards) {

		return getDuplicates(cards, 2);
	}

	private static Collection<Card> getFlush(Collection<Card> cards) {

		List<Card> cardValues = getCardsSortedAceHigh(cards);

		List<Suit> suits = Arrays.asList(Suit.HEART, Suit.DIAMOND, Suit.SPADE, Suit.CLUB);

		for (Suit suit : suits) {
			List<Card> returnCards = cardValues.stream()
					.filter(card -> card.getSuit() == suit || card.getSuit() == Suit.JOKER).limit(5)
					.collect(Collectors.toList());

			if (returnCards.size() == 5) {
				return returnCards;
			}
		}
		return Collections.emptyList();
	}

	public static List<Card> getCardsSortedAceHigh(Collection<Card> cards) {

		List<Card> cardValuesList = new ArrayList<>(cards);
		List<Runnable> runnables = new ArrayList<>();
		Collections.sort(cardValuesList);
		for (Card card : cardValuesList) {
			if (card.getValue() == 1) {
				runnables.add(() ->
				{
					cardValuesList.remove(card);
					cardValuesList.add(0, card);
				});
			}
		}
		runnables.stream().forEach(Runnable::run);
		return cardValuesList;
	}

	private static Collection<Card> getStraight(Collection<Card> cards) {
		// 6, Jo, 4, Jo, 2
		// 6, 5, 4, Jo, Jo
		/*
		 * List<Card> cardValues = new ArrayList<>(cards); Collection<Card> jokers =
		 * getAndRemoveJokers(cardValues); cardValues = new
		 * ArrayList<>(getCardsSortedAceHigh(cardValues)); for (int x = 0; x <
		 * (cardValues.size() - 4) + jokers.size(); x++) { List<Card> tempJokers = new
		 * ArrayList<>(jokers); List<Card> topFiveCards = new ArrayList<>(); Card card =
		 * cardValues.get(x); topFiveCards.add(card); int cardValue =
		 * Math.min(card.getValue() + tempJokers.size(), 13); int count = 1; while
		 * (topFiveCards.size() < 5) { Card nextCard = cardValues.get(x + count) ; if
		 * (nextCard.getValue() == (cardValue - 1)) { count++;
		 * topFiveCards.add(nextCard); cardValue = nextCard.getValue(); } else if
		 * (tempJokers.iterator().hasNext()) { Card joker =
		 * tempJokers.iterator().next(); topFiveCards.add(joker);
		 * tempJokers.remove(joker); cardValue--; } else { break; } } if
		 * (topFiveCards.size() == 5) { return topFiveCards; }
		 * 
		 * } return Collections.emptyList();
		 */

		Collection<Card> jokers = getAndRemoveJokers(cards);

		List<Card> cardsNew = new ArrayList<>(getCardsSortedAceHigh(cards));

		List<Card> topFiveCards = new ArrayList<>();
		Set<Card> cardValues = cardsNew.stream().collect(Collectors.toSet());
		cardValues = new TreeSet<Card>(cardValues);

		for (Collection<Integer> straight : straights) {
			int matchCount = jokers.size();
			for (Card card : cardValues) {
				if (straight.contains(card.getValue())) {
					topFiveCards.add(card);
					matchCount++;
				}
			}
			if (matchCount == 5) {
				break;
			} else {
				topFiveCards.clear();
			}
		}
		if (topFiveCards.size() > 0) {
			if (jokers.size() > 0) {
				for (Card card : jokers) {
					topFiveCards.add(card);
				}
			}
		}
		return topFiveCards;
	}

	private static final Collection<Integer> straight1 = Arrays.asList(1, 2, 3, 4, 5);
	private static final Collection<Integer> straight2 = Arrays.asList(2, 3, 4, 5, 6);
	private static final Collection<Integer> straight3 = Arrays.asList(3, 4, 5, 6, 7);
	private static final Collection<Integer> straight4 = Arrays.asList(4, 5, 6, 7, 8);
	private static final Collection<Integer> straight5 = Arrays.asList(5, 6, 7, 8, 9);
	private static final Collection<Integer> straight6 = Arrays.asList(6, 7, 8, 9, 10);
	private static final Collection<Integer> straight7 = Arrays.asList(7, 8, 9, 10, 11);
	private static final Collection<Integer> straight8 = Arrays.asList(8, 9, 10, 11, 12);
	private static final Collection<Integer> straight9 = Arrays.asList(9, 10, 11, 12, 13);
	private static final Collection<Integer> straight10 = Arrays.asList(10, 11, 12, 13, 1);
	private static final Collection<Collection<Integer>> straights = new ArrayList<Collection<Integer>>(
			Arrays.asList(straight1, straight2, straight3, straight4, straight5, straight6, straight7, straight8,
					straight9, straight10)) {
		{
			Collections.reverse(this);
		}
	};

}
