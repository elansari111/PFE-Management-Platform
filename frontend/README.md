# PFE Management Platform — Frontend Client

Application cliente moderne, réactive et accessible pour la plateforme universitaire intelligente de gestion des Projets de Fin d'Études (PFE).

Développée sur la branche Git **`dev-simo`** dans le respect des contrats d'API du backend Spring Boot 3.3.5 / Java 21 et de la charte graphique académique.

---

## 🚀 1. Stack Technique

- **Cœur & Bundler** : [React 18.3](https://react.dev/) & [Vite 6](https://vitejs.dev/)
- **Design System & Styles** : [Tailwind CSS v4](https://tailwindcss.com/) via le plugin officiel `@tailwindcss/vite`
- **Typographie** : Inter (Google Fonts)
- **Palette de Couleurs** :
  - Fond clair : `#F8FAFC`
  - Cartes & Surfaces : `#FFFFFF`
  - Bordures : `#E2E8F0`
  - Primaire : `#1E3A8A` (Bleu institutionnel)
  - Accent : `#0DAD98` (Émeraude / Turquoise)
  - Texte : `#0F172A`
- **Navigation & Routage** : [React Router v7](https://reactrouter.com/) (routage déclaratif avec layouts imbriqués)
- **Client HTTP** : [Axios 1.7](https://axios-http.com/) centralisé avec intercepteurs JWT et formateur d'erreurs
- **Iconographie** : [Lucide React](https://lucide.dev/) (icônes SVG optimisées et accessibles)
- **Visualisation de Données** : [Recharts 2.15](https://recharts.org/) (graphiques réactifs)
- **Formulaires & Schémas** : [React Hook Form 7.54](https://react-hook-form.com/) & [Zod 3.24](https://zod.dev/)
- **Tests Automatisés** : [Vitest 3.0](https://vitest.dev/), [React Testing Library](https://testing-library.com/) et [JSDOM](https://github.com/jsdom/jsdom)

---

## 📁 2. Architecture du Répertoire `frontend/`

```text
frontend/
├── public/                 # Fichiers publics statiques et favicon.svg
├── scripts/
│   └── test-runner.js      # Runner Vitest assurant la compatibilité avec le workflow CI
├── src/
│   ├── app/                # Racine applicative (App.jsx)
│   ├── assets/             # Ressources multimédias
│   ├── components/
│   │   ├── ui/             # Design System (14 composants réutilisables)
│   │   │   ├── Button.jsx, Card.jsx, Badge.jsx, Input.jsx, Select.jsx
│   │   │   ├── Modal.jsx, ConfirmDialog.jsx, Table.jsx, StatCard.jsx
│   │   │   ├── Skeleton.jsx, EmptyState.jsx, ErrorState.jsx, Spinner.jsx
│   │   │   └── Toast.jsx, useToast.js
│   │   └── layout/         # Layout SaaS (Sidebar, Navbar, MainLayout, ModulePlaceholder)
│   ├── features/
│   │   └── projects/       # Logique métier Projets
│   │       ├── projectSchema.js           # Validation Zod conforme Jakarta
│   │       ├── projectService.js          # Client de données (Mock / API)
│   │       └── components/                # Modales Formulaire et Détails
│   ├── pages/              # Vues de routage
│   │   ├── dashboard/      # Tableau de bord avec statistiques & Recharts
│   │   ├── projects/       # Gestion complète CRUD des projets (Vue Tableau / Cartes)
│   │   ├── tasks/          # Suivi Kanban (placeholder documenté)
│   │   ├── deliverables/   # Suivi livrables (placeholder documenté)
│   │   ├── supervision/    # Espace encadrement (placeholder documenté)
│   │   ├── ai-assistant/   # AI Encadrant (placeholder documenté)
│   │   ├── notifications/  # Centre de notifications (placeholder documenté)
│   │   ├── profile/        # Profil utilisateur (placeholder documenté)
│   │   ├── settings/       # Paramètres
│   │   └── not-found/      # Page 404 intégrée au layout
│   ├── services/
│   │   └── apiClient.js    # Client Axios configuré
│   ├── mocks/              # Données et service in-memory (VITE_USE_MOCK)
│   ├── routes/             # Définition des routes React Router
│   ├── styles/             # globals.css avec thème Tailwind v4
│   └── tests/              # 28 tests automatisés (UI, CRUD, Layout, Navigation)
├── .env.example            # Gabarit des variables d'environnement
├── eslint.config.js        # Configuration ESLint v9 Flat Config
├── vite.config.js          # Configuration Vite avec chunking manuel et Vitest
└── package.json            # Dépendances et scripts
```

---

## ⚙️ 3. Installation et Configuration

### Prérequis
- **Node.js** : version LTS recommandée (v20.x ou supérieure, testé avec succès sur v26.7)
- **npm** : version 10.x ou supérieure

### Installation
Depuis la racine du projet :
```powershell
cd frontend
npm install
```

### Configuration des variables d'environnement (`.env`)
Copiez `.env.example` vers `.env` :
```powershell
# Windows PowerShell :
Copy-Item .env.example .env

# Linux / macOS :
cp .env.example .env
```

Contenu des variables :
```env
# URL de base de l'API Backend Spring Boot
VITE_API_BASE_URL=http://localhost:8080/api/v1

# Mode de données :
# 'true'  -> Mode Démonstration (données simulées en mémoire locale)
# 'false' -> Mode API Réel (appels HTTP réels vers Spring Boot)
VITE_USE_MOCK=false

# Port de développement local (par défaut 5173)
VITE_PORT=5173
```

---

## 🔄 4. Modes de Fonctionnement : Mock vs API Réel

La variable `VITE_USE_MOCK` permet de commuter facilement le comportement de l'application :

| Caractéristique | Mode Démonstration (`VITE_USE_MOCK=true`) | Mode API Réel (`VITE_USE_MOCK=false`) |
|---|---|---|
| **Source de données** | Données locales en mémoire (`src/mocks/`) | Base MySQL via contrôleurs Spring Boot |
| **Consultation des projets** | Immédiate (7 projets types pré-chargés) | Requête réelle `GET /api/v1/projects` |
| **Création / Édition / Suppression** | Opérationnelle en mémoire avec vérification des conflits (409) | Requiert un jeton JWT valide (Spring Security 401 si anonyme) |
| **Persistance** | Réinitialisée au rechargement de page | Permanente dans la base MySQL locale |
| **Indicateur visuel** | Bannière jaune explicite prévenant l'utilisateur | Bannière bleue indiquant la connexion API |
| **Utilité** | Test de l'IHM et validation fonctionnelle sans dépendre de MySQL | Intégration continue et validation de bout en bout |

---

## 🛠️ 5. Commandes Disponibles

Toutes les commandes s'exécutent depuis le dossier `frontend/` :

```powershell
# 1. Démarrer le serveur de développement local (port 5173)
npm run dev

# 2. Exécuter l'ensemble des 28 tests automatisés (Vitest)
npm run test

# 3. Exécuter les tests en mode compatible CI GitHub Actions
npm test -- --watchAll=false

# 4. Lancer les tests en mode interactif watch
npm run test:watch

# 5. Vérifier la qualité et la conformité du code (ESLint)
npm run lint

# 6. Compiler l'application pour la production (dossier dist/)
npm run build

# 7. Prévisualiser le build de production localement
npm run preview

# 8. Analyser la sécurité des dépendances
npm audit
```

---

## ✨ 6. Fonctionnalités Opérationnelles

### Dashboard (`/dashboard`)
- 4 cartes statistiques calculées dynamiquement (*Total Projets*, *En Cours*, *Validés / Finis*, *En Attente / Proposés*).
- Graphique en barres interactif Recharts représentant la distribution des projets par statut.
- Tableau des projets récents avec raccourci vers la fiche de consultation détaillée.
- Chronologie des activités récentes de l'équipe.
- Encart dédié aux recommandations de l'AI Encadrant.

### Gestion des Projets (`/projects`)
- **Bascule de vue** : Vue Tableau (colonnes ID, titre, statut, période, membres, tâches, livrables, actions) et Vue Cartes (cartes synthétiques).
- **Recherche textuelle instantanée** par titre ou mot-clé.
- **Filtre par statut** (`PROPOSED`, `PENDING`, `APPROVED`, `IN_PROGRESS`, `COMPLETED`, `ARCHIVED`).
- **Tri paramétrable** par date de création ou ordre alphabétique.
- **Pagination complète** conforme à `PageResponse` (taille de page, précédent/suivant, saut de page).
- **Création de projet** avec validation Jakarta/Zod en temps réel.
- **Modification de projet** avec pré-remplissage.
- **Suppression sécurisée** avec boîte de dialogue `ConfirmDialog`.
- **Fiche détaillée** (`ProjectDetailModal`) affichant l'intégralité des métadonnées.

### Design System & Navigation
- Layout SaaS responsive (Desktop, Tablette, Mobile).
- Sidebar rétractable avec état actif stylisé (`#0DAD98` / `#1E3A8A`).
- Navbar avec fil d'Ariane dynamique, barre de recherche visuelle (`Ctrl+K`), centre de notifications et simulateur de rôles (`STUDENT`, `SUPERVISOR`, `ADMIN`).
- Système de notifications Toast flottantes.
- Page d'erreur 404 conviviale avec bouton de retour au tableau de bord.

---

## 🔒 7. Intégration Backend & Limitations Connues

L'inspection de `SecurityConfig.java` et de `ProjectController.java` côté Spring Boot met en évidence :

1. **Consultation publique (`GET /api/v1/projects/**`)** :
   - Les requêtes de lecture sont configurées en `.permitAll()`.
   - Elles fonctionnent directement sans authentification lorsque Spring Boot est démarré.
2. **Mutations sécurisées (`POST`, `PUT`, `DELETE /api/v1/projects/**`)** :
   - Ces routes sont configurées en `.authenticated()`.
   - Tant qu'un utilisateur n'est pas connecté avec un jeton JWT valide (stocké dans `localStorage.getItem('pfe_token')`), Spring Security renvoie un statut **HTTP 401 Unauthorized**.
   - Le client frontend intercepte cette réponse et informe l'utilisateur par une notification claire sans bloquer l'interface.
   - **Pour tester les flux de modification et de suppression sans authentification**, activez `VITE_USE_MOCK=true`.
3. **Modules en attente de développement backend** :
   - Tâches (`tasks`), Livrables (`deliverables`), Gestion des utilisateurs et IA Encadrant n'ont pas encore d'endpoints REST côté Spring Boot. Des pages placeholders dédiées documentent les fonctionnalités futures.

---

## 🔀 8. Procédure d'Intégration Git & Pull Request

Le travail sur cette branche respecte l'isolation de l'équipe :
- **Branche locale** : `dev-simo`
- **Branche de destination** : `main` (ou branche commune de sprint)

### Checklist avant création de la Pull Request :
1. Vérifier que l'ensemble des fichiers modifiés se situe dans `frontend/` :
   ```powershell
   git status
   ```
2. Vérifier que le linting ne remonte aucune erreur :
   ```powershell
   cd frontend
   npm run lint
   ```
3. Vérifier que la suite de tests est validée à 100% :
   ```powershell
   npm run test
   npm test -- --watchAll=false
   ```
4. Vérifier que la compilation de production s'exécute sans avertissement :
   ```powershell
   npm run build
   ```
5. Ajouter les fichiers au suivi Git et commiter manuellement :
   ```powershell
   git add frontend/
   git commit -m "feat(frontend): complete phase 1-4 UI design system, dashboard and projects CRUD"
   ```
