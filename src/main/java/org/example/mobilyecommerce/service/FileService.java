package org.example.mobilyecommerce.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Interface defining contract for file operations.
 */
public interface FileService {

    /**
     * Uploads a file and returns its public URL.
     */
    String uploadFile(MultipartFile file) throws IOException;

    /**
     * Loads a file as a resource.
     */
    Resource loadFile(String filename) throws IOException;

    /**
     * Deletes a file by name.
     */
    boolean deleteFile(String filename);
}
