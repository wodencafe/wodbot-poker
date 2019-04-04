package club.wodencafe.data;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class BusinessListener {

	public static final BusinessListener SINGLETON;
	private static Subject<BusinessEntity> createSubject;
	private static Subject<BusinessEntity> saveSubject;
	private static Subject<BusinessEntity> deleteSubject;

	static {
		SINGLETON = new BusinessListener();
		saveSubject = PublishSubject.create();
		saveSubject = saveSubject.toSerialized();
		createSubject = PublishSubject.create();
		createSubject = createSubject.toSerialized();
		deleteSubject = PublishSubject.create();
		deleteSubject = deleteSubject.toSerialized();
	}

	public static final <T extends BusinessEntity> Observable<T> onCreate(final Class<T> clazz) {

		return createSubject.filter(x -> x.getClass().equals(clazz)).map(clazz::cast);
	}

	public static final <T extends BusinessEntity> Observable<T> onSave(final Class<T> clazz) {
		return saveSubject.filter(x -> x.getClass().equals(clazz)).map(clazz::cast);
	}

	public static final <T extends BusinessEntity> Observable<T> onDelete(final Class<T> clazz) {
		return deleteSubject.filter(x -> x.getClass().equals(clazz)).map(clazz::cast);
	}

	@PostRemove
	public <T extends BusinessEntity> void onDelete(final T entity) {
		deleteSubject.onNext(entity);
	}

	@PostPersist
	public <T extends BusinessEntity> void onCreate(final T entity) {
		createSubject.onNext(entity);
	}

	@PostUpdate
	public <T extends BusinessEntity> void onPersist(final T entity) {
		saveSubject.onNext(entity);
	}

}