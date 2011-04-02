..\ASMifierRUN.cmd: processing class "C.class", all args: "C.class"
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.attrs.*;
public class CDump implements Opcodes {

public static byte[] dump () throws Exception {

ClassWriter cw = new ClassWriter(0);
FieldVisitor fv;
MethodVisitor mv;
AnnotationVisitor av0;

cw.visit(V1_6, ACC_SUPER, "C", null, "B", null);

{
   mv = cw.visitMethod(0, "<init>", "()V", null, null);
   mv.visitCode();
   mv.visitVarInsn(ALOAD, 0);
   mv.visitMethodInsn(INVOKESPECIAL, "B", "<init>", "()V");
   mv.visitInsn(RETURN);
   mv.visitMaxs(1, 1);
   mv.visitEnd();
}

{
   mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
   mv.visitCode();
   mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
   mv.visitLdcInsn("from class C's main() method, using C.staticMethod(...)");
   mv.visitMethodInsn(INVOKESTATIC, "C", "staticSomeString", "(Ljava/lang/String;)Ljava/lang/String;");
   mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
   mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
   mv.visitLdcInsn("from class C's main() method, using B.staticMethod(...)");
   mv.visitMethodInsn(INVOKESTATIC, "B", "staticSomeString", "(Ljava/lang/String;)Ljava/lang/String;");
   mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
   mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
   mv.visitLdcInsn("from class C's main() method, using A.staticMethod(...)");
   mv.visitMethodInsn(INVOKESTATIC, "A", "staticSomeString", "(Ljava/lang/String;)Ljava/lang/String;");
   mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
   mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
   mv.visitLdcInsn("---");
   mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
   mv.visitTypeInsn(NEW, "C");
   mv.visitInsn(DUP);
   mv.visitMethodInsn(INVOKESPECIAL, "C", "<init>", "()V");
   mv.visitVarInsn(ASTORE, 1);
   mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
   mv.visitVarInsn(ALOAD, 1);
   mv.visitLdcInsn("from class C's main() method");
   mv.visitMethodInsn(INVOKEVIRTUAL, "C", "tus", "(Ljava/lang/String;)Ljava/lang/String;");
   mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
   mv.visitInsn(RETURN);
   mv.visitMaxs(3, 2);
   mv.visitEnd();
}

{
   mv = cw.visitMethod(ACC_PUBLIC, "tus", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
   mv.visitCode();
   mv.visitVarInsn(ALOAD, 0);
   mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
   mv.visitInsn(DUP);
   mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
   mv.visitLdcInsn("in class C, received: [");
   mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
   mv.visitVarInsn(ALOAD, 1);
   mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
   mv.visitLdcInsn("].");
   mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
   mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
   mv.visitMethodInsn(INVOKESPECIAL, "B", "instanceSomeString", "(Ljava/lang/String;)Ljava/lang/String;");
   mv.visitInsn(ARETURN);
   mv.visitMaxs(3, 2);
   mv.visitEnd();
}

{
   mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "staticSomeString", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
   mv.visitCode();
   mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
   mv.visitInsn(DUP);
   mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
   mv.visitLdcInsn("in class C.staticSomeString(String), received: [");
   mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
   mv.visitVarInsn(ALOAD, 0);
   mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
   mv.visitLdcInsn("].");
   mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
   mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
   mv.visitInsn(ARETURN);
   mv.visitMaxs(2, 1);
   mv.visitEnd();
}

cw.visitEnd();

return cw.toByteArray();
}

}

