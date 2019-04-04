package club.wodencafe.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class PlayerService {
	public static Player load(String ircName) {
		List<Player> players = BusinessServiceUtil.findAllWithJPA(Player.class, (arg0) ->
		{
			CriteriaQuery<Player> cq = arg0.getMiddle();
			Root<Player> root = arg0.getRight();
			CriteriaBuilder cb = arg0.getLeft();
			return cq.select(root).where(cb.equal(root.get("ircName"), ircName));

		});
		return players.iterator().next();
	}

	public static void save(Player player) {
		BusinessServiceUtil.saveWithJPA(player, Player.class);
	}
}
