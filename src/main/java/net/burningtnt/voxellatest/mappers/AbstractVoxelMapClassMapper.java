package net.burningtnt.voxellatest.mappers;

import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;

/**
 * An abstract class used for remap the classes of VoxelMap.
 * You need to override method {@link net.burningtnt.voxellatest.mappers.AbstractVoxelMapClassMapper#matchClass()}
 * and method {@link AbstractVoxelMapClassMapper#remap(ClassNode)} to modify the ASM.
 * What's more, you have to register the class name (in dot form) in fabric.mod.json
 * of voxellatest-remapper like
 * {@code "custom": {"voxellatest.remapperConfigClasses": ["net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.ColorManager"]}}
 */
public abstract class AbstractVoxelMapClassMapper {
    public AbstractVoxelMapClassMapper() {
    }

    /**
     * An abstract method to mark which class should 'I' remap.
     * @return The class name (in dot form) 'I' want to remap.
     */
    @Nonnull
    public abstract String matchClass();

    /**
     * An abstract method to remap the class marked in {@link AbstractVoxelMapClassMapper#matchClass()}
     */
    public abstract void remap(ClassNode classNode);

    /**
     * An abstract Method to get the priority of this Remapper. Default to be 1000.
     * @return the priority of this Remapper
     */

    public int priority() {
        return 1000;
    }
}
