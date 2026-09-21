# Journal des modifications (fork JDupdater)

[English](CHANGELOG.md) | **Français**

Ce fichier ne couvre que les changements faits dans ce fork personnel depuis sa divergence de
[Tobi823/ffupdater](https://github.com/Tobi823/ffupdater) (fork créé à partir de la version amont 81.0.0 / 179).
Pour l'historique du projet amont, consultez son dépôt.

## 86.0.1 (210)
**CI**
- Le workflow Android signe désormais l'APK lui-même avec `apksigner` (à la place de
  `r0adkll/sign-android-release@v1`, qui n'est plus maintenu et utilisait Node 20, la commande obsolète
  `set-output` et d'anciens build-tools 29.0.3). Il échoue si le certificat de signature n'est pas celui
  attendu par la vérification de la mise à jour automatique.
  `actions/checkout`, `actions/upload-artifact` et `softprops/action-gh-release` ont été mis à jour vers
  leurs versions Node 24.

**Nettoyage**
- Correction des avertissements du compilateur Kotlin : suppression des fonctions obsolètes inutilisées de
  `PowerSaveModeReceiver`, du paramètre inutilisé de `RootInstaller` (`appImpl`) ; le code d'état de
  `UserInteractionIsRequiredException` fait maintenant partie de son message ; `SessionInstaller` utilise
  `BundleCompat` au lieu de `Bundle.getParcelable` (obsolète) ; le `inline` inutile de `setVisibleOrGone`
  a disparu.

**Documentation**
- Ajout d'un README en français ([README.fr.md](README.fr.md)) et d'un journal des modifications en français
  ([CHANGELOG.fr.md](CHANGELOG.fr.md)). Les deux sont liés en haut des fichiers anglais, donc accessibles
  depuis la page GitHub du dépôt.

## 86.0.0 (209)
**Corrections**
- Correction de `FileDownloader.areDownloadsCurrentlyRunning()` : le compteur de téléchargements en cours
  n'était jamais incrémenté, donc les garde-fous « téléchargements en cours » (dialogue, tâches en arrière-plan)
  ne se déclenchaient jamais.
- Les applis qui changent de clé de signature (chaîne de certificats de la signature APK v3) ne sont plus
  rejetées avec « Found multiple signatures ». Le certificat enregistré est accepté s'il s'agit du certificat
  de signature ou s'il fait partie de la chaîne de certificats vérifiée.
- Correction des marges des barres système qui grandissaient à chaque distribution des insets de la fenêtre
  (écrans principal, de téléchargement, « tout mettre à jour » et de rapport de crash). Seul l'écran des
  réglages avait été corrigé auparavant.
- Correction du thème clair : la barre d'outils restait claire (les couleurs dynamiques Material You écrasent
  `colorSurface`) alors que son titre et ses icônes sont blancs, donc invisibles. La barre d'outils et la zone
  de la barre d'état sont maintenant toujours bleues, avec titre, icônes et icônes de la barre d'état en blanc.
  Le badge « mise à jour » des cartes d'applis a un fond plus foncé en thème clair pour un contraste lisible.
- `VersionCompareHelper` ne propose plus de rétrogradation quand la version majeure installée est bien plus
  récente que celle disponible (par ex. 153.x installée, 140.x disponible). La détection d'un « changement de
  schéma de version » (Tor Browser 128.x vers 14.x) est limitée à une baisse d'une version majeure à 3 chiffres
  vers une version majeure à 1 ou 2 chiffres.
- Ajout des 39 traductions françaises manquantes (écrans et dialogues « tout mettre à jour », Cromite,
  TrichromeLibrary, Thunderbird, K-9 Mail, FairEmail, IronFox, ...).
- Le message affiché quand JDupdater lui-même est signé avec une clé différente ne parle plus de F-Droid.

**Nettoyage**
- Suppression de la dépendance inutilisée `androidx.compose.material3`.
- Suppression de 6 logos inutilisés (Brave, Brave Beta/Nightly, Firefox Focus Beta, Privacy Browser, Thorium)
  et de deux entrées `<queries>` inutilisées (Thorium, Firefox Rocket).
- `dev/signatures/apk_signature.txt` et `docs/security_measures.md` décrivent maintenant la clé de signature
  de ce fork au lieu de celle du projet amont.
- Fusion de `CORRECTIF-VANADIUM.txt` dans ce journal (voir 85.x ci-dessous).

**CI / tests**
- CodeQL : passage à `codeql-action` v4 (la v3 sera dépréciée en décembre 2026), analyse uniquement
  `java-kotlin` et `python` (il n'y a pas de code JavaScript).
- Le workflow Android ne construit et ne publie plus d'APK debug.
- Correction des tests unitaires, qui ne compilaient plus depuis la suppression de Brave (`App.BRAVE`), et
  ajout de tests pour la chaîne de certificats de signature et la comparaison de versions.

## 85.0.0 - 85.1.8 (jusqu'à 207)
*(Les notes de version individuelles des versions 85.x n'ont pas été conservées, les changements sont donc
listés ensemble.)*
- Réajout de Firefox Klar (supprimé dans la 82.0.0), avec l'implémentation amont d'origine
  (archive Mozilla, paquet `org.mozilla.klar`) et des descriptions en anglais et en français.
- Correction de l'alerte CodeQL « Use of implicit PendingIntents » : l'intent de la notification
  d'optimisation de la batterie est maintenant explicite, et les PendingIntents des notifications sont
  immuables dès Android 6 (auparavant Android 12).
- Correction des réglages Vanadium invisibles : le `PreferenceScreen` imbriqué est devenu un
  `PreferenceCategory` (`root_preferences.xml`).
- Correction de l'écran de réglages vide après avoir validé ou annulé un dialogue : le clavier cassait le
  `RecyclerView`. `SettingsActivity` utilise maintenant `adjustNothing`, refait la mise en page à la fermeture
  du dialogue et les insets de la fenêtre ne s'accumulent plus.
- Correction des notifications de mise à jour manquantes pour Vanadium : GrapheneOS installe lui-même
  Vanadium/TrichromeLibrary (installateur = applis système), donc JDupdater les ignorait en arrière-plan.
  `wasInstalledByOtherApp()` renvoie maintenant `false` pour les deux (même signature GrapheneOS que les APK
  téléchargés par JDupdater).
- Correction de la fausse version `0.0.0.0-<sha>` et du « mise à jour disponible » permanent : le dernier
  commit de la branche GitLab est parfois un commit de configuration sans « version X » dans son titre.
  `GitLabBranchConsumer` interroge maintenant `/repository/commits?path=<APK>` et utilise le dernier commit
  qui a réellement modifié l'APK.
- Correction des téléchargements temporaires jamais nettoyés (amont #787) : le fichier temporaire s'appelle
  maintenant `<UUID>.part` (le point manquait) et `StorageCleaner.deleteOrphanedTempFiles()` supprime au
  démarrage les fichiers `<UUID>[.apk|.zip|.part|apk|zip]` de plus d'une heure.

## 84.0.1 (183)
- Ajout des traductions françaises pour Vanadium, WebLibre et TrichromeLibrary (auparavant uniquement en
  anglais).
- Ajout d'une option dans les réglages pour le sous-dossier `prebuilt/` de Vanadium, indépendante du réglage
  de la branche (GrapheneOS a déjà changé le nom de ce dossier selon les branches, par ex. `arm64` contre
  `arm64-multilib`).
- Nettoyage du README.md et des métadonnées F-Droid (`fastlane/`) : suppression d'entrées obsolètes dans la
  liste des navigateurs, d'un lien markdown cassé resté après la suppression de Brave, de l'ancienne marque
  « FFUpdater » et de liens pointant vers le dépôt amont au lieu de ce fork.

## 84.0.0 (182)
- Ajout de WebLibre (navigateur indépendant respectueux de la vie privée, construit sur le moteur Gecko de
  Mozilla, pas un fork de Firefox). Versions stables uniquement : les versions alpha avaient d'abord été
  essayées, mais elles utilisent un autre paquet et une autre signature, ce qui cassait les mises à jour
  lorsqu'elles étaient mélangées aux stables dans une même entrée d'appli.
- Suppression de Thorium (fin de vie, plus maintenu en amont).
- Fusion de TrichromeLibrary dans le flux d'installation de Vanadium : TrichromeLibrary n'apparaît plus comme
  une appli séparée. Sélectionner ou mettre à jour Vanadium installe/met à jour d'abord la bibliothèque, puis
  Vanadium lui-même, en une seule étape.

## 83.0.1 (181)
- Ajout de WebLibre avec les versions alpha incluses (annulé plus tard dans la 84.0.0, voir ci-dessus).

## 83.0.0 (181)
- Correction d'un bug de comparaison de versions : la vérification de la mise à jour de JDupdater lui-même
  indiquait toujours « mise à jour disponible », même à jour, car le tag de la release GitHub (`v83.0.0`) et
  le nom de version interne de l'appli (`83.0.0`) étaient comparés dans des formats différents.

## 82.0.0 / 82.0.1 / 82.0.2 (180)
*(82.0.1 et 82.0.2 sont des re-tags du même code après un problème de bit exécutable de `gradlew` en CI :
aucune différence fonctionnelle entre eux.)*
- Changement de marque de l'appli, de FFUpdater à **JDupdater** (nom affiché uniquement ; l'identifiant de
  paquet Android est resté inchangé pour que les installations existantes continuent de se mettre à jour
  sans problème).
- Ajout de **Vanadium** et **TrichromeLibrary** (le navigateur Chromium durci de GrapheneOS et sa
  bibliothèque partagée requise), téléchargés directement depuis le GitLab de GrapheneOS. La branche
  GrapheneOS à suivre est configurable dans les réglages.
- Suppression de Brave, Brave Beta, Brave Nightly, Firefox Focus, Firefox Focus Beta, Firefox Klar et
  Privacy Browser (applis non utilisées sur les appareils cibles de ce fork).
- Réduction des traductions incluses à l'anglais et au français uniquement, pour alléger l'appli.
- Mise en place de builds de release signés et de GitHub Releases via GitHub Actions (auparavant seulement
  des builds, sans releases ni signature du code).
- La vérification de la mise à jour automatique (l'entrée « JDupdater » dans la liste des applis) pointe
  maintenant vers le dépôt/les releases GitHub de ce fork au lieu de ceux du projet amont.
