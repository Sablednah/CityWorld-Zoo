package com.sablednah.cityworldzoo.entity;

import java.util.Set;
import java.util.stream.Collectors;

import com.sablednah.cityworldzoo.ZooSounds;
import com.sablednah.cityworldzoo.entity.ai.HuntGoal;
import com.sablednah.cityworldzoo.entity.ai.ProtectYoungGoal;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** The Walking Dead: Shiva, the tiger who walked out of the zoo with King Ezekiel. Fights back. */
public class Tiger extends ZooAnimal {

    public Tiger(EntityType<? extends Tiger> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D);
    }

    /** What it hunts (ids, resolved at first use so our own types are registered by then). */
    private static final Set<String> PREY_IDS = Set.of("cityworld_zoo:deer", "cityworld_zoo:rhesus_macaque", "minecraft:pig", "minecraft:sheep", "minecraft:cow", "minecraft:rabbit", "minecraft:chicken");
    private static Set<EntityType<?>> prey;

    private static Set<EntityType<?>> prey() {
        if (prey == null) prey = PREY_IDS.stream().map(id -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(id))).collect(Collectors.toSet());
        return prey;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(2, new ProtectYoungGoal(this));
        this.targetSelector.addGoal(3, new HuntGoal(this, prey()));
    }

    @Override
    protected boolean fightsBack() {
        return true;
    }

    @Override
    protected Stride stride() {
        return Stride.TIGER;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return BigCats.isMeat(stack);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ZooSounds.TIGER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getAngrySound() {
        return ZooSounds.TIGER_ANGRY.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZooSounds.TIGER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ZooSounds.TIGER_DEATH.get();
    }
}
