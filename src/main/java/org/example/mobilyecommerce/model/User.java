package org.example.mobilyecommerce.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.context.annotation.Lazy;

import java.util.List;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_eco") // استخدم underscore بدل dash
@ToString

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String password;

    private String email;

    private String phone;

//    private String address;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @ToString.Exclude // ⬅️ هذا مهم
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private List<Role> roles;

    @PrePersist
    public void prePersist() {
        if (roles == null || roles.isEmpty()) {
            Role role = new Role();
            role.setRole("ROLE_USER");
            role.setUser(this);
            roles = new java.util.ArrayList<>();
            roles.add(role);
        }
    }
}
