package com.sablednah.cityworldzoo.client.renderer;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.AdultAndBabyModelPair;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

/**
 * One renderer for every species: an adult model, a baby model, a texture, and a baby texture only where the
 * baby mesh lays its boxes out differently (the zebra foal). Everywhere else the baby is the adult mesh
 * through a {@code BabyModelTransform}, so one texture covers both.
 */
public class ZooRenderer<T extends Mob, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends MobRenderer<T, S, M> {

    private final AdultAndBabyModelPair<M> models;
    private final Identifier texture;
    private final Identifier babyTexture;
    private final Supplier<S> states;

    public ZooRenderer(EntityRendererProvider.Context context, M adult, M baby, float shadow, Identifier texture,
            Identifier babyTexture, Supplier<S> states) {
        super(context, adult, shadow);
        this.models = new AdultAndBabyModelPair<>(adult, baby);
        this.texture = texture;
        this.babyTexture = babyTexture;
        this.states = states;
    }

    @Override
    public Identifier getTextureLocation(S state) {
        return state.isBaby ? babyTexture : texture;
    }

    @Override
    public S createRenderState() {
        return states.get();
    }

    @Override
    public void submit(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        this.model = models.getModel(state.isBaby);
        super.submit(state, poseStack, collector, camera);
    }
}
