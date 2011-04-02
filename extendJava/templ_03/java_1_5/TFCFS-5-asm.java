import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.attrs.*;
public class TestFromCFSDump implements Opcodes {

public static byte[] dump () throws Exception {

ClassWriter cw = new ClassWriter(0);
FieldVisitor fv;
MethodVisitor mv;
AnnotationVisitor av0;

cw.visit(V1_5, ACC_SUPER, "TestFromCFS", null, "TestConstrFromSubclass", null);

{
mv = cw.visitMethod(0, "<init>", "(Lorg/rexxla/bsf/engines/rexx/RexxProxy;)V", null, null);
mv.visitCode();
mv.visitVarInsn(ALOAD, 0);
mv.visitVarInsn(ALOAD, 1);
mv.visitMethodInsn(INVOKESPECIAL, "TestConstrFromSubclass", "<init>", "(Lorg/rexxla/bsf/engines/rexx/RexxProxy;)V");
mv.visitInsn(RETURN);
mv.visitMaxs(2, 2);
mv.visitEnd();
}
{
mv = cw.visitMethod(0, "testRgf", "()V", null, null);
mv.visitCode();
mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
mv.visitInsn(DUP);
mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
mv.visitLdcInsn("oha [So, da, la.] null instanceof String: ");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
mv.visitInsn(ACONST_NULL);
mv.visitTypeInsn(INSTANCEOF, "java/lang/String");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Z)Ljava/lang/StringBuilder;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
mv.visitInsn(RETURN);
mv.visitMaxs(3, 1);
mv.visitEnd();
}
cw.visitEnd();

return cw.toByteArray();
}
}
