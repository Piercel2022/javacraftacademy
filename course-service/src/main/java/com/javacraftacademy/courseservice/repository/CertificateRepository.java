package com.javacraftacademy.courseservice.repository;

import com.javacraftacademy.courseservice.model.entity.Certificate;
import com.javacraftacademy.courseservice.model.entity.Course;
import com.javacraftacademy.courseservice.model.enums.CertificateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface pour la gestion des certificats de cours.
 * 
 * Cette interface étend JpaRepository pour fournir les opérations CRUD de base
 * et définit des méthodes personnalisées pour la gestion des certificats d'achèvement
 * des cours dans l'application JavaCraft Academy.
 * 
 * Relations avec l'application :
 * - Utilisé par CertificateService pour l'accès aux données des certificats
 * - Connecté à l'entité Certificate qui a des relations avec :
 *   * Course (Many-to-One) - chaque certificat est lié à un cours
 *   * User (Many-to-One) - chaque certificat appartient à un utilisateur
 *   * Enrollment (One-to-One) - chaque certificat est généré après une inscription réussie
 * - Intégré avec le système de messagerie Kafka pour les événements de certification
 * - Utilisé par les services de notification pour informer les utilisateurs
 * - Connecté au service de génération PDF pour la création physique des certificats
 * 
 * Fonctionnalités principales :
 * - Génération et gestion des certificats d'achèvement
 * - Vérification de l'authenticité des certificats par code unique
 * - Suivi des statistiques de certification par cours et utilisateur
 * - Gestion des certificats expirés ou révoqués
 * - Recherche et filtrage avancés des certificats
 * - Support de la pagination pour les listes de certificats
 * - Intégration avec les systèmes d'audit et de conformité
 * 
 * Sécurité et conformité :
 * - Codes de vérification uniques pour prévenir la falsification
 * - Horodatage précis pour la traçabilité
 * - Support des certificats révocables pour les cas de fraude
 * - Intégration avec les systèmes d'authentification
 * 
 * @author JavaCraft Academy
 * @version 1.0
 * @since 1.0
 */
@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    /**
     * Recherche un certificat par son code de vérification unique.
     * 
     * Cette méthode est cruciale pour la vérification de l'authenticité
     * des certificats. Le code de vérification est utilisé par les employeurs
     * et autres parties tierces pour valider les certificats.
     * 
     * @param verificationCode le code unique de vérification du certificat
     * @return Optional contenant le certificat si trouvé, sinon Optional.empty()
     */
    Optional<Certificate> findByVerificationCode(String verificationCode);

    /**
     * Récupère tous les certificats d'un utilisateur spécifique.
     * 
     * Utilisée pour afficher le portfolio de certificats d'un utilisateur
     * dans son profil ou tableau de bord personnel.
     * 
     * @param userId l'identifiant de l'utilisateur
     * @return List des certificats obtenus par l'utilisateur
     */
    List<Certificate> findByUserId(Long userId);

    /**
     * Récupère tous les certificats émis pour un cours spécifique.
     * 
     * Permet aux instructeurs et administrateurs de voir tous les
     * certificats émis pour leur cours, utile pour les statistiques
     * et le suivi de la réussite.
     * 
     * @param course l'entité Course pour laquelle chercher les certificats
     * @return List des certificats émis pour ce cours
     */
    List<Certificate> findByCourse(Course course);

    /**
     * Récupère tous les certificats émis pour un cours par son ID.
     * 
     * Version alternative de la méthode précédente utilisant l'ID du cours
     * plutôt que l'entité complète.
     * 
     * @param courseId l'identifiant du cours
     * @return List des certificats émis pour ce cours
     */
    List<Certificate> findByCourseId(Long courseId);

    /**
     * Trouve le certificat d'un utilisateur pour un cours spécifique.
     * 
     * Méthode essentielle pour vérifier si un utilisateur a déjà obtenu
     * un certificat pour un cours donné, évitant les doublons.
     * 
     * @param userId l'identifiant de l'utilisateur
     * @param courseId l'identifiant du cours
     * @return Optional contenant le certificat si existant
     */
    Optional<Certificate> findByUserIdAndCourseId(Long userId, Long courseId);

    /**
     * Récupère les certificats par statut.
     * 
     * Permet de filtrer les certificats selon leur statut :
     * ACTIVE, EXPIRED, REVOKED, PENDING, etc.
     * 
     * @param status le statut des certificats à rechercher
     * @return List des certificats ayant le statut spécifié
     */
    List<Certificate> findByStatus(CertificateStatus status);

    /**
     * Récupère les certificats actifs d'un utilisateur.
     * 
     * Combine le filtre par utilisateur et par statut actif,
     * utile pour afficher uniquement les certificats valides.
     * 
     * @param userId l'identifiant de l'utilisateur
     * @return List des certificats actifs de l'utilisateur
     */
    List<Certificate> findByUserIdAndStatus(Long userId, CertificateStatus status);

    /**
     * Récupère les certificats émis dans une période donnée.
     * 
     * Utilisée pour les rapports temporels et les statistiques
     * d'émission de certificats.
     * 
     * @param startDate date de début de la période
     * @param endDate date de fin de la période
     * @return List des certificats émis dans la période
     */
    List<Certificate> findByIssuedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Récupère les certificats expirés.
     * 
     * Trouve tous les certificats dont la date d'expiration
     * est antérieure à la date actuelle.
     * 
     * @return List des certificats expirés
     */
    @Query("SELECT c FROM Certificate c WHERE c.expiresAt < CURRENT_TIMESTAMP AND c.status = 'ACTIVE'")
    List<Certificate> findExpiredCertificates();

    /**
     * Récupère les certificats expirant bientôt.
     * 
     * Trouve les certificats qui vont expirer dans les prochains jours,
     * utile pour envoyer des notifications de renouvellement.
     * 
     * @param days nombre de jours avant expiration
     * @return List des certificats expirant bientôt
     */
    @Query("SELECT c FROM Certificate c WHERE c.expiresAt BETWEEN CURRENT_TIMESTAMP AND CURRENT_TIMESTAMP + :days DAY")
    List<Certificate> findCertificatesExpiringWithin(@Param("days") Integer days);

    /**
     * Compte le nombre de certificats émis pour un cours.
     * 
     * Statistique importante pour mesurer le succès et la popularité
     * d'un cours.
     * 
     * @param courseId l'identifiant du cours
     * @return le nombre de certificats émis pour ce cours
     */
    Long countByCourseId(Long courseId);

    /**
     * Compte le nombre de certificats obtenus par un utilisateur.
     * 
     * Permet de calculer les statistiques de réussite d'un utilisateur
     * et de gamifier l'expérience d'apprentissage.
     * 
     * @param userId l'identifiant de l'utilisateur
     * @return le nombre total de certificats de l'utilisateur
     */
    Long countByUserId(Long userId);

    /**
     * Compte les certificats émis dans une période.
     * 
     * Utilisée pour les rapports de performance et les métriques
     * d'activité de la plateforme.
     * 
     * @param startDate date de début
     * @param endDate date de fin
     * @return nombre de certificats émis dans la période
     */
    Long countByIssuedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Recherche paginée des certificats d'un utilisateur.
     * 
     * Permet d'afficher les certificats d'un utilisateur par pages
     * pour optimiser les performances et l'expérience utilisateur.
     * 
     * @param userId l'identifiant de l'utilisateur
     * @param pageable informations de pagination
     * @return Page des certificats de l'utilisateur
     */
    Page<Certificate> findByUserId(Long userId, Pageable pageable);

    /**
     * Recherche paginée des certificats par cours.
     * 
     * Version paginée de la recherche de certificats par cours.
     * 
     * @param courseId l'identifiant du cours
     * @param pageable informations de pagination
     * @return Page des certificats du cours
     */
    Page<Certificate> findByCourseId(Long courseId, Pageable pageable);

    /**
     * Recherche des certificats par nom de cours (recherche textuelle).
     * 
     * Permet de rechercher des certificats en tapant une partie
     * du nom du cours associé.
     * 
     * @param courseName terme de recherche dans le nom du cours
     * @return List des certificats correspondants
     */
    @Query("SELECT c FROM Certificate c JOIN c.course course WHERE " +
           "LOWER(course.title) LIKE LOWER(CONCAT('%', :courseName, '%'))")
    List<Certificate> findByCourseNameContaining(@Param("courseName") String courseName);

    /**
     * Récupère les certificats les plus récents.
     * 
     * Retourne les derniers certificats émis, utile pour les
     * tableaux de bord et les notifications d'activité récente.
     * 
     * @param pageable informations de pagination pour limiter les résultats
     * @return List des certificats les plus récents
     */
    @Query("SELECT c FROM Certificate c ORDER BY c.issuedAt DESC")
    List<Certificate> findRecentCertificates(Pageable pageable);

    /**
     * Récupère les statistiques de certification par mois.
     * 
     * Analyse temporelle de l'émission des certificats pour
     * les rapports et graphiques de performance.
     * 
     * @param year l'année pour les statistiques
     * @return List d'objets contenant mois et nombre de certificats
     */
    @Query("SELECT MONTH(c.issuedAt) as month, COUNT(c) as count " +
           "FROM Certificate c WHERE YEAR(c.issuedAt) = :year " +
           "GROUP BY MONTH(c.issuedAt) ORDER BY month")
    List<Object[]> getCertificationStatisticsByMonth(@Param("year") Integer year);

    /**
     * Récupère les utilisateurs les plus certifiés.
     * 
     * Classement des utilisateurs par nombre de certificats obtenus,
     * utilisé pour les leaderboards et la gamification.
     * 
     * @param pageable informations de pagination pour limiter les résultats
     * @return Page d'objets contenant utilisateur et nombre de certificats
     */
    @Query("SELECT c.userId, COUNT(c) as certificateCount " +
           "FROM Certificate c WHERE c.status = 'ACTIVE' " +
           "GROUP BY c.userId ORDER BY certificateCount DESC")
    Page<Object[]> findTopCertifiedUsers(Pageable pageable);

    /**
     * Récupère les cours les plus certifiants.
     * 
     * Classement des cours par nombre de certificats émis,
     * indicateur de succès et de popularité des cours.
     * 
     * @param pageable informations de pagination
     * @return Page d'objets contenant cours et nombre de certificats
     */
    @Query("SELECT c.course, COUNT(c) as certificateCount " +
           "FROM Certificate c WHERE c.status = 'ACTIVE' " +
           "GROUP BY c.course ORDER BY certificateCount DESC")
    Page<Object[]> findMostCertifyingCourses(Pageable pageable);

    /**
     * Vérifie si un certificat existe pour une inscription donnée.
     * 
     * Utilisée pour éviter la création de certificats en double
     * pour une même inscription terminée avec succès.
     * 
     * @param enrollmentId l'identifiant de l'inscription
     * @return true si un certificat existe déjà, false sinon
     */
    boolean existsByEnrollmentId(Long enrollmentId);

    /**
     * Vérifie l'unicité du code de vérification.
     * 
     * Utilisée lors de la génération de nouveaux certificats pour
     * garantir l'unicité des codes de vérification.
     * 
     * @param verificationCode le code à vérifier
     * @return true si le code existe déjà, false sinon
     */
    boolean existsByVerificationCode(String verificationCode);

    /**
     * Recherche les certificats par catégorie de cours.
     * 
     * Permet de filtrer les certificats selon la catégorie
     * du cours associé, utile pour les spécialisations.
     * 
     * @param categoryId l'identifiant de la catégorie
     * @return List des certificats pour les cours de cette catégorie
     */
    @Query("SELECT c FROM Certificate c JOIN c.course course " +
           "WHERE course.category.id = :categoryId")
    List<Certificate> findByCourseCategory(@Param("categoryId") Long categoryId);

    /**
     * Récupère les certificats d'un utilisateur par catégorie.
     * 
     * Combine le filtre par utilisateur et par catégorie de cours,
     * utile pour organiser les certificats par domaine d'expertise.
     * 
     * @param userId l'identifiant de l'utilisateur
     * @param categoryId l'identifiant de la catégorie
     * @return List des certificats de l'utilisateur dans cette catégorie
     */
    @Query("SELECT c FROM Certificate c JOIN c.course course " +
           "WHERE c.userId = :userId AND course.category.id = :categoryId")
    List<Certificate> findByUserIdAndCourseCategory(@Param("userId") Long userId, 
                                                   @Param("categoryId") Long categoryId);

    /**
     * Récupère les détails complets des certificats avec joins.
     * 
     * Optimise les requêtes en chargeant les entités liées
     * (cours, catégorie, etc.) en une seule requête.
     * 
     * @return List des certificats avec tous les détails
     */
    @Query("SELECT c FROM Certificate c " +
           "LEFT JOIN FETCH c.course course " +
           "LEFT JOIN FETCH course.category " +
           "ORDER BY c.issuedAt DESC")
    List<Certificate> findAllWithDetails();

    /**
     * Recherche les certificats émis par un instructeur.
     * 
     * Trouve tous les certificats émis pour les cours créés
     * par un instructeur spécifique. Utile pour que les instructeurs
     * puissent suivre l'impact de leurs cours.
     * 
     * @param instructorId l'identifiant de l'instructeur
     * @return List des certificats émis pour les cours de cet instructeur
     */
    @Query("SELECT c FROM Certificate c JOIN c.course course " +
           "WHERE course.instructorId = :instructorId")
    List<Certificate> findByInstructorId(@Param("instructorId") Long instructorId);

    /**
     * Récupère les certificats d'un instructeur avec pagination.
     * 
     * Version paginée de la méthode précédente pour optimiser
     * les performances quand un instructeur a beaucoup de cours.
     * 
     * @param instructorId l'identifiant de l'instructeur
     * @param pageable informations de pagination
     * @return Page des certificats émis pour les cours de cet instructeur
     */
    @Query("SELECT c FROM Certificate c JOIN c.course course " +
           "WHERE course.instructorId = :instructorId " +
           "ORDER BY c.issuedAt DESC")
    Page<Certificate> findByInstructorId(@Param("instructorId") Long instructorId, Pageable pageable);

    /**
     * Compte le nombre de certificats émis par un instructeur.
     * 
     * Statistique pour mesurer l'impact d'un instructeur sur
     * la réussite des étudiants.
     * 
     * @param instructorId l'identifiant de l'instructeur
     * @return nombre total de certificats émis pour les cours de cet instructeur
     */
    @Query("SELECT COUNT(c) FROM Certificate c JOIN c.course course " +
           "WHERE course.instructorId = :instructorId")
    Long countByInstructorId(@Param("instructorId") Long instructorId);

    /**
     * Recherche les certificats révoqués.
     * 
     * Récupère tous les certificats qui ont été révoqués,
     * utile pour l'audit et le suivi des anomalies.
     * 
     * @return List des certificats révoqués
     */
    @Query("SELECT c FROM Certificate c WHERE c.status = 'REVOKED' ORDER BY c.revokedAt DESC")
    List<Certificate> findRevokedCertificates();

    /**
     * Recherche les certificats par niveau de difficulté du cours.
     * 
     * Permet de filtrer les certificats selon le niveau de difficulté
     * du cours associé (BEGINNER, INTERMEDIATE, ADVANCED).
     * 
     * @param level le niveau de difficulté
     * @return List des certificats pour les cours de ce niveau
     */
    @Query("SELECT c FROM Certificate c JOIN c.course course " +
           "WHERE course.level = :level")
    List<Certificate> findByCourseLevel(@Param("level") String level);

    /**
     * Récupère les certificats avec leur score de completion.
     * 
     * Inclut le score obtenu par l'utilisateur lors de la completion
     * du cours, utile pour les certificats avec mention.
     * 
     * @param minScore score minimum requis
     * @return List des certificats avec score supérieur ou égal au minimum
     */
    @Query("SELECT c FROM Certificate c WHERE c.completionScore >= :minScore " +
           "ORDER BY c.completionScore DESC")
    List<Certificate> findByMinimumScore(@Param("minScore") Double minScore);

    /**
     * Recherche les certificats par langue du cours.
     * 
     * Permet de filtrer les certificats selon la langue
     * dans laquelle le cours a été dispensé.
     * 
     * @param language la langue du cours
     * @return List des certificats pour les cours dans cette langue
     */
    @Query("SELECT c FROM Certificate c JOIN c.course course " +
           "WHERE course.language = :language")
    List<Certificate> findByCourseLanguage(@Param("language") String language);

    /**
     * Récupère les certificats avec durée de cours.
     * 
     * Trouve les certificats pour les cours ayant une durée
     * minimale spécifiée, utile pour les certifications professionnelles.
     * 
     * @param minDurationHours durée minimale en heures
     * @return List des certificats pour les cours de durée suffisante
     */
    @Query("SELECT c FROM Certificate c JOIN c.course course " +
           "WHERE course.durationHours >= :minDurationHours")
    List<Certificate> findByMinimumCourseDuration(@Param("minDurationHours") Integer minDurationHours);

    /**
     * Recherche avancée avec critères multiples.
     * 
     * Permet une recherche flexible avec plusieurs critères optionnels.
     * 
     * @param userId l'identifiant de l'utilisateur (optionnel)
     * @param courseId l'identifiant du cours (optionnel)
     * @param status le statut du certificat (optionnel)
     * @param startDate date de début de période (optionnelle)
     * @param endDate date de fin de période (optionnelle)
     * @param pageable informations de pagination
     * @return Page des certificats correspondant aux critères
     */
    @Query("SELECT c FROM Certificate c WHERE " +
           "(:userId IS NULL OR c.userId = :userId) AND " +
           "(:courseId IS NULL OR c.course.id = :courseId) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:startDate IS NULL OR c.issuedAt >= :startDate) AND " +
           "(:endDate IS NULL OR c.issuedAt <= :endDate)")
    Page<Certificate> findByCriteria(@Param("userId") Long userId,
                                   @Param("courseId") Long courseId,
                                   @Param("status") CertificateStatus status,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);
}