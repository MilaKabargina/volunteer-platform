import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AnimatePresence, motion } from 'framer-motion';
import HomePage from './pages/HomePage';
import InitiativesPage from './pages/InitiativesPage';
import InitiativeDetailsPage from './pages/InitiativeDetailsPage';
import ProfilePage from './pages/ProfilePage';
import ProtectedRoute from './components/ProtectedRoute';
import AdminDashboard from './pages/AdminDashboard';
import AllInitiativesPage from './pages/AllInitiativesPage';
import MyInitiativesApplicationsPage from './pages/MyInitiativesApplicationsPage';
import MyApplicationsPage from './pages/MyApplicationsPage';

const pageVariants = {
  initial: { opacity: 0, y: 20 },
  animate: { opacity: 1, y: 0 },
  exit: { opacity: 0, y: -20 },
};
const pageTransition = { duration: 0.3, ease: 'easeInOut' };

function AnimatedPage({ children }) {
  return (
    <motion.div
      initial="initial"
      animate="animate"
      exit="exit"
      variants={pageVariants}
      transition={pageTransition}
    >
      {children}
    </motion.div>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AnimatePresence mode="wait">
        <Routes>
          <Route path="/" element={
            <AnimatedPage>
              <HomePage />
            </AnimatedPage>
          } />
          <Route path="/login" element={<Navigate to="/" replace />} />
          <Route path="/register" element={<Navigate to="/" replace />} />
          <Route path="/initiatives" element={
            <ProtectedRoute>
              <AnimatedPage>
                <InitiativesPage />
              </AnimatedPage>
            </ProtectedRoute>
          } />
          <Route path="/initiatives/:id" element={
            <ProtectedRoute>
              <AnimatedPage>
                <InitiativeDetailsPage />
              </AnimatedPage>
            </ProtectedRoute>
          } />
          <Route path="/profile" element={
            <ProtectedRoute>
              <AnimatedPage>
                <ProfilePage />
              </AnimatedPage>
            </ProtectedRoute>
          } />
          <Route path="*" element={<Navigate to="/" replace />} />
          <Route
            path="/my-initiatives-applications"
            element={
              <ProtectedRoute>
                <MyInitiativesApplicationsPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/all-initiatives"
            element={
              <ProtectedRoute>
                <AllInitiativesPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute>
                <AdminDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/my-applications"
            element={
              <ProtectedRoute>
                <MyApplicationsPage />
              </ProtectedRoute>
            }
          />
        </Routes>
      </AnimatePresence>
    </BrowserRouter>
  );
}

export default App;