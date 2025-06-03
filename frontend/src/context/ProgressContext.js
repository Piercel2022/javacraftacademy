// src/context/ProgressContext.js
import React, { createContext, useState, useEffect } from 'react';
import progressService from '../services/progressService';

export const ProgressContext = createContext();

export const ProgressProvider = ({ children }) => {
  const [progress, setProgress] = useState({});

  useEffect(() => {
    const fetchProgress = async () => {
      const data = await progressService.getUserProgress();
      setProgress(data);
    };
    fetchProgress();
  }, []);

  const updateProgress = async (courseId, lessonId) => {
    const updated = await progressService.updateProgress(courseId, lessonId);
    setProgress(updated);
  };

  return (
    <ProgressContext.Provider value={{ progress, updateProgress }}>
      {children}
    </ProgressContext.Provider>
  );
};
