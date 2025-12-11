    package org.example.mobilyecommerce.dto;


    public record BrandingInfoDto(
            Long id,
            String brandName,
            String brandLogo,
            String facebookLink,
            String whatsappLink,
            String instagramLink,
            String tiktokLink,
            String phoneNumber,
            String email,
            String address
    ) { }