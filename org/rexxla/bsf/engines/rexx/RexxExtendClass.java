package org.rexxla.bsf.engines.rexx;

// DONE: ---rgf, 2009-09-06
//         - change logic for allowing access to masked methods in super, by creating
//           individual methods that invoke the masked method in super; these methods
//           get the string "_forwardToSuper" appended to them, but keep the signature
//
//         - supply class object in static method invocations, such that called back Rexx methods
//           get direct access to it such that static "_forwardToSuper" are possible
//
// 2009-09-05, ---rgf
//      - while testing and arriving at "forwardToSuper(String, Object[])" it became clear, that
//        "Method.invoke(Object, Object[])" for instance methods will cause a dynamic method lookup
//        and not honoring Method.getDeclaringClass();
//
//        due to time-constraints (far too much time invested into BSF4ooRexx 4.0.0, due to the need
//        to override an incredible amount of problems (technical, conceptional), "forwardToSuper"
//        will not be enhanced to cater for this situation for the time being (currently having asm
//        calculate the size of the stack, which probably needs to be deactivated, as a generic
//        method will have to deal with direct invocation of the method in super, which means that
//        for different methods different types and amounts of arguments need to be pushed on to
//        the stack; maybe this gets implemented sometimes in the future, if the "quick" solution
//        below does not work acceptably well
//
//        rather, for all methods that get masked by the dynamically created subclass an additional
//        method will get created with the same signature, but the method's name will be appended
//        with "_forwardToSuper" and this version *will* invoke the method in the super's scope.
//        This actually is adhering to the KISS-principle! ;-)


// DONE: ---rgf, 2009-07-26, 2009-07-30, 2009-07-31, 2009-08-01, 2009-09-04
//
//          -> test with 1.4, 1.5, 1.6
//          -> BsfCreateRexxProxy():
//              - first argument MUST be interface (name or clz) or abstract (name or clz)
//                  - interface [, int2]...     returns JavaProxy
//                  - abstract  [, arg1]...     extends abstract class, creates instance and uses it
//
//          -> bsf.createArrayOf
//
//          - add forwarding to RexxProxy optionally in constructor (either if "<all>" or "<init>"
//            is given; method <desc> will only show the original constructor's desc
//
//          - determine a unique class name (such that creating multiple class instances of the
//            same base class is rather safe
//
//          - no constructor: it is conceivable that there are
//            classes that have no public constructor; in order to deal with those, the
//            methods "setTargetRexxProxy()" and "getTargetRexxProxy()" get created, which allow
//            to set and query the RexxProxy such a Java object will use to forward the
//            method invocations to
//
//          - if not all denoted names could be found, then throw error: OK, implemented
//
//          - indicate to proxy all methods: OK, implemented (just supply null or an empty list
//            for the methods to proxy)
//
//          - add "setTargetRexxProxy(RexxProxy)" and "getTargetRexxProxy()": OK, implemented
//
//          -> allow creating and loading an extended class where denoted method names are implemented
//             in Rexx: OK, implemented


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
 *  supplied in the constructor. All public constructors in the extended class expect a
 *  RexxProxy object as the first argument, followed by all the arguments in the defined order,
 *  if arguments are defined in the original class.
 *
 * <p>The extended class supplies the static public methods <code>setDefaultHandler(RexxProxy rp)</code>
 * and <code>RexxProxy getDefaultHandler()</code> to set and get the default RexxProxy handler, which
 * is used for forwarding static methods and when creating instances of the class and superclass constructors
 * invoke instance methods.
 *
 * <p>An instance of the extended class has the methods <code>RexxProxy getTargetRexxProxy()</code>
 * and <code>setTargetRexxProxy(RexxProxy rp)</code> implemented, which allow to retrieve or set
 * the RexxProxy object to which method invocations get forwarded to.
 *
 * <p>If concrete instance methods of a class are generated in the extended class, then a
 * method named <code>&quot;forwardToSuper&quot;</code> and one named
 <code>&quot;forwardToSuperStrict&quot;</code> will be created which allow the Rexx programmer
 * to explicitly invoke masked methods in the super (extended) class.
 *
 * <p>If concrete static methods of a class are generated in the extended class, then a
 * method named <code>&quot;forwardToSuperStatic&quot;</code> and one named
 <code>&quot;forwardToSuperStaticStrict&quot;</code> will be created which allow the Rexx programmer
 * to explicitly invoke masked methods in the super (extended) class.
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

 /*
    2009-09-06, rgf: supply class object in static method invocations for callbacks, such that
                     invoked Rexx method can directly address the class object for "_forwardToSuper"
                     static methods
    2009-09-01, rgf: made sure that class version tests are not carried out against the
                     source class version, but the desired new class version
 */


public class RexxExtendClass extends ClassAdapter implements ClassVisitor, Opcodes
{

    /**
     * The {@link ClassVisitor} to which this adapter delegates calls.
     */
    protected ClassVisitor cv;

    final static protected boolean bDebugClassVisitor=false;    // true;
    final static protected boolean bDebugMethodVisitor=false;   // true;

    final static String thisPgmName="RexxExtendClass";

    final static String rexxExceptionName="org/rexxla/bsf/engines/rexx/RexxException";
    final static String rexxProxyName    ="org/rexxla/bsf/engines/rexx/RexxProxy";

    // define the string we should append to a method that forwards its invocation to super
    final static String FORWARD_TO_SUPER_APPEND_STRING="_forwardToSuper";

    // map primitive types to corresponding wrapper classes
//    final static HashMap access2string= new HashMap();

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
/*
        // define mapping between ACC and corresponding string
        access2string.put(ACC_PUBLIC      , "public");
        access2string.put(ACC_ABSTRACT    , "abstract");
        access2string.put(ACC_ANNOTATION  , "@ANNOTATION");
        access2string.put(ACC_BRIDGE      , "@BRIDGE@");
        access2string.put(ACC_DEPRECATED  , "deprecated");
        access2string.put(ACC_ENUM        , "enum");
        access2string.put(ACC_FINAL       , "final");
        access2string.put(ACC_INTERFACE   , "interface");
        access2string.put(ACC_NATIVE      , "native");
        access2string.put(ACC_PRIVATE     , "private");
        access2string.put(ACC_PROTECTED   , "protected");
        access2string.put(ACC_STATIC      , "static");
        access2string.put(ACC_STRICT      , "strict");
        access2string.put(ACC_SUPER       , "@SUPER@");
        access2string.put(ACC_SYNCHRONIZED, "synchronized");
        access2string.put(ACC_SYNTHETIC   , "synthetic");
        access2string.put(ACC_TRANSIENT   , "transient");
        access2string.put(ACC_VARARGS     , "varargs/...");
        access2string.put(ACC_VOLATILE    , "volatile");
*/

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
    public TransformationData getTranData()
    {
        return tranData;
    }


    /**
     * Constructs a new {@link ClassAdapter} object. This mode of operation will
     * only implement abstract methods which will be executed by the RexxProxy.
     *
     * @param cv the class visitor to which this adapter must delegate calls.
     */
    public RexxExtendClass(final ClassVisitor cv)
    {
        super(cv);
        this.cv = cv;   // save cv in instance field
        tranData=new TransformationData();
    }


    /**
     * Constructs a new {@link ClassAdapter} object. This mode of operation will
     * implement public abstract methods which will be executed by the RexxProxy. In addition
     * it creates proxy methods for each Java method that carries a name as supplied
     * in the <code>methodNamesToAdd</code> array.
     *
     * <p>This constructor will only create proxy methods for public abstract methods.
     *
     * @param cv the class visitor to which this adapter must delegate calls.
     * @param newClassName determines new name of the class, if null, an attempt to create
     *                     a unique name is undertaken
     * @param newClassVersion if >0 overrides the source class' version
     */
    public RexxExtendClass(final ClassVisitor cv,
                          final String newClassName,
                          final int newClassVersion
                          )
    {
        this(cv);
        tranData.newClassName=newClassName; // save new class name, if any

        if (newClassVersion>0)              // save new class version, if any
        {
            tranData.newClassVersion=newClassVersion;
        }

if (bDebugClassVisitor) System.out.println("--> ---> RexxExtendClass(cv, newClassName="+pp(newClassName)+
                                                     ", newClassVersion="+pp(""+newClassVersion));

    }



    /**
     * Constructs a new {@link ClassAdapter} object. This mode of operation will
     * implement public abstract methods which will be executed by the RexxProxy. In addition
     * it creates proxy methods for each Java method that carries a name as supplied
     * in the <code>methodNamesToAdd</code> array. If the <code>methodNamesToAdd</code> array
     * is <code>null</code> or empty, then all public methods will have a proxy method created for.
     *
     * @param cv the class visitor to which this adapter must delegate calls.
     * @param newClassName determines new name of the class, if null, an attempt to create
     *                     a unique name is undertaken
     * @param newClassVersion if >0 overrides the source class' version
     * @param methodNamesToAdd a String array containing (caseless) method names for
     *        which proxy methods should be created; if callbacks to the RexxProxy are desired
     *        from constructors, then they need to be added to this String array under the name
     *        &quot;<init>&quot; (the Java internal name of constructors).
     */
    public RexxExtendClass(final ClassVisitor cv,
                          final String newClassName,
                          final int newClassVersion,
                          final String [] methodNamesToAdd
                          )
    {
        this(cv, newClassName, newClassVersion);
if (bDebugClassVisitor) System.out.println("--> ---> RexxExtendClass(cv, newClassName="+pp(newClassName)+
                                                     ", newClassVersion="+pp(""+newClassVersion)+
                                                     ", methodNamesToAdd)...");

        if (methodNamesToAdd==null || methodNamesToAdd.length==0)
        {
            tranData.bProcessAllMethods=true;   // create proxies for all methods!
        }
        else    // add methods to proxy to appropriate HashSet, turn into uppercase
        {
            for (int i=0; i<methodNamesToAdd.length; i++)
            {
if (bDebugClassVisitor) System.out.println("--> ---> RexxExtendClass(cv, newClassName, ...): i="+i+" ["+methodNamesToAdd[i]+"]");
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
                          ", [] interfaces="+interfaces+")\n\tnewClassVersion="+tranData.newClassVersion);

        if ((access & ACC_INTERFACE)!=0)    // an interface in hand
        {
            throw new RuntimeException(thisPgmName+": \""+name+"\" is an interface, need a class instead");
        }

        if ((access & ACC_FINAL)  != 0)     // a final class in hand
        {
            throw new RuntimeException(thisPgmName+": class \""+name+"\" is final and cannot be extended");
        }

        if ((access & ACC_PUBLIC) == 0)     // not a public class throw-up
        {
            throw new RuntimeException(thisPgmName+": class \""+name+"\" is not public");
        }


        // if only abstract methods to proxy, then make sure class is marked as abstract
        if (  !tranData.bProcessAllMethods &&
              (tranData.simpleMethodNamesToAdd.size()==0) &&
             ((access & ACC_ABSTRACT)==0)
            )
        {
            throw new RuntimeException(thisPgmName+": \""+name+"\" is not an abstract class (supply an abstract class or use a different constructor)");
        }


/*
        // not an abstract class, but no methods to proxy given!
        if ( ((access & ACC_ABSTRACT)==0) && (tranData.simpleMethodNamesToAdd.size()==0) )
        {
            throw new RuntimeException(thisPgmName+": No methods to proxy given for non-abstract class \""+name+"\"");
        }
*/

        tranData.srcClassVersion  =version;     // Java version: can be V1_2, V1_5, V1_6, V1_7
        if (tranData.newClassVersion==0)        // if "0", then set it to source class version
        {
            tranData.newClassVersion=version;
if (bDebugClassVisitor) System.err.println("---> ... tranData.newClassVersion==0, setting to ["+version+"], now=["+tranData.newClassVersion+"]");
        }

        // if (version<V1_5)
        if (tranData.newClassVersion<V1_5)
        {
            tranData.workPrintLnTarget    ="java/lang/StringBuffer";    // Java 1.4
            tranData.workPrintLnTargetDesc="Ljava/lang/StringBuffer;";    // Java 1.4


                // we may need the following fields (for forwardToSuper[Strict])
            tranData.syntheticStaticFieldsToAdd=new HashSet();
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
        if (tranData.newClassName==null)
        {
            String extension="_$"+thisPgmName+"$_"+(new Object()).hashCode();
            tranData.newClassName=name+extension;
            // TODO: remove the following statement (just there for easying debugging)
            // tranData.newClassName=name+"_dynamicallyExtended_abc";
        }

        // make sure, abstract flag gets removed, also flag class as synthetic
        tranData.newClassAccess= (access & ~ACC_ABSTRACT) | ACC_SYNTHETIC;

if (bDebugClassVisitor) System.err.println("    "+"===> creating newClassName=["+tranData.newClassName+"]...");

	// set signature and interfaces to null
// TODO: test whether in V1_6 and above it is o.k. to not have the frame layout injected;
//       otherwise change the version to V1_5 (or if enough time create correct frame codes)

if (bDebugClassVisitor) System.err.println("RexxExtendClass: version="+version+
                   ", tranData.newClassAccess="+tranData.newClassAccess+
                   ", tranData.newClassName  ="+tranData.newClassName+
                   ",                  name  ="+name);

        cv.visit(  ( tranData.newClassVersion>0 ? tranData.newClassVersion: version),
                   tranData.newClassAccess, tranData.newClassName, null, name, null
                );
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

if (bDebugMethodVisitor) System.err.println("\t---> desc=["+getMethodAccessAsString(access)+"]");

        if ((access & ACC_PUBLIC)==0)       // don't process non-public methods
        {

if (bDebugMethodVisitor) System.err.println("\tNOT PROCESSING! (not public)");
            return null;
        }

        // do not process final and/or synthetic methods
        if ((access & (ACC_FINAL | ACC_SYNTHETIC)) != 0)
        {
if (bDebugMethodVisitor) System.err.println("\tNOT PROCESSING! (final or synthetic)");
            return null;
        }

        if (name.equals("<clinit>"))        // do not copy class initializer/constructor, will be built at the end
        {
if (bDebugMethodVisitor) System.err.println("\tNOT PROCESSING! (class initializer)");
            return null;
        }

        // make sure ACC_SYNCHRONIZED and ACC_STRICT ("strictfp") get removed
        tranData.newMethodAccess= (access & ~(ACC_SYNCHRONIZED|ACC_STRICT));

        tranData.isMethodStatic     =((access & ACC_STATIC)!=0);
        tranData.isMethodConstructor=name.equals("<init>");
        tranData.bCallbackFromConstructor=false;    // reset flag to initial state, i.e. do not forward to RexxProxy

        // not processing all methods and not a constructor and not an abstract method
        if (!tranData.bProcessAllMethods  &&  !tranData.isMethodConstructor && ((access & ACC_ABSTRACT)==0))
        {
            // not in list of methods to proxy, hence do not process it
            if (!tranData.simpleMethodNamesToAdd.contains(name.toUpperCase()))
            {
if (bDebugMethodVisitor) System.err.println("\tNOT PROCESSING! (not in list)");
                return null;
            }

            // in case a constructor method got listed explicitly make sure that forwarding to the RexxProxy takes place! 2009-07-30
            tranData.bCallbackFromConstructor=tranData.isMethodConstructor;

            if (tranData.isMethodStatic)    // masking a static method?
            {
                tranData.isForward2SuperStaticNeeded=true;  // we need a way to forward to super's class/static method
            }
            else                            // masking an instance method?
            {
// if (bDebugMethodVisitor) System.err.println("\tsetting 'isForward2SuperNeeded' uncoditionally to 'true'!");

                tranData.isForward2SuperNeeded=true;    // we need a way to forward to super's method
            }
        }
        else    // remove ACC_ABSTRACT flag
        {
            // make sure we set the forwardToSuperNeeded flag, if processing a concrete method
            if (tranData.isMethodStatic)            // masking a static method?
            {
                tranData.isForward2SuperStaticNeeded=tranData.isForward2SuperStaticNeeded || (((access & ACC_ABSTRACT)==0));
            }
            else                                    // masking an instance method?
            {
// if (bDebugMethodVisitor) System.err.println("\tsetting 'isForward2SuperNeeded', MAYBE, actual=["+tranData.isForward2SuperNeeded+"]: 'ACC_ABSTRACT'=['"+ACC_ABSTRACT+"']; (access & ACC_ABSTRACT)=["+(access & ACC_ABSTRACT)+"]");

                //
                if (tranData.bProcessAllMethods && !tranData.isMethodConstructor)
                {
                    tranData.isForward2SuperNeeded=tranData.isForward2SuperNeeded || (((access & ACC_ABSTRACT)==0));
                }

// if (bDebugMethodVisitor) System.err.println("\tsetting 'isForward2SuperNeeded', MAYBE now 'true': ["+tranData.isForward2SuperNeeded+"]");
            }

            tranData.newMethodAccess= (access & ~ACC_ABSTRACT); // make sure, abstract flag gets removed

            if (tranData.bProcessAllMethods)
            {
                // add this method to those that we intended to add (all public methods)
                tranData.simpleMethodNamesToAdd.add(name.toUpperCase());
            }
        }


        if (exceptions!=null)   // add RexxException to list of exceptions
        {
            boolean bFound=false;
            for (int i=0; i<exceptions.length && bFound==false; i++)
            {
               bFound=bFound || exceptions[i].equals(rexxExceptionName);
            }

            // determine number of elements
            tranData.newMethodsExceptions=new String[exceptions.length+(bFound?0:1)];
            // make a copy of the exceptions array
            System.arraycopy(exceptions, 0,
                             tranData.newMethodsExceptions, 0,
                             exceptions.length);

            if (!bFound)    // new array got extended by one slot
            {
                tranData.newMethodsExceptions[exceptions.length]=rexxExceptionName;
            }
        }
        else    // create exception list with RexxException as its sole element
        {
            tranData.newMethodsExceptions=new String[]{rexxExceptionName};
        }

            // save values
        tranData.srcMethodAccess=access;
        tranData.srcMethodName=name;
        tranData.srcMethodDesc= desc;
        tranData.srcMethodSignature=signature;   // >= V 1.5 signature, can be null
        tranData.srcMethodsExceptions=exceptions; // exceptions method throws

        tranData.srcArgumentTypes =Type.getArgumentTypes(tranData.srcMethodDesc);
        tranData.srcReturnType    =Type.getReturnType(tranData.srcMethodDesc);


        // inject RexxProxy object as first argument ?
        tranData.newMethodDesc= (tranData.isMethodConstructor ? "(L"+rexxProxyName+";"+desc.substring(1)
                                                              : desc);

        tranData.workArgumentTypes=Type.getArgumentTypes(tranData.newMethodDesc);

        // create our proxy method
if (bDebugMethodVisitor) System.err.println("\t---> tranData.newMethodAccess=["+getMethodAccessAsString(tranData.newMethodAccess)+"]");
if (bDebugMethodVisitor) System.err.println("\t---> name=["+name+"], newMethodDesc=["+tranData.newMethodDesc+"], signature=["+signature+"]");
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
        tranData.simpleMethodNamesAdded.add(name.toUpperCase());      // memorize method

        // tranData.fullMethodNamesAdded.add(name+tranData.newMethodDesc); // memorize method with desc (= signature)
        tranData.fullMethodNamesAdded.add(name+desc);   // memorize method with desc (= signature)

        // create code
        mv.visitCode();

        if (tranData.isMethodConstructor)
        {
            workConstructor(mv);
            tranData.hasConstructorDefined=true;    // remember that a constructor got defined
        }
        else
        {
            // rgf, 20090905: a concrete method, hence it gets masked, remember it
            if ((access & ACC_ABSTRACT)==0)
            {
                tranData.listOfMethodsForwardToSuper.add(
                      new MethodForSuperData(access, name, desc, signature, exceptions,
                                             tranData.isMethodStatic,
                                             tranData.srcClassName
                                            )
                      );
            }

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
        // if returning mv, other filters may add code (e.g. in <init>s)
        // return mv;
    }


    // place to add missing members (fields, methods)
    public void visitEnd() {
if (bDebugClassVisitor) System.err.println("--> "+"visitEnd().");

        workAddFields(cv);          // add field definitions
        workAddMethods(cv);         // add instance and static methods we need
        workAddClinitMethod(cv);    // add class initializer code

            // if methods got masked, create slot methods to super
        for (int i=0; i<tranData.listOfMethodsForwardToSuper.size(); i++)
        {
            ((MethodForSuperData) tranData.listOfMethodsForwardToSuper.get(i)).createMethod(cv);
        }

        cv.visitEnd();              // class visitation ended.


/*
        // check whether we have at least one constructor defined, in case we insist on an
        // initializable extended class; however, it is conceivable that there are
        // classes that have no public constructor; in order to deal with those, there
        // are the methods "setTargetRexxProxy()" and "getTargetRexxProxy()", which allow
        // to set and query the RexxProxy such a Java object will use to forward the
        // method invocations
        if (!tranData.hasConstructorDefined)
        {
            throw new RuntimeException(thisPgmName+": Could not find a public constructor in class \""+tranData.srcClassName+"\"");
        }
*/

        // check whether given list of method names to proxy was fulfilled, if not throw an appropriate exception
        if (!tranData.bProcessAllMethods && !tranData.simpleMethodNamesAdded.containsAll(tranData.simpleMethodNamesToAdd))
        {
            HashSet diff=new HashSet();
            diff.addAll(tranData.simpleMethodNamesToAdd);       // add all method names
            diff.removeAll(tranData.simpleMethodNamesAdded);    // remove the added method names

            throw new RuntimeException(thisPgmName+": class \""+tranData.srcClassName+"\"  misses the following method names (uppercased) from the supplied list: "+diff);
        }
    }


// ==========================================================================================

    /** Creates the bytecode for processing arguments.
     */
    void workConstructor(MethodVisitor mv)
    {
if (bDebugMethodVisitor) System.err.println("\t===> "+"workCreateConstructor(): name="+pp(tranData.srcMethodName)+", desc="+pp(tranData.newMethodDesc));

        mv.visitVarInsn(ALOAD, 0);      // load 'this'

            // load arguments, if any, skip first argument (injected RexxProxy)
        int nextSlot=2;     // position on third var slot (0=this, 1=RexxProxy)
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
        mv.visitFieldInsn(PUTFIELD, tranData.newClassName, "target", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;");

        // set target field to RexxProxy
        mv.visitVarInsn(ALOAD, 0);      // load 'this'
        mv.visitVarInsn(ALOAD, 1);      // load RexxProxy and put it into the "target" field
        mv.visitFieldInsn(PUTFIELD, tranData.newClassName, "target", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;");

        // get maxStack and maxSize for constructor
        int maxArgSize=Type.getArgumentsAndReturnSizes(tranData.newMethodDesc)>>2;

        if (tranData.bProcessAllMethods || tranData.bCallbackFromConstructor)    // callback to RexxProxy!
        {
            workProcessArgsAndInvoke(mv);

            if (maxArgSize>tranData.workMaxStackSize)   // do we need to adjust maxStackSize?
            {
                tranData.workMaxStackSize=maxArgSize;
            }

            if (maxArgSize>tranData.workMaxLocalSize)   // do we need to adjust maxLocalSize?
            {
                tranData.workMaxLocalSize=maxArgSize;
            }
        }
        else    // no callback to RexxProxy, plain return from constructor
        {
            mv.visitInsn(RETURN);           // return
            tranData.workMaxStackSize=maxArgSize;   // "this" + size of all args but first arg (RexxProxy)
            tranData.workMaxLocalSize=maxArgSize;   // "this" + size of all args
        }
    }




    /** Creates the bytecode for processing arguments.
     *  @param mv MethodVisitor object to interact with
     */
    void workProcessArgsAndInvoke(MethodVisitor mv)
    {
if (bDebugMethodVisitor) System.err.println("\t===> "+"workProcessArgsAndInvoke(): name="+pp(tranData.srcMethodName)+", desc="+pp(tranData.srcMethodDesc));

        int rawSize=Type.getArgumentsAndReturnSizes(tranData.srcMethodDesc);  // includes implicit "this"
        int argSize=(rawSize>>2)- 1;    // get total size of arguments; subtract "1" for implicit "this" variable!!
        int retSize=rawSize & 0x03;     // get size of return value

        // static methods: arg1=slot0; instance methods: arg1=slot1

        // first slot to store a local variable, appropriate for a static method;
        // take also constructor into account which has a RexxProxy in slot 2 (which we need to skip here)
        int varProxySlot=argSize+(tranData.isMethodConstructor ? 1 : 0);

        // get RexxProxy object to address (target for method invocation)
        if (tranData.isMethodStatic)
        {
            mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "defaultHandler", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;");
            mv.visitVarInsn(ASTORE, varProxySlot);  // store RexxProxy (the target)
        }
        else    // instance method in hand
        {
            varProxySlot++;            // account for "this" in slot 0
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, tranData.newClassName, "target", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;");
                    // store in local variable to make it easier for decompilers to infer the correct logic (rather than using DUP and assigning to local var at the very last moment)
            mv.visitVarInsn(ASTORE, varProxySlot);  // store RexxProxy var (the target)

            mv.visitVarInsn(ALOAD,  varProxySlot);  // get RexxProxy var
            Label l0 = new Label();
            mv.visitJumpInsn(IFNONNULL, l0);    // hmm, "target" null, get "defaultHandler" instead
            mv.visitFieldInsn(GETSTATIC, tranData.newClassName, "defaultHandler", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;");
            mv.visitVarInsn(ASTORE, varProxySlot);  // store RexxProxy (the target)
         mv.visitLabel(l0);
        }

        int varMethodNameSlot=varProxySlot+1;   // method name
        int varMethodDescSlot=varProxySlot+2;   // method desc
        int varArgArraySlot  =varProxySlot+3;   // array of args or null
        int varResultSlot    =varProxySlot+4;   // result from Rexx method in case we need to return a primitive value

        tranData.workMaxStackSize=5;            // "this" + RexxProxy + other invoke-args (methName, methDesc, argArr)
        tranData.workMaxLocalSize=varResultSlot;  // maximum size of local vars

        // arg1: determine and store methodName
        mv.visitLdcInsn(tranData.srcMethodName);
        mv.visitVarInsn(ASTORE, varMethodNameSlot);

        // arg2: get and store methodDesc
        // mv.visitLdcInsn(tranData.srcMethodDesc);
        // 2009-07-28, rgf: send Rexx the original and a user friendly rendering that can be used in a Java source code program
        mv.visitLdcInsn(getPlainMethodSignature(tranData));
        mv.visitVarInsn(ASTORE, varMethodDescSlot);

        // arg3: determine and store arguments in a single array
        mv.visitInsn(ACONST_NULL);          // set to null
        mv.visitVarInsn(ASTORE, varArgArraySlot);


        // determine nr. of args, if no args, we are done
        //                        else create array of appropriate dimension and assign/convert args to it
        boolean bIncreaseStackSize=false;
        int nrArgs=tranData.srcArgumentTypes.length;   // get nr. of args
        if (nrArgs>0)
        {
            tranData.workMaxStackSize++;        // account for individual index values we need to push
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

            if (tranData.isMethodConstructor)   // skip RexxProxy object in slot 1
            {
                nextArgSlot++;
            }

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
//                    if (tranData.srcClassVersion>=V1_5 || t==Type.BOOLEAN_TYPE)  // Java <= 1.4
                    if (tranData.newClassVersion>=V1_5 || t==Type.BOOLEAN_TYPE)  // Java <= 1.4
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

                int tSize=t.getSize();              // get slot size for this Type
                // nextArgSlot += t.getSize();         // determine slot for next argument
                nextArgSlot += tSize;               // determine slot for next argument
                bIncreaseStackSize=(bIncreaseStackSize || tSize>1);   // do we need to increase the stack size?
            }
            mv.visitVarInsn(ASTORE, varArgArraySlot);  // finally save array object
        }
        if (bIncreaseStackSize)
        {
            tranData.workMaxStackSize=7; // should be 6? : arrObj x 2 + wrapperType x 2 + (J|D) size
        }


        // ------------------------------------------------------------------------------------------
        // invoke method on RexxProxy
        mv.visitVarInsn(ALOAD, varProxySlot);       // receiver

        if (tranData.isMethodStatic)                // argument #1
        {
            // TODO: change to supply the class obejct (of the original class, which serves as 'super')
            //       - mv.visitLdcInsn(Type.getType("L"+tranData.newClassName+";");  // only for Java 1.5 and higher
            //       - need to cater for Java 1.4 as well
            // mv.visitInsn(ACONST_NULL);              // set to null, "this" not available in static context

            if (tranData.newClassVersion<49)        // for Java 1.4 or lower
            {
                // mv.visitInsn(ACONST_NULL);              // set to null, "this" not available in static context

                // get the class object
                mv.visitLdcInsn(tranData.newClassName);
                mv.visitMethodInsn(INVOKESTATIC, tranData.newClassName, "class$", "(Ljava/lang/String;)Ljava/lang/Class;");
                // mv.visitInsn(DUP);
            }
            else    // for Java 1.5 or higher
            {
                mv.visitLdcInsn(Type.getType("L"+tranData.newClassName+";"));
            }
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
            // we use two additional local var slots: one for returned value, one for its copy
            tranData.workMaxLocalSize+=2;

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

            // mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
            mv.visitFieldInsn(GETSTATIC, wrapperTypeInternalName, "TYPE", "Ljava/lang/Class;");
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
if (bDebugMethodVisitor) System.err.println("\t===> "+"workAddFields()...");
        FieldVisitor fv=null;

        // create static field "classLock": if creating an instance of this class, use this field
        //      to lock access to the class as a whole. Set the "defaultHandler" before creating
        //      an instance of this class, such that if some code in a superclass constructor
        //      invokes instance methods of the synthetic class, these invocations can be forwarded
        //      to the RexxProxy stored in the static "defaultHandler"
        fv = cv.visitField(ACC_PUBLIC + ACC_STATIC + ACC_SYNTHETIC, "classLock", "Ljava/lang/Object;", null, null);
        if (fv!=null) fv.visitEnd();

        // create static field "defaultHandler"
        fv = cv.visitField(ACC_PRIVATE + ACC_STATIC  + ACC_VOLATILE + ACC_SYNTHETIC, "defaultHandler", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;", null, null);
        if (fv!=null) fv.visitEnd();


        // if Java 1.4 and forwardToSuper[Strict] needed, then create needed synthetic fields, e.g. "class$java$lang$Double"
        // if (tranData.srcClassVersion<V1_5 && (tranData.isForward2SuperNeeded || tranData.isForward2SuperStaticNeeded))
        if (tranData.newClassVersion<V1_5 && (tranData.isForward2SuperNeeded || tranData.isForward2SuperStaticNeeded))
        {
if (bDebugMethodVisitor) System.err.println("\t     synthetic fields (e.g. \"class$java$lang$Double\"): adding Java 1.4 support for \"forwardToSuper[Strict]\"...");
            for (Iterator i=tranData.syntheticStaticFieldsToAdd.iterator(); i.hasNext(); )
            {
                fv = cv.visitField(ACC_STATIC + ACC_SYNTHETIC, (String) i.next(), "Ljava/lang/Class;", null, null);
                if (fv!=null) fv.visitEnd();
            }

            if (tranData.isForward2SuperStaticNeeded)
            {
                fv = cv.visitField(ACC_STATIC + ACC_SYNTHETIC, "class$ThisClz", "Ljava/lang/Class;", null, null);
                if (fv!=null) fv.visitEnd();
            }
        }

        // create instance field "target"
        fv = cv.visitField(ACC_SYNTHETIC, "target", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;", null, null);
        if (fv!=null) fv.visitEnd();


        // if we create the forwardToSuper() methods, we need the following static fields
        if (tranData.isForward2SuperNeeded || tranData.isForward2SuperStaticNeeded)   // ---rgf, 20090902 (tranData.isForward2SuperNeeded)
        {
if (bDebugMethodVisitor) System.err.println("\t     \"prim2obj$\", \"prim2ind$\": adding support for \"forwardToSuper[Strict]\"...");
            // maps primitive and wrapper types (classes) to respective wrapper types (classes)
           fv = cv.visitField(ACC_STATIC + ACC_SYNTHETIC, "prim2obj$", "Ljava/util/Hashtable;", null, null);
           if (fv!=null) fv.visitEnd();

           // maps primitive and warpper types (classes) to BSF4ooRexx type indicator strings
           fv = cv.visitField(ACC_STATIC + ACC_SYNTHETIC, "prim2ind$", "Ljava/util/Hashtable;", null, null);
           if (fv!=null) fv.visitEnd();
        }
    }



    /** Create instance and static fields we need.
     */
    void workAddMethods(ClassVisitor cv)
    {
if (bDebugMethodVisitor) System.err.println("\t===> "+"workAddMethods()...");
        MethodVisitor mv=null;

        // add getter method: get and return "target" RexxProxy
        mv = cv.visitMethod(ACC_PUBLIC + ACC_SYNCHRONIZED + ACC_SYNTHETIC, "getTargetRexxProxy", "()Lorg/rexxla/bsf/engines/rexx/RexxProxy;", null, null);
        if (mv!=null)
        {
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, tranData.newClassName, "target", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;");
            mv.visitInsn(ARETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();
        }


        // add setter method: set "target" RexxProxy
        mv = cv.visitMethod(ACC_PUBLIC + ACC_SYNCHRONIZED + ACC_SYNTHETIC, "setTargetRexxProxy", "(Lorg/rexxla/bsf/engines/rexx/RexxProxy;)V", null, null);
        if (mv!=null)
        {
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, tranData.newClassName, "target", "Lorg/rexxla/bsf/engines/rexx/RexxProxy;");
            mv.visitInsn(RETURN);
            mv.visitMaxs(2, 2);
            mv.visitEnd();
        }


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


        // Java <= 1.4 and forwardToSuper[Strict] needed
        // if (tranData.srcClassVersion<V1_5 && tranData.isForward2SuperNeeded)
        if (tranData.newClassVersion<V1_5 &&  (tranData.isForward2SuperNeeded || tranData.isForward2SuperStaticNeeded)) // ---rgf, 20090902 tranData.isForward2SuperNeeded)
        {
if (bDebugMethodVisitor) System.err.println("\t     \"class$\": adding support for Java 1.4 and earlier ...");
// System.err.println("tranData.newClassVersion="+tranData.newClassVersion+"<"+V1_5+": \"class$\": adding support for Java 1.4 and earlier ...");
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



    /** Create class initializer method.
     */
    void workAddClinitMethod(ClassVisitor cv)
    {
if (bDebugMethodVisitor) System.err.println("\t===> "+"workAddClinitMethod()...");
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
            if  (tranData.isForward2SuperNeeded || tranData.isForward2SuperStaticNeeded)   // ---rgf, 20090902 (tranData.isForward2SuperNeeded)
            {
                // initialize "prim2obj$" and "prim2ind$"
                // if (tranData.srcClassVersion<V1_5)
                if (tranData.newClassVersion<V1_5)
                {
if (bDebugMethodVisitor) System.err.println("\t     adding support for Java 1.4 and earlier...");
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
                }
            }

            mv.visitInsn(RETURN);   // return from initializer
//            mv.visitMaxs( (tranData.srcClassVersion<V1_5 ? 4 : 3) , 0);
            mv.visitMaxs( (tranData.newClassVersion<V1_5 ? 4 : 3) , 0);
            mv.visitEnd();
        }
    }


    // return methods as string
    public static String getMethodAccessAsString(int access)
    {
        StringBuffer sb=new StringBuffer();

        if ((access & ACC_PUBLIC)!=0) sb.append(sb.length()>0 ? " + " : "").append("ACC_PUBLIC");
        if ((access & ACC_PRIVATE)!=0) sb.append(sb.length()>0 ? " + " : "").append("ACC_PRIVATE");
        if ((access & ACC_PROTECTED)!=0) sb.append(sb.length()>0 ? " + " : "").append("ACC_PROTECTED");
        if ((access & ACC_STATIC)!=0) sb.append(sb.length()>0 ? " + " : "").append("ACC_STATIC");
        if ((access & ACC_FINAL)!=0) sb.append(sb.length()>0 ? " + " : "").append("ACC_FINAL");
        if ((access & ACC_SYNCHRONIZED)!=0) sb.append(sb.length()>0 ? " + " : "").append("ACC_SYNCHRONIZED");
        if ((access & ACC_NATIVE)!=0) sb.append(sb.length()>0 ? " + " : "").append("ACC_NATIVE");
        if ((access & ACC_ABSTRACT)!=0) sb.append(sb.length()>0 ? " + " : "").append("ACC_ABSTRACT");
        if ((access & ACC_STRICT)!=0) sb.append(sb.length()>0 ? " + " : "").append("ACC_STRICT");

        return sb.toString();
    }


    /** Create and return a string with the method's <code>&quot;desc&quot;</code> delimited with
     *  <code>&quot;&nbsp;|&nbsp;&quot;</code> and followed by the method's signature as one would
     *  have to give it in a Java program.
     */
    static String getPlainMethodSignature(TransformationData td)
    {
        StringBuffer sb=new StringBuffer();

        if (td.isMethodConstructor)     // if a constructor, then show original constructor's desc
        {
            sb.append(td.srcMethodDesc);// append descriptor
        }
        else    // not a constructor
        {
            sb.append(td.newMethodDesc);// append descriptor
        }
        sb.append(" | ");               // append delimiter


        int access=td.newMethodAccess;
            // add modifiers
        if ((access & ACC_STATIC) != 0)
        {
            sb.append("static ");
        }

            // mutual exclusive:
        if      ((access & ACC_PUBLIC) != 0) { sb.append("public "); }
/*
        // currently (2009-07-29) only public methods get processed, hence commenting out
        else if ((access & ACC_PRIVATE  ) != 0) { sb.append("private "); }
        else if ((access & ACC_PROTECTED) != 0) { sb.append("protected "); }


        if ((access & ACC_STRICT         ) != 0)
        {
            sb.append("strict ");
        }

        if ((access & ACC_STATIC         ) != 0)
        {
            if (sb.length()!=0) { sb.append(' '); }
            sb.append(access2string.get( ACC_STATIC       ));
        }

            // mutual exclusive:
        if      ((access & ACC_PUBLIC         ) != 0) { sb.append(access2string.get( ACC_PUBLIC       )); }
        else if ((access & ACC_PRIVATE        ) != 0) { sb.append(access2string.get( ACC_PRIVATE      )); }
        else if ((access & ACC_PROTECTED      ) != 0) { sb.append(access2string.get( ACC_PROTECTED    )); }


        if ((access & ACC_STRICT         ) != 0)
        {
            if (sb.length()!=0) { sb.append(' '); }
            sb.append(access2string.get( ACC_STRICT       ));
        }
*/
            // these modifiers are not set for synthesized methods or are not part of a Java source-code signature
        // if ((access & ACC_SYNCHRONIZED   ) != 0) { sb.append(access2string.get( ACC_SYNCHRONIZED )); }
        // if ((access & ACC_FINAL          ) != 0) { sb.append(access2string.get( ACC_FINAL        )); }
        // if ((access & ACC_NATIVE         ) != 0) { sb.append(access2string.get( ACC_NATIVE       )); }
        // if ((access & ACC_ABSTRACT       ) != 0) { sb.append(access2string.get( ACC_ABSTRACT     )); }
        // if ((access & ACC_SYNTHETIC      ) != 0) { sb.append(access2string.get( ACC_SYNTHETIC    )); }

            // add return value or void
        sb.append(getPlainValueSignature(td.srcReturnType));

            // add method name
        sb.append(' ');
        sb.append(td.srcMethodName);

            // add arguments, if any
        sb.append('(');
        // if dealing with a constructor, then skip first argument (the injected RexxProxy)
        for (int i=0+(td.isMethodConstructor?1:0); i<td.workArgumentTypes.length; i++)
        {
            if (i>0) { sb.append(", ");} // add delimiter and space for ledgibility
            sb.append(getPlainValueSignature(td.workArgumentTypes[i])); // create and append type info
            sb.append(" arg_");
            sb.append(i+1);             // add argument number (1-based)
        }
        sb.append(')');

            // add exceptions, if any
        if ( (!td.isMethodConstructor && (td.newMethodsExceptions.length>0)) ||
             ( td.isMethodConstructor && (td.newMethodsExceptions.length>1))
           )
        {
            sb.append(" throws ");      // add keyword
            for (int i=0; i<(td.newMethodsExceptions.length+(td.isMethodConstructor?1:0)); i++)
            {
                if (i>0) { sb.append(", ");} // add delimiter and space for ledgibility
                sb.append(td.newMethodsExceptions[i]);  // add exception's fully qualified name
            }
        }

        return sb.toString();
    }


    /** Turn a desc type into the plain Java style; arrays like "[Z" or "[[java.lang.String"
     *  into "boolean []" resp. "java.lang.String [][][]".
    *
    */
    static String getPlainValueSignature(Type t)
    {
        StringBuffer sb=new StringBuffer();
        if (t.getSort()==Type.ARRAY)    // an array type
        {
            int    dimensions=t.getDimensions();
            String descriptor=t.getDescriptor();
            sb.append(descriptor.substring(dimensions).replace('/','.'));   // component type
            for (int i=0; i<dimensions; i++)    // append square bracket pair, one for each dimension
            {
                sb.append(" []");
            }
        }
        else
        {
            sb.append(t.getClassName());
        }

        return sb.toString();
    }



/*
    // created under the wrong impression that Type.getArgumentsAndReturnSizes() had a bug
    // in asm 3.2 (2009-07-25)

    public static int getArgumentsAndReturnSizes(final String desc)
    {
        int size=0;
        // ignore first "(", then split at closing ")"
        String []argAndRet=desc.substring(1).split("\\)");    // [0]=arg, [1]=ret
// System.err.println("desc=["+desc+"]: argAndRet[0]=<"+argAndRet[0]+">, argAndRet[1]=<"+argAndRet[1]+">");

        int pos=0;
        while (argAndRet[0].length()>pos)     // loop as long as characters available
        {
            size++;     // size increases at least by one byte
            switch (argAndRet[0].charAt(pos))
            {
                case 'L':   // an internal type ('L'...';')
                    pos=argAndRet[0].indexOf(';', pos)+1; // find next ';', start search from pos
                    break;

                case 'J':   // long
                case 'D':   // double
                    size++; // size of two bytes, adjust for it

                default:
                    // case 'Z':   // boolean
                    // case 'B':   // byte
                    // case 'C':   // char
                    // case 'S':   // short
                    // case 'I':   // int
                    // case 'F':   // float
                    pos++;  // increase position
                    break;
            }
        }

        size++;     // add 1 for 'this', irrespectable whether instance method (has 'this') or static method (no 'this') not
        size<<=2;   // shift two bits left

        switch (argAndRet[1].charAt(0))      // determine size of return value
        {
            case 'J':       // return value of size 2
            case 'D':
                size+=2;
                break;

            case 'V':       // no return value at all
                break;

            default:        // return value of size 1
                size++;
        }

        return size;
    }
*/

    static String pp(Object o)
    {
        return "[->"+o+"<-]";
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

    ArrayList listOfMethodsForwardToSuper=new ArrayList();  // rgf, 20090905: collects masked methods for which we create accessor methods ("_forwardToSuper")

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
    int    newClassVersion=0;           // if 0, use whatever class version the source class has, otherwise use this version


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
    boolean  isForward2SuperStaticNeeded=false;   // flags need for super forwarding specifically for static methods (if masking a method in super has occurred)

    boolean  hasConstructorDefined=false;   // indicates whether a constructor got created
    boolean  bProcessAllMethods=false;  // indicates whether proxy methods should be created for all methods
    boolean  bCallbackFromConstructor=false;    // determines whether RexxProxy should be called from constructor too

    /* desc related information */
    Type   []workArgumentTypes=null;    // contains injected RexxProxy (as 1st arg)
    Type   []srcArgumentTypes=null;     // represents the original argument types
    Type     srcReturnType=null;        // return type


        // the following may not be used, if letting asm figure out the sizesb
    int      workMaxStackSize=0;        // maximum stack size needed for this method
    int      workMaxLocalSize=0;        // maximum local var size needed for this method

}


/** Class which allows for maintaining method relevant data for creating a "_forwardToSuper" method.
*/
class MethodForSuperData implements Opcodes
{
    public int     access         =0;
    public String  name           =null;
    public String  originalName   =null;
    public String  desc           =null;
    public String  signature      =null;
    public String  []exceptions   =null;
    public boolean isStatic       =false;
    public String  sourceClassName=null;

    MethodForSuperData(
        final int      access,          // access flags
        final String   name,            // method's name
        final String   desc,            // method description (method signature)
        final String   signature,       // type signature
        final String[] exceptions,      // list of exceptions, if any
        final boolean  isStatic,        // flag to indicate this is a static method or not
        final String   sourceClassName  // internal source/base class name
    )
    {

       this.access      =access | ACC_SYNTHETIC;    // add SYNTHETIC flag, leave all others in place
       this.originalName=name;          // original name
       this.name        =name+RexxExtendClass.FORWARD_TO_SUPER_APPEND_STRING;   // new name
       this.desc        =desc;
       this.signature   =signature;
       this.exceptions  =exceptions;
       this.isStatic    =isStatic;      // a static method?
       this.sourceClassName=sourceClassName;
    }

    public void createMethod(ClassVisitor cv)
    {

        MethodVisitor mv=null;

        // add getter method: get and return "target" RexxProxy
        mv = cv.visitMethod(this.access, this.name, this.desc, this.signature, this.exceptions);
        if (mv!=null)
        {
            mv.visitCode();

if (RexxExtendClass.bDebugMethodVisitor) System.err.println("\t===> "+"MethodForSuperData.createMethod(): name="+RexxExtendClass.pp(name)+", desc="+RexxExtendClass.pp(desc));

            int rawSize=Type.getArgumentsAndReturnSizes(this.desc);  // includes implicit "this"
            int argSize=(rawSize>>2)- 1;    // get total size of arguments; subtract "1" for implicit "this" variable!!
            int retSize=rawSize & 0x03;     // get size of return value

            int nextArgSlot=0;              // set to slot offset 0 (fine for static methods)
            if (!this.isStatic)             // an instance method?
            {
                 mv.visitVarInsn(ALOAD, 0); // stack "this"
                 nextArgSlot=1;             // first argument at slot offset 1
            }

            // push all arguments ----------------------------------------------------------
            Type args[]=Type.getArgumentTypes(this.desc);
            for (int i=0; i<args.length; i++)   // iterate over arguments
            {
                int size=args[i].getSize();     // get size of this argument (1 or 2)
                mv.visitVarInsn(args[i].getOpcode(ILOAD), nextArgSlot); // loads (stacks) argument
                nextArgSlot+=size;              // calc next slot
            }


            // invoke method in super (= this.sourceClassName) -----------------------------
            mv.visitMethodInsn((this.isStatic ? INVOKESTATIC : INVOKESPECIAL),
                                this.sourceClassName,   // internal class name of source ("base") class
                                this.originalName,      // method name
                                this.desc);             // method description


            // process return value --------------------------------------------------------
            mv.visitInsn(Type.getReturnType(this.desc).getOpcode(IRETURN) );

            // int maxStackSize: check to see if J or D (=2), add 3-times the size if "new" plus 1
            // int maxLocalSize=varArgArraySlot+1;    // max size of local variables, includes args

            // max stack and localVar size
            int maxStack= (nextArgSlot>retSize ? nextArgSlot : retSize);
            int maxLocal= argSize+(this.isStatic ? 0 : 1);    // count "this" back in, if instance method
            // int maxLocal= argSize+1;    // count "this" back in, if instance method
            mv.visitMaxs(maxStack, maxLocal);   // (maxStackSize, maxLocalVarSize)

            mv.visitEnd();
        }
    }
}


