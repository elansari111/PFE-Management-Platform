import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import { Navbar } from './Navbar'
import { ToastProvider } from '../ui/Toast'

export function MainLayout() {
  const [isCollapsed, setIsCollapsed] = useState(false)
  const [isMobileOpen, setIsMobileOpen] = useState(false)

  return (
    <ToastProvider>
      <div className="min-h-screen bg-[#F8FAFC] text-[#0F172A] flex flex-row">
        {/* Sidebar Component */}
        <Sidebar
          isCollapsed={isCollapsed}
          onToggleCollapse={() => setIsCollapsed(!isCollapsed)}
          isMobileOpen={isMobileOpen}
          onCloseMobile={() => setIsMobileOpen(false)}
        />

        {/* Content Area */}
        <div className="flex-1 flex flex-col min-w-0 min-h-screen">
          {/* Top Navbar */}
          <Navbar onOpenMobile={() => setIsMobileOpen(true)} />

          {/* Main Dynamic View with React Router Outlet */}
          <main className="flex-1 p-4 sm:p-6 lg:p-8 max-w-7xl w-full mx-auto">
            <Outlet />
          </main>

          {/* Academic Platform Footer */}
          <footer className="border-t border-[#E2E8F0] bg-white py-4 px-6 text-center text-xs text-slate-500">
            <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-2">
              <span>
                PFE Management Platform &copy; 2026 — Plateforme Intelligente avec AI Encadrant
              </span>
              <div className="flex items-center space-x-3 text-[11px]">
                <span className="font-mono text-[#1E3A8A] font-semibold">Sprint 2</span>
                <span>•</span>
                <span className="text-emerald-600 font-medium">Spring Boot &amp; React</span>
                <span>•</span>
                <span>v1.0.0</span>
              </div>
            </div>
          </footer>
        </div>
      </div>
    </ToastProvider>
  )
}

export default MainLayout
