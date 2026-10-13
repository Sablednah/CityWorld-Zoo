# Sounds

Every file here must be original or properly licensed (the family rule; the ZARP pack already redrew NPC
skins over licensing). Record the source and licence of each clip on the day it goes in.

| File | What | Source | Licence |
|---|---|---|---|
| `zebra/whinny.ogg` | the zebra's bark-like whinny, one of its ambient takes | found by Sable, 2026-10-13, trimmed with AudioTrimmer.com | **to confirm** -- Sable to note where it came from and under what terms before release |
| `rhesus_macaque/ambient1-2.ogg` | chatter | `art/rhesus_macaque/sounds/monkey-jungle.mp3`, found by Sable 2026-10-13 | **to confirm** |
| `rhesus_macaque/ambient3.ogg`, `hurt1-2.ogg` | chatter; two sharp falling cries | `art/rhesus_macaque/sounds/monkey.mp3`, found by Sable 2026-10-13 | **to confirm** |
| `rhesus_macaque/death1.ogg` | a long screech | `art/rhesus_macaque/sounds/monkey-noise-no-copyright.mp3`, found by Sable 2026-10-13 | **to confirm** (the filename says no copyright; note the site and its terms) |
| `rhesus_macaque/death2.ogg` | a long rising screech | `art/rhesus_macaque/sounds/monkey-sound.mp3`, found by Sable 2026-10-13 | **to confirm** |

Cuts are made by `tools/audio/clips.py` from the raw files in `art/<animal>/sounds/`; its CUTS table is the
record of which stretch became which take.

Everything else the animals say is a vanilla sound event referenced from `sounds.json` (`"type": "event"`):
the zebra's other ambient take, hurt, death and angry calls are the donkey's. Those need no credit.

Format: mono OGG Vorbis, 44.1 kHz (mono, or the client cannot place the sound in the world).
