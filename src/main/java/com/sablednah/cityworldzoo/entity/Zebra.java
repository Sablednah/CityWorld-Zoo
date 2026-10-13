package com.sablednah.cityworldzoo.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * The Last of Us Part II: the zebra in the snare. A horse-shaped animal that is not a horse: no saddle, no riding.
 *
 * <p>One in a hundred wears the bitten skin (Sable's Madagascar nod, 2026-10-13): rolled from the entity's own
 * random in the constructor (no level read: CityWorld builds these on a worldgen thread), kept in
 * {@code Bitten}, drawn by the renderer from {@code zebra_bite.png}.</p>
 */
public class Zebra extends ZooAnimal {

    private static final EntityDataAccessor<Boolean> BITTEN = SynchedEntityData.defineId(Zebra.class, EntityDataSerializers.BOOLEAN);
    public static final int BITTEN_ONE_IN = 100;

    public Zebra(EntityType<? extends Zebra> type, Level level) {
        super(type, level);
        if (!level.isClientSide()) setBitten(this.random.nextInt(BITTEN_ONE_IN) == 0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BITTEN, false);
    }

    public boolean isBitten() {
        return this.entityData.get(BITTEN);
    }

    public void setBitten(boolean bitten) {
        this.entityData.set(BITTEN, bitten);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Bitten", isBitten());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setBitten(input.getBooleanOr("Bitten", false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 25.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT) || stack.is(Items.APPLE);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.HORSE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HORSE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.HORSE_STEP, 0.15F, 1.0F);
    }
}
