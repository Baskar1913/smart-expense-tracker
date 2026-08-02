$ErrorActionPreference = 'Stop'

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = Resolve-Path (Join-Path $scriptRoot '..\..')
$propertiesPath = Join-Path $scriptRoot 'maven-wrapper.properties'

if (-not (Test-Path $propertiesPath)) {
    throw "Missing Maven wrapper properties: $propertiesPath"
}

$properties = ConvertFrom-StringData (Get-Content -Raw $propertiesPath)
$distributionUrl = $properties.distributionUrl

if (-not $distributionUrl) {
    throw 'distributionUrl is missing from maven-wrapper.properties'
}

$archiveName = [System.IO.Path]::GetFileName($distributionUrl)
$distributionName = $archiveName -replace '-bin\.zip$', ''
$mavenUserHome = if ($env:MAVEN_USER_HOME) { $env:MAVEN_USER_HOME } else { Join-Path $HOME '.m2' }
$hashBytes = [System.Security.Cryptography.SHA256]::Create().ComputeHash([Text.Encoding]::UTF8.GetBytes($distributionUrl))
$urlHash = ($hashBytes | ForEach-Object { $_.ToString('x2') }) -join ''
$mavenHome = Join-Path $mavenUserHome "wrapper\dists\$distributionName\$urlHash"
$mavenCommand = Join-Path $mavenHome 'bin\mvn.cmd'

if (-not (Test-Path $mavenCommand)) {
    $temporaryDirectory = Join-Path ([System.IO.Path]::GetTempPath()) ("mvnw-" + [Guid]::NewGuid())
    New-Item -ItemType Directory -Path $temporaryDirectory -Force | Out-Null

    try {
        $archivePath = Join-Path $temporaryDirectory $archiveName
        Write-Host "Downloading $distributionName..."
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $distributionUrl -OutFile $archivePath -UseBasicParsing
        Expand-Archive -Path $archivePath -DestinationPath $temporaryDirectory -Force

        $extractedDirectory = Join-Path $temporaryDirectory $distributionName
        if (-not (Test-Path $extractedDirectory)) {
            $extractedDirectory = Get-ChildItem $temporaryDirectory -Directory |
                Where-Object { Test-Path (Join-Path $_.FullName 'bin\mvn.cmd') } |
                Select-Object -First 1 -ExpandProperty FullName
        }

        if (-not $extractedDirectory) {
            throw 'Unable to locate the extracted Maven directory.'
        }

        New-Item -ItemType Directory -Path (Split-Path -Parent $mavenHome) -Force | Out-Null
        if (Test-Path $mavenHome) {
            Remove-Item $mavenHome -Recurse -Force
        }
        Move-Item $extractedDirectory $mavenHome
    }
    finally {
        if (Test-Path $temporaryDirectory) {
            Remove-Item $temporaryDirectory -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
}

Push-Location $projectRoot
try {
    & $mavenCommand @args
    exit $LASTEXITCODE
}
finally {
    Pop-Location
}
