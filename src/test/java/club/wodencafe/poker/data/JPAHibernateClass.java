package club.wodencafe.poker.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.hsqldb.cmdline.SqlFile;
import org.hsqldb.cmdline.SqlToolError;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPAHibernateClass {

	private static final Logger logger = LoggerFactory.getLogger(JPAHibernateClass.class);
	protected static EntityManagerFactory emf;
	protected static EntityManager em;

	@BeforeClass
	public static void init() throws FileNotFoundException, SQLException {
		emf = Persistence.createEntityManagerFactory("poker-test");
		em = emf.createEntityManager();
	}

	@Before
	public void initializeDatabase() {
		try {
			Session session = em.unwrap(Session.class);

			File file = new File(getClass().getResource("/data.sql").getFile());
			session.doWork(new Work() {
				@Override
				public void execute(Connection connection) throws SQLException {
					try {
						SqlFile sqlFile = new SqlFile(file);
						sqlFile.setConnection(connection);
						sqlFile.execute();
					} catch (SqlToolError | IOException e) {
						logger.error("Unable to run database script.", e);
						throw new RuntimeException("could not initialize with script");
					}
				}
			});
		} catch (Exception e) {
			logger.error("Unable to run unwrap exception.", e);
			throw new RuntimeException(e);
		}
	}

	@AfterClass
	public static void tearDown() {
		em.clear();
		em.close();
		emf.close();
	}
}
