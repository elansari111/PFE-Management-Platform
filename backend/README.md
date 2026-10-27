# Backend Spring Boot - Plateforme de Gestion des Projets PFE

Plateforme collaborative de gestion des projets de fin d'études (PFE) pour les étudiants, encadrants et administrateurs.

---

## 1. Présentation du Backend

Le backend est conçu sous forme d'une API RESTful robuste, modulaire et hautement sécurisée avec Spring Boot 3.3.5 (compatible Java 21+).
Il s'appuie sur une architecture en couches stricte garantissant la séparation des responsabilités, la maintenabilité et la testabilité du code.

### Fonctionnalités socles actuelles :
- **Gestion des Projets PFE** : Consultation publique avec pagination et filtres par statut, création, modification et suppression sécurisées.
- **Modèle de données relationnel** : Utilisateurs, Projets, Membres, Tâches, Livrables.
- **Gestion des migrations de schéma** : Flyway automatisé.
- **Sécurité** : Spring Security 6 stateless, contrôle granulaire par HTTP method et pré-configuration pour JWT.
- **Documentation OpenAPI 3** : Swagger UI interactif avec support de l'authentification Bearer.
- **Monitoring & Observabilité** : Spring Boot Actuator (`/actuator/health`, `/actuator/info`).

---

## 2. Technologies Utilisées

- **Langage** : Java 21 (supporte JDK 21 à JDK 27)
- **Framework** : Spring Boot 3.3.5
- **Accès aux données** : Spring Data JPA / Hibernate 6
- **Base de données relationnelle** : MySQL 8.x
- **Gestion des migrations** : Flyway Migration 10.x
- **Sécurité** : Spring Security 6 & BCrypt Password Encoder
- **Validation** : Jakarta Bean Validation & Hibernate Validator
- **Documentation API** : SpringDoc OpenAPI 2.6.0 (Swagger UI)
- **Monitoring** : Spring Boot Actuator
- **Tests** : JUnit 5, Mockito, Spring Boot Test, H2 In-Memory Database (profil test isolé)
- **Build & Dependency Management** : Apache Maven avec Maven Wrapper (`mvnw` / `mvnw.cmd`)

---

## 3. Prérequis

Avant de lancer le backend, assurez-vous de disposer de :

1. **Java Development Kit (JDK)** version 21 ou supérieure (ex: JDK 21 LTS ou JDK 27).
2. **Serveur MySQL 8.x** démarré et accessible sur `localhost:3306`.
3. Un shell PowerShell (Windows) ou Bash (Linux/macOS).
4. Git configuré.

---

## 4. Installation et Configuration de Java

Vérifiez l'installation de Java :
```bash
java -version
```

Assurez-vous que la variable d'environnement `JAVA_HOME` pointe vers votre JDK :
- **Windows (PowerShell)** :
  ```powershell
  $env:JAVA_HOME = "C:\Program Files\Java\jdk-21"  # ou le chemin de votre JDK
  ```
- **Linux / macOS** :
  ```bash
  export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
  ```

---

## 5. Configuration de MySQL

1. Connectez-vous à votre serveur MySQL en tant qu'administrateur :
   ```bash
   mysql -u root -p
   ```

2. Créez la base de données et l'utilisateur dédié (ou exécutez le script ci-dessous) :
   ```sql
   CREATE DATABASE IF NOT EXISTS pfe_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER IF NOT EXISTS 'pfe_user'@'localhost' IDENTIFIED BY 'pfe_password';
   GRANT ALL PRIVILEGES ON pfe_management.* TO 'pfe_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. Les tables et index seront **automatiquement créés par Flyway** au premier démarrage du backend via le script :
   `src/main/resources/db/migration/V1__initial_schema.sql`.

---

## 6. Variables d'Environnement

Le backend utilise des variables d'environnement externalisées. Un fichier d'exemple est fourni dans `backend/.env.example`.

| Variable | Valeur par défaut | Description |
|---|---|---|
| `PORT` | `8080` | Port d'écoute du serveur Spring Boot |
| `DB_URL` | `jdbc:mysql://localhost:3306/pfe_management` | URL JDBC de la base de données |
| `DB_USERNAME` | `pfe_user` | Utilisateur MySQL |
| `DB_PASSWORD` | `pfe_password` | Mot de passe de l'utilisateur MySQL |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Origines Frontend autorisées |

Pour définir ces variables sous Windows PowerShell :
```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/pfe_management?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "pfe_user"
$env:DB_PASSWORD = "pfe_password"
```

---

## 7. Démarrage du Serveur

Naviguez dans le dossier `backend` et exécutez :

- **Sous Windows** :
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```

- **Sous Linux / macOS** :
  ```bash
  ./mvnw spring-boot:run
  ```

Le serveur sera accessible sur [http://localhost:8080](http://localhost:8080).

---

## 8. Exécution des Tests

Les tests automatisés utilisent un profil dédié `test` avec une base en mémoire H2 pour ne jamais altérer la base MySQL locale :

- **Sous Windows** :
  ```powershell
  .\mvnw.cmd test
  ```

- **Sous Linux / macOS** :
  ```bash
  ./mvnw test
  ```

Couverture actuelle des tests :
- Démarrage et chargement complet du contexte Spring (`PfeManagementApplicationTests`)
- Validation des contraintes DTO (`ProjectDtoValidationTest`)
- Tests unitaires isolés du service métier (`ProjectServiceImplTest`)
- Tests d'intégration Web MVC, sérialisation JSON, gestion des erreurs 400/401/404/409 (`ProjectControllerTest`)

---

## 9. Documentation Interactive Swagger UI

Dès que le serveur est démarré, accédez à la console Swagger UI :
- **Swagger UI** : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Spécification OpenAPI JSON** : [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

### Endpoints REST exposés (`/api/v1/projects`) :

| Méthode | Endpoint | Description | Accès |
|---|---|---|---|
| `GET` | `/api/v1/projects` | Liste paginée des projets (`page`, `size`, `status`, `sortBy`, `direction`) | Public |
| `GET` | `/api/v1/projects/{id}` | Détails d'un projet par son identifiant | Public |
| `POST` | `/api/v1/projects` | Création d'un projet avec validation DTO | Authentifié |
| `PUT` | `/api/v1/projects/{id}` | Mise à jour des informations d'un projet | Authentifié |
| `DELETE` | `/api/v1/projects/{id}` | Suppression d'un projet | Authentifié |

### Monitoring Actuator :
- [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) (Liveness & Readiness)
- [http://localhost:8080/actuator/info](http://localhost:8080/actuator/info)

---

## 10. Architecture des Dossiers

```text
backend/
├── pom.xml                               # Configuration Maven & dépendances
├── mvnw                                  # Script Wrapper Unix
├── mvnw.cmd                              # Script Wrapper Windows
├── .mvn/                                 # Binaires du wrapper Maven
├── .gitignore                            # Exclusions Git backend
├── .env.example                          # Exemple de configuration des secrets
├── README.md                             # Documentation complète
└── src/
    ├── main/
    │   ├── java/com/pfe/management/
    │   │   ├── PfeManagementApplication.java # Point d'entrée de l'application
    │   │   ├── config/                   # Configs OpenAPI / Swagger, etc.
    │   │   ├── controller/               # Contrôleurs REST (@RestController)
    │   │   ├── dto/                      # Data Transfer Objects
    │   │   │   ├── request/              # Objets de requête avec validation (@Valid)
    │   │   │   └── response/             # Réponses API standardisées
    │   │   ├── entity/                   # Entités JPA (User, Project, Task, Deliverable...)
    │   │   ├── repository/               # Interfaces Spring Data JPA
    │   │   ├── service/                  # Contrats d'interfaces de services
    │   │   │   └── impl/                 # Implémentations de la logique métier
    │   │   ├── security/                 # Sécurité Spring Security, BCrypt, Filtres & Handlers
    │   │   ├── exception/                # Gestion centralisée des exceptions (@RestControllerAdvice)
    │   │   └── mapper/                   # Mappeurs découplés Entity <-> DTO
    │   └── resources/
    │       ├── application.yml           # Configuration Spring Boot & MySQL
    │       └── db/migration/
    │           └── V1__initial_schema.sql # Migration initiale Flyway
    └── test/
        ├── java/com/pfe/management/
        │   ├── PfeManagementApplicationTests.java
        │   ├── controller/               # Tests d'intégration contrôleurs & sécurité
        │   ├── dto/                      # Tests unitaires de validation Jakarta
        │   └── service/                  # Tests unitaires de la logique métier
        └── resources/
            └── application-test.yml      # Configuration isolée H2 in-memory
```

---

## 11. Préparation DevOps & Prochaines Étapes

Le socle backend a été spécialement structuré pour faciliter l'intégration continue et le déploiement conteneurisé :
- Aucun chemin local en dur dans le code source.
- Secrets et ports entièrement pilotables par variables d'environnement.
- Migrations automatiques Flyway garantissant la cohérence des bases de données en staging/production.
- Healthchecks Actuator prêts pour les probes Kubernetes et les compose healthchecks.
- Tests automatisés exécutables de manière autonome via `./mvnw test` dans GitHub Actions.
