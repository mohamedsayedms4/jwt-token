package org.example.mobilyecommerce.model;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomePage extends BaseEntity{

    private String imageUrl;
    private String title;
    private String HtmlUrl ;
}
