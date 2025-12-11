package org.example.mobilyecommerce.controller;

import lombok.RequiredArgsConstructor;
import org.example.mobilyecommerce.dto.BrandingInfoDto;
import org.example.mobilyecommerce.service.BrandingInfoService;
import org.example.mobilyecommerce.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/branding")
@RequiredArgsConstructor
public class BrandingInfoController {

    private final BrandingInfoService brandingInfoService;
    private final FileService fileService;

    // GET → جلب بيانات البراند والموقع
    @GetMapping
    public ResponseEntity<BrandingInfoDto> getBrandingInfo() {
        BrandingInfoDto dto = brandingInfoService.getBrandingInfo();
        return ResponseEntity.ok(dto);
    }

    // PUT → تحديث بيانات البراند والموقع
    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<BrandingInfoDto> updateBrandingInfo( @RequestPart("branding") BrandingInfoDto dto,
                                                               @RequestPart(value = "logo", required = false) MultipartFile logo) throws IOException {
        if (logo != null && !logo.isEmpty()) {
            String imageUrl = fileService.uploadFile(logo);
            dto = new BrandingInfoDto(
                    dto.id(),
                    dto.brandName(),
                    imageUrl,        // استخدام الصورة المرفوعة
                    dto.facebookLink(),
                    dto.whatsappLink(),
                    dto.instagramLink(),
                    dto.tiktokLink(),
                    dto.phoneNumber(),
                    dto.email(),
                    dto.address()
            );
        }

        BrandingInfoDto updated = brandingInfoService.updateBrandingInfo(dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<BrandingInfoDto> saveBrandingInfo( @RequestPart("branding") BrandingInfoDto dto,
                                                             @RequestPart(value = "logo", required = false) MultipartFile logo)
        throws IOException {
        if (logo != null && !logo.isEmpty()) {
            String imageUrl = fileService.uploadFile(logo);
            dto = new BrandingInfoDto(
                    dto.id(),
                    dto.brandName(),
                    imageUrl,        // استخدام الصورة المرفوعة
                    dto.facebookLink(),
                    dto.whatsappLink(),
                    dto.instagramLink(),
                    dto.tiktokLink(),
                    dto.phoneNumber(),
                    dto.email(),
                    dto.address()
            );
        }

        BrandingInfoDto saved = brandingInfoService.save(dto);
        return ResponseEntity.ok(saved);
    }
}
