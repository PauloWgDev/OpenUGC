package com.diversestudio.unityapi.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import java.io.IOException;
import java.util.Collection;


public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {


        // Skip token checking for auth endpoints
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }


        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            try {
                Long userId = jwtUtil.extractUserId(token);
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // Extract the authorities from the token
                    Collection<? extends GrantedAuthority> authorities = jwtUtil.extractAuthorities(token);

                    // Create an authentication token with empty authorities for now.
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userId.toString(), null, authorities);

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (JwtException e) {
                // Invalid token, you can log or handle the exception accordingly.
                // Optionally, you can clear the SecurityContext.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
