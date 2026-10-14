package com.sablednah.cityworldzoo.neoforge;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.sablednah.cityworldzoo.BuildInfo;
import com.sablednah.cityworldzoo.ZooEntities;
import com.sablednah.cityworldzoo.ZooMod;
import com.sablednah.cityworldzoo.entity.ZooAnimal;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.fml.ModList;

/**
 * Headless checks on {@code ./gradlew runServer -Pselftest}: every species exists with the attributes the
 * family's other mods rely on, its egg makes it, its files are in the jar, the commands parse AND execute,
 * and nothing overrides {@code finalizeSpawn} (the worldgen-thread rule). Prints every FAILED line, then a
 * verdict. Calls the real code, never a re-derivation.
 */
public final class SelfTest {

    private static final List<String> FAILURES = new ArrayList<>();
    private static int passed;

    public static void run(MinecraftServer server) {
        FAILURES.clear();
        passed = 0;
        ZooMod.LOGGER.info("=== CityWorld: Zoo SelfTest === {}", BuildInfo.describe());
        ServerLevel level = server.overworld();
        var spawn = level.getRespawnData().globalPos().pos();
        // A fresh 26.2 server loads no spawn chunks without a player; force the one we use.
        level.setChunkForced(spawn.getX() >> 4, spawn.getZ() >> 4, true);
        level.getChunk(spawn.getX() >> 4, spawn.getZ() >> 4);

        check("the build stamp parses a missing stream as unknown", BuildInfo.parse(null).commit().equals("unknown"));
        check("messages: the catalogue is not empty and the prefix resolves", Lang.catalogueSize() > 0 && !Lang.get("prefix").equals("prefix"));

        for (ZooEntities.Species<? extends ZooAnimal> s : ZooEntities.ALL) species(server, level, spawn, s);

        // Elephants: a bull is a cow times the scale attribute, and the flag survives a save.
        {
            var e = ZooEntities.ELEPHANT.entityType().create(level, EntitySpawnReason.COMMAND);
            if (e instanceof com.sablednah.cityworldzoo.entity.Elephant el) {
                el.setTusked(false);
                float cow = el.getBbHeight();
                el.setTusked(true);
                check("elephant: a bull stands 3.5 blocks to a cow's 3", Math.abs(el.getBbHeight() / cow - com.sablednah.cityworldzoo.entity.Elephant.BULL_SCALE) < 1e-3
                        && Math.abs(cow - 3.0F) < 1e-3);
                var out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess());
                el.saveWithoutId(out);
                var copy = ZooEntities.ELEPHANT.entityType().create(level, EntitySpawnReason.COMMAND);
                if (copy instanceof com.sablednah.cityworldzoo.entity.Elephant el2) {
                    el2.setTusked(false);
                    el2.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), out.buildResult()));
                    check("elephant: Tusked survives a save and reload, scale with it", el2.isTusked() && Math.abs(el2.getBbHeight() - el.getBbHeight()) < 1e-3);
                    el2.discard();
                }
                el.discard();
            } else {
                FAILURES.add("elephant did not create");
            }
        }

        // Zebras: the bitten skin is one in a hundred and survives a save.
        {
            var e = ZooEntities.ZEBRA.entityType().create(level, EntitySpawnReason.COMMAND);
            if (e instanceof com.sablednah.cityworldzoo.entity.Zebra z) {
                z.setBitten(true);
                var out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess());
                z.saveWithoutId(out);
                var copy = ZooEntities.ZEBRA.entityType().create(level, EntitySpawnReason.COMMAND);
                if (copy instanceof com.sablednah.cityworldzoo.entity.Zebra z2) {
                    z2.setBitten(false);
                    z2.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), out.buildResult()));
                    check("zebra: Bitten survives a save and reload", z2.isBitten());
                    z2.discard();
                }
                z.discard();
                check("zebra: the bitten skin has a texture", resource("/assets/cityworld_zoo/textures/entity/zebra_bite.png"));
            }
        }

        // Sounds: every event of ours is registered, defined in sounds.json, and every file it names is in the jar.
        {
            JsonObject sounds = json("/assets/cityworld_zoo/sounds.json");
            check("sounds.json parses", sounds != null);
            for (var holder : com.sablednah.cityworldzoo.ZooSounds.all()) {
                String path = holder.getId().getPath();
                check("sound " + path + " is registered", BuiltInRegistries.SOUND_EVENT.containsKey(holder.getId()));
                check("sound " + path + " is defined in sounds.json", sounds != null && sounds.has(path));
                check("sound " + path + " has a subtitle", lang("subtitles.cityworld_zoo." + path.substring("entity.".length())));
                if (sounds != null && sounds.has(path)) {
                    for (var take : sounds.getAsJsonObject(path).getAsJsonArray("sounds")) {
                        JsonObject t = take.isJsonObject() ? take.getAsJsonObject() : null;   // a bare string is a file
                        String name = t != null ? t.get("name").getAsString() : take.getAsString();
                        if (t != null && t.has("type") && "event".equals(t.get("type").getAsString())) {
                            check("sound " + path + " borrows a real vanilla event: " + name, BuiltInRegistries.SOUND_EVENT.containsKey(Identifier.parse(name)));
                        } else {
                            check("sound " + path + " take exists in the jar: " + name, resource("/assets/cityworld_zoo/sounds/" + name.substring(name.indexOf(':') + 1) + ".ogg"));
                        }
                    }
                }
            }
        }

        // Natural spawning: the modifier type is registered, its data file names it, every species has a config
        // entry, and the default biome lists are real tags.
        check("biome modifier type cityworld_zoo:natural_spawns is registered",
                net.neoforged.neoforge.registries.NeoForgeRegistries.BIOME_MODIFIER_SERIALIZERS.containsKey(ZooEntities.id("natural_spawns")));
        JsonObject modifier = json("/data/cityworld_zoo/neoforge/biome_modifier/natural_spawns.json");
        check("the natural_spawns data file names the type", modifier != null && "cityworld_zoo:natural_spawns".equals(modifier.get("type").getAsString()));
        for (var s : ZooEntities.ALL) {
            var c = com.sablednah.cityworldzoo.ZooConfig.SPAWNS.get(s.id());
            check(s.id() + " has a spawn config", c != null);
            if (c != null) {
                boolean anyBiome = false;
                for (String b : c.biomes().get()) {
                    if (b.startsWith("#")) {
                        var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME, Identifier.parse(b.substring(1)));
                        anyBiome |= server.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME).get(tag).isPresent();
                    } else {
                        anyBiome |= server.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME).containsKey(Identifier.parse(b));
                    }
                }
                check(s.id() + "'s default biomes exist: " + c.biomes().get(), anyBiome);
            }
        }
        check("the savanna takes a giraffe", server.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .get(net.minecraft.world.level.biome.Biomes.SAVANNA).map(h -> com.sablednah.cityworldzoo.neoforge.ZooSpawns.matches(h, java.util.List.of("#minecraft:is_savanna"))).orElse(false));
        check("the jungle does not", !server.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .get(net.minecraft.world.level.biome.Biomes.JUNGLE).map(h -> com.sablednah.cityworldzoo.neoforge.ZooSpawns.matches(h, java.util.List.of("#minecraft:is_savanna"))).orElse(true));

        // The worldgen-thread rule, as a guard: no species and not the base may override finalizeSpawn.
        List<Class<?>> classes = new ArrayList<>();
        classes.add(ZooAnimal.class);
        for (var s : ZooEntities.ALL) classes.add(s.entityType().getBaseClass());
        for (Class<?> c : classes) {
            boolean overrides;
            try {
                c.getDeclaredMethod("finalizeSpawn", ServerLevelAccessor.class, DifficultyInstance.class, EntitySpawnReason.class, SpawnGroupData.class);
                overrides = true;
            } catch (NoSuchMethodException e) {
                overrides = false;
            }
            check(c.getSimpleName() + " does not override finalizeSpawn (CityWorld spawns it on a worldgen thread)", !overrides);
        }

        // CityWorld's catalog, when CityWorld is in run-mc26.2/mods: every animal of ours must be in it.
        if (ModList.get().isLoaded("cityworld")) {
            try {
                Class<?> catalog = Class.forName("me.daddychurchill.CityWorld.worldgen.ZooCatalog");
                List<?> entries = (List<?>) catalog.getMethod("entries").invoke(null);
                List<String> ids = new ArrayList<>();
                for (Object e : entries) ids.add(String.valueOf(e.getClass().getMethod("id").invoke(e)));
                for (var s : ZooEntities.ALL)
                    check("CityWorld's zoo catalog lists " + s.identifier(), ids.contains(s.identifier().toString()));
            } catch (ReflectiveOperationException | RuntimeException e) {
                FAILURES.add("CityWorld is loaded but its ZooCatalog could not be read: " + e);
            }
        } else {
            ZooMod.LOGGER.info("CityWorld: Zoo SelfTest: CityWorld is not loaded -- the catalog check is skipped (copy its jar into run-mc26.2/mods to run it)");
        }

        // Commands: parse AND execute, and a bad one must be refused.
        CommandDispatcher<CommandSourceStack> dispatcher = server.getCommands().getDispatcher();
        CommandSourceStack source = server.createCommandSourceStack();
        check("/zoo status parses to an executable node", executable(dispatcher.parse("zoo status", source)));
        check("/zoo status executes", run(dispatcher, "zoo status", source) == ZooEntities.ALL.size());
        check("/zoo count 16 parses", executable(dispatcher.parse("zoo count 16", source)));
        check("/zoo sideways is refused", !executable(dispatcher.parse("zoo sideways", source)));

        ZooMod.LOGGER.info("CityWorld: Zoo SelfTest: {} passed, {} FAILED", passed, FAILURES.size());
        for (String f : FAILURES) ZooMod.LOGGER.error("CityWorld: Zoo SelfTest FAILED: {}", f);
        ZooMod.LOGGER.info(FAILURES.isEmpty() ? "CityWorld: Zoo SelfTest: ALL PASSED" : "CityWorld: Zoo SelfTest: FAILURES ABOVE");
    }

    private static <T extends ZooAnimal> void species(MinecraftServer server, ServerLevel level, BlockPos spawn, ZooEntities.Species<T> s) {
        String id = s.identifier().toString();
        EntityType<T> type = s.entityType();
        check(id + " is registered under its id", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString().equals(id));
        check(id + " is in #cityworld_zoo:zoo_animals", ZooEntities.isZooAnimal(type));
        Entity e = type.create(level, EntitySpawnReason.COMMAND);
        check(id + " creates as an Animal", e instanceof Animal);
        if (e instanceof Mob mob) {
            check(id + " has ATTACK_DAMAGE (ZombieMod's melee goals need it)", mob.getAttribute(Attributes.ATTACK_DAMAGE) != null && mob.getAttributeValue(Attributes.ATTACK_DAMAGE) > 0);
            check(id + " has FOLLOW_RANGE and MOVEMENT_SPEED", mob.getAttribute(Attributes.FOLLOW_RANGE) != null && mob.getAttribute(Attributes.MOVEMENT_SPEED) != null);
            check(id + " has health", mob.getMaxHealth() > 0);
            if (e instanceof Animal animal) {
                // Breeding: the young is the same type, and it eats something.
                mob.snapTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0, 0);
                var young = animal.getBreedOffspring(level, animal);
                check(id + " breeds its own kind", young != null && young.getType() == type);
                if (young != null) young.discard();
                boolean eatsSomething = BuiltInRegistries.ITEM.stream().anyMatch(item -> animal.isFood(new ItemStack(item)));
                check(id + " eats something", eatsSomething);
            }
            e.discard();
        }
        ItemStack egg = new ItemStack(s.egg().get());
        check(id + " spawn egg is a spawn egg for this type", egg.getItem() instanceof SpawnEggItem && SpawnEggItem.getType(egg) == type);
        check(id + " and its egg have English names", lang(type.getDescriptionId()) && lang(s.egg().get().getDescriptionId()));
        check(id + " has a loot table in the jar", json("/data/cityworld_zoo/loot_table/entities/" + s.id() + ".json") != null);
        JsonObject zoo = json("/data/cityworld_zoo/cityworld/zoo/animal/" + s.id() + ".json");
        check(id + " has a CityWorld zoo entry naming this entity", zoo != null && id.equals(zoo.get("entity").getAsString()));
        if (zoo != null) {
            String habitat = zoo.get("habitat").getAsString();
            String path = habitat.startsWith("cityworld_zoo:") ? "/data/cityworld_zoo/cityworld/zoo/habitat/" + habitat.substring("cityworld_zoo:".length()) + ".json" : null;
            check(id + "'s habitat " + habitat + " ships in this jar", path != null && json(path) != null);
            check(id + "'s zoo entry requires this mod", zoo.has("requires_mod") && ZooMod.MODID.equals(zoo.get("requires_mod").getAsString()));
        }
        check(id + " has an egg item model and texture", resource("/assets/cityworld_zoo/items/" + s.id() + "_spawn_egg.json")
                && resource("/assets/cityworld_zoo/models/item/" + s.id() + "_spawn_egg.json")
                && resource("/assets/cityworld_zoo/textures/item/" + s.id() + "_spawn_egg.png"));
        check(id + " has an entity texture", resource("/assets/cityworld_zoo/textures/entity/" + s.id() + ".png"));
    }

    private static JsonObject LANG;

    private static boolean lang(String key) {
        if (LANG == null) LANG = json("/assets/cityworld_zoo/lang/en_us.json");
        return LANG != null && LANG.has(key);
    }

    private static JsonObject json(String path) {
        try (InputStream in = SelfTest.class.getResourceAsStream(path)) {
            if (in == null) return null;
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean resource(String path) {
        try (InputStream in = SelfTest.class.getResourceAsStream(path)) {
            return in != null;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean executable(ParseResults<CommandSourceStack> parse) {
        return parse.getExceptions().isEmpty() && !parse.getReader().canRead()
                && parse.getContext().getLastChild().getCommand() != null;
    }

    private static int run(CommandDispatcher<CommandSourceStack> dispatcher, String command, CommandSourceStack source) {
        try {
            return dispatcher.execute(command, source);
        } catch (Exception e) {
            FAILURES.add(command + " threw " + e);
            return -1;
        }
    }

    private static void check(String what, boolean ok) {
        if (ok) passed++;
        else FAILURES.add(what);
    }

    private SelfTest() {}
}
