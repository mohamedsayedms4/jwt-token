package org.example.mobilyecommerce.service;

import org.example.mobilyecommerce.dto.BrandingInfoDto;

public interface BrandingInfoService {

    BrandingInfoDto getBrandingInfo();
    BrandingInfoDto updateBrandingInfo(BrandingInfoDto dto);

    BrandingInfoDto save(BrandingInfoDto dto);
}
