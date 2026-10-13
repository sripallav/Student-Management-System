@echo off
title Student Management System
color 0A

echo ========================================================
echo         STUDENT MANAGEMENT SYSTEM - LAUNCHER
echo ========================================================
echo.

where mvn >nul 2>nul
if %ERRORLEVEL% EQU 0 (
    echo [INFO] Apache Maven detected.
    echo [INFO] Building and starting project with Maven...
    echo.
    mvn compile exec:java
    goto end
)

echo [INFO] Maven not found in PATH, falling back to direct Java compiler...
if not exist "bin" mkdir "bin"

echo [INFO] Compiling Java source files...
javac -cp ".;lib/*;src/main/resources" -d bin src/main/java/com/student/model/*.java src/main/java/com/student/util/*.java src/main/java/com/student/config/*.java src/main/java/com/student/dao/*.java src/main/java/com/student/server/*.java src/main/java/com/student/Main.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Please check Java errors above.
    pause
    exit /b %ERRORLEVEL%
)

echo [INFO] Compilation successful!
echo [INFO] Starting Application Server...
java -cp "bin;lib/*;src/main/resources" com.student.Main

:end
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Server stopped with an error.
    pause
)
