# Developer Guide

## Purpose

This document is the main onboarding guide for developers working on **The Collector**.

It covers:

- the overall architecture;
- gameplay flows as implemented in code;
- persistence;
- client / server separation;
- extension points;
- pitfalls already encountered in the project.

The goal is not to describe every class line by line, but to make it possible to change the mod without breaking hidden invariants.

## Technical Stack

- `Minecraft 1.21.x`
- `NeoForge 21.x`
- `Java 21`
- Gradle build with the `net.neoforged.moddev` plugin
- optional `JEI` integration

Main commands:

```powershell
./gradlew runClient
./gradlew runServer
./gradlew build
./gradlew gameTestServer
```

Useful build files:

- `build.gradle`
- `gradle.properties`
- `src/main/resources/META-INF/neoforge.mods.toml`

## High-Level Overview

The mod is built around a server-driven loop centered on a rare entity, `CollectorEntity`, that:

1. appears in the Overworld;
2. searches for valuable items or chests;
3. steals content;
4. escapes;
5. turns its loot into a persistent stash;
6. drives player progression through traces, alchemy, and the dimension.

In practice, the project is split into six subsystems:

- bootstrap and registries;
- Collector AI and lifecycle;
- traces, resonance, and alembic processing;
- persistence for stashes, entries, and timed states;
- dimension and vault;
- player guidance through advancements, lore, and JEI.

## Package Map

### Core

- `fr.kerian_animals.thecollector.TheCollectorMod`
- `fr.kerian_animals.thecollector.config`
- `fr.kerian_animals.thecollector.registry`

### Server Gameplay

- `entity`
- `entity.goal`
- `spawn`
- `stash`
- `world`
- `world.dimension`
- `world.vault`
- `item`
- `advancement`

### UI and Client

- `client`
- `client.jei`
- `menu`

### Static Content

- `src/main/resources/assets/the_collector`
- `src/main/resources/data/the_collector`

## Mod Bootstrap

Entry point:

- `src/main/java/fr/kerian_animals/thecollector/TheCollectorMod.java`

Responsibilities:

- register blocks, items, entities, menus, and block entities;
- register the shared config;
- connect runtime event handlers to the NeoForge bus.

The most important runtime handlers are:

- `CollectorAdvancementManager`
- `CollectorSpawnHandler`
- `CollectorMiniCacheManager`
- `CollectorTraceInteractionHandler`

Practical rule:

- purely declarative content belongs in registries;
- real-time gameplay reactions belong in handlers or entities;
- avoid hiding gameplay logic inside registry classes.

## Registries

Registries are centralized in `registry/`.

Main classes:

- `ModItems`
- `ModBlocks`
- `ModEntities`
- `ModBlockEntities`
- `ModMenus`
- `ModCreativeTabs`

When you add a playable feature, most changes go through three layers:

1. registry declaration;
2. associated resource in `assets/` or `data/`;
3. gameplay wiring on the server or client side.

Concrete example for a new interactive block:

1. register the block in `ModBlocks`;
2. register its `BlockItem` in `ModItems` if needed;
3. register its `BlockEntity` in `ModBlockEntities` if it has persistent state;
4. register a menu in `ModMenus` if it needs a UI;
5. add blockstate, model, textures, lang, and recipes;
6. implement tick / interaction logic in code.

## Collector Lifecycle

Core class:

- `src/main/java/fr/kerian_animals/thecollector/entity/CollectorEntity.java`

States:

- `IDLE`
- `SCOUTING`
- `COLLECTING`
- `ESCAPING`
- `DESPAWNING`

Main goals:

- `CollectorScoutGoal`
- `CollectorCollectItemGoal`
- `CollectorStealChestGoal`
- `CollectorEscapeGoal`

### Server Flow

1. `CollectorSpawnHandler` attempts periodic spawning in the Overworld.
2. `CollectorEntity` enters its tick loop.
3. Goals pick either an item target or a chest target.
4. The Collector stores stolen items in its internal inventory.
5. An exit condition forces escape:
   - player too close;
   - inventory full;
   - presence timer exceeded;
   - entity attacked;
   - chest fully raided.
6. In `DESPAWNING`, the entity delegates to `CollectorStashManager`.

### Important Invariants

- the entity must never despawn through vanilla distance logic;
- the stash is created at logical despawn time, not earlier;
- `lastTheftPos` is used for trace generation and some advancement triggers;
- debug flags must not leak into standard gameplay behavior.

### Performance

Historically, chest search was the most expensive part.

The current `CollectorStealChestGoal` implementation:

- avoids a full cube scan on every search;
- uses ring-based sampling;
- maintains a local cache of candidate chests;
- invalidates that cache when the Collector moves too far or when chests become invalid.

If you change this logic, keep the following target:

- bounded cost per tick;
- no reliance on massive block entity scans across a large area.

## Collector Spawn

Class:

- `src/main/java/fr/kerian_animals/thecollector/spawn/CollectorSpawnHandler.java`

Current conditions:

- mod enabled;
- Overworld only;
- night only if configured;
- checked every `200` ticks;
- per-level cooldown;
- configurable random chance;
- anchored to a non-spectator player;
- position picked in a configurable distance ring.

The handler keeps `lastSpawnByLevel` in runtime memory. It is not persisted across restarts, which is acceptable for this gameplay loop.

## Loot, Stashes, and Persistence

Main classes:

- `CollectorStashManager`
- `CollectorSavedData`
- `CollectorStash`
- `CollectorEntry`
- `CollectorMiniCache`

### Where State Is Stored

`CollectorSavedData` is always loaded and saved from the Overworld, even when the referenced content lives elsewhere.

This is an important design choice:

- a single source of truth;
- no duplication across dimensions;
- easier retrieval for commands and progression systems.

The `SavedData` stores:

- stashes;
- the last stash seen by each player;
- dimensional entries;
- mini-caches.

### Creating a Stash

When the Collector despawns:

1. its stolen inventory is copied;
2. lore and optional bonus loot may be added;
3. if the Collector dimension is available, loot is deposited into the vault;
4. otherwise, a physical Overworld stash is generated;
5. the structure is recorded in `CollectorSavedData`;
6. nearby players receive a pointer to that stash.

### Known Pitfall

The vault must not be rebuilt destructively after loot has been deposited.

The bug that already happened was:

- items were deposited correctly;
- the room was rebuilt on dimension entry;
- empty chests were recreated;
- players concluded that the vault had never received loot.

The current `CollectorVaultManager` implementation preserves existing storage slots.

## Dimension and Travel

Classes:

- `ModDimensions`
- `CollectorEntryManager`
- `CollectorDimensionTravelHandler`
- `CollectorVaultManager`

Associated resources:

- `data/the_collector/dimension/collector_realm.json`
- `data/the_collector/dimension_type/collector_realm_type.json`

### Overworld Entries

`CollectorEntryManager` handles:

- entry structure creation;
- rebuilding if the structure was broken;
- locating the nearest entry;
- indirect ritual activation through `CollectorCatalystItem`.

An entry has two states:

- it exists;
- it is activated or not.

Activation is stored in `CollectorSavedData`, not in the block structure itself.

### Dimensional Travel

`CollectorDimensionTravelHandler` watches the server-side player while crouching.

The flow is:

1. check travel cooldown;
2. apply a throttled check cadence;
3. if the player stands on an active entry in the Overworld, teleport to the vault;
4. if the player stands on the exit pad in the realm, return to the last used entry.

State stored in `player.getPersistentData()`:

- travel cooldown;
- delay between crouch checks;
- position of the last used entry.

### Vault

`CollectorVaultManager` handles:

- the central room;
- storage slot positions;
- loot deposition;
- fallback item dropping if all containers are full.

Invariants:

- never wipe existing storage containers;
- guarantee a safe arrival point;
- avoid unstable decorative blocks that may fall during generation.

## Traces, Resonance, and Alchemy

Classes:

- `CollectorTraceInteractionHandler`
- `CollectorTraceBlock`
- `ResonanceCauldronSavedData`
- `AlembicRecipes`
- `AlembicBlock`
- `AlembicBlockEntity`
- `AlembicMenu`
- `AlembicScreen`

### Progression Flow

1. the Collector steals something and creates a trace;
2. the player captures the trace with a bottle;
3. this produces `Unstable Resonance`;
4. resonance is processed through the alembic or the cauldron depending on the step;
5. refined items unlock the catalyst, the crystal, and then the compass.

### Alembic

`AlembicBlockEntity` is the core processing unit.

Responsibilities:

- manage 5 slots;
- validate inputs;
- maintain brewing-stand-like progress for the UI;
- consume fuel;
- transform bottle slots;
- synchronize menu data;
- emit server-side particles and sounds.

Recipes are currently encoded in `AlembicRecipes` instead of being fully data-driven.

Consequences:

- simple to maintain for a small recipe count;
- more rigid if the content grows;
- possible future migration to a proper recipe serializer system.

## Advancements, Hints, and JEI

Classes:

- `CollectorAdvancementManager`
- `CollectorAdvancementHelper`
- `CollectorLoreBookFactory`
- `client.jei.*`

### Advancements

`CollectorAdvancementManager` runs server-side on player tick.

Current optimization strategy:

- a single inventory pass when multiple item-based advancements are still pending;
- a persistent cache of already unlocked advancements in player `PersistentData`;
- short-circuiting of proximity checks when already completed.

When adding an advancement:

1. create the JSON in `data/the_collector/advancement/`;
2. add code-side triggering if the advancement is not purely data-driven;
3. add translations in `lang/`;
4. add a hint or contextual message if needed.

### JEI

JEI integration is intentionally optional.

Entry point:

- `client/jei/JeiPluginImpl.java`

Current categories:

- trace capture;
- alembic recipes;
- cauldron crystallization;
- entry ritual.

Rule:

- no gameplay logic must depend on JEI;
- JEI exists only to expose mod processes to the player.

## Client / Server Separation

The project is primarily server-driven. Gameplay logic must stay server-side unless explicit client rendering is required.

### Server Side

- spawn;
- AI;
- loot;
- structures;
- persistence;
- progression;
- dimensional travel;
- actual block and item interactions.

### Client Side

- Collector rendering;
- alembic screen;
- visual compass behavior;
- JEI categories.

Practical rule:

- if the logic changes world state, it belongs on the server;
- if the logic only exists for display, it belongs on the client;
- avoid client-only calls from common server gameplay classes.

## Resources and Data

The project mixes Java logic with data pack content and assets.

Important directories:

- `assets/the_collector/lang`
- `assets/the_collector/models`
- `assets/the_collector/textures`
- `data/the_collector/recipe`
- `data/the_collector/advancement`
- `data/the_collector/dimension`
- `data/the_collector/dimension_type`

When a feature seems to “do nothing”, always verify both halves:

1. Java code;
2. associated resources or data.

## Adding a Feature Without Breaking the Project

### Add a New Progression Item

1. declare the item in `ModItems`;
2. add model, texture, and translation;
3. decide how it is obtained:
   - loot;
   - alembic;
   - stash;
   - ritual;
4. wire the advancement if needed;
5. document the feature in the README and release notes if it is player-facing.

### Add a New Alchemy Step

1. define input item, reagent, fuel, and output in `AlembicRecipes`;
2. verify menu / JEI rendering;
3. verify hints;
4. verify affected advancements.

### Add a New Structure or World Anchor

1. decide whether the state must be persistent;
2. if yes, add it to `CollectorSavedData` or to a dedicated `SavedData`;
3. make generation idempotent;
4. provide a fallback if generation fails;
5. avoid destructive rebuilding of blocks already used for storage or progression.

## Pitfalls and Conventions

### Pitfalls

- rebuilding a structure without preserving containers;
- storing durable state only in runtime memory;
- implementing gameplay logic client-side;
- repeating full inventory or block entity scans every tick;
- forgetting that progression state is split between world `SavedData` and player `PersistentData`;
- assuming JEI is present.

### Useful Conventions

- the mod is server-first;
- durable world state should go to `SavedData` first;
- low-volume per-player state should go to `PersistentData`;
- a generated structure must be safe to call repeatedly;
- comments should explain a gameplay or technical constraint, not restate obvious code.

## Recommended Manual Debug Pass

Before considering a change safe, verify at least:

1. Collector spawn at night in the Overworld;
2. ground item theft;
3. chest raiding;
4. stash creation after escape;
5. trace appearance;
6. `Unstable Resonance` capture;
7. alembic processing;
8. cauldron resting then harvesting;
9. entry activation;
10. round trip into and out of the dimension;
11. loot presence inside vault containers;
12. JEI display if JEI is installed.

## Files You Should Read First

If you take over the project with no prior context, this is the most profitable reading order:

1. `src/main/java/fr/kerian_animals/thecollector/TheCollectorMod.java`
2. `src/main/java/fr/kerian_animals/thecollector/entity/CollectorEntity.java`
3. `src/main/java/fr/kerian_animals/thecollector/entity/goal/CollectorStealChestGoal.java`
4. `src/main/java/fr/kerian_animals/thecollector/stash/CollectorStashManager.java`
5. `src/main/java/fr/kerian_animals/thecollector/stash/CollectorSavedData.java`
6. `src/main/java/fr/kerian_animals/thecollector/world/dimension/CollectorDimensionTravelHandler.java`
7. `src/main/java/fr/kerian_animals/thecollector/world/vault/CollectorVaultManager.java`
8. `src/main/java/fr/kerian_animals/thecollector/item/CollectorTraceInteractionHandler.java`
9. `src/main/java/fr/kerian_animals/thecollector/block/entity/AlembicBlockEntity.java`
10. `src/main/java/fr/kerian_animals/thecollector/advancement/CollectorAdvancementManager.java`

## Final Rule

Whenever a change touches multiple layers, reason in this order:

1. source of truth for the state;
2. exact moment that state changes;
3. persistence;
4. player feedback;
5. per-tick performance impact.

If that chain is not clear, the feature is not ready to be implemented cleanly.
