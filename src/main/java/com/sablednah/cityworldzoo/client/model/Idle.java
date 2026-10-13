package com.sablednah.cityworldzoo.client.model;

import net.minecraft.util.Mth;

/**
 * Idle behaviours without an entity flag: a slow clock on the entity's age opens a window now and then, eased
 * at both ends, and the model blends a pose in while it is open. Multiplied by {@code 1 - Gait.ramp(speed)}
 * the pose fades the moment the animal walks off. Different frequencies and phases keep two animals from
 * doing the same thing at the same time.
 */
public final class Idle {

    /**
     * 0 outside the window, 1 inside, eased over {@code width} of the clock's swing. {@code freq} sets how
     * often (radians per tick: 0.008 is a window every 39 seconds), {@code threshold} how long it stays open
     * (0.3 is about 40% of the time, 0.85 a few seconds).
     */
    public static float window(float age, float freq, float phase, float threshold, float width) {
        return Mth.clamp((Mth.sin(age * freq + phase) - threshold) / width, 0.0F, 1.0F);
    }

    private Idle() {}
}
