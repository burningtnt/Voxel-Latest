package net.burningtnt.voxellatestRemapper.impl.com.mamiyaotaru.voxelmap.fabricmod.mixins;

import net.burningtnt.voxellatest.mappers.ASMUtil;
import net.burningtnt.voxellatest.mappers.AbstractVoxelMapClassMapper;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class ONMixinWorldRenderer extends AbstractVoxelMapClassMapper {
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
