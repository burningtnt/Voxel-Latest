package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.gui.overridden;

import net.burningtnt.voxellatest.asm.ASMUtil;
import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class PopupGuiButton extends AbstractVoxelMapClassMapper {
    @Nonnull
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.gui.overridden.PopupGuiButton";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("<init>(IIILnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;Lcom/mamiyaotaru/voxelmap/gui/overridden/IPopupGuiScreen;)V", classNode);
            classNode.methods.remove(methodNode);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("render",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(25,methodNode) + 6;
            ((JumpInsnNode) methodNode.instructions.get(index)).setOpcode(Opcodes.IFEQ);
        }
    }
}
