import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Modal, Button, Input, Select } from '../../../components/ui'
import { projectSchema } from '../projectSchema'

export function ProjectFormModal({
  isOpen,
  onClose,
  onSubmit,
  initialData = null,
  isLoading = false,
  apiErrors = null,
}) {
  const isEditMode = Boolean(initialData?.id)

  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(projectSchema),
    defaultValues: {
      title: '',
      description: '',
      status: 'PROPOSED',
      startDate: '',
      endDate: '',
    },
  })

  // Synchronise les valeurs initiales lors de l'ouverture
  useEffect(() => {
    if (initialData) {
      reset({
        title: initialData.title || '',
        description: initialData.description || '',
        status: initialData.status || 'PROPOSED',
        startDate: initialData.startDate || '',
        endDate: initialData.endDate || '',
      })
    } else {
      reset({
        title: '',
        description: '',
        status: 'PROPOSED',
        startDate: '',
        endDate: '',
      })
    }
  }, [initialData, reset, isOpen])

  // Injecte les erreurs de validation renvoyées par le backend (Map<String, String>)
  useEffect(() => {
    if (apiErrors && typeof apiErrors === 'object') {
      Object.entries(apiErrors).forEach(([field, msg]) => {
        setError(field, { type: 'server', message: msg })
      })
    }
  }, [apiErrors, setError])

  const handleFormSubmit = async (data) => {
    await onSubmit(data)
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={isEditMode ? 'Modifier le Projet PFE' : 'Créer un Nouveau Projet PFE'}
      description={
        isEditMode
          ? 'Mettez à jour les informations du projet conformément aux règles académiques.'
          : 'Remplissez le formulaire de création de sujet de PFE.'
      }
      size="md"
    >
      <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4" noValidate>
        {/* Titre */}
        <Input
          label="Titre du Projet"
          required
          placeholder="Ex : Plateforme intelligente de gestion des PFE"
          error={errors.title?.message}
          helperText="Entre 3 et 200 caractères (doit être unique)."
          {...register('title')}
        />

        {/* Description */}
        <div className="space-y-1.5">
          <label htmlFor="description" className="block text-xs font-semibold text-slate-700">
            Description détaillée
          </label>
          <textarea
            id="description"
            rows={3}
            placeholder="Objectifs, contexte académique et technologies envisagées..."
            className={`block w-full rounded-lg border bg-white p-2.5 text-xs sm:text-sm text-slate-900 placeholder:text-slate-400 focus:outline-none transition-colors ${
              errors.description
                ? 'border-rose-400 focus:border-rose-500 focus:ring-1 focus:ring-rose-400'
                : 'border-[#E2E8F0] focus:border-[#1E3A8A] focus:ring-1 focus:ring-[#1E3A8A]'
            }`}
            {...register('description')}
          />
          {errors.description && (
            <p className="text-xs text-rose-600 font-medium">{errors.description.message}</p>
          )}
          <p className="text-[11px] text-slate-400">Maximum 2000 caractères.</p>
        </div>

        {/* Statut */}
        <Select
          label="Statut du Projet"
          required
          error={errors.status?.message}
          options={[
            { value: 'PROPOSED', label: 'PROPOSED (Proposition de sujet)' },
            { value: 'PENDING', label: 'PENDING (En attente d’approbation)' },
            { value: 'APPROVED', label: 'APPROVED (Validé par l’encadrement)' },
            { value: 'IN_PROGRESS', label: 'IN_PROGRESS (En cours de développement)' },
            { value: 'COMPLETED', label: 'COMPLETED (Terminé / Soutenu)' },
            { value: 'ARCHIVED', label: 'ARCHIVED (Archivé)' },
          ]}
          {...register('status')}
        />

        {/* Dates début et fin */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Date de début prévue"
            type="date"
            error={errors.startDate?.message}
            {...register('startDate')}
          />
          <Input
            label="Date de fin prévue"
            type="date"
            error={errors.endDate?.message}
            {...register('endDate')}
          />
        </div>

        {/* Footer Actions */}
        <div className="pt-4 border-t border-[#E2E8F0] flex items-center justify-end space-x-3">
          <Button variant="outline" size="sm" type="button" onClick={onClose} disabled={isLoading}>
            Annuler
          </Button>
          <Button variant="primary" size="sm" type="submit" isLoading={isLoading}>
            {isEditMode ? 'Enregistrer les modifications' : 'Créer le Projet'}
          </Button>
        </div>
      </form>
    </Modal>
  )
}

export default ProjectFormModal

