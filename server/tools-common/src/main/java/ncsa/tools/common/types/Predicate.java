/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.types;

import java.io.File;

import ncsa.tools.common.NCSAConstants;
import ncsa.tools.common.exceptions.FailedComparisonException;
import ncsa.tools.common.util.ComparisonUtils;
import ncsa.tools.common.util.TypeUtils;

/**
 * Combines a comparator with an object to use in the comparison.
 * <p>
 * Comparisons to arbitrary subjects follow these semantics:
 * <p>
 * <table>
 * <tr>
 * <td>OPERATOR</td>
 * <td>PRIMITIVE</td>
 * <td>COMPARABLE</td>
 * <td>FILE</td>
 * <td>OTHER</td>
 * </tr>
 * <tr>
 * <td>==</td>
 * <td>numerical equivalence</td>
 * <td>referential equivalence</td>
 * <td>idem</td>
 * <td>idem</td>
 * </tr>
 * <tr>
 * <td>equals</td>
 * <td>numerical equivalence</td>
 * <td>value of the "equals" method</td>
 * <td>four options: a. compare file pathname; b. compare file timestamp; c. compare file length; d. compare stringified contents</td>
 * <td>value of the "equals" method</td>
 * </tr>
 * <tr>
 * <td>&lt;, &gt;, &lt;=, &gt;=</td>
 * <td>numerical ordering</td>
 * <td>result of "compareTo"</td>
 * <td>same 4 options as for "equals"</td>
 * <td>operator applied to stringified representation( o.toString() )</td>
 * </tr>
 * </table>
 * <p>
 * Constraints: A File can be compared to a String or a Long (see above); in all other cases, the two arguments must type-match. If
 * "cast" is set to null (default), type matching is enforced; else a (not so efficient, but) safe cast to the indicated Primitive
 * or String class is attempted on the two objects at runtime.
 *
 * @author Albert L. Rossi
 */
public class Predicate {
    private Object object = null;
    private int comparator = NCSAConstants.EQUALS;
    private int fileComparison = NCSAConstants.PATH;
    private Class cast = null;
    private String sysProp = null;

    public Predicate() {
    }

    public Predicate(Object object, String comparator) {
        this(object, comparator, null, null);
    }

    public Predicate(Object object, String comparator, String cast) {
        this(object, comparator, cast, null);
    }

    public Predicate(Object object, String comparator, String cast, String fileMode) {
        this.object = object;
        if (fileMode != null)
            setFileComparison(fileMode);
        if (comparator != null)
            setComparator(comparator);
        if (cast != null) {
            try {
                setCast(cast);
            } catch (Throwable t) { /* leave it null */
            }
        }
    }

    /**
     * @param s Primitive Wrapper or String class to which the comparands should
     *          be cast at runtime.
     * @throws ClassNotFoundException
     */
    public Predicate setCast(String s) throws ClassCastException, ClassNotFoundException {
        if (!TypeUtils.isPrimVal(s))
            throw new ClassCastException("cannot cast to type " + s + "; type must be String or Primitive");

        cast = TypeUtils.getClassForName(s);

        if (cast.equals(File.class))
            throw new ClassCastException("cannot cast to " + File.class);
        return this;
    } // setCast

    /**
     * @param s options: (1) "=", "==", "eq"; (2) "!=", "<>", "ne"; (3)
     *          "equals"; (4) "nequals"; (5) ">", "gt", "greaterthan"; (6) "<",
     *          "lt", "lessthan"; (7) ">=", "ge", "greaterthanorequalto"; (8) "<=",
     *          "le", "lessthanorequalto"; all of these are case insensitive.
     */
    public Predicate setComparator(String s) throws IllegalArgumentException {
        comparator = ComparisonUtils.getComparatorValue(s);
        return this;
    }

    /**
     * @param i options: one of the OgreConstants EQ, EQUALS, NE, NEQUALS, GT,
     *          GE, LT, or LE.
     */
    public Predicate setComparatorValue(int i) {
        comparator = i;
        return this;
    }

    /**
     * Sets the comparator to the complement of the string value given.
     *
     * @param s options: (1) "=", "==", "eq"; (2) "!=", "<>", "ne"; (3)
     *          "equals"; (4) "nequals"; (5) ">", "gt", "greaterthan"; (6) "<",
     *          "lt", "lessthan"; (7) ">=", "ge", "greaterthanorequalto"; (8) "<=",
     *          "le", "lessthanorequalto"; all of these are case insensitive.
     */
    public Predicate setComparatorComplement(String s) throws IllegalArgumentException {
        setComparatorComplementValue(ComparisonUtils.getComparatorValue(s));
        return this;
    } // setComparatorComplement( string )

    /**
     * @param i options: one of the OgreConstants EQ, EQUALS, NE, NEQUALS, GT,
     *          GE, LT, or LE.
     */
    public Predicate setComparatorComplementValue(int i) {
        switch (i) {
            case NCSAConstants.EQ:
                comparator = NCSAConstants.NE;
                break;
            case NCSAConstants.EQUALS:
                comparator = NCSAConstants.NEQUALS;
                break;
            case NCSAConstants.NE:
                comparator = NCSAConstants.EQ;
                break;
            case NCSAConstants.NEQUALS:
                comparator = NCSAConstants.EQUALS;
                break;
            case NCSAConstants.GT:
                comparator = NCSAConstants.LE;
                break;
            case NCSAConstants.GE:
                comparator = NCSAConstants.LT;
                break;
            case NCSAConstants.LT:
                comparator = NCSAConstants.GE;
                break;
            case NCSAConstants.LE:
                comparator = NCSAConstants.GT;
                break;
            case NCSAConstants.MATCHES:
                comparator = NCSAConstants.NMATCHES;
                break;
            case NCSAConstants.NMATCHES:
                comparator = NCSAConstants.MATCHES;
                break;
            default:
                comparator = NCSAConstants.NEQUALS;
        }
        return this;
    } // setComparatorComplement( int )

    /**
     * Options for mode in which to compare File references.
     *
     * @param s options = (1) "length", "len", "l" [compares file lengths]; (2)
     *          "timestamp", "time", "t", "lastmodified" [compares file
     *          timestamps]; (3) "path", "p", "default" [compares pathnames];
     *          (4) "content", "contents", "c" [stringifies file contents and
     *          compares the strings]; all options are case-insensitive.
     */
    public Predicate setFileComparison(String s) throws IllegalArgumentException {
        if (s.equalsIgnoreCase("length") || s.equalsIgnoreCase("len") || s.equalsIgnoreCase("l"))
            fileComparison = NCSAConstants.LENGTH;
        else if (s.equalsIgnoreCase("timestamp") || s.equalsIgnoreCase("time") || s.equalsIgnoreCase("t")
            || s.equalsIgnoreCase("lastmodified"))
            comparator = NCSAConstants.TIMESTAMP;
        else if (s.equalsIgnoreCase("path") || s.equalsIgnoreCase("p") || s.equalsIgnoreCase("default"))
            comparator = NCSAConstants.PATH;
        else if (s.equalsIgnoreCase("content") || s.equalsIgnoreCase("contents") || s.equalsIgnoreCase("c"))
            comparator = NCSAConstants.CONTENTS;
        else
            throw new IllegalArgumentException(s + " is not a recognized comparison type");
        return this;
    } // setFileComparison

    /**
     * @param i options: one of the NCSAConstants LENGTH, TIMESTAMP, PATH or
     *          CONTENTS.
     */
    public Predicate setFileComparisonValue(int i) {
        fileComparison = i;
        return this;
    }

    /**
     * @param b boolean value to use in comparison.
     */
    public Predicate setBoolean(boolean b) {
        object = new Boolean(b);
        return this;
    }

    /**
     * @param b byte value to use in comparison.
     */
    public Predicate setByte(byte b) {
        object = new Byte(b);
        return this;
    }

    /**
     * @param c char value to use in comparison.
     */
    public Predicate setChar(char c) {
        object = new Character(c);
        return this;
    }

    /**
     * @param d double value to use in comparison.
     */
    public Predicate setDouble(double d) {
        object = new Double(d);
        return this;
    }

    /**
     * @param f float value to use in comparison.
     */
    public Predicate setFloat(float f) {
        object = new Float(f);
        return this;
    }

    /**
     * @param i int value to use in comparison.
     */
    public Predicate setInt(int i) {
        object = new Integer(i);
        return this;
    }

    /**
     * @param l long value to use in comparison.
     */
    public Predicate setLong(long l) {
        object = new Long(l);
        return this;
    }

    /**
     * @param s short value to use in comparison.
     */
    public Predicate setShort(short s) {
        object = new Short(s);
        return this;
    }

    /**
     * @param s String value to use in comparison.
     */
    public Predicate setString(String s) {
        object = s;
        return this;
    }

    /**
     * @param s name of System property whose value should be used in
     *          comparison.
     */
    public Predicate setSysProp(String s) {
        sysProp = s;
        return this;
    }

    /**
     * @param o object to use in comparison.
     */
    public Predicate setObject(Object o) {
        object = o;
        return this;
    }

    /**
     * @return the object used in the comparison.
     */
    public Object getObject() {
        return object;
    }

    /**
     * @return a (normalized) string representation of the comparator.
     */
    public String getComparator() {
        return ComparisonUtils.getComparator(comparator);
    }

    /**
     * @return actual comparator constant value.
     */
    public int getComparatorValue() {
        return comparator;
    }

    /**
     * @return string representation of the predicate as Comparator + type +
     * Object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getComparator());
        if (object != null) {
            Class c = object.getClass();
            Class p = TypeUtils.getTypeForClass(c);
            if (p != null)
                sb.append(p.getName());
            else
                sb.append(c.getName());
            String s = object.toString();
            if (p == null && !c.equals(String.class)) {
                String n = c.getName();
                int i = s.indexOf(n);
                if (i >= 0)
                    sb.append(s.substring(i + n.length()));
                else
                    sb.append(s);
            } else {
                sb.append(s);
            }
        } else {
            sb.append(object);
        }
        return sb.toString();
    } // toString

    /**
     * @param subject of the predicate.
     * @return whether the predicate is valid (true) on the subject.
     */
    public boolean isValid(Object subject) throws FailedComparisonException {
        return isValid(subject, false);
    }

    /**
     * @param subject      of the predicate.
     * @param convertTypes whether it should attempt to flexibly convert types to match
     * @return whether the predicate is valid (true) on the subject.
     */
    public boolean isValid(Object subject, boolean convertTypes) throws FailedComparisonException {
        // if sysProps are set, set args to them
        if (sysProp != null)
            object = System.getProperty(sysProp);

        // always check for file comparison first
        if (subject instanceof File || object instanceof File)
            return ComparisonUtils.compare(subject, object, fileComparison, comparator);

        // if they've requested to automatically convert types, try to match a double, int, or string
        if (convertTypes) {
            if ((object instanceof Double) && (!(subject instanceof Double))) {
                try {
                    subject = new Double(subject.toString());
                } catch (NumberFormatException e) {
                    // can't convert to a double....
                }
            }
            if ((object instanceof Integer) && (!(subject instanceof Integer))) {
                try {
                    subject = new Integer(subject.toString());
                } catch (NumberFormatException e) {
                    // can't convert to a double....
                }
            }
            if ((object instanceof String) && (!(subject instanceof String))) {
                subject = subject.toString();
            }
        }

        return ComparisonUtils.compare(subject, object, comparator, cast);
    } // isValid
}
