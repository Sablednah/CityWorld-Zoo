package com.sablednah.cityworldzoo.client.model;

import net.minecraft.client.model.animal.equine.AbstractEquineModel;
import net.minecraft.client.model.animal.equine.BabyHorseModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.EquineRenderState;

/**
 * A zebra is vanilla's horse mesh (texture 64x64, the horse layout) in stripes; the animation is the horse's.
 * The foal is vanilla's {@link BabyHorseModel} mesh, which lays its boxes out differently, so it has its own
 * texture ({@code zebra_baby.png}) exactly as a vanilla foal does.
 */
public class ZebraModel extends AbstractEquineModel<EquineRenderState> {

    public ZebraModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(createBodyMesh(CubeDeformation.NONE), 64, 64);
    }

    public static LayerDefinition createBabyLayer() {
        return LayerDefinition.create(BabyHorseModel.createBabyMesh(CubeDeformation.NONE), 64, 64);
    }
}
