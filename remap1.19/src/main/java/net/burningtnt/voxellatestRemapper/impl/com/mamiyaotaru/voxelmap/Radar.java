package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap;

import net.burningtnt.voxellatest.mappers.ASMUtil;
import net.burningtnt.voxellatest.mappers.AbstractVoxelMapClassMapper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.feature.VillagerResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

@SuppressWarnings("unused")
public class Radar extends AbstractVoxelMapClassMapper {
    @Override
    @NotNull
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.Radar";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("getHatType",classNode);
//            int index = ASMUtil.getInsnIndexByLineNumber(1186,methodNode) + 2;
//            methodNode.instructions.remove(methodNode.instructions.get(index));
//            ((MethodInsnNode) methodNode.instructions.get(index)).owner = "net/minecraft/resource/Resource";
//            ((MethodInsnNode) methodNode.instructions.get(index)).name = "getMetadata";
//            ((MethodInsnNode) methodNode.instructions.get(index)).desc = "()Lnet/minecraft/resource/metadata/ResourceMetadata;";

            methodNode.instructions.clear();
            methodNode.tryCatchBlocks.clear();
            methodNode.instructions.add(new FieldInsnNode(
                    Opcodes.GETSTATIC,
                    "net/minecraft/client/render/entity/feature/VillagerResourceMetadata$HatType",
                    "NONE",
                    "Lnet/minecraft/client/render/entity/feature/VillagerResourceMetadata$HatType;"
            ));
            methodNode.instructions.add(new InsnNode(
                    Opcodes.ARETURN
            ));
        }
    }
}
