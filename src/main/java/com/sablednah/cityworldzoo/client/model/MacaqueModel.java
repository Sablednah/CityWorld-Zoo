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
 * A rhesus macaque: a small quadruped with a round head, muzzle and a curling tail. Texture 64x32,
 * layout mirrored in {@code tools/textures/paint.py}. Baby: big-headed half size.
 */
public class MacaqueModel extends EntityModel<LivingEntityRenderState> {

    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;
    private final ModelPart tail2;

    public MacaqueModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
        this.tail = root.getChild("tail");
        this.tail2 = tail.getChild("tail2");
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(mesh(), 64, 32);
    }

    public static LayerDefinition createBabyLayer() {
        return LayerDefinition.create(mesh(), 64, 32).apply(new BabyModelTransform(Set.of("head")));
    }

    private static MeshDefinition mesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 8).addBox(-2.5F, -4.5F, -2.5F, 5.0F, 9.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 15.5F, 0.0F, (float) (Math.PI / 2), 0.0F, 0.0F));
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -4.0F, -3.0F, 4.0F, 4.0F, 4.0F)
                        .texOffs(16, 0).addBox("muzzle", -1.0F, -2.0F, -4.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(22, 0).addBox("right_ear", -3.0F, -3.0F, -1.0F, 1.0F, 2.0F, 1.0F)
                        .texOffs(22, 0).addBox("left_ear", 2.0F, -3.0F, -1.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 13.0F, -4.0F));
        CubeListBuilder frontLeg = CubeListBuilder.create().texOffs(20, 8).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F);
        CubeListBuilder hindLeg = CubeListBuilder.create().texOffs(28, 8).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F);
        root.addOrReplaceChild("right_front_leg", frontLeg, PartPose.offset(-2.0F, 18.0F, -3.0F));
        root.addOrReplaceChild("left_front_leg", frontLeg, PartPose.offset(2.0F, 18.0F, -3.0F));
        root.addOrReplaceChild("right_hind_leg", hindLeg, PartPose.offset(-2.0F, 18.0F, 3.0F));
        root.addOrReplaceChild("left_hind_leg", hindLeg, PartPose.offset(2.0F, 18.0F, 3.0F));
        PartDefinition tail = root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(36, 8).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 14.0F, 4.0F, -0.8F, 0.0F, 0.0F));
        tail.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(40, 8).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.9F, 0.0F, 0.0F));
        return mesh;
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.head.yRot = state.yRot * ((float) Math.PI / 180.0F);
        this.head.xRot = state.xRot * ((float) Math.PI / 180.0F);
        float pos = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        this.rightHindLeg.xRot = Mth.cos(pos * 0.6662F) * 1.4F * speed;
        this.leftHindLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * 1.4F * speed;
        this.rightFrontLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * 1.4F * speed;
        this.leftFrontLeg.xRot = Mth.cos(pos * 0.6662F) * 1.4F * speed;
        this.tail.zRot = Mth.cos(state.ageInTicks * 0.2F) * 0.15F;
        this.tail2.xRot = 0.9F + Mth.cos(state.ageInTicks * 0.2F) * 0.2F;
    }
}
