package com.example.guiavirtual.Controller;

import com.example.guiavirtual.Model.UserModel;
import com.example.guiavirtual.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public UserModel currentUser(@AuthenticationPrincipal OAuth2User principal) {
        return userService.findOrCreateUser(principal);
    }
}