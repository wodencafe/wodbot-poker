package club.wodencafe.data;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BusinessServiceUtil {

	private static final ThreadLocal<EntityManager> ENTITY_MANAGER_CACHE = new ThreadLocal<EntityManager>();

	private static EntityManagerFactory emf = null;

	static {
		try {
			DatabaseService.SINGLETON.startUp();
			emf = Persistence.createEntityManagerFactory("poker-prod");
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	private static final Logger logger = LoggerFactory.getLogger(BusinessServiceUtil.class);

	private static EntityManager getEntityManager() {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		logger.trace(methodName);

		EntityManager entityManager = ENTITY_MANAGER_CACHE.get();

		if (entityManager == null) {
			entityManager = emf.createEntityManager();
			ENTITY_MANAGER_CACHE.set(entityManager);
		}
		return entityManager;
		/*
		 * try { EntityManager em =
		 * CompletableFuture.supplyAsync(HibernateUtil::getEntityManager,
		 * fuxecutor).get(); logger.trace("Returning EntityManager " + em); return em; }
		 * catch (InterruptedException | ExecutionException e) {
		 * logger.error("Unable to acquire EntityManager", e); }
		 */
	}

	public static <R> R getCustomResult(Function<EntityManager, R> function) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		logger.trace(methodName);

		return function.apply(getEntityManager());
	}

	@SafeVarargs
	public static <T extends BusinessEntity> List<T> findAllWithJPA(Class<T> clazz,
			Function<Triple<CriteriaBuilder, CriteriaQuery<T>, Root<T>>, CriteriaQuery<T>>... functions) {
		// String methodName =
		// Thread.currentThread().getStackTrace()[1].getMethodName();

		// logger.trace(methodName);

		EntityManager em = getEntityManager();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<T> cq = cb.createQuery(clazz);
		Root<T> rootEntry = cq.from(clazz);
		CriteriaQuery<T> all = cq.select(rootEntry);
		Triple<CriteriaBuilder, CriteriaQuery<T>, Root<T>> triple = new ImmutableTriple<>(cb, all, rootEntry);
		for (Function<Triple<CriteriaBuilder, CriteriaQuery<T>, Root<T>>, CriteriaQuery<T>> function : functions) {
			all = function.apply(triple);
		}
		TypedQuery<T> allQuery = em.createQuery(all);
		return allQuery.getResultList();
	}

	public static <T extends BusinessEntity> List<T> findWithJPA(
			Function<Triple<CriteriaBuilder, CriteriaQuery<T>, Root<T>>, CriteriaQuery<T>> function, Class<T> clazz) {
		return findAllWithJPA(clazz, function);
	}

	public static <T extends BusinessEntity> T findWithJPA(Long id, Class<T> clazz) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		logger.trace(methodName);
		EntityManager em = getEntityManager();
		return em.find(clazz, id);
	}

	private static <T extends BusinessEntity> boolean isDetached(T entity, Class<T> clazz) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		logger.trace(methodName);
		EntityManager em = getEntityManager();
		return entity.getId() != null // must not be transient
				&& entity.getId() > 0 && !em.contains(entity) // must not be managed now
				&& em.find(clazz, entity.getId()) != null; // must not have been
															// removed
	}

	public static <T extends BusinessEntity> boolean deleteWithJPA(T entity, Class<T> clazz) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		logger.trace(methodName);
		EntityManager em = getEntityManager();
		boolean success = false;
		EntityTransaction t = em.getTransaction();
		if (!t.isActive())
			t.begin();
		try {
			em.remove(entity);
			em.flush();
			success = true;
			t.commit();
			entity.setId(null);
		} catch (Exception e) {
			success = false;
			e.printStackTrace();
			if (t.isActive())
				t.rollback();
		}
		if (t.isActive())
			t.rollback();
		em.close();
		return success;

	}

	public static <T extends BusinessEntity> boolean saveWithJPA(T entity, Class<T> clazz) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		logger.trace(methodName);
		EntityManager em = getEntityManager();
		boolean success = false;
		if (entity.getId() != null && entity.getId() > 0) {

			try {
				// TOOD: Replace?
				// HibernateUtil.getSessionFactory().getCache().evict(clazz, entity.getId());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			T entityNew = null;
			try {
				try {
					entityNew = clazz.getConstructor().newInstance();
				} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException
						| SecurityException e) {
					throw new RuntimeException(e);
				}

				logger.trace("Old Identity hashcode is: " + System.identityHashCode(entity));
				logger.trace("New Identity hashcode is: " + System.identityHashCode(entityNew));
				MiscUtil.copyMatchingFields(entity, entityNew);
				entityNew.setVersion(entity.getVersion());
				refreshWithJPA(entity, clazz);
				entity.setVersion(entityNew.getVersion());

			} catch (InstantiationException | IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (!Objects.equals(entity, entityNew)) {
				if (entityNew.getVersion() == entity.getVersion()) {
					try {
						success = attemptSave(entityNew, clazz);
						if (!success)
							throw new Exception();
					} catch (Exception e) {
						// This should not be happening
						e.printStackTrace();
					}
				} else {
					// long version = entityNew.getVersion();
					// MiscUtil.copyMatchingFields(entity, entityNew);
					// entityNew.setVersion(version);
					success = attemptSave(entityNew, clazz);

				}
			} else {
				success = true;
				// success = false;
				System.out.println("Objects are identical.");
			}

		} else {
			success = attemptSave(entity, clazz);
		}
		if (success) {
			try {
				// TOOD: Replace?
				// HibernateUtil.getSessionFactory().getCache().evict(clazz, entity.getId());
				em.refresh(entity);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.err.println("BusinessServiceUtil.saveWithJPA unable to save.");
		}
		em.close();
		return success;
	}

	private static <T extends BusinessEntity> boolean attemptSave(T entity, Class<T> clazz) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		logger.trace(methodName);
		EntityManager em = getEntityManager();
		boolean success = false;
		EntityTransaction t = em.getTransaction();
		if (!t.isActive())
			t.begin();
		try {
			try {
				// em.persist(entity);
				if (!isDetached(entity, clazz)) {
					em.persist(entity);
				} else {
					T entityUpdated = em.merge(entity);
					MiscUtil.copyMatchingFields(entityUpdated, entity);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			try {
				em.flush();

				em.refresh(entity);
				t.commit();
				success = true;
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (t.isActive())
				t.rollback();
		}
		return success;
	}

	public static <T> long count(Class<T> clazz) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		logger.trace(methodName);
		EntityManager entityManager = getEntityManager();
		CriteriaBuilder qb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = qb.createQuery(Long.class);
		cq.select(qb.count(cq.from(clazz)));
		// ParameterExpression<Integer> p = qb.parameter(Integer.class);
		// qb.where(qb.eq(cq.get("age"), 45));
		return entityManager.createQuery(cq).getSingleResult();

	}

	public static <T extends BusinessEntity> void deleteAllWithJPA(Class<T> clazz) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		logger.trace(methodName);

		for (T entity : findAllWithJPA(clazz)) {
			deleteWithJPA(entity, clazz);
		}
	}

	public static <T extends BusinessEntity> void refreshWithJPA(T entity, Class<T> clazz) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		logger.trace(methodName);
		EntityManager em = getEntityManager();
		T entityNew = findWithJPA(entity.getId(), clazz);
		MiscUtil.copyMatchingFields(entityNew, entity);
		em.refresh(entity);
	}

}
