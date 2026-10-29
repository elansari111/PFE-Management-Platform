import { Routes, Route, Navigate } from 'react-router-dom'
import { MainLayout } from '../components/layout'
import { DashboardPage } from '../pages/dashboard'
import { ProjectsPage } from '../pages/projects'
import { TasksPage } from '../pages/tasks'
import { DeliverablesPage } from '../pages/deliverables'
import { SupervisionPage } from '../pages/supervision'
import { AIAssistantPage } from '../pages/ai-assistant'
import { NotificationsPage } from '../pages/notifications'
import { ProfilePage } from '../pages/profile'
import { SettingsPage } from '../pages/settings'
import { NotFoundPage } from '../pages/not-found'
import { AuthPage } from '../pages/auth'
import { ProtectedRoute } from './ProtectedRoute'

export function AppRoutes() {
  return (
    <Routes>
      {/* Route publique : Authentification / Connexion */}
      <Route path="/login" element={<AuthPage />} />
      <Route path="/auth" element={<Navigate to="/login" replace />} />

      {/* Routes protégées : nécessitent une session active */}
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <MainLayout />
          </ProtectedRoute>
        }
      >
        {/* Redirection automatique de la racine vers le tableau de bord */}
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="projects" element={<ProjectsPage />} />
        <Route path="tasks" element={<TasksPage />} />
        <Route path="deliverables" element={<DeliverablesPage />} />
        <Route path="supervision" element={<SupervisionPage />} />
        <Route path="ai-assistant" element={<AIAssistantPage />} />
        <Route path="notifications" element={<NotificationsPage />} />
        <Route path="profile" element={<ProfilePage />} />
        <Route path="settings" element={<SettingsPage />} />

        {/* Route 404 dans le Layout */}
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}

export default AppRoutes
