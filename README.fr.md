[English](README.md) | **Français**

# JDupdater

> **Ceci est un fork personnel de [Tobi823/ffupdater](https://github.com/Tobi823/ffupdater)**, que je maintiens pour mes propres appareils. Il ajoute la prise en charge de Vanadium/TrichromeLibrary de GrapheneOS et retire quelques navigateurs que je n'utilise pas. Une grande partie de ce fork (nouvelles intégrations d'applis, correctifs de CI/build, cette section du README, etc.) a été écrite avec l'aide de l'assistant IA Claude (Anthropic) plutôt qu'entièrement à la main. À utiliser à vos risques et périls ; il n'est ni affilié ni approuvé par le projet FFUpdater d'origine. Ce fork n'est pas publié sur F-Droid ; récupérez plutôt l'APK sur la [page des Releases](https://github.com/Jdont9/ffupdater_JD/releases).

Installe et met à jour les navigateurs suivants :

Logiciels de Mozilla ou basés sur Firefox :

- [Fennec F-Droid](https://f-droid.org/packages/org.mozilla.fennec_fdroid/)
- [Firefox Browser](https://play.google.com/store/apps/details?id=org.mozilla.firefox),
  [Firefox pour Android Beta](https://play.google.com/store/apps/details?id=org.mozilla.firefox_beta),
  [Firefox Nightly](https://play.google.com/store/apps/details?id=org.mozilla.firefox)
  ([dépôt GitHub](https://github.com/mozilla-mobile/firefox-android))
- [Firefox Klar](https://play.google.com/store/apps/details?id=org.mozilla.klar)
- [Iceraven](https://github.com/fork-maintainers/iceraven-browser)
- [Tor Browser](https://www.torproject.org/download),
  [Tor Browser Alpha](https://www.torproject.org/download/alpha/)
- [WebLibre](https://github.com/FaFre/WebLibre) (construit sur le moteur Gecko de Mozilla, pas un fork de Firefox)

Navigateurs qui valent mieux que Google Chrome :

- [Cromite](https://github.com/uazo/cromite)
- [Chromium](https://www.chromium.org/chromium-projects/)
- [DuckDuckGo Browser](https://github.com/duckduckgo/Android)
- [Vanadium](https://gitlab.com/grapheneos/platform_external_vanadium) (le Chromium durci de GrapheneOS ;
  le composant TrichromeLibrary requis est installé/mis à jour automatiquement avec lui, sans étape
  séparée)

Autres applications :

- [Orbot](https://github.com/guardianproject/orbot)

JDupdater vérifie les mises à jour en arrière-plan et les télécharge également. Les applis peuvent être mises
à jour sans interaction de l'utilisateur avec :

- Android 12 ou supérieur
- un smartphone rooté
- [Shizuku](https://shizuku.rikka.app/) / [Sui](https://github.com/RikkaApps/Sui) avec Android 6 ou supérieur

## FAQ

- En cliquant sur l'icône « i », vous voyez l'heure de la dernière vérification de mise à jour en arrière-plan
  réussie.
- Veuillez rouvrir JDupdater après l'avoir déplacé vers le stockage interne/externe.
- Quand le « mode économie d'énergie » est activé, le comportement de JDupdater change. Si le mode est activé
  depuis moins de 24 heures, JDupdater n'effectue aucune vérification de mise à jour en arrière-plan. En
  revanche, s'il reste activé plus longtemps, JDupdater vérifie les mises à jour en arrière-plan, mais ne les
  télécharge pas en arrière-plan.

## Comment contribuer

Ceci est un fork personnel avec un seul mainteneur : le [projet de traduction Weblate](https://hosted.weblate.org/projects/ffupdater)
et le [guide de contribution](HOW_TO_CONTRIBUTE.md) du projet amont s'appliquent à
[Tobi823/ffupdater](https://github.com/Tobi823/ffupdater), pas à ce fork. Y contribuer des traductions aide
le projet d'origine, pas celui-ci.

## Contributeurs du code source

La liste des contributeurs (héritée du projet amont) se trouve dans le
[README anglais](README.md#source-code-contributors).

## Documentation supplémentaire

Ces documents sont en anglais.

[Mesures de sécurité](docs/security_measures.md)

[Sources de téléchargement](docs/download_sources.md)

[Dépôt F-Droid / fichiers APK / autres canaux de distribution](docs/other_distribution_channels.md)

[Bibliothèques tierces](docs/3rd_party_libraries.md)

[Navigateurs obsolètes](docs/deprecated_browsers.md)

[Mainteneur](docs/maintainer.md)

[Mes objectifs](GOALS.md)

[Journal des modifications](CHANGELOG.fr.md)

## Dépôts Git

- Ce fork : https://github.com/Jdont9/ffupdater_JD
- Projet amont : https://github.com/Tobi823/ffupdater

## Licence

Le texte de la licence est conservé en anglais (version originale).

````
FFUpdater -- Updater for privacy friendly browser
Copyright (C) 2019-2023 Tobias Hellmann https://github.com/Tobi823
Copyright (C) 2015-2019 Boris Kraut <krt@nurfuerspam.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
````
