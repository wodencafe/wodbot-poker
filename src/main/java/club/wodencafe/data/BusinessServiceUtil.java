package club.wodencafe.data;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class BusinessServiceUtil {
	public static <T extends BusinessEntity> Collection<T> findAllWithJPA(EntityManager em, Class<T> clazz)
	{
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> rootEntry = cq.from(clazz);
		CriteriaQuery<T> all = cq.select(rootEntry);
		TypedQuery<T> allQuery = em.createQuery(all);
		return allQuery.getResultList();
	}

	public static <T> T findWithJPA(Long id, EntityManager em, Class<T> clazz)
	{
		return em.find(clazz, id);
	}

	public static <T extends BusinessEntity> boolean deleteWithJPA(T entity, EntityManager em, Class<T> clazz)
	{
		boolean success = false;
		EntityTransaction t = em.getTransaction();
		if (!t.isActive())
			t.begin();
		try
		{
			em.remove(entity);
			em.flush();
			success = true;
			t.commit();
			entity.setId(null);
		}
		catch (Exception e)
		{
			success = false;
			e.printStackTrace();
			if (t.isActive())
				t.rollback();
		}
		if (t.isActive())
			t.rollback();
		return success;

	}

	public static <T extends BusinessEntity> boolean saveWithJPA(T entity, EntityManager em, Class<T> clazz)
	{
		boolean success = false;
		EntityTransaction t = em.getTransaction();
		if (!t.isActive())
			t.begin();
		try
		{
			boolean trySuccess = false;
			try
			{
				// em.persist(entity);
				if (entity.getId() == null)
					em.persist(entity);
				else
					em.merge(entity);
				trySuccess = true;
			}
			catch (Exception e)
			{
				e.printStackTrace();
				em.merge(entity);
			}
			if (trySuccess)
				em.flush();
			em.refresh(entity);
			t.commit();
			success = true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			if (t.isActive())
				t.rollback();
		}
		if (t.isActive())
			t.rollback();
		return success;
	}

	public static <T> long count(EntityManager entityManager, Class<T> clazz)
	{
		CriteriaBuilder qb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = qb.createQuery(Long.class);
		cq.select(qb.count(cq.from(clazz)));
		return entityManager.createQuery(cq).getSingleResult();

	}

	public static <T extends BusinessEntity> void deleteAllWithJPA(EntityManager em, Class<T> clazz)
	{
		for (T entity : findAllWithJPA(em, clazz))
		{
			deleteWithJPA(entity, em, clazz);
		}
	}
}
