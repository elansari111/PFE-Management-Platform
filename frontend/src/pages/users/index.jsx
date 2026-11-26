import { useState, useEffect, useCallback, useRef } from 'react'
import {
  UserCog,
  Plus,
  Search,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react'
import {
  Button,
  Badge,
  Select,
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell,
  ConfirmDialog,
  EmptyState,
  ErrorState,
  Skeleton,
  useToast,
} from '../../components/ui'
import { userService } from '../../features/users'
import UserFormModal from '../../features/users/components/UserFormModal'
import { useAuth } from '../../context/useAuth'
import { ROLES } from '../../utils/constants'

const ROLE_FILTERS = [
  { value: 'ALL', label: 'Tous les rôles' },
  { value: 'ADMIN', label: 'Administrateurs' },
  { value: 'SUPERVISOR', label: 'Encadrants' },
  { value: 'STUDENT', label: 'Étudiants' },
]

const ROLE_OPTIONS = [
  { value: 'STUDENT', label: 'STUDENT' },
  { value: 'SUPERVISOR', label: 'SUPERVISOR' },
  { value: 'ADMIN', label: 'ADMIN' },
]

const ROLE_VARIANT = {
  ADMIN: 'primary',
  SUPERVISOR: 'accent',
  STUDENT: 'default',
}

function fullName(user) {
  const name = [user.firstName, user.lastName].filter(Boolean).join(' ').trim()
  return name || user.email || `Utilisateur #${user.id}`
}

function formatDate(value) {
  if (!value) return '—'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '—'
  return date.toLocaleDateString('fr-FR')
}

function AccessDenied() {
  return (
    <div className="space-y-6 animate-in fade-in duration-200">
      <ErrorState
        title="Accès refusé"
        message="Cette section est réservée aux administrateurs. Vous ne disposez pas des droits nécessaires pour gérer les utilisateurs."
      />
    </div>
  )
}

export function UsersPage() {
  const auth = useAuth()
  const currentUser = auth?.user

  // Contrôle d'accès strictement côté ADMIN : aucune requête API pour les autres rôles.
  if (currentUser?.role !== ROLES.ADMIN) {
    return <AccessDenied />
  }

  return <UsersManagement currentUser={currentUser} />
}

function UsersManagement({ currentUser }) {
  const toast = useToast()

  const [users, setUsers] = useState([])
  const [pagination, setPagination] = useState({
    pageNumber: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 1,
    first: true,
    last: true,
  })
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)

  // Recherche (q) avec debounce de 400 ms + filtre par rôle
  const [searchInput, setSearchInput] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [roleFilter, setRoleFilter] = useState('ALL')

  // Modale de création
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [formApiErrors, setFormApiErrors] = useState(null)

  // Confirmations (changement de rôle / activation)
  const [roleTarget, setRoleTarget] = useState(null)
  const [activeTarget, setActiveTarget] = useState(null)
  const [isActionLoading, setIsActionLoading] = useState(false)

  // Évite les mises à jour d'état après le démontage du composant
  const isMountedRef = useRef(true)
  useEffect(() => {
    isMountedRef.current = true
    return () => {
      isMountedRef.current = false
    }
  }, [])

  // Debounce de la recherche : met à jour le terme interrogé 400 ms après la dernière frappe
  useEffect(() => {
    const timer = setTimeout(() => {
      setSearchTerm(searchInput.trim())
    }, 400)
    return () => clearTimeout(timer)
  }, [searchInput])

  const loadUsers = useCallback(
    async (pageToLoad = 0) => {
      setIsLoading(true)
      setError(null)
      try {
        const result = await userService.getUsers({
          role: roleFilter,
          q: searchTerm,
          page: pageToLoad,
          size: pagination.pageSize,
        })

        if (!isMountedRef.current) return

        setUsers(result.content || [])
        setPagination({
          pageNumber: result.pageNumber ?? pageToLoad,
          pageSize: result.pageSize ?? pagination.pageSize,
          totalElements: result.totalElements ?? (result.content || []).length,
          totalPages: result.totalPages ?? 1,
          first: result.first ?? pageToLoad === 0,
          last: result.last ?? true,
        })
      } catch (err) {
        if (!isMountedRef.current) return
        setError(err.message || 'Impossible de charger la liste des utilisateurs.')
      } finally {
        if (isMountedRef.current) {
          setIsLoading(false)
        }
      }
    },
    [roleFilter, searchTerm, pagination.pageSize]
  )

  useEffect(() => {
    loadUsers(0)
  }, [loadUsers])

  // Affichage homogène des erreurs API (400 / 403 / 409)
  const showApiError = (err, fallbackTitle) => {
    const status = err?.status
    if (status === 403) {
      toast.error('Action interdite (403)', err.message || "Vous n'avez pas les droits d'administration requis.")
    } else if (status === 409) {
      toast.error('Email déjà utilisé (409)', err.message || 'Un compte utilise déjà cette adresse email.')
    } else if (status === 400) {
      toast.error('Données invalides (400)', err.message || 'Le serveur a refusé la requête.')
    } else {
      toast.error(fallbackTitle, err.message || "L'opération a échoué.")
    }
  }

  const handleOpenCreate = () => {
    setFormApiErrors(null)
    setIsFormOpen(true)
  }

  const handleCreateUser = async (formData) => {
    setIsActionLoading(true)
    setFormApiErrors(null)
    try {
      const created = await userService.createUser(formData)
      if (!isMountedRef.current) return
      setIsFormOpen(false)
      toast.success('Utilisateur créé', `Le compte de ${fullName(created)} a été créé avec succès.`)
      loadUsers(0)
    } catch (err) {
      if (!isMountedRef.current) return
      if (err.validationErrors) {
        setFormApiErrors(err.validationErrors)
      } else if (err.status === 409) {
        setFormApiErrors({ email: err.message || 'Cette adresse email est déjà utilisée.' })
      }
      showApiError(err, 'Création impossible')
    } finally {
      if (isMountedRef.current) setIsActionLoading(false)
    }
  }

  const handleRoleChange = (user, newRole) => {
    if (newRole === user.role) return
    setRoleTarget({ user, newRole })
  }

  const handleConfirmRoleChange = async () => {
    if (!roleTarget) return
    const { user, newRole } = roleTarget
    setIsActionLoading(true)
    try {
      const updated = await userService.updateUserRole(user.id, newRole)
      if (!isMountedRef.current) return
      setUsers((prev) =>
        prev.map((u) => (u.id === user.id ? { ...u, ...updated, role: updated?.role || newRole } : u))
      )
      setRoleTarget(null)
      toast.success('Rôle mis à jour', `${fullName(user)} est désormais ${newRole}.`)
    } catch (err) {
      if (!isMountedRef.current) return
      showApiError(err, 'Changement de rôle impossible')
    } finally {
      if (isMountedRef.current) setIsActionLoading(false)
    }
  }

  const handleConfirmActiveChange = async () => {
    if (!activeTarget) return
    const { user, nextActive } = activeTarget
    setIsActionLoading(true)
    try {
      const updated = await userService.updateUserActive(user.id, nextActive)
      if (!isMountedRef.current) return
      setUsers((prev) =>
        prev.map((u) => (u.id === user.id ? { ...u, ...updated, active: updated?.active ?? nextActive } : u))
      )
      setActiveTarget(null)
      toast.success(
        nextActive ? 'Compte activé' : 'Compte désactivé',
        `${fullName(user)} peut désormais ${nextActive ? 'accéder' : "ne plus accéder"} à la plateforme.`
      )
    } catch (err) {
      if (!isMountedRef.current) return
      showApiError(err, nextActive ? 'Activation impossible' : 'Désactivation impossible')
    } finally {
      if (isMountedRef.current) setIsActionLoading(false)
    }
  }

  return (
    <div className="space-y-6 animate-in fade-in duration-200">
      {/* En-tête */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-2xl font-extrabold text-[#1E3A8A] tracking-tight">
              Gestion des Utilisateurs
            </h1>
            <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-blue-100 text-[#1E3A8A]">
              {pagination.totalElements} au total
            </span>
          </div>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Administration des comptes : rôles, activation et création d&apos;utilisateurs.
          </p>
        </div>

        <div className="flex items-center space-x-2.5">
          <Button
            variant="outline"
            size="sm"
            onClick={() => loadUsers(pagination.pageNumber)}
            title="Rafraîchir les données"
            leftIcon={<RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />}
          >
            Actualiser
          </Button>

          <Button
            variant="primary"
            size="sm"
            onClick={handleOpenCreate}
            leftIcon={<Plus className="w-4 h-4" />}
          >
            Nouvel utilisateur
          </Button>
        </div>
      </div>

      {/* Recherche + filtre par rôle */}
      <div className="bg-white border border-[#E2E8F0] rounded-xl p-4 shadow-xs">
        <div className="flex flex-col lg:flex-row items-center justify-between gap-3">
          <div className="relative w-full lg:w-80">
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
              <Search className="w-4 h-4" />
            </div>
            <input
              type="text"
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              placeholder="Rechercher par nom ou email..."
              aria-label="Recherche d'utilisateurs"
              className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-[#E2E8F0] rounded-lg focus:outline-none focus:bg-white focus:border-[#1E3A8A] focus:ring-1 focus:ring-[#1E3A8A] transition-colors"
            />
          </div>

          <div className="w-full lg:w-64">
            <Select
              aria-label="Filtrer par rôle"
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
              options={ROLE_FILTERS}
            />
          </div>
        </div>
      </div>

      {/* Contenu : chargement / erreur / vide / tableau */}
      {isLoading ? (
        <div className="bg-white border border-[#E2E8F0] rounded-xl p-4 space-y-3" data-testid="users-loading">
          <Skeleton className="h-10 w-full" />
          <Skeleton className="h-12 w-full" />
          <Skeleton className="h-12 w-full" />
          <Skeleton className="h-12 w-full" />
        </div>
      ) : error ? (
        <ErrorState
          title="Erreur lors du chargement des utilisateurs"
          message={error}
          onRetry={() => loadUsers(pagination.pageNumber)}
        />
      ) : users.length === 0 ? (
        <EmptyState
          icon={UserCog}
          title="Aucun utilisateur trouvé"
          description={
            searchInput || roleFilter !== 'ALL'
              ? 'Aucun utilisateur ne correspond à vos critères de recherche actuels.'
              : 'Créez le premier compte utilisateur de la plateforme.'
          }
          actionLabel="Nouvel utilisateur"
          onAction={handleOpenCreate}
        />
      ) : (
        <div className="overflow-x-auto rounded-xl border border-[#E2E8F0] bg-white shadow-xs">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nom complet</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>Rôle</TableHead>
                <TableHead>Statut</TableHead>
                <TableHead>Créé le</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {users.map((u) => {
                const isSelf = currentUser?.id === u.id
                const name = fullName(u)
                return (
                  <TableRow key={u.id}>
                    <TableCell>
                      <span className="font-semibold text-[#1E3A8A]">
                        {name}
                        {isSelf && (
                          <span className="ml-2 text-[10px] font-bold uppercase text-slate-400">
                            (vous)
                          </span>
                        )}
                      </span>
                    </TableCell>

                    <TableCell className="text-slate-600">{u.email}</TableCell>

                    <TableCell>
                      <Badge variant={ROLE_VARIANT[u.role] || 'default'} size="sm" dot>
                        {u.role}
                      </Badge>
                    </TableCell>

                    <TableCell>
                      <Badge variant={u.active ? 'success' : 'danger'} size="sm" dot>
                        {u.active ? 'Actif' : 'Désactivé'}
                      </Badge>
                    </TableCell>

                    <TableCell className="text-xs text-slate-500">{formatDate(u.createdAt)}</TableCell>

                    <TableCell className="text-right">
                      <div className="inline-flex items-center gap-2">
                        <Select
                          aria-label={`Rôle de ${name}`}
                          value={u.role}
                          disabled={isSelf || isActionLoading}
                          onChange={(e) => handleRoleChange(u, e.target.value)}
                          options={ROLE_OPTIONS}
                          wrapperClassName="w-32"
                          className="py-1.5 text-xs"
                        />

                        <Button
                          variant={u.active ? 'outline' : 'accent'}
                          size="sm"
                          disabled={isSelf || isActionLoading}
                          aria-label={`${u.active ? 'Désactiver' : 'Activer'} ${name}`}
                          onClick={() => setActiveTarget({ user: u, nextActive: !u.active })}
                        >
                          {u.active ? 'Désactiver' : 'Activer'}
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
        </div>
      )}

      {/* Pagination */}
      {!isLoading && !error && pagination.totalPages > 1 && (
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-2">
          <p className="text-xs text-slate-500">
            Page <strong className="text-slate-800">{pagination.pageNumber + 1}</strong> sur{' '}
            <strong className="text-slate-800">{pagination.totalPages}</strong> (
            {pagination.totalElements} utilisateurs)
          </p>

          <div className="flex items-center space-x-1.5">
            <Button
              variant="outline"
              size="sm"
              disabled={pagination.first || isLoading}
              onClick={() => loadUsers(pagination.pageNumber - 1)}
              leftIcon={<ChevronLeft className="w-4 h-4" />}
            >
              Précédent
            </Button>

            <Button
              variant="outline"
              size="sm"
              disabled={pagination.last || isLoading}
              onClick={() => loadUsers(pagination.pageNumber + 1)}
              rightIcon={<ChevronRight className="w-4 h-4" />}
            >
              Suivant
            </Button>
          </div>
        </div>
      )}

      {/* Modale de création */}
      <UserFormModal
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        onSubmit={handleCreateUser}
        isLoading={isActionLoading}
        apiErrors={formApiErrors}
      />

      {/* Confirmation : changement de rôle */}
      <ConfirmDialog
        isOpen={Boolean(roleTarget)}
        onClose={() => setRoleTarget(null)}
        onConfirm={handleConfirmRoleChange}
        title="Changer le rôle de cet utilisateur ?"
        message={
          roleTarget
            ? `${fullName(roleTarget.user)} passera du rôle ${roleTarget.user.role} au rôle ${roleTarget.newRole}.`
            : ''
        }
        confirmText="Changer le rôle"
        cancelText="Annuler"
        variant="primary"
        isLoading={isActionLoading}
      />

      {/* Confirmation : activation / désactivation */}
      <ConfirmDialog
        isOpen={Boolean(activeTarget)}
        onClose={() => setActiveTarget(null)}
        onConfirm={handleConfirmActiveChange}
        title={activeTarget?.nextActive ? 'Activer ce compte ?' : 'Désactiver ce compte ?'}
        message={
          activeTarget
            ? activeTarget.nextActive
              ? `${fullName(activeTarget.user)} pourra à nouveau se connecter et accéder à la plateforme.`
              : `${fullName(activeTarget.user)} ne pourra plus se connecter et sa session active sera refusée.`
            : ''
        }
        confirmText={activeTarget?.nextActive ? 'Activer le compte' : 'Désactiver le compte'}
        cancelText="Annuler"
        variant={activeTarget?.nextActive ? 'primary' : 'danger'}
        isLoading={isActionLoading}
      />
    </div>
  )
}

export default UsersPage
