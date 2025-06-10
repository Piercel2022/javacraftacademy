import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import './index.css';
import reportWebVitals from './reportWebVitals';

// Contextes globaux
import { AuthProvider } from './context/AuthContext';
import { CourseProvider } from './context/CourseContext';
import { ProgressProvider } from './context/ProgressContext';
import { NotificationProvider } from './context/NotificationContext';
import { ThemeProvider } from './context/ThemeContext';

const root = ReactDOM.createRoot(document.getElementById('root'));

root.render(
  <React.StrictMode>
    <BrowserRouter>
      <ThemeProvider>
        <AuthProvider>
          <CourseProvider>
            <ProgressProvider>
              <NotificationProvider>
                <App />
              </NotificationProvider>
            </ProgressProvider>
          </CourseProvider>
        </AuthProvider>
      </ThemeProvider>
    </BrowserRouter>
  </React.StrictMode>
);

// Mesure des performances
reportWebVitals(console.log);

// Service Worker pour PWA (optionnel)
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js')
      .then((registration) => {
        console.log('SW registered: ', registration);
      })
      .catch((registrationError) => {
        console.log('SW registration failed: ', registrationError);
      });
  });
}