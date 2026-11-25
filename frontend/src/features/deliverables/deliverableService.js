import apiClient from '../../services/apiClient'

export const deliverableService = {
  /** Lister les livrables visibles par l'utilisateur (le backend filtre selon le rôle). */
  async getDeliverables({ projectId = null, status = null, page = 0, size = 100 } = {}) {
    const params = { page, size }
    if (projectId) params.projectId = projectId
    if (status) params.status = status
    const response = await apiClient.get('/deliverables', { params })
    return response.data
  },

  /** Déposer un livrable (multipart/form-data). */
  async upload({ projectId, type, file }) {
    const formData = new FormData()
    formData.append('projectId', String(projectId))
    formData.append('type', type)
    formData.append('file', file)
    const response = await apiClient.post('/deliverables', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000,
    })
    return response.data
  },

  /** Télécharger le fichier (retourne un Blob). */
  async download(id) {
    const response = await apiClient.get(`/deliverables/${id}/download`, {
      responseType: 'blob',
      timeout: 120000,
    })
    return response.data
  },

  /** Valider ou refuser un livrable (ADMIN / SUPERVISOR). */
  async review(id, { status, comment }) {
    const response = await apiClient.patch(`/deliverables/${id}/review`, { status, comment })
    return response.data
  },

  async remove(id) {
    await apiClient.delete(`/deliverables/${id}`)
  },
}

export default deliverableService