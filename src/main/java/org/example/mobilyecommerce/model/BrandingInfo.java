package org.example.mobilyecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.persistence.Version;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BrandingInfo extends BaseEntity {


    @Column(columnDefinition = "NVARCHAR(100)")
    private String brandName;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String brandLogo;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String facebookLink;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String whatsappLink;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String instagramLink;

    @Column(columnDefinition = "NVARCHAR(500)")
    private String tiktokLink;

    @Column(columnDefinition = "NVARCHAR(20)")
    private String phoneNumber;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String email;

    @Column(columnDefinition = "NVARCHAR(200)")
    private String address;
}
