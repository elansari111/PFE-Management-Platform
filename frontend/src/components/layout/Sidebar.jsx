import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard,
  FolderGit2,
  CheckSquare,
  FileText,
  Users,
  Bot,
  Bell,
  Settings,
  ChevronLeft,
  ChevronRight,
  GraduationCap,
  Sparkles,
  X,
} from 'lucide-react'

export function Sidebar({
  isCollapsed = false,
  onToggleCollapse,
  isMobileOpen = false,
  onCloseMobile,
}) {
  const navigationItems = [
    {
      to: '/dashboard',
      label: 'Tableau de bord',
      icon: LayoutDashboard,
    },
    {
      to: '/projects',
      label: 'Projets',
      icon: FolderGit2,
      badge: 'CRUD',
    },
    {
      to: '/tasks',
      label: 'Tâches',
      icon: CheckSquare,
    },
    {
      to: '/deliverables',
      label: 'Livrables',
      icon: FileText,
    },
    {
      to: '/supervision',
      label: 'Encadrement',
      icon: Users,
    },
    {
      to: '/ai-assistant',
      label: 'AI Encadrant',
      icon: Bot,
      highlight: true,
      badge: 'IA',
    },
    {
      to: '/notifications',
      label: 'Notifications',
      icon: Bell,
      badgeCount: 3,
    },
    {
      to: '/settings',
      label: 'Paramètres',
      icon: Settings,
    },
  ]

  const navContent = (
    <div className="flex flex-col h-full bg-[#1E3A8A] text-white">
      {/* Brand Header */}
      <div className="h-16 flex items-center justify-between px-4 border-b border-blue-900/60 shrink-0">
        <NavLink
          to="/dashboard"
          onClick={onCloseMobile}
          className="flex items-center space-x-3 group overflow-hidden"
        >
          <div className="w-10 h-10 rounded-xl bg-white/10 group-hover:bg-white/15 border border-white/20 flex items-center justify-center shrink-0 transition-colors">
            <GraduationCap className="w-6 h-6 text-[#0DAD98]" />
          </div>

          {!isCollapsed && (
            <div className="flex flex-col leading-tight overflow-hidden transition-all duration-200">
              <span className="font-extrabold text-base tracking-tight text-white truncate">
                PFE Platform
              </span>
              <span className="text-[10px] text-blue-200 font-medium tracking-wide uppercase truncate">
                Gestion intelligente
              </span>
            </div>
          )}
        </NavLink>

        {/* Mobile Close Button */}
        <button
          type="button"
          onClick={onCloseMobile}
          aria-label="Fermer le menu"
          className="md:hidden p-1.5 rounded-lg text-blue-200 hover:text-white hover:bg-white/10 transition-colors"
        >
          <X className="w-5 h-5" />
        </button>
      </div>

      {/* Navigation Links */}
      <div className="flex-1 py-4 px-3 space-y-1.5 overflow-y-auto overflow-x-hidden">
        {navigationItems.map((item) => {
          const Icon = item.icon
          return (
            <NavLink
              key={item.to}
              to={item.to}
              onClick={onCloseMobile}
              title={isCollapsed ? item.label : undefined}
              className={({ isActive }) =>
                `group relative flex items-center rounded-xl transition-all duration-150 font-medium text-sm ${
                  isCollapsed ? 'justify-center p-2.5' : 'px-3 py-2.5 space-x-3'
                } ${
                  isActive
                    ? 'bg-white text-[#1E3A8A] shadow-md shadow-black/10 font-bold'
                    : 'text-blue-100 hover:bg-white/10 hover:text-white'
                }`
              }
            >
              {({ isActive }) => (
                <>
                  <Icon
                    className={`w-5 h-5 shrink-0 transition-transform duration-150 group-hover:scale-105 ${
                      isActive ? 'text-[#1E3A8A]' : item.highlight ? 'text-[#0DAD98]' : 'text-blue-200'
                    }`}
                  />

                  {!isCollapsed && (
                    <span className="flex-1 truncate tracking-wide">{item.label}</span>
                  )}

                  {!isCollapsed && item.badge && (
                    <span
                      className={`text-[10px] px-1.5 py-0.5 rounded-full font-bold uppercase ${
                        item.highlight
                          ? 'bg-[#0DAD98] text-white'
                          : isActive
                          ? 'bg-blue-100 text-[#1E3A8A]'
                          : 'bg-white/15 text-blue-100'
                      }`}
                    >
                      {item.badge}
                    </span>
                  )}

                  {!isCollapsed && item.badgeCount && (
                    <span
                      className={`text-[11px] px-2 py-0.5 rounded-full font-bold ${
                        isActive ? 'bg-[#1E3A8A] text-white' : 'bg-[#0DAD98] text-white'
                      }`}
                    >
                      {item.badgeCount}
                    </span>
                  )}

                  {/* Active Indicator Line */}
                  {isActive && (
                    <div
                      className="absolute left-0 top-1.5 bottom-1.5 w-1 bg-[#0DAD98] rounded-r"
                      aria-hidden="true"
                    />
                  )}
                </>
              )}
            </NavLink>
          )
        })}
      </div>

      {/* AI Assistant Banner (expanded view only) */}
      {!isCollapsed && (
        <div className="p-3 mx-3 mb-3 rounded-xl bg-gradient-to-br from-blue-900/80 to-slate-900/80 border border-white/10 shadow-inner">
          <div className="flex items-center space-x-2 text-xs font-bold text-[#0DAD98]">
            <Sparkles className="w-3.5 h-3.5" />
            <span>AI Encadrant</span>
          </div>
          <p className="text-[11px] text-blue-200/90 mt-1 leading-snug">
            Assistance méthodologique et recommandations pour votre PFE.
          </p>
          <NavLink
            to="/ai-assistant"
            onClick={onCloseMobile}
            className="mt-2.5 inline-flex items-center text-[11px] font-semibold text-white bg-[#0DAD98] hover:bg-[#0b8f7d] px-2.5 py-1 rounded-md transition-colors"
          >
            Explorer l&apos;IA
          </NavLink>
        </div>
      )}

      {/* Footer Collapse Button (Desktop only) */}
      <div className="hidden md:flex p-3 border-t border-blue-900/60 justify-end shrink-0">
        <button
          type="button"
          onClick={onToggleCollapse}
          aria-label={isCollapsed ? 'Développer la barre latérale' : 'Réduire la barre latérale'}
          className="p-2 rounded-lg text-blue-200 hover:text-white hover:bg-white/10 transition-colors w-full flex items-center justify-center space-x-2"
        >
          {isCollapsed ? (
            <ChevronRight className="w-5 h-5" />
          ) : (
            <>
              <ChevronLeft className="w-4 h-4" />
              <span className="text-xs font-medium">Réduire</span>
            </>
          )}
        </button>
      </div>
    </div>
  )

  return (
    <>
      {/* Desktop Sidebar */}
      <aside
        className={`hidden md:flex flex-col shrink-0 border-r border-[#E2E8F0] h-screen sticky top-0 transition-all duration-200 z-30 ${
          isCollapsed ? 'w-20' : 'w-64'
        }`}
      >
        {navContent}
      </aside>

      {/* Mobile Drawer Backdrop */}
      {isMobileOpen && (
        <div
          className="md:hidden fixed inset-0 z-40 bg-slate-900/60 backdrop-blur-xs transition-opacity duration-200"
          onClick={onCloseMobile}
          aria-hidden="true"
        />
      )}

      {/* Mobile Drawer Menu */}
      <div
        className={`md:hidden fixed inset-y-0 left-0 z-50 w-72 max-w-[85vw] transform transition-transform duration-200 ease-in-out shadow-2xl ${
          isMobileOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        {navContent}
      </div>
    </>
  )
}

export default Sidebar

