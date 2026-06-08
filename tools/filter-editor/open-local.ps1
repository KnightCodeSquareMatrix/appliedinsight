# Optional dev helper: open the repo copy via HTTP (not required for normal use).
# Prefer double-clicking index.html for offline usage.
$ErrorActionPreference = "Stop"

$editorPath = Join-Path $PSScriptRoot "index.html"
if (-not (Test-Path $editorPath)) {
    Write-Error "index.html not found beside this script."
}

Write-Host "Opening offline editor: $editorPath"
Start-Process $editorPath
