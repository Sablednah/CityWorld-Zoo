package com.sablednah.cityworldzoo.entity.ai;

import java.util.EnumSet;

import com.sablednah.cityworldzoo.entity.RhesusMacaque;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Now and then a macaque on the ground picks a tree nearby, walks to the trunk and climbs it straight up:
 * each tick it aims at the log it is actually pressed against (not the trunk's top, which on a bent acacia
 * is another column, which had it sliding off the corner), and {@link RhesusMacaque#onClimbable} lets it
 * rise. Stuck under a branch it moves round the trunk to a face with open sky and carries on; stuck under
 * leaves it scrambles up through them; where the trunk bends it steps across to the next log. Up top it
 * potters about the canopy, leaps gaps ({@link CanopyLeapGoal#tryLeap}), then makes for the ground beside
 * the trunk and drops off the edge (no fall damage). Fallen out of the tree, the climb is over.
 */
public class ClimbTreeGoal extends Goal {

    private static final int SEARCH = 10;
    private final RhesusMacaque mob;
    private @Nullable BlockPos trunkTop;
    private @Nullable BlockPos trunkBase;
    private int ticks;
    private int phase;          // 0 walking to the tree and climbing, 1 on top, 2 coming down, 3 done
    private int phaseTicks;
    private int stay;
    private int stuckTicks;
    private double lastY;

    public ClimbTreeGoal(RhesusMacaque mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!mob.onGround() || mob.isBaby() || mob.getRandom().nextInt(reducedTickDelay(120)) != 0) return false;
        BlockPos top = findTree();
        if (top == null) return false;
        trunkTop = top;
        trunkBase = base(top);
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return ticks < 20 * 240 && phase < 3;
    }

    @Override
    public void start() {
        ticks = 0; phase = 0; phaseTicks = 0; stuckTicks = 0; lastY = mob.getY();
        stay = 20 * (40 + mob.getRandom().nextInt(80));
        mob.getNavigation().moveTo(trunkBase.getX() + 0.5, trunkBase.getY(), trunkBase.getZ() + 0.5, 1.1D);
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        mob.setSitting(false);
        trunkTop = null; trunkBase = null;
    }

    @Override
    public void tick() {
        ticks++; phaseTicks++;
        Level level = mob.level();
        switch (phase) {
            case 0 -> climb(level);
            case 1 -> {
                if (mob.onGround() && !level.getBlockState(mob.blockPosition().below()).is(BlockTags.LEAVES) && phaseTicks > 10) {
                    phase = 3;   // fell out of the tree: the climb is over
                    return;
                }
                // Mostly it sits and feeds; every so often it gets up, moves a step or leaps, and sits again.
                boolean upAndAbout = (phaseTicks / 100) % 3 == 0;
                mob.setSitting(!upAndAbout);
                if (!upAndAbout) {
                    mob.getNavigation().stop();
                } else if (CanopyLeapGoal.tryLeap(mob)) { phaseTicks = Math.max(phaseTicks, 1); }
                else if (phaseTicks % 60 == 0) {
                    // a step or two away, never our own block (a target under one's feet spins the body), with
                    // leaves under the way there too, so it does not walk off a gap
                    BlockPos here = mob.blockPosition();
                    RandomSource r = mob.getRandom();
                    for (int i = 0; i < 8; i++) {
                        int ox = r.nextInt(5) - 2, oz = r.nextInt(5) - 2;
                        if (ox == 0 && oz == 0) continue;
                        BlockPos p = here.offset(ox, 0, oz);
                        BlockPos mid = here.offset(ox / 2, 0, oz / 2);
                        if (level.getBlockState(p).isAir() && level.getBlockState(p.below()).is(BlockTags.LEAVES)
                                && level.getBlockState(mid).isAir() && level.getBlockState(mid.below()).is(BlockTags.LEAVES)) {
                            mob.getMoveControl().setWantedPosition(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, 0.8D);
                            break;
                        }
                    }
                }
                if (phaseTicks > stay) { phase = 2; phaseTicks = 0; mob.setSitting(false); }
            }
            case 2 -> {
                if (trunkBase != null)
                    mob.getMoveControl().setWantedPosition(trunkBase.getX() + 1.5, trunkBase.getY(), trunkBase.getZ() + 0.5, 1.0D);
                if (mob.onGround() && !level.getBlockState(mob.blockPosition().below()).is(BlockTags.LEAVES)) phase = 3;
                else if (phaseTicks > 20 * 15) phase = 3;
            }
            default -> {}
        }
    }

    /** Phase 0: walk to the trunk, then straight up the face it is pressed to. */
    private void climb(Level level) {
        BlockPos here = mob.blockPosition();
        boolean onCanopy = mob.onGround() && level.getBlockState(here.below()).is(BlockTags.LEAVES);
        if (onCanopy || mob.getY() >= trunkTop.getY() + 1) { phase = 1; phaseTicks = 0; return; }
        if (phaseTicks > 20 * 30) { phase = 3; return; }   // never got up: give up

        BlockPos hold = treeBeside(level, here);
        if (hold == null) {
            // not at the tree yet: keep walking to the base
            if (mob.getNavigation().isDone() && phaseTicks % 20 == 0)
                mob.getNavigation().moveTo(trunkBase.getX() + 0.5, trunkBase.getY(), trunkBase.getZ() + 0.5, 1.1D);
            return;
        }
        // Pressed to the tree: aim into the very block we hold, a little above us, so we climb its face dead straight.
        mob.getNavigation().stop();
        mob.getMoveControl().setWantedPosition(hold.getX() + 0.5, mob.getY() + 1.5, hold.getZ() + 0.5, 1.0D);
        mob.getLookControl().setLookAt(hold.getX() + 0.5, mob.getEyeY() + 1.0, hold.getZ() + 0.5);

        if (mob.getY() <= lastY + 0.01) stuckTicks++; else stuckTicks = 0;
        lastY = mob.getY();
        if (stuckTicks < 12) return;
        stuckTicks = 0;

        BlockPos above = here.above();
        BlockPos above2 = here.above(2);
        if (level.getBlockState(above).is(BlockTags.LOGS) || level.getBlockState(above2).is(BlockTags.LOGS)) {
            // a branch overhead: round the trunk to a face with open sky above it
            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos side = hold.relative(d);
                if (side.equals(here)) continue;
                if (level.getBlockState(side).isAir() && level.getBlockState(side.above()).isAir() && level.getBlockState(side.above(2)).isAir()
                        && !level.getBlockState(side.above(3)).is(BlockTags.LOGS)) {
                    mob.setPos(Vec3.atBottomCenterOf(side));
                    return;
                }
            }
        } else if (level.getBlockState(above).is(BlockTags.LEAVES)) {
            // leaves overhead: scramble up through them to the first air
            BlockPos p = above;
            int climbed = 0;
            while (level.getBlockState(p).is(BlockTags.LEAVES) && climbed < 10) { p = p.above(); climbed++; }
            if (level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir()) mob.setPos(Vec3.atBottomCenterOf(p));
        } else {
            // open air above and we stopped rising: the trunk has ended or bent. Step across to a log within a block.
            for (int dy = 2; dy >= 0; dy--) {
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    BlockPos log = here.relative(d).above(dy);
                    if (level.getBlockState(log).is(BlockTags.LOGS) && !log.equals(hold)) {
                        BlockPos stand = log.above();
                        if (level.getBlockState(stand).isAir() && level.getBlockState(stand.above()).isAir()) {
                            mob.setPos(Vec3.atBottomCenterOf(stand));
                            return;
                        }
                    }
                }
            }
            phase = 1; phaseTicks = 0;   // nothing further up: this is the top
        }
    }

    /** The log (preferred) or leaves block beside the monkey at foot or head height, or null. */
    private static @Nullable BlockPos treeBeside(Level level, BlockPos at) {
        BlockPos leaves = null;
        for (int dy = 0; dy <= 1; dy++) {
            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos p = at.relative(d).above(dy);
                var s = level.getBlockState(p);
                if (s.is(BlockTags.LOGS)) return p;
                if (leaves == null && s.is(BlockTags.LEAVES)) leaves = p;
            }
        }
        return leaves;
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
