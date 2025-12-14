package com.uade.comedor;

import java.util.List;

import com.uade.comedor.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // desactiva CSRF para APIs REST
            .cors(Customizer.withDefaults()) // habilita CORS usando el bean corsConfigurationSource
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (sin autenticación)
                .requestMatchers(
                    "/actuator/health/**",
                    "/actuator/info",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/static/openapi.yaml"
                ).permitAll()
                
                // Endpoints que requieren SUBROL_CAJERO
                .requestMatchers(
                    "/reservations/confirmation/**",
                    "/carts/confirmation/**"
                ).hasAuthority("SUBROL_CAJERO")
                
                // Endpoints que requieren SUBROL_CHEF (productos y edición de menús)
                .requestMatchers("/products/**").hasAuthority("SUBROL_CHEF")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/menus/**").hasAuthority("SUBROL_CHEF")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/menus/**").hasAuthority("SUBROL_CHEF")
                .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/menus/**").hasAuthority("SUBROL_CHEF")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/menus/**").hasAuthority("SUBROL_CHEF")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/meal-schedules/**").hasAuthority("SUBROL_CHEF")
                
                // Resto de endpoints solo requieren autenticación (sin importar subrol)
                // Incluye: crear reservas, ver menús, locations, carts (crear/ver/editar/eliminar)
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(httpBasic -> httpBasic.disable()) // Deshabilita HTTP Basic
            .formLogin(formLogin -> formLogin.disable()) // Deshabilita form login
            .oauth2ResourceServer(oauth2 -> oauth2.disable()); // Deshabilita OAuth2 Resource Server

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "https://proyecto-react-shadcn.vercel.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

