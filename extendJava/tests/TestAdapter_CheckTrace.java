// import org.objectweb.asm.*;
// import org.objectweb.asm.util.*;

import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.CheckClassAdapter;
import java.io.PrintWriter;



// import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
// import org.objectweb.asm.MethodAdapter;
// import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
// import org.objectweb.asm.Type;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;


class TestAdapter_CheckTrace // extends ClassAdapter implements Opcodes
{
    public static void main (String args[]) throws java.io.FileNotFoundException, java.lang.ClassNotFoundException
    {
        String name=args[0];        // filename
        doTheWork1(name);
        System.out.println("---");
        // doTheWork2(name);
    }


    static void doTheWork1(String name) throws java.io.FileNotFoundException, java.lang.ClassNotFoundException
    {
    // gets an input stream to read the bytecode of the class
        InputStream is =new FileInputStream(name);
        byte[] b;

        // adapts the class on the fly
        try {
            ClassReader cr = new ClassReader(is);


            ClassWriter        cw = new ClassWriter(0);
//            TraceClassVisitor tcv = new TraceClassVisitor(cw, new PrintWriter(System.out));
//            CheckClassAdapter cca = new CheckClassAdapter(cw);
            ClassCruncher     ccr = new ClassCruncher(cw);

            // ClassWriter cw = new ClassWriter(ct);

            // TraceClassVisitor tcv = new TraceClassVisitor(cw, new PrintWriter(System.out));
            // TraceClassVisitor tcv = new TraceClassVisitor(ct, new PrintWriter(System.out));

            // CheckClassAdapter cca = new CheckClassAdapter(tcv);



            // ??
            // cr.accept(cca, 0);
            cr.accept(ccr, 0);

            b = cw.toByteArray();
        } catch (Exception e) {
            throw new ClassNotFoundException(name, e);
        }

        // optional: stores the adapted class on disk
        try {
            String newName=name.substring(0, name.lastIndexOf('.'))+"Adapted.class";
            // FileOutputStream fos = new FileOutputStream(name + ".adapted");
            FileOutputStream fos = new FileOutputStream(newName);
            fos.write(b);
            fos.close();
            System.out.println("adapted class written to: ["+newName+"]");
        } catch (Exception e) {
        }

        // returns the adapted class
        // return defineClass(name, b, 0, b.length);
    }


    static void doTheWork2(String name) throws java.io.FileNotFoundException, java.lang.ClassNotFoundException
    {
    // gets an input stream to read the bytecode of the class
        InputStream is =new FileInputStream(name);
        byte[] b;

        // adapts the class on the fly
        try {
            ClassReader cr = new ClassReader(is);

            ClassWriter cw = new ClassWriter(0);
            CheckClassAdapter cca = new CheckClassAdapter(cw);
            TraceClassVisitor tcv = new TraceClassVisitor(cca, new PrintWriter(System.out));


            // ??
            cr.accept(tcv, 0);

            b = cw.toByteArray();
        } catch (Exception e) {
            throw new ClassNotFoundException(name, e);
        }

        // optional: stores the adapted class on disk
        try {
            FileOutputStream fos = new FileOutputStream(name + ".adapted");
            fos.write(b);
            fos.close();
        } catch (Exception e) {
        }

        // returns the adapted class
        // return defineClass(name, b, 0, b.length);
    }
}


