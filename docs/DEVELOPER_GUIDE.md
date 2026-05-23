# Developer Guide

## Objectif

Ce document sert de guide de reprise pour le mod **The Collector**.

Il couvre :

- l'architecture générale ;
- les flux de gameplay côté code ;
- la persistance ;
- la séparation client / serveur ;
- les points d'extension ;
- les pièges déjà rencontrés sur le projet.

Le but n'est pas de décrire chaque classe ligne par ligne, mais de permettre à un développeur de modifier le mod sans casser les invariants implicites.

## Stack technique

- `Minecraft 1.21.x`
- `NeoForge 21.x`
- `Java 21`
- build via Gradle avec plugin `net.neoforged.moddev`
- intégration `JEI` optionnelle

Commandes principales :

```powershell
./gradlew runClient
./gradlew runServer
./gradlew build
./gradlew gameTestServer
```

Fichiers de build utiles :

- `build.gradle`
- `gradle.properties`
- `src/main/resources/META-INF/neoforge.mods.toml`

## Vue d'ensemble

Le mod repose sur une boucle serveur centrée sur une entité rare, `CollectorEntity`, qui :

1. apparaît dans l'Overworld ;
2. cherche des objets ou des coffres intéressants ;
3. vole du contenu ;
4. s'échappe ;
5. transforme son butin en cache persistante ;
6. alimente une progression joueur fondée sur les traces, l'alchimie et la dimension.

En pratique, le projet se découpe en six sous-systèmes :

- bootstrap et registries ;
- IA et cycle de vie du Collector ;
- traces, résonance et alambic ;
- persistance des caches, entrées et états temporisés ;
- dimension et coffre-fort ;
- guidage joueur via advancements, lore et JEI.

## Carte des packages

### Noyau

- `fr.kerian_animals.thecollector.TheCollectorMod`
- `fr.kerian_animals.thecollector.config`
- `fr.kerian_animals.thecollector.registry`

### Gameplay serveur

- `entity`
- `entity.goal`
- `spawn`
- `stash`
- `world`
- `world.dimension`
- `world.vault`
- `item`
- `advancement`

### UI et client

- `client`
- `client.jei`
- `menu`

### Contenu statique

- `src/main/resources/assets/the_collector`
- `src/main/resources/data/the_collector`

## Bootstrap du mod

Point d'entrée :

- `src/main/java/fr/kerian_animals/thecollector/TheCollectorMod.java`

Responsabilités :

- enregistrer blocs, items, entités, menus et block entities ;
- enregistrer la config commune ;
- brancher les handlers d'événements runtime sur le bus NeoForge.

Les handlers runtime les plus importants sont :

- `CollectorAdvancementManager`
- `CollectorSpawnHandler`
- `CollectorMiniCacheManager`
- `CollectorTraceInteractionHandler`

Règle pratique :

- tout ce qui est purement déclaratif va dans les registries ;
- tout ce qui réagit au jeu temps réel va dans un handler ou dans une entité ;
- éviter de cacher de la logique gameplay dans les registries.

## Registries

Les registries sont centralisées dans `registry/`.

Principales classes :

- `ModItems`
- `ModBlocks`
- `ModEntities`
- `ModBlockEntities`
- `ModMenus`
- `ModCreativeTabs`

Quand vous ajoutez un contenu jouable, la plupart des modifications passent par trois couches :

1. déclaration registry ;
2. ressource associée dans `assets/` ou `data/` ;
3. branchement gameplay côté serveur ou côté client.

Exemple concret pour un nouveau bloc interactif :

1. enregistrer le bloc dans `ModBlocks` ;
2. enregistrer son `BlockItem` dans `ModItems` si nécessaire ;
3. enregistrer sa `BlockEntity` dans `ModBlockEntities` si état persistant ;
4. enregistrer un menu dans `ModMenus` si interface ;
5. ajouter blockstate, model, textures, lang et recettes ;
6. ajouter la logique de tick / interaction côté code.

## Cycle de vie du Collector

Classe centrale :

- `src/main/java/fr/kerian_animals/thecollector/entity/CollectorEntity.java`

États :

- `IDLE`
- `SCOUTING`
- `COLLECTING`
- `ESCAPING`
- `DESPAWNING`

Goals principaux :

- `CollectorScoutGoal`
- `CollectorCollectItemGoal`
- `CollectorStealChestGoal`
- `CollectorEscapeGoal`

### Flux serveur

1. `CollectorSpawnHandler` tente un spawn périodique en Overworld.
2. `CollectorEntity` entre dans sa boucle de tick.
3. Les goals choisissent une cible item ou coffre.
4. Le Collector stocke les objets volés dans son inventaire interne.
5. Une condition de sortie force la fuite :
   - joueur trop proche ;
   - inventaire plein ;
   - temps de présence dépassé ;
   - attaque subie ;
   - coffre pillé.
6. En `DESPAWNING`, l'entité délègue à `CollectorStashManager`.

### Invariants importants

- l'entité ne doit jamais despawn de façon vanilla ;
- le stash est créé au moment du despawn logique, pas avant ;
- `lastTheftPos` sert à générer les traces et à attribuer certains advancements ;
- les flags debug ne doivent pas polluer le comportement standard.

### Performance

Le point historiquement coûteux était la recherche de coffres.

L'implémentation actuelle de `CollectorStealChestGoal` :

- évite un scan cubique complet à chaque recherche ;
- utilise un échantillonnage en anneaux ;
- maintient un cache local de coffres candidats ;
- invalide ce cache quand le Collector se déplace trop ou quand les coffres deviennent non valides.

Si vous changez cette logique, gardez l'objectif suivant :

- coût borné par tick ;
- aucune dépendance à un scan massif de block entities sur une large zone.

## Spawn du Collector

Classe :

- `src/main/java/fr/kerian_animals/thecollector/spawn/CollectorSpawnHandler.java`

Conditions actuelles :

- mod activé ;
- Overworld uniquement ;
- nuit uniquement si configuré ;
- vérification toutes les `200` ticks ;
- cooldown par niveau ;
- probabilité configurable ;
- ancrage sur un joueur non spectateur ;
- position trouvée dans une couronne de distance configurable.

Le handler garde `lastSpawnByLevel` en mémoire runtime. Ce n'est pas persistant entre redémarrages, ce qui est acceptable pour ce gameplay.

## Butin, caches et persistance

Classes principales :

- `CollectorStashManager`
- `CollectorSavedData`
- `CollectorStash`
- `CollectorEntry`
- `CollectorMiniCache`

### Où l'état est stocké

`CollectorSavedData` est toujours lu et écrit depuis l'Overworld, même quand la donnée concerne un autre espace de jeu.

C'est un choix important :

- un seul point de vérité ;
- pas de duplication entre dimensions ;
- récupération plus simple lors des commandes et de la progression joueur.

Le `SavedData` conserve :

- les stashes ;
- la dernière cache vue par joueur ;
- les entrées dimensionnelles ;
- les mini-caches.

### Création d'un stash

Au despawn du Collector :

1. on copie l'inventaire volé ;
2. on ajoute éventuellement du lore et du bonus loot ;
3. si la dimension du Collector est disponible, le butin est déposé dans le vault ;
4. sinon, un stash physique Overworld est généré ;
5. la structure est enregistrée dans `CollectorSavedData` ;
6. les joueurs proches reçoivent un pointeur vers ce stash.

### Piège connu

Le vault ne doit pas être reconstruit de manière destructive après dépôt du loot.

Le bug rencontré était :

- dépôt correct des items ;
- reconstruction de la salle à l'entrée dans la dimension ;
- recréation de coffres vides ;
- impression côté joueur que les coffres n'étaient jamais remplis.

L'implémentation actuelle de `CollectorVaultManager` préserve les slots de stockage existants.

## Dimension et voyage

Classes :

- `ModDimensions`
- `CollectorEntryManager`
- `CollectorDimensionTravelHandler`
- `CollectorVaultManager`

Ressources associées :

- `data/the_collector/dimension/collector_realm.json`
- `data/the_collector/dimension_type/collector_realm_type.json`

### Entrées Overworld

`CollectorEntryManager` gère :

- la création de la structure d'entrée ;
- sa reconstruction si elle a été cassée ;
- la recherche de l'entrée la plus proche ;
- l'activation rituelle indirecte via `CollectorCatalystItem`.

Une entrée a deux états :

- existante ;
- activée ou non.

L'activation est stockée dans `CollectorSavedData`, pas dans la structure de blocs elle-même.

### Voyage dimensionnel

`CollectorDimensionTravelHandler` observe le joueur serveur lorsqu'il est accroupi.

Le flux est :

1. vérifier le cooldown ;
2. appliquer un throttle de vérification ;
3. si le joueur est sur une entrée active en Overworld, téléportation vers le vault ;
4. si le joueur est dans le royaume sur le pad de sortie, retour à la dernière entrée utilisée.

État stocké dans `player.getPersistentData()` :

- cooldown de voyage ;
- délai entre checks accroupi ;
- position de la dernière entrée utilisée.

### Vault

`CollectorVaultManager` gère :

- la salle centrale ;
- les emplacements de stockage ;
- le dépôt du butin ;
- le fallback en drop d'items si tous les conteneurs sont pleins.

Invariants :

- ne pas effacer les conteneurs déjà existants ;
- garantir un point d'apparition sûr ;
- éviter les blocs décoratifs instables qui tombent à la génération.

## Traces, résonance et alchimie

Classes :

- `CollectorTraceInteractionHandler`
- `CollectorTraceBlock`
- `ResonanceCauldronSavedData`
- `AlembicRecipes`
- `AlembicBlock`
- `AlembicBlockEntity`
- `AlembicMenu`
- `AlembicScreen`

### Flux de progression

1. le Collector vole et déclenche une trace ;
2. le joueur capture la trace avec une fiole ;
3. cela produit `Unstable Resonance` ;
4. la résonance est traitée via alambic ou chaudron selon l'étape ;
5. les objets raffinés débloquent le catalyseur, le cristal puis la boussole.

### Alambic

`AlembicBlockEntity` est le coeur du traitement.

Responsabilités :

- gérer 5 slots ;
- valider les entrées ;
- maintenir une progression compatible avec l'UI type brewing stand ;
- consommer le combustible ;
- transformer les bouteilles ;
- synchroniser les données de menu ;
- émettre des particules et sons serveur.

Les recettes sont actuellement codées dans `AlembicRecipes` plutôt que data-driven.

Conséquence :

- simple à maintenir pour un petit nombre de recettes ;
- plus rigide si le contenu grossit ;
- migration vers un vrai système recipe serializer possible plus tard.

## Advancements, indices et JEI

Classes :

- `CollectorAdvancementManager`
- `CollectorAdvancementHelper`
- `CollectorLoreBookFactory`
- `client.jei.*`

### Advancements

`CollectorAdvancementManager` tourne côté serveur sur tick joueur.

Optimisation en place :

- passe d'inventaire unique quand plusieurs advancements item-based restent à débloquer ;
- cache persistant des advancements déjà acquis dans le `PersistentData` du joueur ;
- court-circuit des checks de proximité déjà validés.

Quand vous ajoutez un advancement :

1. créer le JSON dans `data/the_collector/advancement/` ;
2. ajouter le déclenchement côté code si l'advancement n'est pas purement data-driven ;
3. ajouter les textes côté `lang/` ;
4. si besoin, ajouter un hint ou un message contextuel.

### JEI

L'intégration JEI est volontairement optionnelle.

Point d'entrée :

- `client/jei/JeiPluginImpl.java`

Catégories actuelles :

- capture de trace ;
- recettes d'alambic ;
- cristallisation en chaudron ;
- rituel d'entrée.

Règle :

- aucune logique gameplay ne doit dépendre de JEI ;
- JEI ne sert qu'à exposer les process du mod au joueur.

## Séparation client / serveur

Le projet est majoritairement orienté serveur. La logique gameplay doit rester côté serveur tant qu'un rendu client explicite n'est pas requis.

### Côté serveur

- spawn ;
- IA ;
- loot ;
- structures ;
- persistance ;
- progression ;
- voyages dimensionnels ;
- interactions réelles avec blocs et items.

### Côté client

- rendu du Collector ;
- écran d'alambic ;
- comportement visuel de la boussole ;
- catégories JEI.

Règle pratique :

- si une logique modifie l'état du monde, elle doit vivre côté serveur ;
- si une logique ne sert qu'à afficher, elle vit côté client ;
- éviter les appels client depuis une classe serveur commune.

## Ressources et data

Le projet mélange logique Java et contenu data pack / assets.

Répertoires importants :

- `assets/the_collector/lang`
- `assets/the_collector/models`
- `assets/the_collector/textures`
- `data/the_collector/recipe`
- `data/the_collector/advancement`
- `data/the_collector/dimension`
- `data/the_collector/dimension_type`

Quand une feature semble "ne rien faire", il faut vérifier les deux moitiés :

1. le code Java ;
2. les ressources ou data associées.

## Ajouter une feature sans casser le projet

### Ajouter un nouvel item de progression

1. déclarer l'item dans `ModItems` ;
2. ajouter modèle, texture et traduction ;
3. décider où il est obtenu :
   - loot ;
   - alambic ;
   - cache ;
   - rituel ;
4. brancher l'advancement si nécessaire ;
5. documenter la feature dans README et release notes si elle est visible joueur.

### Ajouter une nouvelle étape d'alchimie

1. définir l'item d'entrée, le réactif, le fuel et l'output dans `AlembicRecipes` ;
2. vérifier le rendu menu / JEI ;
3. vérifier les hints ;
4. vérifier les advancements impactés.

### Ajouter une nouvelle structure ou un nouveau point de monde

1. décider si l'état doit être persistant ;
2. si oui, l'ajouter à `CollectorSavedData` ou à un `SavedData` dédié ;
3. rendre la génération idempotente ;
4. prévoir un fallback si la génération échoue ;
5. éviter toute reconstruction destructive de blocs déjà utilisés comme stockage ou progression.

## Pièges et conventions

### Pièges

- reconstruire une structure sans préserver les conteneurs ;
- stocker un état durable uniquement en mémoire runtime ;
- faire une logique gameplay côté client ;
- multiplier les scans complets d'inventaire ou de block entities sur tick ;
- oublier que certaines données de progression sont réparties entre `SavedData` monde et `PersistentData` joueur ;
- supposer que JEI est présent.

### Conventions utiles

- le mod est d'abord pensé serveur ;
- les données persistantes du monde passent en priorité par `SavedData` ;
- les états spécifiques au joueur à faible volumétrie passent par `PersistentData` ;
- une structure générée doit être sûre à rappeler plusieurs fois ;
- les commentaires doivent expliquer une contrainte métier, pas paraphraser le code.

## Débogage manuel recommandé

Avant de considérer une modification comme sûre, vérifier au minimum :

1. spawn du Collector de nuit en Overworld ;
2. vol d'objet au sol ;
3. pillage de coffre ;
4. création d'un stash après fuite ;
5. apparition des traces ;
6. capture de `Unstable Resonance` ;
7. fonctionnement de l'alambic ;
8. repos en chaudron puis récolte ;
9. activation d'une entrée ;
10. aller-retour dans la dimension ;
11. présence du butin dans les coffres du vault ;
12. affichage JEI si le mod JEI est installé.

## Fichiers à connaître en priorité

Si vous reprenez le projet sans contexte, l'ordre de lecture le plus rentable est :

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

## Dernière règle

Quand une modification touche plusieurs couches, toujours raisonner dans cet ordre :

1. source de vérité de l'état ;
2. moment exact où cet état change ;
3. persistance ;
4. feedback joueur ;
5. impact perf sur tick.

Si cette chaîne n'est pas claire, la feature n'est pas encore prête à être codée proprement.
