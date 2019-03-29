package club.wodencafe.poker.holdem;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

import java.util.Set;
import java.util.TreeMap;

public class PotManager {
	private NavigableMap<PlayerRoundData, Long> potFromPlayers = new TreeMap<>();
	public PotManager(List<PlayerRoundData> playersList) {
		for (PlayerRoundData player : playersList) {
			if (!player.isFolded()) {
				potFromPlayers.put(player, 0L);
			}
		}
	}

	public boolean isPotSatisfied() {

	    Set<Long> values = new HashSet<Long>();
		
		for (Entry<PlayerRoundData, Long> playerData : potFromPlayers.entrySet()) {
			if (playerData.getKey().get().getMoney() > 0 && !playerData.getKey().isFolded()) {
				values.add(playerData.getValue());
				if (values.size() > 1) {
					return false;
				}
			}
		}
		return true;
	}
	public void check(PlayerRoundData player) {
		potFromPlayers.put(player, 0L);
	}
	public void bet(PlayerRoundData player, long amount) {
		potFromPlayers.put(player, amount);
	}
	public void call(PlayerRoundData player) {

		for (Map.Entry<PlayerRoundData, Long> e : potFromPlayers.entrySet()) {
		    Map.Entry<PlayerRoundData, Long> prev = potFromPlayers.lowerEntry(e.getKey());  // previous
		
		    long prevAmount = prev.getValue();
		    
		    potFromPlayers.put(e.getKey(), prevAmount);
		    
		    break;
		}	
	}
	public void raise(PlayerRoundData player, long amount) {
		potFromPlayers.put(player, amount);
	}
}
