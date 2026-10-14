package com.sablednah.cityworldzoo.entity;

import com.sablednah.cityworldzoo.ZooSounds;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** The Walking Dead: Shiva, the tiger who walked out of the zoo with King Ezekiel. Fights back. */
public class Tiger extends ZooAnimal {

    public Tiger(EntityType<? extends Tiger> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D);
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
