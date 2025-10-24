package org.example.mobilyecommerce.service;

import org.example.mobilyecommerce.controller.vm.AuthRequestVm;
import org.example.mobilyecommerce.controller.vm.AuthResponseVm;
import org.example.mobilyecommerce.model.User;
import org.springframework.web.bind.annotation.RequestBody;

public interface AuthService {

    AuthResponseVm login(@RequestBody AuthRequestVm login);

    AuthResponseVm  signup(@RequestBody User user);

    AuthResponseVm refresh(String refreshToken);
}
