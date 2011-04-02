
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

// import org.objectweb.asm.util.CheckMethodAdapter;
import java.io.FileOutputStream;
import java.io.PrintWriter;

class RgfTestCreate
{

    public static void main (String args[]) throws java.io.IOException
    {
        String clzName="TestAbstract";      // class to work with

        // COMPUTE_MAXS:    +10% overhead
        // COMPUTE_FRAMES: +100% overhead (but calcs MAXS as well)
        // although relatively slow, it still makes things simpler:
        // ClassWriter       cw =new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        // ClassWriter       cw =new ClassWriter(ClassWriter.COMPUTE_MAXS);
        ClassWriter       cw =new ClassWriter(ClassWriter.COMPUTE_FRAMES);


        CheckClassAdapter cca=new CheckClassAdapter(cw);    // check class

        PrintWriter        pw=new PrintWriter(System.out);
        TraceClassVisitor tcv=new TraceClassVisitor(cca, pw);    // a printWriter
        // TraceClassVisitor tcv=new TraceClassVisitor(cw, pw);    // a printWriter

        RgfExtendClass    rec=new RgfExtendClass(tcv);
        // RgfExtendClass    rec=new RgfExtendClass(cw);

        ClassReader       cr =new ClassReader(clzName);
        cr.accept(rec, 0);


        byte [] b=cw.toByteArray();     // get bytecodes


        // optional: stores the adapted class on disk
        try {
            FileOutputStream fos = new FileOutputStream(clzName + ".proxied");
            fos.write(b);
            fos.close();
        } catch (Exception e) {
        }

    }

}
