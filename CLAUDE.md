# CLAUDE.md

Guidance for Claude Code working in this repository.

## What this is

**CityWorld: Zoo** — zoo animals from zombie media, as ordinary NeoForge `Animal`s: giraffe and rhesus
macaque (The Last of Us), zebra (TLOU Part II), deer (TLOU, I Am Legend), lion (I Am Legend, 12 Monkeys,
RE Outbreak), tiger (The Walking Dead's Shiva), elephant (RE Outbreak), brown bear. Released on its own
in Sable's CurseForge family, and shipping a datapack that fills CityWorld's zoos with them. CityWorld is
optional: without it the animals come from spawn eggs. Mod id `cityworld_zoo`, package
`com.sablednah.cityworldzoo`, MIT. `../Chronicler/CLAUDE.md` and `../SableCraft-Standards/CLAUDE.md`
carry the family conventions this repo follows without restating them; `../Cast` is the template it was
built from.

| | |
|---|---|
| Minecraft / NeoForge / Java | 26.2 / 26.2.0.72 (the ZARP pack runs .88) / 25 (`mc26.2`, the only branch so far) |
| Dev server | port **25575**, RCON **25585** (`run-mc26.2/server.properties`, gitignored) |
| JAVA_HOME | `/home/sable/.gradle/jdks/eclipse_adoptium-25-amd64-linux.2` (no system Java) |

```bash
./gradlew compileJava              # the inner loop
./gradlew runServer -Pselftest     # neoforge/SelfTest on ServerStartedEvent; read "SelfTest:" and every FAILED
./deploy.sh                        # -> the CurseForge "26.2" instance (never the ZARP one: .sablecraft-no-deploy)
python3 tools/textures/paint.py    # regenerate the placeholder textures from the box lists
```

Run the self-test in the FOREGROUND of one command, kill only the JVM whose command line has
`CityWorld-Zoo/build/classes`, and never `pkill` on `java` or `fml.modFolders`. Boot checks print the
ERROR lines (`grep -nE "ERROR|FATAL|RegistryDataLoader"`), never a count. Note **25575 is also Standards'
RCON port** in Chronicler's port table: the two dev servers cannot be up together.

## The rules everything follows from

**Constructors and `finalizeSpawn` never touch the real level.** CityWorld creates these entities on a
worldgen thread through the generation region (`SpawnProvider.placeEntity`, spawn reason
`CHUNK_GENERATION`) and then calls `EventHooks.finalizeMobSpawn`; a read of the real level there deadlocks
world creation at "Preparing spawn area". No species overrides `finalizeSpawn` and the self-test asserts
it by reflection. CityWorld's `"finalize": false` flag is the escape hatch if a species ever needs one.

**Keep them ordinary.** ZombieMod's GenusApplier makes infected versions through the normal entity
factory (the standard `(EntityType, Level)` constructor), sets attributes with `setBaseValue` and layers
melee goals on. So every species extends `entity/ZooAnimal` (an `Animal` with vanilla goals) and
registers **`ATTACK_DAMAGE`** -- vanilla's `Mob.doHurtTarget` throws without it -- plus `FOLLOW_RANGE`
and `MOVEMENT_SPEED`. The self-test checks all three on every species.

**The CityWorld contract is data, not code.** `data/cityworld_zoo/cityworld/zoo/animal/<id>.json` and
`.../habitat/<name>.json` are CityWorld 6.3.0's datapack registries (`worldgen/ZooCatalog` in
`../CityWorld-ReForged-worktrees/mc26.2`; schema in that class's comment). Nothing here imports
CityWorld; the self-test reads its catalog by reflection when the jar is in `run-mc26.2/mods`. An entry
is skipped quietly when its entity, habitat or `requires_mod` is missing. Change the schema with the
CityWorld session, never by guessing.

**Art and sound are original or licensed.** The textures today are `tools/textures/paint.py`'s
placeholders, painted on each model's box net; Sable is revamping them. Sounds are vanilla sound events
standing in (horse for the zebra, polar bear for the bear, ocelot pitched down for the cats ...). Models
match the 26.1+ vanilla style: the zebra IS the horse mesh, the cats the ocelot mesh at 2x, the bear the
polar bear mesh; giraffe, deer, elephant and macaque are ours.

## Layout

```
ZooMod, BuildInfo, ZooEntities          mod entry, build stamp, the Species table (type + egg + attributes + placement)
entity/ZooAnimal + 8 species            vanilla-goal animals; fightsBack() = melee + hurt-by for cats, bear, elephant
client/ZooClient, ZooModelLayers        layer + renderer registration (loaded only on a client)
client/model/*, client/renderer/ZooRenderer   one adult + one baby layer each, one texture each
neoforge/Lang, Feedback, ZooCommands    messages.yml, '&' colours, /zoo status | count [radius] (operators)
neoforge/ZooEvents, SelfTest            Lang on ServerAboutToStart; the self-test on ServerStarted when -Pselftest
data/cityworld_zoo/cityworld/zoo/       the CityWorld datapack: 8 animals, 5 habitats
data/cityworld_zoo/tags/entity_type/zoo_animals.json   #cityworld_zoo:zoo_animals (Threadwork's spawn rules)
tools/textures/paint.py                 placeholder textures + spawn eggs from the box lists (keep in step with the models)
```

## Who consumes what

- **CityWorld** (`../CityWorld-ReForged-worktrees/mc26.2`, commit 99c48ca2 "Zoos are data", 6.3.0+): the zoo
  datapack. `size: "tall"` (giraffe) raises the fence; `"large"` (elephant) needs a big pen.
- **Threadwork / ZARP** (`../Threadwork`): spawns these wild near zoo lots by the entity tag; Kit's quest
  "Other City: Before" needs only `cityworld_zoo:giraffe` to exist. Ids are a promise once ZARP ships.
- **ZombieMod**: genus bases, see "Keep them ordinary".

## Known traps

- A model's box list lives twice: Java (`client/model`) and `tools/textures/paint.py`. Change one, change
  the other, or the new box renders pink.
- `BabyModelTransform` only transforms ROOT children: a head under a neck (giraffe, deer) cannot be
  big-headed, so those babies are uniform half-size. Horse and bear babies use the vanilla baby meshes'
  idea but the adult texture, so one PNG per species.
- 26.2 names: `EntityTypes.X`, `Identifier`, models under `client/model/animal/<name>/`, renderers
  `submit(...)` with `SubmitNodeCollector`, `Item.Properties.spawnEgg(type)` (a per-mob egg texture, no
  tints). Vanilla sources: `../Threadwork/build/moddev/artifacts/minecraft-patched-26.2.0.72-sources.jar`.
- Entity types register before items (vanilla's registry order), which is what lets a spawn egg's
  properties call `type.get()`.
- Never edit a sibling's `CLAUDE.md`; raise it with Sable instead. Never touch the ZARP instance's
  `mods/` while the game runs, and only with Sable's OK (copy to `.new`, verify, rename).
