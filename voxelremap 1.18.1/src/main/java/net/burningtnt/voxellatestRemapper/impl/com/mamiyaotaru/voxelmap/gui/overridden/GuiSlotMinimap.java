package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.gui.overridden;

import net.burningtnt.voxellatest.mappers.ASMUtil;
import net.burningtnt.voxellatest.mappers.AbstractVoxelMapClassMapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

@SuppressWarnings("unused")
public class GuiSlotMinimap extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.gui.overridden.GuiSlotMinimap";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("setDimensions",classNode);
            classNode.methods.remove(methodNode);
        }
    }
}
