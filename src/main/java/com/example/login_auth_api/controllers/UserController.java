package com.example.login_auth_api.controllers;

import com.example.login_auth_api.domain.user.Role;
import com.example.login_auth_api.domain.user.User;
import com.example.login_auth_api.exceptions.CustomException;
import com.example.login_auth_api.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // Rota para mudança de role do usuário (role passada como parâmetro, não no body)
    @PutMapping("/user/changerole/{userId}")
    public ResponseEntity<String> changeUserRole(@PathVariable String userId, @RequestParam String newRole) {
        try {
            // Converte a String para o Enum Role
            Role role = Role.valueOf(newRole.toUpperCase());

            // Busca o usuário no repositório
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException("Usuário não encontrado."));

            // Atualiza o papel do usuário
            user.setRole(role);
            userRepository.save(user);

            return ResponseEntity.ok("Função do usuário atualizada com sucesso!");

        } catch (IllegalArgumentException ex) {
            // Captura a exceção e retorna uma mensagem amigável
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Função inválida. As funções permitidas são: USER, ADMIN.");
        } catch (CustomException ex) {
            // Tratamento para outras exceções personalizadas
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (Exception ex) {
            // Tratamento genérico para exceções não previstas
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocorreu um erro interno no servidor.");
        }
    }

    // Rota para autorização de usuário (mudar a autorização para true)
    @PutMapping("/user/authorize/{userId}")
    public ResponseEntity<?> authorizeUser(@PathVariable String userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
            }

            User user = userOpt.get();
            user.setAuthorized(true);
            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.OK).body("Autorização concedida com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    // Rota para remoção da autorização de usuário (mudar a autorização para false)
    @PutMapping("/user/unauthorize/{userId}")
    public ResponseEntity<String> unauthorizeUser(@PathVariable String userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
            }

            User user = userOpt.get();
            user.setAuthorized(false);
            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.OK).body("Autorização removida com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    // Rota para listagem geral de usuários
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.status(HttpStatus.OK).body(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Rota para listagem de um único usuário
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuário não encontrado");
            }

            return ResponseEntity.status(HttpStatus.OK).body(userOpt.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

}
