package ncsa.tools.common.types.filters;

import ncsa.tools.common.Filter;
import ncsa.tools.common.util.ComparisonUtils;

public class ClassFilter implements Filter
{
	private Class clzz = null;

	public void setTypeFromString(String s) throws ClassNotFoundException
	{
		clzz = Class.forName(s);
	}

	public void setTypeFromClass(Class c)
	{
		clzz = c;
	}

	public Class getType()
	{
		return clzz;
	}

	public String getTypeString()
	{
		return clzz == null ? null : clzz.toString();
	}

	/**
	 * @return true if object satisfies filter; false otherwise.
	 */
	public boolean matches(Object o)
	{
		return ComparisonUtils.matches(this, o.getClass());
	}
}