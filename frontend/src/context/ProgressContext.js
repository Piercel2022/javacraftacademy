// Complete fix for ProgressContext.js

import React, { createContext, useContext, useReducer, useEffect } from 'react';
import progressService from '../services/progressService';

// Initial state
const initialState = {
  overallProgress: null,
  courseProgress: {},
  lessonProgress: {},
  loading: false,
  error: null
};

// Reducer
const progressReducer = (state, action) => {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, loading: action.payload };
    case 'SET_ERROR':
      return { ...state, error: action.payload, loading: false };
    case 'SET_OVERALL_PROGRESS':
      return { ...state, overallProgress: action.payload, loading: false, error: null };
    case 'SET_COURSE_PROGRESS':
      return { 
        ...state, 
        courseProgress: { ...state.courseProgress, [action.courseId]: action.payload },
        loading: false,
        error: null
      };
    case 'SET_LESSON_PROGRESS':
      return { 
        ...state, 
        lessonProgress: { ...state.lessonProgress, [action.lessonId]: action.payload },
        loading: false,
        error: null
      };
    case 'CLEAR_ERROR':
      return { ...state, error: null };
    default:
      return state;
  }
};

// Context
const ProgressContext = createContext();

// Provider
export const ProgressProvider = ({ children }) => {
  const [state, dispatch] = useReducer(progressReducer, initialState);

  // Fetch overall progress
  const fetchOverallProgress = async () => {
    dispatch({ type: 'SET_LOADING', payload: true });
    try {
      const response = await progressService.getOverallProgress();
      
      if (response.success) {
        dispatch({ type: 'SET_OVERALL_PROGRESS', payload: response.data });
      } else {
        dispatch({ type: 'SET_ERROR', payload: response.error });
      }
    } catch (error) {
      dispatch({ type: 'SET_ERROR', payload: 'Erreur de connexion' });
    }
  };

  // Fetch course progress
  const fetchCourseProgress = async (courseId) => {
    dispatch({ type: 'SET_LOADING', payload: true });
    try {
      const response = await progressService.getCourseProgress(courseId);
      
      if (response.success) {
        dispatch({ 
          type: 'SET_COURSE_PROGRESS', 
          courseId, 
          payload: response.data 
        });
      } else {
        dispatch({ type: 'SET_ERROR', payload: response.error });
      }
    } catch (error) {
      dispatch({ type: 'SET_ERROR', payload: 'Erreur de connexion' });
    }
  };

  // Fetch lesson progress
  const fetchLessonProgress = async (lessonId) => {
    dispatch({ type: 'SET_LOADING', payload: true });
    try {
      const response = await progressService.getLessonProgress(lessonId);
      
      if (response.success) {
        dispatch({ 
          type: 'SET_LESSON_PROGRESS', 
          lessonId, 
          payload: response.data 
        });
      } else {
        dispatch({ type: 'SET_ERROR', payload: response.error });
      }
    } catch (error) {
      dispatch({ type: 'SET_ERROR', payload: 'Erreur de connexion' });
    }
  };

  // Update progress
  const updateProgress = async (progressData) => {
    try {
      const response = await progressService.updateProgress(progressData);
      
      if (response.success) {
        // Refresh overall progress after update
        await fetchOverallProgress();
        return response;
      } else {
        dispatch({ type: 'SET_ERROR', payload: response.error });
        return response;
      }
    } catch (error) {
      dispatch({ type: 'SET_ERROR', payload: 'Erreur de mise à jour' });
      return { success: false, error: 'Erreur de mise à jour' };
    }
  };

  // Clear error
  const clearError = () => {
    dispatch({ type: 'CLEAR_ERROR' });
  };

  // Load initial data
  useEffect(() => {
    fetchOverallProgress();
  }, []);

  const value = {
    ...state,
    fetchOverallProgress,
    fetchCourseProgress,
    fetchLessonProgress,
    updateProgress,
    clearError,
    // Add other service methods as needed
    startLesson: progressService.startLesson.bind(progressService),
    completeLesson: progressService.completeLesson.bind(progressService),
    startCourse: progressService.startCourse.bind(progressService),
    completeCourse: progressService.completeCourse.bind(progressService),
  };

  return (
    <ProgressContext.Provider value={value}>
      {children}
    </ProgressContext.Provider>
  );
};

// Hook
export const useProgress = () => {
  const context = useContext(ProgressContext);
  if (!context) {
    throw new Error('useProgress must be used within a ProgressProvider');
  }
  return context;
};