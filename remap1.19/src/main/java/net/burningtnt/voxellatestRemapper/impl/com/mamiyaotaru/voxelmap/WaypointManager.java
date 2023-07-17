package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap;

import net.burningtnt.voxellatest.asm.ASMUtil;
import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class WaypointManager extends AbstractVoxelMapClassMapper {
    @Nonnull
    @NotNull
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.WaypointManager";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("onResourceManagerReload",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(201,methodNode) + 1;
            ASMUtil.removeInsnSinceIndex(index,2,methodNode);
        }
    }
}
