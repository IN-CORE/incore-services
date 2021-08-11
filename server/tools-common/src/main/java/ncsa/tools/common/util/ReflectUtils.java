/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.util;

import ncsa.tools.common.comparators.MethodComparator;
import ncsa.tools.common.exceptions.ReflectionException;
import ncsa.tools.common.types.MethodPattern;

import java.lang.reflect.*;
import java.util.*;

/**
 * Provides convenience static wrappers around some of the standard Java
 * reflection functionality.
 *
 * @author Albert L. Rossi
 */
public class ReflectUtils {
    /**
     * Static utility class; cannot be constructed.
     */
    private ReflectUtils() {
    }

    private static final String[] mutatorPrefixes = {"set", "add", "addConfigured"};

    private static final MethodComparator methodComparator = new MethodComparator();

    /**
     * @param methodName of null parameter method to invoke.
     * @param target     object on which to invoke.
     * @return return value of invoked method.
     * @throws ReflectionException if no such method exists.
     */
    public static Object invokeOn(String methodName, Object target) throws ReflectionException {
        Method method = null;
        try {
            method = target.getClass().getMethod(methodName);
        } catch (NoSuchMethodException nsme) {
            throw new ReflectionException("invokeOn: " + nsme);
        }
        return invokeOn(method, target, null);
    } // invokeOn ( String, Object )

    /**
     * @param methodName of method to invoke.
     * @param target     object on which to invoke.
     * @param ptypes     classes of method parameters.
     * @param pvalues    parameter values.
     * @return return value of invoked method.
     * @throws ReflectionException if no such method exists.
     */
    public static Object invokeOn(String methodName, Object target, Class[] ptypes, Object[] pvalues) throws ReflectionException {
        Method method = null;
        try {
            method = target.getClass().getMethod(methodName, ptypes);
        } catch (NoSuchMethodException nsme) {
            throw new ReflectionException("invokeOn: " + nsme);
        }
        return invokeOn(method, target, pvalues);
    } // invokeOn( String, Object, Class[], Object[] )

    /**
     * @param method  method to invoke.
     * @param target  object on which to invoke.
     * @param pvalues parameter values.
     * @return return value of invoked method.
     * @throws ReflectionException ,
     *                             if invocation throws exception.
     */
    public static Object invokeOn(Method method, Object target, Object[] pvalues) throws ReflectionException {
        try {
            return method.invoke(target, pvalues);
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
            throw new ReflectionException("invokeOn: " + ite);
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
            throw new ReflectionException("invokeOn: " + iae);
        } catch (IllegalArgumentException iarge) {
            iarge.printStackTrace();
            throw new ReflectionException("invokeOn: " + iarge);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            throw new ReflectionException("invokeOn: " + npe);
        } catch (ExceptionInInitializerError eiie) {
            eiie.printStackTrace();
            throw new ReflectionException("invokeOn: " + eiie);
        }
    } // invokeOn( Method, Object, Object[] )

    /**
     * @param method  array of methods to try to invoke. returns on first successful
     *                invocation
     * @param target  object on which to invoke.
     * @param pvalues parameter values.
     * @return return value of invoked method.
     * @throws ReflectionException ,
     *                             if all invocations fail.
     */
    public static Object invokeOn(Method[] method, Object target, Object[] pvalues) throws ReflectionException {
        Object returnValue = null;
        boolean succeeded = false;
        ReflectionException last = null;
        for (int i = 0; i < method.length; i++) {
            try {
                returnValue = invokeOn(method[i], target, pvalues);
                succeeded = true;
                break;
            } catch (ReflectionException ncsae) {
                last = ncsae;
            }
        }
        if (succeeded)
            return returnValue;
        throw new ReflectionException("no successful invocations; tried " + method.length + " methods; last exception thrown:", last);
    } // // invokeOn( Method[], Object, Object[] )

    /**
     * @param fqn     of object to be constructed.
     * @param ptypes  classes of constructor's parameters.
     * @param pvalues parameter values.
     * @return the constructed object.
     * @throws ReflectionException ,
     *                             if invocation throws exception.
     */
    public static Object callConstructor(String fqn, Class[] ptypes, Object[] pvalues) throws ReflectionException {
        try {
            Class instanceType = TypeUtils.getClassForName(fqn);
            return callConstructor(instanceType, ptypes, pvalues);
        } catch (ClassNotFoundException cnfe) {
            throw new ReflectionException("callConstructor " + fqn, cnfe);
        }
    } // callConstructor

    /**
     * @param type    class of object to be constructed.
     * @param ptypes  classes of constructor's parameters.
     * @param pvalues parameter values.
     * @return the constructed object.
     * @throws ReflectionException ,
     *                             if invocation throws exception.
     */
    public static Object callConstructor(Class type, Class[] ptypes, Object[] pvalues) throws ReflectionException {
        Constructor constructor = null;
        try {
            constructor = type.getConstructor(ptypes);
            return constructor.newInstance(pvalues);
        } catch (NoSuchMethodException nsme) {
            throw new ReflectionException("callConstructor", nsme);
        } catch (InstantiationException ie) {
            throw new ReflectionException("callConstructor", ie);
        } catch (InvocationTargetException ite) {
            throw new ReflectionException("callConstructor", ite);
        } catch (IllegalAccessException iae) {
            throw new ReflectionException("callConstructor", iae);
        } catch (IllegalArgumentException iarge) {
            throw new ReflectionException("callConstructor", iarge);
        } catch (NullPointerException npe) {
            throw new ReflectionException("callConstructor", npe);
        } catch (ExceptionInInitializerError eiie) {
            throw new ReflectionException("callConstructor", eiie);
        }
    } // callConstructor

    /**
     * This method will change xml-element-style names into humpback: e.g.,
     * 'name-of-element' becomes 'nameOfElement';
     *
     * @param prefix e.g., "add, create, get, set, etc.".
     * @param name   usually corresponds to the attribute or element name.
     * @return properly concatenated full method name.
     */
    public static String getMethodName(String prefix, String name) {
        StringBuffer mName = new StringBuffer(prefix);
        if (name != null) {
            mName.append(name.substring(0, 1).toUpperCase(Locale.ENGLISH));
            int lastCharIndex = name.length() - 1;
            for (int i = 1; i < name.length(); i++) {
                char c = name.charAt(i);
                if (c == '-') {
                    if (i < lastCharIndex) {
                        i++;
                        String s = "" + name.charAt(i);
                        s = s.toUpperCase(Locale.ENGLISH);
                        c = s.charAt(0);
                    }
                }
                mName.append(c);
            }
        }
        return mName.toString();
    } // getMethodName

    /**
     * @param clzz   class of object in question.
     * @param name   of method in question.
     * @param ptypes types of the method's parameters.
     * @return the method, if it exists, or null.
     */
    public static Method getMethod(Class clzz, String name, Class[] ptypes) {
        try {
            return clzz.getMethod(name, ptypes);
        } catch (NoSuchMethodException pass) {
        }
        return null;
    } // getMethod

    /**
     * @param type   fqn of object in question.
     * @param name   of method in question.
     * @param ptypes types of the method's parameters.
     * @return the method, if it exists, or null.
     */
    public static Method getMethod(String type, String name, Class[] ptypes) throws ClassNotFoundException {
        return getMethod(TypeUtils.getClassForName(type), type, ptypes);
    }

    /**
     * @param value presumably indexed object.
     * @return numerical wrapper representing size.
     * @throws ReflectionException if value is null or not an Array or List.
     */
    public static Long getIndexedSize(Object value) throws IllegalArgumentException {
        if (value == null)
            throw new IllegalArgumentException("object was null");

        if (value instanceof List)
            return new Long(((List) value).size());

        if (value.getClass().isArray())
            return new Long(Array.getLength(value));

        throw new IllegalArgumentException("cannot get size of " + value + "; object is not a List or an Array");
    } // getIndexedSize

    /**
     * Generates a "tag" from the class name by changing '$' to '-' and making
     * all letters lower case.
     *
     * @param clzz Class for which to derive a tag.
     * @return tag for the Class name.
     */
    public static String getTag(Class clzz) {
        String className = clzz.getName();
        int index = className.lastIndexOf(".") + 1;
        return className.substring(index).replace('$', '-').toLowerCase();
    } // getTag

    public static boolean isAssignableFrom(String className, Object o) {
        Class clzz = o.getClass();
        List possibleClasses = new ArrayList();
        possibleClasses.add(clzz);
        possibleClasses.addAll(Arrays.asList(getSuperclasses(clzz)));
        possibleClasses.addAll(Arrays.asList(getInterfaces(clzz)));
        Class[] allClasses = (Class[]) possibleClasses.toArray(new Class[0]);
        for (int i = 0; i < allClasses.length; i++)
            if (className.equals(allClasses[i].getName()))
                return true;
        return false;
    }

    /**
     * Recurs on all superclasses and interfaces.
     *
     * @param clzz to check for implementation of interfaces.
     * @return array of Class objects corresponding to all implemented
     * interfaces, including those extended by the class's interfaces, as
     * well as those of superclasses.
     */
    public static Class[] getInterfaces(Class clzz) {
        List classes = new ArrayList();
        classes.add(clzz);
        classes.addAll(Arrays.asList(getSuperclasses(clzz)));
        HashMap map = new HashMap();
        for (ListIterator lit = classes.listIterator(); lit.hasNext(); ) {
            Class[] interfaces = ((Class) lit.next()).getInterfaces();
            if (interfaces == null)
                continue;
            for (int i = 0; i < interfaces.length; i++) {
                map.put(interfaces[i], interfaces[i]);
                Class[] superinterfaces = getInterfaces(interfaces[i]);
                for (int j = 0; j < superinterfaces.length; j++)
                    map.put(superinterfaces[j], superinterfaces[j]);
            }
        }
        return (Class[]) map.values().toArray(new Class[0]);
    } // getInterfaces

    /**
     * @param clzz to check for ancestor classes.
     * @return all superclasses of the given class.
     */
    public static Class[] getSuperclasses(Class clzz) {
        List l = new ArrayList();
        Class c = clzz;
        while (true) {
            c = c.getSuperclass();
            if (c == null)
                break;
            l.add(c);
        }
        return (Class[]) l.toArray(new Class[0]);
    } // getSuperclasses

    public static Method[] findMatchingInvocableMethods(Class target, String methodName, Object[] params, boolean full)
        throws ReflectionException {
        int numParams = params == null ? 0 : params.length;

        Method[] m = getPublicMethodsForName(target, methodName, numParams, full);
        if (numParams == 0)
            return m;
        List matches = new ArrayList();
        int i = 0;
        if (m != null) {
            for (; i < m.length; i++) {
                Class[] ptypes = m[i].getParameterTypes();
                int j = 0;
                for (; j < numParams; j++) {
                    if (params[j] == null)
                        continue;
                    if (!ptypes[j].isAssignableFrom(params[j].getClass())
                        && !TypeUtils.arePrimitiveAnalogous(ptypes[j], params[j].getClass()))
                        break;
                }
                if (j == numParams)
                    matches.add(m[i]);
            }
        }
        return (Method[]) matches.toArray(new Method[0]);
    }

    public static Constructor findMatchingConstructor(Class target, Object[] params) throws ReflectionException {
        Constructor[] constructors = target.getConstructors();
        for (int i = 0; i < constructors.length; i++) {
            Class[] ptypes = constructors[i].getParameterTypes();
            if (ptypes == null || ptypes.length == 0) {
                if (params == null || params.length == 0)
                    return constructors[i];
            }
            if (ptypes.length == params.length) {
                int j = 0;
                for (; j < ptypes.length; j++) {
                    if (!ptypes[j].isAssignableFrom(params[j].getClass()))
                        break;
                }
                if (j == ptypes.length)
                    return constructors[i];
            }
        }
        throw new ReflectionException("no matching constructor found for " + (params == null ? "empty" : "" + Arrays.asList(params))
            + " on " + target);
    }

    public static Map mapGetters(Object o, boolean fullNameKey) {
        Method[] m = findGetterMethods(o);
        Map map = new HashMap();
        if (m != null) {
            for (int i = 0; i < m.length; i++) {
                String name = m[i].getName();
                if (fullNameKey)
                    map.put(name, m[i]);
                else {
                    String prefix = "get";
                    if (name.startsWith("is"))
                        prefix = "is";
                    map.put(StringUtils.getLCSuffix(prefix, name), m[i]);
                }
            }
        }
        return map;
    }

    public static Method[] findGetterMethods(Object o) {
        List v = new ArrayList();
        Class clzz = o.getClass();
        while (clzz != null) {
            if (clzz.equals(Object.class))
                break;
            if (clzz == null)
                break;
            Method[] m = clzz.getDeclaredMethods();
            for (int i = 0; i < m.length; i++) {
                if (isPublicInstanceMethod(m[i].getModifiers()) && (m[i].getName().startsWith("get") || m[i].getName().startsWith("is"))
                    && m[i].getParameterTypes().length == 0 && !m[i].getReturnType().equals(Void.class))
                    v.add(m[i]);
            }
            clzz = clzz.getSuperclass();
        }
        Method[] result = (Method[]) v.toArray(new Method[0]);
        Arrays.sort(result, methodComparator);
        return result;
    }

    public static Method[] findSetterMethods(Object o, String suffix) throws ReflectionException {
        return findSingleParamVoidReturnMethods(o, "set", suffix);
    }

    public static Method[] findAddMethods(Object o, String suffix) throws ReflectionException {
        return findSingleParamVoidReturnMethods(o, "add", suffix);
    }

    public static Method[] findAddConfiguredMethods(Object o, String suffix) throws ReflectionException {
        return findSingleParamVoidReturnMethods(o, "addConfigured", suffix);
    }

    public static Method[] findSingleParamVoidReturnMethods(Object o, String prefix, String suffix) throws ReflectionException {
        if (prefix == null)
            throw new ReflectionException("findSingleParamVoidReturnMethods: " + "cannot look up method with null prefix ");
        String name = (suffix == null ? prefix : ReflectUtils.getMethodName(prefix, suffix));
        List v = new ArrayList();
        Class clzz = o.getClass();
        while (clzz != null) {
            if (clzz.equals(Object.class))
                break;
            if (clzz == null)
                break;
            Method[] m = clzz.getDeclaredMethods();
            for (int i = 0; i < m.length; i++) {
                if (isPublicInstanceMethod(m[i].getModifiers()) && m[i].getName().equals(name) && m[i].getParameterTypes().length == 1
                    && m[i].getReturnType().equals(Void.TYPE))
                    v.add(m[i]);
            }
            clzz = clzz.getSuperclass();
        }
        if (v.size() < 1)
            throw new ReflectionException("cannot find method " + name + " on " + o.getClass());

        return (Method[]) v.toArray(new Method[0]);
    }

    public static boolean isPublicInstanceMethod(int modifiers) {
        return !Modifier.isAbstract(modifiers) && !Modifier.isNative(modifiers) && Modifier.isPublic(modifiers)
            && !Modifier.isStatic(modifiers);
    }

    public static Class findTypeBasedOnMethodName(Object target, String name) throws ReflectionException {
        for (int i = 0; i < mutatorPrefixes.length; i++) {
            Method[] m = null;
            try {
                m = ReflectUtils.findSingleParamVoidReturnMethods(target, mutatorPrefixes[i], name);
            } catch (ReflectionException didNotFind) {
            }
            if (m != null && m.length > 0) {
                Class[] paramClzzs = m[0].getParameterTypes();
                // if multiple, return first
                if (paramClzzs.length > 0)
                    return paramClzzs[0];
            }
        }

        throw new ReflectionException("no such method for tag " + name + " on " + target);
    }

    /* ////////////////////////// METHOD SCANNER ///////////////////////////// */

    /**
     * Searches for all public non-static "set..." single-parameter methods.
     *
     * @param clzz class of object to be inspected.
     * @param full if true, searches superclasses up to but excluding Object.
     *             else, searches only the immediate class.
     * @return array of two-member String arrays containing the
     * normalized name (first letter after prefix changed to lower
     * case) plus fully qualified Java class name.
     * @throws ReflectionException if scan fails.
     */
    public static String[][] getAttributes(Class clzz, boolean full) throws ReflectionException {
        return getPublicInstanceMethodsForPrefix(clzz, "set", true, full);
    }

    /**
     * Searches for all public non-static "add..." or "addConfigured..."
     * single-parameter methods.
     *
     * @param clzz class of object to be inspected.
     * @param full if true, searches superclasses up to but excluding Object.
     *             else, searches only the immediate class.
     * @return array of two-member String arrays containing the
     * normalized name (first letter after prefix changed to lower
     * case) plus fully qualified Java class name.
     * @throws ReflectionException if scan fails.
     */
    public static String[][] getAddedElements(Class clzz, boolean full) throws ReflectionException {
        return getPublicInstanceMethodsForPrefix(clzz, "add", true, full);
    }

    /**
     * Searches for all public non-static "create..." zero-parameter methods.
     *
     * @param clzz class of object to be inspected.
     * @param full if true, searches superclasses up to but excluding Object.
     *             else, searches only the immediate class.
     * @return array of two-member String arrays containing the
     * normalized name (first letter after prefix changed to lower
     * case) plus fully qualified Java class name.
     * @throws ReflectionException if scan fails.
     */
    public static String[][] getCreatedElements(Class clzz, boolean full) throws ReflectionException {
        return getPublicInstanceMethodsForPrefix(clzz, "create", false, full);
    }

    /**
     * Searches for all public non-static "get..." zero-parameter methods.
     *
     * @param clzz class of object to be inspected.
     * @param full if true, searches superclasses up to but excluding Object.
     *             else, searches only the immediate class.
     * @return array of two-member String arrays containing the
     * normalized name (first letter after prefix changed to lower
     * case) plus fully qualified Java class name.
     * @throws ReflectionException if scan fails.
     */
    public static String[][] getReturned(Class clzz, boolean full) throws ReflectionException {
        return getPublicInstanceMethodsForPrefix(clzz, "get", false, full);
    }

    /**
     * Searches for all public non-static "is..." zero-parameter methods.
     *
     * @param clzz class of object to be inspected.
     * @param full if true, searches superclasses up to but excluding Object.
     *             else, searches only the immediate class.
     * @return array of two-member String arrays containing the
     * normalized name (first letter after prefix changed to lower
     * case) plus fully qualified Java class name.
     * @throws ReflectionException if scan fails.
     */
    public static String[][] getBoolean(Class clzz, boolean full) throws ReflectionException {
        return getPublicInstanceMethodsForPrefix(clzz, "is", false, full);
    }

    /**
     * @param clzz        class of object to be inspected.
     * @param prefix      prefix string to match.
     * @param singleParam if true, looks for 1-parameter methods; if false,
     *                    0-parameter methods.
     * @param full        if true, all superclasses excluding Object will be searched.
     * @return array of two-member String arrays containing the
     * normalized name (first letter after prefix changed to lower
     * case) plus fully qualified Java class name for parameter type
     * @throws ReflectionException if scan fails.
     */
    public static String[][] getPublicInstanceMethodsForPrefix(Class clzz, String prefix, boolean singleParam, boolean full)
        throws ReflectionException {
        try {
            MethodPattern methodPattern = new MethodPattern();
            methodPattern.setClzz(clzz);
            methodPattern.addInclude(prefix + "**");
            methodPattern.setNumParams(singleParam ? 1 : 0);
            methodPattern.addWeaklyIncludedModifier("public");
            methodPattern.addWeaklyExcludedModifier("static");
            methodPattern.addWeaklyExcludedModifier("abstract");
            methodPattern.addWeaklyExcludedModifier("native");
            methodPattern.setInObjectMethods(false);

            MethodScanner scanner = new MethodScanner(methodPattern);

            if (full)
                scanner.fullScan();
            else
                scanner.shallowScan();

            List pairs = new ArrayList();
            String name;
            String type;
            Method[] methods = scanner.getIncluded();
            for (int i = 0; i < methods.length; i++) {
                name = StringUtils.getLCSuffix(prefix, methods[i].getName());
                if (methods[i].getParameterTypes().length == 1)
                    type = methods[i].getParameterTypes()[0].getName();
                else
                    type = "null";
                pairs.add(new String[]{name, type});
            }
            return (String[][]) pairs.toArray(new String[0][0]);
        } catch (Throwable t) {
            throw new ReflectionException("getMethodsForPrefix", t);
        }
    } // getMethodsForPrefix

    /**
     * @param clzz      class of object to be inspected.
     * @param name      string to match.
     * @param numParams number of parameters on method.
     * @param full      if true, all superclasses excluding Object will be searched.
     * @return matching method descriptors
     * @throws ReflectionException if scan fails.
     */
    public static Method[] getPublicMethodsForName(Class clzz, String name, int numParams, boolean full) throws ReflectionException {
        try {
            MethodPattern methodPattern = new MethodPattern();
            methodPattern.setClzz(clzz);
            methodPattern.addInclude(name);
            methodPattern.setNumParams(numParams);
            methodPattern.addWeaklyIncludedModifier("public");
            methodPattern.addWeaklyExcludedModifier("abstract");
            methodPattern.addWeaklyExcludedModifier("native");
            methodPattern.setInObjectMethods(false);

            MethodScanner scanner = new MethodScanner(methodPattern);

            if (full)
                scanner.fullScan();
            else
                scanner.shallowScan();
            return scanner.getIncluded();
        } catch (Throwable t) {
            throw new ReflectionException("getMethodsForPrefix", t);
        }
    } // getMethodsForPrefix

}
