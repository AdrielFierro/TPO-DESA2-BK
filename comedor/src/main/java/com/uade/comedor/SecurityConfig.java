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
                    "/sessions/**",     // permite tu endpoint de prueba
                    "/actuator/**"      // permite el actuator
                ).permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults()); // deja Basic Auth habilitado para otros endpoints

        return http.build();
    }
}