import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { LogIn, UserPlus, Shield, Lock, Mail, User, AlertCircle, CheckCircle } from 'lucide-react'
import { useAuth } from '../../context'
import { Card, Button, useToast } from '../../components/ui'

export function AuthPage() {
  const { login, register, isAuthenticated, user, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const toast = useToast()

  const [isLoginMode, setIsLoginMode] = useState(true)
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  })
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState(null)

  const from = location.state?.from?.pathname || '/dashboard'

  const handleSubmit = async (e) => {
    e.preventDefault()
    setIsLoading(true)
    setErrorMessage(null)

    try {
      if (isLoginMode) {
        const loggedUser = await login(formData.email.trim(), formData.password)
        toast.success(
          'Connexion réussie',
          `Bienvenue, ${loggedUser.firstName} ${loggedUser.lastName} (${loggedUser.role})`
        )
        navigate(from, { replace: true })
      } else {
        const newUser = await register({
          firstName: formData.firstName.trim(),
          lastName: formData.lastName.trim(),
          email: formData.email.trim(),
          password: formData.password,
        })
        toast.success(
          'Compte créé avec succès',
          `Bienvenue ${newUser.firstName} ! Votre compte est activé avec le rôle ${newUser.role}.`
        )
        navigate(from, { replace: true })
      }
    } catch (err) {
      console.error('Erreur authentification:', err)
      setErrorMessage(
        err.message || 'Identifiants invalides ou impossible de contacter le serveur.'
      )
    } finally {
      setIsLoading(false)
    }
  }

  // Comptes de démonstration préconfigurés
  const handleQuickLogin = (email, password) => {
    setFormData((prev) => ({ ...prev, email, password }))
    setErrorMessage(null)
  }

  if (isAuthenticated && user) {
    return (
      <div className="max-w-md mx-auto my-12 p-6 bg-white rounded-2xl shadow-sm border border-slate-200 text-center space-y-4">
        <div className="w-12 h-12 mx-auto rounded-full bg-emerald-100 text-emerald-600 flex items-center justify-center">
          <CheckCircle className="w-6 h-6" />
        </div>
        <h2 className="text-xl font-bold text-slate-900">Session active</h2>
        <p className="text-sm text-slate-500">
          Vous êtes connecté en tant que <strong className="text-slate-800">{user.firstName} {user.lastName}</strong> ({user.email}).
        </p>
        <div className="flex gap-2 justify-center pt-2">
          <Button variant="primary" onClick={() => navigate('/dashboard')}>
            Aller au tableau de bord
          </Button>
          <Button variant="outline" onClick={logout}>
            Se déconnecter
          </Button>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-lg mx-auto my-8 px-4 animate-in fade-in duration-200">
      <div className="text-center mb-6">
        <h1 className="text-2xl font-black text-[#1E3A8A] tracking-tight">
          Plateforme PFE &bull; Authentification
        </h1>
        <p className="text-xs text-slate-500 mt-1">
          Connectez-vous pour accéder à vos projets, livrables et recommandations IA.
        </p>
      </div>

      <Card className="p-6 border border-slate-200 shadow-sm">
        {/* Onglets Connexion / Inscription */}
        <div className="flex border-b border-slate-200 mb-6">
          <button
            type="button"
            onClick={() => {
              setIsLoginMode(true)
              setErrorMessage(null)
            }}
            className={`flex-1 pb-3 text-xs font-bold text-center border-b-2 transition-colors ${
              isLoginMode
                ? 'border-[#1E3A8A] text-[#1E3A8A]'
                : 'border-transparent text-slate-400 hover:text-slate-700'
            }`}
          >
            Se Connecter
          </button>
          <button
            type="button"
            onClick={() => {
              setIsLoginMode(false)
              setErrorMessage(null)
            }}
            className={`flex-1 pb-3 text-xs font-bold text-center border-b-2 transition-colors ${
              !isLoginMode
                ? 'border-[#1E3A8A] text-[#1E3A8A]'
                : 'border-transparent text-slate-400 hover:text-slate-700'
            }`}
          >
            Créer un Compte Étudiant
          </button>
        </div>

        {errorMessage && (
          <div className="mb-4 p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-800 flex items-start gap-2.5 text-xs">
            <AlertCircle className="w-4 h-4 text-rose-600 shrink-0 mt-0.5" />
            <span>{errorMessage}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {!isLoginMode && (
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Prénom</label>
                <div className="relative">
                  <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                    <User className="w-4 h-4" />
                  </div>
                  <input
                    type="text"
                    required
                    value={formData.firstName}
                    onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                    placeholder="Prénom"
                    className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-[#1E3A8A] focus:bg-white"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Nom</label>
                <input
                  type="text"
                  required
                  value={formData.lastName}
                  onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                  placeholder="Nom"
                  className="w-full px-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-[#1E3A8A] focus:bg-white"
                />
              </div>
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Adresse Email</label>
            <div className="relative">
              <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                <Mail className="w-4 h-4" />
              </div>
              <input
                type="email"
                required
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                placeholder="votre.email@pfe.local"
                className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-[#1E3A8A] focus:bg-white"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-700 mb-1">Mot de passe</label>
            <div className="relative">
              <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
                <Lock className="w-4 h-4" />
              </div>
              <input
                type="password"
                required
                minLength={8}
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                placeholder="••••••••"
                className="w-full pl-9 pr-3 py-2 text-xs bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:border-[#1E3A8A] focus:bg-white"
              />
            </div>
          </div>

          <Button
            type="submit"
            variant="primary"
            className="w-full justify-center mt-2"
            isLoading={isLoading}
            leftIcon={isLoginMode ? <LogIn className="w-4 h-4" /> : <UserPlus className="w-4 h-4" />}
          >
            {isLoginMode ? 'Connexion' : 'Créer mon compte'}
          </Button>
        </form>

        {/* Section Comptes de test préconfigurés */}
        <div className="mt-6 pt-5 border-t border-slate-200">
          <p className="text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-2 flex items-center gap-1.5">
            <Shield className="w-3.5 h-3.5 text-[#1E3A8A]" />
            <span>Comptes de test préconfigurés (cliquez pour remplir) :</span>
          </p>

          <div className="space-y-1.5">
            <button
              type="button"
              onClick={() => handleQuickLogin('student@pfe.local', 'Password123!')}
              className="w-full text-left p-2 rounded-lg bg-slate-50 hover:bg-blue-50/60 border border-slate-200 transition-colors flex items-center justify-between text-xs"
            >
              <div>
                <strong className="text-slate-800">Étudiant :</strong> student@pfe.local
              </div>
              <span className="text-[10px] px-2 py-0.5 rounded bg-blue-100 text-blue-800 font-bold">
                STUDENT
              </span>
            </button>

            <button
              type="button"
              onClick={() => handleQuickLogin('supervisor@pfe.local', 'Password123!')}
              className="w-full text-left p-2 rounded-lg bg-slate-50 hover:bg-teal-50/60 border border-slate-200 transition-colors flex items-center justify-between text-xs"
            >
              <div>
                <strong className="text-slate-800">Encadrant :</strong> supervisor@pfe.local
              </div>
              <span className="text-[10px] px-2 py-0.5 rounded bg-teal-100 text-teal-800 font-bold">
                SUPERVISOR
              </span>
            </button>

            <button
              type="button"
              onClick={() => handleQuickLogin('admin@pfe.local', 'Password123!')}
              className="w-full text-left p-2 rounded-lg bg-slate-50 hover:bg-rose-50/60 border border-slate-200 transition-colors flex items-center justify-between text-xs"
            >
              <div>
                <strong className="text-slate-800">Admin :</strong> admin@pfe.local
              </div>
              <span className="text-[10px] px-2 py-0.5 rounded bg-rose-100 text-rose-800 font-bold">
                ADMIN
              </span>
            </button>
          </div>
          <p className="text-[10px] text-slate-400 mt-2 text-center">
            Mot de passe commun pour les 3 comptes : <code className="font-mono text-slate-600">Password123!</code>
          </p>
        </div>
      </Card>
    </div>
  )
}

export default AuthPage
