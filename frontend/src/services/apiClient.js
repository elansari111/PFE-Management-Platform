import axios from 'axios'

/**
 * Configuration de l'URL de base :
 * Supporte soit http://localhost:8080 soit http://localhost:8080/api/v1
 */
const rawBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1'
const normalizedBaseUrl = rawBaseUrl.endsWith('/api/v1')
  ? rawBaseUrl
  : `${rawBaseUrl.replace(/\/+$/, '')}/api/v1`

export const apiClient = axios.create({
  baseURL: normalizedBaseUrl,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
})

// Intercepteur de requête : injection du token JWT si disponible
apiClient.interceptors.request.use(
  (config) => {
    try {
      if (typeof window !== 'undefined' && typeof window.localStorage !== 'undefined') {
        const token = window.localStorage.getItem('pfe_token')
        if (token) {
          config.headers.Authorization = `Bearer ${token}`
        }
      }
    } catch {
      // Ignorer si localStorage est inaccessible
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Intercepteur de réponse : formatage unifié des erreurs
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status || 500
    const requestUrl = error.config?.url || ''

    // Session expirée / non autorisée (401) hors tentative de connexion :
    // on purge la session locale puis on redirige vers la page de connexion.
    if (status === 401 && !requestUrl.includes('/auth/login')) {
      try {
        if (typeof window !== 'undefined' && typeof window.localStorage !== 'undefined') {
          window.localStorage.removeItem('pfe_token')
          window.localStorage.removeItem('pfe_user')
        }
        // Pas de navigation en environnement de test (Vitest / jsdom)
        if (
          typeof window !== 'undefined' &&
          import.meta.env.MODE !== 'test' &&
          window.location &&
          window.location.pathname !== '/login'
        ) {
          window.location.assign('/login')
        }
      } catch {
        // Ignorer si localStorage ou la navigation sont indisponibles
      }
    }

    const formattedError = {
      message: 'Une erreur inattendue est survenue.',
      status,
      timestamp: error.response?.data?.timestamp || new Date().toISOString(),
      validationErrors: error.response?.data?.errors || error.response?.data?.validationErrors || null,
      raw: error,
    }

    if (error.response?.data?.message) {
      formattedError.message = error.response.data.message
    } else if (error.code === 'ECONNABORTED') {
      formattedError.message = 'Délai d’attente dépassé (timeout).'
    } else if (!error.response) {
      formattedError.message = 'Impossible de contacter le serveur backend (vérifiez que Spring Boot est démarré).'
    }

    return Promise.reject(formattedError)
  }
)

export default apiClient

