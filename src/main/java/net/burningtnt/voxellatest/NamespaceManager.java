package net.burningtnt.voxellatest;

import net.burningtnt.voxellatest.util.Logger;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import net.fabricmc.tinyremapper.NonClassCopyMode;
import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.function.Supplier;

public final class NamespaceManager {
    private NamespaceManager() {
    }

    public static final String MAPPING_INTERMEDIARY = "intermediary";
    public static final String MAPPING_YARN = "named";

    private static MappingResolver yarnMappingResolver = null;
    private static MappingResolver intermediaryMappingResolver = null;
    private static Path minecraftIntermediaryFile = null;
    private static Path minecraftYarnFile = null;

    public static String getCurrentNamespace() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return MAPPING_YARN;
        }
        return MAPPING_INTERMEDIARY;
    }

    public static InputStream getMappingInputStream() {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("voxellatest/mappings.tiny");
    }

    public static BufferedReader getMappingBufferedReader() {
        return new BufferedReader(new InputStreamReader(getMappingInputStream()));
    }

    public static void cleanup() {
        yarnMappingResolver = null;
        intermediaryMappingResolver = null;
    }

    @SuppressWarnings("unchecked")
    public static void init() throws IOException {
        try {
            Class<? extends MappingResolver> mappingResolverImpl = (Class<? extends MappingResolver>) Class.forName("net.fabricmc.loader.impl.MappingResolverImpl");
            Constructor<? extends MappingResolver> constructor = mappingResolverImpl.getDeclaredConstructor(Supplier.class, String.class);
            constructor.setAccessible(true);
            yarnMappingResolver = constructor.newInstance((Supplier<TinyTree>) () -> {
                try (BufferedReader reader = getMappingBufferedReader()) {
                    return TinyMappingFactory.loadWithDetection(reader);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, MAPPING_YARN);
            intermediaryMappingResolver = constructor.newInstance((Supplier<TinyTree>) () -> {
                try (BufferedReader reader = getMappingBufferedReader()) {
                    return TinyMappingFactory.loadWithDetection(reader);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, MAPPING_INTERMEDIARY);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }

        Logger.info("Getting minecraftintermediary file");

        Path currentMinecraftFile = ModInfo.MINECRAFT_MOD.getOrigin().getPaths().get(0);

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Logger.info("Remapping: Prepare TinyRemapper in developing environment.");
            minecraftYarnFile = currentMinecraftFile;
            minecraftIntermediaryFile = ModInfo.MOD_DIR.resolve("minecraft-intermediary.jar");

            remapJar(NamespaceManager.MAPPING_YARN, NamespaceManager.MAPPING_INTERMEDIARY, currentMinecraftFile, minecraftIntermediaryFile, null);
        } else {
            Logger.info("Remapping: Prepare TinyRemapper in normal environment.");
            minecraftYarnFile = ModInfo.MOD_DIR.resolve("minecraft-yarn.jar");
            minecraftIntermediaryFile = currentMinecraftFile;

            remapJar(NamespaceManager.MAPPING_INTERMEDIARY, NamespaceManager.MAPPING_YARN, currentMinecraftFile, minecraftYarnFile, null);
        }
    }

    public static MappingResolver getYarnMappingResolver() {
        if (yarnMappingResolver == null) {
            throw new RuntimeException("net.burningtnt.voxellatest.NamespaceManager.yarnMappingResolver has not init.");
        }
        return yarnMappingResolver;
    }

    public static MappingResolver getIntermediaryMappingResolver() {
        if (intermediaryMappingResolver == null) {
            throw new RuntimeException("net.burningtnt.voxellatest.NamespaceManager.intermediaryMappingResolver has not init.");
        }
        return intermediaryMappingResolver;
    }

    public static Path getMinecraftIntermediaryFile() {
        return minecraftIntermediaryFile;
    }

    public static Path getMinecraftYarnFile() {
        return minecraftYarnFile;
    }

    public static String mapClassName(String targetNamespace, String className) {

        String res = NamespaceManager.getYarnMappingResolver().unmapClassName(
                targetNamespace,
                className.replace('/', '.')
        ).replace('.', '/');
        if (!res.equals(className)) {
            return res;
        }

        res = NamespaceManager.getIntermediaryMappingResolver().unmapClassName(
                targetNamespace,
                className.replace('/', '.')
        ).replace('.', '/');

        return res;
    }

    public static String mapMethodName(String targetNamespace, String owner, String name, String desc) {
        String res = NamespaceManager.getYarnMappingResolver().mapMethodName(
                targetNamespace.equals(NamespaceManager.MAPPING_YARN) ? NamespaceManager.MAPPING_INTERMEDIARY : NamespaceManager.MAPPING_YARN,
                owner.replace('/', '.'), name, desc
        );
        if (!res.equals(name)) {
            return res;
        }

        res = NamespaceManager.getIntermediaryMappingResolver().mapMethodName(
                targetNamespace.equals(NamespaceManager.MAPPING_YARN) ? NamespaceManager.MAPPING_INTERMEDIARY : NamespaceManager.MAPPING_YARN,
                owner.replace('/', '.'), name, desc
        );

        return res;
    }

    public static String mapDesc(String targetNamespace, String desc) {
        int index = 0;

        loopLabel:
        while (true) {
            int start;
            for (start = index; true; start++) {
                if (start >= desc.length()) {
                    break loopLabel;
                }
                if (desc.charAt(start) == 'L') {
                    break;
                }
            }
            int end;
            for (end = start + 1; end < desc.length(); end++) {
                if (desc.charAt(end) == ';') {
                    break;
                }
            }
            String className = desc.substring(start + 1, end);
            className = mapClassName(targetNamespace, className);
            desc = desc.substring(0, start + 1) + className + desc.substring(end);
            index = start + className.length() + 2;
        }
        return desc;
    }

    public static void remapJar(String fromName, String toName, Path fromPath, Path toPath) throws IOException {
        remapJar(fromName, toName, fromPath, toPath, fromName.equals(MAPPING_YARN) ? getMinecraftYarnFile() : getMinecraftIntermediaryFile());
    }

    public static void remapJar(String fromName, String toName, Path fromPath, Path toPath, Path sourceFile) throws IOException {
        Logger.info(String.format(
                "Remap file from namespace \"%s\" to namespace \"%s\" with tiny-remapper from path \"%s\" to path \"%s\" with Tiny Remapper",
                fromName, toName, fromPath, toPath
        ));

        Path[] sourcePath = sourceFile != null ? new Path[]{sourceFile} : new Path[]{};
        TinyRemapper remapper = TinyRemapper.newRemapper()
                .withMappings(TinyUtils.createTinyMappingProvider(getMappingBufferedReader(), fromName, toName))
                .ignoreFieldDesc(false)
                .withForcedPropagation(Collections.emptySet())
                .propagatePrivate(false)
                .propagateBridges(TinyRemapper.LinkedMethodPropagation.DISABLED)
                .removeFrames(false)
                .ignoreConflicts(false)
                .checkPackageAccess(false)
                .fixPackageAccess(false)
                .resolveMissing(false)
                .rebuildSourceFilenames(false)
                .skipLocalVariableMapping(false)
                .renameInvalidLocals(false)
                .invalidLvNamePattern(null)
                .threads(-1)
                .build();

        try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(toPath).build()) {
            outputConsumer.addNonClassFiles(fromPath, NonClassCopyMode.FIX_META_INF, remapper);

            remapper.readInputs(fromPath);
            remapper.readClassPath(sourcePath);

            remapper.apply(outputConsumer);
        } finally {
            remapper.finish();
        }
    }
}
