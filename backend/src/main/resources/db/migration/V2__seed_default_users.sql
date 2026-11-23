-- V2__seed_default_users.sql
-- Comptes de démonstration. CHANGER LES MOTS DE PASSE avant toute mise en production.
-- admin@pfe.com       / Admin@12345
-- supervisor@pfe.com  / Supervisor@12345

INSERT IGNORE INTO users (first_name, last_name, email, password, role)
VALUES
('Admin', 'PFE', 'admin@pfe.com',
 '$2a$10$AaFcfDf1CKyNzoHKhv/7Ye1ynPgijuMeyWnTDc8jNYViCF8ksDKh2', 'ADMIN'),
('Encadrant', 'Demo', 'supervisor@pfe.com',
 '$2a$10$VYnYlLyp7gC4nLo00KWAROUvicCNPuBK8L.JPlIqliE97XaITY6VG', 'SUPERVISOR');