import { CheckSquare } from 'lucide-react'
import { ModulePlaceholder } from '../../components/layout/ModulePlaceholder'

export function TasksPage() {
  return (
    <ModulePlaceholder
      title="Gestion des Tâches & Vue Kanban"
      description="Organisation agile des activités, suivi par statut et gestion des priorités."
      phase="Planifié : Phase 4"
      icon={CheckSquare}
      expectedFeatures={[
        'Tableau Kanban interactif (TODO, IN_PROGRESS, DONE, BLOCKED)',
        'Gestion des priorités (LOW, MEDIUM, HIGH, URGENT)',
        'Assignation aux membres de l’équipe de projet',
        'Dates limites et alertes de retard',
        'Filtrage par membre et statut',
      ]}
    />
  )
}

export default TasksPage
