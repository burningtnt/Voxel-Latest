package net.burningtnt.voxellatest.asm;

import net.burningtnt.voxellatest.NamespaceManager;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CommonSuperClassManager {
    private static final Map<String, CommonSuperClassManager> cache = new HashMap<>();

    public static void putClass(ClassNode classNode) {
        if (!cache.containsKey(classNode.name)) {
            boolean isInterface = (classNode.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE;
            cache.put(classNode.name, new CommonSuperClassManager(classNode.name, classNode.superName, isInterface));
        }
    }

    public static String getCommonSuperClass(String className1, String className2) {
        CommonSuperClassManager instance1 = getByClassName(className1);
        CommonSuperClassManager instance2 = getByClassName(className2);
        if (instance1.isInterface || instance2.isInterface || instance1.className.equals(instance2.className)) {
            return "java/lang/Object";
        }
        while (true) {
            if (instance1.isSuperClassOf(instance2)) {
                return instance1.className;
            }
            if (instance1.className.equals("java/lang/Object") || instance1.superClassName == null) {
                return "java/lang/Object";
            }
            instance1 = getByClassName(instance1.superClassName);
        }
    }

    public static void cleanup() {
        cache.clear();
    }

    private static CommonSuperClassManager getByClassName(String className) {
        if (!cache.containsKey(className)) {
            if (className.startsWith("net/minecraft/")) {
                // Minecraft 类，不能调用 Class.forName 以避免类加载
                if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                    // Minecraft Yarn-named Class
                    byte[] data = null;
                    try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(className + ".class")) {
                        if (inputStream == null) {
                            throw new NullPointerException(String.format("Cannot read resource from \"%s.class\" because InputStream is null.", className));
                        }
                        data = inputStream.readAllBytes();
                    } catch (Throwable e) {
                        throw new RuntimeException(String.format("An Error was thrown while reading resource from \"%s.class\".", className),e);
                    }
                    ClassReader classReader = new ClassReader(data);
                    ClassNode classNode = new ClassNode();
                    classReader.accept(classNode, 0);
                    cache.put(className, new CommonSuperClassManager(className,classNode.superName,(classNode.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE));
                } else {
                    // Minecraft Intermediary-named Class
                    byte[] data = null;
                    String intermediaryClassName = NamespaceManager.mapClassName(NamespaceManager.MAPPING_INTERMEDIARY,className);
                    try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(intermediaryClassName + ".class")) {
                        if (inputStream == null) {
                            throw new NullPointerException(String.format("Cannot read resource from \"%s.class\" because InputStream is null.", intermediaryClassName));
                        }
                        data = inputStream.readAllBytes();
                    } catch (Throwable e) {
                        throw new RuntimeException(String.format("An Error was thrown while reading resource from \"%s.class\".", intermediaryClassName),e);
                    }
                    ClassReader classReader = new ClassReader(data);
                    ClassNode classNode = new ClassNode();
                    classReader.accept(classNode, 0);
                    String yarnSuperClassName = NamespaceManager.mapClassName(NamespaceManager.MAPPING_YARN,classNode.superName);
                    cache.put(className, new CommonSuperClassManager(className,yarnSuperClassName,(classNode.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE));
                }
            } else {
                Class<?> classInstance = null;
                try {
                    classInstance = Class.forName(className.replace('/', '.'), false, CommonSuperClassManager.class.getClassLoader());
                } catch (ClassNotFoundException e) {
                    throw new TypeNotPresentException(className.replace('/', '.'), e);
                }
                Class<?> superClass = classInstance.getSuperclass();
                cache.put(className, new CommonSuperClassManager(className, superClass == null ? null : superClass.getName().replace('.', '/'), classInstance.isInterface()));
            }
        }
        return cache.get(className);
    }

    private final String className;
    private final String superClassName;
    private final boolean isInterface;

    private CommonSuperClassManager(String className, String superClassName, boolean isInterface) {
        this.className = className;
        if (isInterface) {
            this.superClassName = "java/lang/Object";
        } else {
            this.superClassName = superClassName;
        }
        this.isInterface = isInterface;
    }

    private boolean isSuperClassOf(CommonSuperClassManager instance) {
        while (true) {
            if (this.className.equals(instance.className)) {
                return true;
            }
            if (instance.className.equals("java/lang/Object") || instance.superClassName == null) {
                return false;
            }
            instance = getByClassName(instance.superClassName);
        }
    }
}
