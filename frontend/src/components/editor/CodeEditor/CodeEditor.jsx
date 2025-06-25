
import React, { useEffect, useRef, useState, useCallback, useMemo } from 'react';
import { Editor } from '@monaco-editor/react';
import PropTypes from 'prop-types';
import styles from './CodeEditor.module.css';
import { useCodeEditor } from '../../hooks/useCodeEditor';
import { useAuth } from '../../hooks/useAuth';
import { useNotification } from '../../hooks/useNotification';
import { compilerService } from '../../services/compilerService';
import { courseService } from '../../services/courseService';
import { validateJavaCode, formatCode, extractMethodSignatures } from '../../utils/codeUtils';
import { debounce } from '../../utils/helpers';

/**
 * CodeEditor - Éditeur de code intégré avec fonctionnalités avancées
 * 
 * Ce composant fournit un éditeur de code Monaco avec:
 * - Coloration syntaxique pour Java
 * - Auto-complétion intelligente
 * - Validation en temps réel
 * - Sauvegarde automatique
 * - Collaboration en temps réel
 * - Intégration avec le système de progression
 * - Support des snippets de code
 * - Analyse statique du code
 * 
 * Relations avec l'application:
 * - useCodeEditor: Hook pour la gestion de l'état de l'éditeur
 * - useAuth: Authentification pour la sauvegarde personnalisée
 * - useNotification: Notifications d'erreurs et de succès
 * - compilerService: Service de compilation et validation
 * - courseService: Intégration avec les cours et exercices
 * 
 * @component
 * @param {Object} props - Props du composant
 * @param {string} props.initialCode - Code initial à afficher
 * @param {string} props.language - Langage de programmation (défaut: 'java')
 * @param {string} props.theme - Thème de l'éditeur ('vs-dark', 'light')
 * @param {boolean} props.readOnly - Mode lecture seule
 * @param {Function} props.onChange - Callback lors du changement de code
 * @param {Function} props.onSave - Callback lors de la sauvegarde
 * @param {Function} props.onValidation - Callback lors de la validation
 * @param {Object} props.options - Options supplémentaires pour Monaco
 * @param {string} props.exerciseId - ID de l'exercice associé
 * @param {string} props.lessonId - ID de la leçon associée
 * @param {boolean} props.autoSave - Sauvegarde automatique activée
 * @param {number} props.autoSaveDelay - Délai de sauvegarde automatique (ms)
 * @param {boolean} props.showMinimap - Afficher la minimap
 * @param {boolean} props.enableCollaboration - Activer la collaboration
 * @param {Array} props.collaborators - Liste des collaborateurs
 * @param {Object} props.codeTemplate - Template de code pour l'exercice
 * @param {boolean} props.enableAnalysis - Activer l'analyse statique
 * @param {Function} props.onAnalysisResult - Callback des résultats d'analyse
 */
const CodeEditor = ({
  initialCode = '',
  language = 'java',
  theme = 'vs-dark',
  readOnly = false,
  onChange,
  onSave,
  onValidation,
  options = {},
  exerciseId,
  lessonId,
  autoSave = true,
  autoSaveDelay = 3000,
  showMinimap = true,
  enableCollaboration = false,
  collaborators = [],
  codeTemplate,
  enableAnalysis = true,
  onAnalysisResult,
  className = '',
  ...rest
}) => {
  // Refs et états
  const editorRef = useRef(null);
  const monacoRef = useRef(null);
  const [code, setCode] = useState(initialCode);
  const [isValidating, setIsValidating] = useState(false);
  const [validationErrors, setValidationErrors] = useState([]);
  const [suggestions, setSuggestions] = useState([]);
  const [isCollaborating, setIsCollaborating] = useState(false);
  const [analysisResults, setAnalysisResults] = useState(null);

  // Hooks personnalisés
  const { user } = useAuth();
  const { showNotification } = useNotification();
  const {
    editorSettings,
    saveCode,
    loadCode,
    getCodeHistory,
    subscribeToCollaboration,
    unsubscribeFromCollaboration,
    sendCollaborationUpdate,
    applyCollaborationChange
  } = useCodeEditor();

  /**
   * Configuration par défaut de l'éditeur Monaco
   */
  const defaultOptions = useMemo(() => ({
    selectOnLineNumbers: true,
    roundedSelection: false,
    readOnly: readOnly,
    cursorStyle: 'line',
    automaticLayout: true,
    glyphMargin: true,
    useTabStops: false,
    fontSize: editorSettings.fontSize || 14,
    fontFamily: editorSettings.fontFamily || 'Consolas, Monaco, "Courier New", monospace',
    minimap: {
      enabled: showMinimap
    },
    scrollBeyondLastLine: false,
    wordWrap: 'on',
    lineNumbers: 'on',
    folding: true,
    foldingStrategy: 'indentation',
    showFoldingControls: 'always',
    unfoldOnClickAfterEndOfLine: false,
    tabSize: 4,
    insertSpaces: true,
    trimAutoWhitespace: true,
    autoIndent: 'full',
    formatOnPaste: true,
    formatOnType: true,
    suggestOnTriggerCharacters: true,
    acceptSuggestionOnEnter: 'on',
    quickSuggestions: true,
    parameterHints: {
      enabled: true
    },
    ...options
  }), [editorSettings, showMinimap, readOnly, options]);

  /**
   * Gestionnaire de validation du code avec debounce
   */
  const debouncedValidation = useCallback(
    debounce(async (codeToValidate) => {
      if (!codeToValidate.trim()) return;

      setIsValidating(true);
      try {
        // Validation syntaxique locale
        const localErrors = validateJavaCode(codeToValidate);
        
        // Validation côté serveur si nécessaire
        const serverValidation = await compilerService.validateCode(codeToValidate, {
          exerciseId,
          lessonId
        });

        const allErrors = [...localErrors, ...serverValidation.errors];
        setValidationErrors(allErrors);

        // Mettre à jour les marqueurs d'erreur dans l'éditeur
        if (monacoRef.current && editorRef.current) {
          const model = editorRef.current.getModel();
          const markers = allErrors.map(error => ({
            severity: monacoRef.current.MarkerSeverity.Error,
            startLineNumber: error.line,
            startColumn: error.column,
            endLineNumber: error.line,
            endColumn: error.column + error.length,
            message: error.message,
            source: 'JavaCraft'
          }));
          monacoRef.current.editor.setModelMarkers(model, 'JavaCraft', markers);
        }

        // Callback de validation
        if (onValidation) {
          onValidation({
            errors: allErrors,
            isValid: allErrors.length === 0,
            suggestions: serverValidation.suggestions || []
          });
        }

      } catch (error) {
        console.error('Erreur lors de la validation:', error);
        showNotification('Erreur lors de la validation du code', 'error');
      } finally {
        setIsValidating(false);
      }
    }, 1000),
    [exerciseId, lessonId, onValidation, showNotification]
  );

  /**
   * Gestionnaire de sauvegarde automatique avec debounce
   */
  const debouncedAutoSave = useCallback(
    debounce(async (codeToSave) => {
      if (!autoSave || !user || !codeToSave.trim()) return;

      try {
        await saveCode({
          code: codeToSave,
          exerciseId,
          lessonId,
          userId: user.id,
          timestamp: new Date().toISOString()
        });

        showNotification('Code sauvegardé automatiquement', 'success', 2000);
      } catch (error) {
        console.error('Erreur lors de la sauvegarde automatique:', error);
        showNotification('Erreur lors de la sauvegarde', 'error');
      }
    }, autoSaveDelay),
    [autoSave, autoSaveDelay, saveCode, exerciseId, lessonId, user, showNotification]
  );

  /**
   * Analyse statique du code
   */
  const analyzeCode = useCallback(async (codeToAnalyze) => {
    if (!enableAnalysis || !codeToAnalyze.trim()) return;

    try {
      const analysis = {
        complexity: calculateComplexity(codeToAnalyze),
        methods: extractMethodSignatures(codeToAnalyze),
        classes: extractClassDefinitions(codeToAnalyze),
        imports: extractImports(codeToAnalyze),
        suggestions: generateOptimizationSuggestions(codeToAnalyze)
      };

      setAnalysisResults(analysis);

      if (onAnalysisResult) {
        onAnalysisResult(analysis);
      }
    } catch (error) {
      console.error('Erreur lors de l\'analyse du code:', error);
    }
  }, [enableAnalysis, onAnalysisResult]);

  /**
   * Gestionnaire principal de changement de code
   */
  const handleCodeChange = useCallback((value, event) => {
    if (value === code) return;

    setCode(value);
    
    // Callback externe
    if (onChange) {
      onChange(value, event);
    }

    // Validation automatique
    debouncedValidation(value);

    // Sauvegarde automatique
    if (autoSave) {
      debouncedAutoSave(value);
    }

    // Analyse du code
    if (enableAnalysis) {
      analyzeCode(value);
    }

    // Collaboration en temps réel
    if (enableCollaboration && isCollaborating) {
      sendCollaborationUpdate({
        type: 'codeChange',
        code: value,
        cursor: editorRef.current?.getPosition(),
        user: user
      });
    }
  }, [
    code, onChange, debouncedValidation, debouncedAutoSave, 
    analyzeCode, enableCollaboration, isCollaborating, 
    sendCollaborationUpdate, user, autoSave, enableAnalysis
  ]);

  /**
   * Configuration de l'éditeur après montage
   */
  const handleEditorDidMount = useCallback((editor, monaco) => {
    editorRef.current = editor;
    monacoRef.current = monaco;

    // Configuration des snippets Java
    monaco.languages.registerCompletionItemProvider('java', {
      provideCompletionItems: (model, position) => {
        const suggestions = getJavaSnippets(monaco);
        return { suggestions };
      }
    });

    // Configuration des actions personnalisées
    editor.addAction({
      id: 'save-code',
      label: 'Sauvegarder',
      keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS],
      run: () => handleSave()
    });

    editor.addAction({
      id: 'format-code',
      label: 'Formater le code',
      keybindings: [monaco.KeyMod.CtrlCmd | monaco.KeyMod.Shift | monaco.KeyCode.KeyF],
      run: () => handleFormat()
    });

    editor.addAction({
      id: 'run-code',
      label: 'Exécuter',
      keybindings: [monaco.KeyCode.F5],
      run: () => handleRun()
    });

    // Collaboration en temps réel
    if (enableCollaboration) {
      setupCollaboration(editor, monaco);
    }

    // Focus automatique
    editor.focus();
  }, [enableCollaboration]);

  /**
   * Configuration de la collaboration en temps réel
   */
  const setupCollaboration = useCallback((editor, monaco) => {
    subscribeToCollaboration((update) => {
      if (update.user.id !== user?.id) {
        applyCollaborationChange(update, editor);
        
        // Afficher les curseurs des autres utilisateurs
        showCollaboratorCursor(update.user, update.cursor, monaco);
      }
    });

    setIsCollaborating(true);
  }, [subscribeToCollaboration, applyCollaborationChange, user]);

  /**
   * Sauvegarde manuelle
   */
  const handleSave = useCallback(async () => {
    if (!user) {
      showNotification('Connectez-vous pour sauvegarder', 'warning');
      return;
    }

    try {
      await saveCode({
        code,
        exerciseId,
        lessonId,
        userId: user.id,
        timestamp: new Date().toISOString()
      });

      if (onSave) {
        onSave(code);
      }

      showNotification('Code sauvegardé avec succès', 'success');
    } catch (error) {
      console.error('Erreur lors de la sauvegarde:', error);
      showNotification('Erreur lors de la sauvegarde', 'error');
    }
  }, [code, saveCode, exerciseId, lessonId, user, onSave, showNotification]);

  /**
   * Formatage du code
   */
  const handleFormat = useCallback(async () => {
    try {
      const formattedCode = await formatCode(code);
      setCode(formattedCode);
      editorRef.current?.setValue(formattedCode);
      showNotification('Code formaté', 'success', 2000);
    } catch (error) {
      console.error('Erreur lors du formatage:', error);
      showNotification('Erreur lors du formatage', 'error');
    }
  }, [code, showNotification]);

  /**
   * Exécution du code
   */
  const handleRun = useCallback(async () => {
    if (!code.trim()) {
      showNotification('Aucun code à exécuter', 'warning');
      return;
    }

    try {
      const result = await compilerService.runCode(code, {
        exerciseId,
        lessonId
      });

      // Émettre un événement pour afficher les résultats
      window.dispatchEvent(new CustomEvent('codeExecution', {
        detail: { result, code }
      }));

    } catch (error) {
      console.error('Erreur lors de l\'exécution:', error);
      showNotification('Erreur lors de l\'exécution', 'error');
    }
  }, [code, exerciseId, lessonId, showNotification]);

  /**
   * Chargement du code initial
   */
  useEffect(() => {
    const loadInitialCode = async () => {
      if (exerciseId && lessonId && user) {
        try {
          const savedCode = await loadCode({
            exerciseId,
            lessonId,
            userId: user.id
          });

          if (savedCode) {
            setCode(savedCode);
          } else if (codeTemplate) {
            setCode(codeTemplate.content || '');
          }
        } catch (error) {
          console.error('Erreur lors du chargement du code:', error);
          if (codeTemplate) {
            setCode(codeTemplate.content || '');
          }
        }
      } else if (initialCode) {
        setCode(initialCode);
      } else if (codeTemplate) {
        setCode(codeTemplate.content || '');
      }
    };

    loadInitialCode();
  }, [exerciseId, lessonId, user, loadCode, initialCode, codeTemplate]);

  /**
   * Nettoyage lors du démontage
   */
  useEffect(() => {
    return () => {
      if (enableCollaboration) {
        unsubscribeFromCollaboration();
      }
    };
  }, [enableCollaboration, unsubscribeFromCollaboration]);

  return (
    <div className={`${styles.codeEditor} ${className}`} {...rest}>
      {/* Barre d'outils */}
      <div className={styles.toolbar}>
        <div className={styles.toolbarLeft}>
          <button 
            className={styles.toolbarButton}
            onClick={handleSave}
            disabled={!user}
            title="Sauvegarder (Ctrl+S)"
          >
            💾 Sauvegarder
          </button>
          <button 
            className={styles.toolbarButton}
            onClick={handleFormat}
            title="Formater (Ctrl+Shift+F)"
          >
            🎨 Formater
          </button>
          <button 
            className={styles.toolbarButton}
            onClick={handleRun}
            title="Exécuter (F5)"
          >
            ▶️ Exécuter
          </button>
        </div>

        <div className={styles.toolbarRight}>
          {/* Indicateur de validation */}
          {isValidating && (
            <div className={styles.validationIndicator}>
              <span className={styles.spinner}></span>
              Validation...
            </div>
          )}
          
          {/* Erreurs de validation */}
          {validationErrors.length > 0 && (
            <div className={styles.errorIndicator} title={`${validationErrors.length} erreur(s)`}>
              ❌ {validationErrors.length}
            </div>
          )}

          {/* Collaborateurs */}
          {enableCollaboration && collaborators.length > 0 && (
            <div className={styles.collaborators}>
              {collaborators.map(collaborator => (
                <div 
                  key={collaborator.id}
                  className={styles.collaboratorAvatar}
                  title={collaborator.name}
                >
                  {collaborator.avatar ? (
                    <img src={collaborator.avatar} alt={collaborator.name} />
                  ) : (
                    collaborator.name.charAt(0).toUpperCase()
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Éditeur Monaco */}
      <div className={styles.editorContainer}>
        <Editor
          height="100%"
          language={language}
          theme={theme}
          value={code}
          options={defaultOptions}
          onChange={handleCodeChange}
          onMount={handleEditorDidMount}
          loading={<div className={styles.loading}>Chargement de l'éditeur...</div>}
        />
      </div>

      {/* Analyse du code */}
      {enableAnalysis && analysisResults && (
        <div className={styles.analysisPanel}>
          <div className={styles.analysisHeader}>
            <h4>Analyse du code</h4>
          </div>
          <div className={styles.analysisContent}>
            <div className={styles.analysisMetric}>
              <span>Complexité:</span>
              <span className={getComplexityClass(analysisResults.complexity)}>
                {analysisResults.complexity}
              </span>
            </div>
            <div className={styles.analysisMetric}>
              <span>Méthodes:</span>
              <span>{analysisResults.methods.length}</span>
            </div>
            <div className={styles.analysisMetric}>
              <span>Classes:</span>
              <span>{analysisResults.classes.length}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

/**
 * Génère les snippets Java pour l'auto-complétion
 */
const getJavaSnippets = (monaco) => {
  return [
    {
      label: 'main',
      kind: monaco.languages.CompletionItemKind.Snippet,
      insertText: 'public static void main(String[] args) {\n\t$0\n}',
      insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
      documentation: 'Méthode main'
    },
    {
      label: 'class',
      kind: monaco.languages.CompletionItemKind.Snippet,
      insertText: 'public class ${1:ClassName} {\n\t$0\n}',
      insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
      documentation: 'Déclaration de classe'
    },
    {
      label: 'method',
      kind: monaco.languages.CompletionItemKind.Snippet,
      insertText: 'public ${1:void} ${2:methodName}(${3:params}) {\n\t$0\n}',
      insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
      documentation: 'Déclaration de méthode'
    },
    {
      label: 'for',
      kind: monaco.languages.CompletionItemKind.Snippet,
      insertText: 'for (int ${1:i} = 0; ${1:i} < ${2:length}; ${1:i}++) {\n\t$0\n}',
      insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
      documentation: 'Boucle for'
    },
    {
      label: 'while',
      kind: monaco.languages.CompletionItemKind.Snippet,
      insertText: 'while (${1:condition}) {\n\t$0\n}',
      insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
      documentation: 'Boucle while'
    },
    {
      label: 'if',
      kind: monaco.languages.CompletionItemKind.Snippet,
      insertText: 'if (${1:condition}) {\n\t$0\n}',
      insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
      documentation: 'Condition if'
    },
    {
      label: 'try-catch',
      kind: monaco.languages.CompletionItemKind.Snippet,
      insertText: 'try {\n\t${1:// code}\n} catch (${2:Exception} e) {\n\t${3:// handle exception}\n}',
      insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
      documentation: 'Bloc try-catch'
    }
  ];
};

/**
 * Calcule la complexité cyclomatique du code
 */
const calculateComplexity = (code) => {
  const complexityKeywords = ['if', 'else', 'while', 'for', 'switch', 'case', 'catch', '&&', '||', '?'];
  let complexity = 1; // Complexité de base
  
  complexityKeywords.forEach(keyword => {
    const regex = new RegExp(`\\b${keyword}\\b`, 'g');
    const matches = code.match(regex);
    if (matches) {
      complexity += matches.length;
    }
  });
  
  return complexity;
};

/**
 * Extrait les définitions de classes
 */
const extractClassDefinitions = (code) => {
  const classRegex = /(?:public|private|protected)?\s*class\s+(\w+)/g;
  const classes = [];
  let match;
  
  while ((match = classRegex.exec(code)) !== null) {
    classes.push({
      name: match[1],
      line: code.substring(0, match.index).split('\n').length
    });
  }
  
  return classes;
};

/**
 * Extrait les imports
 */
const extractImports = (code) => {
  const importRegex = /import\s+(?:static\s+)?([^;]+);/g;
  const imports = [];
  let match;
  
  while ((match = importRegex.exec(code)) !== null) {
    imports.push(match[1].trim());
  }
  
  return imports;
};

/**
 * Génère des suggestions d'optimisation
 */
const generateOptimizationSuggestions = (code) => {
  const suggestions = [];
  
  // Vérifier les variables non utilisées
  if (code.includes('int i;') && !code.includes('i++') && !code.includes('i--')) {
    suggestions.push('Variable "i" déclarée mais non utilisée');
  }
  
  // Vérifier les boucles potentiellement optimisables
  if (code.includes('for') && code.includes('System.out.println')) {
    suggestions.push('Évitez les appels System.out.println dans les boucles pour de meilleures performances');
  }
  
  return suggestions;
};

/**
 * Détermine la classe CSS pour la complexité
 */
const getComplexityClass = (complexity) => {
  if (complexity <= 5) return 'low';
  if (complexity <= 10) return 'medium';
  return 'high';
};

/**
 * Affiche le curseur des collaborateurs
 */
const showCollaboratorCursor = (user, position, monaco) => {
  // Implémentation de l'affichage des curseurs collaboratifs
  // Cette fonction nécessiterait une extension Monaco personnalisée
  console.log(`Collaborateur ${user.name} à la position:`, position);
};

// PropTypes pour la validation des props
CodeEditor.propTypes = {
  initialCode: PropTypes.string,
  language: PropTypes.string,
  theme: PropTypes.string,
  readOnly: PropTypes.bool,
  onChange: PropTypes.func,
  onSave: PropTypes.func,
  onValidation: PropTypes.func,
  options: PropTypes.object,
  exerciseId: PropTypes.string,
  lessonId: PropTypes.string,
  autoSave: PropTypes.bool,
  autoSaveDelay: PropTypes.number,
  showMinimap: PropTypes.bool,
  enableCollaboration: PropTypes.bool,
  collaborators: PropTypes.array,
  codeTemplate: PropTypes.object,
  enableAnalysis: PropTypes.bool,
  onAnalysisResult: PropTypes.func,
  className: PropTypes.string
};

export default CodeEditor;
