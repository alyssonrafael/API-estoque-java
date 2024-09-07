package com.example.login_auth_api.controllers;

import com.example.login_auth_api.domain.user.Role;
import com.example.login_auth_api.domain.user.User;
import com.example.login_auth_api.dto.LoginRequestDTO;
import com.example.login_auth_api.dto.RegisterRequestDTO;
import com.example.login_auth_api.dto.ResponseDTO;
import com.example.login_auth_api.infra.security.TokenService;
import com.example.login_auth_api.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO body) {
        try {
            Optional<User> userOptional = this.repository.findByEmail(body.email());

            // Se o usuário não for encontrado, retorna uma resposta com status 400 Bad Request
            if (userOptional.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message","Credenciais inválidas"));
            }

            User user = userOptional.get();

            // Verifica se o usuário está autorizado
            if (!user.getAuthorized()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Usuário ainda não autorizado");
            }

            // Verifica se a senha está correta
            if (passwordEncoder.matches(body.password(), user.getPassword())) {
                String token = this.tokenService.generateToken(user);
                return ResponseEntity.ok(new ResponseDTO(token));
            }

            // Se a senha estiver incorreta, retorna bad request
            return ResponseEntity.badRequest().body("Credenciais inválidas");
        } catch (Exception e) {
            // Retorna uma mensagem de erro genérica para erros inesperados
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ocorreu um erro interno no servidor.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO body) {
        try {
            // Verifica se o número de usuários cadastrados já atingiu o limite de 5
            long userCount = this.repository.count(); // Conta o número total de usuários
            if (userCount >= 5) {
                // Retorna um erro 400 Bad Request com uma mensagem personalizada
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body("Limite máximo de usuários atingido. Não é possível criar mais usuários.");
            }

            // Verifica se o e-mail já está registrado
            Optional<User> user = this.repository.findByEmail(body.email());
            if (user.isPresent()) {
                // Retorna um erro 400 Bad Request com uma mensagem personalizada
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Erro ao Realizar cadastro, tente novamente");
            }

            // Cria um novo usuário
            User newUser = new User();
            newUser.setEmail(body.email());
            newUser.setPassword(passwordEncoder.encode(body.password()));
            newUser.setName(body.name());
            newUser.setRole(Role.USER);
            newUser.setAuthorized(false); // Define authorized como false durante o registro
            this.repository.save(newUser);

            // Retorna um status 201 Created com uma mensagem de sucesso
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Cadastro realizado com sucesso. Aguarde a autorização do administrador.");
        } catch (Exception e) {
            // Retorna uma mensagem de erro genérica para erros inesperados
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ocorreu um erro interno no servidor.");
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody TokenRequest tokenRequest) {
        String token = tokenRequest.getToken(); // Extrai o token do body da requisição

        String email = tokenService.validateToken(token);

        if (email != null) {
            // Retorna status 200 com o e-mail do usuário
            return ResponseEntity.ok(email);
        } else {
            // Retorna status 401 caso a validação falhe
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token inválido ou expirado");
        }
    }

    // Classe para receber a requisição com o token
    public static class TokenRequest {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }


}
