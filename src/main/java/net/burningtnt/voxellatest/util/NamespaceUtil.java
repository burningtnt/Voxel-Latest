package net.burningtnt.voxellatest.util;

import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

public class NamespaceUtil {
    public static String MAPPING_INTERMEDIARY = "intermediary";
    public static String MAPPING_YARN = "named";

    private static MappingResolver yarnMappingResolver = null;
    private static MappingResolver intermediaryMappingResolver = null;

    public static String getCurrentNamespace() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return MAPPING_YARN;
        }
        return MAPPING_INTERMEDIARY;
    }

    public static void cleanup() {
        yarnMappingResolver = null;
        intermediaryMappingResolver = null;
    }

    public static void init(File tinyFile) {
        try {
            Class<? extends MappingResolver> mappingResolverImpl = (Class<? extends MappingResolver>) Class.forName("net.fabricmc.loader.impl.MappingResolverImpl");
            Constructor<? extends MappingResolver> constructor = mappingResolverImpl.getDeclaredConstructor(Supplier.class, String.class);
            constructor.setAccessible(true);
            yarnMappingResolver = constructor.newInstance((Supplier<TinyTree>) () -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tinyFile)))) {
                    return TinyMappingFactory.loadWithDetection(reader);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, MAPPING_YARN);
            intermediaryMappingResolver = constructor.newInstance((Supplier<TinyTree>) () -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tinyFile)))) {
                    return TinyMappingFactory.loadWithDetection(reader);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, MAPPING_INTERMEDIARY);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 InstantiationException e) {
            throw new RuntimeException(e);
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

    public static String mapClassName(String targetNamespace, String className) {

        String res = NamespaceUtil.getYarnMappingResolver().unmapClassName(
                targetNamespace,
                className.replace('/', '.')
        ).replace('.', '/');
        if (!res.equals(className)) {
//            LoggerManagerUtil.info(String.format("Remap reference \"%s\" to namespace \"%s\" as \"%s\"", className, targetNamespace, res));
            return res;
        }

        res = NamespaceUtil.getIntermediaryMappingResolver().unmapClassName(
                targetNamespace,
                className.replace('/', '.')
        ).replace('.', '/');
        if (!res.equals(className)) {
//            LoggerManagerUtil.info(String.format("Remap reference \"%s\" to namespace \"%s\" as \"%s\"", className, targetNamespace, res));
            return res;
        }

//        LoggerManagerUtil.warn(String.format("Fail to remap reference \"%s\" to namespace \"%s\"", className, targetNamespace));
        return res;
    }

    public static String mapMethodName(String targetNamespace, String owner, String name, String desc) {
        String res = NamespaceUtil.getYarnMappingResolver().mapMethodName(
                targetNamespace.equals(NamespaceUtil.MAPPING_YARN) ? NamespaceUtil.MAPPING_INTERMEDIARY : NamespaceUtil.MAPPING_YARN,
                owner.replace('/','.'), name, desc
        );
        if (!res.equals(name)) {
//            LoggerManagerUtil.info(String.format(
//                    "Remap reference \"%s#%s%s\" to namespace \"%s\" as \"%s#%s%s\"",
//                    owner, name, desc, targetNamespace, owner, res, desc
//            ));
            return res;
        }

        res = NamespaceUtil.getIntermediaryMappingResolver().mapMethodName(
                targetNamespace.equals(NamespaceUtil.MAPPING_YARN) ? NamespaceUtil.MAPPING_INTERMEDIARY : NamespaceUtil.MAPPING_YARN,
                owner.replace('/','.'), name, desc
        );
        if (!res.equals(name)) {
//            LoggerManagerUtil.info(String.format(
//                    "Remap reference \"%s#%s%s\" to namespace \"%s\" as \"%s#%s%s\"",
//                    owner, name, desc, targetNamespace, owner, res, desc
//            ));
            return res;
        }

//        LoggerManagerUtil.warn(String.format(
//                "Fail to remap reference \"%s#%s%s\" to namespace \"%s\"",
//                owner, name, desc, targetNamespace
//        ));
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
}
