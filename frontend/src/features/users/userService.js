import apiClient from '../../services/apiClient'

export const userService = {
  /**
   * Lister les utilisateurs avec pagination, filtre par rôle et recherche (q).
   * Réservé aux rôles ADMIN et SUPERVISOR côté Spring Security.
   */
  async getUsers({ role = null, q = '', page = 0, size = 10 } = {}) {
    const params = { page, size }

    if (role && role !== 'ALL') {
      params.role = role
    }

    if (q && q.trim()) {
      params.q = q.trim()
    }

    const response = await apiClient.get('/users', { params })
    return response.data
  },

  /**
   * Créer un nouvel utilisateur (réservé à l'ADMIN).
   */
  async createUser(userData) {
    const payload = {
      firstName: userData.firstName?.trim(),
      lastName: userData.lastName?.trim(),
      email: userData.email?.trim(),
      password: userData.password,
      role: userData.role,
    }

    const response = await apiClient.post('/users', payload)
    return response.data
  },

  /**
   * Modifier le rôle d'un utilisateur (réservé à l'ADMIN).
   */
  async updateUserRole(id, role) {
    const response = await apiClient.patch(`/users/${id}/role`, { role })
    return response.data
  },

  /**
   * Activer ou désactiver un compte utilisateur (réservé à l'ADMIN).
   */
  async updateUserActive(id, active) {
    const response = await apiClient.patch(`/users/${id}/active`, { active })
    return response.data
  },
}

export default userService
