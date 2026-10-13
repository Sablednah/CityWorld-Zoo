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
 * Resident Evil Outbreak: the zoo's elephant, before and after. Slow, heavy, shoves back when hurt.
 *
 * <p>Two kinds, Sable's call (2026-10-13): a <b>bull</b>, 3.5 blocks tall with tusks, and a <b>cow</b>, 3 blocks
 * and tuskless; half of them are bulls. Calves are never drawn with tusks. The kind is rolled from the
 * entity's own random in the constructor (no level read: CityWorld builds these on a worldgen thread) and
 * kept in {@code Tusked}; the bull's extra size is vanilla's {@code minecraft:scale} attribute, so its
 * hitbox, eye height and model all follow.</p>
 */
public class Elephant extends ZooAnimal {

    private static final EntityDataAccessor<Boolean> TUSKED = SynchedEntityData.defineId(Elephant.class, EntityDataSerializers.BOOLEAN);
    /** A bull is 3.5 blocks to a cow's 3. */
    public static final double BULL_SCALE = 3.5D / 3.0D;

    public Elephant(EntityType<? extends Elephant> type, Level level) {
        super(type, level);
        if (!level.isClientSide()) setTusked(this.random.nextBoolean());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TUSKED, false);
    }

    public boolean isTusked() {
        return this.entityData.get(TUSKED);
    }

    public void setTusked(boolean tusked) {
        this.entityData.set(TUSKED, tusked);
        var scale = this.getAttribute(Attributes.SCALE);
        if (scale != null) scale.setBaseValue(tusked ? BULL_SCALE : 1.0D);
        this.refreshDimensions(); // the attribute alone leaves the old hitbox until the next size check
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("Tusked", isTusked());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setTusked(input.getBooleanOr("Tusked", false));
    }

    @Override
    protected boolean fightsBack() {
        return true;
    }

    @Override
    protected double strollSpeed() {
        return 0.8D;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT) || stack.is(Items.APPLE) || stack.is(Items.HAY_BLOCK);
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * (isTusked() ? 0.55F : 0.65F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CAMEL_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CAMEL_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CAMEL_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.RAVAGER_STEP, 0.2F, 0.8F);
    }
}
