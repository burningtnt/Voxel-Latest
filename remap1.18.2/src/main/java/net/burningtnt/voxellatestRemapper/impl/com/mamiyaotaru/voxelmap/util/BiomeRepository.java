package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.util;

import net.burningtnt.voxellatest.asm.ASMUtil;
import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class BiomeRepository extends AbstractVoxelMapClassMapper {
    @Nonnull
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.util.BiomeRepository";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("getBiomes",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(36,methodNode) + 2;
            ((FieldInsnNode) methodNode.instructions.get(index)).name = "SWAMP";
            ((FieldInsnNode) methodNode.instructions.get(index)).desc = "Lnet/minecraft/util/registry/RegistryKey;";
        }
    }
}
