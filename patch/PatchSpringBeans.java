import org.objectweb.asm.*;
import java.io.*;
import java.util.jar.*;

public class PatchSpringBeans {
    static final String TARGET_CLASS = "org/springframework/beans/factory/support/FactoryBeanRegistrySupport";
    static final String TARGET_METHOD = "getTypeForFactoryBeanFromAttributes";
    static final String TARGET_DESC = "(Lorg/springframework/core/AttributeAccessor;)Lorg/springframework/core/ResolvableType;";

    public static void main(String[] args) throws Exception {
        String springBeans = args.length > 0 ? args[0] : "/root/.m2/repository/org/springframework/spring-beans/6.1.1/spring-beans-6.1.1.jar";
        String patched = args.length > 1 ? args[1] : "/workspace/spring-beans-patched.jar";

        JarFile jar = new JarFile(springBeans);
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(patched))) {
            for (java.util.Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements(); ) {
                JarEntry entry = e.nextElement();
                if (entry.getName().equals(TARGET_CLASS + ".class")) {
                    byte[] bytes;
                    try (InputStream is = jar.getInputStream(entry)) {
                        bytes = patchClass(is.readAllBytes());
                    }
                    JarEntry ne = new JarEntry(entry.getName());
                    jos.putNextEntry(ne);
                    jos.write(bytes);
                    jos.closeEntry();
                    System.out.println("Patched " + entry.getName());
                } else {
                    JarEntry ne = new JarEntry(entry.getName());
                    jos.putNextEntry(ne);
                    jar.getInputStream(entry).transferTo(jos);
                    jos.closeEntry();
                }
            }
        }
        jar.close();
        System.out.println("Patched jar at " + patched);
    }

    static byte[] patchClass(byte[] classBytes) {
        ClassReader cr = new ClassReader(classBytes);
        ClassLoader origCl = Thread.currentThread().getContextClassLoader();
        if (origCl == null) origCl = PatchSpringBeans.class.getClassLoader();
        final ClassLoader cl = origCl;
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                try {
                    Class<?> c1 = Class.forName(type1.replace('/', '.'), false, cl);
                    Class<?> c2 = Class.forName(type2.replace('/', '.'), false, cl);
                    if (c1.isAssignableFrom(c2)) return type1;
                    if (c2.isAssignableFrom(c1)) return type2;
                    if (c1.isInterface() || c2.isInterface()) return "java/lang/Object";
                    return "java/lang/Object";
                } catch (ClassNotFoundException e) {
                    return "java/lang/Object";
                }
            }
        };

        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (TARGET_METHOD.equals(name) && TARGET_DESC.equals(descriptor)) {
                    System.out.println("Replacing method: " + name + descriptor);
                    return new ReplaceMethodVisitor(mv);
                }
                return mv;
            }
        };

        cr.accept(cv, 0);
        return cw.toByteArray();
    }

    static class ReplaceMethodVisitor extends MethodVisitor {
        private final MethodVisitor mv;

        public ReplaceMethodVisitor(MethodVisitor mv) {
            super(Opcodes.ASM9, mv);
            this.mv = mv;
        }

        @Override
        public void visitCode() {
            generateNewBody();
        }

        private void generateNewBody() {
            Label lStart = new Label();
            Label lNonNull = new Label();
            Label lNotRT = new Label();
            Label lNotClass = new Label();
            Label lTryStart = new Label();
            Label lTryEnd = new Label();
            Label lCatchHandler = new Label();
            Label lNotString = new Label();

            mv.visitCode();
            mv.visitLabel(lStart);

            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitLdcInsn("factoryBeanObjectType");
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                "org/springframework/core/AttributeAccessor", "getAttribute",
                "(Ljava/lang/String;)Ljava/lang/Object;",
                true);
            mv.visitVarInsn(Opcodes.ASTORE, 2);

            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitJumpInsn(Opcodes.IFNONNULL, lNonNull);
            mv.visitFieldInsn(Opcodes.GETSTATIC,
                "org/springframework/core/ResolvableType", "NONE",
                "Lorg/springframework/core/ResolvableType;");
            mv.visitInsn(Opcodes.ARETURN);

            mv.visitLabel(lNonNull);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitTypeInsn(Opcodes.INSTANCEOF, "org/springframework/core/ResolvableType");
            mv.visitJumpInsn(Opcodes.IFEQ, lNotRT);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitTypeInsn(Opcodes.CHECKCAST, "org/springframework/core/ResolvableType");
            mv.visitInsn(Opcodes.ARETURN);

            mv.visitLabel(lNotRT);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitTypeInsn(Opcodes.INSTANCEOF, "java/lang/Class");
            mv.visitJumpInsn(Opcodes.IFEQ, lNotClass);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Class");
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                "org/springframework/core/ResolvableType", "forClass",
                "(Ljava/lang/Class;)Lorg/springframework/core/ResolvableType;",
                false);
            mv.visitInsn(Opcodes.ARETURN);

            mv.visitLabel(lNotClass);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitInsn(Opcodes.DUP);
            mv.visitTypeInsn(Opcodes.INSTANCEOF, "java/lang/String");
            mv.visitJumpInsn(Opcodes.IFEQ, lNotString);

            mv.visitLabel(lTryStart);
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/String");
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                "java/lang/Class", "forName",
                "(Ljava/lang/String;)Ljava/lang/Class;",
                false);
            mv.visitLabel(lTryEnd);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                "org/springframework/core/ResolvableType", "forClass",
                "(Ljava/lang/Class;)Lorg/springframework/core/ResolvableType;",
                false);
            mv.visitInsn(Opcodes.ARETURN);

            mv.visitLabel(lCatchHandler);
            mv.visitInsn(Opcodes.POP);

            mv.visitLabel(lNotString);
            mv.visitTypeInsn(Opcodes.NEW, "java/lang/IllegalArgumentException");
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "java/lang/Object", "getClass",
                "()Ljava/lang/Class;",
                false);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "java/lang/Class", "getName",
                "()Ljava/lang/String;",
                false);
            Handle bootstrap = new Handle(
                Opcodes.H_INVOKESTATIC,
                "java/lang/invoke/StringConcatFactory",
                "makeConcatWithConstants",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;",
                false);
            mv.visitInvokeDynamicInsn(
                "makeConcatWithConstants",
                "(Ljava/lang/String;)Ljava/lang/String;",
                bootstrap,
                "Invalid value type for attribute 'factoryBeanObjectType': \u0001");
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                "java/lang/IllegalArgumentException", "<init>",
                "(Ljava/lang/String;)V",
                false);
            mv.visitInsn(Opcodes.ATHROW);

            mv.visitTryCatchBlock(lTryStart, lTryEnd, lCatchHandler, "java/lang/Throwable");

            mv.visitMaxs(3, 3);
            mv.visitEnd();
        }

        @Override
        public void visitInsn(int opcode) { }
        @Override
        public void visitVarInsn(int opcode, int var) { }
        @Override
        public void visitTypeInsn(int opcode, String descriptor) { }
        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String descriptor) { }
        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) { }
        @Override
        public void visitJumpInsn(int opcode, Label label) { }
        @Override
        public void visitLdcInsn(Object value) { }
        @Override
        public void visitLabel(Label label) { }
        @Override
        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) { }
        @Override
        public void visitMaxs(int maxStack, int maxLocals) { }
        @Override
        public void visitEnd() { }
    }
}
