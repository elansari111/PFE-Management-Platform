import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { DeliverablesPage } from '../pages/deliverables'
import { ToastProvider } from '../components/ui/Toast'
import { deliverableService } from '../features/deliverables'
import { projectService } from '../features/projects'
import { useAuth } from '../context/useAuth'

vi.mock('../features/deliverables', () => ({
  deliverableService: {
    getDeliverables: vi.fn(),
    upload: vi.fn(),
    download: vi.fn(),
    review: vi.fn(),
    remove: vi.fn(),
  },
}))

vi.mock('../features/projects', () => ({
  projectService: { getProjects: vi.fn() },
}))

vi.mock('../context/useAuth', () => ({
  useAuth: vi.fn(),
  default: vi.fn(),
}))

const sample = {
  content: [
    {
      id: 10,
      type: 'RAPPORT_FINAL',
      version: 2,
      status: 'SUBMITTED',
      originalFilename: 'rapport.pdf',
      fileSize: 2048,
      projectId: 1,
      projectTitle: 'Plateforme PFE',
      createdBy: { id: 3, fullName: 'Alice Student' },
      submissionDate: '2026-11-25T10:00:00',
    },
  ],
  totalElements: 1,
}

const renderPage = () =>
  render(
    <ToastProvider>
      <DeliverablesPage />
    </ToastProvider>
  )

describe('Page Livrables', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    projectService.getProjects.mockResolvedValue({ content: [{ id: 1, title: 'Plateforme PFE' }] })
    useAuth.mockReturnValue({ user: { id: 3, role: 'STUDENT' } })
  })

  it('affiche la liste des livrables avec la version', async () => {
    deliverableService.getDeliverables.mockResolvedValue(sample)
    renderPage()

    expect(await screen.findByText('rapport.pdf')).toBeInTheDocument()
    expect(screen.getByText(/v2/)).toBeInTheDocument()
    expect(screen.getByText('Soumis')).toBeInTheDocument()
  })

  it("affiche un état vide quand aucun livrable n'existe", async () => {
    deliverableService.getDeliverables.mockResolvedValue({ content: [], totalElements: 0 })
    renderPage()

    expect(await screen.findByText('Aucun livrable')).toBeInTheDocument()
  })

  it('dépose un fichier valide via le formulaire', async () => {
    deliverableService.getDeliverables.mockResolvedValue({ content: [], totalElements: 0 })
    deliverableService.upload.mockResolvedValue({ id: 11, version: 1 })
    renderPage()

    await screen.findByText('Aucun livrable')
    await screen.findByRole('option', { name: 'Plateforme PFE' })

    fireEvent.change(screen.getByLabelText('Projet'), { target: { value: '1' } })
    const file = new File(['contenu'], 'rapport.pdf', { type: 'application/pdf' })
    fireEvent.change(screen.getByLabelText('Fichier à déposer'), { target: { files: [file] } })
    fireEvent.click(screen.getByRole('button', { name: 'Déposer le livrable' }))

    await waitFor(() => {
      expect(deliverableService.upload).toHaveBeenCalledWith({
        projectId: 1,
        type: 'RAPPORT_FINAL',
        file,
      })
    })
  })

  it('refuse une extension interdite côté client', async () => {
    deliverableService.getDeliverables.mockResolvedValue({ content: [], totalElements: 0 })
    renderPage()

    await screen.findByText('Aucun livrable')
    const bad = new File(['x'], 'virus.exe', { type: 'application/octet-stream' })
    fireEvent.change(screen.getByLabelText('Fichier à déposer'), { target: { files: [bad] } })

    expect(await screen.findByText(/Extension non autorisée/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Déposer le livrable' })).toBeDisabled()
    expect(deliverableService.upload).not.toHaveBeenCalled()
  })

  it("un encadrant voit « Évaluer » mais pas le formulaire de dépôt", async () => {
    useAuth.mockReturnValue({ user: { id: 2, role: 'SUPERVISOR' } })
    deliverableService.getDeliverables.mockResolvedValue(sample)
    renderPage()

    expect(await screen.findByRole('button', { name: 'Évaluer rapport.pdf' })).toBeInTheDocument()
    expect(screen.queryByLabelText('Fichier à déposer')).not.toBeInTheDocument()
  })
})