package org.rexxla.bsf.engines.rexx;

// author: Rony G. Flatscher, (c) July 2009
// code runs on Java 1.4 as well



import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;


/** Java class which allows extending (abstract and concrete) Java classes on the fly such that
 * the methods in the extended class will forward the method invocation to a RexxProxy; the extended
 * class will be loaded and returned.
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2009 Rony G. Flatscher
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ----------------------------------------------------------------------------- </pre>
 *
 * @author Rony G. Flatscher (<a href="http://www.wu.ac.at">WU Vienna </a>, <a href="http://www.wu-wien.ac.at/english">http://www.wu-wien.ac.at/english</a>)
 * @version 1.0
 * @since   2009-07-29
 */

public class RexxExtendingClassLoader extends ClassLoader {

    /** Gets and stores the class version of the JVM. This determines the default class version
     *  of the extended, dynamically created classes.
     */
    final static int jvmClassVersion=Float.valueOf(System.getProperty("java.class.version")).intValue();


    /** Reads the given class and generates a new, extended which forwards all method invocations
     *  to a RexxProxy.
     */
    public synchronized Class generateAndLoadExtendedClass (
            final String clzName
        )
            throws ClassNotFoundException
    {
        // create InputStream to allow reading the bytecode
        String     res=clzName.replace('.', '/') + ".class";
        String     clzNewName=null;     // let the extender define a suitable new name
        InputStream is=getResourceAsStream(res);
        byte[] b;

        // create an extended class forwarding invocations to RexxProxy
        try
        {
            ClassReader cr = new ClassReader(is);
            int targetClassVersion=jvmClassVersion;  // use the JVM's class version

            ClassWriter cw = new ClassWriter( (targetClassVersion>49
                                                 ? ClassWriter.COMPUTE_FRAMES // Java 1.6 or higher, let asm compute frames
                                                 : 0
                                               )    // do not compute anything (neither MAXS nor FRAMES)
                                            );

            // ClassVisitor that extends
            RexxExtendClass rec=new RexxExtendClass(cw, clzNewName, targetClassVersion);

            cr.accept(rec, 0);
            b = cw.toByteArray();
        }
        catch (Exception e)
        {
            throw new ClassNotFoundException(clzName, e);
        }

        // define and return the newly extended class
        return defineClass(clzNewName, b, 0, b.length);
    }



    /** Reads the given class and generates a new, extended which forwards all method invocations
     *  to a RexxProxy.
     */
    public synchronized Class generateAndLoadExtendedClass (
            final String clzName,
            final String clzNewName,
            final int    classVersion
        )
            throws ClassNotFoundException
    {
        // create InputStream to allow reading the bytecode
        String     res=clzName.replace('.', '/') + ".class";
        InputStream is=getResourceAsStream(res);
        byte[] b;

        // create an extended class forwarding invocations to RexxProxy
        try
        {
            ClassReader cr = new ClassReader(is);
            int targetClassVersion=(classVersion==0       // no class version given
                                        ? jvmClassVersion  // use the JVM's class version
                                        : classVersion     // pass on (will use
                                    );

            ClassWriter cw = new ClassWriter( (targetClassVersion>49
                                                 ? ClassWriter.COMPUTE_FRAMES // Java 1.6 or higher, let asm compute frames
                                                 : 0
                                              )     // do not compute anything (neither MAXS nor FRAMES)
                                            );

            // ClassVisitor that extends
            RexxExtendClass rec=new RexxExtendClass(cw, clzNewName, targetClassVersion);

            cr.accept(rec, 0);
            b = cw.toByteArray();
        }
        catch (Exception e)
        {
            throw new ClassNotFoundException(clzName, e);
        }

        // define and return the newly extended class
        return defineClass(clzNewName, b, 0, b.length);
    }



    /** Reads the given class and generates a new, extended which forwards all method invocations
     *  to a RexxProxy.
     */
    public synchronized Class generateAndLoadExtendedClass (
            final String clzName,
            final String clzNewName,
            final int    classVersion,
            final String [] methodNamesToAdd
        )
            throws ClassNotFoundException
    {
        // create InputStream to allow reading the bytecode
        String     res=clzName.replace('.', '/') + ".class";
        InputStream is=getResourceAsStream(res);
        byte[] b;

        // create an extended class forwarding invocations to RexxProxy
        try
        {
            ClassReader cr = new ClassReader(is);
            int targetClassVersion=(classVersion==0       // no class version given
                                        ? jvmClassVersion  // use the JVM's class version
                                        : classVersion     // pass on (will use
                                    );

            ClassWriter cw = new ClassWriter( (targetClassVersion>49
                                                 ? ClassWriter.COMPUTE_FRAMES // Java 1.6 or higher, let asm compute frames
                                                 : 0
                                              )     // do not compute anything (neither MAXS nor FRAMES)
                                            );

            // ClassVisitor that extends
            RexxExtendClass rec=new RexxExtendClass(cw, clzNewName, targetClassVersion, methodNamesToAdd);

            cr.accept(rec, 0);
            b = cw.toByteArray();
        }
        catch (Exception e)
        {
            throw new ClassNotFoundException(clzName, e);
        }

        // define and return the newly extended class
        return defineClass(clzNewName, b, 0, b.length);
    }


    /** Allows to supply the Java class name, the extended (new) name to use and the desired
     *  class version of the newly created extended class file.
     */
    public static void main(final String args[]) throws Exception
    {
        // code to determine class version, used to determine whether frames should be computed (> V1_5)
        int    classVersion=Float.valueOf(System.getProperty("java.class.version")).intValue();
        // System.err.println("main(): ["+System.getProperty("java.class.version")+"], classVersion="+classVersion+"\n===");

        String clzName=null;                // class to work with
        String clzNewName=null;

        if (args.length==0)
        {
            System.err.println("Argument className is missing, usage:\n"+
                               "java RexxExtendingClassLoader className [classNameForStoring [int_classVersion]]");
            System.exit(-1);
        }

        if (args.length>0)  {clzName   =args[0];}   // source name (without ".class")
        else {clzName="TestAbstract";}              // for debugging purposes

        if (args.length>1)  {clzNewName=args[1];}   // target name (without ".class")

        if (args.length>2)                          // force target class version
        {
            classVersion=Float.valueOf(args[2]).intValue();
            System.err.println("main(): now forcing class version to: ["+classVersion+"] ('0'=use source class format)!");
        }

// System.err.println("===> ===> ===> clzName=["+clzName+"], clzNewName=["+clzNewName+"], classVersion="+classVersion);

        // according to asm 3.2 docs as of 2009-07-29:
        // COMPUTE_MAXS:    +10% overhead
        // COMPUTE_FRAMES: +100% overhead (calcs MAXS as well)

        ClassWriter       cw =new ClassWriter(classVersion>=50 ? ClassWriter.COMPUTE_FRAMES : 0);

        CheckClassAdapter cca=new CheckClassAdapter(cw);    // check class

        // - use this JVM's class version possibly overriding the source's class version
        RexxExtendClass    rec=new RexxExtendClass(cca, clzNewName, classVersion, new String[0]);    // process all public concrete methods

        ClassReader       cr =new ClassReader(clzName);
        cr.accept(rec, 0);

        byte [] b=cw.toByteArray();     // get bytecodes

        // optional: stores the adapted class on disk
        try {
            // FileOutputStream fos = new FileOutputStream(clzName + "_proxied.class");
            FileOutputStream fos = new FileOutputStream(rec.getTranData().newClassName+".class");
            fos.write(b);
            fos.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("\trec.getTranData().newClassName="+rec.getTranData().newClassName);
            System.err.println("\t                    clzNewName="+clzNewName);
        }
    }
}
