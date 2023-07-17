package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.gui;

import net.burningtnt.voxellatest.asm.ASMUtil;
import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

@SuppressWarnings("unused")
public class GuiSlotMobs extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.gui.GuiSlotMobs";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("updateFilter",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(124,methodNode) + 5;
            LabelNode labelNode = ((JumpInsnNode)methodNode.instructions.get(index)).label;
            methodNode.instructions.remove(methodNode.instructions.get(index));
            ASMUtil.insertInsnAtIndex(index,new MethodInsnNode(Opcodes.INVOKEVIRTUAL,"java/lang/String","equals","(Ljava/lang/Object;)Z"),methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,new JumpInsnNode(Opcodes.IFEQ,labelNode),methodNode);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("lambda$updateFilter$1",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(130,methodNode) + 3;
            ASMUtil.insertInsnAtIndex(index,new TypeInsnNode(Opcodes.CHECKCAST,"net/minecraft/client/gui/widget/EntryListWidget$Entry"),methodNode);
        }
    }
}
