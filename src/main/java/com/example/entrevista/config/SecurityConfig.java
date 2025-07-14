package com.example.entrevista.config;

import com.example.entrevista.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Usar allowedOriginPatterns en lugar de allowedOrigins cuando allowCredentials=true
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "https://localhost:*", 
            "http://127.0.0.1:*",
            "https://127.0.0.1:*"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/auth/**").permitAll()
                
                // Empresas - Gestión de convocatorias y candidatos
                .requestMatchers(HttpMethod.POST, "/api/convocatorias/**").hasRole("EMPRESA")
                .requestMatchers(HttpMethod.PUT, "/api/convocatorias/**").hasRole("EMPRESA")
                .requestMatchers(HttpMethod.DELETE, "/api/convocatorias/**").hasRole("EMPRESA")
                .requestMatchers(HttpMethod.GET, "/api/convocatorias/empresa/**").hasRole("EMPRESA")
                .requestMatchers(HttpMethod.GET, "/api/postulaciones/convocatoria/**").hasRole("EMPRESA")
                .requestMatchers(HttpMethod.PATCH, "/api/postulaciones/*/estado").hasRole("EMPRESA")
                .requestMatchers(HttpMethod.GET, "/api/evaluaciones/postulacion/**").hasRole("EMPRESA")
                .requestMatchers(HttpMethod.GET, "/api/evaluaciones/por-entrevista/**").hasRole("EMPRESA")
                
                // Usuarios - Postulaciones y entrevistas
                .requestMatchers(HttpMethod.GET, "/api/convocatorias/activas").hasRole("USUARIO")
                .requestMatchers(HttpMethod.POST, "/api/postulaciones").hasRole("USUARIO")
                .requestMatchers(HttpMethod.GET, "/api/postulaciones/usuario/**").hasAnyRole("USUARIO", "EMPRESA")
                .requestMatchers(HttpMethod.GET, "/api/postulaciones/mis-postulaciones").hasRole("USUARIO")
                .requestMatchers(HttpMethod.PATCH, "/api/postulaciones/*/iniciar-entrevista").hasRole("USUARIO")
                .requestMatchers(HttpMethod.PATCH, "/api/postulaciones/*/completar-entrevista").hasRole("USUARIO")
                .requestMatchers(HttpMethod.POST, "/api/preguntas/generar").hasRole("USUARIO")
                .requestMatchers(HttpMethod.GET, "/api/preguntas/postulacion/**").hasRole("USUARIO")
                .requestMatchers(HttpMethod.POST, "/api/evaluaciones/evaluar").hasRole("USUARIO")
                .requestMatchers(HttpMethod.GET, "/api/evaluaciones/mis-resultados/**").hasRole("USUARIO")
                .requestMatchers(HttpMethod.PATCH, "/api/postulaciones/*/marcar-preguntas-generadas").hasRole("USUARIO")
                
                // Ambos roles pueden ver detalles específicos
                .requestMatchers(HttpMethod.GET, "/api/convocatorias/*").hasAnyRole("USUARIO", "EMPRESA")
                .requestMatchers(HttpMethod.GET, "/api/postulaciones/*").hasAnyRole("USUARIO", "EMPRESA")
                .requestMatchers(HttpMethod.GET, "/api/empresas/*").hasAnyRole("USUARIO", "EMPRESA")
                
                // Registro público
                .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/empresas").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/usuarios/email/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/empresas/email/**").permitAll()
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
