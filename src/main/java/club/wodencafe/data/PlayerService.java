package club.wodencafe.data;

public class PlayerService {
	public static Player load(String ircName) {
		try {
			return BusinessServiceUtil.findAllWithJPA(Player.class).stream().findFirst().orElse(null);
			/*
			 * List<Player> players = BusinessServiceUtil.findAllWithJPA(Player.class,
			 * (arg0) -> { CriteriaQuery<Player> cq = arg0.getMiddle(); Root<Player> root =
			 * arg0.getRight(); CriteriaBuilder cb = arg0.getLeft(); return
			 * cq.select(root).where(cb.equal(root.get("ircName"), ircName));
			 * 
			 * });
			 */
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	public static void save(Player player) {
		BusinessServiceUtil.saveWithJPA(player, Player.class);
	}
}
