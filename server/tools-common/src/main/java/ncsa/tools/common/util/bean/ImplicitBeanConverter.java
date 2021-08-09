/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.util.bean;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import ncsa.tools.common.exceptions.TypeConversionException;
import ncsa.tools.common.util.ReflectUtils;
import ncsa.tools.common.util.TypeUtils;

public class ImplicitBeanConverter {
    private static Logger logger = Logger.getLogger(ImplicitBeanConverter.class);

    private Map seen = new HashMap();
    private Map typeMap;

    /**
     * Introspection will be performed on the object as follows:
     * If the object is a primitive or String, and 'to' matches that class,
     * a new object will be contructed using the String contructor.
     * <p>
     * <p>
     * If the object is a Collection or Map, a new Collection or Map will be constructed, the typeMap will be used to lookup the
     * corresponding target class for entries, and this method will be iteratively applied to them. In this case, the handler must
     * be initialized with a typeMap, or the conversion will fail. Arrays are handled iteratively like collections, but do not
     * require a typeMap.
     * <p>
     * <p>
     * Otherwise, the source and target objects will be treated as Java beans, with this method recursively applied to their fields
     * using bean introspection.
     *
     * <p>
     * <p>
     * Also handles the special case of conversion from and to Axis-style wrapped arrays (with 'item' fields).
     *
     * @param from source object.
     * @param to   target class.
     * @return converted object.
     * @throws Exception if the two objects are not analogous (dissimilar
     *                   number of getters/setters or mismatched method names), if the parameters or
     *                   return values of field methods do not observe the contract of this
     *                   convert method, or if the source class is a Map or Collection and
     *                   there is no typeMap.
     */
    public Object convert(Object from, Class to) throws IllegalArgumentException, SecurityException, NoSuchMethodException,
        InstantiationException, IllegalAccessException, InvocationTargetException, TypeConversionException {
        logger.debug("convert: " + from + " to " + to);
        if (from == null)
            return null;

        String objectKey = ObjectUtils.identityToString(from);
        if (seen.containsKey(objectKey))
            return seen.get(objectKey);

        if (to == null)
            throw new IllegalArgumentException("to Class cannot be null");
        if (from instanceof Collection || from instanceof Map)
            throw new IllegalArgumentException("cannot handle instance of Collection or Map");

        Class classOfFrom = from.getClass();
        if (TypeUtils.isPrimVal(classOfFrom.getName()))
            return convertPrimitiveOrString(from, classOfFrom, to);

        if (classOfFrom.isArray())
            return convertArray(from, classOfFrom, to);

        Method getItem = getItemMethod(from.getClass());
        if (getItem != null && getItem.getReturnType().isArray()) {
            Object[] fromArray = getWrappedArray(from);
            return convert(fromArray, to);
        }

        if (from instanceof Collection)
            return convertCollection(from);

        if (from instanceof Map)
            return convertMap(from);

        return convertBean(from, classOfFrom, to);
    }

    // AUXILIARIES

    private Object convertArray(Object from, Class classOfFrom, Class classOfTo) throws InstantiationException, IllegalAccessException,
        ArrayIndexOutOfBoundsException, IllegalArgumentException, SecurityException, NoSuchMethodException, InvocationTargetException,
        TypeConversionException {
        logger.debug("convertArray: " + from + ", " + classOfTo);
        Object toWrapper = null;
        Method getItem = null;
        Class toSubType = null;
        Class fromSubType = classOfFrom.getComponentType();
        int len = Array.getLength(from);

        if (classOfTo != null) {
            getItem = getItemMethod(classOfTo);
            if (getItem != null && getItem.getReturnType().isArray()) {
                toWrapper = classOfTo.newInstance();
                seen.put(ObjectUtils.identityToString(from), toWrapper);
                toSubType = getItem.getReturnType().getComponentType();
            } else if (classOfTo.isArray()) {
                toSubType = classOfTo.getComponentType();
            } else {
                toSubType = (Class) typeMap.get(fromSubType.getName());
            }
        }

        if (toSubType == null)
            throw new IllegalArgumentException("no target class corresponding to " + fromSubType);

        Object[] toArray = (Object[]) Array.newInstance(toSubType, len);
        if (toWrapper == null)
            seen.put(ObjectUtils.identityToString(from), toArray);

        for (int i = 0; i < len; i++)
            toArray[i] = convert(Array.get(from, i), toSubType);

        if (toWrapper != null) {
            setWrappedArray(toArray, toWrapper);
            return toWrapper;
        }
        return toArray;
    }

    private Object convertCollection(Object from) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
        SecurityException, NoSuchMethodException, InvocationTargetException, TypeConversionException {
        logger.debug("convertCollection: " + from);
        if (typeMap == null)
            throw new IllegalArgumentException("cannot handle collections without a type map");
        Collection fromCollection = (Collection) from;
        Collection toCollection = (Collection) from.getClass().newInstance();
        for (Iterator i = fromCollection.iterator(); i.hasNext(); ) {
            Object next = i.next();
            Class to = (Class) typeMap.get(next.getClass().getName());
            toCollection.add(convert(i.next(), to));
        }
        return toCollection;
    }

    private Object convertMap(Object from) throws InstantiationException, IllegalAccessException, IllegalArgumentException,
        SecurityException, NoSuchMethodException, InvocationTargetException, TypeConversionException {
        logger.debug("convertMap: " + from);
        if (typeMap == null)
            throw new IllegalArgumentException("cannot handle maps without a type map");
        Map fromMap = (Map) from;
        Map toMap = (Map) from.getClass().newInstance();
        for (Iterator i = fromMap.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            Class toKeyClass = (Class) typeMap.get(entry.getKey().getClass().getName());
            Class toValueClass = (Class) typeMap.get(entry.getValue().getClass().getName());
            Object toKey = convert(entry.getKey(), toKeyClass);
            Object toValue = convert(entry.getValue(), toValueClass);
            toMap.put(toKey, toValue);
        }
        return toMap;
    }

    private Object convertBean(Object from, Class classOfFrom, Class to) throws IllegalArgumentException, SecurityException,
        NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException, TypeConversionException {
        logger.debug("convertArray: " + from + ", " + to);
        Object toBean = to.newInstance();
        seen.put(ObjectUtils.identityToString(from), toBean);
        Map fromGetters = ReflectUtils.mapGetters(from, false);
        Map toGetters = ReflectUtils.mapGetters(toBean, false);
        // check for homologous objects
        if (fromGetters.size() != toGetters.size())
            throw new IllegalArgumentException(from + " has " + fromGetters.size() + " get methods, " + toBean + " has " + toGetters.size());

        for (Iterator i = fromGetters.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            String field = (String) entry.getKey();
            Method fromGet = (Method) entry.getValue();
            Class fromRtValType = fromGet.getReturnType();
            String fromRtValTypeName = fromRtValType.getName();

            Class toRtValType = null;
            Method toGet = (Method) toGetters.get(field);
            if (toGet == null)
                throw new NoSuchMethodException("target class " + to + " has no get method for " + field);
            toRtValType = toGet.getReturnType();
            if (toRtValType == null)
                throw new IllegalArgumentException("no target class mapping for type " + fromRtValTypeName);

            Object fromRtVal = fromGet.invoke(from);
            Object toRtVal = convert(fromRtVal, toRtValType);

            String setterName = ReflectUtils.getMethodName("set", field);
            Method toSet = ReflectUtils.getMethod(to, setterName, new Class[]{toRtValType});
            if (toSet == null)
                throw new NoSuchMethodException("target class " + to + " has no set method for " + field + ", " + toRtValType);

            logger.debug("invoking " + toSet + " on " + toBean + " with " + toRtVal);
            toSet.invoke(toBean, new Object[]{toRtVal});
        }
        return toBean;
    }

    // STATIC AUXILIARIES

    private static Object convertPrimitiveOrString(Object from, Class classOfFrom, Class to) throws IllegalArgumentException,
        TypeConversionException {
        logger.debug("convertPrimitiveOrString: " + from + ", " + to);
        if (to != null && !TypeUtils.arePrimitiveAnalogous(classOfFrom, to))
            throw new IllegalArgumentException(to + " does not match " + classOfFrom);

        return TypeUtils.convertPrim(classOfFrom, from);
    }

    private static Method getItemMethod(Class clzz) {
        return ReflectUtils.getMethod(clzz, "getItem", null);
    }

    private static Object[] getWrappedArray(Object wrapper) throws IllegalArgumentException, IllegalAccessException,
        InvocationTargetException {
        Method m = ReflectUtils.getMethod(wrapper.getClass(), "getItem", null);
        return (Object[]) m.invoke(wrapper);
    }

    private static void setWrappedArray(Object[] array, Object wrapper) throws IllegalArgumentException, IllegalAccessException,
        InvocationTargetException {
        Method m = ReflectUtils.getMethod(wrapper.getClass(), "setItem", new Class[]{array.getClass()});
        m.invoke(wrapper, new Object[]{array});
    }

    public Map getTypeMap() {
        return typeMap;
    }

    public void setTypeMap(Map typeMap) {
        this.typeMap = typeMap;
    }

}
