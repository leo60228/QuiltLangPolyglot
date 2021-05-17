package dev.vriska.quiltlangpolyglot;

import net.devtech.grossfabrichacks.instrumentation.InstrumentationApi;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.ListIterator;
import java.lang.reflect.Method;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.FieldDef;
import net.fabricmc.mapping.tree.MethodDef;
import net.fabricmc.mapping.util.EntryTriple;
import net.fabricmc.mapping.util.ClassMapper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class GraalRemapper implements Runnable {
    static final String GRAAL_REMAPPER = "dev/vriska/quiltlangpolyglot/GraalRemapper";
    static final String REMAP_CLASS_DESCRIPTOR = "(Ljava/lang/String;)Ljava/lang/String;";
    static final String HOST_OBJECT = "com/oracle/truffle/polyglot/HostObject";
    static final String GET_LOOKUP_CLASS_DESCRIPTOR = "()Ljava/lang/Class;";
    static final String REMAP_MEMBER_DESCRIPTOR = "(Ljava/lang/Class;Ljava/lang/String;Z)Ljava/lang/String;";
    static final String REMAP_METHOD_DESCRIPTOR = "(Ljava/lang/reflect/Method;)Ljava/lang/String;";

    public static Map<String, String> classNames = null;
    public static Map<String, String> inverseClassNames = null;
    public static Map<EntryTriple, String> fields = null;
    public static Map<EntryTriple, String> methods = null;

    public static void loadMappings() {
        try {
            InputStream mappingStream = GraalRemapper.class.getResourceAsStream("/mappings.tiny");
            BufferedReader mappingReader = new BufferedReader(new InputStreamReader(mappingStream));
            TinyTree mappings = TinyMappingFactory.loadWithDetection(mappingReader);
            mappingStream.close();

            classNames = new HashMap<>();
            inverseClassNames = new HashMap<>();

            MappingResolver fabricResolver = FabricLoader.getInstance().getMappingResolver();

            for (ClassDef classDef : mappings.getClasses()) {
                String named = classDef.getName("named");
                String intermediary = classDef.getName("intermediary").replace('/', '.');
                String mapped = fabricResolver.mapClassName("intermediary", intermediary).replace('.', '/');
                classNames.put(named, mapped);
                inverseClassNames.put(mapped, named);
            }

            ClassMapper classMapper = new ClassMapper(classNames);
            fields = new HashMap<>();
            methods = new HashMap<>();

            for (ClassDef classDef : mappings.getClasses()) {
                String classNamed = classDef.getName("named");
                String classIntermediary = classDef.getName("intermediary").replace('/', '.');
                String classMapped = classNames.get(classNamed);
                for (FieldDef fieldDef : classDef.getFields()) {
                    String fieldNamed = fieldDef.getName("named");

                    String fieldIntermediary = fieldDef.getName("intermediary");
                    String fieldDescIntermediary = fieldDef.getDescriptor("intermediary");

                    String fieldMapped = fabricResolver.mapFieldName("intermediary", classIntermediary, fieldIntermediary, fieldDescIntermediary);

                    EntryTriple lookup = new EntryTriple(classMapped, fieldNamed, "");

                    fields.put(lookup, fieldMapped);
                }

                for (MethodDef methodDef : classDef.getMethods()) {
                    String methodNamed = methodDef.getName("named");
                    String methodDescNamed = methodDef.getDescriptor("named");

                    String methodIntermediary = methodDef.getName("intermediary");
                    String methodDescIntermediary = methodDef.getDescriptor("intermediary");

                    String methodMapped = fabricResolver.mapMethodName("intermediary", classIntermediary, methodIntermediary, methodDescIntermediary);

                    EntryTriple named = new EntryTriple(classNamed, methodNamed, methodDescNamed);
                    EntryTriple mapped = named.map(classMapper, methodMapped);

                    if (methodNamed.equals("register")) {
                        System.out.println(mapped);
                    }

                    methods.put(mapped, methodNamed);
                }
            }
        } catch (IOException ex) {
            throw new IOError(ex);
        }
    }

    @Override
    public void run() {
        try {
            Class<?> hostContext = Class.forName("com.oracle.truffle.polyglot.HostLanguage$HostContext");
            System.out.println(hostContext);
            InstrumentationApi.retransform(hostContext, (s, b) -> {
                for (MethodNode node : b.methods) {
                    if (node.name.equals("findClassImpl")) {
                        System.out.println("patching");

                        InsnList prepended = new InsnList();
                        prepended.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        prepended.add(new MethodInsnNode(Opcodes.INVOKESTATIC, GRAAL_REMAPPER, "remapClass", REMAP_CLASS_DESCRIPTOR));
                        prepended.add(new VarInsnNode(Opcodes.ASTORE, 1));
                        node.instructions.insert(prepended);
                    }
                }
            });

            Class<?> hostObject = Class.forName("com.oracle.truffle.polyglot.HostObject");
            System.out.println(hostObject);
            InstrumentationApi.retransform(hostObject, (s, b) -> {
                for (MethodNode node : b.methods) {
                    if (node.name.equals("readMember") || node.name.equals("invokeMember")) {
                        System.out.println("patching " + node.name);

                        InsnList prepended = new InsnList();
                        prepended.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        prepended.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, HOST_OBJECT, "getLookupClass", GET_LOOKUP_CLASS_DESCRIPTOR));
                        prepended.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        prepended.add(new InsnNode(node.name.equals("invokeMember") ? Opcodes.ICONST_1 : Opcodes.ICONST_0));
                        prepended.add(new MethodInsnNode(Opcodes.INVOKESTATIC, GRAAL_REMAPPER, "remapMember", REMAP_MEMBER_DESCRIPTOR));
                        prepended.add(new VarInsnNode(Opcodes.ASTORE, 1));
                        node.instructions.insert(prepended);
                    }
                }
            });

            Class<?> members = Class.forName("com.oracle.truffle.polyglot.HostClassDesc$Members");
            System.out.println(members);
            InstrumentationApi.retransform(members, (s, b) -> {
                for (MethodNode node : b.methods) {
                    if (node.name.equals("putMethod")) {
                        System.out.println("patching");

                        ListIterator<AbstractInsnNode> iter = node.instructions.iterator();
                        while (iter.hasNext()) {
                            AbstractInsnNode insn = iter.next();

                            if (insn instanceof MethodInsnNode call && call.name.equals("getName")) {
                                System.out.println(call);
                                iter.set(new MethodInsnNode(Opcodes.INVOKESTATIC, GRAAL_REMAPPER, "remapMethod", REMAP_METHOD_DESCRIPTOR));
                            }
                        }
                    }
                }
            });
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static String remapClass(String original) {
        try {
            System.out.println("remapClass(" + original + ")");

            if (classNames == null) loadMappings();

            String binary = original.replace('.', '/');
            String mapped = classNames.getOrDefault(binary, original).replace('/', '.');

            System.out.println("mapped to: " + mapped);

            return mapped;
        } catch (Exception ex) {
            ex.printStackTrace(System.out);
            throw ex;
        }
    }

    public static String remapMember(Class<?> klass, String original, boolean invoke) {
        System.out.println("remapMember(" + klass + ", " + original + ", " + invoke + ")");

        if (fields == null) loadMappings();

        EntryTriple lookup = new EntryTriple(Type.getInternalName(klass), original, "");
        System.out.println(lookup);

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

        String className = Type.getInternalName(method.getDeclaringClass());
        String methodName = method.getName();
        String methodDesc = Type.getMethodDescriptor(method);

        EntryTriple lookup = new EntryTriple(className, methodName, methodDesc);
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
