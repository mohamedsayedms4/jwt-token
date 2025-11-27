package org.example.mobilyecommerce.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.mobilyecommerce.service.FileService;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    // مسار حفظ الصور في السيرفر
    private final Path fileStorageLocation = Paths.get("/var/www/bigzero-image")
            .toAbsolutePath()
            .normalize();

    // رابط موقع الـ Backend
    private final String BASE_URL = "https://api-spring.bigzero.online";

    public FileServiceImpl() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            log.error("Could not create upload directory", e);
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        if (originalFileName.contains("..")) {
            throw new IOException("Filename contains invalid path sequence: " + originalFileName);
        }

        Path targetLocation = this.fileStorageLocation.resolve(originalFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // إرجاع URL كامل قابل للوصول من React أو أي Frontend
        return BASE_URL + "/api/files/" + originalFileName;
    }

    @Override
    public Resource loadFile(String filename) throws IOException {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new IOException("File not found: " + filename);
            }

        } catch (MalformedURLException e) {
            throw new IOException("File path is invalid: " + filename, e);
        }
    }

    @Override
    public boolean deleteFile(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            return Files.deleteIfExists(filePath);

        } catch (IOException e) {
            log.error("Failed to delete file: {}", filename, e);
            return false;
        }
    }
}
