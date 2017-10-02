package edu.illinois.ncsa.incore.service.data.model.datawolf.domain;

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
    public FileDescriptor storeFile(InputStream is) throws IOException;

    /**
     * Store file in file storage system
     *
     * @param filename - name of the file
     * @param is       - inputstream of the file to store
     * @return FileDescriptor for the stored file
     * @throws IOException if an I/O error occurs
     */
    public FileDescriptor storeFile(String filename, InputStream is) throws IOException;

    /**
     * Store file in file storage system
     *
     * @param filename - name of the file
     * @param is       - inputstream of the file to store
     * @param creator  - file creator
     * @return FileDescriptor for the stored file
     * @throws IOException if an I/O error occurs
     */
    public FileDescriptor storeFile(String filename, InputStream is, Person creator) throws IOException;

    /**
     * Store file in file storage system
     *
     * @param fd - file descriptor to store the file with
     * @param is - inputstream of the file to store
     * @return URL of stored file
     * @throws IOException if an I/O error occurs
     */
    public URL storeFile(FileDescriptor fd, InputStream is) throws IOException;

    /**
     * Store file in file storage system
     *
     * @param id       - descriptor id to store the file with
     * @param filename - name of the file
     * @param is       - inputstream of the file to store
     * @return FileDescriptor for the stored file
     * @throws IOException if an I/O error occurs
     */
    public FileDescriptor storeFile(String id, String filename, InputStream is) throws IOException;

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
    public FileDescriptor storeFile(String id, String filename, InputStream is, Person creator) throws IOException;

    /**
     * Read file from storage
     *
     * @param fd FileDescriptor of file to read
     * @return Inpustream of the file
     * @throws IOException if an I/O error occurs
     */
    public InputStream readFile(FileDescriptor fd) throws IOException;

    /**
     * Deletes file from storage
     *
     * @param fd - File descriptor representing the file to delete
     * @return true if the delete operation was successful.
     */
    public boolean deleteFile(FileDescriptor fd);

    /**
     * Deletes file from storage
     *
     * @param fd      - File descriptor representing the file to delete
     * @param creator - file creator
     * @return true if the delete operation was successful.
     */
    public boolean deleteFile(FileDescriptor fd, Person creator);
}
