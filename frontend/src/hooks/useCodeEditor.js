// JavaCraft Academy/frontend/src/hooks/useCodeEditor.js
// Hook personnalisé pour la gestion de l'éditeur de code avec compilation et exécution

import { useState, useCallback, useRef, useEffect } from 'react';
import { useLocalStorage } from './useLocalStorage';

/**
 * Hook personnalisé pour gérer l'éditeur de code
 * @param {Object} options - Options de configuration
 * @returns {Object} - État et fonctions de gestion de l'éditeur
 */
export const useCodeEditor = (options = {}) => {
  const {
    initialCode = '',
    language = 'java',
    theme = 'dark',
    fontSize = 14,
    tabSize = 2,
    autoSave = true,
    autoSaveDelay = 2000,
    storageKey = 'code_editor_content'
  } = options;

  // État de l'éditeur
  const [code, setCode] = useLocalStorage(storageKey, initialCode);
  const [currentLanguage, setCurrentLanguage] = useState(language);
  const [currentTheme, setCurrentTheme] = useState(theme);
  const [editorSettings, setEditorSettings] = useLocalStorage('editor_settings', {
    fontSize,
    tabSize,
    wordWrap: true,
    lineNumbers: true,
    autoCloseBrackets: true,
    autoIndent: true
  });

  // État de compilation et exécution
  const [isCompiling, setIsCompiling] = useState(false);
  const [isRunning, setIsRunning] = useState(false);
  const [compilationResult, setCompilationResult] = useState(null);
  const [executionResult, setExecutionResult] = useState(null);
  const [consoleOutput, setConsoleOutput] = useState([]);
  const [errors, setErrors] = useState([]);

  // État de l'historique
  const [history, setHistory] = useState([]);
  const [historyIndex, setHistoryIndex] = useState(-1);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);

  // Références
  const autoSaveTimerRef = useRef(null);
  const editorRef = useRef(null);
  const abortControllerRef = useRef(null);

  // Fonction pour mettre à jour le code
  const updateCode = useCallback((newCode, addToHistory = true) => {
    setCode(newCode);
    setHasUnsavedChanges(true);
    
    // Ajouter à l'historique si demandé
    if (addToHistory && newCode !== code) {
      setHistory(prev => {
        const newHistory = prev.slice(0, historyIndex + 1);
        newHistory.push(code);
        return newHistory.slice(-50); // Garder seulement les 50 dernières entrées
      });
      setHistoryIndex(prev => Math.min(prev + 1, 49));
    }

    // Auto-save
    if (autoSave) {
      if (autoSaveTimerRef.current) {
        clearTimeout(autoSaveTimerRef.current);
      }
      autoSaveTimerRef.current = setTimeout(() => {
        setHasUnsavedChanges(false);
        console.log('Code sauvegardé automatiquement');
      }, autoSaveDelay);
    }
  }, [code, historyIndex, autoSave, autoSaveDelay, setCode]);

  // Fonctions d'historique (Undo/Redo)
  const undo = useCallback(() => {
    if (historyIndex > 0) {
      const previousCode = history[historyIndex - 1];
      setCode(previousCode);
      setHistoryIndex(prev => prev - 1);
      setHasUnsavedChanges(true);
    }
  }, [history, historyIndex, setCode]);

  const redo = useCallback(() => {
    if (historyIndex < history.length - 1) {
      const nextCode = history[historyIndex + 1];
      setCode(nextCode);
      setHistoryIndex(prev => prev + 1);
      setHasUnsavedChanges(true);
    }
  }, [history, historyIndex, setCode]);

  // Fonction pour compiler le code
  const compileCode = useCallback(async () => {
    if (!code.trim()) {
      setErrors([{ message: 'Aucun code à compiler', line: 0 }]);
      return false;
    }

    setIsCompiling(true);
    setCompilationResult(null);
    setErrors([]);
    
    // Annuler la compilation précédente si elle existe
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    abortControllerRef.current = new AbortController();

    try {
      const response = await fetch('/api/compile', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          code,
          language: currentLanguage
        }),
        signal: abortControllerRef.current.signal
      });

      if (!response.ok) {
        throw new Error(`Erreur de compilation: ${response.status}`);
      }

      const result = await response.json();
      setCompilationResult(result);

      if (result.errors && result.errors.length > 0) {
        setErrors(result.errors);
        return false;
      }

      return true;
    } catch (error) {
      if (error.name !== 'AbortError') {
        console.error('Erreur lors de la compilation:', error);
        setErrors([{ message: error.message, line: 0 }]);
      }
      return false;
    } finally {
      setIsCompiling(false);
    }
  }, [code, currentLanguage]);

  // Fonction pour exécuter le code
  const runCode = useCallback(async (input = '') => {
    if (!compilationResult || errors.length > 0) {
      const compiled = await compileCode();
      if (!compiled) return false;
    }

    setIsRunning(true);
    setExecutionResult(null);
    setConsoleOutput([]);

    try {
      const response = await fetch('/api/run', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          code,
          language: currentLanguage,
          input,
          compilationResult
        }),
        signal: abortControllerRef.current.signal
      });

      if (!response.ok) {
        throw new Error(`Erreur d'exécution: ${response.status}`);
      }

      const result = await response.json();
      setExecutionResult(result);
      
      if (result.output) {
        setConsoleOutput(prev => [...prev, ...result.output]);
      }

      return result;
    } catch (error) {
      if (error.name !== 'AbortError') {
        console.error('Erreur lors de l\'exécution:', error);
        setConsoleOutput(prev => [...prev, { type: 'error', message: error.message }]);
      }
      return null;
    } finally {
      setIsRunning(false);
    }
  }, [code, currentLanguage, compilationResult, errors, compileCode]);

  // Fonction pour formater le code
  const formatCode = useCallback(async () => {
    try {
      const response = await fetch('/api/format', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          code,
          language: currentLanguage,
          settings: editorSettings
        })
      });

      if (!response.ok) {
        throw new Error(`Erreur de formatage: ${response.status}`);
      }

      const result = await response.json();
      if (result.formattedCode) {
        updateCode(result.formattedCode);
      }
    } catch (error) {
      console.error('Erreur lors du formatage:', error);
      setConsoleOutput(prev => [...prev, { type: 'error', message: 'Erreur de formatage: ' + error.message }]);
    }
  }, [code, currentLanguage, editorSettings, updateCode]);

  // Fonction pour analyser le code (linting)
  const analyzeCode = useCallback(async () => {
    try {
      const response = await fetch('/api/analyze', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          code,
          language: currentLanguage
        })
      });

      if (!response.ok) {
        throw new Error(`Erreur d'analyse: ${response.status}`);
      }

      const result = await response.json();
      return result.suggestions || [];
    } catch (error) {
      console.error('Erreur lors de l\'analyse:', error);
      return [];
    }
  }, [code, currentLanguage]);

  // Fonction pour réinitialiser l'éditeur
  const resetEditor = useCallback(() => {
    updateCode(initialCode, false);
    setCompilationResult(null);
    setExecutionResult(null);
    setConsoleOutput([]);
    setErrors([]);
    setHistory([]);
    setHistoryIndex(-1);
    setHasUnsavedChanges(false);
  }, [initialCode, updateCode]);

  // Fonction pour arrêter l'exécution
  const stopExecution = useCallback(() => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    setIsRunning(false);
    setIsCompiling(false);
  }, []);

  // Fonction pour exporter le code
  const exportCode = useCallback((filename) => {
    const blob = new Blob([code], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename || `code.${getFileExtension(currentLanguage)}`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }, [code, currentLanguage]);

  // Fonction pour importer du code
  const importCode = useCallback((file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (e) => {
        const importedCode = e.target.result;
        updateCode(importedCode);
        resolve(importedCode);
      };
      reader.onerror = reject;
      reader.readAsText(file);
    });
  }, [updateCode]);

  // Fonction utilitaire pour obtenir l'extension de fichier
  const getFileExtension = (lang) => {
    const extensions = {
      java: 'java',
      javascript: 'js',
      python: 'py',
      cpp: 'cpp',
      c: 'c',
      html: 'html',
      css: 'css'
    };
    return extensions[lang] || 'txt';
  };

  // Nettoyage
  useEffect(() => {
    return () => {
      if (autoSaveTimerRef.current) {
        clearTimeout(autoSaveTimerRef.current);
      }
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, []);

  return {
    // État du code
    code,
    currentLanguage,
    currentTheme,
    editorSettings,
    hasUnsavedChanges,
    
    // État de compilation/exécution
    isCompiling,
    isRunning,
    compilationResult,
    executionResult,
    consoleOutput,
    errors,
    
    // État de l'historique
    history,
    historyIndex,
    canUndo: historyIndex > 0,
    canRedo: historyIndex < history.length - 1,
    
    // Fonctions de base
    updateCode,
    setLanguage: setCurrentLanguage,
    setTheme: setCurrentTheme,
    setEditorSettings,
    
    // Fonctions d'historique
    undo,
    redo,
    
    // Fonctions de compilation/exécution
    compileCode,
    runCode,
    stopExecution,
    
    // Fonctions utilitaires
    formatCode,
    analyzeCode,
    resetEditor,
    exportCode,
    importCode,
    
    // Références
    editorRef
  };
};

export default useCodeEditor;