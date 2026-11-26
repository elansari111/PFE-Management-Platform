import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Modal, Button, Input, Select } from '../../../components/ui'
import { userSchema } from '../userSchema'

const DEFAULT_VALUES = {
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  role: 'STUDENT',
}

const ROLE_OPTIONS = [
  { value: 'STUDENT', label: 'STUDENT (Étudiant)' },
  { value: 'SUPERVISOR', label: 'SUPERVISOR (Encadrant)' },
  { value: 'ADMIN', label: 'ADMIN (Administrateur)' },
]

export function UserFormModal({ isOpen, onClose, onSubmit, isLoading = false, apiErrors = null }) {
  const {
    register,
    handleSubmit,
    reset,
    setError,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(userSchema),
    defaultValues: DEFAULT_VALUES,
  })

  // Réinitialise le formulaire à chaque ouverture
  useEffect(() => {
    if (isOpen) {
      reset(DEFAULT_VALUES)
    }
  }, [isOpen, reset])

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
      title="Nouvel utilisateur"
      description="Créez un compte et attribuez-lui un rôle sur la plateforme."
      size="md"
    >
      <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4" noValidate>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <Input
            label="Prénom"
            required
            placeholder="Ex : Nadia"
            error={errors.firstName?.message}
            {...register('firstName')}
          />
          <Input
            label="Nom"
            required
            placeholder="Ex : Benali"
            error={errors.lastName?.message}
            {...register('lastName')}
          />
        </div>

        <Input
          label="Email"
          type="email"
          required
          placeholder="nom.prenom@universite.ma"
          error={errors.email?.message}
          {...register('email')}
        />

        <Input
          label="Mot de passe"
          type="password"
          required
          placeholder="••••••••"
          error={errors.password?.message}
          helperText="8 caractères minimum, dont au moins une majuscule et un chiffre."
          {...register('password')}
        />

        <Select
          label="Rôle"
          required
          error={errors.role?.message}
          options={ROLE_OPTIONS}
          {...register('role')}
        />

        <div className="pt-4 border-t border-[#E2E8F0] flex items-center justify-end space-x-3">
          <Button variant="outline" size="sm" type="button" onClick={onClose} disabled={isLoading}>
            Annuler
          </Button>
          <Button variant="primary" size="sm" type="submit" isLoading={isLoading}>
            Créer l&apos;utilisateur
          </Button>
        </div>
      </form>
    </Modal>
  )
}

export default UserFormModal
