package com.cdwater.cdticket.admin.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {
    private static final String ROLE_PLATFORM_ADMIN = "PLATFORM_ADMIN";
    private static final String ROLE_CINEMA_ADMIN = "CINEMA_ADMIN";

    private final TokenStoreService tokenStore;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            TokenAuthenticationFilter.AdminContext info = tokenStore.resolve(token);
            if (info != null) {
                SimpleGrantedAuthority authority = info.isPlatformAdmin()
                        ? new SimpleGrantedAuthority(ROLE_PLATFORM_ADMIN)
                        : new SimpleGrantedAuthority(ROLE_CINEMA_ADMIN);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        info.adminId(), null, List.of(authority));
                auth.setDetails(info);
                SecurityContextHolder.getContext().setAuthentication(auth);
                tokenStore.renew(token, info.adminId());
            }
        }
        chain.doFilter(request, response);
    }

    public record AdminContext(long adminId, int role, long cinemaId) {
        public boolean isPlatformAdmin() {
            return role == 0;
        }
    }
}
