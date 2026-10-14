package com.sablednah.cityworldzoo.entity.ai;

import java.util.EnumSet;

import com.sablednah.cityworldzoo.entity.RhesusMacaque;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Now and then a macaque on the ground picks a tree nearby, walks to the trunk and climbs it (the wall-climber
 * navigation walks it into the trunk, and {@link RhesusMacaque#onClimbable} lets it rise against logs and
 * leaves, up the outside of the canopy to the top). Up there it potters about for a while, then makes for
 * the ground beside the trunk, dropping off the edge: it takes no fall damage. On the canopy it also leaps
 * gaps ({@link CanopyLeapGoal#tryLeap}). A climber blocked by leaves overhead scrambles up through them to
 * the first air above, so an overhanging canopy never pins it under the branches.
 */
public class ClimbTreeGoal extends Goal {

    private static final int SEARCH = 10;
    private final RhesusMacaque mob;
    private @Nullable BlockPos trunkTop;
    private @Nullable BlockPos trunkBase;
    private int ticks;
    private int phase;          // 0 going to the tree and climbing, 1 on top, 2 coming down
    private int phaseTicks;
    private int stay;
    private int stuckTicks;
    private double lastY;

    public ClimbTreeGoal(RhesusMacaque mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (!mob.onGround() || mob.isBaby() || mob.getRandom().nextInt(reducedTickDelay(300)) != 0) return false;
        BlockPos top = findTree();
        if (top == null) return false;
        trunkTop = top;
        trunkBase = base(top);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return ticks < 20 * 90 && phase < 3;
    }

    @Override
    public void start() {
        ticks = 0; phase = 0; phaseTicks = 0;
        stay = 20 * (8 + mob.getRandom().nextInt(12));
        mob.getNavigation().moveTo(trunkTop.getX() + 0.5, trunkTop.getY() + 1, trunkTop.getZ() + 0.5, 1.1D);
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        trunkTop = null; trunkBase = null;
    }

    @Override
    public void tick() {
        ticks++; phaseTicks++;
        Level level = mob.level();
        switch (phase) {
            case 0 -> {
                // Keep steering into the trunk top; the climb happens on collision with the logs and leaves.
                if (mob.getNavigation().isDone())
                    mob.getMoveControl().setWantedPosition(trunkTop.getX() + 0.5, trunkTop.getY() + 2, trunkTop.getZ() + 0.5, 1.1D);
                // Pinned under leaves (climbing, but not rising): scramble up through them to the first air above.
                if (mob.horizontalCollision && !mob.onGround() && mob.getY() <= lastY + 0.01) stuckTicks++; else stuckTicks = 0;
                lastY = mob.getY();
                if (stuckTicks > 15) {
                    stuckTicks = 0;
                    BlockPos p = mob.blockPosition().above();
                    int climbed = 0;
                    while (level.getBlockState(p).is(BlockTags.LEAVES) && climbed < 6) { p = p.above(); climbed++; }
                    if (climbed > 0 && level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir())
                        mob.setPos(Vec3.atBottomCenterOf(p));
                }
                boolean onCanopy = level.getBlockState(mob.blockPosition().below()).is(BlockTags.LEAVES) && mob.onGround();
                if (onCanopy || mob.getY() >= trunkTop.getY() + 1) { phase = 1; phaseTicks = 0; }
                else if (phaseTicks > 20 * 25) phase = 3;   // never got up: give up
            }
            case 1 -> {
                // On top: wander the canopy a little (straight-line moves, leaves are solid to stand on), and
                // leap a gap to the next tree now and then.
                if (CanopyLeapGoal.tryLeap(mob)) { phaseTicks = Math.max(phaseTicks, 1); }
                else if (phaseTicks % 60 == 0) {
                    BlockPos here = mob.blockPosition();
                    RandomSource r = mob.getRandom();
                    for (int i = 0; i < 8; i++) {
                        BlockPos p = here.offset(r.nextInt(7) - 3, r.nextInt(3) - 1, r.nextInt(7) - 3);
                        if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).is(BlockTags.LEAVES)) {
                            mob.getMoveControl().setWantedPosition(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, 0.8D);
                            break;
                        }
                    }
                }
                if (phaseTicks > stay) { phase = 2; phaseTicks = 0; }
            }
            case 2 -> {
                // Down: head for the ground beside the trunk; walking off the edge is fine.
                if (trunkBase != null)
                    mob.getMoveControl().setWantedPosition(trunkBase.getX() + 1.5, trunkBase.getY(), trunkBase.getZ() + 0.5, 1.0D);
                if (mob.onGround() && !level.getBlockState(mob.blockPosition().below()).is(BlockTags.LEAVES)) phase = 3;
                else if (phaseTicks > 20 * 15) phase = 3;
            }
            default -> {}
        }
    }

    /** The topmost log of a trunk within reach that has leaves above it, or null. */
    private @Nullable BlockPos findTree() {
        Level level = mob.level();
        BlockPos here = mob.blockPosition();
        RandomSource r = mob.getRandom();
        for (int i = 0; i < 24; i++) {
            BlockPos p = here.offset(r.nextInt(SEARCH * 2 + 1) - SEARCH, r.nextInt(5) - 1, r.nextInt(SEARCH * 2 + 1) - SEARCH);
            if (!level.getBlockState(p).is(BlockTags.LOGS)) continue;
            BlockPos top = p;
            while (level.getBlockState(top.above()).is(BlockTags.LOGS) && top.getY() - p.getY() < 20) top = top.above();
            boolean leavesAbove = false;
            for (int dy = 1; dy <= 3 && !leavesAbove; dy++)
                for (int dx = -1; dx <= 1 && !leavesAbove; dx++)
                    for (int dz = -1; dz <= 1 && !leavesAbove; dz++)
                        leavesAbove = level.getBlockState(top.offset(dx, dy, dz)).is(BlockTags.LEAVES);
            if (leavesAbove) return top;
        }
        return null;
    }

    private BlockPos base(BlockPos top) {
        BlockPos b = top;
        while (mob.level().getBlockState(b.below()).is(BlockTags.LOGS) && top.getY() - b.getY() < 20) b = b.below();
        return b;
    }
}
