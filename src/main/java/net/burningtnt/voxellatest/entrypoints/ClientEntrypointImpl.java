package net.burningtnt.voxellatest.entrypoints;

import net.burningtnt.voxellatest.util.LoggerManagerUtil;
import net.fabricmc.api.ClientModInitializer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ClientEntrypointImpl implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        try {
            Class<?> voxelMapClass = Class.forName("com.mamiyaotaru.voxelmap.fabricmod.FabricModVoxelMap");
            Constructor<?> constructor = voxelMapClass.getConstructor();
            Method method = voxelMapClass.getMethod("onInitializeClient");
            method.invoke(constructor.newInstance());
        } catch (Throwable var4) {
            LoggerManagerUtil.fail("An Error was thrown while invoking com.mamiyaotaru.voxelmap.fabricmod.FabricModVoxelMap#onInitializeClient", var4);
            throw new RuntimeException("An Error was thrown while invoking com.mamiyaotaru.voxelmap.fabricmod.FabricModVoxelMap#onInitializeClient", var4);
        }
    }
}
