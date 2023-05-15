package net.burningtnt.voxellatest.util;

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

public class NamespaceUtil {
    public static String MAPPING_INTERMEDIARY = "intermediary";
    public static String MAPPING_YARN = "named";

    private static MappingResolver yarnMappingResolver = null;
    private static MappingResolver intermediaryMappingResolver = null;
    private static File minecraftIntermediaryFile = null;
    private static File minecraftYarnFile = null;

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

    public static void init() {
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

        LoggerManagerUtil.info("Getting minecraftintermediary file");

        File currentMinecraftFile = FabricLoader.getInstance().getModContainer(ModInfoUtil.MINECRAFT).get().getOrigin().getPaths().get(0).toFile();

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LoggerManagerUtil.info("Remapping: Prepare TinyRemapper in developing environment.");
            minecraftYarnFile = currentMinecraftFile;
            minecraftIntermediaryFile = new File(ModInfoUtil.getModDir(), "minecraft-intermediary.jar");

            run(NamespaceUtil.MAPPING_YARN, NamespaceUtil.MAPPING_INTERMEDIARY, currentMinecraftFile, minecraftIntermediaryFile, null);
        } else {
            LoggerManagerUtil.info("Remapping: Prepare TinyRemapper in normal environment.");
            minecraftYarnFile = new File(ModInfoUtil.getModDir(), "minecraft-yarn.jar");
            minecraftIntermediaryFile = currentMinecraftFile;

            run(NamespaceUtil.MAPPING_INTERMEDIARY, NamespaceUtil.MAPPING_YARN, currentMinecraftFile, minecraftYarnFile, null);
        }
    }

    public static MappingResolver getYarnMappingResolver() {
        if (yarnMappingResolver == null) {
            throw new RuntimeException("net.burningtnt.voxellatest.util.NamespaceUtil.yarnMappingResolver has not init.");
        }
        return yarnMappingResolver;
    }

    public static MappingResolver getIntermediaryMappingResolver() {
        if (intermediaryMappingResolver == null) {
            throw new RuntimeException("net.burningtnt.voxellatest.util.NamespaceUtil.intermediaryMappingResolver has not init.");
        }
        return intermediaryMappingResolver;
    }

    public static File getMinecraftIntermediaryFile() {
        return minecraftIntermediaryFile;
    }

    public static File getMinecraftYarnFile() {
        return minecraftYarnFile;
    }

    public static String mapClassName(String targetNamespace, String className) {

        String res = NamespaceUtil.getYarnMappingResolver().unmapClassName(
                targetNamespace,
                className.replace('/', '.')
        ).replace('.', '/');
        if (!res.equals(className)) {
            return res;
        }

        res = NamespaceUtil.getIntermediaryMappingResolver().unmapClassName(
                targetNamespace,
                className.replace('/', '.')
        ).replace('.', '/');

        return res;
    }

    public static String mapMethodName(String targetNamespace, String owner, String name, String desc) {
        String res = NamespaceUtil.getYarnMappingResolver().mapMethodName(
                targetNamespace.equals(NamespaceUtil.MAPPING_YARN) ? NamespaceUtil.MAPPING_INTERMEDIARY : NamespaceUtil.MAPPING_YARN,
                owner.replace('/', '.'), name, desc
        );
        if (!res.equals(name)) {
            return res;
        }

        res = NamespaceUtil.getIntermediaryMappingResolver().mapMethodName(
                targetNamespace.equals(NamespaceUtil.MAPPING_YARN) ? NamespaceUtil.MAPPING_INTERMEDIARY : NamespaceUtil.MAPPING_YARN,
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

    public static void run(String fromName, String toName, File fromFile, File toFile) {
        run(fromName, toName, fromFile, toFile, fromName.equals(MAPPING_YARN) ? getMinecraftYarnFile() : getMinecraftIntermediaryFile());
    }

    public static void run(String fromName, String toName, File fromFile, File toFile, File sourceFile) {
        LoggerManagerUtil.info(String.format(
                "Remap file from namespace \"%s\" to namespace \"%s\" with tiny-remapper from path \"%s\" to path \"%s\" with Tiny Remapper",
                fromName, toName, fromFile.getAbsolutePath(), toFile.getAbsolutePath()
        ));
        TinyRemapper.Builder builder = TinyRemapper.newRemapper()
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
                .threads(-1);
        TinyRemapper remapper = builder.build();

        Path[] sourcePath = sourceFile != null ? new Path[]{sourceFile.toPath()} : new Path[]{};

        try (OutputConsumerPath outputConsumer = new OutputConsumerPath.Builder(toFile.toPath()).build()) {
            outputConsumer.addNonClassFiles(fromFile.toPath(), NonClassCopyMode.FIX_META_INF, remapper);

            remapper.readInputs(fromFile.toPath());
            remapper.readClassPath(sourcePath);

            remapper.apply(outputConsumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            remapper.finish();
        }
    }
}
