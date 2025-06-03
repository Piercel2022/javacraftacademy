// src/services/courseService.js
import api from './api';

const getAllCourses = async () => {
  const response = await api.get('/courses');
  return response.data;
};

const getCourseById = async (id) => {
  const response = await api.get(`/courses/${id}`);
  return response.data;
};

export default {
  getAllCourses,
  getCourseById,
};
