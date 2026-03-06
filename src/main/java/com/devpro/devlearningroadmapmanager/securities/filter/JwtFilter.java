package com.devpro.devlearningroadmapmanager.securities.filter;

import com.devpro.devlearningroadmapmanager.securities.configuration.JwtUtils;
import com.devpro.devlearningroadmapmanager.securities.service.CustumUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustumUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Extraire le token JWT de la requête
            String jwt = jwtUtils.extractJwtFromRequest(request);

            // 2. VÉRIFIER SI TOKEN EST BLACKLISTÉ
            if (jwt != null && jwtUtils.isTokenBlacklisted(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Session expirée");
                return;
            }


            // 2. Si pas de token, continuer sans authentification
            if (jwt == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 3. Extraire le username du token
            String username = jwtUtils.extractUsername(jwt);

            // 4. Si l'utilisateur n'est pas déjà authentifié
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. Charger les détails de l'utilisateur
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 6. Valider le token
                if (jwtUtils.validateToken(jwt, userDetails)) {

                    // 7. Créer l'objet d'authentification
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 8. Mettre à jour le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Utilisateur authentifié : {}", username);
                }
            }

        } catch (Exception e) {
            log.error("Erreur dans le filtre JWT: {}", e.getMessage());
            // Ne pas bloquer la requête, continuer
        }

        // 9. Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }

    /**
     * Exclure les routes publiques du filtre JWT
     * AJOUTEZ @Override !
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        // Routes qui ne nécessitent PAS de JWT
        return path.startsWith("/api/auth/") ||  // ← TRÈS IMPORTANT
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.startsWith("/favicon.ico") ||
                path.startsWith("/error") ||
                path.equals("/");
    }
}