import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { TasksPage } from '../pages/tasks'
import { ToastProvider } from '../components/ui/Toast'
import { taskService } from '../features/tasks'

vi.mock('../features/tasks', () => ({
  taskService: {
    getTasks: vi.fn(),
    updateTaskStatus: vi.fn(),
  },
}))

const sampleTasks = {
  content: [
    {
      id: 1,
      title: 'Concevoir le schema de la base',
      description: 'Diagramme UML',
      status: 'IN_PROGRESS',
      priority: 'HIGH',
      dueDate: '2099-12-15',
      projectId: 1,
      projectTitle: 'Plateforme PFE',
      assignedTo: { id: 3, firstName: 'Salma', lastName: 'Test' },
    },
    {
      id: 2,
      title: 'Preparer la soutenance',
      status: 'TODO',
      priority: 'LOW',
      projectId: 1,
    },
  ],
  totalElements: 2,
}

const renderPage = () =>
  render(
    <ToastProvider>
      <TasksPage />
    </ToastProvider>
  )

describe('Page Tâches (Kanban)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('affiche les tâches dans les bonnes colonnes', async () => {
    taskService.getTasks.mockResolvedValue(sampleTasks)
    renderPage()

    expect(await screen.findByText('Concevoir le schema de la base')).toBeInTheDocument()
    expect(screen.getByText('Preparer la soutenance')).toBeInTheDocument()
    expect(screen.getByText('Salma Test')).toBeInTheDocument()

    const enCours = screen.getByRole('region', { name: 'Colonne En cours' })
    expect(enCours).toHaveTextContent('Concevoir le schema de la base')
    const aFaire = screen.getByRole('region', { name: 'Colonne À faire' })
    expect(aFaire).toHaveTextContent('Preparer la soutenance')
  })

  it("affiche un état vide quand aucune tâche n'existe", async () => {
    taskService.getTasks.mockResolvedValue({ content: [], totalElements: 0 })
    renderPage()

    expect(await screen.findByText('Aucune tâche à afficher')).toBeInTheDocument()
  })

  it('affiche une erreur si le backend est inaccessible', async () => {
    taskService.getTasks.mockRejectedValue({ message: 'Impossible de contacter le serveur backend.' })
    renderPage()

    expect(await screen.findByText('Impossible de contacter le serveur backend.')).toBeInTheDocument()
  })

  it('change le statut via le sélecteur et appelle le backend', async () => {
    taskService.getTasks.mockResolvedValue(sampleTasks)
    taskService.updateTaskStatus.mockResolvedValue({ ...sampleTasks.content[1], status: 'DONE' })
    renderPage()

    const select = await screen.findByLabelText('Changer le statut de la tâche Preparer la soutenance')
    fireEvent.change(select, { target: { value: 'DONE' } })

    await waitFor(() => {
      expect(taskService.updateTaskStatus).toHaveBeenCalledWith(2, 'DONE')
    })
    const termine = screen.getByRole('region', { name: 'Colonne Terminé' })
    await waitFor(() => expect(termine).toHaveTextContent('Preparer la soutenance'))
  })
})