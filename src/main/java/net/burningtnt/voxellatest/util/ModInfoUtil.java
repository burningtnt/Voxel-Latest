package net.burningtnt.voxellatest.util;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public class ModInfoUtil {
    public static String VOXEL_MAP = "voxelmap";
    public static String VOXEL_LATEST = "voxellatest";
    public static String VOXEL_REMAPPER = "voxellatest-remapper";
    public static String MINECRAFT = "minecraft";

    public static String getMinecraftVersion() {
        return FabricLoader.getInstance().getModContainer(MINECRAFT).get().getMetadata().getVersion().getFriendlyString();
    }

    private static final File MOD_DIR = new File(FabricLoader.getInstance().getGameDir().toFile(), "voxellatest");
    private static final File VOXEL_MAP_RAW_FILE = new File(MOD_DIR, "resource.jar");
    private static final File VOXEL_MAP_REMAPPED_FILE = new File(MOD_DIR, "remapped.jar");
    private static final File VERSION_CONFIG_FILE = new File(MOD_DIR, "version-config.json");

    public static File getModDir() {
        return MOD_DIR;
    }

    public static File getVoxelMapRawFile() {
        return VOXEL_MAP_RAW_FILE;
    }

    public static File getVoxelMapRemappedFile() {
        return VOXEL_MAP_REMAPPED_FILE;
    }

    public static File getVersionConfigFile() {
        return VERSION_CONFIG_FILE;
    }
}
