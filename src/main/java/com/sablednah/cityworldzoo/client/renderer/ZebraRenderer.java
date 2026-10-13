package com.sablednah.cityworldzoo.client.renderer;

import com.sablednah.cityworldzoo.client.model.ZebraModel;
import com.sablednah.cityworldzoo.entity.Zebra;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

/** The shared renderer, plus the skin choice: one zebra in a hundred wears Sable's bitten texture (a Madagascar nod). */
public class ZebraRenderer extends ZooRenderer<Zebra, ZebraRenderState, ZebraModel> {

    private final Identifier bitten;

    public ZebraRenderer(EntityRendererProvider.Context context, ZebraModel adult, ZebraModel baby, float shadow, Identifier texture, Identifier bitten) {
        super(context, adult, baby, shadow, texture, texture, ZebraRenderState::new);
        this.bitten = bitten;
    }

    @Override
    public Identifier getTextureLocation(ZebraRenderState state) {
        return state.bitten ? bitten : super.getTextureLocation(state);
    }

    @Override
    public void extractRenderState(Zebra entity, ZebraRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.bitten = entity.isBitten();
    }
}
