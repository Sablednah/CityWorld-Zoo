package com.sablednah.cityworldzoo;

import java.util.List;
import java.util.Map;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-owner dials, {@code config/cityworld_zoo-common.toml}. Spawning is read when a world's biomes are
 * built (a restart, not {@code /reload}); behaviour is read live.
 *
 * <p>Natural spawns are per species: on or off, a weight against the biome's other creatures (a cow is 8,
 * a horse 5), a herd size, and the biomes as a list of biome ids or {@code #tags}. The defaults put each
 * animal where it lives: savanna for giraffe, zebra, lion and elephant; jungle for tiger and macaque;
 * forest and taiga for deer; taiga for the bear.</p>
 */
public final class ZooConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public record Spawn(ModConfigSpec.BooleanValue enabled, ModConfigSpec.IntValue weight, ModConfigSpec.IntValue minHerd,
            ModConfigSpec.IntValue maxHerd, ModConfigSpec.ConfigValue<List<? extends String>> biomes) {}

    public static final ModConfigSpec.BooleanValue NATURAL_SPAWNS;
    public static final Map<String, Spawn> SPAWNS;
    public static final ModConfigSpec.BooleanValue HUNT;
    public static final ModConfigSpec.IntValue HUNT_INTERVAL;
    public static final ModConfigSpec.IntValue HUNT_REST;
    public static final ModConfigSpec.BooleanValue PROTECT_YOUNG;
    public static final ModConfigSpec.IntValue PROTECT_RANGE;

    static {
        BUILDER.comment("Natural spawning in ordinary worlds. CityWorld's zoos and a pack's own spawns are separate from this.").push("spawns");
        NATURAL_SPAWNS = BUILDER.comment("Spawn the animals naturally at all. Off, they only come from zoos, eggs and whatever spawns them on purpose.")
                .define("natural", true);
        SPAWNS = Map.ofEntries(
                spawn("giraffe", 6, 2, 4, List.of("#minecraft:is_savanna")),
                spawn("zebra", 8, 3, 6, List.of("#minecraft:is_savanna")),
                spawn("lion", 2, 1, 3, List.of("#minecraft:is_savanna")),
                spawn("elephant", 2, 1, 3, List.of("#minecraft:is_savanna")),
                spawn("tiger", 3, 1, 2, List.of("#minecraft:is_jungle")),
                spawn("rhesus_macaque", 6, 3, 6, List.of("#minecraft:is_jungle")),
                spawn("deer", 8, 2, 5, List.of("#minecraft:is_forest", "#minecraft:is_taiga")),
                spawn("brown_bear", 3, 1, 2, List.of("#minecraft:is_taiga")));
        BUILDER.pop();

        BUILDER.comment("What the animals do to each other and to you.").push("behaviour");
        HUNT = BUILDER.comment("Lions hunt zebras, deer and farm animals; tigers hunt deer, macaques and farm animals.").define("hunt", true);
        HUNT_INTERVAL = BUILDER.comment("How keen a hunter is: on average one hunt starts per this many ticks of having prey in view (20 ticks a second).")
                .defineInRange("huntInterval", 1200, 20, 72000);
        HUNT_REST = BUILDER.comment("Ticks a predator rests after a kill before it hunts again.").defineInRange("huntRest", 6000, 0, 72000);
        PROTECT_YOUNG = BUILDER.comment("Lions, tigers and bears attack a player who comes near their cub, as a polar bear does.").define("protectYoung", true);
        PROTECT_RANGE = BUILDER.comment("How near to a cub (blocks) is too near.").defineInRange("protectRange", 8, 1, 32);
        BUILDER.pop();
    }

    private static Map.Entry<String, Spawn> spawn(String id, int weight, int min, int max, List<String> biomes) {
        BUILDER.push(id);
        Spawn s = new Spawn(
                BUILDER.define("enabled", true),
                BUILDER.comment("Spawn weight against the biome's other creatures.").defineInRange("weight", weight, 0, 1000),
                BUILDER.comment("Herd size when a group spawns.").defineInRange("minHerd", min, 1, 32),
                BUILDER.defineInRange("maxHerd", max, 1, 32),
                BUILDER.comment("Biome ids or #tags it spawns in.").defineListAllowEmpty("biomes", biomes, () -> "", o -> o instanceof String));
        BUILDER.pop();
        return Map.entry(id, s);
    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    private ZooConfig() {}
}
