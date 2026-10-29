import {
  Calendar,
  Users,
  CheckSquare,
  FileText,
  Clock,
  FolderGit2,
} from 'lucide-react'
import { Modal, Button, Badge } from '../../../components/ui'

export function ProjectDetailModal({ isOpen, onClose, project, onEdit }) {
  if (!project) return null

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

  const formatDate = (dateStr) => {
    if (!dateStr) return 'Non définie'
    try {
      const date = new Date(dateStr)
      return date.toLocaleDateString('fr-FR', {
        day: '2-digit',
        month: 'long',
        year: 'numeric',
      })
    } catch {
      return dateStr
    }
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Détails du Projet PFE"
      description={`Identifiant unique : #${project.id}`}
      size="lg"
      footer={
        <div className="flex items-center justify-between w-full">
          <span className="text-xs text-slate-400">
            Dernière mise à jour : {formatDate(project.updatedAt || project.createdAt)}
          </span>
          <div className="flex items-center space-x-2">
            <Button variant="outline" size="sm" onClick={onClose}>
              Fermer
            </Button>
            {onEdit && (
              <Button
                variant="primary"
                size="sm"
                onClick={() => {
                  onClose()
                  onEdit(project)
                }}
              >
                Modifier le projet
              </Button>
            )}
          </div>
        </div>
      }
    >
      <div className="space-y-6">
        {/* En-tête : Titre & Statut */}
        <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3 p-4 rounded-xl bg-slate-50 border border-[#E2E8F0]">
          <div className="space-y-1">
            <div className="flex items-center space-x-2">
              <FolderGit2 className="w-5 h-5 text-[#1E3A8A]" />
              <h4 className="text-base sm:text-lg font-bold text-[#1E3A8A]">{project.title}</h4>
            </div>
            <p className="text-xs text-slate-500">
              Créé le {formatDate(project.createdAt)}
            </p>
          </div>
          <Badge variant={statusVariant(project.status)} size="md" dot>
            {project.status}
          </Badge>
        </div>

        {/* Description */}
        <div className="space-y-2">
          <h5 className="text-xs font-bold text-slate-700 uppercase tracking-wider">
            Description du sujet
          </h5>
          <div className="p-4 rounded-xl bg-white border border-[#E2E8F0] text-xs sm:text-sm text-slate-700 leading-relaxed">
            {project.description ? (
              <p className="whitespace-pre-line">{project.description}</p>
            ) : (
              <p className="text-slate-400 italic">Aucune description détaillée renseignée.</p>
            )}
          </div>
        </div>

        {/* Métriques et indicateurs clés */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div className="p-4 rounded-xl bg-white border border-[#E2E8F0] shadow-2xs space-y-1">
            <div className="flex items-center space-x-2 text-slate-500 text-xs">
              <Users className="w-4 h-4 text-[#1E3A8A]" />
              <span>Membres assignés</span>
            </div>
            <p className="text-xl font-bold text-slate-900">
              {project.membersCount ?? 0}{' '}
              <span className="text-xs font-normal text-slate-500">étudiant(s)</span>
            </p>
          </div>

          <div className="p-4 rounded-xl bg-white border border-[#E2E8F0] shadow-2xs space-y-1">
            <div className="flex items-center space-x-2 text-slate-500 text-xs">
              <CheckSquare className="w-4 h-4 text-emerald-600" />
              <span>Tâches associées</span>
            </div>
            <p className="text-xl font-bold text-slate-900">
              {project.tasksCount ?? 0}{' '}
              <span className="text-xs font-normal text-slate-500">tâche(s)</span>
            </p>
          </div>

          <div className="p-4 rounded-xl bg-white border border-[#E2E8F0] shadow-2xs space-y-1">
            <div className="flex items-center space-x-2 text-slate-500 text-xs">
              <FileText className="w-4 h-4 text-[#0DAD98]" />
              <span>Livrables déposés</span>
            </div>
            <p className="text-xl font-bold text-slate-900">
              {project.deliverablesCount ?? 0}{' '}
              <span className="text-xs font-normal text-slate-500">livrable(s)</span>
            </p>
          </div>
        </div>

        {/* Planning & Dates */}
        <div className="space-y-2">
          <h5 className="text-xs font-bold text-slate-700 uppercase tracking-wider">
            Planning prévisionnel
          </h5>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="flex items-center space-x-3 p-3 rounded-lg border border-[#E2E8F0] bg-slate-50/50">
              <Calendar className="w-4 h-4 text-slate-500" />
              <div>
                <span className="text-[11px] text-slate-500 block">Date de début</span>
                <span className="text-xs font-semibold text-slate-800">
                  {formatDate(project.startDate)}
                </span>
              </div>
            </div>

            <div className="flex items-center space-x-3 p-3 rounded-lg border border-[#E2E8F0] bg-slate-50/50">
              <Clock className="w-4 h-4 text-slate-500" />
              <div>
                <span className="text-[11px] text-slate-500 block">Date de fin prévue</span>
                <span className="text-xs font-semibold text-slate-800">
                  {formatDate(project.endDate)}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Modal>
  )
}

export default ProjectDetailModal

