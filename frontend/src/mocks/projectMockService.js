import { initialMockProjects } from './projectMockData'

// In-memory store initialized from initial data
let inMemoryProjects = [...initialMockProjects]

export const projectMockService = {
  /**
   * Récupère la liste paginée et filtrée des projets
   */
  async getAllProjects({
    status = null,
    page = 0,
    size = 10,
    sortBy = 'createdAt',
    direction = 'DESC',
    search = '',
  } = {}) {
    // Simulation d'une latence réseau réaliste de 150ms
    await new Promise((resolve) => setTimeout(resolve, 150))

    let filtered = [...inMemoryProjects]

    // Filtre par statut
    if (status && status !== 'ALL') {
      filtered = filtered.filter((p) => p.status === status)
    }

    // Recherche par titre ou description
    if (search && search.trim()) {
      const q = search.trim().toLowerCase()
      filtered = filtered.filter(
        (p) =>
          p.title.toLowerCase().includes(q) ||
          (p.description && p.description.toLowerCase().includes(q))
      )
    }

    // Tri
    filtered.sort((a, b) => {
      let valA = a[sortBy] ?? ''
      let valB = b[sortBy] ?? ''

      if (typeof valA === 'string') valA = valA.toLowerCase()
      if (typeof valB === 'string') valB = valB.toLowerCase()

      if (valA < valB) return direction.toUpperCase() === 'ASC' ? -1 : 1
      if (valA > valB) return direction.toUpperCase() === 'ASC' ? 1 : -1
      return 0
    })

    const totalElements = filtered.length
    const totalPages = Math.ceil(totalElements / size) || 1
    const startIndex = page * size
    const content = filtered.slice(startIndex, startIndex + size)

    return {
      content,
      pageNumber: page,
      pageSize: size,
      totalElements,
      totalPages,
      first: page === 0,
      last: page >= totalPages - 1,
    }
  },

  /**
   * Récupère un projet par son ID
   */
  async getProjectById(id) {
    await new Promise((resolve) => setTimeout(resolve, 100))
    const numericId = Number(id)
    const project = inMemoryProjects.find((p) => p.id === numericId)
    if (!project) {
      const error = new Error(`Projet introuvable avec l'identifiant: ${id}`)
      error.status = 404
      throw error
    }
    return { ...project }
  },

  /**
   * Crée un nouveau projet
   */
  async createProject(request) {
    await new Promise((resolve) => setTimeout(resolve, 200))

    // Vérification de doublon de titre (comme en Spring Boot)
    const normalizedTitle = request.title.trim().toLowerCase()
    const exists = inMemoryProjects.some((p) => p.title.trim().toLowerCase() === normalizedTitle)
    if (exists) {
      const error = new Error(`Un projet portant le titre '${request.title}' existe déjà`)
      error.status = 409
      throw error
    }

    // Validation des dates
    if (request.startDate && request.endDate) {
      if (new Date(request.endDate) < new Date(request.startDate)) {
        const error = new Error('La date de fin ne peut pas être antérieure à la date de début')
        error.status = 400
        throw error
      }
    }

    const nextId = inMemoryProjects.length > 0 ? Math.max(...inMemoryProjects.map((p) => p.id)) + 1 : 1
    const now = new Date().toISOString()

    const newProject = {
      id: nextId,
      title: request.title.trim(),
      description: request.description ? request.description.trim() : '',
      status: request.status || 'PROPOSED',
      startDate: request.startDate || null,
      endDate: request.endDate || null,
      membersCount: 1, // Le créateur est membre par défaut
      tasksCount: 0,
      deliverablesCount: 0,
      createdAt: now,
      updatedAt: now,
    }

    inMemoryProjects = [newProject, ...inMemoryProjects]
    return { ...newProject }
  },

  /**
   * Met à jour un projet existant
   */
  async updateProject(id, request) {
    await new Promise((resolve) => setTimeout(resolve, 200))
    const numericId = Number(id)
    const index = inMemoryProjects.findIndex((p) => p.id === numericId)
    if (index === -1) {
      const error = new Error(`Projet introuvable avec l'identifiant: ${id}`)
      error.status = 404
      throw error
    }

    const currentProject = inMemoryProjects[index]

    // Vérification de doublon si le titre a changé
    const normalizedTitle = request.title.trim().toLowerCase()
    const duplicate = inMemoryProjects.some(
      (p) => p.id !== numericId && p.title.trim().toLowerCase() === normalizedTitle
    )
    if (duplicate) {
      const error = new Error(`Un projet portant le titre '${request.title}' existe déjà`)
      error.status = 409
      throw error
    }

    // Validation des dates
    if (request.startDate && request.endDate) {
      if (new Date(request.endDate) < new Date(request.startDate)) {
        const error = new Error('La date de fin ne peut pas être antérieure à la date de début')
        error.status = 400
        throw error
      }
    }

    const updated = {
      ...currentProject,
      title: request.title.trim(),
      description: request.description !== undefined ? request.description.trim() : currentProject.description,
      status: request.status || currentProject.status,
      startDate: request.startDate || currentProject.startDate,
      endDate: request.endDate || currentProject.endDate,
      updatedAt: new Date().toISOString(),
    }

    inMemoryProjects[index] = updated
    return { ...updated }
  },

  /**
   * Supprime un projet par son ID
   */
  async deleteProject(id) {
    await new Promise((resolve) => setTimeout(resolve, 150))
    const numericId = Number(id)
    const index = inMemoryProjects.findIndex((p) => p.id === numericId)
    if (index === -1) {
      const error = new Error(`Projet introuvable avec l'identifiant: ${id}`)
      error.status = 404
      throw error
    }
    inMemoryProjects = inMemoryProjects.filter((p) => p.id !== numericId)
    return true
  },

  /**
   * Réinitialise les données mock à leur état initial (utile pour les tests)
   */
  reset() {
    inMemoryProjects = [...initialMockProjects]
  },
}

export default projectMockService

