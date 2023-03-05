package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.persistent;

import net.burningtnt.voxellatest.mappers.ASMUtil;
import net.burningtnt.voxellatest.mappers.AbstractVoxelMapClassMapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

@SuppressWarnings("unused")
public class CachedRegion extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.persistent.CachedRegion";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("loadAnvilData",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(497,methodNode) + 13;
            ASMUtil.removeInsnSinceIndex(index,1,methodNode);
            ((MethodInsnNode) methodNode.instructions.get(index)).desc = "(Lnet/minecraft/util/registry/RegistryKey;Ljava/nio/file/Path;)Ljava/nio/file/Path;";
            index ++;
            ASMUtil.insertInsnAtIndex(index,new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "java/nio/file/Path",
                    "toFile",
                    "()Ljava/io/File;"
            ),methodNode);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("lambda$loadAnvilData$1",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(526,methodNode) + 10;
            ASMUtil.insertInsnAtIndex(index,new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "java/util/Optional",
                    "empty",
                    "()Ljava/util/Optional;"
            ),methodNode);
            index ++;
            ((MethodInsnNode) methodNode.instructions.get(index)).desc = "(Lnet/minecraft/util/registry/RegistryKey;Ljava/util/function/Supplier;Lnet/minecraft/nbt/NbtCompound;Ljava/util/Optional;)Lnet/minecraft/nbt/NbtCompound;";

        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("lambda$loadAnvilData$3",classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(703,methodNode) + 4;
            ((MethodInsnNode) methodNode.instructions.get(index)).desc = "(Ljava/util/function/BooleanSupplier;Z)V";
            ASMUtil.insertInsnAtIndex(index,new VarInsnNode(Opcodes.ALOAD,0),methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,new FieldInsnNode(
                    Opcodes.GETFIELD,
                    "com/mamiyaotaru/voxelmap/persistent/CachedRegion",
                    "executor",
                    "Lnet/minecraft/util/thread/ThreadExecutor;"
            ),methodNode);
            index ++;
            ASMUtil.insertInsnAtIndex(index,new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "net/minecraft/util/thread/ThreadExecutor",
                    "isOnThread","()Z"),methodNode
            );
        }
    }
}
