import { AlertTriangle, Info } from 'lucide-react'
import { Modal } from './Modal'
import { Button } from './Button'

export function ConfirmDialog({
  isOpen,
  onClose,
  onConfirm,
  title = 'Confirmer l’action',
  message = 'Êtes-vous sûr de vouloir continuer ? Cette opération ne pourra pas être annulée.',
  confirmText = 'Confirmer',
  cancelText = 'Annuler',
  variant = 'danger',
  isLoading = false,
}) {
  const isDanger = variant === 'danger'

  return (
    <Modal isOpen={isOpen} onClose={onClose} size="sm">
      <div className="flex items-start space-x-4">
        <div
          className={`shrink-0 w-11 h-11 rounded-full flex items-center justify-center ${
            isDanger ? 'bg-rose-100 text-rose-600' : 'bg-blue-100 text-[#1E3A8A]'
          }`}
        >
          {isDanger ? <AlertTriangle className="w-5 h-5" /> : <Info className="w-5 h-5" />}
        </div>

        <div className="space-y-2">
          <h4 className="text-base font-bold text-slate-900">{title}</h4>
          <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">{message}</p>
        </div>
      </div>

      <div className="mt-6 flex items-center justify-end space-x-3 pt-4 border-t border-[#E2E8F0]">
        <Button variant="outline" size="sm" onClick={onClose} disabled={isLoading}>
          {cancelText}
        </Button>
        <Button
          variant={isDanger ? 'danger' : 'primary'}
          size="sm"
          onClick={onConfirm}
          isLoading={isLoading}
        >
          {confirmText}
        </Button>
      </div>
    </Modal>
  )
}

export default ConfirmDialog

