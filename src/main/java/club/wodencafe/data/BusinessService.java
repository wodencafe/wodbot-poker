package club.wodencafe.data;

import java.util.List;

public interface BusinessService<T extends BusinessEntity> {

	void delete(Long id);

	List<T> findAll();

	T find(Long id);

	boolean save(T entity);

	long count();

	void deleteAll();

	public Class<T> getWrappedClass();

	void refresh(T entity);
}
