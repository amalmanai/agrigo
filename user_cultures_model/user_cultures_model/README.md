# AgriGame — Comment démarrer le jeu

Ce petit guide décrit comment lancer rapidement le jeu AGRI dans ce projet (Windows / IntelliJ / Maven).

Prérequis minimaux
- JDK 17 (ou supérieur) installé et JAVA_HOME configuré.
- IntelliJ IDEA (recommandé). Si vous préférez la ligne de commande : Maven installé et dans le PATH.
- (Si JavaFX n'est pas intégré à votre JDK) SDK JavaFX 21 (https://openjfx.io/) — voir section VM options.

Méthode A — Lancer depuis IntelliJ (recommandé)
1. Ouvrez le projet dans IntelliJ.
2. Assurez-vous que Maven a fini d'importer les dépendances (fenêtre Maven). Si IntelliJ demande d"importer des changements du POM, acceptez.
3. Ouvrez `src/main/java/Tests/MainAgriTest.java`.
4. Clic droit → Run 'MainAgriTest' (ou placez le caret dans main() et exécutez).

Si vous voyez une erreur du type "JavaFX runtime components are missing" :
- Téléchargez le SDK JavaFX (ex. Gluon JavaFX 21) et notez son dossier `lib` (ex. `C:\javafx-sdk-21\lib`).
- Éditez la configuration Run (Run → Edit Configurations → votre configuration MainAgriTest) et ajoutez dans VM options :

--module-path "C:\chemin\vers\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml

(Remplacez le chemin par le vôtre.)

Méthode B — Lancer via Maven (cmd.exe)
1. Ouvrez une invite de commandes (cmd.exe) dans le dossier du projet (même dossier que `pom.xml`).
2. Si Maven est installé, exécutez :

mvn javafx:run

Le plugin Maven JavaFX lancera la classe `Tests.MainAgriTest` (configuration déjà ajoutée au `pom.xml`).

Méthode C — Construire un JAR exécutable et le lancer
1. `mvn package` (génère un JAR ombré via maven-shade-plugin).
2. Vérifiez `target/` pour le JAR (typiquement `crudpii-1.0-SNAPSHOT.jar` ou un JAR avec "shaded" dans son nom).
3. Lancez :

java -jar target\<NOM_DU_JAR>.jar

Script pratique (Windows)
- J'ai ajouté `run-game.bat` à la racine du projet qui tente automatiquement `mvn javafx:run` si Maven est disponible, sinon essaie d'exécuter un JAR ombré dans `target\`.

Remarques sur les dépendances et sécurité
- `pom.xml` contient plusieurs dépendances (JavaFX, Twilio, OpenCV, etc.). La vérification statique signale quelques avertissements de sécurité transitoires sur des librairies (c.-à-d. avertissements sur des CVE connus dans des dépendances transitives). Ces avertissements n'empêchent pas le jeu de démarrer, mais vous pouvez mettre à jour certaines dépendances si vous voulez corriger ces alertes.

Besoin d'aide ?
- Dites-moi si vous préférez que je :
  - configure un exécutable plus simple (ex : créer un script PowerShell plus complet),
  - modifie la configuration pour que le `javafx:run` utilise la classe principale réelle de l'application,
  - mette à jour des dépendances vulnérables identifiées (je proposerai des versions sûres).

Bonne chance — dites-moi quelle méthode vous souhaitez et je vous fournis les commandes exactes ou j'ajoute un script plus poussé.

