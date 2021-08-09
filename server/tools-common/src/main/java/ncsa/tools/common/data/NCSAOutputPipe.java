/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import ncsa.tools.common.NCSAConstants;

import org.apache.log4j.Logger;

/**
 * General-purpose pipe for capturing data written to an output stream.
 * Modes of usage: (a) write to log; (b) write to file;
 * (c) read a chunk at a time (consume the buffer).
 *
 * @author Albert L. Rossi
 */
public class NCSAOutputPipe extends OutputStream {
    protected static Logger logger = Logger.getLogger(NCSAOutputPipe.class);

    protected StringBuffer sb = new StringBuffer(0);
    protected String reply = "";
    protected File fout = null;
    protected boolean log = false;
    protected boolean buffer = true;
    protected boolean append = false;
    protected boolean error = false;
    protected BufferedWriter bw = null;

    /**
     * @param b if true, stores received output in a buffer to be read
     *          (default=true).
     */
    public void setBuffer(boolean b) {
        buffer = b;
    }

    /**
     * @param b if true, this stream represents an error stream.
     */
    public void setError(boolean b) {
        error = b;
    }

    /**
     * @param b if true, log message as INFO or ERROR (if an error stream).
     */
    public void setLog(boolean b) {
        log = b;
    }

    /**
     * @param f file to which to write output.
     */
    public void setFile(File f) {
        fout = f;
    }

    /**
     * @param b if true, append to the indicated file (default=false).
     */
    public void setAppend(boolean b) {
        append = b;
    }

    /**
     * Appends to string buffer.
     *
     * @param bigbyte int representation of a byte to write.
     * @throws IOException if output exceeds memory, or if the
     *                     stream has been closed.
     */
    public void write(int bigbyte) throws IOException {
        try {
            if (bigbyte == NCSAConstants.EOF)
                return;
            synchronized (this) {
                sb.append((char) bigbyte);
            }
        } catch (OutOfMemoryError oome) {
            throw new IOException("ListInput.write: output too big to store in memory: " + oome.toString());
        } catch (NullPointerException npe) {
            throw new IOException("ListInput stream has been closed; cannot write");
        }
    } // write

    /**
     * If log is true, logs the current buffer as info; if file exists,
     * writes it to file and flushes.
     * If buffer is set to true, appends to the current reply string
     * and zeros-out the buffer.
     */
    public void flush() throws IOException {
        try {
            synchronized (this) {
                if (fout != null) {
                    if (bw == null) {
                        bw = new BufferedWriter(new FileWriter(fout, append));
                    }
                    bw.write(sb.toString());
                    bw.flush();
                }

                if (log) {
                    if (error)
                        logger.error(sb.toString());
                    else
                        logger.info(sb.toString());
                }

                if (buffer) {
                    reply += sb.toString();
                }

                sb.setLength(0);
            }
        } catch (NullPointerException npe) {
            throw new IOException("ListInput stream has been closed; cannot flush");
        }
    } // flush

    /**
     * Nulls out the buffer.
     */
    public void close() {
        synchronized (this) {
            sb = null;
        }

        try {
            if (bw != null) {
                bw.flush();
                bw.close();
            }
        } catch (IOException ioe) {
            logger.error("could not close " + bw, ioe);
        }
    } // close

    /**
     * Sets reply to empty after read.
     *
     * @return the reply.
     */
    public String readReply() {
        String result = null;
        synchronized (this) {
            result = reply;
            reply = "";
        }
        return result;
    } // readReply

}
