package com.sablednah.cityworldzoo.entity.ai;

import com.sablednah.cityworldzoo.ZooConfig;
import com.sablednah.cityworldzoo.entity.ZooAnimal;

import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

/**
 * A grown animal goes for a player who comes near a cub of its kind, as a polar bear does: only while a
 * baby of the same species is within the config's range. Off in config, it never fires.
 */
public class ProtectYoungGoal extends NearestAttackableTargetGoal<Player> {

    private final ZooAnimal parent;

    public ProtectYoungGoal(ZooAnimal parent) {
        super(parent, Player.class, 20, true, true, null);
        this.parent = parent;
    }

    @Override
    public boolean canUse() {
        if (!ZooConfig.PROTECT_YOUNG.get() || parent.isBaby() || !super.canUse()) return false;
        double r = ZooConfig.PROTECT_RANGE.get();
        return parent.level().getEntitiesOfClass(parent.getClass(), parent.getBoundingBox().inflate(r, r / 2, r), ZooAnimal::isBaby).stream().findAny().isPresent();
    }

    @Override
    protected double getFollowDistance() {
        return super.getFollowDistance() * 0.5;
    }
}
