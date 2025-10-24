package org.example.mobilyecommerce.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ✅ بدل OneToOne بخليها ManyToOne
     * عشان المستخدم يقدر يمتلك أكتر من Refresh Token
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 1000)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean expired = false;

    /**
     * ✅ التحقق من انتهاء صلاحية التوكن
     */
    public boolean isExpired() {
        return this.expiryDate.isBefore(Instant.now()) || this.expired;
    }
}
