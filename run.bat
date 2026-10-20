@echo off
echo ====================================================
echo  CareConnect - Clinic Appointment Management System
echo ====================================================
echo Compiling Java source files...
if not exist bin mkdir bin
dir /s /b src\*.java > sources.txt
javac -d bin @sources.txt
if errorlevel 1 (
    echo Compilation failed. Please inspect errors above.
    del sources.txt
    pause
    exit /b %errorlevel%
)
del sources.txt
echo Compilation successful. Launching CareConnect UI...
java -cp bin com.careconnect.Main
