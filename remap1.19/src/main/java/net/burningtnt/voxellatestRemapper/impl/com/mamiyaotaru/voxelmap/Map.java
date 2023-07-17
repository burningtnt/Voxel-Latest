package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap;

import net.burningtnt.voxellatest.asm.ASMUtil;
import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

public class Map extends AbstractVoxelMapClassMapper {
    @Nonnull
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.Map";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("<init>",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(406,methodNode) + 5;
            ((FieldInsnNode) methodNode.instructions.get(index)).name = "allKeys";
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("calculateCurrentLightAndSkyColor",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(996,methodNode) + 15;
            ((MethodInsnNode) methodNode.instructions.get(index)).desc = "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/registry/RegistryEntry;";
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("getPixelColor",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(1530,methodNode) + 8;
            ((MethodInsnNode) methodNode.instructions.get(index)).desc = "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/registry/RegistryEntry;";
        }
    }
}
