@echo off
rem Creates and seeds the free_clinic database. Run from the project root:  db\setup_db.bat
rem Requires the "mysql" command to be on PATH. You will be prompted for the MySQL password.
set /p DBUSER=MySQL user [root]:
if "%DBUSER%"=="" set DBUSER=root
echo.
echo Loading schema, logic, seed data and extensions...
mysql -u %DBUSER% -p < db\01_schema.sql   || goto :fail
mysql -u %DBUSER% -p < db\02_logic.sql    || goto :fail
mysql -u %DBUSER% -p < db\03_seed.sql     || goto :fail
mysql -u %DBUSER% -p < db\05_extensions.sql || goto :fail
echo.
echo Done. Database free_clinic is ready.
exit /b 0
:fail
echo.
echo Setup failed - see the error above.
exit /b 1
