/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import ncsa.tools.common.NCSAConstants;
import ncsa.tools.common.exceptions.FileReadException;
import ncsa.tools.common.exceptions.FileWriteException;
import ncsa.tools.common.exceptions.VerificationException;
import ncsa.tools.common.types.FileFormatType;
import ncsa.tools.common.types.filters.RegExFilter;
import ncsa.tools.common.types.filters.RegExFilterQueue;

/**
 * Wrapper static methods for doing reads and writes using RandomAccessFile
 * object.
 *
 * @author Albert L. Rossi
 */
public class FileUtils {
    /**
     * Static utility class; cannot be constructed.
     */
    private FileUtils() {
    }

    /**
     * Checksum using java.util.zip.
     */
    public static long getChecksumValue(String path) throws IOException {
        Checksum checksum = new CRC32();
        BufferedInputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(path));
            byte[] bytes = new byte[NCSAConstants.READER_BUFFER_SIZE];
            int len = 0;
            while ((len = is.read(bytes)) >= 0) {
                checksum.update(bytes, 0, len);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException ignore) {
                }
        }
        return checksum.getValue();
    }

    public static void sync(File file) throws Throwable {
        if (file.isDirectory())
            return;
        FileInputStream fis = new FileInputStream(file);
        fis.getFD().sync();
        fis.close();
    }

    /*
     * ////////////////////////////////////////////////////////////////////////
     * READ //
     * /////////////////////////////////////////////////////////////////////
     */

    public static List readLinesFromStart(File file, long numLines, boolean includeEmptyLines) throws FileReadException {
        return readLines(file, 0, numLines, includeEmptyLines);
    }

    public static List readLinesFromEnd(File file, long numLines, boolean includeEmptyLines) throws FileReadException {
        return readLines(file, NCSAConstants.UNDEFINED, numLines, includeEmptyLines);
    }

    public static List readLines(File file, long firstLine, long numLines, boolean includeEmptyLines) throws FileReadException {
        List temp = new ArrayList();
        String line = null;
        long lineCount = countLines(file);

        // TAIL
        if (firstLine == NCSAConstants.UNDEFINED) {
            if (numLines == NCSAConstants.UNDEFINED) {
                numLines = lineCount;
            } else if (numLines < 0) {
                throw new FileReadException("bad number of lines: " + numLines);
            }
            firstLine = numLines == 0 ? 0 : lineCount - numLines;
        } else {
            if (numLines == NCSAConstants.UNDEFINED) {
                numLines = lineCount - firstLine;
            } else if (numLines < 0) {
                throw new FileReadException("bad number of lines: " + numLines);
            }
        }

        if (firstLine < 0 || firstLine > lineCount)
            throw new FileReadException("bad first line " + firstLine + ", number of lines: " + lineCount);

        if (firstLine + numLines > lineCount)
            throw new FileReadException("bad number of lines: last line number " + (firstLine + numLines - 1) + ", number of lines: "
                + lineCount);

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file), NCSAConstants.READER_BUFFER_SIZE);
            for (int i = 0; i < firstLine; i++) {
                line = br.readLine();
                if (line == null)
                    break;
            }

            for (int i = 0; i < numLines; i++) {
                line = br.readLine();
                if (line == null)
                    break;
                if (!line.trim().equals("") || includeEmptyLines)
                    temp.add(line);
            }
        } catch (OutOfMemoryError error) {
            throw new FileReadException(file + " too big to read into memory", error);
        } catch (IOException ioe) {
            throw new FileReadException("Read.readLines", ioe);
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException ignore) {
                }
        }

        return temp;
    } // readLines

    /**
     * Counts the number of lines in the file.
     *
     * @param file to read.
     * @return number of lines in file.
     */
    public static long countLines(File file) throws FileReadException {
        long lineCount = 0;
        String line = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file), NCSAConstants.READER_BUFFER_SIZE);
            while (true) {
                try {
                    line = br.readLine();
                } catch (IOException ioe) {
                    throw new FileReadException("Read.readLines", ioe);
                }
                if (line == null)
                    break;
                lineCount++;
            }
        } catch (Throwable t) {
            throw new FileReadException("problem reading lines for " + file, t);
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException ignore) {
                }
        }
        return lineCount;
    }

    public static byte[] readBytes(File file, int start, int numBytes) throws FileReadException {
        String byteString = read(file, start, numBytes);
        return byteString.getBytes();
    }

    public static String read(File file, long startOffset, long numBytes) throws FileReadException {
        if (numBytes == 0)
            return "";

        if (startOffset > file.length())
            throw new FileReadException("startOffset " + startOffset + " is past the end of the file: " + file.length());

        if (numBytes < 0 || file.length() - startOffset < numBytes)
            numBytes = file.length() - startOffset;

        int read = 0;
        long left = 0;
        byte[] bytes = new byte[NCSAConstants.READER_BUFFER_SIZE];
        StringBuffer sb = new StringBuffer();
        FileInputStream stream = null;

        try {
            stream = new FileInputStream(file);
            left = startOffset;
            int toRead = bytes.length;

            // read up to startOffset
            while (left > 0) {
                if (left < toRead)
                    toRead = (int) left;
                read = stream.read(bytes, 0, toRead);
                left -= read;
            }

            left = numBytes;

            while (left > 0) {
                try {
                    read = stream.read(bytes, 0, bytes.length);
                } catch (EOFException eof) {
                    break;
                }
                if (read == 0)
                    continue;
                if (read > left) {
                    // if left is less than read, it is an integer
                    sb.append(new String(bytes, 0, (int) left));
                    break;
                }
                sb.append(new String(bytes, 0, read));
                left -= read;
            }
        } catch (Throwable t) {
            throw new FileReadException("could not read " + file, t);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException t) {
                    throw new FileReadException(t);
                }
            }
        }
        return sb.toString();
    }

    /**
     * Calculates offset and number of bytes to read from set fields.
     * Loops through the format pattern, calling the underlying read
     * method on the RandomAccessFile as indicated by the FormatType.
     * For whitespace or return chars, calls skip or skipPast. Adds
     * the read object to values List.
     *
     * @param file        to read.
     * @param format      list of patterns to use in sequence.
     * @param startOffset where to begin (in bytes); if < 0, will be
     *                    set to 0.
     * @param numBytes    number of bytes to read from offset; if
     *                    undefined ( NCSAConstants.UNDEFINED ), will read to the end
     *                    of the file.
     * @param maxCycles   maximum number of times to loop through the format
     *                    list ( NCSAConstants>UNPROCESSED = indefinitely/to the end ).
     * @return the read values as a list (of possibly heterogeneous types).
     */
    public static List readFormatted(File file, FileFormatType[] format, long startOffset, long numBytes, int maxCycles)
        throws FileReadException, FileNotFoundException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");

        int fIndex = 0;
        int count = 0;
        long pos = 0;
        List values = new ArrayList();

        if (startOffset < 0)
            startOffset = 0;

        if (numBytes == NCSAConstants.UNDEFINED) {
            try {
                numBytes = raf.length() - startOffset;
            } catch (IOException ioe) {
                throw new FileReadException("readFormatted", ioe);
            }
        }

        try {
            raf.seek(startOffset);
        } catch (IOException ioe) {
            throw new FileReadException("readFormatted, could not call seek on " + file, ioe);
        }

        try {
            while (true) {
                if (maxCycles != NCSAConstants.UNDEFINED && count > maxCycles)
                    break;

                try {
                    if (raf.getFilePointer() - startOffset >= numBytes)
                        break;
                } catch (IOException ioe) {
                    throw new FileReadException("readFormatted, could not get file pointer for " + file, ioe);
                }

                Object next = null;
                try {
                    switch (format[fIndex].getType()) {
                        case NCSAConstants.BOOL:
                            next = new Boolean(raf.readBoolean());
                            break;
                        case NCSAConstants.BYTE:
                            next = new Byte(raf.readByte());
                            break;
                        case NCSAConstants.UBYT:
                            next = new Integer(raf.readUnsignedByte());
                            break;
                        case NCSAConstants.CHAR:
                            next = new Character(raf.readChar());
                            break;
                        case NCSAConstants.UTFC:
                            next = new String(raf.readUTF());
                            break;
                        case NCSAConstants.DOUB:
                            next = new Double(raf.readDouble());
                            break;
                        case NCSAConstants.FLOT:
                            next = new Float(raf.readFloat());
                            break;
                        case NCSAConstants.INTG:
                            next = new Integer(raf.readInt());
                            break;
                        case NCSAConstants.LONG:
                            next = new Long(raf.readLong());
                            break;
                        case NCSAConstants.SHRT:
                            next = new Short(raf.readShort());
                            break;
                        case NCSAConstants.USHT:
                            next = new Integer(raf.readUnsignedShort());
                            break;
                        default:
                            break;
                    }
                    if (next != null)
                        values.add(next);

                    switch (format[fIndex].getType()) {
                        case NCSAConstants.WSPC:
                            pos = skip(raf, new char[]{' ', '\t'}, true);
                            raf.seek(pos);
                            next = "";
                            break;
                        case NCSAConstants.CRET:
                            pos = skip(raf, new char[]{'\r'}, false);
                            raf.seek(pos);
                            next = "";
                            break;
                        case NCSAConstants.NEWL:
                            pos = skip(raf, new char[]{NCSAConstants.LINE_SEP_CHAR}, false);
                            raf.seek(pos);
                            next = "";
                            break;
                        default:
                            break;
                    }
                } catch (EOFException eofe) {
                    break;
                } catch (IOException ioe) {
                    throw new FileReadException("readFormatted", ioe);
                }
                if (next == null)
                    break;
                count++;
                fIndex = (fIndex + 1) % format.length;
            }
        } catch (OutOfMemoryError error) {
            throw new FileReadException(file + " too big to read into memory", error);
        } finally {
            try {
                raf.close();
            } catch (IOException t) {
            }
        }
        return values;
    } // readFormatted

    /*
     * ////////////////////////////////////////////////////////////////////////
     * CONTENT VERIFICATION //
     * /////////////////////////////////////////////////////////////////////
     */

    public static boolean verifyByLines(File file, String regexPattern, long linesFromStart, long linesFromEnd)
        throws VerificationException {
        return verifyByLines(file, regexPattern, linesFromStart, linesFromEnd, NCSAConstants.MAX_TAG_LENGTH,
            NCSAConstants.MAX_PATTERN_LENGTH);
    }

    public static boolean verifyByChunks(File file, String regexPattern) throws VerificationException {
        return verifyByChunks(file, regexPattern, NCSAConstants.MAX_TAG_LENGTH, NCSAConstants.MAX_PATTERN_LENGTH);
    }

    public static boolean verifyByLines(File file, String regexPattern, long linesFromStart, long linesFromEnd, int maxTagLength,
                                        int maxPatternLength) throws VerificationException {
        RegExFilterQueue queue = getInitializedFilterQueue(regexPattern, maxTagLength, maxPatternLength);
        List list = null;
        try {
            if (linesFromEnd != NCSAConstants.UNDEFINED) {
                list = FileUtils.readLines(file, NCSAConstants.UNDEFINED, linesFromEnd, true);
            } else if (linesFromStart != NCSAConstants.UNDEFINED) {
                list = FileUtils.readLines(file, 0, linesFromStart, true);
            }
        } catch (Throwable t) {
            throw new VerificationException("problem reading lines", t);
        }

        if (list == null || list.isEmpty())
            throw new VerificationException("read lines for " + file.getAbsolutePath() + " returned no lines");
        String[] lines = (String[]) list.toArray(new String[0]);
        for (int i = 0; i < lines.length; i++) {
            List match = queue.applyFilters(lines[i]);
            if (!match.isEmpty())
                return true;
        }
        return false;
    }

    public static boolean verifyByChunks(File file, String regexPattern, int maxTagLength, int maxPatternLength)
        throws VerificationException {
        RegExFilterQueue queue = getInitializedFilterQueue(regexPattern, maxTagLength, maxPatternLength);
        long totalRead = 0;
        int bytes = 0;
        char[] chars = new char[NCSAConstants.COPY_BUFFER_SIZE];
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file), NCSAConstants.READER_BUFFER_SIZE);

            while (totalRead < file.length()) {
                try {
                    bytes = br.read(chars, 0, chars.length);
                } catch (EOFException eof) {
                    break;
                }
                if (bytes == 0)
                    continue; // should this happen?
                totalRead += bytes;
                String chunk = new String(chars, 0, bytes);
                List match = queue.applyFilters(chunk);
                if (!match.isEmpty())
                    return true;
            }
        } catch (Throwable t) {
            throw new VerificationException("problem reading chunk", t);
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException ignore) {
                }
        }
        return false;
    }

    public static boolean verifyLineCount(File file, long numberOfLines, String comparatorTag) throws VerificationException {
        long lineCount = 0;
        try {
            lineCount = FileUtils.countLines(file);
        } catch (Throwable t) {
            throw new VerificationException("problem reading lines for " + file.getAbsolutePath(), t);
        }

        int comparator = NCSAConstants.UNDEFINED;
        try {
            comparator = ComparisonUtils.getComparatorValue(comparatorTag);
        } catch (IllegalArgumentException t) {
            throw new VerificationException("comparator " + comparatorTag + " not recognized", t);
        }

        switch (comparator) {
            case NCSAConstants.NE:
            case NCSAConstants.NEQUALS:
                return (lineCount != numberOfLines);
            case NCSAConstants.GT:
                return (lineCount > numberOfLines);
            case NCSAConstants.GE:
                return (lineCount >= numberOfLines);
            case NCSAConstants.LT:
                return (lineCount < numberOfLines);
            case NCSAConstants.LE:
                return (lineCount <= numberOfLines);
            case NCSAConstants.EQ:
            case NCSAConstants.EQUALS:
            default:
                return (lineCount == numberOfLines);
        }
    }

    /*
     * ////////////////////////////////////////////////////////////////////////
     * WRITE //
     * /////////////////////////////////////////////////////////////////////
     */

    public static void writeBytes(File file, byte[] bytes, boolean append) throws FileWriteException {
        try {
            FileOutputStream stream = new FileOutputStream(file, append);
            stream.write(bytes);
            stream.flush();
            stream.close();
        } catch (IOException t) {
            throw new FileWriteException("could not write to " + file + ": " + new String(bytes), t);
        }
    }

    public static void writeLines(File file, List lines, boolean append) throws FileWriteException {
        writeLines(file, (String[]) lines.toArray(new String[0]), append, true);
    }

    public static void writeLines(File file, List lines, boolean append, boolean includeEmpty) throws FileWriteException {
        writeLines(file, (String[]) lines.toArray(new String[0]), append, includeEmpty);
    }

    public static void writeLines(File file, String[] lines, boolean append, boolean includeEmpty) throws FileWriteException {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, append));
            for (int i = 0; i < lines.length; i++) {
                if (includeEmpty || (lines[i] != null && !"".equals(lines[i].trim()))) {
                    writer.write(lines[i]);
                    writer.write(NCSAConstants.LINE_SEP);
                }
            }
            writer.flush();
            writer.close();
        } catch (IOException t) {
            throw new FileWriteException("could not write lines to " + file, t);
        }
    }

    public static void writeBinary(File file, List content, boolean append, String mode) throws FileWriteException {
        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(file, mode);
        } catch (FileNotFoundException t1) {
            throw new FileWriteException(t1);
        }

        try {
            if (append)
                raf.seek(raf.length());
            else {
                raf.setLength(0);
                raf.seek(0);
            }
        } catch (IOException ioe) {
            throw new FileWriteException("FileUtils.write, could not call seek on " + raf, ioe);
        }

        try {
            for (ListIterator it = content.listIterator(); it.hasNext(); ) {
                Object o = it.next();
                if (o == null)
                    raf.writeBytes(null);
                else if (o instanceof Boolean)
                    raf.writeBoolean(((Boolean) o).booleanValue());
                else if (o instanceof Byte)
                    raf.writeByte(((Byte) o).intValue());
                else if (o instanceof Character)
                    raf.writeChar(((Character) o).charValue());
                else if (o instanceof Double)
                    raf.writeDouble(((Double) o).doubleValue());
                else if (o instanceof Float)
                    raf.writeFloat(((Float) o).floatValue());
                else if (o instanceof Integer)
                    raf.writeInt(((Integer) o).intValue());
                else if (o instanceof Long)
                    raf.writeLong(((Long) o).longValue());
                else if (o instanceof Short)
                    raf.writeShort(((Short) o).shortValue());
                else
                    raf.writeBytes(o.toString());
            }
        } catch (IOException ioe) {
            throw new FileWriteException("FileUtils.write", ioe);
        } finally {
            try {
                raf.getFD().sync();
                raf.close();
            } catch (IOException t) {
            }
        }
    }

    /*
     * ////////////////////////////////////////////////////////////////////////
     * AUXILIARY METHODS //
     * /////////////////////////////////////////////////////////////////////
     */

    /**
     * If wspc is true, skips whitespace until it finds the first
     * matching character; else, skips past all matching characters until
     * it finds the first non-match.
     *
     * @param file  as initialized by execute method.
     * @param match array of characters which are valid matches.
     * @param wspc  true = skip whitespace to first match;
     *              else skip to first non-matching.
     * @return the file pointer value at return.
     * @throws NCSAException if an IOException is thrown getting the
     *                       current file pointer.
     */
    private static long skip(RandomAccessFile file, char[] match, boolean wspc) throws IOException {
        long pos = file.getFilePointer();

        while (true) {
            char c = 0;
            try {
                c = file.readChar();
                boolean matches = false;
                for (int i = 0; i < match.length; i++) {
                    if (c == match[i]) {
                        matches = true;
                        break;
                    }
                }
                if (wspc) {
                    if (!matches)
                        break;
                    pos = file.getFilePointer();
                } else {
                    pos = file.getFilePointer();
                    if (matches)
                        break;
                }
            } catch (IOException ieo) {
                // either end of file or next byte is not a char
                break;
            }
        }
        return pos;
    } // skip

    private static RegExFilterQueue getInitializedFilterQueue(String regexPattern, int maxTagLength, int maxPatternLength)
        throws VerificationException {
        try {
            RegExFilter filter = new RegExFilter();
            filter.setPattern(regexPattern);
            filter.setMaxTagLength(maxTagLength);
            filter.setMaxPatternLength(maxPatternLength);
            filter.setMatchLines(false);
            RegExFilterQueue queue = new RegExFilterQueue();
            queue.setType("one-to-one");
            queue.addRegexFilter(filter);
            return queue;
        } catch (Throwable t) {
            throw new VerificationException("could not initialize filter queue", t);
        }
    }
}
