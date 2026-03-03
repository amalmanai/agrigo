@echo off
rem Script pour lancer AgriGame sur Windows (cmd.exe)
setlocal

echo ----- AgriGame Launcher -----

rem Vérifier si mvn est disponible
where mvn >nul 2>&1
if %ERRORLEVEL%==0 (
  echo Maven detecte. Lancement avec: mvn javafx:run
  mvn javafx:run
  if %ERRORLEVEL%==0 goto end
  echo mvn javafx:run a echoue.
) else (
  echo Maven non trouve dans le PATH.
)

echo Tentative de lancement via JAR ombre (target\*jar)
set "JAR="
for %%f in (target\*.jar) do set "JAR=%%f"
if defined JAR (
  echo JAR trouve: %JAR%
  echo Execution: java -jar "%JAR%"
  java -jar "%JAR%"
  if %ERRORLEVEL%==0 goto end
  echo Execution du JAR a echoue.
) else (
  echo Aucun JAR trouve dans le dossier target\. Compilez d'abord avec: mvn package
)

echo Impossible de demarrer le jeu automatiquement.
echo Utilisez IntelliJ pour lancer la classe Tests.MainAgriTest ou installez Maven et lancez: mvn javafx:run

:end
endlocal
pause
