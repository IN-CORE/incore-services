package ncsa.tools.common.comparators;

import java.lang.reflect.Method;

/**
 * Compares stringified Method descriptors.
 * 
 * @author Albert L. Rossi
 */
public class MethodComparator extends ReversibleComparator
{
	public int compare(Object arg0, Object arg1)
	{
		if (arg0 == null)
			return sign;
		if (arg1 == null)
			return -sign;

		if (arg0 instanceof Method && arg1 instanceof Method) {
			String s0 = ((Method) arg0).getName();
			String s1 = ((Method) arg1).getName();
			return s0.compareTo(s1) * sign;
		}
		return 0;
	}
}
