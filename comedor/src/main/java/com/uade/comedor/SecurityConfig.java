package com.uade.comedor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // desactiva CSRF para APIs REST
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/ext/ping",       // permite el endpoint de prueba
                              "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui.html",
                    "/sessions/**", // permite tu endpoint de prueba
                    "/products/**", // permite tus endpoints de productos sin autenticación
                    "/reservations/**", // permite tus endpoints de reservas sin autenticación
                    "/actuator/**"      // permite el actuator
                ).permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults()); // deja Basic Auth habilitado para otros endpoints

        return http.build();
    }
}