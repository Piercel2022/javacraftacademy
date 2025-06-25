
import React, { useState, useEffect, useRef, useImperativeHandle, forwardRef } from 'react';
import { useNotification } from '../../../hooks/useNotification';
import { useCodeEditor } from '../../../hooks/useCodeEditor';
import { formatters } from '../../../utils/formatters';
import { dateUtils } from '../../../utils/dateUtils';
import styles from './OutputPanel.module.css';

/**
 * OutputPanel - Composant de panneau de sortie pour l'affichage des résultats d'exécution de code
 * 
 * Ce composant gère l'affichage des résultats d'exécution de code, des erreurs de compilation,
 * des logs de débogage et des métriques de performance. Il s'intègre avec le système de
 * compilation et l'éditeur de code pour fournir un feedback en temps réel.
 * 
 * @component
 * @param {Object} props - Les propriétés du composant
 * @param {string} props.output - Sortie d'exécution du code
 * @param {string} props.error - Messages d'erreur de compilation/exécution
 * @param {Array} props.logs - Logs de débogage et d'information
 * @param {boolean} props.isLoading - État de chargement de l'exécution
 * @param {Object} props.executionMetrics - Métriques de performance d'exécution
 * @param {string} props.language - Langage de programmation utilisé
 * @param {Function} props.onClear - Callback pour vider le panneau
 * @param {Function} props.onCopy - Callback pour copier le contenu
 * @param {Function} props.onDownload - Callback pour télécharger les résultats
 * @param {boolean} props.showLineNumbers - Afficher les numéros de ligne
 * @param {string} props.theme - Thème du panneau (light/dark)
 * @param {boolean} props.collapsible - Panneau réductible
 * @param {boolean} props.autoScroll - Défilement automatique vers le bas
 * @param {number} props.maxHeight - Hauteur maximale du panneau
 * @param {Function} props.onResize - Callback lors du redimensionnement
 * 
 * Relations avec l'application:
 * - CodeEditor: Reçoit les résultats d'exécution
 * - CodeRunner: Affiche les sorties de compilation/exécution
 * - NotificationContext: Notifications pour les actions utilisateur
 * - ProgressContext: Suivi de la progression des exercices
 * - CompilerService: Intégration avec le service de compilation
 * - ThemeContext: Application du thème choisi
 * 
 * @returns {React.Component} Composant OutputPanel
 */
const OutputPanel = forwardRef(({
  output = '',
  error = '',
  logs = [],
  isLoading = false,
  executionMetrics = null,
  language = 'java',
  onClear = () => {},
  onCopy = () => {},
  onDownload = () => {},
  showLineNumbers = true,
  theme = 'light',
  collapsible = true,
  autoScroll = true,
  maxHeight = 400,
  onResize = () => {}
}, ref) => {
  // État local du composant
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState('output');
  const [filter, setFilter] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [fontSize, setFontSize] = useState(14);
  const [wordWrap, setWordWrap] = useState(true);
  const [isResizing, setIsResizing] = useState(false);
  const [currentHeight, setCurrentHeight] = useState(maxHeight);

  // Références
  const outputRef = useRef(null);
  const errorRef = useRef(null);
  const logsRef = useRef(null);
  const resizeRef = useRef(null);
  const panelRef = useRef(null);

  // Hooks personnalisés
  const { showNotification } = useNotification();
  const { executionHistory, addToHistory } = useCodeEditor();

  /**
   * Expose les méthodes publiques du composant
   */
  useImperativeHandle(ref, () => ({
    clear: handleClear,
    scrollToBottom: scrollToBottom,
    focus: focusPanel,
    resize: handleResize,
    exportResults: handleExportResults
  }));

  /**
   * Effet pour le défilement automatique
   */
  useEffect(() => {
    if (autoScroll && (output || error || logs.length > 0)) {
      scrollToBottom();
    }
  }, [output, error, logs, autoScroll]);

  /**
   * Effet pour l'ajout à l'historique
   */
  useEffect(() => {
    if (output || error) {
      const executionResult = {
        id: Date.now(),
        timestamp: new Date().toISOString(),
        language,
        output,
        error,
        logs,
        metrics: executionMetrics
      };
      addToHistory(executionResult);
    }
  }, [output, error, logs, language, executionMetrics, addToHistory]);

  /**
   * Filtre les logs selon le filtre actuel
   * @returns {Array} Logs filtrés
   */
  const getFilteredLogs = () => {
    let filtered = logs;

    // Filtrage par type
    if (filter !== 'all') {
      filtered = filtered.filter(log => log.level === filter);
    }

    // Filtrage par terme de recherche
    if (searchTerm) {
      filtered = filtered.filter(log => 
        log.message.toLowerCase().includes(searchTerm.toLowerCase()) ||
        log.timestamp.includes(searchTerm)
      );
    }

    return filtered;
  };

  /**
   * Défilement vers le bas du panneau
   */
  const scrollToBottom = () => {
    const currentRef = getCurrentRef();
    if (currentRef && currentRef.current) {
      currentRef.current.scrollTop = currentRef.current.scrollHeight;
    }
  };

  /**
   * Obtient la référence du contenu actuel
   * @returns {Object} Référence du contenu actuel
   */
  const getCurrentRef = () => {
    switch (activeTab) {
      case 'output': return outputRef;
      case 'error': return errorRef;
      case 'logs': return logsRef;
      default: return outputRef;
    }
  };

  /**
   * Focus sur le panneau
   */
  const focusPanel = () => {
    if (panelRef.current) {
      panelRef.current.focus();
    }
  };

  /**
   * Vide le contenu du panneau
   */
  const handleClear = () => {
    onClear();
    showNotification('Panneau vidé', 'success');
  };

  /**
   * Copie le contenu dans le presse-papier
   */
  const handleCopy = async () => {
    try {
      const content = getCurrentContent();
      await navigator.clipboard.writeText(content);
      onCopy(content);
      showNotification('Contenu copié dans le presse-papier', 'success');
    } catch (err) {
      showNotification('Erreur lors de la copie', 'error');
    }
  };

  /**
   * Obtient le contenu actuel selon l'onglet actif
   * @returns {string} Contenu actuel
   */
  const getCurrentContent = () => {
    switch (activeTab) {
      case 'output': return output;
      case 'error': return error;
      case 'logs': return getFilteredLogs().map(log => 
        `[${log.timestamp}] ${log.level.toUpperCase()}: ${log.message}`
      ).join('\n');
      default: return output;
    }
  };

  /**
   * Télécharge les résultats
   */
  const handleDownload = () => {
    const content = getAllContent();
    const blob = new Blob([content], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `execution_results_${dateUtils.formatFileName(new Date())}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    
    onDownload(content);
    showNotification('Résultats téléchargés', 'success');
  };

  /**
   * Obtient tout le contenu du panneau
   * @returns {string} Contenu complet
   */
  const getAllContent = () => {
    const sections = [];
    
    if (output) {
      sections.push(`=== SORTIE ===\n${output}`);
    }
    
    if (error) {
      sections.push(`=== ERREURS ===\n${error}`);
    }
    
    if (logs.length > 0) {
      sections.push(`=== LOGS ===\n${logs.map(log => 
        `[${log.timestamp}] ${log.level.toUpperCase()}: ${log.message}`
      ).join('\n')}`);
    }

    if (executionMetrics) {
      sections.push(`=== MÉTRIQUES ===\n${formatExecutionMetrics(executionMetrics)}`);
    }

    return sections.join('\n\n');
  };

  /**
   * Exporte les résultats avec métadonnées
   */
  const handleExportResults = () => {
    const exportData = {
      timestamp: new Date().toISOString(),
      language,
      output,
      error,
      logs,
      metrics: executionMetrics,
      settings: {
        theme,
        fontSize,
        wordWrap,
        showLineNumbers
      }
    };

    const content = JSON.stringify(exportData, null, 2);
    const blob = new Blob([content], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `execution_export_${dateUtils.formatFileName(new Date())}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);

    showNotification('Résultats exportés', 'success');
  };

  /**
   * Formate les métriques d'exécution
   * @param {Object} metrics - Métriques d'exécution
   * @returns {string} Métriques formatées
   */
  const formatExecutionMetrics = (metrics) => {
    if (!metrics) return '';

    return `Temps d'exécution: ${metrics.executionTime}ms
Mémoire utilisée: ${formatters.formatBytes(metrics.memoryUsage)}
CPU: ${metrics.cpuUsage}%
Statut: ${metrics.status}`;
  };

  /**
   * Gère le redimensionnement du panneau
   */
  const handleResize = (newHeight) => {
    setCurrentHeight(Math.max(200, Math.min(800, newHeight)));
    onResize(newHeight);
  };

  /**
   * Gère le début du redimensionnement
   */
  const handleResizeStart = (e) => {
    setIsResizing(true);
    const startY = e.clientY;
    const startHeight = currentHeight;

    const handleMouseMove = (e) => {
      const deltaY = e.clientY - startY;
      handleResize(startHeight + deltaY);
    };

    const handleMouseUp = () => {
      setIsResizing(false);
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    };

    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);
  };

  /**
   * Obtient les statistiques du contenu
   * @returns {Object} Statistiques
   */
  const getContentStats = () => {
    const outputLines = output ? output.split('\n').length : 0;
    const errorLines = error ? error.split('\n').length : 0;
    const logCount = logs.length;

    return { outputLines, errorLines, logCount };
  };

  const stats = getContentStats();
  const filteredLogs = getFilteredLogs();

  return (
    <div 
      ref={panelRef}
      className={`${styles.outputPanel} ${styles[theme]} ${isCollapsed ? styles.collapsed : ''}`}
      style={{ height: currentHeight }}
      tabIndex={0}
    >
      {/* En-tête du panneau */}
      <div className={styles.header}>
        <div className={styles.headerLeft}>
          <h3 className={styles.title}>Sortie d'exécution</h3>
          {isLoading && (
            <div className={styles.loadingIndicator}>
              <span className={styles.spinner}></span>
              <span>Exécution en cours...</span>
            </div>
          )}
        </div>

        <div className={styles.headerRight}>
          {/* Statistiques */}
          <div className={styles.stats}>
            <span className={styles.stat}>
              Sortie: {stats.outputLines} lignes
            </span>
            <span className={styles.stat}>
              Erreurs: {stats.errorLines} lignes
            </span>
            <span className={styles.stat}>
              Logs: {stats.logCount}
            </span>
          </div>

          {/* Actions */}
          <div className={styles.actions}>
            <button
              className={styles.actionButton}
              onClick={handleClear}
              title="Vider le panneau"
              disabled={isLoading}
            >
              🗑️
            </button>
            <button
              className={styles.actionButton}
              onClick={handleCopy}
              title="Copier le contenu"
              disabled={isLoading}
            >
              📋
            </button>
            <button
              className={styles.actionButton}
              onClick={handleDownload}
              title="Télécharger les résultats"
              disabled={isLoading}
            >
              💾
            </button>
            <button
              className={styles.actionButton}
              onClick={handleExportResults}
              title="Exporter avec métadonnées"
              disabled={isLoading}
            >
              📤
            </button>
            {collapsible && (
              <button
                className={styles.collapseButton}
                onClick={() => setIsCollapsed(!isCollapsed)}
                title={isCollapsed ? 'Développer' : 'Réduire'}
              >
                {isCollapsed ? '▲' : '▼'}
              </button>
            )}
          </div>
        </div>
      </div>

      {!isCollapsed && (
        <>
          {/* Onglets */}
          <div className={styles.tabs}>
            <button
              className={`${styles.tab} ${activeTab === 'output' ? styles.active : ''}`}
              onClick={() => setActiveTab('output')}
            >
              Sortie {output && <span className={styles.badge}>{stats.outputLines}</span>}
            </button>
            <button
              className={`${styles.tab} ${activeTab === 'error' ? styles.active : ''} ${error ? styles.hasError : ''}`}
              onClick={() => setActiveTab('error')}
            >
              Erreurs {error && <span className={styles.badge}>{stats.errorLines}</span>}
            </button>
            <button
              className={`${styles.tab} ${activeTab === 'logs' ? styles.active : ''}`}
              onClick={() => setActiveTab('logs')}
            >
              Logs {logs.length > 0 && <span className={styles.badge}>{logs.length}</span>}
            </button>
          </div>

          {/* Barre d'outils */}
          <div className={styles.toolbar}>
            <div className={styles.toolbarLeft}>
              {activeTab === 'logs' && (
                <>
                  <select
                    value={filter}
                    onChange={(e) => setFilter(e.target.value)}
                    className={styles.select}
                  >
                    <option value="all">Tous les logs</option>
                    <option value="info">Info</option>
                    <option value="warn">Avertissement</option>
                    <option value="error">Erreur</option>
                    <option value="debug">Debug</option>
                  </select>
                  <input
                    type="text"
                    placeholder="Rechercher dans les logs..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className={styles.searchInput}
                  />
                </>
              )}
            </div>

            <div className={styles.toolbarRight}>
              <label className={styles.checkboxLabel}>
                <input
                  type="checkbox"
                  checked={wordWrap}
                  onChange={(e) => setWordWrap(e.target.checked)}
                />
                Retour à la ligne
              </label>
              <label className={styles.checkboxLabel}>
                <input
                  type="checkbox"
                  checked={showLineNumbers}
                  onChange={(e) => setShowLineNumbers(e.target.checked)}
                />
                Numéros de ligne
              </label>
              <select
                value={fontSize}
                onChange={(e) => setFontSize(Number(e.target.value))}
                className={styles.fontSizeSelect}
              >
                <option value={12}>12px</option>
                <option value={13}>13px</option>
                <option value={14}>14px</option>
                <option value={16}>16px</option>
                <option value={18}>18px</option>
              </select>
            </div>
          </div>

          {/* Contenu */}
          <div className={styles.content}>
            {/* Onglet Sortie */}
            {activeTab === 'output' && (
              <div
                ref={outputRef}
                className={`${styles.outputContent} ${wordWrap ? styles.wordWrap : ''}`}
                style={{ fontSize: `${fontSize}px` }}
              >
                {output ? (
                  <pre className={styles.pre}>
                    {showLineNumbers && (
                      <div className={styles.lineNumbers}>
                        {output.split('\n').map((_, index) => (
                          <div key={index} className={styles.lineNumber}>
                            {index + 1}
                          </div>
                        ))}
                      </div>
                    )}
                    <div className={styles.codeContent}>
                      {output}
                    </div>
                  </pre>
                ) : (
                  <div className={styles.placeholder}>
                    {isLoading ? 'Exécution en cours...' : 'Aucune sortie'}
                  </div>
                )}
              </div>
            )}

            {/* Onglet Erreurs */}
            {activeTab === 'error' && (
              <div
                ref={errorRef}
                className={`${styles.errorContent} ${wordWrap ? styles.wordWrap : ''}`}
                style={{ fontSize: `${fontSize}px` }}
              >
                {error ? (
                  <pre className={styles.pre}>
                    {showLineNumbers && (
                      <div className={styles.lineNumbers}>
                        {error.split('\n').map((_, index) => (
                          <div key={index} className={styles.lineNumber}>
                            {index + 1}
                          </div>
                        ))}
                      </div>
                    )}
                    <div className={styles.codeContent}>
                      {error}
                    </div>
                  </pre>
                ) : (
                  <div className={styles.placeholder}>
                    Aucune erreur
                  </div>
                )}
              </div>
            )}

            {/* Onglet Logs */}
            {activeTab === 'logs' && (
              <div
                ref={logsRef}
                className={styles.logsContent}
                style={{ fontSize: `${fontSize}px` }}
              >
                {filteredLogs.length > 0 ? (
                  <div className={styles.logsList}>
                    {filteredLogs.map((log, index) => (
                      <div
                        key={index}
                        className={`${styles.logEntry} ${styles[log.level]}`}
                      >
                        <span className={styles.logTimestamp}>
                          [{dateUtils.formatTime(log.timestamp)}]
                        </span>
                        <span className={styles.logLevel}>
                          {log.level.toUpperCase()}
                        </span>
                        <span className={styles.logMessage}>
                          {log.message}
                        </span>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div className={styles.placeholder}>
                    {searchTerm || filter !== 'all' ? 'Aucun log correspondant' : 'Aucun log'}
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Métriques d'exécution */}
          {executionMetrics && (
            <div className={styles.metrics}>
              <div className={styles.metricsTitle}>Métriques d'exécution</div>
              <div className={styles.metricsGrid}>
                <div className={styles.metric}>
                  <span className={styles.metricLabel}>Temps:</span>
                  <span className={styles.metricValue}>{executionMetrics.executionTime}ms</span>
                </div>
                <div className={styles.metric}>
                  <span className={styles.metricLabel}>Mémoire:</span>
                  <span className={styles.metricValue}>
                    {formatters.formatBytes(executionMetrics.memoryUsage)}
                  </span>
                </div>
                <div className={styles.metric}>
                  <span className={styles.metricLabel}>CPU:</span>
                  <span className={styles.metricValue}>{executionMetrics.cpuUsage}%</span>
                </div>
                <div className={styles.metric}>
                  <span className={styles.metricLabel}>Statut:</span>
                  <span className={`${styles.metricValue} ${styles[executionMetrics.status]}`}>
                    {executionMetrics.status}
                  </span>
                </div>
              </div>
            </div>
          )}

          {/* Poignée de redimensionnement */}
          <div
            className={`${styles.resizeHandle} ${isResizing ? styles.resizing : ''}`}
            onMouseDown={handleResizeStart}
            title="Redimensionner le panneau"
          >
            <div className={styles.resizeGrip}></div>
          </div>
        </>
      )}
    </div>
  );
});

OutputPanel.displayName = 'OutputPanel';

export default OutputPanel;