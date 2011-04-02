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
// import org.objectweb.asm.ClassVisitor;
// import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;

import org.objectweb.asm.util.AbstractVisitor;

/**
 * An empty {@link MethodVisitor} that delegates to another
 * {@link MethodVisitor}. This class can be used as a super class to quickly
 * implement usefull method adapter classes, just by overriding the necessary
 * methods.
 *
 * @author Eric Bruneton
 */
public class MethodCruncher implements MethodVisitor {

    /**
     * The {@link MethodVisitor} to which this adapter delegates calls.
     */
    protected MethodVisitor mv;

    /**
     * Constructs a new {@link MethodAdapter} object.
     *
     * @param mv the code visitor to which this adapter must delegate calls.
     */
    public MethodCruncher(final MethodVisitor mv) {
        this.mv = mv;
    }

    public AnnotationVisitor visitAnnotationDefault() {

System.err.println("--> ---> "+"visitAnnotationDefault()");
return null;

//         return mv.visitAnnotationDefault();
    }

    public AnnotationVisitor visitAnnotation(
        final String desc,
        final boolean visible)
    {
System.err.println("--> ---> "+"visitAnnotation(desc="+desc+", visible="+visible+")");
return null;
//         return mv.visitAnnotation(desc, visible);
    }

    public AnnotationVisitor visitParameterAnnotation(
        final int parameter,
        final String desc,
        final boolean visible)
    {
System.err.println("--> ---> "+"visitParameterAnnotation(parameter="+parameter+
                               ", desc="+desc+", visible="+visible+")");
return null;
//         return mv.visitParameterAnnotation(parameter, desc, visible);
    }

    public void visitAttribute(final Attribute attr) {
System.err.println("--> ---> "+"visitAttribute(attr="+attr+")");
return;
//         mv.visitAttribute(attr);
    }

    public void visitCode() {
System.err.println("--> ---> "+"visitCode()");
return;
//         mv.visitCode();
    }

    public void visitFrame(
        final int type,
        final int nLocal,
        final Object[] local,
        final int nStack,
        final Object[] stack)
    {
System.err.println("--> ---> "+"visitFrame(type="+type+", nLocal="+nLocal+
                               ", local="+local+", nStack="+nStack+", stack="+stack+")");
return;
//         mv.visitFrame(type, nLocal, local, nStack, stack);
    }

    public void visitInsn(final int opcode) {
System.err.println("--> ---> "+"visitInsn(opcode="+opcode+"/"+AbstractVisitor.OPCODES[opcode]+")");
return;
//         mv.visitInsn(opcode);
    }

    public void visitIntInsn(final int opcode, final int operand) {
System.err.println("--> ---> "+"visitIntInsn(opcode="+opcode+"/"+AbstractVisitor.OPCODES[opcode]+", operand="+operand+")");
return;
//         mv.visitIntInsn(opcode, operand);
    }

    public void visitVarInsn(final int opcode, final int var) {
System.err.println("--> ---> "+"visitVarInsn(opcode="+opcode+"/"+AbstractVisitor.OPCODES[opcode]+", int="+var+")");
return;
//         mv.visitVarInsn(opcode, var);
    }

    public void visitTypeInsn(final int opcode, final String type) {
System.err.println("--> ---> "+"visitTypeInsn(opcode="+opcode+"/"+AbstractVisitor.OPCODES[opcode]+", type="+type+")");
return;
//         mv.visitTypeInsn(opcode, type);
    }

    public void visitFieldInsn(
        final int opcode,
        final String owner,
        final String name,
        final String desc)
    {
System.err.println("--> ---> "+"visitFieldInsn(opcode="+opcode+"/"+AbstractVisitor.OPCODES[opcode]+", owner="+owner+
                               ", name="+name+", desc="+desc+")");
return;
//         mv.visitFieldInsn(opcode, owner, name, desc);
    }

    public void visitMethodInsn(
        final int opcode,
        final String owner,
        final String name,
        final String desc)
    {
System.err.println("--> ---> "+"visitMethodInsn(opcode="+opcode+"/"+AbstractVisitor.OPCODES[opcode]+", owner="+owner+
                               ", name="+name+", desc="+desc+")");
return;
/*
System.err.println("          ==> ==> ==> "+"mv="+mv.toString()+"...");
        mv.visitMethodInsn(opcode, owner, name, desc);
System.err.println("          <== <== <== "+"mv="+mv.toString()+".");
*/
    }

    public void visitJumpInsn(final int opcode, final Label label) {
System.err.println("--> ---> "+"visitJumpInsn(opcode="+opcode+"/"+AbstractVisitor.OPCODES[opcode]+", label="+label+")");
return;
//         mv.visitJumpInsn(opcode, label);
    }

    public void visitLabel(final Label label) {
System.err.println("--> ---> "+"visitLabel(label="+label+")");
return;
//         mv.visitLabel(label);
    }

    public void visitLdcInsn(final Object cst) {
System.err.println("--> ---> "+"visitLdcInsn(cst="+cst+")");
return;
//         mv.visitLdcInsn(cst);
    }

    public void visitIincInsn(final int var, final int increment) {
System.err.println("--> ---> "+"visitIincInsn(var="+var+", increment="+increment+")");
return;
//         mv.visitIincInsn(var, increment);
    }

    public void visitTableSwitchInsn(
        final int min,
        final int max,
        final Label dflt,
        final Label[] labels)
    {
System.err.println("--> ---> "+"visitTableSwitchInsn(min="+min+", max="+max+
                               ", dflt="+dflt+", labels="+labels+")");
return;
//         mv.visitTableSwitchInsn(min, max, dflt, labels);
    }

    public void visitLookupSwitchInsn(
        final Label dflt,
        final int[] keys,
        final Label[] labels)
    {
System.err.println("--> ---> "+"visitLookupSwitchInsn(dflt="+dflt+", keys="+keys+", labels="+labels+")");
return;
//         mv.visitLookupSwitchInsn(dflt, keys, labels);
    }

    public void visitMultiANewArrayInsn(final String desc, final int dims) {
System.err.println("--> ---> "+"visitMultiANewArrayInsn(desc="+desc+", dims="+dims+")");
return;
//         mv.visitMultiANewArrayInsn(desc, dims);
    }

    public void visitTryCatchBlock(
        final Label start,
        final Label end,
        final Label handler,
        final String type)
    {
System.err.println("--> ---> "+"visitTryCatchBlock(start="+start+", end="+end+
                               ", handler="+handler+", type="+type+")");
return;
//         mv.visitTryCatchBlock(start, end, handler, type);
    }

    public void visitLocalVariable(
        final String name,
        final String desc,
        final String signature,
        final Label start,
        final Label end,
        final int index)
    {
System.err.println("--> ---> "+"visitLocalVariable(name="+name+", desc="+desc+
                               ", signature="+signature+", start="+start+", end="+end+
                               ", index="+index+")");
return;
//         mv.visitLocalVariable(name, desc, signature, start, end, index);
    }

    public void visitLineNumber(final int line, final Label start) {
System.err.println("--> ---> "+"visitLineNumber(line="+line+", start="+start+")");
return;
//         mv.visitLineNumber(line, start);
    }

    public void visitMaxs(final int maxStack, final int maxLocals) {
System.err.println("--> ---> "+"visitMaxs(maxStack="+maxStack+", maxLocals="+maxLocals+")");
return;
//         mv.visitMaxs(maxStack, maxLocals);
    }

    public void visitEnd() {
System.err.println("--> ---> "+"visitEnd()");
return;
//         mv.visitEnd();
    }
}
