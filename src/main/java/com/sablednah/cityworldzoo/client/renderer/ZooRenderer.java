package com.sablednah.cityworldzoo.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sablednah.cityworldzoo.entity.ZooAnimal;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * One renderer for every species: a model, a texture, and the model's drawing scale. On 1.21.1 there are no
 * render states and no mesh transformers (both 1.21.2+): the model reads the entity in {@code setupAnim}, the
 * species' mesh scale is applied here (as vanilla's polar bear renderer does), and a baby is the whole mesh at
 * half size, which is what 26.2's {@code BabyModelTransform(Set.of())} drew. The scale goes on before the
 * renderer's {@code -1.501} translate, so the feet stay on the ground.
 */
public class ZooRenderer<T extends ZooAnimal, M extends EntityModel<T>> extends MobRenderer<T, M> {

    private final ResourceLocation texture;
    private final float meshScale;

    public ZooRenderer(EntityRendererProvider.Context context, M model, float shadow, ResourceLocation texture, float meshScale) {
        super(context, model, shadow);
        this.texture = texture;
        this.meshScale = meshScale;
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture;
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTicks) {
        float s = meshScale * (entity.isBaby() ? 0.5F : 1.0F);
        poseStack.scale(s, s, s);
        super.scale(entity, poseStack, partialTicks);
    }
}
