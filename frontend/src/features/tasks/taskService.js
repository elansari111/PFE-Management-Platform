import apiClient from '../../services/apiClient'

export const taskService = {
  /**
   * Lister les tâches visibles par l'utilisateur connecté.
   * Le backend filtre selon le rôle (un étudiant ne voit que les tâches de ses projets).
   */
  async getTasks({ projectId = null, status = null, assigneeId = null, page = 0, size = 200 } = {}) {
    const params = { page, size }
    if (projectId) params.projectId = projectId
    if (status) params.status = status
    if (assigneeId) params.assigneeId = assigneeId

    const response = await apiClient.get('/tasks', { params })
    return response.data
  },

  /**
   * Changer le statut d'une tâche (PATCH /tasks/{id}/status)
   */
  async updateTaskStatus(id, status) {
    const response = await apiClient.patch(`/tasks/${id}/status`, { status })
    return response.data
  },
}

export default taskService