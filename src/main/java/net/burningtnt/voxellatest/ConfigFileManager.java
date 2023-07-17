package net.burningtnt.voxellatest;

import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;
import net.burningtnt.voxellatest.util.Lang;
import net.burningtnt.voxellatest.util.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class ConfigFileManager {
    private ConfigFileManager() {
    }

    private static class Keys {
        public static final String CONFIG_FILE_VERSION = "configFileVersion";
        public static final String VOXEL_LATEST_VERSION = "voxelLatestVersion";
        public static final String VOXEL_REMAPPER_VERSION = "voxelRemapperVersion;";
        public static final String MINECRAFT_VERSION = "minecraftVersion";

        public static final int CURRENT_CONFIG_FILE_VERSION = 1;
    }

    public static final class Config {
        private static final Config currentConfig = new Config(
                Keys.CURRENT_CONFIG_FILE_VERSION,
                ModInfo.VOXEL_LATEST_MOD.getMetadata().getVersion().getFriendlyString(),
                ModInfo.VOXEL_REMAPPER_MOD.getMetadata().getVersion().getFriendlyString(),
                ModInfo.MINECRAFT_MOD.getMetadata().getVersion().getFriendlyString()
        );

        @SerializedName(Keys.CONFIG_FILE_VERSION)
        private final int configFileVersion;

        @SerializedName(Keys.VOXEL_LATEST_VERSION)
        private final String voxelLatestVersion;

        @SerializedName(Keys.VOXEL_REMAPPER_VERSION)
        private final String voxelRemapperVersion;

        @SerializedName(Keys.MINECRAFT_VERSION)
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

        public String getMinecraftVersion() {
            return minecraftVersion;
        }

        public String getVoxelLatestVersion() {
            return voxelLatestVersion;
        }

        public String getVoxelRemapperVersion() {
            return voxelRemapperVersion;
        }

        public boolean isLatest() {
            if (configFileVersion != Keys.CURRENT_CONFIG_FILE_VERSION) {
                return false;
            }
            if (!Objects.equals(voxelLatestVersion, ModInfo.VOXEL_LATEST_MOD.getMetadata().getVersion().getFriendlyString())) {
                return false;
            }
            if (!Objects.equals(voxelRemapperVersion, ModInfo.VOXEL_REMAPPER_MOD.getMetadata().getVersion().getFriendlyString())) {
                return false;
            }
            if (!Objects.equals(minecraftVersion, ModInfo.MINECRAFT_MOD.getMetadata().getVersion().getFriendlyString())) {
                return false;
            }
            return true;
        }
    }

    public static void writeTo(Path configFile) throws IOException {
        Lang.delete(configFile);
        Files.writeString(configFile, ModInfo.GSON.toJson(Config.currentConfig));
    }

    public static Config readFrom(Path configFile) throws IOException {
        if (!Files.isReadable(configFile)) {
            return Config.currentConfig;
        }

        try {
            return ModInfo.GSON.fromJson(new String(Files.readAllBytes(configFile)), Config.class);
        } catch (JsonParseException e) {
            Logger.fail("Illegal config file.", e);
            return Config.currentConfig;
        }
    }
}
