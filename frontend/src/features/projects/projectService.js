import apiClient from '../../services/apiClient'
import { isMockEnabled, projectMockService } from '../../mocks'

export const projectService = {
  /**
   * Lister les projets avec pagination et filtres
   */
  async getProjects({
    status = null,
    page = 0,
    size = 10,
    sortBy = 'createdAt',
    direction = 'DESC',
    search = '',
  } = {}) {
    if (isMockEnabled()) {
      return projectMockService.getAllProjects({
        status,
        page,
        size,
        sortBy,
        direction,
        search,
      })
    }

    const params = {
      page,
      size,
      sortBy,
      direction,
    }

    if (status && status !== 'ALL') {
      params.status = status
    }

    const response = await apiClient.get('/projects', { params })
    let data = response.data

    // Si une recherche textuelle est demandée et non prise en charge nativement par l'API
    if (search && search.trim() && Array.isArray(data.content)) {
      const q = search.trim().toLowerCase()
      const filtered = data.content.filter(
        (p) =>
          p.title?.toLowerCase().includes(q) ||
          p.description?.toLowerCase().includes(q)
      )
      data = {
        ...data,
        content: filtered,
        totalElements: filtered.length,
      }
    }

    return data
  },

  /**
   * Consulter un projet par son identifiant
   */
  async getProjectById(id) {
    if (isMockEnabled()) {
      return projectMockService.getProjectById(id)
    }

    const response = await apiClient.get(`/projects/${id}`)
    return response.data
  },

  /**
   * Créer un nouveau projet (requiert JWT côté Spring Boot)
   */
  async createProject(projectData) {
    if (isMockEnabled()) {
      return projectMockService.createProject(projectData)
    }

    const payload = {
      title: projectData.title?.trim(),
      description: projectData.description?.trim() || null,
      status: projectData.status || 'PROPOSED',
      startDate: projectData.startDate || null,
      endDate: projectData.endDate || null,
    }

    const response = await apiClient.post('/projects', payload)
    return response.data
  },

  /**
   * Modifier un projet existant
   */
  async updateProject(id, projectData) {
    if (isMockEnabled()) {
      return projectMockService.updateProject(id, projectData)
    }

    const payload = {
      title: projectData.title?.trim(),
      description: projectData.description?.trim() || null,
      status: projectData.status,
      startDate: projectData.startDate || null,
      endDate: projectData.endDate || null,
    }

    const response = await apiClient.put(`/projects/${id}`, payload)
    return response.data
  },

  /**
   * Supprimer un projet
   */
  async deleteProject(id) {
    if (isMockEnabled()) {
      return projectMockService.deleteProject(id)
    }

    await apiClient.delete(`/projects/${id}`)
    return true
  },

  /**
   * Calcule les métriques globales pour le tableau de bord
   */
  async getDashboardStats() {
    if (isMockEnabled()) {
      const all = await projectMockService.getAllProjects({ size: 100 })
      const list = all.content || []

      const total = all.totalElements || list.length
      const inProgress = list.filter((p) => p.status === 'IN_PROGRESS').length
      const approved = list.filter((p) => p.status === 'APPROVED').length
      const pending = list.filter((p) => p.status === 'PENDING').length
      const proposed = list.filter((p) => p.status === 'PROPOSED').length
      const completed = list.filter((p) => p.status === 'COMPLETED').length
      const archived = list.filter((p) => p.status === 'ARCHIVED').length

      const distribution = [
        { name: 'En cours', status: 'IN_PROGRESS', count: inProgress, color: '#1E3A8A' },
        { name: 'Validés', status: 'APPROVED', count: approved, color: '#0DAD98' },
        { name: 'En attente', status: 'PENDING', count: pending, color: '#F59E0B' },
        { name: 'Proposés', status: 'PROPOSED', count: proposed, color: '#64748B' },
        { name: 'Terminés', status: 'COMPLETED', count: completed, color: '#10B981' },
        { name: 'Archivés', status: 'ARCHIVED', count: archived, color: '#94A3B8' },
      ]

      return {
        total,
        inProgress,
        approved,
        pending,
        proposed,
        completed,
        archived,
        distribution,
        recentProjects: list.slice(0, 5),
        isMock: true,
      }
    }

    // Mode API Réel : récupération du premier lot
    try {
      const pageData = await this.getProjects({ page: 0, size: 50, sortBy: 'createdAt', direction: 'DESC' })
      const list = pageData.content || []
      const total = pageData.totalElements || list.length

      const inProgress = list.filter((p) => p.status === 'IN_PROGRESS').length
      const approved = list.filter((p) => p.status === 'APPROVED').length
      const pending = list.filter((p) => p.status === 'PENDING').length
      const proposed = list.filter((p) => p.status === 'PROPOSED').length
      const completed = list.filter((p) => p.status === 'COMPLETED').length
      const archived = list.filter((p) => p.status === 'ARCHIVED').length

      const distribution = [
        { name: 'En cours', status: 'IN_PROGRESS', count: inProgress, color: '#1E3A8A' },
        { name: 'Validés', status: 'APPROVED', count: approved, color: '#0DAD98' },
        { name: 'En attente', status: 'PENDING', count: pending, color: '#F59E0B' },
        { name: 'Proposés', status: 'PROPOSED', count: proposed, color: '#64748B' },
        { name: 'Terminés', status: 'COMPLETED', count: completed, color: '#10B981' },
        { name: 'Archivés', status: 'ARCHIVED', count: archived, color: '#94A3B8' },
      ]

      return {
        total,
        inProgress,
        approved,
        pending,
        proposed,
        completed,
        archived,
        distribution,
        recentProjects: list.slice(0, 5),
        isMock: false,
      }
    } catch {
      // En cas de backend éteint en mode API, retour de structure vide sans bloquer
      return {
        total: 0,
        inProgress: 0,
        approved: 0,
        pending: 0,
        proposed: 0,
        completed: 0,
        archived: 0,
        distribution: [],
        recentProjects: [],
        isMock: false,
        error: true,
      }
    }
  },
}

export default projectService

