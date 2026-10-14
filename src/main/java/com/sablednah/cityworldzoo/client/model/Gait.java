package com.sablednah.cityworldzoo.client.model;

import net.minecraft.util.Mth;

/**
 * Matching a leg swing to the ground going by, from vanilla's constants rather than by eye.
 *
 * <p>{@code LivingEntity.updateWalkAnimation} advances the walk position by {@code min(4 * blocks moved, 1)}
 * a tick, and models drive legs with {@code cos(0.6662 * position)}: one full cycle is {@code 2 * PI / 0.6662 =
 * 9.43} position units, which is {@code 9.43 / 4 = 2.36} blocks of travel whatever the speed (every animal
 * here is far under the cap). A planted foot is on the ground for half the cycle, {@code 1.18} blocks, and a
 * foot swinging from {@code +A} to {@code -A} about a hip {@code L} units above the ground sweeps
 * {@code 2 L sin A / 16} blocks. No slip means {@code sin A = 9.43 / L}. Scaling that angle by the entity's
 * walk speed, as vanilla's quadrupeds do, is what made the giraffe and elephant glide: at a stroll the
 * speed is 0.2-0.4, so the feet swept a fraction of the ground going by.</p>
 *
 * <p>The first version of {@link #swing} forgot the 2 in {@code 2 L sin A} and doubled every angle (anything
 * under 19 units of leg pegged at 90 degrees: the "almost 180 arc" Sable saw on 2026-10-14). The angle is
 * now chosen per species in {@code entity/Stride}, for looks and so legs never cross, and the entity's
 * walk cadence is set from it instead; {@link #swing} is the no-slip angle for reference.</p>
 */
public final class Gait {

    /** Position units in one leg cycle: {@code 2 PI / 0.6662}. */
    public static final float CYCLE = (float) (2.0 * Math.PI / 0.6662);
    /** Ground a planted foot must sweep per cycle, in model units: half the cycle's travel, times 16. */
    public static final float HALF_STRIDE_UNITS = CYCLE / 4.0F * 16.0F / 2.0F;

    /** The no-slip swing amplitude (radians) for a hip {@code legUnits} up at vanilla's cadence: {@code asin(9.43 / L)}. */
    public static float swing(float legUnits) {
        return (float) Math.asin(Math.min(1.0F, HALF_STRIDE_UNITS / (2.0F * Math.max(1.0F, legUnits))));
    }

    /** 1 while walking, falling to 0 as the walk speed decays after stopping, so legs settle to neutral. */
    public static float ramp(float walkAnimationSpeed) {
        return Mth.clamp(walkAnimationSpeed * 6.0F, 0.0F, 1.0F);
    }

    private Gait() {}
}
