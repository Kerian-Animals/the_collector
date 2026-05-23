## [1.2.2] - 2026-03-25

### Français

#### Corrigé

- Correction du coffre-fort du royaume du Collector : le contenu n'est plus effacé à l'entrée dans la dimension.

#### Optimisé

- Réduction du coût de recherche des coffres pour le Collector grâce à un échantillonnage en anneaux et à un cache local de coffres candidats.
- Ajout d'un throttle sur les vérifications de voyage dimensionnel quand le joueur reste accroupi.
- Réduction des scans d'inventaire répétés pour les advancements grâce à une passe unique et à un cache des advancements déjà acquis.

#### Documentation

- Nettoyage des commentaires inutiles dans le code.
- Refonte du `README` français et anglais avec une documentation plus claire sur la progression, la configuration, les commandes et l'architecture du mod.

### English

#### Fixed

- Fixed the Collector realm vault so stored loot is no longer wiped when entering the dimension.

#### Optimized

- Reduced Collector chest search cost with ring-based sampling and a local candidate chest cache.
- Added throttling for crouch-based dimension travel checks.
- Reduced repeated inventory scans for advancements through a single-pass scan and a cache of already unlocked advancements.

#### Documentation

- Removed unnecessary code comments.
- Reworked the French and English `README` files with clearer documentation for progression, configuration, commands, and project structure.

## [1.2.1] - 2026-03-18

### Français

#### Ajouté

- Ajout d'une intégration **JEI** optionnelle.

#### Modifié

- Correction du chargement des recettes data pack du mod pour qu'elles apparaissent correctement dans JEI et dans les recettes vanilla.

### English

#### Added

- Added optional **JEI** integration.

#### Changed

- Fixed the mod's data pack recipe loading so the recipes appear properly in JEI and in vanilla recipe views.
