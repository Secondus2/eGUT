package utility;

import linearAlgebra.Vector;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dataIO.Log;
import dataIO.Log.Tier;
import generalInterfaces.Copyable;

/**
 * return deep copies of input objects
 * @author baco
 *
 */
public final class Copier {
	
	/**
	 * Attempts to create a deep copy of any input object
	 * @param <T>
	 * @param <K>
	 * @param copyable
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T, K> Object copy(Object copyable)
	{
		if (copyable == null)
		{
			Log.out(Tier.DEBUG, "Copier returns a null object");
			return null;
		}
		if (copyable instanceof Double || copyable.getClass() == double.class)
		{
			return new Double((double) copyable);
		}
		if (copyable instanceof Double[] || copyable.getClass() == 
				double[].class)
		{
			return Vector.copy((double[]) copyable);
		}
		if (copyable instanceof Integer || copyable.getClass() == int.class)
		{
			return new Integer((int) copyable);
		}
		if (copyable instanceof Integer[] || copyable.getClass() == 
				int[].class)
		{
			return Vector.copy((int[]) copyable);
		}
		if (copyable instanceof Boolean || copyable.getClass() == boolean.class)
		{
			return new Boolean((boolean) copyable);
		}
		if (copyable instanceof Boolean[] || copyable.getClass() == 
				boolean[].class)
		{
			return Vector.copy((boolean[]) copyable);
		}
		if  (copyable instanceof String)
		{
			// Strings are immutable
			return String.valueOf((String) copyable);
		}
		if (copyable instanceof List<?>)
		{
			List<T> spawn = new LinkedList<T>();
			for(int i = 0; i < ((List<?>) copyable).size(); i++)
				spawn.add((T) Copier.copy(((List<?>) copyable).get(i)));	
			return spawn;
		}
		if (copyable instanceof HashMap<?,?>)
		{
			Map<K,T> spawn = new HashMap<K,T>();
			for(Object key : ((Map<?,?>) copyable).keySet())
				spawn.put((K) key, (T) Copier.copy(((Map<?,?>) copyable).get((K) key)));	
			return spawn;
		}
		if (copyable instanceof Copyable)
		{
			return ((Copyable) copyable).copy();
		} 
		else 
		{
			Log.out(Tier.DEBUG,"WARNING: Attempting to deep copy unkown object"
					+ "of type" + copyable.getClass().getName() + " causion!");
			try {
				T duplicate = (T) copyable.getClass().newInstance();
				Field[] fields = duplicate.getClass().getDeclaredFields();
				for(Field f : fields)
				{
					f.set(duplicate, copy(f.get(copyable)));
				}
				return duplicate;				
			} catch (InstantiationException e) {
				Log.out(Tier.CRITICAL, "failed to create new instance of " + 
						copyable.getClass().getName());
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				Log.out(Tier.CRITICAL, "Copier could not acces object of type: "
						+ copyable.getClass().getName());
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
