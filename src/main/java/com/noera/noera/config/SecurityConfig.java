package com.noera.noera.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public UserDetailsService fasf(){
        UserDetails admin = User.withDefaultPasswordEncoder()
        .username("admin")
        .password("1234")
        .roles("ADMIN")
        .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain filter(HttpSecurity http) throws Exception{
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/admin/**")
        .hasRole("ADMIN").anyRequest().permitAll())
        .formLogin(form -> form.defaultSuccessUrl("/admin/products", true).permitAll())
        
        .logout(Customizer.withDefaults());
        return http.csrf().disable().build();
    }
}
