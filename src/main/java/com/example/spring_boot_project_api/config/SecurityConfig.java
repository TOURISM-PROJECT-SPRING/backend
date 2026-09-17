package com.example.spring_boot_project_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

import com.example.spring_boot_project_api.security.AppUserDetailsService;
import com.example.spring_boot_project_api.security.JwtAuthenticationFilter; 
import com.example.spring_boot_project_api.security.JwtService;
import com.example.spring_boot_project_api.security.RestAccessDeniedHandler;
import com.example.spring_boot_project_api.security.RestAuthenticationEntryPoint;
import com.example.spring_boot_project_api.security.TokenBlacklistService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final AppUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtService jwtService,
                          AppUserDetailsService userDetailsService,
                          TokenBlacklistService tokenBlacklistService,
                          RestAuthenticationEntryPoint authenticationEntryPoint,
                          RestAccessDeniedHandler accessDeniedHandler) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // public: authentication flow, API docs, payment callbacks,
                        // contact submission, newsletter subscription
                        .requestMatchers("/api/auth/register", "/api/auth/login",
                                "/api/auth/forgot-password", "/api/auth/reset-password",
                                "/swagger-ui.html", "/swagger-ui/**",
                                "/api-docs/**", "/v3/api-docs/**",
                                "/api/payments/callback", "/api/payments/callback-form",
                                "/api/v1/bakong/**",
                                "/ws-tourism/**", "/api/bookings/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/newsletter/subscribe").permitAll()

                        // ADMIN: admin dashboards (stats + global bookings feed)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // OWNER + ADMIN: owner dashboard
                        .requestMatchers("/api/owner/**").hasAnyRole("ADMIN", "OWNER")

                        // ADMIN: admin module + admin-only data + global categories
                        .requestMatchers("/api/management/**").hasRole("ADMIN")
                        .requestMatchers("/api/contact/**", "/api/newsletter/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/place-categories/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/place-categories/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/place-categories/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/food-orders",
                                "/api/food-orders/status/**",
                                "/api/room-bookings")
                        .hasAnyRole("ADMIN", "OWNER")

                        // OWNER: business management
                        .requestMatchers(HttpMethod.POST,
                                "/api/hotels/**", "/api/hotel-rooms/**",
                                "/api/room-types/**", "/api/rooms/**",
                                "/api/restaurants/**", "/api/foods/**",
                                "/api/food-categories/**", "/api/promotions/**",
                                "/api/tour-places/**")
                        .hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/hotels/**", "/api/hotel-rooms/**",
                                "/api/room-types/**", "/api/rooms/**",
                                "/api/restaurants/**", "/api/foods/**",
                                "/api/food-categories/**", "/api/promotions/**",
                                "/api/tour-places/**")
                        .hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/hotels/**", "/api/hotel-rooms/**",
                                "/api/room-types/**", "/api/rooms/**",
                                "/api/restaurants/**", "/api/foods/**",
                                "/api/food-categories/**", "/api/promotions/**",
                                "/api/tour-places/**")
                        .hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.POST, "/api/tour-place-attachments/**",
                                "/api/hotels/*/attachments", "/api/rooms/*/attachments",
                                "/api/foods/*/attachments", "/api/restaurants/*/attachments")
                        .hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/tour-place-attachments/**",
                                "/api/hotels/*/attachments", "/api/rooms/*/attachments",
                                "/api/foods/*/attachments", "/api/restaurants/*/attachments")
                        .hasAnyRole("ADMIN", "OWNER")

                        .requestMatchers(HttpMethod.POST, "/api/tickets/**")
                        .hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/tickets/**")
                        .hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.DELETE, "/api/tickets/**")
                        .hasAnyRole("ADMIN", "OWNER")

                        // OWNER: restaurant order management + ticket staff actions
                        .requestMatchers(HttpMethod.GET,
                                "/api/food-orders/restaurant/**")
                        .hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/food-orders/*/status")
                        .hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.POST,
                                "/api/ticket-bookings/verify", "/api/ticket-bookings/*/use")
                        .hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.GET,
                                "/api/ticket-bookings/status/**", "/api/ticket-bookings/date",
                                "/api/ticket-bookings/ticket/**",
                                "/api/ticket-bookings",
                                "/api/room-bookings/status/**", "/api/room-bookings/room/**")
                        .hasAnyRole("ADMIN", "OWNER")
                        .requestMatchers(HttpMethod.PUT, "/api/room-bookings/*")
                        .hasAnyRole("ADMIN", "OWNER")

                        // TOURIST: own reads + booking/commerce actions
                        .requestMatchers(HttpMethod.GET,
                                "/api/food-orders/user/**", "/api/food-orders/{id}",
                                "/api/ticket-bookings/user/**", "/api/ticket-bookings/{id}",
                                "/api/ticket-bookings/*/eticket",
                                "/api/room-bookings/user/**", "/api/room-bookings/{id}")
                        .hasAnyRole("ADMIN", "TOURIST")
                        .requestMatchers(HttpMethod.POST,
                                "/api/food-orders/**",
                                "/api/ticket-bookings",
                                "/api/ticket-bookings/*/cancel",
                                "/api/ticket-bookings/*/payment",
                                "/api/room-bookings", "/api/room-bookings/*/cancel")
                        .hasAnyRole("ADMIN", "TOURIST")
                        .requestMatchers("/api/carts/**")
                        .hasAnyRole("ADMIN", "TOURIST")

                        // any authenticated user: profile images + auth-only endpoints
                        .requestMatchers(HttpMethod.POST, "/api/users/*/attachments")
                        .authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*/attachments")
                        .authenticated()
                        .requestMatchers("/api/auth/change-password", "/api/auth/logout")
                        .authenticated()
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/food-orders/*", "/api/ticket-bookings/*",
                                "/api/room-bookings/*")
                        .hasRole("ADMIN")

                        // everything else stays open for browsing
                        .anyRequest().permitAll())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(new JwtAuthenticationFilter(
                        jwtService, userDetailsService, tokenBlacklistService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}