package com.sablednah.cityworldzoo.neoforge;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.sablednah.cityworldzoo.ZooConfig;
import com.sablednah.cityworldzoo.ZooEntities;
import com.sablednah.cityworldzoo.ZooMod;
import com.sablednah.cityworldzoo.entity.ZooAnimal;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Natural spawning from the config rather than from fixed data: one biome modifier
 * ({@code data/cityworld_zoo/neoforge/biome_modifier/natural_spawns.json}, type
 * {@code cityworld_zoo:natural_spawns}) that, as each biome is built, adds every enabled species whose
 * biome list names that biome or a tag it carries, at the configured weight and herd size.
 */
public final class ZooSpawns {

    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ZooMod.MODID);

    public static final MapCodec<NaturalSpawns> CODEC = MapCodec.unit(NaturalSpawns::new);
    static { SERIALIZERS.register("natural_spawns", () -> CODEC); }

    public static void register(IEventBus modBus) {
        SERIALIZERS.register(modBus);
    }

    /** The modifier itself: stateless, everything comes from {@link ZooConfig}. */
    public record NaturalSpawns() implements BiomeModifier {
        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase != Phase.ADD || !ZooConfig.NATURAL_SPAWNS.get()) return;
            for (ZooEntities.Species<? extends ZooAnimal> s : ZooEntities.ALL) {
                ZooConfig.Spawn c = ZooConfig.SPAWNS.get(s.id());
                if (c == null || !c.enabled().get() || c.weight().get() <= 0 || !matches(biome, c.biomes().get())) continue;
                int min = c.minHerd().get(), max = Math.max(min, c.maxHerd().get());
                builder.getMobSpawnSettings().addSpawn(MobCategory.CREATURE, c.weight().get(), new MobSpawnSettings.SpawnerData(s.entityType(), min, max));
            }
        }

        @Override
        public MapCodec<? extends BiomeModifier> codec() {
            return CODEC;
        }
    }

    /** Whether a biome is one of the entries: {@code #namespace:tag} is a tag, anything else a biome id. */
    public static boolean matches(Holder<Biome> biome, List<? extends String> entries) {
        for (String e : entries) {
            if (e == null || e.isBlank()) continue;
            if (e.startsWith("#")) {
                Identifier id = Identifier.tryParse(e.substring(1));
                if (id != null && biome.is(TagKey.create(Registries.BIOME, id))) return true;
            } else {
                Identifier id = Identifier.tryParse(e);
                if (id != null && biome.is(ResourceKey.create(Registries.BIOME, id))) return true;
            }
        }
        return false;
    }

    private ZooSpawns() {}
}
