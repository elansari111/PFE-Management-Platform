import { useState, useEffect, useCallback } from 'react'
import {
  FolderGit2,
  Plus,
  Search,
  LayoutGrid,
  List,
  RefreshCw,
  Eye,
  Pencil,
  Trash2,
  Calendar,
  Users,
  CheckSquare,
  FileText,
  AlertTriangle,
  ChevronLeft,
  ChevronRight,
  Sparkles,
} from 'lucide-react'
import {
  Button,
  Badge,
  Card,
  CardHeader,
  CardTitle,
  CardContent,
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell,
  ConfirmDialog,
  EmptyState,
  ErrorState,
  SkeletonCard,
  Skeleton,
  useToast,
} from '../../components/ui'
import { projectService } from '../../features/projects'
import { isMockEnabled } from '../../mocks'
import ProjectFormModal from '../../features/projects/components/ProjectFormModal'
import ProjectDetailModal from '../../features/projects/components/ProjectDetailModal'

export function ProjectsPage() {
  const toast = useToast()
  const isMock = isMockEnabled()

  // États des données
  const [projects, setProjects] = useState([])
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

  // Filtres, recherche et affichage
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [sortBy, setSortBy] = useState('createdAt')
  const [sortDirection, setSortDirection] = useState('DESC')
  const [viewMode, setViewMode] = useState('table') // 'table' | 'cards'

  // États des modales
  const [isFormOpen, setIsFormOpen] = useState(false)
  const [isDetailOpen, setIsDetailOpen] = useState(false)
  const [isDeleteOpen, setIsDeleteOpen] = useState(false)
  const [selectedProject, setSelectedProject] = useState(null)
  const [isActionLoading, setIsActionLoading] = useState(false)
  const [formApiErrors, setFormApiErrors] = useState(null)

  // Chargement des projets
  const loadProjects = useCallback(
    async (pageToLoad = 0) => {
      setIsLoading(true)
      setError(null)
      try {
        const result = await projectService.getProjects({
          status: statusFilter,
          page: pageToLoad,
          size: pagination.pageSize,
          sortBy,
          direction: sortDirection,
          search: searchTerm,
        })

        setProjects(result.content || [])
        setPagination({
          pageNumber: result.pageNumber ?? pageToLoad,
          pageSize: result.pageSize ?? pagination.pageSize,
          totalElements: result.totalElements ?? (result.content || []).length,
          totalPages: result.totalPages ?? 1,
          first: result.first ?? pageToLoad === 0,
          last: result.last ?? true,
        })
      } catch (err) {
        console.error('Erreur chargement projets:', err)
        setError(
          err.message ||
            'Impossible de charger la liste des projets. Vérifiez la connexion au backend Spring Boot.'
        )
      } finally {
        setIsLoading(false)
      }
    },
    [statusFilter, pagination.pageSize, sortBy, sortDirection, searchTerm]
  )

  useEffect(() => {
    loadProjects(0)
  }, [loadProjects])

  // Gestion de la création / modification
  const handleOpenCreate = () => {
    setSelectedProject(null)
    setFormApiErrors(null)
    setIsFormOpen(true)
  }

  const handleOpenEdit = (project) => {
    setSelectedProject(project)
    setFormApiErrors(null)
    setIsFormOpen(true)
  }

  const handleOpenDetail = (project) => {
    setSelectedProject(project)
    setIsDetailOpen(true)
  }

  const handleOpenDelete = (project) => {
    setSelectedProject(project)
    setIsDeleteOpen(true)
  }

  const handleFormSubmit = async (formData) => {
    setIsActionLoading(true)
    setFormApiErrors(null)
    try {
      if (selectedProject?.id) {
        // Mise à jour
        const updated = await projectService.updateProject(selectedProject.id, formData)
        setIsFormOpen(false)
        toast.success(
          'Projet modifié avec succès',
          `Le projet "${updated.title}" a été mis à jour.`
        )
      } else {
        // Création
        const created = await projectService.createProject(formData)
        setIsFormOpen(false)
        toast.success(
          'Projet créé avec succès',
          `Le sujet "${created.title}" a été enregistré.`
        )
      }
      loadProjects(pagination.pageNumber)
    } catch (err) {
      if (err.validationErrors) {
        setFormApiErrors(err.validationErrors)
      } else if (err.status === 409) {
        setFormApiErrors({ title: err.message || 'Un projet avec ce titre existe déjà' })
      } else if (err.status === 401) {
        toast.error(
          'Action non autorisée (401)',
          'Cette opération requiert un jeton JWT valide (Spring Security). Utilisez le mode Mock pour simuler l’action.'
        )
      } else {
        toast.error('Erreur', err.message || 'Échec de l’opération sur le projet.')
      }
    } finally {
      setIsActionLoading(false)
    }
  }

  const handleConfirmDelete = async () => {
    if (!selectedProject?.id) return
    setIsActionLoading(true)
    try {
      await projectService.deleteProject(selectedProject.id)
      setIsDeleteOpen(false)
      toast.success(
        'Projet supprimé',
        `Le projet #${selectedProject.id} a été retiré de la plateforme.`
      )
      loadProjects(pagination.pageNumber)
    } catch (err) {
      if (err.status === 401) {
        toast.error(
          'Suppression non autorisée (401)',
          'Spring Security bloque les requêtes DELETE anonymes. Activez VITE_USE_MOCK=true pour tester en mode démo.'
        )
      } else {
        toast.error('Erreur de suppression', err.message || 'Impossible de supprimer le projet.')
      }
    } finally {
      setIsActionLoading(false)
    }
  }

  const statusVariant = (status) => {
    switch (status) {
      case 'APPROVED':
        return 'accent'
      case 'IN_PROGRESS':
        return 'primary'
      case 'COMPLETED':
        return 'success'
      case 'PENDING':
        return 'warning'
      case 'PROPOSED':
        return 'default'
      default:
        return 'outline'
    }
  }

  return (
    <div className="space-y-6 animate-in fade-in duration-200">
      {/* En-tête de la page */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-2xl font-extrabold text-[#1E3A8A] tracking-tight">
              Gestion des Projets PFE
            </h1>
            <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-blue-100 text-[#1E3A8A]">
              {pagination.totalElements} au total
            </span>
          </div>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Catalogue complet des sujets de PFE, suivi de validation et progression.
          </p>
        </div>

        <div className="flex items-center space-x-2.5">
          <Button
            variant="outline"
            size="sm"
            onClick={() => loadProjects(pagination.pageNumber)}
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
            Nouveau Projet
          </Button>
        </div>
      </div>

      {/* Bannière Mode Démonstration explicite si VITE_USE_MOCK est actif */}
      {isMock ? (
        <div className="p-3.5 rounded-xl bg-amber-50/90 border border-amber-200 text-amber-900 flex items-start space-x-3 text-xs shadow-2xs">
          <Sparkles className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
          <div className="flex-1">
            <strong className="font-bold">Mode Démonstration Actif (VITE_USE_MOCK=true) :</strong> Les
            opérations CRUD (création, modification, suppression) sont simulées en mémoire locale.
            Aucune altération n&apos;est transmise au backend Spring Boot ni à la base MySQL.
          </div>
        </div>
      ) : (
        <div className="p-3.5 rounded-xl bg-blue-50/70 border border-blue-200 text-blue-900 flex items-start space-x-3 text-xs">
          <FolderGit2 className="w-4 h-4 text-[#1E3A8A] shrink-0 mt-0.5" />
          <div className="flex-1">
            <strong className="font-bold">Mode API Spring Boot Connecté :</strong> Les données sont
            interrogées via <code className="font-mono text-blue-950 font-semibold">GET /api/v1/projects</code>.
            Les opérations de modification requièrent un jeton JWT valide selon les règles de sécurité Spring Security.
          </div>
        </div>
      )}

      {/* Barre de filtres et recherche */}
      <Card className="p-4">
        <div className="flex flex-col lg:flex-row items-center justify-between gap-3">
          {/* Recherche */}
          <div className="relative w-full lg:w-80">
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3 text-slate-400">
              <Search className="w-4 h-4" />
            </div>
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Rechercher par titre ou mot-clé..."
              aria-label="Recherche de projets"
              className="w-full pl-9 pr-3 py-1.5 text-xs bg-slate-50 border border-[#E2E8F0] rounded-lg focus:outline-none focus:bg-white focus:border-[#1E3A8A] focus:ring-1 focus:ring-[#1E3A8A] transition-colors"
            />
          </div>

          {/* Filtre Statut & Tri */}
          <div className="flex flex-wrap items-center gap-2.5 w-full lg:w-auto justify-end">
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              aria-label="Filtrer par statut"
              className="px-2.5 py-1.5 text-xs bg-slate-50 border border-[#E2E8F0] rounded-lg text-slate-700 focus:outline-none focus:border-[#1E3A8A]"
            >
              <option value="ALL">Tous les statuts</option>
              <option value="PROPOSED">PROPOSED (Proposés)</option>
              <option value="PENDING">PENDING (En attente)</option>
              <option value="APPROVED">APPROVED (Validés)</option>
              <option value="IN_PROGRESS">IN_PROGRESS (En cours)</option>
              <option value="COMPLETED">COMPLETED (Terminés)</option>
              <option value="ARCHIVED">ARCHIVED (Archivés)</option>
            </select>

            <select
              value={`${sortBy}-${sortDirection}`}
              onChange={(e) => {
                const [sb, sd] = e.target.value.split('-')
                setSortBy(sb)
                setSortDirection(sd)
              }}
              aria-label="Trier par critère"
              className="px-2.5 py-1.5 text-xs bg-slate-50 border border-[#E2E8F0] rounded-lg text-slate-700 focus:outline-none focus:border-[#1E3A8A]"
            >
              <option value="createdAt-DESC">Plus récents d&apos;abord</option>
              <option value="createdAt-ASC">Plus anciens d&apos;abord</option>
              <option value="title-ASC">Titre (A &rarr; Z)</option>
              <option value="title-DESC">Titre (Z &rarr; A)</option>
            </select>

            {/* Bascule de vue Grille / Tableau */}
            <div className="flex items-center border border-[#E2E8F0] rounded-lg p-0.5 bg-slate-50">
              <button
                type="button"
                onClick={() => setViewMode('table')}
                aria-label="Vue Tableau"
                className={`p-1.5 rounded-md transition-colors ${
                  viewMode === 'table' ? 'bg-white shadow-2xs text-[#1E3A8A]' : 'text-slate-400 hover:text-slate-700'
                }`}
              >
                <List className="w-4 h-4" />
              </button>
              <button
                type="button"
                onClick={() => setViewMode('cards')}
                aria-label="Vue Cartes"
                className={`p-1.5 rounded-md transition-colors ${
                  viewMode === 'cards' ? 'bg-white shadow-2xs text-[#1E3A8A]' : 'text-slate-400 hover:text-slate-700'
                }`}
              >
                <LayoutGrid className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      </Card>

      {/* Contenu principal : Loading, Error, Empty ou Liste */}
      {isLoading ? (
        viewMode === 'table' ? (
          <div className="bg-white border border-[#E2E8F0] rounded-xl p-4 space-y-3">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-12 w-full" />
            <Skeleton className="h-12 w-full" />
            <Skeleton className="h-12 w-full" />
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
          </div>
        )
      ) : error ? (
        <ErrorState
          title="Erreur lors de la récupération des projets"
          message={error}
          onRetry={() => loadProjects(pagination.pageNumber)}
        />
      ) : projects.length === 0 ? (
        <EmptyState
          title="Aucun projet trouvé"
          description={
            searchTerm || statusFilter !== 'ALL'
              ? 'Aucun projet ne correspond à vos critères de recherche actuels.'
              : 'Commencez par créer le premier projet de fin d’études.'
          }
          actionLabel="Créer un Projet"
          onAction={handleOpenCreate}
        />
      ) : viewMode === 'table' ? (
        /* VUE TABLEAU */
        <Card className="overflow-hidden">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-16">ID</TableHead>
                <TableHead>Titre du Projet</TableHead>
                <TableHead>Statut</TableHead>
                <TableHead>Période</TableHead>
                <TableHead className="text-center">Indicateurs</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {projects.map((proj) => (
                <TableRow key={proj.id} className="group">
                  <TableCell className="font-mono text-xs text-slate-400">
                    #{proj.id}
                  </TableCell>

                  <TableCell className="max-w-xs sm:max-w-md">
                    <div className="space-y-0.5">
                      <span className="font-bold text-[#1E3A8A] block truncate">
                        {proj.title}
                      </span>
                      <p className="text-xs text-slate-500 line-clamp-1">
                        {proj.description || 'Sans description'}
                      </p>
                    </div>
                  </TableCell>

                  <TableCell>
                    <Badge variant={statusVariant(proj.status)} size="sm" dot>
                      {proj.status}
                    </Badge>
                  </TableCell>

                  <TableCell className="text-xs text-slate-600">
                    {proj.startDate || proj.endDate ? (
                      <span>
                        {proj.startDate || '—'} &rarr; {proj.endDate || '—'}
                      </span>
                    ) : (
                      <span className="text-slate-400">Non planifié</span>
                    )}
                  </TableCell>

                  <TableCell>
                    <div className="flex items-center justify-center space-x-3 text-xs text-slate-500">
                      <span className="flex items-center gap-1" title="Membres assignés">
                        <Users className="w-3.5 h-3.5 text-slate-400" />
                        <span>{proj.membersCount ?? 0}</span>
                      </span>
                      <span className="flex items-center gap-1" title="Tâches">
                        <CheckSquare className="w-3.5 h-3.5 text-emerald-500" />
                        <span>{proj.tasksCount ?? 0}</span>
                      </span>
                      <span className="flex items-center gap-1" title="Livrables">
                        <FileText className="w-3.5 h-3.5 text-[#0DAD98]" />
                        <span>{proj.deliverablesCount ?? 0}</span>
                      </span>
                    </div>
                  </TableCell>

                  <TableCell className="text-right">
                    <div className="inline-flex items-center space-x-1">
                      <Button
                        variant="ghost"
                        size="sm"
                        aria-label={`Détails ${proj.title}`}
                        className="p-1.5 text-slate-500 hover:text-[#1E3A8A]"
                        onClick={() => handleOpenDetail(proj)}
                      >
                        <Eye className="w-4 h-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        aria-label={`Modifier ${proj.title}`}
                        className="p-1.5 text-slate-500 hover:text-amber-600"
                        onClick={() => handleOpenEdit(proj)}
                      >
                        <Pencil className="w-4 h-4" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        aria-label={`Supprimer ${proj.title}`}
                        className="p-1.5 text-slate-500 hover:text-rose-600"
                        onClick={() => handleOpenDelete(proj)}
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Card>
      ) : (
        /* VUE CARTES */
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {projects.map((proj) => (
            <Card
              key={proj.id}
              className="flex flex-col justify-between hover:border-slate-300 transition-all duration-150"
            >
              <CardHeader className="space-y-3">
                <div className="flex items-start justify-between gap-2">
                  <Badge variant={statusVariant(proj.status)} size="sm" dot>
                    {proj.status}
                  </Badge>
                  <span className="font-mono text-[11px] text-slate-400">#{proj.id}</span>
                </div>
                <div>
                  <CardTitle className="text-base line-clamp-2 leading-snug">
                    {proj.title}
                  </CardTitle>
                  <p className="text-xs text-slate-500 line-clamp-3 mt-1.5 leading-relaxed">
                    {proj.description || 'Aucune description fournie.'}
                  </p>
                </div>
              </CardHeader>

              <CardContent className="pt-0 space-y-4">
                <div className="flex items-center justify-between text-xs text-slate-500 border-t border-[#E2E8F0] pt-3">
                  <span className="flex items-center gap-1.5">
                    <Calendar className="w-3.5 h-3.5 text-slate-400" />
                    <span>{proj.endDate ? `Fin : ${proj.endDate}` : 'Date libre'}</span>
                  </span>
                  <span className="flex items-center gap-1 text-slate-600">
                    <Users className="w-3.5 h-3.5 text-[#1E3A8A]" />
                    <span>{proj.membersCount ?? 0} memb.</span>
                  </span>
                </div>

                <div className="flex items-center justify-between pt-2 border-t border-slate-100">
                  <div className="flex items-center space-x-2 text-xs text-slate-500">
                    <span>{proj.tasksCount ?? 0} tâches</span>
                    <span>&bull;</span>
                    <span>{proj.deliverablesCount ?? 0} livrables</span>
                  </div>

                  <div className="flex items-center space-x-1">
                    <Button
                      variant="ghost"
                      size="sm"
                      aria-label="Voir"
                      className="p-1.5 text-slate-500 hover:text-[#1E3A8A]"
                      onClick={() => handleOpenDetail(proj)}
                    >
                      <Eye className="w-4 h-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      aria-label="Modifier"
                      className="p-1.5 text-slate-500 hover:text-amber-600"
                      onClick={() => handleOpenEdit(proj)}
                    >
                      <Pencil className="w-4 h-4" />
                    </Button>
                    <Button
                      variant="ghost"
                      size="sm"
                      aria-label="Supprimer"
                      className="p-1.5 text-slate-500 hover:text-rose-600"
                      onClick={() => handleOpenDelete(proj)}
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Pagination Controls */}
      {pagination.totalPages > 1 && (
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-2">
          <p className="text-xs text-slate-500">
            Page <strong className="text-slate-800">{pagination.pageNumber + 1}</strong> sur{' '}
            <strong className="text-slate-800">{pagination.totalPages}</strong> (
            {pagination.totalElements} projets)
          </p>

          <div className="flex items-center space-x-1.5">
            <Button
              variant="outline"
              size="sm"
              disabled={pagination.first || isLoading}
              onClick={() => loadProjects(pagination.pageNumber - 1)}
              leftIcon={<ChevronLeft className="w-4 h-4" />}
            >
              Précédent
            </Button>

            {Array.from({ length: pagination.totalPages }).map((_, idx) => (
              <button
                key={idx}
                type="button"
                onClick={() => loadProjects(idx)}
                className={`w-8 h-8 rounded-lg text-xs font-semibold transition-colors ${
                  pagination.pageNumber === idx
                    ? 'bg-[#1E3A8A] text-white shadow-xs'
                    : 'bg-white border border-[#E2E8F0] text-slate-600 hover:bg-slate-50'
                }`}
              >
                {idx + 1}
              </button>
            ))}

            <Button
              variant="outline"
              size="sm"
              disabled={pagination.last || isLoading}
              onClick={() => loadProjects(pagination.pageNumber + 1)}
              rightIcon={<ChevronRight className="w-4 h-4" />}
            >
              Suivant
            </Button>
          </div>
        </div>
      )}

      {/* Modale de Création / Modification */}
      <ProjectFormModal
        isOpen={isFormOpen}
        onClose={() => setIsFormOpen(false)}
        onSubmit={handleFormSubmit}
        initialData={selectedProject}
        isLoading={isActionLoading}
        apiErrors={formApiErrors}
      />

      {/* Modale de Détails */}
      <ProjectDetailModal
        isOpen={isDetailOpen}
        onClose={() => setIsDetailOpen(false)}
        project={selectedProject}
        onEdit={(proj) => handleOpenEdit(proj)}
      />

      {/* Modale de Confirmation de Suppression */}
      <ConfirmDialog
        isOpen={isDeleteOpen}
        onClose={() => setIsDeleteOpen(false)}
        onConfirm={handleConfirmDelete}
        title="Supprimer définitivement le projet ?"
        message={`Êtes-vous certain de vouloir supprimer le projet "${selectedProject?.title}" ? Cette action est irréversible.`}
        confirmText="Supprimer le projet"
        cancelText="Conserver"
        variant="danger"
        isLoading={isActionLoading}
      />
    </div>
  )
}

export default ProjectsPage
