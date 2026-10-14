package com.sablednah.cityworldzoo.entity;

import java.util.Optional;

import com.sablednah.cityworldzoo.ZooSounds;
import com.sablednah.cityworldzoo.entity.ai.BrowseLeavesGoal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.jspecify.annotations.Nullable;
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
 * The Last of Us: the giraffes at the top of Salt Lake City. Tall, placid, eats wheat. Mostly silent. Feeds from
 * trees: {@link BrowseLeavesGoal} finds a leaf block three to six blocks up, walks beside it and the neck
 * reaches in (the leaf is synced as {@code Browsing} for the client to pose the neck and head). Leaves are
 * never broken.
 */
public class Giraffe extends ZooAnimal {

    private static final EntityDataAccessor<Optional<BlockPos>> BROWSING = SynchedEntityData.defineId(Giraffe.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public Giraffe(EntityType<? extends Giraffe> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(5, new BrowseLeavesGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BROWSING, Optional.empty());
    }

    /** The leaf block it is feeding from, while it is. */
    public Optional<BlockPos> browsing() {
        return this.entityData.get(BROWSING);
    }

    public void setBrowsing(@Nullable BlockPos leaf) {
        this.entityData.set(BROWSING, Optional.ofNullable(leaf));
    }

    /** A mouthful: the chewing sound, from the browse goal. */
    public void chew() {
        this.makeSound(ZooSounds.GIRAFFE_EAT.get());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected Stride stride() {
        return Stride.GIRAFFE;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT);
    }

    /** A snort, rarely: a giraffe says almost nothing a person can hear. */
    @Override
    protected SoundEvent getAmbientSound() {
        return ZooSounds.GIRAFFE_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 600;
    }

    /** Chewing leaves, when fed (vanilla's {@code playEatingSound} is the hook on 26.2). */
    @Override
    protected void playEatingSound() {
        this.makeSound(ZooSounds.GIRAFFE_EAT.get());
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
        this.playSound(SoundEvents.CAMEL_STEP, 0.15F, 0.8F);
    }
}
