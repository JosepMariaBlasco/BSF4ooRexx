import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.attrs.*;
public class TestFromCFSDump implements Opcodes {

public static byte[] dump () throws Exception {

ClassWriter cw = new ClassWriter(0);
FieldVisitor fv;
MethodVisitor mv;
AnnotationVisitor av0;

cw.visit(V1_2, ACC_SUPER, "TestFromCFS", null, "TestConstrFromSubclass", null);

cw.visitSource("TestFromCFS.java", null);

{
mv = cw.visitMethod(0, "<init>", "(Lorg/rexxla/bsf/engines/rexx/RexxProxy;)V", null, null);
mv.visitCode();
Label l0 = new Label();
mv.visitLabel(l0);
mv.visitLineNumber(7, l0);
mv.visitVarInsn(ALOAD, 0);
mv.visitVarInsn(ALOAD, 1);
mv.visitMethodInsn(INVOKESPECIAL, "TestConstrFromSubclass", "<init>", "(Lorg/rexxla/bsf/engines/rexx/RexxProxy;)V");
Label l1 = new Label();
mv.visitLabel(l1);
mv.visitLineNumber(8, l1);
mv.visitInsn(RETURN);
mv.visitMaxs(2, 2);
mv.visitEnd();
}
{
mv = cw.visitMethod(0, "testRgf", "()V", null, null);
mv.visitCode();
Label l0 = new Label();
mv.visitLabel(l0);
mv.visitLineNumber(12, l0);
mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
mv.visitTypeInsn(NEW, "java/lang/StringBuffer");
mv.visitInsn(DUP);
mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "()V");
mv.visitLdcInsn("oha [So, da, la.] null instanceof String: ");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
mv.visitInsn(ACONST_NULL);
mv.visitTypeInsn(INSTANCEOF, "java/lang/String");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Z)Ljava/lang/StringBuffer;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
Label l1 = new Label();
mv.visitLabel(l1);
mv.visitLineNumber(13, l1);
mv.visitInsn(RETURN);
mv.visitMaxs(3, 1);
mv.visitEnd();
}
cw.visitEnd();

return cw.toByteArray();
}
}
