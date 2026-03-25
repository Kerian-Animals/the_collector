# The Collector

README par défaut : **français**.  
Version anglaise : [README.en.md](README.en.md).

## Aperçu

**The Collector** est un mod **NeoForge** pour **Minecraft 1.21.x** centré sur une créature rare qui observe, vole, fuit, puis laisse derrière elle des indices exploitables.

Le projet mélange quatre axes de jeu :

- chasse à une entité furtive ;
- lecture du terrain et investigation ;
- progression alchimique autour de la résonance ;
- exploration de caches, ruines et accès dimensionnels.

## Boucle de progression

1. Le `Collector` apparaît ponctuellement dans l'Overworld.
2. Il repère des objets de valeur au sol ou dans des coffres.
3. Après un vol, il s'échappe et sème des `Collector Trace`.
4. Le joueur apprend à capturer, distiller et stabiliser cette résonance.
5. Cette progression débloque de nouveaux outils de traque.
6. L'enquête mène vers des caches, des entrées rituelles et le royaume du Collector.

## Contenu du mod

### Entité et comportements

- entité `The Collector` avec IA à états ;
- vol d'objets au sol ;
- pillage limité de coffres ;
- inventaire interne plafonné ;
- fuite, disparition et conversion du butin en cache persistante.

### Monde et exploration

- mini-caches générées en Overworld ;
- traces déposées après les vols ;
- fragments de reliques et éléments de lore ;
- structures d'entrée menant au contenu avancé du mod ;
- royaume du Collector avec dépôt de butin persistant.

### Résonance et alchimie

- capture d'une résonance instable depuis les traces ;
- chaîne de transformation autour de l'alambic ;
- cristallisation et raffinage ;
- objets intermédiaires servant à la progression ;
- outils de localisation liés au Collector.

### Guidage du joueur

- arbre d'advancements dédié ;
- messages contextuels et tooltips de progression ;
- interface d'alambic inspirée du `Brewing Stand` vanilla ;
- intégration **JEI** optionnelle pour exposer les recettes et étapes spécifiques du mod.

## Commandes

Le mod enregistre deux racines équivalentes :

- `/thecollector`
- `/collector`

Sous-commandes disponibles :

- `/collector locate latest`
- `/collector locate nearest`
- `/collector locate all`
- `/collector entry locate`
- `/collector entry create`

## Configuration

Fichier généré :

- `run/config/the_collector-common.toml`

Principaux réglages :

- `enabled`
- `spawnChancePerCheck`
- `spawnCooldownTicks`
- `spawnMinDistance`
- `spawnMaxDistance`
- `nightOnlySpawn`
- `itemSearchRadius`
- `chestTheftEnabled`
- `chestSearchRadius`
- `maxStealsPerChest`
- `collectorInventorySlots`
- `maxStolenStacks`
- `escapeDistance`
- `maxPresenceTicks`
- `stashEnabled`
- `bonusLootEnabled`
- `miniCacheEnabled`

## Architecture rapide

Quelques points d'entrée utiles pour reprendre le code :

- `TheCollectorMod` : enregistrement du contenu et des handlers NeoForge ;
- `CollectorEntity` : logique centrale de l'entité et cycle de vie ;
- `CollectorStashManager` : création et persistance du butin volé ;
- `CollectorEntryManager` : génération et maintenance des structures d'entrée ;
- `CollectorCatalystItem` : activation rituelle d'une entrée ;
- `TheCollectorConfig` : configuration commune du mod.

## Développement

Lancer le client de dev :

```powershell
./gradlew runClient
```

Compiler le mod :

```powershell
./gradlew build
```

## Compatibilité

- `Minecraft 1.21.1 -> 1.21.5`
- `NeoForge 21.1.220 -> 21.5.x`

### JEI

`JEI` n'est pas requis pour utiliser le mod.

S'il est installé, il affiche notamment :

- la capture de la résonance ;
- les recettes d'alambic ;
- les étapes de cristallisation ;
- le rituel d'activation des entrées.

## Auteur

- Kérian

## Licence

Projet distribué sous licence **MIT**.  
Voir [LICENSE](LICENSE).
