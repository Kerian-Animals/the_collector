## [1.0.1] - 2026-03-15

### Français

#### Modifié

- Migration du projet de ForgeGradle vers NeoForge pour Minecraft 1.21.1.
- Remplacement du descripteur `mods.toml` par `neoforge.mods.toml`.
- Mise à jour des dépendances du mod pour accepter NeoForge `21.1.x`.
- Adaptation des imports, registres et handlers d'événements vers les API NeoForge.
- Ajout des runs Gradle NeoForge `runClient`, `runServer`, `runData` et `runGameTestServer`.

#### Corrigé

- Le jar n'est plus détecté comme un mod Forge ou un ancien mod NeoForge.
- Le mod peut maintenant être chargé sur les versions NeoForge 1.21.1 compatibles avec la plage déclarée.

### English

#### Changed

- Migrated the project from ForgeGradle to NeoForge for Minecraft 1.21.1.
- Replaced `mods.toml` with `neoforge.mods.toml`.
- Updated mod dependencies to accept NeoForge `21.1.x`.
- Updated imports, registries, and event handlers to NeoForge APIs.
- Added NeoForge Gradle runs: `runClient`, `runServer`, `runData`, and `runGameTestServer`.

#### Fixed

- The jar is no longer detected as a Forge mod or an older NeoForge mod.
- The mod can now load on NeoForge 1.21.1 versions compatible with the declared range.
