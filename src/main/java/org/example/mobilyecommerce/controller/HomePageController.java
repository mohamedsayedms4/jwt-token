package org.example.mobilyecommerce.controller;

import org.example.mobilyecommerce.model.HomePage;
import org.example.mobilyecommerce.service.FileService;
import org.example.mobilyecommerce.service.impl.HomePageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/home")
public class HomePageController {

    private final HomePageService homePageService;
    private final FileService fileService;

    public HomePageController(HomePageService homePageService, FileService fileService) {
        this.homePageService = homePageService;
        this.fileService = fileService;
    }

    // ================= CREATE ====================
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HomePage> create(
            @RequestParam("image") MultipartFile image,
            @RequestParam("title") String title,
            @RequestParam("htmlUrl") String htmlUrl
    ) throws IOException {

        String imageUrl = fileService.uploadFile(image);

        HomePage homePage = new HomePage();
        homePage.setImageUrl(imageUrl);
        homePage.setTitle(title);
        homePage.setHtmlUrl(htmlUrl);

        return ResponseEntity.ok(homePageService.save(homePage));
    }


    // ================ GET ALL ====================
    @GetMapping
    public ResponseEntity<List<HomePage>> getAll() {
        return ResponseEntity.ok(homePageService.getAll());
    }

    // ================ GET BY ID ===================
    @GetMapping("/{id}")
    public ResponseEntity<HomePage> getById(@PathVariable Long id) {
        return ResponseEntity.ok(homePageService.getById(id));
    }

    // ================ UPDATE ======================
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<HomePage> update(
            @PathVariable Long id,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam("title") String title,
            @RequestParam("htmlUrl") String htmlUrl
    ) throws IOException {

        HomePage existing = homePageService.getById(id);

        if (image != null && !image.isEmpty()) {
            String newUrl = fileService.uploadFile(image);
            existing.setImageUrl(newUrl);
        }

        existing.setTitle(title);
        existing.setHtmlUrl(htmlUrl);

        return ResponseEntity.ok(homePageService.save(existing));
    }

    // ================ DELETE ======================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        homePageService.delete(id);
        return ResponseEntity.ok("Deleted Successfully!");
    }
}
