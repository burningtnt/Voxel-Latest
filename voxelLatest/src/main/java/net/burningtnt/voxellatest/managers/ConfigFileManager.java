package net.burningtnt.voxellatest.managers;

import net.burningtnt.voxellatest.util.ModInfoUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;

public class ConfigFileManager {
    public static final File configRootDirectory = new File(FabricLoader.getInstance().getGameDir().toFile(), ModInfoUtil.SELF);
    public static final File remapRootDirectory = new File(configRootDirectory, "remaps");
    public static final File downloadRootDirectory = new File(configRootDirectory, "downloads");

    public static void init() {
        createDirectory(configRootDirectory);
        createDirectory(remapRootDirectory);
        createDirectory(downloadRootDirectory);
    }

    public static class RemapConfigFileManager {
        private static final File remapVersionConfigFile = new File(configRootDirectory, "version-config.json");
        private static final File remapedFile = new File(configRootDirectory, String.format("remapped-%s-%s", ModInfoUtil.getVoxelLatestVersion(), ModInfoUtil.getVoxelRemapperVersion()));
        private static final File resourceFile = new File(configRootDirectory, "resource.jar");

        public static File getRemapVersionConfigFile() {
            return remapVersionConfigFile;
        }

        public static File getRemapedFile() {
            return remapedFile;
        }

        public static File getResourceFile() {
            return resourceFile;
        }
    }

    private static void createDirectory(File directory) {
        if (directory.exists()) {
            if (directory.isFile()) {
                throw new RuntimeException(String.format("File \"%s\" must be a directory.", directory.getAbsolutePath()));
            }
        } else {
            if (!directory.mkdirs()) {
                throw new SecurityException(String.format("Failed to create directory \"%s\".", directory.getAbsolutePath()));
            }
        }
    }
}
