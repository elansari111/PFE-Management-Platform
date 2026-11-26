-- V5__add_users_active.sql
-- Ajout du statut d'activation des comptes utilisateurs.
-- Les comptes existants (dont les comptes de démonstration) restent actifs par défaut.

ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
