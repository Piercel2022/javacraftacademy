// src/services/compilerService.js
import api from './api';

const compileCode = async (language, code) => {
  const response = await api.post('/compiler/execute', { language, code });
  return response.data;
};

export default {
  compileCode,
};
