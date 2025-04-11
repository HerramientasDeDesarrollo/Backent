package com.example.guiavirtual.Service;

import com.example.guiavirtual.Model.UserModel;
import com.example.guiavirtual.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserModel findOrCreateUser(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");

        return userRepository.findByEmail(email).orElseGet(() -> {
            UserModel user = new UserModel();
            user.setEmail(email);
            user.setName(oAuth2User.getAttribute("name"));
            user.setPicture(oAuth2User.getAttribute("picture"));
            return userRepository.save(user);
        });
    }
}
