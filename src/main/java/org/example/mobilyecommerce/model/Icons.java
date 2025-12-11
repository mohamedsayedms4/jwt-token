package org.example.mobilyecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)

public class Icons extends BaseEntity{

    private String iconName;

    @ElementCollection
    @CollectionTable(
            name = "icon_details",
            joinColumns = @JoinColumn(name = "icons_id")
    )
    private List<IconDetail> icons;



}
