package club.wodencafe.data;

import java.util.List;

import net.jodah.typetools.TypeResolver;

public class BusinessServiceJPA<T extends BusinessEntity> implements BusinessService<T> {

	private Class<T> clazz;

	public BusinessServiceJPA() {
		Class<?>[] typeArguments = TypeResolver.resolveRawArguments(BusinessServiceJPA.class, getClass());
		this.clazz = (Class<T>) typeArguments[0];

	}

	public BusinessServiceJPA(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public List<T> findAll() {
		try {
			return BusinessServiceUtil.findAllWithJPA(clazz);
		} catch (Throwable th) {
			th.printStackTrace();
		}
		return null;
	}

	@Override
	public void delete(Long id) {
		T entity = BusinessServiceUtil.findWithJPA(id, clazz);
		if (entity != null)
			BusinessServiceUtil.deleteWithJPA(entity, clazz);
	}

	@Override
	public T find(Long id) {
		return BusinessServiceUtil.findWithJPA(id, clazz);

	}

	@Override
	public boolean save(T entity) {
		return BusinessServiceUtil.saveWithJPA(entity, clazz);

	}

	@Override
	public long count() {
		return BusinessServiceUtil.count(clazz);

	}

	@Override
	public void deleteAll() {
		BusinessServiceUtil.deleteAllWithJPA(clazz);

	}

	@Override
	public Class<T> getWrappedClass() {
		return clazz;
	}

	@Override
	public void refresh(T entity) {
		BusinessServiceUtil.refreshWithJPA(entity, clazz);
	}
}