package com.sablednah.cityworldzoo.client;

import com.sablednah.cityworldzoo.ZooEntities;

import net.minecraft.client.model.geom.ModelLayerLocation;

/** One adult and one baby layer per species. */
public final class ZooModelLayers {
    public static final ModelLayerLocation GIRAFFE = adult("giraffe");
    public static final ModelLayerLocation GIRAFFE_BABY = baby("giraffe");
    public static final ModelLayerLocation RHESUS_MACAQUE = adult("rhesus_macaque");
    public static final ModelLayerLocation RHESUS_MACAQUE_BABY = baby("rhesus_macaque");
    public static final ModelLayerLocation ZEBRA = adult("zebra");
    public static final ModelLayerLocation ZEBRA_BABY = baby("zebra");
    public static final ModelLayerLocation DEER = adult("deer");
    public static final ModelLayerLocation DEER_BABY = baby("deer");
    public static final ModelLayerLocation LION = adult("lion");
    public static final ModelLayerLocation LION_BABY = baby("lion");
    public static final ModelLayerLocation TIGER = adult("tiger");
    public static final ModelLayerLocation TIGER_BABY = baby("tiger");
    public static final ModelLayerLocation ELEPHANT = adult("elephant");
    public static final ModelLayerLocation ELEPHANT_BABY = baby("elephant");
    public static final ModelLayerLocation BROWN_BEAR = adult("brown_bear");
    public static final ModelLayerLocation BROWN_BEAR_BABY = baby("brown_bear");

    private static ModelLayerLocation adult(String id) {
        return new ModelLayerLocation(ZooEntities.id(id), "main");
    }

    private static ModelLayerLocation baby(String id) {
        return new ModelLayerLocation(ZooEntities.id(id), "baby");
    }

    private ZooModelLayers() {}
}
