
package com.javacraftacademy.userservice.exception;

/**
 * Exception levée lorsqu'une tentative de création ou de mise à jour d'utilisateur
 * utilise une adresse email déjà présente dans le système.
 * 
 * <p>Cette exception métier est essentielle pour maintenir l'intégrité des données
 * et respecter la contrainte d'unicité des adresses email dans l'application.
 * Elle est levée avant toute tentative d'insertion en base de données pour
 * éviter les violations de contraintes et fournir un message d'erreur explicite.</p>
 * 
 * <h3>Contextes d'utilisation :</h3>
 * <ul>
 *   <li>Inscription d'un nouveau utilisateur avec un email déjà utilisé</li>
 *   <li>Mise à jour du profil avec changement d'email vers un email existant</li>
 *   <li>Validation de l'unicité lors des opérations de migration de données</li>
 *   <li>Import en lot d'utilisateurs avec détection de doublons</li>
 * </ul>
 * 
 * <h3>Relations avec l'application :</h3>
 * <ul>
 *   <li><strong>AuthService</strong> : Levée lors du processus d'inscription (register)</li>
 *   <li><strong>UserService</strong> : Utilisée lors des mises à jour d'informations utilisateur</li>
 *   <li><strong>ProfileService</strong> : Levée lors de la modification d'email de profil</li>
 *   <li><strong>UserRepository</strong> : Vérification d'existence avant insertion/modification</li>
 *   <li><strong>AuthController</strong> : Gestion des erreurs d'inscription</li>
 *   <li><strong>GlobalExceptionHandler</strong> : Transformée en réponse HTTP 409 (Conflict)</li>
 *   <li><strong>EmailValidator</strong> : Peut déclencher une vérification d'unicité</li>
 * </ul>
 * 
 * <h3>Flux typique d'utilisation :</h3>
 * <pre>
 * 1. Utilisateur soumet un formulaire d'inscription
 * 2. AuthService valide les données
 * 3. Vérification de l'unicité de l'email via UserRepository
 * 4. Si email existe : lancement de EmailAlreadyExistsException
 * 5. GlobalExceptionHandler intercepte et retourne HTTP 409
 * 6. Client reçoit un message d'erreur formaté
 * </pre>
 * 
 * <h3>Sécurité et confidentialité :</h3>
 * <p>Cette exception doit être gérée avec soin pour éviter l'énumération
 * d'emails valides. Les messages d'erreur doivent être informatifs pour
 * l'utilisateur légitime tout en ne révélant pas d'informations sensibles
 * à des attaquants potentiels.</p>
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 * @see com.javacraftacademy.userservice.service.AuthService
 * @see com.javacraftacademy.userservice.service.UserService
 * @see com.javacraftacademy.userservice.exception.GlobalExceptionHandler
 */
public class EmailAlreadyExistsException extends RuntimeException {

    /**
     * Construit une nouvelle EmailAlreadyExistsException avec un message par défaut.
     * 
     * <p>Ce constructeur fournit un message générique indiquant que l'adresse
     * email est déjà utilisée, sans révéler d'informations spécifiques.</p>
     */
    public EmailAlreadyExistsException() {
        super("Cette adresse email est déjà utilisée");
    }

    /**
     * Construit une nouvelle EmailAlreadyExistsException avec un message personnalisé.
     * 
     * <p>Ce constructeur permet de personnaliser le message d'erreur selon
     * le contexte spécifique où l'exception est levée. Il est recommandé
     * de fournir des messages clairs et orientés utilisateur.</p>
     * 
     * @param message Le message détaillé de l'exception
     * 
     * @example
     * <pre>
     * throw new EmailAlreadyExistsException("L'email user@example.com est déjà associé à un compte");
     * throw new EmailAlreadyExistsException("Impossible de modifier l'email : adresse déjà utilisée");
     * </pre>
     */
    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    /**
     * Construit une nouvelle EmailAlreadyExistsException avec un message et une cause.
     * 
     * <p>Ce constructeur est utilisé lorsque l'exception est levée en réaction
     * à une autre exception, comme une violation de contrainte de base de données.
     * Il preserve la trace complète de l'erreur pour faciliter le debugging.</p>
     * 
     * @param message Le message détaillé de l'exception
     * @param cause L'exception originale qui a causé cette EmailAlreadyExistsException
     * 
     * @example
     * <pre>
     * try {
     *     userRepository.save(user);
     * } catch (DataIntegrityViolationException ex) {
     *     throw new EmailAlreadyExistsException("Email déjà utilisé lors de l'insertion", ex);
     * }
     * </pre>
     */
    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construit une nouvelle EmailAlreadyExistsException avec une cause.
     * 
     * <p>Ce constructeur est utilisé lorsque la cause de l'exception est
     * suffisamment explicite et qu'un message générique suffit.</p>
     * 
     * @param cause L'exception qui a causé cette EmailAlreadyExistsException
     */
    public EmailAlreadyExistsException(Throwable cause) {
        super("Cette adresse email est déjà utilisée", cause);
    }

    /**
     * Crée une EmailAlreadyExistsException spécifique pour le processus d'inscription.
     * 
     * <p>Méthode utilitaire statique qui génère une exception avec un message
     * adapté au contexte d'inscription d'un nouvel utilisateur.</p>
     * 
     * @param email L'adresse email qui cause le conflit (peut être null pour la sécurité)
     * @return Une nouvelle instance avec un message approprié pour l'inscription
     * 
     * @example
     * <pre>
     * if (userRepository.existsByEmail(email)) {
     *     throw EmailAlreadyExistsException.forRegistration(email);
     * }
     * </pre>
     */
    public static EmailAlreadyExistsException forRegistration(String email) {
        return new EmailAlreadyExistsException(
            "Un compte existe déjà avec cette adresse email. " +
            "Veuillez utiliser une autre adresse ou vous connecter à votre compte existant."
        );
    }

    /**
     * Crée une EmailAlreadyExistsException spécifique pour la mise à jour de profil.
     * 
     * <p>Méthode utilitaire statique qui génère une exception avec un message
     * adapté au contexte de modification d'un profil utilisateur existant.</p>
     * 
     * @param email L'adresse email qui cause le conflit
     * @return Une nouvelle instance avec un message approprié pour la mise à jour
     * 
     * @example
     * <pre>
     * if (userRepository.existsByEmailAndIdNot(newEmail, userId)) {
     *     throw EmailAlreadyExistsException.forProfileUpdate(newEmail);
     * }
     * </pre>
     */
    public static EmailAlreadyExistsException forProfileUpdate(String email) {
        return new EmailAlreadyExistsException(
            "Impossible de modifier l'adresse email : cette adresse est déjà utilisée par un autre compte."
        );
    }

    /**
     * Crée une EmailAlreadyExistsException pour les opérations d'administration.
     * 
     * <p>Méthode utilitaire statique pour les cas d'usage administrateur
     * où des informations plus détaillées peuvent être appropriées.</p>
     * 
     * @param email L'adresse email qui cause le conflit
     * @return Une nouvelle instance avec un message adapté aux administrateurs
     * 
     * @example
     * <pre>
     * if (userRepository.existsByEmail(email)) {
     *     throw EmailAlreadyExistsException.forAdminOperation(email);
     * }
     * </pre>
     */
    public static EmailAlreadyExistsException forAdminOperation(String email) {
        return new EmailAlreadyExistsException(
            "L'adresse email '" + email + "' est déjà associée à un compte utilisateur existant."
        );
    }

    /**
     * Crée une EmailAlreadyExistsException pour les imports en lot.
     * 
     * <p>Méthode utilitaire statique utilisée lors d'opérations d'import
     * de données en lot, où il est important de signaler les doublons
     * sans interrompre complètement le processus.</p>
     * 
     * @param email L'adresse email en doublon
     * @param lineNumber Le numéro de ligne dans le fichier d'import (optionnel)
     * @return Une nouvelle instance avec un message adapté aux imports
     * 
     * @example
     * <pre>
     * throw EmailAlreadyExistsException.forBatchImport("user@example.com", 15);
     * </pre>
     */
    public static EmailAlreadyExistsException forBatchImport(String email, Integer lineNumber) {
        String message = "Email en doublon détecté : " + email;
        if (lineNumber != null) {
            message += " (ligne " + lineNumber + ")";
        }
        return new EmailAlreadyExistsException(message);
    }
}