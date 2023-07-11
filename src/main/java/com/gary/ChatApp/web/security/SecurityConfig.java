package com.gary.ChatApp.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.SecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorize -> authorize
                        .requestMatchers("/login").permitAll() // Allow access to login endpoint
                        .anyRequest().authenticated() // Require authentication for other endpoints
                )
                .formLogin(login -> login
                        .loginPage("/login") // Custom login page URL
                        .defaultSuccessUrl("/home") // Default URL after successful login
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login") // URL after successful logout
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                        .maximumSessions(1) // Allow only one session per user
                        .maxSessionsPreventsLogin(true) // Prevent new login if maximum sessions reached
                );
        return http.build();
    }

}
