package org.example.mobilyecommerce.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 1000)
    private String token;

    private boolean revoked;
    private boolean expired;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant expiryDate; // ⏳ مدة صلاحية التوكن

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    public void setExpiryDate() {
        this.expiryDate = Instant.now().plusSeconds(60); // ⏱️ دقيقة واحدة مثلاً
    }
}
