package net.burningtnt.voxellatest.util;

import com.google.gson.*;
import net.burningtnt.voxellatest.managers.ConfigFileManager;
import net.burningtnt.voxellatest.managers.RemapConfigManager;
import net.burningtnt.voxellatest.mappers.ASMRemapManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import sun.misc.Unsafe;

import java.io.*;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("restriction")
/*
 * 让 VoxelMap 在高版本可以运行！
 * Voxel Latest 1.0.0 正式发布，他可以让 Voxel Map 在 1.17+ 版本正常使用。
 * 用不惯 Xaero's Mini Map 的风格？没关系！换回 Voxel Map！
 * 使用方法：
 * 1 在 mods 文件夹内放入本 Mod。
 * 2 在 voxelmap 文件夹内放入 1.17.1 版本的 Voxel Map。
 * 3 保持网络连接通畅，首次使用 Voxel Latest 时需要从互联网上下载数据并重映射 Voxel Map，这可能需要一段时间。
 * 4 尽情玩吧！
 * * 如果您使用过旧版本的 Voxel Latest，您需要删除游戏文件夹下的 voxellatest 文件夹以获取更新。
 */
public class VoxelMapClassRemapUtil {
    private static String currentMinecraftVersion = null;
    private static File voxelMapJarFile = null;

    private static File remappedJarFile = null;

    private static String currentMappingVersionCache = null;
    private static boolean hasDone = false;
    private static File minecraftIntermediaryFile = null;
    private static File minecraftYarnFile = null;

    public static void run() {
        if (hasDone) {
            return;
        }
        try {
            mapVoxelMapJarFile();
        } catch (Throwable e) {
            LoggerManagerUtil.fail("An Error was thrown while remapping voxelmap", e);
            try {
                System.exit(-1);
            } catch (Throwable e2) {
                e2.addSuppressed(new RuntimeException("An Error was thrown while remapping voxelmap.", e));
                throw new RuntimeException("An Error was thrown while remapping voxelmap.", new RuntimeException("An Error was thrown while invoking System.exit .", e2));
            }
        }
        hasDone = true;
        try {
            addToJVM();
        } catch (Throwable e) {
            LoggerManagerUtil.fail("An Error was thrown while injecting voxelmap", e);
            try {
                System.exit(-1);
            } catch (Throwable e2) {
                e2.addSuppressed(new RuntimeException("An Error was thrown while injecting voxelmap", e));
                throw new RuntimeException("An Error was thrown while injecting voxelmap", new RuntimeException("An Error was thrown while invoking System.exit .", e2));
            }
        }

        try {
            CommonSuperClassUtil.cleanup();
        } catch (Throwable e) {
            LoggerManagerUtil.fail("An Error was thrown while releasing memory of CommonSuperClassUtil.", e);
        }

        try {
            NamespaceUtil.cleanup();
        } catch (Throwable e) {
            LoggerManagerUtil.fail("An Error was thrown while releasing memory of NamespaceUtil.", e);
        }

        LoggerManagerUtil.info("Remapping: All Finish");
    }

    public static File getRemappedJarFile() {
        if (!hasDone) {
            throw new RuntimeException("Remapping isn't finished yet.");
        }
        return remappedJarFile;
    }

    private static void downloadVoxelMapJarFile() {
        FabricLoader fabricLoader = FabricLoader.getInstance();

        voxelMapJarFile = ConfigFileManager.RemapConfigFileManager.getResourceFile();

        String url = "https://mediafilez.forgecdn.net/files/3345/206/fabricmod_VoxelMap-1.10.15_for_1.17.0.jar";

        LoggerManagerUtil.info(String.format("Downloading voxelmap from \"%s\"", url));
        try {
            URL tinyUrl = new URL(url);
            HttpURLConnection request = (HttpURLConnection) tinyUrl.openConnection();
            request.setReadTimeout(10000);
            request.connect();
            try (FileOutputStream fileOutputStream = new FileOutputStream(voxelMapJarFile)) {
                request.getInputStream().transferTo(fileOutputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("An Error was thrown while reading data from \"%s\".", url), e);
        }
    }

    private static String getFileType(String filename) {
        String[] l = filename.split("\\.");
        if (l.length == 0) {
            return "";
        }
        return l[l.length - 1];
    }

    private static void mapVoxelMapJarFile() {
        FabricLoader fabricLoader = FabricLoader.getInstance();

        if (!RemapConfigManager.shouldRemapVoxelMap()) {
            LoggerManagerUtil.info("Find remapped.jar. Skip Remapping");
            return;
        }

        LoggerManagerUtil.info("Remapping voxelmap.jar. This may cost a huge amount of time. Please wait patiently");

        downloadVoxelMapJarFile();

        ModInfoUtil.getMinecraftVersion();
        getMappingVersion();
        getMinecraftFile();

        remappedJarFile = ConfigFileManager.RemapConfigFileManager.getRemapedFile();

        // Remapping...
        LoggerManagerUtil.info(String.format("Remapping: %s %s -> %s %s", ModInfoUtil.getMinecraftVersion(), NamespaceUtil.MAPPING_INTERMEDIARY, ModInfoUtil.getMinecraftVersion(), NamespaceUtil.MAPPING_YARN));
        invokeRemapper(NamespaceUtil.MAPPING_INTERMEDIARY, NamespaceUtil.MAPPING_YARN, voxelMapJarFile, remappedJarFile);

        LoggerManagerUtil.info(String.format("Remapping: %s %s -> %s %s", ModInfoUtil.getMinecraftVersion(), NamespaceUtil.MAPPING_YARN, ModInfoUtil.getMinecraftVersion(), NamespaceUtil.MAPPING_YARN));
        prepareRemapByASM();
        remapByASM();

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LoggerManagerUtil.info(String.format("Remapping: Minecraft %s developing environment detected.", ModInfoUtil.getMinecraftVersion()));
        } else {
            LoggerManagerUtil.info(String.format("Remapping: %s %s -> %s %s", ModInfoUtil.getMinecraftVersion(), NamespaceUtil.MAPPING_YARN, ModInfoUtil.getMinecraftVersion(), NamespaceUtil.MAPPING_INTERMEDIARY));

            String nextStepFileName = remappedJarFile.getAbsolutePath();
            nextStepFileName = nextStepFileName.substring(0, nextStepFileName.length() - getFileType(nextStepFileName).length() - 1) + "_INTERMEDIARY.jar";
            File nextStepFile = new File(nextStepFileName);
            invokeRemapper(NamespaceUtil.MAPPING_YARN, NamespaceUtil.MAPPING_INTERMEDIARY, remappedJarFile, nextStepFile);
            if (!remappedJarFile.delete()) {
                throw new SecurityException(String.format("Failed to delete file \"%s\"", remappedJarFile.getAbsolutePath()));
            }
            if (!nextStepFile.renameTo(remappedJarFile)) {
                throw new SecurityException(String.format("Failed to rename file \"%s\" to \"%s\"", nextStepFile.getAbsolutePath(), remappedJarFile.getAbsolutePath()));
            }
        }

        LoggerManagerUtil.info("Remapping: Finish");

        RemapConfigManager.writeCurrentConfig();
    }


    private static void getMinecraftFile() {

        LoggerManagerUtil.info("Getting minecraftintermediary file");
//        LoggerManagerUtil.warn("Starting using sun.misc.Unsafe package. JVM may crash");

        File currentMinecraftFile = FabricLoaderImpl.INSTANCE.getModContainer("minecraft").get().getRoot().toFile();
//        try {
//            // Get Unsafe instance
//            Unsafe theUnsafe = null;
//            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
//            theUnsafeField.setAccessible(true);
//            theUnsafe = (Unsafe) theUnsafeField.get(null);
//
//            // Get zipFileSystemInstance.zfpath
//            Object zipFileSystemInstance = FabricLoaderImpl.INSTANCE.getModContainer("minecraft").get().getRoot().getFileSystem();
//            Class<?> zipFileSystemClass = Class.forName("jdk.nio.zipfs.ZipFileSystem");
//            Field zfpathField = zipFileSystemClass.getDeclaredField("zfpath");
//
//            Path path = (Path) theUnsafe.getObject(zipFileSystemInstance, theUnsafe.objectFieldOffset(zfpathField));
//            currentMinecraftFile = path.toFile();
//        } catch (Throwable e) {
//            throw new RuntimeException("No matched client-intermediary.jar found.", e);
//        }
//        LoggerManagerUtil.info("Finish using sun.misc.Unsafe package. JVM won't crash");

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LoggerManagerUtil.info("Remapping: Prepare TinyRemapper in developing environment.");
            File intermediaryFileTemp = new File(FabricLoader.getInstance().getGameDir().toFile(), "voxellatest/minecraft-intermediary.jar");
            invokeRemapper(NamespaceUtil.MAPPING_YARN, NamespaceUtil.MAPPING_INTERMEDIARY, currentMinecraftFile, intermediaryFileTemp);
            minecraftIntermediaryFile = intermediaryFileTemp;
            minecraftYarnFile = currentMinecraftFile;
        } else {
            LoggerManagerUtil.info("Remapping: Prepare TinyRemapper in normal environment.");
            File yarnFileTemp = new File(FabricLoader.getInstance().getGameDir().toFile(), "voxellatest/minecraft-yarn.jar");
            invokeRemapper(NamespaceUtil.MAPPING_INTERMEDIARY, NamespaceUtil.MAPPING_YARN, currentMinecraftFile, yarnFileTemp);
            minecraftIntermediaryFile = currentMinecraftFile;
            minecraftYarnFile = yarnFileTemp;
        }
    }

    private static void addToJVM() {
        LoggerManagerUtil.info(String.format("Add \"%s\" to Fabric", remappedJarFile.getAbsolutePath()));
        FabricLauncherBase.getLauncher().addToClassPath(remappedJarFile.toPath());
//        Mixins.addConfiguration("mixin.voxelmap.json");
//        Delay the injection of mixin to avoid CME.
        ModInjector.run();
    }

    private static void invokeRemapper(String fromName, String toName, File fromFile, File toFile) {
        if (fromName.equals(NamespaceUtil.MAPPING_INTERMEDIARY) && minecraftIntermediaryFile != null) {
            try {
                TinyRemapperAgency.run(getRemapFile(), fromName, toName, fromFile, toFile, minecraftIntermediaryFile);
            } catch (Throwable t) {
                throw new RuntimeException("An Error was thrown while invoking Tiny Remapper.", t);
            }
        } else if (fromName.equals(NamespaceUtil.MAPPING_YARN) && minecraftYarnFile != null) {
            try {
                TinyRemapperAgency.run(getRemapFile(), fromName, toName, fromFile, toFile, minecraftYarnFile);
            } catch (Throwable t) {
                throw new RuntimeException("An Error was thrown while invoking Tiny Remapper.", t);
            }
        } else {
            try {
                TinyRemapperAgency.run(getRemapFile(), fromName, toName, fromFile, toFile, null);
            } catch (Throwable t) {
                throw new RuntimeException("An Error was thrown while invoking Tiny Remapper.", t);
            }
        }
//        ArrayList<String> tinyRemapperArgs = new ArrayList<>(List.of(fromFile.getAbsolutePath(), toFile.getAbsolutePath(), remapperFile.getAbsolutePath(), fromName, toName));
//        if (fromName.equals(NamespaceUtil.MAPPING_INTERMEDIARY) && minecraftIntermediaryFile != null) {
//            tinyRemapperArgs.add(minecraftIntermediaryFile.getAbsolutePath());
//        }
//        if (fromName.equals(NamespaceUtil.MAPPING_YARN) && minecraftYarnFile != null) {
//            tinyRemapperArgs.add(minecraftYarnFile.getAbsolutePath());
//        }
//        String[] args = new String[tinyRemapperArgs.size()];
//        tinyRemapperArgs.toArray(args);
//
//        SecurityManagerNoSystemExitUtil.enableCurrentSecurityManager();
//        LoggerManagerUtil.info("===== Here is the beginning of the output of Tiny Remapper =====");
//        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//        Thread.currentThread().setContextClassLoader(classLoader.getClass().getClassLoader());
//
//        try {
//            net.fabricmc.tinyremapper.Main.main(args);
//        } catch (Throwable t) {
//            Thread.currentThread().setContextClassLoader(classLoader);
//            SecurityManagerNoSystemExitUtil.disableCurrentSecurityManager();
//            LoggerManagerUtil.fail("An Error was thrown while invoking Lnet/fabricmc/tinyremapper/Main;main([Ljava/lang/String;)V", t);
//            System.exit(-1);
//        }
//
//        Thread.currentThread().setContextClassLoader(classLoader);
//        LoggerManagerUtil.info("===== Here is the ending of the output of Tiny Remapper =====");
//        SecurityManagerNoSystemExitUtil.disableCurrentSecurityManager();
    }

    public synchronized static File getRemapFile() {
        File tinyRemapFile = new File(ConfigFileManager.configRootDirectory, String.format("yarn-%s.tiny", ModInfoUtil.getMinecraftVersion()));

        if (!tinyRemapFile.exists()) {
            downloadMappingFile(tinyRemapFile);
        }
        NamespaceUtil.init(tinyRemapFile);
        return tinyRemapFile;
    }

    private static void downloadMappingFile(File tinyRemapFile) {
        String url = String.format("https://maven.fabricmc.net/net/fabricmc/yarn/%s/yarn-%s-v2.jar", currentMappingVersionCache, currentMappingVersionCache);
        LoggerManagerUtil.info(String.format("Downloading file from \"%s\"", url));
        byte[] data;
        try {
            URL tinyUrl = new URL(url);
            HttpURLConnection tinyRequest = (HttpURLConnection) tinyUrl.openConnection();
            tinyRequest.setReadTimeout(10000);
            tinyRequest.connect();
            try (ZipInputStream zipInputStream = new ZipInputStream(tinyRequest.getInputStream())) {
                boolean flag = false;
                while (true) {
                    ZipEntry fileInside = zipInputStream.getNextEntry();
                    if (fileInside == null) {
                        break;
                    }
                    if (fileInside.getName().equals("mappings/mappings.tiny")) {
                        try (FileOutputStream fileOutputStream = new FileOutputStream(tinyRemapFile)) {
                            zipInputStream.transferTo(fileOutputStream);
                            flag = true;
                        }
                        break;
                    }
                }
                if (!flag) {
                    throw new RuntimeException(String.format("Broken data from \"%s\".", url));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(String.format("An Error was thrown while reading data from \"%s\".", url), e);
        }
    }

    private static void getMappingVersion() {
        if (currentMappingVersionCache != null) {
            return;
        }
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LoggerManagerUtil.info("Getting mapping version from current develop environment");
            Path path = null;
            LoggerManagerUtil.warn("Starting using sun.misc.Unsafe package. JVM may crash");
            try {
                // Get Unsafe instance
                Unsafe theUnsafe = null;
                Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
                theUnsafeField.setAccessible(true);
                theUnsafe = (Unsafe) theUnsafeField.get(null);

                // Get zipFileSystemInstance.zfpath
                Object zipFileSystemInstance = FabricLoaderImpl.INSTANCE.getModContainer("minecraft").get().getRoot().getFileSystem();
                Class<?> zipFileSystemClass = Class.forName("jdk.nio.zipfs.ZipFileSystem");
                Field zfpathField = zipFileSystemClass.getDeclaredField("zfpath");

                path = (Path) theUnsafe.getObject(zipFileSystemInstance, theUnsafe.objectFieldOffset(zfpathField));
            } catch (Throwable e) {
                throw new RuntimeException("No matched client-intermediary.jar found.", e);
            }
            LoggerManagerUtil.info("Finish using sun.misc.Unsafe package. JVM won't crash");
            String pathName = path.toFile().getParentFile().getName();
            int start = -1;
            for (int i = 0; i < 4; i++) {
                for (int j = start + 1; j < pathName.length(); j++) {
                    if (pathName.charAt(j) == '.') {
                        start = j;
                        break;
                    }
                }
            }
            currentMappingVersionCache = pathName.substring(start + 1, pathName.length() - "-v2".length());
        } else {
            LoggerManagerUtil.info("Getting mapping version from \"https://meta.fabricmc.net/v2/versions/yarn\"");
            JsonElement jsonElement;
            try {
                URL versionUrl = new URL("https://meta.fabricmc.net/v2/versions/yarn");
                HttpURLConnection versionRequest = (HttpURLConnection) versionUrl.openConnection();
                versionRequest.setReadTimeout(10000);
                versionRequest.connect();
                InputStream inputStream = versionRequest.getInputStream();
                jsonElement = new Gson().fromJson(new String(inputStream.readAllBytes()), JsonElement.class);
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(String.format("An Error was thrown while reading data from \"%s\".", "https://meta.fabricmc.net/v2/versions/yarn"), e);
            }
            try {
                JsonArray jsonArray = jsonElement.getAsJsonArray();
                int currentBuildVersion = -1;
                String path = null;
                for (int i = 0; i < jsonArray.size(); i++) {
                    String itemGameVersion = jsonArray.get(i).getAsJsonObject().get("gameVersion").getAsString();
                    if (itemGameVersion.equals(ModInfoUtil.getMinecraftVersion())) {
                        if (currentBuildVersion < jsonArray.get(i).getAsJsonObject().get("build").getAsInt()) {
                            currentBuildVersion = jsonArray.get(i).getAsJsonObject().get("build").getAsInt();
                            path = jsonArray.get(i).getAsJsonObject().get("version").getAsString();
                        }
                    }
                }
                if (currentBuildVersion == -1) {
                    throw new RuntimeException(String.format("No matching yarn version found for minecarft \"%s\".", ModInfoUtil.getMinecraftVersion()));
                }
                currentMappingVersionCache = path;
            } catch (Throwable e) {
                throw new RuntimeException("Broken data from \"https://meta.fabricmc.net/v2/versions/yarn\".", e);
            }
        }
    }

    private static void prepareRemapByASM() {
        ASMRemapManager.initRemappers();

        try (ZipFile voxelMapJarZipFile = new ZipFile(remappedJarFile)) {
            Enumeration<? extends ZipEntry> inputEntries = voxelMapJarZipFile.entries();
            while (inputEntries.hasMoreElements()) {
                ZipEntry fileInside = inputEntries.nextElement();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(voxelMapJarZipFile.getInputStream(fileInside));
                byte[] data = bufferedInputStream.readAllBytes();
                bufferedInputStream.close();
                if (getFileType(fileInside.getName()).equalsIgnoreCase("class")) {
                    try {
                        ClassReader classReader = new ClassReader(data);
                        ClassNode classNode = new ClassNode();
                        classReader.accept(classNode, 0);
                        CommonSuperClassUtil.putClass(classNode);
                    } catch (RuntimeException e) {
                        throw new RuntimeException(String.format("An Error was thron while preparing remapping class \"%s\".", fileInside.getName()), e);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("An Error was thrown while preparing remapping voxelmap jar file.", e);
        }
    }

    private static void remapByASM() {
        ZipOutputStream outputStream = null;
        StringBuilder nextStepFileCacheName = new StringBuilder(remappedJarFile.getAbsolutePath());
        int offset = nextStepFileCacheName.length() - getFileType(remappedJarFile.getAbsolutePath()).length() - 1;
        nextStepFileCacheName.insert(offset, "_ASM_REMAPPING");
        File nextStepFileCache = new File(nextStepFileCacheName.toString());
        try {
            outputStream = new ZipOutputStream(new FileOutputStream(nextStepFileCache));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(String.format("An Error was thrown while creating a ZipOutputStream for \"%s\".", remappedJarFile.getAbsolutePath()), e);
        }
        try (ZipFile voxelMapJarZipFile = new ZipFile(remappedJarFile)) {
            Enumeration<? extends ZipEntry> inputEntries = voxelMapJarZipFile.entries();
            while (inputEntries.hasMoreElements()) {
                ZipEntry fileInside = inputEntries.nextElement();
                if (fileInside.getName().equals("mixin.voxelmap.refmap.json") || fileInside.getName().equals("pack.mcmeta")) {
                    continue;
                }
                BufferedInputStream bufferedInputStream = new BufferedInputStream(voxelMapJarZipFile.getInputStream(fileInside));
                byte[] data = bufferedInputStream.readAllBytes();
                bufferedInputStream.close();
                if (getFileType(fileInside.getName()).equalsIgnoreCase("class")) {
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
        } catch (IOException e) {
            throw new RuntimeException("An Error was thrown while remapping voxelmap jar file.", e);
        }
        try {
            outputStream.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException(String.format("An Error was thrown while closing \"%s\".", nextStepFileCache.getAbsolutePath()), e);
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(String.format("An Error was thrown while closing \"%s\".", nextStepFileCache.getAbsolutePath()), e);
        }
        if (!remappedJarFile.delete()) {
            throw new SecurityException(String.format("An Error was thrown while deleting \"%s\"", remappedJarFile.getAbsolutePath()));
        }
        if (!nextStepFileCache.renameTo(remappedJarFile)) {
            throw new SecurityException(String.format("An Error was thrown while renaming \"%s\" to \"%s\"", nextStepFileCache.getAbsolutePath(), remappedJarFile.getAbsolutePath()));
        }
    }

    private static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File fileInside : files) {
                    deleteFile(fileInside);
                }
            }
        }
        if (!file.delete()) {
            throw new SecurityException(String.format("Cannot delete file or directory at \"%s\".", file.getAbsolutePath()));
        }
    }
}
