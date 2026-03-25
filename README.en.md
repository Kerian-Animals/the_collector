# The Collector

Default README: **French**.  
French version: [README.md](README.md).

## Overview

**The Collector** is a **NeoForge** mod for **Minecraft 1.21.x** built around a rare creature that scouts, steals, escapes, and leaves behind exploitable clues.

The mod combines four gameplay pillars:

- hunting a stealthy entity;
- environmental investigation;
- alchemical progression built around resonance;
- exploration of stashes, ruins, and dimensional access points.

## Progression Loop

1. The `Collector` appears from time to time in the Overworld.
2. It targets valuable items on the ground or inside chests.
3. After stealing, it escapes and leaves `Collector Trace` blocks behind.
4. The player learns how to capture, distill, and stabilize that resonance.
5. This progression unlocks more reliable tracking tools.
6. The investigation eventually leads to stashes, ritual entry points, and the Collector realm.

## Mod Content

### Entity and Behaviors

- `The Collector` entity with a state-driven AI;
- ground item theft;
- limited chest stealing;
- capped internal inventory;
- escape and despawn flow that turns stolen loot into persistent stashes.

### World and Exploration

- mini-cache generation in the Overworld;
- residue trails left after theft events;
- relic fragments and lore items;
- entry structures tied to late progression;
- a Collector realm used as a persistent loot destination.

### Resonance and Alchemy

- unstable resonance capture from traces;
- alembic-based processing chain;
- crystallization and refinement steps;
- intermediate progression items;
- tracking tools tied to Collector activity.

### Player Guidance

- dedicated advancement tree;
- contextual feedback and progression tooltips;
- an alembic GUI modeled after the vanilla `Brewing Stand`;
- optional **JEI** integration for recipes and progression-specific processes.

## Commands

The mod registers two equivalent roots:

- `/thecollector`
- `/collector`

Available subcommands:

- `/collector locate latest`
- `/collector locate nearest`
- `/collector locate all`
- `/collector entry locate`
- `/collector entry create`

## Configuration

Generated file:

- `run/config/the_collector-common.toml`

Main settings:

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

## Architecture Summary

Useful entry points when navigating the codebase:

- `TheCollectorMod`: content registration and NeoForge event wiring;
- `CollectorEntity`: core entity lifecycle and behavior;
- `CollectorStashManager`: stolen loot conversion and persistence;
- `CollectorEntryManager`: entry generation and structure maintenance;
- `CollectorCatalystItem`: ritual activation of an entry;
- `TheCollectorConfig`: shared mod configuration.

## Development

Detailed developer guide:

- [docs/DEVELOPER_GUIDE.en.md](docs/DEVELOPER_GUIDE.en.md)

Run the dev client:

```powershell
./gradlew runClient
```

Build the mod:

```powershell
./gradlew build
```

## Compatibility

- `Minecraft 1.21.1 -> 1.21.5`
- `NeoForge 21.1.220 -> 21.5.x`

### JEI

`JEI` is not required for the mod to work.

If installed, it can display:

- resonance capture;
- alembic recipes;
- crystallization steps;
- entry activation rituals.

## Author

- Kérian

## License

This project is licensed under **MIT**.  
See [LICENSE](LICENSE).
