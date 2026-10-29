import { FolderSearch } from 'lucide-react'
import { Button } from './Button'

export function EmptyState({
  title = 'Aucun élément trouvé',
  description = 'Il n’y a aucune donnée à afficher pour le moment.',
  icon: Icon = FolderSearch,
  actionLabel,
  onAction,
  actionButton,
  className = '',
}) {
  return (
    <div
      className={`bg-white border border-[#E2E8F0] border-dashed rounded-xl p-8 sm:p-12 text-center flex flex-col items-center justify-center max-w-lg mx-auto ${className}`}
    >
      <div className="w-14 h-14 rounded-2xl bg-slate-50 border border-slate-100 flex items-center justify-center text-slate-400 mb-4 shadow-2xs">
        <Icon className="w-7 h-7" />
      </div>

      <h3 className="text-base font-bold text-[#1E3A8A]">{title}</h3>
      <p className="text-xs sm:text-sm text-slate-500 mt-1.5 max-w-sm leading-relaxed">
        {description}
      </p>

      {actionButton ? (
        <div className="mt-6">{actionButton}</div>
      ) : (
        actionLabel &&
        onAction && (
          <div className="mt-6">
            <Button size="sm" onClick={onAction}>
              {actionLabel}
            </Button>
          </div>
        )
      )}
    </div>
  )
}

export default EmptyState

