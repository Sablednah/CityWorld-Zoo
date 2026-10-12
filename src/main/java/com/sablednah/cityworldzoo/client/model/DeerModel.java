package com.sablednah.cityworldzoo.client.model;

import java.util.Set;

import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/**
 * A deer: a slim body on thin legs, a raised neck, antlers. Texture 64x64, layout mirrored in
 * {@code tools/textures/paint.py}. Baby: the whole mesh at half size (a fawn keeps its antlers for now).
 */
public class DeerModel extends EntityModel<LivingEntityRenderState> {

    private static final float NECK_LEAN = 0.5F;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public DeerModel(ModelPart root) {
        super(root);
        this.neck = root.getChild("neck");
        this.head = neck.getChild("head");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(mesh(), 64, 64);
    }

    public static LayerDefinition createBabyLayer() {
        return LayerDefinition.create(mesh(), 64, 64).apply(new BabyModelTransform(Set.of()));
    }

    private static MeshDefinition mesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(20, 12).addBox(-4.0F, -9.0F, -5.0F, 8.0F, 18.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 7.0F, 1.0F, (float) (Math.PI / 2), 0.0F, 0.0F));
        PartDefinition neck = root.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(0, 12).addBox(-2.0F, -8.0F, -3.0F, 4.0F, 8.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 5.0F, -6.0F, NECK_LEAN, 0.0F, 0.0F));
        neck.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -3.0F, -6.0F, 4.0F, 5.0F, 6.0F)
                        .texOffs(20, 0).addBox("right_ear", -4.0F, -4.0F, -2.0F, 2.0F, 3.0F, 1.0F)
                        .texOffs(26, 0).addBox("left_ear", 2.0F, -4.0F, -2.0F, 2.0F, 3.0F, 1.0F)
                        .texOffs(32, 0).addBox("right_antler", -2.0F, -8.0F, -2.0F, 1.0F, 5.0F, 1.0F)
                        .texOffs(32, 0).addBox("left_antler", 1.0F, -8.0F, -2.0F, 1.0F, 5.0F, 1.0F)
                        .texOffs(36, 0).addBox("right_tine", -3.0F, -7.0F, -2.0F, 1.0F, 3.0F, 1.0F)
                        .texOffs(36, 0).addBox("left_tine", 2.0F, -7.0F, -2.0F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, -NECK_LEAN, 0.0F, 0.0F));
        CubeListBuilder frontLeg = CubeListBuilder.create().texOffs(0, 26).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F);
        CubeListBuilder hindLeg = CubeListBuilder.create().texOffs(0, 42).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 12.0F, 3.0F);
        root.addOrReplaceChild("right_front_leg", frontLeg, PartPose.offset(-3.0F, 12.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", frontLeg, PartPose.offset(3.0F, 12.0F, -5.0F));
        root.addOrReplaceChild("right_hind_leg", hindLeg, PartPose.offset(-3.0F, 12.0F, 6.0F));
        root.addOrReplaceChild("left_hind_leg", hindLeg, PartPose.offset(3.0F, 12.0F, 6.0F));
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(52, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 5.0F, 10.0F, 0.4F, 0.0F, 0.0F));
        return mesh;
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.neck.yRot = state.yRot * ((float) Math.PI / 180.0F) * 0.5F;
        this.head.yRot = state.yRot * ((float) Math.PI / 180.0F) * 0.5F;
        this.head.xRot = -NECK_LEAN + state.xRot * ((float) Math.PI / 180.0F);
        float pos = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        this.rightHindLeg.xRot = Mth.cos(pos * 0.6662F) * 1.4F * speed;
        this.leftHindLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * 1.4F * speed;
        this.rightFrontLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * 1.4F * speed;
        this.leftFrontLeg.xRot = Mth.cos(pos * 0.6662F) * 1.4F * speed;
    }
}
