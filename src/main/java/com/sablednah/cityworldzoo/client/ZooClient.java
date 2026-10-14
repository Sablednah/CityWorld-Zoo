package com.sablednah.cityworldzoo.client;

import java.util.function.Supplier;

import com.sablednah.cityworldzoo.ZooEntities;
import com.sablednah.cityworldzoo.client.model.BrownBearModel;
import com.sablednah.cityworldzoo.client.model.DeerModel;
import com.sablednah.cityworldzoo.client.model.ElephantModel;
import com.sablednah.cityworldzoo.client.model.GiraffeModel;
import com.sablednah.cityworldzoo.client.model.LionModel;
import com.sablednah.cityworldzoo.client.model.MacaqueModel;
import com.sablednah.cityworldzoo.client.model.TigerModel;
import com.sablednah.cityworldzoo.client.model.ZebraModel;
import com.sablednah.cityworldzoo.client.renderer.ElephantRenderer;
import com.sablednah.cityworldzoo.client.renderer.GiraffeRenderer;
import com.sablednah.cityworldzoo.client.renderer.MacaqueRenderer;
import com.sablednah.cityworldzoo.client.renderer.ZebraRenderer;
import com.sablednah.cityworldzoo.client.renderer.ZooRenderer;
import com.sablednah.cityworldzoo.entity.ZooAnimal;

import java.util.function.Function;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
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
        event.registerLayerDefinition(ZooModelLayers.LION, LionModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.LION_BABY, LionModel::createBabyLayer);
        event.registerLayerDefinition(ZooModelLayers.TIGER, TigerModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.TIGER_BABY, TigerModel::createBabyLayer);
        event.registerLayerDefinition(ZooModelLayers.ELEPHANT, ElephantModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.ELEPHANT_BABY, ElephantModel::createBabyLayer);
        event.registerLayerDefinition(ZooModelLayers.BROWN_BEAR, BrownBearModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.BROWN_BEAR_BABY, BrownBearModel::createBabyLayer);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ZooEntities.GIRAFFE.entityType(), ctx -> new GiraffeRenderer(ctx,
                new GiraffeModel(ctx.bakeLayer(ZooModelLayers.GIRAFFE)), new GiraffeModel(ctx.bakeLayer(ZooModelLayers.GIRAFFE_BABY)), 1.1F, texture("giraffe")));
        event.registerEntityRenderer(ZooEntities.RHESUS_MACAQUE.entityType(), ctx -> new MacaqueRenderer(ctx,
                new MacaqueModel(ctx.bakeLayer(ZooModelLayers.RHESUS_MACAQUE)), new MacaqueModel(ctx.bakeLayer(ZooModelLayers.RHESUS_MACAQUE_BABY)), 0.35F, texture("rhesus_macaque")));
        plain(event, ZooEntities.DEER, DeerModel::new, ZooModelLayers.DEER, ZooModelLayers.DEER_BABY, 0.6F);
        event.registerEntityRenderer(ZooEntities.ELEPHANT.entityType(), ctx -> new ElephantRenderer(ctx,
                new ElephantModel(ctx.bakeLayer(ZooModelLayers.ELEPHANT)), new ElephantModel(ctx.bakeLayer(ZooModelLayers.ELEPHANT_BABY)), 1.2F, texture("elephant")));
        plain(event, ZooEntities.BROWN_BEAR, BrownBearModel::new, ZooModelLayers.BROWN_BEAR, ZooModelLayers.BROWN_BEAR_BABY, 0.9F);
        event.registerEntityRenderer(ZooEntities.ZEBRA.entityType(), ctx -> new ZebraRenderer(ctx,
                new ZebraModel(ctx.bakeLayer(ZooModelLayers.ZEBRA)), new ZebraModel(ctx.bakeLayer(ZooModelLayers.ZEBRA_BABY)), 0.75F,
                texture("zebra"), texture("zebra_bite")));
        plain(event, ZooEntities.LION, LionModel::new, ZooModelLayers.LION, ZooModelLayers.LION_BABY, 0.8F);
        plain(event, ZooEntities.TIGER, TigerModel::new, ZooModelLayers.TIGER, ZooModelLayers.TIGER_BABY, 0.8F);
    }

    private static Identifier texture(String name) {
        return ZooEntities.id("textures/entity/" + name + ".png");
    }

    private static <T extends ZooAnimal, M extends EntityModel<? super LivingEntityRenderState>> void plain(
            EntityRenderersEvent.RegisterRenderers event, ZooEntities.Species<T> species, Function<ModelPart, M> model,
            ModelLayerLocation adult, ModelLayerLocation baby, float shadow) {
        register(event, species, model, model, adult, baby, shadow, texture(species.id()), texture(species.id()), LivingEntityRenderState::new);
    }

    private static <T extends ZooAnimal, S extends LivingEntityRenderState, M extends EntityModel<? super S>> void register(
            EntityRenderersEvent.RegisterRenderers event, ZooEntities.Species<T> species, Function<ModelPart, ? extends M> adultModel,
            Function<ModelPart, ? extends M> babyModel, ModelLayerLocation adult, ModelLayerLocation baby, float shadow,
            Identifier texture, Identifier babyTexture, Supplier<S> state) {
        event.registerEntityRenderer(species.entityType(), ctx -> new ZooRenderer<T, S, M>(ctx,
                adultModel.apply(ctx.bakeLayer(adult)), babyModel.apply(ctx.bakeLayer(baby)), shadow, texture, babyTexture, state));
    }

    private ZooClient() {}
}
