package org.example.mobilyecommerce.service;

import org.example.mobilyecommerce.controller.vm.AuthRequestVm;
import org.example.mobilyecommerce.controller.vm.AuthResponseVm;
import org.example.mobilyecommerce.model.User;

public interface AuthServiceInterface {

    AuthResponseVm signup(User user, String ip, String agent);

    AuthResponseVm login(AuthRequestVm login, String ip, String agent);

    AuthResponseVm refresh(String refreshTokenValue, String ip, String agent);

    void logout(User user);
}
