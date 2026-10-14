package com.sablednah.cityworldzoo.entity.ai;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Standing on leaves, a monkey that sees more canopy two to four blocks away across a gap leaps for it: a
 * burst of horizontal speed with enough lift to clear a block, landing on the far leaves (it takes no fall
 * damage, so a miss costs nothing). Independent of {@link ClimbTreeGoal}: it fires wherever the monkey is
 * on a canopy, so a troop crosses from tree to tree.
 */
public class CanopyLeapGoal extends Goal {

    private final PathfinderMob mob;
    private @Nullable Vec3 target;
    private int cooldown;

    public CanopyLeapGoal(PathfinderMob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) { cooldown--; return false; }
        if (!mob.onGround() || mob.getRandom().nextInt(reducedTickDelay(80)) != 0) return false;
        Level level = mob.level();
        BlockPos here = mob.blockPosition();
        if (!level.getBlockState(here.below()).is(BlockTags.LEAVES)) return false;
        Direction[] dirs = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
        Direction d = dirs[mob.getRandom().nextInt(dirs.length)];
        // a gap of at least one block, then leaves to land on, two to four blocks out, within a block of our height
        if (!level.getBlockState(here.relative(d).below()).isAir()) return false;
        for (int dist = 2; dist <= 4; dist++) {
            for (int dy = 1; dy >= -1; dy--) {
                BlockPos land = here.relative(d, dist).above(dy);
                if (level.getBlockState(land.below()).is(BlockTags.LEAVES) && level.getBlockState(land).isAir() && level.getBlockState(land.above()).isAir()) {
                    target = Vec3.atBottomCenterOf(land);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return false;   // one leap, then the goal is over
    }

    @Override
    public void start() {
        if (target == null) return;
        Vec3 to = target.subtract(mob.position());
        double dist = Math.max(1.0, to.horizontalDistance());
        Vec3 push = new Vec3(to.x / dist, 0, to.z / dist).scale(0.18 + 0.07 * dist).add(0, 0.42 + Math.max(0, to.y) * 0.1, 0);
        mob.setDeltaMovement(push);
        mob.needsSync = true;   // 26.2: hasImpulse became needsSync
        mob.getLookControl().setLookAt(target);
        cooldown = 40 + mob.getRandom().nextInt(60);
        target = null;
    }
}
