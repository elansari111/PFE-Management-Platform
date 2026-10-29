import { User } from 'lucide-react'
import { ModulePlaceholder } from '../../components/layout/ModulePlaceholder'

export function ProfilePage() {
  return (
    <ModulePlaceholder
      title="Profil Utilisateur"
      description="Gestion des informations personnelles, affiliation universitaire et préférences de compte."
      phase="Planifié : Phase 4"
      icon={User}
      expectedFeatures={[
        'Informations de l’étudiant ou de l’enseignant (nom, prénom, filière)',
        'Rôles attribués (STUDENT, SUPERVISOR, ADMIN)',
        'Gestion des identifiants et mot de passe',
        'Historique des projets associés',
      ]}
    />
  )
}

export default ProfilePage
