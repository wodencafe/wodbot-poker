package club.wodencafe.poker.holdem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PotManager {
	private Map<PlayerRoundData, Long> potFromPlayers = new HashMap<>();
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
}
