package com.example.login_auth_api.controllers;

import com.example.login_auth_api.domain.user.Role;
import com.example.login_auth_api.domain.user.User;
import com.example.login_auth_api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Rota para mudança de role do usuario esse e passado como parametro nao no body
    @PutMapping("/user/changerole/{userId}")
    public ResponseEntity<String> changeUserRole(@PathVariable String userId, @RequestParam Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(newRole);
        userRepository.save(user);

        return ResponseEntity.ok("User role updated to " + newRole);
    }

    // Rota para autorização de usuário mudar a altorizaçao para true
    @PutMapping("/user/authorize/{userId}")
    public ResponseEntity<String> authorizeUser(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAuthorized(true);
        userRepository.save(user);

        return ResponseEntity.ok("User authorized successfully.");
    }

    // Rota para autorização de usuário mudar a altorizaçao para false
    @PutMapping("/user/unauthorize/{userId}")
    public ResponseEntity<String> unauthorizeUser(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAuthorized(false);
        userRepository.save(user);

        return ResponseEntity.ok("User unauthorized successfully.");
    }

    // Rota para listagem geral de usuários
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    // Rota para listagem de um único usuário
    @GetMapping("/user/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }
}
