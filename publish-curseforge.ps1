<#
.SYNOPSIS
    Local CurseForge upload workaround for ModPublisher 2.2.3.

.DESCRIPTION
    The ModPublisher CurseUpload4J dependency sends User-Agent "CurseUpload4J"
    and X-Api-Token header, which CurseForge/Cloudflare may block with HTTP 403.
    This script re-implements the same legacy CurseForge upload and lets you
    switch host + auth mode:
      - HostTarget: minecraft (default) | legacy
      - AuthMode:   query (default) | header
    Game version metadata mirrors ModPublisher's CurseUploadTask:
      1.21.1 (Minecraft 1.21 type) + Client + Server (environment group).
    Use -DryRun to only print the request.

.EXAMPLE
    .\publish-curseforge.ps1 -DryRun
    .\publish-curseforge.ps1 -HostTarget minecraft -AuthMode header
#>
[CmdletBinding()]
param(
    [switch]$DryRun,
    [ValidateSet('minecraft','legacy')]
    [string]$HostTarget = 'minecraft',
    [ValidateSet('query','header')]
    [string]$AuthMode = 'header'
)

$ErrorActionPreference = 'Stop'
$ProjectId = 1022428

function Resolve-Token {
    param([string]$Name)
    $value = [Environment]::GetEnvironmentVariable($Name, 'Process')
    if ([string]::IsNullOrWhiteSpace($value)) {
        $value = [Environment]::GetEnvironmentVariable($Name, 'User')
    }
    if ([string]::IsNullOrWhiteSpace($value)) {
        $value = [Environment]::GetEnvironmentVariable($Name, 'Machine')
    }
    if (-not [string]::IsNullOrWhiteSpace($value)) {
        [Environment]::SetEnvironmentVariable($Name, $value, 'Process')
    }
    return $value
}

$ct = Resolve-Token 'CURSE_TOKEN'
if ([string]::IsNullOrWhiteSpace($ct)) {
    throw 'CURSE_TOKEN is not set (Process/User/Machine).'
}

$modVersionLine = (Get-Content '.\gradle.properties' | Where-Object { $_ -match '^mod_version=' } | Select-Object -First 1)
if (-not $modVersionLine) { throw 'mod_version not found in gradle.properties' }
$modVersion = ($modVersionLine -split '=', 2)[1].Trim()

$jar = ".\build\libs\SlashBladeResharped-$modVersion.jar"
if (-not (Test-Path $jar)) {
    throw "Jar not found: $jar (run .\gradlew jar first)"
}

$browserUA = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36'

# Resolve numeric CurseForge version ids (read-only)
Write-Host '[publish-curseforge] Resolving CurseForge version ids ...'
$versionsResp = & curl.exe -s -H "X-Api-Token: $ct" -H "Accept: application/json" -H "User-Agent: $browserUA" 'https://minecraft.curseforge.com/api/game/versions'
$versions = $versionsResp | ConvertFrom-Json
$typesResp = & curl.exe -s -H "X-Api-Token: $ct" -H "Accept: application/json" -H "User-Agent: $browserUA" 'https://minecraft.curseforge.com/api/game/version-types'
$versionTypes = $typesResp | ConvertFrom-Json

$minecraftType = $versionTypes | Where-Object { $_.slug -eq 'minecraft-1-21' } | Select-Object -First 1
if (-not $minecraftType) { throw 'Could not resolve CurseForge version type "minecraft-1-21"' }

$gameVersion = $versions | Where-Object { $_.name -eq '1.21.1' -and $_.gameVersionTypeID -eq $minecraftType.id } | Select-Object -First 1
if (-not $gameVersion) { $gameVersion = $versions | Where-Object { $_.name -eq '1.21.1' } | Select-Object -First 1 }
if (-not $gameVersion) { throw 'Could not resolve CurseForge game version id for 1.21.1' }

$clientVersion = $versions | Where-Object { $_.name -eq 'Client' -or $_.name -eq 'client' } | Select-Object -First 1
$serverVersion = $versions | Where-Object { $_.name -eq 'Server' -or $_.name -eq 'server' } | Select-Object -First 1
if (-not $clientVersion -or -not $serverVersion) {
    throw 'Could not resolve Client/Server environment version ids'
}

$modloaderType = $versionTypes | Where-Object { $_.slug -eq 'modloader' } | Select-Object -First 1
if (-not $modloaderType) { throw 'Could not resolve CurseForge version type "modloader"' }
$neoforgeVersion = $versions | Where-Object { $_.gameVersionTypeID -eq $modloaderType.id -and $_.name -eq 'NeoForge' } | Select-Object -First 1
if (-not $neoforgeVersion) { throw 'Could not resolve NeoForge modloader version id' }

$gameVersionIds = @([long]$gameVersion.id, [long]$clientVersion.id, [long]$serverVersion.id, [long]$neoforgeVersion.id)
Write-Host ("[publish-curseforge] version ids = game: {0}, client: {1}, server: {2}, neoforge: {3}" -f $gameVersion.id, $clientVersion.id, $serverVersion.id, $neoforgeVersion.id)

$metadata = [ordered]@{
    changelog               = 'Changelog WIP...'
    changelogType           = 'markdown'
    displayName             = "[1.21.1] SlashBlade:Resharped - $modVersion"
    gameVersions            = $gameVersionIds
    releaseType             = 'release'
    isMarkedForManualRelease = $false
} | ConvertTo-Json -Compress

$metaTmp = Join-Path $env:TEMP 'cf-upload-metadata.json'
[System.IO.File]::WriteAllText($metaTmp, $metadata, [System.Text.UTF8Encoding]::new($false))
$respTmp = Join-Path $env:TEMP 'cf-upload-response.json'
Remove-Item $respTmp -ErrorAction SilentlyContinue

$baseUrl = if ($HostTarget -eq 'legacy') { 'https://legacy.curseforge.com/api' } else { 'https://minecraft.curseforge.com/api' }
$uploadUrl = "$baseUrl/projects/$ProjectId/upload-file"
if ($AuthMode -eq 'query') { $uploadUrl = "$uploadUrl`?token=$ct" }
$displayUrl = $uploadUrl -replace [regex]::Escape($ct), '***'

Write-Host '[publish-curseforge] Request summary:'
Write-Host "  Host/Auth : $HostTarget / $AuthMode"
Write-Host "  URL       : $displayUrl"
Write-Host "  Jar       : $jar"
Write-Host "  Metadata  : $metadata"

if ($DryRun) {
    Write-Host '[publish-curseforge] DRY RUN - no upload performed.'
    exit 0
}

$curlArgs = @('-s', '-o', $respTmp, '-w', '%{http_code}', '-X', 'POST',
    "-F", "metadata=<$metaTmp",
    "-F", "file=@$jar",
    '-H', "User-Agent: $browserUA",
    '-H', 'Accept: application/json')
if ($AuthMode -eq 'header') {
    $curlArgs += @('-H', "X-Api-Token: $ct")
}
$curlArgs += $uploadUrl

Write-Host '[publish-curseforge] Uploading ...'
$httpCode = & curl.exe @curlArgs

Write-Host "[publish-curseforge] HTTP=$httpCode"
$resp = Get-Content $respTmp -Raw
if ($resp -match 'Just a moment|cf_chl|challenge-platform|Enable JavaScript') {
    $show = if ($resp.Length -gt 300) { $resp.Substring(0, 300) + '...[truncated]' } else { $resp }
    Write-Host "[publish-curseforge] DETECTED: Cloudflare bot challenge page (upload blocked). Preview: $show"
} else {
    $show = if ($resp.Length -gt 600) { $resp.Substring(0, 600) + '...[truncated]' } else { $resp }
    Write-Host "[publish-curseforge] Response: $show"
}

if ($httpCode -eq '200') {
    try {
        $obj = $resp | ConvertFrom-Json
        if ($obj.id) {
            Write-Host "[publish-curseforge] SUCCESS: uploaded file id=$($obj.id)"
            exit 0
        }
    } catch { }
    Write-Host '[publish-curseforge] HTTP 200 but response did not contain an id; treat as uncertain.'
    exit 1
}
Write-Host '[publish-curseforge] FAILED.'
exit 1
