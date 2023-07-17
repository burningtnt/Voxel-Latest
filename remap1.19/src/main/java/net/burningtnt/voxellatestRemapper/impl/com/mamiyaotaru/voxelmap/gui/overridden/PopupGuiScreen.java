package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.gui.overridden;

import net.burningtnt.voxellatest.asm.ASMUtil;
import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

@SuppressWarnings("unused")
public class PopupGuiScreen extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.gui.overridden.PopupGuiScreen";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("overPopup",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(48,methodNode) + 3;
            ASMUtil.removeInsnSinceIndex(index,0,methodNode);
            LabelNode labelNode = new LabelNode();
            ASMUtil.insertInsnAtIndex(index,new JumpInsnNode(Opcodes.IFNE,labelNode),methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,new InsnNode(Opcodes.ICONST_1),methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,new InsnNode(Opcodes.IRETURN),methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,labelNode,methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,new LineNumberNode(49,labelNode),methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,new InsnNode(Opcodes.ICONST_0),methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,new InsnNode(Opcodes.IRETURN),methodNode);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("popupOpen",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(53,methodNode) + 4;
            ((JumpInsnNode) methodNode.instructions.get(index)).setOpcode(Opcodes.IFGE);
        }
    }
}
