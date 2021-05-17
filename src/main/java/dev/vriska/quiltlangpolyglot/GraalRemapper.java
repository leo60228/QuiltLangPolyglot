package dev.vriska.quiltlangpolyglot;

import net.devtech.grossfabrichacks.instrumentation.InstrumentationApi;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.Opcodes;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class GraalRemapper implements Runnable {
    static final String GRAAL_REMAPPER = "dev/vriska/quiltlangpolyglot/GraalRemapper";
    static final String REMAP_CLASS_DESCRIPTOR = "(Ljava/lang/String;)Ljava/lang/String;";
    static final String HOST_OBJECT = "com/oracle/truffle/polyglot/HostObject";
    static final String GET_LOOKUP_CLASS_DESCRIPTOR = "()Ljava/lang/Class;";
    static final String REMAP_MEMBER_DESCRIPTOR = "(Ljava/lang/Class;Ljava/lang/String;Z)Ljava/lang/String;";

    public static Map<String, String> classNames = null;

    public static void loadMappings() {
        try {
            InputStream mappingStream = GraalRemapper.class.getResourceAsStream("/mappings.tiny");
            BufferedReader mappingReader = new BufferedReader(new InputStreamReader(mappingStream));
            TinyTree mappings = TinyMappingFactory.loadWithDetection(mappingReader);
            mappingStream.close();

            classNames = new HashMap<>();

            for (ClassDef classDef : mappings.getClasses()) {
                String named = classDef.getName("named").replace('/', '.');
                String intermediary = classDef.getName("intermediary").replace('/', '.');
                classNames.put(named, intermediary);
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
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static String remapClass(String original) {
        System.out.println("remapClass(" + original + ")");

        if (classNames == null) loadMappings();

        String mapped = original;

        if (classNames.containsKey(original)) {
            String intermediary = classNames.getOrDefault(original, original);
            System.out.println("intermediary: " + intermediary);

            MappingResolver fabricResolver = FabricLoader.getInstance().getMappingResolver();
            mapped = fabricResolver.mapClassName("intermediary", intermediary);

            System.out.println("mapped to: " + mapped);
        } else {
            System.out.println("not in mappings");
        }

        return mapped;
    }

    public static String remapMember(Class<?> klass, String original, boolean invoke) {
        System.out.println("remapMember(" + klass + ", " + original + ", " + invoke + ")");
        return original.replace("PREFIX_", "");
    }
}
