<#
.SYNOPSIS
    Run Gradle publishMod without restarting the terminal.

.DESCRIPTION
    ModPublisher reads MODRINTH_TOKEN / CURSE_TOKEN from the process environment.
    If the tokens were added to the Windows User/Machine environment after this
    shell (or IDE) was started, the running process does not see them yet.

    This wrapper syncs the values from Process -> User -> Machine scope into the
    current process before launching Gradle, so a terminal restart is not needed.

.EXAMPLE
    .\publish.ps1
    .\publish.ps1 --no-daemon --console=plain
#>
[CmdletBinding()]
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$GradleArgs
)

$ErrorActionPreference = 'Stop'

function Resolve-Token {
    param([string]$Name)

    # Existing process value wins unless it is blank.
    $value = [Environment]::GetEnvironmentVariable($Name, 'Process')
    if ([string]::IsNullOrWhiteSpace($value)) {
        $value = [Environment]::GetEnvironmentVariable($Name, 'User')
    }
    if ([string]::IsNullOrWhiteSpace($value)) {
        $value = [Environment]::GetEnvironmentVariable($Name, 'Machine')
    }

    if (-not [string]::IsNullOrWhiteSpace($value)) {
        [Environment]::SetEnvironmentVariable($Name, $value, 'Process')
        Write-Host ("[publish.ps1] {0}: synchronized into this process (len={1})." -f $Name, $value.Length)
    } else {
        Write-Warning ("[publish.ps1] {0}: NOT set in Process/User/Machine scope. This platform will be skipped or fail." -f $Name)
    }
}

Resolve-Token 'MODRINTH_TOKEN'
Resolve-Token 'CURSE_TOKEN'

$hasTask = $GradleArgs | Where-Object { -not $_.StartsWith('-') } | Select-Object -First 1
if (-not $hasTask) {
    $GradleArgs = @('publishMod') + $GradleArgs
}

Write-Host "[publish.ps1] Running: gradlew.bat $($GradleArgs -join ' ')"
& .\gradlew.bat @GradleArgs
exit $LASTEXITCODE
