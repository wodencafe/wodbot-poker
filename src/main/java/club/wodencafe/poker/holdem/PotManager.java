package club.wodencafe.poker.holdem;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public class PotManager {
	private List<Entry<PlayerRoundData, AtomicLong>> potFromPlayers = new ArrayList<>();
	public PotManager(List<PlayerRoundData> playersList) {
		for (PlayerRoundData player : playersList) {
			if (!player.isFolded()) {
				potFromPlayers.add(new AbstractMap.SimpleEntry<>(player, new AtomicLong(0)));
			}
		}
	}

	public boolean isPotSatisfied() {

	    Set<Long> values = new HashSet<Long>();
		
		for (Entry<PlayerRoundData, AtomicLong> playerData : potFromPlayers) {
			if (playerData.getKey().get().getMoney() > 0 && !playerData.getKey().isFolded()) {
				values.add(playerData.getValue().get());
				if (values.size() > 1) {
					return false;
				}
			}
		}
		return true;
	}
	public void check(PlayerRoundData player) {
		for (Entry<PlayerRoundData, AtomicLong> playerData : potFromPlayers) { 
			if (Objects.equals(playerData.getKey(), player)) {
				// This isn't necessary.
				playerData.getValue().set(0);
			}
		}
		throw new RuntimeException("Player " + player.get() + " not found.");
	}
	public void bet(PlayerRoundData player, long amount) {
		for (Entry<PlayerRoundData, AtomicLong> playerData : potFromPlayers) { 
			if (Objects.equals(playerData.getKey(), player)) {
				// This isn't necessary.
				playerData.getValue().set(amount);
			}
		}
		throw new RuntimeException("Player " + player.get() + " not found.");
	}
	public void call(PlayerRoundData player) {

		for (int x = 0; x < potFromPlayers.size(); x++) {
			Map.Entry<PlayerRoundData, AtomicLong> e = potFromPlayers.get(x);

			if (Objects.equals(e.getKey(), player)) {
				
				Map.Entry<PlayerRoundData, AtomicLong> prev;
				
				if (x > 0) {
					prev = potFromPlayers.get(x);
					
				}
				else {
					prev = potFromPlayers.get(potFromPlayers.size() - 1);
				}
				
				long prevAmount = prev.getValue().get();
				
				e.getValue().set(prevAmount);
				
				return;
			}
		    
		}

		throw new RuntimeException("Player " + player.get() + " not found.");
	}
	public void raise(PlayerRoundData player, long amount) {
		for (int x = 0; x < potFromPlayers.size(); x++) {
			Map.Entry<PlayerRoundData, AtomicLong> e = potFromPlayers.get(x);

			if (Objects.equals(e.getKey(), player)) {
				
				Map.Entry<PlayerRoundData, AtomicLong> prev;
				
				if (x > 0) {
					prev = potFromPlayers.get(x);
					
				}
				else {
					prev = potFromPlayers.get(potFromPlayers.size() - 1);
				}
				
				long prevAmount = prev.getValue().get();
				
				if (amount > prevAmount) {
					
					e.getValue().set(amount);
					
					return;
				}
				else {
					throw new RuntimeException("Cannot raise to lower than previous amount.");
				}
				
			}
		    
		}
		throw new RuntimeException("Player " + player.get() + " not found.");
	}
	
	public Long getTotalPot() {
		return potFromPlayers
			.stream()
			.map(x -> x.getValue().get())
			.mapToLong(Long::longValue)
			.sum();
	}
}
