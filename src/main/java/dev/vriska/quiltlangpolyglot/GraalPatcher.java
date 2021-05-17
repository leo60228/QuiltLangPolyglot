package dev.vriska.quiltlangpolyglot;

import net.devtech.grossfabrichacks.instrumentation.InstrumentationApi;
import net.devtech.grossfabrichacks.unsafe.UnsafeUtil;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.Opcodes;
import java.util.ListIterator;
import java.io.IOException;
import java.io.IOError;
import net.fabricmc.loader.launch.common.FabricLauncherBase;

public class GraalPatcher implements Runnable {
    static final String TYPE = "org/objectweb/asm/Type";
    static final String GRAAL_REMAPPER = "dev/vriska/quiltlangpolyglot/GraalRemapper";
    static final String FIELD_LOOKUP = "dev/vriska/quiltlangpolyglot/GraalRemapper$FieldLookup";
    static final String METHOD_LOOKUP = "dev/vriska/quiltlangpolyglot/GraalRemapper$MethodLookup";
    static final String REMAP_CLASS_DESCRIPTOR = "(Ljava/lang/String;)Ljava/lang/String;";
    static final String HOST_OBJECT = "com/oracle/truffle/polyglot/HostObject";
    static final String GET_LOOKUP_CLASS_DESCRIPTOR = "()Ljava/lang/Class;";
    static final String REMAP_MEMBER_DESCRIPTOR = "(Ljava/lang/Class;Ljava/lang/String;Z)Ljava/lang/String;";
    static final String REMAP_METHOD_DESCRIPTOR = "(Ljava/lang/reflect/Method;)Ljava/lang/String;";

    @Override
    public void run() {
        try {
            byte[] typeBytecode = FabricLauncherBase.getLauncher().getClassByteArray(TYPE, false);
            UnsafeUtil.defineClass(TYPE, typeBytecode, null);
            byte[] fieldLookupBytecode = FabricLauncherBase.getLauncher().getClassByteArray(FIELD_LOOKUP, false);
            UnsafeUtil.defineClass(FIELD_LOOKUP, fieldLookupBytecode, null);
            byte[] methodLookupBytecode = FabricLauncherBase.getLauncher().getClassByteArray(METHOD_LOOKUP, false);
            UnsafeUtil.defineClass(METHOD_LOOKUP, methodLookupBytecode, null);
            byte[] remapperBytecode = FabricLauncherBase.getLauncher().getClassByteArray(GRAAL_REMAPPER, false);
            UnsafeUtil.defineClass(GRAAL_REMAPPER, remapperBytecode, null);

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

            Class<?> methodInfo = Class.forName("com.oracle.truffle.polyglot.HostClassDesc$Members$1MethodInfo");
            System.out.println(methodInfo);
            InstrumentationApi.retransform(methodInfo, (s, b) -> {
                for (MethodNode node : b.methods) {
                    if (node.name.equals("<init>")) {
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
        } catch (IOException ex) {
            throw new IOError(ex);
        }
    }
}
