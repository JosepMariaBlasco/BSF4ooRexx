import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import org.rexxla.bsf.engines.rexx.RexxException;
import org.apache.bsf.BSFException;


/** Extends a supplied class and creates proxy methods (dispatching to RexxProxy objects),
 *  if the Java methods are defined as abstract or if a list of Java methods to proxy gets
 *  supplied in the constructor.
 *
 * @author Rony G. Flatscher
 */
public class RgfExtendClass extends ClassAdapter implements ClassVisitor, Opcodes
{

    /**
     * The {@link ClassVisitor} to which this adapter delegates calls.
     */
    protected ClassVisitor cv;

    final static protected boolean bDebugClassVisitor=true;
    final static protected boolean bDebugMethodVisitor=true;

    final static String thisPgmName="RgfExtendClass";

    final static String rexxExceptionName="org/rexxla/bsf/engines/rexx/RexxException";
    final static String rexxProxyName    ="org/rexxla/bsf/engines/rexx/RexxProxy";

    // map primitive types to corresponding wrapper classes
    final static HashMap type2wrapperClass= new HashMap();

    // map primitive types to corresponding method description of static method valueOf
    final static HashMap type2valueOfDesc= new HashMap();

    // map primitive types to corresponding method description for the constructor ("<init>")
    final static HashMap type2initDesc= new HashMap();

    // map primitive types to corresponeding method and method desc which returns the primitive value
    final static HashMap type2getPrimitiveValue= new HashMap();

    // map primitive types to corresponding static synthetic wrapper class fields
    final static HashMap type2syntheticWrapperFields= new HashMap();

    // set of primitive types
    final static HashSet primitiveTypes=new HashSet();

    // array of ICONST_ opcodes used for denoting index positions in arrays
    final static int []arr_ICONST={ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5};

    static
    {
        // define mapping between primitive types and corresponding wrapper class
        type2wrapperClass.put(Type.BOOLEAN_TYPE, "java/lang/Boolean"  );
        type2wrapperClass.put(Type.BYTE_TYPE   , "java/lang/Byte"     );
        type2wrapperClass.put(Type.CHAR_TYPE   , "java/lang/Character");
        type2wrapperClass.put(Type.SHORT_TYPE  , "java/lang/Short"    );
        type2wrapperClass.put(Type.INT_TYPE    , "java/lang/Integer"  );
        type2wrapperClass.put(Type.LONG_TYPE   , "java/lang/Long"     );
        type2wrapperClass.put(Type.FLOAT_TYPE  , "java/lang/Float"    );
        type2wrapperClass.put(Type.DOUBLE_TYPE , "java/lang/Double"   );

        type2valueOfDesc.put(Type.BOOLEAN_TYPE, "(Z)Ljava/lang/Boolean;"  );
        type2valueOfDesc.put(Type.BYTE_TYPE   , "(B)Ljava/lang/Byte;"     );
        type2valueOfDesc.put(Type.CHAR_TYPE   , "(C)Ljava/lang/Character;");
        type2valueOfDesc.put(Type.SHORT_TYPE  , "(S)Ljava/lang/Short;"    );
        type2valueOfDesc.put(Type.INT_TYPE    , "(I)Ljava/lang/Integer;"  );
        type2valueOfDesc.put(Type.LONG_TYPE   , "(J)Ljava/lang/Long;"     );
        type2valueOfDesc.put(Type.FLOAT_TYPE  , "(F)Ljava/lang/Float;"    );
        type2valueOfDesc.put(Type.DOUBLE_TYPE , "(D)Ljava/lang/Double;"   );

        // define method desc for <init> of wrapper types
        type2initDesc.put(Type.BOOLEAN_TYPE, "(Z)V");
        type2initDesc.put(Type.BYTE_TYPE   , "(B)V");
        type2initDesc.put(Type.CHAR_TYPE   , "(C)V");
        type2initDesc.put(Type.SHORT_TYPE  , "(S)V");
        type2initDesc.put(Type.INT_TYPE    , "(I)V");
        type2initDesc.put(Type.LONG_TYPE   , "(J)V");
        type2initDesc.put(Type.FLOAT_TYPE  , "(F)V");
        type2initDesc.put(Type.DOUBLE_TYPE , "(D)V");

        // map to method name and method desc:                     methName: [0]       methDesc: [1]
        type2getPrimitiveValue.put(Type.BOOLEAN_TYPE, new String[] { "booleanValue"  , "()Z"}  );
        type2getPrimitiveValue.put(Type.BYTE_TYPE   , new String[] { "byteValue"     , "()B"}  );
        type2getPrimitiveValue.put(Type.CHAR_TYPE   , new String[] { "charValue"     , "()C"}  );
        type2getPrimitiveValue.put(Type.SHORT_TYPE  , new String[] { "shortValue"    , "()S"}  );
        type2getPrimitiveValue.put(Type.INT_TYPE    , new String[] { "intValue"      , "()I"}  );
        type2getPrimitiveValue.put(Type.LONG_TYPE   , new String[] { "longValue"     , "()J"}  );
        type2getPrimitiveValue.put(Type.FLOAT_TYPE  , new String[] { "floatValue"    , "()F"}  );
        type2getPrimitiveValue.put(Type.DOUBLE_TYPE , new String[] { "doubleValue"   , "()D"}  );


        // define primitive types
        primitiveTypes.add(Type.BOOLEAN_TYPE);
        primitiveTypes.add(Type.BYTE_TYPE   );
        primitiveTypes.add(Type.CHAR_TYPE   );
        primitiveTypes.add(Type.SHORT_TYPE  );
        primitiveTypes.add(Type.INT_TYPE    );
        primitiveTypes.add(Type.LONG_TYPE   );
        primitiveTypes.add(Type.FLOAT_TYPE  );
        primitiveTypes.add(Type.DOUBLE_TYPE );

        // < V1_5: define mapping between primitive types and corresponding synthetic static wrapper class fields
        type2syntheticWrapperFields.put(Type.BOOLEAN_TYPE, "class$java$lang$Boolean"  );
        type2syntheticWrapperFields.put(Type.BYTE_TYPE   , "class$java$lang$Byte"     );
        type2syntheticWrapperFields.put(Type.CHAR_TYPE   , "class$java$lang$Character");
        type2syntheticWrapperFields.put(Type.SHORT_TYPE  , "class$java$lang$Short"    );
        type2syntheticWrapperFields.put(Type.INT_TYPE    , "class$java$lang$Integer"  );
        type2syntheticWrapperFields.put(Type.LONG_TYPE   , "class$java$lang$Long"     );
        type2syntheticWrapperFields.put(Type.FLOAT_TYPE  , "class$java$lang$Float"    );
        type2syntheticWrapperFields.put(Type.DOUBLE_TYPE , "class$java$lang$Double"   );
    }

    TransformationData tranData=null;


    /**
     * Constructs a new {@link ClassAdapter} object. This mode of operation will
     * only implement abstract methods which will be executed by the RexxProxy.
     *
     * @param cv the class visitor to which this adapter must delegate calls.
     */
    public RgfExtendClass(final ClassVisitor cv)
    {
        super(cv);
        this.cv = cv;
        tranData=new TransformationData();
    }

    /**
     * Constructs a new {@link ClassAdapter} object. This mode of operation will
     * implement abstract methods which will be executed by the RexxProxy. In addition
     * it creates proxy methods for each Java method that carries a name as supplied
     * in the <code>methodNamesToAdd</code> array.
     *
     * @param cv the class visitor to which this adapter must delegate calls.
     * @param methodNamesToAdd a String array containing (caseless) method names for
     *        which proxy methods should be created
     */
    public RgfExtendClass(final ClassVisitor cv, String [] methodNamesToAdd)
    {
        this(cv);           // invoke the main constructor
        this.cv = cv;
            // add methods to proxy to appropriate HashSet, turn into uppercase
        if (methodNamesToAdd!=null)
        {
            for (int i=0; i<methodNamesToAdd.length; i++)
            {
                tranData.simpleMethodNamesToAdd.add(methodNamesToAdd[i].toUpperCase());
            }
        }
    }



    // ClassVisitor method
    public void visit(
        final int version,
        final int access,
        final String name,
        final String signature,
        final String superName,
        final String[] interfaces) // throws Throwable
    {

if (bDebugClassVisitor) System.err.println("--> "+"visit(version="+version+", access="+access+
                          ", name="+name+", signature="+signature+", superName="+superName+
                          ", [] interfaces="+interfaces+")");


        if ((access & ACC_INTERFACE)!=0)    // an interface in hand
        {
            throw new RuntimeException("\""+name+"\" is an interface, need a class instead");
        }

        if ((access & ACC_PUBLIC) == 0)     // not a public class throw-up
        {
            throw new RuntimeException("class \""+name+"\" is not public");
        }

        if ((access & ACC_FINAL)  != 0)     // a final class in hand
        {
            throw new RuntimeException("class \""+name+"\" is final and cannot be extended");
        }

        // not an abstract class, but no methods to proxy given!
        if ( ((access & ACC_ABSTRACT)==0) && (tranData.simpleMethodNamesToAdd.size()==0) )
        {
            throw new RuntimeException("No methods to proxy given for non-abstract class \""+name+"\"");
        }

        tranData.srcClassVersion  =version;     // Java version: can be V1_2, V1_5, V1_6, V1_7
        if (version<V1_5)
        {
            tranData.workPrintLnTarget    ="java/lang/StringBuffer";    // Java 1.4
            tranData.workPrintLnTargetDesc="Ljava/lang/StringBuffer;";    // Java 1.4

                // make sure we have the following fields available to us
            tranData.syntheticStaticFieldsToAdd.add("class$java$lang$Boolean"  );
            tranData.syntheticStaticFieldsToAdd.add("class$java$lang$Byte"     );
            tranData.syntheticStaticFieldsToAdd.add("class$java$lang$Character");
            tranData.syntheticStaticFieldsToAdd.add("class$java$lang$Short"    );
            tranData.syntheticStaticFieldsToAdd.add("class$java$lang$Integer"  );
            tranData.syntheticStaticFieldsToAdd.add("class$java$lang$Long"     );
            tranData.syntheticStaticFieldsToAdd.add("class$java$lang$Float"    );
            tranData.syntheticStaticFieldsToAdd.add("class$java$lang$Double"   );
        }
        tranData.srcClassAccess   =access;      // save access bits
        tranData.srcClassName     =name;        // internal name
        tranData.srcClassSuperName=superName;   // internal name
        tranData.srcClassInterfaces=interfaces;

            // create a unique class name, leave it in the same package
        String extension="_$"+thisPgmName+"$_"+(new Object()).hashCode();
        tranData.newClassName=name+extension;
        // make sure, abstract flag gets removed, also flag class as synthetic
        tranData.newClassAccess= (access & ~ACC_ABSTRACT) | ACC_SYNTHETIC;

if (bDebugClassVisitor) System.err.println("    "+"===> creating newClassName=["+tranData.newClassName+"]...");

	// set signature and interfaces to null
        cv.visit(version, tranData.newClassAccess, tranData.newClassName, null, name, null);
    }


    // ClassVisitor method
    public void visitSource(final String source, final String debug) {
if (bDebugClassVisitor) System.err.println("--> "+"visitSource(source="+source+", debug="+debug+")");
            // for debugging purposes
        cv.visitSource(thisPgmName+": extended class \""+tranData.srcClassName
        +"\" to create \""+tranData.newClassName+"\"", null);
    }



    // ClassVisitor method
        // do not visit!
    public void visitOuterClass(
        final String owner,
        final String name,
        final String desc)
    {
if (bDebugClassVisitor) System.err.println("--> "+"visitOuterClass(owner="+owner+", name="+name+", desc="+desc+")");
        // cv.visitOuterClass(owner, name, desc);
        return;
    }

    // ClassVisitor method
    // do not visit!
    public AnnotationVisitor visitAnnotation(
        final String desc,
        final boolean visible)
    {
if (bDebugClassVisitor) System.err.println("--> "+"visitAnnotation(desc="+desc+", visible="+visible+")");
        // return cv.visitAnnotation(desc, visible);
        return null;
    }

    // ClassVisitor method
    // do not visit!
    public void visitAttribute(final Attribute attr) {
if (bDebugClassVisitor) System.err.println("--> "+"visitAttribute(attr="+attr+")");
        // cv.visitAttribute(attr);
        return;
    }

    // ClassVisitor method
    // do not visit!
    public void visitInnerClass(
        final String name,
        final String outerName,
        final String innerName,
        final int access)
    {
if (bDebugClassVisitor) System.err.println("--> "+"visitInnerClass(name="+name+", outerName="+outerName+
                          ", innerName="+innerName+", access="+access+")");
        // cv.visitInnerClass(name, outerName, innerName, access);
        return;
    }

    // ClassVisitor method
    // do not visit!
    public FieldVisitor visitField(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final Object value)
    {
if (bDebugClassVisitor) System.err.println("--> "+"visitField(access="+access+", name="+name+
                          ", desc="+desc+", signature="+signature+", value="+value+")");

        return null;    // do not process this field
    }


    // ClassVisitor method
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions)
    {
if (bDebugClassVisitor) System.err.println("--> "+"visitMethod(access="+access+", name="+name+
                          ", desc="+desc+", signature="+signature+", [] exceptions="+exceptions+")");

        if ((access & ACC_PUBLIC)==0)       // don't process non-public methods
        {
            return null;
        }

        // do not process final and/or synthetic methods
        if ((access & (ACC_FINAL|ACC_SYNTHETIC)) != 0)
        {
            return null;
        }

        if (name.equals("<clinit>"))        // do not copy class initializer/constructor, will be built at the end
        {
            return null;
        }

        if ((access & ACC_ABSTRACT)==0)     // not an abstract method
        {
            // not in list of methods to proxy, hence do not process it
            if (!tranData.simpleMethodNamesToAdd.contains(name.toUpperCase()))
            {
                return null;
            }
            tranData.isForward2SuperNeeded=true;     // we are masking an existing method, hence we need a way to forward to super's method
        }
        else    // remove ACC_ABSTRACT flag
        {
            tranData.newMethodAccess= (access & ~ACC_ABSTRACT); // make sure, abstract flag gets removed
        }

        // make sure ACC_SYNCHRONIZED and ACC_STRICT ("strictfp") get removed
        tranData.newMethodAccess= (access & ~(ACC_SYNCHRONIZED|ACC_STRICT));

        if (exceptions!=null)   // add RexxException to list of exceptions
        {
            boolean bFound=false;
            for (int i=0; i<exceptions.length && bFound==false; i++)
            {
               bFound=exceptions[i].equals(rexxExceptionName);
            }

            // determine number of elements
            tranData.newMethodsExceptions=new String[exceptions.length+(bFound?0:1)];
            // make a copy of the exceptions array
            System.arraycopy(exceptions, 0,
                             tranData.newMethodsExceptions, 0,
                             exceptions.length);

            if (!bFound)
            {
                tranData.newMethodsExceptions[exceptions.length]=rexxExceptionName;
            }
        }
        else    // create exception list with RexxException as its sole element
        {
            tranData.newMethodsExceptions=new String[]{rexxExceptionName};
        }

        tranData.isMethodConstructor=name.equals("<init>");
        tranData.isMethodStatic     =((access & ACC_STATIC)!=0);

            // save values
        tranData.srcMethodAccess=access;
        tranData.srcMethodName=name;
        tranData.srcMethodDesc= desc;
        tranData.srcMethodSignature=signature;   // >= V 1.5 signature, can be null
        tranData.srcMethodsExceptions=exceptions; // exceptions method throws

        tranData.srcArgumentTypes =Type.getArgumentTypes(tranData.srcMethodDesc);
        tranData.srcReturnType    =Type.getReturnType(tranData.srcMethodDesc);


        // inject RexxProxy object as first argument ?
        tranData.newMethodDesc= (tranData.isMethodConstructor ? "("+rexxProxyName+desc.substring(1)
                                                              : desc);

        tranData.workArgumentTypes=Type.getArgumentTypes(tranData.newMethodDesc);

        // create our proxy method
        MethodVisitor mv=cv.visitMethod(tranData.newMethodAccess,
                                        name,
                                        tranData.newMethodDesc,
                                        signature,
                                        tranData.newMethodsExceptions);
        if (mv==null)   // a ClassVisitor may have consumed this
        {
            return null;
        }

        // memorize processed method
        tranData.simpleMethodNamesAdded.add(name);      // memorize method

        // tranData.fullMethodNamesAdded.add(name+tranData.newMethodDesc); // memorize method with desc (= signature)
        tranData.fullMethodNamesAdded.add(name+desc);   // memorize method with desc (= signature)

        // create code
        mv.visitCode();

        if (tranData.isMethodConstructor)
        {
            workConstructor(mv);
        }
        else
        {
            // - create code to handle arguments, if any
            //   depends whether method is static or not (first=idx=0 var is "this")
            //   [if <V1_5, then possible need for synthetic static fields and method "class$"

            // - invoke method on RexxProxy


            // - if void:
            //      - invoke RexxProxy, pop return value, return

            // - if return value:
            //      - invoke RexxProxy
            //          - if Object, return as is
            //          - if primitive value, convert and return it
            //          - else cast return value to return type and return it

            workProcessArgsAndInvoke(mv);

        }

        // end of this method's code
        mv.visitMaxs(tranData.workMaxStackSize, tranData.workMaxLocalSize);
        mv.visitEnd();

        return null;

        // return new MethodTeller(cv.visitMethod(access, name, desc, signature, exceptions));
//        return new MethodCruncher(cv.visitMethod(access, name, desc, signature, exceptions));
//        return null;
    }


    // place to add missing members (fields, methods)
    public void visitEnd() {
if (bDebugClassVisitor) System.err.println("--> "+"visitEnd().");

        workAddFields(cv);          // add field definitions
        workAddMethods(cv);         // add instance and static methods we need
        workAddMethodsToSuper(cv);  // add "forwardToSuper" methods
        workAddClinitMethod(cv);    // add class initializer code

        cv.visitEnd();              // class visitation ended.
    }


// ==========================================================================================

    /** Creates the bytecode for processing arguments.
     */
    void workConstructor(MethodVisitor mv)
    {
if (bDebugMethodVisitor) System.err.println("--> "+"workCreateConstructor(): name="+tranData.srcMethodName+", desc="+tranData.newMethodDesc);

        mv.visitVarInsn(ALOAD, 0);      // load 'this'
        int nextSlot=1;

            // load arguments, if any, skip first argument (injected RexxProxy)
        for (int i=1; i<tranData.workArgumentTypes.length; i++)
        {
            Type t=tranData.workArgumentTypes[i];   // get argument type
            mv.visitVarInsn(t.getOpcode(ILOAD), nextSlot);   // get matching opcode
            nextSlot+=t.getSize();                  // determine next slot
        }

            // invoke superclass' constructor
        mv.visitMethodInsn(INVOKESPECIAL, tranData.srcClassName,    // superclass
                                          "<init>",                 // name of constructor
                                          tranData.srcMethodDesc    // original desc
                          );

        // set target field to null
        mv.visitVarInsn(ALOAD, 0);      // 'this'
        mv.visitInsn(ACONST_NULL);      // load NULL value and put it into the "target" field
        mv.visitFieldInsn(PUTFIELD, tranData.srcClassName, "target", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;");

        // set target field to RexxProxy
        mv.visitVarInsn(ALOAD, 0);      // load 'this'
        mv.visitVarInsn(ALOAD, 1);      // load RexxProxy and put it into the "target" field
        mv.visitFieldInsn(PUTFIELD, tranData.srcClassName, "target", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;");

        mv.visitInsn(RETURN);           // return

        int maxArgSize=Type.getArgumentsAndReturnSizes(tranData.newMethodDesc)>>2;
        tranData.workMaxStackSize=maxArgSize;       // "this" + size of all but first args
        tranData.workMaxLocalSize=maxArgSize + 1;   // "this" + size of all args
    }




    /** Creates the bytecode for processing arguments.
     */
    void workProcessArgsAndInvoke(MethodVisitor mv)
    {
if (bDebugMethodVisitor) System.err.println("--> "+"workProcessArgsAndInvoke(): name="+tranData.srcMethodName+", desc="+tranData.srcMethodDesc);

        // get total size of arguments
        int argSize=Type.getArgumentsAndReturnSizes(tranData.srcMethodDesc)>>2;

        // static methods: arg1=slot0; instance methods: arg1=slot1
        int varProxySlot=argSize;      // first slot to store a local variable, appropriate for a static method

        // get RexxProxy object to address (target for method invocation)
        if (tranData.isMethodStatic)
        {
            mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "defaultHandler", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;");
        }
        else    // instance method in hand
        {
            varProxySlot++;            // account for "this" in slot 0
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, tranData.newClassName, "target", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;");
            mv.visitInsn(DUP);
            Label l0 = new Label();
            mv.visitJumpInsn(IFNONNULL, l0);    // hmm, "target" null, get "defaultHandler" instead
            mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "defaultHandler", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;");
         mv.visitLabel(l0);
        }
        mv.visitVarInsn(ASTORE, varProxySlot);  // store RexxProxy (the target)

        int varMethodNameSlot=varProxySlot+1;   // method name
        int varMethodDescSlot=varProxySlot+2;   // method desc
        int varArgArraySlot  =varProxySlot+3;   // array of args or null
        int varResultSlot    =varProxySlot+4;   // result from Rexx method in case we need to return a primitive value

        // arg1: determine and store methodName
        mv.visitLdcInsn(tranData.srcMethodName);
        mv.visitVarInsn(ASTORE, varMethodNameSlot);

        // arg2: get and store methodDesc
        mv.visitLdcInsn(tranData.srcMethodDesc);
        mv.visitVarInsn(ASTORE, varMethodDescSlot);

        // arg3: determine and store arguments in a single array
        mv.visitInsn(ACONST_NULL);          // set to null
        mv.visitVarInsn(ASTORE, varArgArraySlot);


        // determine nr. of args, if no args, we are done
        //                        else create array of appropriate dimension and assign/convert args to it
        int nrArgs=tranData.srcArgumentTypes.length;   // get nr. of args
        if (nrArgs>0)
        {
                // set number of elements
            if (nrArgs<6)
            {
                mv.visitInsn(arr_ICONST[tranData.srcArgumentTypes.length]);
            }
            else    // BIPUSH for args<127, SIPUSH else (in theory up to 255 args allowed)
            {
                mv.visitIntInsn( (nrArgs<127 ? BIPUSH : SIPUSH) , nrArgs);
            }
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");


            // process arguments, box primitive values
            int nextArgSlot= (tranData.isMethodStatic ? 0 : 1); // determine slot of first supplied argument

            for (int i=0; i<tranData.srcArgumentTypes.length; i++)
            {
                mv.visitInsn(DUP);      // duplicate array object on stack

                // determine array index
                if (i<6)                // store array index to use
                {
                    mv.visitInsn(arr_ICONST[i]);
                }
                else    // BIPUSH for args<127, SIPUSH else (in theory up to 255 args allowed)
                {
                    mv.visitIntInsn( (i<127 ? BIPUSH : SIPUSH) , i);
                }


                // process argument, if primitive, box it
                Type t=tranData.srcArgumentTypes[i];   // get Type of argument
                if (primitiveTypes.contains(t))
                {
                    // starting with Java 1.5 we have the static method valueOf() with primitive values for all wrapper classes
                    if (tranData.srcClassVersion>=V1_5 || t==Type.BOOLEAN_TYPE)  // Java <= 1.4
                    {
                        // load argument
                        mv.visitVarInsn(t.getOpcode(ILOAD), nextArgSlot);   // get matching opcode

                        // box it
                        mv.visitMethodInsn(INVOKESTATIC,
                                           (String) type2wrapperClass.get(t), // internal class name, e.g. "java/lang/Boolean"
                                           "valueOf",
                                           (String) type2valueOfDesc.get(t)   // method's desc, e.g. "(Z)Ljava/lang/Boolean;"
                                          );
                    }
                    else    // Java 1.4
                    {
                        // we need to use the wrapper's class constructor, as valueOf() for primitive values are not available in this version
                        // create an empty instance
                        mv.visitTypeInsn(NEW, (String) type2wrapperClass.get(t)); // internal class name, e.g. "java/lang/Long");
                        mv.visitInsn(DUP);

                        // load argument
                        mv.visitVarInsn(t.getOpcode(ILOAD), nextArgSlot);   // get matching opcode

                        // create wrapped object
                        mv.visitMethodInsn(INVOKESPECIAL,
                                           (String) type2wrapperClass.get(t), // internal class name, e.g. "java/lang/Long",
                                           "<init>",
                                           (String) type2initDesc.get(t)   // method's desc, e.g. "(J)V"
                                          );
                    }
                }
                else    // non primitive type, just load argument and save it into array
                {
                    mv.visitVarInsn(ALOAD, nextArgSlot);
                }
                mv.visitInsn(AASTORE);              // store object in array at given position

                nextArgSlot += t.getSize();         // determine slot for next argument
            }
            mv.visitVarInsn(ASTORE, varArgArraySlot);  // finally save array object
        }


        // ------------------------------------------------------------------------------------------
        // invoke method on RexxProxy
        mv.visitVarInsn(ALOAD, varProxySlot);       // receiver

        if (tranData.isMethodStatic)                // argument #1
        {
            mv.visitInsn(ACONST_NULL);              // set to null, "this" not available in static context
        }
        else    // pass on this Java object, such that Rexx can retrieve it and send messages to it
        {
            mv.visitVarInsn(ALOAD, 0);              // "this"
        }

        mv.visitVarInsn(ALOAD, varMethodNameSlot);  // argument #2: method name
        mv.visitVarInsn(ALOAD, varMethodDescSlot);  // argument #3: method desc
        mv.visitVarInsn(ALOAD, varArgArraySlot);    // argument #4: argument array
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/rexxla/bsf/engines/rexx/RexxProxy", "invoke", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;");




        // ------------------------------------------------------------------------------------------
        // process return value, if any
        Type tRet=tranData.srcReturnType;           // get Type of return value

        // a void method: no return value, clean up and return
        if (tRet==Type.VOID_TYPE)     // no return value
        {
            mv.visitInsn(POP);      // remove return value from stack
            mv.visitInsn(RETURN);   // return
            return;
        }

        // return value of type java.lang.Object, no need to do anything anymore, just return received value
        if (tRet.getClassName().equals("java.lang.Object"))
        {
            mv.visitInsn(ARETURN);  // return value from stack
            return;
        }


        // return value is a primitive value; check return value from Rexx, get the primitive value from it
        if (primitiveTypes.contains(tRet))
        {
            mv.visitVarInsn(ASTORE, varResultSlot);     // save result from Rexx

            String wrapperTypeInternalName=(String) type2wrapperClass.get(tRet);

            // if Rexx returned null, throw an exception
            mv.visitVarInsn(ALOAD, varResultSlot);
            Label l1 = new Label();
            mv.visitJumpInsn(IFNONNULL, l1);
            mv.visitTypeInsn(NEW, "java/lang/ClassCastException");
            mv.visitInsn(DUP);
            mv.visitTypeInsn(NEW, tranData.workPrintLnTarget);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, tranData.workPrintLnTarget, "<init>", "()V");
            mv.visitLdcInsn("Returned result is 'null', which cannot be cast to the primitive return type: \"");
            mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "append", "(Ljava/lang/String;)"+tranData.workPrintLnTargetDesc);
                    // get internal name of corresponding wrapper type to get at the corresponding primitive class object
            // mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
            mv.visitFieldInsn(GETSTATIC, wrapperTypeInternalName, "TYPE", "Ljava/lang/Class;");

            mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "append", "(Ljava/lang/Object;)"+tranData.workPrintLnTargetDesc);
            mv.visitLdcInsn("\"");
            mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "append", "(Ljava/lang/String;)"+tranData.workPrintLnTargetDesc);
            mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "toString", "()Ljava/lang/String;");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/ClassCastException", "<init>", "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);

            // if a String object, then use it to create an instance of the corresponding Java wrapper class
         mv.visitLabel(l1);
            mv.visitVarInsn(ALOAD, varResultSlot);
            mv.visitVarInsn(ASTORE, varResultSlot+1);       // save original result from Rexx
            mv.visitVarInsn(ALOAD, varResultSlot);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/String");
            Label l2 = new Label();
            mv.visitJumpInsn(IFEQ, l2);
            mv.visitVarInsn(ALOAD, varResultSlot);
            mv.visitTypeInsn(CHECKCAST, "java/lang/String");
            mv.visitVarInsn(ASTORE, varResultSlot);

               // treat Boolean special, in that it accepts "1" and the caseless string "TRUE" as true
            if (tRet==Type.BOOLEAN_TYPE)    // accept "1" and caseless "TRUE" as Boolean.TRUE
            {
                Label l_continue= new Label();  // jump target for special Boolean treatment

                mv.visitVarInsn(ALOAD, varResultSlot);
                mv.visitLdcInsn("1");                // load value to compare to
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
                Label l_0 = new Label();
                mv.visitJumpInsn(IFNE, l_0);          // "true", return TRUE object

                mv.visitVarInsn(ALOAD, varResultSlot);
                mv.visitLdcInsn("true");             // load value to compare to
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equalsIgnoreCase", "(Ljava/lang/String;)Z");
                Label l_1 = new Label();
                mv.visitJumpInsn(IFEQ, l_1);

             mv.visitLabel(l_0);
                mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
                mv.visitJumpInsn(GOTO, l_continue);
             mv.visitLabel(l_1);                      // not "true", hence return the FALSE object
                mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
             mv.visitLabel(l_continue);
            }
            else    // create wrapper object from the string
            {
                mv.visitVarInsn(ALOAD, varResultSlot);
                mv.visitMethodInsn(INVOKESTATIC, wrapperTypeInternalName, "valueOf", "(Ljava/lang/String;)L"+wrapperTypeInternalName+";");
            }
            mv.visitVarInsn(ASTORE, varResultSlot);

            // check whether result is an instance of corresponding wrapper type
         mv.visitLabel(l2);
            mv.visitVarInsn(ALOAD, varResultSlot);
            mv.visitTypeInsn(INSTANCEOF, wrapperTypeInternalName);
            Label l3 = new Label();
            mv.visitJumpInsn(IFNE, l3);
            mv.visitTypeInsn(NEW, "java/lang/ClassCastException");
            mv.visitInsn(DUP);
            mv.visitTypeInsn(NEW, tranData.workPrintLnTarget);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, tranData.workPrintLnTarget, "<init>", "()V");
            mv.visitLdcInsn("Returned result type \"");
            mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "append", "(Ljava/lang/String;)"+tranData.workPrintLnTargetDesc);
            mv.visitVarInsn(ALOAD, varResultSlot+1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "append", "(Ljava/lang/Object;)"+tranData.workPrintLnTargetDesc);
            mv.visitLdcInsn("\" cannot be cast to the primitive return type: \"");
            mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "append", "(Ljava/lang/String;)"+tranData.workPrintLnTargetDesc);
            mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
            mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "append", "(Ljava/lang/Object;)"+tranData.workPrintLnTargetDesc);
            mv.visitLdcInsn("\"");
            mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "append", "(Ljava/lang/String;)"+tranData.workPrintLnTargetDesc);
            mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "toString", "()Ljava/lang/String;");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/ClassCastException", "<init>", "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);

            // use wrapper type instance to fetch and return the primitive value
         mv.visitLabel(l3);
            mv.visitVarInsn(ALOAD, varResultSlot);
            mv.visitTypeInsn(CHECKCAST, wrapperTypeInternalName);
            // mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
            String []tmpArr=(String[]) type2getPrimitiveValue.get(tRet);
            mv.visitMethodInsn(INVOKEVIRTUAL, wrapperTypeInternalName, tmpArr[0], tmpArr[1]);
            mv.visitInsn(tRet.getOpcode(IRETURN) );
        }

        else    // a Java type, just cast return value to it
        {
            // TODO: cast really necessary or done/checked automatically by JVM ?
            mv.visitTypeInsn(CHECKCAST, tRet.getInternalName());
            mv.visitInsn(ARETURN);
        }

        //       ??? determine maxStack, maxLocal
        // int maxStackSize: check to see if J or D (=2), add 3-times the size if "new" plus 1
        // int maxLocalSize=varArgArraySlot+1;    // max size of local variables, includes args
    }




    /** Create instance and static fields we need.
     */
    void workAddFields(ClassVisitor cv)
    {
        FieldVisitor fv=null;

        // create static field "classLock": if creating an instance of this class, use this field
        //      to lock access to the class as a whole. Set the "defaultHandler" before creating
        //      an instance of this class, such that if some code in a superclass constructor
        //      invokes instance methods of the synthetic class, these invocations can be forwarded
        //      to the RexxProxy stored in the static "defaultHandler"
        fv = cv.visitField(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, "classLock", "Ljava/lang/Object;", null, null);
        if (fv!=null) fv.visitEnd();

        // create static field "defaultHandler"
        fv = cv.visitField(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, "defaultHandler", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;", null, null);
        if (fv!=null) fv.visitEnd();

        // in Java 1.4: create indicated static synthetic fields, e.g. "class$java$lang$Double"
        if (tranData.srcClassVersion<V1_5)  // if Java 1.4, create needed synthetic fields, e.g. "class$java$lang$Double"
        {
            for (Iterator i=tranData.syntheticStaticFieldsToAdd.iterator(); i.hasNext(); )
            {
                fv = cv.visitField(ACC_STATIC + ACC_SYNTHETIC, (String) i.next(), "Ljava/lang/Class;", null, null);
                if (fv!=null) fv.visitEnd();
            }
        }

        // create instance field "target"
        fv = cv.visitField(ACC_SYNTHETIC, "target", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;", null, null);
        if (fv!=null) fv.visitEnd();


        // if we create the forwardToSuper() methods, we need the following static fields
        if (tranData.isForward2SuperNeeded)
        {
            // maps primitive and wrapper types (classes) to respective wrapper types (classes)
           fv = cv.visitField(ACC_STATIC + ACC_SYNTHETIC, "prim2obj$", "Ljava/util/Hashtable;", null, null);
           if (fv!=null) fv.visitEnd();

           // maps primitive and warpper types (classes) to BSF4Rexx type indicator strings
           fv = cv.visitField(ACC_STATIC + ACC_SYNTHETIC, "prim2ind$", "Ljava/util/Hashtable;", null, null);
           if (fv!=null) fv.visitEnd();
        }
    }



    /** Create instance and static fields we need.
     */
    void workAddMethods(ClassVisitor cv)
    {
        MethodVisitor mv=null;

        // add getter method: get and return "defaultHandler" value
        mv = cv.visitMethod(ACC_PUBLIC + ACC_STATIC + ACC_SYNTHETIC, "getDefaultHandler", "()Lorg/rexxla/bsf/engines/rexx/RexxProxy;", null, null);
        if (mv!=null)
        {
            mv.visitCode();             // get field value and return it
            mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "defaultHandler", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 0);
            mv.visitEnd();
        }


        // add setter method
        mv = cv.visitMethod(ACC_PUBLIC + ACC_STATIC + ACC_SYNTHETIC, "setDefaultHandler", "(Lorg/rexxla/bsf/engines/rexx/RexxProxy;)V", null, null);
        if (mv!=null)
        {
           mv.visitCode();
           Label l0 = new Label();      // start of try-block
           Label l1 = new Label();      // end of try-block
           Label l2 = new Label();      // start of catch-block
           mv.visitTryCatchBlock(l0, l1, l2, null);
           Label l3 = new Label();
           mv.visitTryCatchBlock(l2, l3, l2, null);
           mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "classLock", "Ljava/lang/Object;");
           mv.visitInsn(DUP);           // get lock object
           mv.visitVarInsn(ASTORE, 1);  // save lock object
           mv.visitInsn(MONITORENTER);  // start of synchronize
        mv.visitLabel(l0);
           mv.visitVarInsn(ALOAD, 0);   // save new value
           mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "defaultHandler", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;");
           mv.visitVarInsn(ALOAD, 1);   // load lock object
           mv.visitInsn(MONITOREXIT);   // end of synchronize
        mv.visitLabel(l1);
           Label l4 = new Label();
           mv.visitJumpInsn(GOTO, l4);  // everything o.k., return
        mv.visitLabel(l2);              // exception occurred
           mv.visitVarInsn(ASTORE, 2);  // save Throwable object
           mv.visitVarInsn(ALOAD, 1);   // load lock object
           mv.visitInsn(MONITOREXIT);   // unlock (end of synchronize)
        mv.visitLabel(l3);
           mv.visitVarInsn(ALOAD, 2);   // load Throwable object
           mv.visitInsn(ATHROW);        // rethrow
        mv.visitLabel(l4);
           mv.visitInsn(RETURN);        // return
           mv.visitMaxs(2, 3);
           mv.visitEnd();
        }


        if (tranData.srcClassVersion<V1_5)      // Java <= 1.4
        {
                // create static synthetic method which dynamically loads a class object
            mv = cv.visitMethod(ACC_STATIC + ACC_SYNTHETIC, "class$", "(Ljava/lang/String;)Ljava/lang/Class;", null, null);
            if (mv!=null)
            {
                mv.visitCode();
                Label l0 = new Label();     // start of try block
                Label l1 = new Label();     // end of try block
                Label l2 = new Label();     // start of catch block
                mv.visitTryCatchBlock(l0, l1, l2, "java/lang/ClassNotFoundException");
             mv.visitLabel(l0);
                mv.visitVarInsn(ALOAD, 0);  // load argument
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
             mv.visitLabel(l1);
                mv.visitInsn(ARETURN);      // return value
             mv.visitLabel(l2);
                mv.visitVarInsn(ASTORE, 1); // save Throwable, create a new Throwable and initialize it
                mv.visitTypeInsn(NEW, "java/lang/NoClassDefFoundError");
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NoClassDefFoundError", "<init>", "()V");
                mv.visitVarInsn(ALOAD, 1);  // load Throwable and assign it to new Throwable as its initial cause
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/NoClassDefFoundError", "initCause", "(Ljava/lang/Throwable;)Ljava/lang/Throwable;");
                mv.visitInsn(ATHROW);       // throw new Throwable
                mv.visitMaxs(2, 2);
                mv.visitEnd();
            }
        }

    }


    /** Create instance methods that allow invoking masked methods in super.
     */
    void workAddMethodsToSuper(ClassVisitor cv)
    {
        if (!tranData.isForward2SuperNeeded)    // no need to create these methods
        {
            return;
        }

        MethodVisitor mv=null;

        // -------------------------------------------------------------------------------
        // add "forwardToSuperMethod"
        mv = cv.visitMethod(ACC_PUBLIC + ACC_SYNTHETIC, "forwardToSuper", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;", null, new String[] { "java/lang/NoSuchMethodException", "java/lang/IllegalAccessException", "java/lang/reflect/InvocationTargetException" });
        if (mv!=null)
        {
           mv.visitCode();
           Label l0 = new Label();
           Label l1 = new Label();
           Label l2 = new Label();
           mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");
           Label l3 = new Label();
           Label l4 = new Label();
           Label l5 = new Label();
           mv.visitTryCatchBlock(l3, l4, l5, "java/lang/IllegalAccessException");
           mv.visitVarInsn(ALOAD, 2);
           Label l6 = new Label();
           mv.visitJumpInsn(IFNONNULL, l6);
           mv.visitInsn(ICONST_0);
           Label l7 = new Label();
           mv.visitJumpInsn(GOTO, l7);
        mv.visitLabel(l6);
           mv.visitVarInsn(ALOAD, 2);
           mv.visitInsn(ARRAYLENGTH);
        mv.visitLabel(l7);
           mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
           mv.visitVarInsn(ASTORE, 3);
           mv.visitVarInsn(ALOAD, 0);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSuperclass", "()Ljava/lang/Class;");
           mv.visitVarInsn(ASTORE, 4);
           Label l8 = new Label();
        mv.visitLabel(l8);
           mv.visitVarInsn(ALOAD, 4);
           Label l9 = new Label();
           mv.visitJumpInsn(IFNULL, l9);
           mv.visitVarInsn(ALOAD, 4);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethods", "()[Ljava/lang/reflect/Method;");
           mv.visitVarInsn(ASTORE, 5);
           mv.visitInsn(ICONST_0);
           mv.visitVarInsn(ISTORE, 6);
           Label l10 = new Label();
        mv.visitLabel(l10);
           mv.visitVarInsn(ILOAD, 6);
           mv.visitVarInsn(ALOAD, 5);
           mv.visitInsn(ARRAYLENGTH);
           Label l11 = new Label();
           mv.visitJumpInsn(IF_ICMPGE, l11);
           mv.visitVarInsn(ALOAD, 5);
           mv.visitVarInsn(ILOAD, 6);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getName", "()Ljava/lang/String;");
           mv.visitVarInsn(ALOAD, 1);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equalsIgnoreCase", "(Ljava/lang/String;)Z");
           Label l12 = new Label();
           mv.visitJumpInsn(IFEQ, l12);
           mv.visitVarInsn(ALOAD, 5);
           mv.visitVarInsn(ILOAD, 6);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getModifiers", "()I");
           mv.visitMethodInsn(INVOKESTATIC, "java/lang/reflect/Modifier", "isPublic", "(I)Z");
           mv.visitJumpInsn(IFEQ, l12);
           mv.visitVarInsn(ALOAD, 5);
           mv.visitVarInsn(ILOAD, 6);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getParameterTypes", "()[Ljava/lang/Class;");
           mv.visitVarInsn(ASTORE, 7);
           mv.visitInsn(ICONST_0);
           mv.visitVarInsn(ISTORE, 8);
           mv.visitVarInsn(ALOAD, 2);
           Label l13 = new Label();
           mv.visitJumpInsn(IFNONNULL, l13);
           mv.visitVarInsn(ALOAD, 7);
           mv.visitInsn(ARRAYLENGTH);
           Label l14 = new Label();
           mv.visitJumpInsn(IFNE, l14);
           mv.visitVarInsn(ALOAD, 2);
           mv.visitJumpInsn(IFNONNULL, l14);
           mv.visitInsn(ICONST_1);
           Label l15 = new Label();
           mv.visitJumpInsn(GOTO, l15);
        mv.visitLabel(l14);
           mv.visitInsn(ICONST_0);
        mv.visitLabel(l15);
           mv.visitVarInsn(ISTORE, 8);
           Label l16 = new Label();
           mv.visitJumpInsn(GOTO, l16);
        mv.visitLabel(l13);
           mv.visitVarInsn(ALOAD, 7);
           mv.visitInsn(ARRAYLENGTH);
           mv.visitVarInsn(ALOAD, 2);
           mv.visitInsn(ARRAYLENGTH);
           Label l17 = new Label();
           mv.visitJumpInsn(IF_ICMPNE, l17);
           mv.visitInsn(ICONST_1);
           Label l18 = new Label();
           mv.visitJumpInsn(GOTO, l18);
        mv.visitLabel(l17);
           mv.visitInsn(ICONST_0);
        mv.visitLabel(l18);
           mv.visitVarInsn(ISTORE, 8);
        mv.visitLabel(l16);
           mv.visitInsn(ICONST_0);
           mv.visitVarInsn(ISTORE, 9);
           Label l19 = new Label();
        mv.visitLabel(l19);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitVarInsn(ALOAD, 7);
           mv.visitInsn(ARRAYLENGTH);
           Label l20 = new Label();
           mv.visitJumpInsn(IF_ICMPGE, l20);
           mv.visitVarInsn(ILOAD, 8);
           mv.visitJumpInsn(IFEQ, l20);
           mv.visitVarInsn(ALOAD, 7);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(AALOAD);
           mv.visitVarInsn(ALOAD, 2);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isAssignableFrom", "(Ljava/lang/Class;)Z");
           Label l21 = new Label();
           mv.visitJumpInsn(IFEQ, l21);
           mv.visitVarInsn(ALOAD, 3);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitVarInsn(ALOAD, 2);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(AALOAD);
           mv.visitInsn(AASTORE);
           Label l22 = new Label();
           mv.visitJumpInsn(GOTO, l22);
        mv.visitLabel(l21);
           mv.visitVarInsn(ALOAD, 7);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isPrimitive", "()Z");
           Label l23 = new Label();
           mv.visitJumpInsn(IFEQ, l23);
           mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
           mv.visitVarInsn(ALOAD, 7);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
           mv.visitTypeInsn(CHECKCAST, "java/lang/Class");
           mv.visitVarInsn(ALOAD, 2);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isAssignableFrom", "(Ljava/lang/Class;)Z");
           mv.visitJumpInsn(IFEQ, l23);
           mv.visitVarInsn(ALOAD, 3);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitVarInsn(ALOAD, 2);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(AALOAD);
           mv.visitInsn(AASTORE);
           mv.visitJumpInsn(GOTO, l22);
        mv.visitLabel(l23);
           mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
           mv.visitVarInsn(ALOAD, 7);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "containsKey", "(Ljava/lang/Object;)Z");
           Label l24 = new Label();
           mv.visitJumpInsn(IFEQ, l24);
           mv.visitInsn(ACONST_NULL);
           mv.visitVarInsn(ASTORE, 10);
        mv.visitLabel(l0);
           mv.visitVarInsn(ALOAD, 3);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitVarInsn(ALOAD, 2);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(AALOAD);
           mv.visitTypeInsn(CHECKCAST, "java/lang/String");
           mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
           mv.visitVarInsn(ALOAD, 7);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
           mv.visitTypeInsn(CHECKCAST, "java/lang/Class");
           mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "convertFromStringTo$", "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;");
           mv.visitInsn(AASTORE);
        mv.visitLabel(l1);
           mv.visitJumpInsn(GOTO, l22);
        mv.visitLabel(l2);
           mv.visitVarInsn(ASTORE, 11);
        mv.visitLabel(l24);
           mv.visitInsn(ICONST_0);
           mv.visitVarInsn(ISTORE, 8);
        mv.visitLabel(l22);
           mv.visitIincInsn(9, 1);
           mv.visitJumpInsn(GOTO, l19);
        mv.visitLabel(l20);
           mv.visitVarInsn(ILOAD, 8);
           mv.visitJumpInsn(IFEQ, l12);
        mv.visitLabel(l3);
           mv.visitVarInsn(ALOAD, 5);
           mv.visitVarInsn(ILOAD, 6);
           mv.visitInsn(AALOAD);
           mv.visitVarInsn(ALOAD, 0);
           mv.visitVarInsn(ALOAD, 3);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
        mv.visitLabel(l4);
           mv.visitInsn(ARETURN);
        mv.visitLabel(l5);
           mv.visitVarInsn(ASTORE, 9);
           mv.visitVarInsn(ALOAD, 5);
           mv.visitVarInsn(ILOAD, 6);
           mv.visitInsn(AALOAD);
           mv.visitInsn(ICONST_1);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "setAccessible", "(Z)V");
           mv.visitVarInsn(ALOAD, 5);
           mv.visitVarInsn(ILOAD, 6);
           mv.visitInsn(AALOAD);
           mv.visitVarInsn(ALOAD, 0);
           mv.visitVarInsn(ALOAD, 3);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
           mv.visitInsn(ARETURN);
        mv.visitLabel(l12);
           mv.visitIincInsn(6, 1);
           mv.visitJumpInsn(GOTO, l10);
        mv.visitLabel(l11);
           mv.visitVarInsn(ALOAD, 4);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSuperclass", "()Ljava/lang/Class;");
           mv.visitVarInsn(ASTORE, 4);
           mv.visitJumpInsn(GOTO, l8);
        mv.visitLabel(l9);
           mv.visitTypeInsn(NEW, "java/lang/NoSuchMethodException");
           mv.visitInsn(DUP);
           mv.visitTypeInsn(NEW, tranData.workPrintLnTarget);
           mv.visitInsn(DUP);
           mv.visitMethodInsn(INVOKESPECIAL, tranData.workPrintLnTarget, "<init>", "()V");
           mv.visitLdcInsn("cannot find method named \"");
           mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "append", "(Ljava/lang/String;)"+tranData.workPrintLnTargetDesc);
           mv.visitVarInsn(ALOAD, 1);
           mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "append", "(Ljava/lang/String;)"+tranData.workPrintLnTargetDesc);
           mv.visitLdcInsn("\" with matching argument(s)");
           mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "append", "(Ljava/lang/String;)"+tranData.workPrintLnTargetDesc);
           mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "toString", "()Ljava/lang/String;");
           mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NoSuchMethodException", "<init>", "(Ljava/lang/String;)V");
           mv.visitInsn(ATHROW);
           mv.visitMaxs(6, 12);
           mv.visitEnd();
        }


        // -------------------------------------------------------------------------------
        // add "forwardToSuperMethodStrict"
        mv = cv.visitMethod(ACC_PUBLIC + ACC_SYNTHETIC, "forwardToSuperStrict", "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;", null, new String[] { "java/lang/NoSuchMethodException", "java/lang/IllegalAccessException", "java/lang/reflect/InvocationTargetException" });
        if (mv!=null)
        {
           mv.visitCode();
           Label l0 = new Label();
           Label l1 = new Label();
           Label l2 = new Label();
           mv.visitTryCatchBlock(l0, l1, l2, "java/lang/IllegalAccessException");
           mv.visitInsn(ACONST_NULL);
           mv.visitVarInsn(ASTORE, 3);
           mv.visitVarInsn(ALOAD, 2);
           Label l3 = new Label();
           mv.visitJumpInsn(IFNULL, l3);
           mv.visitVarInsn(ALOAD, 2);
           mv.visitInsn(ARRAYLENGTH);
           mv.visitInsn(ICONST_2);
           mv.visitInsn(IDIV);
           mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
           mv.visitVarInsn(ASTORE, 3);
        mv.visitLabel(l3);
           mv.visitVarInsn(ALOAD, 0);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSuperclass", "()Ljava/lang/Class;");
           mv.visitVarInsn(ASTORE, 4);
           Label l4 = new Label();
        mv.visitLabel(l4);
           mv.visitVarInsn(ALOAD, 4);
           Label l5 = new Label();
           mv.visitJumpInsn(IFNULL, l5);
           mv.visitVarInsn(ALOAD, 4);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethods", "()[Ljava/lang/reflect/Method;");
           mv.visitVarInsn(ASTORE, 5);
           mv.visitInsn(ICONST_0);
           mv.visitVarInsn(ISTORE, 6);
           Label l6 = new Label();
        mv.visitLabel(l6);
           mv.visitVarInsn(ILOAD, 6);
           mv.visitVarInsn(ALOAD, 5);
           mv.visitInsn(ARRAYLENGTH);
           Label l7 = new Label();
           mv.visitJumpInsn(IF_ICMPGE, l7);
           mv.visitVarInsn(ALOAD, 5);
           mv.visitVarInsn(ILOAD, 6);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getName", "()Ljava/lang/String;");
           mv.visitVarInsn(ALOAD, 1);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
           Label l8 = new Label();
           mv.visitJumpInsn(IFEQ, l8);
           mv.visitVarInsn(ALOAD, 5);
           mv.visitVarInsn(ILOAD, 6);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getModifiers", "()I");
           mv.visitMethodInsn(INVOKESTATIC, "java/lang/reflect/Modifier", "isPublic", "(I)Z");
           mv.visitJumpInsn(IFEQ, l8);
           mv.visitVarInsn(ALOAD, 5);
           mv.visitVarInsn(ILOAD, 6);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getParameterTypes", "()[Ljava/lang/Class;");
           mv.visitVarInsn(ASTORE, 7);
           mv.visitInsn(ICONST_0);
           mv.visitVarInsn(ISTORE, 8);
           mv.visitVarInsn(ALOAD, 2);
           Label l9 = new Label();
           mv.visitJumpInsn(IFNONNULL, l9);
           mv.visitVarInsn(ALOAD, 7);
           mv.visitInsn(ARRAYLENGTH);
           Label l10 = new Label();
           mv.visitJumpInsn(IFNE, l10);
           mv.visitVarInsn(ALOAD, 2);
           mv.visitJumpInsn(IFNONNULL, l10);
           mv.visitInsn(ICONST_1);
           Label l11 = new Label();
           mv.visitJumpInsn(GOTO, l11);
        mv.visitLabel(l10);
           mv.visitInsn(ICONST_0);
        mv.visitLabel(l11);
           mv.visitVarInsn(ISTORE, 8);
           Label l12 = new Label();
           mv.visitJumpInsn(GOTO, l12);
        mv.visitLabel(l9);
           mv.visitVarInsn(ALOAD, 7);
           mv.visitInsn(ARRAYLENGTH);
           mv.visitInsn(ICONST_2);
           mv.visitInsn(IMUL);
           mv.visitVarInsn(ALOAD, 2);
           mv.visitInsn(ARRAYLENGTH);
           Label l13 = new Label();
           mv.visitJumpInsn(IF_ICMPNE, l13);
           mv.visitInsn(ICONST_1);
           Label l14 = new Label();
           mv.visitJumpInsn(GOTO, l14);
        mv.visitLabel(l13);
           mv.visitInsn(ICONST_0);
        mv.visitLabel(l14);
           mv.visitVarInsn(ISTORE, 8);
        mv.visitLabel(l12);
           mv.visitInsn(ICONST_0);
           mv.visitVarInsn(ISTORE, 9);
           Label l15 = new Label();
        mv.visitLabel(l15);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitVarInsn(ALOAD, 7);
           mv.visitInsn(ARRAYLENGTH);
           Label l16 = new Label();
           mv.visitJumpInsn(IF_ICMPGE, l16);
           mv.visitVarInsn(ILOAD, 8);
           mv.visitJumpInsn(IFEQ, l16);
           mv.visitVarInsn(ALOAD, 2);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(ICONST_2);
           mv.visitInsn(IMUL);
           mv.visitInsn(ICONST_1);
           mv.visitInsn(IADD);
           mv.visitInsn(AALOAD);
           mv.visitVarInsn(ASTORE, 10);
           mv.visitVarInsn(ALOAD, 3);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitVarInsn(ALOAD, 10);
           mv.visitInsn(AASTORE);
           mv.visitVarInsn(ALOAD, 7);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(AALOAD);
           mv.visitVarInsn(ALOAD, 10);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isAssignableFrom", "(Ljava/lang/Class;)Z");
           Label l17 = new Label();
           mv.visitJumpInsn(IFEQ, l17);
           Label l18 = new Label();
           mv.visitJumpInsn(GOTO, l18);
        mv.visitLabel(l17);
           mv.visitVarInsn(ALOAD, 7);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isPrimitive", "()Z");
           Label l19 = new Label();
           mv.visitJumpInsn(IFNE, l19);
           mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
           mv.visitVarInsn(ALOAD, 7);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "containsKey", "(Ljava/lang/Object;)Z");
           Label l20 = new Label();
           mv.visitJumpInsn(IFEQ, l20);
        mv.visitLabel(l19);
           mv.visitVarInsn(ALOAD, 2);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(ICONST_2);
           mv.visitInsn(IMUL);
           mv.visitInsn(AALOAD);
           mv.visitTypeInsn(CHECKCAST, "java/lang/String");
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toUpperCase", "()Ljava/lang/String;");
           mv.visitVarInsn(ASTORE, 11);
           mv.visitVarInsn(ALOAD, 11);
           mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
           mv.visitVarInsn(ALOAD, 7);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
           mv.visitTypeInsn(CHECKCAST, "java/lang/String");
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "startsWith", "(Ljava/lang/String;)Z");
           mv.visitJumpInsn(IFEQ, l18);
           mv.visitVarInsn(ALOAD, 10);
           mv.visitTypeInsn(INSTANCEOF, "java/lang/String");
           mv.visitJumpInsn(IFEQ, l18);
           mv.visitVarInsn(ALOAD, 3);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitVarInsn(ALOAD, 10);
           mv.visitTypeInsn(CHECKCAST, "java/lang/String");
           mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
           mv.visitVarInsn(ALOAD, 7);
           mv.visitVarInsn(ILOAD, 9);
           mv.visitInsn(AALOAD);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
           mv.visitTypeInsn(CHECKCAST, "java/lang/Class");
           mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "convertFromStringTo$", "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;");
           mv.visitInsn(AASTORE);
           mv.visitJumpInsn(GOTO, l18);
        mv.visitLabel(l20);
           mv.visitInsn(ICONST_0);
           mv.visitVarInsn(ISTORE, 8);
        mv.visitLabel(l18);
           mv.visitIincInsn(9, 1);
           mv.visitJumpInsn(GOTO, l15);
        mv.visitLabel(l16);
           mv.visitVarInsn(ILOAD, 8);
           mv.visitJumpInsn(IFEQ, l8);
        mv.visitLabel(l0);
           mv.visitVarInsn(ALOAD, 5);
           mv.visitVarInsn(ILOAD, 6);
           mv.visitInsn(AALOAD);
           mv.visitVarInsn(ALOAD, 0);
           mv.visitVarInsn(ALOAD, 3);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
        mv.visitLabel(l1);
           mv.visitInsn(ARETURN);
        mv.visitLabel(l2);
           mv.visitVarInsn(ASTORE, 9);
           mv.visitVarInsn(ALOAD, 5);
           mv.visitVarInsn(ILOAD, 6);
           mv.visitInsn(AALOAD);
           mv.visitInsn(ICONST_1);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "setAccessible", "(Z)V");
           mv.visitVarInsn(ALOAD, 5);
           mv.visitVarInsn(ILOAD, 6);
           mv.visitInsn(AALOAD);
           mv.visitVarInsn(ALOAD, 0);
           mv.visitVarInsn(ALOAD, 3);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
           mv.visitInsn(ARETURN);
        mv.visitLabel(l8);
           mv.visitIincInsn(6, 1);
           mv.visitJumpInsn(GOTO, l6);
        mv.visitLabel(l7);
           mv.visitVarInsn(ALOAD, 4);
           mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSuperclass", "()Ljava/lang/Class;");
           mv.visitVarInsn(ASTORE, 4);
           mv.visitJumpInsn(GOTO, l4);
        mv.visitLabel(l5);
           mv.visitTypeInsn(NEW, "java/lang/NoSuchMethodException");
           mv.visitInsn(DUP);
           mv.visitTypeInsn(NEW, tranData.workPrintLnTarget);
           mv.visitInsn(DUP);
           mv.visitMethodInsn(INVOKESPECIAL, tranData.workPrintLnTarget, "<init>", "()V");
           mv.visitLdcInsn("cannot find method named \"");
           mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "append", "(Ljava/lang/String;)"+tranData.workPrintLnTargetDesc);
           mv.visitVarInsn(ALOAD, 1);
           mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "append", "(Ljava/lang/String;)"+tranData.workPrintLnTargetDesc);
           mv.visitLdcInsn("\" with matching argument(s)");
           mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "append", "(Ljava/lang/String;)"+tranData.workPrintLnTargetDesc);
           mv.visitMethodInsn(INVOKEVIRTUAL, tranData.workPrintLnTarget, "toString", "()Ljava/lang/String;");
           mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NoSuchMethodException", "<init>", "(Ljava/lang/String;)V");
           mv.visitInsn(ATHROW);
           mv.visitMaxs(6, 12);
           mv.visitEnd();
        }


        // if Java 1.4, add static synthetic "class$" method
        if (tranData.srcClassVersion<V1_5)
        {
            // -------------------------------------------------------------------------------
            // add static method "convertFromStringTo$": convert a string value to matching wrapper class object
            mv = cv.visitMethod(ACC_STATIC + ACC_SYNTHETIC, "convertFromStringTo$", "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;", null, null);
            if (mv!=null)
            {
               mv.visitCode();
                                                    // test for "Boolean" wrapper class
               mv.visitVarInsn(ALOAD, 1);           // load clz argument
               mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Boolean", "Ljava/lang/Class;");
               Label l0 = new Label();
               mv.visitJumpInsn(IFNONNULL, l0);     // if synthetic static field is still null, set it
               mv.visitLdcInsn("java.lang.Boolean");
               mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
               mv.visitInsn(DUP);
               mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Boolean", "Ljava/lang/Class;");
               Label l1 = new Label();
               mv.visitJumpInsn(GOTO, l1);
            mv.visitLabel(l0);
               mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Boolean", "Ljava/lang/Class;");
            mv.visitLabel(l1);                      // synthetic static field is now set
               Label l2 = new Label();
               mv.visitJumpInsn(IF_ACMPNE, l2);     // if clz-argument is not Boolean then try next primitive wrapper type
               mv.visitVarInsn(ALOAD, 0);           // get first argument (String value)
               mv.visitLdcInsn("1");                // load value to compare to
               mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
               Label l3 = new Label();
               mv.visitJumpInsn(IFNE, l3);          // "true", return TRUE object
               mv.visitVarInsn(ALOAD, 0);           // get string argument
               mv.visitLdcInsn("true");             // load value to compare to
               mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equalsIgnoreCase", "(Ljava/lang/String;)Z");
               Label l4 = new Label();
               mv.visitJumpInsn(IFEQ, l4);
            mv.visitLabel(l3);
               mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
               mv.visitInsn(ARETURN);
            mv.visitLabel(l4);                      // not "true", hence return the FALSE object
               mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
               mv.visitInsn(ARETURN);

            mv.visitLabel(l2);                      // test for "Byte" wrapper class
               mv.visitVarInsn(ALOAD, 1);           // load clz argument
               mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Byte", "Ljava/lang/Class;");
               Label l5 = new Label();
               mv.visitJumpInsn(IFNONNULL, l5);
               mv.visitLdcInsn("java.lang.Byte");
               mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
               mv.visitInsn(DUP);
               mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Byte", "Ljava/lang/Class;");
               Label l6 = new Label();
               mv.visitJumpInsn(GOTO, l6);
            mv.visitLabel(l5);
               mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Byte", "Ljava/lang/Class;");
            mv.visitLabel(l6);
               Label l7 = new Label();
               mv.visitJumpInsn(IF_ACMPNE, l7);
               mv.visitVarInsn(ALOAD, 0);
               mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(Ljava/lang/String;)Ljava/lang/Byte;");
               mv.visitInsn(ARETURN);

            mv.visitLabel(l7);                      // test for "Character" wrapper class
               mv.visitVarInsn(ALOAD, 1);           // load clz argument
               mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Character", "Ljava/lang/Class;");
               Label l8 = new Label();
               mv.visitJumpInsn(IFNONNULL, l8);
               mv.visitLdcInsn("java.lang.Character");
               mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
               mv.visitInsn(DUP);
               mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Character", "Ljava/lang/Class;");
               Label l9 = new Label();
               mv.visitJumpInsn(GOTO, l9);
            mv.visitLabel(l8);
               mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Character", "Ljava/lang/Class;");
            mv.visitLabel(l9);
               Label l10 = new Label();
               mv.visitJumpInsn(IF_ACMPNE, l10);
               mv.visitTypeInsn(NEW, "java/lang/Character");
               mv.visitInsn(DUP);
               mv.visitVarInsn(ALOAD, 0);
               mv.visitInsn(ICONST_0);
               mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C");
               mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Character", "<init>", "(C)V");
               mv.visitInsn(ARETURN);

            mv.visitLabel(l10);                     // test for "Short" wrapper class
               mv.visitVarInsn(ALOAD, 1);           // load clz argument
               mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Short", "Ljava/lang/Class;");
               Label l11 = new Label();
               mv.visitJumpInsn(IFNONNULL, l11);
               mv.visitLdcInsn("java.lang.Short");
               mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
               mv.visitInsn(DUP);
               mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Short", "Ljava/lang/Class;");
               Label l12 = new Label();
               mv.visitJumpInsn(GOTO, l12);
            mv.visitLabel(l11);
               mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Short", "Ljava/lang/Class;");
            mv.visitLabel(l12);
               Label l13 = new Label();
               mv.visitJumpInsn(IF_ACMPNE, l13);
               mv.visitVarInsn(ALOAD, 0);
               mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(Ljava/lang/String;)Ljava/lang/Short;");
               mv.visitInsn(ARETURN);

            mv.visitLabel(l13);                     // test for "Integer" wrapper class
               mv.visitVarInsn(ALOAD, 1);           // load clz argument
               mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Integer", "Ljava/lang/Class;");
               Label l14 = new Label();
               mv.visitJumpInsn(IFNONNULL, l14);
               mv.visitLdcInsn("java.lang.Integer");
               mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
               mv.visitInsn(DUP);
               mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Integer", "Ljava/lang/Class;");
               Label l15 = new Label();
               mv.visitJumpInsn(GOTO, l15);
            mv.visitLabel(l14);
               mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Integer", "Ljava/lang/Class;");
            mv.visitLabel(l15);
               Label l16 = new Label();
               mv.visitJumpInsn(IF_ACMPNE, l16);
               mv.visitVarInsn(ALOAD, 0);
               mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;)Ljava/lang/Integer;");
               mv.visitInsn(ARETURN);

            mv.visitLabel(l16);                     // test for "Long" wrapper class
               mv.visitVarInsn(ALOAD, 1);           // load clz argument
               mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Long", "Ljava/lang/Class;");
               Label l17 = new Label();
               mv.visitJumpInsn(IFNONNULL, l17);
               mv.visitLdcInsn("java.lang.Long");
               mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
               mv.visitInsn(DUP);
               mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Long", "Ljava/lang/Class;");
               Label l18 = new Label();
               mv.visitJumpInsn(GOTO, l18);
            mv.visitLabel(l17);
               mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Long", "Ljava/lang/Class;");
            mv.visitLabel(l18);
               Label l19 = new Label();
               mv.visitJumpInsn(IF_ACMPNE, l19);
               mv.visitVarInsn(ALOAD, 0);
               mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(Ljava/lang/String;)Ljava/lang/Long;");
               mv.visitInsn(ARETURN);

            mv.visitLabel(l19);                     // test for "Float" wrapper class
               mv.visitVarInsn(ALOAD, 1);           // load clz argument
               mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Float", "Ljava/lang/Class;");
               Label l20 = new Label();
               mv.visitJumpInsn(IFNONNULL, l20);
               mv.visitLdcInsn("java.lang.Float");
               mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
               mv.visitInsn(DUP);
               mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Float", "Ljava/lang/Class;");
               Label l21 = new Label();
               mv.visitJumpInsn(GOTO, l21);
            mv.visitLabel(l20);
               mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Float", "Ljava/lang/Class;");
            mv.visitLabel(l21);
               Label l22 = new Label();
               mv.visitJumpInsn(IF_ACMPNE, l22);
               mv.visitVarInsn(ALOAD, 0);
               mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(Ljava/lang/String;)Ljava/lang/Float;");
               mv.visitInsn(ARETURN);

            mv.visitLabel(l22);                     // test for "Float" wrapper class
               mv.visitVarInsn(ALOAD, 0);           // load clz argument
               mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(Ljava/lang/String;)Ljava/lang/Double;");
               mv.visitInsn(ARETURN);

               mv.visitMaxs(4, 2);
               mv.visitEnd();
            }
        }
        else    // >= V1_5
        {
            // -------------------------------------------------------------------------------
            // add static method "convertFromStringTo$": convert a string value to matching wrapper class object
            mv = cv.visitMethod(ACC_STATIC + ACC_SYNTHETIC, "convertFromStringTo$", "(Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/Object;", null, null);
            if (mv!=null)
            {
               mv.visitCode();
                                                    // test for "Boolean" wrapper class
               mv.visitVarInsn(ALOAD, 1);           // load clz argument
               mv.visitLdcInsn(Type.getType("Ljava/lang/Boolean;"));
               Label l0 = new Label();
               mv.visitJumpInsn(IF_ACMPNE, l0);
               mv.visitVarInsn(ALOAD, 0);
               mv.visitLdcInsn("1");
               mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
               Label l1 = new Label();
               mv.visitJumpInsn(IFNE, l1);
               mv.visitVarInsn(ALOAD, 0);
               mv.visitLdcInsn("true");
               mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equalsIgnoreCase", "(Ljava/lang/String;)Z");
               Label l2 = new Label();
               mv.visitJumpInsn(IFEQ, l2);
            mv.visitLabel(l1);
               mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;");
               mv.visitInsn(ARETURN);
            mv.visitLabel(l2);
               mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;");
               mv.visitInsn(ARETURN);

            mv.visitLabel(l0);                      // test for "Byte" wrapper class
               mv.visitVarInsn(ALOAD, 1);           // load clz argument
               mv.visitLdcInsn(Type.getType("Ljava/lang/Byte;"));
               Label l3 = new Label();
               mv.visitJumpInsn(IF_ACMPNE, l3);
               mv.visitVarInsn(ALOAD, 0);
               mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(Ljava/lang/String;)Ljava/lang/Byte;");
               mv.visitInsn(ARETURN);

            mv.visitLabel(l3);                      // test for "Character" wrapper class
               mv.visitVarInsn(ALOAD, 1);           // load clz argument
               mv.visitLdcInsn(Type.getType("Ljava/lang/Character;"));
               Label l4 = new Label();
               mv.visitJumpInsn(IF_ACMPNE, l4);
               mv.visitTypeInsn(NEW, "java/lang/Character");
               mv.visitInsn(DUP);
               mv.visitVarInsn(ALOAD, 0);
               mv.visitInsn(ICONST_0);
               mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "charAt", "(I)C");
               mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Character", "<init>", "(C)V");
               mv.visitInsn(ARETURN);

            mv.visitLabel(l4);                      // test for "Short" wrapper class
               mv.visitVarInsn(ALOAD, 1);           // load clz argument
               mv.visitLdcInsn(Type.getType("Ljava/lang/Short;"));
               Label l5 = new Label();
               mv.visitJumpInsn(IF_ACMPNE, l5);
               mv.visitVarInsn(ALOAD, 0);
               mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(Ljava/lang/String;)Ljava/lang/Short;");
               mv.visitInsn(ARETURN);

            mv.visitLabel(l5);                      // test for "Integer" wrapper class
               mv.visitVarInsn(ALOAD, 1);           // load clz argument
               mv.visitLdcInsn(Type.getType("Ljava/lang/Integer;"));
               Label l6 = new Label();
               mv.visitJumpInsn(IF_ACMPNE, l6);
               mv.visitVarInsn(ALOAD, 0);
               mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(Ljava/lang/String;)Ljava/lang/Integer;");
               mv.visitInsn(ARETURN);

            mv.visitLabel(l6);                      // test for "Long" wrapper class
               mv.visitVarInsn(ALOAD, 1);           // load clz argument
               mv.visitLdcInsn(Type.getType("Ljava/lang/Long;"));
               Label l7 = new Label();
               mv.visitJumpInsn(IF_ACMPNE, l7);
               mv.visitVarInsn(ALOAD, 0);
               mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(Ljava/lang/String;)Ljava/lang/Long;");
               mv.visitInsn(ARETURN);

            mv.visitLabel(l7);                      // test for "Float" wrapper class
               mv.visitVarInsn(ALOAD, 1);           // load clz argument
               mv.visitLdcInsn(Type.getType("Ljava/lang/Float;"));
               Label l8 = new Label();
               mv.visitJumpInsn(IF_ACMPNE, l8);
               mv.visitVarInsn(ALOAD, 0);
               mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(Ljava/lang/String;)Ljava/lang/Float;");
               mv.visitInsn(ARETURN);

            mv.visitLabel(l8);                      // test for "Double" wrapper class
               mv.visitVarInsn(ALOAD, 0);           // load clz argument
               mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(Ljava/lang/String;)Ljava/lang/Double;");
               mv.visitInsn(ARETURN);

               mv.visitMaxs(4, 2);
               mv.visitEnd();
            }
        }

    }



    /** Create class initializer method.
     */
    void workAddClinitMethod(ClassVisitor cv)
    {
        MethodVisitor mv=null;

        mv = cv.visitMethod(ACC_STATIC + ACC_SYNTHETIC, "<clinit>", "()V", null, null);
        if (mv!=null)
        {
            mv.visitCode();

            // initialize "classLock": create an object, assign it to "classLock"
            mv.visitTypeInsn(NEW, "java/lang/Object");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
            mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "classLock", "Ljava/lang/Object;");

            // set "defaultHandler" to null
            mv.visitInsn(ACONST_NULL);
            mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "defaultHandler", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;");
//    mv.visitMaxs(2, 0);


            // if we create the "forwardToSuper" methods, initialize static collection
            if (tranData.isForward2SuperNeeded)
            {
                // initialize "prim2obj$" and "prim2ind$"
                if (tranData.srcClassVersion<V1_5)
                {
                    // initialize "prim2obj$"
                   mv.visitInsn(ACONST_NULL);       // set to null
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");

                   mv.visitInsn(ACONST_NULL);       // set to null
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");

                   mv.visitTypeInsn(NEW, "java/util/Hashtable");
                   mv.visitInsn(DUP);
                   mv.visitMethodInsn(INVOKESPECIAL, "java/util/Hashtable", "<init>", "()V");
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");

                   mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Boolean", "Ljava/lang/Class;");
                   Label l0 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l0);
                   mv.visitLdcInsn("java.lang.Boolean");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Boolean", "Ljava/lang/Class;");
                   Label l1 = new Label();
                   mv.visitJumpInsn(GOTO, l1);
                mv.visitLabel(l0);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Boolean", "Ljava/lang/Class;");
                mv.visitLabel(l1);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Byte", "Ljava/lang/Class;");
                   Label l2 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l2);
                   mv.visitLdcInsn("java.lang.Byte");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Byte", "Ljava/lang/Class;");
                   Label l3 = new Label();
                   mv.visitJumpInsn(GOTO, l3);
                mv.visitLabel(l2);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Byte", "Ljava/lang/Class;");
                mv.visitLabel(l3);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Character", "Ljava/lang/Class;");
                   Label l4 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l4);
                   mv.visitLdcInsn("java.lang.Character");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Character", "Ljava/lang/Class;");
                   Label l5 = new Label();
                   mv.visitJumpInsn(GOTO, l5);
                mv.visitLabel(l4);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Character", "Ljava/lang/Class;");
                mv.visitLabel(l5);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Short", "Ljava/lang/Class;");
                   Label l6 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l6);
                   mv.visitLdcInsn("java.lang.Short");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Short", "Ljava/lang/Class;");
                   Label l7 = new Label();
                   mv.visitJumpInsn(GOTO, l7);
                mv.visitLabel(l6);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Short", "Ljava/lang/Class;");
                mv.visitLabel(l7);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Integer", "Ljava/lang/Class;");
                   Label l8 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l8);
                   mv.visitLdcInsn("java.lang.Integer");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Integer", "Ljava/lang/Class;");
                   Label l9 = new Label();
                   mv.visitJumpInsn(GOTO, l9);
                mv.visitLabel(l8);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Integer", "Ljava/lang/Class;");
                mv.visitLabel(l9);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Long", "Ljava/lang/Class;");
                   Label l10 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l10);
                   mv.visitLdcInsn("java.lang.Long");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Long", "Ljava/lang/Class;");
                   Label l11 = new Label();
                   mv.visitJumpInsn(GOTO, l11);
                mv.visitLabel(l10);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Long", "Ljava/lang/Class;");
                mv.visitLabel(l11);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Float", "Ljava/lang/Class;");
                   Label l12 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l12);
                   mv.visitLdcInsn("java.lang.Float");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Float", "Ljava/lang/Class;");
                   Label l13 = new Label();
                   mv.visitJumpInsn(GOTO, l13);
                mv.visitLabel(l12);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Float", "Ljava/lang/Class;");
                mv.visitLabel(l13);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Double", "Ljava/lang/Class;");
                   Label l14 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l14);
                   mv.visitLdcInsn("java.lang.Double");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Double", "Ljava/lang/Class;");
                   Label l15 = new Label();
                   mv.visitJumpInsn(GOTO, l15);
                mv.visitLabel(l14);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Double", "Ljava/lang/Class;");
                mv.visitLabel(l15);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Boolean", "Ljava/lang/Class;");
                   Label l16 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l16);
                   mv.visitLdcInsn("java.lang.Boolean");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Boolean", "Ljava/lang/Class;");
                   Label l17 = new Label();
                   mv.visitJumpInsn(GOTO, l17);
                mv.visitLabel(l16);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Boolean", "Ljava/lang/Class;");
                mv.visitLabel(l17);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Boolean", "Ljava/lang/Class;");
                   Label l18 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l18);
                   mv.visitLdcInsn("java.lang.Boolean");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Boolean", "Ljava/lang/Class;");
                   Label l19 = new Label();
                   mv.visitJumpInsn(GOTO, l19);
                mv.visitLabel(l18);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Boolean", "Ljava/lang/Class;");
                mv.visitLabel(l19);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Byte", "Ljava/lang/Class;");
                   Label l20 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l20);
                   mv.visitLdcInsn("java.lang.Byte");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Byte", "Ljava/lang/Class;");
                   Label l21 = new Label();
                   mv.visitJumpInsn(GOTO, l21);
                mv.visitLabel(l20);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Byte", "Ljava/lang/Class;");
                mv.visitLabel(l21);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Byte", "Ljava/lang/Class;");
                   Label l22 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l22);
                   mv.visitLdcInsn("java.lang.Byte");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Byte", "Ljava/lang/Class;");
                   Label l23 = new Label();
                   mv.visitJumpInsn(GOTO, l23);
                mv.visitLabel(l22);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Byte", "Ljava/lang/Class;");
                mv.visitLabel(l23);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Character", "Ljava/lang/Class;");
                   Label l24 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l24);
                   mv.visitLdcInsn("java.lang.Character");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Character", "Ljava/lang/Class;");
                   Label l25 = new Label();
                   mv.visitJumpInsn(GOTO, l25);
                mv.visitLabel(l24);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Character", "Ljava/lang/Class;");
                mv.visitLabel(l25);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Character", "Ljava/lang/Class;");
                   Label l26 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l26);
                   mv.visitLdcInsn("java.lang.Character");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Character", "Ljava/lang/Class;");
                   Label l27 = new Label();
                   mv.visitJumpInsn(GOTO, l27);
                mv.visitLabel(l26);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Character", "Ljava/lang/Class;");
                mv.visitLabel(l27);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Short", "Ljava/lang/Class;");
                   Label l28 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l28);
                   mv.visitLdcInsn("java.lang.Short");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Short", "Ljava/lang/Class;");
                   Label l29 = new Label();
                   mv.visitJumpInsn(GOTO, l29);
                mv.visitLabel(l28);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Short", "Ljava/lang/Class;");
                mv.visitLabel(l29);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Short", "Ljava/lang/Class;");
                   Label l30 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l30);
                   mv.visitLdcInsn("java.lang.Short");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Short", "Ljava/lang/Class;");
                   Label l31 = new Label();
                   mv.visitJumpInsn(GOTO, l31);
                mv.visitLabel(l30);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Short", "Ljava/lang/Class;");
                mv.visitLabel(l31);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Integer", "Ljava/lang/Class;");
                   Label l32 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l32);
                   mv.visitLdcInsn("java.lang.Integer");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Integer", "Ljava/lang/Class;");
                   Label l33 = new Label();
                   mv.visitJumpInsn(GOTO, l33);
                mv.visitLabel(l32);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Integer", "Ljava/lang/Class;");
                mv.visitLabel(l33);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Integer", "Ljava/lang/Class;");
                   Label l34 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l34);
                   mv.visitLdcInsn("java.lang.Integer");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Integer", "Ljava/lang/Class;");
                   Label l35 = new Label();
                   mv.visitJumpInsn(GOTO, l35);
                mv.visitLabel(l34);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Integer", "Ljava/lang/Class;");
                mv.visitLabel(l35);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Long", "Ljava/lang/Class;");
                   Label l36 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l36);
                   mv.visitLdcInsn("java.lang.Long");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Long", "Ljava/lang/Class;");
                   Label l37 = new Label();
                   mv.visitJumpInsn(GOTO, l37);
                mv.visitLabel(l36);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Long", "Ljava/lang/Class;");
                mv.visitLabel(l37);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Long", "Ljava/lang/Class;");
                   Label l38 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l38);
                   mv.visitLdcInsn("java.lang.Long");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Long", "Ljava/lang/Class;");
                   Label l39 = new Label();
                   mv.visitJumpInsn(GOTO, l39);
                mv.visitLabel(l38);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Long", "Ljava/lang/Class;");
                mv.visitLabel(l39);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Float", "Ljava/lang/Class;");
                   Label l40 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l40);
                   mv.visitLdcInsn("java.lang.Float");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Float", "Ljava/lang/Class;");
                   Label l41 = new Label();
                   mv.visitJumpInsn(GOTO, l41);
                mv.visitLabel(l40);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Float", "Ljava/lang/Class;");
                mv.visitLabel(l41);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Float", "Ljava/lang/Class;");
                   Label l42 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l42);
                   mv.visitLdcInsn("java.lang.Float");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Float", "Ljava/lang/Class;");
                   Label l43 = new Label();
                   mv.visitJumpInsn(GOTO, l43);
                mv.visitLabel(l42);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Float", "Ljava/lang/Class;");
                mv.visitLabel(l43);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Double", "Ljava/lang/Class;");
                   Label l44 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l44);
                   mv.visitLdcInsn("java.lang.Double");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Double", "Ljava/lang/Class;");
                   Label l45 = new Label();
                   mv.visitJumpInsn(GOTO, l45);
                mv.visitLabel(l44);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Double", "Ljava/lang/Class;");
                mv.visitLabel(l45);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Double", "Ljava/lang/Class;");
                   Label l46 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l46);
                   mv.visitLdcInsn("java.lang.Double");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Double", "Ljava/lang/Class;");
                   Label l47 = new Label();
                   mv.visitJumpInsn(GOTO, l47);
                mv.visitLabel(l46);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Double", "Ljava/lang/Class;");
                mv.visitLabel(l47);
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);


                   // initialize "prim2ind$"
                   mv.visitTypeInsn(NEW, "java/util/Hashtable");
                   mv.visitInsn(DUP);
                   mv.visitMethodInsn(INVOKESPECIAL, "java/util/Hashtable", "<init>", "()V");
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
                   mv.visitLdcInsn("BO");
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
                   mv.visitLdcInsn("BY");
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
                   mv.visitLdcInsn("SH");
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
                   mv.visitLdcInsn("I");
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
                   mv.visitLdcInsn("L");
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
                   mv.visitLdcInsn("F");
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
                   mv.visitLdcInsn("D");
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Boolean", "Ljava/lang/Class;");
                   Label l48 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l48);
                   mv.visitLdcInsn("java.lang.Boolean");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Boolean", "Ljava/lang/Class;");
                   Label l49 = new Label();
                   mv.visitJumpInsn(GOTO, l49);
                mv.visitLabel(l48);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Boolean", "Ljava/lang/Class;");
                mv.visitLabel(l49);
                   mv.visitLdcInsn("BO");
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Byte", "Ljava/lang/Class;");
                   Label l50 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l50);
                   mv.visitLdcInsn("java.lang.Byte");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Byte", "Ljava/lang/Class;");
                   Label l51 = new Label();
                   mv.visitJumpInsn(GOTO, l51);
                mv.visitLabel(l50);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Byte", "Ljava/lang/Class;");
                mv.visitLabel(l51);
                   mv.visitLdcInsn("BY");
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Short", "Ljava/lang/Class;");
                   Label l52 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l52);
                   mv.visitLdcInsn("java.lang.Short");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Short", "Ljava/lang/Class;");
                   Label l53 = new Label();
                   mv.visitJumpInsn(GOTO, l53);
                mv.visitLabel(l52);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Short", "Ljava/lang/Class;");
                mv.visitLabel(l53);
                   mv.visitLdcInsn("SH");
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Integer", "Ljava/lang/Class;");
                   Label l54 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l54);
                   mv.visitLdcInsn("java.lang.Integer");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Integer", "Ljava/lang/Class;");
                   Label l55 = new Label();
                   mv.visitJumpInsn(GOTO, l55);
                mv.visitLabel(l54);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Integer", "Ljava/lang/Class;");
                mv.visitLabel(l55);
                   mv.visitLdcInsn("I");
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Long", "Ljava/lang/Class;");
                   Label l56 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l56);
                   mv.visitLdcInsn("java.lang.Long");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Long", "Ljava/lang/Class;");
                   Label l57 = new Label();
                   mv.visitJumpInsn(GOTO, l57);
                mv.visitLabel(l56);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Long", "Ljava/lang/Class;");
                mv.visitLabel(l57);
                   mv.visitLdcInsn("L");
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Float", "Ljava/lang/Class;");
                   Label l58 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l58);
                   mv.visitLdcInsn("java.lang.Float");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Float", "Ljava/lang/Class;");
                   Label l59 = new Label();
                   mv.visitJumpInsn(GOTO, l59);
                mv.visitLabel(l58);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Float", "Ljava/lang/Class;");
                mv.visitLabel(l59);
                   mv.visitLdcInsn("F");
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);

                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Double", "Ljava/lang/Class;");
                   Label l60 = new Label();
                   mv.visitJumpInsn(IFNONNULL, l60);
                   mv.visitLdcInsn("java.lang.Double");
                   mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                   mv.visitInsn(DUP);
                   mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "class$java$lang$Double", "Ljava/lang/Class;");
                   Label l61 = new Label();
                   mv.visitJumpInsn(GOTO, l61);
                mv.visitLabel(l60);
                   mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "class$java$lang$Double", "Ljava/lang/Class;");
                mv.visitLabel(l61);
                   mv.visitLdcInsn("D");
                   mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                   mv.visitInsn(POP);
//    mv.visitMaxs(4, 0);
                }
                else    // >= V1_5
                {
                    // initialize "prim2obj$"
                    mv.visitInsn(ACONST_NULL);
                    mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitInsn(ACONST_NULL);
                    mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");

                    mv.visitTypeInsn(NEW, "java/util/Hashtable");
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKESPECIAL, "java/util/Hashtable", "<init>", "()V");
                    mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Boolean;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Byte;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Character;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Short;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Integer;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Long;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Float;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Double;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Boolean;"));
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Boolean;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Byte;"));
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Byte;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Character;"));
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Character;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Short;"));
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Short;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Integer;"));
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Integer;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Long;"));
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Long;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Float;"));
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Float;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2obj$", "Ljava/util/Hashtable;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Double;"));
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Double;"));
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    // initialize "prim2ind$"
                    mv.visitTypeInsn(NEW, "java/util/Hashtable");
                    mv.visitInsn(DUP);
                    mv.visitMethodInsn(INVOKESPECIAL, "java/util/Hashtable", "<init>", "()V");
                    mv.visitFieldInsn(PUTSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
                    mv.visitLdcInsn("BO");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
                    mv.visitLdcInsn("BY");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
                    mv.visitLdcInsn("SH");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
                    mv.visitLdcInsn("I");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
                    mv.visitLdcInsn("L");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
                    mv.visitLdcInsn("F");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                    mv.visitFieldInsn(GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
                    mv.visitLdcInsn("D");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Boolean;"));
                    mv.visitLdcInsn("BO");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Byte;"));
                    mv.visitLdcInsn("BY");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Short;"));
                    mv.visitLdcInsn("SH");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Integer;"));
                    mv.visitLdcInsn("I");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Long;"));
                    mv.visitLdcInsn("L");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Float;"));
                    mv.visitLdcInsn("F");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);

                    mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "prim2ind$", "Ljava/util/Hashtable;");
                    mv.visitLdcInsn(Type.getType("Ljava/lang/Double;"));
                    mv.visitLdcInsn("D");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Hashtable", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
                    mv.visitInsn(POP);
//   mv.visitMaxs(3, 0);
                }
            }

            mv.visitInsn(RETURN);   // return from initializer
            mv.visitMaxs( (tranData.srcClassVersion<V1_5 ? 4 : 3) , 0);
            mv.visitEnd();
        }
    }



}



// ==========================================================================================

/** Class which allows for maintaining class and method relevant data for
 *  the transformation purpose.
*/
class TransformationData
{
    /* transformation related maintenance fields    */
    HashSet syntheticStaticFieldsToAdd=null;  // in < V1_5: there is a need for synthetic static fields that
                                        // are used to store the class object retrieved via the synthetic
                                        // static method "class$"

    HashSet simpleMethodNamesToAdd=new HashSet();   // set of methods to add, if extending a regular class
    HashSet simpleMethodNamesAdded=new HashSet();   // methodNames added so far
    HashSet fullMethodNamesAdded  =new HashSet();   // methodName+internalMethodDesc added so far

    String   workPrintLnTarget    = "java/lang/StringBuilder";    // >= V1_5 (was "java/lang/StringBuffer" in earlier versions)
    String   workPrintLnTargetDesc= "Ljava/lang/StringBuilder;";  // >= V1_5 (was "java/lang/StringBuffer" in earlier versions)


    /* class relevant information   */
    int      srcClassVersion=0;         // Indicates Java version, e.g. V1_2, V1_5, V1_6, V1_7
    int      srcClassAccess=0;          // access rights
    String   srcClassName=null;         // internal name of source class
    String   srcClassSignature=null;    // >= V 1.5 signature, can be null
    String   srcClassSuperName=null;    // internal name
    String []srcClassInterfaces=null;   // implemented interfaces

    String newClassName=null;           // internal name of new ("target") class
    int    newClassAccess=0;            // new access rights (e.g. ACC_ABSTRACT removed)


    /* method relevant information  */
    int      srcMethodAccess=0;         // access rights
    String   srcMethodName=null;        // method name
    String   srcMethodDesc=null;        // internal description of argument and return value types
    String   srcMethodSignature=null;   // >= V 1.5 signature, can be null
    String []srcMethodsExceptions=null; // exceptions method throws

    String   newMethodDesc=null;        // constructors get a RexxProxy argument injected as first argument!
    String []newMethodsExceptions=null; // exceptions method throws, must contain at least RexxException
    int      newMethodAccess=0;         // new access rights (e.g. ACC_ABSTRACT removed)

    boolean  isMethodConstructor=false; // flags constructors
    boolean  isMethodStatic     =false; // flags static methods
    boolean  isForward2SuperNeeded=false;   // flags need for super forwarding (if masking a method in super has occurred)

    /* desc related information */
    Type   []workArgumentTypes=null;    // contains injected RexxProxy (as 1st arg)
    Type   []srcArgumentTypes=null;     // represents the original argument types
    Type     srcReturnType=null;        // return type

        // the following may not be used, if letting asm figure out the sizesb
    int      workMaxStackSize=0;        // maximum stack size needed for this method
    int      workMaxLocalSize=0;        // maximum local var size needed for this method
}




