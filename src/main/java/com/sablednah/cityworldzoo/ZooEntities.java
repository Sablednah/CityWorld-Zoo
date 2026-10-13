package com.sablednah.cityworldzoo;

import java.util.List;
import java.util.function.Supplier;

import com.sablednah.cityworldzoo.entity.BrownBear;
import com.sablednah.cityworldzoo.entity.Deer;
import com.sablednah.cityworldzoo.entity.Elephant;
import com.sablednah.cityworldzoo.entity.Giraffe;
import com.sablednah.cityworldzoo.entity.Lion;
import com.sablednah.cityworldzoo.entity.RhesusMacaque;
import com.sablednah.cityworldzoo.entity.Tiger;
import com.sablednah.cityworldzoo.entity.Zebra;
import com.sablednah.cityworldzoo.entity.ZooAnimal;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The eight species: entity type, spawn egg, attributes and spawn placement, declared once each.
 *
 * <p>Every species registers {@code ATTACK_DAMAGE} (ZombieMod's infected versions get melee goals and
 * vanilla's {@code Mob.doHurtTarget} throws on a mob without it), {@code FOLLOW_RANGE} and
 * {@code MOVEMENT_SPEED}. All of them are in {@code #cityworld_zoo:zoo_animals}
 * ({@code data/cityworld_zoo/tags/entity_type/zoo_animals.json}) for Threadwork's spawn rules.</p>
 *
 * <p>No natural spawns: zoos (CityWorld) and packs (Threadwork) decide where these live. The spawn
 * placements exist so a spawner, a datapack biome modifier or {@code /summon} gets the vanilla
 * on-ground rule.</p>
 */
public final class ZooEntities {

    private static final DeferredRegister.Entities ENTITIES = DeferredRegister.createEntities(ZooMod.MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ZooMod.MODID);

    public static final TagKey<EntityType<?>> ZOO_ANIMALS = TagKey.create(Registries.ENTITY_TYPE, id("zoo_animals"));

    /** One species: its id, type, spawn egg and the attribute set it is registered with. */
    public record Species<T extends ZooAnimal>(String id, DeferredHolder<EntityType<?>, EntityType<T>> type,
            DeferredItem<Item> egg, Supplier<AttributeSupplier.Builder> attributes) {
        public EntityType<T> entityType() { return type.get(); }
        public Identifier identifier() { return ZooEntities.id(id); }
    }

    private static <T extends ZooAnimal> Species<T> species(String id, EntityType.EntityFactory<T> factory,
            float width, float height, float eyeHeight, Supplier<AttributeSupplier.Builder> attributes) {
        DeferredHolder<EntityType<?>, EntityType<T>> type = ENTITIES.registerEntityType(id, factory, MobCategory.CREATURE,
                b -> b.sized(width, height).eyeHeight(eyeHeight).clientTrackingRange(10));
        // Entity types register before items (vanilla's registry order), so the type is real here.
        DeferredItem<Item> egg = ITEMS.registerItem(id + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(type.get()));
        return new Species<>(id, type, egg, attributes);
    }

    public static final Species<Giraffe> GIRAFFE = species("giraffe", Giraffe::new, 1.6F, 4.2F, 3.6F, Giraffe::createAttributes);
    public static final Species<RhesusMacaque> RHESUS_MACAQUE = species("rhesus_macaque", RhesusMacaque::new, 0.5F, 0.9F, 0.7F, RhesusMacaque::createAttributes);
    public static final Species<Zebra> ZEBRA = species("zebra", Zebra::new, 1.4F, 1.6F, 1.5F, Zebra::createAttributes);
    public static final Species<Deer> DEER = species("deer", Deer::new, 0.7F, 1.6F, 1.3F, Deer::createAttributes);
    public static final Species<Lion> LION = species("lion", Lion::new, 1.2F, 1.4F, 1.1F, Lion::createAttributes);
    public static final Species<Tiger> TIGER = species("tiger", Tiger::new, 1.2F, 1.4F, 1.1F, Tiger::createAttributes);
    public static final Species<Elephant> ELEPHANT = species("elephant", Elephant::new, 1.6F, 3.0F, 2.5F, Elephant::createAttributes);
    public static final Species<BrownBear> BROWN_BEAR = species("brown_bear", BrownBear::new, 1.4F, 1.4F, 1.1F, BrownBear::createAttributes);

    /** Declared after the species it lists: a collection declared before its fillers is null when they run. */
    public static final List<Species<? extends ZooAnimal>> ALL = List.of(GIRAFFE, RHESUS_MACAQUE, ZEBRA, DEER, LION, TIGER, ELEPHANT, BROWN_BEAR);

    /** Whether a type is one of ours by tag (26.2 dropped {@code EntityType.is(TagKey)}; the holder keeps it). */
    public static boolean isZooAnimal(EntityType<?> type) {
        return type.builtInRegistryHolder().is(ZOO_ANIMALS);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(ZooMod.MODID, path);
    }

    public static void register(IEventBus modBus) {
        ENTITIES.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(ZooEntities::attributes);
        modBus.addListener(ZooEntities::placements);
        modBus.addListener(ZooEntities::creativeTabs);
    }

    private static void attributes(EntityAttributeCreationEvent event) {
        for (Species<? extends ZooAnimal> s : ALL) event.put(s.entityType(), s.attributes().get().build());
    }

    private static void placements(RegisterSpawnPlacementsEvent event) {
        for (Species<? extends ZooAnimal> s : ALL) place(event, s);
    }

    private static <T extends ZooAnimal> void place(RegisterSpawnPlacementsEvent event, Species<T> s) {
        event.register(s.entityType(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Animal::checkAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    private static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.SPAWN_EGGS) return;
        for (Species<? extends ZooAnimal> s : ALL) event.accept(s.egg().get());
    }

    private ZooEntities() {}
}
