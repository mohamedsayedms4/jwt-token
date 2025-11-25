package org.example.mobilyecommerce.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service interface for handling file operations such as upload, download, and deletion.
 */
public interface FileService {

    /**
     * Uploads the given file to the server and returns the public URL where it can be accessed.
     *
     * @param file the MultipartFile to be uploaded
     * @return the URL of the uploaded file
     * @throws IOException if an I/O error occurs during upload
     */
    String uploadFile(MultipartFile file) throws IOException;

    /**
     * Loads the specified file as a Spring {@link Resource}.
     *
     * @param filename the name of the file to load
     * @return the file as a Resource
     * @throws IOException if the file does not exist or cannot be read
     */
    Resource loadFile(String filename) throws IOException;

    /**
     * Deletes the file with the given name from the server storage.
     *
     * @param filename the name of the file to delete
     * @return true if the file was successfully deleted, false otherwise
     */
    boolean deleteFile(String filename);
}
