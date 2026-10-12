package com.sablednah.cityworldzoo.client;

import java.util.function.Supplier;

import com.sablednah.cityworldzoo.ZooEntities;
import com.sablednah.cityworldzoo.client.model.BigCatModel;
import com.sablednah.cityworldzoo.client.model.BrownBearModel;
import com.sablednah.cityworldzoo.client.model.DeerModel;
import com.sablednah.cityworldzoo.client.model.ElephantModel;
import com.sablednah.cityworldzoo.client.model.GiraffeModel;
import com.sablednah.cityworldzoo.client.model.MacaqueModel;
import com.sablednah.cityworldzoo.client.model.ZebraModel;
import com.sablednah.cityworldzoo.client.renderer.ZooRenderer;
import com.sablednah.cityworldzoo.entity.ZooAnimal;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.entity.state.FelineRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * The client half: a model layer pair and a renderer per species. Loaded only on a client
 * ({@code ZooMod} checks the dist before naming this class).
 */
public final class ZooClient {

    public static void register(IEventBus modBus) {
        modBus.addListener(ZooClient::layers);
        modBus.addListener(ZooClient::renderers);
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ZooModelLayers.GIRAFFE, GiraffeModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.GIRAFFE_BABY, GiraffeModel::createBabyLayer);
        event.registerLayerDefinition(ZooModelLayers.RHESUS_MACAQUE, MacaqueModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.RHESUS_MACAQUE_BABY, MacaqueModel::createBabyLayer);
        event.registerLayerDefinition(ZooModelLayers.ZEBRA, ZebraModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.ZEBRA_BABY, ZebraModel::createBabyLayer);
        event.registerLayerDefinition(ZooModelLayers.DEER, DeerModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.DEER_BABY, DeerModel::createBabyLayer);
        event.registerLayerDefinition(ZooModelLayers.LION, () -> BigCatModel.createBodyLayer(true));
        event.registerLayerDefinition(ZooModelLayers.LION_BABY, () -> BigCatModel.createBabyLayer(true));
        event.registerLayerDefinition(ZooModelLayers.TIGER, () -> BigCatModel.createBodyLayer(false));
        event.registerLayerDefinition(ZooModelLayers.TIGER_BABY, () -> BigCatModel.createBabyLayer(false));
        event.registerLayerDefinition(ZooModelLayers.ELEPHANT, ElephantModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.ELEPHANT_BABY, ElephantModel::createBabyLayer);
        event.registerLayerDefinition(ZooModelLayers.BROWN_BEAR, BrownBearModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.BROWN_BEAR_BABY, BrownBearModel::createBabyLayer);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        plain(event, ZooEntities.GIRAFFE, GiraffeModel::new, ZooModelLayers.GIRAFFE, ZooModelLayers.GIRAFFE_BABY, 1.0F);
        plain(event, ZooEntities.RHESUS_MACAQUE, MacaqueModel::new, ZooModelLayers.RHESUS_MACAQUE, ZooModelLayers.RHESUS_MACAQUE_BABY, 0.35F);
        plain(event, ZooEntities.DEER, DeerModel::new, ZooModelLayers.DEER, ZooModelLayers.DEER_BABY, 0.6F);
        plain(event, ZooEntities.ELEPHANT, ElephantModel::new, ZooModelLayers.ELEPHANT, ZooModelLayers.ELEPHANT_BABY, 1.2F);
        plain(event, ZooEntities.BROWN_BEAR, BrownBearModel::new, ZooModelLayers.BROWN_BEAR, ZooModelLayers.BROWN_BEAR_BABY, 0.9F);
        register(event, ZooEntities.ZEBRA, ZebraModel::new, ZooModelLayers.ZEBRA, ZooModelLayers.ZEBRA_BABY, 0.75F, EquineRenderState::new);
        register(event, ZooEntities.LION, BigCatModel::new, ZooModelLayers.LION, ZooModelLayers.LION_BABY, 0.8F, FelineRenderState::new);
        register(event, ZooEntities.TIGER, BigCatModel::new, ZooModelLayers.TIGER, ZooModelLayers.TIGER_BABY, 0.8F, FelineRenderState::new);
    }

    private static <T extends ZooAnimal, M extends EntityModel<? super LivingEntityRenderState>> void plain(
            EntityRenderersEvent.RegisterRenderers event, ZooEntities.Species<T> species, java.util.function.Function<ModelPart, M> model,
            ModelLayerLocation adult, ModelLayerLocation baby, float shadow) {
        register(event, species, model, adult, baby, shadow, LivingEntityRenderState::new);
    }

    private static <T extends ZooAnimal, S extends LivingEntityRenderState, M extends EntityModel<? super S>> void register(
            EntityRenderersEvent.RegisterRenderers event, ZooEntities.Species<T> species, java.util.function.Function<ModelPart, M> model,
            ModelLayerLocation adult, ModelLayerLocation baby, float shadow, Supplier<S> state) {
        Identifier texture = Identifier.fromNamespaceAndPath(ZooEntities.id(species.id()).getNamespace(), "textures/entity/" + species.id() + ".png");
        event.registerEntityRenderer(species.entityType(), ctx -> new ZooRenderer<T, S, M>(ctx,
                model.apply(ctx.bakeLayer(adult)), model.apply(ctx.bakeLayer(baby)), shadow, texture, state));
    }

    private ZooClient() {}
}
