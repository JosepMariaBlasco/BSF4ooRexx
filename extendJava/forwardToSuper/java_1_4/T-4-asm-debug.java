import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.attrs.*;
public class TestDump implements Opcodes {

public static byte[] dump () throws Exception {

ClassWriter cw = new ClassWriter(0);
FieldVisitor fv;
MethodVisitor mv;
AnnotationVisitor av0;

cw.visit(V1_2, ACC_PUBLIC + ACC_SUPER, "Test", null, "java/lang/Object", null);

cw.visitSource("Test.java", null);

{
mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
mv.visitCode();
Label l0 = new Label();
mv.visitLabel(l0);
mv.visitLineNumber(1, l0);
mv.visitVarInsn(ALOAD, 0);
mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
mv.visitInsn(RETURN);
mv.visitMaxs(1, 1);
mv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
mv.visitCode();
Label l0 = new Label();
mv.visitLabel(l0);
mv.visitLineNumber(4, l0);
mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
mv.visitLdcInsn("in Test.main(), static...");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
Label l1 = new Label();
mv.visitLabel(l1);
mv.visitLineNumber(6, l1);
mv.visitMethodInsn(INVOKESTATIC, "Test", "aha", "()V");
Label l2 = new Label();
mv.visitLabel(l2);
mv.visitLineNumber(7, l2);
mv.visitInsn(RETURN);
mv.visitMaxs(2, 1);
mv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "aha", "()V", null, null);
mv.visitCode();
Label l0 = new Label();
mv.visitLabel(l0);
mv.visitLineNumber(11, l0);
mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
mv.visitLdcInsn("in Test.aha(), static...");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
Label l1 = new Label();
mv.visitLabel(l1);
mv.visitLineNumber(12, l1);
mv.visitInsn(RETURN);
mv.visitMaxs(2, 0);
mv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PUBLIC, "aha", "(I)V", null, null);
mv.visitCode();
Label l0 = new Label();
mv.visitLabel(l0);
mv.visitLineNumber(16, l0);
mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
mv.visitTypeInsn(NEW, "java/lang/StringBuffer");
mv.visitInsn(DUP);
mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");
mv.visitLdcInsn("in Test.aha(int i), instance: i=");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
mv.visitVarInsn(ILOAD, 1);
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(I)Ljava/lang/StringBuffer;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
Label l1 = new Label();
mv.visitLabel(l1);
mv.visitLineNumber(17, l1);
mv.visitInsn(RETURN);
mv.visitMaxs(3, 2);
mv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PUBLIC, "aha", "(Ljava/lang/Integer;)V", null, null);
mv.visitCode();
Label l0 = new Label();
mv.visitLabel(l0);
mv.visitLineNumber(21, l0);
mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
mv.visitTypeInsn(NEW, "java/lang/StringBuffer");
mv.visitInsn(DUP);
mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");
mv.visitLdcInsn("in Test.aha(Integer i), instance: i=");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
mv.visitVarInsn(ALOAD, 1);
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuffer;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
Label l1 = new Label();
mv.visitLabel(l1);
mv.visitLineNumber(22, l1);
mv.visitInsn(RETURN);
mv.visitMaxs(3, 2);
mv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PUBLIC, "hashCode", "()I", null, null);
mv.visitCode();
Label l0 = new Label();
mv.visitLabel(l0);
mv.visitLineNumber(26, l0);
mv.visitVarInsn(ALOAD, 0);
mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "identityHashCode", "(Ljava/lang/Object;)I");
mv.visitVarInsn(ISTORE, 1);
Label l1 = new Label();
mv.visitLabel(l1);
mv.visitLineNumber(27, l1);
mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
mv.visitTypeInsn(NEW, "java/lang/StringBuffer");
mv.visitInsn(DUP);
mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");
mv.visitLdcInsn("in Test.hashCode(), instance: hashCode=");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
mv.visitVarInsn(ILOAD, 1);
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(I)Ljava/lang/StringBuffer;");
mv.visitLdcInsn("...");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
Label l2 = new Label();
mv.visitLabel(l2);
mv.visitLineNumber(28, l2);
mv.visitVarInsn(ILOAD, 1);
mv.visitInsn(IRETURN);
mv.visitMaxs(3, 2);
mv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PUBLIC, "mno", "(I)V", null, null);
mv.visitCode();
Label l0 = new Label();
mv.visitLabel(l0);
mv.visitLineNumber(34, l0);
mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
mv.visitTypeInsn(NEW, "java/lang/StringBuffer");
mv.visitInsn(DUP);
mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");
mv.visitLdcInsn("in Test.mno(int i), instance: i=");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
mv.visitVarInsn(ILOAD, 1);
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(I)Ljava/lang/StringBuffer;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
Label l1 = new Label();
mv.visitLabel(l1);
mv.visitLineNumber(35, l1);
mv.visitInsn(RETURN);
mv.visitMaxs(3, 2);
mv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PUBLIC, "xyz", "(Ljava/lang/Integer;)V", null, null);
mv.visitCode();
Label l0 = new Label();
mv.visitLabel(l0);
mv.visitLineNumber(39, l0);
mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
mv.visitTypeInsn(NEW, "java/lang/StringBuffer");
mv.visitInsn(DUP);
mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");
mv.visitLdcInsn("in Test.xyz(Integer i), instance: i=");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
mv.visitVarInsn(ALOAD, 1);
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuffer;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
Label l1 = new Label();
mv.visitLabel(l1);
mv.visitLineNumber(40, l1);
mv.visitInsn(RETURN);
mv.visitMaxs(3, 2);
mv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "static_mno", "(I)V", null, new String[] { "java/lang/RuntimeException" });
mv.visitCode();
Label l0 = new Label();
mv.visitLabel(l0);
mv.visitLineNumber(45, l0);
mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
mv.visitTypeInsn(NEW, "java/lang/StringBuffer");
mv.visitInsn(DUP);
mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");
mv.visitLdcInsn("in Test.static_mno(int i), arg: i=");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
mv.visitVarInsn(ILOAD, 0);
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(I)Ljava/lang/StringBuffer;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
Label l1 = new Label();
mv.visitLabel(l1);
mv.visitLineNumber(46, l1);
mv.visitInsn(RETURN);
mv.visitMaxs(3, 1);
mv.visitEnd();
}
{
mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "static_xyz", "(Ljava/lang/Integer;)V", null, null);
mv.visitCode();
Label l0 = new Label();
mv.visitLabel(l0);
mv.visitLineNumber(50, l0);
mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
mv.visitTypeInsn(NEW, "java/lang/StringBuffer");
mv.visitInsn(DUP);
mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");
mv.visitLdcInsn("in Test.static_xyz(Integer i), arg: i=");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
mv.visitVarInsn(ALOAD, 0);
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuffer;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
Label l1 = new Label();
mv.visitLabel(l1);
mv.visitLineNumber(51, l1);
mv.visitInsn(RETURN);
mv.visitMaxs(3, 1);
mv.visitEnd();
}
cw.visitEnd();

return cw.toByteArray();
}
}
