package com.sablednah.cityworldzoo.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** What the elephant model needs beyond a living entity's state: whether to draw tusks. */
public class ElephantRenderState extends LivingEntityRenderState {
    public boolean tusked;
}
