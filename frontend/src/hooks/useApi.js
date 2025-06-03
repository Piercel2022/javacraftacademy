// JavaCraft Academy/frontend/src/hooks/useApi.js
// Hook personnalisé pour la gestion des appels API avec gestion d'état et cache

import { useState, useEffect, useCallback, useRef } from 'react';

/**
 * Hook personnalisé pour effectuer des appels API avec gestion d'état
 * @param {string|function} url - URL de l'API ou fonction qui retourne l'URL
 * @param {Object} options - Options de configuration
 * @returns {Object} - État et fonctions de gestion de l'API
 */
export const useApi = (url, options = {}) => {
  const {
    method = 'GET',
    headers = {},
    body = null,
    dependencies = [],
    immediate = true,
    transform = null,
    onSuccess = null,
    onError = null,
    retryCount = 0,
    retryDelay = 1000,
    timeout = 10000
  } = options;

  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [lastFetch, setLastFetch] = useState(null);
  
  const abortControllerRef = useRef(null);
  const retryCountRef = useRef(0);

  // Fonction pour effectuer l'appel API
  const fetchData = useCallback(async (customOptions = {}) => {
    // Annuler la requête précédente si elle existe
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }

    // Créer un nouveau contrôleur d'abandon
    abortControllerRef.current = new AbortController();
    
    setLoading(true);
    setError(null);
    
    try {
      const requestUrl = typeof url === 'function' ? url() : url;
      
      if (!requestUrl) {
        throw new Error('URL manquante pour l\'appel API');
      }

      const requestOptions = {
        method: customOptions.method || method,
        headers: {
          'Content-Type': 'application/json',
          ...headers,
          ...customOptions.headers
        },
        signal: abortControllerRef.current.signal,
        ...customOptions
      };

      // Ajouter le body si nécessaire
      const requestBody = customOptions.body || body;
      if (requestBody && method !== 'GET') {
        requestOptions.body = typeof requestBody === 'string' 
          ? requestBody 
          : JSON.stringify(requestBody);
      }

      // Créer une promesse avec timeout
      const timeoutPromise = new Promise((_, reject) =>
        setTimeout(() => reject(new Error('Timeout de la requête')), timeout)
      );

      const fetchPromise = fetch(requestUrl, requestOptions);
      const response = await Promise.race([fetchPromise, timeoutPromise]);

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status} ${response.statusText}`);
      }

      const contentType = response.headers.get('content-type');
      let responseData;

      if (contentType && contentType.includes('application/json')) {
        responseData = await response.json();
      } else {
        responseData = await response.text();
      }

      // Transformer les données si une fonction de transformation est fournie
      const finalData = transform ? transform(responseData) : responseData;
      
      setData(finalData);
      setLastFetch(new Date());
      retryCountRef.current = 0;

      // Callback de succès
      if (onSuccess) {
        onSuccess(finalData, response);
      }

      return finalData;

    } catch (err) {
      if (err.name === 'AbortError') {
        console.log('Requête annulée');
        return;
      }

      console.error('Erreur API:', err);
      
      // Logique de retry
      if (retryCountRef.current < retryCount) {
        retryCountRef.current++;
        console.log(`Tentative ${retryCountRef.current} sur ${retryCount}`);
        
        setTimeout(() => {
          fetchData(customOptions);
        }, retryDelay * retryCountRef.current);
        
        return;
      }

      setError(err);
      
      // Callback d'erreur
      if (onError) {
        onError(err);
      }
      
      throw err;
    } finally {
      setLoading(false);
    }
  }, [url, method, headers, body, transform, onSuccess, onError, retryCount, retryDelay, timeout]);

  // Fonction pour refetch les données
  const refetch = useCallback(() => {
    return fetchData();
  }, [fetchData]);

  // Fonction pour effectuer un appel avec des options personnalisées
  const mutate = useCallback((customOptions) => {
    return fetchData(customOptions);
  }, [fetchData]);

  // Effet pour le chargement initial
  useEffect(() => {
    if (immediate && url) {
      fetchData();
    }

    // Cleanup
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, dependencies);

  return {
    data,
    loading,
    error,
    lastFetch,
    refetch,
    mutate,
    cancel: () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    }
  };
};

/**
 * Hook pour gérer plusieurs appels API en parallèle
 * @param {Array} requests - Tableau de configurations de requêtes
 * @returns {Object} - État combiné des requêtes
 */
export const useMultipleApi = (requests = []) => {
  const [combinedData, setCombinedData] = useState({});
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});
  const [completed, setCompleted] = useState(0);

  const fetchAll = useCallback(async () => {
    setLoading(true);
    setErrors({});
    setCompleted(0);
    
    const promises = requests.map(async (request, index) => {
      try {
        const response = await fetch(request.url, {
          method: request.method || 'GET',
          headers: {
            'Content-Type': 'application/json',
            ...request.headers
          },
          body: request.body ? JSON.stringify(request.body) : null
        });

        if (!response.ok) {
          throw new Error(`Erreur HTTP: ${response.status}`);
        }

        const data = await response.json();
        const transformedData = request.transform ? request.transform(data) : data;
        
        setCombinedData(prev => ({
          ...prev,
          [request.key || index]: transformedData
        }));
        
        setCompleted(prev => prev + 1);
        
        return { success: true, data: transformedData, key: request.key || index };
      } catch (error) {
        setErrors(prev => ({
          ...prev,
          [request.key || index]: error
        }));
        
        setCompleted(prev => prev + 1);
        
        return { success: false, error, key: request.key || index };
      }
    });

    const results = await Promise.allSettled(promises);
    setLoading(false);
    
    return results;
  }, [requests]);

  useEffect(() => {
    if (requests.length > 0) {
      fetchAll();
    }
  }, [fetchAll]);

  return {
    data: combinedData,
    loading,
    errors,
    completed,
    total: requests.length,
    isComplete: completed === requests.length,
    refetchAll: fetchAll
  };
};

/**
 * Hook pour gérer un cache simple des requêtes API
 * @param {string} cacheKey - Clé unique pour le cache
 * @param {number} maxAge - Durée de vie du cache en millisecondes
 * @returns {Object} - Fonctions de gestion du cache
 */
export const useApiCache = (cacheKey, maxAge = 5 * 60 * 1000) => { // 5 minutes par défaut
  const [cache, setCache] = useState(() => {
    try {
      const cached = localStorage.getItem(`api_cache_${cacheKey}`);
      if (cached) {
        const { data, timestamp } = JSON.parse(cached);
        if (Date.now() - timestamp < maxAge) {
          return { data, timestamp, isValid: true };
        }
      }
    } catch (error) {
      console.error('Erreur lors de la lecture du cache:', error);
    }
    return { data: null, timestamp: null, isValid: false };
  });

  const setCache = useCallback((data) => {
    const cacheData = {
      data,
      timestamp: Date.now()
    };
    
    try {
      localStorage.setItem(`api_cache_${cacheKey}`, JSON.stringify(cacheData));
      setCache({ ...cacheData, isValid: true });
    } catch (error) {
      console.error('Erreur lors de la sauvegarde du cache:', error);
    }
  }, [cacheKey]);

  const clearCache = useCallback(() => {
    try {
      localStorage.removeItem(`api_cache_${cacheKey}`);
      setCache({ data: null, timestamp: null, isValid: false });
    } catch (error) {
      console.error('Erreur lors de la suppression du cache:', error);
    }
  }, [cacheKey]);

  const isExpired = useCallback(() => {
    return !cache.timestamp || (Date.now() - cache.timestamp) > maxAge;
  }, [cache.timestamp, maxAge]);

  return {
    cachedData: cache.data,
    isValid: cache.isValid && !isExpired(),
    setCache,
    clearCache,
    isExpired
  };
};

export default useApi;