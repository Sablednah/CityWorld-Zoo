package com.sablednah.cityworldzoo.neoforge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.sablednah.cityworldzoo.ZooMod;

import net.neoforged.fml.loading.FMLPaths;

/**
 * Every player-facing string the server sends, owner-editable in {@code config/cityworld_zoo/messages.yml}
 * and merged on every start via {@code messages.known} (the family's Lang). Entity and item names are the
 * exception: they are vanilla translatable components in {@code assets/cityworld_zoo/lang}, because a
 * client must carry this mod to see the animals at all.
 */
public final class Lang {

    private static final Map<String, String> DEFAULTS = new LinkedHashMap<>();
    private static Map<String, String> active = new LinkedHashMap<>();

    static String def(String key, String template) {
        DEFAULTS.put(key, template);
        return key;
    }

    static {
        def("prefix", "&2[Zoo]&r ");
        def("msg.status", "{prefix}&f{count} species registered, {tagged} in #cityworld_zoo:zoo_animals; CityWorld {cityworld}.");
        def("msg.status.animal", "  &7{id}: &f{name}&7, {health} health, {speed} speed, {damage} damage, {tall}");
        def("msg.status.build", "&7Build: &f{build}");
        def("msg.status.cityworld.present", "present (its zoos take these animals)");
        def("msg.status.cityworld.absent", "absent (spawn eggs only)");
        def("msg.count.none", "{prefix}&7No zoo animals loaded within {radius} blocks of you.");
        def("msg.count.header", "{prefix}&fZoo animals loaded within {radius} blocks:");
        def("msg.count.entry", "  &7{name}: &f{count}");
    }

    public static String get(String key) {
        String template = active.getOrDefault(key, DEFAULTS.get(key));
        if (template == null) {
            ZooMod.LOGGER.warn("Missing message key '{}'", key);
            return key;
        }
        return template.contains("{prefix}") ? template.replace("{prefix}", active.getOrDefault("prefix", DEFAULTS.get("prefix"))) : template;
    }

    public static String fmt(String key, Object... kv) {
        String out = get(key);
        for (int n = 0; n + 1 < kv.length; n += 2) out = out.replace("{" + kv[n] + "}", String.valueOf(kv[n + 1]));
        return out;
    }

    public static int catalogueSize() {
        return DEFAULTS.size();
    }

    private static Path dir() { return FMLPaths.CONFIGDIR.get().resolve(ZooMod.MODID); }

    public static synchronized void load() {
        Path path = dir().resolve("messages.yml");
        Path known = dir().resolve("messages.known");
        try {
            Files.createDirectories(path.getParent());
            if (!Files.exists(path)) {
                StringBuilder sb = new StringBuilder("# CityWorld: Zoo messages -- every player-facing string. '&' colour codes work.\n# Deleted keys fall back to defaults; new keys are appended once, then left alone.\n\n");
                DEFAULTS.forEach((k, v) -> sb.append(line(k, v)));
                Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
            } else {
                Set<String> seen = Files.exists(known)
                        ? new LinkedHashSet<>(Files.readAllLines(known, StandardCharsets.UTF_8)) : Set.of();
                Set<String> present = new LinkedHashSet<>();
                Object parsed = new org.yaml.snakeyaml.Yaml().load(Files.readString(path, StandardCharsets.UTF_8));
                if (parsed instanceof Map<?, ?> map) map.keySet().forEach(k -> present.add(String.valueOf(k)));
                StringBuilder add = new StringBuilder();
                DEFAULTS.forEach((k, v) -> { if (!seen.contains(k) && !present.contains(k)) add.append(line(k, v)); });
                if (!add.isEmpty()) Files.writeString(path, Files.readString(path, StandardCharsets.UTF_8) + "\n# --- new keys ---\n" + add, StandardCharsets.UTF_8);
            }
            Map<String, String> loaded = new LinkedHashMap<>();
            Object parsed = new org.yaml.snakeyaml.Yaml().load(Files.readString(path, StandardCharsets.UTF_8));
            if (parsed instanceof Map<?, ?> map) map.forEach((k, v) -> { if (k != null && v != null) loaded.put(String.valueOf(k), String.valueOf(v)); });
            active = loaded;
            Files.writeString(known, String.join("\n", DEFAULTS.keySet()) + "\n", StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException e) {
            ZooMod.LOGGER.error("CityWorld: Zoo: could not load messages.yml -- using defaults", e);
            active = new LinkedHashMap<>();
        }
    }

    private static String line(String k, String v) {
        return k + ": \"" + v.replace("\\", "\\\\").replace("\"", "\\\"") + "\"\n";
    }

    private Lang() {}
}
