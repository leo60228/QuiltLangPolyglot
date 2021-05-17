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
    static final String GET_CLASS_NAME_DESCRIPTOR = "(Ljava/lang/Class;)Ljava/lang/String;";
    static final String REMAP_FIELD_DESCRIPTOR = "(Ljava/lang/reflect/Field;)Ljava/lang/String;";
    static final String REMAP_METHOD_DESCRIPTOR = "(Ljava/lang/reflect/Method;)Ljava/lang/String;";
    static final String FIND_INNER_CLASS_DESCRIPTOR = "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Class;";

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

            Class<?> hostInteropReflect = Class.forName("com.oracle.truffle.polyglot.HostInteropReflect");
            System.out.println(hostInteropReflect);
            InstrumentationApi.retransform(hostInteropReflect, (s, b) -> {
                for (MethodNode node : b.methods) {
                    if (node.name.equals("findInnerClass")) {
                        System.out.println("patching " + node.name);

                        node.instructions.clear();
                        node.visitVarInsn(Opcodes.ALOAD, 0);
                        node.visitVarInsn(Opcodes.ALOAD, 1);
                        node.visitMethodInsn(Opcodes.INVOKESTATIC, GRAAL_REMAPPER, "findInnerClass", FIND_INNER_CLASS_DESCRIPTOR, false);
                        node.visitInsn(Opcodes.ARETURN);
                    } else if (node.name.equals("toNameAndSignature")) {
                        System.out.println("patching " + node.name);

                        ListIterator<AbstractInsnNode> iter = node.instructions.iterator();
                        while (iter.hasNext()) {
                            AbstractInsnNode insn = iter.next();

                            if (insn instanceof MethodInsnNode call && call.name.equals("getTypeName")) {
                                System.out.println(call);
                                iter.set(new MethodInsnNode(Opcodes.INVOKESTATIC, GRAAL_REMAPPER, "getClassName", GET_CLASS_NAME_DESCRIPTOR));
                            }
                        }
                    }
                }
            });

            Class<?> members = Class.forName("com.oracle.truffle.polyglot.HostClassDesc$Members");
            System.out.println(members);
            InstrumentationApi.retransform(members, (s, b) -> {
                for (MethodNode node : b.methods) {
                    if (node.name.equals("putMethod") || node.name.equals("collectPublicFields") || node.name.equals("collectPublicInstanceFields")) {
                        System.out.println("patching " + node.name);

                        ListIterator<AbstractInsnNode> iter = node.instructions.iterator();
                        while (iter.hasNext()) {
                            AbstractInsnNode insn = iter.next();

                            if (insn instanceof MethodInsnNode call && call.name.equals("getName")) {
                                System.out.println(call);
                                if (node.name.equals("putMethod")) {
                                    iter.set(new MethodInsnNode(Opcodes.INVOKESTATIC, GRAAL_REMAPPER, "remapMethod", REMAP_METHOD_DESCRIPTOR));
                                } else {
                                    iter.set(new MethodInsnNode(Opcodes.INVOKESTATIC, GRAAL_REMAPPER, "remapField", REMAP_FIELD_DESCRIPTOR));
                                }
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
