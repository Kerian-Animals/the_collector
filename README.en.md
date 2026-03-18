# The Collector

Default README: **French**.  
French version: [README.md](README.md).

## Overview

**The Collector** is a NeoForge mod for Minecraft 1.21.x built around a stealthy creature that steals valuable items, disappears, and leaves behind traces the player can learn to study.

The mod combines:
- stalking a rare entity
- environmental investigation
- alchemical progression
- stash and dimension-oriented exploration

## Gameplay Loop

1. The `Collector` rarely appears in the Overworld.
2. It steals selected items from the ground or from chests, then escapes.
3. It leaves `Collector Trace` blocks behind.
4. The player gradually learns how to exploit that unstable resonance.
5. That experimentation eventually unlocks more advanced tracking tools.
6. The investigation then leads toward stashes, entries, and Collector-related locations.

## Current Features

### The Collector

- `The Collector` entity
- Rare Overworld spawn
- State-driven AI (`IDLE`, `SCOUTING`, `COLLECTING`, `ESCAPING`, `DESPAWNING`)
- Ground item theft
- Valuable chest item theft
- Limited internal inventory
- Escape, despawn, and persistent stash generation

### World and Exploration

- Overworld mini-caches
- Relic fragments and lore pages
- Collector traces left behind after thefts
- Collector entries and realm access

### Resonance Progression

- Exploitable traces left behind after the Collector passes through
- An experimentation system built around unstable resonance
- A dedicated alembic and alchemical handling
- Intermediate items, refinement, and stabilization
- An end point that unlocks Collector-oriented detection tools

### Player Guidance

- Dedicated Collector advancement chain
- Lore and hint tooltips on resonance-related items
- Contextual feedback during the experimentation steps
- Alembic GUI styled after the vanilla brewing stand

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
- `miniCacheEnabled`

## Development

Run the client:

```powershell
./gradlew runClient
```

Build the mod:

```powershell
./gradlew build
```

## Compatibility

The mod targets `Minecraft 1.21.1 -> 1.21.5` with `NeoForge 21.1.220 -> 21.5.x`.

## Current Version

- `1.2`

## Author

- Kérian

## License

This project is licensed under **All Rights Reserved**.  
See [LICENSE](LICENSE).
