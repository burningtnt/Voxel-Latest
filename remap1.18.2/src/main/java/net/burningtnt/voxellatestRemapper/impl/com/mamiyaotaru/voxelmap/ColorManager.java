package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap;

import net.burningtnt.voxellatest.mappers.ASMUtil;
import net.burningtnt.voxellatest.mappers.AbstractVoxelMapClassMapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

@SuppressWarnings("unused")
public class ColorManager extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.ColorManager";
    }

    @Override
    public void remap(ClassNode classNode) {
        classNode.fields.remove(ASMUtil.getFieldNodeByName("WORLD_HEIGHT", classNode));
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("<init>", classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(138, methodNode);
            ASMUtil.removeInsnSinceIndex(index, 4, methodNode);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("checkForBiomeTinting", classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(850, methodNode);
            ASMUtil.removeInsnSinceIndex(index + 1, 4, methodNode);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("createTintTable", classNode);
            classNode.methods.remove(methodNode);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("tintFromFakePlacedBlock", classNode);
            int start = ASMUtil.getInsnIndexByLineNumber(878, methodNode);
            int end = ASMUtil.getInsnIndexByLineNumber(897, methodNode);
            ASMUtil.removeInsnBetween(start, end + 2, methodNode);
            methodNode.tryCatchBlocks.remove(0);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("getBiomeTint", classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(990, methodNode) - 4;
            ((MethodInsnNode) methodNode.instructions.get(index - 1)).desc = "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/registry/RegistryEntry;";
            ASMUtil.insertInsnAtIndex(index, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/minecraft/util/registry/RegistryEntry", "value", "()Ljava/lang/Object;", true), methodNode);
            index++;
            ASMUtil.insertInsnAtIndex(index, new TypeInsnNode(Opcodes.CHECKCAST, "net/minecraft/world/biome/Biome"), methodNode);
        }
    }
}
