// Rhesus macaque: the whole mesh.
// Open this file in Blockbench (File > Open Model; it is the "Modded Entity" format, Mojang mappings 1.17+).
// Texture: src/main/resources/assets/cityworld_zoo/textures/entity/rhesus_macaque.png, 64x32.
// Copied from the real model class (constants and Math.PI written out as numbers) -- edit in Blockbench, export as Java,
// and port the numbers back into src/main/java/.../client/model/; this file is not compiled or shipped.
package blockbench;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class RhesusMacaqueBlockbench {
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 8).addBox(-2.5F, -4.5F, -2.5F, 5.0F, 9.0F, 5.0F),
                PartPose.offsetAndRotation(0.0F, 15.5F, 0.0F, 1.5708F, 0.0F, 0.0F));
        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.0F, -4.0F, -3.0F, 4.0F, 4.0F, 4.0F)
                        .texOffs(16, 0).addBox(-1.0F, -2.0F, -4.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(22, 0).addBox(-3.0F, -3.0F, -1.0F, 1.0F, 2.0F, 1.0F)
                        .texOffs(22, 0).addBox(2.0F, -3.0F, -1.0F, 1.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, 13.0F, -4.0F));
        CubeListBuilder frontLeg = CubeListBuilder.create().texOffs(20, 8).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F);
        CubeListBuilder hindLeg = CubeListBuilder.create().texOffs(28, 8).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F);
        root.addOrReplaceChild("right_front_leg", frontLeg, PartPose.offset(-2.0F, 18.0F, -3.0F));
        root.addOrReplaceChild("left_front_leg", frontLeg, PartPose.offset(2.0F, 18.0F, -3.0F));
        root.addOrReplaceChild("right_hind_leg", hindLeg, PartPose.offset(-2.0F, 18.0F, 3.0F));
        root.addOrReplaceChild("left_hind_leg", hindLeg, PartPose.offset(2.0F, 18.0F, 3.0F));
        PartDefinition tail = root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(36, 8).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 14.0F, 4.0F, -0.8F, 0.0F, 0.0F));
        tail.addOrReplaceChild("tail2",
                CubeListBuilder.create().texOffs(40, 8).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.9F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }
}
