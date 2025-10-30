package org.example.mobilyecommerce.controller.vm;


import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String username;
    private String newPassword;
}
