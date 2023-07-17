package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.fabricmod.mixins;

import net.burningtnt.voxellatest.asm.AbstractVoxelMapClassMapper;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class ONMixinWorldRenderer extends AbstractVoxelMapClassMapper {
    @Nonnull
    @Override
    public String matchClass() {
        return "com.mamiyaotaru.voxelmap.fabricmod.mixins.ONMixinWorldRenderer";
    }

    @Override
    public void remap(ClassNode classNode) {
//        {
//            MethodNode methodNode = ASMUtil.getMethodNodeByName("onRotate",classNode);
//            AnnotationNode redirectAnnotationNode = ASMUtil.getAnnotationNodeByName(Redirect.class.descriptorString(),methodNode);
//            @SuppressWarnings("unchecked")
//            ArrayList<String> arrayList = (ArrayList<String>) ASMUtil.getPropertyByName("method",redirectAnnotationNode);
//            arrayList.set(0,"renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/math/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V");
//        }
    }
}
