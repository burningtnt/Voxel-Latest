package net.burningtnt.voxellatest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Path;

public final class ModInfo {
    private ModInfo() {
    }

    public static final String VOXEL_MAP = "voxelmap";

    public static final String VOXEL_LATEST = "voxellatest";

    public static final String VOXEL_REMAPPER = "voxellatest-remapper";

    public static final String MINECRAFT = "minecraft";

    public static final ModContainer VOXEL_MAP_MOD = FabricLoader.getInstance().getModContainer(ModInfo.VOXEL_LATEST).orElseThrow(() -> new IllegalStateException("VoxelLatest must be loaded"));

    public static final ModContainer VOXEL_LATEST_MOD = FabricLoader.getInstance().getModContainer(ModInfo.VOXEL_LATEST).orElseThrow(() -> new IllegalStateException("VoxelLatest must be loaded"));

    public static final ModContainer VOXEL_REMAPPER_MOD = FabricLoader.getInstance().getModContainer(ModInfo.VOXEL_LATEST).orElseThrow(() -> new IllegalStateException("VoxelLatest must be loaded"));

    public static final ModContainer MINECRAFT_MOD = FabricLoader.getInstance().getModContainer(ModInfo.VOXEL_LATEST).orElseThrow(() -> new IllegalStateException("VoxelLatest must be loaded"));

    public static final Path MOD_DIR = FabricLoader.getInstance().getGameDir().resolve("voxellatest").toAbsolutePath();

    public static final Path VOXEL_MAP_RAW_FILE = MOD_DIR.resolve("resource.jar").toAbsolutePath();

    public static final Path VOXEL_MAP_REMAPPING_YARN_DOWN = MOD_DIR.resolve("remapping-1.17-yarn.jar").toAbsolutePath();

    public static final Path VOXEL_MAP_REMAPPING_ASM_DONE = MOD_DIR.resolve("remapping-current-yarn.jar").toAbsolutePath();

    public static final Path VOXEL_MAP_REMAPPING_DONE = MOD_DIR.resolve("remapped.jar").toAbsolutePath();

    public static final Path VERSION_CONFIG_FILE = MOD_DIR.resolve("version-config.json").toAbsolutePath();

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
}
