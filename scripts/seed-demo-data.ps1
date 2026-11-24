# Donnees de demonstration : projet, membres et 6 taches
# Prerequis : l'application tourne (docker compose up -d) et les comptes @pfe.local existent (migration V3)
# Usage     : .\scripts\seed-demo-data.ps1
param([string]$Api = "http://localhost:8080/api/v1")

function Call($method, $url, $token, $body) {
  $p = @{ Method = $method; Uri = $url; ContentType = "application/json" }
  if ($token) { $p.Headers = @{ Authorization = "Bearer $token" } }
  if ($body)  { $p.Body = ($body | ConvertTo-Json) }
  try { Invoke-RestMethod @p }
  catch { Write-Host ("HTTP " + $_.Exception.Response.StatusCode.value__ + " <- " + $method + " " + $url) -ForegroundColor Red }
}

# Connexions (comptes de demo)
$aStu = Call Post "$Api/auth/login" $null @{ email="student@pfe.local";    password="Password123!" }
$aSup = Call Post "$Api/auth/login" $null @{ email="supervisor@pfe.local"; password="Password123!" }
$aAdm = Call Post "$Api/auth/login" $null @{ email="admin@pfe.local";      password="Password123!" }
if (-not $aStu -or -not $aSup -or -not $aAdm) { Write-Host "Connexion impossible : l'application est-elle demarree ?" -ForegroundColor Red; exit 1 }
$tSup = $aSup.token; $tAdm = $aAdm.token

# Ne rien faire si des taches existent deja
$existing = Call Get "$Api/tasks?size=1" $tAdm $null
if ($existing.totalElements -gt 0) {
  Write-Host "Des taches existent deja ($($existing.totalElements)). Rien a faire." -ForegroundColor Yellow
  exit 0
}

# Projet : reutilise le premier, sinon en cree un
$projects = Call Get "$Api/projects?size=50" $null $null
if ($projects.content.Count -gt 0) {
  $projectId = $projects.content[0].id
} else {
  $proj = Call Post "$Api/projects" $tSup @{ title="Plateforme de gestion PFE"; description="Projet de demonstration"; startDate="2026-12-01"; endDate="2027-06-30" }
  $projectId = $proj.id
}
Write-Host "Projet utilise : $projectId"

# Membres
Call Post "$Api/projects/$projectId/members" $tAdm @{ userId = $aStu.user.id } | Out-Null
Call Post "$Api/projects/$projectId/members" $tAdm @{ userId = $aSup.user.id } | Out-Null

# Taches
function NewTask($title, $desc, $status, $prio, $due, $assignee) {
  $b = @{ title=$title; description=$desc; status=$status; priority=$prio; dueDate=$due; projectId=$projectId }
  if ($assignee) { $b.assignedToId = $assignee }
  Call Post "$Api/tasks" $tSup $b
}
$created = @(
  (NewTask "Rediger le cahier des charges"    "Analyse des besoins"      "DONE"        "HIGH"   "2026-12-05" $aStu.user.id),
  (NewTask "Concevoir le schema de la base"   "Diagramme UML"            "IN_PROGRESS" "HIGH"   "2026-12-15" $aStu.user.id),
  (NewTask "Developper l API des projets"     "CRUD et securite"         "IN_PROGRESS" "MEDIUM" "2026-12-20" $aSup.user.id),
  (NewTask "Mettre en place la CI/CD"         "GitHub Actions et Docker" "TODO"        "MEDIUM" "2026-12-28" $aSup.user.id),
  (NewTask "Rediger le rapport intermediaire" "Etat d avancement"        "TODO"        "LOW"    "2027-01-10" $aStu.user.id),
  (NewTask "Preparer la soutenance"           "Slides et demonstration"  "TODO"        "LOW"    "2027-02-01" $null)
)
$created | Format-Table id, title, status, priority
Write-Host "Termine : $($created.Count) taches creees." -ForegroundColor Green