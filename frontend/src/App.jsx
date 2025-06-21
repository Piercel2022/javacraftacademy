import React, { Suspense, lazy } from 'react';
import { Routes, Route } from 'react-router-dom';
import './App.css';

// Components critiques (pas de lazy loading)
import Header from './components/common/Header';
import Footer from './components/common/Footer';
import ErrorBoundary from './components/common/ErrorBoundary';
import ProtectedRoute from './components/auth/ProtectedRoute';
import LoadingSpinner from './components/common/LoadingSpinner/LoadingSpinner';
import NotificationsContainer from './components/common/NotificationsContainer';

// Pages critiques (chargement immédiat)
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import NotFound from './pages/NotFound';

// Lazy loading pour les pages moins critiques
const Dashboard = lazy(() => import('./pages/Dashboard'));
const Courses = lazy(() => import('./pages/Courses'));
const CourseDetail = lazy(() => import('./pages/CourseDetail'));
const Lesson = lazy(() => import('./pages/Lesson'));
const CodePlayground = lazy(() => import('./pages/CodePlayground'));
const Profile = lazy(() => import('./pages/Profile'));
const Progress = lazy(() => import('./pages/Progress'));

// Pages secondaires
const About = lazy(() => import('./pages/About'));
const Contact = lazy(() => import('./pages/Contact'));
const Help = lazy(() => import('./pages/Help'));
const FAQ = lazy(() => import('./pages/FAQ'));
const Community = lazy(() => import('./pages/Community'));
const Blog = lazy(() => import('./pages/Blog'));
const BlogPost = lazy(() => import('./pages/BlogPost'));

// Pages légales
const Terms = lazy(() => import('./pages/Terms'));
const Privacy = lazy(() => import('./pages/Privacy'));
const Legal = lazy(() => import('./pages/Legal'));
const Cookies = lazy(() => import('./pages/Cookies'));

// Hooks
import { useAuth } from './hooks/useAuth';
import { useNotification } from './hooks/useNotification';

// Composant de chargement pour les pages
const PageSuspenseFallback = () => (
  <div className="page-loading">
    <LoadingSpinner />
    <p>Chargement de la page...</p>
  </div>
);

// Configuration des routes pour une meilleure organisation
const publicRoutes = [
  { path: '/', element: <Home />, exact: true },
  { path: '/about', element: <About /> },
  { path: '/contact', element: <Contact /> },
  { path: '/courses', element: <Courses /> },
  { path: '/courses/:id', element: <CourseDetail /> },
  { path: '/help', element: <Help /> },
  { path: '/faq', element: <FAQ /> },
  { path: '/community', element: <Community /> },
  { path: '/blog', element: <Blog /> },
  { path: '/blog/:id', element: <BlogPost /> },
  { path: '/terms', element: <Terms /> },
  { path: '/privacy', element: <Privacy /> },
  { path: '/legal', element: <Legal /> },
  { path: '/cookies', element: <Cookies /> },
];

const authRoutes = [
  { path: '/login', element: <Login /> },
  { path: '/register', element: <Register /> },
];

const protectedRoutes = [
  { path: '/dashboard', element: <Dashboard /> },
  { path: '/lesson/:courseId/:lessonId', element: <Lesson /> },
  { path: '/playground', element: <CodePlayground /> },
  { path: '/profile', element: <Profile /> },
  { path: '/progress', element: <Progress /> },
];

function App() {
  const { isLoading, isAuthenticated } = useAuth();
  const { notifications } = useNotification();

  // Chargement initial de l'application
  if (isLoading) {
    return (
      <div className="app-loading" role="status" aria-label="Chargement de l'application">
        <LoadingSpinner size="large" />
        <p>Chargement de JavaCraft Academy...</p>
      </div>
    );
  }

  return (
    <ErrorBoundary>
      <div className="App">
        <Header />
        
        <main className="main-content" role="main">
          <Suspense fallback={<PageSuspenseFallback />}>
            <Routes>
              {/* Routes publiques */}
              {publicRoutes.map(({ path, element }) => (
                <Route key={path} path={path} element={element} />
              ))}
              
              {/* Routes d'authentification */}
              {authRoutes.map(({ path, element }) => (
                <Route key={path} path={path} element={element} />
              ))}
              
              {/* Routes protégées */}
              {protectedRoutes.map(({ path, element }) => (
                <Route
                  key={path}
                  path={path}
                  element={
                    <ProtectedRoute isAuthenticated={isAuthenticated}>
                      {element}
                    </ProtectedRoute>
                  }
                />
              ))}

              {/* Route 404 - doit être en dernier */}
              <Route path="*" element={<NotFound />} />
            </Routes>
          </Suspense>
        </main>
        
        <Footer />
        
        {/* Notifications */}
        <NotificationsContainer notifications={notifications} />
      </div>
    </ErrorBoundary>
  );
}

export default App;