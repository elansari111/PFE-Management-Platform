import { useState, useCallback } from 'react'
import { CheckCircle2, AlertCircle, Info, AlertTriangle, X } from 'lucide-react'
import { ToastContext } from './useToast'

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  const addToast = useCallback(
    ({ title, message, type = 'info', duration = 4000 }) => {
      const id = Date.now().toString() + Math.random().toString(36).substring(2, 5)
      const newToast = { id, title, message, type, duration }

      setToasts((prev) => [...prev, newToast])

      if (duration > 0) {
        setTimeout(() => {
          removeToast(id)
        }, duration)
      }
      return id
    },
    [removeToast]
  )

  const toast = {
    show: addToast,
    success: (title, message) => addToast({ title, message, type: 'success' }),
    error: (title, message) => addToast({ title, message, type: 'error' }),
    info: (title, message) => addToast({ title, message, type: 'info' }),
    warning: (title, message) => addToast({ title, message, type: 'warning' }),
    remove: removeToast,
  }

  return (
    <ToastContext.Provider value={toast}>
      {children}
      <ToastContainer toasts={toasts} onClose={removeToast} />
    </ToastContext.Provider>
  )
}

export function ToastContainer({ toasts, onClose }) {
  if (toasts.length === 0) return null

  return (
    <div
      aria-live="polite"
      className="fixed bottom-4 right-4 z-50 flex flex-col space-y-2.5 max-w-sm w-full pointer-events-none"
    >
      {toasts.map((toast) => (
        <ToastItem key={toast.id} toast={toast} onClose={() => onClose(toast.id)} />
      ))}
    </div>
  )
}

function ToastItem({ toast, onClose }) {
  const typeStyles = {
    success: {
      border: 'border-emerald-200',
      bg: 'bg-white',
      icon: <CheckCircle2 className="w-5 h-5 text-emerald-600" />,
      accent: 'bg-emerald-500',
    },
    error: {
      border: 'border-rose-200',
      bg: 'bg-white',
      icon: <AlertCircle className="w-5 h-5 text-rose-600" />,
      accent: 'bg-rose-500',
    },
    warning: {
      border: 'border-amber-200',
      bg: 'bg-white',
      icon: <AlertTriangle className="w-5 h-5 text-amber-600" />,
      accent: 'bg-amber-500',
    },
    info: {
      border: 'border-blue-200',
      bg: 'bg-white',
      icon: <Info className="w-5 h-5 text-[#1E3A8A]" />,
      accent: 'bg-[#1E3A8A]',
    },
  }

  const currentStyle = typeStyles[toast.type] || typeStyles.info

  return (
    <div
      role="alert"
      className={`pointer-events-auto relative flex items-start p-4 rounded-xl border ${currentStyle.border} ${currentStyle.bg} shadow-lg shadow-slate-900/5 transition-all animate-in slide-in-from-bottom-3 duration-200`}
    >
      <div className="shrink-0 mr-3 mt-0.5">{currentStyle.icon}</div>
      <div className="flex-1 pr-2">
        {toast.title && <h5 className="text-xs sm:text-sm font-bold text-slate-900">{toast.title}</h5>}
        {toast.message && (
          <p className="text-xs text-slate-600 mt-0.5 leading-relaxed">{toast.message}</p>
        )}
      </div>
      <button
        type="button"
        onClick={onClose}
        aria-label="Fermer la notification"
        className="shrink-0 -mr-1 -mt-1 p-1 text-slate-400 hover:text-slate-600 rounded-md transition-colors"
      >
        <X className="w-4 h-4" />
      </button>
    </div>
  )
}

export default ToastProvider
