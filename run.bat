@echo off
echo ====================================================
echo  CareConnect - Clinic Appointment Management System
echo ====================================================
echo Compiling Java source files...
if not exist bin mkdir bin
if not exist lib mkdir lib
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
rem lib\* must hold mysql-connector-j-*.jar for the live MySQL database.
rem Without it the app still runs, but in in-memory mode.
java -cp "bin;lib\*" com.careconnect.Main
