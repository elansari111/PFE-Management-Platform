import { useState, useRef, useEffect } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import {
  Menu,
  Search,
  Bell,
  ChevronRight,
  User,
  LogOut,
  Settings,
  Shield,
  Check,
  LogIn,
} from 'lucide-react'
import { Badge } from '../ui/Badge'
import { useToast } from '../ui'
import { useAuth } from '../../context'

export function Navbar({ onOpenMobile }) {
  const location = useLocation()
  const navigate = useNavigate()
  const toast = useToast()
  const { user, isAuthenticated, logout } = useAuth()

  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false)
  const [isNotifMenuOpen, setIsNotifMenuOpen] = useState(false)

  const profileMenuRef = useRef(null)
  const notifMenuRef = useRef(null)

  // Click outside listener for dropdown menus
  useEffect(() => {
    function handleClickOutside(event) {
      if (profileMenuRef.current && !profileMenuRef.current.contains(event.target)) {
        setIsProfileMenuOpen(false)
      }
      if (notifMenuRef.current && !notifMenuRef.current.contains(event.target)) {
        setIsNotifMenuOpen(false)
      }
    }
    document.addEventListener('mousedown', handleClickOutside)
    return () => document.removeEventListener('mousedown', handleClickOutside)
  }, [])

  // Dynamic breadcrumbs based on pathname
  const routeTitles = {
    '/': 'Tableau de bord',
    '/dashboard': 'Tableau de bord',
    '/projects': 'Projets PFE',
    '/tasks': 'Tâches & Kanban',
    '/deliverables': 'Livrables',
    '/supervision': 'Espace Encadrement',
    '/ai-assistant': 'AI Encadrant',
    '/notifications': 'Centre de Notifications',
    '/profile': 'Profil Utilisateur',
    '/settings': 'Paramètres',
    '/login': 'Connexion & Inscription',
    '/auth': 'Connexion & Inscription',
  }

  const currentTitle = routeTitles[location.pathname] || 'Navigation'

  const handleLogout = () => {
    setIsProfileMenuOpen(false)
    logout()
    toast.success('Déconnexion', 'Vous avez été déconnecté avec succès.')
    navigate('/login')
  }

  // Initiales dynamiques
  const getInitials = (firstName, lastName) => {
    const f = firstName ? firstName[0] : 'U'
    const l = lastName ? lastName[0] : ''
    return `${f}${l}`.toUpperCase()
  }

  const role = user?.role || 'INVITÉ'

  return (
    <header className="sticky top-0 z-30 flex items-center justify-between h-16 px-4 sm:px-6 lg:px-8 bg-white/95 backdrop-blur-md border-b border-[#E2E8F0]">
      {/* Left: Mobile trigger & Breadcrumbs */}
      <div className="flex items-center space-x-3">
        <button
          type="button"
          onClick={onOpenMobile}
          aria-label="Ouvrir le menu latéral"
          className="lg:hidden p-2 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition-colors focus:outline-none"
        >
          <Menu className="w-5 h-5" />
        </button>

        <nav aria-label="Fil d'ariane" className="flex items-center space-x-2 text-xs">
          <Link
            to="/dashboard"
            className="text-slate-400 hover:text-slate-700 font-medium transition-colors"
          >
            Accueil
          </Link>
          <ChevronRight className="w-3.5 h-3.5 text-slate-400" />
          <span className="font-bold text-[#1E3A8A]">{currentTitle}</span>
        </nav>
      </div>

      {/* Right: Actions, Notifications & Profile */}
      <div className="flex items-center space-x-2 sm:space-x-3">
        {/* Global Search Bar */}
        <div className="hidden md:flex items-center relative">
          <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
            <Search className="w-4 h-4" />
          </div>
          <input
            type="search"
            placeholder="Rechercher projets, livrables..."
            aria-label="Recherche globale"
            className="w-56 lg:w-64 pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-[#E2E8F0] rounded-lg focus:outline-none focus:border-[#1E3A8A] focus:bg-white focus:ring-1 focus:ring-[#1E3A8A] transition-colors"
          />
        </div>

        {/* Notifications Dropdown */}
        <div className="relative" ref={notifMenuRef}>
          <button
            type="button"
            onClick={() => setIsNotifMenuOpen(!isNotifMenuOpen)}
            aria-label="Centre de notifications"
            className="relative p-2 text-slate-500 hover:text-slate-800 hover:bg-slate-100 rounded-lg transition-colors focus:outline-none"
          >
            <Bell className="w-5 h-5" />
            <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-rose-500 ring-2 ring-white" />
          </button>

          {isNotifMenuOpen && (
            <div className="absolute right-0 mt-2 w-80 bg-white rounded-xl shadow-xl border border-[#E2E8F0] py-2 z-50 animate-in fade-in zoom-in-95 duration-150">
              <div className="px-4 py-2 border-b border-[#E2E8F0] flex items-center justify-between">
                <span className="text-xs font-bold text-slate-900">Notifications</span>
                <span className="text-[10px] font-semibold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full">
                  Système actif
                </span>
              </div>
              <div className="divide-y divide-slate-100 max-h-64 overflow-y-auto">
                <div className="p-3 text-xs text-slate-600 hover:bg-slate-50">
                  <p className="font-semibold text-slate-800">Projets synchronisés</p>
                  <p className="text-[11px] text-slate-500 mt-0.5">
                    Connecté à l&apos;API Spring Boot (MySQL).
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Profile Dropdown / Connexion */}
        {isAuthenticated && user ? (
          <div className="relative" ref={profileMenuRef}>
            <button
              type="button"
              onClick={() => setIsProfileMenuOpen(!isProfileMenuOpen)}
              aria-label="Menu du profil utilisateur"
              aria-expanded={isProfileMenuOpen}
              className="flex items-center space-x-2.5 p-1 rounded-lg hover:bg-slate-100 transition-colors"
            >
              <div className="w-9 h-9 rounded-lg bg-[#1E3A8A] text-white font-bold text-xs flex items-center justify-center shadow-xs border border-blue-900/40">
                {getInitials(user.firstName, user.lastName)}
              </div>
              <div className="hidden sm:flex flex-col text-left leading-tight">
                <span className="text-xs font-bold text-slate-900 truncate max-w-[120px]">
                  {user.firstName} {user.lastName}
                </span>
                <span className="text-[10px] font-medium text-slate-500 truncate uppercase">
                  {role}
                </span>
              </div>
            </button>

            {isProfileMenuOpen && (
              <div className="absolute right-0 mt-2 w-64 bg-white rounded-xl shadow-xl border border-[#E2E8F0] py-2 z-50 animate-in fade-in zoom-in-95 duration-150">
                {/* User Details */}
                <div className="px-4 py-3 border-b border-[#E2E8F0]">
                  <p className="text-xs font-bold text-slate-900">
                    {user.firstName} {user.lastName}
                  </p>
                  <p className="text-[11px] text-slate-500 truncate">{user.email}</p>
                  <div className="mt-2 flex items-center gap-1.5">
                    <Badge
                      variant={
                        role === 'ADMIN'
                          ? 'danger'
                          : role === 'SUPERVISOR'
                          ? 'accent'
                          : 'primary'
                      }
                      size="sm"
                    >
                      {role}
                    </Badge>
                    <span className="text-[10px] text-slate-400">Compte BDD</span>
                  </div>
                </div>

                {/* Profile Links */}
                <div className="py-1">
                  <Link
                    to="/profile"
                    onClick={() => setIsProfileMenuOpen(false)}
                    className="flex items-center space-x-2.5 px-4 py-2 text-xs text-slate-700 hover:bg-slate-50 transition-colors"
                  >
                    <User className="w-4 h-4 text-slate-400" />
                    <span>Mon Profil</span>
                  </Link>

                  <Link
                    to="/settings"
                    onClick={() => setIsProfileMenuOpen(false)}
                    className="flex items-center space-x-2.5 px-4 py-2 text-xs text-slate-700 hover:bg-slate-50 transition-colors"
                  >
                    <Settings className="w-4 h-4 text-slate-400" />
                    <span>Paramètres</span>
                  </Link>

                  <button
                    type="button"
                    onClick={handleLogout}
                    className="w-full flex items-center space-x-2.5 px-4 py-2 text-xs text-rose-600 hover:bg-rose-50 transition-colors text-left"
                  >
                    <LogOut className="w-4 h-4 text-rose-500" />
                    <span>Se Déconnecter</span>
                  </button>
                </div>
              </div>
            )}
          </div>
        ) : (
          <Link
            to="/login"
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded-lg bg-[#1E3A8A] text-white text-xs font-semibold hover:bg-blue-900 transition-colors shadow-2xs"
          >
            <LogIn className="w-3.5 h-3.5" />
            <span>Se Connecter</span>
          </Link>
        )}
      </div>
    </header>
  )
}

export default Navbar
