package com.devpro.devlearningroadmapmanager.securities.configuration;

import com.devpro.devlearningroadmapmanager.securities.filter.JwtFilter;
import com.devpro.devlearningroadmapmanager.securities.service.CustumUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustumUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;

    // le SecurityFilterChain permet de définir la configuration des filtres de securite
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                // cette methode permet de définir les routes autorisées et les routes non-autoriser
                // le requestMatchers("/login", "/register").permitAll() permet de dire que le chemin "/login", "/register"
                // sont autorisé (c'est-à-dire qu'on n'a pas besoin identification ou d'autre securite pour acceder a ces routes.)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/journal-manager/download-pdf").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // le anyRequest().authenticated() permet de dire que pour toute autre requête, il faut être authentifié
                        .anyRequest().authenticated()

                )
                // Ce code permet le verification du token envoyé
                // Pourquoi before et pas after ?
        //        REQUÊTE ENTRE →
        //        1. [VOTRE JwtFilter] → Vérifie token JWT
        //        ↓ Si token valide → Utilisateur authentifié → SAUTE les étapes 2-3
        //         ↓ Si pas de token → Continue
        //
        //        2. [UsernamePasswordAuthenticationFilter] → Vérifie formulaire login
        //        ↓ Si login valide → Crée session
        //        ↓ Si pas de login → Continue
        //
        //        3. [Autres filtres de sécurité]
        //        ↓
        //        4. Votre Controller
                .addFilterBefore(new JwtFilter( jwtUtils, userDetailsService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200",
                "educationpriorite.up.railway.app"
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    //cette methode permet d'encoder le mot de passe avant de la stocker dans la bd
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //AuthenticationManager de verifier si un utilisateur est bien enregistré dans la base de donne et si ces donnes correspondent
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity httpSecurity, PasswordEncoder passwordEncoder) throws Exception {
        //AuthenticationManagerBuilder permet d'instancier le AuthenticationManager
        AuthenticationManagerBuilder authenticationManagerBuilder = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
        // cette methode permet de verifier si les donnes de l'utilisateur correspond avec celle de la bd
        // et le passwordEncoder(passwordEncoder) permet de verifier que le mot de passe envoyé correspond au mot de passe encoder dans la bd
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }
}