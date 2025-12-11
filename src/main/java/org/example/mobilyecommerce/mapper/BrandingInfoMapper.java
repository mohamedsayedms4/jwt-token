package org.example.mobilyecommerce.mapper;

import org.example.mobilyecommerce.dto.BrandingInfoDto;
import org.example.mobilyecommerce.model.BrandingInfo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BrandingInfoMapper {

    BrandingInfoDto toDto(BrandingInfo entity);

    BrandingInfo toEntity(BrandingInfoDto dto);

    void updateEntityFromDto(BrandingInfoDto dto, @MappingTarget BrandingInfo entity);
}
