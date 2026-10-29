import { useState, useEffect, useCallback } from 'react'
import { Link } from 'react-router-dom'
import {
  FolderGit2,
  CheckSquare,
  FileText,
  TrendingUp,
  Plus,
  ArrowRight,
  Sparkles,
  Clock,
  CheckCircle2,
  AlertCircle,
  Eye,
  Bot,
  ExternalLink,
  RefreshCw,
} from 'lucide-react'
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Cell,
} from 'recharts'
import {
  Button,
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  StatCard,
  Badge,
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell,
  SkeletonCard,
  Skeleton,
  useToast,
} from '../../components/ui'
import { projectService } from '../../features/projects'
import ProjectDetailModal from '../../features/projects/components/ProjectDetailModal'
import ProjectFormModal from '../../features/projects/components/ProjectFormModal'

export function DashboardPage() {
  const toast = useToast()

  const [stats, setStats] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [selectedProject, setSelectedProject] = useState(null)
  const [isDetailOpen, setIsDetailOpen] = useState(false)
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const loadDashboardData = useCallback(async () => {
    setIsLoading(true)
    try {
      const data = await projectService.getDashboardStats()
      setStats(data)
    } catch (err) {
      console.error('Erreur dashboard:', err)
      toast.error('Erreur', 'Impossible de charger les métriques du tableau de bord.')
    } finally {
      setIsLoading(false)
    }
  }, [toast])

  useEffect(() => {
    loadDashboardData()
  }, [loadDashboardData])

  const handleOpenDetail = (project) => {
    setSelectedProject(project)
    setIsDetailOpen(true)
  }

  const handleCreateProject = async (formData) => {
    setIsSubmitting(true)
    try {
      const created = await projectService.createProject(formData)
      setIsCreateOpen(false)
      toast.success('Projet créé', `Le projet "${created.title}" a été ajouté.`)
      loadDashboardData()
    } catch (err) {
      if (err.status === 401) {
        toast.error(
          'Action requiert JWT',
          'Spring Security bloque les requêtes POST anonymes. Utilisez le mode Mock pour simuler la création.'
        )
      } else {
        toast.error('Erreur', err.message || 'Impossible d’enregistrer le projet.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  const statusBadgeVariant = (status) => {
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

  const recentActivitiesMock = [
    {
      id: 1,
      user: 'Salma',
      action: 'a soumis le livrable',
      target: 'Architecture Microservices v1.0',
      time: 'Il y a 25 minutes',
      icon: FileText,
      color: 'text-blue-600 bg-blue-50',
    },
    {
      id: 2,
      user: 'Yassine',
      action: 'a validé la tâche',
      target: 'Intégration Flyway et tests Spring Boot',
      time: 'Il y a 1 heure',
      icon: CheckCircle2,
      color: 'text-emerald-600 bg-emerald-50',
    },
    {
      id: 3,
      user: 'Dr. Alami (Encadrant)',
      action: 'a approuvé le statut du projet',
      target: 'Plateforme PFE avec CI/CD',
      time: 'Il y a 3 heures',
      icon: AlertCircle,
      color: 'text-amber-600 bg-amber-50',
    },
    {
      id: 4,
      user: 'AI Encadrant',
      action: 'a généré des recommandations pour',
      target: 'Plan de test et couverture de code',
      time: 'Hier à 18:30',
      icon: Bot,
      color: 'text-purple-600 bg-purple-50',
    },
  ]

  return (
    <div className="space-y-8 animate-in fade-in duration-300">
      {/* Bannière de Bienvenue */}
      <div className="bg-gradient-to-r from-[#1E3A8A] via-[#1E3A8A] to-[#172554] rounded-2xl p-6 sm:p-8 text-white shadow-md relative overflow-hidden">
        <div
          className="absolute -right-12 -top-12 w-64 h-64 bg-[#0DAD98]/20 rounded-full blur-3xl pointer-events-none"
          aria-hidden="true"
        />

        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div className="space-y-2 max-w-2xl">
            <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full text-xs font-semibold bg-white/10 text-emerald-300 border border-white/10">
              <Sparkles className="w-3.5 h-3.5" />
              <span>Espace Académique &bull; Phase 3 Active</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
              Bonjour Mohamed 👋
            </h1>
            <p className="text-sm text-blue-100/90 leading-relaxed">
              Tableau de bord interconnecté avec les données réelles et le catalogue des projets de fin
              d&apos;études. Suivez les jalons, les métriques d&apos;avancement et les recommandations de votre
              AI Encadrant.
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <Button
              variant="outline"
              size="sm"
              onClick={loadDashboardData}
              className="bg-white/10 text-white border-white/20 hover:bg-white/20"
              leftIcon={<RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />}
            >
              Actualiser
            </Button>
            <Button
              variant="accent"
              size="sm"
              onClick={() => setIsCreateOpen(true)}
              leftIcon={<Plus className="w-4 h-4" />}
            >
              Nouveau Projet
            </Button>
          </div>
        </div>

        {/* Source des données & Statut */}
        <div className="mt-6 pt-4 border-t border-white/10 flex flex-wrap items-center justify-between gap-2 text-xs text-blue-200">
          <div className="flex items-center space-x-2">
            <span
              className={`w-2 h-2 rounded-full ${
                stats?.isMock ? 'bg-amber-400 animate-pulse' : 'bg-[#0DAD98]'
              }`}
            />
            <span>
              {stats?.isMock
                ? 'Données calculées en Mode Démonstration (VITE_USE_MOCK=true)'
                : 'Données issues de l’API Spring Boot (/api/v1/projects)'}
            </span>
          </div>
          <span className="font-mono text-xs text-blue-300">Environnement : Sprint 2 Connecté</span>
        </div>
      </div>

      {/* 4 Cartes Statistiques Métier */}
      <section aria-labelledby="dashboard-metrics" className="space-y-2">
        <h2 id="dashboard-metrics" className="sr-only">
          Métriques du Tableau de bord
        </h2>
        {isLoading || !stats ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
            <StatCard
              title="Total Projets"
              value={stats.total}
              icon={FolderGit2}
              iconBg="bg-blue-50"
              iconColor="text-[#1E3A8A]"
              caption="Catalogue global enregistré"
            />
            <StatCard
              title="Projets En Cours"
              value={stats.inProgress}
              icon={TrendingUp}
              iconBg="bg-indigo-50"
              iconColor="text-indigo-600"
              trend={stats.total > 0 ? `${Math.round((stats.inProgress / stats.total) * 100)}%` : '0%'}
              trendDirection="up"
              caption="En phase de développement"
            />
            <StatCard
              title="Projets Validés / Finis"
              value={stats.approved + stats.completed}
              icon={CheckSquare}
              iconBg="bg-emerald-50"
              iconColor="text-emerald-600"
              trend={`${stats.completed} terminés`}
              trendDirection="up"
              caption="Cadrage approuvé ou soutenu"
            />
            <StatCard
              title="En Attente / Proposés"
              value={stats.pending + stats.proposed}
              icon={Clock}
              iconBg="bg-amber-50"
              iconColor="text-amber-600"
              trend={`${stats.pending} en attente`}
              trendDirection="neutral"
              caption="En attente de validation encadrant"
            />
          </div>
        )}
      </section>

      {/* Grille principale : Projets récents & Graphique Recharts */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Colonne Gauche (2/3) : Projets récents + Graphique Recharts */}
        <div className="lg:col-span-2 space-y-8">
          {/* Liste des Projets Récents */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <div>
                <CardTitle>Projets Récents</CardTitle>
                <CardDescription>
                  Derniers projets enregistrés sur la plateforme
                </CardDescription>
              </div>
              <Link to="/projects">
                <Button
                  variant="ghost"
                  size="sm"
                  className="text-[#1E3A8A] text-xs font-semibold"
                  rightIcon={<ArrowRight className="w-3.5 h-3.5" />}
                >
                  Voir tous les projets
                </Button>
              </Link>
            </CardHeader>

            <CardContent className="p-0">
              {isLoading || !stats ? (
                <div className="p-6 space-y-3">
                  <Skeleton className="h-8 w-full" />
                  <Skeleton className="h-8 w-full" />
                  <Skeleton className="h-8 w-full" />
                </div>
              ) : stats.recentProjects.length === 0 ? (
                <div className="p-8 text-center text-xs text-slate-500">
                  Aucun projet enregistré pour le moment.
                </div>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Titre du Projet</TableHead>
                      <TableHead>Statut</TableHead>
                      <TableHead>Membres</TableHead>
                      <TableHead>Échéance</TableHead>
                      <TableHead className="text-right">Action</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {stats.recentProjects.map((project) => (
                      <TableRow key={project.id}>
                        <TableCell className="font-semibold text-[#1E3A8A] max-w-[240px] truncate">
                          {project.title}
                        </TableCell>
                        <TableCell>
                          <Badge variant={statusBadgeVariant(project.status)} size="sm" dot>
                            {project.status}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-xs text-slate-600">
                          {project.membersCount ?? 0} étudiant(s)
                        </TableCell>
                        <TableCell className="text-xs text-slate-500 font-mono">
                          {project.endDate || '—'}
                        </TableCell>
                        <TableCell className="text-right">
                          <Button
                            variant="ghost"
                            size="sm"
                            aria-label={`Détails ${project.title}`}
                            className="p-1.5 text-slate-500 hover:text-[#1E3A8A]"
                            onClick={() => handleOpenDetail(project)}
                          >
                            <Eye className="w-4 h-4" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>

          {/* Graphique Recharts : Répartition des projets par statut */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <div>
                <CardTitle>Répartition des Projets par Statut</CardTitle>
                <CardDescription>
                  Visualisation graphique calculée dynamiquement à partir des données de projet
                </CardDescription>
              </div>
              <Badge variant="accent" size="sm">
                Recharts Dynamique
              </Badge>
            </CardHeader>
            <CardContent>
              {isLoading || !stats ? (
                <Skeleton className="h-64 w-full rounded-xl" />
              ) : (
                <div className="space-y-4">
                  <div className="h-64 w-full">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart
                        data={stats.distribution}
                        margin={{ top: 10, right: 10, left: -20, bottom: 5 }}
                      >
                        <XAxis
                          dataKey="name"
                          tick={{ fontSize: 11, fill: '#64748B' }}
                          axisLine={{ stroke: '#E2E8F0' }}
                          tickLine={false}
                        />
                        <YAxis
                          allowDecimals={false}
                          tick={{ fontSize: 11, fill: '#64748B' }}
                          axisLine={{ stroke: '#E2E8F0' }}
                          tickLine={false}
                        />
                        <Tooltip
                          contentStyle={{
                            backgroundColor: '#FFFFFF',
                            borderRadius: '8px',
                            border: '1px solid #E2E8F0',
                            fontSize: '12px',
                            boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.05)',
                          }}
                        />
                        <Bar dataKey="count" radius={[6, 6, 0, 0]} name="Nombre de projets">
                          {stats.distribution.map((entry, index) => (
                            <Cell key={`cell-${index}`} fill={entry.color || '#1E3A8A'} />
                          ))}
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  </div>

                  <p className="text-[11px] text-slate-400 text-center">
                    Note technique : Les métriques reflètent les projets chargés ({stats.total} projets).
                    Si la pagination serveur est restreinte, seules les données retournées par l&apos;API sont agrégées.
                  </p>
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* Colonne Droite (1/3) : AI Encadrant & Activités Récentes */}
        <div className="space-y-8">
          {/* AI Encadrant Card */}
          <Card className="border-blue-200 bg-gradient-to-b from-blue-50/50 to-white">
            <CardHeader>
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-2">
                  <div className="w-8 h-8 rounded-lg bg-[#0DAD98] text-white flex items-center justify-center shadow-xs">
                    <Bot className="w-4 h-4" />
                  </div>
                  <CardTitle className="text-base">AI Encadrant</CardTitle>
                </div>
                <Badge variant="accent" size="sm">
                  Phase 4 Prévue
                </Badge>
              </div>
              <CardDescription>
                Suggestions automatisées pour l&apos;encadrement de votre projet
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="p-3.5 rounded-xl bg-white border border-blue-100 shadow-2xs space-y-1.5">
                <span className="text-[11px] font-bold text-[#1E3A8A] uppercase tracking-wide flex items-center gap-1">
                  <Sparkles className="w-3 h-3 text-[#0DAD98]" />
                  <span>Recommandation Projets</span>
                </span>
                <p className="text-xs text-slate-700 leading-relaxed">
                  {stats?.inProgress ?? 0} projet(s) sont en cours. Veillez à ce que chaque projet
                  dispose d&apos;au moins une date limite et de livrables planifiés.
                </p>
              </div>

              <div className="p-3.5 rounded-xl bg-white border border-blue-100 shadow-2xs space-y-1.5">
                <span className="text-[11px] font-bold text-[#1E3A8A] uppercase tracking-wide flex items-center gap-1">
                  <CheckCircle2 className="w-3 h-3 text-emerald-600" />
                  <span>Contrôle Qualité Frontend</span>
                </span>
                <p className="text-xs text-slate-700 leading-relaxed">
                  L&apos;intégration CRUD avec les DTO Jakarta est effective. Préparez vos tests
                  Vitest pour valider l&apos;ensemble des scénarios nominaux et d&apos;erreur.
                </p>
              </div>

              <Link to="/ai-assistant">
                <Button
                  variant="outline"
                  size="sm"
                  className="w-full text-xs font-semibold text-[#1E3A8A] border-blue-200 hover:bg-blue-50 mt-2"
                  rightIcon={<ExternalLink className="w-3.5 h-3.5" />}
                >
                  Accéder à l&apos;espace AI Encadrant
                </Button>
              </Link>
            </CardContent>
          </Card>

          {/* Activités Récentes */}
          <Card>
            <CardHeader>
              <CardTitle>Activités Récentes</CardTitle>
              <CardDescription>Flux en direct des contributions de l&apos;équipe</CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="relative pl-6 space-y-6 before:absolute before:left-2.5 before:top-2 before:bottom-2 before:w-0.5 before:bg-slate-200">
                {recentActivitiesMock.map((act) => {
                  const Icon = act.icon
                  return (
                    <div key={act.id} className="relative group">
                      <div
                        className={`absolute -left-6 top-0.5 w-5 h-5 rounded-full flex items-center justify-center ring-4 ring-white ${act.color}`}
                      >
                        <Icon className="w-3 h-3" />
                      </div>
                      <div className="text-xs">
                        <p className="text-slate-800">
                          <strong className="font-semibold text-slate-900">{act.user}</strong>{' '}
                          {act.action}{' '}
                          <span className="font-medium text-[#1E3A8A]">{act.target}</span>
                        </p>
                        <span className="text-[10px] text-slate-400 mt-0.5 block">{act.time}</span>
                      </div>
                    </div>
                  )
                })}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Modale de Détails */}
      <ProjectDetailModal
        isOpen={isDetailOpen}
        onClose={() => setIsDetailOpen(false)}
        project={selectedProject}
      />

      {/* Modale de Création rapide */}
      <ProjectFormModal
        isOpen={isCreateOpen}
        onClose={() => setIsCreateOpen(false)}
        onSubmit={handleCreateProject}
        isLoading={isSubmitting}
      />
    </div>
  )
}

export default DashboardPage
