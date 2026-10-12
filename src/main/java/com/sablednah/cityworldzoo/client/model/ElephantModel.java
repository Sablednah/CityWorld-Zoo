package com.sablednah.cityworldzoo.client.model;

import java.util.Set;

import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

/**
 * An elephant: a barrel body on pillar legs, a big head with ears, tusks and a three-joint trunk that
 * swings as it walks. Texture 128x64, layout mirrored in {@code tools/textures/paint.py}; the whole
 * mesh is scaled 1.4. Baby: the whole mesh at half size (a full-size head would float ahead of a half body:
 * {@code BabyModelTransform} keeps a head part's pivot where it was, and this head's pivot is far out).
 */
public class ElephantModel extends EntityModel<LivingEntityRenderState> {

    private static final float SCALE = 1.4F;
    private final ModelPart head;
    private final ModelPart trunk;
    private final ModelPart trunk2;
    private final ModelPart trunk3;
    private final ModelPart leftEar;
    private final ModelPart rightEar;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;

    public ElephantModel(ModelPart root) {
        super(root);
        this.head = root.getChild("head");
        this.trunk = head.getChild("trunk");
        this.trunk2 = trunk.getChild("trunk2");
        this.trunk3 = trunk2.getChild("trunk3");
        this.leftEar = head.getChild("left_ear");
        this.rightEar = head.getChild("right_ear");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(mesh(), 128, 64).apply(MeshTransformer.scaling(SCALE));
    }

    public static LayerDefinition createBabyLayer() {
        return LayerDefinition.create(mesh(), 128, 64).apply(MeshTransformer.scaling(SCALE)).apply(new BabyModelTransform(Set.of()));
    }

    private static MeshDefinition mesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 20).addBox(-8.0F, -14.0F, -8.0F, 16.0F, 28.0F, 14.0F),
                PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, (float) (Math.PI / 2), 0.0F, 0.0F));
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -6.0F, -10.0F, 10.0F, 10.0F, 10.0F),
                PartPose.offset(0.0F, -2.0F, -14.0F));
        // Ears: pivot at the top-back corner of each side of the head, the panel hanging down and back from it,
        // yawed so the back edge swings outward and rolled so the bottom edge flares out.
        head.addOrReplaceChild("left_ear",
                CubeListBuilder.create().texOffs(40, 0).addBox(0.0F, -1.0F, -1.0F, 1.0F, 10.0F, 8.0F),
                PartPose.offsetAndRotation(5.0F, -4.0F, -7.0F, 0.0F, 0.5F, -0.25F));
        head.addOrReplaceChild("right_ear",
                CubeListBuilder.create().texOffs(58, 0).addBox(-1.0F, -1.0F, -1.0F, 1.0F, 10.0F, 8.0F),
                PartPose.offsetAndRotation(-5.0F, -4.0F, -7.0F, 0.0F, -0.5F, 0.25F));
        PartDefinition trunk = head.addOrReplaceChild("trunk",
                CubeListBuilder.create().texOffs(76, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 2.0F, -8.5F, 0.2F, 0.0F, 0.0F));
        PartDefinition trunk2 = trunk.addOrReplaceChild("trunk2",
                CubeListBuilder.create().texOffs(88, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        trunk2.addOrReplaceChild("trunk3",
                CubeListBuilder.create().texOffs(100, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F),
                PartPose.offset(0.0F, 6.0F, 0.0F));
        head.addOrReplaceChild("left_tusk",
                CubeListBuilder.create().texOffs(76, 10).addBox(-0.5F, -0.5F, -6.0F, 1.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(2.5F, 3.0F, -9.5F, -0.3F, 0.0F, 0.0F));
        head.addOrReplaceChild("right_tusk",
                CubeListBuilder.create().texOffs(90, 10).addBox(-0.5F, -0.5F, -6.0F, 1.0F, 1.0F, 6.0F),
                PartPose.offsetAndRotation(-2.5F, 3.0F, -9.5F, -0.3F, 0.0F, 0.0F));
        CubeListBuilder frontLeg = CubeListBuilder.create().texOffs(60, 20).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F);
        CubeListBuilder hindLeg = CubeListBuilder.create().texOffs(84, 20).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 14.0F, 6.0F);
        root.addOrReplaceChild("right_front_leg", frontLeg, PartPose.offset(-5.0F, 10.0F, -8.0F));
        root.addOrReplaceChild("left_front_leg", frontLeg, PartPose.offset(5.0F, 10.0F, -8.0F));
        root.addOrReplaceChild("right_hind_leg", hindLeg, PartPose.offset(-5.0F, 10.0F, 8.0F));
        root.addOrReplaceChild("left_hind_leg", hindLeg, PartPose.offset(5.0F, 10.0F, 8.0F));
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(60, 40).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 12.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 14.0F, 0.2F, 0.0F, 0.0F));
        return mesh;
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.head.yRot = state.yRot * ((float) Math.PI / 180.0F);
        this.head.xRot = state.xRot * ((float) Math.PI / 180.0F);
        float pos = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        this.rightHindLeg.xRot = Mth.cos(pos * 0.6662F) * 0.8F * speed;
        this.leftHindLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * 0.8F * speed;
        this.rightFrontLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * 0.8F * speed;
        this.leftFrontLeg.xRot = Mth.cos(pos * 0.6662F) * 0.8F * speed;
        float sway = Mth.cos(state.ageInTicks * 0.08F) * 0.1F + Mth.cos(pos * 0.6662F) * 0.3F * speed;
        this.trunk.xRot = 0.2F + sway;
        this.trunk2.xRot = sway * 0.6F;
        this.trunk3.xRot = sway * 0.4F;
        float flap = Mth.cos(state.ageInTicks * 0.12F) * 0.15F;
        this.leftEar.yRot = 0.5F + flap;
        this.rightEar.yRot = -0.5F - flap;
        this.tail.yRot = Mth.cos(state.ageInTicks * 0.15F) * 0.25F;
    }
}
