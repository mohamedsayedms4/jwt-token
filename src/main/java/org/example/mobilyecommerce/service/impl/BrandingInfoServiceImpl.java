package org.example.mobilyecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.mobilyecommerce.dto.BrandingInfoDto;
import org.example.mobilyecommerce.mapper.BrandingInfoMapper;
import org.example.mobilyecommerce.model.BrandingInfo;
import org.example.mobilyecommerce.repository.BrandingInfoRepository;
import org.example.mobilyecommerce.service.BrandingInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class BrandingInfoServiceImpl implements BrandingInfoService {

    private final BrandingInfoRepository repository;
    private final BrandingInfoMapper mapper;

    @Override
    public BrandingInfoDto getBrandingInfo() {
        BrandingInfoDto info = repository.findById(1L).map(mapper::toDto).orElse(null);
        return info;
    }

    @Override
    public BrandingInfoDto save(BrandingInfoDto dto){
        BrandingInfo info = mapper.toEntity(dto);
        BrandingInfo saved = repository.save(info);
        return mapper.toDto(saved);
    }
    @Override
    @Transactional
    public BrandingInfoDto updateBrandingInfo(BrandingInfoDto dto) {
        BrandingInfo info = repository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Branding info not found"));

        info.setBrandName(dto.brandName());
        info.setBrandLogo(dto.brandLogo());
        info.setFacebookLink(dto.facebookLink());
        info.setInstagramLink(dto.instagramLink());
        info.setWhatsappLink(dto.whatsappLink());
        info.setTiktokLink(dto.tiktokLink());
        info.setPhoneNumber(dto.phoneNumber()); // assuming it's already String
        info.setEmail(dto.email());
        info.setAddress(dto.address());

        repository.save(info);

        // Return updated DTO
        return new BrandingInfoDto(
                info.getId(),
                info.getBrandName(),
                info.getBrandLogo(),
                info.getFacebookLink(),
                info.getInstagramLink(),
                info.getWhatsappLink(),
                info.getTiktokLink(),
                info.getPhoneNumber(),
                info.getEmail(),
                info.getAddress()
        );
    }


    /**
     * Fetch the branding info singleton.
     * If it doesn't exist, create it with ID = 1.
     */

}
