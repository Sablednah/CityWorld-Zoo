package com.sablednah.cityworldzoo.client;

import com.sablednah.cityworldzoo.ZooEntities;
import com.sablednah.cityworldzoo.client.model.BrownBearModel;
import com.sablednah.cityworldzoo.client.model.DeerModel;
import com.sablednah.cityworldzoo.client.model.ElephantModel;
import com.sablednah.cityworldzoo.client.model.GiraffeModel;
import com.sablednah.cityworldzoo.client.model.LionModel;
import com.sablednah.cityworldzoo.client.model.MacaqueModel;
import com.sablednah.cityworldzoo.client.model.TigerModel;
import com.sablednah.cityworldzoo.client.model.ZebraModel;
import com.sablednah.cityworldzoo.client.renderer.ZebraRenderer;
import com.sablednah.cityworldzoo.client.renderer.ZooRenderer;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * The client half: a model layer and a renderer per species. Loaded only on a client ({@code ZooMod} checks
 * the dist before naming this class).
 */
public final class ZooClient {

    public static void register(IEventBus modBus) {
        modBus.addListener(ZooClient::layers);
        modBus.addListener(ZooClient::renderers);
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ZooModelLayers.GIRAFFE, GiraffeModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.RHESUS_MACAQUE, MacaqueModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.ZEBRA, ZebraModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.DEER, DeerModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.LION, LionModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.TIGER, TigerModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.ELEPHANT, ElephantModel::createBodyLayer);
        event.registerLayerDefinition(ZooModelLayers.BROWN_BEAR, BrownBearModel::createBodyLayer);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ZooEntities.GIRAFFE.entityType(), ctx -> new ZooRenderer<>(ctx,
                new GiraffeModel(ctx.bakeLayer(ZooModelLayers.GIRAFFE)), 1.1F, texture("giraffe"), GiraffeModel.SCALE));
        event.registerEntityRenderer(ZooEntities.RHESUS_MACAQUE.entityType(), ctx -> new ZooRenderer<>(ctx,
                new MacaqueModel(ctx.bakeLayer(ZooModelLayers.RHESUS_MACAQUE)), 0.35F, texture("rhesus_macaque"), MacaqueModel.SCALE));
        event.registerEntityRenderer(ZooEntities.DEER.entityType(), ctx -> new ZooRenderer<>(ctx,
                new DeerModel(ctx.bakeLayer(ZooModelLayers.DEER)), 0.6F, texture("deer"), DeerModel.SCALE));
        event.registerEntityRenderer(ZooEntities.ELEPHANT.entityType(), ctx -> new ZooRenderer<>(ctx,
                new ElephantModel(ctx.bakeLayer(ZooModelLayers.ELEPHANT)), 1.2F, texture("elephant"), ElephantModel.SCALE));
        event.registerEntityRenderer(ZooEntities.BROWN_BEAR.entityType(), ctx -> new ZooRenderer<>(ctx,
                new BrownBearModel(ctx.bakeLayer(ZooModelLayers.BROWN_BEAR)), 0.9F, texture("brown_bear"), BrownBearModel.SCALE));
        event.registerEntityRenderer(ZooEntities.ZEBRA.entityType(), ctx -> new ZebraRenderer(ctx,
                new ZebraModel(ctx.bakeLayer(ZooModelLayers.ZEBRA)), 0.75F, texture("zebra"), texture("zebra_bite")));
        event.registerEntityRenderer(ZooEntities.LION.entityType(), ctx -> new ZooRenderer<>(ctx,
                new LionModel(ctx.bakeLayer(ZooModelLayers.LION)), 0.8F, texture("lion"), LionModel.SCALE));
        event.registerEntityRenderer(ZooEntities.TIGER.entityType(), ctx -> new ZooRenderer<>(ctx,
                new TigerModel(ctx.bakeLayer(ZooModelLayers.TIGER)), 0.8F, texture("tiger"), TigerModel.SCALE));
    }

    private static ResourceLocation texture(String name) {
        return ZooEntities.id("textures/entity/" + name + ".png");
    }

    private ZooClient() {}
}
