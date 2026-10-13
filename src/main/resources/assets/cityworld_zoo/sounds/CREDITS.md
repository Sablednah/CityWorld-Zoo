# Sounds

Every file here must be original or properly licensed (the family rule; the ZARP pack already redrew NPC
skins over licensing). Record the source and licence of each clip on the day it goes in.

**Pixabay Content License** (https://pixabay.com/service/license-summary/, confirmed by Sable 2026-10-13 for
every clip below): free to use and modify in commercial and non-commercial work, no attribution required,
but the clips may not be sold or redistributed on their own as sound files. Shipping them cut and mixed into
this mod's jar is within the licence; publishing the raw files as a sound pack would not be.

| File | What | Source | Licence |
|---|---|---|---|
| `zebra/whinny.ogg` | the zebra's bark-like whinny, one of its ambient takes | Pixabay, found by Sable 2026-10-13, trimmed with AudioTrimmer.com | Pixabay Content License |
| `rhesus_macaque/ambient1-2.ogg` | chatter | Pixabay; raw `art/rhesus_macaque/sounds/monkey-jungle.mp3` | Pixabay Content License |
| `rhesus_macaque/ambient3.ogg`, `hurt1-2.ogg` | chatter; two sharp falling cries | Pixabay; raw `art/rhesus_macaque/sounds/monkey.mp3` | Pixabay Content License |
| `rhesus_macaque/death1.ogg` | a long screech | Pixabay; raw `art/rhesus_macaque/sounds/monkey-noise-no-copyright.mp3` | Pixabay Content License |
| `rhesus_macaque/death2.ogg` | a long rising screech | Pixabay; raw `art/rhesus_macaque/sounds/monkey-sound.mp3` | Pixabay Content License |

Everything else the animals say is a vanilla sound event referenced from `sounds.json` (`"type": "event"`):
the zebra's other ambient take, hurt, death and angry calls are the donkey's. Those need no credit.

Cuts are made by `tools/audio/clips.py` from the raw files in `art/<animal>/sounds/`; its CUTS table is the
record of which stretch became which take. Format: mono OGG Vorbis (mono, or the client cannot place the
sound in the world); sample rate as recorded.
