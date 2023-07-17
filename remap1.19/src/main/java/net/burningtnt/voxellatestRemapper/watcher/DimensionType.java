package net.burningtnt.voxellatestRemapper.watcher;

import net.burningtnt.voxellatest.asm.AbstractVoxelMapInsnWatcher;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

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

                    if (methodInsnNode.owner.equals("net/minecraft/client/render/BufferBuilder") && methodInsnNode.name.equals("end") && methodInsnNode.desc.equals("()V")) {
                        methodInsnNode.desc = "()Lnet/minecraft/client/render/BufferBuilder$BuiltBuffer;";
                    }

                    if (methodInsnNode.owner.equals("net/minecraft/text/MutableText") && methodInsnNode.name.equals("method_27692") && methodInsnNode.desc.equals("(Lnet/minecraft/util/Formatting;)Lnet/minecraft/text/MutableText;")) {
                        methodInsnNode.name = "formatted";
                    }
                }

                abstractInsnNode = abstractInsnNode.getNext();
            }
        }
    }

    @Nonnull
    @NotNull
    @Override
    public Shift injectPoint() {
        return Shift.BEFORE;
    }
}
