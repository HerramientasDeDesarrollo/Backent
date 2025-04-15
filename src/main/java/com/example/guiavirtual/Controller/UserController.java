package com.example.guiavirtual.Controller;

import com.example.guiavirtual.Model.UserModel;
import com.example.guiavirtual.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Create
    @PostMapping
    public ResponseEntity<UserModel> createUser(@RequestBody UserModel user) {
        return ResponseEntity.ok(userService.createUser(user.getEmail(), user.getName(), user.getPicture()));
    }

    // Read All
    @GetMapping
    public ResponseEntity<List<UserModel>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Read One
    @GetMapping("/{id}")
    public ResponseEntity<UserModel> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<UserModel> updateUser(@PathVariable Long id, @RequestBody UserModel user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    // Current user endpoint
    @GetMapping("/me")
    public UserModel currentUser() {
        return userService.getCurrentUser();
    }
}