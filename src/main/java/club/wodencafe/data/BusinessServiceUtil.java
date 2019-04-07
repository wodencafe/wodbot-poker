package club.wodencafe.data;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

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
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

import club.wodencafe.bot.WodData;

public class BusinessServiceUtil {
	private static final XLogger logger = XLoggerFactory.getXLogger(BusinessServiceUtil.class);

	private static class Saveable<T extends BusinessEntity> implements Predicate<T> {
		private static final XLogger logger = XLoggerFactory.getXLogger(Saveable.class);

		private Class<T> clazz;

		public Saveable(Class<T> clazz) {
			this.clazz = clazz;

		}

		@Override
		public boolean test(T arg0) {
			logger.entry(arg0);
			boolean returnValue = false;
			try {
				EntityManager em = emf.createEntityManager();

				T foundValue;
				if (arg0.getId() != null && arg0.getId() > -1) {
					foundValue = findWithJPA(arg0.getId(), clazz);
					em.detach(foundValue);
					MiscUtil.copyMatchingFields(arg0, foundValue);

					em.getTransaction().begin();
					em.merge(foundValue);
					em.getTransaction().commit();
				} else {

					em.getTransaction().begin();
					em.persist(arg0);
					em.getTransaction().commit();
				}
				return true;
			} catch (Throwable th) {
				logger.error("Save failed", th);
				logger.catching(th);
				throw new RuntimeException(th);
			} finally {
				logger.exit(returnValue);
			}
		}

	}

	private static final ThreadLocal<EntityManager> ENTITY_MANAGER_CACHE = new ThreadLocal<EntityManager>();

	private static EntityManagerFactory emf = null;

	static {
		try {
			if (!DatabaseService.SINGLETON.isRunning()) {
				DatabaseService.SINGLETON.startAsync();
				DatabaseService.SINGLETON.awaitRunning();
				EntityManagerFactory emf = Persistence.createEntityManagerFactory(WodData.databaseName);
				BusinessServiceUtil.emf = emf;
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static EntityManager getEntityManager() {
		logger.entry();

		EntityManager entityManager = null;
		try {
			String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

			logger.trace(methodName);

			entityManager = ENTITY_MANAGER_CACHE.get();

			if (entityManager == null || !entityManager.isOpen()) {
				entityManager = emf.createEntityManager();
				ENTITY_MANAGER_CACHE.set(entityManager);
			}
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(entityManager);
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
		logger.entry(function);
		R returnValue = null;
		try {
			String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

			logger.trace(methodName);

			returnValue = function.apply(getEntityManager());
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		}
		logger.exit(returnValue);
		return returnValue;
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
		logger.entry(id, clazz);
		T returnValue = null;
		try {
			EntityManager em = getEntityManager();
			returnValue = em.find(clazz, id);
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(returnValue);
		}
		return returnValue;
	}

	private static <T extends BusinessEntity> boolean isDetached(T entity, Class<T> clazz) {
		logger.entry(entity, clazz);
		boolean returnValue = false;
		try {
			EntityManager em = getEntityManager();
			returnValue = entity.getId() != null // must not be transient
					&& entity.getId() > 0 && !em.contains(entity) // must not be managed now
					&& em.find(clazz, entity.getId()) != null; // must not have been
																// removed
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(returnValue);
		}
		return returnValue;
	}

	public static <T extends BusinessEntity> boolean deleteWithJPA(T entity, Class<T> clazz) {
		logger.entry(entity, clazz);
		boolean success = false;
		try {
			EntityManager em = getEntityManager();
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
				logger.error("Unable to commit delete", e);
				if (t.isActive())
					t.rollback();
			}
			if (t.isActive())
				t.rollback();
			em.close();
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		}
		logger.exit(success);
		return success;

	}

	public static <T extends BusinessEntity> boolean saveWithJPA(T entity, Class<T> clazz) {
		logger.entry(entity, clazz);
		/*
		 * String methodName =
		 * Thread.currentThread().getStackTrace()[1].getMethodName();
		 * 
		 * logger.trace(methodName); EntityManager em = getEntityManager(); boolean
		 * success = false; if (entity.getId() != null && entity.getId() > 0) {
		 * 
		 * try { // TOOD: Replace? emf.getCache().evict(clazz, entity.getId()); //
		 * HibernateUtil.getSessionFactory().getCache().evict(clazz, entity.getId()); }
		 * catch (Exception e1) { // TODO Auto-generated catch block
		 * e1.printStackTrace(); }
		 */
		// T entityNew = null;
		// try {
		// try {
		// entityNew = clazz.getConstructor().newInstance();
		// } catch (IllegalArgumentException | InvocationTargetException |
		// NoSuchMethodException
		// | SecurityException e) {
		// throw new RuntimeException(e);
		// }
		/*
		 * logger.trace("Old Identity hashcode is: " + System.identityHashCode(entity));
		 * logger.trace("New Identity hashcode is: " +
		 * System.identityHashCode(entityNew)); MiscUtil.copyMatchingFields(entity,
		 * entityNew); entityNew.setVersion(entity.getVersion()); refreshWithJPA(entity,
		 * clazz); MiscUtil.copyMatchingFields(entityNew, entity);
		 * entity.setVersion(entityNew.getVersion());
		 */

		// } catch (InstantiationException | IllegalAccessException e1) {
		// TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		// if (!Objects.equals(entity, entityNew)) {
		// if (entityNew.getVersion() == entity.getVersion()) {
		// try {
		// success = attemptSave(entity, clazz);
		// if (!success)
		// throw new RuntimeException();
		// } catch (Exception e) {
		// logger.error("Error", e);
		// This should not be happening
		// e.printStackTrace();
		// }
		// } else {
		// long version = entityNew.getVersion();
		// MiscUtil.copyMatchingFields(entity, entityNew);
		// entityNew.setVersion(version);
		// success = attemptSave(entityNew, clazz);

		// }
		// } else {
		// success = true;
		// success = false;
		// System.out.println("Objects are identical.");
		// }

		/*
		 * } else { success = attemptSave(entity, clazz); } if (success) { //
		 * em.flush(); try { // TOOD: Replace?
		 * 
		 * emf.getCache().evict(clazz, entity.getId()); //
		 * HibernateUtil.getSessionFactory().getCache().evict(clazz, entity.getId());
		 * refreshWithJPA(entity, clazz, Optional.of(em)); } catch (Exception e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); } } else {
		 * System.err.println("BusinessServiceUtil.saveWithJPA unable to save."); }
		 * em.close(); return success;
		 */
		boolean returnValue = false;
		try {
			Saveable<T> saveable = new Saveable<T>(clazz);

			returnValue = saveable.test(entity);
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(returnValue);
		}
		return returnValue;
	}

	private static <T extends BusinessEntity> boolean attemptSave(T entity, Class<T> clazz) {
		Saveable<T> saveable = new Saveable<T>(clazz);

		return saveable.test(entity);
		/*
		 * String methodName =
		 * Thread.currentThread().getStackTrace()[1].getMethodName();
		 * 
		 * logger.trace(methodName); EntityManager em = getEntityManager(); boolean
		 * success = false; EntityTransaction t = em.getTransaction(); if
		 * (!t.isActive()) t.begin(); try { try { // em.persist(entity); if
		 * (!isDetached(entity, clazz)) { em.persist(entity); } else { T entityUpdated =
		 * em.merge(entity); MiscUtil.copyMatchingFields(entityUpdated, entity);
		 * em.merge(entityUpdated); } } catch (Exception e) { e.printStackTrace(); throw
		 * e; } try { em.flush();
		 * 
		 * try { em.refresh(entity); } catch (IllegalArgumentException e) {
		 * logger.error("Couldn't refresh, trying merge.");
		 * 
		 * try { em.refresh(em.merge(entity)); } catch (StaleObjectStateException |
		 * OptimisticLockException se) { T foundValue = findWithJPA(entity.getId(),
		 * clazz);
		 * 
		 * MiscUtil.copyMatchingFields(foundValue, entity);
		 * em.refresh(em.merge(foundValue)); } } t.commit(); success = true; } catch
		 * (Exception e) { e.printStackTrace(); throw e; } } catch (Exception e) {
		 * e.printStackTrace(); throw e; } finally { if (t.isActive()) t.rollback(); }
		 * return success;
		 */
		// return false;
	}

	public static <T> long count(Class<T> clazz) {
		logger.entry(clazz);
		long count = -1;
		try {
			EntityManager entityManager = getEntityManager();
			CriteriaBuilder qb = entityManager.getCriteriaBuilder();
			CriteriaQuery<Long> cq = qb.createQuery(Long.class);
			cq.select(qb.count(cq.from(clazz)));
			// ParameterExpression<Integer> p = qb.parameter(Integer.class);
			// qb.where(qb.eq(cq.get("age"), 45));
			count = entityManager.createQuery(cq).getSingleResult();
		} catch (Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit(count);
		}
		return count;

	}

	public static <T extends BusinessEntity> void deleteAllWithJPA(Class<T> clazz) {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		logger.trace(methodName);

		for (T entity : findAllWithJPA(clazz)) {
			deleteWithJPA(entity, clazz);
		}
	}

	public static <T extends BusinessEntity> void refreshWithJPA(T entity, Class<T> clazz,
			Optional<EntityManager> oem) {
		logger.entry(entity, clazz, oem);
		try {
			EntityManager em = oem.isPresent() ? oem.get() : getEntityManager();
			T entityNew = findWithJPA(entity.getId(), clazz);
			MiscUtil.copyMatchingFields(entityNew, entity);

			try {
				em.refresh(entity);
			} catch (IllegalArgumentException e) {
				logger.catching(e);
				// Not a JPA object so do nothing.
			}

		} catch (

		Throwable th) {
			logger.catching(th);
			throw new RuntimeException(th);
		} finally {
			logger.exit();
		}

	}

}
