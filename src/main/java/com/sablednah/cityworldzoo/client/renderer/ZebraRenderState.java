package com.sablednah.cityworldzoo.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/** What the zebra renderer needs beyond a living entity's state: whether this is the one-in-a-hundred bitten skin. */
public class ZebraRenderState extends LivingEntityRenderState {
    public boolean bitten;
}
