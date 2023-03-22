package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap;

import net.burningtnt.voxellatest.mappers.ASMUtil;
import net.burningtnt.voxellatest.mappers.AbstractVoxelMapClassMapper;
import net.minecraft.world.dimension.DimensionType;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

@SuppressWarnings("unused")
public class VoxelMap extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.VoxelMap";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("lateInit",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(139,methodNode) + 3;
            ((TypeInsnNode) methodNode.instructions.get(index)).desc = "net/minecraft/resource/ReloadableResourceManagerImpl";

            index = ASMUtil.getInsnIndexByLineNumber(140,methodNode) + 3;
            MethodInsnNode methodInsnNode = (MethodInsnNode) methodNode.instructions.get(index);
            methodInsnNode.owner = "net/minecraft/resource/ReloadableResourceManagerImpl";
            methodInsnNode.name = "registerReloader";
            methodInsnNode.desc = "(Lnet/minecraft/resource/ResourceReloader;)V";
            methodInsnNode.setOpcode(Opcodes.INVOKEVIRTUAL);
            methodInsnNode.itf = false;

            LocalVariableNode localVariableNode = ASMUtil.getLocalVariableByName("resourceManager",methodNode);
            localVariableNode.desc = "Lnet/minecraft/resource/ReloadableResourceManagerImpl;";
        }
    }
}
