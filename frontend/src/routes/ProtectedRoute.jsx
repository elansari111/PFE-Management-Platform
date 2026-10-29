import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context'
import { Spinner } from '../components/ui'

/**
 * Composant de protection des routes.
 * - Si l'authentification est en cours de chargement → affiche un spinner centré.
 * - Si l'utilisateur n'est pas connecté → redirige vers /auth en conservant
 *   la route d'origine dans `state.from` pour y revenir après connexion.
 * - Si l'utilisateur est connecté → rend les enfants normalement.
 */
export function ProtectedRoute({ children }) {
  const { isAuthenticated, isLoading } = useAuth()
  const location = useLocation()

  // Attendre la vérification du token en localStorage avant de décider
  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-slate-50">
        <div className="flex flex-col items-center gap-3">
          <Spinner size="lg" color="primary" />
          <p className="text-sm text-slate-500">Vérification de la session…</p>
        </div>
      </div>
    )
  }

  if (!isAuthenticated) {
    // Sauvegarder la destination voulue pour rediriger après login
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return children
}

export default ProtectedRoute

