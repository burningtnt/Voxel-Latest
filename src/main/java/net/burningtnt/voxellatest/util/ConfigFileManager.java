package net.burningtnt.voxellatest.util;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ConfigFileManager {
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
            if (!Objects.equals(voxelLatestVersion, FabricLoader.getInstance().getModContainer(ModInfoUtil.VOXEL_LATEST).get().getMetadata().getVersion().getFriendlyString())) {
                return false;
            }
            if (!Objects.equals(voxelRemapperVersion, FabricLoader.getInstance().getModContainer(ModInfoUtil.VOXEL_REMAPPER).get().getMetadata().getVersion().getFriendlyString())) {
                return false;
            }
            if (!Objects.equals(minecraftVersion, ModInfoUtil.getMinecraftVersion())) {
                return false;
            }
            return true;
        }

        public JsonElement toJsonElement() {
            if (this.voxelLatestVersion == null) {
                throw new NullPointerException("net.burningtnt.voxellatest.util.ConfigFileManager.Config.voxelLatestVersion is null.");
            }
            if (this.voxelRemapperVersion == null) {
                throw new NullPointerException("net.burningtnt.voxellatest.util.ConfigFileManager.Config.voxelRemapperVersion is null.");
            }
            if (this.minecraftVersion == null) {
                throw new NullPointerException("net.burningtnt.voxellatest.util.ConfigFileManager.Config.minecraftVersion is null.");
            }

            JsonObject root = new JsonObject();
            root.add(Keys.CONFIG_FILE_VERSION, new JsonPrimitive(this.configFileVersion));
            root.add(Keys.VOXEL_LATEST_VERSION, new JsonPrimitive(this.voxelLatestVersion));
            root.add(Keys.VOXEL_REMAPPER_VERSION, new JsonPrimitive(this.voxelRemapperVersion));
            root.add(Keys.MINECRAFT_VERSION, new JsonPrimitive(this.minecraftVersion));
            return root;
        }

        public static class ConfigFactory {
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
                    configFactory.setVoxelLatestVersion(FabricLoader.getInstance().getModContainer(ModInfoUtil.VOXEL_LATEST).get().getMetadata().getVersion().getFriendlyString());
                    configFactory.setVoxelRemapperVersion(FabricLoader.getInstance().getModContainer(ModInfoUtil.VOXEL_REMAPPER).get().getMetadata().getVersion().getFriendlyString());
                    configFactory.setMinecraftVersion(ModInfoUtil.getMinecraftVersion());

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

    public static Config of(File configFile) {
        return parseConfigFile(configFile);
    }

    public static void writeTo(File configFile) {
        if (configFile.exists() && (!configFile.isFile() || !configFile.canWrite())) {
            throw new SecurityException(String.format("Failed to write to file \"%s\".", configFile.getAbsolutePath()));
        }

        byte[] data = Config.ConfigFactory.getCurrent().toJsonElement().toString().getBytes(StandardCharsets.UTF_8);

        try (FileOutputStream fileOutputStream = new FileOutputStream(configFile)) {
            fileOutputStream.write(data);
        } catch (IOException e) {
            throw new RuntimeException(String.format("An Error was thrown while writing data to \"%s\".", configFile.getAbsolutePath()),e);
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
            throw new RuntimeException(String.format("An Error was thrown while reading data from \"%s\".", configFile),e);
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
