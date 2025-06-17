
package com.javacraftacademy.userservice.exception;

/**
 * Exception levée lors d'échecs d'authentification dus à des identifiants invalides.
 * 
 * <p>Cette exception de sécurité est cruciale pour le système d'authentification
 * de l'application. Elle est levée lorsque les identifiants fournis (email/mot de passe,
 * tokens, etc.) ne correspondent à aucun utilisateur valide ou sont incorrects.
 * Elle joue un rôle essentiel dans la sécurisation de l'application en empêchant
 * les accès non autorisés.</p>
 * 
 * <h3>Contextes d'utilisation :</h3>
 * <ul>
 *   <li>Tentative de connexion avec email/mot de passe incorrects</li>
 *   <li>Utilisation d'un token JWT invalide ou corrompu</li>
 *   <li>Tentative d'accès avec un token expiré (bien que TokenExpiredException soit plus spécifique)</li>
 *   <li>Échec de validation de refresh token</li>
 *   <li>Tentative d'utilisation d'identifiants temporaires expirés</li>
 * </ul>
 * 
 * <h3>Relations avec l'application :</h3>
 * <ul>
 *   <li><strong>AuthService</strong> : Principale source de cette exception lors des processus de login</li>
 *   <li><strong>JwtService</strong> : Levée lors de la validation de tokens JWT invalides</li>
 *   <li><strong>CustomUserDetailsService</strong> : Peut lever cette exception lors du chargement des détails utilisateur</li>
 *   <li><strong>JwtAuthenticationFilter</strong> : Gestion des erreurs d'authentification JWT</li>
 *   <li><strong>AuthController</strong> : Point d'entrée où cette exception est souvent interceptée</li>
 *   <li><strong>GlobalExceptionHandler</strong> : Transformée en réponse HTTP 401 (Unauthorized)</li>
 *   <li><strong>SecurityConfig</strong> : Configuration de la gestion des erreurs d'authentification</li>
 * </ul>
 * 
 * <h3>Considérations de sécurité :</h3>
 * <ul>
 *   <li><strong>Messages génériques</strong> : Les messages d'erreur ne doivent pas révéler si l'email existe</li>
 *   <li><strong>Protection contre l'énumération</strong> : Éviter de donner des indices sur les comptes existants</li>
 *   <li><strong>Logging sécurisé</strong> : Enregistrer les tentatives pour détecter les attaques</li>
 *   <li><strong>Rate limiting</strong> : Cette exception peut déclencher des mécanismes anti-brute force</li>
 * </ul>
 * 
 * <h3>Flux d'authentification typique :</h3>
 * <pre>
 * 1. Utilisateur soumet identifiants de connexion
 * 2. AuthService valide les identifiants
 * 3. Si invalides : lancement de InvalidCredentialsException
 * 4. GlobalExceptionHandler intercepte et retourne HTTP 401
 * 5. Logging de la tentative de connexion échouée
 * 6. Client reçoit un message d'erreur générique
 * </pre>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 * @see com.javacraftacademy.userservice.service.AuthService
 * @see com.javacraftacademy.userservice.service.JwtService
 * @see com.javacraftacademy.userservice.security.CustomUserDetailsService
 * @see com.javacraftacademy.userservice.exception.GlobalExceptionHandler
 */
public class InvalidCredentialsException extends RuntimeException {

    /**
     * Construit une nouvelle InvalidCredentialsException avec un message par défaut.
     * 
     * <p>Ce constructeur utilise un message générique qui ne révèle aucune
     * information sur la nature spécifique de l'erreur d'authentification,
     * conformément aux bonnes pratiques de sécurité.</p>
     */
    public InvalidCredentialsException() {
        super("Identifiants invalides");
    }

    /**
     * Construit une nouvelle InvalidCredentialsException avec un message personnalisé.
     * 
     * <p>Ce constructeur permet de spécifier un message d'erreur personnalisé.
     * Attention : le message doit rester générique pour des raisons de sécurité
     * et ne pas révéler d'informations sensibles sur les comptes utilisateur.</p>
     * 
     * @param message Le message d'erreur personnalisé
     * 
     * @example
     * <pre>
     * throw new InvalidCredentialsException("Échec de l'authentification");
     * throw new InvalidCredentialsException("Token d'authentification invalide");
     * </pre>
     */
    public InvalidCredentialsException(String message) {
        super(message);
    }

    /**
     * Construit une nouvelle InvalidCredentialsException avec un message et une cause.
     * 
     * <p>Ce constructeur est utilisé lorsque l'exception est levée suite à
     * une autre exception sous-jacente (par exemple, erreur de base de données
     * lors de la vérification des identifiants). Il preserve la stack trace
     * complète pour le debugging.</p>
     * 
     * @param message Le message d'erreur
     * @param cause L'exception originale qui a causé cette InvalidCredentialsException
     * 
     * @example
     * <pre>
     * try {
     *     User user = userRepository.findByEmail(email);
     *     // Vérification du mot de passe...
     * } catch (DataAccessException ex) {
     *     throw new InvalidCredentialsException("Erreur lors de la vérification des identifiants", ex);
     * }
     * </pre>
     */
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construit une nouvelle InvalidCredentialsException avec une cause.
     * 
     * <p>Ce constructeur utilise un message par défaut tout en préservant
     * la cause originale de l'exception pour les besoins de debugging.</p>
     * 
     * @param cause L'exception qui a causé cette InvalidCredentialsException
     */
    public InvalidCredentialsException(Throwable cause) {
        super("Identifiants invalides", cause);
    }

    /**
     * Crée une InvalidCredentialsException spécifique pour les échecs de connexion par email/mot de passe.
     * 
     * <p>Méthode utilitaire statique utilisée lors des tentatives de connexion
     * traditionnelles avec email et mot de passe. Le message reste volontairement
     * vague pour des raisons de sécurité.</p>
     * 
     * @return Une nouvelle instance adaptée aux échecs de connexion
     * 
     * @example
     * <pre>
     * if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
     *     throw InvalidCredentialsException.forLoginAttempt();
     * }
     * </pre>
     */
    public static InvalidCredentialsException forLoginAttempt() {
        return new InvalidCredentialsException(
            "Email ou mot de passe incorrect. Veuillez vérifier vos identifiants et réessayer."
        );
    }

    /**
     * Crée une InvalidCredentialsException pour les erreurs de validation de token JWT.
     * 
     * <p>Méthode utilitaire statique utilisée lorsqu'un token JWT est invalide,
     * corrompu ou ne peut pas être vérifié. Cette exception peut être levée
     * lors de la validation de la signature, du format ou du contenu du token.</p>
     * 
     * @return Une nouvelle instance adaptée aux erreurs de token JWT
     * 
     * @example
     * <pre>
     * try {
     *     Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
     * } catch (JwtException ex) {
     *     throw InvalidCredentialsException.forInvalidToken();
     * }
     * </pre>
     */
    public static InvalidCredentialsException forInvalidToken() {
        return new InvalidCredentialsException(
            "Token d'authentification invalide. Veuillez vous reconnecter."
        );
    }

    /**
     * Crée une InvalidCredentialsException pour les échecs de validation de refresh token.
     * 
     * <p>Méthode utilitaire statique utilisée lors de la validation des refresh tokens.
     * Ces tokens sont utilisés pour renouveler les tokens d'accès sans redemander
     * les identifiants à l'utilisateur.</p>
     * 
     * @return Une nouvelle instance adaptée aux erreurs de refresh token
     * 
     * @example
     * <pre>
     * RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenValue);
     * if (refreshToken == null || refreshToken.isExpired()) {
     *     throw InvalidCredentialsException.forInvalidRefreshToken();
     * }
     * </pre>
     */
    public static InvalidCredentialsException forInvalidRefreshToken() {
        return new InvalidCredentialsException(
            "Token de rafraîchissement invalide. Veuillez vous reconnecter."
        );
    }

    /**
     * Crée une InvalidCredentialsException pour les tentatives d'accès non autorisées.
     * 
     * <p>Méthode utilitaire statique utilisée lorsqu'un utilisateur tente d'accéder
     * à une ressource sans les permissions appropriées ou avec des identifiants
     * insuffisants.</p>
     * 
     * @return Une nouvelle instance adaptée aux accès non autorisés
     * 
     * @example
     * <pre>
     * if (!hasPermission(user, resource)) {
     *     throw InvalidCredentialsException.forUnauthorizedAccess();
     * }
     * </pre>
     */
    public static InvalidCredentialsException forUnauthorizedAccess() {
        return new InvalidCredentialsException(
            "Accès non autorisé. Vos identifiants ne permettent pas d'accéder à cette ressource."
        );
    }

    /**
     * Crée une InvalidCredentialsException pour les comptes désactivés ou verrouillés.
     * 
     * <p>Méthode utilitaire statique utilisée lorsqu'un utilisateur tente de se
     * connecter avec des identifiants corrects mais que son compte est dans un
     * état qui ne permet pas la connexion (désactivé, verrouillé, etc.).</p>
     * 
     * @return Une nouvelle instance adaptée aux comptes désactivés
     * 
     * @example
     * <pre>
     * if (!user.isEnabled() || !user.isAccountNonLocked()) {
     *     throw InvalidCredentialsException.forDisabledAccount();
     * }
     * </pre>
     */
    public static InvalidCredentialsException forDisabledAccount() {
        return new InvalidCredentialsException(
            "Ce compte est temporairement indisponible. Veuillez contacter l'administrateur."
        );
    }

    /**
     * Crée une InvalidCredentialsException pour les tentatives de connexion multiples échouées.
     * 
     * <p>Méthode utilitaire statique utilisée dans le cadre de mécanismes de
     * protection contre les attaques par force brute. Elle peut être levée
     * après un certain nombre de tentatives de connexion échouées consécutives.</p>
     * 
     * @param remainingAttempts Le nombre de tentatives restantes avant verrouillage
     * @return Une nouvelle instance avec information sur les tentatives restantes
     * 
     * @example
     * <pre>
     * if (failedAttempts >= maxAttempts - 1) {
     *     throw InvalidCredentialsException.forMultipleFailedAttempts(1);
     * }
     * </pre>
     */
    public static InvalidCredentialsException forMultipleFailedAttempts(int remainingAttempts) {
        return new InvalidCredentialsException(
            "Identifiants incorrects. Il vous reste " + remainingAttempts + 
            " tentative(s) avant verrouillage temporaire du compte."
        );
    }
}