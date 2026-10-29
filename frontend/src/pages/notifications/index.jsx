import { Bell } from 'lucide-react'
import { ModulePlaceholder } from '../../components/layout/ModulePlaceholder'

export function NotificationsPage() {
  return (
    <ModulePlaceholder
      title="Centre de Notifications"
      description="Historique des alertes, validations d’encadrant et rappels de délais de soutenance."
      phase="Planifié : Phase 4"
      icon={Bell}
      expectedFeatures={[
        'Notifications en temps réel des actions d’équipe',
        'Rappels configurables par email ou dans l’interface',
        'Marquage comme lu / tout effacer',
        'Filtres par type (Encadrant, Livrable, Tâche, Système)',
      ]}
    />
  )
}

export default NotificationsPage

