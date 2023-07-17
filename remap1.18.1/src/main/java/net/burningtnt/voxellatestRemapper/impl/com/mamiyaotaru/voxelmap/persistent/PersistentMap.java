package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.persistent;

import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.tree.ClassNode;

@SuppressWarnings("unused")
public class PersistentMap extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.persistent.PersistentMap";
    }

    @Override
    public void remap(ClassNode classNode) {
    }
}
