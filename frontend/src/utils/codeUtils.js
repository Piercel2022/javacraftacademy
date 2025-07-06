/**
 * @fileoverview Utilitaires pour l'analyse, la validation et la manipulation du code Java
 * 
 * Ce module fournit des fonctions utilitaires pour:
 * - Validation syntaxique du code Java
 * - Formatage et organisation du code
 * - Extraction de métadonnées (méthodes, classes, imports)
 * - Analyse de la complexité du code
 * - Génération de suggestions d'optimisation
 * - Détection de patterns et anti-patterns
 * 
 * Relations avec l'application:
 * - CodeEditor: Validation en temps réel et analyse du code
 * - compilerService: Préparation du code pour compilation
 * - courseService: Évaluation des exercices et progression
 * - progressService: Métriques de qualité du code
 * - LessonPlayer: Vérification des solutions d'exercices
 * - CodeRunner: Préparation avant exécution
 * 
 * @author JavaCraft Academy
 * @version 1.0.0
 */

/**
 * Expressions régulières pour l'analyse du code Java
 */
const JAVA_PATTERNS = {
    // Déclarations
    CLASS_DECLARATION: /(?:public|private|protected|abstract|final)?\s*class\s+(\w+)(?:\s+extends\s+\w+)?(?:\s+implements\s+[\w,\s]+)?\s*\{/g,
    INTERFACE_DECLARATION: /(?:public|private|protected)?\s*interface\s+(\w+)(?:\s+extends\s+[\w,\s]+)?\s*\{/g,
    METHOD_DECLARATION: /(?:public|private|protected|static|abstract|final|synchronized|native)*\s*(\w+(?:<[^>]+>)?)\s+(\w+)\s*\(([^)]*)\)\s*(?:throws\s+[\w,\s]+)?\s*[{;]/g,
    CONSTRUCTOR_DECLARATION: /(?:public|private|protected)?\s*(\w+)\s*\(([^)]*)\)\s*(?:throws\s+[\w,\s]+)?\s*\{/g,
    FIELD_DECLARATION: /(?:public|private|protected|static|final|transient|volatile)*\s*(\w+(?:<[^>]+>)?)\s+(\w+)(?:\s*=\s*[^;]+)?;/g,
    
    // Imports et packages
    IMPORT_STATEMENT: /import\s+(?:static\s+)?([^;]+);/g,
    PACKAGE_STATEMENT: /package\s+([^;]+);/g,
    
    // Structures de contrôle
    IF_STATEMENT: /\bif\s*\(/g,
    ELSE_STATEMENT: /\belse\b/g,
    WHILE_LOOP: /\bwhile\s*\(/g,
    FOR_LOOP: /\bfor\s*\(/g,
    DO_WHILE_LOOP: /\bdo\s*\{/g,
    SWITCH_STATEMENT: /\bswitch\s*\(/g,
    CASE_STATEMENT: /\bcase\s+[^:]+:/g,
    TRY_BLOCK: /\btry\s*\{/g,
    CATCH_BLOCK: /\bcatch\s*\(/g,
    FINALLY_BLOCK: /\bfinally\s*\{/g,
    
    // Opérateurs et expressions
    LOGICAL_AND: /&&/g,
    LOGICAL_OR: /\|\|/g,
    TERNARY_OPERATOR: /\?[^:]*:/g,
    
    // Commentaires
    SINGLE_LINE_COMMENT: /\/\/.*$/gm,
    MULTI_LINE_COMMENT: /\/\*[\s\S]*?\*\//g,
    JAVADOC_COMMENT: /\/\*\*[\s\S]*?\*\//g,
    
    // Littéraux et identifiants
    STRING_LITERAL: /"(?:[^"\\]|\\.)*"/g,
    CHAR_LITERAL: /'(?:[^'\\]|\\.)*'/g,
    NUMERIC_LITERAL: /\b\d+(?:\.\d+)?(?:[eE][+-]?\d+)?[fFdDlL]?\b/g,
    IDENTIFIER: /\b[a-zA-Z_$][a-zA-Z0-9_$]*\b/g,
    
    // Annotations
    ANNOTATION: /@\w+(?:\([^)]*\))?/g
};

/**
 * Mots-clés Java pour la validation syntaxique
 */
const JAVA_KEYWORDS = [
    'abstract', 'assert', 'boolean', 'break', 'byte', 'case', 'catch', 'char',
    'class', 'const', 'continue', 'default', 'do', 'double', 'else', 'enum',
    'extends', 'final', 'finally', 'float', 'for', 'goto', 'if', 'implements',
    'import', 'instanceof', 'int', 'interface', 'long', 'native', 'new', 'null',
    'package', 'private', 'protected', 'public', 'return', 'short', 'static',
    'strictfp', 'super', 'switch', 'synchronized', 'this', 'throw', 'throws',
    'transient', 'try', 'void', 'volatile', 'while', 'true', 'false'
];

/**
 * Types de données Java primitifs et objets courants
 */
const JAVA_TYPES = [
    'boolean', 'byte', 'char', 'short', 'int', 'long', 'float', 'double',
    'String', 'Object', 'Integer', 'Double', 'Float', 'Long', 'Short',
    'Boolean', 'Character', 'Byte', 'BigInteger', 'BigDecimal',
    'ArrayList', 'HashMap', 'HashSet', 'LinkedList', 'TreeMap', 'TreeSet',
    'List', 'Map', 'Set', 'Collection', 'Iterator', 'Comparable'
];

/**
 * Valide le code Java et retourne les erreurs de syntaxe
 * 
 * @param {string} code - Code Java à valider
 * @param {Object} options - Options de validation
 * @param {boolean} options.checkSyntax - Vérifier la syntaxe de base
 * @param {boolean} options.checkStyle - Vérifier le style de code
 * @param {boolean} options.checkComplexity - Vérifier la complexité
 * @param {number} options.maxComplexity - Complexité maximale autorisée
 * @returns {Array<Object>} Liste des erreurs trouvées
 */
export const validateJavaCode = (code, options = {}) => {
    const {
        checkSyntax = true,
        checkStyle = true,
        checkComplexity = false,
        maxComplexity = 10
    } = options;
    
    const errors = [];
    
    if (!code || typeof code !== 'string') {
        errors.push({
            type: 'error',
            message: 'Code invalide ou vide',
            line: 1,
            column: 1,
            length: 0,
            severity: 'error'
        });
        return errors;
    }
    
    // Validation syntaxique de base
    if (checkSyntax) {
        errors.push(...validateBasicSyntax(code));
        errors.push(...validateBraces(code));
        errors.push(...validateParentheses(code));
        errors.push(...validateSemicolons(code));
        errors.push(...validateStringLiterals(code));
    }
    
    // Vérification du style de code
    if (checkStyle) {
        errors.push(...validateCodeStyle(code));
    }
    
    // Vérification de la complexité
    if (checkComplexity) {
        const complexity = calculateCyclomaticComplexity(code);
        if (complexity > maxComplexity) {
            errors.push({
                type: 'warning',
                message: `Complexité cyclomatique trop élevée: ${complexity} (max: ${maxComplexity})`,
                line: 1,
                column: 1,
                length: 0,
                severity: 'warning'
            });
        }
    }
    
    return errors;
};

/**
 * Valide la syntaxe de base du code Java
 * 
 * @param {string} code - Code à valider
 * @returns {Array<Object>} Erreurs de syntaxe trouvées
 */
const validateBasicSyntax = (code) => {
    const errors = [];
    const lines = code.split('\n');
    
    lines.forEach((line, index) => {
        const lineNumber = index + 1;
        const trimmedLine = line.trim();
        
        // Vérifier les mots-clés invalides
        const words = trimmedLine.split(/\s+/);
        words.forEach((word, wordIndex) => {
            if (word && !isValidJavaIdentifier(word) && !JAVA_KEYWORDS.includes(word) && 
                !JAVA_TYPES.includes(word) && !isNumericLiteral(word) && 
                !isStringLiteral(word) && !isOperator(word)) {
                
                // Vérifier si c'est un identifiant valide
                if (!/^[a-zA-Z_$][a-zA-Z0-9_$]*$/.test(word)) {
                    errors.push({
                        type: 'error',
                        message: `Identifiant invalide: ${word}`,
                        line: lineNumber,
                        column: line.indexOf(word) + 1,
                        length: word.length,
                        severity: 'error'
                    });
                }
            }
        });
        
        // Vérifier la structure des déclarations de classe
        if (trimmedLine.includes('class ')) {
            const classMatch = trimmedLine.match(/class\s+(\w+)/);
            if (classMatch) {
                const className = classMatch[1];
                if (!/^[A-Z][a-zA-Z0-9_]*$/.test(className)) {
                    errors.push({
                        type: 'warning',
                        message: `Le nom de classe devrait commencer par une majuscule: ${className}`,
                        line: lineNumber,
                        column: line.indexOf(className) + 1,
                        length: className.length,
                        severity: 'warning'
                    });
                }
            }
        }
        
        // Vérifier la structure des méthodes
        if (trimmedLine.includes('(') && trimmedLine.includes(')') && 
            (trimmedLine.includes('public') || trimmedLine.includes('private') || 
             trimmedLine.includes('protected') || trimmedLine.includes('static'))) {
            const methodMatch = trimmedLine.match(/(\w+)\s*\(/);
            if (methodMatch) {
                const methodName = methodMatch[1];
                if (!/^[a-z][a-zA-Z0-9_]*$/.test(methodName) && methodName !== 'main') {
                    errors.push({
                        type: 'warning',
                        message: `Le nom de méthode devrait commencer par une minuscule: ${methodName}`,
                        line: lineNumber,
                        column: line.indexOf(methodName) + 1,
                        length: methodName.length,
                        severity: 'warning'
                    });
                }
            }
        }
    });
    
    return errors;
};

/**
 * Valide l'équilibrage des accolades
 * 
 * @param {string} code - Code à valider
 * @returns {Array<Object>} Erreurs d'accolades trouvées
 */
const validateBraces = (code) => {
    const errors = [];
    const lines = code.split('\n');
    let braceCount = 0;
    let braceStack = [];
    
    lines.forEach((line, index) => {
        const lineNumber = index + 1;
        
        for (let i = 0; i < line.length; i++) {
            const char = line[i];
            
            if (char === '{') {
                braceCount++;
                braceStack.push({ line: lineNumber, column: i + 1 });
            } else if (char === '}') {
                braceCount--;
                if (braceCount < 0) {
                    errors.push({
                        type: 'error',
                        message: 'Accolade fermante sans ouverture correspondante',
                        line: lineNumber,
                        column: i + 1,
                        length: 1,
                        severity: 'error'
                    });
                } else {
                    braceStack.pop();
                }
            }
        }
    });
    
    // Vérifier les accolades non fermées
    if (braceCount > 0) {
        braceStack.forEach(brace => {
            errors.push({
                type: 'error',
                message: 'Accolade ouvrante sans fermeture correspondante',
                line: brace.line,
                column: brace.column,
                length: 1,
                severity: 'error'
            });
        });
    }
    
    return errors;
};

/**
 * Valide l'équilibrage des parenthèses
 * 
 * @param {string} code - Code à valider
 * @returns {Array<Object>} Erreurs de parenthèses trouvées
 */
const validateParentheses = (code) => {
    const errors = [];
    const lines = code.split('\n');
    let parenCount = 0;
    let parenStack = [];
    
    lines.forEach((line, index) => {
        const lineNumber = index + 1;
        
        for (let i = 0; i < line.length; i++) {
            const char = line[i];
            
            if (char === '(') {
                parenCount++;
                parenStack.push({ line: lineNumber, column: i + 1 });
            } else if (char === ')') {
                parenCount--;
                if (parenCount < 0) {
                    errors.push({
                        type: 'error',
                        message: 'Parenthèse fermante sans ouverture correspondante',
                        line: lineNumber,
                        column: i + 1,
                        length: 1,
                        severity: 'error'
                    });
                } else {
                    parenStack.pop();
                }
            }
        }
    });
    
    // Vérifier les parenthèses non fermées
    if (parenCount > 0) {
        parenStack.forEach(paren => {
            errors.push({
                type: 'error',
                message: 'Parenthèse ouvrante sans fermeture correspondante',
                line: paren.line,
                column: paren.column,
                length: 1,
                severity: 'error'
            });
        });
    }
    
    return errors;
};

/**
 * Valide les points-virgules manquants
 * 
 * @param {string} code - Code à valider
 * @returns {Array<Object>} Erreurs de points-virgules trouvées
 */
const validateSemicolons = (code) => {
    const errors = [];
    const lines = code.split('\n');
    
    lines.forEach((line, index) => {
        const lineNumber = index + 1;
        const trimmedLine = line.trim();
        
        // Ignorer les lignes vides, commentaires et structures de contrôle
        if (!trimmedLine || trimmedLine.startsWith('//') || trimmedLine.startsWith('/*') ||
            trimmedLine.startsWith('*') || trimmedLine.endsWith('{') || trimmedLine.endsWith('}') ||
            trimmedLine.includes('if ') || trimmedLine.includes('else') || trimmedLine.includes('for ') ||
            trimmedLine.includes('while ') || trimmedLine.includes('switch ') || trimmedLine.includes('case ') ||
            trimmedLine.includes('try') || trimmedLine.includes('catch') || trimmedLine.includes('finally') ||
            trimmedLine.includes('class ') || trimmedLine.includes('interface ') || trimmedLine.includes('enum ') ||
            trimmedLine.includes('package ') || trimmedLine.includes('import ')) {
            return;
        }
        
        // Vérifier si la ligne devrait se terminer par un point-virgule
        if (trimmedLine.length > 0 && !trimmedLine.endsWith(';') && !trimmedLine.endsWith('{') && 
            !trimmedLine.endsWith('}') && !trimmedLine.endsWith('*/')) {
            
            // Vérifier si c'est une déclaration ou une instruction
            if (trimmedLine.includes('=') || trimmedLine.includes('return') || 
                trimmedLine.includes('System.out.println') || trimmedLine.includes('new ') ||
                /^\w+\s*\(.*\)$/.test(trimmedLine)) {
                
                errors.push({
                    type: 'error',
                    message: 'Point-virgule manquant à la fin de l\'instruction',
                    line: lineNumber,
                    column: line.length,
                    length: 1,
                    severity: 'error'
                });
            }
        }
    });
    
    return errors;
};

/**
 * Valide les littéraux de chaînes
 * 
 * @param {string} code - Code à valider
 * @returns {Array<Object>} Erreurs de chaînes trouvées
 */
const validateStringLiterals = (code) => {
    const errors = [];
    const lines = code.split('\n');
    
    lines.forEach((line, index) => {
        const lineNumber = index + 1;
        
        // Vérifier les chaînes non terminées
        let inString = false;
        let stringStart = -1;
        
        for (let i = 0; i < line.length; i++) {
            const char = line[i];
            const prevChar = i > 0 ? line[i - 1] : '';
            
            if (char === '"' && prevChar !== '\\') {
                if (!inString) {
                    inString = true;
                    stringStart = i;
                } else {
                    inString = false;
                }
            }
        }
        
        if (inString) {
            errors.push({
                type: 'error',
                message: 'Chaîne de caractères non terminée',
                line: lineNumber,
                column: stringStart + 1,
                length: line.length - stringStart,
                severity: 'error'
            });
        }
    });
    
    return errors;
};

/**
 * Valide le style de code Java
 * 
 * @param {string} code - Code à valider
 * @returns {Array<Object>} Avertissements de style trouvés
 */
const validateCodeStyle = (code) => {
    const errors = [];
    const lines = code.split('\n');
    
    lines.forEach((line, index) => {
        const lineNumber = index + 1;
        
        // Vérifier l'indentation
        const leadingSpaces = line.match(/^(\s*)/)[1].length;
        if (leadingSpaces % 4 !== 0 && line.trim().length > 0) {
            errors.push({
                type: 'warning',
                message: 'Indentation incorrecte (utilisez 4 espaces)',
                line: lineNumber,
                column: 1,
                length: leadingSpaces,
                severity: 'warning'
            });
        }
        
        // Vérifier les lignes trop longues
        if (line.length > 120) {
            errors.push({
                type: 'warning',
                message: 'Ligne trop longue (max 120 caractères)',
                line: lineNumber,
                column: 121,
                length: line.length - 120,
                severity: 'warning'
            });
        }
        
        // Vérifier les espaces avant/après les opérateurs
        const operators = ['=', '+', '-', '*', '/', '%', '==', '!=', '<', '>', '<=', '>=', '&&', '||'];
        operators.forEach(op => {
            const regex = new RegExp(`\\S${op}\\S`, 'g');
            if (regex.test(line)) {
                errors.push({
                    type: 'warning',
                    message: `Ajoutez des espaces autour de l'opérateur '${op}'`,
                    line: lineNumber,
                    column: line.indexOf(op) + 1,
                    length: op.length,
                    severity: 'warning'
                });
            }
        });
    });
    
    return errors;
};

/**
 * Formate le code Java selon les conventions standard
 * 
 * @param {string} code - Code à formater
 * @param {Object} options - Options de formatage
 * @param {number} options.indentSize - Taille d'indentation (défaut: 4)
 * @param {boolean} options.insertSpaces - Utiliser des espaces (défaut: true)
 * @param {number} options.maxLineLength - Longueur maximale des lignes
 * @returns {Promise<string>} Code formaté
 */
export const formatCode = async (code, options = {}) => {
    const {
        indentSize = 4,
        insertSpaces = true,
        maxLineLength = 120
    } = options;
    
    if (!code || typeof code !== 'string') {
        return '';
    }
    
    try {
        let formattedCode = code;
        
        // Normaliser les fins de ligne
        formattedCode = formattedCode.replace(/\r\n/g, '\n').replace(/\r/g, '\n');
        
        // Supprimer les espaces en fin de ligne
        formattedCode = formattedCode.replace(/[ \t]+$/gm, '');
        
        // Formater l'indentation
        formattedCode = formatIndentation(formattedCode, indentSize, insertSpaces);
        
        // Formater les espaces autour des opérateurs
        formattedCode = formatOperatorSpacing(formattedCode);
        
        // Formater les accolades
        formattedCode = formatBraces(formattedCode);
        
        // Formater les déclarations d'imports
        formattedCode = formatImports(formattedCode);
        
        // Supprimer les lignes vides multiples
        formattedCode = formattedCode.replace(/\n\s*\n\s*\n/g, '\n\n');
        
        return formattedCode;
        
    } catch (error) {
        console.error('Erreur lors du formatage:', error);
        return code; // Retourner le code original en cas d'erreur
    }
};

/**
 * Formate l'indentation du code
 * 
 * @param {string} code - Code à indenter
 * @param {number} indentSize - Taille d'indentation
 * @param {boolean} insertSpaces - Utiliser des espaces
 * @returns {string} Code indenté
 */
const formatIndentation = (code, indentSize, insertSpaces) => {
    const lines = code.split('\n');
    let indentLevel = 0;
    const indentChar = insertSpaces ? ' '.repeat(indentSize) : '\t';
    
    return lines.map(line => {
        const trimmedLine = line.trim();
        
        if (!trimmedLine) return '';
        
        // Diminuer l'indentation pour les accolades fermantes
        if (trimmedLine.startsWith('}')) {
            indentLevel = Math.max(0, indentLevel - 1);
        }
        
        // Diminuer l'indentation pour case et default
        if (trimmedLine.startsWith('case ') || trimmedLine.startsWith('default:')) {
            indentLevel = Math.max(0, indentLevel - 1);
        }
        
        const indentedLine = indentChar.repeat(indentLevel) + trimmedLine;
        
        // Augmenter l'indentation pour les accolades ouvrantes
        if (trimmedLine.endsWith('{') || trimmedLine.endsWith(': {')) {
            indentLevel++;
        }
        
        // Augmenter l'indentation pour case et default
        if (trimmedLine.startsWith('case ') || trimmedLine.startsWith('default:')) {
            indentLevel++;
        }
        
        return indentedLine;
    }).join('\n');
};

/**
 * Formate les espaces autour des opérateurs
 * 
 * @param {string} code - Code à formater
 * @returns {string} Code formaté
 */
const formatOperatorSpacing = (code) => {
    const operators = [
        { op: '=', regex: /(\S)=(\S)/g, replacement: '$1 = $2' },
        { op: '+', regex: /(\S)\+(\S)/g, replacement: '$1 + $2' },
        { op: '-', regex: /(\S)-(\S)/g, replacement: '$1 - $2' },
        { op: '*', regex: /(\S)\*(\S)/g, replacement: '$1 * $2' },
        { op: '/', regex: /(\S)\/(\S)/g, replacement: '$1 / $2' },
        { op: '%', regex: /(\S)%(\S)/g, replacement: '$1 % $2' },
        { op: '==', regex: /(\S)==(\S)/g, replacement: '$1 == $2' },
        { op: '!=', regex: /(\S)!=(\S)/g, replacement: '$1 != $2' },
        { op: '<', regex: /(\S)<(\S)/g, replacement: '$1 < $2' },
        { op: '>', regex: /(\S)>(\S)/g, replacement: '$1 > $2' },
        { op: '<=', regex: /(\S)<=(\S)/g, replacement: '$1 <= $2' },
        { op: '>=', regex: /(\S)>=(\S)/g, replacement: '$1 >= $2' },
        { op: '&&', regex: /(\S)&&(\S)/g, replacement: '$1 && $2' },
        { op: '||', regex: /(\S)\|\|(\S)/g, replacement: '$1 || $2' }
    ];
    
    let formattedCode = code;
    
    operators.forEach(({ regex, replacement }) => {
        formattedCode = formattedCode.replace(regex, replacement);
    });
    
    return formattedCode;
};

/**
 * Formate les accolades selon le style Java
 * 
 * @param {string} code - Code à formater
 * @returns {string} Code formaté
 */
const formatBraces = (code) => {
    let formattedCode = code;
    
    // Assurer que les accolades ouvrantes sont sur la même ligne
    formattedCode = formattedCode.replace(/\n\s*\{/g, ' {');
    
    // Assurer une nouvelle ligne après les accolades ouvrantes
    formattedCode = formattedCode.replace(/\{\s*([^\s}])/g, '{\n$1');
    
    // Assurer une nouvelle ligne avant les accolades fermantes
    formattedCode = formattedCode.replace(/([^\s{])\s*\}/g, '$1\n}');
    
    return formattedCode;
};

/**
 * Formate les déclarations d'imports
 * 
 * @param {string} code - Code à formater
 * @returns {string} Code formaté
 */
const formatImports = (code) => {
    const lines = code.split('\n');
    const imports = [];
    const otherLines = [];
    let packageLine = '';
    
    lines.forEach(line => {
        const trimmedLine = line.trim();
        if (trimmedLine.startsWith('package ')) {
            packageLine = trimmedLine;
        } else if (trimmedLine.startsWith('import ')) {
            imports.push(trimmedLine);
        } else {
            otherLines.push(line);
        }
    });
    
    // Trier les imports
    imports.sort((a, b) => {
        // java.* en premier
        if (a.includes('java.') && !b.includes('java.')) return -1;
        if (!a.includes('java.') && b.includes('java.')) return 1;
        
        // javax.* en second
        if (a.includes('javax.') && !b.includes('javax.')) return -1;
        if (!a.includes('javax.') && b.includes('javax.')) return 1;
        
        // Ordre alphabétique
        return a.localeCompare(b);
    });
    
    // Reconstruire le code
    const result = [];
    if (packageLine) {
        result.push(packageLine);
        result.push('');
    }
    
    if (imports.length > 0) {
        result.push(...imports);
        result.push('');
    }
    
    result.push(...otherLines);
    
    return result.join('\n');
};

/**
 * Extrait les signatures des méthodes du code Java
 * 
 * @param {string} code - Code Java à analyser
 * @returns {Array<Object>} Liste des méthodes trouvées
 */
export const extractMethodSignatures = (code) => {
    const methods = [];
    const lines = code.split('\n');
    
    // Supprimer les commentaires pour une analyse plus précise
    const cleanCode = removeComments(code);
    
    let match;
    JAVA_PATTERNS.METHOD_DECLARATION.lastIndex = 0;
    
    while ((match = JAVA_PATTERNS.METHOD_DECLARATION.exec(cleanCode)) !== null) {
            const [fullMatch, returnType, methodName, parameters] = match;
            
            // Calculer la ligne de la méthode
            const beforeMatch = cleanCode.substring(0, match.index);
            const lineNumber = beforeMatch.split('\n').length;
            
            // Parser les paramètres
            const parsedParameters = parseParameters(parameters);
            
            // Extraire les modificateurs
            const modifiers = extractModifiers(fullMatch);
            
            methods.push({
                name: methodName,
                returnType: returnType,
                parameters: parsedParameters,
                modifiers: modifiers,
                line: lineNumber,
                signature: fullMatch.trim()
            });
        }
        
        return methods;
    };

    /**
     * Extrait les classes du code Java
     * 
     * @param {string} code - Code Java à analyser
     * @returns {Array<Object>} Liste des classes trouvées
     */
    export const extractClasses = (code) => {
        const classes = [];
        const cleanCode = removeComments(code);
        
        let match;
        JAVA_PATTERNS.CLASS_DECLARATION.lastIndex = 0;
        
        while ((match = JAVA_PATTERNS.CLASS_DECLARATION.exec(cleanCode)) !== null) {
            const [fullMatch, className] = match;
            
            // Calculer la ligne de la classe
            const beforeMatch = cleanCode.substring(0, match.index);
            const lineNumber = beforeMatch.split('\n').length;
            
            // Extraire les modificateurs
            const modifiers = extractModifiers(fullMatch);
            
            // Extraire l'extension et les implémentations
            const extendsMatch = fullMatch.match(/extends\s+(\w+)/);
            const implementsMatch = fullMatch.match(/implements\s+([\w,\s]+)/);
            
            classes.push({
                name: className,
                modifiers: modifiers,
                extends: extendsMatch ? extendsMatch[1] : null,
                implements: implementsMatch ? implementsMatch[1].split(',').map(s => s.trim()) : [],
                line: lineNumber,
                signature: fullMatch.trim()
            });
        }
        
        return classes;
    };

    /**
     * Extrait les interfaces du code Java
     * 
     * @param {string} code - Code Java à analyser
     * @returns {Array<Object>} Liste des interfaces trouvées
     */
    export const extractInterfaces = (code) => {
        const interfaces = [];
        const cleanCode = removeComments(code);
        
        let match;
        JAVA_PATTERNS.INTERFACE_DECLARATION.lastIndex = 0;
        
        while ((match = JAVA_PATTERNS.INTERFACE_DECLARATION.exec(cleanCode)) !== null) {
            const [fullMatch, interfaceName] = match;
            
            // Calculer la ligne de l'interface
            const beforeMatch = cleanCode.substring(0, match.index);
            const lineNumber = beforeMatch.split('\n').length;
            
            // Extraire les modificateurs
            const modifiers = extractModifiers(fullMatch);
            
            // Extraire les extensions
            const extendsMatch = fullMatch.match(/extends\s+([\w,\s]+)/);
            
            interfaces.push({
                name: interfaceName,
                modifiers: modifiers,
                extends: extendsMatch ? extendsMatch[1].split(',').map(s => s.trim()) : [],
                line: lineNumber,
                signature: fullMatch.trim()
            });
        }
        
        return interfaces;
    };

    /**
     * Extrait les champs (variables d'instance) du code Java
     * 
     * @param {string} code - Code Java à analyser
     * @returns {Array<Object>} Liste des champs trouvés
     */
    export const extractFields = (code) => {
        const fields = [];
        const cleanCode = removeComments(code);
        
        let match;
        JAVA_PATTERNS.FIELD_DECLARATION.lastIndex = 0;
        
        while ((match = JAVA_PATTERNS.FIELD_DECLARATION.exec(cleanCode)) !== null) {
            const [fullMatch, fieldType, fieldName] = match;
            
            // Calculer la ligne du champ
            const beforeMatch = cleanCode.substring(0, match.index);
            const lineNumber = beforeMatch.split('\n').length;
            
            // Extraire les modificateurs
            const modifiers = extractModifiers(fullMatch);
            
            // Extraire la valeur d'initialisation
            const initMatch = fullMatch.match(/=\s*([^;]+)/);
            
            fields.push({
                name: fieldName,
                type: fieldType,
                modifiers: modifiers,
                initialValue: initMatch ? initMatch[1].trim() : null,
                line: lineNumber,
                signature: fullMatch.trim()
            });
        }
        
        return fields;
    };

    /**
     * Extrait les imports du code Java
     * 
     * @param {string} code - Code Java à analyser
     * @returns {Array<Object>} Liste des imports trouvés
     */
    export const extractImports = (code) => {
        const imports = [];
        
        let match;
        JAVA_PATTERNS.IMPORT_STATEMENT.lastIndex = 0;
        
        while ((match = JAVA_PATTERNS.IMPORT_STATEMENT.exec(code)) !== null) {
            const [fullMatch, importPath] = match;
            
            // Calculer la ligne de l'import
            const beforeMatch = code.substring(0, match.index);
            const lineNumber = beforeMatch.split('\n').length;
            
            const isStatic = fullMatch.includes('static');
            const isWildcard = importPath.endsWith('*');
            
            imports.push({
                path: importPath.trim(),
                isStatic: isStatic,
                isWildcard: isWildcard,
                line: lineNumber,
                statement: fullMatch.trim()
            });
        }
        
        return imports;
    };

    /**
     * Calcule la complexité cyclomatique du code Java
     * 
     * @param {string} code - Code Java à analyser
     * @returns {number} Complexité cyclomatique
     */
    export const calculateCyclomaticComplexity = (code) => {
        let complexity = 1; // Complexité de base
        
        // Compter les structures de contrôle
        const controlStructures = [
            JAVA_PATTERNS.IF_STATEMENT,
            JAVA_PATTERNS.WHILE_LOOP,
            JAVA_PATTERNS.FOR_LOOP,
            JAVA_PATTERNS.DO_WHILE_LOOP,
            JAVA_PATTERNS.SWITCH_STATEMENT,
            JAVA_PATTERNS.CASE_STATEMENT,
            JAVA_PATTERNS.CATCH_BLOCK,
            JAVA_PATTERNS.LOGICAL_AND,
            JAVA_PATTERNS.LOGICAL_OR,
            JAVA_PATTERNS.TERNARY_OPERATOR
        ];
        
        controlStructures.forEach(pattern => {
            const matches = code.match(pattern);
            if (matches) {
                complexity += matches.length;
            }
        });
        
        return complexity;
    };

    /**
     * Analyse la qualité du code Java
     * 
     * @param {string} code - Code Java à analyser
     * @returns {Object} Métriques de qualité
     */
    export const analyzeCodeQuality = (code) => {
        const metrics = {
            linesOfCode: 0,
            linesOfComments: 0,
            linesBlank: 0,
            cyclomaticComplexity: 0,
            maintainabilityIndex: 0,
            methods: [],
            classes: [],
            interfaces: [],
            fields: [],
            imports: [],
            codeSmells: []
        };
        
        if (!code || typeof code !== 'string') {
            return metrics;
        }
        
        const lines = code.split('\n');
        
        // Analyser les lignes
        lines.forEach(line => {
            const trimmedLine = line.trim();
            
            if (!trimmedLine) {
                metrics.linesBlank++;
            } else if (trimmedLine.startsWith('//') || trimmedLine.startsWith('/*') || 
                       trimmedLine.startsWith('*') || trimmedLine.endsWith('*/')) {
                metrics.linesOfComments++;
            } else {
                metrics.linesOfCode++;
            }
        });
        
        // Extraire les éléments du code
        metrics.methods = extractMethodSignatures(code);
        metrics.classes = extractClasses(code);
        metrics.interfaces = extractInterfaces(code);
        metrics.fields = extractFields(code);
        metrics.imports = extractImports(code);
        
        // Calculer la complexité cyclomatique
        metrics.cyclomaticComplexity = calculateCyclomaticComplexity(code);
        
        // Calculer l'indice de maintenabilité (simplifié)
        const halsteadVolume = Math.log2(metrics.linesOfCode + 1) * 10;
        const commentRatio = metrics.linesOfComments / (metrics.linesOfCode + metrics.linesOfComments + 1);
        
        metrics.maintainabilityIndex = Math.max(0, 
            171 - 5.2 * Math.log(halsteadVolume) - 
            0.23 * metrics.cyclomaticComplexity - 
            16.2 * Math.log(metrics.linesOfCode + 1) + 
            50 * Math.sin(Math.sqrt(2.4 * commentRatio))
        );
        
        // Détecter les code smells
        metrics.codeSmells = detectCodeSmells(code, metrics);
        
        return metrics;
    };

    /**
     * Détecte les code smells dans le code Java
     * 
     * @param {string} code - Code Java à analyser
     * @param {Object} metrics - Métriques du code
     * @returns {Array<Object>} Liste des code smells détectés
     */
    const detectCodeSmells = (code, metrics) => {
        const smells = [];
        
        // Méthodes trop longues
        metrics.methods.forEach(method => {
            if (method.parameters.length > 5) {
                smells.push({
                    type: 'Long Parameter List',
                    message: `La méthode ${method.name} a trop de paramètres (${method.parameters.length})`,
                    line: method.line,
                    severity: 'warning'
                });
            }
        });
        
        // Classes trop grandes
        if (metrics.methods.length > 20) {
            smells.push({
                type: 'Large Class',
                message: `La classe contient trop de méthodes (${metrics.methods.length})`,
                line: 1,
                severity: 'warning'
            });
        }
        
        // Complexité cyclomatique élevée
        if (metrics.cyclomaticComplexity > 15) {
            smells.push({
                type: 'High Cyclomatic Complexity',
                message: `Complexité cyclomatique élevée (${metrics.cyclomaticComplexity})`,
                line: 1,
                severity: 'warning'
            });
        }
        
        // Manque de commentaires
        const commentRatio = metrics.linesOfComments / (metrics.linesOfCode + 1);
        if (commentRatio < 0.1 && metrics.linesOfCode > 50) {
            smells.push({
                type: 'Insufficient Comments',
                message: `Pas assez de commentaires (${Math.round(commentRatio * 100)}%)`,
                line: 1,
                severity: 'info'
            });
        }
        
        // Méthodes dupliquées (noms similaires)
        const methodNames = metrics.methods.map(m => m.name);
        const duplicates = methodNames.filter((name, index) => 
            methodNames.indexOf(name) !== index
        );
        
        if (duplicates.length > 0) {
            smells.push({
                type: 'Duplicate Method Names',
                message: `Méthodes avec des noms dupliqués: ${duplicates.join(', ')}`,
                line: 1,
                severity: 'warning'
            });
        }
        
        return smells;
    };

    /**
     * Génère des suggestions d'optimisation pour le code Java
     * 
     * @param {string} code - Code Java à analyser
     * @returns {Array<Object>} Liste des suggestions
     */
    export const generateOptimizationSuggestions = (code) => {
        const suggestions = [];
        const metrics = analyzeCodeQuality(code);
        
        // Suggestions basées sur la complexité
        if (metrics.cyclomaticComplexity > 10) {
            suggestions.push({
                type: 'complexity',
                priority: 'high',
                title: 'Réduire la complexité cyclomatique',
                description: 'Diviser les méthodes complexes en méthodes plus petites',
                impact: 'Améliore la lisibilité et la maintenabilité'
            });
        }
        
        // Suggestions basées sur les métriques
        if (metrics.methods.length > 15) {
            suggestions.push({
                type: 'structure',
                priority: 'medium',
                title: 'Diviser la classe',
                description: 'Considérer diviser cette classe en plusieurs classes plus petites',
                impact: 'Améliore la cohésion et réduit le couplage'
            });
        }
        
        // Suggestions basées sur les commentaires
        const commentRatio = metrics.linesOfComments / (metrics.linesOfCode + 1);
        if (commentRatio < 0.15) {
            suggestions.push({
                type: 'documentation',
                priority: 'medium',
                title: 'Ajouter plus de commentaires',
                description: 'Documenter les méthodes et classes importantes',
                impact: 'Améliore la compréhension du code'
            });
        }
        
        // Suggestions basées sur les patterns
        const lines = code.split('\n');
        let hasStringConcatenation = false;
        
        lines.forEach((line, index) => {
            // Détecter la concaténation de chaînes dans les boucles
            if (line.includes('+=') && line.includes('"') && 
                (code.includes('for') || code.includes('while'))) {
                hasStringConcatenation = true;
            }
        });
        
        if (hasStringConcatenation) {
            suggestions.push({
                type: 'performance',
                priority: 'medium',
                title: 'Utiliser StringBuilder',
                description: 'Remplacer la concaténation de chaînes par StringBuilder',
                impact: 'Améliore les performances pour les chaînes longues'
            });
        }
        
        // Suggestions basées sur les imports
        if (metrics.imports.some(imp => imp.isWildcard)) {
            suggestions.push({
                type: 'style',
                priority: 'low',
                title: 'Éviter les imports wildcard',
                description: 'Importer explicitement les classes utilisées',
                impact: 'Améliore la lisibilité et évite les conflits'
            });
        }
        
        return suggestions;
    };

    /**
     * Fonctions utilitaires
     */

    /**
     * Supprime les commentaires du code Java
     * 
     * @param {string} code - Code Java
     * @returns {string} Code sans commentaires
     */
    const removeComments = (code) => {
        let result = code;
        
        // Supprimer les commentaires multilignes
        result = result.replace(JAVA_PATTERNS.MULTI_LINE_COMMENT, '');
        result = result.replace(JAVA_PATTERNS.JAVADOC_COMMENT, '');
        
        // Supprimer les commentaires d'une ligne
        result = result.replace(JAVA_PATTERNS.SINGLE_LINE_COMMENT, '');
        
        return result;
    };

    /**
     * Parse les paramètres d'une méthode
     * 
     * @param {string} parametersString - Chaîne des paramètres
     * @returns {Array<Object>} Liste des paramètres parsés
     */
    const parseParameters = (parametersString) => {
        if (!parametersString || !parametersString.trim()) {
            return [];
        }
        
        const parameters = [];
        const paramParts = parametersString.split(',');
        
        paramParts.forEach(param => {
            const trimmedParam = param.trim();
            if (trimmedParam) {
                const parts = trimmedParam.split(/\s+/);
                if (parts.length >= 2) {
                    const type = parts[0];
                    const name = parts[1];
                    
                    parameters.push({
                        type: type,
                        name: name,
                        isFinal: trimmedParam.includes('final'),
                        isVarArgs: trimmedParam.includes('...')
                    });
                }
            }
        });
        
        return parameters;
    };

    /**
     * Extrait les modificateurs d'une déclaration
     * 
     * @param {string} declaration - Déclaration à analyser
     * @returns {Array<string>} Liste des modificateurs
     */
    const extractModifiers = (declaration) => {
        const modifiers = [];
        const modifierKeywords = [
            'public', 'private', 'protected', 'static', 'final', 'abstract',
            'synchronized', 'native', 'transient', 'volatile', 'strictfp'
        ];
        
        modifierKeywords.forEach(modifier => {
            if (declaration.includes(modifier)) {
                modifiers.push(modifier);
            }
        });
        
        return modifiers;
    };

    /**
     * Vérifie si un mot est un identifiant Java valide
     * 
     * @param {string} word - Mot à vérifier
     * @returns {boolean} True si valide
     */
    const isValidJavaIdentifier = (word) => {
        return /^[a-zA-Z_$][a-zA-Z0-9_$]*$/.test(word);
    };

    /**
     * Vérifie si une chaîne est un littéral numérique
     * 
     * @param {string} str - Chaîne à vérifier
     * @returns {boolean} True si numérique
     */
    const isNumericLiteral = (str) => {
        return JAVA_PATTERNS.NUMERIC_LITERAL.test(str);
    };

    /**
     * Vérifie si une chaîne est un littéral de chaîne
     * 
     * @param {string} str - Chaîne à vérifier
     * @returns {boolean} True si littéral de chaîne
     */
    const isStringLiteral = (str) => {
        return JAVA_PATTERNS.STRING_LITERAL.test(str) || JAVA_PATTERNS.CHAR_LITERAL.test(str);
    };

    /**
     * Vérifie si une chaîne est un opérateur
     * 
     * @param {string} str - Chaîne à vérifier
     * @returns {boolean} True si opérateur
     */
    const isOperator = (str) => {
        const operators = [
            '+', '-', '*', '/', '%', '=', '==', '!=', '<', '>', '<=', '>=',
            '&&', '||', '!', '&', '|', '^', '~', '<<', '>>', '>>>', '?', ':',
            '++', '--', '+=', '-=', '*=', '/=', '%=', '&=', '|=', '^=', '<<=', '>>=', '>>>='
        ];
        
        return operators.includes(str);
    };

    /**
     * Extrait le nom du package du code Java
     * 
     * @param {string} code - Code Java
     * @returns {string|null} Nom du package ou null
     */
    export const extractPackageName = (code) => {
        const match = code.match(JAVA_PATTERNS.PACKAGE_STATEMENT);
        return match ? match[1] : null;
    };

    /**
     * Vérifie si le code contient une classe main
     * 
     * @param {string} code - Code Java
     * @returns {boolean} True si contient main
     */
    export const hasMainMethod = (code) => {
        const mainPattern = /public\s+static\s+void\s+main\s*\(\s*String\s*\[\s*\]\s*\w+\s*\)/;
        return mainPattern.test(code);
    };

    /**
     * Compte les lignes de code effectif (sans commentaires ni lignes vides)
     * 
     * @param {string} code - Code Java
     * @returns {number} Nombre de lignes de code
     */
    export const countEffectiveLines = (code) => {
        const cleanCode = removeComments(code);
        const lines = cleanCode.split('\n');
        
        return lines.filter(line => line.trim().length > 0).length;
    };

    /**
     * Extrait tous les identifiants utilisés dans le code
     * 
     * @param {string} code - Code Java
     * @returns {Array<string>} Liste des identifiants uniques
     */
    export const extractIdentifiers = (code) => {
        const cleanCode = removeComments(code);
        const identifiers = new Set();
        
        let match;
        JAVA_PATTERNS.IDENTIFIER.lastIndex = 0;
        
        while ((match = JAVA_PATTERNS.IDENTIFIER.exec(cleanCode)) !== null) {
            const identifier = match[0];
            
            // Exclure les mots-clés et types Java
            if (!JAVA_KEYWORDS.includes(identifier) && !JAVA_TYPES.includes(identifier)) {
                identifiers.add(identifier);
            }
        }
        
        return Array.from(identifiers);
    };

    /**
     * Génère un rapport d'analyse complet du code Java
     * 
     * @param {string} code - Code Java à analyser
     * @returns {Object} Rapport d'analyse complet
     */
    export const generateAnalysisReport = (code) => {
        const metrics = analyzeCodeQuality(code);
        const suggestions = generateOptimizationSuggestions(code);
        const validationErrors = validateJavaCode(code);
        
        return {
            summary: {
                linesOfCode: metrics.linesOfCode,
                linesOfComments: metrics.linesOfComments,
                linesBlank: metrics.linesBlank,
                cyclomaticComplexity: metrics.cyclomaticComplexity,
                maintainabilityIndex: Math.round(metrics.maintainabilityIndex),
                codeQuality: getQualityRating(metrics.maintainabilityIndex)
            },
            structure: {
                classes: metrics.classes.length,
                interfaces: metrics.interfaces.length,
                methods: metrics.methods.length,
                fields: metrics.fields.length,
                imports: metrics.imports.length,
                packageName: extractPackageName(code),
                hasMainMethod: hasMainMethod(code)
            },
            metrics: metrics,
            suggestions: suggestions,
            errors: validationErrors.filter(e => e.severity === 'error'),
            warnings: validationErrors.filter(e => e.severity === 'warning'),
            codeSmells: metrics.codeSmells
        };
    };

    /**
     * Détermine la note de qualité basée sur l'indice de maintenabilité
     * 
     * @param {number} maintainabilityIndex - Indice de maintenabilité
     * @returns {string} Note de qualité
     */
    const getQualityRating = (maintainabilityIndex) => {
        if (maintainabilityIndex >= 80) return 'Excellent';
        if (maintainabilityIndex >= 60) return 'Bon';
        if (maintainabilityIndex >= 40) return 'Moyen';
        if (maintainabilityIndex >= 20) return 'Faible';
        return 'Très faible';
    };