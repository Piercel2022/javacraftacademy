import React from 'react';
import { Routes, Route } from 'react-router-dom';
import './App.css';

// Components
import Header from './components/common/Header';
import Footer from './components/common/Footer';
import ErrorBoundary from './components/common/ErrorBoundary';
import ProtectedRoute from './components/auth/ProtectedRoute';

// Pages - Existantes
import Home from './pages/Home';
{/* 
import Login from './pages/Login';
import Register from './pages/Register';
*/}

import Dashboard from './pages/Dashboard';
import Courses from './pages/Courses';
import CourseDetail from './pages/CourseDetail';
import Lesson from './pages/Lesson';
import CodePlayground from './pages/CodePlayground';
import Profile from './pages/Profile';
import Progress from './pages/Progress';
import NotFound from './pages/NotFound';

// Pages - Nouvelles à ajouter
import About from './pages/About';
import Contact from './pages/Contact';
import Help from './pages/Help';
import FAQ from './pages/FAQ';
import Community from './pages/Community';
import Blog from './pages/Blog';
import BlogPost from './pages/BlogPost';
import Terms from './pages/Terms';
import Privacy from './pages/Privacy';
import Legal from './pages/Legal';
import Cookies from './pages/Cookies';

// Hooks
import { useAuth } from './hooks/useAuth';
import { useNotification } from './hooks/useNotification';

function App() {
  const { isLoading } = useAuth();
  const { notifications } = useNotification();

  if (isLoading) {
    return (
      <div className="app-loading">
        <div className="loading-spinner"></div>
        <p>Chargement de JavaCraft Academy...</p>
      </div>
    );
  }

  return (
    <ErrorBoundary>
      <div className="App">
        <Header />
        <main className="main-content">
          <Routes>
            {/* Routes publiques principales */}
            <Route path="/" element={<Home />} />
            <Route path="/about" element={<About />} />
            <Route path="/contact" element={<Contact />} />
            <Route path="/courses" element={<Courses />} />
            <Route path="/courses/:id" element={<CourseDetail />} />
            
            {/* Routes de support et communauté */}
            <Route path="/help" element={<Help />} />
            <Route path="/faq" element={<FAQ />} />
            <Route path="/community" element={<Community />} />
            
            {/* Routes blog */}
            <Route path="/blog" element={<Blog />} />
            <Route path="/blog/:id" element={<BlogPost />} />
            
            {/* Routes légales */}
            <Route path="/terms" element={<Terms />} />
            <Route path="/privacy" element={<Privacy />} />
            <Route path="/legal" element={<Legal />} />
            <Route path="/cookies" element={<Cookies />} />
            
            {/* Routes d'authentification 
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            */}
            

            {/* Routes protégées */}
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/lesson/:courseId/:lessonId"
              element={
                <ProtectedRoute>
                  <Lesson />
                </ProtectedRoute>
              }
            />
            <Route
              path="/playground"
              element={
                <ProtectedRoute>
                  <CodePlayground />
                </ProtectedRoute>
              }
            />
            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <Profile />
                </ProtectedRoute>
              }
            />
            <Route
              path="/progress"
              element={
                <ProtectedRoute>
                  <Progress />
                </ProtectedRoute>
              }
            />

            {/* Route 404 */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </main>
        <Footer />
        
        {/* Notifications Toast */}
        {notifications.length > 0 && (
          <div className="notifications-container">
            {notifications.map((notification) => (
              <div
                key={notification.id}
                className={`notification notification--${notification.type}`}
              >
                {notification.message}
              </div>
            ))}
          </div>
        )}
      </div>
    </ErrorBoundary>
  );
}

export default App;