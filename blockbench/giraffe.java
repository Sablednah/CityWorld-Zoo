// Giraffe: the whole mesh (rendered at 1.0).
// Open this file in Blockbench (File > Open Model; it is the "Modded Entity" format, Mojang mappings 1.17+).
// Texture: src/main/resources/assets/cityworld_zoo/textures/entity/giraffe.png, 128x128.
// Copied from the real model class (constants and Math.PI written out as numbers) -- edit in Blockbench, export as Java,
// and port the numbers back into src/main/java/.../client/model/; this file is not compiled or shipped.
package blockbench;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class GiraffeBlockbench {
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(32, 16).addBox(-5.0F, -11.0F, -7.0F, 10.0F, 22.0F, 10.0F),
                PartPose.offsetAndRotation(0.0F, -7.0F, 2.0F, 1.3908F, 0.0F, 0.0F));
        PartDefinition neck = root.addOrReplaceChild("neck",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, -22.0F, -2.0F, 4.0F, 22.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -10.0F, -6.0F, 0.35F, 0.0F, 0.0F));
        neck.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.5F, -3.0F, -7.0F, 5.0F, 5.0F, 7.0F)
                        .texOffs(24, 0).addBox(-2.0F, -6.0F, -3.0F, 1.0F, 3.0F, 1.0F)
                        .texOffs(24, 0).addBox(1.0F, -6.0F, -3.0F, 1.0F, 3.0F, 1.0F)
                        .texOffs(30, 0).addBox(-4.5F, -2.0F, -2.0F, 2.0F, 2.0F, 1.0F)
                        .texOffs(30, 0).addBox(2.5F, -2.0F, -2.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -22.0F, 0.0F, -0.35F, 0.0F, 0.0F));
        CubeListBuilder frontLeg = CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 26.0F, 4.0F);
        CubeListBuilder hindLeg = CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 22.0F, 4.0F);
        root.addOrReplaceChild("right_front_leg", frontLeg, PartPose.offset(-3.0F, -2.0F, -6.0F));
        root.addOrReplaceChild("left_front_leg", frontLeg, PartPose.offset(3.0F, -2.0F, -6.0F));
        root.addOrReplaceChild("right_hind_leg", hindLeg, PartPose.offset(-3.0F, 2.0F, 10.0F));
        root.addOrReplaceChild("left_hind_leg", hindLeg, PartPose.offset(3.0F, 2.0F, 10.0F));
        root.addOrReplaceChild("tail",
                CubeListBuilder.create().texOffs(72, 16).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 10.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, -7.0F, 13.0F, 0.3F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }
}
