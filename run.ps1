Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "         STUDENT MANAGEMENT SYSTEM - LAUNCHER" -ForegroundColor Cyan
Write-Host "========================================================" -ForegroundColor Cyan
Write-Host ""

$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue

if ($mvnCmd) {
    Write-Host "[INFO] Maven detected. Compiling and starting via Maven..." -ForegroundColor Green
    mvn compile exec:java
} else {
    Write-Host "[INFO] Compiling directly with javac..." -ForegroundColor Yellow
    if (!(Test-Path "bin")) {
        New-Item -ItemType Directory -Path "bin" | Out-Null
    }

    $javaFiles = Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
    & javac -cp ".;lib/*;src/main/resources" -d bin $javaFiles

    if ($LASTEXITCODE -eq 0) {
        Write-Host "[INFO] Compilation successful! Launching..." -ForegroundColor Green
        & java -cp "bin;lib/*;src/main/resources" com.student.Main
    } else {
        Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
    }
}
