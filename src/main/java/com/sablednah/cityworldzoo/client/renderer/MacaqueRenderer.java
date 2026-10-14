package com.sablednah.cityworldzoo.client.renderer;

import com.sablednah.cityworldzoo.client.model.MacaqueModel;
import com.sablednah.cityworldzoo.entity.RhesusMacaque;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** The shared renderer, plus the climbing flag for the model's trunk-hugging pose. */
public class MacaqueRenderer extends ZooRenderer<RhesusMacaque, MacaqueRenderState, MacaqueModel> {

    public MacaqueRenderer(EntityRendererProvider.Context context, MacaqueModel adult, MacaqueModel baby, float shadow, Identifier texture) {
        super(context, adult, baby, shadow, texture, texture, MacaqueRenderState::new);
    }

    @Override
    public void extractRenderState(RhesusMacaque entity, MacaqueRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.climbing = entity.isClimbing();
    }
}
