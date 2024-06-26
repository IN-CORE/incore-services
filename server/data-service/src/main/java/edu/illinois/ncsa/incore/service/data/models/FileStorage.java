/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *
 *   Contributors:
 *   Chris Navarro (NCSA) - initial API and implementation
 *******************************************************************************/

package edu.illinois.ncsa.incore.service.data.models;

/**
 * Created by ywkim on 9/26/2017.
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * This interface allows files to be saved, read and deleted for different
 * storage implementations.
 *
 * @author Rob Kooper <kooper@illinois.edu>
 */
public interface FileStorage {
    /**
     * Store file in file storage system
     *
     * @param is - inputstream of the file to store
     * @return FileDescriptor for the stored file
     * @throws IOException if an I/O error occurs
     */
    FileDescriptor storeFile(InputStream is) throws IOException;

    /**
     * Store file in file storage system
     *
     * @param filename - name of the file
     * @param is       - inputstream of the file to store
     * @return FileDescriptor for the stored file
     * @throws IOException if an I/O error occurs
     */
    FileDescriptor storeFile(String filename, InputStream is) throws IOException;

    /**
     * Store file in file storage system
     *
     * @param filename - name of the file
     * @param is       - inputstream of the file to store
     * @param creator  - file creator
     * @return FileDescriptor for the stored file
     * @throws IOException if an I/O error occurs
     */
    FileDescriptor storeFile(String filename, InputStream is, Person creator) throws IOException;

    /**
     * Store file in file storage system
     *
     * @param fd - file descriptor to store the file with
     * @param is - inputstream of the file to store
     * @return URL of stored file
     * @throws IOException if an I/O error occurs
     */
    URL storeFile(FileDescriptor fd, InputStream is) throws IOException;

    /**
     * Store file in file storage system
     *
     * @param id       - descriptor id to store the file with
     * @param filename - name of the file
     * @param is       - inputstream of the file to store
     * @return FileDescriptor for the stored file
     * @throws IOException if an I/O error occurs
     */
    FileDescriptor storeFile(String id, String filename, InputStream is) throws IOException;

    /**
     * Store file in file storage system
     *
     * @param id       - descriptor id to store the file with
     * @param filename - name of the file
     * @param is       - inputstream of the file to store
     * @param creator  - file creator
     * @return FileDescriptor for the stored file
     * @throws IOException if an I/O error occurs
     */
    FileDescriptor storeFile(String id, String filename, InputStream is, Person creator) throws IOException;

    /**
     * Read file from storage
     *
     * @param fd FileDescriptor of file to read
     * @return Inpustream of the file
     * @throws IOException if an I/O error occurs
     */
    InputStream readFile(FileDescriptor fd) throws IOException;

    /**
     * Deletes file from storage
     *
     * @param fd - File descriptor representing the file to delete
     * @return true if the delete operation was successful.
     */
    boolean deleteFile(FileDescriptor fd);

    /**
     * Deletes file from storage
     *
     * @param fd      - File descriptor representing the file to delete
     * @param creator - file creator
     * @return true if the delete operation was successful.
     */
    boolean deleteFile(FileDescriptor fd, Person creator);
}
