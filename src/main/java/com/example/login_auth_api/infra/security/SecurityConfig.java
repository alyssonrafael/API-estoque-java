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
                        .requestMatchers(HttpMethod.POST, "/auth/validate-token").permitAll()

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
                        .requestMatchers(HttpMethod.GET, "/categories/deleted").hasAnyRole("USER", "ADMIN")


                        //configuraóes de acesso para rotas de produtos
                        // Protege as rotas de produtos
                        .requestMatchers(HttpMethod.POST, "/products").hasRole("ADMIN") // Criar produto
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("ADMIN") // atualizaçao completa do produto
                        .requestMatchers(HttpMethod.PUT, "/products/update-name/**").hasRole("ADMIN") // Atualizar nome do produto
                        .requestMatchers(HttpMethod.PUT, "/products/delete/**").hasRole("ADMIN") // Excluir produto marcar com deletet true
                        .requestMatchers(HttpMethod.PUT, "/products/restore/**").hasRole("ADMIN") // Restaurar produto marcar com deleted false
                        .requestMatchers(HttpMethod.GET, "/products").hasAnyRole("USER", "ADMIN") // Listar e obter produto permitido pra user e adm
                        .requestMatchers(HttpMethod.GET, "/products/deleted").hasAnyRole("USER", "ADMIN") // Listar e obter produto permitido pra user e adm
                        .requestMatchers(HttpMethod.GET, "/products/count").hasAnyRole("USER", "ADMIN") // Listar e obter produto permitido pra user e adm

                        //configuracoes de acesso pra rotas de vendas
                        .requestMatchers(HttpMethod.POST, "/sales").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/sales").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/sales/last-five-sales").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/sales/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/sales/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/sales/salesByDateRange/**").hasRole("ADMIN")
                        //configuracoes de acesso pra rotas de relatorios
                        .requestMatchers(HttpMethod.GET, "/reports/sales/**").hasRole( "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reports/products/**").hasRole( "ADMIN")
                        //relatorios de numeros
                        .requestMatchers(HttpMethod.GET, "/reports/sales-by-payment-method").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reports/gift-sales-count-month").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reports/sales-count-month").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reports/sales-today").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.GET, "/reports/sales-this-month").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.GET, "/reports/sales-this-year").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reports/total-sales-today").hasAnyRole("ADMIN","USER")
                        .requestMatchers(HttpMethod.GET, "/reports/total-sales-this-month").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reports/total-sales-this-year").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reports/total-sales-by-month-last-six-months").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reports/category-top-today").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reports/category-top-this-month").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reports/category-top-this-year").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reports/sales-last-six-months").hasRole("ADMIN")




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