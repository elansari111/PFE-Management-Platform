-- V3__seed_demo_accounts_local.sql
-- Comptes de démonstration affichés sur la page de connexion (mot de passe : Password123!)
-- A SUPPRIMER avant toute mise en production.

INSERT IGNORE INTO users (first_name, last_name, email, password, role)
VALUES
('Etudiant', 'Demo', 'student@pfe.local',
 '$2a$10$2rwF.NjSUlODnAD3aYaPVOMxdLed6LVbvFJ4OFUnCU2.ZF4A3oM6G', 'STUDENT'),
('Encadrant', 'Demo', 'supervisor@pfe.local',
 '$2a$10$2rwF.NjSUlODnAD3aYaPVOMxdLed6LVbvFJ4OFUnCU2.ZF4A3oM6G', 'SUPERVISOR'),
('Admin', 'Demo', 'admin@pfe.local',
 '$2a$10$2rwF.NjSUlODnAD3aYaPVOMxdLed6LVbvFJ4OFUnCU2.ZF4A3oM6G', 'ADMIN');