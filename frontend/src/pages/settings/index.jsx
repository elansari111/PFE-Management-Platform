import { Settings } from 'lucide-react'
import { ModulePlaceholder } from '../../components/layout/ModulePlaceholder'

export function SettingsPage() {
  return (
    <ModulePlaceholder
      title="Paramètres de la Plateforme"
      description="Préférences générales, intégration backend Spring Boot et configuration de l’affichage."
      phase="Planifié : Phase 4"
      icon={Settings}
      expectedFeatures={[
        'Configuration de l’URL API Backend (VITE_API_BASE_URL)',
        'Activation du mode démo / mocks (VITE_USE_MOCK)',
        'Préférence de thème (Mode clair actuel / futur mode sombre)',
        'Gestion des clés d’API pour les services IA',
      ]}
    />
  )
}

export default SettingsPage
