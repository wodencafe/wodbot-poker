package club.wodencafe.poker.cards;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class HandUtil {

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

		Set<Card> cardValues = getCardsSortedAceHigh(cards);

		List<Card> topFiveCards = new ArrayList<>();

		Map<Integer, Collection<Card>> cardValuesFound = new HashMap<>();
		
		for (Card card : cardValues) {

			int cardValue = card.getValue();
			
			Collection<Card> collection = cardValuesFound.get(cardValue);
			
            if (collection == null) {
            	collection = new ArrayList<Card>(Arrays.asList(card));
            	
            	cardValuesFound.put(cardValue, collection);
            } else {
            	
            	collection.add(card);
            	
                if (collection.size() == 4) {
                	break;
                }
            }
		}
		
		for (Collection<Card> collection : cardValuesFound.values()) {
			if (collection.size() == 4) {
				List<Card> cardValuesList = new ArrayList<>(cardValues);
				for (Card card : collection) {
					topFiveCards.add(card);
									
					cardValuesList.remove(card);	
					
				}
				
				topFiveCards.add(cardValuesList.iterator().next());
			}
		}
		
		return topFiveCards;
	}
	
	public static Collection<Card> getTwoPair(Collection<Card> cards) {

		Set<Card> cardValues = getCardsSortedAceHigh(cards);

		List<Card> topFiveCards = new ArrayList<>();

		List<Card> cardValuesList = new ArrayList<>(cardValues);

		Map<Card, Integer> cardValuesFound = new HashMap<>();

		Set<Card> pairSet = new HashSet<>();

		for (int x = 0; x < cardValuesList.size(); x++) {
			Card card = cardValuesList.remove(x);

			int cardValue = card.getValue();

			if (!cardValuesFound.containsValue(cardValue)) {
				cardValuesFound.put(card, cardValue);
			} else {
				for (Entry<Card, Integer> entry : cardValuesFound.entrySet()) {
					if (Objects.equals(cardValue, entry.getValue())) {
						pairSet.add(entry.getKey());
						pairSet.add(card);
						break;
					}
				}
			}
			if (pairSet.size() == 4) {
				break;
			}
		}
		if (pairSet.size() == 4) {
			topFiveCards.addAll(pairSet);
			
			cardValuesList = new ArrayList<>(cardValues);
			
			cardValuesList.removeAll(pairSet);
			
			Card card = cardValuesList.iterator().next();
			
			topFiveCards.add(card);
		}
		
		return topFiveCards;

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

		Set<Card> cardValues = getCardsSortedAceHigh(cards);
		
		List<Card> topFiveCards = new ArrayList<>();
		
		List<Card> trips = new ArrayList<>(getTrips(cardValues));
		
		if (trips.size() > 0) {
			cardValues.removeAll(trips);
			
			List<Card> pair = new ArrayList<>(getPair(cardValues));
			
			if (pair.size() > 0) {
				topFiveCards.add(trips.get(0));
				
				topFiveCards.add(trips.get(1));
				
				topFiveCards.add(trips.get(2));

				topFiveCards.add(pair.get(0));
				
				topFiveCards.add(pair.get(1));
			}
			
		}
		
		return topFiveCards;
	}
	
	public static Collection<Card> getTrips(Collection<Card> cards) {

		Set<Card> cardValues = getCardsSortedAceHigh(cards);
		
		List<Runnable> runnables = new ArrayList<>();

		List<Card> jokers = new ArrayList<>();

		List<Card> topFiveCards = new ArrayList<>();
		
		for (Card card : cardValues) {
			if (card.getSuit() == Suit.JOKER) {
				runnables.add(() -> cardValues.remove(card));
				jokers.add(card);
			}
		}

		for (Runnable runnable : runnables) {
			runnable.run();
		}
		
		if (jokers.size() > 0) {
			topFiveCards.addAll(jokers);	
		}
		
		for (Card card : cardValues) {
			List<Card> potentialTrips = cardValues.stream().filter(x -> x.getValue() == card.getValue()).collect(Collectors.toList());
			
			if (potentialTrips.size() + jokers.size() > 2) {
				topFiveCards.addAll(potentialTrips);
			}
		}
		if (topFiveCards.size() > 2 && topFiveCards.size() < 5) {
			for (Card card : cardValues) {
				if (!topFiveCards.contains(card)) {
					topFiveCards.add(card);
				}
				if (topFiveCards.size() == 5) {
					break;
				}
			}
		}
		
		return topFiveCards;
		
	}
	
	public static Collection<Card> getPair(Collection<Card> cards) {
		Set<Card> cardValues = getCardsSortedAceHigh(cards);
		
		List<Runnable> runnables = new ArrayList<>();

		List<Card> jokers = new ArrayList<>();

		List<Card> topFiveCards = new ArrayList<>();
		
		for (Card card : cardValues) {
			if (card.getSuit() == Suit.JOKER) {
				runnables.add(() -> cardValues.remove(card));
				jokers.add(card);
			}
		}

		for (Runnable runnable : runnables) {
			runnable.run();
		}
		
		if (jokers.size() > 0) {
			topFiveCards.addAll(jokers);
			
			topFiveCards.addAll(cardValues.stream().limit(5 - jokers.size()).collect(Collectors.toList()));
		}
		else {
			// check for aces first
			Collection<Card> possiblePair = cardValues.stream().filter(card -> card.getValue() == 1).collect(Collectors.toList());
			
			if (possiblePair.size() > 1) {
				for (Card card : possiblePair) {
					topFiveCards.add(card);
					
					cardValues.remove(card);
					
					topFiveCards.addAll(cardValues.stream().limit(5 - possiblePair.size()).collect(Collectors.toList()));
				}
			}
			else {
				for (int x = 13; x > 1; x--) {
					final int finalx = x;
					possiblePair = cardValues.stream().filter(card -> card.getValue() == finalx).collect(Collectors.toList());
					
					if (possiblePair.size() > 1) {
						for (Card card : possiblePair) {
							topFiveCards.add(card);
							
							cardValues.remove(card);
							
							topFiveCards.addAll(cardValues.stream().limit(5 - possiblePair.size()).collect(Collectors.toList()));
						}
						
						break;
					}
				}
				
			}
		}
		
		return topFiveCards;
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
