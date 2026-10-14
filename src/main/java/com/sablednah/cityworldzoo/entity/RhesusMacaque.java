package com.sablednah.cityworldzoo.entity;

import com.sablednah.cityworldzoo.ZooSounds;
import com.sablednah.cityworldzoo.entity.ai.CanopyLeapGoal;
import com.sablednah.cityworldzoo.entity.ai.ClimbTreeGoal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * The Last of Us: the lab monkeys at the University of Eastern Colorado, infected but never turned. Small,
 * quick, fond of fruit. Its calls are cut from recordings Sable found (art/rhesus_macaque/sounds, through
 * tools/audio/clips.py): chatter for ambient, a sharp falling cry for hurt, the long screeches for death.
 * A baby's voice is pitched up by vanilla ({@code getVoicePitch}).
 *
 * <p>It climbs trees: against a log or leaves it is on a climbable, the way a spider is against any wall
 * ({@link #onClimbable}, a synced flag set from the collision on the server), with the spider's wall-climber
 * navigation, no fall damage, {@link ClimbTreeGoal} to go up one and come back down, and
 * {@link CanopyLeapGoal} to jump a gap between canopies.</p>
 */
public class RhesusMacaque extends ZooAnimal {

    private static final EntityDataAccessor<Boolean> CLIMBING = SynchedEntityData.defineId(RhesusMacaque.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(RhesusMacaque.class, EntityDataSerializers.BOOLEAN);

    public RhesusMacaque(EntityType<? extends RhesusMacaque> type, Level level) {
        super(type, level);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WallClimberNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(4, new ClimbTreeGoal(this));
        this.goalSelector.addGoal(4, new CanopyLeapGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CLIMBING, false);
        builder.define(SITTING, false);
    }

    /** Sat down to feed in a tree (the climb goal's doing); the model sits and brings a hand to its mouth. */
    public boolean isSitting() {
        return this.entityData.get(SITTING);
    }

    public void setSitting(boolean sitting) {
        this.entityData.set(SITTING, sitting);
    }

    public boolean isClimbing() {
        return this.entityData.get(CLIMBING);
    }

    /** On the server, a climbable the moment it presses against a tree; the client reads the synced flag. */
    @Override
    public boolean onClimbable() {
        return this.level().isClientSide() ? isClimbing() : this.horizontalCollision && touchingTree();
    }

    @Override
    public void tick() {
        super.tick();
        // The pose is for climbing, not for bumping a trunk at ground level: off the ground and against a tree.
        if (!this.level().isClientSide()) this.entityData.set(CLIMBING, this.horizontalCollision && !this.onGround() && touchingTree());
    }

    /** A log or leaves beside it, at foot or head height, in any of the four directions. */
    private boolean touchingTree() {
        BlockPos at = this.blockPosition();
        for (Direction d : Direction.Plane.HORIZONTAL) {
            for (int dy = 0; dy <= 1; dy++) {
                BlockState s = this.level().getBlockState(at.relative(d).above(dy));
                if (s.is(BlockTags.LOGS) || s.is(BlockTags.LEAVES)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, net.minecraft.world.damagesource.DamageSource source) {
        return false;   // a monkey lands on its feet from any tree
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected double strollSpeed() {
        return 1.2D;
    }

    @Override
    protected Stride stride() {
        return Stride.RHESUS_MACAQUE;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.APPLE) || stack.is(Items.MELON_SLICE) || stack.is(Items.SWEET_BERRIES);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ZooSounds.RHESUS_MACAQUE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ZooSounds.RHESUS_MACAQUE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ZooSounds.RHESUS_MACAQUE_DEATH.get();
    }
}
