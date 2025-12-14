package org.example.mobilyecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.mobilyecommerce.dto.BrandingInfoDto;
import org.example.mobilyecommerce.mapper.BrandingInfoMapper;
import org.example.mobilyecommerce.model.BrandingInfo;
import org.example.mobilyecommerce.repository.BrandingInfoRepository;
import org.example.mobilyecommerce.service.BrandingInfoService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandingInfoServiceImpl implements BrandingInfoService {

    private final BrandingInfoRepository repository;
    private final BrandingInfoMapper mapper;

    @Override
    @Cacheable(value = "brandingInfo", key = "'singleton'")
    public BrandingInfoDto getBrandingInfo() {
        return repository.findById(1L)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    @CacheEvict(value = "brandingInfo", allEntries = true)
    public BrandingInfoDto save(BrandingInfoDto dto) {
        BrandingInfo info = mapper.toEntity(dto);
        BrandingInfo saved = repository.save(info);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "brandingInfo", allEntries = true)
    public BrandingInfoDto updateBrandingInfo(BrandingInfoDto dto) {
        BrandingInfo info = repository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Branding info not found"));

        info.setBrandName(dto.brandName());
        info.setBrandLogo(dto.brandLogo());
        info.setFacebookLink(dto.facebookLink());
        info.setInstagramLink(dto.instagramLink());
        info.setWhatsappLink(dto.whatsappLink());
        info.setTiktokLink(dto.tiktokLink());
        info.setPhoneNumber(dto.phoneNumber());
        info.setEmail(dto.email());
        info.setAddress(dto.address());

        repository.save(info);

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
}