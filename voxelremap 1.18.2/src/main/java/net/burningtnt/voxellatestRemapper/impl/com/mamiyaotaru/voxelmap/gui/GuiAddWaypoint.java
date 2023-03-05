package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.gui;

import net.burningtnt.voxellatest.mappers.ASMUtil;
import net.burningtnt.voxellatest.mappers.AbstractVoxelMapClassMapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

@SuppressWarnings("unused")
public class GuiAddWaypoint extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.gui.GuiAddWaypoint";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("keyPressed", classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(199, methodNode) + 3;
            ((JumpInsnNode) methodNode.instructions.get(index)).setOpcode(Opcodes.IFEQ);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("charTyped", classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(234, methodNode) + 3;
            ((JumpInsnNode) methodNode.instructions.get(index)).setOpcode(Opcodes.IFNE);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("mouseClicked", classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(257,methodNode) + 3;
            ((JumpInsnNode) methodNode.instructions.get(index)).setOpcode(Opcodes.IFEQ);
            int index2 = ASMUtil.getInsnIndexByLineNumber(304,methodNode) + 4;
            ((JumpInsnNode) methodNode.instructions.get(index)).setOpcode(Opcodes.IFEQ);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("mouseReleased",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(312,methodNode) + 3;
            ((JumpInsnNode) methodNode.instructions.get(index)).setOpcode(Opcodes.IFEQ);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("mouseDragged",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(320,methodNode) + 3;
            ((JumpInsnNode) methodNode.instructions.get(index)).setOpcode(Opcodes.IFEQ);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("mouseScrolled",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(328,methodNode) + 3;
            ((JumpInsnNode) methodNode.instructions.get(index)).setOpcode(Opcodes.IFEQ);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("overPopup",classNode);
            methodNode.instructions.remove(methodNode.instructions.getLast().getPrevious());
            LabelNode labelNode = new LabelNode();
            methodNode.instructions.insertBefore(methodNode.instructions.getLast(),new JumpInsnNode(Opcodes.IFEQ,labelNode));
            methodNode.instructions.insertBefore(methodNode.instructions.getLast(),new InsnNode(Opcodes.ICONST_0));
            methodNode.instructions.insertBefore(methodNode.instructions.getLast(),new InsnNode(Opcodes.IRETURN));
            methodNode.instructions.insertBefore(methodNode.instructions.getLast(),labelNode);
            methodNode.instructions.insertBefore(methodNode.instructions.getLast(),new InsnNode(Opcodes.ICONST_1));
            methodNode.instructions.insertBefore(methodNode.instructions.getLast(),new InsnNode(Opcodes.IRETURN));
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("popupOpen",classNode);
            methodNode.instructions.remove(methodNode.instructions.getLast().getPrevious());
            LabelNode labelNode = new LabelNode();
            methodNode.instructions.insertBefore(methodNode.instructions.getLast(),new JumpInsnNode(Opcodes.IFEQ,labelNode));
            methodNode.instructions.insertBefore(methodNode.instructions.getLast(),new InsnNode(Opcodes.ICONST_0));
            methodNode.instructions.insertBefore(methodNode.instructions.getLast(),new InsnNode(Opcodes.IRETURN));
            methodNode.instructions.insertBefore(methodNode.instructions.getLast(),labelNode);
            methodNode.instructions.insertBefore(methodNode.instructions.getLast(),new InsnNode(Opcodes.ICONST_1));
            methodNode.instructions.insertBefore(methodNode.instructions.getLast(),new InsnNode(Opcodes.IRETURN));
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("setTooltip",classNode);
            methodNode.desc = methodNode.desc.substring(0,methodNode.desc.lastIndexOf(")")) + ")V";
            int index = ASMUtil.getInsnIndexByLineNumber(452,methodNode);
            methodNode.instructions.remove(methodNode.instructions.get(index + 5));
            methodNode.instructions.remove(methodNode.instructions.get(index + 3));
            methodNode.instructions.insertBefore(new InsnNode(Opcodes.RETURN),methodNode.instructions.getLast());
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("drawTexturedModalRect(Lcom/mamiyaotaru/voxelmap/textures/Sprite;FF)V",classNode);
            classNode.methods.remove(methodNode);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("charTyped",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(234,methodNode) + 3;
            ((JumpInsnNode) methodNode.instructions.get(index)).setOpcode(Opcodes.IFEQ);
        }
    }
}
