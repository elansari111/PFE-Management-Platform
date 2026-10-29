import { useState, useRef, useEffect } from 'react'
import { Link, useLocation } from 'react-router-dom'
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
  Sparkles,
} from 'lucide-react'
import { Badge } from '../ui/Badge'
import { useToast } from '../ui'

export function Navbar({ onOpenMobile }) {
  const location = useLocation()
  const toast = useToast()

  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false)
  const [isNotifMenuOpen, setIsNotifMenuOpen] = useState(false)
  const [activeRole, setActiveRole] = useState('STUDENT') // STUDENT | SUPERVISOR | ADMIN

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
  }

  const currentTitle = routeTitles[location.pathname] || 'Navigation'

  const handleLogout = () => {
    setIsProfileMenuOpen(false)
    toast.info(
      'Déconnexion Interface',
      'Action enregistrée. L’authentification complète JWT sera raccordée en Phase 4.'
    )
  }

  const switchRole = (role) => {
    setActiveRole(role)
    setIsProfileMenuOpen(false)
    toast.success('Rôle Actif', `Mode d'affichage basculé vers le rôle ${role}`)
  }

  return (
    <header className="sticky top-0 z-20 h-16 bg-white border-b border-[#E2E8F0] shadow-2xs">
      <div className="h-full px-4 sm:px-6 lg:px-8 flex items-center justify-between gap-4">
        {/* Left Section: Mobile Menu Toggle & Breadcrumbs */}
        <div className="flex items-center space-x-3 sm:space-x-4 min-w-0">
          <button
            type="button"
            onClick={onOpenMobile}
            aria-label="Ouvrir le menu de navigation"
            className="md:hidden p-2 rounded-lg text-slate-600 hover:text-slate-900 hover:bg-slate-100 transition-colors shrink-0"
          >
            <Menu className="w-5 h-5" />
          </button>

          {/* Breadcrumbs */}
          <nav aria-label="Fil d’ariane" className="flex items-center space-x-1.5 text-xs sm:text-sm font-medium truncate">
            <Link
              to="/dashboard"
              className="text-slate-500 hover:text-[#1E3A8A] transition-colors truncate hidden sm:inline"
            >
              Accueil
            </Link>
            <ChevronRight className="w-4 h-4 text-slate-400 shrink-0 hidden sm:inline" />
            <span className="text-[#1E3A8A] font-bold truncate">{currentTitle}</span>
          </nav>
        </div>

        {/* Center Section: Visual Search Bar */}
        <div className="hidden lg:flex flex-1 max-w-md mx-4">
          <div className="relative w-full">
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
              <Search className="w-4 h-4" />
            </div>
            <input
              type="text"
              placeholder="Rechercher un projet, une tâche, un livrable..."
              aria-label="Recherche rapide"
              className="w-full pl-9 pr-16 py-1.5 text-xs bg-slate-50 border border-[#E2E8F0] rounded-lg text-slate-900 placeholder:text-slate-400 focus:outline-none focus:bg-white focus:border-[#1E3A8A] focus:ring-1 focus:ring-[#1E3A8A] transition-all"
            />
            <div className="absolute inset-y-0 right-0 flex items-center pr-2">
              <kbd className="hidden sm:inline-block px-1.5 py-0.5 text-[10px] font-semibold text-slate-400 bg-white border border-slate-200 rounded shadow-2xs">
                Ctrl K
              </kbd>
            </div>
          </div>
        </div>

        {/* Right Section: Actions, Notifications, User Menu */}
        <div className="flex items-center space-x-2 sm:space-x-3 shrink-0">
          {/* Notifications Dropdown */}
          <div className="relative" ref={notifMenuRef}>
            <button
              type="button"
              onClick={() => setIsNotifMenuOpen(!isNotifMenuOpen)}
              aria-label="Afficher les notifications"
              aria-expanded={isNotifMenuOpen}
              className="relative p-2 rounded-lg text-slate-500 hover:text-slate-800 hover:bg-slate-100 transition-colors"
            >
              <Bell className="w-5 h-5" />
              <span className="absolute top-1.5 right-1.5 w-2 h-2 rounded-full bg-[#0DAD98] ring-2 ring-white" />
            </button>

            {isNotifMenuOpen && (
              <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-white rounded-xl shadow-xl border border-[#E2E8F0] py-2 z-50 animate-in fade-in zoom-in-95 duration-150">
                <div className="px-4 py-2 border-b border-[#E2E8F0] flex items-center justify-between">
                  <span className="text-xs font-bold text-[#1E3A8A] uppercase tracking-wide">
                    Notifications
                  </span>
                  <Badge variant="accent" size="sm">
                    3 nouvelles
                  </Badge>
                </div>
                <div className="divide-y divide-slate-100 max-h-72 overflow-y-auto">
                  <div className="p-3 hover:bg-slate-50 transition-colors cursor-pointer">
                    <p className="text-xs font-semibold text-slate-800">
                      Validation de cadrage par l’encadrant
                    </p>
                    <p className="text-[11px] text-slate-500 mt-0.5">
                      Projet &quot;Plateforme PFE&quot; — Statut mis à jour vers APPROVED.
                    </p>
                    <span className="text-[10px] text-slate-400 mt-1 block">Il y a 10 minutes</span>
                  </div>
                  <div className="p-3 hover:bg-slate-50 transition-colors cursor-pointer">
                    <p className="text-xs font-semibold text-slate-800">
                      Rappel de soumission de livrable
                    </p>
                    <p className="text-[11px] text-slate-500 mt-0.5">
                      Livrable &quot;Cahier des charges v1.2&quot; attendu avant vendredi.
                    </p>
                    <span className="text-[10px] text-slate-400 mt-1 block">Il y a 2 heures</span>
                  </div>
                  <div className="p-3 hover:bg-slate-50 transition-colors cursor-pointer">
                    <p className="text-xs font-semibold text-slate-800 flex items-center gap-1">
                      <Sparkles className="w-3 h-3 text-[#0DAD98]" />
                      <span>Recommandation AI Encadrant</span>
                    </p>
                    <p className="text-[11px] text-slate-500 mt-0.5">
                      Nouvelle suggestion méthodologique disponible pour vos tests unitaires.
                    </p>
                    <span className="text-[10px] text-slate-400 mt-1 block">Hier</span>
                  </div>
                </div>
                <div className="px-4 py-2 border-t border-[#E2E8F0] bg-slate-50/70 text-center">
                  <Link
                    to="/notifications"
                    onClick={() => setIsNotifMenuOpen(false)}
                    className="text-xs font-semibold text-[#1E3A8A] hover:text-[#0DAD98] transition-colors"
                  >
                    Voir toutes les notifications &rarr;
                  </Link>
                </div>
              </div>
            )}
          </div>

          <div className="h-6 w-px bg-slate-200 hidden sm:block" />

          {/* User Profile Menu */}
          <div className="relative" ref={profileMenuRef}>
            <button
              type="button"
              onClick={() => setIsProfileMenuOpen(!isProfileMenuOpen)}
              aria-label="Menu du profil utilisateur"
              aria-expanded={isProfileMenuOpen}
              className="flex items-center space-x-2.5 p-1 rounded-lg hover:bg-slate-100 transition-colors"
            >
              <div className="w-9 h-9 rounded-lg bg-[#1E3A8A] text-white font-bold text-xs flex items-center justify-center shadow-xs border border-blue-900/40">
                MS
              </div>
              <div className="hidden sm:flex flex-col text-left leading-tight">
                <span className="text-xs font-bold text-slate-900 truncate max-w-[120px]">
                  Mohamed (Simo)
                </span>
                <span className="text-[10px] font-medium text-slate-500 truncate uppercase">
                  {activeRole}
                </span>
              </div>
            </button>

            {isProfileMenuOpen && (
              <div className="absolute right-0 mt-2 w-64 bg-white rounded-xl shadow-xl border border-[#E2E8F0] py-2 z-50 animate-in fade-in zoom-in-95 duration-150">
                {/* User Details */}
                <div className="px-4 py-3 border-b border-[#E2E8F0]">
                  <p className="text-xs font-bold text-slate-900">Mohamed (Simo)</p>
                  <p className="text-[11px] text-slate-500 truncate">simo@student.universite.ma</p>
                  <div className="mt-2 flex items-center gap-1.5">
                    <Badge variant={activeRole === 'ADMIN' ? 'danger' : activeRole === 'SUPERVISOR' ? 'accent' : 'primary'} size="sm">
                      {activeRole}
                    </Badge>
                    <span className="text-[10px] text-slate-400">Branche dev-simo</span>
                  </div>
                </div>

                {/* Role Simulation Switcher (Conformément aux 3 rôles backend Role.java) */}
                <div className="px-4 py-2 border-b border-[#E2E8F0] bg-slate-50/60">
                  <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wider flex items-center gap-1">
                    <Shield className="w-3 h-3 text-[#1E3A8A]" />
                    <span>Vue du Rôle (Simulation UI)</span>
                  </span>
                  <div className="mt-1.5 grid grid-cols-3 gap-1">
                    {['STUDENT', 'SUPERVISOR', 'ADMIN'].map((role) => (
                      <button
                        key={role}
                        type="button"
                        onClick={() => switchRole(role)}
                        className={`text-[10px] py-1 px-1.5 rounded font-semibold text-center transition-colors flex items-center justify-center gap-0.5 ${
                          activeRole === role
                            ? 'bg-[#1E3A8A] text-white shadow-2xs'
                            : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-100'
                        }`}
                      >
                        {activeRole === role && <Check className="w-2.5 h-2.5" />}
                        <span>{role === 'SUPERVISOR' ? 'SUP' : role}</span>
                      </button>
                    ))}
                  </div>
                </div>

                {/* Menu items */}
                <div className="py-1 text-xs">
                  <Link
                    to="/profile"
                    onClick={() => setIsProfileMenuOpen(false)}
                    className="flex items-center space-x-2.5 px-4 py-2 text-slate-700 hover:bg-slate-50 hover:text-[#1E3A8A] transition-colors"
                  >
                    <User className="w-4 h-4 text-slate-400" />
                    <span>Mon Profil</span>
                  </Link>

                  <Link
                    to="/settings"
                    onClick={() => setIsProfileMenuOpen(false)}
                    className="flex items-center space-x-2.5 px-4 py-2 text-slate-700 hover:bg-slate-50 hover:text-[#1E3A8A] transition-colors"
                  >
                    <Settings className="w-4 h-4 text-slate-400" />
                    <span>Paramètres du compte</span>
                  </Link>

                  <div className="my-1 border-t border-slate-100" />

                  <button
                    type="button"
                    onClick={handleLogout}
                    className="w-full flex items-center space-x-2.5 px-4 py-2 text-rose-600 hover:bg-rose-50 transition-colors text-left"
                  >
                    <LogOut className="w-4 h-4 text-rose-500" />
                    <span>Déconnexion</span>
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </header>
  )
}

export default Navbar

