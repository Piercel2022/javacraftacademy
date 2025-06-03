// src/services/userService.js
import api from './api';

const getUserProfile = async () => {
  const response = await api.get('/user/profile');
  return response.data;
};

const updateUserProfile = async (profileData) => {
  const response = await api.put('/user/profile', profileData);
  return response.data;
};

export default {
  getUserProfile,
  updateUserProfile,
};
