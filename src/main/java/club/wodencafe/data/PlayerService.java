package club.wodencafe.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

public class PlayerService {
	private static final XLogger logger = XLoggerFactory.getXLogger(PlayerService.class);

	public static Player load(String ircName) {
		logger.entry(ircName);
		Player returnValue = null;
		try {

			List<Player> players = BusinessServiceUtil.findAllWithJPA(Player.class, (arg0) ->
			{
				CriteriaQuery<Player> cq = arg0.getMiddle();
				Root<Player> root = arg0.getRight();
				CriteriaBuilder cb = arg0.getLeft();
				return cq.select(root).where(cb.equal(root.get("ircName"), ircName));

			});

			if (!players.isEmpty()) {
				returnValue = players.iterator().next();
			}
		} catch (Throwable e) {
			logger.error("Unable to load player", e);
			logger.catching(e);
			throw new RuntimeException(e);
		} finally {
			logger.exit(returnValue);
		}
		return returnValue;
	}

	public static void save(Player player) {
		logger.entry(player);
		try {
			BusinessServiceUtil.saveWithJPA(player, Player.class);
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}
	}

	public static void delete(Player player) {

		logger.entry(player);
		try {
			BusinessServiceUtil.deleteWithJPA(player, Player.class);
		} catch (Throwable th) {
			logger.catching(th);
		} finally {
			logger.exit();
		}
	}

	public static void deleteAll() {
		logger.entry();
		try {
			BusinessServiceUtil.deleteAllWithJPA(Player.class);
		} catch (Throwable th) {
			logger.catching(th);
		} finally {
			logger.exit();
		}
	}
}
