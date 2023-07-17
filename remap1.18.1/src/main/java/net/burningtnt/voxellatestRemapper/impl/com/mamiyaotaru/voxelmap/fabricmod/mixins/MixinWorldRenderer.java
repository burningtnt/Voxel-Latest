package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.fabricmod.mixins;

import net.burningtnt.voxellatest.asm.ASMUtil;
import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

@SuppressWarnings("unused")
public class MixinWorldRenderer extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.fabricmod.mixins.MixinWorldRenderer";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("postRenderLayer",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(78,methodNode) - 1;
            ASMUtil.insertInsnAtIndex(index,new MethodInsnNode(Opcodes.INVOKESTATIC,"net/minecraft/client/MinecraftClient","getInstance","()Lnet/minecraft/client/MinecraftClient;"),methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,new FieldInsnNode(Opcodes.GETFIELD,"net/minecraft/client/MinecraftClient","WorldRenderer","Lnet/minecraft/client/render/WorldRenderer;"),methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"net/minecraft/client/render/WorldRenderer","getTranslucentFramebuffer","()Lnet/minecraft/client/gl/Framebuffer;"),methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,new JumpInsnNode(Opcodes.IFNULL,ASMUtil.getLabelNodeByLineNumber(82,methodNode)),methodNode);
        }
    }
}
