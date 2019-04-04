package club.wodencafe.bot;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import club.wodencafe.data.DatabaseService;

public class WodBotClass {
	public static void main(String[] args) throws Exception {

		try (DatabaseService service = DatabaseService.SINGLETON) {
			service.startAsync();
			service.awaitRunning();
			EntityManager em = Persistence.createEntityManagerFactory("poker-prod").createEntityManager();
			System.out.println(em);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
