/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.util.bean;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.apache.commons.lang.ObjectUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;

import ncsa.tools.common.AbstractSerializationComparator;
import ncsa.tools.common.comparators.DefaultSerializationComparator;
import ncsa.tools.common.exceptions.ReflectionException;
import ncsa.tools.common.exceptions.SerializationException;
import ncsa.tools.common.util.ReflectUtils;
import ncsa.tools.common.util.StringUtils;
import ncsa.tools.common.util.TypeUtils;

public class ImplicitJBeanSerializer {
    static Namespace namespace = new Namespace("meta", "ncsa.common.util");

    private static DefaultSerializationComparator defaultComparator = new DefaultSerializationComparator();

    private int count;
    private Map ancestorRefs = null;
    private AbstractSerializationComparator comparator = defaultComparator;

    public void initialize() {
        count = 0;
        ancestorRefs = new HashMap();
    }

    public String getBeanAsXml(Object bean) throws SerializationException {
        Element root = getElement(null, null, bean);
        return root.asXML();
    }

    public Element getElement(Element parent, String mSuffix, Object instanceOfThisType) throws SerializationException {
        if (instanceOfThisType == null)
            return null;

        Element element = null;

        String[] mdata = getTagQNPair(instanceOfThisType);

        if (instanceOfThisType instanceof String || TypeUtils.isPrimitiveWrapper(instanceOfThisType.getClass())) {
            if (parent == null) {
                parent = createElement(mdata, mSuffix, parent, instanceOfThisType);
                if (parent == null)
                    throw new SerializationException("element was null for " + instanceOfThisType);
                String id = Integer.toString(count++);
                parent.addAttribute(new QName("id", namespace), id);
                element = parent;
            }
            addAttribute(mdata, mSuffix, parent, instanceOfThisType);
        } else {
            element = createElement(mdata, mSuffix, parent, instanceOfThisType);
            if (element == null)
                throw new SerializationException("element was null for " + instanceOfThisType);

            String hashCode = ObjectUtils.identityToString(instanceOfThisType);
            if (ancestorRefs.containsKey(hashCode)) {
                element.addAttribute(new QName("id", namespace), (String) ancestorRefs.get(hashCode));
                if (parent != null)
                    parent.add(element);
                return element;
            }

            String id = Integer.toString(count++);
            element.addAttribute(new QName("id", namespace), id);
            if (parent != null)
                parent.add(element);
            ancestorRefs.put(hashCode, id);

            if (instanceOfThisType.getClass().isArray()) {
                processArrayElement(element, instanceOfThisType);
            } else if (instanceOfThisType instanceof Collection) {
                processCollectionElement(element, instanceOfThisType);
            } else if (instanceOfThisType instanceof Map) {
                processMapElement(element, instanceOfThisType);
            } else {
                processBeanElement(element, instanceOfThisType);
            }
        }
        return element;
    }

    private void addAttribute(String[] mdata, String mSuffix, Element parent, Object instance) throws SerializationException {
        if (parent == null) {
            throw new SerializationException("unexpected parse error: attribute: " + instance + " has no parent element");
        }

        if (mSuffix == null) {
            mSuffix = "value";
        }

        if (mSuffix.equals("value")) {
            String pname = parent.getName();
            if (pname.equals("collection-element") || pname.equals("key") || pname.equals("value"))
                parent.addAttribute("type", mdata[1]);
        }

        parent.addAttribute(mSuffix, instance.toString());
    }

    private Element createElement(String[] mdata, String mSuffix, Element parent, Object instance) {

        Element element = DocumentHelper.createElement(mdata[0]);
        element.addAttribute(new QName("class", namespace), mdata[1]);
        if (mSuffix != null)
            element.addAttribute(new QName("methodSuffix", namespace), mSuffix);
        return element;
    }

    private void processArrayElement(Element element, Object instance) throws SerializationException {
        // "array" element name already set
        int len = Array.getLength(instance);
        Object[] array = new Object[len];
        for (int i = 0; i < len; i++)
            array[i] = Array.get(instance, i);
        processArray(element, array);
    }

    private void processCollectionElement(Element element, Object instance) throws SerializationException {
        element.setName("collection");
        Collection c = (Collection) instance;
        Object[] array = c.toArray();
        if (!(c instanceof SortedSet)) {
            Arrays.sort(array, comparator);
        }
        processArray(element, array);
    }

    private void processMapElement(Element element, Object instance) throws SerializationException {
        element.setName("map");
        Map m = (Map) instance;
        Map.Entry[] entries = (Map.Entry[]) m.entrySet().toArray(new Map.Entry[0]);
        if (!(m instanceof SortedMap)) {
            Arrays.sort(entries, comparator);
        }
        processMapEntries(element, entries);
    }

    private void processBeanElement(Element element, Object instance) throws SerializationException {
        Method[] getters = ReflectUtils.findGetterMethods(instance);
        for (int i = 0; i < getters.length; i++) {
            String subSuffix = StringUtils.getLCSuffix("get", getters[i].getName());
            try {
                Object subInstance = ReflectUtils.invokeOn(getters[i], instance, null);
                if (subInstance == null)
                    continue;
                getElement(element, subSuffix, subInstance);
            } catch (ReflectionException re) {
                throw new SerializationException("processBeanElement", re);
            }
        }
    }

    private void processArray(Element parent, Object[] array) throws SerializationException {
        String entryType = parent.getName() + "-element";
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null) {
                Element entry = parent.addElement("null");
                entry.addAttribute("index", "" + i);
            } else {
                Element entry = parent.addElement(entryType);
                entry.addAttribute("index", "" + i);
                getElement(entry, null, array[i]);
            }
        }
    }

    private void processMapEntries(Element parent, Map.Entry[] entries) throws SerializationException {
        for (int i = 0; i < entries.length; i++) {
            Element entry = parent.addElement("map-entry");
            Element key = entry.addElement("key");
            getElement(key, null, entries[i].getKey());
            if (entries[i].getValue() != null) {
                Element value = entry.addElement("value");
                getElement(value, null, entries[i].getValue());
            }
        }
    }

    private static String[] getTagQNPair(Object o) {
        String[] parts = new String[2];
        parts[1] = o.getClass().getName();

        if (parts[1].endsWith(";")) {
            parts[1] = parts[1].substring(2, parts[1].length() - 1);
            parts[0] = "array";
        } else if (parts[1].startsWith("[")) {
            parts[1] = parts[1].substring(1);
            if (parts[1].length() == 1)
                switch (parts[1].charAt(0)) {
                    case 'Z':
                        parts[1] = "boolean";
                        break;
                    case 'B':
                        parts[1] = "byte";
                        break;
                    case 'C':
                        parts[1] = "char";
                        break;
                    case 'D':
                        parts[1] = "double";
                        break;
                    case 'F':
                        parts[1] = "float";
                        break;
                    case 'I':
                        parts[1] = "int";
                        break;
                    case 'J':
                        parts[1] = "long";
                        break;
                    case 'S':
                        parts[1] = "short";
                        break;
                }
            parts[0] = "array";
        } else {
            if (parts[1].indexOf(".") > 0) {
                StringBuffer sb = new StringBuffer(parts[1].substring(parts[1].lastIndexOf(".") + 1));

                for (int i = 1; i < sb.length(); i++) {
                    if (Character.isUpperCase(sb.charAt(i)) && i < sb.length() - 1 && Character.isLowerCase(sb.charAt(i + 1)))
                        sb.insert(i++, '-');
                    else if (sb.charAt(i) == '$')
                        sb.deleteCharAt(i--);
                }

                parts[0] = sb.toString().toLowerCase();
            } else {
                parts[0] = parts[1];
            }
        }

        return parts;
    }

    // BEAN METHODS
    public AbstractSerializationComparator getComparator() {
        return comparator;
    }

    public void setComparator(AbstractSerializationComparator comparator) {
        this.comparator = comparator;
    }
}
