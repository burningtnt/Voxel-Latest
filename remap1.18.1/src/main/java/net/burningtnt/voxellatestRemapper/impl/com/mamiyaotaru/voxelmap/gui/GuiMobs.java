package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.gui;

import net.burningtnt.voxellatest.mappers.ASMUtil;
import net.burningtnt.voxellatest.mappers.AbstractVoxelMapClassMapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

@SuppressWarnings("unused")
public class GuiMobs extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.gui.GuiMobs";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("setTooltip",classNode);
            methodNode.desc = methodNode.desc.substring(0,methodNode.desc.lastIndexOf(")")) + ")V";
            int index = ASMUtil.getInsnIndexByLineNumber(207,methodNode);
            methodNode.instructions.remove(methodNode.instructions.get(index + 5));
            methodNode.instructions.remove(methodNode.instructions.get(index + 3));
            methodNode.instructions.insertBefore(new InsnNode(Opcodes.RETURN),methodNode.instructions.getLast());
        }
    }
}
