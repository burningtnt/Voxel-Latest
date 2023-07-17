package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.fabricmod.mixins;

import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.tree.ClassNode;

@SuppressWarnings("unused")
public class ONMixinWorldRenderer extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.fabricmod.mixins.ONMixinWorldRenderer";
    }

    @Override
    public void remap(ClassNode classNode) {
    }
}
