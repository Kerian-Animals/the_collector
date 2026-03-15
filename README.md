# The Collector

README par défaut : **français**.  
Version anglaise : [README.en.md](README.en.md).

## Présentation

**The Collector** est un mod Minecraft 1.21.* pour NeoForge qui ajoute une créature rare et furtive :
- elle repère les objets de valeur ;
- elle vole (au sol et dans les coffres) ;
- elle fuit ;
- elle cache son butin dans une cache persistante.

Le joueur peut ensuite traquer la cache et récupérer ses objets.

## Fonctionnalités V1

- Entité `The Collector`
- Spawn rare en Overworld (configurable)
- IA à états (`IDLE`, `SCOUTING`, `COLLECTING`, `ESCAPING`, `DESPAWNING`)
- Vol d'objets au sol
- Vol d'objets de valeur dans les coffres
- Inventaire interne limité
- Fuite puis disparition
- Création d'une cache persistante
- Objet de traque : `collector_compass`

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

## Lancer en Développement

```powershell
./gradlew runClient
```

## Compilation

```powershell
./gradlew build
```

## Auteur

- Kérian

## Licence

Ce projet est sous licence **All Rights Reserved**.
Voir [LICENSE](LICENSE).
