package club.wodencafe.poker.data;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.junit.Assert;
import org.junit.Test;

import club.wodencafe.data.Player;

public class PlayerDatabaseTest extends JPAHibernateClass {

	@Test
	public void test() throws Exception {
		em.getTransaction().begin();

		Player alice = new Player();

		alice.addMoney(100L);

		alice.setIrcName("alice");

		em.persist(alice);

		em.getTransaction().commit();

		List<Player> players = loadAllPlayers();

		Assert.assertEquals(players.size(), 1);

		em.getTransaction().begin();

		Player bob = new Player();

		alice.addMoney(100L);

		alice.setIrcName("bob");

		em.persist(bob);

		em.getTransaction().commit();

		players = loadAllPlayers();

		Assert.assertEquals(players.size(), 2);

	}

	private List<Player> loadAllPlayers() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Player> query = builder.createQuery(Player.class);
		Root<Player> variableRoot = query.from(Player.class);
		query.select(variableRoot);

		return em.createQuery(query).getResultList();
	}
}
