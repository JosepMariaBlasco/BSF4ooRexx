/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2007 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
// package org.objectweb.asm;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Opcodes;

/**
 * An empty {@link ClassVisitor} that delegates to another {@link ClassVisitor}.
 * This class can be used as a super class to quickly implement usefull class
 * adapter classes, just by overriding the necessary methods.
 *
 * @author Eric Bruneton
 */
public class ClassCruncher extends ClassAdapter implements Opcodes {

    /**
     * Constructs a new {@link ClassAdapter} object.
     *
     * @param cv the class visitor to which this adapter must delegate calls.
     */

//    public ClassCruncher(ClassReader classReader, int flags)  {super(classReader, flags);}

//    public ClassCruncher(int flags)  {super(flags);}

    ClassVisitor cv=null;

    public ClassCruncher(ClassVisitor cv)  {
        super(cv);
        this.cv=cv;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces)
    {
        // add abstract flag to class
        int newAccess= (access | ACC_ABSTRACT);
        // newAccess &= ~ACC_PUBLIC;   // turn off public bit
        cv.visit(version, newAccess, name, signature, superName, interfaces);
    }


    public MethodVisitor visitMethod(
        final int access,
        String name,
        final String desc,
        final String signature,
        final String[] exceptions)
    {
System.err.println("--> "+"visitMethod(access="+access+", name="+name+
                          ", desc="+desc+", signature="+signature+", [] exceptions="+exceptions+")");

        // return new MethodTeller(cv.visitMethod(access, name, desc, signature, exceptions));
        // return new MethodCruncher( cv.visitMethod(access, name, desc, signature, exceptions));
        MethodVisitor mv=null;

System.err.println("    "+"==> ==> ==> before new MethodCruncher(), cv="+cv.toString()+"...");

        if (name.equals("<clinit>") || name.equals("testOneDblDouble_1") || name.equals("class$"))
        {
System.err.println("    "+"  BINGO, BINGO, BINGO!");
                // let this method be copied
            // newMV=new MethodAdapter( cv.visitMethod(access, name, desc, signature, exceptions) );
            mv=cv.visitMethod(access, name, desc, signature, exceptions);
    System.err.println("    "+"<== <== <== AFTER  new MethodCruncher(), returning: mv="+mv.toString()+".");
            return mv;
            // newMV.visitCode();
        }
        else
        {
            if (! name.endsWith("init>"))
            {
                name=name+"_rgf";
            }
            // newMV=new MethodCruncher( cv.visitMethod(access | ACC_ABSTRACT, name, desc, signature, exceptions));

            mv=cv.visitMethod(access | ACC_ABSTRACT, name, desc, signature, exceptions);
    System.err.println("    "+"<== <== <== AFTER  new MethodCruncher(), returning null; mv="+mv.toString()+".");

            return null;
        }

        // return newMV;

/*
System.err.println("--> "+"BEVOR CREATION visitMethod(...)");
        MethodVisitor mvNew=new MethodTeller();
System.err.println("--> "+"BEVOR visitMethod(...)");
        mvNew.visitMethod(access, name, desc, signature, exceptions);
System.err.println("--> "+"NACH  visitMethod(...)");

        return cv.visitMethod(access, name, desc, signature, exceptions);
*/
    }

}
