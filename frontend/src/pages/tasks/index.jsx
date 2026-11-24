import { useState, useEffect, useCallback, useRef } from 'react'
import { RefreshCw, Calendar, User, Folder, CheckSquare } from 'lucide-react'
import { Button, Badge, Skeleton, EmptyState, ErrorState, useToast } from '../../components/ui'
import { taskService } from '../../features/tasks'

const COLUMNS = [
  { key: 'TODO', label: 'À faire', accent: 'border-t-slate-400' },
  { key: 'IN_PROGRESS', label: 'En cours', accent: 'border-t-blue-500' },
  { key: 'DONE', label: 'Terminé', accent: 'border-t-emerald-500' },
  { key: 'BLOCKED', label: 'Bloqué', accent: 'border-t-rose-500' },
]

const PRIORITIES = {
  LOW: { label: 'Basse', variant: 'default' },
  MEDIUM: { label: 'Moyenne', variant: 'primary' },
  HIGH: { label: 'Haute', variant: 'warning' },
  URGENT: { label: 'Urgente', variant: 'danger' },
}

function getAssigneeName(task) {
  const a = task.assignedTo
  if (a && typeof a === 'object') {
    const full = a.fullName || [a.firstName, a.lastName].filter(Boolean).join(' ')
    if (full) return full
  }
  if (task.assignedToName) return task.assignedToName
  if (task.assigneeName) return task.assigneeName
  if (task.assignedToId) return `Utilisateur #${task.assignedToId}`
  if (typeof a === 'number') return `Utilisateur #${a}`
  return 'Non assignée'
}

function getProjectName(task) {
  return task.projectTitle || task.project?.title || (task.projectId ? `Projet #${task.projectId}` : '')
}

function isOverdue(task) {
  if (!task.dueDate || task.status === 'DONE') return false
  return task.dueDate < new Date().toISOString().slice(0, 10)
}

function TaskCard({ task, onStatusChange, isUpdating }) {
  const priority = PRIORITIES[task.priority]
  const overdue = isOverdue(task)

  return (
    <div className="bg-white border border-[#E2E8F0] rounded-lg p-3.5 shadow-xs space-y-2.5">
      <div className="flex items-start justify-between gap-2">
        <h4 className="text-sm font-semibold text-slate-800 leading-snug">{task.title}</h4>
        {priority && (
          <Badge variant={priority.variant} size="sm">
            {priority.label}
          </Badge>
        )}
      </div>

      {task.description && (
        <p className="text-xs text-slate-500 leading-relaxed line-clamp-2">{task.description}</p>
      )}

      <div className="space-y-1 text-[11px] text-slate-500">
        {getProjectName(task) && (
          <div className="flex items-center gap-1.5">
            <Folder className="w-3 h-3 shrink-0" />
            <span className="truncate">{getProjectName(task)}</span>
          </div>
        )}
        <div className="flex items-center gap-1.5">
          <User className="w-3 h-3 shrink-0" />
          <span className="truncate">{getAssigneeName(task)}</span>
        </div>
        {task.dueDate && (
          <div className={`flex items-center gap-1.5 ${overdue ? 'text-rose-600 font-semibold' : ''}`}>
            <Calendar className="w-3 h-3 shrink-0" />
            <span>
              {task.dueDate}
              {overdue ? ' (en retard)' : ''}
            </span>
          </div>
        )}
      </div>

      <select
        aria-label={`Changer le statut de la tâche ${task.title}`}
        value={task.status}
        disabled={isUpdating}
        onChange={(e) => onStatusChange(task, e.target.value)}
        className="w-full text-xs border border-[#E2E8F0] rounded-md px-2 py-1.5 bg-slate-50 text-slate-700 focus:outline-none focus:ring-2 focus:ring-[#1E3A8A] disabled:opacity-50"
      >
        {COLUMNS.map((c) => (
          <option key={c.key} value={c.key}>
            {c.label}
          </option>
        ))}
      </select>
    </div>
  )
}

export function TasksPage() {
  const toast = useToast()
  const [tasks, setTasks] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)
  const [updatingId, setUpdatingId] = useState(null)

  // Évite les mises à jour d'état après le démontage du composant
  const isMountedRef = useRef(true)
  useEffect(() => {
    isMountedRef.current = true
    return () => {
      isMountedRef.current = false
    }
  }, [])

  const loadTasks = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const result = await taskService.getTasks()
      if (!isMountedRef.current) return
      setTasks(result?.content || [])
    } catch (err) {
      if (!isMountedRef.current) return
      setError(err.message || 'Impossible de charger les tâches.')
    } finally {
      if (isMountedRef.current) setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    loadTasks()
  }, [loadTasks])

  const handleStatusChange = async (task, newStatus) => {
    if (newStatus === task.status) return
    setUpdatingId(task.id)
    try {
      const updated = await taskService.updateTaskStatus(task.id, newStatus)
      if (!isMountedRef.current) return
      setTasks((prev) =>
        prev.map((t) => (t.id === task.id ? { ...t, ...updated, status: updated?.status || newStatus } : t))
      )
      toast.success('Statut mis à jour', `« ${task.title} » est maintenant dans la colonne choisie.`)
    } catch (err) {
      if (!isMountedRef.current) return
      toast.error('Modification refusée', err.message || 'Impossible de changer le statut.')
    } finally {
      if (isMountedRef.current) setUpdatingId(null)
    }
  }

  return (
    <div className="space-y-6 animate-in fade-in duration-200">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-2xl font-extrabold text-[#1E3A8A] tracking-tight">Tâches & Kanban</h1>
            <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-blue-100 text-[#1E3A8A]">
              {tasks.length} au total
            </span>
          </div>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Suivi des tâches par statut. Vous ne voyez que les tâches de vos projets.
          </p>
        </div>

        <Button
          variant="outline"
          size="sm"
          onClick={loadTasks}
          title="Rafraîchir les données"
          leftIcon={<RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />}
        >
          Actualiser
        </Button>
      </div>

      {error ? (
        <ErrorState title="Chargement impossible" message={error} onRetry={loadTasks} />
      ) : isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4" data-testid="tasks-loading">
          {COLUMNS.map((c) => (
            <Skeleton key={c.key} className="h-64 rounded-xl" />
          ))}
        </div>
      ) : tasks.length === 0 ? (
        <EmptyState
          icon={CheckSquare}
          title="Aucune tâche à afficher"
          description="Aucune tâche n'existe encore dans vos projets, ou vous n'êtes membre d'aucun projet."
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4 items-start">
          {COLUMNS.map((col) => {
            const colTasks = tasks.filter((t) => t.status === col.key)
            return (
              <section
                key={col.key}
                aria-label={`Colonne ${col.label}`}
                className={`bg-slate-50 border border-[#E2E8F0] border-t-4 ${col.accent} rounded-xl p-3 space-y-3`}
              >
                <div className="flex items-center justify-between px-1">
                  <h3 className="text-sm font-bold text-slate-700">{col.label}</h3>
                  <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-white border border-[#E2E8F0] text-slate-600">
                    {colTasks.length}
                  </span>
                </div>

                {colTasks.length === 0 ? (
                  <p className="text-xs text-slate-400 text-center py-6">Aucune tâche</p>
                ) : (
                  colTasks.map((task) => (
                    <TaskCard
                      key={task.id}
                      task={task}
                      onStatusChange={handleStatusChange}
                      isUpdating={updatingId === task.id}
                    />
                  ))
                )}
              </section>
            )
          })}
        </div>
      )}
    </div>
  )
}

export default TasksPage