package com.example.demo.config;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct; // Import pour @PostConstruct
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value; // Import pour @Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService; // Optionnel
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret.key}") // Injection de la clé depuis application.properties
    private String jwtSecretString;

    private SecretKey jwtSecretKey; // La SecretKey sera initialisée dans init()

    @PostConstruct // Exécuté après l'injection des dépendances
    public void init() {
        // Assurez-vous que l'algorithme ici correspond à celui utilisé pour signer le token dans AuthService
        this.jwtSecretKey = new SecretKeySpec(
                jwtSecretString.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256" // Doit correspondre à SignatureAlgorithm.HS256.getJcaName()
        );
    }

    private final UserRepository userRepository;
    // Si vous avez un UserDetailsService personnalisé :
    // private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = parseJwt(request);
            if (jwt != null && validateJwtToken(jwt)) {
                String email = getUserNameFromJwtToken(jwt);

                // Charger l'utilisateur depuis la base de données.
                // Dans un cas plus complexe, on utiliserait UserDetailsService.
                User userEntity = userRepository.findByEmail(email).orElse(null);

                if (userEntity != null && userEntity.isVerified()) {
                    // Créer UserDetails. Pour les rôles/autorités, adapter selon votre modèle User.
                    // Le mot de passe de userEntity.getPassword() n'est pas crucial ici car l'authentification est via token,
                    // mais l'interface UserDetails le requiert souvent.
                    UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                            userEntity.getEmail(),
                            userEntity.getPassword(), // Peut être une chaîne vide si non pertinent pour l'auth par token
                            new ArrayList<>() // TODO: Remplacer par les vraies autorités/rôles de l'utilisateur
                    );

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // Utiliser logger.error pour les exceptions ici, pas e.printStackTrace() en production
            logger.error("Cannot set user authentication: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7); // Extrait le token après "Bearer "
        }
        return null;
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(this.jwtSecretKey).build().parseClaimsJws(authToken); // Utilisation de la clé initialisée
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public String getUserNameFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(this.jwtSecretKey) // Utilisation de la clé initialisée
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject(); // Le sujet devrait être l'email de l'utilisateur
    }
}
