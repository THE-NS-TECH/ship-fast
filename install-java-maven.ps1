# install-java-maven.ps1
# Downloads and configures Java 17 (Adoptium Temurin) and Maven 3.9.9

$ErrorActionPreference = "Stop"

$installDir = "C:\dev-tools"
$javaDir = "$installDir\java-17"
$mavenDir = "$installDir\maven"

# Create install directory
if (-not (Test-Path $installDir)) {
    New-Item -ItemType Directory -Path $installDir -Force | Out-Null
}

# --- Java 17 (Adoptium Temurin) ---
Write-Host "`n=== Installing Java 17 (Adoptium Temurin) ===" -ForegroundColor Cyan

$javaZip = "$installDir\java17.zip"
$javaUrl = "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse?project=jdk"

if (-not (Test-Path "$javaDir\bin\java.exe")) {
    Write-Host "Downloading Java 17..."
    Invoke-WebRequest -Uri $javaUrl -OutFile $javaZip -UseBasicParsing
    
    Write-Host "Extracting Java 17..."
    Expand-Archive -Path $javaZip -DestinationPath "$installDir\java-temp" -Force
    
    # The archive extracts to a versioned folder, rename it
    $extracted = Get-ChildItem "$installDir\java-temp" | Select-Object -First 1
    if (Test-Path $javaDir) { Remove-Item $javaDir -Recurse -Force }
    Move-Item $extracted.FullName $javaDir
    
    # Cleanup
    Remove-Item $javaZip -Force
    Remove-Item "$installDir\java-temp" -Recurse -Force
    
    Write-Host "Java 17 installed at: $javaDir" -ForegroundColor Green
} else {
    Write-Host "Java 17 already installed at: $javaDir" -ForegroundColor Yellow
}

# --- Maven 3.9.9 ---
Write-Host "`n=== Installing Maven 3.9.9 ===" -ForegroundColor Cyan

$mavenZip = "$installDir\maven.zip"
$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip"

if (-not (Test-Path "$mavenDir\bin\mvn.cmd")) {
    Write-Host "Downloading Maven 3.9.9..."
    Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip -UseBasicParsing
    
    Write-Host "Extracting Maven 3.9.9..."
    Expand-Archive -Path $mavenZip -DestinationPath "$installDir\maven-temp" -Force
    
    # The archive extracts to apache-maven-3.9.9
    $extracted = Get-ChildItem "$installDir\maven-temp" | Select-Object -First 1
    if (Test-Path $mavenDir) { Remove-Item $mavenDir -Recurse -Force }
    Move-Item $extracted.FullName $mavenDir
    
    # Cleanup
    Remove-Item $mavenZip -Force
    Remove-Item "$installDir\maven-temp" -Recurse -Force
    
    Write-Host "Maven installed at: $mavenDir" -ForegroundColor Green
} else {
    Write-Host "Maven already installed at: $mavenDir" -ForegroundColor Yellow
}

# --- Set Environment Variables (User scope) ---
Write-Host "`n=== Configuring Environment Variables ===" -ForegroundColor Cyan

# Set JAVA_HOME
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", $javaDir, "User")
Write-Host "JAVA_HOME set to: $javaDir"

# Set MAVEN_HOME
[System.Environment]::SetEnvironmentVariable("MAVEN_HOME", $mavenDir, "User")
Write-Host "MAVEN_HOME set to: $mavenDir"

# Update PATH
$currentPath = [System.Environment]::GetEnvironmentVariable("Path", "User")
$pathsToAdd = @("$javaDir\bin", "$mavenDir\bin")
$pathChanged = $false

foreach ($p in $pathsToAdd) {
    if ($currentPath -notlike "*$p*") {
        $currentPath = "$p;$currentPath"
        $pathChanged = $true
        Write-Host "Added to PATH: $p"
    } else {
        Write-Host "Already in PATH: $p" -ForegroundColor Yellow
    }
}

if ($pathChanged) {
    [System.Environment]::SetEnvironmentVariable("Path", $currentPath, "User")
}

# Also set for current session
$env:JAVA_HOME = $javaDir
$env:MAVEN_HOME = $mavenDir
$env:Path = "$javaDir\bin;$mavenDir\bin;$env:Path"

Write-Host "`n=== Installation Complete ===" -ForegroundColor Green
Write-Host "Verifying installations:" -ForegroundColor Cyan

Write-Host "`nJava version:"
& "$javaDir\bin\java.exe" -version

Write-Host "`nMaven version:"
& "$mavenDir\bin\mvn.cmd" --version

Write-Host "`n[IMPORTANT] Open a NEW terminal for PATH changes to take effect." -ForegroundColor Yellow
