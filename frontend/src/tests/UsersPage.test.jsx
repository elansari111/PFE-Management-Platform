import { render, screen, fireEvent, waitFor, within } from '@testing-library/react'
import { describe, it, expect, beforeEach, vi } from 'vitest'
import { UsersPage } from '../pages/users'
import { ToastProvider } from '../components/ui'
import { userService } from '../features/users'
import { useAuth } from '../context/useAuth'

vi.mock('../features/users', () => ({
  userService: {
    getUsers: vi.fn(),
    createUser: vi.fn(),
    updateUserRole: vi.fn(),
    updateUserActive: vi.fn(),
  },
}))

vi.mock('../context/useAuth', () => ({
  useAuth: vi.fn(),
  default: vi.fn(),
}))

const sampleUsers = {
  content: [
    {
      id: 1,
      firstName: 'Mohamed',
      lastName: 'El Amrani',
      email: 'mohamed@univ.ma',
      role: 'ADMIN',
      active: true,
      createdAt: '2026-01-10T09:00:00',
    },
    {
      id: 2,
      firstName: 'Salma',
      lastName: 'Benali',
      email: 'salma@univ.ma',
      role: 'STUDENT',
      active: true,
      createdAt: '2026-02-11T10:30:00',
    },
    {
      id: 3,
      firstName: 'Karim',
      lastName: 'Idrissi',
      email: 'karim@univ.ma',
      role: 'SUPERVISOR',
      active: false,
      createdAt: '2026-03-12T11:00:00',
    },
  ],
  pageNumber: 0,
  pageSize: 10,
  totalElements: 3,
  totalPages: 1,
  first: true,
  last: true,
}

const emptyPage = {
  content: [],
  pageNumber: 0,
  pageSize: 10,
  totalElements: 0,
  totalPages: 1,
  first: true,
  last: true,
}

const renderPage = () =>
  render(
    <ToastProvider>
      <UsersPage />
    </ToastProvider>
  )

describe('Page Utilisateurs (administration)', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    // Utilisateur connecté : ADMIN (id 1) -> ligne non modifiable
    useAuth.mockReturnValue({ user: { id: 1, role: 'ADMIN' } })
  })

  it('affiche la liste des utilisateurs avec rôle et statut', async () => {
    userService.getUsers.mockResolvedValue(sampleUsers)
    renderPage()

    const studentRow = (await screen.findByText('Salma Benali')).closest('tr')
    expect(studentRow).toHaveTextContent('salma@univ.ma')
    expect(studentRow).toHaveTextContent('STUDENT')
    expect(studentRow).toHaveTextContent('Actif')

    const supervisorRow = screen.getByText('Karim Idrissi').closest('tr')
    expect(supervisorRow).toHaveTextContent('SUPERVISOR')
    expect(supervisorRow).toHaveTextContent('Désactivé')
  })

  it("affiche un état vide quand aucun utilisateur n'existe", async () => {
    userService.getUsers.mockResolvedValue(emptyPage)
    renderPage()

    expect(await screen.findByText('Aucun utilisateur trouvé')).toBeInTheDocument()
  })

  it('affiche un état d’erreur puis recharge via Réessayer', async () => {
    userService.getUsers.mockRejectedValueOnce({
      message: 'Impossible de contacter le serveur backend.',
      status: 500,
    })
    userService.getUsers.mockResolvedValue(sampleUsers)
    renderPage()

    expect(await screen.findByText('Impossible de contacter le serveur backend.')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: /Réessayer/ }))

    await waitFor(() => {
      expect(userService.getUsers).toHaveBeenCalledTimes(2)
    })
    expect(await screen.findByText('Salma Benali')).toBeInTheDocument()
  })

  it('crée un utilisateur via la modale', async () => {
    userService.getUsers.mockResolvedValue(sampleUsers)
    userService.createUser.mockResolvedValue({
      id: 9,
      firstName: 'Nadia',
      lastName: 'Benali',
      email: 'nadia@univ.ma',
      role: 'STUDENT',
      active: true,
    })
    renderPage()

    await screen.findByText('Salma Benali')
    fireEvent.click(screen.getByRole('button', { name: /Nouvel utilisateur/i }))

    fireEvent.change(screen.getByLabelText(/Prénom/i), { target: { value: 'Nadia' } })
    fireEvent.change(screen.getByLabelText(/^Nom/i), { target: { value: 'Benali' } })
    fireEvent.change(screen.getByLabelText(/Email/i), { target: { value: 'nadia@univ.ma' } })
    fireEvent.change(screen.getByLabelText(/Mot de passe/i), { target: { value: 'Password1' } })

    fireEvent.click(screen.getByRole('button', { name: /Créer l.utilisateur/ }))

    await waitFor(() => {
      expect(userService.createUser).toHaveBeenCalledWith({
        firstName: 'Nadia',
        lastName: 'Benali',
        email: 'nadia@univ.ma',
        password: 'Password1',
        role: 'STUDENT',
      })
    })
  })

  it('refuse un mot de passe faible (validation zod)', async () => {
    userService.getUsers.mockResolvedValue(sampleUsers)
    renderPage()

    await screen.findByText('Salma Benali')
    fireEvent.click(screen.getByRole('button', { name: /Nouvel utilisateur/i }))

    fireEvent.change(screen.getByLabelText(/Prénom/i), { target: { value: 'Nadia' } })
    fireEvent.change(screen.getByLabelText(/^Nom/i), { target: { value: 'Benali' } })
    fireEvent.change(screen.getByLabelText(/Email/i), { target: { value: 'nadia@univ.ma' } })
    fireEvent.change(screen.getByLabelText(/Mot de passe/i), { target: { value: 'faible' } })

    fireEvent.click(screen.getByRole('button', { name: /Créer l.utilisateur/ }))

    expect(
      await screen.findByText('Le mot de passe doit comporter au moins 8 caractères')
    ).toBeInTheDocument()
    expect(userService.createUser).not.toHaveBeenCalled()
  })

  it('désactive un utilisateur après confirmation', async () => {
    userService.getUsers.mockResolvedValue(sampleUsers)
    userService.updateUserActive.mockResolvedValue({ id: 2, active: false })
    renderPage()

    await screen.findByText('Salma Benali')
    fireEvent.click(screen.getByRole('button', { name: 'Désactiver Salma Benali' }))

    fireEvent.click(await screen.findByRole('button', { name: 'Désactiver le compte' }))

    await waitFor(() => {
      expect(userService.updateUserActive).toHaveBeenCalledWith(2, false)
    })
  })

  it('refuse l’accès à un non-administrateur sans requête API', () => {
    useAuth.mockReturnValue({ user: { id: 5, role: 'STUDENT' } })
    renderPage()

    expect(screen.getByText('Accès refusé')).toBeInTheDocument()
    expect(userService.getUsers).not.toHaveBeenCalled()
  })

  it('désactive les actions sur la ligne de l’utilisateur connecté', async () => {
    userService.getUsers.mockResolvedValue(sampleUsers)
    renderPage()

    const selfRow = (await screen.findByText('Mohamed El Amrani')).closest('tr')
    expect(within(selfRow).getByLabelText('Rôle de Mohamed El Amrani')).toBeDisabled()
    expect(
      within(selfRow).getByRole('button', { name: 'Désactiver Mohamed El Amrani' })
    ).toBeDisabled()

    // Une autre ligne reste entièrement modifiable
    const otherRow = screen.getByText('Salma Benali').closest('tr')
    expect(within(otherRow).getByLabelText('Rôle de Salma Benali')).not.toBeDisabled()
    expect(
      within(otherRow).getByRole('button', { name: 'Désactiver Salma Benali' })
    ).not.toBeDisabled()
  })
})
