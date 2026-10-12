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
 * A giraffe: a long-legged body, a neck leaning forward with the head on top, ossicones, a tail. Texture
 * 128x128; the UV layout is mirrored in {@code tools/textures/paint.py}. Baby: the whole mesh at half size.
 */
public class GiraffeModel extends EntityModel<LivingEntityRenderState> {

    private static final float NECK_LEAN = 0.35F;
    /** The back slopes down from the withers: 4 units over the body's 22, matched by 26-unit front legs and 22-unit hind legs. */
    private static final float BODY_SLOPE = 0.18F;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;

    public GiraffeModel(ModelPart root) {
        super(root);
        this.neck = root.getChild("neck");
        this.head = neck.getChild("head");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(mesh(), 128, 128);
    }

    public static LayerDefinition createBabyLayer() {
        return LayerDefinition.create(mesh(), 128, 128).apply(new BabyModelTransform(Set.of()));
    }

    private static MeshDefinition mesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(32, 16).addBox(-5.0F, -11.0F, -7.0F, 10.0F, 22.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, -7.0F, 2.0F, (float) (Math.PI / 2) - BODY_SLOPE, 0.0F, 0.0F));
        PartDefinition neck = root.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, -22.0F, -2.0F, 4.0F, 22.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -10.0F, -6.0F, NECK_LEAN, 0.0F, 0.0F));
        neck.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5F, -3.0F, -7.0F, 5.0F, 5.0F, 7.0F)
                        .texOffs(24, 0).addBox("right_ossicone", -2.0F, -6.0F, -3.0F, 1.0F, 3.0F, 1.0F)
                        .texOffs(24, 0).addBox("left_ossicone", 1.0F, -6.0F, -3.0F, 1.0F, 3.0F, 1.0F)
                        .texOffs(30, 0).addBox("right_ear", -4.5F, -2.0F, -2.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(30, 0).mirror().addBox("left_ear", 2.5F, -2.0F, -2.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -22.0F, 0.0F, -NECK_LEAN, 0.0F, 0.0F));
        CubeListBuilder frontLeg = CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 26.0F, 4.0F);
        CubeListBuilder hindLeg = CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 22.0F, 4.0F);
        root.addOrReplaceChild("right_front_leg", frontLeg, PartPose.offset(-3.0F, -2.0F, -6.0F));
        root.addOrReplaceChild("left_front_leg", frontLeg, PartPose.offset(3.0F, -2.0F, -6.0F));
        root.addOrReplaceChild("right_hind_leg", hindLeg, PartPose.offset(-3.0F, 2.0F, 10.0F));
        root.addOrReplaceChild("left_hind_leg", hindLeg, PartPose.offset(3.0F, 2.0F, 10.0F));
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(72, 16).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 10.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -7.0F, 13.0F, 0.3F, 0.0F, 0.0F));
        return mesh;
    }

    @Override
    public void setupAnim(LivingEntityRenderState state) {
        super.setupAnim(state);
        this.neck.yRot = state.yRot * ((float) Math.PI / 180.0F) * 0.6F;
        this.head.yRot = state.yRot * ((float) Math.PI / 180.0F) * 0.4F;
        this.head.xRot = -NECK_LEAN + state.xRot * ((float) Math.PI / 180.0F);
        float pos = state.walkAnimationPos;
        float speed = state.walkAnimationSpeed;
        this.rightHindLeg.xRot = Mth.cos(pos * 0.6662F) * 1.0F * speed;
        this.leftHindLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * 1.0F * speed;
        this.rightFrontLeg.xRot = Mth.cos(pos * 0.6662F + (float) Math.PI) * 1.0F * speed;
        this.leftFrontLeg.xRot = Mth.cos(pos * 0.6662F) * 1.0F * speed;
        this.tail.yRot = Mth.cos(state.ageInTicks * 0.1F) * 0.2F;
    }
}
