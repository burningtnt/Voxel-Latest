package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.gui;

import net.burningtnt.voxellatest.asm.ASMUtil;
import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class GuiWaypoints extends AbstractVoxelMapClassMapper {
    @Nonnull
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.gui.GuiWaypoints";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("teleportClicked",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(213,methodNode) + 9;
            methodNode.instructions.remove(methodNode.instructions.get(index));
            ASMUtil.insertInsnAtIndex(index,new MethodInsnNode(Opcodes.INVOKESTATIC,"net/minecraft/client/MinecraftClient","getInstance","()Lnet/minecraft/client/MinecraftClient;"),methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,new FieldInsnNode(Opcodes.GETFIELD,"net/minecraft/client/MinecraftClient","world","Lnet/minecraft/client/world/ClientWorld;"),methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"net/minecraft/world/HeightLimitView","getBottomY","()I"),methodNode);
            index = ASMUtil.getInsnIndexByLineNumber(214,methodNode) + 19;
            methodNode.instructions.remove(methodNode.instructions.get(index));
            ASMUtil.insertInsnAtIndex(index,new MethodInsnNode(Opcodes.INVOKESTATIC,"net/minecraft/client/MinecraftClient","getInstance","()Lnet/minecraft/client/MinecraftClient;"),methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,new FieldInsnNode(Opcodes.GETFIELD,"net/minecraft/client/MinecraftClient","world","Lnet/minecraft/client/world/ClientWorld;"),methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"net/minecraft/world/HeightLimitView","getTopY","()I"),methodNode);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("setTooltip",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(406,methodNode);
            methodNode.desc = methodNode.desc.substring(0,methodNode.desc.indexOf(')')) + ")V";
            methodNode.instructions.remove(methodNode.instructions.get(index + 5));
            methodNode.instructions.remove(methodNode.instructions.get(index + 3));
            methodNode.instructions.insertBefore(new InsnNode(Opcodes.RETURN),methodNode.instructions.getLast());
        }
    }
}
