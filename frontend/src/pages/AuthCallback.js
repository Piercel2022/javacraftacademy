// pages/AuthCallback.js
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../services/authService';

const AuthCallback = () => {
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('Traitement de l\'authentification...');
  const navigate = useNavigate();

  useEffect(() => {
    const handleCallback = async () => {
      try {
        const result = authService.handleAuthCallback();
        
        if (result.success) {
          setMessage('Connexion réussie ! Redirection...');
          setTimeout(() => {
            navigate('/dashboard'); // ou votre page d'accueil
          }, 2000);
        } else {
          setMessage(result.message || 'Erreur lors de l\'authentification');
          setTimeout(() => {
            navigate('/login');
          }, 3000);
        }
      } catch (error) {
        console.error('Erreur callback:', error);
        setMessage('Erreur lors du traitement de l\'authentification');
        setTimeout(() => {
          navigate('/login');
        }, 3000);
      } finally {
        setLoading(false);
      }
    };

    handleCallback();
  }, [navigate]);

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-50">
      <div className="max-w-md w-full bg-white shadow-lg rounded-lg p-6">
        <div className="text-center">
          {loading ? (
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          ) : (
            <div className="mb-4">
              {message.includes('réussie') ? (
                <div className="h-12 w-12 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7"></path>
                  </svg>
                </div>
              ) : (
                <div className="h-12 w-12 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="h-6 w-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"></path>
                  </svg>
                </div>
              )}
            </div>
          )}
          <h2 className="text-xl font-semibold text-gray-900 mb-2">
            Authentification OAuth
          </h2>
          <p className="text-gray-600">
            {message}
          </p>
        </div>
      </div>
    </div>
  );
};

export default AuthCallback;