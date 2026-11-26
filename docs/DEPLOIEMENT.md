# Guide de Déploiement Continu (CD) — PFE Management Platform

Ce document décrit la mise en place **complète** du CD : serveur, secrets GitHub,
environnement protégé, premier déploiement et dépannage.

- Dépôt : `elansari111/PFE-Management-Platform`
- Pipeline : [.github/workflows/cd.yml](../.github/workflows/cd.yml)
- Stack de production : [docker-compose.prod.yml](../docker-compose.prod.yml)
- Images GHCR : `ghcr.io/elansari111/pfe-backend` et `ghcr.io/elansari111/pfe-frontend`

---

## 0. Vue d'ensemble du flux

```
push sur main
   └─► CI « CI - Pipeline Globale (Backend & Frontend) » (jobs backend-ci, frontend-ci, ci-status-check)
          └─► (succès uniquement) CD déclenché via workflow_run
                 └─► job build-and-push : images backend + frontend -> GHCR (tags :<sha court> et :latest)
                        └─► job deploy (environment: production, approbation manuelle)
                               ├─ SCP de docker-compose.prod.yml vers /opt/pfe
                               ├─ docker login ghcr.io sur le serveur (token lecture seule)
                               ├─ docker compose pull && up -d (IMAGE_TAG=<sha court>)
                               ├─ docker image prune -f
                               ├─ santé /actuator/health : 10 tentatives x 10 s
                               └─ si échec : rollback automatique vers la version précédente + job en échec
```

Prérequis GitHub : le fichier `cd.yml` doit exister sur la branche par défaut (`main`)
pour que le déclenchement `workflow_run` fonctionne.

---

## 1. Préparation du serveur (VM Ubuntu)

### 1.1 Installer Docker, Buildx et le plugin Compose v2

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg

sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | \
  sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

sudo systemctl enable --now docker
docker --version && docker compose version
```

### 1.2 Créer l'utilisateur de déploiement `deploy`

Pas de `sudo` : l'appartenance au groupe `docker` suffit pour piloter Docker.

```bash
sudo adduser --disabled-password --gecos "" deploy
sudo usermod -aG docker deploy
```

> Important : les nouveaux groupes ne prennent effet qu'à la **prochaine session SSH**.
> Vérifier ensuite : `id deploy` doit contenir `docker`.

### 1.3 Paire de clés SSH dédiée au déploiement

**Sur la machine locale (Windows PowerShell)** — génération :

```powershell
ssh-keygen -t ed25519 -C "gha-deploy-pfe" -f $HOME\.ssh\pfe_deploy -N '""'
```

Cela crée :
- `$HOME\.ssh\pfe_deploy`     → **clé privée** (à mettre dans le secret `SSH_PRIVATE_KEY`) ;
- `$HOME\.ssh\pfe_deploy.pub` → clé publique (à installer sur le serveur).

**Sur le serveur** — installer la clé publique pour `deploy`
(copier le contenu de `pfe_deploy.pub` puis) :

```bash
sudo mkdir -p /home/deploy/.ssh
sudo chmod 700 /home/deploy/.ssh
sudo sh -c 'cat >> /home/deploy/.ssh/authorized_keys'   # coller la clé publique, puis Ctrl-D
sudo chown -R deploy:deploy /home/deploy/.ssh
sudo chmod 600 /home/deploy/.ssh/authorized_keys
```

**Test depuis la machine locale** :

```powershell
ssh -i $HOME\.ssh\pfe_deploy deploy@<IP_OU_DOMAINE>
# doit ouvrir une session sans mot de passe
```

### 1.4 Pare-feu (UFW)

Le navigateur doit joindre le frontend (**3000**) et l'API (**8080**). MySQL n'est pas exposé.

```bash
sudo ufw allow OpenSSH        # port 22 (SSH)
sudo ufw allow 3000/tcp       # frontend nginx
sudo ufw allow 8080/tcp       # API backend (vue par le navigateur)
sudo ufw enable
sudo ufw status verbose       # 22, 3000 et 8080 doivent être listés ; 3306 NE doit PAS y être
```

### 1.5 Créer le dossier applicatif

```bash
sudo mkdir -p /opt/pfe
sudo chown deploy:deploy /opt/pfe
```

### 1.6 Créer le fichier `.env` de production sur le serveur

Se connecter en `deploy`, puis :

```bash
cd /opt/pfe
cat > .env <<EOF
MYSQL_ROOT_PASSWORD=$(openssl rand -base64 24)
DB_NAME=pfe_management
DB_USERNAME=pfe_user
DB_PASSWORD=$(openssl rand -base64 24)
JWT_SECRET=$(openssl rand -base64 48)
JWT_EXPIRATION_MS=86400000
CORS_ALLOWED_ORIGINS=http://<IP_OU_DOMAINE>:3000
EOF
chmod 600 .env
```

- `JWT_SECRET` : base64 256 bits minimum (48 octets ⇒ 64 caractères base64) — généré ici via `openssl`.
- `CORS_ALLOWED_ORIGINS` : **l'origine du frontend telle que vue par le navigateur**, soit `http://<IP_OU_DOMAINE>:3000`.
- Le fichier `.env` n'est **jamais** commité (déjà couvert par `.gitignore`).

### 1.7 Autoriser le serveur à tirer les images depuis GHCR

Créer un **Personal Access Token (classic)** GitHub avec **uniquement** le scope `read:packages`,
puis, sur le serveur :

```bash
echo "<LE_PAT_READ_PACKAGES>" | docker login ghcr.io -u elansari111 --password-stdin
# -> Login Succeeded
```

> Cette étape est aussi refaite automatiquement par le CD à chaque déploiement,
> mais elle permet de valider l'accès manuellement. Alternative : rendre les packages
> publics (Package settings > Change visibility > Public) et se passer de token.

---

## 2. Secrets et variables GitHub à créer

`Settings > Secrets and variables > Actions`.

### Secrets (onglet *Secrets*)

| Nom | Valeur attendue | Où l'obtenir |
|---|---|---|
| `SSH_HOST` | IP publique ou domaine de la VM | Console du fournisseur cloud / `ip a` sur le serveur |
| `SSH_USER` | `deploy` | Utilisateur créé en §1.2 |
| `SSH_PORT` | `22` (ou le port SSH personnalisé) | Configuration `sshd` du serveur |
| `SSH_PRIVATE_KEY` | Contenu **complet** de `$HOME\.ssh\pfe_deploy` | Généré en §1.3 (inclure les lignes `-----BEGIN/END OPENSSH PRIVATE KEY-----`) |
| `GHCR_READ_TOKEN` | PAT *classic* avec le seul scope `read:packages` | GitHub > Settings > Developer settings > Personal access tokens > Tokens (classic) |

> `GITHUB_TOKEN` est fourni automatiquement par GitHub Actions : **ne pas** le créer.

### Variables (onglet *Variables*)

| Nom | Valeur attendue | Remarque |
|---|---|---|
| `PROD_API_URL` | `http://<IP_OU_DOMAINE>:8080/api/v1` | Injectée au build du frontend via `VITE_API_BASE_URL` (URL vue par le navigateur). Le CD échoue si elle est vide. |

---

## 3. Créer l'environnement GitHub `production` (approbation manuelle)

1. `Settings > Environments > New environment` → nom : **`production`** → *Configure environment*.
2. Cocher **Required reviewers** et ajouter le(s) compte(s) devant approuver chaque déploiement.
3. (Recommandé) **Deployment branches and tags** → `Selected branches` → ajouter la règle `main`.
4. Enregistrer.

Résultat : après le job `build-and-push`, le job `deploy` reste en **Waiting** jusqu'à
l'approbation d'un relecteur. Le déploiement démarre ensuite automatiquement.

---

## 4. Ordre exact des étapes de mise en place

1. §1.1 → installer Docker + plugin Compose sur le serveur.
2. §1.2 → créer l'utilisateur `deploy` (groupe `docker`), puis se reconnecter.
3. §1.3 → générer la paire de clés, installer la clé publique, tester `ssh`.
4. §1.5 + §1.6 → créer `/opt/pfe` et le fichier `.env` de production.
5. §1.4 → ouvrir les ports 22 / 3000 / 8080 (UFW).
6. §1.7 → créer le PAT `read:packages`, faire un `docker login ghcr.io` de test.
7. §2 → créer les **5 secrets** et la **variable** `PROD_API_URL`.
8. §3 → créer l'environnement `production` avec approbation manuelle.
9. Merger (ou pousser) `cd.yml` et `docker-compose.prod.yml` sur `main` via une PR :
   la CI s'exécute, et si elle réussit, le CD se déclenche.
10. Premier déploiement : approuver le job `deploy`, vérifier §5.

> Le premier déploiement initialise la base MySQL (migrations Flyway exécutées
> automatiquement au démarrage du backend).

---

## 5. Tester sans risque et vérifier

### 5.1 Test à blanc du fichier compose (local, sans déployer)

Depuis le dépôt, valider la syntaxe et la résolution des variables :

```powershell
# Variables minimales pour la validation
$env:IMAGE_TAG="latest"
docker compose -f .\docker-compose.prod.yml config
```

Aucune erreur ⇒ le fichier est valide.

### 5.2 Déclencher le CD manuellement

`GitHub > onglet Actions > CD - Déploiement Continu (Production) > Run workflow > branche main > Run workflow`.

Cela contourne l'attente de la CI, mais **conserve** l'approbation de l'environnement.

> Précautions avant un test : s'assurer que `PROD_API_URL` est correcte et que le
> serveur répond en SSH. Le rollback automatique protège en cas de santé KO.

### 5.3 Vérifier le déploiement (PowerShell)

```powershell
# 1. Conteneurs en cours sur le serveur
ssh -i $HOME\.ssh\pfe_deploy deploy@<IP> "docker compose -f /opt/pfe/docker-compose.prod.yml ps"

# 2. Santé du backend depuis le serveur
ssh -i $HOME\.ssh\pfe_deploy deploy@<IP> "curl -fsS http://localhost:8080/actuator/health"

# 3. Image réellement déployée (tag = sha court du run CD)
ssh -i $HOME\.ssh\pfe_deploy deploy@<IP> "docker inspect -f '{{.Config.Image}}' pfe-backend"

# 4. Versions mémorisées (pour le rollback)
ssh -i $HOME\.ssh\pfe_deploy deploy@<IP> "cat /opt/pfe/.current_image_tag /opt/pfe/.previous_image_tag"

# 5. Test externe (nécessite les ports ouverts)
Invoke-WebRequest http://<IP>:8080/actuator/health -UseBasicParsing
Invoke-WebRequest http://<IP>:3000         -UseBasicParsing
```

### 5.4 Vérifier les logs en cas de doute

```bash
ssh deploy@<IP> "cd /opt/pfe && docker compose -f docker-compose.prod.yml logs --tail=100 backend"
ssh deploy@<IP> "cd /opt/pfe && docker compose -f docker-compose.prod.yml logs --tail=100 frontend"
```

---

## 6. Erreurs fréquentes et solutions

| Erreur | Cause | Solution |
|---|---|---|
| `Permission denied (publickey)` | Clé publique absente/invalide ou mauvaises permissions | Revérifier §1.3 ; `authorized_keys` en 600, `.ssh` en 700, propriétaire `deploy`. |
| `Host key verification failed` | Empreinte serveur absente du `known_hosts` du runner | Laisser la step `ssh-keyscan` s'exécuter ; vérifier que `SSH_HOST`/`SSH_PORT` sont corrects. |
| `permission denied while trying to connect to the Docker daemon` | `deploy` pas encore dans le groupe `docker` (session non rechargée) | `sudo usermod -aG docker deploy` puis **reconnexion SSH**. |
| `denied: permission_denied: read_packages` | Token GHCR sans scope `read:packages`, ou package privé sans accès | Recréer le PAT avec `read:packages` ; ou rendre les packages publics. |
| `manifest unknown` / `not found` au `pull` | Tag inexistant (build non poussé) ou org différente | Vérifier le job `build-and-push` dans GHCR (`Packages`), et les noms `pfe-backend`/`pfe-frontend`. |
| Le CD ne se déclenche jamais après la CI | Nom de la CI modifié, ou `cd.yml` absent de `main`, ou CI en échec | Le `name:` de `ci.yml` doit rester exactement `CI - Pipeline Globale (Backend & Frontend)` ; `cd.yml` doit être sur `main`. |
| `docker compose: command not found` | Plugin Compose v2 absent | `sudo apt-get install -y docker-compose-plugin`. |
| Backend `unhealthy` juste après le 1er démarrage | MySQL pas encore prêt, ou `JWT_SECRET` vide | Le `depends_on: service_healthy` gère MySQL ; vérifier que `.env` définit bien `JWT_SECRET` (base64). |
| Erreur CORS dans la console du navigateur | `CORS_ALLOWED_ORIGINS` ≠ origine du frontend | Doit valoir exactement `http://<IP>:3000` (schéma + port inclus). |
| Frontend affiche des appels vers `localhost:8080` | `PROD_API_URL` non définie/incorrecte au moment du build | Corriger la variable puis relancer le CD (le bundle est régénéré). |
| `port is already allocated` (3000 ou 8080) | Un autre processus occupe le port | `sudo ss -lptn 'sport = :8080'` puis arrêter le service conflictif. |
| Santé KO **et** rollback KO | Aucune version précédente valide (`latest`) | Inspecter les logs ; au besoin, déployer un tag SHA antérieur manuellement (`IMAGE_TAG=<sha>`). |
| MySQL refuse la connexion après changement de `.env` | Volume `db_data` déjà initialisé avec l'ancien mot de passe | Soit conserver l'ancien mot de passe, soit réinitialiser le volume (`docker compose down -v` — **destructif**). |

---

## 7. Rollback manuel (si nécessaire)

```bash
ssh deploy@<IP>
cd /opt/pfe
docker images | grep pfe-backend          # lister les tags disponibles
IMAGE_TAG=<sha_court_valide> docker compose -f docker-compose.prod.yml up -d
```

Le pipeline fait ce rollback automatiquement en cas d'échec de la vérification de santé.
