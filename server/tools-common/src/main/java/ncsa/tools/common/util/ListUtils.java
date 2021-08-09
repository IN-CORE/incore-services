/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import ncsa.tools.common.exceptions.DeserializationException;
import ncsa.tools.common.exceptions.ReflectionException;

/**
 * Methods for manipulating java.util.List objects.
 *
 * @author Albert L. Rossi
 */
public class ListUtils {
    /**
     * Static utility class; cannot be constructed.
     */
    private ListUtils() {
    }

    /**
     * Converts list of to comma-delimited string.
     */
    public static String toString(List list) {
        if (list == null)
            return null;
        if (list.isEmpty())
            return "";
        StringBuffer sb = new StringBuffer();
        Iterator i = list.iterator();
        if (i.hasNext())
            sb.append(i.next());
        while (i.hasNext())
            sb.append(",").append(i.next());
        return sb.toString();
    }

    /**
     * Converts array to comma-delimited string.
     */
    public static String toString(Object[] array) {
        if (array == null)
            return null;
        if (array.length == 0)
            return "";
        StringBuffer sb = new StringBuffer();
        sb.append(array[0]);
        for (int i = 1; i < array.length; i++)
            sb.append(",").append(array[i]);
        return sb.toString();
    }

    /**
     * Converts a list of name=value Strings into a Properties object.
     *
     * @param l list of name=value Strings.
     * @return corresponding properties.
     */
    public static Properties listToProperties(List l) {
        Properties p = new Properties();
        for (ListIterator it = l.listIterator(); it.hasNext(); ) {
            String name = null;
            String value = null;
            StringTokenizer st = new StringTokenizer((String) it.next(), "=");
            if (st.hasMoreTokens())
                name = st.nextToken();
            if (st.hasMoreTokens())
                value = st.nextToken();
            if (name != null && !name.equals("") && value != null)
                p.put(name.trim(), value.trim());
        }
        return p;
    } // listToProperties

    /**
     * Duplicate (for non-string objects) is here
     * construed as referential identity, not semantic equivalence.
     * Order of elements is preserved, so it is the first element
     * of any set of duplicates which is retained.
     *
     * @param l list from which to eliminate duplicates.
     * @return new list with duplicates eliminated.
     */
    public static List eliminateDuplicates(List l) {
        if (l == null)
            return null;
        Map map = new HashMap();
        List list = new ArrayList();
        for (ListIterator lit = l.listIterator(); lit.hasNext(); ) {
            Object next = lit.next();
            if (map.containsKey(next))
                continue;
            list.add(next);
            map.put(next, null);
        }
        return list;
    } // eliminateDuplicates

    /**
     * Checks lists for equivalence.
     *
     * @param l1               list to compare to.
     * @param l2               list to compare.
     * @param ignoreDuplicates if true, calls eliminateDuplicates first.
     * @param ordered          if true, the lists are considered to be ordered.
     * @return new list with duplicates eliminated.
     * @see #eliminateDuplicates
     */
    public static boolean equals(List l1, List l2, boolean ignoreDuplicates, boolean ordered) {
        List a = l1;
        List b = l2;
        if (a == null) {
            if (b != null)
                return false;
        } else {
            if (b == null)
                return false;
            if (ignoreDuplicates) {
                a = eliminateDuplicates(l1);
                b = eliminateDuplicates(l2);
            }
            if (a.size() != b.size())
                return false;
            if (ordered) {
                for (int i = 0; i < a.size(); i++)
                    if (!a.get(i).equals(b.get(i)))
                        return false;
            } else {
                Map map = new HashMap();
                for (int i = 0; i < a.size(); i++)
                    map.put(a.get(i), null);
                for (int i = 0; i < b.size(); i++)
                    if (!map.containsKey(b.get(i)))
                        return false;
            }
        }
        return true;
    } // equals( l1, l2 )

    /**
     * @param list to be sorted.
     * @param c    comparator to use.
     * @return new sorted list.
     */
    public static List sort(List list, Comparator c) {
        if (list == null)
            return null;
        Object[] array = list.toArray();
        if (c == null)
            Arrays.sort(array);
        else
            Arrays.sort(array, c);
        return new ArrayList(Arrays.asList(array));
    } // sort

    /**
     * Does a union of the two lists. Order is preserved, but
     * duplicates are eliminated.
     *
     * @param l1 first list in union.
     * @param l2 second list in union.
     * @return new list which is the union of the two.
     */
    public static List union(List l1, List l2) {
        List union = null;
        if (l2 == null)
            union = l1;
        else if (l1 == null)
            union = l2;
        else {
            union = new ArrayList();
            Map map = new HashMap();
            Object next = null;
            for (int i = 0; i < l1.size(); i++) {
                next = l1.get(i);
                if (map.containsKey(next))
                    continue;
                union.add(next);
                map.put(next, null);
            }
            for (int i = 0; i < l2.size(); i++) {
                next = l2.get(i);
                if (!map.containsKey(next))
                    union.add(next);
            }
        }
        return union;
    } // union

    /**
     * Does an intersection of the two lists. The order of the first list
     * is preserved, but duplicates are eliminated.
     *
     * @param l1 first list in intersection.
     * @param l2 second list in intersection.
     * @return new list which is the intersection of the two.
     */
    public static List intersection(List l1, List l2) {
        List intersection = null;
        if (l1 == null || l2 == null)
            intersection = null;
        else {
            intersection = new ArrayList();
            Map m1 = new HashMap();
            Map m2 = new HashMap();
            Object next = null;
            for (int i = 0; i < l2.size(); i++)
                m2.put(l2.get(i), null);

            for (int i = 0; i < l1.size(); i++) {
                next = l1.get(i);
                if (m2.containsKey(next)) {
                    if (!m1.containsKey(next))
                        intersection.add(next);
                    m1.put(next, null);
                }
            }
        }
        return intersection;
    } // union

    /**
     * Does a difference of the two lists. The order of the first list
     * is preserved, but duplicates are eliminated.
     *
     * @param l1 base list.
     * @param l2 list whose elements should not be found in result.
     * @return new list which is the difference l1 - l2.
     */
    public static List difference(List l1, List l2) {
        List difference = null;
        if (l1 == null || l2 == null)
            difference = null;
        else {
            difference = new ArrayList();
            Map m1 = new HashMap();
            Map m2 = new HashMap();
            Object next = null;
            for (int i = 0; i < l2.size(); i++)
                m2.put(l2.get(i), null);

            for (int i = 0; i < l1.size(); i++) {
                next = l1.get(i);
                if (!m2.containsKey(next)) {
                    if (!m1.containsKey(next))
                        difference.add(next);
                    m1.put(next, null);
                }
            }
        }
        return difference;
    } // union

    /*
     * ////////////////////////////////////////////////////////////////////////
     * COLLECTION-ARRAY CONVERSION //
     * /////////////////////////////////////////////////////////////////////
     */

    /**
     * Converts all internal arrays into Lists.
     * Recurs on Collection or Map subparts.
     */
    public static List toList(Object[] array) {
        if (array == null)
            return null;
        List list = new ArrayList(Arrays.asList(array));
        for (int i = 0; i < list.size(); i++) {
            list.set(i, toList(list.get(i)));
        }
        return list;
    }

    private static Object toList(Object element) {
        if (element != null) {
            if (element.getClass().isArray()) {
                element = toList((Object[]) element);
            } else if (element instanceof Collection) {
                element = toList(((Collection) element).toArray());
            } else if (element instanceof Map) {
                Map m = (Map) element;
                for (Iterator it = m.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry entry = (Map.Entry) it.next();
                    entry.setValue(toList(entry.getValue()));
                }
            }
        }
        return element;
    }

    /**
     * Converts all internal collections into arrays.
     * Recurs on array members.
     */
    public static Object[] toArray(Collection collection) {
        if (collection == null)
            return null;
        Object[] array = collection.toArray();
        for (int i = 0; i < array.length; i++) {
            array[i] = toArray(array[i]);
        }
        return array;
    }

    private static Object toArray(Object member) {
        if (member != null) {
            if (member.getClass().isArray()) {
                List l = Arrays.asList((Object[]) member);
                member = toArray(l);
            } else if (member instanceof Collection) {
                member = toArray((List) member);
            } else if (member instanceof Map) {
                Map m = (Map) member;
                for (Iterator it = m.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry entry = (Map.Entry) it.next();
                    entry.setValue(toArray(entry.getValue()));
                }
            }
        }
        return member;
    }

    /*
     * ////////////////////////////////////////////////////////////////////////
     * SIMPLE SERIALIZATION & DESERIALIZATION //
     * /////////////////////////////////////////////////////////////////////
     */

    /**
     * Recurs on all subparts.
     * <p>
     * All string parameters can have no more than one non-whitespace char;
     * only sep can be entirely whitespace.
     *
     * @param o       to be serialized.
     * @param c_start denotes beginning of collection.
     * @param c_end   denotes end of collection.
     * @param m_start denotes beginning of map.
     * @param m_end   denotes end of map.
     * @param asg     denotes assignment in map.
     * @param sep     to use to separate elements.
     */
    public static String listSerialization(Object o, String c_start, String c_end, String m_start, String m_end, String asg, String sep)
        throws ClassNotFoundException {
        TypePattern tp = getPatternInstance(c_start, c_end, m_start, m_end, asg, sep, null);
        return listSerialization(o, tp);
    }

    private static String listSerialization(Object o, TypePattern tp) throws IllegalArgumentException {
        StringBuffer buffer = new StringBuffer(0);
        if (o instanceof Collection) {
            Collection c = (Collection) o;
            Iterator i = c.iterator();
            buffer.append(tp.cStartDelim);
            if (i.hasNext())
                buffer.append(listSerialization(i.next(), tp));
            while (i.hasNext()) {
                buffer.append(tp.sep).append(listSerialization(i.next(), tp));
            }
            buffer.append(tp.cEndDelim);
        } else if (o != null && o.getClass().isArray()) {
            Object[] array = (Object[]) o;
            buffer.append(tp.cStartDelim);
            if (array.length > 0)
                buffer.append(listSerialization(array[0], tp));
            for (int i = 1; i < array.length; i++) {
                buffer.append(tp.sep).append(listSerialization(array[i], tp));
            }
            buffer.append(tp.cEndDelim);
        } else if (o instanceof Map) {
            Map m = (Map) o;
            Iterator i = m.entrySet().iterator();
            buffer.append(tp.mStartDelim);
            if (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
                buffer.append(e.getKey()).append(tp.asg).append(listSerialization(e.getValue(), tp));
            }
            while (i.hasNext()) {
                Map.Entry e = (Map.Entry) i.next();
                buffer.append(tp.sep).append(e.getKey()).append(tp.asg).append(listSerialization(e.getValue(), tp));
            }
            buffer.append(tp.mEndDelim);
        } else if ("".equals(o)) {
            buffer.append(tp.sep);
        } else {
            buffer.append(o);
        }
        return buffer.toString();
    }

    /**
     * Recurs on all subparts.
     *
     * @param o       to be deserialized.
     * @param c_start denotes beginning of collection.
     * @param c_end   denotes end of collection.
     * @param m_start denotes beginning of map.
     * @param m_end   denotes end of map.
     * @param asg     denotes assignment in map.
     * @param sep     to use to separate elements.
     * @param pattern list of TypePattern specifications.
     */
    public static Object listDeserialization(String o, String c_start, String c_end, String m_start, String m_end, String asg, String sep,
                                             List pattern) throws DeserializationException {
        try {
            TypePattern tp = getPatternInstance(c_start, c_end, m_start, m_end, asg, sep, pattern);
            return patternedParse(o, 0, tp);
        } catch (Throwable t) {
            throw new DeserializationException("listDeserialization", t);
        }
    }

    private static TypePattern getPatternInstance(String c_start, String c_end, String m_start, String m_end, String asg, String sep,
                                                  List pattern) throws ClassNotFoundException {
        TypePattern tp = new TypePattern();
        if (c_start.trim().length() != 1)
            throw new IllegalArgumentException("cannot use " + sep + " as separator; must have a single non-whitespace character");
        tp.cStartDelim = c_start.trim().charAt(0);
        if (c_end.trim().length() != 1)
            throw new IllegalArgumentException("cannot use " + sep + " as separator; must have a single non-whitespace character");
        tp.cEndDelim = c_end.trim().charAt(0);
        if (m_start.trim().length() != 1)
            throw new IllegalArgumentException("cannot use " + sep + " as separator; must have a single non-whitespace character");
        tp.mStartDelim = m_start.trim().charAt(0);
        if (m_end.trim().length() != 1)
            throw new IllegalArgumentException("cannot use " + sep + " as separator; must have a single non-whitespace character");
        tp.mEndDelim = m_end.trim().charAt(0);
        if (asg.trim().length() != 1)
            throw new IllegalArgumentException("cannot use " + sep + " as separator; must have a single non-whitespace character");
        tp.asg = asg.trim().charAt(0);
        if (sep.trim().length() > 1)
            throw new IllegalArgumentException("cannot use " + sep + " as separator; must have only one non-whitespace character");
        tp.sep = sep;
        if (pattern != null) {
            for (int i = 0; i < pattern.size(); i++)
                tp.addPatternType(pattern.get(i));
        }
        return tp;
    }

    private static Object patternedParse(String o, int index, TypePattern tp) throws ReflectionException {
        if (o.charAt(index) == tp.cStartDelim) {
            List l = new ArrayList();
            handleList(l, o, ++index, tp);
            return l;
        } else if (o.charAt(index) == tp.mStartDelim) {
            Map m = new HashMap();
            handleMap(m, o, ++index, tp);
            return m;
        }
        return convert(o, tp);
    }

    private static int handleList(List list, String o, int index, TypePattern tp) throws ReflectionException {
        StringBuffer value = new StringBuffer(0);
        boolean popped = false;
        while (index < o.length()) {
            char c = o.charAt(index);
            if (c == tp.cStartDelim) {
                List l = new ArrayList();
                index = handleList(l, o, ++index, tp);
                list.add(l);
                popped = true;
            } else if (c == tp.mStartDelim) {
                Map m = new HashMap();
                index = handleMap(m, o, ++index, tp);
                list.add(m);
                popped = true;
            } else if (c == tp.cEndDelim) {
                if (!popped && value.length() > 0)
                    list.add(convert(value.toString(), tp));
                index++;
                break;
            } else if (o.substring(index).startsWith(tp.sep)) {
                if (!popped)
                    list.add(convert(value.toString(), tp));
                value.setLength(0);
                index = index + tp.sep.length();
            } else {
                value.append(c);
                popped = false;
                index++;
            }
        }
        return index;
    }

    private static int handleMap(Map map, String o, int index, TypePattern tp) throws ReflectionException {
        StringBuffer key = new StringBuffer(0);
        StringBuffer value = new StringBuffer(0);
        boolean keyChar = true;
        while (index < o.length()) {
            char c = o.charAt(index);
            if (c == tp.mStartDelim) {
                Map m = new HashMap();
                index = handleMap(m, o, ++index, tp);
                map.put(key.toString(), m);
                key.setLength(0);
                keyChar = true;
            } else if (c == tp.cStartDelim) {
                List l = new ArrayList();
                index = handleList(l, o, ++index, tp);
                map.put(key.toString(), l);
                key.setLength(0);
                keyChar = true;
            } else if (c == tp.mEndDelim) {
                if (!keyChar && key.length() > 0)
                    map.put(key.toString(), convert(value.toString(), tp));
                index++;
                break;
            } else if (o.substring(index).startsWith(tp.sep)) {
                map.put(key.toString(), convert(value.toString(), tp));
                index = index + tp.sep.length();
                key.setLength(0);
                value.setLength(0);
                keyChar = true;
            } else if (c == tp.asg) {
                keyChar = false;
                index++;
            } else {
                if (keyChar)
                    key.append(c);
                else
                    value.append(c);
                index++;
            }
        }
        return index;
    }

    /**
     * Auxiliary which applies the current pattern to the given token
     * and converts it into the given type. The type pattern's marker
     * is automatically advanced by the get call to the next internal element.
     *
     * @param token   to convert.
     * @param pattern encapsulating the type sequence to use as "template".
     * @return object of new type.
     * @throws ReflectionException
     */
    private static Object convert(String token, TypePattern pattern) throws ReflectionException {
        Class clzz = pattern.getNext();
        if ("null".equals(token)) {
            return null;
        } else if (clzz.equals(String.class)) {
            return token;
        } else if (clzz.equals(Character.class)) {
            return new Character(token.charAt(0));
        } else {
            return ReflectUtils.callConstructor(clzz, new Class[]{String.class}, new Object[]{token});
        }
    }

    // ///////////////////////// INNER CLASS /////////////////////////////////

    /**
     * Datastructure for matching and converting strings in
     * the serialized List into their appropriate types.
     */
    private static class TypePattern {
        private List list = new ArrayList();
        private int marker = 0;
        private char cStartDelim;
        private char cEndDelim;
        private char mStartDelim;
        private char mEndDelim;
        private char asg;
        private String sep;

        /**
         * Appends to the current pattern.
         *
         * @param o instance object, string name or Class from which
         *          to derive the type.
         * @throws ClassNotFoundException
         */
        public void addPatternType(Object o) throws ClassNotFoundException {
            if (o instanceof String)
                list.add(TypeUtils.getClassForName((String) o));
            else if (o instanceof Class)
                list.add(o);
            else if (o == null)
                list.add(o);
            else
                list.add(o.getClass());
        }

        /**
         * Replaces element in current pattern.
         *
         * @param o     instance object, string name or Class from which
         *              to derive the type.
         * @param index of type in pattern to be substituted.
         * @throws ClassNotFoundException
         */
        public void setPatternType(Object o, int index) throws ClassNotFoundException {
            if (o instanceof String)
                list.set(index, TypeUtils.getClassForName((String) o));
            else if (o instanceof Class)
                list.set(index, o);
            else if (o == null)
                list.set(index, o);
            else
                list.set(index, o.getClass());
        }

        /**
         * Returns the next type object and advances the internal
         * marker (modulo pattern size).
         *
         * @return next type.
         */
        public Class getNext() {
            if (list.size() == 0)
                return String.class;
            Class c = (Class) list.get(marker);
            marker = (marker + 1) % list.size();
            return c;
        }

        /**
         * @return length of pattern.
         */
        public int length() {
            return list.size();
        }

        /**
         * Restores the marker to the beginning of the pattern.
         */
        public void clearMarker() {
            marker = 0;
        }
    }
}
