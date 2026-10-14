package com.sablednah.cityworldzoo.entity;

/**
 * How an animal walks, shared by its entity (the cadence) and its model (the swing), so the two agree.
 *
 * <p>Vanilla advances the walk position by {@code min(4 * blocks moved, 1)} a tick times a position scale,
 * and models swing legs by {@code cos(0.6662 * position)}: one cycle is {@code 2 PI / 0.6662 = 9.43}
 * position units. A foot planted for half the cycle, swinging from {@code +swing} to {@code -swing} about
 * a hip {@code hip} units up, carries the body {@code 2 * hip * sin(swing)} units; the cycle covers
 * {@code 2 * 9.43 / (4 * cadence)} blocks, {@code 9.43 * 4 / cadence} units... which, set equal, gives
 * {@code cadence = 9.43 / (hip * sin(swing))} for a foot that does not slide. So the swing is chosen for
 * looks (and so that front and hind legs never cross: {@code 2 * hip * sin(swing)} under the leg spacing),
 * and the cadence follows: short legs step faster, as vanilla's babies do with their position scale of 3.</p>
 *
 * @param hip   hip height in rendered units, adult (model units times any mesh scaling)
 * @param swing half the leg's arc, radians
 */
public record Stride(float hip, float swing) {

    /** Position units in one leg cycle: {@code 2 PI / 0.6662}. */
    public static final float CYCLE = (float) (2.0 * Math.PI / 0.6662);

    public static final Stride GIRAFFE = new Stride(37.5F, 0.22F);        // 30 * 1.25; legs 20 apart: 2*37.5*sin = 16
    public static final Stride ELEPHANT = new Stride(25.0F, 0.32F);       // 19 * 1.315; legs 19 apart
    public static final Stride DEER = new Stride(9.6F, 0.50F);            // 12 * 0.8; legs 11 apart
    public static final Stride RHESUS_MACAQUE = new Stride(6.0F, 0.45F);  // legs 6 apart
    public static final Stride ZEBRA = new Stride(14.0F, 0.45F);          // legs 17 apart
    public static final Stride LION = new Stride(11.0F, 0.50F);           // legs 15.5 apart
    public static final Stride TIGER = new Stride(16.0F, 0.45F);          // legs 16.5 apart
    public static final Stride BROWN_BEAR = new Stride(12.0F, 0.50F);     // 10 * 1.2; legs 17 apart

    /** The walk position scale that keeps a planted foot still on the ground at this hip and swing. */
    public float cadence() {
        return CYCLE / (hip * (float) Math.sin(swing));
    }
}
