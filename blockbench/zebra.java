// Zebra: vanilla's horse mesh (AbstractEquineModel), untouched.
// Open this file in Blockbench (File > Open Model; it is the "Modded Entity" format, Mojang mappings 1.17+).
// Texture: src/main/resources/assets/cityworld_zoo/textures/entity/zebra.png, 64x64.
// Copied from the real model class (constants and Math.PI written out as numbers) -- edit in Blockbench, export as Java,
// and port the numbers back into src/main/java/.../client/model/; this file is not compiled or shipped.
package blockbench;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class ZebraBlockbench {
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 32).addBox(-5.0F, -8.0F, -17.0F, 10.0F, 10.0F, 22.0F),
                PartPose.offset(0.0F, 11.0F, 5.0F));
        PartDefinition headParts = root.addOrReplaceChild("head_parts",
                CubeListBuilder.create().texOffs(0, 35).addBox(-2.05F, -6.0F, -2.0F, 4.0F, 12.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 4.0F, -12.0F, 0.5236F, 0.0F, 0.0F));
        PartDefinition head = headParts.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 13).addBox(-3.0F, -11.0F, -2.0F, 6.0F, 5.0F, 7.0F), PartPose.ZERO);
        headParts.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(56, 36).addBox(-1.0F, -11.0F, 5.01F, 2.0F, 16.0F, 2.0F), PartPose.ZERO);
        headParts.addOrReplaceChild("upper_mouth", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -11.0F, -7.0F, 4.0F, 5.0F, 5.0F), PartPose.ZERO);
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-3.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F), PartPose.offset(4.0F, 14.0F, 7.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.0F, 4.0F, 11.0F, 4.0F), PartPose.offset(-4.0F, 14.0F, 7.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-3.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F), PartPose.offset(4.0F, 14.0F, -10.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0F, -1.01F, -1.9F, 4.0F, 11.0F, 4.0F), PartPose.offset(-4.0F, 14.0F, -10.0F));
        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(42, 36).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 14.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -5.0F, 2.0F, 0.5236F, 0.0F, 0.0F));
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(19, 16).addBox(0.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F), PartPose.ZERO);
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(19, 16).addBox(-2.55F, -13.0F, 4.0F, 2.0F, 3.0F, 1.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }
}
