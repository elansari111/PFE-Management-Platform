# Plateforme de Gestion des Projets PFE (PFE Management Platform)

[![CI - Validation](https://github.com/elansari111/PFE-Management-Platform/actions/workflows/ci.yml/badge.svg)](https://github.com/elansari111/PFE-Management-Platform/actions/workflows/ci.yml)
[![DevSecOps - Security](https://github.com/elansari111/PFE-Management-Platform/actions/workflows/security.yml/badge.svg)](https://github.com/elansari111/PFE-Management-Platform/actions/workflows/security.yml)

Plateforme collaborative et sécurisée dédiée à la gestion complète des Projets de Fin d'Études (PFE) : soumission des sujets, affectation, suivi des livrables et soutenances.

---

## 👥 Équipe de Développement
Projet académique réalisé par une équipe de 4 développeurs :
- **Yassine El Ansari** (Branche : `dev-yassine`)
- **Safia** (Branche : `dev-safia`)
- **Salma** (Branche : `dev-salma`)
- **Mohamed (Simo)** (Branche : `dev-simo`)

---

## 📁 Structure du Projet

```text
PFE-Management-Platform/
├── frontend/             # Application cliente (UI / Dashboard)
├── backend/              # API serveur et logique métier
├── .github/
│   └── workflows/        # Pipelines GitHub Actions (CI & Sécurité)
├── .gitignore            # Fichiers et dossiers ignorés par Git
├── .env.example          # Modèle de variables d'environnement
└── README.md             # Documentation du projet
```

---

## 🔄 Workflow Git Collaboratif

1. **Branches principales** :
   - `main` : Version stable et prête pour la production.
   - `develop` : Branche commune d'intégration.
2. **Branches personnelles** :
   - `dev-yassine`, `dev-safia`, `dev-salma`, `dev-simo`.
3. **Cycle de développement** :
   - Chaque développeur travaille sur sa branche personnelle.
   - Synchronisation régulière avec `develop` :
     ```bash
     git checkout develop
     git pull origin develop
     git checkout dev-votre-nom
     git merge develop
     ```
   - Création d'une **Pull Request vers `develop`**.
   - Exécution automatique des vérifications CI/CD.
   - Revue et validation par un autre membre de l'équipe avant merge.
   - `develop` est ensuite fusionnée dans `main` pour les versions stables.

---

## 🚀 Démarrage Rapide

### 1. Cloner le dépôt
```bash
git clone https://github.com/elansari111/PFE-Management-Platform.git
cd PFE-Management-Platform
```

### 2. Configurer les variables d'environnement
```bash
# Windows (PowerShell) :
Copy-Item .env.example .env

# Linux / macOS / Git Bash :
cp .env.example .env
```

> **Important** : Ne jamais commiter le fichier `.env` résultant.
