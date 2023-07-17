package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.util;

import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;

@SuppressWarnings("unused")
public class Waypoint extends AbstractVoxelMapClassMapper {
    @NotNull
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.util.Waypoint";
    }

    @Override
    public void remap(ClassNode classNode) {
    }
}
