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
 * damage, so a miss costs nothing). {@link ClimbTreeGoal} calls {@link #tryLeap} itself while it holds the
 * monkey on a canopy (two goals cannot share the move flag); this goal covers a monkey on leaves on its own,
 * so a troop crosses from tree to tree either way.
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
        if (!mob.onGround() || mob.getRandom().nextInt(reducedTickDelay(60)) != 0) return false;
        target = findLanding(mob);
        return target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return false;   // one leap, then the goal is over
    }

    @Override
    public void start() {
        if (target != null) leap(mob, target);
        cooldown = 40 + mob.getRandom().nextInt(60);
        target = null;
    }

    /** Standing on leaves: a landing spot across a gap in a random direction, or null. */
    public static @Nullable Vec3 findLanding(PathfinderMob mob) {
        Level level = mob.level();
        BlockPos here = mob.blockPosition();
        if (!level.getBlockState(here.below()).is(BlockTags.LEAVES)) return null;
        Direction[] dirs = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
        Direction d = dirs[mob.getRandom().nextInt(dirs.length)];
        // a gap of at least one block, then leaves to land on, two to five blocks out, within a block of our height
        if (!level.getBlockState(here.relative(d).below()).isAir()) return null;
        for (int dist = 2; dist <= 5; dist++) {
            for (int dy = 1; dy >= -1; dy--) {
                BlockPos land = here.relative(d, dist).above(dy);
                if (level.getBlockState(land.below()).is(BlockTags.LEAVES) && level.getBlockState(land).isAir() && level.getBlockState(land.above()).isAir())
                    return Vec3.atBottomCenterOf(land);
            }
        }
        return null;
    }

    /** The jump itself: enough forward speed for the distance and lift to clear a block and the rise. */
    public static void leap(PathfinderMob mob, Vec3 target) {
        Vec3 to = target.subtract(mob.position());
        double dist = Math.max(1.0, to.horizontalDistance());
        Vec3 push = new Vec3(to.x / dist, 0, to.z / dist).scale(0.16 + 0.075 * dist).add(0, 0.45 + Math.max(0, to.y) * 0.12, 0);
        mob.setDeltaMovement(push);
        mob.needsSync = true;   // 26.2: hasImpulse became needsSync
        mob.getLookControl().setLookAt(target);
    }

    /** Leap now and then from wherever the monkey stands on leaves; for the climb goal's canopy phase. */
    public static boolean tryLeap(PathfinderMob mob) {
        if (!mob.onGround() || mob.getRandom().nextInt(60) != 0) return false;
        Vec3 land = findLanding(mob);
        if (land == null) return false;
        leap(mob, land);
        return true;
    }
}
