package net.burningtnt.voxellatest;

import net.burningtnt.voxellatest.util.Logger;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ContactInformation;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModEnvironment;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.fabricmc.loader.impl.discovery.ModCandidate;
import net.fabricmc.loader.impl.entrypoint.EntrypointStorage;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
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

public final class Injector {
    private Injector() {
    }

    private static ArrayList<Object> getAuthorPersonObject() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> authorPersonClass = Class.forName("net.fabricmc.loader.impl.metadata.SimplePerson");
        Constructor<?> authorPersonConstructor = authorPersonClass.getDeclaredConstructor(String.class);
        authorPersonConstructor.setAccessible(true);
        return new ArrayList<>(List.of(
                authorPersonConstructor.newInstance("MamiyaOtaru"),
                authorPersonConstructor.newInstance("Burning_TNT (Injected by Voxel Latest)")
        ));
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

    private static HashMap<String, LanguageAdapter> getLanguageAdapter() {
        HashMap<String, LanguageAdapter> map = new HashMap<>();
        map.put("default", DefaultLanguageAdapter.INSTANCE);
        return map;
    }

    public static void injectToFabricLoader() {
        Logger.info(String.format("Add \"%s\" to Fabric", ModInfo.VOXEL_MAP_REMAPPING_DONE));
        FabricLauncherBase.getLauncher().addToClassPath(ModInfo.VOXEL_MAP_REMAPPING_DONE);

        try {
            ArrayList<Object> authorPersonObject = getAuthorPersonObject();
            HashMap<String, String> contactInformationHashMap = getContactInformationMap();
            Object iconEntrySingleObject = getIconEntrySingleObject();
            HashMap<String, List<EntrypointMetadata>> entryPointMetaDataMap = getEntryPointMetaDataMap();
            HashMap<String, CustomValue> customValueMap = getCustomValueMap();
            HashMap<String, LanguageAdapter> languageAdapter = getLanguageAdapter();
            Class<?> v1ModMetadataClass = Class.forName("net.fabricmc.loader.impl.metadata.V1ModMetadata");
            Constructor<?> v1ModMetadataConstructor = v1ModMetadataClass.getDeclaredConstructor(
                    String.class, Version.class, Collection.class, ModEnvironment.class, Map.class, Collection.class,
                    Collection.class, String.class, Collection.class, boolean.class, String.class, String.class,
                    Collection.class, Collection.class, ContactInformation.class, Collection.class,
                    Class.forName("net.fabricmc.loader.impl.metadata.V1ModMetadata$IconEntry"), Map.class, Map.class
            );
            v1ModMetadataConstructor.setAccessible(true);
            Object v1ModMetadataObject = v1ModMetadataConstructor.newInstance(
                    ModInfo.VOXEL_MAP, new StringVersion(String.format("1.10.15+%s-%s+%s-%s", ModInfo.VOXEL_LATEST, ModInfo.VOXEL_LATEST_MOD.getMetadata().getVersion().getFriendlyString(), ModInfo.VOXEL_REMAPPER, ModInfo.VOXEL_REMAPPER_MOD.getMetadata().getVersion().getFriendlyString())), new ArrayList<String>(), ModEnvironment.CLIENT, entryPointMetaDataMap, new ArrayList<NestedJarEntry>(),
                    new ArrayList<>(), null, new ArrayList<>(), false, "VoxelMap (Upgraded by Voxel Latest)", "Minimap and world map",
                    authorPersonObject, new ArrayList<>(), new ContactInformationImpl(contactInformationHashMap), new ArrayList<>(),
                    iconEntrySingleObject, languageAdapter, customValueMap
            );

            Constructor<ModCandidate> modCandidateConstructor = ModCandidate.class.getDeclaredConstructor(List.class, String.class, long.class, LoaderModMetadata.class, boolean.class, Collection.class);
            modCandidateConstructor.setAccessible(true);
            ModCandidate modCandidateObject = modCandidateConstructor.newInstance(
                    new ArrayList<>(List.of(ModInfo.VOXEL_MAP_REMAPPING_DONE)),
                    ModInfo.VOXEL_MAP_REMAPPING_DONE.toString(),
                    (long) ModInfo.VOXEL_MAP_REMAPPING_DONE.hashCode(),
                    v1ModMetadataObject,
                    false,
                    new ArrayList<>()
            );

            ModContainerImpl modContainerImpl = new ModContainerImpl(modCandidateObject);

            Field entrypointStorageField = FabricLoaderImpl.class.getDeclaredField("entrypointStorage");
            entrypointStorageField.setAccessible(true);
            EntrypointStorage entrypointStorageMap = (EntrypointStorage) entrypointStorageField.get(FabricLoader.getInstance());
            if (entrypointStorageMap != null) {
                entrypointStorageMap.add(modContainerImpl, "client", entryPointMetaDataMap.get("client").get(0), languageAdapter);
            } else {
                Logger.warn("net.fabricmc.loader.impl.FabricLoaderImpl.modMap is null");
            }

            Field modMapField = FabricLoaderImpl.class.getDeclaredField("modMap");
            modMapField.setAccessible(true);
            Map<String, ModContainerImpl> modMap = (Map<String, ModContainerImpl>) modMapField.get(FabricLoader.getInstance());
            if (modMap != null) {
                modMap.put(ModInfo.VOXEL_MAP, modContainerImpl);
            } else {
                Logger.warn("net.fabricmc.loader.impl.FabricLoaderImpl.modMap is null");
            }

            Field modsField = FabricLoaderImpl.class.getDeclaredField("mods");
            modsField.setAccessible(true);
            List<ModContainerImpl> mods = (List<ModContainerImpl>) modsField.get(FabricLoader.getInstance());
            if (mods != null) {
                mods.add(modContainerImpl);
            } else {
                Logger.warn("net.fabricmc.loader.impl.FabricLoaderImpl.mods is null");
            }
        } catch (Throwable e) {
            throw new RuntimeException("An Error was thrown while adding VoxelMap to Fabric.", e);
        }
    }
}
