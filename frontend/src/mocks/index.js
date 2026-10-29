import { projectMockService } from './projectMockService'
import { initialMockProjects } from './projectMockData'

let mockOverride = null

/**
 * Permet de forcer temporairement le mode mock ou API réelle (utile dans les tests automatisés)
 */
export const setMockMode = (enabled) => {
  mockOverride = enabled
}

/**
 * Mécanisme explicite de sélection du mode Mock ou API Spring Boot réelle.
 * Priorités :
 * 1. Valeur surchargée manuellement (setMockMode)
 * 2. Variable d'environnement VITE_USE_MOCK === 'true'
 * 3. Environnement de test Vitest (MODE === 'test') par défaut pour isoler les tests
 */
export const isMockEnabled = () => {
  if (mockOverride !== null) {
    return mockOverride
  }
  if (import.meta.env.VITE_USE_MOCK !== undefined) {
    return import.meta.env.VITE_USE_MOCK === 'true'
  }
  return import.meta.env.MODE === 'test'
}

export const mockNotice = {
  active: isMockEnabled(),
  disclaimer:
    'Mode démonstration actif — Les opérations sont simulées en mémoire locale et ne sont pas persistées dans la base MySQL.',
}

export { projectMockService, initialMockProjects }
