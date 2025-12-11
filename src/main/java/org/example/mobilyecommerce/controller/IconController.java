package org.example.mobilyecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.example.mobilyecommerce.dto.BrandingInfoDto;
import org.example.mobilyecommerce.model.IconDetail;
import org.example.mobilyecommerce.model.Icons;
import org.example.mobilyecommerce.service.FileService;
import org.example.mobilyecommerce.service.IconService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/icons")
public class IconController {


    private final IconService iconService;
    private final FileService fileService;


    @PostMapping("/icons")
    public ResponseEntity<Icons> createIcons(
            @RequestParam("iconName") String iconName,  // غيّر من @RequestPart إلى @RequestParam
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam("names") List<String> names) {  // غيّر من @RequestPart إلى @RequestParam

        try {
            Icons icons = new Icons();
            icons.setIconName(iconName);

            List<IconDetail> iconDetails = new ArrayList<>();

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                String fileUrl = fileService.uploadFile(file);

                IconDetail detail = new IconDetail();
                detail.setName(names.get(i));
                detail.setIcon_url(fileUrl);
                iconDetails.add(detail);
            }

            icons.setIcons(iconDetails);
            Icons savedIcons = iconService.createIcon(icons);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedIcons);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Icons>> getAllIcons() {
        List<Icons> icons = iconService.getAllIcons();
        return ResponseEntity.ok(icons);
    }

    // Read by ID
    @GetMapping("/{id}")
    public ResponseEntity<Icons> getIconById(@PathVariable Long id) {
        Icons icon = iconService.getIconById(id);
        return ResponseEntity.ok(icon);
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<Icons> updateIcon(@PathVariable Long id, @RequestBody Icons icons) {
        Icons updated = iconService.updateIcon(id, icons);
        return ResponseEntity.ok(updated);
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIcon(@PathVariable Long id) {
        iconService.deleteIcon(id);
        return ResponseEntity.noContent().build();
    }
}
