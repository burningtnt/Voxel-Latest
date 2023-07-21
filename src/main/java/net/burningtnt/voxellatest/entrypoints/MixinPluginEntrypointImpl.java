package net.burningtnt.voxellatest.entrypoints;

import net.burningtnt.voxellatest.VoxelMapClassRemapManager;
import net.burningtnt.voxellatest.util.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MixinPluginEntrypointImpl implements IMixinConfigPlugin {
    static {
        try {
            Logger.info("Start to remap VoxelMap");
            VoxelMapClassRemapManager.run();
            Logger.info("Success to remap VoxelMap");
        } catch (Throwable e) {
            System.err.println("[Voxel Latest] A fatal error happended while invoking net.burningtnt.voxellatest.VoxelMapClassRemapManager.run .");
            e.printStackTrace(System.err);
            try {
                System.exit(-1);
            } catch (Throwable e2) {
                System.err.println("[Voxel Latest] A fatal error happened while invoking java.lang.System.exit . Use Unsafe package to crash JVM.");
                e.printStackTrace(System.err);

                try {
                    Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                    Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
                    theUnsafeField.setAccessible(true);
                    Method getObjectMethod = unsafeClass.getMethod("getObject", Object.class, long.class);
                    getObjectMethod.invoke(theUnsafeField.get(null), null, 0);
                } catch (Throwable e3) {
                    System.err.println("[Voxel Latest] A fatal error happened while using Unsafe package to crash JVM. Use infinity loop instead.");
                    e3.printStackTrace(System.err);

                    //noinspection InfiniteLoopStatement
                    while (true) {
                    }
                }
            }
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        Mixins.addConfiguration("mixin.voxelmap.json");
        return new ArrayList<>();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
