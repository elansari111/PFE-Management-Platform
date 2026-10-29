import { Bot } from 'lucide-react'
import { ModulePlaceholder } from '../../components/layout/ModulePlaceholder'

export function AIAssistantPage() {
  return (
    <ModulePlaceholder
      title="AI Encadrant — Assistant Méthodologique Intelligent"
      description="Compagnon virtuel pour guider les étudiants dans la rédaction, la structure et le choix technologique de leur PFE."
      phase="Planifié : Phase 4"
      icon={Bot}
      expectedFeatures={[
        'Chatbot conversationnel contextuel au projet',
        'Assistance à la rédaction du plan de mémoire et abstract',
        'Recommandations de méthodologie Agile et bonnes pratiques DevOps',
        'Génération de suggestions de tâches et découpage en user stories',
        'Historique des échanges et annotations sauvegardées',
      ]}
    />
  )
}

export default AIAssistantPage
