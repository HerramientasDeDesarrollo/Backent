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
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
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
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/auth/**").permitAll()
                
                // Empresas - Gestión de convocatorias y candidatos
                .requestMatchers(HttpMethod.POST, "/api/convocatorias/**").hasAuthority("ROLE_EMPRESA")
                .requestMatchers(HttpMethod.PUT, "/api/convocatorias/**").hasAuthority("ROLE_EMPRESA")
                .requestMatchers(HttpMethod.DELETE, "/api/convocatorias/**").hasAuthority("ROLE_EMPRESA")
                .requestMatchers(HttpMethod.GET, "/api/convocatorias/empresa/**").hasAuthority("ROLE_EMPRESA")
                .requestMatchers(HttpMethod.GET, "/api/postulaciones/convocatoria/**").hasAuthority("ROLE_EMPRESA")
                .requestMatchers(HttpMethod.PATCH, "/api/postulaciones/*/estado").hasAuthority("ROLE_EMPRESA")
                .requestMatchers(HttpMethod.GET, "/api/evaluaciones/postulacion/**").hasAuthority("ROLE_EMPRESA")
                .requestMatchers(HttpMethod.GET, "/api/evaluaciones/por-entrevista/**").hasAuthority("ROLE_EMPRESA")
                
                // Usuarios - Postulaciones y entrevistas
                .requestMatchers(HttpMethod.GET, "/api/convocatorias/activas").hasAuthority("ROLE_USUARIO")
                .requestMatchers(HttpMethod.POST, "/api/postulaciones").hasAuthority("ROLE_USUARIO")
                .requestMatchers(HttpMethod.GET, "/api/postulaciones/usuario/**").hasAuthority("ROLE_USUARIO")
                .requestMatchers(HttpMethod.POST, "/api/preguntas/generar").hasAuthority("ROLE_USUARIO")
                .requestMatchers(HttpMethod.GET, "/api/preguntas/postulacion/**").hasAuthority("ROLE_USUARIO")
                .requestMatchers(HttpMethod.POST, "/api/evaluaciones/evaluar").hasAuthority("ROLE_USUARIO")
                .requestMatchers(HttpMethod.GET, "/api/evaluaciones/mis-resultados/**").hasAuthority("ROLE_USUARIO")
                
                // Ambos roles pueden ver detalles específicos
                .requestMatchers(HttpMethod.GET, "/api/convocatorias/*").hasAnyAuthority("ROLE_USUARIO", "ROLE_EMPRESA")
                .requestMatchers(HttpMethod.GET, "/api/postulaciones/*").hasAnyAuthority("ROLE_USUARIO", "ROLE_EMPRESA")
                .requestMatchers(HttpMethod.GET, "/api/empresas/*").hasAnyAuthority("ROLE_USUARIO", "ROLE_EMPRESA")
                
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
