package com.dailycodework.lakesidehotel.security;

import com.dailycodework.lakesidehotel.security.jwt.AuthTokenFilter;
import com.dailycodework.lakesidehotel.security.jwt.JwtAuthEntryPoint;
import com.dailycodework.lakesidehotel.security.user.HotelUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Simpson Alfred
 */
@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class WebSecurityConfig {
    private final HotelUserDetailsService userDetailsService;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;

    @Bean
    public AuthTokenFilter authenticationTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(
                        exception -> exception.authenticationEntryPoint(jwtAuthEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/rooms/room/types", "/rooms/all-rooms", "/rooms/room/{roomId}",
                                "/rooms/available-rooms")
                        .permitAll()
                        .requestMatchers("/bookings/room/{roomId}/booking", "/bookings/confirmation/{confirmationCode}")
                        .permitAll()
                        .requestMatchers("/rooms/add/new-room", "/rooms/delete/room/{roomId}", "/rooms/update/{roomId}",
                                "/rooms/test-auth")
                        .hasRole("ADMIN")
                        .requestMatchers("/bookings/all-bookings").hasRole("ADMIN")
                        .requestMatchers("/bookings/user/{email}/bookings", "/bookings/booking/{bookingId}/delete")
                        .authenticated()
                        .requestMatchers("/users/all").hasRole("ADMIN")
                        .requestMatchers("/users/{email}", "/users/delete/{userId}").authenticated()
                        .requestMatchers("/roles/**").hasRole("ADMIN")
                        .anyRequest().authenticated());
        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(authenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
