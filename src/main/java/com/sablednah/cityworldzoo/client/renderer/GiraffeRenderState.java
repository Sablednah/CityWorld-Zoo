package com.sablednah.cityworldzoo.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/** What the giraffe model needs beyond a living entity's state: the leaf it is feeding from, relative to its feet. */
public class GiraffeRenderState extends LivingEntityRenderState {
    public @Nullable Vec3 browse;
}
