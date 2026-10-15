package com.sablednah.cityworldzoo.client;

import com.sablednah.cityworldzoo.ZooEntities;

import net.minecraft.client.model.geom.ModelLayerLocation;

/** One layer per species: on 1.21.1 the baby is the renderer's half scale of the same mesh, not a layer of its own. */
public final class ZooModelLayers {
    public static final ModelLayerLocation GIRAFFE = adult("giraffe");
    public static final ModelLayerLocation RHESUS_MACAQUE = adult("rhesus_macaque");
    public static final ModelLayerLocation ZEBRA = adult("zebra");
    public static final ModelLayerLocation DEER = adult("deer");
    public static final ModelLayerLocation LION = adult("lion");
    public static final ModelLayerLocation TIGER = adult("tiger");
    public static final ModelLayerLocation ELEPHANT = adult("elephant");
    public static final ModelLayerLocation BROWN_BEAR = adult("brown_bear");

    private static ModelLayerLocation adult(String id) {
        return new ModelLayerLocation(ZooEntities.id(id), "main");
    }

    private ZooModelLayers() {}
}
