package net.burningtnt.voxellatest.asm;

import org.objectweb.asm.tree.ClassNode;

public abstract class AbstractVoxelMapInsnWatcher {
    public enum Shift {
        BEFORE("BEFORE"),
        AFTER("AFTER");
        public final String name;

        Shift(String name) {
            this.name = name;
        }
    }

    public AbstractVoxelMapInsnWatcher() {
    }

    public int priority() {
        return 1000;
    }

    public abstract void remap(ClassNode classNode);

    public abstract Shift injectPoint();
}
