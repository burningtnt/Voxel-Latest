package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.persistent;

import net.burningtnt.voxellatest.mappers.ASMUtil;
import net.burningtnt.voxellatest.mappers.AbstractVoxelMapClassMapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

@SuppressWarnings("unused")
public class GuiPersistentMap extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.persistent.GuiPersistentMap";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("render", classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(705, methodNode) + 8;
            ((JumpInsnNode) methodNode.instructions.get(index)).setOpcode(Opcodes.IFEQ);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("popupAction", classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(1388, methodNode) + 4;
            ASMUtil.insertInsnAtIndex(index, new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "net/minecraft/client/MinecraftClient",
                    "getInstance",
                    "()Lnet/minecraft/client/MinecraftClient;"
            ), methodNode);
            index++;
            ASMUtil.insertInsnAtIndex(index, new FieldInsnNode(
                    Opcodes.GETFIELD,
                    "net/minecraft/client/MinecraftClient",
                    "world",
                    "Lnet/minecraft/client/world/ClientWorld;"
            ), methodNode);
            index++;
            ASMUtil.insertInsnAtIndex(index, new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "net/minecraft/world/HeightLimitView",
                    "getBottomY",
                    "()I"
            ), methodNode);
            index++;
            ASMUtil.insertInsnAtIndex(index, new InsnNode(
                    Opcodes.ISUB
            ), methodNode);
            index += 15;
            ASMUtil.removeInsnSinceIndex(index, 1, methodNode);
            ASMUtil.insertInsnAtIndex(index, new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "net/minecraft/client/MinecraftClient",
                    "getInstance",
                    "()Lnet/minecraft/client/MinecraftClient;"
            ), methodNode);
            index++;
            ASMUtil.insertInsnAtIndex(index, new FieldInsnNode(
                    Opcodes.GETFIELD,
                    "net/minecraft/client/MinecraftClient",
                    "world",
                    "Lnet/minecraft/client/world/ClientWorld;"
            ), methodNode);
            index++;
            ASMUtil.insertInsnAtIndex(index, new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "net/minecraft/world/HeightLimitView",
                    "getTopY",
                    "()I"
            ), methodNode);

            index = ASMUtil.getInsnIndexByLineNumber(1397,methodNode) + 8;
            ASMUtil.removeInsnSinceIndex(index,1,methodNode);
            ASMUtil.insertInsnAtIndex(index, new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "net/minecraft/client/MinecraftClient",
                    "getInstance",
                    "()Lnet/minecraft/client/MinecraftClient;"
            ), methodNode);
            index++;
            ASMUtil.insertInsnAtIndex(index, new FieldInsnNode(
                    Opcodes.GETFIELD,
                    "net/minecraft/client/MinecraftClient",
                    "world",
                    "Lnet/minecraft/client/world/ClientWorld;"
            ), methodNode);
            index++;
            ASMUtil.insertInsnAtIndex(index, new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "net/minecraft/world/HeightLimitView",
                    "getTopY",
                    "()I"
            ), methodNode);
        }
    }
}
