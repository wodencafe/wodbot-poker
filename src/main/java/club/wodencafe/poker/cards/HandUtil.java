package club.wodencafe.poker.cards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import club.wodencafe.poker.cards.hands.Hand;

public class HandUtil {

	public static Hand getHand(Collection<Card> cards) {
		
		
		
		return null;
	}
	public static Collection<Card> getRoyalFlush(Collection<Card> cards) {
	

		Collection<Card> topFiveCards = getStraightFlush(cards);
		
		if (topFiveCards.size() == 5) {
			
			List<Integer> straightFlush = Arrays.asList(10, 11, 12, 13, 1);
			
			if (topFiveCards.stream()
				.map(Card::getValue)
				.filter(straightFlush::contains)
				.count() == 5) {
				return topFiveCards;
			}
		}
		return new ArrayList<>();
		
		
		
	}
	public static Collection<Card> getQuads(Collection<Card> cards) {


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
	public static Map<Integer, Collection<Card>> getCardGroups(Collection<Card> cards) {

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
	
	public static Collection<Card> getDuplicates(Collection<Card> cards, int count) {

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
				
			}
		}
		
		return returnCards;
	}
	
	private static Collection<Card> getTwoPair(Collection<Card> cards) {
		Collection<Card> pair = getPair(cards);
		
		if (pair.isEmpty()) {
			return pair;
		}
		
		List<Card> cardValues = cards.stream()
			.filter(x -> !pair.contains(x))
			.collect(Collectors.toList());
		
		Collection<Card> pair2 = getPair(cardValues);
		
		if (pair2.isEmpty()) {
			return pair2;
		}
		
		Collection<Card> returnCards = new ArrayList<>(pair);
		returnCards.addAll(pair2);
		
		return returnCards;
	}
	
	public static Collection<Card> getStraightFlush(Collection<Card> cards) {
		Collection<Card> flush = getFlush(cards);
		
		if (!flush.isEmpty()) {
			flush = getStraight(cards);
			if (!flush.isEmpty()) {
				return flush;
			}
		}
		
		return new ArrayList<>();
	}
	
	public static Collection<Card> getFullHouse(Collection<Card> cards) {

		Collection<Card> trips = getTrips(cards);
		
		if (trips.isEmpty()) {
			return trips;
		}
		
		List<Card> cardValues = cards.stream()
			.filter(x -> !trips.contains(x))
			.collect(Collectors.toList());
		
		Collection<Card> pair2 = getPair(cardValues);
		
		if (pair2.isEmpty()) {
			return pair2;
		}
		
		Collection<Card> returnCards = new ArrayList<>(trips);
		returnCards.addAll(pair2);
		
		return returnCards;		
	}
	
	public static Collection<Card> getTrips(Collection<Card> cards) {


		return getDuplicates(cards, 3);
	}
	
	public static Collection<Card> getPair(Collection<Card> cards) {
	
		return getDuplicates(cards, 2);
	}
	
	public static Collection<Card> getFlush(Collection<Card> cards) {

		Set<Card> cardValues = getCardsSortedAceHigh(cards);
		
		List<Suit> suits = Arrays.asList(Suit.HEART, Suit.DIAMOND, Suit.SPADE, Suit.CLUB);
		
		for (Suit suit : suits) {
			List<Card> returnCards = cardValues.stream().filter(card -> card.getSuit() == suit || card.getSuit() == Suit.JOKER).limit(5).collect(Collectors.toList());
			
			if (returnCards.size() == 5) {
				return returnCards;
			}
		}
		return Collections.emptyList();
	}

	private static Set<Card> getCardsSortedAceHigh(Collection<Card> cards) {
		List<Card> cardsNew = new ArrayList<>(cards);
		
		Set<Card> cardValues = cardsNew.stream()
			.collect(Collectors.toSet());
		cardValues = new TreeSet<Card>(cardValues);
		
		List<Card> cardValuesList = new ArrayList<>(cardValues);

		for (Card card : cardValuesList) {
			if (card.getValue() == 1) {
				cardValuesList.remove(card);
				
				cardValuesList.add(0, card);
			}
		}
		return cardValues;
	}
	public static Collection<Card> getStraight(Collection<Card> cards) {
		
		List<Card> topFiveCards = new ArrayList<>();
		
		List<Card> jokers = new ArrayList<>();
		
		List<Card> cardsNew = new ArrayList<>(cards);
		
		List<Runnable> runnables = new ArrayList<>();
		
		for (Card card : cardsNew) {
			if (card.getSuit() == Suit.JOKER) {
				runnables.add(() -> cardsNew.remove(card));
				jokers.add(card);
			}
		}
		
		for (Runnable runnable : runnables) {
			runnable.run();
		}
		
		Set<Card> cardValues = cardsNew.stream()
			.collect(Collectors.toSet());
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
	private static final Collection<Collection<Integer>> straights = new ArrayList<Collection<Integer>>(Arrays.asList(straight1,
		straight2, straight3, straight4, straight5, straight6, straight7, straight8, straight9, straight10)) {{
			Collections.reverse(this);
		}};
	
}
