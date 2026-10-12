// Brown bear: vanilla's polar bear mesh; the game renders it at 1.2.
// Open this file in Blockbench (File > Open Model; it is the "Modded Entity" format, Mojang mappings 1.17+).
// Texture: src/main/resources/assets/cityworld_zoo/textures/entity/brown_bear.png, 128x64.
// Copied from the real model class (constants and Math.PI written out as numbers) -- edit in Blockbench, export as Java,
// and port the numbers back into src/main/java/.../client/model/; this file is not compiled or shipped.
package blockbench;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class BrownBearBlockbench {
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.5F, -3.0F, -3.0F, 7.0F, 7.0F, 7.0F)
                        .texOffs(0, 44).addBox(-2.5F, 1.0F, -6.0F, 5.0F, 3.0F, 3.0F)
                        .texOffs(26, 0).addBox(-4.5F, -4.0F, -1.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(26, 0).addBox(2.5F, -4.0F, -1.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 10.0F, -16.0F));
        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 19).addBox(-5.0F, -13.0F, -7.0F, 14.0F, 14.0F, 11.0F)
                        .texOffs(39, 0).addBox(-4.0F, -25.0F, -7.0F, 12.0F, 12.0F, 10.0F),
                PartPose.offsetAndRotation(-2.0F, 9.0F, 12.0F, 1.5708F, 0.0F, 0.0F));
        CubeListBuilder hindLeg = CubeListBuilder.create().texOffs(50, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 8.0F);
        root.addOrReplaceChild("right_hind_leg", hindLeg, PartPose.offset(-4.5F, 14.0F, 6.0F));
        root.addOrReplaceChild("left_hind_leg", hindLeg, PartPose.offset(4.5F, 14.0F, 6.0F));
        CubeListBuilder frontLeg = CubeListBuilder.create().texOffs(50, 40).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 6.0F);
        root.addOrReplaceChild("right_front_leg", frontLeg, PartPose.offset(-3.5F, 14.0F, -8.0F));
        root.addOrReplaceChild("left_front_leg", frontLeg, PartPose.offset(3.5F, 14.0F, -8.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }
}
