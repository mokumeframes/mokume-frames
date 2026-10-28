param([Parameter(Mandatory=$true)][string]$Destination)
$ErrorActionPreference='Stop'
$manifest=Get-Content -LiteralPath (Join-Path $PSScriptRoot '..\dependency-manifest.json') -Raw | ConvertFrom-Json
$targetDir=[IO.Path]::GetFullPath($Destination)
New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
foreach($artifact in $manifest.artifacts){
    $target=Join-Path $targetDir $artifact.filename
    if(Test-Path -LiteralPath $target){
        if((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash.ToLower() -ne $artifact.sha256){throw "Existing file has a different checksum; preserve it and resolve manually: $target"}
        Write-Output "Verified existing $($artifact.name)";continue
    }
    $download=Join-Path $targetDir ($artifact.filename+'.download')
    Invoke-WebRequest -Uri $artifact.url -OutFile $download
    if((Get-FileHash -LiteralPath $download -Algorithm SHA256).Hash.ToLower() -ne $artifact.sha256){throw "Upstream content changed or download failed for $($artifact.name). File retained as .download; do not install."}
    Move-Item -LiteralPath $download -Destination $target
    Write-Output "Fetched and verified $($artifact.name)"
}
Write-Output 'Install NeoForge 21.1.217 and the Rising Heroes development JAR separately. KilaGraph is nested inside Photon.'
