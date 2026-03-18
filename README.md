# The Collector

README par défaut : **français**.  
Version anglaise : [README.en.md](README.en.md).

## Présentation

**The Collector** est un mod NeoForge pour Minecraft 1.21.x centré sur une créature furtive qui vole les objets de valeur, les fait disparaître, puis laisse derrière elle des traces que le joueur peut apprendre à exploiter.

Le mod mélange :
- chasse à une entité rare ;
- investigation environnementale ;
- progression alchimique ;
- exploration de caches et d'entrées dimensionnelles.

## Boucle de jeu

1. Le `Collector` apparaît rarement en Overworld.
2. Il vole certains objets au sol ou dans les coffres, puis s'échappe.
3. Il laisse des `Collector Trace` dans le monde.
4. Le joueur découvre peu à peu comment exploiter cette résonance instable.
5. Cette expérimentation finit par ouvrir l'accès à des outils de traque plus avancés.
6. L'enquête mène ensuite vers les caches, les entrées et les lieux liés au Collector.

## Fonctionnalités actuelles

### Le Collector

- Entité `The Collector`
- Spawn rare en Overworld
- IA à états (`IDLE`, `SCOUTING`, `COLLECTING`, `ESCAPING`, `DESPAWNING`)
- Vol d'objets au sol
- Vol d'objets de valeur dans les coffres
- Inventaire interne limité
- Fuite, disparition et génération de cache persistante

### Monde et exploration

- Mini-caches générées en Overworld
- Fragments de reliques et pages de lore
- Traces du Collector laissées après ses passages
- Entrées et royaume du Collector

### Progression de résonance

- Traces exploitables laissées après le passage du Collector
- Système d'expérimentation autour d'une résonance instable
- Alambic dédié et manipulations alchimiques
- Objets intermédiaires, raffinage et stabilisation
- Aboutissement vers des outils de détection liés au Collector

### Guidage du joueur

- Arbre d'advancements dédié à la progression du Collector
- Tooltips de lore et d'indices sur les objets liés à la résonance
- Feedback contextuel pendant les étapes d'expérimentation
- GUI d'alambic calqué sur le brewing stand vanilla

## Configuration

Fichier généré :
- `run/config/the_collector-common.toml`

Exemples de réglages :
- `spawnChancePerCheck`
- `spawnCooldownTicks`
- `itemSearchRadius`
- `chestTheftEnabled`
- `chestSearchRadius`
- `maxStealsPerChest`
- `maxStolenStacks`
- `stashEnabled`
- `miniCacheEnabled`

## Développement

Lancer le client :

```powershell
./gradlew runClient
```

Compiler le mod :

```powershell
./gradlew build
```

## Compatibilité

Le mod cible `Minecraft 1.21.1 -> 1.21.5` avec `NeoForge 21.1.220 -> 21.5.x`.

## Version actuelle

- `1.2`

## Auteur

- Kérian

## Licence

Ce projet est sous licence **All Rights Reserved**.  
Voir [LICENSE](LICENSE).
