import { FileText } from 'lucide-react'
import { ModulePlaceholder } from '../../components/layout/ModulePlaceholder'

export function DeliverablesPage() {
  return (
    <ModulePlaceholder
      title="Suivi des Livrables Académiques"
      description="Dépôt des rapports, cahiers des charges et mémoires avec retours de l’encadrant."
      phase="Planifié : Phase 4"
      icon={FileText}
      expectedFeatures={[
        'Historique des versions de documents',
        'Statuts de validation (Soumis, En révision, Validé)',
        'Espace d’annotation et commentaires de l’encadrant',
        'Téléchargement sécurisé des fichiers',
      ]}
    />
  )
}

export default DeliverablesPage
