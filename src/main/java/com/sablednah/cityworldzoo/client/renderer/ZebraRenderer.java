package com.sablednah.cityworldzoo.client.renderer;

import com.sablednah.cityworldzoo.client.model.ZebraModel;
import com.sablednah.cityworldzoo.entity.Zebra;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** The shared renderer, plus the skin choice: one zebra in a hundred wears Sable's bitten texture (a Madagascar nod). */
public class ZebraRenderer extends ZooRenderer<Zebra, ZebraModel> {

    private final ResourceLocation bitten;

    public ZebraRenderer(EntityRendererProvider.Context context, ZebraModel model, float shadow, ResourceLocation texture, ResourceLocation bitten) {
        super(context, model, shadow, texture, ZebraModel.SCALE);
        this.bitten = bitten;
    }

    @Override
    public ResourceLocation getTextureLocation(Zebra entity) {
        return entity.isBitten() ? bitten : super.getTextureLocation(entity);
    }
}
