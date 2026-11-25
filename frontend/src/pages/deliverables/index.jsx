import { useState, useEffect, useCallback, useRef } from 'react'
import { RefreshCw, Upload, Download, Trash2, FileText, ClipboardCheck } from 'lucide-react'
import {
  Button,
  Badge,
  Select,
  Modal,
  ConfirmDialog,
  Skeleton,
  EmptyState,
  ErrorState,
  useToast,
} from '../../components/ui'
import { deliverableService } from '../../features/deliverables'
import { projectService } from '../../features/projects'
import { useAuth } from '../../context/useAuth'

const MAX_FILE_SIZE = 20 * 1024 * 1024
const ALLOWED_EXTENSIONS = ['pdf', 'docx', 'pptx', 'zip', 'md', 'txt']

const TYPE_OPTIONS = [
  { value: 'CAHIER_DES_CHARGES', label: 'Cahier des charges' },
  { value: 'RAPPORT_INTERMEDIAIRE', label: 'Rapport intermédiaire' },
  { value: 'RAPPORT_FINAL', label: 'Rapport final' },
  { value: 'PRESENTATION', label: 'Présentation' },
  { value: 'AUTRE', label: 'Autre' },
]

const STATUS_META = {
  SUBMITTED: { label: 'Soumis', variant: 'primary' },
  APPROVED: { label: 'Validé', variant: 'success' },
  REJECTED: { label: 'Refusé', variant: 'danger' },
}

const typeLabel = (type) => TYPE_OPTIONS.find((t) => t.value === type)?.label || type

function formatSize(bytes) {
  if (!bytes && bytes !== 0) return ''
  if (bytes < 1024) return `${bytes} o`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} Ko`
  return `${(bytes / (1024 * 1024)).toFixed(1)} Mo`
}

function formatDate(value) {
  if (!value) return ''
  const d = new Date(value)
  return Number.isNaN(d.getTime()) ? '' : d.toLocaleString('fr-FR')
}

function validateFile(file) {
  if (!file) return 'Veuillez choisir un fichier.'
  const ext = file.name.includes('.') ? file.name.split('.').pop().toLowerCase() : ''
  if (!ALLOWED_EXTENSIONS.includes(ext)) {
    return `Extension non autorisée. Formats acceptés : ${ALLOWED_EXTENSIONS.join(', ')}.`
  }
  if (file.size > MAX_FILE_SIZE) return 'Fichier trop volumineux (maximum 20 Mo).'
  if (file.size === 0) return 'Le fichier est vide.'
  return null
}

function saveBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename || 'livrable'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

export function DeliverablesPage() {
  const toast = useToast()
  const auth = useAuth()
  const user = auth?.user
  const role = user?.role

  const canUpload = role === 'STUDENT' || role === 'ADMIN'
  const canReview = role === 'ADMIN' || role === 'SUPERVISOR'

  const [items, setItems] = useState([])
  const [projects, setProjects] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)

  // Formulaire de dépôt
  const [projectId, setProjectId] = useState('')
  const [type, setType] = useState('RAPPORT_FINAL')
  const [file, setFile] = useState(null)
  const [fileError, setFileError] = useState(null)
  const [isUploading, setIsUploading] = useState(false)
  const fileInputRef = useRef(null)

  // Révision
  const [reviewTarget, setReviewTarget] = useState(null)
  const [reviewStatus, setReviewStatus] = useState('APPROVED')
  const [reviewComment, setReviewComment] = useState('')
  const [isReviewing, setIsReviewing] = useState(false)

  // Suppression
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [isDeleting, setIsDeleting] = useState(false)

  const [downloadingId, setDownloadingId] = useState(null)

  const isMountedRef = useRef(true)
  useEffect(() => {
    isMountedRef.current = true
    return () => {
      isMountedRef.current = false
    }
  }, [])

  const loadDeliverables = useCallback(async () => {
    setIsLoading(true)
    setError(null)
    try {
      const result = await deliverableService.getDeliverables()
      if (!isMountedRef.current) return
      setItems(result?.content || [])
    } catch (err) {
      if (!isMountedRef.current) return
      setError(err.message || 'Impossible de charger les livrables.')
    } finally {
      if (isMountedRef.current) setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    loadDeliverables()
  }, [loadDeliverables])

  useEffect(() => {
    let cancelled = false
    projectService
      .getProjects({ size: 100 })
      .then((data) => {
        if (!cancelled) setProjects(data?.content || [])
      })
      .catch(() => {
        if (!cancelled) setProjects([])
      })
    return () => {
      cancelled = true
    }
  }, [])

  const handleFileChange = (e) => {
    const selected = e.target.files?.[0] || null
    setFile(selected)
    setFileError(selected ? validateFile(selected) : null)
  }

  const handleUpload = async (e) => {
    e.preventDefault()
    if (!projectId) {
      setFileError('Veuillez choisir un projet.')
      return
    }
    const problem = validateFile(file)
    if (problem) {
      setFileError(problem)
      return
    }
    setIsUploading(true)
    try {
      const created = await deliverableService.upload({ projectId: Number(projectId), type, file })
      if (!isMountedRef.current) return
      toast.success('Livrable déposé', `Version ${created?.version ?? ''} enregistrée.`)
      setFile(null)
      setFileError(null)
      if (fileInputRef.current) fileInputRef.current.value = ''
      await loadDeliverables()
    } catch (err) {
      if (!isMountedRef.current) return
      toast.error('Dépôt refusé', err.message || 'Impossible de déposer le fichier.')
    } finally {
      if (isMountedRef.current) setIsUploading(false)
    }
  }

  const handleDownload = async (item) => {
    setDownloadingId(item.id)
    try {
      const blob = await deliverableService.download(item.id)
      saveBlob(blob, item.originalFilename)
    } catch (err) {
      const message =
        err.status === 403
          ? "Vous n'avez pas accès à ce fichier."
          : err.status === 404
            ? 'Fichier introuvable sur le serveur.'
            : err.message || 'Téléchargement impossible.'
      toast.error('Téléchargement impossible', message)
    } finally {
      if (isMountedRef.current) setDownloadingId(null)
    }
  }

  const openReview = (item) => {
    setReviewTarget(item)
    setReviewStatus('APPROVED')
    setReviewComment('')
  }

  const handleReview = async () => {
    if (reviewStatus === 'REJECTED' && !reviewComment.trim()) {
      toast.error('Commentaire obligatoire', 'Merci de justifier le refus.')
      return
    }
    setIsReviewing(true)
    try {
      await deliverableService.review(reviewTarget.id, {
        status: reviewStatus,
        comment: reviewComment.trim() || null,
      })
      if (!isMountedRef.current) return
      toast.success('Livrable évalué', reviewStatus === 'APPROVED' ? 'Livrable validé.' : 'Livrable refusé.')
      setReviewTarget(null)
      await loadDeliverables()
    } catch (err) {
      if (!isMountedRef.current) return
      toast.error('Évaluation refusée', err.message || "Impossible d'évaluer ce livrable.")
    } finally {
      if (isMountedRef.current) setIsReviewing(false)
    }
  }

  const handleDelete = async () => {
    setIsDeleting(true)
    try {
      await deliverableService.remove(deleteTarget.id)
      if (!isMountedRef.current) return
      toast.success('Livrable supprimé', deleteTarget.originalFilename)
      setDeleteTarget(null)
      await loadDeliverables()
    } catch (err) {
      if (!isMountedRef.current) return
      toast.error('Suppression refusée', err.message || 'Impossible de supprimer ce livrable.')
    } finally {
      if (isMountedRef.current) setIsDeleting(false)
    }
  }

  const canDelete = (item) =>
    role === 'ADMIN' || (item.createdBy?.id === user?.id && item.status === 'SUBMITTED')

  const projectOptions = projects.map((p) => ({ value: String(p.id), label: p.title }))

  return (
    <div className="space-y-6 animate-in fade-in duration-200">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <h1 className="text-2xl font-extrabold text-[#1E3A8A] tracking-tight">Livrables</h1>
            <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-blue-100 text-[#1E3A8A]">
              {items.length} au total
            </span>
          </div>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Dépôt, versions et validation des livrables de vos projets (20 Mo max : pdf, docx, pptx, zip, md, txt).
          </p>
        </div>
        <Button
          variant="outline"
          size="sm"
          onClick={loadDeliverables}
          leftIcon={<RefreshCw className={`w-3.5 h-3.5 ${isLoading ? 'animate-spin' : ''}`} />}
        >
          Actualiser
        </Button>
      </div>

      {canUpload && (
        <form
          onSubmit={handleUpload}
          aria-label="Formulaire de dépôt"
          className="bg-white border border-[#E2E8F0] rounded-xl p-4 sm:p-5 space-y-4"
        >
          <h2 className="text-sm font-bold text-slate-700">Déposer un livrable</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
            <Select
              label="Projet"
              name="projectId"
              value={projectId}
              onChange={(e) => setProjectId(e.target.value)}
              placeholder="Choisir un projet"
              options={projectOptions}
            />
            <Select
              label="Type de livrable"
              name="type"
              value={type}
              onChange={(e) => setType(e.target.value)}
              options={TYPE_OPTIONS}
            />
            <div className="space-y-1.5">
              <label htmlFor="deliverable-file" className="block text-xs font-semibold text-slate-700 tracking-wide">
                Fichier
              </label>
              <input
                id="deliverable-file"
                ref={fileInputRef}
                type="file"
                aria-label="Fichier à déposer"
                accept=".pdf,.docx,.pptx,.zip,.md,.txt"
                onChange={handleFileChange}
                className="block w-full text-xs text-slate-600 file:mr-3 file:rounded-lg file:border-0 file:bg-slate-100 file:px-3 file:py-2 file:text-xs file:font-medium hover:file:bg-slate-200"
              />
            </div>
          </div>
          {fileError && (
            <p role="alert" className="text-xs text-rose-600 font-medium">
              {fileError}
            </p>
          )}
          <div className="flex justify-end">
            <Button
              type="submit"
              isLoading={isUploading}
              disabled={!file || Boolean(fileError)}
              leftIcon={<Upload className="w-4 h-4" />}
            >
              Déposer le livrable
            </Button>
          </div>
        </form>
      )}

      {error ? (
        <ErrorState title="Chargement impossible" message={error} onRetry={loadDeliverables} />
      ) : isLoading ? (
        <div className="space-y-3" data-testid="deliverables-loading">
          <Skeleton className="h-16 rounded-xl" />
          <Skeleton className="h-16 rounded-xl" />
          <Skeleton className="h-16 rounded-xl" />
        </div>
      ) : items.length === 0 ? (
        <EmptyState
          icon={FileText}
          title="Aucun livrable"
          description="Aucun livrable n'a encore été déposé dans vos projets."
        />
      ) : (
        <div className="bg-white border border-[#E2E8F0] rounded-xl overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-xs text-slate-500 uppercase tracking-wide">
              <tr>
                <th className="text-left px-4 py-3">Fichier</th>
                <th className="text-left px-4 py-3">Type</th>
                <th className="text-left px-4 py-3">Projet</th>
                <th className="text-left px-4 py-3">Déposé par</th>
                <th className="text-left px-4 py-3">Statut</th>
                <th className="text-right px-4 py-3">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#E2E8F0]">
              {items.map((item) => {
                const meta = STATUS_META[item.status] || { label: item.status, variant: 'default' }
                return (
                  <tr key={item.id} className="align-top">
                    <td className="px-4 py-3">
                      <div className="font-semibold text-slate-800">{item.originalFilename}</div>
                      <div className="text-[11px] text-slate-500">
                        v{item.version} · {formatSize(item.fileSize)} · {formatDate(item.submissionDate)}
                      </div>
                      {item.comment && (
                        <div className="mt-1 text-[11px] text-slate-600 italic">« {item.comment} »</div>
                      )}
                    </td>
                    <td className="px-4 py-3 text-slate-700">{typeLabel(item.type)}</td>
                    <td className="px-4 py-3 text-slate-700">{item.projectTitle || `Projet #${item.projectId}`}</td>
                    <td className="px-4 py-3 text-slate-700">{item.createdBy?.fullName || '—'}</td>
                    <td className="px-4 py-3">
                      <Badge variant={meta.variant} size="sm">
                        {meta.label}
                      </Badge>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          aria-label={`Télécharger ${item.originalFilename}`}
                          isLoading={downloadingId === item.id}
                          onClick={() => handleDownload(item)}
                          leftIcon={<Download className="w-3.5 h-3.5" />}
                        >
                          Télécharger
                        </Button>
                        {canReview && (
                          <Button
                            variant="accent"
                            size="sm"
                            aria-label={`Évaluer ${item.originalFilename}`}
                            onClick={() => openReview(item)}
                            leftIcon={<ClipboardCheck className="w-3.5 h-3.5" />}
                          >
                            Évaluer
                          </Button>
                        )}
                        {canDelete(item) && (
                          <Button
                            variant="ghost"
                            size="sm"
                            aria-label={`Supprimer ${item.originalFilename}`}
                            onClick={() => setDeleteTarget(item)}
                            leftIcon={<Trash2 className="w-3.5 h-3.5" />}
                          >
                            Supprimer
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}

      <Modal
        isOpen={Boolean(reviewTarget)}
        onClose={() => setReviewTarget(null)}
        title="Évaluer le livrable"
        description={reviewTarget?.originalFilename}
        footer={
          <>
            <Button variant="outline" size="sm" onClick={() => setReviewTarget(null)} disabled={isReviewing}>
              Annuler
            </Button>
            <Button size="sm" onClick={handleReview} isLoading={isReviewing}>
              Enregistrer
            </Button>
          </>
        }
      >
        <div className="space-y-4">
          <Select
            label="Décision"
            name="reviewStatus"
            value={reviewStatus}
            onChange={(e) => setReviewStatus(e.target.value)}
            options={[
              { value: 'APPROVED', label: 'Valider' },
              { value: 'REJECTED', label: 'Refuser' },
            ]}
          />
          <div className="space-y-1.5">
            <label htmlFor="review-comment" className="block text-xs font-semibold text-slate-700 tracking-wide">
              Commentaire {reviewStatus === 'REJECTED' ? '(obligatoire)' : '(optionnel)'}
            </label>
            <textarea
              id="review-comment"
              rows={4}
              value={reviewComment}
              onChange={(e) => setReviewComment(e.target.value)}
              className="block w-full rounded-lg border border-[#E2E8F0] p-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#1E3A8A]/30"
            />
          </div>
        </div>
      </Modal>

      <ConfirmDialog
        isOpen={Boolean(deleteTarget)}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        isLoading={isDeleting}
        title="Supprimer ce livrable ?"
        message={`« ${deleteTarget?.originalFilename || ''} » sera définitivement supprimé.`}
        confirmText="Supprimer"
      />
    </div>
  )
}

export default DeliverablesPage