package com.sablednah.cityworldzoo.entity.ai;

import java.util.EnumSet;

import com.sablednah.cityworldzoo.entity.Giraffe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * A giraffe eats from trees: now and then it finds a leaf block three to six blocks above the ground within
 * reach, walks to stand beside it, and feeds -- the leaf position is synced so the client bends the neck and
 * head to put the muzzle in it -- chewing for a while before it wanders off. The leaves are never broken.
 */
public class BrowseLeavesGoal extends Goal {

    private static final int SEARCH = 10;
    private final Giraffe mob;
    private @Nullable BlockPos leaf;
    private @Nullable Vec3 stand;
    private int ticks;
    private int eating;
    private int feed;

    public BrowseLeavesGoal(Giraffe mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (mob.isBaby() || mob.getRandom().nextInt(reducedTickDelay(400)) != 0) return false;
        return find();
    }

    @Override
    public boolean canContinueToUse() {
        return leaf != null && ticks < 20 * 40 && (eating == 0 || eating < feed);
    }

    @Override
    public void start() {
        ticks = 0; eating = 0;
        feed = 20 * (6 + mob.getRandom().nextInt(8));
        mob.getNavigation().moveTo(stand.x, stand.y, stand.z, 1.0D);
    }

    @Override
    public void stop() {
        mob.setBrowsing(null);
        mob.getNavigation().stop();
        leaf = null; stand = null;
    }

    @Override
    public void tick() {
        ticks++;
        if (leaf == null || stand == null) return;
        Vec3 leafCentre = Vec3.atCenterOf(leaf);
        mob.getLookControl().setLookAt(leafCentre.x, mob.getEyeY(), leafCentre.z);
        double flat = mob.position().multiply(1, 0, 1).distanceTo(stand.multiply(1, 0, 1));
        if (eating == 0) {
            if (flat < 1.2 || (mob.getNavigation().isDone() && flat < 2.5)) {
                eating = 1;
                mob.getNavigation().stop();
                mob.setBrowsing(leaf);
            } else if (mob.getNavigation().isDone() && ticks % 20 == 0) {
                mob.getNavigation().moveTo(stand.x, stand.y, stand.z, 1.0D);
            }
        } else {
            eating++;
            // face the leaves squarely while feeding
            Vec3 to = leafCentre.subtract(mob.position());
            float yaw = (float) (Math.toDegrees(Math.atan2(-to.x, to.z)));
            mob.setYRot(yaw); mob.yBodyRot = yaw; mob.yHeadRot = yaw;
            if (eating % 35 == 1) mob.chew();
            if (!mob.level().getBlockState(leaf).is(BlockTags.LEAVES)) eating = feed;   // someone took the tree
        }
    }

    /** A leaf block 3-6 above the ground with a place to stand beside it; sets {@link #leaf} and {@link #stand}. */
    private boolean find() {
        Level level = mob.level();
        BlockPos here = mob.blockPosition();
        RandomSource r = mob.getRandom();
        for (int i = 0; i < 30; i++) {
            BlockPos p = here.offset(r.nextInt(SEARCH * 2 + 1) - SEARCH, 3 + r.nextInt(4), r.nextInt(SEARCH * 2 + 1) - SEARCH);
            if (!level.getBlockState(p).is(BlockTags.LEAVES)) continue;
            // a spot one or two blocks out from the leaf, on the ground under open air, with the leaf's side open
            for (Direction d : Direction.Plane.HORIZONTAL) {
                if (!level.getBlockState(p.relative(d)).isAir()) continue;
                for (int out = 1; out <= 2; out++) {
                    BlockPos col = p.relative(d, out);
                    BlockPos ground = groundBelow(level, col, p.getY() - 2);
                    if (ground == null) continue;
                    int rise = p.getY() - ground.getY();
                    if (rise < 3 || rise > 6) continue;
                    leaf = p;
                    stand = Vec3.atBottomCenterOf(ground);
                    return true;
                }
            }
        }
        return false;
    }

    /** The first standable block top at or below {@code from.y} down to {@code minY}: air above, something to stand on. */
    private static @Nullable BlockPos groundBelow(Level level, BlockPos from, int minY) {
        BlockPos p = from;
        while (p.getY() > minY - 6) {
            if (!level.getBlockState(p).isAir()) return null;
            BlockPos below = p.below();
            if (!level.getBlockState(below).getCollisionShape(level, below).isEmpty())
                return level.getBlockState(p.above()).isAir() && level.getBlockState(p.above(2)).isAir() ? p : null;
            p = below;
        }
        return null;
    }
}
