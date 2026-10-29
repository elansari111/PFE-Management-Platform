import { Users } from 'lucide-react'
import { ModulePlaceholder } from '../../components/layout/ModulePlaceholder'

export function SupervisionPage() {
  return (
    <ModulePlaceholder
      title="Espace Encadrement PFE"
      description="Coordination entre étudiants, encadrants pédagogiques et tuteurs d’entreprise."
      phase="Planifié : Phase 4"
      icon={Users}
      expectedFeatures={[
        'Annuaire des enseignants encadrants et spécialités',
        'Planification des réunions de suivi et jalons de soutenance',
        'Fiches d’évaluation et grilles de notation',
        'Suivi du temps et journal de bord',
      ]}
    />
  )
}

export default SupervisionPage

