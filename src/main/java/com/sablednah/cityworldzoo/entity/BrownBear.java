package com.sablednah.cityworldzoo.entity;

import com.sablednah.cityworldzoo.ZooSounds;
import com.sablednah.cityworldzoo.entity.ai.ProtectYoungGoal;

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

/** The zoo's bear: a polar bear's shape in brown. Fights back, eats what a bear eats. */
public class BrownBear extends ZooAnimal {

    public BrownBear(EntityType<? extends BrownBear> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.targetSelector.addGoal(2, new ProtectYoungGoal(this));
    }

    @Override
    protected boolean fightsBack() {
        return true;
    }

    @Override
    protected Stride stride() {
        return Stride.BROWN_BEAR;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return BigCats.isMeat(stack) || stack.is(Items.SWEET_BERRIES) || stack.is(Items.HONEY_BOTTLE);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isBaby() ? SoundEvents.POLAR_BEAR_AMBIENT_BABY : ZooSounds.BROWN_BEAR_AMBIENT.get();
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.POLAR_BEAR_WARNING;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.POLAR_BEAR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.POLAR_BEAR_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.POLAR_BEAR_STEP, 0.15F, 1.0F);
    }
}
