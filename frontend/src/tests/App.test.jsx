import { render, screen, waitFor } from '@testing-library/react'
import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { BrowserRouter } from 'react-router-dom'
import App from '../app/App'
import { setMockMode } from '../mocks'

describe('App Smoke Test - Phase 3', () => {
  beforeEach(() => {
    setMockMode(true)
    localStorage.setItem('pfe_token', 'mock-token-test')
    localStorage.setItem(
      'pfe_user',
      JSON.stringify({
        id: 1,
        firstName: 'Mohamed',
        lastName: 'El Amrani',
        email: 'mohamed@student.universite.ma',
        role: 'STUDENT',
      })
    )
  })

  afterEach(() => {
    setMockMode(null)
    localStorage.clear()
  })

  it('rend l’application avec le Layout SaaS, le tableau de bord et les métriques', async () => {
    render(
      <BrowserRouter>
        <App />
      </BrowserRouter>
    )

    // Vérification de la marque (présente dans Sidebar desktop + mobile drawer)
    const brandTitles = screen.getAllByText('PFE Platform')
    expect(brandTitles.length).toBeGreaterThan(0)

    // Vérification du message de bienvenue du Dashboard
    expect(screen.getByText(/Bonjour Mohamed/i)).toBeInTheDocument()

    // Vérification des 4 cartes statistiques calculées
    await waitFor(() => {
      expect(screen.getByText('Total Projets')).toBeInTheDocument()
      expect(screen.getByText('Projets En Cours')).toBeInTheDocument()
      expect(screen.getByText('Projets Validés / Finis')).toBeInTheDocument()
      expect(screen.getByText('En Attente / Proposés')).toBeInTheDocument()
    })
  })
})
