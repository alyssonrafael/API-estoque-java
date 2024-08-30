package com.example.login_auth_api.infra.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        //rotas de registro e login permitida para todos
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()

                        //Rotas de usuario
                        //Restrições específicas para administradores
                        .requestMatchers(HttpMethod.PUT, "/user/changerole/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/user/authorize/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")
                        //Acesso liberado para ambos USER e ADMIN
                        .requestMatchers(HttpMethod.GET, "/user/**").hasAnyRole("USER", "ADMIN")

                        //Configurações de acesso para rotas de categoria
                        // Apenas administradores podem modificar categorias
                        .requestMatchers(HttpMethod.PUT, "/categories/delete/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/categories/restore/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/categories/update-name/**").hasRole("ADMIN")
                        // Acesso liberado para ambos USER e ADMIN para listagem
                        .requestMatchers(HttpMethod.POST, "/categories").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.GET, "/categories").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/categories/**").hasAnyRole("USER", "ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}