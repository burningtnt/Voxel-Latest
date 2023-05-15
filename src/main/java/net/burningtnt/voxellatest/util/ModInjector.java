package net.burningtnt.voxellatest.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.discovery.ModCandidate;
import net.fabricmc.loader.impl.metadata.ContactInformationImpl;
import net.fabricmc.loader.impl.metadata.EntrypointMetadata;
import net.fabricmc.loader.impl.metadata.LoaderModMetadata;
import net.fabricmc.loader.impl.metadata.NestedJarEntry;
import net.fabricmc.loader.impl.util.DefaultLanguageAdapter;
import net.fabricmc.loader.impl.util.version.StringVersion;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ModInjector {
    private static Object getAuthorPersonObject() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> authorPersonClass = Class.forName("net.fabricmc.loader.impl.metadata.SimplePerson");
        Constructor<?> authorPersonConstructor = authorPersonClass.getDeclaredConstructor(String.class);
        authorPersonConstructor.setAccessible(true);
        return authorPersonConstructor.newInstance("MamiyaOtaru");
    }

    private static HashMap<String, String> getContactInformationMap() {
        HashMap<String, String> contactInformationHashMap = new HashMap<>();
        contactInformationHashMap.put("homepage", "https://minecraft.curseforge.com/projects/voxelmap");
        return contactInformationHashMap;
    }

    private static Object getIconEntrySingleObject() throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> iconEntrySingleClass = Class.forName("net.fabricmc.loader.impl.metadata.V1ModMetadata$Single");
        Constructor<?> iconEntrySingleConstructor = iconEntrySingleClass.getDeclaredConstructor(String.class);
        iconEntrySingleConstructor.setAccessible(true);
        return iconEntrySingleConstructor.newInstance("assets/voxelmap/icon.png");
    }

    private static HashMap<String, List<EntrypointMetadata>> getEntryPointMetaDataMap() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException {
        Class<?> entryPointMetaDataImplClass = Class.forName("net.fabricmc.loader.impl.metadata.V1ModMetadata$EntrypointMetadataImpl");
        Constructor<?> entryPointMetaDataConstructor = entryPointMetaDataImplClass.getDeclaredConstructor(String.class, String.class);
        entryPointMetaDataConstructor.setAccessible(true);
        Object entryPointMetaDataImplObject = entryPointMetaDataConstructor.newInstance("default", "com.mamiyaotaru.voxelmap.fabricmod.FabricModVoxelMap");
        HashMap<String, List<EntrypointMetadata>> entryPointMetaDataMap = new HashMap<>();
        entryPointMetaDataMap.put("client", new ArrayList<>(List.of((EntrypointMetadata) entryPointMetaDataImplObject)));
        return entryPointMetaDataMap;
    }

    private static HashMap<String, CustomValue> getCustomValueMap() {
        return new HashMap<>();
    }

    public static void run() {
        try {
            Object authorPersonObject = getAuthorPersonObject();
            HashMap<String, String> contactInformationHashMap = getContactInformationMap();
            Object iconEntrySingleObject = getIconEntrySingleObject();
            HashMap<String, List<EntrypointMetadata>> entryPointMetaDataMap = getEntryPointMetaDataMap();
            HashMap<String, CustomValue> customValueMap = getCustomValueMap();
            HashMap<String, String> languageAdapter = new HashMap<>();
            Class<?> v1ModMetadataClass = Class.forName("net.fabricmc.loader.impl.metadata.V1ModMetadata");
            Constructor<?> v1ModMetadataConstructor = v1ModMetadataClass.getDeclaredConstructor(
                    String.class, Version.class, Collection.class, ModEnvironment.class, Map.class, Collection.class,
                    Collection.class, String.class, Collection.class, boolean.class, String.class, String.class,
                    Collection.class, Collection.class, ContactInformation.class, Collection.class,
                    Class.forName("net.fabricmc.loader.impl.metadata.V1ModMetadata$IconEntry"), Map.class, Map.class
            );
            v1ModMetadataConstructor.setAccessible(true);
            Object v1ModMetadataObject = v1ModMetadataConstructor.newInstance(
                    ModInfoUtil.VOXEL_MAP, new StringVersion(String.format("1.10.15+%s", ModInfoUtil.VOXEL_LATEST)), new ArrayList<String>(), ModEnvironment.CLIENT, entryPointMetaDataMap, new ArrayList<NestedJarEntry>(),
                    new ArrayList<>(), null, new ArrayList<>(), false, "VoxelMap", "Minimap and world map",
                    new ArrayList<>(List.of(authorPersonObject)), new ArrayList<>(), new ContactInformationImpl(contactInformationHashMap), new ArrayList<>(),
                    iconEntrySingleObject, languageAdapter, customValueMap
            );

            Constructor<ModCandidate> modCandidateConstructor = ModCandidate.class.getDeclaredConstructor(List.class, String.class, long.class, LoaderModMetadata.class, boolean.class, Collection.class);
            modCandidateConstructor.setAccessible(true);
            ModCandidate modCandidateObject = modCandidateConstructor.newInstance(
                    new ArrayList<>(List.of(VoxelMapClassRemapUtil.getRemappedJarFile().toPath())),
                    VoxelMapClassRemapUtil.getRemappedJarFile().getAbsolutePath(),
                    (long) VoxelMapClassRemapUtil.getRemappedJarFile().hashCode(),
                    v1ModMetadataObject,
                    false,
                    new ArrayList<>()
            );

            ModContainerImpl modContainerImpl = new ModContainerImpl(modCandidateObject);

//            Field entrypointStorageField = FabricLoaderImpl.class.getDeclaredField("entrypointStorage");
//            entrypointStorageField.setAccessible(true);
//            EntrypointStorage entrypointStorageMap = (EntrypointStorage) entrypointStorageField.get(FabricLoader.getInstance());
//            if (entrypointStorageMap != null) {
//                entrypointStorageMap.add(modContainerImpl, "client", entryPointMetaDataMap.get("client").get(0), languageAdapter);
//            } else {
//                LoggerManagerUtil.warn("net.fabricmc.loader.impl.FabricLoaderImpl.modMap is null");
//            }

            Field modMapField = FabricLoaderImpl.class.getDeclaredField("modMap");
            modMapField.setAccessible(true);
            Map<String, ModContainerImpl> modMap = (Map<String, ModContainerImpl>) modMapField.get(FabricLoader.getInstance());
            if (modMap != null) {
                modMap.put(ModInfoUtil.VOXEL_MAP, modContainerImpl);
            } else {
                LoggerManagerUtil.warn("net.fabricmc.loader.impl.FabricLoaderImpl.modMap is null");
            }

            Field modsField = FabricLoaderImpl.class.getDeclaredField("mods");
            modsField.setAccessible(true);
            List<ModContainerImpl> mods = (List<ModContainerImpl>) modsField.get(FabricLoader.getInstance());
            if (mods != null) {
                mods.add(modContainerImpl);
            } else {
                LoggerManagerUtil.warn("net.fabricmc.loader.impl.FabricLoaderImpl.mods is null");
            }
        } catch (Throwable e) {
            throw new RuntimeException("An Error was thrown while adding VoxelMap to Fabric.", e);
        }
    }
}
