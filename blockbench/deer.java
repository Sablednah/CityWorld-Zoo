// Deer: the whole mesh; the game renders it at 0.8 (MeshTransformer.scaling), which Blockbench does not show.
// Open this file in Blockbench (File > Open Model; it is the "Modded Entity" format, Mojang mappings 1.17+).
// Texture: src/main/resources/assets/cityworld_zoo/textures/entity/deer.png, 64x64.
// Copied from the real model class (constants and Math.PI written out as numbers) -- edit in Blockbench, export as Java,
// and port the numbers back into src/main/java/.../client/model/; this file is not compiled or shipped.
package blockbench;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class DeerBlockbench {
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(20, 12).addBox(-3.0F, -8.0F, -4.0F, 6.0F, 16.0F, 7.0F),
                PartPose.offsetAndRotation(0.0F, 8.0F, 1.0F, 1.5708F, 0.0F, 0.0F));
        PartDefinition neck = root.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(0, 12).addBox(-1.5F, -8.0F, -2.5F, 3.0F, 8.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, -6.0F, 0.5F, 0.0F, 0.0F));
        neck.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -2.0F, -6.0F, 4.0F, 4.0F, 6.0F)
                        .texOffs(20, 0).addBox(-4.0F, -4.0F, -2.0F, 2.0F, 3.0F, 1.0F)
                        .texOffs(26, 0).addBox(2.0F, -4.0F, -2.0F, 2.0F, 3.0F, 1.0F)
                        .texOffs(32, 0).addBox(-2.0F, -8.0F, -2.0F, 1.0F, 5.0F, 1.0F)
                        .texOffs(32, 0).addBox(1.0F, -8.0F, -2.0F, 1.0F, 5.0F, 1.0F)
                        .texOffs(36, 0).addBox(-3.0F, -7.0F, -2.0F, 1.0F, 3.0F, 1.0F)
                        .texOffs(36, 0).addBox(2.0F, -7.0F, -2.0F, 1.0F, 3.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, -0.5F, 0.0F, 0.0F));
        CubeListBuilder frontLeg = CubeListBuilder.create().texOffs(0, 26).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F);
        CubeListBuilder hindLeg = CubeListBuilder.create().texOffs(0, 42).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F);
        root.addOrReplaceChild("right_front_leg", frontLeg, PartPose.offset(-2.0F, 12.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", frontLeg, PartPose.offset(2.0F, 12.0F, -5.0F));
        root.addOrReplaceChild("right_hind_leg", hindLeg, PartPose.offset(-2.0F, 12.0F, 6.0F));
        root.addOrReplaceChild("left_hind_leg", hindLeg, PartPose.offset(2.0F, 12.0F, 6.0F));
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(52, 12).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 4.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 6.0F, 9.0F, 0.4F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 64);
    }
}
