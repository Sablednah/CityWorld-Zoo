# Blockbench files

One self-contained Java model file per animal, in the form Blockbench's **Modded Entity** format opens
directly (File > Open Model, pick the `.java`; mappings "Mojang 1.17+"). Each names its texture, in
`src/main/resources/assets/cityworld_zoo/textures/entity/`; load that in Blockbench's texture panel.

These are copies for editing, not the shipped code: the real models are
`src/main/java/com/sablednah/cityworldzoo/client/model/`. Lion, tiger, zebra and the zebra foal build on
vanilla meshes in the real code and are inlined here so Blockbench sees every box. Whole-mesh scaling
(deer 0.8, elephant 1.4, bear 1.2, cats 2.0) is applied in the game, not in these files, so Blockbench shows
them at box size.

Round trip: edit in Blockbench, File > Export > Java Entity (Mojang mappings), and hand the exported numbers
to the session (or paste them over the matching `mesh()` method) and re-run `tools/textures/paint.py`'s box
list for any box that moved on the texture.

These files are rewritten from the real classes by hand when they change; `git diff` the two if in doubt.
