package com.example.login_auth_api.infra.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.login_auth_api.domain.user.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            return JWT.create()
                    .withIssuer("login-auth-api")
                    .withSubject(user.getEmail())
                    .withClaim("role", user.getRole().name())
                    .withClaim("name", user.getName())
                    .withClaim("userId", user.getId())
                    .withExpiresAt(this.generateExpirationDate())
                    .sign(algorithm);

        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao criar o token", exception);
        }
    }

    // Retorna o e-mail do usuário se o token for válido, senão retorna null
    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            var decodedJWT = JWT.require(algorithm)
                    .withIssuer("login-auth-api")
                    .build()
                    .verify(token);

            return decodedJWT.getSubject(); // Retorna o email do usuário

        } catch (JWTVerificationException exception) {
            return null;
        }
    }

    // Retorna um objeto com todas as informações do token
    public TokenValidationResponse getTokenValidationResponse(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            var decodedJWT = JWT.require(algorithm)
                    .withIssuer("login-auth-api")
                    .build()
                    .verify(token);

            return new TokenValidationResponse(true,
                    decodedJWT.getSubject(),
                    decodedJWT.getClaim("role").asString(),
                    decodedJWT.getClaim("name").asString(),
                    decodedJWT.getClaim("userId").asString());

        } catch (JWTVerificationException exception) {
            return new TokenValidationResponse(false, null, null, null, null);
        }
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(8).toInstant(ZoneOffset.of("-03:00"));
    }

    public static class TokenValidationResponse {
        private boolean valid;
        private String email;
        private String role;
        private String name;
        private String userId;

        public TokenValidationResponse(boolean valid, String email, String role, String name, String userId) {
            this.valid = valid;
            this.email = email;
            this.role = role;
            this.name = name;
            this.userId = userId;
        }

        public boolean isValid() {
            return valid;
        }

        public String getEmail() {
            return email;
        }

        public String getRole() {
            return role;
        }

        public String getName() {
            return name;
        }

        public String getUserId() {
            return userId;
        }
    }
}
