package com.sablednah.cityworldzoo.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * What every zoo animal shares: an ordinary {@link Animal} with vanilla goals, breedable with its food,
 * whose young is its own type.
 *
 * <p><b>Keep this ordinary.</b> ZombieMod makes infected versions by creating the type through the normal
 * factory and layering goals on from data, so the shape it relies on is exactly this: the standard
 * {@code (EntityType, Level)} constructor, {@code PathfinderMob} goals, and an attribute set that includes
 * attack damage. And nothing here may read the level during construction or spawn setup: CityWorld
 * creates these on a worldgen thread (see {@code ZooMod}). Do not override {@code finalizeSpawn}.</p>
 */
public abstract class ZooAnimal extends Animal {

    protected ZooAnimal(EntityType<? extends ZooAnimal> type, Level level) {
        super(type, level);
    }

    /** Whether this animal fights back when hurt (a melee goal and a hurt-by target) or runs. */
    protected boolean fightsBack() {
        return false;
    }

    /** Walking speed modifier for the stroll, follow and tempt goals. */
    protected double strollSpeed() {
        return 1.0D;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        if (fightsBack()) {
            this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25D, true));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        } else {
            this.goalSelector.addGoal(1, new PanicGoal(this, 1.5D));
        }
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1D, this::isFood, false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, strollSpeed()));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return (AgeableMob) this.getType().create(level, EntitySpawnReason.BREEDING);
    }
}
