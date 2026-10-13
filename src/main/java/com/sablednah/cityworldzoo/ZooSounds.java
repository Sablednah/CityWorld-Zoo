package com.sablednah.cityworldzoo;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The animals' own sound events. Each is defined in {@code assets/cityworld_zoo/sounds.json}, where its takes
 * are either files under {@code assets/cityworld_zoo/sounds/} (credited in {@code sounds/CREDITS.md}) or
 * vanilla sound events borrowed by reference. A species whose sounds are still vanilla stand-ins names the
 * vanilla events directly in its class; it moves here when it gets takes of its own.
 */
public final class ZooSounds {

    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ZooMod.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ZEBRA_AMBIENT = sound("entity.zebra.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZEBRA_HURT = sound("entity.zebra.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZEBRA_DEATH = sound("entity.zebra.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> ZEBRA_ANGRY = sound("entity.zebra.angry");
    public static final DeferredHolder<SoundEvent, SoundEvent> RHESUS_MACAQUE_AMBIENT = sound("entity.rhesus_macaque.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> RHESUS_MACAQUE_HURT = sound("entity.rhesus_macaque.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> RHESUS_MACAQUE_DEATH = sound("entity.rhesus_macaque.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> LION_AMBIENT = sound("entity.lion.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> LION_ANGRY = sound("entity.lion.angry");
    public static final DeferredHolder<SoundEvent, SoundEvent> LION_HURT = sound("entity.lion.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> LION_DEATH = sound("entity.lion.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_AMBIENT = sound("entity.tiger.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_ANGRY = sound("entity.tiger.angry");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_HURT = sound("entity.tiger.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIGER_DEATH = sound("entity.tiger.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> BROWN_BEAR_AMBIENT = sound("entity.brown_bear.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRAFFE_AMBIENT = sound("entity.giraffe.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIRAFFE_EAT = sound("entity.giraffe.eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_AMBIENT = sound("entity.elephant.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_ANGRY = sound("entity.elephant.angry");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_HURT = sound("entity.elephant.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ELEPHANT_DEATH = sound("entity.elephant.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_AMBIENT = sound("entity.deer.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_AMBIENT_BABY = sound("entity.deer.ambient_baby");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_HURT = sound("entity.deer.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_DEATH = sound("entity.deer.death");

    /** Every event, for the self-test to check against sounds.json. */
    public static java.util.List<DeferredHolder<SoundEvent, SoundEvent>> all() {
        return java.util.List.of(ZEBRA_AMBIENT, ZEBRA_HURT, ZEBRA_DEATH, ZEBRA_ANGRY, RHESUS_MACAQUE_AMBIENT, RHESUS_MACAQUE_HURT, RHESUS_MACAQUE_DEATH,
                LION_AMBIENT, LION_ANGRY, LION_HURT, LION_DEATH, TIGER_AMBIENT, TIGER_ANGRY, TIGER_HURT, TIGER_DEATH,
                BROWN_BEAR_AMBIENT, GIRAFFE_AMBIENT, GIRAFFE_EAT, ELEPHANT_AMBIENT, ELEPHANT_ANGRY, ELEPHANT_HURT, ELEPHANT_DEATH,
                DEER_AMBIENT, DEER_AMBIENT_BABY, DEER_HURT, DEER_DEATH);
    }

    private static DeferredHolder<SoundEvent, SoundEvent> sound(String id) {
        return SOUNDS.register(id, () -> SoundEvent.createVariableRangeEvent(ZooEntities.id(id)));
    }

    public static void register(IEventBus modBus) {
        SOUNDS.register(modBus);
    }

    private ZooSounds() {}
}
