package com.sablednah.cityworldzoo.entity;

import com.sablednah.cityworldzoo.ZooSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The Last of Us (Ellie's winter hunt) and I Am Legend (the herd on Fifth Avenue). Skittish; bolts when hurt.
 * Grunts, mews and the odd stag roar (art/deer/sounds, through tools/audio/clips.py); a fawn bleats; the
 * alarm snort when hurt; hooves are the goat's.
 */
public class Deer extends ZooAnimal {

    public Deer(EntityType<? extends Deer> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 14.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT) || stack.is(Items.APPLE) || stack.is(Items.SWEET_BERRIES);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isBaby() ? ZooSounds.DEER_AMBIENT_BABY.get() : ZooSounds.DEER_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240; // a quiet animal
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZooSounds.DEER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ZooSounds.DEER_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.GOAT_STEP, 0.15F, 1.0F);
    }
}
