import { AlertCircle, RefreshCw } from 'lucide-react'
import { Button } from './Button'

export function ErrorState({
  title = 'Une erreur est survenue',
  message = 'Impossible de charger les données. Veuillez vérifier votre connexion ou réessayer plus tard.',
  onRetry,
  retryLabel = 'Réessayer',
  className = '',
}) {
  return (
    <div
      className={`bg-white border border-rose-200 rounded-xl p-8 sm:p-10 text-center flex flex-col items-center justify-center max-w-lg mx-auto shadow-xs ${className}`}
    >
      <div className="w-14 h-14 rounded-2xl bg-rose-50 border border-rose-100 flex items-center justify-center text-rose-600 mb-4 shadow-2xs">
        <AlertCircle className="w-7 h-7" />
      </div>

      <h3 className="text-base font-bold text-rose-900">{title}</h3>
      <p className="text-xs sm:text-sm text-rose-700/80 mt-1.5 max-w-sm leading-relaxed">
        {message}
      </p>

      {onRetry && (
        <div className="mt-6">
          <Button
            size="sm"
            variant="outline"
            onClick={onRetry}
            leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
          >
            {retryLabel}
          </Button>
        </div>
      )}
    </div>
  )
}

export default ErrorState

