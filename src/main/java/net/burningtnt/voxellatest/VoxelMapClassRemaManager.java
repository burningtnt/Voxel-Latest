package net.burningtnt.voxellatest;

import net.burningtnt.voxellatest.asm.ASMRemapManager;
import net.burningtnt.voxellatest.asm.CommonSuperClassManager;
import net.burningtnt.voxellatest.util.*;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/*
 * 让 VoxelMap 在高版本可以运行！
 * Voxel Latest 1.0.0 正式发布，他可以让 Voxel Map 在 1.17+ 版本正常使用。
 * 用不惯 Xaero's Mini Map 的风格？没关系！换回 Voxel Map！
 * 使用方法：
 * 1 在 mods 文件夹内放入本 Mod。
 * 2 保持网络连接通畅，首次使用 Voxel Latest 时需要从互联网上下载数据并重映射 Voxel Map，这可能需要一段时间。
 * 3 尽情玩吧！
 */
public class VoxelMapClassRemaManager {
    private static boolean hasDone = false;

    public static void run() {
        if (hasDone) {
            return;
        }
        try {
            mapVoxelMapJarFile();
        } catch (Throwable e) {
            Logger.fail("An Error was thrown while remapping voxelmap", e);
            try {
                System.exit(-1);
            } catch (Throwable e2) {
                e2.addSuppressed(new RuntimeException("An Error was thrown while remapping voxelmap.", e));
                throw new RuntimeException("An Error was thrown while remapping voxelmap.", new RuntimeException("An Error was thrown while invoking System.exit .", e2));
            }
        }
        hasDone = true;

        try {
            Injector.injectToFabricLoader();
        } catch (Throwable e) {
            Logger.fail("An Error was thrown while injecting voxelmap", e);
            try {
                System.exit(-1);
            } catch (Throwable e2) {
                e2.addSuppressed(new RuntimeException("An Error was thrown while injecting voxelmap", e));
                throw new RuntimeException("An Error was thrown while injecting voxelmap", new RuntimeException("An Error was thrown while invoking System.exit .", e2));
            }
        }

        try {
            CommonSuperClassManager.cleanup();
        } catch (Throwable e) {
            Logger.fail("An Error was thrown while releasing memory of CommonSuperClassManager.", e);
        }

        try {
            NamespaceManager.cleanup();
        } catch (Throwable e) {
            Logger.fail("An Error was thrown while releasing memory of NamespaceManager.", e);
        }

        Logger.info("Remapping: All Finish");
    }

    private static void downloadVoxelMapRawJarFile(Path path) throws IOException {
        String url = "https://mediafilez.forgecdn.net/files/3345/206/fabricmod_VoxelMap-1.10.15_for_1.17.0.jar";

        Logger.info(String.format("Downloading voxelmap from \"%s\"", url));

        URL tinyUrl = new URL(url);
        HttpURLConnection request = (HttpURLConnection) tinyUrl.openConnection();
        request.setReadTimeout(10000);
        request.connect();
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            request.getInputStream().transferTo(outputStream);
        }
    }

    private static boolean shouldUpdateVoxelMapJarFileMapCache() throws IOException {
        if ("never".equals(System.getProperty("voxellatest.cache"))) {
            return true;
        }

        if (!Files.isDirectory(ModInfo.MOD_DIR)) {
            Logger.info("VoxelLatestFolder isn't a directory");
            return true;
        }

        Path configFile = ModInfo.VERSION_CONFIG_FILE;
        if (!Files.isRegularFile(configFile)) {
            Logger.info("ConfigFile isn't a File");
            return true;
        }

        if (isVoxellatestRemapperDeveloping()) {
            Logger.info("Mod voxellatest-remapper is developing. Force update");
            return true;
        }

        if (!Files.isRegularFile(ModInfo.VOXEL_MAP_REMAPPING_DONE)) {
            return true;
        }

        if (!Files.isRegularFile(ModInfo.VOXEL_MAP_RAW_FILE)) {
            return true;
        }

        return !ConfigFileManager.readFrom(configFile).isLatest();
    }

    private static void mapVoxelMapJarFile() throws IOException {
        if (!shouldUpdateVoxelMapJarFileMapCache()) {
            Logger.info("Find remapped.jar. Skip Remapping");
            return;
        }

        Logger.info("Remapping voxelmap.jar. This may cost a huge amount of time. Please wait patiently");

        if (!Files.isDirectory(ModInfo.MOD_DIR)) {
            Lang.delete(ModInfo.MOD_DIR);
            Files.createDirectory(ModInfo.MOD_DIR);
        }

        // ModInfo.getVoxelMapRawFile() -> ModInfo.getVoxelMapIntermediaryRemappingFile() -> ModInfo.getVoxelMapASMRemappingFile(), ModInfo.getVoxelMapRemappedFile()

        NamespaceManager.init();

        if (!Files.isRegularFile(ModInfo.VOXEL_MAP_RAW_FILE)) {
            Lang.delete(ModInfo.VOXEL_MAP_RAW_FILE);
            downloadVoxelMapRawJarFile(ModInfo.VOXEL_MAP_RAW_FILE);
        }

        // Remapping: 1.17 intermediary -> current yarn
        Logger.info(String.format("Remapping: %s %s -> %s %s", ModInfo.MINECRAFT_MOD.getMetadata().getVersion().getFriendlyString(), NamespaceManager.MAPPING_INTERMEDIARY, ModInfo.MINECRAFT_MOD.getMetadata().getVersion().getFriendlyString(), NamespaceManager.MAPPING_YARN));
        Lang.delete(ModInfo.VOXEL_MAP_REMAPPING_YARN_DOWN);
        NamespaceManager.run(NamespaceManager.MAPPING_INTERMEDIARY, NamespaceManager.MAPPING_YARN, ModInfo.VOXEL_MAP_RAW_FILE, ModInfo.VOXEL_MAP_REMAPPING_YARN_DOWN);

        // Remapping: ASM Remapping
        Logger.info("Remapping: ASM Remapping");
        Lang.delete(ModInfo.VOXEL_MAP_REMAPPING_ASM_DONE);
        remapByASM(ModInfo.VOXEL_MAP_REMAPPING_YARN_DOWN, ModInfo.VOXEL_MAP_REMAPPING_ASM_DONE);

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Logger.info(String.format("Remapping: Minecraft %s developing environment detected.", ModInfo.MINECRAFT_MOD.getMetadata().getVersion().getFriendlyString()));
            Lang.delete(ModInfo.VOXEL_MAP_REMAPPING_DONE);
            Files.copy(ModInfo.VOXEL_MAP_REMAPPING_ASM_DONE, ModInfo.VOXEL_MAP_REMAPPING_DONE);
        } else {
            // Remapping: current yarn -> current intermediary
            Logger.info(String.format("Remapping: %s %s -> %s %s", ModInfo.MINECRAFT_MOD.getMetadata().getVersion().getFriendlyString(), NamespaceManager.MAPPING_YARN, ModInfo.MINECRAFT_MOD.getMetadata().getVersion().getFriendlyString(), NamespaceManager.MAPPING_INTERMEDIARY));
            Lang.delete(ModInfo.VOXEL_MAP_REMAPPING_DONE);
            NamespaceManager.run(NamespaceManager.MAPPING_YARN, NamespaceManager.MAPPING_INTERMEDIARY, ModInfo.VOXEL_MAP_REMAPPING_ASM_DONE, ModInfo.VOXEL_MAP_REMAPPING_DONE);
        }

        Logger.info("Remapping: Finish");

        createConfigFile();
    }

    private static boolean isVoxellatestRemapperDeveloping() {
        if ("ignoreDeveloping".equals(System.getProperty("voxellatest.cache"))) {
            return false;
        }
        return Files.isDirectory(ModInfo.VOXEL_REMAPPER_MOD.getOrigin().getPaths().get(0));
    }


    private static void createConfigFile() throws IOException {
        if (isVoxellatestRemapperDeveloping()) {
            return;
        }

        ConfigFileManager.writeTo(ModInfo.VERSION_CONFIG_FILE);
    }

    private static void remapByASM(Path fromPath, Path toPath) throws IOException {
        ASMRemapManager.initRemappers();

        try (ZipFile voxelMapJarZipFile = new ZipFile(fromPath.toFile())) {
            {
                Enumeration<? extends ZipEntry> inputEntries = voxelMapJarZipFile.entries();
                while (inputEntries.hasMoreElements()) {
                    ZipEntry fileInside = inputEntries.nextElement();
                    if (Lang.getFileType(fileInside.getName()).equalsIgnoreCase("class")) {
                        try {
                            ClassNode classNode = new ClassNode();
                            new ClassReader(Lang.readAllBytesAndClose(voxelMapJarZipFile.getInputStream(fileInside))).accept(classNode, 0);
                            CommonSuperClassManager.putClass(classNode);
                        } catch (Throwable e) {
                            throw new RuntimeException(String.format("An Error was thrown while preparing remapping class \"%s\".", fileInside.getName()), e);
                        }
                    }
                }
            }

            try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(toPath))) {
                Enumeration<? extends ZipEntry> inputEntries = voxelMapJarZipFile.entries();
                while (inputEntries.hasMoreElements()) {
                    ZipEntry fileInside = inputEntries.nextElement();
                    if (fileInside.getName().equals("mixin.voxelmap.refmap.json") || fileInside.getName().equals("pack.mcmeta")) {
                        continue;
                    }

                    byte[] data = Lang.readAllBytesAndClose(voxelMapJarZipFile.getInputStream(fileInside));
                    if (Lang.getFileType(fileInside.getName()).equalsIgnoreCase("class")) {
                        try {
                            data = ASMRemapManager.remapClass(data, fileInside.getName());
                        } catch (Throwable e) {
                            throw new RuntimeException(String.format("Fail to remap class \"%s\".", fileInside.getName()), e);
                        }
                    }
                    outputStream.putNextEntry(new ZipEntry(fileInside.getName()));
                    outputStream.write(data);
                    outputStream.closeEntry();
                }

                outputStream.putNextEntry(new ZipEntry("mixin.voxelmap.refmap.json"));
                outputStream.write(ASMRemapManager.buildRefMap().getBytes(StandardCharsets.UTF_8));
                outputStream.closeEntry();
            }
        }
    }
}
