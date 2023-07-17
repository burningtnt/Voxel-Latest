package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.gui;

import net.burningtnt.voxellatest.asm.ASMUtil;
import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

@SuppressWarnings("unused")
public class GuiSelectPlayer extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.gui.GuiSelectPlayer";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("setTooltip",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(219,methodNode);
            methodNode.desc = methodNode.desc.substring(0,methodNode.desc.indexOf(')')) + ")V";
            methodNode.instructions.remove(methodNode.instructions.get(index + 5));
            methodNode.instructions.remove(methodNode.instructions.get(index + 3));
            methodNode.instructions.insertBefore(new InsnNode(Opcodes.RETURN),methodNode.instructions.getLast());
        }
    }
}
