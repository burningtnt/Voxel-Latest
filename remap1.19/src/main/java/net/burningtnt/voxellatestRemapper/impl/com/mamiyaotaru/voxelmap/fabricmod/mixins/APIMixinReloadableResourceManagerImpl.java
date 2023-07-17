package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.fabricmod.mixins;

import net.burningtnt.voxellatest.asm.ASMUtil;
import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

@SuppressWarnings("unused")
public class APIMixinReloadableResourceManagerImpl extends AbstractVoxelMapClassMapper {
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.fabricmod.mixins.APIMixinReloadableResourceManagerImpl";
    }

    @Override
    public void remap(ClassNode classNode) {
        {
            FieldNode fieldNode = ASMUtil.getFieldNodeByName("zipPack",classNode);
            classNode.fields.remove(fieldNode);
        }
        {
            MethodNode methodNode = ASMUtil.getMethodNodeByName("reload",classNode);
            classNode.methods.remove(methodNode);
        }
    }
}
