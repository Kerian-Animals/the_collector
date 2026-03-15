## [1.1.0] - 2026-03-16

### Français

#### Ajouté

- Ajout d'un lore narratif servant de tutoriel dissimulé pour découvrir l'accès au royaume du Collectionneur.
- Ajout de plusieurs fragments de lore sous forme de livres écrits trouvables dans les caches du Collectionneur.
- Ajout d'une tab créative dédiée au mod regroupant les objets du Collectionneur, les fragments et les livres de lore.
- Ajout des textes de lore dans les fichiers de localisation `fr_fr` et `en_us`.

#### Modifié

- Les livres de lore utilisent désormais des identifiants techniques en anglais et des textes entièrement localisés.
- Les caches du Collectionneur injectent automatiquement un fragment narratif en plus du butin volé.

#### Corrigé

- Correction du stockage du butin dans le royaume du Collectionneur: les objets volés sont maintenant déposés dans les containers du vault au lieu d'apparaître au sol dans les cas normaux.
- Renforcement de la logique de création et de récupération des containers du vault pour fiabiliser le dépôt du loot.

### English

#### Added

- Added narrative lore that acts as a hidden tutorial for discovering how to access the Collector realm.
- Added multiple lore fragments as written books that can be found in Collector stashes.
- Added a dedicated creative tab for the mod containing Collector items, fragments, and lore books.
- Added lore text to the `fr_fr` and `en_us` localization files.

#### Changed

- Lore books now use English technical identifiers and fully localized text content.
- Collector stashes now automatically include a narrative fragment alongside stolen loot.

#### Fixed

- Fixed Collector realm loot storage: stolen items are now deposited into vault containers instead of appearing on the ground in normal cases.
- Hardened vault container creation and retrieval logic to make loot deposit reliable.

## [1.0.1] - 2026-03-15

### Français

#### Modifié

- Migration du projet de ForgeGradle vers NeoForge pour Minecraft 1.21.*.
- Remplacement du descripteur `mods.toml` par `neoforge.mods.toml`.
- Mise à jour des dépendances du mod pour accepter NeoForge `21.x`.
- Adaptation des imports, registres et handlers d'événements vers les API NeoForge.
- Ajout des runs Gradle NeoForge `runClient`, `runServer`, `runData` et `runGameTestServer`.

#### Corrigé

- Le jar n'est plus détecté comme un mod Forge ou un ancien mod NeoForge.
- Le mod peut maintenant être chargé sur les versions NeoForge 1.21.* compatibles avec la plage déclarée.

### English

#### Changed

- Migrated the project from ForgeGradle to NeoForge for Minecraft 1.21.*.
- Replaced `mods.toml` with `neoforge.mods.toml`.
- Updated mod dependencies to accept NeoForge `21.x`.
- Updated imports, registries, and event handlers to NeoForge APIs.
- Added NeoForge Gradle runs: `runClient`, `runServer`, `runData`, and `runGameTestServer`.

#### Fixed

- The jar is no longer detected as a Forge mod or an older NeoForge mod.
- The mod can now load on NeoForge 1.21.* versions compatible with the declared range.
