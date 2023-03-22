package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap;

import net.burningtnt.voxellatest.mappers.ASMUtil;
import net.burningtnt.voxellatest.mappers.AbstractVoxelMapClassMapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

@SuppressWarnings("unused")
public class Radar extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.Radar";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
//            MethodNode methodNode = ASMUtil.getMethodNodeByName("lambda$static$1", classNode);
//            int index = ASMUtil.getInsnIndexByLineNumber(1021, methodNode) + 2;
//            ((FieldInsnNode) methodNode.instructions.get(index)).name = "NONE";
//            index = ASMUtil.getInsnIndexByLineNumber(1022, methodNode) + 2;
//            ((FieldInsnNode) methodNode.instructions.get(index)).name = "WHITE";
//            index = ASMUtil.getInsnIndexByLineNumber(1023, methodNode) + 2;
//            ((FieldInsnNode) methodNode.instructions.get(index)).name = "WHITE_FIELD";
//            index = ASMUtil.getInsnIndexByLineNumber(1024, methodNode) + 2;
//            ((FieldInsnNode) methodNode.instructions.get(index)).name = "WHITE_DOTS";
//            index = ASMUtil.getInsnIndexByLineNumber(1025, methodNode) + 2;
//            ((FieldInsnNode) methodNode.instructions.get(index)).name = "BLACK_DOTS";
        }
    }
}
