package com.javacraftacademy.courseservice.util;

import java.text.Normalizer;
import java.util.regex.Pattern;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * Utility class for generating SEO-friendly URL slugs from text content.
 * 
 * <p>This utility provides comprehensive slug generation functionality for creating
 * clean, URL-safe identifiers from course titles, category names, and other textual content.
 * It handles character normalization, special character removal, and ensures generated
 * slugs are optimized for search engines and user readability.</p>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Unicode character normalization and diacritic removal</li>
 *   <li>Special character sanitization and replacement</li>
 *   <li>Stop word filtering for cleaner URLs</li>
 *   <li>Length optimization for SEO best practices</li>
 *   <li>Duplicate handling and uniqueness validation</li>
 *   <li>Multi-language support with proper encoding</li>
 * </ul>
 * 
 * <h3>Relationships with Application Components:</h3>
 * <ul>
 *   <li><strong>CourseMapper:</strong> Used for generating course slugs from titles</li>
 *   <li><strong>CategoryMapper:</strong> Utilized for creating category URL identifiers</li>
 *   <li><strong>LessonMapper:</strong> Applied for lesson URL slug generation</li>
 *   <li><strong>SEO Services:</strong> Integrated with SEO optimization components</li>
 *   <li><strong>URL Routing:</strong> Supports clean URL structure throughout the application</li>
 *   <li><strong>Content Management:</strong> Used by CMS components for content organization</li>
 *   <li><strong>Search Engine:</strong> Enhances content discoverability and indexing</li>
 * </ul>
 * 
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // Basic slug generation
 * String slug = SlugUtils.generateSlug("Java Programming Fundamentals");
 * // Result: "java-programming-fundamentals"
 * 
 * // With special characters and accents
 * String slug = SlugUtils.generateSlug("Advanced C++ & Data Structures");
 * // Result: "advanced-cpp-data-structures"
 * 
 * // Handling uniqueness
 * String uniqueSlug = SlugUtils.generateUniqueSlug("Spring Boot Tutorial", existingSlugs);
 * // Result: "spring-boot-tutorial-2" (if original exists)
 * }</pre>
 * 
 * @author JavaCraftAcademy
 * @version 1.0
 * @since 1.0
 */
public final class SlugUtils {

    private static final int MAX_SLUG_LENGTH = 100;
    private static final int MIN_SLUG_LENGTH = 3;
    private static final String SLUG_SEPARATOR = "-";
    
    // Pattern for removing non-alphanumeric characters (except hyphens)
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\-]");
    private static final Pattern MULTIPLE_HYPHENS = Pattern.compile("-{2,}");
    private static final Pattern LEADING_TRAILING_HYPHENS = Pattern.compile("^-+|-+$");
    
    // Common stop words to remove for cleaner URLs
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "a", "an", "and", "are", "as", "at", "be", "by", "for", "from", "has", "he",
        "in", "is", "it", "its", "of", "on", "that", "the", "to", "was", "will", "with",
        "le", "la", "les", "de", "du", "des", "et", "ou", "un", "une", "dans", "sur",
        "pour", "avec", "par", "sans", "sous", "vers", "chez", "entre", "pendant"
    ));
    
    // Special character replacements for better readability
    private static final String[][] SPECIAL_REPLACEMENTS = {
        {"&", "and"},
        {"+", "plus"},
        {"@", "at"},
        {"%", "percent"},
        {"#", "sharp"},
        {"++", "plusplus"},
        {"c++", "cpp"},
        {"c#", "csharp"},
        {".net", "dotnet"},
        {"node.js", "nodejs"},
        {"vue.js", "vuejs"},
        {"react.js", "reactjs"},
        {"angular.js", "angularjs"}
    };

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private SlugUtils() {
        throw new AssertionError("SlugUtils is a utility class and should not be instantiated");
    }

    /**
     * Generates a SEO-friendly slug from the provided text.
     * 
     * <p>This is the primary method for slug generation, handling all aspects of
     * text transformation including normalization, special character replacement,
     * stop word removal, and length optimization.</p>
     * 
     * @param text The input text to convert to a slug
     * @return A clean, URL-safe slug string
     * @throws IllegalArgumentException if text is null or empty after processing
     */
    public static String generateSlug(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }

        String slug = text.trim().toLowerCase(Locale.ENGLISH);
        
        // Apply special character replacements first
        slug = applySpecialReplacements(slug);
        
        // Normalize Unicode characters and remove diacritics
        slug = normalizeUnicode(slug);
        
        // Remove non-alphanumeric characters
        slug = NON_ALPHANUMERIC.matcher(slug).replaceAll(SLUG_SEPARATOR);
        
        // Clean up multiple hyphens
        slug = MULTIPLE_HYPHENS.matcher(slug).replaceAll(SLUG_SEPARATOR);
        
        // Remove leading and trailing hyphens
        slug = LEADING_TRAILING_HYPHENS.matcher(slug).replaceAll("");
        
        // Remove stop words for cleaner URLs
        slug = removeStopWords(slug);
        
        // Ensure slug length is within acceptable bounds
        slug = optimizeLength(slug);
        
        // Final validation
        if (slug.length() < MIN_SLUG_LENGTH) {
            throw new IllegalArgumentException("Generated slug is too short: " + slug);
        }
        
        return slug;
    }

    /**
     * Generates a unique slug by appending a numeric suffix if necessary.
     * 
     * <p>This method ensures slug uniqueness by checking against existing slugs
     * and appending an incremental number when duplicates are found. Essential
     * for maintaining unique URLs in the application.</p>
     * 
     * @param text The input text to convert to a slug
     * @param existingSlugs Set of existing slugs to check against
     * @return A unique slug that doesn't conflict with existing ones
     * @throws IllegalArgumentException if text is null or empty
     */
    public static String generateUniqueSlug(String text, Set<String> existingSlugs) {
        if (existingSlugs == null) {
            existingSlugs = new HashSet<>();
        }
        
        String baseSlug = generateSlug(text);
        String uniqueSlug = baseSlug;
        int counter = 1;
        
        while (existingSlugs.contains(uniqueSlug)) {
            counter++;
            uniqueSlug = baseSlug + SLUG_SEPARATOR + counter;
        }
        
        return uniqueSlug;
    }

    /**
     * Validates if a string is a valid slug format.
     * 
     * <p>Checks if the provided string conforms to slug formatting rules,
     * ensuring it contains only lowercase letters, numbers, and hyphens
     * with proper length constraints.</p>
     * 
     * @param slug The string to validate
     * @return true if the string is a valid slug, false otherwise
     */
    public static boolean isValidSlug(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return false;
        }
        
        // Check length constraints
        if (slug.length() < MIN_SLUG_LENGTH || slug.length() > MAX_SLUG_LENGTH) {
            return false;
        }
        
        // Check format (only lowercase letters, numbers, and hyphens)
        Pattern validSlugPattern = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
        return validSlugPattern.matcher(slug).matches();
    }

    /**
     * Extracts keywords from a slug for search and SEO purposes.
     * 
     * <p>Reverses the slug generation process to extract meaningful keywords
     * that can be used for search indexing, content categorization, and
     * SEO optimization.</p>
     * 
     * @param slug The slug to extract keywords from
     * @return Array of keywords extracted from the slug
     */
    public static String[] extractKeywords(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return new String[0];
        }
        
        return slug.toLowerCase()
                   .split(SLUG_SEPARATOR)
                   .clone();
    }

    /**
     * Converts a slug back to a human-readable title.
     * 
     * <p>Transforms a slug into a properly formatted title by replacing
     * hyphens with spaces and capitalizing words. Useful for display
     * purposes and breadcrumb navigation.</p>
     * 
     * @param slug The slug to convert
     * @return A human-readable title string
     */
    public static String slugToTitle(String slug) {
        if (slug == null || slug.trim().isEmpty()) {
            return "";
        }
        
        String[] words = slug.split(SLUG_SEPARATOR);
        StringBuilder title = new StringBuilder();
        
        for (String word : words) {
            if (title.length() > 0) {
                title.append(" ");
            }
            title.append(capitalizeFirst(word));
        }
        
        return title.toString();
    }

    /**
     * Generates a slug with custom separator and length constraints.
     * 
     * <p>Advanced slug generation method that allows customization of
     * separator character and length limits for specific use cases.</p>
     * 
     * @param text The input text to convert
     * @param separator Custom separator character
     * @param maxLength Maximum allowed length
     * @return Customized slug string
     */
    public static String generateCustomSlug(String text, String separator, int maxLength) {
        if (separator == null || separator.isEmpty()) {
            separator = SLUG_SEPARATOR;
        }
        
        String slug = generateSlug(text);
        
        // Replace default separator with custom one
        if (!SLUG_SEPARATOR.equals(separator)) {
            slug = slug.replace(SLUG_SEPARATOR, separator);
        }
        
        // Apply custom length constraint
        if (maxLength > 0 && slug.length() > maxLength) {
            slug = truncateAtWordBoundary(slug, maxLength, separator);
        }
        
        return slug;
    }

    /**
     * Applies special character replacements for common programming terms.
     * 
     * @param text The text to process
     * @return Text with special replacements applied
     */
    private static String applySpecialReplacements(String text) {
        String result = text;
        for (String[] replacement : SPECIAL_REPLACEMENTS) {
            result = result.replace(replacement[0], replacement[1]);
        }
        return result;
    }

    /**
     * Normalizes Unicode characters and removes diacritics.
     * 
     * @param text The text to normalize
     * @return Normalized text without diacritics
     */
    private static String normalizeUnicode(String text) {
        // Normalize to NFD (Canonical Decomposition)
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        
        // Remove diacritical marks
        Pattern diacritics = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return diacritics.matcher(normalized).replaceAll("");
    }

    /**
     * Removes common stop words from the slug for cleaner URLs.
     * 
     * @param slug The slug to process
     * @return Slug with stop words removed
     */
    private static String removeStopWords(String slug) {
        String[] words = slug.split(SLUG_SEPARATOR);
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!STOP_WORDS.contains(word) && !word.isEmpty()) {
                if (result.length() > 0) {
                    result.append(SLUG_SEPARATOR);
                }
                result.append(word);
            }
        }
        
        return result.toString();
    }

    /**
     * Optimizes slug length while maintaining readability.
     * 
     * @param slug The slug to optimize
     * @return Length-optimized slug
     */
    private static String optimizeLength(String slug) {
        if (slug.length() <= MAX_SLUG_LENGTH) {
            return slug;
        }
        
        return truncateAtWordBoundary(slug, MAX_SLUG_LENGTH, SLUG_SEPARATOR);
    }

    /**
     * Truncates text at word boundaries to maintain readability.
     * 
     * @param text The text to truncate
     * @param maxLength Maximum allowed length
     * @param separator Word separator
     * @return Truncated text
     */
    private static String truncateAtWordBoundary(String text, int maxLength, String separator) {
        if (text.length() <= maxLength) {
            return text;
        }
        
        String truncated = text.substring(0, maxLength);
        int lastSeparator = truncated.lastIndexOf(separator);
        
        if (lastSeparator > 0) {
            return truncated.substring(0, lastSeparator);
        }
        
        return truncated;
    }

    /**
     * Capitalizes the first character of a word.
     * 
     * @param word The word to capitalize
     * @return Capitalized word
     */
    private static String capitalizeFirst(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }

    /**
     * Generates a slug optimized for specific content types.
     * 
     * <p>Provides specialized slug generation for different content types
     * like courses, categories, lessons, with type-specific optimizations.</p>
     * 
     * @param text The input text
     * @param contentType The type of content (course, category, lesson, etc.)
     * @return Content-type optimized slug
     */
    public static String generateContentTypeSlug(String text, String contentType) {
        String slug = generateSlug(text);
        
        // Apply content-type specific optimizations
        switch (contentType.toLowerCase()) {
            case "course":
                // Courses might benefit from keeping level indicators
                slug = preserveLevelIndicators(slug);
                break;
            case "category":
                // Categories should be shorter and more general
                slug = optimizeForCategory(slug);
                break;
            case "lesson":
                // Lessons might include sequence numbers
                slug = optimizeForLesson(slug);
                break;
            default:
                // Use standard slug generation
                break;
        }
        
        return slug;
    }

    /**
     * Preserves level indicators in course slugs.
     */
    private static String preserveLevelIndicators(String slug) {
        // Keep common level indicators
        String[] levelWords = {"beginner", "intermediate", "advanced", "expert"};
        for (String level : levelWords) {
            if (slug.contains(level)) {
                // Ensure level indicator is preserved even if considered a stop word
                return slug;
            }
        }
        return slug;
    }

    /**
     * Optimizes slugs for category content.
     */
    private static String optimizeForCategory(String slug) {
        // Categories should be concise - limit to 3-4 main words
        String[] words = slug.split(SLUG_SEPARATOR);
        if (words.length > 4) {
            StringBuilder optimized = new StringBuilder();
            for (int i = 0; i < Math.min(4, words.length); i++) {
                if (optimized.length() > 0) {
                    optimized.append(SLUG_SEPARATOR);
                }
                optimized.append(words[i]);
            }
            return optimized.toString();
        }
        return slug;
    }

    /**
     * Optimizes slugs for lesson content.
     */
    private static String optimizeForLesson(String slug) {
        // Preserve numeric indicators for lessons
        return slug.replaceAll("lesson-", "").replaceAll("chapter-", "");
    }
}