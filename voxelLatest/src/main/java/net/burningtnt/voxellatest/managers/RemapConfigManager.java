package net.burningtnt.voxellatest.managers;

import com.google.gson.*;
import net.burningtnt.voxellatest.util.LoggerManagerUtil;
import net.burningtnt.voxellatest.util.ModInfoUtil;
import net.burningtnt.voxellatest.util.VoxelMapClassRemapUtil;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class RemapConfigManager {
    private static class Keys {
        public static final String CONFIG_FILE_VERSION = "configFileVersion";
        public static final String VOXEL_LATEST_VERSION = "voxelLatestVersion";
        public static final String VOXEL_REMAPPER_VERSION = "voxelRemapperVersion;";
        public static final String MINECRAFT_VERSION = "minecraftVersion";

        public static final int CURRENT_CONFIG_FILE_VERSION = 1;
    }

    public static class Config {
        private final int configFileVersion;
        @Nullable
        private final String voxelLatestVersion;
        @Nullable
        private final String voxelRemapperVersion;
        @Nullable
        private final String minecraftVersion;

        private Config(int configFileVersion, @Nullable String voxelLatestVersion, @Nullable String voxelRemapperVersion, @Nullable String minecraftVersion) {
            this.configFileVersion = configFileVersion;
            this.voxelLatestVersion = voxelLatestVersion;
            this.voxelRemapperVersion = voxelRemapperVersion;
            this.minecraftVersion = minecraftVersion;
        }

        public int getConfigFileVersion() {
            return configFileVersion;
        }

        @Nullable
        public String getMinecraftVersion() {
            return minecraftVersion;
        }

        @Nullable
        public String getVoxelLatestVersion() {
            return voxelLatestVersion;
        }

        @Nullable
        public String getVoxelRemapperVersion() {
            return voxelRemapperVersion;
        }

        public boolean isLatest() {
            if (configFileVersion != Keys.CURRENT_CONFIG_FILE_VERSION) {
                return false;
            }
            if (!Objects.equals(voxelLatestVersion, FabricLoader.getInstance().getModContainer(ModInfoUtil.SELF).get().getMetadata().getVersion().getFriendlyString())) {
                return false;
            }
            if (!Objects.equals(voxelRemapperVersion, FabricLoader.getInstance().getModContainer(ModInfoUtil.VOXEL_REMAPPER).get().getMetadata().getVersion().getFriendlyString())) {
                return false;
            }
            if (!Objects.equals(minecraftVersion, VoxelMapClassRemapUtil.getMinecraftVersion())) {
                return false;
            }
            return true;
        }

        public JsonElement toJsonElement() {
            if (this.voxelLatestVersion == null) {
                throw new NullPointerException("net.burningtnt.voxellatest.config.ConfigFileManager.Config.voxelLatestVersion is null.");
            }
            if (this.voxelRemapperVersion == null) {
                throw new NullPointerException("net.burningtnt.voxellatest.config.ConfigFileManager.Config.voxelRemapperVersion is null.");
            }
            if (this.minecraftVersion == null) {
                throw new NullPointerException("net.burningtnt.voxellatest.config.ConfigFileManager.Config.minecraftVersion is null.");
            }

            JsonObject root = new JsonObject();
            root.add(Keys.CONFIG_FILE_VERSION, new JsonPrimitive(this.configFileVersion));
            root.add(Keys.VOXEL_LATEST_VERSION, new JsonPrimitive(this.voxelLatestVersion));
            root.add(Keys.VOXEL_REMAPPER_VERSION, new JsonPrimitive(this.voxelRemapperVersion));
            root.add(Keys.MINECRAFT_VERSION, new JsonPrimitive(this.minecraftVersion));
            return root;
        }

        private static class ConfigFactory {
            private int configFileVersion = -1;
            @Nullable
            private String voxelLatestVersion = null;
            @Nullable
            private String voxelRemapperVersion = null;
            @Nullable
            private String minecraftVersion = null;

            private static Config currentConfig = null;

            private ConfigFactory() {
            }

            public static ConfigFactory of() {
                return new ConfigFactory();
            }

            public static Config getCurrent() {
                if (currentConfig == null) {
                    Config.ConfigFactory configFactory = Config.ConfigFactory.of();
                    configFactory.setConfigFileVersion(Keys.CURRENT_CONFIG_FILE_VERSION);
                    configFactory.setVoxelLatestVersion(FabricLoader.getInstance().getModContainer(ModInfoUtil.SELF).get().getMetadata().getVersion().getFriendlyString());
                    configFactory.setVoxelRemapperVersion(FabricLoader.getInstance().getModContainer(ModInfoUtil.VOXEL_REMAPPER).get().getMetadata().getVersion().getFriendlyString());
                    configFactory.setMinecraftVersion(VoxelMapClassRemapUtil.getMinecraftVersion());

                    currentConfig = configFactory.build();
                }

                return currentConfig;
            }

            public void setConfigFileVersion(int configFileVersion) {
                this.configFileVersion = configFileVersion;
            }

            public void setVoxelLatestVersion(@Nullable String voxelLatestVersion) {
                this.voxelLatestVersion = voxelLatestVersion;
            }

            public void setVoxelRemapperVersion(@Nullable String voxelRemapperVersion) {
                this.voxelRemapperVersion = voxelRemapperVersion;
            }

            public void setMinecraftVersion(@Nullable String minecraftVersion) {
                this.minecraftVersion = minecraftVersion;
            }

            public int getConfigFileVersion() {
                return configFileVersion;
            }

            @Nullable
            public String getMinecraftVersion() {
                return minecraftVersion;
            }

            @Nullable
            public String getVoxelLatestVersion() {
                return voxelLatestVersion;
            }

            @Nullable
            public String getVoxelRemapperVersion() {
                return voxelRemapperVersion;
            }

            public Config build() {
                return new Config(this.configFileVersion, this.voxelLatestVersion, this.voxelRemapperVersion, this.minecraftVersion);
            }
        }
    }

    private static boolean isVoxellatestRemapperDeveloping() {
        if ("ignoreDeveloping".equals(System.getProperty("voxellatest.cache"))) {
            return false;
        }
        List<Path> pathList = FabricLoader.getInstance().getModContainer("voxellatest-remapper").get().getRootPaths();
        try {
            return pathList.get(0).toFile().isDirectory();
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean shouldRemapVoxelMap() {
        if ("never".equals(System.getProperty("voxellatest.cache"))) {
            return true;
        }

        File configFile = ConfigFileManager.RemapConfigFileManager.getRemapVersionConfigFile();
        if (!configFile.exists()) {
            LoggerManagerUtil.info("ConfigFile don't exists");
            return true;
        }
        if (!configFile.isFile()) {
            LoggerManagerUtil.info("ConfigFile is not a File");
            return true;
        }

        if (isVoxellatestRemapperDeveloping()) {
            LoggerManagerUtil.info("Mod voxellatest-remapper is developing. Force update");
            return true;
        }

        File remappedJarFile = ConfigFileManager.RemapConfigFileManager.getRemapedFile();
        if (!remappedJarFile.exists()) {
            return true;
        }
        if (!remappedJarFile.isFile()) {
            return true;
        }

        File voxelMapJarFile = ConfigFileManager.RemapConfigFileManager.getRemapedFile();
        if (!voxelMapJarFile.exists()) {
            return true;
        }
        if (!voxelMapJarFile.isFile()) {
            return true;
        }

        return !RemapConfigManager.parseConfigFile(configFile).isLatest();
    }

    public static void writeCurrentConfig() {
        if (isVoxellatestRemapperDeveloping()) {
            return;
        }

//        File hashFile = new File(voxellatestFolder, "hash");
//        try (FileOutputStream fileOutputStream = new FileOutputStream(hashFile)) {
////            long sizeData = voxelMapJarFile.length();
////            fileOutputStream.write((byte) (sizeData >>> 24 & 0xFF));
////            fileOutputStream.write((byte) (sizeData >>> 16 & 0xFF));
////            fileOutputStream.write((byte) (sizeData >>> 8 & 0xFF));
////            fileOutputStream.write((byte) (sizeData & 0xFF));
//            fileOutputStream.write(String.valueOf(voxelMapJarFile.length()).getBytes());
//        } catch (IOException e) {
//            throw new RuntimeException(String.format("An Error was thron while writing data to \"%s\".", hashFile.getAbsolutePath()), e);
//        }

        File configFile = ConfigFileManager.RemapConfigFileManager.getRemapVersionConfigFile();

        RemapConfigManager.writeTo(configFile);
    }


    private static void writeTo(File configFile) {
        if (configFile.exists() && (!configFile.isFile() || !configFile.canWrite())) {
            throw new SecurityException(String.format("Failed to write to file \"%s\".", configFile.getAbsolutePath()));
        }

        byte[] data = Config.ConfigFactory.getCurrent().toJsonElement().toString().getBytes(StandardCharsets.UTF_8);

        try (FileOutputStream fileOutputStream = new FileOutputStream(configFile)) {
            fileOutputStream.write(data);
        } catch (IOException e) {
            throw new RuntimeException(String.format("An Error was thrown while writing data to \"%s\".", configFile.getAbsolutePath()), e);
        }
    }

    @NotNull
    private static Config parseConfigFile(@NotNull File configFile) {
        Config.ConfigFactory configFactory = Config.ConfigFactory.of();
        if (!configFile.exists() || !configFile.isFile() || !configFile.canRead()) {
            return configFactory.build();
        }

        byte[] data;
        try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
            data = fileInputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(String.format("An Error was thrown while reading data from \"%s\".", configFile), e);
        }

        JsonElement configJsonRoot;
        try {
            configJsonRoot = new Gson().fromJson(new String(data), JsonElement.class);
        } catch (JsonParseException e) {
            return configFactory.build();
        }

        if (!configJsonRoot.isJsonObject()) {
            return configFactory.build();
        }
        if (!configJsonRoot.getAsJsonObject().has(Keys.CONFIG_FILE_VERSION)) {
            return configFactory.build();
        }
        if (!configJsonRoot.getAsJsonObject().get(Keys.CONFIG_FILE_VERSION).isJsonPrimitive()) {
            return configFactory.build();
        }
        if (!configJsonRoot.getAsJsonObject().get(Keys.CONFIG_FILE_VERSION).getAsJsonPrimitive().isNumber()) {
            return configFactory.build();
        }
        configFactory.setConfigFileVersion(configJsonRoot.getAsJsonObject().get(Keys.CONFIG_FILE_VERSION).getAsJsonPrimitive().getAsInt());

        if (!configJsonRoot.getAsJsonObject().has(Keys.VOXEL_LATEST_VERSION)) {
            return configFactory.build();
        }
        if (!configJsonRoot.getAsJsonObject().get(Keys.VOXEL_LATEST_VERSION).isJsonPrimitive()) {
            return configFactory.build();
        }
        if (!configJsonRoot.getAsJsonObject().get(Keys.VOXEL_LATEST_VERSION).getAsJsonPrimitive().isString()) {
            return configFactory.build();
        }
        configFactory.setVoxelLatestVersion(configJsonRoot.getAsJsonObject().get(Keys.VOXEL_LATEST_VERSION).getAsJsonPrimitive().getAsString());

        if (!configJsonRoot.getAsJsonObject().has(Keys.VOXEL_REMAPPER_VERSION)) {
            return configFactory.build();
        }
        if (!configJsonRoot.getAsJsonObject().get(Keys.VOXEL_REMAPPER_VERSION).isJsonPrimitive()) {
            return configFactory.build();
        }
        if (!configJsonRoot.getAsJsonObject().get(Keys.VOXEL_REMAPPER_VERSION).getAsJsonPrimitive().isString()) {
            return configFactory.build();
        }
        configFactory.setVoxelRemapperVersion(configJsonRoot.getAsJsonObject().get(Keys.VOXEL_REMAPPER_VERSION).getAsJsonPrimitive().getAsString());

        if (!configJsonRoot.getAsJsonObject().has(Keys.MINECRAFT_VERSION)) {
            return configFactory.build();
        }
        if (!configJsonRoot.getAsJsonObject().get(Keys.MINECRAFT_VERSION).isJsonPrimitive()) {
            return configFactory.build();
        }
        if (!configJsonRoot.getAsJsonObject().get(Keys.MINECRAFT_VERSION).getAsJsonPrimitive().isString()) {
            return configFactory.build();
        }
        configFactory.setMinecraftVersion(configJsonRoot.getAsJsonObject().get(Keys.MINECRAFT_VERSION).getAsJsonPrimitive().getAsString());

        return configFactory.build();
    }
}
