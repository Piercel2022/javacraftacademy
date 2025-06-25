// SyntaxHighlighter.jsx
import React, { useState, useEffect, useMemo, useRef } from 'react';
import { Copy, Download, Expand, Minimize, Search, X } from 'lucide-react';
import styles from './SyntaxHighlighter.module.css';

const SyntaxHighlighter = ({
  code = '',
  language = 'java',
  theme = 'dark',
  showLineNumbers = true,
  showCopyButton = true,
  showDownloadButton = false,
  showExpandButton = false,
  highlightLines = [],
  maxHeight = '400px',
  fontSize = 'medium',
  readOnly = true,
  fileName = '',
  onCodeChange,
  className = '',
  ...props
}) => {
  const [copied, setCopied] = useState(false);
  const [expanded, setExpanded] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [currentSearchIndex, setCurrentSearchIndex] = useState(-1);
  const [showSearch, setShowSearch] = useState(false);
  const codeRef = useRef(null);
  const searchInputRef = useRef(null);

  // Patterns de coloration syntaxique pour Java
  const javaTokens = {
    // Mots-clés
    keywords: /\b(abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|try|void|volatile|while)\b/g,
    
    // Types primitifs et classes courantes
    types: /\b(String|Integer|Boolean|Double|Float|Long|Short|Byte|Character|Object|Array|List|ArrayList|HashMap|HashSet|Scanner|System|Math|Random|Date|Calendar|StringBuilder|StringBuffer)\b/g,
    
    // Chaînes de caractères
    strings: /(["'`])(?:(?!\1)[^\\]|\\.))*?\1/g,
    
    // Commentaires
    comments: /\/\/.*$|\/\*[\s\S]*?\*\//gm,
    
    // Nombres
    numbers: /\b\d+(\.\d+)?[fLdF]?\b/g,
    
    // Annotations
    annotations: /@\w+/g,
    
    // Méthodes
    methods: /\b\w+(?=\s*\()/g,
    
    // Opérateurs
    operators: /[+\-*/%=!<>&|^~?:]+/g,
    
    // Parenthèses et crochets
    brackets: /[()[\]{}]/g
  };

  // Fonction de coloration syntaxique
  const highlightSyntax = useMemo(() => {
    if (!code) return '';

    let highlightedCode = code;
    
    // Échapper les caractères HTML
    highlightedCode = highlightedCode
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#x27;');

    // Appliquer la coloration syntaxique
    if (language === 'java') {
      // Commentaires (en premier pour éviter de colorer à l'intérieur)
      highlightedCode = highlightedCode.replace(
        javaTokens.comments,
        match => `<span class="${styles.comment}">${match}</span>`
      );

      // Chaînes de caractères
      highlightedCode = highlightedCode.replace(
        javaTokens.strings,
        match => `<span class="${styles.string}">${match}</span>`
      );

      // Annotations
      highlightedCode = highlightedCode.replace(
        javaTokens.annotations,
        match => `<span class="${styles.annotation}">${match}</span>`
      );

      // Mots-clés
      highlightedCode = highlightedCode.replace(
        javaTokens.keywords,
        match => `<span class="${styles.keyword}">${match}</span>`
      );

      // Types
      highlightedCode = highlightedCode.replace(
        javaTokens.types,
        match => `<span class="${styles.type}">${match}</span>`
      );

      // Nombres
      highlightedCode = highlightedCode.replace(
        javaTokens.numbers,
        match => `<span class="${styles.number}">${match}</span>`
      );

      // Méthodes
      highlightedCode = highlightedCode.replace(
        javaTokens.methods,
        match => `<span class="${styles.method}">${match}</span>`
      );

      // Opérateurs
      highlightedCode = highlightedCode.replace(
        javaTokens.operators,
        match => `<span class="${styles.operator}">${match}</span>`
      );

      // Parenthèses et crochets
      highlightedCode = highlightedCode.replace(
        javaTokens.brackets,
        match => `<span class="${styles.bracket}">${match}</span>`
      );
    }

    return highlightedCode;
  }, [code, language]);

  // Fonction de recherche dans le code
  useEffect(() => {
    if (!searchTerm) {
      setSearchResults([]);
      setCurrentSearchIndex(-1);
      return;
    }

    const regex = new RegExp(searchTerm.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'gi');
    const matches = [];
    let match;

    while ((match = regex.exec(code)) !== null) {
      matches.push({
        index: match.index,
        length: match[0].length,
        line: code.substring(0, match.index).split('\n').length
      });
    }

    setSearchResults(matches);
    setCurrentSearchIndex(matches.length > 0 ? 0 : -1);
  }, [searchTerm, code]);

  // Copier le code
  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(code);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      console.error('Erreur lors de la copie:', err);
    }
  };

  // Télécharger le code
  const handleDownload = () => {
    const blob = new Blob([code], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName || `code.${language}`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  // Basculer le mode étendu
  const handleToggleExpand = () => {
    setExpanded(!expanded);
  };

  // Navigation dans les résultats de recherche
  const handleSearchNavigation = (direction) => {
    if (searchResults.length === 0) return;

    let newIndex;
    if (direction === 'next') {
      newIndex = currentSearchIndex < searchResults.length - 1 
        ? currentSearchIndex + 1 
        : 0;
    } else {
      newIndex = currentSearchIndex > 0 
        ? currentSearchIndex - 1 
        : searchResults.length - 1;
    }

    setCurrentSearchIndex(newIndex);
  };

  // Fermer la recherche
  const handleCloseSearch = () => {
    setShowSearch(false);
    setSearchTerm('');
    setSearchResults([]);
    setCurrentSearchIndex(-1);
  };

  // Focus sur l'input de recherche
  useEffect(() => {
    if (showSearch && searchInputRef.current) {
      searchInputRef.current.focus();
    }
  }, [showSearch]);

  // Générer les numéros de ligne
  const generateLineNumbers = () => {
    const lines = code.split('\n');
    return lines.map((_, index) => {
      const lineNumber = index + 1;
      const isHighlighted = highlightLines.includes(lineNumber);
      
      return (
        <div
          key={lineNumber}
          className={`${styles.lineNumber} ${isHighlighted ? styles.highlightedLine : ''}`}
        >
          {lineNumber}
        </div>
      );
    });
  };

  // Générer le code avec surbrillance des résultats de recherche
  const generateHighlightedCode = () => {
    let codeWithSearch = highlightSyntax;

    if (searchTerm && searchResults.length > 0) {
      const regex = new RegExp(
        searchTerm.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'),
        'gi'
      );
      
      codeWithSearch = codeWithSearch.replace(regex, (match, offset) => {
        const resultIndex = searchResults.findIndex(result => 
          result.index <= offset && offset < result.index + result.length
        );
        
        const isCurrentResult = resultIndex === currentSearchIndex;
        const className = isCurrentResult 
          ? styles.currentSearchResult 
          : styles.searchResult;
          
        return `<span class="${className}">${match}</span>`;
      });
    }

    return codeWithSearch;
  };

  const containerClass = `
    ${styles.syntaxHighlighter}
    ${styles[theme]}
    ${styles[fontSize]}
    ${expanded ? styles.expanded : ''}
    ${className}
  `.trim();

  return (
    <div className={containerClass} {...props}>
      {/* En-tête */}
      <div className={styles.header}>
        <div className={styles.headerLeft}>
          {fileName && (
            <span className={styles.fileName}>{fileName}</span>
          )}
          <span className={styles.language}>{language.toUpperCase()}</span>
        </div>

        <div className={styles.headerRight}>
          {/* Barre de recherche */}
          {showSearch && (
            <div className={styles.searchContainer}>
              <input
                ref={searchInputRef}
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Rechercher dans le code..."
                className={styles.searchInput}
              />
              
              {searchResults.length > 0 && (
                <div className={styles.searchNavigation}>
                  <span className={styles.searchCount}>
                    {currentSearchIndex + 1} / {searchResults.length}
                  </span>
                  <button
                    onClick={() => handleSearchNavigation('prev')}
                    className={styles.searchButton}
                    title="Précédent"
                  >
                    ↑
                  </button>
                  <button
                    onClick={() => handleSearchNavigation('next')}
                    className={styles.searchButton}
                    title="Suivant"
                  >
                    ↓
                  </button>
                </div>
              )}
              
              <button
                onClick={handleCloseSearch}
                className={styles.closeSearchButton}
                title="Fermer la recherche"
              >
                <X size={14} />
              </button>
            </div>
          )}

          {/* Boutons d'action */}
          <div className={styles.actions}>
            <button
              onClick={() => setShowSearch(!showSearch)}
              className={styles.actionButton}
              title="Rechercher"
            >
              <Search size={16} />
            </button>

            {showCopyButton && (
              <button
                onClick={handleCopy}
                className={styles.actionButton}
                title={copied ? 'Copié !' : 'Copier le code'}
              >
                <Copy size={16} />
                {copied && <span className={styles.copiedText}>Copié !</span>}
              </button>
            )}

            {showDownloadButton && (
              <button
                onClick={handleDownload}
                className={styles.actionButton}
                title="Télécharger le code"
              >
                <Download size={16} />
              </button>
            )}

            {showExpandButton && (
              <button
                onClick={handleToggleExpand}
                className={styles.actionButton}
                title={expanded ? 'Réduire' : 'Étendre'}
              >
                {expanded ? <Minimize size={16} /> : <Expand size={16} />}
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Contenu du code */}
      <div 
        className={styles.codeContainer}
        style={{ maxHeight: expanded ? 'none' : maxHeight }}
      >
        <div className={styles.codeWrapper}>
          {/* Numéros de ligne */}
          {showLineNumbers && (
            <div className={styles.lineNumbers}>
              {generateLineNumbers()}
            </div>
          )}

          {/* Code */}
          <div className={styles.codeContent}>
            {readOnly ? (
              <pre
                ref={codeRef}
                className={styles.codeBlock}
                dangerouslySetInnerHTML={{ __html: generateHighlightedCode() }}
              />
            ) : (
              <textarea
                value={code}
                onChange={(e) => onCodeChange && onCodeChange(e.target.value)}
                className={styles.codeTextarea}
                spellCheck={false}
              />
            )}
          </div>
        </div>

        {/* Overlay pour les lignes surlignées */}
        {highlightLines.length > 0 && (
          <div className={styles.highlightOverlay}>
            {highlightLines.map(lineNumber => (
              <div
                key={lineNumber}
                className={styles.highlightLine}
                style={{
                  top: `${(lineNumber - 1) * 1.5}em`,
                  height: '1.5em'
                }}
              />
            ))}
          </div>
        )}
      </div>

      {/* Pied de page avec statistiques */}
      <div className={styles.footer}>
        <div className={styles.stats}>
          <span>Lignes: {code.split('\n').length}</span>
          <span>Caractères: {code.length}</span>
          {searchResults.length > 0 && (
            <span>Résultats: {searchResults.length}</span>
          )}
        </div>
      </div>
    </div>
  );
};

export default SyntaxHighlighter;