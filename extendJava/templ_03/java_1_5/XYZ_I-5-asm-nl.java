import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.attrs.*;
public class XYZ_InterfaceDump implements Opcodes {

public static byte[] dump () throws Exception {

ClassWriter cw = new ClassWriter(0);
FieldVisitor fv;
MethodVisitor mv;
AnnotationVisitor av0;

cw.visit(V1_5, ACC_ABSTRACT + ACC_INTERFACE, "XYZ_Interface", null, "java/lang/Object", null);

{
   fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "float_xyz", "F", null, new Float("19.91"));
   fv.visitEnd();
}

{
   fv = cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "double_abc", "D", null, new Double("23.32"));
   fv.visitEnd();
}

{
   mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "testPrimitiveBooleanPrimitiveBoolean_1", "(Z)Z", null, new String[] { "java/lang/Throwable" });
   mv.visitEnd();
}

{
   mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "testBooleanBoolean_1", "(Ljava/lang/Boolean;)Ljava/lang/Boolean;", null, new String[] { "java/lang/Throwable" });
   mv.visitEnd();
}

{
   mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "TestPrimitivesObject", "(ZBCSIJFDLjava/lang/String;)Ljava/lang/Object;", null, new String[] { "java/lang/Throwable" });
   mv.visitEnd();
}

{
   mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "testBooleanVoid_1", "(Ljava/lang/Boolean;)V", null, new String[] { "java/lang/Throwable" });
   mv.visitEnd();
}

cw.visitEnd();

return cw.toByteArray();
}

}

