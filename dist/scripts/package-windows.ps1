param(
    [string]$JarPath,
    [string]$AppVersion = "1.0.0"
)

$ErrorActionPreference = "Stop"

$RepoRoot = Resolve-Path "$PSScriptRoot\..\.."
$InputDir = Join-Path $RepoRoot "dist\input"
$OutputDir = Join-Path $RepoRoot "dist\output"
$AppName = "Personal Finance"
$PortableDirName = "Personal-Finance"

if ($JarPath -eq "--AppVersion") {
    if ($args.Count -lt 1) {
        throw "Missing value for --AppVersion. Use: -AppVersion 1.0.0"
    }
    $AppVersion = $args[0]
    $JarPath = $null
} elseif ($JarPath -like "--AppVersion=*") {
    $AppVersion = $JarPath.Substring("--AppVersion=".Length)
    $JarPath = $null
} elseif ($JarPath -like "-*" -and -not (Test-Path $JarPath)) {
    throw "Invalid argument '$JarPath'. Use PowerShell named params with single hyphen, for example: -AppVersion 1.0.0"
}

if (-not $JarPath) {
    $jar = Get-ChildItem -Path (Join-Path $RepoRoot "backend\target") -Filter "*.jar" |
        Where-Object { $_.Name -notlike "*.original" } |
        Select-Object -First 1
    if (-not $jar) {
        throw "No executable JAR found in backend/target."
    }
    $JarPath = $jar.FullName
}

if (-not (Test-Path $JarPath)) {
    throw "JAR not found: $JarPath"
}
if (-not $env:JAVA_HOME) {
    throw "JAVA_HOME is required to package with jpackage."
}

New-Item -ItemType Directory -Force -Path $InputDir | Out-Null
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

$PortableSourceDir = Join-Path $OutputDir $AppName
if (Test-Path $PortableSourceDir) {
    Remove-Item -Path $PortableSourceDir -Recurse -Force
}

$JarFileName = Split-Path $JarPath -Leaf
Copy-Item -Path $JarPath -Destination (Join-Path $InputDir $JarFileName) -Force

$jpackageArgs = @(
    "--type", "app-image",
    "--name", $AppName,
    "--app-version", $AppVersion,
    "--input", $InputDir,
    "--main-jar", $JarFileName,
    "--main-class", "org.springframework.boot.loader.launch.JarLauncher",
    "--dest", $OutputDir,
    "--runtime-image", $env:JAVA_HOME
)


jpackage @jpackageArgs

if (-not (Test-Path $PortableSourceDir)) {
    throw "Portable app-image not found: $PortableSourceDir"
}

$PortableZipPath = Join-Path $OutputDir "$PortableDirName-$AppVersion-windows-portable.zip"
if (Test-Path $PortableZipPath) {
    Remove-Item -Path $PortableZipPath -Force
}

Compress-Archive -Path (Join-Path $PortableSourceDir "*") -DestinationPath $PortableZipPath
