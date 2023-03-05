package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.gui.overridden;

import net.burningtnt.voxellatest.mappers.ASMUtil;
import net.burningtnt.voxellatest.mappers.AbstractVoxelMapClassMapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

@SuppressWarnings("unused")
public class GuiOptionButtonMinimap extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.gui.overridden.GuiOptionButtonMinimap";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("<init>(IIILnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)V",classNode);
            classNode.methods.remove(methodNode);
        }
    }
}
