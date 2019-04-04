package club.wodencafe.data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.hsqldb.persist.HsqlProperties;
import org.hsqldb.server.Server;

import com.google.common.util.concurrent.AbstractIdleService;

public class DatabaseService extends AbstractIdleService implements AutoCloseable {
	public static DatabaseService SINGLETON = new DatabaseService();
	private static boolean shutdownHookAdded = false;

	Server server = new Server();

	private DatabaseService() {
		if (!shutdownHookAdded) {
			shutdownHookAdded = true;

			Runtime.getRuntime().addShutdownHook(new DatabaseServiceShutdownHook());
		}
	}

	public void shutDown() {
		if (!server.isNotRunning()) {
			try {
				server.shutdown();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			try {
				server.stop();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

	}

	public void startUp() throws Exception {

		Path userDir = Paths.get(System.getProperty("user.home"), ".wodbot");
		if (!Files.exists(userDir)) {
			Files.createDirectories(userDir);
		}
		Path databasePath = userDir.resolve("database.hsql");
		System.out.println("Starting Database");
		HsqlProperties p = new HsqlProperties();
		p.setProperty("server.database.0", "file:" + databasePath);
		p.setProperty("server.dbname.0", "hsqldb");
		p.setProperty("server.port", "9001");

		server.setProperties(p);

		server.setLogWriter(null); // can use custom writer
		server.setErrWriter(null); // can use custom writer
		server.start();

	}

	private static class DatabaseServiceShutdownHook extends Thread {

		public DatabaseServiceShutdownHook() {
			setDaemon(true);
		}

		@Override
		public void run() {
			try {
				SINGLETON.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();

		close();
	}

	@Override
	public void close() throws Exception {
		try {
			if (SINGLETON.isRunning()) {
				DatabaseService.SINGLETON.shutDown();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
