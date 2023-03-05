package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.gui;

import net.burningtnt.voxellatest.mappers.ASMUtil;
import net.burningtnt.voxellatest.mappers.AbstractVoxelMapClassMapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

@SuppressWarnings("unused")
public class GuiButtonRowListPlayers_Row extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.gui.GuiButtonRowListPlayers$Row";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("drawButton", classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(195, methodNode) + 5;
            ((MethodInsnNode) methodNode.instructions.get(index)).desc = "(Lcom/mamiyaotaru/voxelmap/gui/GuiSelectPlayer;Lnet/minecraft/text/Text;)V";
            ASMUtil.removeInsnSinceIndex(index + 1,1,methodNode);
        }
    }
}
