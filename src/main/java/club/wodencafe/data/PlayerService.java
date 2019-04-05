package club.wodencafe.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerService {
	private static final Logger logger = LoggerFactory.getLogger(PlayerService.class);

	public static Player load(String ircName) {
		try {

			List<Player> players = BusinessServiceUtil.findAllWithJPA(Player.class, (arg0) ->
			{
				CriteriaQuery<Player> cq = arg0.getMiddle();
				Root<Player> root = arg0.getRight();
				CriteriaBuilder cb = arg0.getLeft();
				return cq.select(root).where(cb.equal(root.get("ircName"), ircName));

			});

			if (!players.isEmpty()) {
				return players.iterator().next();
			} else {
				return null;
			}
		} catch (Throwable e) {
			logger.error("Unable to load player", e);
			throw new RuntimeException(e);
		}
	}

	public static void save(Player player) {
		BusinessServiceUtil.saveWithJPA(player, Player.class);
	}
}
