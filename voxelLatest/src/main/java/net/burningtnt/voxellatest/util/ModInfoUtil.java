package net.burningtnt.voxellatest.util;

import net.fabricmc.loader.api.FabricLoader;

public class ModInfoUtil {
    public static String VOXEL_MAP = "voxelmap";
    public static String SELF = "voxellatest";
    public static String VOXEL_REMAPPER = "voxellatest-remapper";

    public static String getVoxelLatestVersion() {
        return FabricLoader.getInstance().getModContainer(ModInfoUtil.SELF).get().getMetadata().getVersion().getFriendlyString();
    }

    public static String getVoxelRemapperVersion() {
        return FabricLoader.getInstance().getModContainer(ModInfoUtil.VOXEL_REMAPPER).get().getMetadata().getVersion().getFriendlyString();
    }

    public static String getMinecraftVersion() {
        return FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion().getFriendlyString();
    }


}
