import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import { ProjectsPage } from '../pages/projects'
import { DashboardPage } from '../pages/dashboard'
import { projectMockService, setMockMode } from '../mocks'
import { projectSchema } from '../features/projects/projectSchema'
import { ToastProvider } from '../components/ui/Toast'
import apiClient from '../services/apiClient'

describe('Phase 4 - Tests Fonctionnels & Qualité Frontend', () => {
  beforeEach(() => {
    setMockMode(true)
    projectMockService.reset()
  })

  afterEach(() => {
    setMockMode(null)
    vi.restoreAllMocks()
  })

  describe('Validation du Schéma Zod (projectSchema)', () => {
    it('valide un projet conforme', () => {
      const validData = {
        title: 'Système de gestion PFE intelligent',
        description: 'Description détaillée du projet',
        status: 'IN_PROGRESS',
        startDate: '2026-11-01',
        endDate: '2027-06-30',
      }
      const result = projectSchema.safeParse(validData)
      expect(result.success).toBe(true)
    })

    it('rejette un titre de moins de 3 caractères', () => {
      const invalidData = {
        title: 'AB',
        status: 'PROPOSED',
      }
      const result = projectSchema.safeParse(invalidData)
      expect(result.success).toBe(false)
      expect(result.error?.issues[0]?.message).toMatch(/au moins 3 caractères/i)
    })

    it('rejette une date de fin antérieure à la date de début', () => {
      const invalidDates = {
        title: 'Projet avec dates incohérentes',
        status: 'PROPOSED',
        startDate: '2027-01-01',
        endDate: '2026-01-01',
      }
      const result = projectSchema.safeParse(invalidDates)
      expect(result.success).toBe(false)
      expect(result.error?.issues[0]?.message).toMatch(
        /date de fin ne peut pas être antérieure/i
      )
    })
  })

  describe('Service Mock (projectMockService)', () => {
    it('récupère la liste paginée et applique le filtre par statut', async () => {
      const all = await projectMockService.getAllProjects({ size: 10 })
      expect(all.content.length).toBeGreaterThan(0)
      expect(all.totalElements).toBeGreaterThan(0)

      const inProgressOnly = await projectMockService.getAllProjects({
        status: 'IN_PROGRESS',
      })
      expect(inProgressOnly.content.every((p) => p.status === 'IN_PROGRESS')).toBe(true)
    })

    it('crée un projet et empêche les doublons de titre (409 Conflict)', async () => {
      const newProj = await projectMockService.createProject({
        title: 'Nouveau Sujet Unique 2026',
        description: 'Description du sujet unique',
        status: 'PROPOSED',
      })
      expect(newProj.id).toBeDefined()
      expect(newProj.title).toBe('Nouveau Sujet Unique 2026')

      // Tentative de doublon
      await expect(
        projectMockService.createProject({
          title: 'Nouveau Sujet Unique 2026',
        })
      ).rejects.toThrow(/existe déjà/i)
    })

    it('met à jour et supprime un projet existant', async () => {
      const updated = await projectMockService.updateProject(1, {
        title: 'Plateforme PFE - Titre Modifié',
        status: 'COMPLETED',
      })
      expect(updated.title).toBe('Plateforme PFE - Titre Modifié')
      expect(updated.status).toBe('COMPLETED')

      const deleted = await projectMockService.deleteProject(1)
      expect(deleted).toBe(true)

      await expect(projectMockService.getProjectById(1)).rejects.toThrow(/introuvable/i)
    })
  })

  describe('Page ProjectsPage (Interface, Filtres, Modales et Interactions)', () => {
    it('affiche la liste des projets et permet la bascule de vue (Tableau / Cartes)', async () => {
      render(
        <ToastProvider>
          <MemoryRouter>
            <ProjectsPage />
          </MemoryRouter>
        </ToastProvider>
      )

      await waitFor(() => {
        expect(
          screen.getByText('Plateforme PFE avec CI/CD, DevSecOps & AI Encadrant')
        ).toBeInTheDocument()
      })

      // Bascule vers la vue Cartes
      const cardsBtn = screen.getByLabelText(/Vue Cartes/i)
      fireEvent.click(cardsBtn)

      expect(
        screen.getByText('Plateforme PFE avec CI/CD, DevSecOps & AI Encadrant')
      ).toBeInTheDocument()

      // Retour en vue Tableau
      const tableBtn = screen.getByLabelText(/Vue Tableau/i)
      fireEvent.click(tableBtn)
      expect(screen.getByRole('table')).toBeInTheDocument()
    })

    it('filtre les projets via la barre de recherche textuelle', async () => {
      render(
        <ToastProvider>
          <MemoryRouter>
            <ProjectsPage />
          </MemoryRouter>
        </ToastProvider>
      )

      await waitFor(() => {
        expect(
          screen.getByText('Plateforme PFE avec CI/CD, DevSecOps & AI Encadrant')
        ).toBeInTheDocument()
      })

      const searchInput = screen.getByLabelText(/Recherche de projets/i)
      fireEvent.change(searchInput, { target: { value: 'IoT de Surveillance' } })

      await waitFor(() => {
        expect(
          screen.getByText('Système IoT de Surveillance Environnementale Urbaine')
        ).toBeInTheDocument()
        expect(
          screen.queryByText('Plateforme PFE avec CI/CD, DevSecOps & AI Encadrant')
        ).not.toBeInTheDocument()
      })
    })

    it('filtre les projets par statut via le sélecteur', async () => {
      render(
        <ToastProvider>
          <MemoryRouter>
            <ProjectsPage />
          </MemoryRouter>
        </ToastProvider>
      )

      await waitFor(() => {
        expect(
          screen.getByText('Plateforme PFE avec CI/CD, DevSecOps & AI Encadrant')
        ).toBeInTheDocument()
      })

      const statusSelect = screen.getByLabelText(/Filtrer par statut/i)
      fireEvent.change(statusSelect, { target: { value: 'COMPLETED' } })

      await waitFor(() => {
        expect(
          screen.getByText('Application Mobile de Télémédecine Sécurisée')
        ).toBeInTheDocument()
        expect(
          screen.queryByText('Plateforme PFE avec CI/CD, DevSecOps & AI Encadrant')
        ).not.toBeInTheDocument()
      })
    })

    it('ouvre la modale de détails et affiche les métriques associées', async () => {
      render(
        <ToastProvider>
          <MemoryRouter>
            <ProjectsPage />
          </MemoryRouter>
        </ToastProvider>
      )

      await waitFor(() => {
        expect(
          screen.getByText('Plateforme PFE avec CI/CD, DevSecOps & AI Encadrant')
        ).toBeInTheDocument()
      })

      const detailButtons = screen.getAllByLabelText(/Détails/i)
      fireEvent.click(detailButtons[0])

      await waitFor(() => {
        expect(screen.getByText(/Détails du Projet PFE/i)).toBeInTheDocument()
        expect(screen.getByText(/Membres assignés/i)).toBeInTheDocument()
      })

      // Fermeture de la modale via le bouton du footer
      const closeBtn = screen.getByRole('button', { name: /^Fermer$/i })
      fireEvent.click(closeBtn)

      await waitFor(() => {
        expect(screen.queryByText(/Détails du Projet PFE/i)).not.toBeInTheDocument()
      })
    })

    it('exécute un flux complet de création de projet via le formulaire modal', async () => {
      render(
        <ToastProvider>
          <MemoryRouter>
            <ProjectsPage />
          </MemoryRouter>
        </ToastProvider>
      )

      await waitFor(() => {
        expect(
          screen.getByText('Plateforme PFE avec CI/CD, DevSecOps & AI Encadrant')
        ).toBeInTheDocument()
      })

      // Clic sur "Nouveau Projet"
      const createBtn = screen.getByRole('button', { name: /Nouveau Projet/i })
      fireEvent.click(createBtn)

      // Vérification présence de la modale
      expect(screen.getByText(/Créer un Nouveau Projet PFE/i)).toBeInTheDocument()

      // Remplissage du champ titre
      const titleInput = screen.getByLabelText(/Titre du Projet/i)
      fireEvent.change(titleInput, { target: { value: 'Projet Robotique Autonome 2026' } })

      // Soumission
      const submitBtn = screen.getByRole('button', { name: /Créer le Projet/i })
      fireEvent.click(submitBtn)

      // Le nouveau projet doit être présent dans la liste
      await waitFor(() => {
        expect(screen.getByText('Projet Robotique Autonome 2026')).toBeInTheDocument()
      })
    })

    it('exécute une suppression confirmée via ConfirmDialog', async () => {
      render(
        <ToastProvider>
          <MemoryRouter>
            <ProjectsPage />
          </MemoryRouter>
        </ToastProvider>
      )

      await waitFor(() => {
        expect(
          screen.getByText('Plateforme PFE avec CI/CD, DevSecOps & AI Encadrant')
        ).toBeInTheDocument()
      })

      // Clic sur l'icône de suppression du premier projet
      const deleteButtons = screen.getAllByLabelText(/Supprimer/i)
      fireEvent.click(deleteButtons[0])

      // Vérification présence de la boîte de confirmation
      await waitFor(() => {
        expect(screen.getByText(/Supprimer définitivement le projet/i)).toBeInTheDocument()
      })

      // Confirmation de la suppression
      const confirmBtn = screen.getByRole('button', { name: /Supprimer le projet/i })
      fireEvent.click(confirmBtn)

      // Attente de la suppression du projet de la liste
      await waitFor(() => {
        expect(
          screen.queryByText('Plateforme PFE avec CI/CD, DevSecOps & AI Encadrant')
        ).not.toBeInTheDocument()
      })
    })

    it('gère les erreurs en mode API réel avec affichage de l’ErrorState', async () => {
      setMockMode(false)
      vi.spyOn(apiClient, 'get').mockRejectedValueOnce({
        message: 'Impossible de contacter le serveur backend.',
        status: 500,
      })

      render(
        <ToastProvider>
          <MemoryRouter>
            <ProjectsPage />
          </MemoryRouter>
        </ToastProvider>
      )

      await waitFor(() => {
        expect(
          screen.getByText(/Erreur lors de la récupération des projets/i)
        ).toBeInTheDocument()
        expect(screen.getByRole('button', { name: /Réessayer/i })).toBeInTheDocument()
      })
    })
  })

  describe('Page DashboardPage (Métriques & Graphique Recharts)', () => {
    it('affiche les 4 cartes statistiques calculées et les projets récents', async () => {
      render(
        <ToastProvider>
          <MemoryRouter>
            <DashboardPage />
          </MemoryRouter>
        </ToastProvider>
      )

      await waitFor(() => {
        expect(screen.getByText('Total Projets')).toBeInTheDocument()
        expect(screen.getByText('Projets En Cours')).toBeInTheDocument()
        expect(screen.getByText('Projets Validés / Finis')).toBeInTheDocument()
        expect(screen.getByText('En Attente / Proposés')).toBeInTheDocument()
      })

      // Vérification du conteneur Recharts
      expect(
        screen.getByText('Répartition des Projets par Statut')
      ).toBeInTheDocument()
    })
  })
})
