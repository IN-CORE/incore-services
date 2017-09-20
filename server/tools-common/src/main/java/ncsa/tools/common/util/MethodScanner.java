package ncsa.tools.common.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ncsa.tools.common.NCSAConstants;
import ncsa.tools.common.comparators.MethodComparator;
import ncsa.tools.common.comparators.ToStringComparator;
import ncsa.tools.common.exceptions.ScanException;
import ncsa.tools.common.types.MethodPattern;

/**
 * Does introspection of a given class for methods matching the
 * various patterns or requirements set.
 * 
 * @author Albert L. Rossi
 */
public class MethodScanner extends AbstractScanner
{
	/* Class to inspect */
	private Class clzz = null;

	/*
	 * Conjunctive matching:
	 * include means all must be included;
	 * exlude means all must be excluded.
	 * 
	 * 0 = paramter types; 1 = included modifiers; 2 = excluded modifiers.
	 */
	private List[] strongConstraints = new ArrayList[] { null, null, null };

	/*
	 * Disjunctive matching:
	 * include means at least one must be satisfied;
	 * exlude means at least one must be excluded.
	 * 
	 * 0 = included return types; 1 = excluded return types;
	 * 2 = included exception types; 3 = excluded exception types
	 * 4 = included modifiers; 5 = excluded modifiers.
	 */
	private Map[] weakConstraints = new HashMap[] { null, null, null, null, null, null };

	/* Results */
	private List included = null;
	private List excluded = null;
	private List notIncluded = null;

	private int numParams = NCSAConstants.UNDEFINED;
	private boolean nullParam = false;
	private boolean voidReturn = false;
	private boolean noExceptions = false;
	private boolean includeObjectMethods = true;
	private boolean sort = false;

	/**
	 * Constructor. Associates scanner with TrebuchetPattern. Sets class type
	 * and internal fields on basis of pattern.
	 * 
	 * @param mp
	 *            TrebuchetMethodPattern to use in connection with this scanner.
	 * @throws ScanException
	 * @throws NCSAException
	 *             if class is not found.
	 */
	public MethodScanner(MethodPattern mp) throws ScanException
	{
		clzz = mp.getClzz();
		setPatterns(mp);
	} // constructor

	/**
	 * Scans object class and all superclasses. Sorts results, if so
	 * indicated.
	 * 
	 * @throws ScanException
	 * 
	 * @throws NCSAException
	 *             if scanning fails.
	 */
	public void fullScan() throws ScanException
	{
		if (clzz == null && failonerror)
			throw new ScanException(this.getClass().getName(), new ClassNotFoundException("null class"));

		init();
		List v = new ArrayList();

		Class c = clzz;

		while (c != null) {
			if (!includeObjectMethods && clzz.equals(Object.class))
				break;
			Method[] dm = c.getDeclaredMethods();
			if (dm != null)
				v.addAll(Arrays.asList(dm));
			c = c.getSuperclass();
		}
		Method[] methods = (Method[]) v.toArray(new Method[0]);
		scanMethods(methods);
		sort();
	} // fullScan

	/**
	 * Scans only the object's class. Returns list of superclasses.
	 * Sorts results if so indicated.
	 * 
	 * @throws NCSAException
	 *             if scanning fails.
	 */
	public List shallowScan() throws ScanException
	{
		if (clzz == null && failonerror)
			throw new ScanException(this.getClass().getName(), new ClassNotFoundException("null class"));

		init();
		List superclasses = new ArrayList();
		if (clzz == null)
			return superclasses;

		Method[] methods = clzz.getDeclaredMethods();
		scanMethods(methods);

		Class currClzz = clzz.getSuperclass();
		while (currClzz != null && !currClzz.equals(Object.class)) {
			superclasses.add(currClzz.getName());
			currClzz = currClzz.getSuperclass();
		}
		sort();
		if (sort)
			superclasses = ListUtils.sort(superclasses, new ToStringComparator());
		return superclasses;
	} // shallowScan

	/**
	 * NOP.
	 */
	public List scanNext()
	{
		return new ArrayList();
	}

	/**
	 * Same as full s
	 * 
	 * /**
	 * NOP.
	 */
	public void close()
	{
	}

	/**
	 * Constructs storage Lists or clears them if present.
	 */
	protected void init()
	{
		try {
			included.clear();
			notIncluded.clear();
			excluded.clear();
		} catch (NullPointerException npe) {
			included = new ArrayList();
			notIncluded = new ArrayList();
			excluded = new ArrayList();
		}
	} // init

	/*
	 * //////////////////////////////////////////////////////////////////////////
	 * MUTATORS //
	 * ///////////////////////////////////////////////////////////////////////
	 */

	/**
	 * @param b
	 *            if true, sort the list of methods
	 *            by their string representation.
	 */
	public void setSort(boolean b)
	{
		sort = b;
	}

	/*
	 * //////////////////////////////////////////////////////////////////////////
	 * ACCESSORS //
	 * ///////////////////////////////////////////////////////////////////////
	 */

	/**
	 * @return included method objects.
	 */
	public Method[] getIncluded()
	{
		return (Method[]) included.toArray(new Method[0]);
	}

	/**
	 * @return excluded method objects.
	 */
	public Method[] getExcluded()
	{
		return (Method[]) excluded.toArray(new Method[0]);
	}

	/**
	 * @return not included method objects.
	 */
	public Method[] getNotIncluded()
	{
		return (Method[]) notIncluded.toArray(new Method[0]);
	}

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * AUXILIARY MATCHING METHODS //
	 * //////////////////////////////////////////////////////////////////////
	 */

	/**
	 * @return true only if all include and parameter contraints
	 *         are satifisfied.
	 */
	private boolean isIncluded(Method method)
	{
		return includesName(method.getName()) && checkStrongModifiers(method.getModifiers(), true)
				&& checkWeakModifiers(method.getModifiers(), true) && satisfiesParamConstraints(method)
				&& includesReturn(method.getReturnType()) && includesException(method.getExceptionTypes());
	} // isIncluded

	/**
	 * @return true if at least one exclude constraint is satisfied.
	 */
	private boolean isExcluded(Method method)
	{
		return excludesName(method.getName()) || checkStrongModifiers(method.getModifiers(), false)
				|| checkWeakModifiers(method.getModifiers(), false) || excludesReturn(method.getReturnType())
				|| excludesException(method.getExceptionTypes());
	} // isIncluded

	/**
	 * @param name
	 *            method name to check.
	 * @return true if name matches include pattern.
	 */
	private boolean includesName(String name)
	{
		for (int i = 0; i < includes.length; i++) {
			if (match(includes[i], name, isCaseSensitive)) {
				return true;
			}
		}
		return false;
	} // isIncluded

	/**
	 * @param name
	 *            method name to check.
	 * @return true if name matches exclude pattern.
	 */
	private boolean excludesName(String name)
	{
		for (int i = 0; i < excludes.length; i++) {
			if (match(excludes[i], name, isCaseSensitive)) {
				return true;
			}
		}
		return false;
	} // isExcluded

	/**
	 * @param methodModifiers
	 *            bit-wise value for method.
	 * @param include
	 *            whether this is an include or exclude match.
	 * @return true if method modifiers match all of the strong constraints.
	 */
	private boolean checkStrongModifiers(int methodModifiers, boolean include)
	{
		int index = include ? 1 : 2;

		if (strongConstraints[index] == null)
			return include ? true : false;

		int modifier = NCSAConstants.UNDEFINED;
		for (int i = 0; i < strongConstraints[index].size(); i++) {
			modifier = ((Integer) strongConstraints[index].get(i)).intValue();
			switch (modifier) {
			case Modifier.ABSTRACT:
				if (!Modifier.isAbstract(methodModifiers))
					return false;
				break;
			case Modifier.FINAL:
				if (!Modifier.isFinal(methodModifiers))
					return false;
				break;
			case Modifier.NATIVE:
				if (!Modifier.isNative(methodModifiers))
					return false;
				break;
			case Modifier.PRIVATE:
				if (!Modifier.isPrivate(methodModifiers))
					return false;
				break;
			case Modifier.PROTECTED:
				if (!Modifier.isProtected(methodModifiers))
					return false;
				break;
			case Modifier.PUBLIC:
				if (!Modifier.isPublic(methodModifiers))
					return false;
				break;
			case Modifier.STATIC:
				if (!Modifier.isStatic(methodModifiers))
					return false;
				break;
			case Modifier.SYNCHRONIZED:
				if (!Modifier.isSynchronized(methodModifiers))
					return false;
				break;
			}
		}
		return true;
	} // checkStrongModifiers

	/**
	 * @param methodModifiers
	 *            bit-wise value for method.
	 * @param include
	 *            whether this is an include or exclude match.
	 * @return true if method modifiers match at least one weak constraint.
	 */
	private boolean checkWeakModifiers(int methodModifiers, boolean include)
	{
		int index = include ? 4 : 5;

		if (weakConstraints[index] == null)
			return include ? true : false;

		if (Modifier.isAbstract(methodModifiers) && weakConstraints[index].containsKey(new Integer(Modifier.ABSTRACT)))
			return true;
		if (Modifier.isFinal(methodModifiers) && weakConstraints[index].containsKey(new Integer(Modifier.FINAL)))
			return true;
		if (Modifier.isNative(methodModifiers) && weakConstraints[index].containsKey(new Integer(Modifier.NATIVE)))
			return true;
		if (Modifier.isPrivate(methodModifiers) && weakConstraints[index].containsKey(new Integer(Modifier.PRIVATE)))
			return true;
		if (Modifier.isProtected(methodModifiers) && weakConstraints[index].containsKey(new Integer(Modifier.PROTECTED)))
			return true;
		if (Modifier.isPublic(methodModifiers) && weakConstraints[index].containsKey(new Integer(Modifier.PUBLIC)))
			return true;
		if (Modifier.isStatic(methodModifiers) && weakConstraints[index].containsKey(new Integer(Modifier.STATIC)))
			return true;
		if (Modifier.isSynchronized(methodModifiers) && weakConstraints[index].containsKey(new Integer(Modifier.SYNCHRONIZED)))
			return true;

		return false;
	} // checkWeakModifiers

	/**
	 * @param method
	 *            to check for parameter number and types.
	 * @return true if method's parameters match the constraints.
	 */
	private boolean satisfiesParamConstraints(Method method)
	{
		Class[] types = method.getParameterTypes(); // will not be null

		if (nullParam) {
			if (types.length == 0)
				return true;
			return false;
		}

		if (numParams != NCSAConstants.UNDEFINED) {
			if (types.length == numParams)
				return true;
			return false;
		}

		if (strongConstraints[0] == null)
			return true;

		if (types.length != strongConstraints[0].size())
			return false;

		for (int i = 0; i < types.length; i++)
			if (!strongConstraints[0].get(i).equals(types[i]))
				return false;
		return true;
	} // matchesParamConstraints

	/**
	 * @param type
	 *            to check.
	 * @return true if type matches at least one pattern.
	 */
	private boolean includesReturn(Class type)
	{
		if (voidReturn) {
			if (type == null || type.equals(Void.TYPE))
				return true;
			return false;
		}
		if (weakConstraints[0] == null)
			return true;
		return weakConstraints[0].containsKey(type);
	} // includesReturn

	/**
	 * @param type
	 *            to check.
	 * @return true if type matches at least one pattern.
	 */
	private boolean excludesReturn(Class type)
	{
		if (weakConstraints[1] == null)
			return false;
		return weakConstraints[1].containsKey(type);
	} // excludesReturn

	/**
	 * @param types
	 *            to check.
	 * @return true if at least one type matches at least one pattern.
	 */
	private boolean includesException(Class[] types)
	{
		if (noExceptions) {
			if (types.length == 0)
				return true;
			return false;
		}

		if (weakConstraints[2] == null)
			return true;

		for (int i = 0; i < types.length; i++)
			if (weakConstraints[2].containsKey(types[i]))
				return true;
		return false;
	} // includesException

	/**
	 * @param types
	 *            to check.
	 * @return true if at least one type matches at least one pattern.
	 */
	private boolean excludesException(Class[] types)
	{
		if (weakConstraints[3] == null)
			return false;

		for (int i = 0; i < types.length; i++)
			if (weakConstraints[3].containsKey(types[i]))
				return true;
		return false;
	} // excludesException

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * AUXILIARY OBJECT FIELD METHODS //
	 * //////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Accesses the method pattern object for all fields.
	 * Normalizes null includes and sets default excludes if so indicated.
	 * 
	 * @throws NCSAException
	 *             if an inconsistent setting is encountered.
	 */
	private void setPatterns(MethodPattern methodPattern) throws ScanException
	{
		List l = methodPattern.getIncludes();
		if (l != null)
			setIncludes((String[]) l.toArray(new String[0]));
		if (includes == null || includes.length == 0) {
			// No includes supplied, so set it to 'matches all'
			includes = new String[1];
			includes[0] = "**";
		}

		l = methodPattern.getExcludes();
		if (l != null)
			setExcludes((String[]) l.toArray(new String[0]));
		if (excludes == null)
			excludes = new String[0];

		l = methodPattern.getParamTypes();
		if (l != null)
			setStrongConstraints(0, (String[]) l.toArray(new String[0]));
		l = methodPattern.getStInModifiers();
		if (l != null)
			setStrongConstraints(1, (String[]) l.toArray(new String[0]));
		l = methodPattern.getStExModifiers();
		if (l != null)
			setStrongConstraints(2, (String[]) l.toArray(new String[0]));

		l = methodPattern.getInReturnTypes();
		if (l != null)
			setWeakConstraints(0, (String[]) l.toArray(new String[0]));
		l = methodPattern.getExReturnTypes();
		if (l != null)
			setWeakConstraints(1, (String[]) l.toArray(new String[0]));
		l = methodPattern.getInExceptionTypes();
		if (l != null)
			setWeakConstraints(2, (String[]) l.toArray(new String[0]));
		l = methodPattern.getExExceptionTypes();
		if (l != null)
			setWeakConstraints(3, (String[]) l.toArray(new String[0]));
		l = methodPattern.getWkInModifiers();
		if (l != null)
			setWeakConstraints(4, (String[]) l.toArray(new String[0]));
		l = methodPattern.getWkExModifiers();
		if (l != null)
			setWeakConstraints(5, (String[]) l.toArray(new String[0]));

		includeObjectMethods = methodPattern.isInObjectMethods();
		setCaseSensitive(methodPattern.isCaseSensitive());

		nullParam = methodPattern.isNullParam(); // overrides previous setting
		if (nullParam)
			strongConstraints[0] = null;

		numParams = methodPattern.getNumParams();
		if (strongConstraints[0] != null && numParams != -1 && strongConstraints[0].size() != numParams)
			throw new ScanException("number of parameters is inconsistent");

		voidReturn = methodPattern.isVoidReturn(); // overrides previous setting
		if (voidReturn) {
			weakConstraints[0] = null;
			weakConstraints[1] = null;
		}

		noExceptions = methodPattern.isNoExceptions(); // overrides previous setting
		if (noExceptions) {
			weakConstraints[2] = null;
			weakConstraints[3] = null;
		}

	} // setPatterns

	/**
	 * Sets the weak constraint data structure.
	 * 
	 * @param index
	 *            pointing to the data structure.
	 * @param s
	 *            array of objects to set.
	 */
	private void setWeakConstraints(int index, String[] s) throws ScanException
	{
		Object[] o = s;

		if (o == null) {
			weakConstraints[index] = null;
			return;
		}

		if (weakConstraints[index] == null) {
			weakConstraints[index] = new HashMap();
		} else {
			weakConstraints[index].clear();
		}

		if (index < 4) {
			try {
				o = getClassArray(s);
			} catch (ClassNotFoundException cnfe) {
				throw new ScanException(this.getClass().getName(), cnfe);
			}
		} else if (index < 6) {
			o = getModifierCodes(s);
		}

		for (int i = 0; i < o.length; i++)
			weakConstraints[index].put(o[i], new Method[0]);
	} // setWeakConstraint

	/**
	 * Sets the strong constraint data structure.
	 * 
	 * @param index
	 *            pointing to the data structure.
	 * @param s
	 *            array of objects to set.
	 */
	private void setStrongConstraints(int index, String[] s) throws ScanException
	{
		Object[] o = s;

		if (o == null) {
			strongConstraints[index] = null;
			return;
		}

		if (strongConstraints[index] == null) {
			strongConstraints[index] = new ArrayList();
		} else {
			strongConstraints[index].clear();
		}

		if (index == 0) {
			try {
				o = getClassArray(s);
			} catch (ClassNotFoundException cnfe) {
				throw new ScanException(this.getClass().getName(), cnfe);
			}
		} else if (index < 3) {
			o = getModifierCodes(s);
		}

		for (int i = 0; i < o.length; i++)
			strongConstraints[index].add(o[i]);
	} // setStrongConstraint

	/**
	 * Converts a string into the corresponding modifier code.
	 * 
	 * @param s
	 *            modifier types.
	 * @return wrapped Modifier code value.
	 * @throws NCSAException
	 *             if type is not valid.
	 */
	private Integer[] getModifierCodes(String[] s) throws IllegalArgumentException
	{
		Integer[] result = new Integer[s.length];
		for (int i = 0; i < s.length; i++) {
			if (s[i].equalsIgnoreCase("abstract"))
				result[i] = new Integer(Modifier.ABSTRACT);
			else if (s[i].equalsIgnoreCase("final"))
				result[i] = new Integer(Modifier.FINAL);
			else if (s[i].equalsIgnoreCase("native"))
				result[i] = new Integer(Modifier.NATIVE);
			else if (s[i].equalsIgnoreCase("private"))
				result[i] = new Integer(Modifier.PRIVATE);
			else if (s[i].equalsIgnoreCase("protected"))
				result[i] = new Integer(Modifier.PROTECTED);
			else if (s[i].equalsIgnoreCase("public"))
				result[i] = new Integer(Modifier.PUBLIC);
			else if (s[i].equalsIgnoreCase("static"))
				result[i] = new Integer(Modifier.STATIC);
			else if (s[i].equalsIgnoreCase("synchronized"))
				result[i] = new Integer(Modifier.SYNCHRONIZED);
			else
				throw new IllegalArgumentException("invalid modifier for methods: " + s);
		}
		return result;
	} // toModifier

	/**
	 * Converts array of objects into array of corresponding classes.
	 * Strings are treated as fully qualified names.
	 * 
	 * @param s
	 *            array of fully qualified name strings.
	 * @return array of Class objects corresponding to the given types.
	 */
	private Class[] getClassArray(String[] s) throws ClassNotFoundException
	{
		if (s == null)
			return null;
		Class[] c = new Class[s.length];
		for (int i = 0; i < c.length; i++)
			c[i] = Class.forName(s[i]);
		return c;
	} // getClassArray

	/**
	 * Sorts the Lists, if appropriate.
	 */
	private void sort()
	{
		MethodComparator c = new MethodComparator();
		c.setDescending(sortDescending);
		if (sort) {
			if (retainIncluded)
				included = ListUtils.sort(included, c);
			if (retainExcluded)
				excluded = ListUtils.sort(excluded, c);
			if (retainNotIncluded)
				notIncluded = ListUtils.sort(notIncluded, c);
		}
	} // sort

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * AUXILIARY SCANNING METHODS //
	 * //////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Does the actual scanning call.
	 * 
	 * @param methods
	 *            array of methods to scan.
	 */
	private void scanMethods(Method[] methods)
	{
		for (int i = 0; i < methods.length; i++) {
			if (isIncluded(methods[i])) {
				if (!isExcluded(methods[i])) {
					if (retainIncluded) {
						included.add(methods[i]);
					}
				} else if (retainExcluded) {
					excluded.add(methods[i]);
				}
			} else if (retainNotIncluded) {
				notIncluded.add(methods[i]);
			}
		}
	} // scanMethods

}
