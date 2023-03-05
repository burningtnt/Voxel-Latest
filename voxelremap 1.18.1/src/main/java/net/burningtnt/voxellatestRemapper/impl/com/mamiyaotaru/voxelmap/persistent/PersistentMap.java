package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.persistent;

import net.burningtnt.voxellatest.mappers.ASMUtil;
import net.burningtnt.voxellatest.mappers.AbstractVoxelMapClassMapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

@SuppressWarnings("unused")
public class PersistentMap extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.persistent.PersistentMap";
    }

    @Override
    public void remap(ClassNode classNode) {
//        {
//            MethodNode methodNode = ASMUtil.getMethodNodeByName("getAndStoreData", classNode);
//            int index = ASMUtil.getInsnIndexByLineNumber(248, methodNode) + 8;
//            ASMUtil.insertInsnAtIndex(index, new MethodInsnNode(
//                    Opcodes.INVOKEVIRTUAL,
//                    "net/minecraft/util/registry/RegistryEntry",
//                    "value",
//                    "()Ljava/lang/Object;"
//            ), methodNode);
//            index ++;
//            ASMUtil.insertInsnAtIndex(index,new TypeInsnNode(
//                    Opcodes.CHECKCAST,
//                    "net/minecraft/world/biome/Biome"
//            ),methodNode);
//
//            index = ASMUtil.getInsnIndexByLineNumber(248, methodNode) + 7;
//            ((MethodInsnNode) methodNode.instructions.get(index)).desc = "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/registry/RegistryEntry;";
//        }
    }
}
