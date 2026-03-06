package com.devpro.devlearningroadmapmanager.securities.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtils {

    // le @Value permet de récupérer une valeur définie dans l'app.properties
    @Value("${jwt.secret.key}")
    private String secretKey;

    @Value("${jwt.expiration.time}")
    private long expirationTime;

    // Blacklist simplifiée (pour développement)
    private final Set<String> tokenBlacklist = new HashSet<>();

    // ====================================================================
    // MÉTHODE PRINCIPALE : GÉNÉRATION DE TOKEN
    // ====================================================================

    /**
     * GÉNÈRE UN TOKEN JWT POUR UN UTILISATEUR

     * Version simple sans informations supplémentaires.
     */
    public String generateToken(Map<String, Object> claims, String username) {
        return createToken(claims, username);
    }

    /**
     * GÉNÈRE UN TOKEN AVEC DES CLAIMS PERSONNALISÉS
     */
    public String generateToken(String username, Map<String, Object> claims) {
        return createToken(claims, username);
    }

    // ====================================================================
    // MÉTHODE CŒUR : CRÉATION DU TOKEN (API MODERNE)
    // ====================================================================

    /**
     * CRÉE UN TOKEN JWT AVEC L'API MODERNE (non-dépréciée)

     * Nouvelle API depuis JJWT 0.12.x :
     * - builder() → JwtBuilder
     * - signWith() prend une SecretKey directement
     * - Pas besoin de spécifier l'algorithme (détecté automatiquement)
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)                    // Nouvelle API pour le sujet
                .issuedAt(now)                       // Date d'émission
                .expiration(expiration)              // Date d'expiration
                .signWith(getSignKey(), Jwts.SIG.HS384)              // Signature avec clé (algorithme auto-détecté) cette cle permet de chiffre et de déchiffrer les infos d'un user
                .compact();                          // Génération finale du string
    }

    // ====================================================================
    // MÉTHODE DE SIGNATURE : CLÉ SECRÈTE
    // ====================================================================

    /**
     * OBTIENT LA CLÉ DE SIGNATURE

     * Utilise Keys.hmacShaKeyFor() pour créer une SecretKey
     * compatible avec HMAC-SHA.
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ====================================================================
    // MÉTHODE DE VALIDATION
    // ====================================================================

    /**
     * VALIDE SI UN TOKEN EST CORRECT POUR UN UTILISATEUR

     * Vérifie trois choses :
     * 1. Username correspond
     * 2. Le token n'est pas expiré
     * 3. La signature est valide.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // ====================================================================
    // MÉTHODES D'EXTRACTION D'INFORMATIONS
    // ====================================================================

    /**
     * EXTRAIT LE USERNAME D'UN TOKEN
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * EXTRAIT UNE CLAIM SPÉCIFIQUE DU TOKEN

     * Méthode générique utilisant une Function.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * EXTRAIT TOUTES LES CLAIMS DU TOKEN

     * Utilise la nouvelle API Jwts.parser() avec verifyWith()
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())      // Vérification signature
                .build()
                .parseSignedClaims(token)       // Parse le token signé
                .getPayload();                  // Retourne le payload (claims)
    }

    // ====================================================================
    // MÉTHODES DE VÉRIFICATION D'EXPIRATION
    // ====================================================================

    /**
     * VÉRIFIE SI LE TOKEN EST EXPIRÉ
     */
    public Boolean isTokenExpired(String token) {

        return extractExpiration(token).before(new Date());

    }

    /**
     * EXTRAIT LA DATE D'EXPIRATION
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String logout(String token){
        // 1. Vérifier token
        if (token == null || token.isEmpty()) {
            return "Token manquant";
        }

        // 2. Enlever "Bearer "
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 4. Récupérer l'utilisateur
        String username = extractUsername(token);

        // 5. Ajouter à la blacklist
        tokenBlacklist.add(token);

        // 6. Nettoyer le contexte
        SecurityContextHolder.clearContext();

        return username;
    }

    // Méthode pour vérifier si token est blacklisté
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklist.contains(token);
    }

    /**
     * Extrait le token JWT du header Authorization
     * CORRECTION : Vérifie si le header existe avant de faire substring
     */
    public String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // CORRECTION ICI : Vérifiez d'abord si le header existe et est valide
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Retirer "Bearer "
        }

        return null;
    }
}