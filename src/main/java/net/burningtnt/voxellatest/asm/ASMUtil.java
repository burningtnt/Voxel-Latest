package net.burningtnt.voxellatest.asm;

import net.burningtnt.voxellatest.NamespaceManager;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.injection.struct.TargetNotSupportedException;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class ASMUtil {
    private ASMUtil() {
    }

    public static FieldNode getFieldNodeByName(String name, ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.name.equals(name)) {
                return fieldNode;
            }
        }
        throw new TargetNotSupportedException(String.format("No such field \"%s\" in class \"%s\".", name, classNode.name));
    }

    public static LocalVariableNode getLocalVariableByName(String name, MethodNode methodNode) {
        for (LocalVariableNode localVariableNode : methodNode.localVariables) {
            if (localVariableNode.name.equals(name)) {
                return localVariableNode;
            }
        }
        throw new TargetNotSupportedException(String.format("No such localVariable \"%s\" in method \"%s\".", name, methodNode.name + methodNode.desc));
    }

    public static MethodNode getMethodNodeByName(String name, ClassNode classNode) {
        if (name.contains("(")) {
            // Full name
            String methodName = name.substring(0, name.indexOf('('));
            String methodArg = name.substring(name.indexOf('(') + 1, name.indexOf(')'));
            for (MethodNode methodNode : classNode.methods) {
                String expectMethodArg = methodNode.desc.substring(1, methodNode.desc.indexOf(')'));
                if (methodNode.name.equals(methodName) && expectMethodArg.equals(methodArg)) {
                    return methodNode;
                }
            }
        } else {
            // Simple name
            MethodNode cache = null;
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals(name)) {
                    if (cache == null) {
                        cache = methodNode;
                    } else {
                        throw new RuntimeException(String.format("Method \"%s\" in class \"%s\" is Overloaded.", name, classNode.name.replace("/", ".")));
                    }
                }
            }
            if (cache != null) {
                return cache;
            }
        }
        ClassNode superClassNode = getSuperClassNode(classNode);
        if (superClassNode != null) {
            MethodNode methodNode = null;
            try {
                methodNode = getMethodNodeByName(name, superClassNode);
            } catch (TargetNotSupportedException ignored) {
            }
            if (methodNode != null) {
                return methodNode;
            }
        }

        List<ClassNode> interfaceClassNodeList = getInterfaceClassNode(classNode);
        for (ClassNode interfaceClassNode: interfaceClassNodeList) {
            MethodNode methodNode = null;
            try {
                methodNode = getMethodNodeByName(name, interfaceClassNode);
            } catch (TargetNotSupportedException ignored) {
            }
            if (methodNode != null) {
                return methodNode;
            }
        }

        throw new TargetNotSupportedException(String.format("No such method \"%s\" in class \"%s\".", name, classNode.name));
    }

    public static AnnotationNode getAnnotationNodeByName(String annotationName, MethodNode methodNode) {
        List<AnnotationNode> annotationNodeList = new ArrayList<>();
        if (methodNode.visibleAnnotations != null) {
            annotationNodeList.addAll(methodNode.visibleAnnotations);
        }
        if (methodNode.invisibleAnnotations != null) {
            annotationNodeList.addAll(methodNode.invisibleAnnotations);
        }
        for (AnnotationNode annotationNode : annotationNodeList) {
            if (annotationNode.desc.equals(annotationName)) {
                return annotationNode;
            }
        }
        throw new TargetNotSupportedException(String.format("No such Annotation \"%s\" in method \"%s\".", annotationName, methodNode.name + methodNode.desc));
    }

    public static Object getPropertyByName(String name, AnnotationNode annotationNode) {
        for (int i = 0; i < annotationNode.values.size(); i += 2) {
            if (!(annotationNode.values.get(i) instanceof String)) {
                throw new UnsupportedOperationException("org.objectweb.asm.tree.AnnotationNode.values[2 * n] must instanceof java.lang.String.");
            }
            if (name.equals(annotationNode.values.get(i))) {
                return annotationNode.values.get(i + 1);
            }
        }
        throw new TargetNotSupportedException(String.format("No such Property \"%s\" in annotation \"%s\".", name, annotationNode.desc));
    }

    public static void setPropertyByName(String name, Object value, AnnotationNode annotationNode) {
        for (int i = 0; i < annotationNode.values.size(); i += 2) {
            if (!(annotationNode.values.get(i) instanceof String)) {
                throw new UnsupportedOperationException("org.objectweb.asm.tree.AnnotationNode.values[2 * n] must instanceof java.lang.String.");
            }
            if (name.equals(annotationNode.values.get(i))) {
                annotationNode.values.set(i + 1, value);
            }
        }
        throw new TargetNotSupportedException(String.format("No such Property \"%s\" in annotation \"%s\".", name, annotationNode.desc));
    }

    public static int getInsnIndexByLineNumber(int lineNumber, MethodNode methodNode) {
        for (int i = 0; i < methodNode.instructions.size(); i++) {
            if (methodNode.instructions.get(i) instanceof LineNumberNode lineNumberNode) {
                if (lineNumberNode.line == lineNumber) {
                    return i;
                }
            }
        }
        throw new TargetNotSupportedException(String.format("No such LineNumberNode \"%d\" in method \"%s\".", lineNumber, methodNode.name + methodNode.desc));
    }

    public static LabelNode getLabelNodeByLineNumber(int lineNumber, MethodNode methodNode) {
        for (int i = 0; i < methodNode.instructions.size(); i++) {
            if (methodNode.instructions.get(i) instanceof LineNumberNode lineNumberNode) {
                if (lineNumberNode.line == lineNumber) {
                    return lineNumberNode.start;
                }
            }
        }
        throw new TargetNotSupportedException(String.format("No such LineNumberNode \"%d\" in method \"%s\".", lineNumber, methodNode.name + methodNode.desc));
    }

    public static void removeInsnSinceIndex(int index, int size, MethodNode methodNode) {
        for (int i = 0; i < size; i++) {
            methodNode.instructions.remove(methodNode.instructions.get(index));
        }
    }

    public static void removeInsnBetween(int start, int end, MethodNode methodNode) {
        removeInsnSinceIndex(start, end - start, methodNode);
    }

    public static void insertInsnAtIndex(int index, AbstractInsnNode abstractInsnNode, MethodNode methodNode) {
        methodNode.instructions.insertBefore(methodNode.instructions.get(index), abstractInsnNode);
    }

    @Nullable
    public static ClassNode getSuperClassNode(ClassNode classNode) {
        if (classNode.superName == null) {
            return null;
        }
        return getClassNode(classNode.superName);
    }

    public static List<ClassNode> getInterfaceClassNode(ClassNode classNode) {
        ArrayList<ClassNode> arrayList = new ArrayList<>();
        for (String interfaceName : classNode.interfaces) {
            arrayList.add(getClassNode(interfaceName));
        }
        return arrayList;
    }

    public static ClassNode getClassNode(String className) {
        className = className.replace('.','/');
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            byte[] data;
            try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(className + ".class")) {
                if (inputStream == null) {
                    throw new NullPointerException(String.format("Cannot read resource from \"%s.class\" because InputStream is null.", className));
                }
                data = inputStream.readAllBytes();
            } catch (Throwable e) {
                throw new RuntimeException(String.format("An Error was thrown while reading resource from \"%s.class\".", className), e);
            }
            ClassReader classReader = new ClassReader(data);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);
            return classNode;
        } else {
            String intermediaryClassName = NamespaceManager.mapClassName(NamespaceManager.MAPPING_INTERMEDIARY, className);
            byte[] data;
            try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(intermediaryClassName + ".class")) {
                if (inputStream == null) {
                    throw new NullPointerException(String.format("Cannot read resource from \"%s.class\" because InputStream is null.", intermediaryClassName));
                }
                data = inputStream.readAllBytes();
            } catch (Throwable e) {
                throw new RuntimeException(String.format("An Error was thrown while reading resource from \"%s.class\".", intermediaryClassName), e);
            }
            ClassReader classReader = new ClassReader(data);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);
            return classNode;
        }
    }
}
