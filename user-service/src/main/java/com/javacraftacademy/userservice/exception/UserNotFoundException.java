
package com.javacraftacademy.userservice.exception;

/**
 * Exception levée lorsqu'un utilisateur demandé n'est pas trouvé dans le système.
 * 
 * <p>Cette exception métier est utilisée pour signaler qu'une opération a échoué
 * car l'utilisateur recherché n'existe pas dans la base de données. Elle est
 * généralement levée lors d'opérations de lecture, mise à jour ou suppression
 * d'utilisateurs inexistants.</p>
 * 
 * <h3>Contextes d'utilisation :</h3>
 * <ul>
 *   <li>Recherche d'un utilisateur par ID, email ou nom d'utilisateur</li>
 *   <li>Tentative de mise à jour d'un profil utilisateur inexistant</li>
 *   <li>Opérations nécessitant une validation de l'existence d'un utilisateur</li>
 *   <li>Récupération d'informations utilisateur pour l'authentification</li>
 * </ul>
 * 
 * <h3>Relations avec l'application :</h3>
 * <ul>
 *   <li><strong>UserService</strong> : Levée lors des opérations CRUD sur les utilisateurs</li>
 *   <li><strong>AuthService</strong> : Utilisée lors de la validation des identifiants</li>
 *   <li><strong>ProfileService</strong> : Levée lors des opérations sur les profils</li>
 *   <li><strong>UserRepository</strong> : Les méthodes de recherche peuvent déclencher cette exception</li>
 *   <li><strong>Controllers</strong> : Interceptée par le GlobalExceptionHandler pour retourner HTTP 404</li>
 *   <li><strong>Security</strong> : Peut être levée lors de la récupération des détails utilisateur</li>
 * </ul>
 * 
 * <h3>Gestion par le GlobalExceptionHandler :</h3>
 * <p>Cette exception est automatiquement interceptée et transformée en réponse
 * HTTP 404 (Not Found) avec un message d'erreur approprié, assurant une
 * expérience utilisateur cohérente.</p>
 * 
 * <h3>Bonnes pratiques :</h3>
 * <ul>
 *   <li>Fournir des messages d'erreur spécifiques et informatifs</li>
 *   <li>Inclure l'identifiant de l'utilisateur recherché si pertinent</li>
 *   <li>Ne pas exposer d'informations sensibles dans le message</li>
 *   <li>Logger l'erreur pour le monitoring et le debugging</li>
 * </ul>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 * @see com.javacraftacademy.userservice.service.UserService
 * @see com.javacraftacademy.userservice.service.AuthService
 * @see com.javacraftacademy.userservice.exception.GlobalExceptionHandler
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Construit une nouvelle UserNotFoundException avec un message d'erreur par défaut.
     * 
     * <p>Ce constructeur est utilisé lorsqu'aucun détail spécifique n'est nécessaire
     * dans le message d'erreur.</p>
     */
    public UserNotFoundException() {
        super("Utilisateur non trouvé");
    }

    /**
     * Construit une nouvelle UserNotFoundException avec un message d'erreur personnalisé.
     * 
     * <p>Ce constructeur permet de fournir un message d'erreur spécifique
     * selon le contexte de l'exception. Il est recommandé d'utiliser ce
     * constructeur pour donner des informations plus précises sur l'erreur.</p>
     * 
     * @param message Le message détaillé de l'exception qui sera retourné au client
     * 
     * @example
     * <pre>
     * throw new UserNotFoundException("Utilisateur avec l'ID " + userId + " non trouvé");
     * throw new UserNotFoundException("Aucun utilisateur trouvé avec l'email : " + email);
     * </pre>
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * Construit une nouvelle UserNotFoundException avec un message et une cause.
     * 
     * <p>Ce constructeur est utilisé lorsque cette exception est levée en réaction
     * à une autre exception (par exemple, une exception de base de données).
     * Il permet de conserver la trace complète de l'erreur pour le debugging.</p>
     * 
     * @param message Le message détaillé de l'exception
     * @param cause L'exception qui a causé cette UserNotFoundException
     * 
     * @example
     * <pre>
     * try {
     *     // Opération base de données
     * } catch (DataAccessException ex) {
     *     throw new UserNotFoundException("Erreur lors de la recherche de l'utilisateur", ex);
     * }
     * </pre>
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construit une nouvelle UserNotFoundException avec une cause.
     * 
     * <p>Ce constructeur est utilisé lorsque la cause de l'exception est
     * suffisamment explicite et qu'aucun message supplémentaire n'est nécessaire.</p>
     * 
     * @param cause L'exception qui a causé cette UserNotFoundException
     */
    public UserNotFoundException(Throwable cause) {
        super("Utilisateur non trouvé", cause);
    }

    /**
     * Crée une UserNotFoundException avec un message formaté pour un utilisateur recherché par ID.
     * 
     * <p>Méthode utilitaire statique qui simplifie la création d'exceptions
     * avec des messages standardisés pour les recherches par ID.</p>
     * 
     * @param userId L'ID de l'utilisateur qui n'a pas été trouvé
     * @return Une nouvelle instance de UserNotFoundException avec un message formaté
     * 
     * @example
     * <pre>
     * throw UserNotFoundException.byId(123L);
     * // Message : "Utilisateur avec l'ID 123 non trouvé"
     * </pre>
     */
    public static UserNotFoundException byId(Long userId) {
        return new UserNotFoundException("Utilisateur avec l'ID " + userId + " non trouvé");
    }

    /**
     * Crée une UserNotFoundException avec un message formaté pour un utilisateur recherché par email.
     * 
     * <p>Méthode utilitaire statique qui simplifie la création d'exceptions
     * avec des messages standardisés pour les recherches par email.</p>
     * 
     * @param email L'adresse email de l'utilisateur qui n'a pas été trouvé
     * @return Une nouvelle instance de UserNotFoundException avec un message formaté
     * 
     * @example
     * <pre>
     * throw UserNotFoundException.byEmail("user@example.com");
     * // Message : "Aucun utilisateur trouvé avec l'email : user@example.com"
     * </pre>
     */
    public static UserNotFoundException byEmail(String email) {
        return new UserNotFoundException("Aucun utilisateur trouvé avec l'email : " + email);
    }

    /**
     * Crée une UserNotFoundException avec un message formaté pour un utilisateur recherché par nom d'utilisateur.
     * 
     * <p>Méthode utilitaire statique qui simplifie la création d'exceptions
     * avec des messages standardisés pour les recherches par nom d'utilisateur.</p>
     * 
     * @param username Le nom d'utilisateur qui n'a pas été trouvé
     * @return Une nouvelle instance de UserNotFoundException avec un message formaté
     * 
     * @example
     * <pre>
     * throw UserNotFoundException.byUsername("johndoe");
     * // Message : "Aucun utilisateur trouvé avec le nom d'utilisateur : johndoe"
     * </pre>
     */
    public static UserNotFoundException byUsername(String username) {
        return new UserNotFoundException("Aucun utilisateur trouvé avec le nom d'utilisateur : " + username);
    }
}