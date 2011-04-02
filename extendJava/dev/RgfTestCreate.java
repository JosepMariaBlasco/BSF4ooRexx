
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

// import org.objectweb.asm.util.CheckMethodAdapter;
import java.io.FileOutputStream;
import java.io.PrintWriter;

class RgfTestCreate
{

    // possibility to determine class version for current JVM
    final static public int currentClassVersion=Float.valueOf(System.getProperty("java.class.version")).intValue();

    public static void main (String args[]) throws java.io.IOException
    {
/*
    public static final int 	V1_1 	196653
    public static final int 	V1_2 	46
    public static final int 	V1_3 	47
    public static final int 	V1_4 	48
    public static final int 	V1_5 	49
    public static final int 	V1_6 	50   // frames !
    public static final int 	V1_7 	51
*/
        // code to determine class version, used to determine whether frames should be computed (> V1_5)

        int    classVersion=Float.valueOf(System.getProperty("java.class.version")).intValue();
        System.err.println("System.getProperty(\"java.class.version\"=["+System.getProperty("java.class.version")+"], (int) classVersion="+classVersion+"\n=============");

// force class version to 1.4
classVersion=Opcodes.V1_4;

        String clzName="TestAbstract";      // class to work with
        // clzName="TestSub";
        String clzNewName="";
        String extension="_$RexxExtendClass$_"+(new Object()).hashCode();
        clzNewName=clzName+extension;
            // TODO: remove the following statement (just there for easying debugging)
        clzNewName=clzName+"_dynamicallyExtended_abc";


        // COMPUTE_MAXS:    +10% overhead
        // COMPUTE_FRAMES: +100% overhead (but calcs MAXS as well)
        // although relatively slow, it still makes things simpler:
        // ClassWriter       cw =new ClassWriter(classVersion>=50 ? ClassWriter.COMPUTE_FRAMES : ClassWriter.COMPUTE_MAXS);
        ClassWriter       cw =new ClassWriter(classVersion>=50 ? ClassWriter.COMPUTE_FRAMES : 0);
        // ClassWriter       cw =new ClassWriter(ClassWriter.COMPUTE_MAXS);
        // ClassWriter       cw =new ClassWriter(0);   // we compute max stack and max local var ourselves


        CheckClassAdapter cca=new CheckClassAdapter(cw);    // check class

        PrintWriter        pw=new PrintWriter(System.out);
        TraceClassVisitor tcv=new TraceClassVisitor(cca, pw);    // a printWriter
        // TraceClassVisitor tcv=new TraceClassVisitor(cw, pw);    // a printWriter

        // RexxExtendClass    rec=new RexxExtendClass(tcv);   // only abstract methods
        String []methNames={ "test_concrete_Void_ManyArguments", "test_concrete_VoidVoid", "test_concrete_boolean_boolean"  };   // will catch all methods by that name, irrespectible of case and desc


        // RexxExtendClass    rec=new RexxExtendClass(cw);
        // RexxExtendClass    rec=new RexxExtendClass(tcv, methNames);
        // RexxExtendClass    rec=new RexxExtendClass(tcv, null);    // process all public concrete methods

        // - use this JVM's class version possibly overriding the source's class version
        // RexxExtendClass    rec=new RexxExtendClass(tcv, clzNewName, classVersion, new String[0]);    // process all public concrete methods
        RexxExtendClass    rec=new RexxExtendClass(tcv, clzNewName, classVersion);    // process all public concrete methods

        ClassReader       cr =new ClassReader(clzName);
        cr.accept(rec, 0);


        byte [] b=cw.toByteArray();     // get bytecodes


        // optional: stores the adapted class on disk
        try {
            // FileOutputStream fos = new FileOutputStream(clzName + "_proxied.class");
            FileOutputStream fos = new FileOutputStream(rec.getTranData().newClassName+".class");
            fos.write(b);
            fos.close();
        } catch (Exception e) {
        }

    }

}
