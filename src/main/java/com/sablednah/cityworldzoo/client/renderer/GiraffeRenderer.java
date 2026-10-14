package com.sablednah.cityworldzoo.client.renderer;

import com.sablednah.cityworldzoo.client.model.GiraffeModel;
import com.sablednah.cityworldzoo.entity.Giraffe;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** The shared renderer, plus where the giraffe is feeding, for the neck and head to reach. */
public class GiraffeRenderer extends ZooRenderer<Giraffe, GiraffeRenderState, GiraffeModel> {

    public GiraffeRenderer(EntityRendererProvider.Context context, GiraffeModel adult, GiraffeModel baby, float shadow, Identifier texture) {
        super(context, adult, baby, shadow, texture, texture, GiraffeRenderState::new);
    }

    @Override
    public void extractRenderState(Giraffe entity, GiraffeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.browse = entity.browsing().map(pos -> Vec3.atCenterOf(pos).subtract(entity.position())).orElse(null);
    }
}
