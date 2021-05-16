const ClassTinkerers = Java.type('com.chocohead.mm.api.ClassTinkerers');
const Opcodes = Java.type('org.objectweb.asm.Opcodes');
const LdcInsnNode = Java.type('org.objectweb.asm.tree.LdcInsnNode');
const MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
const VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
const InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
const String = Java.type('java.lang.String');

function run() {
  ClassTinkerers.addTransformation('net/minecraft/client/gui/hud/DebugHud', hud => {
    for (const method of hud.methods) {
      if (method.name === 'getRightText') {
        const posBeforeReturn = method.instructions.size() - 3;
        const beforeReturn = method.instructions.get(posBeforeReturn);

        const ldcInsn = new LdcInsnNode(':crab:');
        method.instructions.insert(beforeReturn, ldcInsn);

        const methodInsn = new MethodInsnNode(Opcodes.INVOKEINTERFACE, 'java/util/List', 'add', '(Ljava/lang/Object;)Z', true);
        method.instructions.insert(ldcInsn, methodInsn);

        const popInsn = new InsnNode(Opcodes.POP);
        method.instructions.insert(methodInsn, popInsn);

        const varInsn = new VarInsnNode(Opcodes.ALOAD, beforeReturn.var);
        method.instructions.insert(popInsn, varInsn);
      }
    }
  });
}
