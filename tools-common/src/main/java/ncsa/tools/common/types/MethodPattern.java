package ncsa.tools.common.types;

import java.util.ArrayList;
import java.util.List;

import ncsa.tools.common.NCSAConstants;
import ncsa.tools.common.util.TypeUtils;

public class MethodPattern
{
	private Class clzz;
	private List includes;
	private List excludes;
	private int numParams = NCSAConstants.UNDEFINED;
	private boolean nullParam = false;
	private boolean voidReturn = false;
	private boolean noExceptions = false;
	private boolean inObjectMethods = false;
	private boolean caseSensitive = true;
	private List wkInModifiers;
	private List wkExModifiers;
	private List stInModifiers;
	private List stExModifiers;
	private List paramTypes;
	private List inReturnTypes;
	private List exReturnTypes;
	private List inExceptionTypes;
	private List exExceptionTypes;

	public Class getClzz()
	{
		return clzz;
	}

	public void setClzz(Class clzz)
	{
		this.clzz = clzz;
	}

	public void setClassName(String fqn) throws ClassNotFoundException
	{
		this.clzz = TypeUtils.getClassForName(fqn);
	}

	public void setTarget(Object target)
	{
		this.clzz = target.getClass();
	}

	public List getExExceptionTypes()
	{
		return exExceptionTypes;
	}

	public void setExExceptionTypes(List exExceptionTypes)
	{
		this.exExceptionTypes = exExceptionTypes;
	}

	public List getExReturnTypes()
	{
		return exReturnTypes;
	}

	public void setExReturnTypes(List exReturnTypes)
	{
		this.exReturnTypes = exReturnTypes;
	}

	public List getInExceptionTypes()
	{
		return inExceptionTypes;
	}

	public void setInExceptionTypes(List inExceptionTypes)
	{
		this.inExceptionTypes = inExceptionTypes;
	}

	public boolean isCaseSensitive()
	{
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive)
	{
		this.caseSensitive = caseSensitive;
	}

	public boolean isInObjectMethods()
	{
		return inObjectMethods;
	}

	public void setInObjectMethods(boolean inObjectMethods)
	{
		this.inObjectMethods = inObjectMethods;
	}

	public List getInReturnTypes()
	{
		return inReturnTypes;
	}

	public void setInReturnTypes(List inReturnTypes)
	{
		this.inReturnTypes = inReturnTypes;
	}

	public boolean isNoExceptions()
	{
		return noExceptions;
	}

	public void setNoExceptions(boolean noExceptions)
	{
		this.noExceptions = noExceptions;
	}

	public boolean isNullParam()
	{
		return nullParam;
	}

	public void setNullParam(boolean nullParam)
	{
		this.nullParam = nullParam;
	}

	public int getNumParams()
	{
		return numParams;
	}

	public void setNumParams(int numParams)
	{
		this.numParams = numParams;
	}

	public List getParamTypes()
	{
		return paramTypes;
	}

	public void setParamTypes(List paramTypes)
	{
		this.paramTypes = paramTypes;
	}

	public List getStExModifiers()
	{
		return stExModifiers;
	}

	public void setStExModifiers(List stExModifiers)
	{
		this.stExModifiers = stExModifiers;
	}

	public List getStInModifiers()
	{
		return stInModifiers;
	}

	public void setStInModifiers(List stInModifiers)
	{
		this.stInModifiers = stInModifiers;
	}

	public boolean isVoidReturn()
	{
		return voidReturn;
	}

	public void setVoidReturn(boolean voidReturn)
	{
		this.voidReturn = voidReturn;
	}

	public List getWkExModifiers()
	{
		return wkExModifiers;
	}

	public void setWkExModifiers(List wkExModifiers)
	{
		this.wkExModifiers = wkExModifiers;
	}

	public List getWkInModifiers()
	{
		return wkInModifiers;
	}

	public void setWkInModifiers(List wkInModifiers)
	{
		this.wkInModifiers = wkInModifiers;
	}

	public List getExcludes()
	{
		return excludes;
	}

	public void setExcludes(List excludes)
	{
		this.excludes = excludes;
	}

	public List getIncludes()
	{
		return includes;
	}

	public void setIncludes(List includes)
	{
		this.includes = includes;
	}

	/**
	 * @param s
	 *            pattern to include.
	 */
	public void addInclude(String s)
	{
		if (includes == null)
			includes = new ArrayList();
		includes.add(s);
	} // addInclude

	/**
	 * @param s
	 *            pattern to exclude.
	 */
	public void addExclude(String s)
	{
		if (excludes == null)
			excludes = new ArrayList();
		excludes.add(s);
	} // addExcludes

	/**
	 * @param s
	 *            modifier to be included in a set, one element of which the
	 *            method must have.
	 */
	public void addWeaklyIncludedModifier(String s)
	{
		if (wkInModifiers == null)
			wkInModifiers = new ArrayList();
		wkInModifiers.add(s);
	} // addWeaklyIncludedModifier

	/**
	 * @param s
	 *            modifier to be included in a set, any element of which will
	 *            disqualify the method having it.
	 */
	public void addWeaklyExcludedModifier(String s)
	{
		if (wkExModifiers == null)
			wkExModifiers = new ArrayList();
		wkExModifiers.add(s);
	} // addWeaklyExcludedModifier

	/**
	 * @param s
	 *            modifier to be included in a set, all elements of which the
	 *            method must have.
	 */
	public void addStronglyIncludedModifier(String s)
	{
		if (stInModifiers == null)
			stInModifiers = new ArrayList();
		stInModifiers.add(s);
	} // addStronglyIncludedModifier

	/**
	 * @param s
	 *            modifier to be included in a set disqualifying any method having
	 *            all of its elements.
	 */
	public void addStronglyExcludedModifier(String s)
	{
		if (stExModifiers == null)
			stExModifiers = new ArrayList();
		stExModifiers.add(s);
	} // addStronglyExcludedModifier

	/**
	 * @param s
	 *            parameter type to match (must be added in order).
	 */
	public void addParameterType(String s)
	{
		if (paramTypes == null)
			paramTypes = new ArrayList();
		paramTypes.add(s);
	} // addParameterType

	/**
	 * @param s
	 *            return type to match (can be more than one).
	 */
	public void addIncludedReturnType(String s)
	{
		if (inReturnTypes == null)
			inReturnTypes = new ArrayList();
		inReturnTypes.add(s);
	} // addIncludedReturnType

	/**
	 * @param s
	 *            return type to exclude (can be more than one).
	 */
	public void addExcludedReturnType(String s)
	{
		if (exReturnTypes == null)
			exReturnTypes = new ArrayList();
		exReturnTypes.add(s);
	} // addExcludedReturnType

	/**
	 * @param s
	 *            exception type to match (can be more than one).
	 */
	public void addIncludedExceptionType(String s)
	{
		if (inExceptionTypes == null)
			inExceptionTypes = new ArrayList();
		inExceptionTypes.add(s);
	} // addIncludedExceptionType

	/**
	 * @param s
	 *            exception type to exclude (can be more than one).
	 */
	public void addExcludedExceptionType(String s)
	{
		if (exExceptionTypes == null)
			exExceptionTypes = new ArrayList();
		exExceptionTypes.add(s);
	} // addExcludedExceptionType

}
