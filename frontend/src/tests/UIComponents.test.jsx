import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import {
  Button,
  Badge,
  Card,
  CardTitle,
  CardContent,
  StatCard,
  Modal,
  ConfirmDialog,
  EmptyState,
  ErrorState,
} from '../components/ui'
import { FolderGit2 } from 'lucide-react'

describe('UI Components Design System - Phase 2', () => {
  describe('Button', () => {
    it('affiche le libellé et réagit au clic', () => {
      const handleClick = vi.fn()
      render(<Button onClick={handleClick}>Valider</Button>)

      const btn = screen.getByRole('button', { name: /Valider/i })
      expect(btn).toBeInTheDocument()

      fireEvent.click(btn)
      expect(handleClick).toHaveBeenCalledTimes(1)
    })

    it('affiche un spinner et désactive le bouton lorsque isLoading=true', () => {
      const handleClick = vi.fn()
      render(
        <Button isLoading onClick={handleClick}>
          Sauvegarder
        </Button>
      )

      const btn = screen.getByRole('button')
      expect(btn).toBeDisabled()
      expect(screen.getByRole('status')).toBeInTheDocument()

      fireEvent.click(btn)
      expect(handleClick).not.toHaveBeenCalled()
    })
  })

  describe('Badge', () => {
    it('affiche le texte et applique la variante', () => {
      render(
        <Badge variant="accent" dot>
          APPROVED
        </Badge>
      )
      const badge = screen.getByText('APPROVED')
      expect(badge).toBeInTheDocument()
    })
  })

  describe('Card', () => {
    it('rend correctement le titre et le contenu', () => {
      render(
        <Card>
          <CardTitle>Titre de Carte</CardTitle>
          <CardContent>Contenu de test</CardContent>
        </Card>
      )
      expect(screen.getByText('Titre de Carte')).toBeInTheDocument()
      expect(screen.getByText('Contenu de test')).toBeInTheDocument()
    })
  })

  describe('StatCard', () => {
    it('affiche le titre, la valeur et la tendance', () => {
      render(
        <StatCard
          title="Projets Actifs"
          value="12"
          icon={FolderGit2}
          trend="+2 ce mois"
          trendDirection="up"
          caption="Objectif validé"
        />
      )

      expect(screen.getByText('Projets Actifs')).toBeInTheDocument()
      expect(screen.getByText('12')).toBeInTheDocument()
      expect(screen.getByText('+2 ce mois')).toBeInTheDocument()
      expect(screen.getByText('Objectif validé')).toBeInTheDocument()
    })
  })

  describe('Modal & ConfirmDialog', () => {
    it('affiche la modale lorsque isOpen=true et déclenche onClose lors du clic sur le bouton de fermeture', () => {
      const handleClose = vi.fn()
      render(
        <Modal isOpen={true} onClose={handleClose} title="Modale de test">
          <p>Corps de la modale</p>
        </Modal>
      )

      expect(screen.getByText('Modale de test')).toBeInTheDocument()
      expect(screen.getByText('Corps de la modale')).toBeInTheDocument()

      const closeButton = screen.getByLabelText(/Fermer la boîte de dialogue/i)
      fireEvent.click(closeButton)
      expect(handleClose).toHaveBeenCalledTimes(1)
    })

    it('ne rend rien lorsque isOpen=false', () => {
      render(
        <Modal isOpen={false} title="Invisible">
          <p>Texte caché</p>
        </Modal>
      )
      expect(screen.queryByText('Invisible')).not.toBeInTheDocument()
    })

    it('gère la confirmation via ConfirmDialog', () => {
      const handleConfirm = vi.fn()
      const handleClose = vi.fn()

      render(
        <ConfirmDialog
          isOpen={true}
          onClose={handleClose}
          onConfirm={handleConfirm}
          title="Supprimer l’élément"
          confirmText="Oui, Supprimer"
        />
      )

      expect(screen.getByText('Supprimer l’élément')).toBeInTheDocument()
      const confirmBtn = screen.getByRole('button', { name: /Oui, Supprimer/i })
      fireEvent.click(confirmBtn)
      expect(handleConfirm).toHaveBeenCalledTimes(1)
    })
  })

  describe('EmptyState & ErrorState', () => {
    it('affiche l’état vide avec bouton d’action', () => {
      const handleAction = vi.fn()
      render(
        <EmptyState
          title="Aucun projet trouvé"
          description="Créez votre premier projet"
          actionLabel="Nouveau Projet"
          onAction={handleAction}
        />
      )

      expect(screen.getByText('Aucun projet trouvé')).toBeInTheDocument()
      const actionBtn = screen.getByRole('button', { name: /Nouveau Projet/i })
      fireEvent.click(actionBtn)
      expect(handleAction).toHaveBeenCalledTimes(1)
    })

    it('affiche l’état d’erreur avec bouton de réessai', () => {
      const handleRetry = vi.fn()
      render(
        <ErrorState
          title="Erreur de connexion"
          message="Impossible de joindre le serveur"
          onRetry={handleRetry}
        />
      )

      expect(screen.getByText('Erreur de connexion')).toBeInTheDocument()
      const retryBtn = screen.getByRole('button', { name: /Réessayer/i })
      fireEvent.click(retryBtn)
      expect(handleRetry).toHaveBeenCalledTimes(1)
    })
  })
})

