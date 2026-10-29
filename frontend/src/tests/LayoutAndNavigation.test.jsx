import { render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { MemoryRouter } from 'react-router-dom'
import { AppRoutes } from '../routes'
import { setMockMode } from '../mocks'

describe('Layout and Navigation System - Phase 3', () => {
  beforeEach(() => {
    setMockMode(true)
  })

  afterEach(() => {
    setMockMode(null)
  })

  it('rend le Layout principal et navigue vers le tableau de bord par défaut', async () => {
    render(
      <MemoryRouter initialEntries={['/']}>
        <AppRoutes />
      </MemoryRouter>
    )

    // Vérification de la marque (desktop & mobile drawer)
    const brandElements = screen.getAllByText('PFE Platform')
    expect(brandElements.length).toBeGreaterThan(0)

    // Vérification des liens de navigation principaux
    const dashboardLinks = screen.getAllByText('Tableau de bord')
    expect(dashboardLinks.length).toBeGreaterThan(0)

    const projectLinks = screen.getAllByText('Projets')
    expect(projectLinks.length).toBeGreaterThan(0)

    const taskLinks = screen.getAllByText('Tâches')
    expect(taskLinks.length).toBeGreaterThan(0)

    const deliverableLinks = screen.getAllByText('Livrables')
    expect(deliverableLinks.length).toBeGreaterThan(0)

    const aiLinks = screen.getAllByText('AI Encadrant')
    expect(aiLinks.length).toBeGreaterThan(0)

    // Vérification du contenu du Dashboard
    expect(screen.getByText(/Bonjour Mohamed/i)).toBeInTheDocument()

    await waitFor(() => {
      expect(screen.getByText('Total Projets')).toBeInTheDocument()
      expect(screen.getByText('Projets En Cours')).toBeInTheDocument()
      expect(screen.getByText('Projets Validés / Finis')).toBeInTheDocument()
      expect(screen.getByText('En Attente / Proposés')).toBeInTheDocument()
    })
  })

  it('affiche la page Projets complète avec la liste des sujets', async () => {
    render(
      <MemoryRouter initialEntries={['/projects']}>
        <AppRoutes />
      </MemoryRouter>
    )

    await waitFor(() => {
      expect(screen.getByText('Gestion des Projets PFE')).toBeInTheDocument()
      expect(screen.getByRole('button', { name: /Nouveau Projet/i })).toBeInTheDocument()
    })
  })

  it('affiche la page 404 lors de la navigation vers une URL inconnue', () => {
    render(
      <MemoryRouter initialEntries={['/une-route-inexistante']}>
        <AppRoutes />
      </MemoryRouter>
    )

    expect(screen.getByText('Erreur 404')).toBeInTheDocument()
    expect(screen.getByText('Page Introuvable')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Retour au Tableau de bord/i })).toBeInTheDocument()
  })
})
