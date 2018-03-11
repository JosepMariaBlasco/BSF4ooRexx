package org.rexxla.bsf.engines.rexx;

import org.apache.bsf.BSFException;
import org.apache.bsf.*;
import org.apache.bsf.util.*;

import java.util.concurrent.ConcurrentHashMap;
// import java.util.Arrays;
import java.util.*;

    // allow easy references to the Enum values
import org.rexxla.bsf.engines.rexx.*;
import static org.rexxla.bsf.engines.rexx.InvocationType.*;
import static org.rexxla.bsf.engines.rexx.RexxReflectUtil.ReflectionType.*;

import        java.lang.reflect.Modifier;
// import static java.lang.reflect.Modifier.*;



/**
 * This is a utility class for {@link RexxReflectInterface} classes and {@link RexxAndJava}. It
 * determines the reflection type and allows for communicating the state of the reflection among
 * different classes.
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2018 Rony G. Flatscher
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
 * @version 1.0, 2018-01-08-2018-02-18, 2018-03-10
 * @since BSF4ooRexx 600 (implementation started: 2018-01-05)
 * @author Rony G. Flatscher (<a href="http://www.wu-wien.ac.at">WU-Wien/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu-wien.ac.at/english">http://www.wu-wien.ac.at/english</a>)
 */


class RexxReflectUtil
{

    static final boolean bDebug=false;      // enable debug output (quite verbose)

    /** This Enumeration defines the three reflection types. */
    static enum ReflectionType {
            REFLECT_METHOD,
            REFLECT_FIELD,
            REFLECT_CONSTRUCTOR
            ;
    }

    /** Empty {@link java.lang.Object} array field.  */
    final static Object [] emptyObjArr     = new Object [0];    // empty Object array, if no Rexx arguments were supplied

    /** ClassLoader of this class. */
    final static ClassLoader rruClassLoader = RexxReflectUtil.class.getClassLoader();

    /** Rexx arguments, a String array. */
    String      rexxArgs[]        ;     // arguments from Rexx

    /** Rexx arguments, replaced by corresponding Java objects from the BSF registry. */
    Object      funcArgs[]        ;     // pre-processed Rexx args (.NIL, Java objects referred to in registry etc.)

    /** {@link RexxAndJava} object which created this instance. */
    RexxAndJava rajo              ;     // RexxAndJava object/instance to work for/with

    /** {@link RexxAndJava} object which created this instance. */
    InvocationType choice         ;     // desired function (as defined in RexxAndJava.java)

    /** The reflection type to carry out. */
    ReflectionType reflectionType ;     // reflection type to carry out

    /** The Java object that is to be reflected. */
    Object bean           = null  ;     // Java object to reflect (name 'bean' stems from the Apache 'Bean Scripting Framework (BSF)')

    /** The name (index) in the BSF repositroy of the Java object that is to be reflected. */
    String beanName       = null  ;     // if supplied for constructor, use it for registering in BSF registry
    /** The class of the Java object to be reflected. */
    Class  beanClz        = null  ;     // bean's class (either the instance' class, or if a class object, the class object itself)

    /** Indicates whether the Java object to be reflected is a Java class object. */
    boolean bBeanIsClass  = false ;     // is bean a class object? If so, look for static members

    /**  The method or field name to reflect. */
    String memberName             ;     // field or method name, will be null for constructors
    /**  The method or field name used as index for storing to the cache. */
    String keyMemberName          ;     // field or method name version that should be used for caching (strict: as is, uppercase otherwise)

    /** Invocation has strict semantics, i.e. names and arguments must match exactly. */
    boolean bStrict       = false ;     // a "_STRICT_" invocation?
    /** Determines whether only a static reflection is to be carried out. */
    boolean bStatic       = false ;     // look for STATIC (field, method) only?
    /** In case of a reflective field access, determines whether the value of a field should be retrieved and returned. */
    boolean bFieldGet     = false ;     // a get field invocation?
    /** Determines whether a resulting Java string value should be returned as a reference to the Java object. If false the Java string
     *  will be turned into a Rexx string.
    */
    boolean bReturnJSO    = false ;     // return a JSO (Java String Object reference instead of the String value)?
    /** Determines whether the arguments should be coerced to supplied types. */
    boolean bTryCoercions = false ;     // use type coercion to Java for the supplied Rexx values depending on the Method,
                                        // Constructor or Field object in hand; cf.
                                        // "static Object [] RexxAndJava.coerceArgs(Object [] funcArgs, Class [] paramTypes)"

    /** Determines whether a stored field cache entry is actually a method cache entry and vice versa, same for method cache entries. */
    boolean bOtherKind    = false ;     // if true: seeking setXXX, getXXX, isXXX, hasXXX fields for the supplied fieldName
                                        // to mimickry ooRexx attributes; if a field found, store its MethodHandle in cachedFields instead
    /** Determines whether reflecting fields and methods is currently done recursively. */
    boolean bRecursing    = false;      // if true: in recursion state for setXXX, getXXX, isXXX, hasXXX fields for the supplied methodName

    /** Indicates whether operation could be carried out successfully. */
    boolean success       = false;      // was requested operation carried out successfully ?
    /** If an operation was carried out successfully, this field holds the result, if any. */
    Object  result        = null;       // contains result if success==true and invocation returns a result

    /** If any of the invocations in the RexxReflectJava{6|7} methods <code>processField</code>,
     *  <code>processMethod</code> or <code>processConstructor</code> causes a Throwable, this field
     *  will store it, such that it can be supplied as the cause of not being able to find an invocable.
    */
    Throwable throwable   = null;


    /** Default constructor. */
    RexxReflectUtil()                   // a default constructor to allow uninitialized instances
    {}

    /** Constructor, pre-processes the Rexx invocation information.
     *
     * @param rajo  the {@link RexxAndJava} object that creates this instance
     * @param choice the {@link InvocationType} choice of the reflective operation to carry out
     * @param rexxArgs the String array of the arguments supplied by Rexx
     *
     */
    RexxReflectUtil(RexxAndJava rajo, InvocationType choice, String rexxArgs[]) throws BSFException
    {
        this.rajo           = rajo;     // the RexxAndJava instance to use
        this.choice         = choice;   // the Rexx invocation type
        this.rexxArgs       = rexxArgs; // the Rexx supplied arguments (strings only)

        beanName=rexxArgs[1];           // String to be used to register new Java object in the BSF registry by RexxAndJava
        if (rexxArgs.length>=3)         // default memberName to rexxArgs[3], if present
        {
            memberName=rexxArgs[2];     // arg[0]=choice, arg[1]=beanOrClassName, arg[2]=memberName
        }

        switch (choice)
        {
                // if creating an instance: arg[1] may be null (so lookup not possible) or the desired name for registering the new object
                //                          arg[2] is the fully qualified name of the Java class to create an instance of
                //                          arg[3] ... optional arguments
            case REGISTER_BEAN:
            case REGISTER_BEAN_STRICT:
                if (rexxArgs[2]==null)      // class/type to create an instance of
                {
                    throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                             "BSF4ooRexx subfunction "+'"'+rexxArgs[0]+'"'+": type/class argument must not be \"null\"");
                }
                else if (rexxArgs[2] instanceof String)      // a fully qualified class name
                {
                        // load class
                    bean=beanClz=loadClass(rajo, rexxArgs[0], rexxArgs[2]);
                }
                else
                {
                    throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                             "BSF4ooRexx subfunction "+'"'+rexxArgs[0]+'"'+": cannot load Java class with the name \""+rexxArgs[2]+"\"");
                }
                bBeanIsClass=true;      // indicate that bean is a class object
                break;


            default:
                // get Java object on which we reflect (assuming arguments got checked in RexxAndJava, such that we do not get out-of-bound indices
                bean=rajo.lookupBean(rajo, rexxArgs[1]);    // get bean (Java object) from BSF repository
                if (bean==null)     // not in registry
                {
                    if (rexxArgs[1]==null || rexxArgs[1].equals(rajo.null4Rexx))
                    {
                        throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                 "BSF4ooRexx subfunction "+'"'+rexxArgs[0]+'"'+": object '"+rexxArgs[1]+"' not registered");
                    }

                    // ad this stage, the second argument could be the name of a Java class, either fully qualified or
                    // the name in the BSF registry (e.g. "int.class")
                    bean=rexxArgs[1];
if (bDebug)
{
    System.err.print("RexxReflectUtil(), 'default' processing: invoke_strict(): bean not found, either a Java class name or maybe assuming a String object operation: ["+truncString(rexxArgs[1],50)+"]");
//     System.err.println(": rexxArgs=\""+Arrays.deepToString(rexxArgs)+"\"");
}
                }
                bBeanIsClass=bean instanceof Class;     // did we receive a class object to work with?
                beanClz   = (bBeanIsClass ? (Class) bean : bean.getClass() );

        }


        switch (choice)     // a static operation requeste?
        {
            case GET_STATIC_FIELD_VALUE_JSO        :
            case GET_STATIC_FIELD_VALUE_STRICT_JSO :
            case GET_STATIC_FIELD_VALUE            :
            case GET_STATIC_FIELD_VALUE_STRICT     :
                bStatic     =true;      // look for static fields only
                if (!bBeanIsClass)      // we need a class/interface object; maybe the String name of a class/interface was supplied
                {
                    if (bean instanceof String) // name of class/interface supplied, try to load it
                    {
                            // load and save class object
                        bean=beanClz=loadClass(rajo, rexxArgs[0], (String) bean);
                        bBeanIsClass=true;
                    }
                    else    // not a String object, nor a class object, use that bean's class instead (long standing BSF4ooRexx behaviour)
                    {
                        beanClz=bean.getClass();
                        bean   =bean;
                        bBeanIsClass=true;
                    }
                }
                break;
        }

        // determine reflection characteristics
        switch (choice)     // determine whether strict invocation from Rexx
        {
            case SET_FIELD_VALUE_STRICT     :
                   // if only the value was supplied, we take the first Field to which the value can be coerced to
                 bTryCoercions=(   rexxArgs.length==4  ||
                                 ( rexxArgs[3]==null || rexxArgs[4]==null )
                 );

                 // coerce Rexx arguments to Java objects according to the supplied type indicator
                 if (bTryCoercions)      // convert value according to given strict type
                 {
                     funcArgs=new Object[1];
                         // fifth argument missing or null ?
                         // look up the BSFRegistry and if found get the Java objects, otherwise the Rexx string
                     if (rexxArgs.length==4 || rexxArgs[4]==null )
                     {
                         funcArgs[0]=rajo.convFromRexx(rexxArgs[3]);     // fourth argument (type) has value
                     }
                     else
                     {
                         funcArgs[0]=rajo.convFromRexx(rexxArgs[4]);     // fifth argument (value), but no type
                     }
                     bStrict=true;          // indicate we have strict semantics, we need to coerce argument values
                     break;
                 }

            case REGISTER_BEAN_STRICT       :
            case GET_FIELD_VALUE_STRICT     :
            case GET_FIELD_VALUE_STRICT_JSO :
            case GET_STATIC_FIELD_VALUE_STRICT :
            case GET_STATIC_FIELD_VALUE_STRICT_JSO :
            case INVOKE_STRICT              :
            case INVOKE_STRICT_JSO          :
                bStrict=true;           // indicate we have strict semantics, we need to coerce argument values

                // convert function-arguments to the indicated type
                funcArgs = (rexxArgs.length==3 ? emptyObjArr : new Object[(rexxArgs.length-3)/2] ) ;

                if (rexxArgs.length>3)  // are arguments supplied ?
                {
                    for (int i=3, idx=0; i<rexxArgs.length; i+=2, idx++)
                    {
                            // convert arguments to Java type (substitute values from BSF registry, if any)
                        funcArgs[idx] = rajo.convFromRexx( rexxArgs[i], rexxArgs[i+1]);
                    }
                }
                break;

            default:
                bTryCoercions=true;     // use coercion to find matching reflected Method, Constructor or Field object

                    // resolve function-arguments if possible
                funcArgs = (rexxArgs.length==3 ? emptyObjArr : new Object [(rexxArgs.length-3)] ) ;
                    // convert arguments to Java type, if possible
                    // look up the BSFRegistry and if found get the Java objects, otherwise the Rexx string
                for (int i=3; i<rexxArgs.length; i++ )
                {
                    funcArgs[i-3] = rajo.convFromRexx(rexxArgs[i]);
                }
        }


        switch (choice)     // determine reflection type, whether a get Field and/or need to return a JSO (Java String Object reference, not the string value)
        {
            // reflect Method objects
            case INVOKE_JSO                 :
            case INVOKE_STRICT_JSO          :
                bReturnJSO=true;        // indicate we need to return a JSO

            case INVOKE                     :
            case INVOKE_STRICT              :
                reflectionType=REFLECT_METHOD;  // reflection type
                bStatic=bBeanIsClass;       // if class object, look for static methods only
                break;

                // reflect Constructor objects (create new instances of supplied class)
            case REGISTER_BEAN              :
            case REGISTER_BEAN_STRICT       :
                reflectionType=REFLECT_CONSTRUCTOR; // reflection type
                    // load class, if a string got supplied
                if (!bBeanIsClass)      // we need a class/interface object; maybe the String name of a class/interface was supplied
                {
                    if (bean instanceof String) // name of class/interface supplied, try to load it
                    {
                        // load and save class object
                        bean=beanClz=loadClass(rajo, rexxArgs[0], (String) bean);
                        bBeanIsClass=true;
                    }
                    else    // not a String object, nor a class object, use that bean's class instead (long standing BSF4ooRexx behaviour)
                    {
                        throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                     "BSF4ooRexx subfunction \"NEW\": supplied argument '"+bean+"' is neither a Java class object, nor the fully qualified name of a Java class that can be loaded");
                    }
                }
                break;

                // reflect Field objects
            case GET_FIELD_VALUE_JSO        :
            case GET_FIELD_VALUE_STRICT_JSO :
            case GET_STATIC_FIELD_VALUE_JSO :
            case GET_STATIC_FIELD_VALUE_STRICT_JSO :
                bReturnJSO=true;        // indicate we need to return a JSO

            case GET_FIELD_VALUE            :
            case GET_FIELD_VALUE_STRICT     :
            case GET_STATIC_FIELD_VALUE     :
            case GET_STATIC_FIELD_VALUE_STRICT :
                bFieldGet=true;         // indicate we need to get a Field value

            case SET_FIELD_VALUE            :
            case SET_FIELD_VALUE_STRICT     :
                reflectionType=REFLECT_FIELD;   // reflection type
                break;
        }

            // which name to use as index for caching
        keyMemberName=( bStrict || (choice==REGISTER_BEAN) ? memberName : memberName.toUpperCase() );

if (bDebug)
if ( bean != null && bean instanceof String && rexxArgs[1] != null && bean.equals(rexxArgs[1]) )
{
    System.err.print("RexxReflectUtil(), 'default string' processing: bean not found, assuming a String object operation: ["+truncString(rexxArgs[1],50)+"]");
    System.err.println(": rexxArgs=\""+Arrays.deepToString(rexxArgs)+"\"");
}

    }

    // apply org.apache.bsf.util.EngineUtils.loadClass() comparable strategy: first Thread context ClassLoader, then this class' ClassLoader

    /** Loads the denoted Java class and returns it. It first uses the thread context class loader and if that fails
     *  uses the class loader that loaded this class.
     *
     * @param rajo the {@link RexxAndJava} object invoking this method
     * @param strBsfSubFunctionName the subfunction name which invokes this method
     * @param the fully qualified Java class name
     *
     * @return the loaded Java class object
     *
     * @throws BSFException if the class cannot be loaded
     */
    static Class loadClass(RexxAndJava rajo, String strBsfSubFunctionName, String className) throws BSFException
    {
        Class <?> tmpClass=(Class) rajo.lookupBean(rajo, className);     // already in the BSF registry?
        if (tmpClass!=null)     // found, return it!
        {
            return tmpClass;
        }

        try
        {
            ClassLoader cl=Thread.currentThread().getContextClassLoader();  // try the Thread's context loader first
            if (cl!=null)   // if set, use it
            {
                try
                {
                        tmpClass=cl.loadClass(className);
                }
                catch (ClassNotFoundException e) {}
            }

            if (cl==null || tmpClass==null)     // not found yet
            {
                try     // now try the class loader that loaded this class
                {
                    tmpClass= rruClassLoader.loadClass(className);
                }
                catch (ClassNotFoundException cnfe) {
                    throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                          "BSF4ooRexx subfunction \""+strBsfSubFunctionName+"\": while attempting to load class '"+className+"', threw exception: ["+cnfe+"]", cnfe);
                }
            }
        }
        catch (Throwable t)
        {
            throw new BSFException (BSFException.REASON_OTHER_ERROR,
                      "BSF4ooRexx subfunction \""+strBsfSubFunctionName+"\": while attempting to load class '"+className+"', threw: ["+t+"]", t);
        }

            // register Java class object in BSF registry
        String beanName=rajo.makeBeanName(rajo, tmpClass);
        rajo.registerBean(rajo, beanName, tmpClass);   // will use <O> indicator
        return tmpClass;
    }

    // returns a copy of itself
    /** Creates a copy of this instance and returns it.
     *
     * @return a copy of this instance
    */
    RexxReflectUtil copy()
    {
        RexxReflectUtil newRru=new RexxReflectUtil();

        newRru.rexxArgs      = this.rexxArgs       ;    // arguments from Rexx
        newRru.funcArgs      = this.funcArgs       ;    // pre-processed Rexx args (.NIL, Java objects referred to in registry etc.)

        newRru.rajo          = this.rajo           ;    // RexxAndJava object/instance to work for/with
        newRru.choice        = this.choice         ;    // desired function (as defined in RexxAndJava.java)
        newRru.reflectionType= this.reflectionType ;    // reflection type to carry out

        newRru.bean          = this.bean           ;    // Java object to reflect (name 'bean' stems from the Apache 'Bean Scripting Framework (BSF)')
        newRru.beanName      = this.beanName       ;    // the beanName (index into BSF registry)
        newRru.beanClz       = this.beanClz        ;    // bean's class (either the instance' class, or if a class object, the class object itself)
        newRru.bBeanIsClass  = this.bBeanIsClass   ;    // is bean a class object? If so, look for static members

        newRru.memberName    = this.memberName     ;    // field or method name, will be null for constructors
        newRru.keyMemberName = this.keyMemberName  ;    // field or method name version that should be used for caching (strict: as is, uppercase otherwise)

        newRru.bStrict       = this.bStrict        ;    // a "_STRICT_" invocation?
        newRru.bStatic       = this.bStatic        ;    // look for STATIC (field, method) only?
        newRru.bFieldGet     = this.bFieldGet      ;    // a get field invocation?
        newRru.bReturnJSO    = this.bReturnJSO     ;    // return a JSO (Java String Object reference instead of the String value)?
        newRru.bTryCoercions = this. bTryCoercions ;    // use type coercion to Java for the supplied Rexx values depending on the Method,
                                                        // Constructor or Field object in hand; cf.
                                                        // "static Object [] RexxAndJava.coerceArgs(Object [] funcArgs, Class [] paramTypes)"
        newRru.bOtherKind    = this.bOtherKind ;        // if true: seeking setXXX, getXXX, isXXX, hasXXX fields for the supplied fieldName
                                                        // to mimickry ooRexx attributes; if a field found, store its MethodHandle in cachedFields instead

        newRru.bRecursing    = this.bRecursing     ;    // if true: in recursion state for setXXX, getXXX, isXXX, hasXXX fields for the supplied methodName

        newRru.success       = this.success        ;    // was appropriate member found?
        newRru.result        = this. result        ;    // contains result if success==true and invocation returns a result

        newRru.throwable     = this.throwable      ;    // the Throwable that might have been created when invoking in the processField, processMethod or processConstructor

        return newRru;
    }



    // return settings of all fields for debugging purposes
    /** Creates and returns a String for debugging purposes, denoting the current values of the fields.
     *
     * @return the debug string
     *
     */
    String debugString()
    {
        Object tmpRexxArgs[]=Arrays.copyOfRange(rexxArgs, 3, rexxArgs.length);  // get argument part

        return this+", field values:\n--->"
            +"\tthis            =\""+this          +"\"\n"
            +"\trajo            =\""+rajo          +"\"\n"
            +"\tchoice          =\""+choice+"\"\n"
            +"\treflectionType  =\""+reflectionType+"\"\n"
            +"\tbeanName        =\""+beanName      +"\"\n"
            +"\tbean            =\""+bean          +"\" instanceof Class? \""+bBeanIsClass+"\"\n"
            +"\tbeanClz         =\""+beanClz       +"\"\n"
            +"\tbStatic         =\""+bStatic       +"\"\n"
            +"\tbStrict         =\""+bStrict       +"\"\n"
            +"\tmemberName      =\""+memberName    +"\" keyMemberName=\""+keyMemberName    +"\"\n"
            +"\tbFieldGet       =\""+bFieldGet     +"\" bOtherKind=\""+bOtherKind+"\" bRecursing=\""+bRecursing+"\"\n"
            +"\tsuccess         =\""+success       +"\"\n"
            +"\trexxArgs[]      =\""+rexxArgs      +"\", rexxArgs.length   =\""+rexxArgs.length+"\": "+Arrays.toString(rexxArgs)+"\n"
            +"\ttmpRexxArgs[]   =\""+tmpRexxArgs   +"\", tmpRexxArgs.length=\""+tmpRexxArgs.length+"\": "+Arrays.toString(tmpRexxArgs)+"\n"
            +"\tfuncArgs[]      =\""+funcArgs + (funcArgs==null ? (null+"\"") : "\", funcArgs.length   =\""+funcArgs.length+"\": "+Arrays.toString(funcArgs))+"\n"
            +"\tbReturnJSO      =\""+bReturnJSO    +"\"\n"
            +"\tbTryCoercions   =\""+bTryCoercions +"\"\n"
            +"\tthrowable       =\""+(throwable==null ? null : throwable.toString() +"\"\n"
            +"<---\n"
            );
    }


    /* If the message to process is "MAKERRAY" or "SUPPLIER" and
//          - MAKEARRAY || SUPPLIER: bean has one of the interfaces java.util.Enumeration, java.lang.Iterable,
//                                   java.util.Iterator, java.util.Collection, java.util.Map
//               create and return Rexx array or supplier object; cf. RexxAndJava, line # 4204
    */

    /** Utility method that carries out the ooRexx <code>MAKEARRAY</code> and <code>SUPPLEIR</code> semantics
     *  on <code>Enum</code> classes and on Java objects with any of the interfaces
     *  {@link java.util.Enumeration}, {@link java.lang.Iterable},
     *  {@link java.util.Iterator}, {@link java.util.Collection} or  {@link java.util.Map}.
     *
     *  It creates and poupates a {@link RexxProxy} representing either an ooRexx <code>Array</code> or
     *  <code>Supplier</code> object and stores it in the {@link result} field.
     *
     * @throws BSFException in case an exception is thrown up while interacting with the  {@link RexxProxy}
     */
    void processMakearrayOrSupplier() throws BSFException
    {
if (bDebug) System.err.println("... ... processMakearrayOrSupplier(): arrived ....");
        boolean bMakeArray=this.keyMemberName.equals("MAKEARRAY");           // MAKEARRAY or SUPPLIER ?

        BSFEngine rexxEngine=this.rajo.getRexxEngine();      // get Rexx engine from RexxAndJava instance
        Object tmpResult=null;
        String rexxCreateArray   ="return .array~new                          \n" ;

        String rexxCreateSupplier="use arg itemArray, indexArray              \n" +
                                  "return .supplier~new(itemArray,indexArray) \n" +
                                  "::requires BSF.CLS                           " ;


        if (this.bBeanIsClass)   // if a java.lang.Enum class in hand, iterate over all enum values
        {
            // Class beanClz=(Class) bean;
            if (this.beanClz.isEnum())     // iterate over all values()
            {
                Enum values[]=(Enum[])this.beanClz.getEnumConstants(); // get all Enum values, they come as an array already
                if (bMakeArray)   // MAKEARRAY message
                {
                      // create an ooRexx array object, return it
                    RexxProxy rpArr1=(RexxProxy) rexxEngine.apply("Invoked_by_RexxReflectUtil_Enum_01.rex", 0, 0, rexxCreateArray, null, null);
                      // populate Rexx array
                    for (int i=0; i<values.length;i++)
                    {
                        rpArr1.sendMessage1("APPEND",values[i]);
                    }
                    tmpResult=rpArr1;
                }
                else  // SUPPLIER message: create index array using the Enum ordinal values
                {     // do as if a Map ordinal->value was in hand, i.e. use the ordinal values as index values for the Rexx supplier
                    RexxProxy rpItemArray =(RexxProxy) rexxEngine.apply("Invoked_by_RexxReflectUtil_Enum_02.rex", 0, 0, rexxCreateArray, null, null);
                    RexxProxy rpIndexArray=(RexxProxy) rexxEngine.apply("Invoked_by_RexxReflectUtil_Enum_03.rex", 0, 0, rexxCreateArray, null, null);

                    for (int i=0; i<values.length; i++)
                    {
                        rpItemArray .sendMessage1("APPEND",values[i]);
                        rpIndexArray.sendMessage1("APPEND",values[i].ordinal());
                    }
                    Vector<RexxProxy> vArgs=new Vector<RexxProxy>();
                    vArgs.add(rpItemArray);
                    vArgs.add(rpIndexArray);
                    tmpResult=rexxEngine.apply("Invoked_by_RexxReflectUtil_Enum_04.rex", 0, 0, rexxCreateSupplier, null, vArgs);
                }
            }
        }
        else  // ? instance of java.lang.Iterable, java.util.Enumeration, java.util.Collection, java.util.Iterator, java.util.Map ?
        {
            boolean isMap=Map.class.isInstance(this.bean);

            if ( isMap                              ||
                 Iterable.class   .isInstance(this.bean) ||
                 Iterator.class   .isInstance(this.bean) ||
                 Collection.class .isInstance(this.bean) ||
                 Enumeration.class.isInstance(this.bean)
               )
            {
                Iterator it=null;
                  // try to create iterators
                if (isMap)    // get the keyset and create an iterator for it
                {
                    it= ((Map) this.bean).keySet().iterator();

if (bDebug) System.err.println("in ... -- /// isMap, setting \"it\"...");
                }

                else if (Iterable.class.isInstance(this.bean))
                {
                    it= ((Iterable) this.bean).iterator();
if (bDebug) System.err.println("in ... -- /// isIterable, setting \"it\"...");
                }

                else if (Iterator.class.isInstance(this.bean) )
                {
                    it= (Iterator) this.bean;
if (bDebug) System.err.println("in ... -- /// isIterator, setting \"it\"...");
                }
                else if (Collection.class.isInstance(this.bean) )
                {
                    it= ((Collection) this.bean).iterator();
if (bDebug) System.err.println("in ... -- /// isCollection, setting \"it\"...");
                }


                  // create an ooRexx array object, return it
                RexxProxy rpArr1=(RexxProxy) rexxEngine.apply("Invoked_by_RexxReflectUtil_01.rex", 0, 0, rexxCreateArray, null, null);

                  // create the Rexx array with the iterator, usable for MAKEARRAY and SUPPLIER
                  // do not use the Map iterator as for SUPPLIER it needs to fetch the value
                if ( (bMakeArray && it!=null) || (it!=null && !isMap) )
                {
                      // populate Rexx array
                    while (it.hasNext())
                    {
                        rpArr1.sendMessage1("APPEND",it.next());
                    }
                }
                  // Java 9 will have an "asIterator()", but not available in 1.6/6.0
                else if (Enumeration.class.isInstance(this.bean) )
                {
if (bDebug) System.err.println("in ... --> isEnumeration, MAKEARRAY \"it\"...");
                    Enumeration en=(Enumeration) this.bean;
                    while (en.hasMoreElements())
                    {
                        rpArr1.sendMessage1("APPEND",en.nextElement());
                    }
                }

                if (bMakeArray)   // MAKEARRAY message
                {
                    tmpResult=rpArr1;
                }
                else      // SUPPLIER message: all but Map get a Rexx like integer array
                {
if (bDebug) System.err.println("in ... # i: SUPPLIER branch...");
                    if (isMap)    // it has the keys already
                    {
                        Map mBean=(Map) this.bean;
                          // create an ooRexx array object, return it
                        RexxProxy rpArr2=(RexxProxy) rexxEngine.apply("Invoked_by_RexxReflectUtil_02.rex", 0, 0, rexxCreateArray, null, null);
                          // populate Rexx array
                        while (it.hasNext())
                        {
                            Object key=it.next();
                            rpArr1.sendMessage1("APPEND", key);
                            rpArr2.sendMessage1("APPEND", mBean.get(key));
                        }
                        Vector<RexxProxy> vArgs=new Vector<RexxProxy>();
                        vArgs.add(rpArr2);    // value/item array
                        vArgs.add(rpArr1);    // key/index array
                        tmpResult=rexxEngine.apply("Invoked_by_RexxReflectUtil_03.rex", 0, 0, rexxCreateSupplier, null, vArgs);
                    }
                    else
                    {
                          // return a supplier as if from a bag, i.e. item and index array are the same
                        Vector<RexxProxy> vArgs=new Vector<RexxProxy>();
                        vArgs.add(rpArr1);
                        vArgs.add(rpArr1);
                        tmpResult=rexxEngine.apply("Invoked_by_RexxReflectUtil_04.rex", 0, 0, rexxCreateSupplier, null, vArgs);
                    }
                }
            }
        }

        if (tmpResult!=null)
        {
            this.result=tmpResult;
            this.success=true;        // indicate we were successful
        }
    }


    /** If reflection was not able to find an appropriate Field, Method or Constructor create an informative
     *  (Rexx-like &quot;human centric&quot;) message that should help the Rexx programmer to identify the
     *  problem with the supplied name or arguments. In the case that Java exceptions were thrown while seeking
     *  for the desired member, the last thrown exception will be supplied to the BSFException to allow the
     *  programmer to get additional hints, if found members could not be used.
     *
     *  @throws a BSFException
     */
    String throwNotFoundBSFException() throws BSFException
    {
            // one error could be: using a value which is a key into the BSF registry, hence give
        StringBuffer dump=new StringBuffer();       // programmer as much information as possible to find out about this


        // if arguments then format them for the error message, depending on the calling style
        int i=0;
        int distance=1;     // next argument in rexxArgs is "distance" away; in case of "typeIndicator,value..." the distance is 2
          // we may have typed arguments in the form "typeIndicator,value..." so beanName is
        if (bStrict && (rexxArgs.length-3!=funcArgs.length) ) distance=2;

        for(i=0; i<funcArgs.length; i++) //  i=i+distance)
        {

            dump.append("\t\targ # "+(i+1)+": ");

            if (distance==1)        // normal argument, no prepended type informaion
            {
                // dump.append("Rexx argument=["+truncString(this.rexxArgs[3+i],45)+"]");
                dump.append("Rexx argument=["+this.rexxArgs[3+i]+"]");
            }
            else
            {
                int idx=3+(i*distance); // position in rexxArgs; note: strict setField's may leave out type info, account for that

                dump.append( "Rexx strict argument=["+ (idx<rexxArgs.length ? "type" : "Rexx value" )
                            + "=\""+this.rexxArgs[idx]);
                // + "=\""+truncString(this.rexxArgs[idx],45));

                if (idx<rexxArgs.length)    // both given, type and Rexx value
                {
                    dump.append("\", Rexx value=\""+truncString(this.rexxArgs[3+(i*distance)+1],65)+"\"]");
                }
                else
                {
                    dump.append("]");
                }
            }

            dump.append(" --> Java value=\""+ (truncString(""+this.funcArgs[i], 65)) +"\""
                                 +" type=<"+(funcArgs[i]==null ? null : funcArgs[i].getClass().getName())+">\n"
                        );
        }

            // now create the message
        StringBuffer msg=new StringBuffer();
        msg.append(  "BSF4ooRexx subfunction "+'"'+this.rexxArgs[0]+'"'+": "
                             + "\n\tbean:        ["+this.bean+"]"+ " --> type: <"+(this.beanClz==null ? null : this.beanClz.getName())+">"
                             + "\n\t"
                             + (  this.reflectionType==REFLECT_METHOD ? "method:      [" :
                                      (this.reflectionType==REFLECT_FIELD  ? "field:       ["  : "constructor")
                               )
                   );

        if (this.reflectionType!=REFLECT_CONSTRUCTOR)
        {
            msg.append(this.rexxArgs[2]+"]");
        }

        if (reflectionType==REFLECT_METHOD)
        {
            msg.append(" not found or execution error!");
        }
        else
        {
            msg.append(" not found!");
        }


        if (this.bStrict)
        {
            msg.append(" (Strict mode invocation");
            if (this.reflectionType!=REFLECT_CONSTRUCTOR)
            {
                msg.append(": check the *exact* case of the "+(reflectionType==REFLECT_FIELD ? "field" : "method") +" name.)");
            }
        }

        msg.append('\n');

        if (this.reflectionType==REFLECT_CONSTRUCTOR)   // this Rexx invocation will yield bean to be the class to instantiate
        {
            msg.append( "\n\t-> check name=["+this.rexxArgs[2]+"]");
            if (bean instanceof Class)  // if a class object, show its class name
            {
               msg.append(" -> Java class name=<"+beanClz.getName()+">");
            }
        }
        else
        {
            msg.append( "\n\t-> check "+(reflectionType==REFLECT_FIELD ? "field" : "method") +" name=["+this.rexxArgs[2]+"]" );
        }

        msg.append(" ("+(bStrict?"":"caseless o.k., but ")+"correct spelling?)\n");

        if (dump.length()>0)    // if arguments supplied, show them
        {
            msg.append("\t-> check supplied arguments (correct number, correct types?):\n");
            msg.append(dump.toString());
        }

            // now throw the BSFException
        if (throwable!=null)
        {
            throw new BSFException(BSFException.REASON_INVALID_ARGUMENT, msg.toString(), throwable);
        }
        else
        {
            throw new BSFException(BSFException.REASON_INVALID_ARGUMENT, msg.toString());
        }
    }


    /** Truncates string, if longer than given maxLength and append ellipsis (three dots: '...'). */
    String truncString (String s, int maxLength)
    {
        if (s==null)
        {
            return null;
        }

        if (s.length()>maxLength)
        {
            return s.substring(0, maxLength-1)+"...";
        }
        return s;
    }


}

