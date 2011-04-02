import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.attrs.*;
public class ABC_InterfaceDump implements Opcodes {

public static byte[] dump () throws Exception {

ClassWriter cw = new ClassWriter(0);
FieldVisitor fv;
MethodVisitor mv;
AnnotationVisitor av0;

cw.visit(V1_5, ACC_ABSTRACT + ACC_INTERFACE, "ABC_Interface", null, "java/lang/Object", null);

{
   fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "int_abc", "I", null, new Integer(19));
   fv.visitEnd();
}

{
   fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "long_abc", "J", null, new Long(23L));
   fv.visitEnd();
}

{
   fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "short_abc", "S", null, new Integer(11));
   fv.visitEnd();
}

{
   fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "string_abc", "Ljava/lang/String;", null, "abc");
   fv.visitEnd();
}

{
   fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "object_abc", "Ljava/lang/Object;", null, null);
   fv.visitEnd();
}

{
   mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "testVoid", "()V", null, new String[] { "java/lang/Throwable" });
   mv.visitEnd();
}

{
   mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "testPrimitiveBooleanVoid_1", "(Z)V", null, new String[] { "java/lang/Throwable" });
   mv.visitEnd();
}

{
   mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "testBooleanVoid_1", "(Ljava/lang/Boolean;)V", null, new String[] { "java/lang/Throwable" });
   mv.visitEnd();
}

{
   mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
   mv.visitCode();
   mv.visitTypeInsn(NEW, "java/lang/Object");
   mv.visitInsn(DUP);
   mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
   mv.visitFieldInsn(PUTSTATIC, "ABC_Interface", "object_abc", "Ljava/lang/Object;");
   mv.visitInsn(RETURN);
   mv.visitMaxs(2, 0);
   mv.visitEnd();
}

cw.visitEnd();

return cw.toByteArray();
}

}

