package com.grupo_cordillera.microservicio_kpi.config;

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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitamos CSRF para permitir peticiones POST/PUT de Feign/Postman
                .csrf(csrf -> csrf.disable())

                // 2. Agregamos el acumulador a la lista blanca para que ms-ventas entre directo
                .authorizeHttpRequests(auth -> auth
                        // 🌟 Liberamos la ruta exacta del acumulador
                        .requestMatchers("/api/kpi/acumular", "/api/kpi/acumular/**", "/error","/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/webjars/**").permitAll()
                        // Liberamos las rutas para que el Gateway permita al front consultar definiciones y métricas
                        .requestMatchers("/api/kpi/definiciones/**", "/api/kpi/metricas/**").permitAll()
                        .anyRequest().authenticated()
                )

                // 3. Mantener autenticación básica por si otros componentes la usan
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}