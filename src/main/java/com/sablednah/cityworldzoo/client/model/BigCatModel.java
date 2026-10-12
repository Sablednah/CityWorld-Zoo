package com.sablednah.cityworldzoo.client.model;

import java.util.Set;

import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.animal.feline.AdultFelineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.renderer.entity.state.FelineRenderState;

/**
 * Lion and tiger: vanilla's adult feline mesh (the ocelot's, texture 64x32) at twice the size, with the
 * feline walk, and for the lion a mane box round the head at texture (40, 12).
 */
public class BigCatModel extends AdultFelineModel<FelineRenderState> {

    private static final float SCALE = 2.0F;

    public BigCatModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer(boolean mane) {
        return LayerDefinition.create(mesh(mane), 64, 32).apply(MeshTransformer.scaling(SCALE));
    }

    public static LayerDefinition createBabyLayer(boolean mane) {
        // A cub has no mane whatever its father looks like. Uniform half size: this head's pivot is 18 units
        // out, so a kept-full-size head would sit clear of the body.
        return LayerDefinition.create(mesh(false), 64, 32).apply(MeshTransformer.scaling(SCALE)).apply(new BabyModelTransform(Set.of()));
    }

    private static MeshDefinition mesh(boolean mane) {
        MeshDefinition mesh = createBodyMesh(CubeDeformation.NONE);
        if (mane) {
            mesh.getRoot().getChild("head").addOrReplaceChild("mane",
                    CubeListBuilder.create().texOffs(40, 12).addBox(-4.5F, -3.0F, -1.0F, 9.0F, 8.0F, 3.0F),
                    PartPose.ZERO);
        }
        return mesh;
    }
}
