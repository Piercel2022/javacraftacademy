// src/context/CourseContext.js
import React, { createContext, useState, useEffect } from 'react';
import courseService from '../services/courseService';

export const CourseContext = createContext();

export const CourseProvider = ({ children }) => {
  const [courses, setCourses] = useState([]);

  useEffect(() => {
    const fetchCourses = async () => {
      const data = await courseService.getAllCourses();
      setCourses(data);
    };
    fetchCourses();
  }, []);

  return (
    <CourseContext.Provider value={{ courses }}>
      {children}
    </CourseContext.Provider>
  );
};
