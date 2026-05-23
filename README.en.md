# The Collector

Default README: **French**.  
French version: [README.md](README.md).

## Overview

**The Collector** is a Minecraft 1.21.1 mod (Forge) that adds a rare and stealthy creature:
- it looks for valuable items
- it steals ground items
- it can steal chests (and their content)
- it escapes
- it transfers stolen loot to its own realm

## V1 Features

- `The Collector` entity (Enderman model + custom texture)
- Rare Overworld spawn (configurable)
- State-based AI (`IDLE`, `SCOUTING`, `COLLECTING`, `ESCAPING`, `DESPAWNING`)
- Ground item theft
- Chest theft with loot transfer
- Escape and despawn flow
- Dedicated dimension stash: `the_collector:collector_realm`
- Temporary 6x6 vault room (V1)
- Random Overworld entries to the realm
- Tracking systems:
  - compass (`collector_compass`)
  - locate commands
- Dangerous realm access quest:
  - rare relic fragments
  - `collector_catalyst` crafting
  - ritual activation of entries
  - hostile backlash on activation

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

## Commands

- Player:
  - `/collector locate`
  - `/collector locate latest`
  - `/collector locate nearest`
  - `/collector locate all`
  - `/collector entry locate`
- Admin (OP level 2):
  - `/collector entry create`
  - `/collector spawn_static` (debug spawn, fixed, no despawn)

Alias available: `/thecollector ...`

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
- Kérian_Animals
- kerian_animals

## License

This project is licensed under **All Rights Reserved**.
See [LICENSE](LICENSE).
