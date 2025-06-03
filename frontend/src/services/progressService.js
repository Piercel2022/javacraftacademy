// src/services/progressService.js
import api from './api';

const getUserProgress = async () => {
  const response = await api.get('/progress');
  return response.data;
};

const updateProgress = async (courseId, lessonId) => {
  const response = await api.post('/progress/update', { courseId, lessonId });
  return response.data;
};

export default {
  getUserProgress,
  updateProgress,
};
