package com.cdwater.cdticket.app.infrastructure.config;

import com.cdwater.cdticket.app.common.Result;
import com.cdwater.cdticket.app.common.ResultCode;
import com.cdwater.cdticket.app.common.util.JwtUtil;
import com.cdwater.cdticket.app.interfaces.auth.JwtAuthenticationFilter;
import com.cdwater.cdticket.app.interfaces.auth.RateLimitFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    public static final String[] PERMIT_ALL_PATHS = {
            "/api/user/auth/sms-code",
            "/api/user/auth/login/sms",
            "/api/user/auth/login/password",
            "/api/user/auth/refresh",
            "/api/user/banners",
            "/api/user/movies",
            "/api/user/movies/**",
            "/api/user/cinemas",
            "/api/user/cinemas/**",
            "/api/user/screenings",
            "/api/user/screenings/**"
    };

    private final JwtUtil jwtUtil;
    private final RateLimitFilter rateLimitFilter;

    /** 取消 Spring Boot 对 Filter bean 的自动注册，避免与 security 链内注册重复执行 */
    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<RateLimitFilter>
            disableRateLimitAutoRegister(RateLimitFilter filter) {
        org.springframework.boot.web.servlet.FilterRegistrationBean<RateLimitFilter> reg =
                new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(this::writeUnauthorized))
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response,
                                   org.springframework.security.core.AuthenticationException e) throws IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(Result.fail(ResultCode.UNAUTHORIZED).toJson());
    }
}
