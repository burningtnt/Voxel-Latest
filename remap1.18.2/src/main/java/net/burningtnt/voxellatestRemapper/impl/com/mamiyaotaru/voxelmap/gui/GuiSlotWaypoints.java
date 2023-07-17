package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.gui;

import net.burningtnt.voxellatest.asm.ASMUtil;
import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class GuiSlotWaypoints extends AbstractVoxelMapClassMapper {
    @Nonnull
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
    }
}
