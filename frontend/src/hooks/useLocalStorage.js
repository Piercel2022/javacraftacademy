// JavaCraft Academy/frontend/src/hooks/useLocalStorage.js
// Hook personnalisé pour la gestion du localStorage avec synchronisation d'état

import { useState, useEffect, useCallback } from 'react';

/**
 * Hook personnalisé pour gérer le localStorage avec synchronisation automatique
 * @param {string} key - Clé du localStorage
 * @param {*} initialValue - Valeur initiale si aucune valeur n'existe
 * @returns {[*, function, function]} - [valeur, setter, remover]
 */
export const useLocalStorage = (key, initialValue) => {
  // État local pour stocker la valeur
  const [storedValue, setStoredValue] = useState(() => {
    try {
      // Récupérer la valeur du localStorage
      const item = window.localStorage.getItem(key);
      // Parser la valeur JSON ou retourner la valeur initiale
      return item ? JSON.parse(item) : initialValue;
    } catch (error) {
      console.error(`Erreur lors de la lecture du localStorage pour la clé "${key}":`, error);
      return initialValue;
    }
  });

  // Fonction pour mettre à jour la valeur
  const setValue = useCallback((value) => {
    try {
      // Permettre à la valeur d'être une fonction pour être cohérent avec useState
      const valueToStore = value instanceof Function ? value(storedValue) : value;
      
      // Sauvegarder dans l'état local
      setStoredValue(valueToStore);
      
      // Sauvegarder dans le localStorage
      if (valueToStore === undefined) {
        window.localStorage.removeItem(key);
      } else {
        window.localStorage.setItem(key, JSON.stringify(valueToStore));
      }
      
      // Déclencher un événement personnalisé pour synchroniser les autres composants
      window.dispatchEvent(new CustomEvent('localStorageChange', {
        detail: { key, value: valueToStore }
      }));
    } catch (error) {
      console.error(`Erreur lors de l'écriture dans le localStorage pour la clé "${key}":`, error);
    }
  }, [key, storedValue]);

  // Fonction pour supprimer la valeur
  const removeValue = useCallback(() => {
    try {
      setStoredValue(undefined);
      window.localStorage.removeItem(key);
      
      // Déclencher un événement pour la suppression
      window.dispatchEvent(new CustomEvent('localStorageChange', {
        detail: { key, value: undefined }
      }));
    } catch (error) {
      console.error(`Erreur lors de la suppression du localStorage pour la clé "${key}":`, error);
    }
  }, [key]);

  // Écouter les changements du localStorage depuis d'autres onglets/fenêtres
  useEffect(() => {
    const handleStorageChange = (e) => {
      if (e.key === key) {
        try {
          const newValue = e.newValue ? JSON.parse(e.newValue) : initialValue;
          setStoredValue(newValue);
        } catch (error) {
          console.error(`Erreur lors de la synchronisation du localStorage pour la clé "${key}":`, error);
        }
      }
    };

    // Écouter les changements personnalisés (même onglet)
    const handleCustomStorageChange = (e) => {
      if (e.detail.key === key) {
        setStoredValue(e.detail.value);
      }
    };

    window.addEventListener('storage', handleStorageChange);
    window.addEventListener('localStorageChange', handleCustomStorageChange);

    return () => {
      window.removeEventListener('storage', handleStorageChange);
      window.removeEventListener('localStorageChange', handleCustomStorageChange);
    };
  }, [key, initialValue]);

  return [storedValue, setValue, removeValue];
};

/**
 * Hook pour gérer plusieurs valeurs du localStorage
 * @param {Object} initialValues - Objet avec les clés et valeurs initiales
 * @returns {Object} - Objet avec les valeurs et fonctions de gestion
 */
export const useMultipleLocalStorage = (initialValues) => {
  const [values, setValues] = useState(() => {
    const result = {};
    Object.keys(initialValues).forEach(key => {
      try {
        const item = window.localStorage.getItem(key);
        result[key] = item ? JSON.parse(item) : initialValues[key];
      } catch (error) {
        console.error(`Erreur lors de la lecture du localStorage pour la clé "${key}":`, error);
        result[key] = initialValues[key];
      }
    });
    return result;
  });

  const setMultipleValues = useCallback((updates) => {
    const newValues = { ...values };
    
    Object.keys(updates).forEach(key => {
      try {
        const value = updates[key];
        newValues[key] = value;
        
        if (value === undefined) {
          window.localStorage.removeItem(key);
        } else {
          window.localStorage.setItem(key, JSON.stringify(value));
        }
      } catch (error) {
        console.error(`Erreur lors de l'écriture dans le localStorage pour la clé "${key}":`, error);
      }
    });
    
    setValues(newValues);
  }, [values]);

  const clearAll = useCallback(() => {
    Object.keys(initialValues).forEach(key => {
      try {
        window.localStorage.removeItem(key);
      } catch (error) {
        console.error(`Erreur lors de la suppression du localStorage pour la clé "${key}":`, error);
      }
    });
    setValues(initialValues);
  }, [initialValues]);

  return {
    values,
    setValues: setMultipleValues,
    clearAll
  };
};

export default useLocalStorage;