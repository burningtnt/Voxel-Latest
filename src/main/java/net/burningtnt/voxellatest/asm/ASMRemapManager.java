package net.burningtnt.voxellatest.asm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.burningtnt.voxellatest.util.Logger;
import net.burningtnt.voxellatest.NamespaceManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.injection.struct.TargetNotSupportedException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.ToIntFunction;

public final class ASMRemapManager {
    private ASMRemapManager() {
    }

    private static final Map<String, List<AbstractVoxelMapClassMapper>> remappers = new HashMap<>();

    private static final List<AbstractVoxelMapInsnWatcher> beforeWatchers = new ArrayList<>();

    private static final List<AbstractVoxelMapInsnWatcher> afterWatchers = new ArrayList<>();

    private static final JsonObject refmapJsonRoot = new JsonObject();

    private static boolean isExtendsFrom(Class<?> currentClass, Class<?> superClass) {
        if (currentClass.getSuperclass() == null) {
            return false;
        }
        if (currentClass.getSuperclass().equals(superClass)) {
            return true;
        }
        return isExtendsFrom(currentClass.getSuperclass(), superClass);
    }

    public static void initRemappers() {
        Optional<ModContainer> optional = FabricLoader.getInstance().getModContainer("voxellatest-remapper");
        if (optional.isEmpty()) {
            throw new RuntimeException(new ClassNotFoundException("No voxellatest-remapper mod found."));
        }
        CustomValue customValue = optional.get().getMetadata().getCustomValue("voxellatest.remapperConfigClasses");
        if (customValue == null) {
            throw new RuntimeException(new ClassNotFoundException("No custom value \"voxellatest.remapperConfigClasses\" in voxellatest-remapper mod found."));
        }
        if (customValue.getType() != CustomValue.CvType.ARRAY) {
            throw new RuntimeException(new ClassNotFoundException("the custom value \"voxellatest.remapperConfigClasses\" in voxellatest-remapper mod isn't CvType.ARRAY."));
        }
        CustomValue.CvArray classPathes = customValue.getAsArray();
        for (CustomValue classPath : classPathes) {
            if (classPath.getType() != CustomValue.CvType.STRING) {
                throw new RuntimeException(new ClassNotFoundException(String.format("the custom value \"voxellatest.remapperConfigClasses[%s]\" in voxellatest-remapper mod isn't CvType.STRING.", classPath)));
            }
            Class<?> configClass;
            try {
                configClass = Class.forName(classPath.getAsString());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(new ClassNotFoundException(String.format("The Class of the custom value \"voxellatest.remapperConfigClasses[%s]\" in voxellatest-remapper mod not found.", classPath)));
            }

            boolean isClassMapper = isExtendsFrom(configClass, AbstractVoxelMapClassMapper.class);
            boolean isInsnWatcher = !isClassMapper && isExtendsFrom(configClass, AbstractVoxelMapInsnWatcher.class);
            if (!isClassMapper && !isInsnWatcher) {
                throw new RuntimeException(new ClassCastException(String.format("Cannot cast class of the custom value \"voxellatest.remapperConfigClasses[%s]\" in voxellatest-remapper mod to Class %s and Class %s", classPath, AbstractVoxelMapClassMapper.class.getName(), AbstractVoxelMapInsnWatcher.class.getName())));
            }

            Constructor<?> constructor;
            try {
                constructor = configClass.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(new NoSuchMethodException("The Constructor public () of the custom value \"voxellatest.remapperConfigClasses[%s]\" in voxellatest-remapper mod doesn't exist."));
            }

            Object instance;
            try {
                instance = constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Fail to new an instance of the custom value \"voxellatest.remapperConfigClasses[%s]\" in voxellatest-remapper mod.");
            }

            if (isClassMapper) {
                String className = ((AbstractVoxelMapClassMapper) instance).matchClass();
                if (!remappers.containsKey(className)) {
                    remappers.put(className, new ArrayList<>());
                }
                remappers.get(className).add((AbstractVoxelMapClassMapper) instance);
            } else {
                AbstractVoxelMapInsnWatcher.Shift shift = ((AbstractVoxelMapInsnWatcher) instance).injectPoint();
                if (shift.equals(AbstractVoxelMapInsnWatcher.Shift.BEFORE)) {
                    beforeWatchers.add((AbstractVoxelMapInsnWatcher) instance);
                } else if (shift.equals(AbstractVoxelMapInsnWatcher.Shift.AFTER)) {
                    afterWatchers.add((AbstractVoxelMapInsnWatcher) instance);
                }
            }
        }

        for (String className : remappers.keySet()) {
            remappers.get(className).sort(intReverseComparingInt(AbstractVoxelMapClassMapper::priority));
        }
        beforeWatchers.sort(intReverseComparingInt(AbstractVoxelMapInsnWatcher::priority));
        afterWatchers.sort(intReverseComparingInt(AbstractVoxelMapInsnWatcher::priority));
    }

    private static <T> Comparator<T> intReverseComparingInt(ToIntFunction<T> toIntFunction) {
        return (T t1, T t2) -> Integer.compare(toIntFunction.applyAsInt(t2), toIntFunction.applyAsInt(t1));
    }

    public static byte[] remapClass(byte[] data, String classLocation) {
        ClassReader classReader = new ClassReader(data);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);

        for (AbstractVoxelMapInsnWatcher instance : beforeWatchers) {
            instance.remap(classNode);
        }

        List<AbstractVoxelMapClassMapper> currentRemappers = remappers.get(classNode.name.replace('/', '.'));
        if (currentRemappers != null) {
            for (AbstractVoxelMapClassMapper instance : currentRemappers) {
                instance.remap(classNode);
            }
        }

        for (AbstractVoxelMapInsnWatcher instance : afterWatchers) {
            instance.remap(classNode);
        }

        checkMethodInsn(classNode);
        createRefMap(classNode);
        ClassWriter classWriter = new VoxelMapClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    private static void createRefMap(ClassNode classNode) {
        List<AnnotationNode> annotationNodeList = new ArrayList<>();
        if (classNode.visibleAnnotations != null) {
            annotationNodeList.addAll(classNode.visibleAnnotations);
        }
        if (classNode.invisibleAnnotations != null) {
            annotationNodeList.addAll(classNode.invisibleAnnotations);
        }
        for (AnnotationNode classAnnotationNode : annotationNodeList) {
            if (classAnnotationNode.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")) {
                JsonObject currentClassRefMapJsonRoot = new JsonObject();
                Object ownerObject = ASMUtil.getPropertyByName("value", classAnnotationNode);
                String owner;
                if (ownerObject instanceof String) {
                    owner = (String) ownerObject;
                } else {
                    owner = ((ArrayList<Type>) ownerObject).get(0).getClassName();
                }
//                owner = owner.substring(1,owner.length() - 1);
                for (MethodNode methodNode : classNode.methods) {
                    for (String mixinInjectionName : new String[]{"Lorg/spongepowered/asm/mixin/injection/Inject;", "Lorg/spongepowered/asm/mixin/injection/Redirect;"}) {
                        try {
                            AnnotationNode annotationNode = ASMUtil.getAnnotationNodeByName(mixinInjectionName, methodNode);
                            try {
                                Object object = ASMUtil.getPropertyByName("method", annotationNode);
                                if (object instanceof String target) {
                                    String targetRefMap = NamespaceManager.mapDesc(NamespaceManager.MAPPING_INTERMEDIARY, target.substring(target.indexOf('(')));
                                    targetRefMap = fixMethodRef(owner, target.substring(0, target.indexOf('(')), target.substring(target.indexOf('('))) + targetRefMap;
                                    Logger.info(String.format("Create Mixin Reference Map from \"%s\" to \"%s\"", target, targetRefMap));
                                    currentClassRefMapJsonRoot.add(target, new JsonPrimitive(targetRefMap));
                                } else {
                                    ArrayList<String> methods = (ArrayList<String>) object;
                                    for (String target : methods) {
                                        String targetRefMap = NamespaceManager.mapDesc(NamespaceManager.MAPPING_INTERMEDIARY, target.substring(target.indexOf('(')));
                                        targetRefMap = fixMethodRef(owner, target.substring(0, target.indexOf('(')), target.substring(target.indexOf('('))) + targetRefMap;
                                        Logger.info(String.format("Create Mixin Reference Map from \"%s\" to \"%s\"", target, targetRefMap));
                                        currentClassRefMapJsonRoot.add(target, new JsonPrimitive(targetRefMap));
                                    }
                                }
                            } catch (TargetNotSupportedException ignored) {
                            }
                            try {
                                Object object = ASMUtil.getPropertyByName("at", annotationNode);
                                if (object instanceof AnnotationNode atAnnotationNode) {
                                    try {
                                        String target = (String) ASMUtil.getPropertyByName("target", atAnnotationNode);
                                        String targetRefMap = NamespaceManager.mapDesc(NamespaceManager.MAPPING_INTERMEDIARY, target.substring(target.indexOf('(')));
                                        targetRefMap = "L" + NamespaceManager.mapClassName(NamespaceManager.MAPPING_INTERMEDIARY, target.substring(1, target.indexOf(';'))) + ";" + fixMethodRef(target.substring(1, target.indexOf(';')), target.substring(target.indexOf(';') + 1, target.indexOf('(')), target.substring(target.indexOf('('))) + targetRefMap;
                                        Logger.info(String.format("Create Mixin Reference Map from \"%s\" to \"%s\"", target, targetRefMap));
                                        currentClassRefMapJsonRoot.add(target, new JsonPrimitive(targetRefMap));
                                    } catch (TargetNotSupportedException ignored) {
                                    }

                                } else {
                                    ArrayList<AnnotationNode> atAnnotationNodes = (ArrayList<AnnotationNode>) object;
                                    for (AnnotationNode atAnnotationNode : atAnnotationNodes) {
                                        try {
                                            String target = (String) ASMUtil.getPropertyByName("target", atAnnotationNode);
                                            String targetRefMap = NamespaceManager.mapDesc(NamespaceManager.MAPPING_INTERMEDIARY, target.substring(target.indexOf('(')));
                                            targetRefMap = "L" + NamespaceManager.mapClassName(NamespaceManager.MAPPING_INTERMEDIARY, target.substring(1, target.indexOf(';'))) + ";" + fixMethodRef(target.substring(1, target.indexOf(';')), target.substring(target.indexOf(';') + 1, target.indexOf('(')), target.substring(target.indexOf('('))) + targetRefMap;
                                            Logger.info(String.format("Create Mixin Reference Map from \"%s\" to \"%s\"", target, targetRefMap));
                                            currentClassRefMapJsonRoot.add(target, new JsonPrimitive(targetRefMap));
                                        } catch (TargetNotSupportedException ignored) {
                                        }
                                    }
                                }
                            } catch (TargetNotSupportedException ignored) {
                            }
                        } catch (TargetNotSupportedException ignored) {
                        }
                    }
                }
                refmapJsonRoot.add(classNode.name, currentClassRefMapJsonRoot);
                break;
            }
        }
    }

    public static String buildRefMap() {
        JsonObject res = new JsonObject();
        res.add("mappings", refmapJsonRoot);
        res.add("data", new JsonObject());
        ((JsonObject) res.get("data")).add("named:intermediary", refmapJsonRoot);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(res);
    }

    private static void checkMethodInsn(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            AbstractInsnNode insnNode = methodNode.instructions.getFirst();
            while (insnNode != null) {
                if (insnNode instanceof MethodInsnNode methodInsnNode) {
                    if (methodInsnNode.owner.startsWith("net/minecraft")) {
                        ClassNode invokeClassNode;
                        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                            invokeClassNode = ASMUtil.getClassNode(methodInsnNode.owner);
                        } else {
                            invokeClassNode = ASMUtil.getClassNode(NamespaceManager.mapClassName(NamespaceManager.MAPPING_INTERMEDIARY, methodInsnNode.owner));
                        }
                        methodInsnNode.itf = (invokeClassNode.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE;

                        if (methodInsnNode.itf) {
                            methodInsnNode.setOpcode(Opcodes.INVOKEINTERFACE);
                        } else {
                            MethodNode invokeMethodNode;
                            methodInsnNode.name = fixMethodRef(methodInsnNode.owner, methodInsnNode.name, methodInsnNode.desc);
                            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                                invokeMethodNode = ASMUtil.getMethodNodeByName(methodInsnNode.name + methodInsnNode.desc, invokeClassNode);
                            } else {
                                invokeMethodNode = ASMUtil.getMethodNodeByName(
                                        NamespaceManager.mapMethodName(
                                                NamespaceManager.MAPPING_INTERMEDIARY,
                                                methodInsnNode.owner.replace('/', '.'),
                                                methodInsnNode.name,
                                                methodInsnNode.desc
                                        ) + NamespaceManager.mapDesc(NamespaceManager.MAPPING_INTERMEDIARY, methodInsnNode.desc),
                                        invokeClassNode
                                );
                            }
                            if ((invokeMethodNode.access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC) {
                                methodInsnNode.setOpcode(Opcodes.INVOKESTATIC);
                            } else if (methodInsnNode.getOpcode() != Opcodes.INVOKESPECIAL) {
                                if ((invokeClassNode.access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE) {
                                    methodInsnNode.setOpcode(Opcodes.INVOKEINTERFACE);
                                } else {
                                    methodInsnNode.setOpcode(Opcodes.INVOKEVIRTUAL);
                                }
                            }
                        }
                    }
                }

                insnNode = insnNode.getNext();
            }
        }
    }

    private static String fixMethodRef(String owner, String name, String desc) {
        ClassNode invokeClassNode = ASMUtil.getClassNode(owner);
        boolean isMethodFound = false;
        for (MethodNode methodNode : invokeClassNode.methods) {
            if (methodNode.name.equals(name) && methodNode.desc.equals(desc)) {
                isMethodFound = true;
                break;
            }
        }
        if (isMethodFound) {
            return name;
        }

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            // owner: yarn, name: intermediary, desc: yarn
            String intermediaryOwner = NamespaceManager.mapClassName(NamespaceManager.MAPPING_INTERMEDIARY, owner);
            String intermediaryDesc = NamespaceManager.mapDesc(NamespaceManager.MAPPING_INTERMEDIARY, desc);
            String yarnName = NamespaceManager.mapMethodName(NamespaceManager.MAPPING_YARN, intermediaryOwner, name, intermediaryDesc);
            if (!name.equals(yarnName)) {
                return yarnName;
            }
        } else {
            // owner: intermediary, name: yarn, desc: intermediary
            String yarnOwner = NamespaceManager.mapClassName(NamespaceManager.MAPPING_YARN, owner);
            String yarnDesc = NamespaceManager.mapDesc(NamespaceManager.MAPPING_YARN, desc);
            String intermediaryName = NamespaceManager.mapMethodName(NamespaceManager.MAPPING_INTERMEDIARY, yarnOwner, name, yarnDesc);
            if (!name.equals(intermediaryName)) {
                return intermediaryName;
            }
        }

        ClassNode superClassNode = ASMUtil.getSuperClassNode(ASMUtil.getClassNode(NamespaceManager.mapClassName(NamespaceManager.getCurrentNamespace(), owner)));
        if (superClassNode != null) {
            String yarnSuperMethodName = fixMethodRef(superClassNode.name, name, desc);
            if (!yarnSuperMethodName.equals(name)) {
                Logger.warn(String.format(
                        "Unmapped reference to \"%s#%s%s\" found. Redirect to \"%s#%s%s\"",
                        owner, name, desc, superClassNode.name, yarnSuperMethodName, desc
                ));
                return yarnSuperMethodName;
            }
        }

        List<ClassNode> interfaceClassNodeList = ASMUtil.getInterfaceClassNode(ASMUtil.getClassNode(NamespaceManager.mapClassName(NamespaceManager.getCurrentNamespace(), owner)));
        for (ClassNode interfaceClassNode : interfaceClassNodeList) {
            String yarnInterfaceMethodName = fixMethodRef(interfaceClassNode.name, name, desc);
            if (!yarnInterfaceMethodName.equals(name)) {
                Logger.warn(String.format(
                        "Unmapped reference to \"%s#%s%s\" found. Redirect to \"%s#%s%s\"",
                        owner, name, desc, interfaceClassNode.name, yarnInterfaceMethodName, desc
                ));
                return yarnInterfaceMethodName;
            }
        }

        return name;
    }

    private static class VoxelMapClassWriter extends ClassWriter {
        public VoxelMapClassWriter(int flags) {
            super(flags);
        }

        protected String getCommonSuperClass(final String type1, final String type2) {
            return CommonSuperClassManager.getCommonSuperClass(type1, type2);
        }
    }
}
