package com.example.spring_boot_project_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // បិទ CSRF ជាបណ្ដោះអាសន្នដើម្បីឱ្យងាយស្រួល Test
            .cors(withDefaults())        // បើកសិទ្ធិ CORS
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // អនុញ្ញាតគ្រប់ Request ទាំងអស់
            
        return http.build();
    }
}
