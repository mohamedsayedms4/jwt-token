package org.example.mobilyecommerce.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

/**
 * Implementation of FileService for handling file upload, retrieval, and deletion.
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    private final Path uploadDir = Paths.get("uploads");

    public FileServiceImpl() {
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("Created upload directory: {}", uploadDir.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty!");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + "_" + originalFilename;

        Path targetLocation = this.uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        log.info("File saved: {}", targetLocation);

        // ✅ استخدم URL كامل بدل المسار النسبي
        return "https://api-spring.bigzero.online/api/files" + fileName;
    }

    @Override
    public Resource loadFile(String filename) throws IOException {
        try {
            Path filePath = uploadDir.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new NoSuchFileException("File not found: " + filename);
            }

            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid file path: " + filename, e);
        }
    }

    @Override
    public boolean deleteFile(String filename) {
        try {
            Path filePath = uploadDir.resolve(filename).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Error deleting file: {}", filename, e);
            return false;
        }
    }
}
