package org.example.mobilyecommerce.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin("*")
public class FileController {

    private final Path uploadDir = Paths.get("uploads");

    public FileController() {
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!", e);
        }
    }

    // 🟢 رفع صورة
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 1️⃣ تحقق من أن الملف مش فاضي
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty!");
            }

            // 2️⃣ اسم الملف الأصلي
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

            // 3️⃣ توليد اسم فريد للملف
            String fileName = UUID.randomUUID() + "_" + originalFilename;

            // 4️⃣ حفظ الملف
            Path targetLocation = this.uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 5️⃣ إرجاع رابط التحميل
            String fileUrl = "/api/files/" + fileName;
            return ResponseEntity.ok(fileUrl);

        } catch (IOException e) {
            log.error("Error saving file: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not store file: " + e.getMessage());
        }
    }

    // 🟡 عرض الصورة أو تحميلها
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            Path filePath = uploadDir.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            // تحديد نوع المحتوى
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
