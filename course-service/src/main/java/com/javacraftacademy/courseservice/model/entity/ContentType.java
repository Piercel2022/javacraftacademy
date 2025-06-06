package com.javacraftacademy.courseservice.model.enums;

/**
 * Énumération définissant les types de contenu multimédia supportés dans JavaCraftAcademy.
 * 
 * <p>Cette énumération centralise la définition de tous les formats de contenu éducatif
 * pris en charge par la plateforme, permettant une gestion unifiée et extensible
 * des ressources pédagogiques. Chaque type de contenu a ses propres caractéristiques
 * de traitement, d'affichage et d'optimisation.</p>
 * 
 * <h3>Catégories de contenu :</h3>
 * <ul>
 *   <li><strong>Média audiovisuel</strong> : VIDEO, AUDIO pour contenu dynamique</li>
 *   <li><strong>Documents statiques</strong> : DOCUMENT, IMAGE pour références</li>
 *   <li><strong>Contenu interactif</strong> : INTERACTIVE, CODE_SAMPLE pour pratique</li>
 *   <li><strong>Archives</strong> : ARCHIVE pour projets complets</li>
 * </ul>
 * 
 * <h3>Intégrations techniques :</h3>
 * <ul>
 *   <li><strong>Transcoding automatique</strong> : Conversion multi-format selon le type</li>
 *   <li><strong>Génération de miniatures</strong> : Previews automatiques</li>
 *   <li><strong>Optimisation CDN</strong> : Cache et distribution adaptés</li>
 *   <li><strong>Validation MIME</strong> : Vérification de cohérence format/type</li>
 *   <li><strong>Analytics différenciées</strong> : Métriques spécifiques par type</li>
 * </ul>
 * 
 * <h3>Extensibilité :</h3>
 * <ul>
 *   <li>Ajout de nouveaux types sans impact sur l'existant</li>
 *   <li>Mapping automatique avec types MIME</li>
 *   <li>Configuration par type (limites taille, durée, etc.)</li>
 *   <li>Handlers spécialisés par catégorie de contenu</li>
 * </ul>
 * 
 * @author JavaCraftAcademy Team
 * @version 1.0
 * @since 1.0
 * @see com.javacraftacademy.courseservice.model.entity.CourseContent
 */
public enum ContentType {
    
    /**
     * Contenu vidéo (MP4, AVI, MOV, etc.).
     * 
     * <p><strong>Caractéristiques :</strong></p>
     * <ul>
     *   <li>Support du streaming adaptatif (HLS, DASH)</li>
     *   <li>Génération automatique de miniatures</li>
     *   <li>Extraction de métadonnées (durée, résolution, codec)</li>
     *   <li>Chapitrage et points de repère temporels</li>
     *   <li>Sous-titres et pistes audio multiples</li>
     *   <li>Analyse de qualité vidéo automatique</li>
     * </ul>
     * 
     * <p><strong>Optimisations :</strong></p>
     * <ul>
     *   <li>Transcodage multi-résolution (1080p, 720p, 480p)</li>
     *   <li>Compression adaptative selon la bande passante</li>
     *   <li>Prefetch intelligent des segments suivants</li>
     *   <li>CDN optimisé pour streaming</li>
     * </ul>
     * 
     * <p><strong>Types MIME supportés :</strong> video/mp4, video/avi, video/quicktime, video/webm</p>
     * <p><strong>Taille max recommandée :</strong> 2 GB par fichier</p>
     * <p><strong>Durée max recommandée :</strong> 3 heures</p>
     */
    VIDEO("video", "Contenu vidéo", "fa-play-circle", true, 2147483648L, 10800),
    
    /**
     * Contenu audio (MP3, WAV, AAC, etc.).
     * 
     * <p><strong>Caractéristiques :</strong></p>
     * <ul>
     *   <li>Support du streaming audio progressif</li>
     *   <li>Génération de waveforms visuelles</li>
     *   <li>Extraction de métadonnées (durée, bitrate, artiste)</li>
     *   <li>Chapitrage audio avec marqueurs temporels</li>
     *   <li>Normalisation automatique du volume</li>
     * </ul>
     * 
     * <p><strong>Optimisations :</strong></p>
     * <ul>
     *   <li>Compression adaptative (AAC, MP3 VBR)</li>
     *   <li>Préchargement intelligent des segments</li>
     *   <li>Cache agressif pour fichiers fréquents</li>
     * </ul>
     * 
     * <p><strong>Types MIME supportés :</strong> audio/mpeg, audio/wav, audio/aac, audio/ogg</p>
     * <p><strong>Taille max recommandée :</strong> 500 MB par fichier</p>
     * <p><strong>Durée max recommandée :</strong> 4 heures</p>
     */
    AUDIO("audio", "Contenu audio", "fa-volume-up", true, 524288000L, 14400),
    
    /**
     * Documents textuels (PDF, DOC, PPT, etc.).
     * 
     * <p><strong>Caractéristiques :</strong></p>
     * <ul>
     *   <li>Prévisualisation intégrée dans le navigateur</li>
     *   <li>Extraction de texte pour recherche full-text</li>
     *   <li>Génération de miniatures par page</li>
     *   <li>Conversion automatique en formats web</li>
     *   <li>Support de l'annotation et du surlignage</li>
     *   <li>Téléchargement sécurisé avec watermarking</li>
     * </ul>
     * 
     * <p><strong>Optimisations :</strong></p>
     * <ul>
     *   <li>Compression PDF avec préservation de qualité</li>
     *   <li>Lazy loading des pages volumineuses</li>
     *   <li>Cache des versions converties</li>
     *   <li>OCR automatique pour documents scannés</li>
     * </ul>
     * 
     * <p><strong>Types MIME supportés :</strong> application/pdf, application/msword, 
     * application/vnd.openxmlformats-officedocument.*, text/plain</p>
     * <p><strong>Taille max recommandée :</strong> 100 MB par fichier</p>
     */
    DOCUMENT("document", "Document", "fa-file-text", false, 104857600L, null),
    
    /**
     * Images et graphiques (PNG, JPG, SVG, etc.).
     * 
     * <p><strong>Caractéristiques :</strong></p>
     * <ul>
     *   <li>Redimensionnement automatique multi-résolution</li>
     *   <li>Optimisation de compression sans perte de qualité</li>
     *   <li>Support des formats vectoriels (SVG)</li>
     *   <li>Génération de formats WebP pour navigateurs modernes</li>
     *   <li>Détection automatique de contenu (diagrammes, captures d'écran)</li>
     *   <li>Support des images animées (GIF, WebP animé)</li>
     * </ul>
     * 
     * <p><strong>Optimisations :</strong></p>
     * <ul>
     *   <li>Responsive images avec srcset automatique</li>
     *   <li>Lazy loading avec placeholder flou</li>
     *   <li>CDN optimisé pour images avec transformation à la volée</li>
     *   <li>Compression adaptative selon le contexte d'affichage</li>
     * </ul>
     * 
     * <p><strong>Types MIME supportés :</strong> image/jpeg, image/png, image/gif, 
     * image/svg+xml, image/webp</p>
     * <p><strong>Taille max recommandée :</strong> 50 MB par fichier</p>
     */
    IMAGE("image", "Image", "fa-image", false, 52428800L, null),
    
    /**
     * Contenu interactif (Quiz, Simulations, H5P, etc.).
     * 
     * <p><strong>Caractéristiques :</strong></p>
     * <ul>
     *   <li>Intégration de frameworks interactifs (H5P, Articulate)</li>
     *   <li>Suivi des interactions et analytics avancées</li>
     *   <li>Support SCORM pour compatibilité LMS</li>
     *   <li>Sauvegarde automatique de progression</li>
     *   <li>Évaluation automatique avec feedback immédiat</li>
     *   <li>Adaptation au niveau de l'apprenant</li>
     * </ul>
     * 
     * <p><strong>Optimisations :</strong></p>
     * <ul>
     *   <li>Chargement progressif des ressources interactives</li>
     *   <li>Cache intelligent des états de progression</li>
     *   <li>Synchronisation temps réel multi-dispositifs</li>
     * </ul>
     * 
     * <p><strong>Types MIME supportés :</strong> application/zip (H5P), text/html, 
     * application/javascript</p>
     * <p><strong>Taille max recommandée :</strong> 200 MB par contenu</p>
     */
    INTERACTIVE("interactive", "Contenu interactif", "fa-gamepad", false, 209715200L, null),
    
    /**
     * Exemples de code avec coloration syntaxique.
     * 
     * <p><strong>Caractéristiques :</strong></p>
     * <ul>
     *   <li>Coloration syntaxique automatique multi-langages</li>
     *   <li>Exécution de code en sandbox sécurisé</li>
     *   <li>Versioning et historique des modifications</li>
     *   <li>Partage et collaboration en temps réel</li>
     *   <li>Intégration avec IDEs populaires</li>
     *   <li>Tests automatisés et validation de code</li>
     * </ul>
     * 
     * <p><strong>Optimisations :</strong></p>
     * <ul>
     *   <li>Highlight.js avec détection automatique de langage</li>
     *   <li>Compression intelligente préservant la lisibilité</li>
     *   <li>Cache des rendus de coloration syntaxique</li>
     * </ul>
     * 
     * <p><strong>Types MIME supportés :</strong> text/plain, text/x-python, text/x-java, 
     * text/javascript, text/x-c++src</p>
     * <p><strong>Taille max recommandée :</strong> 10 MB par fichier</p>
     */
    CODE_SAMPLE("code", "Exemple de code", "fa-code", false, 10485760L, null),
    
    /**
     * Archives ZIP avec projets complets.
     * 
     * <p><strong>Caractéristiques :</strong></p>
     * <ul>
     *   <li>Extraction et prévisualisation de contenu d'archive</li>
     *   <li>Analyse de structure de projet automatique</li>
     *   <li>Détection de technologies et frameworks</li>
     *   <li>Génération de documentation projet automatique</li>
     *   <li>Vérification de sécurité des archives</li>
     *   <li>Support des formats multiples (ZIP, TAR, 7Z)</li>
     * </ul>
     * 
     * <p><strong>Optimisations :</strong></p>
     * <ul>
     *   <li>Scan antivirus automatique avant extraction</li>
     *   <li>Décompression à la demande avec cache</li>
     *   <li>Limitation de profondeur d'extraction</li>
     * </ul>
     * 
     * <p><strong>Types MIME supportés :</strong> application/zip, application/x-tar, 
     * application/x-7z-compressed</p>
     * <p><strong>Taille max recommandée :</strong> 1 GB par archive</p>
     */
    ARCHIVE("archive", "Archive", "fa-file-archive", false, 1073741824L, null);

    /**
     * Identifiant technique du type de contenu.
     * Utilisé pour les APIs et le traitement programmatique.
     */
    private final String key;
    
    /**
     * Libellé d'affichage pour l'interface utilisateur.
     * Localisable et convivial pour les utilisateurs finaux.
     */
    private final String displayName;
    
    /**
     * Icône Font Awesome associée au type.
     * Pour affichage dans l'interface utilisateur.
     */
    private final String iconClass;
    
    /**
     * Indique si ce type de contenu a une durée.
     * True pour VIDEO et AUDIO, false pour les autres.
     */
    private final boolean hasDuration;
    
    /**
     * Taille maximale recommandée en octets.
     * Utilisée pour validation et quotas.
     */
    private final Long maxSizeBytes;
    
    /**
     * Durée maximale recommandée en secondes.
     * Applicable uniquement pour les contenus temporels.
     */
    private final Integer maxDurationSeconds;

    /**
     * Constructeur de l'énumération ContentType.
     * 
     * @param key Identifiant technique
     * @param displayName Libellé d'affichage
     * @param iconClass Classe d'icône Font Awesome
     * @param hasDuration Si le type a une notion de durée
     * @param maxSizeBytes Taille maximum en octets
     * @param maxDurationSeconds Durée maximum en secondes (peut être null)
     */
    ContentType(String key, String displayName, String iconClass, boolean hasDuration, 
                Long maxSizeBytes, Integer maxDurationSeconds) {
        this.key = key;
        this.displayName = displayName;
        this.iconClass = iconClass;
        this.hasDuration = hasDuration;
        this.maxSizeBytes = maxSizeBytes;
        this.maxDurationSeconds = maxDurationSeconds;
    }

    // Getters

    /**
     * Obtient l'identifiant technique du type.
     * 
     * @return L'identifiant technique
     */
    public String getKey() {
        return key;
    }

    /**
     * Obtient le libellé d'affichage du type.
     * 
     * @return Le libellé d'affichage
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Obtient la classe d'icône Font Awesome.
     * 
     * @return La classe d'icône
     */
    public String getIconClass() {
        return iconClass;
    }

    /**
     * Vérifie si ce type de contenu a une durée.
     * 
     * @return true si le type a une durée, false sinon
     */
    public boolean isHasDuration() {
        return hasDuration;
    }

    /**
     * Obtient la taille maximale recommandée.
     * 
     * @return La taille maximale en octets
     */
    public Long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    /**
     * Obtient la durée maximale recommandée.
     * 
     * @return La durée maximale en secondes (peut être null)
     */
    public Integer getMaxDurationSeconds() {
        return maxDurationSeconds;
    }

    // Méthodes utilitaires

    /**
     * Vérifie si un type MIME est compatible avec ce type de contenu.
     * 
     * @param mimeType Le type MIME à vérifier
     * @return true si compatible, false sinon
     */
    public boolean isCompatibleMimeType(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            return false;
        }
        
        String lowerMimeType = mimeType.toLowerCase();
        
        switch (this) {
            case VIDEO:
                return lowerMimeType.startsWith("video/");
            case AUDIO:
                return lowerMimeType.startsWith("audio/");
            case IMAGE:
                return lowerMimeType.startsWith("image/");
            case DOCUMENT:
                return lowerMimeType.startsWith("application/pdf") ||
                       lowerMimeType.startsWith("application/msword") ||
                       lowerMimeType.startsWith("application/vnd.openxmlformats-officedocument") ||
                       lowerMimeType.startsWith("text/");
            case INTERACTIVE:
                return lowerMimeType.equals("application/zip") ||
                       lowerMimeType.equals("text/html") ||
                       lowerMimeType.equals("application/javascript");
            case CODE_SAMPLE:
                return lowerMimeType.startsWith("text/") ||
                       lowerMimeType.equals("application/javascript");
            case ARCHIVE:
                return lowerMimeType.equals("application/zip") ||
                       lowerMimeType.equals("application/x-tar") ||
                       lowerMimeType.equals("application/x-7z-compressed");
            default:
                return false;
        }
    }

    /**
     * Détermine le type de contenu à partir d'un type MIME.
     * 
     * @param mimeType Le type MIME
     * @return Le ContentType correspondant ou null si non trouvé
     */
    public static ContentType fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            return null;
        }
        
        for (ContentType type : ContentType.values()) {
            if (type.isCompatibleMimeType(mimeType)) {
                return type;
            }
        }
        
        return null;
    }

    /**
     * Détermine le type de contenu à partir d'une extension de fichier.
     * 
     * @param filename Le nom du fichier avec extension
     * @return Le ContentType correspondant ou null si non trouvé
     */
    public static ContentType fromFileName(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        
        switch (extension) {
            // Vidéos
            case "mp4":
            case "avi":
            case "mov":
            case "wmv":
            case "flv":
            case "webm":
            case "mkv":
                return VIDEO;
                
            // Audio
            case "mp3":
            case "wav":
            case "aac":
            case "flac":
            case "ogg":
            case "m4a":
                return AUDIO;
                
            // Images
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
            case "svg":
            case "webp":
                return IMAGE;
                
            // Documents
            case "pdf":
            case "doc":
            case "docx":
            case "ppt":
            case "pptx":
            case "xls":
            case "xlsx":
            case "txt":
            case "rtf":
                return DOCUMENT;
                
            // Code
            case "java":
            case "js":
            case "py":
            case "cpp":
            case "c":
            case "html":
            case "css":
            case "sql":
            case "xml":
            case "json":
                return CODE_SAMPLE;
                
            // Archives
            case "zip":
            case "tar":
            case "gz":
            case "7z":
            case "rar":
                return ARCHIVE;
                
            default:
                return null;
        }
    }

    /**
     * Extrait l'extension d'un nom de fichier.
     * 
     * @param filename Le nom du fichier
     * @return L'extension sans le point, ou chaîne vide si pas d'extension
     */
    private static String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1);
    }

    /**
     * Formate la taille maximale en format lisible.
     * 
     * @return La taille maximale formatée
     */
    public String getFormattedMaxSize() {
        if (maxSizeBytes == null) {
            return "Non définie";
        }
        
        if (maxSizeBytes < 1024) {
            return maxSizeBytes + " B";
        } else if (maxSizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", maxSizeBytes / 1024.0);
        } else if (maxSizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", maxSizeBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", maxSizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Formate la durée maximale en format lisible.
     * 
     * @return La durée maximale formatée
     */
    public String getFormattedMaxDuration() {
        if (maxDurationSeconds == null) {
            return "Non applicable";
        }
        
        int hours = maxDurationSeconds / 3600;
        int minutes = (maxDurationSeconds % 3600) / 60;
        
        if (hours > 0) {
            return String.format("%d h %02d min", hours, minutes);
        } else {
            return String.format("%d min", minutes);
        }
    }
}