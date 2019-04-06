package club.wodencafe.data;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.apache.commons.beanutils.BeanUtils;

public class MiscUtil {

	public static void createNewPlayer(Player player) {

	}

	/**
	 * Use reflection to shallow copy simple type fields with matching names from
	 * one object to another
	 * 
	 * @param fromObj the object to copy from
	 * @param toObj   the object to copy to
	 */
	public static void copyMatchingFields(Object fromObj, Object toObj) {
		/*
		 * if (fromObj == null || toObj == null) throw new
		 * NullPointerException("Source and destination objects must be non-null" );
		 * 
		 * Class fromClass = fromObj.getClass(); Class toClass = toObj.getClass();
		 * 
		 * Field[] fields = fromClass.getDeclaredFields(); for (Field f : fields) { try
		 * { Field t = toClass.getDeclaredField(f.getName());
		 * 
		 * if (t.getType() == f.getType()) { // extend this if to copy more immutable
		 * types if interested if (t.getType() == String.class || t.getType() ==
		 * int.class || t.getType() == Integer.class || t.getType() == char.class ||
		 * t.getType() == Character.class) { f.setAccessible(true);
		 * t.setAccessible(true); t.set(toObj, f.get(fromObj)); } else if (t.getType()
		 * == Date.class) { // dates are not immutable, so clone non-null dates into //
		 * the destination object Date d = (Date) f.get(fromObj); f.setAccessible(true);
		 * t.setAccessible(true); t.set(toObj, d != null ? d.clone() : null); } } }
		 * catch (NoSuchFieldException ex) { // skip it } catch (IllegalAccessException
		 * ex) { ex.printStackTrace(); // log.error("Unable to copy field: {}",
		 * f.getName()); } }
		 */
		try {
			String version = null;
			try {
				version = BeanUtils.getProperty(toObj, "version");
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				BeanUtils.copyProperties(toObj, fromObj);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			}
			if (version != null) {
				try {
					BeanUtils.setProperty(toObj, "version", version);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Object getStaticPrivateField(Class<?> clazz, String fieldName) {
		try {
			// Get field instance
			Field field = clazz.getDeclaredField(fieldName);
			field.setAccessible(true); // Suppress Java language access checking

			// Remove "final" modifier
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

			return field.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
