package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap;

import net.burningtnt.voxellatest.asm.ASMUtil;
import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

@SuppressWarnings("unused")
public class ColorManager extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.ColorManager";
    }

    @Override
    public void remap(ClassNode classNode) {
        classNode.fields.remove(ASMUtil.getFieldNodeByName("WORLD_HEIGHT", classNode));
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("<init>", classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(138, methodNode);
            ASMUtil.removeInsnSinceIndex(index, 4, methodNode);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("checkForBiomeTinting", classNode);
            int index = ASMUtil.getInsnIndexByLineNumber(850, methodNode);
            ASMUtil.removeInsnSinceIndex(index + 1, 4, methodNode);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("createTintTable", classNode);
            classNode.methods.remove(methodNode);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("tintFromFakePlacedBlock", classNode);
            int start = ASMUtil.getInsnIndexByLineNumber(878, methodNode);
            int end = ASMUtil.getInsnIndexByLineNumber(897, methodNode);
            ASMUtil.removeInsnBetween(start, end + 2, methodNode);
            methodNode.tryCatchBlocks.remove(0);
        }
    }
}
