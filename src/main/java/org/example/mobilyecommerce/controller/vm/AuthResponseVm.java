package org.example.mobilyecommerce.controller.vm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthResponseVm {
    private String accessToken;
    private String refreshToken;
}
