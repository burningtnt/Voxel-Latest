package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.gui;

import net.burningtnt.voxellatest.asm.ASMUtil;
import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class GuiSlotMobs_MobItem extends AbstractVoxelMapClassMapper {
    @Nonnull
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.gui.GuiSlotMobs$MobItem";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("render",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(180,methodNode) + 5;
            ((MethodInsnNode) methodNode.instructions.get(index)).desc = "(Lcom/mamiyaotaru/voxelmap/gui/GuiMobs;Lnet/minecraft/text/Text;)V";
            ASMUtil.removeInsnSinceIndex(index + 1,1,methodNode);
        }
    }
}
