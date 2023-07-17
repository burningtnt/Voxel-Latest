package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.gui;

import net.burningtnt.voxellatest.asm.ASMUtil;
import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

@SuppressWarnings("unused")
public class GuiSlotWaypoints extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.gui.GuiSlotWaypoints";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("lambda$updateFilter$1",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(211,methodNode) + 3;
            ASMUtil.insertInsnAtIndex(index,new TypeInsnNode(Opcodes.CHECKCAST,"net/minecraft/client/gui/widget/EntryListWidget$Entry"),methodNode);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("lambda$new$0",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(54,methodNode) + 3;
            ASMUtil.insertInsnAtIndex(index,new TypeInsnNode(Opcodes.CHECKCAST,"net/minecraft/client/gui/widget/EntryListWidget$Entry"),methodNode);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("setSelected(Lcom/mamiyaotaru/voxelmap/gui/GuiSlotWaypoints$WaypointItem;)V",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(62,methodNode) + 15;
            ((MethodInsnNode) methodNode.instructions.get(index)).owner = "net/minecraft/client/resource/language/I18n";
            ((MethodInsnNode) methodNode.instructions.get(index)).name = "translate";
            ((MethodInsnNode) methodNode.instructions.get(index)).desc = "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;";
            methodNode.instructions.remove(methodNode.instructions.get(index + 1));
            index = ASMUtil.getInsnIndexByLineNumber(62,methodNode) + 2;
            methodNode.instructions.remove(methodNode.instructions.get(index + 1));
            methodNode.instructions.remove(methodNode.instructions.get(index));
        }
    }
}
