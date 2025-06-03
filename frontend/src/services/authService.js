// src/services/authService.js
import api from './api';

const login = async ({ email, password }) => {
  const response = await api.post('/auth/login', { email, password });
  const { accessToken, user } = response.data;
  localStorage.setItem('accessToken', accessToken);
  localStorage.setItem('user', JSON.stringify(user));
  return user;
};

const logout = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('user');
};

const getCurrentUser = () => {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
};

export default {
  login,
  logout,
  getCurrentUser,
};
