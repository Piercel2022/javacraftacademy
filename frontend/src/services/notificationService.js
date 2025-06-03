// src/services/notificationService.js
import api from './api';

const getNotifications = async () => {
  const response = await api.get('/notifications');
  return response.data;
};

const markAsRead = async (id) => {
  const response = await api.post(`/notifications/${id}/read`);
  return response.data;
};

export default {
  getNotifications,
  markAsRead,
};
