package com.javacraftacademy.userservice.security;

import com.javacraftacademy.userservice.service.JwtService;
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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.util.*;

import java.io.IOException;

/**
 * Filtre JWT pour l'authentification des requêtes
 * Vérifie la présence et la validité du token JWT dans l'en-tête Authorization
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtService.validateToken(jwt)) {
                String username = jwtService.getUsernameFromToken(jwt);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Cannot set user authentication: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrait le token JWT de l'en-tête Authorization
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
// Ajoutez cette méthode à votre JwtAuthenticationFilter ou créez un nouveau filtre

@Component
public class SuperAdminAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtService.isSuperAdminToken(token) && jwtService.validateToken(token)) {
                // Créer une authentification spéciale pour le super admin
                String email = jwtService.getEmailFromToken(token);
                List<String> permissions = jwtService.getPermissionsFromToken(token);
                
                // Créer les autorités avec tous les permissions
                List<SimpleGrantedAuthority> authorities = permissions.contains("ALL") 
                    ? List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"),
                             new SimpleGrantedAuthority("PERMISSION_ALL"))
                    : permissions.stream()
                        .map(perm -> new SimpleGrantedAuthority("PERMISSION_" + perm))
                        .collect(Collectors.toList());
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}

// Annotation pour contrôler l'accès au super admin
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERMISSION_ALL')")
public @interface RequireSuperAdmin {
}

// Exemple d'utilisation dans un contrôleur
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    
    @GetMapping("/super-admin-only")
    @RequireSuperAdmin
    public ResponseEntity<String> superAdminOnlyEndpoint() {
        return ResponseEntity.ok("Accès réservé au super admin");
    }
    
    @GetMapping("/stats")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        // Statistiques système
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.getTotalUsers());
        stats.put("activeUsers", userService.getActiveUsers());
        stats.put("systemUptime", getSystemUptime());
        return ResponseEntity.ok(stats);
    }
}
