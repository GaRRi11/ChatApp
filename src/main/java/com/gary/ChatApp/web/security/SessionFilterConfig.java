package com.gary.ChatApp.web.security;

import com.gary.ChatApp.domain.repository.UserRepository;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SessionFilterConfig {

    private final SessionManager sessionManager;
    private final UserRepository userRepository;

    @Bean
    public FilterRegistrationBean<Filter> filterRegistrationBean(){
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new SessionAuthenticationFilter(sessionManager,userRepository));
        filterRegistrationBean.addUrlPatterns("/chat/**");
        return filterRegistrationBean;
    }
}
