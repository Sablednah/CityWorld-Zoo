package com.sablednah.cityworldzoo.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** What the macaque model needs beyond a living entity's state: whether it is climbing. */
public class MacaqueRenderState extends LivingEntityRenderState {
    public boolean climbing;
}
