package com.example.entrevista.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.entrevista.util.JwtUtil;
import com.example.entrevista.security.UserPrincipal;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = Logger.getLogger(JwtAuthenticationFilter.class.getName());

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        logger.info("=== JWT FILTER DEBUG ===");
        logger.info("Request to: " + request.getRequestURI());
        logger.info("Authorization header: " + (header != null ? "Present" : "Missing"));
        
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                Long userId = jwtUtil.extractUserId(token);
                String nombre = jwtUtil.extractNombre(token);
                String apellidoPaterno = jwtUtil.extractApellidoPaterno(token);
                String apellidoMaterno = jwtUtil.extractApellidoMaterno(token);
                String userType = jwtUtil.extractUserType(token);
                
                logger.info("JWT Token username: " + username);
                logger.info("JWT Token role: " + role);
                logger.info("JWT Token userId: " + userId);
                logger.info("JWT Token nombre: " + nombre);
                logger.info("JWT Token userType: " + userType);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Important: Add ROLE_ prefix for Spring Security compatibility
                    String authorityName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                    SimpleGrantedAuthority authority = new SimpleGrantedAuthority(authorityName);
                    logger.info("Created authority: " + authority.getAuthority());
                    
                    // Create a custom authentication principal that includes user info
                    UserPrincipal userPrincipal = new UserPrincipal(username, userId, nombre, apellidoPaterno, apellidoMaterno, userType);
                    
                    // Create authentication with the role properly formatted
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userPrincipal, null, Collections.singletonList(authority));
                    
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    logger.info("Authentication set in SecurityContext for: " + username);
                    logger.info("Authorities in context: " + auth.getAuthorities());
                }
            } catch (Exception e) {
                logger.severe("JWT Token processing error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            logger.info("No Authorization header found or invalid format");
        }
        
        // Log current authentication before continuing
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            logger.info("Current authentication: " + SecurityContextHolder.getContext().getAuthentication().getName());
            logger.info("Current authorities: " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        } else {
            logger.info("No authentication in SecurityContext");
        }
        
        filterChain.doFilter(request, response);
    }
}
