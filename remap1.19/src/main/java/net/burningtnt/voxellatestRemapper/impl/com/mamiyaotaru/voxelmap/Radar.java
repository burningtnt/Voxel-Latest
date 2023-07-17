package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap;

import net.burningtnt.voxellatest.asm.ASMUtil;
import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
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
