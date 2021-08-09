/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.util;

import ncsa.tools.common.AbstractSerializationComparator;
import ncsa.tools.common.UserFacing;
import ncsa.tools.common.exceptions.DeserializationException;
import ncsa.tools.common.exceptions.ParseException;
import ncsa.tools.common.exceptions.SerializationException;
import ncsa.tools.common.util.bean.ImplicitJBeanDeserializer;
import ncsa.tools.common.util.bean.ImplicitJBeanSerializer;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;

/**
 * A few utility methods for processing or handling xml. Wraps both Bean and
 * Dom4j serialization/deserialization methods as well as Dom4j printing.
 *
 * @author Albert L. Rossi.
 */
public class XmlUtils {
    private static final String CDATA_START = "<![CDATA[";
    private static final String CDATA_END = "]]>";

    /**
     * Static utility class; cannot be constructed.
     */
    private XmlUtils() {
    }

    /**
     * Flattens nested CDATA sections; e.g.:<br>
     * <br>
     * ...<![CDATA[ ... <![CDATA[ ... ]]> ... ]]> ...<br>
     * <br>
     * becomes:<br>
     * <br>
     * ...<![CDATA[ ... ]]><![CDATA[ ... ]]> ...<br>
     */
    public static String splitCDATA(String xml) throws ParseException {
        return new CDataParser(xml).parse();
    }

    private static class CDataParser {
        private String xml;
        private LinkedList cdataStack;
        private StringBuffer buffer;
        private StringBuffer normalized;
        private StringBuffer tag;
        private int len;
        private int end;
        private int index;
        private char token;

        private CDataParser(String xml) {
            this.xml = xml;
            cdataStack = new LinkedList();
            buffer = new StringBuffer(0);
            normalized = new StringBuffer(0);
            tag = new StringBuffer(0);
            len = xml.length();
            end = 0;
            token = 0;
            index = 0;
        }

        private String parse() throws ParseException {
            String result = null;
            try {
                while (index < len) {
                    token = xml.charAt(index);
                    switch (token) {
                        case '<':
                            handleOpenAngleBracket();
                            break;
                        case ']':
                            handleClosedSquareBracket();
                            break;
                        default:
                            buffer.append(token);
                    }
                    index++;
                }
                if (buffer.length() > 0)
                    normalized.append(buffer.toString());
                result = normalized.toString();
            } finally {
                clear();
            }
            return result;
        }

        private void handleOpenAngleBracket() throws ParseException {
            tag.append(token);
            end = index + CDATA_START.length();
            if (end <= len) {
                boolean breakflag = false;
                int i = 1;
                for (; i < CDATA_START.length(); i++) {
                    index++;
                    token = xml.charAt(index);
                    tag.append(token);
                    if (token == '>') {
                        breakflag = true;
                        break;
                    }
                    switch (i) {
                        case 1:
                            if (token != '!')
                                breakflag = true;
                            break;
                        case 2:
                            if (token != '[')
                                breakflag = true;
                            break;
                        case 3:
                            if (token != 'C')
                                breakflag = true;
                            break;
                        case 4:
                            if (token != 'D')
                                breakflag = true;
                            break;
                        case 5:
                            if (token != 'A')
                                breakflag = true;
                            break;
                        case 6:
                            if (token != 'T')
                                breakflag = true;
                            break;
                        case 7:
                            if (token != 'A')
                                breakflag = true;
                            break;
                        case 8:
                            if (token != '[')
                                breakflag = true;
                            break;
                    }
                    if (breakflag)
                        break;
                }
                if (!breakflag) {
                    if (!cdataStack.isEmpty()) {
                        tag.insert(0, CDATA_END);
                    }
                    cdataStack.addFirst(buffer.toString() + tag);
                    buffer.setLength(0);
                } else {
                    buffer.append(tag);
                }
                tag.setLength(0);
            } else {
                if (!cdataStack.isEmpty()) {
                    throw new ParseException("problem at char " + index + "; unmatched CDATA: " + cdataStack);
                }
                normalized.append(buffer.toString()).append(xml.substring(index, len));
                buffer.setLength(0);
                index = len;
            }
        }

        private void handleClosedSquareBracket() throws ParseException {
            tag.append(token);
            end = index + CDATA_END.length();
            if (end <= len) {
                boolean breakflag = false;
                for (int i = 1; i < CDATA_END.length(); i++) {
                    index++;
                    token = xml.charAt(index);
                    tag.append(token);
                    switch (i) {
                        case 1:
                            if (token != ']')
                                breakflag = true;
                            break;
                        case 2:
                            if (token != '>')
                                breakflag = true;
                            break;
                    }
                    if (breakflag)
                        break;
                }
                if (!breakflag) {
                    String head = cdataStack.isEmpty() ? "" : (String) cdataStack.removeFirst();
                    buffer.insert(0, head);
                    if (cdataStack.isEmpty()) {
                        buffer.append(tag);
                        normalized.append(buffer.toString());
                    } else {
                        tag.append(CDATA_START);
                        buffer.insert(0, cdataStack.removeFirst());
                        cdataStack.addFirst(buffer.toString());
                    }
                    buffer.setLength(0);
                } else {
                    buffer.append(tag);
                }
                tag.setLength(0);
            } else {
                if (!cdataStack.isEmpty()) {
                    throw new ParseException("problem at char " + index + "; unmatched CDATA: " + cdataStack);
                }
                normalized.append(buffer.toString()).append(xml.substring(index, len));
                buffer.setLength(0);
                index = len;
            }
        }

        private void clear() {
            cdataStack.clear();
            buffer.setLength(0);
            normalized.setLength(0);
            tag.setLength(0);
            cdataStack = null;
            buffer = null;
            normalized = null;
            tag = null;
        }
    }

    /**
     * This is merely a printing convenience; it does not do full introspection
     * on arbitary objects. Tags collections, maps, arrays and recurs on the
     * their elements; calls asElement.asXML on UserFacing objects; otherwise
     * simply reports the object's type and calls toString to get its value.
     */
    public static String asXML(Object object) throws DocumentException, IOException {
        StringBuffer buffer = new StringBuffer();
        asXML(object, buffer);
        return XmlUtils.prettyPrint(buffer.toString(), "UTF-8");
    }

    public static String prettyPrint(String xml, String format) throws DocumentException, IOException {
        OutputFormat createPrettyPrint = OutputFormat.createPrettyPrint();
        createPrettyPrint.setEncoding(format);
        return prettyPrint(xml, createPrettyPrint);
    }

    /**
     * Calls Dom4j methods.
     *
     * @param xml syntactically valid XML to be printed.
     * @return xml pretty printed using the default pretty print OutputFormat.
     */
    public static String prettyPrint(String xml) throws DocumentException, IOException {
        return prettyPrint(xml, "UTF-16");
    }

    public static void prettyPrint(Element element, Writer w) throws IOException {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-16");

        XMLWriter writer = new XMLWriter(w, format);
        writer.write(element);
        writer.close();
    }

    /**
     * Calls Dom4j methods.
     *
     * @param xml    syntactically valid XML to be printed.
     * @param format to use in printing.
     * @return xml pretty printed using the given OutputFormat.
     */
    public static String prettyPrint(String xml, OutputFormat format) throws DocumentException, IOException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new StringReader(xml));
        StringWriter sw = new StringWriter();
        XMLWriter writer = new XMLWriter(sw, format);
        writer.write(document);
        return sw.toString();
    } // prettyPrint( format )

    public static Element readIn(File f) throws DocumentException {
        SAXReader r = new SAXReader();
        Document d = r.read(f);
        return d.getRootElement();
    }

    /*
     * /////////////////////////////// JAVA.BEANS ////////////////////////////////
     */

    /**
     * Sets up a ByteArrayInputStream, initializes XMLDecoder and calls
     * readObject on it. Type-checks result if type is not null.
     *
     * @param xml  to be deserialized.
     * @param type fully qualified name of bean.
     * @return the deserialized bean.
     */
    public static Object deserializeBean(String xml, String type) throws DeserializationException {
        Object bean = null;
        ByteArrayInputStream sbis = new ByteArrayInputStream(xml.getBytes());
        XMLDecoder decoder = new XMLDecoder(sbis);
        bean = decoder.readObject();
        decoder.close();
        if (bean != null && type != null && !bean.getClass().getName().equals(type))
            throw new DeserializationException("deserialized object class: " + bean.getClass() + " should have been " + type);
        return bean;
    } // deserializeBean

    /**
     * Sets up a FileInputStream, initializes XMLDecoder and calls readObject on
     * it. Type-checks result if type is not null.
     *
     * @param path of xml file to be deserialized.
     * @param type fully qualified name of bean.
     * @return the deserialized bean.
     */
    public static Object deserializeBeanFromFile(String path, String type) throws DeserializationException {
        Object bean = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
        } catch (IOException ioe) {
            throw new DeserializationException("deserializeBeanFromFile", ioe);
        }
        XMLDecoder decoder = new XMLDecoder(fis);
        bean = decoder.readObject();
        decoder.close();
        if (bean != null && type != null && !bean.getClass().getName().equals(type))
            throw new DeserializationException("deserialized object class: " + bean.getClass() + " should have been " + type);
        return bean;
    } // deserializeBeanFromFile

    /**
     * Sets up a ByteArrayOutputStream, initializes XMLEncoder and calls
     * writeObject.
     *
     * @param bean to be serialized.
     * @return the serialized bean.
     */
    public static String serializeBean(Object bean) throws SerializationException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(baos);
        encoder.writeObject(bean);

        try {
            baos.flush();
        } catch (IOException ioe) {
            throw new SerializationException("serializeBean", ioe);
        }
        encoder.close();
        return baos.toString();
    } // serializeBean

    /**
     * Sets up a FileOutputStream, initializes XMLEncoder and calls writeObject.
     *
     * @param bean to be serialized.
     * @param path of file to write serialized object to.
     */
    public static void serializeBeanToFile(Object bean, String path) throws SerializationException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
        } catch (IOException ioe) {
            throw new SerializationException("serializeToFile", ioe);
        }
        XMLEncoder encoder = new XMLEncoder(fos);
        encoder.writeObject(bean);
        try {
            fos.flush();
        } catch (IOException ioe) {
            throw new SerializationException("serializeToFile", ioe);
        }
        encoder.close();
    } // serializeToFile

    /*
     * ///////////////////////////// Dom4J ELEMENT ///////////////////////////////
     */

    public static Element dom4jElementFromString(String xml) throws DocumentException {
        SAXReader reader = new SAXReader();
        StringReader stringReader = new StringReader(xml);
        Document document = reader.read(stringReader);
        return document.getRootElement();
    }

    public static Element dom4jElementFromFile(String xmlPath) throws DocumentException, IOException {
        SAXReader reader = new SAXReader();
        try {
            // first try unicode....
            FileInputStream is = new FileInputStream(xmlPath);
            InputStreamReader isReader = new InputStreamReader(is, "UTF-16");

            Document document = reader.read(isReader);
            return document.getRootElement();

        } catch (DocumentException e) {
            try {
                // first try unicode....
                FileInputStream is = new FileInputStream(xmlPath);
                InputStreamReader isReader = new InputStreamReader(is, "UTF-8");

                Document document = reader.read(isReader);
                return document.getRootElement();

            } catch (DocumentException e1) {

                // if it's ANSI instead of unicode, we'll get a document exception,
                // so let's try again.
                FileReader fileReader = new FileReader(xmlPath);
                Document document = reader.read(fileReader);
                return document.getRootElement();

            }
        }
    }

    public static Element dom4jElementFromFile(File file) throws DocumentException, IOException {
        return dom4jElementFromFile(file.getAbsolutePath());
    }

    public static Element dom4jElementFromStream(InputStream stream) throws DocumentException {
        SAXReader reader = new SAXReader();
        InputSource is = new InputSource(stream);
        Document document = reader.read(is);
        return document.getRootElement();
    }

    public static void dom4jElementToFile(Element element, String path) throws DocumentException, IOException {
        String xml = prettyPrint(element.asXML());
        PrintWriter fw = new PrintWriter(getUnicodeFileWriter(path, false));
        fw.write(xml);
        fw.flush();
    }

    /*
     * /////////////////////////////// IMPLICIT //////////////////////////////////
     */

    public static String serializeImplicitBean(Object bean, AbstractSerializationComparator comparator) throws SerializationException {
        ImplicitJBeanSerializer serializer = new ImplicitJBeanSerializer();
        if (comparator != null)
            serializer.setComparator(comparator);
        serializer.initialize();
        return serializer.getBeanAsXml(bean);
    }

    public static void serializeImplicitBeanToFile(Object bean, String path, AbstractSerializationComparator comparator)
        throws SerializationException {
        serializeImplicitBeanToFile(bean, path, "UTF-16", comparator);
    }

    public static void serializeImplicitBeanToFile(Object bean, String path, String format, AbstractSerializationComparator comparator)
        throws SerializationException {
        String xml;
        try {
            xml = prettyPrint(serializeImplicitBean(bean, comparator), format);
            PrintWriter fw = new PrintWriter(getUnicodeFileWriter(path, false));
            fw.write(xml);
            fw.flush();
        } catch (Throwable t) {
            throw new SerializationException("could not serialize " + bean + " to " + path, t);
        }
    }

    public static Object deserializeImplicitBean(String xml) throws DeserializationException {
        ImplicitJBeanDeserializer deserializer = new ImplicitJBeanDeserializer();
        try {
            deserializer.initialize();
            Element element = dom4jElementFromString(xml);
            return deserializer.instantiate(element, null);
        } catch (Throwable t) {
            throw new DeserializationException("could not deserialize " + xml, t);
        }
    } // deserializeImplicitBean

    public static Object deserializeImplicitBeanFromFile(String xmlPath) throws DeserializationException {
        ImplicitJBeanDeserializer deserializer = new ImplicitJBeanDeserializer();
        try {
            deserializer.initialize();
            Element element = dom4jElementFromFile(xmlPath);
            return deserializer.instantiate(element, null);
        } catch (Throwable t) {
            throw new DeserializationException("could not deserialize from " + xmlPath, t);
        }
    }

    /*
     * ///////////////////////////// USER FACING /////////////////////////////////
     */

    public static String serializeUserFacingBean(UserFacing bean) {
        Element e = bean.asElement();
        return e == null ? null : e.asXML();
    }

    public static void serializeUserFacingBeanToFile(UserFacing bean, String path) throws SerializationException {
        serializeUserFacingBeanToFile(bean, "UTF-16", path);
    }

    public static void serializeUserFacingBeanToFile(UserFacing bean, String format, String path) throws SerializationException {

        try {
            String xml = prettyPrint(serializeUserFacingBean(bean), format);
            PrintWriter fw = new PrintWriter(getUnicodeFileWriter(path, false));
            fw.write(xml);
            fw.flush();
        } catch (Throwable t) {
            throw new SerializationException("could not serialize " + bean + " to " + path, t);
        }
    }

    public static void deserializeUserFacingBean(String xml, UserFacing instance) throws DeserializationException {
        try {
            Element root = dom4jElementFromString(xml);
            instance.initializeFromElement(root);
        } catch (Throwable t) {
            throw new DeserializationException("could not deserialize " + xml, t);
        }
    }

    public static void deserializeUserFacingBeanFromFile(String xmlPath, UserFacing instance) throws DeserializationException {
        try {
            Element root = dom4jElementFromFile(xmlPath);
            instance.initializeFromElement(root);
        } catch (Throwable t) {
            throw new DeserializationException("could not deserialize from file " + xmlPath, t);
        }
    }

    public static void deserializeUserFacingBeanFromFile(URL entry, UserFacing instance) throws DeserializationException {
        try {
            Element root = dom4jElementFromStream(entry.openStream());
            instance.initializeFromElement(root);
        } catch (Throwable t) {
            throw new DeserializationException("could not deserialize from file " + entry, t);
        }
    }

    public static void deserializeUserFacingBeanFromFile(File file, UserFacing instance) throws DeserializationException {
        try {
            Element root = dom4jElementFromFile(file);
            instance.initializeFromElement(root);
        } catch (Throwable t) {
            throw new DeserializationException("could not deserialize from file " + file.getAbsolutePath(), t);
        }
    }

    public static void addArrayOfUserFacing(Element parent, String subGroupTag, UserFacing[] uf) {
        if (uf != null) {
            Element e = subGroupTag == null ? parent : parent.addElement(subGroupTag);
            for (int i = 0; i < uf.length; i++) {
                e.add(uf[i].asElement());
            }
        }
    }

    public static UserFacing[] getInitializedArrayOfUserFacing(Element parent, String elementTag, Class clzz)
        throws InstantiationException, IllegalAccessException {
        if (!UserFacing.class.isAssignableFrom(clzz))
            throw new InstantiationException(clzz + " is not UserFacing");
        UserFacing[] uf = null;
        if (parent != null) {
            List elements = parent.elements(elementTag);
            if (elements != null) {
                uf = new UserFacing[elements.size()];
                for (int i = 0; i < uf.length; i++) {
                    Element sub = (Element) elements.get(i);
                    if (sub != null) {
                        uf[i] = (UserFacing) clzz.newInstance();
                        uf[i].initializeFromElement(sub);
                    }
                }
            }
        }
        return uf;
    }

    // AUXILIARIES

    private static void asXML(Object o, StringBuffer buffer) {
        if (o == null)
            o = "null";
        if (o.getClass().isArray()) {
            buffer.append("<array>");
            int len = Array.getLength(o);
            for (int i = 0; i < len; i++)
                asXML(Array.get(o, i), buffer);
            buffer.append("</array>");
        } else if (o instanceof Collection) {
            buffer.append("<collection ");
            buffer.append("type=\"");
            buffer.append(o.getClass());
            buffer.append("\">");
            for (Iterator it = ((Collection) o).iterator(); it.hasNext(); )
                asXML(it.next(), buffer);
            buffer.append("</collection>");
        } else if (o instanceof Map) {
            buffer.append("<map ");
            buffer.append("type=\"");
            buffer.append(o.getClass());
            buffer.append("\">");
            for (Iterator it = ((Map) o).entrySet().iterator(); it.hasNext(); )
                asXML(it.next(), buffer);
            buffer.append("</map>");
        } else if (o instanceof Map.Entry) {
            Map.Entry entry = (Map.Entry) o;
            buffer.append("<entry key=\"");
            buffer.append(entry.getKey());
            buffer.append("\">");
            asXML(entry.getValue(), buffer);
            buffer.append("</entry>");
        } else if (o instanceof UserFacing) {
            buffer.append(((UserFacing) o).asElement().asXML());
        } else {
            buffer.append("<element type=\"");
            buffer.append(o.getClass());
            buffer.append("\">");
            buffer.append(o);
            buffer.append("</element>");
        }
    }

    // //HELPER////

    private static OutputStreamWriter getUnicodeFileWriter(String filename, boolean append) throws FileNotFoundException, IOException {
        FileOutputStream os = new FileOutputStream(filename, false);
        OutputStreamWriter writer;
        try {
            writer = new OutputStreamWriter(os, "Unicode");
        } catch (UnsupportedEncodingException e) {
            writer = new OutputStreamWriter(os);
        }
        return writer;
    }
}
