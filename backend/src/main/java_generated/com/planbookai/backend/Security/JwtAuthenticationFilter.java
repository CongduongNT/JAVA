package com.planbookai.backend.Security;

import com.planbookai.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // ==========================
        // ✅ SKIP SWAGGER + PUBLIC
        // ==========================
        if (
                path.startsWith("/api-docs") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/api/v1/auth") ||
                path.equals("/") ||
                path.startsWith("/error")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        // ==========================
        // ✅ GET TOKEN
        // ==========================
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        // ==========================
        // ✅ VALIDATE TOKEN
        // ==========================
        if (!jwtTokenProvider.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtTokenProvider.getEmailFromToken(token);

        // ==========================
        // ✅ SET AUTH CONTEXT
        // ==========================
        userRepository.findByEmail(email).ifPresent(user -> {

            List<SimpleGrantedAuthority> authorities =
                    user.getRole() != null
                            ? List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_" + user.getRole().getName().name()
                                )
                              )
                            : List.of();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            authorities
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        });

        filterChain.doFilter(request, response);
    }
}