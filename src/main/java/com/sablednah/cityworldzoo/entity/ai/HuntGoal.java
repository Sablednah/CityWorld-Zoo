package com.sablednah.cityworldzoo.entity.ai;

import java.util.Set;

import com.sablednah.cityworldzoo.ZooConfig;
import com.sablednah.cityworldzoo.entity.ZooAnimal;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

/**
 * A predator picks prey it can see, now and then, the way a fox goes for chickens: the chance is the
 * config's hunt interval, and a kill is followed by a rest. The chase and the bite are the mob's ordinary
 * melee goal; this only chooses the target. Off in config, it never fires.
 */
public class HuntGoal extends NearestAttackableTargetGoal<LivingEntity> {

    private final ZooAnimal hunter;

    public HuntGoal(ZooAnimal hunter, Set<EntityType<?>> prey) {
        super(hunter, LivingEntity.class, 10, true, false, target -> prey.contains(target.getType()));
        this.hunter = hunter;
    }

    @Override
    public boolean canUse() {
        if (!ZooConfig.HUNT.get() || hunter.isBaby() || hunter.huntRest > 0) return false;
        // the parent rolls its own 1-in-10 before looking; this is the keenness on top of that
        if (hunter.getRandom().nextInt(Math.max(1, ZooConfig.HUNT_INTERVAL.get() / 10)) != 0) return false;
        return super.canUse();
    }

    @Override
    public void stop() {
        LivingEntity t = hunter.getTarget();
        if (t != null && !t.isAlive()) hunter.huntRest = ZooConfig.HUNT_REST.get();   // fed: rest before the next
        super.stop();
    }
}
