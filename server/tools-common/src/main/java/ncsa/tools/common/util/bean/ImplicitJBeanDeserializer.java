/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.util.bean;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.QName;

import ncsa.tools.common.exceptions.DeserializationException;
import ncsa.tools.common.exceptions.ReflectionException;
import ncsa.tools.common.exceptions.TypeConversionException;
import ncsa.tools.common.util.ReflectUtils;
import ncsa.tools.common.util.TypeUtils;

public class ImplicitJBeanDeserializer {
    private Map ancestorRefs = null;

    public void initialize() {
        ancestorRefs = new HashMap();
    }

    public Object instantiate(Element element, Object parent) throws DeserializationException {
        String name = element.getName();
        Object instanceOfThisElement = null;

        QName idQn = new QName("id", ImplicitJBeanSerializer.namespace);
        QName classQn = new QName("class", ImplicitJBeanSerializer.namespace);
        QName mSuffixQn = new QName("methodSuffix", ImplicitJBeanSerializer.namespace);

        Attribute metaId = element.attribute(idQn);
        Attribute metaClass = element.attribute(classQn);
        Attribute metaMethodSuffix = element.attribute(mSuffixQn);

        if (metaId == null)
            throw new DeserializationException("missing id metadata for element " + element);
        String id = metaId.getValue();
        if (metaClass == null)
            throw new DeserializationException("missing type metadata for element " + element);

        Class clzz = null;
        try {
            clzz = TypeUtils.getClassForName(metaClass.getValue());
        } catch (ClassNotFoundException t) {
            throw new DeserializationException("could not get class " + metaClass.getValue(), t);
        }

        if (parent != null && metaMethodSuffix == null)
            throw new DeserializationException("do not know how element " + element + " relates to parent " + parent);

        String suffix = null;
        if (metaMethodSuffix != null)
            suffix = metaMethodSuffix.getValue();

        if (ancestorRefs.containsKey(id)) {
            instanceOfThisElement = ancestorRefs.get(id);
        } else if (TypeUtils.isPrimVal(name)) {
            instanceOfThisElement = instantiatePrimitive(clzz, element.attribute("value"));
        } else if (name.equals("array")) {
            instanceOfThisElement = instantiateArray(id, clzz, element.elements());
            clzz = instanceOfThisElement.getClass();
        } else if (name.equals("collection")) {
            instanceOfThisElement = instantiateCollection(id, clzz, element.elements());
        } else if (name.equals("map")) {
            instanceOfThisElement = instantiateMap(id, clzz, element.elements());
        } else if (name.equals("null")) {
            instanceOfThisElement = null;
        } else {
            instanceOfThisElement = instantiateBean(id, element, clzz, suffix);
        }

        // now attach the object to parent
        if (parent != null) {
            Method[] m;
            try {
                m = ReflectUtils.findSetterMethods(parent, suffix);
                ReflectUtils.invokeOn(m, parent, new Object[]{instanceOfThisElement});
            } catch (ReflectionException t) {
                throw new DeserializationException("could not set " + instanceOfThisElement + " on " + parent, t);
            }

        }
        return instanceOfThisElement;
    }

    private Object instantiateArray(String id, Class elementType, List elements) throws DeserializationException {
        Object arrayInstance = Array.newInstance(elementType, elements.size());
        ancestorRefs.put(id, arrayInstance);

        for (int i = 0; i < elements.size(); i++) {
            Element e = (Element) elements.get(i);

            if (e.getName().equals("null")) {
                Array.set(arrayInstance, i, null);
                continue;
            }

            if (!e.getName().equals("array-element"))
                throw new DeserializationException("malformed array: element " + e + " is not an array-element");
            Attribute a = e.attribute("value");
            if (a != null) {
                Object o = null;
                try {
                    o = TypeUtils.convertPrim(elementType, a.getValue());
                } catch (TypeConversionException t) {
                    throw new DeserializationException("instantiateArray", t);
                }
                if (elementType.equals(Boolean.TYPE)) {
                    Array.setBoolean(arrayInstance, i, ((Boolean) o).booleanValue());
                } else if (elementType.equals(Byte.TYPE)) {
                    Array.setByte(arrayInstance, i, ((Byte) o).byteValue());
                } else if (elementType.equals(Character.TYPE)) {
                    Array.setChar(arrayInstance, i, ((Character) o).charValue());
                } else if (elementType.equals(Double.TYPE)) {
                    Array.setDouble(arrayInstance, i, ((Double) o).doubleValue());
                } else if (elementType.equals(Float.TYPE)) {
                    Array.setFloat(arrayInstance, i, ((Float) o).floatValue());
                } else if (elementType.equals(Integer.TYPE)) {
                    Array.setInt(arrayInstance, i, ((Integer) o).intValue());
                } else if (elementType.equals(Long.TYPE)) {
                    Array.setLong(arrayInstance, i, ((Long) o).longValue());
                } else if (elementType.equals(Short.TYPE)) {
                    Array.setShort(arrayInstance, i, ((Integer) o).shortValue());
                } else {
                    Array.set(arrayInstance, i, o);
                }
            } else {
                // there should be only one
                Element nested = (Element) e.elements().get(0);
                Object o = instantiate(nested, null);
                if (!elementType.isInstance(o))
                    throw new DeserializationException("array element " + i + " of wrong type " + o.getClass() + ", should be "
                        + elementType);
                Array.set(arrayInstance, i, o);
            }
        }
        return arrayInstance;
    }

    private Object instantiateCollection(String id, Class clzz, List elements) throws DeserializationException {
        Collection collectionInstance = null;
        try {
            collectionInstance = (Collection) ReflectUtils.callConstructor(clzz, null, null);
        } catch (ReflectionException t) {
            throw new DeserializationException("could not instantiate " + clzz, t);
        }
        ancestorRefs.put(id, collectionInstance);

        Object[] array = new Object[elements.size()];

        for (int i = 0; i < elements.size(); i++) {
            Element e = (Element) elements.get(i);
            Object o = null;
            if (!e.getName().equals("null")) {
                if (!e.getName().equals("collection-element"))
                    throw new DeserializationException("malformed collection: element " + e + " is not a collection-element");
                o = instantiatePlaceholder(e);
            }

            int index = Integer.parseInt(e.attribute("index").getValue());
            array[index] = o;
        }
        for (int i = 0; i < array.length; i++)
            collectionInstance.add(array[i]);
        return collectionInstance;
    }

    private Object instantiateMap(String id, Class clzz, List elements) throws DeserializationException {
        Map mapInstance = null;
        try {
            mapInstance = (Map) ReflectUtils.callConstructor(clzz, null, null);
        } catch (ReflectionException t) {
            throw new DeserializationException("could not instantiate " + clzz, t);
        }
        ancestorRefs.put(id, mapInstance);

        for (int i = 0; i < elements.size(); i++) {
            Element e = (Element) elements.get(i);
            if (!e.getName().equals("map-entry"))
                throw new DeserializationException("malformed map: element " + e + " is not a map-entry");

            Element keyElement = e.element("key");
            Element valueElement = e.element("value");

            if (keyElement == null)
                throw new DeserializationException("map entry " + e + " has no key");

            Object key = instantiatePlaceholder(keyElement);
            Object value = null;
            if (valueElement != null)
                value = instantiatePlaceholder(valueElement);

            mapInstance.put(key, value);
        }
        return mapInstance;
    }

    private Object instantiatePlaceholder(Element e) throws DeserializationException {
        Object o = null;
        Attribute a = e.attribute("value");
        if (a != null) {
            Attribute t = e.attribute("type");
            Class elementType = null;
            if (t == null)
                elementType = String.class;
            else
                try {
                    elementType = TypeUtils.getClassForName(t.getValue());
                } catch (ClassNotFoundException t1) {
                    throw new DeserializationException("instantiatePlaceholder", t1);
                }
            try {
                o = TypeUtils.convertPrim(elementType, a.getValue());
            } catch (TypeConversionException t1) {
                throw new DeserializationException("instantiatePlaceholder", t1);
            }

        } else {
            // there should be only one
            Element nested = (Element) e.elements().get(0);
            o = instantiate(nested, null);
        }
        return o;
    }

    private Object instantiatePrimitive(Class clzz, Attribute attribute) throws DeserializationException {
        if (attribute == null)
            throw new DeserializationException("cannot construct primitive object without value attribute");
        String value = attribute.getValue();
        if (value == null)
            throw new DeserializationException("cannot construct primitive object with null value parameter");

        // will fail if no string contructor
        try {
            return ReflectUtils.callConstructor(clzz, new Class[]{String.class}, new Object[]{value});
        } catch (ReflectionException t) {
            throw new DeserializationException("could not construct " + clzz + " with " + value, t);
        }
    }

    private Object instantiateBean(String id, Element element, Class clzz, String suffix) throws DeserializationException {
        try {
            // will fail if no default contructor
            Object o = ReflectUtils.callConstructor(clzz, null, null);
            ancestorRefs.put(id, o);

            // take care of attributes and children
            List attributes = element.attributes();
            for (int i = 0; i < attributes.size(); i++) {
                Attribute a = (Attribute) attributes.get(i);
                if (a.getNamespace().equals(ImplicitJBeanSerializer.namespace))
                    continue;
                Method[] m = ReflectUtils.findSetterMethods(o, a.getName());
                /*
                 * attribute methods should not be overloaded; so m[].length should = 1
                 * get the type of the method on the object, create the parameter and
                 * invoke (it had better have a String constructor!)
                 */
                Class paramClzz = m[0].getParameterTypes()[0];
                if (paramClzz.isPrimitive())
                    paramClzz = TypeUtils.getClassForType(paramClzz);
                Object param = ReflectUtils.callConstructor(paramClzz, new Class[]{String.class}, new Object[]{a.getValue()});
                ReflectUtils.invokeOn(m[0], o, new Object[]{param});
            }

            List children = element.elements();
            for (int i = 0, size = children.size(); i < size; i++) {
                Element child = (Element) children.get(i);
                instantiate(child, o);
            }

            return o;
        } catch (ReflectionException re) {
            throw new DeserializationException("could not instantiate bean " + id + " from " + element + ", class " + clzz, re);
        }
    }

}
