package dev.vriska.quiltlangpolyglot;

import org.objectweb.asm.Type;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class GraalRemapper {
    public static Map<String, String> classNames = null;
    public static Map<FieldLookup, String> fields = null;
    public static Map<MethodLookup, String> methods = null;

    public record FieldLookup(String owner, String name) {}
    public record MethodLookup(String owner, String name, String descriptor) {}

    @SuppressWarnings("unchecked")
    private static void loadMappings() {
        try {
            ClassLoader knot = Thread.currentThread().getContextClassLoader();
            Class<?> mappings = knot.loadClass("dev.vriska.quiltlangpolyglot.GraalMappings");
            Class<?> fieldLookup = knot.loadClass("dev.vriska.quiltlangpolyglot.GraalRemapper$FieldLookup");
            Class<?> methodLookup = knot.loadClass("dev.vriska.quiltlangpolyglot.GraalRemapper$MethodLookup");

            Field classNamesField = mappings.getDeclaredField("classNames");
            Field fieldsField = mappings.getDeclaredField("fields");
            Field methodsField = mappings.getDeclaredField("methods");

            Field fieldOwnerField = fieldLookup.getDeclaredField("owner");
            Field fieldNameField = fieldLookup.getDeclaredField("name");

            Field methodOwnerField = methodLookup.getDeclaredField("owner");
            Field methodNameField = methodLookup.getDeclaredField("name");
            Field methodDescriptorField = methodLookup.getDeclaredField("descriptor");

            if (classNamesField.get(null) == null) {
                Method loadMappings = mappings.getDeclaredMethod("loadMappings");
                loadMappings.invoke(null);
            }

            classNames = new HashMap<>((Map<String, String>) classNamesField.get(null));

            fields = new HashMap<>();
            Map<Object, String> foreignFields = (Map<Object, String>) fieldsField.get(null);
            foreignFields.forEach((foreignKey, value) -> {
                try {
                    String owner = (String) fieldOwnerField.get(foreignKey);
                    String name = (String) fieldNameField.get(foreignKey);
                    FieldLookup key = new FieldLookup(owner, name);
                    fields.put(key, value);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException(ex);
                }
            });

            methods = new HashMap<>();
            Map<Object, String> foreignMethods = (Map<Object, String>) methodsField.get(null);
            foreignMethods.forEach((foreignKey, value) -> {
                try {
                    String owner = (String) methodOwnerField.get(foreignKey);
                    String name = (String) methodNameField.get(foreignKey);
                    String descriptor = (String) methodDescriptorField.get(foreignKey);
                    MethodLookup key = new MethodLookup(owner, name, descriptor);
                    methods.put(key, value);
                } catch (IllegalAccessException ex) {
                    throw new IllegalStateException(ex);
                }
            });

            assert classNames != null;
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        } catch (NoSuchFieldException ex) {
            throw new IllegalStateException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } catch (InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static String remapClass(String original) {
        try {
            System.out.println("remapClass(" + original + ")");

            if (classNames == null) loadMappings();

            if (original.endsWith("[]")) {
                String element = original.substring(0, original.length() - 2);
                String binary = element.replace('.', '/');
                String mapped = classNames.getOrDefault(binary, element).replace('/', '.') + "[]";

                System.out.println("mapped to: " + mapped);

                return mapped;
            } else {
                String binary = original.replace('.', '/');
                String mapped = classNames.getOrDefault(binary, original).replace('/', '.');

                System.out.println("mapped to: " + mapped);

                return mapped;
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            throw ex;
        }
    }

    public static String remapMember(Class<?> klass, String original, boolean invoke) {
        System.out.println("remapMember(" + klass + ", " + original + ", " + invoke + ")");

        if (classNames == null) loadMappings();

        FieldLookup lookup = new FieldLookup(Type.getInternalName(klass), original);
        System.out.println(lookup);
        System.out.println(lookup.hashCode());

        fields.forEach((k, v) -> {
            if (k.name.equals("METAL")) {
                System.out.println(k);
                System.out.println(v);
                System.out.println(k.equals(lookup));
                System.out.println(k.hashCode() == lookup.hashCode());
            }
        });

        if (fields.containsKey(lookup)) {
            String mapped = fields.get(lookup);
            System.out.println("mapped: " + mapped);
            return mapped;
        } else {
            System.out.println("not in mappings");
            return original;
        }
    }

    public static String remapMethod(Method method) {
        System.out.printf("remapMethod(%s)\n", method);

        if (classNames == null) loadMappings();

        String className = Type.getInternalName(method.getDeclaringClass());
        String methodName = method.getName();
        String methodDesc = Type.getMethodDescriptor(method);

        MethodLookup lookup = new MethodLookup(className, methodName, methodDesc);
        System.out.println(lookup);

        if (methods.containsKey(lookup)) {
            String mapped = methods.get(lookup);
            System.out.println("mapped: " + mapped);
            return mapped;
        } else {
            System.out.println("not in mappings");
            return methodName;
        }
    }
}
