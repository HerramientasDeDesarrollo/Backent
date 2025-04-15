package com.example.guiavirtual.Service;

import com.example.guiavirtual.Model.UserModel;
import com.example.guiavirtual.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserModel createUser(String email, String name, String picture) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            UserModel user = new UserModel();
            user.setEmail(email);
            user.setName(name);
            user.setPicture(picture);
            return userRepository.save(user);
        });
    }

    public List<UserModel> getAllUsers() {
        return userRepository.findAll();
    }

    public UserModel getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public UserModel updateUser(Long id, UserModel userDetails) {
        UserModel user = getUserById(id);
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setPicture(userDetails.getPicture());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
    }

    public UserModel getCurrentUser() {
        // TODO: Implementar lógica para obtener usuario actual
        return null;
    }
}
