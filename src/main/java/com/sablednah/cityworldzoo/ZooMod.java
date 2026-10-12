package com.sablednah.cityworldzoo;

import com.mojang.logging.LogUtils;
import com.sablednah.cityworldzoo.neoforge.ZooCommands;
import com.sablednah.cityworldzoo.neoforge.ZooEvents;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

/**
 * CityWorld: Zoo — zoo animals from zombie media, as ordinary vanilla-style {@code Animal}s.
 *
 * <p>Eight species ({@link ZooEntities}), each a spawn egg, breedable, with vanilla goals and nothing
 * exotic, so another mod (ZombieMod's genera) can use them as base bodies. A datapack in this jar
 * ({@code data/cityworld_zoo/cityworld/zoo/}) fills CityWorld's zoos with them; without CityWorld
 * the animals still exist, from eggs.</p>
 *
 * <p><b>The hard rule:</b> CityWorld creates these entities on a worldgen thread through the generation
 * region (spawn reason {@code CHUNK_GENERATION}), then calls {@code finalizeSpawn}. Nothing in a
 * constructor or a {@code finalizeSpawn} may read the real level, or world creation deadlocks at
 * "Preparing spawn area". No species overrides {@code finalizeSpawn}; the self-test checks that.</p>
 */
@Mod(ZooMod.MODID)
public class ZooMod {
    public static final String MODID = "cityworld_zoo";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ZooMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("CityWorld: Zoo {} initialising", BuildInfo.describe());
        ZooEntities.register(modEventBus);
        NeoForge.EVENT_BUS.register(ZooEvents.class);
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> ZooCommands.register(event.getDispatcher()));
        if (FMLEnvironment.getDist().isClient()) com.sablednah.cityworldzoo.client.ZooClient.register(modEventBus);
    }
}
