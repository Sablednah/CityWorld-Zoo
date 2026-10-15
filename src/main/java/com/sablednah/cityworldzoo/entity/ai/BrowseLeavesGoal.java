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
import org.jetbrains.annotations.Nullable;

/**
 * A giraffe eats from trees: now and then it finds a leaf block three to six blocks above the ground within
 * reach, walks to stand beside it, and feeds -- the leaf position is synced so the client bends the neck and
 * head to put the muzzle in it -- chewing for a while before it wanders off. The leaves are never broken.
 * Only leaves on the canopy's edge count, reached from a spot under open sky one block out, so it never
 * feeds from underneath or stops short.
 */
public class BrowseLeavesGoal extends Goal {

    private static final int SEARCH = 10;
    private final Giraffe mob;
    private @Nullable BlockPos leaf;
    private @Nullable Vec3 stand;
    private int ticks;
    private int eating;
    private int feed;
    private int repaths;

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
        ticks = 0; eating = 0; repaths = 0;
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
            if (flat < 0.9) {
                eating = 1;
                mob.getNavigation().stop();
                mob.setBrowsing(leaf);
            } else if (mob.getNavigation().isDone() && ticks % 20 == 0) {
                if (++repaths > 4) { leaf = null; return; }   // cannot get there: give up rather than feed from afar
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
        for (int i = 0; i < 80; i++) {
            BlockPos p = here.offset(r.nextInt(SEARCH * 2 + 1) - SEARCH, 2 + r.nextInt(6), r.nextInt(SEARCH * 2 + 1) - SEARCH);
            if (!level.getBlockState(p).is(BlockTags.LEAVES)) continue;
            // an edge leaf: open air beside it and beyond; stand one block out, under open sky all the way up
            for (Direction d : Direction.Plane.HORIZONTAL) {
                if (!level.getBlockState(p.relative(d)).isAir() || !level.getBlockState(p.relative(d, 2)).isAir()) continue;
                BlockPos col = p.relative(d);
                BlockPos ground = groundBelow(level, col, p.getY() - 2);
                if (ground == null) continue;
                int rise = p.getY() - ground.getY();
                if (rise < 3 || rise > 6) continue;
                boolean clear = true;
                for (BlockPos q = ground; clear && q.getY() <= p.getY() + 1; q = q.above()) clear = level.getBlockState(q).isAir();
                if (!clear) continue;
                leaf = p;
                stand = Vec3.atBottomCenterOf(ground);
                return true;
            }
        }
        return false;
    }

    /**
     * The standable spot in this column below {@code from.y}: down through air and leaves (a giraffe stands
     * under the canopy it eats from) to the first block with a shape that is not leaves, then the block above
     * it, which must have two more blocks of air over it for the body.
     */
    private static @Nullable BlockPos groundBelow(Level level, BlockPos from, int minY) {
        BlockPos p = from;
        while (p.getY() > minY - 8) {
            var state = level.getBlockState(p);
            if (!state.isAir() && !state.is(BlockTags.LEAVES)) {
                BlockPos stand = p.above();
                return level.getBlockState(stand).isAir() && level.getBlockState(stand.above()).isAir() && level.getBlockState(stand.above(2)).isAir() ? stand : null;
            }
            p = p.below();
        }
        return null;
    }
}
