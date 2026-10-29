/**
 * Constantes de l'application alignées sur les contrats du Backend Spring Boot
 */

// Rôles utilisateurs (cf. Role.java)
export const ROLES = {
  ADMIN: 'ADMIN',
  STUDENT: 'STUDENT',
  SUPERVISOR: 'SUPERVISOR',
}

// Statuts des projets (cf. ProjectStatus.java)
export const PROJECT_STATUS = {
  PROPOSED: 'PROPOSED',
  PENDING: 'PENDING',
  APPROVED: 'APPROVED',
  IN_PROGRESS: 'IN_PROGRESS',
  COMPLETED: 'COMPLETED',
  ARCHIVED: 'ARCHIVED',
}

// Statuts des tâches (cf. TaskStatus.java)
export const TASK_STATUS = {
  TODO: 'TODO',
  IN_PROGRESS: 'IN_PROGRESS',
  DONE: 'DONE',
  BLOCKED: 'BLOCKED',
}

// Priorités des tâches (cf. TaskPriority.java)
export const TASK_PRIORITY = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
  URGENT: 'URGENT',
}

// Palette de couleurs de la charte graphique
export const THEME_COLORS = {
  background: '#F8FAFC',
  surface: '#FFFFFF',
  border: '#E2E8F0',
  primary: '#1E3A8A',
  accent: '#0DAD98',
  text: '#0F172A',
}

