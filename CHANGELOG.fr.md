# Journal des modifications (fork JDupdater)

[English](CHANGELOG.md) | **Français**

Ce fichier ne couvre que les changements faits dans ce fork personnel depuis sa divergence de
[Tobi823/ffupdater](https://github.com/Tobi823/ffupdater) (fork créé à partir de la version amont 81.0.0 / 179).
Pour l'historique du projet amont, consultez son dépôt.

## 86.1.2 (213)
**Corrections**
- Le lien « page du projet » affiché en haut de la fiche d'information de l'appli (bouton « i ») pointait
  vers une page marketing de Mozilla (`mozilla.org/firefox/browsers/mobile/android/`) au lieu du vrai code
  source, pour Firefox Release, Firefox Beta et Firefox Nightly. Il pointe maintenant vers le vrai dépôt,
  `github.com/mozilla-firefox/firefox`.
- La page du projet de Firefox Klar pointait vers `github.com/mozilla-mobile/firefox-android`, qui a été
  archivé le 17/06/2024 lors de la fusion de Fenix/Focus/Klar et Android Components dans le dépôt unifié
  mozilla-central. Elle pointe maintenant vers le même dépôt actuel que ci-dessus.
- La page du projet de Tor Browser et Tor Browser Alpha pointait vers une page de téléchargement de
  torproject.org au lieu de la source. Elle pointe maintenant vers le vrai dépôt source,
  `gitlab.torproject.org/tpo/applications/tor-browser`.

## 86.1.1 (212)
**Corrections**
- Correction de la ligne « Disponible » de l'écran principal qui se coupait maladroitement en plein milieu
  de la date pour les numéros de version longs ou les versions plus anciennes (ex. « Disponible :
  153.0.8010.52.0 (Il y a 4 jours, 23:19) » qui débordait sur deux lignes). La date relative est maintenant
  sur sa propre ligne (plus petite) sous la version, avec un format plus court et abrégé (« il y a 4 h » /
  « 5 sept. » au lieu de « il y a 4 heures, 13:16 » / « 5 sept., 16:47 »).
- Correction de la carte de Firefox Nightly qui affichait un horodatage de build brut, non formaté, entre
  crochets (ex. `[2026-09-22T21:13:42]`) à côté de la version installée et de la version disponible, en plus
  de la date relative déjà affichée - ce qui faisait déborder sa carte sur 3-4 lignes et coupait le texte,
  contrairement à toutes les autres applis. Cet horodatage interne (utilisé seulement pour distinguer deux
  builds Nightly qui partagent le même numéro de version) n'est désormais plus affiché sur la carte.

## 86.1.0 (211)
**Retraits**
- Retrait de Vivaldi : il n'est que partiellement open source (l'interface Android/desktop est propriétaire,
  seul le cœur Chromium est ouvert).
- Retrait de FairEmail, K-9 Mail, Thunderbird et Thunderbird Beta : ce sont des clients mail, pas des
  navigateurs. (Orbot reste : ce n'est pas non plus un navigateur, mais il est activement maintenu et était
  déjà listé sous « Autres applications », pas dans les catégories de navigateurs.)

**Ajouts**
- Les applis installées dont la dernière version connue date de 90 jours ou plus affichent maintenant un
  badge gris « Obsolète » sur leur carte, à côté du badge bleu « MàJ » existant, pour que ce soit visible
  sans avoir à ouvrir la fiche d'information de l'appli.
- L'écran « Ajouter une appli » avait déjà une section « Navigateurs en fin de vie » inutilisée
  (`DisplayCategory.EOL`). Les applis pas encore installées dont la dernière version connue date de 90 jours
  ou plus y sont maintenant affichées automatiquement au lieu de leur catégorie habituelle (même règle des
  90 jours que le badge ci-dessus), sans avoir à les coder en dur dans cette catégorie. Les applis suivies
  par branche plutôt que par versions datées (Vanadium, TrichromeLibrary) ne sont jamais concernées, faute
  de date de publication pour calculer un âge.

**Corrections**
- Correction des APK de mise à jour de JDupdater mis en cache (`FFUPDATER_<...>.apk` dans
  `Android/data/de.marmaro.krt.ffupdater/files/download`) qui n'étaient jamais supprimés : l'auto-installation
  silencieuse de JDupdater nécessite toujours une confirmation de l'utilisateur (sans root ni Shizuku), donc
  `background__delete_cache_if_install_failed` (désactivé par défaut) gardait pour toujours chaque
  téléchargement de mise à jour échoué, et les téléchargements lancés depuis la notification/`DownloadActivity`
  ne nettoyaient jamais les anciennes versions mises en cache (seul `AppUpdater`, le chemin en arrière-plan, le
  faisait). `DownloadActivity` supprime maintenant les APK en cache des anciennes versions de l'appli en cours
  de téléchargement avant de démarrer un nouveau téléchargement, quels que soient ces réglages.
  Les fichiers déjà accumulés doivent encore être supprimés une fois à la main (par ex. avec un gestionnaire
  de fichiers) ; ceci empêche seulement que de nouveaux s'accumulent.
  Cela concerne toutes les applis, pas seulement JDupdater : une installation en arrière-plan qui échoue
  (rare pour les navigateurs classiques, qui ont de toute façon besoin d'Android 12+/root/Shizuku pour
  s'installer silencieusement) ne laisse plus non plus son cache derrière elle. Vous pouvez toujours
  redésactiver ce réglage dans les préférences.

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
