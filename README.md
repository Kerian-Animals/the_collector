# The Collector

README par défaut : **français**.  
Version anglaise : [README.en.md](README.en.md).

## Présentation

**The Collector** est un mod Minecraft 1.21.1 (Forge) qui ajoute une créature rare et furtive :
- elle repère les objets de valeur ;
- elle vole des objets au sol ;
- elle peut voler des coffres (et leur contenu) ;
- elle fuit ;
- elle transfère le butin dans son royaume.

## Fonctionnalités V1

- Entité `The Collector` (modèle Enderman + texture custom)
- Spawn rare en Overworld (configurable)
- IA à états (`IDLE`, `SCOUTING`, `COLLECTING`, `ESCAPING`, `DESPAWNING`)
- Vol d'objets au sol
- Vol de coffres et butin associé
- Fuite puis disparition
- Cache dans une dimension dédiée : `the_collector:collector_realm`
- Salle de coffres 6x6 (V1) dans le royaume
- Entrées Overworld aléatoires vers le royaume
- Système de traque :
  - boussole (`collector_compass`)
  - commandes de localisation
- Accès dangereux au royaume :
  - reliques rares à obtenir
  - craft du `collector_catalyst`
  - rituel d'activation de l'entrée
  - vague hostile à l'activation

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

## Commandes

- Joueur :
  - `/collector locate`
  - `/collector locate latest`
  - `/collector locate nearest`
  - `/collector locate all`
  - `/collector entry locate`
- Admin (OP niveau 2) :
  - `/collector entry create`
  - `/collector spawn_static` (spawn debug, fixe, sans disparition)

Alias disponible : `/thecollector ...`

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
- Kérian_Animals
- kerian_animals

## Licence

Ce projet est sous licence **All Rights Reserved**.
Voir [LICENSE](LICENSE).
