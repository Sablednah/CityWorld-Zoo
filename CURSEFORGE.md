![CityWorld: Zoo](https://raw.githubusercontent.com/Sablednah/CityWorld-Zoo/mc26.2/docs/wordmark-850.png)

# CityWorld: Zoo — the animals every zombie story walks past

**Eight zoo animals, each from a scene you remember.** The giraffes at the Salt Lake City overpass,
and the lab monkeys that never turned. The zebra in the snare. The deer in the snow, and the herd on
Fifth Avenue. The lions that took it. Shiva. The elephant from Raccoon City's zoo. And a bear, because
every zoo has a bear.

They are ordinary Minecraft animals: a spawn egg each, they breed on their food, they wander, and the
ones with teeth fight back if you hurt them. No new mechanics to learn, nothing to craft. They just
live in the world, and in the ruins of a city, they make the zoo look like somewhere people used to go.

![The eight spawn eggs in an inventory row: giraffe, macaque, zebra, deer, lion, tiger, elephant, bear](https://raw.githubusercontent.com/Sablednah/CityWorld-Zoo/mc26.2/docs/screenshots/spawn-eggs.png)

---

![A CityWorld zoo pen signed The Zoo Tigers, two tigers inside, the city's towers behind](https://raw.githubusercontent.com/Sablednah/CityWorld-Zoo/mc26.2/docs/screenshots/zoo-tigers.jpg)

## The animals

- **Giraffe** — five blocks tall, neck leaning forward as it walks, and it feeds: it finds a tree with
  leaves three to six blocks up, stands under the canopy's edge and puts the tip of its muzzle into the
  leaves to chew. It never breaks them. Shove one mid-mouthful and it bends its neck to keep eating.

![A giraffe with its muzzle in an acacia canopy, the sea and a town below](https://raw.githubusercontent.com/Sablednah/CityWorld-Zoo/mc26.2/docs/screenshots/giraffe-browsing.jpg)

- **Rhesus macaque** — a climber. Troops favour trees: a monkey picks a trunk, climbs the log it is
  holding, works round branches and overhangs, and once in the canopy it sits, scratches and eats,
  hand to mouth. It leaps gaps of up to five blocks between canopies and never takes fall damage.

![Three rhesus macaques sitting on top of an acacia canopy](https://raw.githubusercontent.com/Sablednah/CityWorld-Zoo/mc26.2/docs/screenshots/macaques-canopy.jpg)

- **Zebra** — herds on the savanna, a donkey's bray and a barking whinny. One in a hundred is a little
  bit *different*. You will know it when you see it.

![A zebra herd with a foal on a savanna hillside](https://raw.githubusercontent.com/Sablednah/CityWorld-Zoo/mc26.2/docs/screenshots/zebra-herd.jpg)

- **Deer** — skittish. It grazes, flicks its tail, lifts its head to look, and bolts if you come near
  unless you are sneaking.

![A doe and her fawn in long grass, a city across the water](https://raw.githubusercontent.com/Sablednah/CityWorld-Zoo/mc26.2/docs/screenshots/deer-fawn.jpg)

- **Lion** and **tiger** — they sit, they sniff (the tiger's whiskers twitch), and they hunt the way a
  fox does: a lion with a zebra or deer in view will, sooner or later, give chase. Tigers take deer,
  macaques and farm animals. Both guard their cubs: come within eight blocks of one and its parent
  comes for you, as a polar bear would.

![A tiger and her cub in the grass, a deer behind them](https://raw.githubusercontent.com/Sablednah/CityWorld-Zoo/mc26.2/docs/screenshots/tiger-cub.jpg)

- **Elephant** — two kinds. Cows are three blocks tall; bulls are three and a half with tusks. Calves
  are born tuskless and find out which they are when they grow up. Idle, an elephant curls its trunk
  and flaps its ears to cool off. Its knees bend.

![A tusked bull elephant, a cow and a calf](https://raw.githubusercontent.com/Sablednah/CityWorld-Zoo/mc26.2/docs/screenshots/elephants.jpg)

- **Brown bear** — sways and sniffs the air, and defends its cub.

![A brown bear and its cub in savanna grass at the edge of a city](https://raw.githubusercontent.com/Sablednah/CityWorld-Zoo/mc26.2/docs/screenshots/bear-cub.jpg)

Every one has its own voice, cut from real recordings: the giraffe's hum, the macaques' chatter, the
elephant's rumble, the lion's roar. Every animal has a walk whose stride matches its speed, so feet
plant and push instead of sliding.

## In the wild

They spawn naturally in the biomes you would expect, in herds and troops: giraffes, zebras, lions and
elephants on the savanna; tigers and macaques in the jungle; deer in forests and taiga; bears in the
taiga. No lions in the jungle.

![A lion in the grass by a river, a zebra grazing a few blocks away](https://raw.githubusercontent.com/Sablednah/CityWorld-Zoo/mc26.2/docs/screenshots/lion-zebra.jpg)

All of it is a dial in `config/cityworld_zoo-common.toml`: natural spawning on or off, each species
on or off, its spawn weight, its herd size and the biomes (ids or `#tags`) it spawns in. Hunting and
cub defence are switches too, with the hunters' keenness and rest time beside them.

## With CityWorld

Install [CityWorld ReForged](https://www.curseforge.com/minecraft/mc-mods/cityworld-reforged) 6.3.0 or
newer beside it and the city's zoos fill with them: giraffes behind a taller fence, an elephant in a big
paddock or a sunken pit, lions on a savanna lot, macaques in the trees, a bear on the rockery. Each
animal ships its own habitat and sign text; CityWorld's catalog picks from them and its own. No
configuration. `/cityfind lot zoo` finds the nearest one.

![Two tigers in a bamboo-planted zoo pen](https://raw.githubusercontent.com/Sablednah/CityWorld-Zoo/mc26.2/docs/screenshots/zoo-pen.jpg)

Without CityWorld the animals are still here, in the wild and from the spawn eggs.

## For other mods

They are plain `Animal`s with vanilla goals, a standard constructor and the attributes a mob needs, so
a mod that makes variants of creatures (an infected lion, say) can use them as-is. The entity-type tag
`#cityworld_zoo:zoo_animals` lists all eight for spawn rules.

## Requirements

| Minecraft | NeoForge | Java |
|---|---|---|
| 26.2 | 26.2.0.72+ | 25 |

**Install on the server and on every client.** These are new creatures, so a client without the mod
cannot join. Commands for operators: `/zoo status` (which build this is), `/zoo count [radius]`.

## Credits and licence

MIT, by **Sablednah**. Source: **[github.com/Sablednah/CityWorld-Zoo](https://github.com/Sablednah/CityWorld-Zoo)**.

All models, textures and spawn eggs are original work. The animal sounds are cut from recordings on
[Pixabay](https://pixabay.com/) under the Pixabay Content License; the full list is in the jar at
`assets/cityworld_zoo/sounds/CREDITS.md`. Nothing is taken from the games or films these animals
remember.
