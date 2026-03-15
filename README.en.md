# The Collector

Default README: **French**.  
French version: [README.md](README.md).

## Overview

**The Collector** is a NeoForge mod for Minecraft 1.21.* that adds a rare and stealthy creature:
- it looks for valuable items
- it steals (ground items and chest items)
- it escapes
- it hides the loot inside a persistent stash

Then the player can track the stash and recover the stolen items.

## V1 Features

- `The Collector` entity
- Rare Overworld spawn (configurable)
- State-based AI (`IDLE`, `SCOUTING`, `COLLECTING`, `ESCAPING`, `DESPAWNING`)
- Ground item theft
- Valuable chest item theft
- Limited internal inventory
- Escape and despawn behavior
- Persistent stash creation
- Tracking item: `collector_compass`

## Configuration

Generated config file:
- `run/config/the_collector-common.toml`

Main settings include:
- `spawnChancePerCheck`
- `spawnCooldownTicks`
- `itemSearchRadius`
- `chestTheftEnabled`
- `chestSearchRadius`
- `maxStealsPerChest`
- `maxStolenStacks`
- `stashEnabled`

## Run in dev

```powershell
./gradlew runClient
```

## Build

```powershell
./gradlew build
```

## Author

- Kérian

## License

This project is licensed under **All Rights Reserved**.
See [LICENSE](LICENSE).
