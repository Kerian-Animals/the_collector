# The Collector

README par défaut : **français**.  
English version: [README.en.md](README.en.md).

## Présentation

**The Collector** est un mod Minecraft 1.21.1 (Forge) qui ajoute une créature rare et furtive :
- elle repere les objets de valeur
- elle vole (au sol et dans les coffres)
- elle fuit
- elle cache son butin dans une cache persistante

Le joueur peut ensuite traquer la cache et récupérer ses objets.

## Features V1

- Entite `The Collector`
- Spawn rare en Overworld (configurable)
- IA à états (`IDLE`, `SCOUTING`, `COLLECTING`, `ESCAPING`, `DESPAWNING`)
- Vol d'objets au sol
- Vol d'objets de valeur dans les coffres
- Inventaire interne limite
- Fuite puis disparition
- Creation d'une cache persistante
- Item de traque: `collector_compass`

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

## Lancer en Dev

```powershell
./gradlew runClient
```

## Build

```powershell
./gradlew build
```

## Auteur

- Kérian
- Kérian_Animals
- kerian_animals

## Licence

Ce projet est sous licence **All Rights Reserved**.
Voir [LICENSE](LICENSE).
