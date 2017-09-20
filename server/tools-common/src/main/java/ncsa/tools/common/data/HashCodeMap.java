package ncsa.tools.common.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.ObjectUtils;

/**
 * Composes a synchronized HashMap. Overrides the <code>put</code> method
 * to use the hashcode of the value as the actual key. Also provides
 * a single-parameter version of <code>put</code> for the same purpose.
 * <p>
 * 
 * All other methods delegate to the internal map.
 * 
 * @author Albert L. Rossi
 */
public class HashCodeMap implements Map
{
	private Map map = Collections.synchronizedMap(new HashMap());

	/**
	 * Uses the hashcode of the object as key.
	 * 
	 * @param o
	 *            value to store.
	 * @return hashcode.
	 * 
	 */
	public String put(Object o)
	{
		return put(o, map);
	}

	/**
	 * Calls put( p2 ). Ignores p1.
	 */
	public Object put(Object p1, Object p2)
	{
		return put(p2, map);
	}

	/**
	 * @return size of internal map
	 */
	public int size()
	{
		return map.size();
	}

	/**
	 * @return true if internal map is empty.
	 */
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	/**
	 * @param key
	 *            to check.
	 * @return true if key is in the internal map.
	 */
	public boolean containsKey(Object key)
	{
		return map.containsKey(key);
	}

	/**
	 * @param value
	 *            to check
	 * @return true if value is in the internal map.
	 */
	public boolean containsValue(Object value)
	{
		return map.containsValue(value);
	}

	/**
	 * @param key
	 *            of the value to get.
	 * @return value.
	 */
	public Object get(Object key)
	{
		return map.get(key);
	}

	/**
	 * @param key
	 *            of the value to remove.
	 * @return value.
	 */
	public Object remove(Object key)
	{
		return map.remove(key);
	}

	/**
	 * NB: the hashcode of the objects in the map will be substituted for
	 * their original keys.
	 * 
	 * @param m
	 *            all of whose key-value entries should be added.
	 */
	public void putAll(Map m)
	{
		HashMap tmp = new HashMap(); // avoids repeated call on synchronized map
		synchronized (m) {
			for (Iterator it = m.values().iterator(); it.hasNext();)
				put(it.next(), tmp);
		}
		map.putAll(tmp);
	} // putAll

	/**
	 * Clears internal map.
	 */
	public void clear()
	{
		map.clear();
	}

	/**
	 * @return key set of internal map.
	 */
	public Set keySet()
	{
		return map.keySet();
	}

	/**
	 * @return values of internal map.
	 */
	public Collection values()
	{
		return map.values();
	}

	/**
	 * @return entry set of internal map.
	 */
	public Set entrySet()
	{
		return map.entrySet();
	}

	/**
	 * Auxiliary. Uses object's hashcode as key.
	 * This method is unsynchronized.
	 * 
	 * @param o
	 *            object to add to map
	 * @param m
	 *            map to which to add object.
	 * @return hashcode.
	 */
	private static String put(Object o, Map m)
	{
		String hashCode = ObjectUtils.identityToString(o);
		m.put(hashCode, o);
		return hashCode;
	} // put( o, m )

}
