# Changelog

## 1.0.0 — 2026-10-15

The first release. Minecraft 26.2 on NeoForge 26.2.0.72 or newer, Java 25. Install on the server and
on every client.

- **Eight animals**, each a spawn egg and breedable: giraffe, rhesus macaque, zebra, deer, lion, tiger,
  elephant, brown bear. Models, textures and spawn eggs by Sablednah; sounds cut from Pixabay recordings
  (licence in the jar's `sounds/CREDITS.md`). Every animal's stride is matched to its speed.
- **Behaviour.** Giraffes browse from leaves three to six blocks up, muzzle in the canopy's edge, never
  breaking a block. Macaques climb trees by the log they hold, round branches and overhangs, leap canopy
  gaps of up to five blocks, and sit in the canopy to eat; no fall damage. Deer are skittish unless you
  sneak. Lions and tigers hunt (zebras, deer, farm animals; tigers also macaques) and, like bears, go
  for a player near their cub. Elephants come as cows (3 blocks) and tusked bulls (3.5); calves are
  tuskless and roll which they become when they grow up. Idle animations throughout: cats sit, the
  tiger sniffs with a whisker twitch, elephants flap their ears and curl their trunk, bears sway and
  sniff, monkeys scratch, grazers graze. One zebra in a hundred is not quite a zebra.
- **Natural spawning** in savanna, jungle, forest and taiga biomes, in herds and troops, all from
  `config/cityworld_zoo-common.toml`: on/off overall and per species, weight, herd size, biome ids or
  tags. Hunting and cub defence are switches with their own dials. Biome changes take a restart.
- **CityWorld zoos.** A datapack of eight animal entries over five habitats (savanna, woodland,
  scrubland, canopy, rockery) for CityWorld's data-driven zoos (6.3.0 or newer). Giraffes get the
  taller fence; elephants need a big pen. CityWorld is optional.
- `#cityworld_zoo:zoo_animals`, an entity-type tag of all eight, for other mods' spawn rules. The
  animals are plain `Animal`s with vanilla goals and the standard attributes, so variant-making mods
  can use them as bases.
- `/zoo status` (what is registered, which build) and `/zoo count [radius]` (what is loaded near you),
  for operators.
