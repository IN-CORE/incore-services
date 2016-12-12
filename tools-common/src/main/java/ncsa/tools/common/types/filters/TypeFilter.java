package ncsa.tools.common.types.filters;

import ncsa.tools.common.Filter;
import ncsa.tools.common.TypeFilterable;
import ncsa.tools.common.util.ComparisonUtils;

public class TypeFilter implements Filter
{
	private String type = null;

	public void setType(String s)
	{
		type = s;
	}

	public String getType()
	{
		return type;
	}

	/**
	 * @return true if object satisfies filter; false otherwise.
	 */
	public boolean matches(Object o)
	{
		if (!(o instanceof TypeFilterable))
			return false;
		String t = ((TypeFilterable) o).getType();
		return ComparisonUtils.matches(this, t);
	}
}
