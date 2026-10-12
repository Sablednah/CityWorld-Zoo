package com.sablednah.cityworldzoo.neoforge;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Server lifecycle: messages load before the server opens; the self-test runs once it is up, when asked. */
public final class ZooEvents {

    @SubscribeEvent
    public static void onAboutToStart(ServerAboutToStartEvent event) {
        Lang.load();
    }

    @SubscribeEvent
    public static void onStarted(ServerStartedEvent event) {
        if (Boolean.getBoolean("cityworld_zoo.selftest")) SelfTest.run(event.getServer());
    }

    private ZooEvents() {}
}
