package club.wodencafe.data;

import java.util.Date;
import java.util.Properties;

import javax.persistence.EntityManager;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.event.spi.PostDeleteEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.entity.EntityPersister;

public class HibernateUtil {

	private static final ThreadLocal<EntityManager> ENTITY_MANAGER_CACHE = new ThreadLocal<EntityManager>();

	private static SessionFactory sessionFactory;

	public static SessionFactory getSessionFactory() throws Exception {
		if (sessionFactory == null) {
			boolean success = false;
			try {
				if (DatabaseService.SINGLETON.server.isNotRunning())
					DatabaseService.SINGLETON.startUp();
				success = true;
			} catch (Exception e) {
				e.printStackTrace();

			}

			if (success) {
				try {

					Properties c = new Properties();
					c.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
					c.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
					// c.setProperty("hibernate.connection.url",
					// "jdbc:hsqldb:mem:testdb");
					c.setProperty("hibernate.connection.url", "jdbc:hsqldb:hsql://localhost/hsqldb");
					// c.setProperty("hibernate.default_schema", "myschema");

					c.setProperty("hibernate.hbm2ddl.auto", "create-drop");
					c.setProperty("hibernate.connection.username", "sa");
					c.setProperty("hibernate.connection.password", "");
					c.setProperty(AvailableSettings.SHOW_SQL, "false");
					c.setProperty(AvailableSettings.FORMAT_SQL, "true");
					c.setProperty("hibernate.connection.autoReconnect", "true");

					Configuration cfg = new Configuration().addAnnotatedClass(BusinessEntity.class)
							.addAnnotatedClass(Player.class)
					// .setInterceptor(new BusinessListener())

					;

					cfg.setProperties(c);
					StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
							.applySettings(cfg.getProperties()).build();

					// registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(new
					// GrPreUpdateEventListener());
					// registry.getEventListenerGroup(EventType.MERGE).appendListener(new
					// GrPreMergeEventListener());

					sessionFactory = cfg.buildSessionFactory(serviceRegistry);
					EventListenerRegistry registry = ((SessionFactoryImpl) sessionFactory).getServiceRegistry()
							.getService(EventListenerRegistry.class);
					registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(new PostInsertEventListener() {

						@Override
						public boolean requiresPostCommitHanding(EntityPersister persister) {
							// TODO Auto-generated method stub
							return false;
						}

						@Override
						public void onPostInsert(PostInsertEvent event) {

							BusinessEntity entity = (BusinessEntity) event.getEntity();
							entity.setCreatedDate(new Date());
							BusinessListener.SINGLETON.onCreate(entity);
						}
					});
					registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(new PostUpdateEventListener() {

						@Override
						public boolean requiresPostCommitHanding(EntityPersister persister) {
							// TODO Auto-generated method stub
							return false;
						}

						@Override
						public void onPostUpdate(PostUpdateEvent event) {
							BusinessEntity entity = (BusinessEntity) event.getEntity();
							entity.setModifiedDate(new Date());
							BusinessListener.SINGLETON.onPersist(entity);

						}
					});
					registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(new PostDeleteEventListener() {

						@Override
						public boolean requiresPostCommitHanding(EntityPersister persister) {
							// TODO Auto-generated method stub
							return false;
						}

						@Override
						public void onPostDelete(PostDeleteEvent event) {

							BusinessListener.SINGLETON.onDelete((BusinessEntity) event.getEntity());
						}
					});
				} catch (Throwable ex) {
					// Log the exception.
					System.err.println("Initial SessionFactory creation failed." + ex);
					throw new ExceptionInInitializerError(ex);
				}
			}
		}
		return sessionFactory;
	}

	public static EntityManager getEntityManager() {

		EntityManager entityManager = ENTITY_MANAGER_CACHE.get();
		if (entityManager == null || !entityManager.isOpen()) {
			System.out.println(HibernateUtil.class.getResource("META-INF/persistence.xml"));
			try {
				entityManager = getSessionFactory().createEntityManager();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				System.out.println(Class.forName("org.hibernate.jpa.HibernatePersistenceProvider"));
			} catch (ClassNotFoundException e) {

				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// EntityManagerFactory entityManagerFactory =
			// Persistence.createEntityManagerFactory("persistence");
			// entityManager = entityManagerFactory.createEntityManager();
			ENTITY_MANAGER_CACHE.set(entityManager);
		}
		return entityManager;
	}
}
