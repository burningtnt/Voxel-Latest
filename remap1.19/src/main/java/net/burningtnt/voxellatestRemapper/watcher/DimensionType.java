package net.burningtnt.voxellatestRemapper.watcher;

import net.burningtnt.voxellatest.mappers.AbstractVoxelMapInsnWatcher;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

@SuppressWarnings("unused")
public class DimensionType extends AbstractVoxelMapInsnWatcher {
    @Override
    public void remap(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            AbstractInsnNode abstractInsnNode = methodNode.instructions.getFirst();
            while (abstractInsnNode != null) {
                if (abstractInsnNode instanceof MethodInsnNode methodInsnNode) {
                    if (methodInsnNode.owner.equals("net/minecraft/world/dimension/DimensionType") && methodInsnNode.name.equals("method_31110") && methodInsnNode.desc.equals("()D")) {
                        methodInsnNode.name = "coordinateScale";
                    }
                }

                abstractInsnNode = abstractInsnNode.getNext();
            }
        }
    }

    @NotNull
    @Override
    public Shift injectPoint() {
        return Shift.BEFORE;
    }
}
