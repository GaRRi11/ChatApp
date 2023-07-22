//package com.gary.ChatApp.web.security;
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.core.RedisOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.session.security.SpringSessionBackedSessionRegistry;
//import org.springframework.session.data.redis.RedisSessionRepository;
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(securedEnabled = true)
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//}
//
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
////
////        http
////                .authorizeRequests()
////                .requestMatchers("/auth/register").permitAll()
////                .anyRequest().authenticated();
////
////        http
////                .authorizeRequests()
////                .requestMatchers("/auth/register").permitAll()
////                .anyRequest().authenticated()
////                .and()
////                .formLogin()
////                .loginPage("/login")
////                .defaultSuccessUrl("/home")
////                .and()
////                .logout()
////                .logoutSuccessUrl("/login")
////                .and()
////                .sessionManagement()
////                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
////                .maximumSessions(1)
////                .maxSessionsPreventsLogin(true)
////                .sessionRegistry(sessionRegistry());
////
////
////
////        return http.build();
//
//
//
//////        http
//////                .authorizeRequests(authorize -> authorize
//////                        .requestMatchers("/auth/**").permitAll() // Allow access to login endpoint
//////                        .anyRequest().authenticated() // Require authentication for other endpoints
//////                )
//        http
//                .authorizeRequests(authorize -> authorize
//                        .requestMatchers("/auth/register").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .formLogin(login -> login
//                        .loginPage("/auth/login") // Custom login page URL
//                        .defaultSuccessUrl("/chat") // Default URL after successful login
//                )
//                .logout(logout -> logout
//                        .logoutSuccessUrl("/auth/login") // URL after successful logout
//                )
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
//                        .maximumSessions(1) // Allow only one session per user
//                        .maxSessionsPreventsLogin(true) // Prevent new login if maximum sessions reached
//                );
//        return http.build();
//    }
//
//    @Bean
//    public SpringSessionBackedSessionRegistry sessionRegistry() {
//        return new SpringSessionBackedSessionRegistry<>(sessionRepository());
//    }
//
//    @Bean
//    public SessionRepository<ExpiringSession> sessionRepository() {
//        return new RedisOperationsSessionRepository(redisTemplate);
//    }
//
//}
