package com.sablednah.cityworldzoo.client.renderer;

import com.sablednah.cityworldzoo.client.model.ElephantModel;
import com.sablednah.cityworldzoo.entity.Elephant;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** The shared renderer, plus the one fact the model needs: a bull shows tusks, a cow and a calf do not. */
public class ElephantRenderer extends ZooRenderer<Elephant, ElephantRenderState, ElephantModel> {

    public ElephantRenderer(EntityRendererProvider.Context context, ElephantModel adult, ElephantModel baby, float shadow, Identifier texture) {
        super(context, adult, baby, shadow, texture, texture, ElephantRenderState::new);
    }

    @Override
    public void extractRenderState(Elephant entity, ElephantRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.tusked = entity.isTusked() && !entity.isBaby();
    }
}
