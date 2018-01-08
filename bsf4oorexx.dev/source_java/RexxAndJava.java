package org.rexxla.bsf.engines.rexx;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ConcurrentHashMap;
import java.util.StringTokenizer;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import org.apache.bsf.*;
import org.apache.bsf.util.*;
import org.apache.bsf.util.type.*;

import static org.rexxla.bsf.engines.rexx.InvocationType.*;

/**
  * This class manages the calls from Rexx or Object Rexx into Java.
  * (Any scripting language being able to pass Strings to and
  * from Java will be able to use this interface, making it <em>really</em> easy to interface
  * with Java.)
  *
  *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2001-2018 Rony G. Flatscher
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
  * @version 6.0.0, 2018-01-04
  * @author Rony G. Flatscher (<a href="http://www.wu-wien.ac.at">WU-Wien/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu-wien.ac.at/english">http://www.wu-wien.ac.at/english</a>)
  */



// ---rgf, 2016-11-27: leave it for backward compatibility as there are users of BSF4Rexx code for years to come
//
//                           - remove outdated EVENT_*, SyncPutGet (old eventListener support via BSF), can be done directly (easier, real-time)
//                           - check: is checking for object prefix in *registerBean() still necessary? YES: to allow mixing with pure BSF4Rexx code


/*
TODO: (maybe)
    - 20141004/5, rgf:
                     - if reverting change 2014-05-15, then starting with Java 1.8/8 analyze explicitly
                       all interfaces a class implements and look for default methods, which have as
                       their modifiers: public+non-abstract+non-static; rgf, 2016-12-08: so far there has been
                       no need to do it the old way (performance comparable)

    - 2018-01-08, rgf: - rework the static int constants into an Enum class with a String constructor method
*/

 /*

     2018-01-08, ---rgf, - changed switch to use new InvocationType enum

     2018-01-07, ---rgf - make sure access to rajRegistry is synchronized where values might get changed
                        - changed static private int constants (indicating the desired function from Rexx)
                          to package private to allow RexxReflect* classes access them

     2018-01-06, ---rgf: remove deprecation warning of the Java 9 compiler, if there is a Java 6 compliant,
                         non-deprecated alternative; note: the deprecation warning on "finalize" will not
                         be honored as this is intentional as it is the only means to allow garbage collection
                         on the Rexx side as well, when Java Rexx peers get out of scope

     2018-01-05, ---rgf: turn utility methods into static:


                        RexxAndJava.java

                        -    static Object [] coerceArgs(Object [] funcArgs, Class [] paramTypes)
                        -    static protected String makeString4Rexx(Object o)  // changed 2003-01-06 from "makeResultString()" to "makeString4Rexx()"
                        -    static public String makeBeanName(Object o)
                        -    static protected String registerBean(String beanName, Object object)
                        -    static protected Object lookupBean(String beanName)
                        -    static int spilloverCounter=0;

                        - added new instance method for native code compatibility:

                             private String makeString4Rexx(Object o)

                        Need to supply RexxAndJava object/instance (named "rajo") to now static methods:

                            Need to supply "bTerminated" to now static method:
                            -    static protected String registerBean(String beanName, Object object)
                            Need to supply or change "this" to RexxAndJava.class to now static method:
                            -    static protected Object lookupBean(String beanName)
                            -    static protected String registerBean(String beanName, Object object)
                            -    static public String makeBeanName(RexxAndJava rajo, Object o)


     2018-01-04, ---rgf, - ARRAY_PUT with Boolean/boolean component type will now behave like SET PROPERTY_VALUE
                           and SET_FIELD_VALUE:
                           if Rexx string is "1" (.true) then Boolean.TRUE will be used, otherwise the
                           type convertor will create the Boolean value (which means that a Rexx string
                           value named "true" will yield Boolean.TRUE as well, otherwise BOOLEAN.FALSE,
                           which follows from using java.lang.Boolean.valueOf(String))

     2018-01-01, ---rgf, - fixed a bug in ARRAY_PUT: only use the type convertor, if convFromRexx() returns
                           a string value and the component type is primitive, otherwise use the Object
                           value convFromRexx() returned

     2017-04-04, ---rgf, - removed debug-output in registerBean4JNI()

     2016-12-20, ---rgf, - remove rii_id in unregisterBean, not needed anymore

     2016-12-19, ---rgf, - refactoring the new registry code such that we can save calls from the instance
                           *Registry*() to the static *Registry*()-methods

     2016-12-08, ---rgf, - tidying up code, removing all code relating to the Java registry changes/additions
                           of 2015-08-03, new central Java registry replaces it

     2016-11-28, ---rgf, - convFromRexx(): check that type string is not the empty string, change error message
                           to be more meaningful
                         - fixing a subtle bug where even invokestrict() may be fooled in method chosing: e.g.
                           java.lang.List possesses two remove methods with the signature "Object remove(int)"
                           and "boolean remove(Object)". The current algorithm in INVOKE_STRICT can be misled
                           to choose the wrong method if checking the "remove(Object)" first (because an Integer
                           object, to which the primitive int value was converted to already, could be used as
                           an argument). Now checking if argument must be primitive, so not to accept a parameter
                           of type Object.

     2016-11-27, ---rgf, - new central RAJRegistry with RAJBeans, global to all Rexx interpreter instances
                         - turning bsfPrefixReturnValue into static to become available in static methods
                         - make glob_tcr static

     2016-11-19, ---rgf, - updated major version number to 6 to indicate new base level Java 1.6/6

     2016-11-12, ---rgf, change from .isAssignableFrom() to .isInstance() when checking whether MAKEARRAY/SUPPLIER
                    could be honored

     2016-11-06, ---rgf, if non-existing MAKEARRAY or SUPPLIER methods should be invoked, then check, whether
                    we have a Java collection in hand that has one of the following interfaces implemented:
                    java.lang.Iterable, java.util.Collection, java.util.Enumeration, java.util.Iterator or
                    java.util.Map. If so, then create and return a genuine Rexx object of type .Array or
                    .Supplier wrapped as a RexxProxy which can be unwrapped on the ooRexx side using BsfRexxProxy(rp,"o")
                    as will be done in BSF.CLS' class BSF.

                    In addition treat a Java class object of type java.lang.Enum as a collection of enum values
                    and return them for MAKEARRAY; in the case of SUPPLIER use the ordinal enum values for the index
                    array and the enum values for the item array

     2016-11-04, ---rgf, - basing on 1.6/6.0, get rid of generic warnings
                    source_java>javac -Xlint:unchecked RexxAndJava.java
                    RexxAndJava.java:543: warning: [unchecked] unchecked call to put(K,V) as a member of the raw type java.util.HashMap
                            pendingUninits.put(new_rii_ID, new ArrayList(64));
                                              ^
                    RexxAndJava.java:711: warning: [unchecked] unchecked call to put(K,V) as a member of the raw type java.util.HashMap
                                    pendingUninits.put(rii_ID, new ArrayList(64));
                                                      ^
                    RexxAndJava.java:4070: warning: [unchecked] unchecked call to isAssignableFrom(java.lang.Class<?>) as a member of the raw type java.lang.Class
                                                                  bFound=(paramTypes[j].isAssignableFrom(funcArgs[j].getClass()));
                                                                                                        ^
                    RexxAndJava.java:5179: warning: [unchecked] unchecked call to add(E) as a member of the raw type java.util.ArrayList
                                                              al.add(args[1]);
                                                                    ^
                    RexxAndJava.java:5594: warning: [unchecked] unchecked call to put(K,V) as a member of the raw type java.util.Hashtable
                                           orxReferences.put(beanName, newRefCount); // remember refCount
                                                            ^
                    RexxAndJava.java:5911: warning: [unchecked] unchecked call to isAssignableFrom(java.lang.Class<?>) as a member of the raw type java.lang.Class
                                    if (!(paramTypes[k].isAssignableFrom (funcArgs[k].getClass()))) // not compatible, break
                                                                         ^
                    RexxAndJava.java:5957: warning: [unchecked] unchecked call to getConstructor(java.lang.Class<?>...) as a member of the raw type java.lang.Class
                                            Constructor constr=paramTypes[k].getConstructor( argClass );
                                                                                           ^
                    RexxAndJava.java:6167: warning: [unchecked] unchecked call to put(K,V) as a member of the raw type java.util.Hashtable
                                orxReferences.put(beanName, new Integer(refCount)); // remember refCount
                                                 ^
                    RexxAndJava.java:6173: warning: [unchecked] unchecked call to add(E) as a member of the raw type java.util.HashSet
                                        beansCreated.add(beanName);
                                                        ^
                    RexxAndJava.java:6236: warning: [unchecked] unchecked call to put(K,V) as a member of the raw type java.util.Hashtable
                                       orxReferences.put(beanName, new Integer(refCount));
                                                        ^
                    RexxAndJava.java:6597: warning: [unchecked] unchecked call to addElement(E) as a member of the raw type java.util.Vector
                                               eventTextVectorHigh.addElement(o);   // changed to addElement() for JRE 1.1.8, ---rgf
                                                                             ^
                    RexxAndJava.java:6601: warning: [unchecked] unchecked call to addElement(E) as a member of the raw type java.util.Vector
                                               eventTextVectorLow.addElement(o);    // changed to addElement() for JRE 1.1.8, ---rgf
                                                                            ^
                    RexxAndJava.java:6606: warning: [unchecked] unchecked call to addElement(E) as a member of the raw type java.util.Vector
                                               eventTextVector.addElement(o);       // changed to addElement() for JRE 1.1.8, ---rgf
                                                                         ^
                    RexxAndJava.java:6764: warning: [unchecked] unchecked call to <T>toArray(T[]) as a member of the raw type java.util.ArrayList
                                    entries=(String[]) al.toArray(entries);
                                                                 ^
                    14 warnings



     2016-08-14, ---rgf, - updated kudos-entries
     2015-08-03, ---rgf, - change code to only deregister bean, if running in the context of the same RII
                           in which registering took place!

                           - defining a class field "pendingUninits": a HashMap<String,HashSet> (RII_ID,Vector of beanNames to unregister)

                           - case "UNREGISTER_BEAN" adopted accordingly

                           - terminate: make sure all registered beans get deregistered (BSFManager may
                                        initiate a new Rexx interpreter instance which should be able to
                                        access any other registered Java object)

     2015-08-01, ---rgf, - change error message "method not found or error (exception) executing method!" to
                           unambiguous message: "(exception) executing method!"
                         - add subfunction "get_RII_ID", which returns the "Rexx interpreter instance ID" (a string)

     2015-07-27, ---rgf, - make sure that no (added) debug statements are carried out

     2015-05-09, ---rgf, - added access to important Rexx objects and interpreter related information
                           - protected native Object jniGetLocalEnvironment0
                           - protected native Object jniGetGlobalEnvironment0
                           - protected native Object jniNil0
                           - protected native long   jniInterpreterVersion0
                           - protected native long   jniLanguageLevel0

     2014-10-04, ---rgf, - add subfunctions that return a reference to a java.lang.String object or
                           a NetRexx object, if such a value needs to be returned (rather than turning
                           it into a Rexx string), added subfunctions:

                               - getFieldValueJSO
                               - getFieldValueStrictJSO
                               - getPropertyValueJSO
                               - getStaticStrictValueJSO
                               - getStaticValueJSO
                               - invokeJSO
                               - invokeStrictJSO

     2014-05-15, ---rgf, - correct new method lookup for INVOKE (did not find fields, nor was coercing
                           the arguments, rather used the INVOKE_STRICT logic)

     2014-05-14, ---rgf, - change method lookup for INVOKE to use Class.getMethods() instead of Class.getDeclaredMethods();
                           getMethods() returns all public methods, which in Java 8 also includes default (interface) methods,
                           if necessary

     2014-04-02, ---rgf, - move initialization of JNI library to RexxAndJava constructor to make
                           sure that non-Rexx JNI functions are available to Java (like jniProcEnvironment())

     2014-03-29, ---rgf, - add jniProcEnvironment(...): this allows to get, set and unset the
                           process environment from Java; reason: AOO 4.x removes the PATH environment
                           variable (seems to be unintended, hence an error), which inhibits the
                           Rexx interpreter to initialize successfully; this jni function is intended
                           to allow as a last resort to set the missing PATH environment variable;
                           the subfunctions "getEnv" and "setEnv" will not be explicitly documented

     2013-06-25, ---rgf, - add a terminate() method which nullifies the peer BSFManager and RexxEngine
                           object to allow a terminated BSFManager RexxEngine to be garbage collected

     2013-01-04, ---rgf, - use alternative code to fix bug https://sourceforge.net/tracker/?func=detail&aid=3599351&group_id=400586&atid=1660873

     2013-01-03, ---rgf, - fix bug https://sourceforge.net/tracker/?func=detail&aid=3599351&group_id=400586&atid=1660873

     2012-11-01, ---rgf, - fix bug https://sourceforge.net/tracker/?func=detail&aid=3581957&group_id=400586&atid=1660873

     2012-04-17, ---rgf, - fixed a bug in createRexxInterpreterInstance() where the new RII was
                           not assigned to the RexxEngine if that had no RII assigned to it yet
                         - added protected void set_rii_ID(String rii_ID) to allow Java4Rexx to
                           set the reference to the Rexx interpreter instance that invoked it
                         - tidying up the code a bit (removing comments and TODOs that are not actual anymore)

     2012-03-12, ---rgf, - now loading BSF4ooRexx DLL/so after trying to fetch the netrexx.lang.Rexx class,
                           such that native library can access static field "nlR_class" and fetch the class object,
                           if Java found it
                         - change scope of "bsfPrefixReturnValue" from static to instance, such that
                           the Rexx interpreter instances can run with different settings

     2012-02-25, ---rgf, - added methods jniNil(), jniInterpreterVersion(), jniLanguageLevel()
     2012-02-24, ---rgf, - added methods jniGetLocalEnvironment(), jniGetGlobalEnvironment()

     2012-02-19, ---rgf, - added methods jniGetCallerContext(), jniGetAllContextVariables

     2012-02-11, ---rgf, - added methods:
                           jniCheckCondition(), jniClearCondition(), jniGetConditionInfo(),
                           jniSetThread(), jniSetThreadTrace(), jniHaltThread()

     2012-02-08, ---rgf, - added methods:
                           jniRaiseCondition(), jniRaiseException(), jniRaiseException0(),
                           jniRaiseException1(), jniRaiseException2(), jniGetContextVariable(),
                           jniSetContextVariable()

     2012-02-06, ---rgf, - changed visibility of all jni*()-functions from public to protected
                         - changed jniRegisterBSF() to jniSetupNativeJVMPointer() to reflect the current purpose
                         - changed jniRexxCreateInterpreterInstance to accept new options argument
                         - added private field "rii_ID" and getter "get_rii_ID()", could also be inferred via field "rexxEngine"



     -------------------------------------- before adding functionality to configure the Rexx interpreter instance

     needs: Java level 1.4 due to using Exception's getCause();
            ooRexx 4.0
     2011-06-04, ---rgf, - make sure that BSFExceptions get the Throwable assigned, if this
                           intelligible from context, such that one can retrieve it by using
                           the BSFException instance method getTargetException()

     2011-03-15, ---rgf, - add new persons to credit/kudos

     2011-02-23, ---rgf, - as this class is not public, its public members cannot be accessed
                           by non-package code; therefore added that a new subfunction
                           "carryOutSystemExit": if no argument ("0"|.false or "1"|.true) is given,
                           then the current setting will be returned, otherwise the boolean field
                           will be set to the argument, which gets returned as well

     2011-02-09, ---rgf, - added static public boolean field 'bCarryOutSystemExit': if set to false,
                           then the subfunction EXIT does not carry out a delayed System.exit (ignores it);
                           this allows BSF4ooRexx programs like 'ooRexxTry.rxj' to execute BSF4ooRexx
                           programs that employ bsf.exit, which - if carried out - would exit Java and
                           by that means will shut down 'ooRexxTry.rxj'

     2011-01-24, ---rgf, - add new persons to credit

     2010-05-22, ---rgf, - removed special handling of Character.class or char.class arguments because
                           of changes in BSF4ooRexx.cc

     2010-05-20, ---rgf, - fixed a bug in converting to a char or Character, if the value was "00"x
                         - coerceArgs(): if matching wrapper objects are passed for primitive values
                                         from Rexx, then accept them from now on

     2010-04-20, ---rgf, - fixed a bug in INVOKE, if looking up and resolving a method on class-objects

     2010-04-18, ---rgf, - changed logic in REGISTER_BEAN | NEW (matching changed logic in
                           BSF.CLS' .BSF~bsf.import): Java class object is already loaded,
                           hence just look it up in the registry

     2010-04-17, ---rgf, - fixed conversion error in setFieldValue[Strict], if assigning "1" to a boolean

     2010-03-27, ---rgf, - getCauseAsString() now if processing an InvocationTargetException then
                           first attempts to do a getTargetException(), if not successful falls back
                           to current getCause().

     2010-02-20, ---rgf, - added isA|instanceOf(jObject, jClass)

     2009-10-31, ---rgf, - replaced European chars with their Unicode values
                         - made error message for BSFREGISTRY more informative for the programmer

     2009-10-19, ---rgf, - renaming "BSF4Rexx" to "BSF4ooRexx"
     2009-10-18, ---rgf, - added private methods "lookupBean4JNI" and "registerBean4JNI"
     2009-10-12, ---rgf, - allow "getRAJO" as synonym for "getRexxAndJava"

     2009-10-07, ---rgf, - allow retrieving this RexxEngine instance

     2009-10-02, ---rgf, - allow retrieving this RexxAndJava instance

     2009-09-10, ---rgf, - added a synonym "createJavaArrayOf" for "createArrayOf" to make this self-documuntary
                           clear that a Java array object is to be created and not an ooRexx one
                         - added a synonmym "createJavaArray" for "createArray": this allows to make
                           it clear that a Java array object is to be created and not an ooRexx one

     2009-08-27, ---rgf, - added the argument "returnType" to rexxInterface.jniRexxSendMessageToRexxObject()
                         - added private method "convFromJNI(type,value)"
     2009-07-30, ---rgf, - adding subfunction "createArrayOf": creates a single dimensioned
                           Java array and returns it; the first argument determines the type of the
                           array ("componentType"); all subsequent values in the array are then coerced
                           to the "componentType" and stored. If no values are given (just the single
                           "componentType") then the Java array will have a size of "0", but still being
                           of the given "componentType"
     2009-07-14, ---rgf, - treat Boolean in "convFromRexx(typeIndicator,value)" string "true" as Boolean
                           (so far only "1" = Rexx truth value was regarded as "true"); because of the
                           typeIndicator it is clear that a Boolean (string) value is submitted; allowing
                           the (caseless) string "true" will make transcriptions from Java programs easier

     2009-07-13, ---rgf, - corrected a bug in INVOKE and INVOKE_STRICT, if object was a class object
                         - applying System.identityHashValue() for all Java objects, as we truly need
                           uniquely identifiable peers

     2009-07-12, ---rgf, - renamed argument "methodDescription" to "methodDescriptor" for
                           jniRexxSendMessageToRexxObject()

     2009-07-11, ---rgf, - added arguments "javaObject" and "methodDescription" to
                           jniRexxSendMessageToRexxObject()

     2009-06-27, ---rgf, - changed signature of jniRexxSendMessageToRexxObject() to accept the
                           beanname of an optional Java method object

     2009-06-25, ---rgf, - CREATE_ARRAY_OBJECT: now also accepts a string denoting a fully qualified
                           Java class as its component type

     2009-06-23, ---rgf, - removed argument "creationTID" in jniRexxSendMessageToRexxObject (...)

     2009-06-20, ---rgf, - subfunction POLL_EVENT_TEXT: now checks for correct number of arguments and
                           checks that timeout value is not negative, if supplied

     2009-06-07, ---rgf, - removed subfunction "addEventListenerReturningEventInfos",
                           use new external Rexx function BsfCreateRexxProxy() instead

     2009-05-.., ---rgf, - added jniCreateInterpreterInstance
                         - added jniGetRexxInterpreterInstanceRoot
                         - added jniRexxRunProgram
                         - added jniRexxSendMessageToRexxObject
                         - added jniUnregisterRexxObject
                         - added jniRexxHaltInterpreterInstance
                         - added jniRexxTerminateInterpreterInstance

     2009-05-04, ---rgf, - added getRexxEngine() accessor

     // ===================================== pre-4.0 editions
     2009-05-01, ---rgf, - made output of arguments in 'invoke' and 'invokeStrict' better ledgible and
                           more informative

     2009-04-02, ---rgf, - added 'jniRexxGetTID()', 'jniRexxSetHalt(tid)'
                         - added new subfunction 'haltThread'

     2008-09-09, ---rgf, - 'unregisterBean' returns the beanName, if unregistering could take
                           place, .nil else (because refCounter is not 0 yet)

     2008-09-08, ---rgf, - fixed bugs in return string in subfunctions "new|register[strict]"
                           and "unregister"

     2008-09-01, ---rgf, - removed undocumented, experimental subfunction
                           "getOrLoadClass(JavaClassName)"

     2008-08-28, ---rgf, - added rawRegisterBean(beanName,object)

     2008-08-26, ---rgf, - added new subfunction "getOrLoadClass(JavaClassName)"

     2008-06-,23, ---rgf, if method javaCallBSF() is invoked recursively, don't apply
                          makeString4Rexx() to the result, as that got applied already at that
                          point in time

     2008-06-22, ---rgf, - after 7 hrs of debugging found and fixed a subtle bug in
                           combination of BSF.CLS' algorithm to determine a bean: returning the
                           beanname as a string causes BSF.CLS to create a BSF_REFERENCE which will
                           cause deregestering the bean from the BSFRegistry whenever the ooRexx
                           reference object goes out of scope, invalidating any other references
                           to the Java object!
                         - added a new BSF-subfunction "bsfPrefixReturnValue([.true|.false])" which
                           controls whether makeString4Rexx() prepends the return value (always a
                           string) with "<O>" (following string is index to BSFRegistry) or
                           "<S>" (plain string follows); will be set by BSF.CLS to allow BSF.WRAP()
                           to unanimously distinguish between string values and object string
                           references
     2008-06-14, ---rgf, passing "scriptSourceLocation" to jniRexxStart()
     2008-06-13, ---rgf, added a missing "]" in an output string
     2008-06-08, ---rgf, if an InvocationTargetException()-exception, then show its contained exception message
                         to ease identifying the cause of the exception
     2008-06-05, ---rgf, if EngineUtils.loadClass() in BSF 2.4 returns "null", then make sure that a
                         class-not-found exception is thrown; BSF should take care of that in the future
     2007-09-21, ---rgf, now using updated "EngineUtils.loadClass(...)" under all circumstances
     2007-01-28, ---rgf, changed "EngineUtils.loadClass(...)" to use instead the Thread's context class loader
     2006-12-10, ---rgf, changed subfunction "version" to accept one argument which indicates which
                         version to return (BSFManager or one of the org.rexxla.bsf.eninges.rexx
                         classes)

     2006-12-08, ---rgf, added subfunction "getBSFManager"; this allows retrieving the
                         BSF version, but also employing other scripts sharing the same
                         BSF registry

     2006-11-19, ---rgf, add transparent NetRexx support, ie. treat "netrexx.lang.Rexx" like
                         "java.lang.String"

     2006-08-01, ---rgf, make sure that the created beanName is unique even in the case of
                         java.lang.reflect.Constructor (which on Sun's Java returns the same
                         hashCode()-value for all of its instances!)
     2006-05-29, ---rgf, fixed bugs in coercing Rexx string values to Boolean and Character using BSF's
                         rather relaxed conversion infrastructure; this caused wrong methods to be picked
                         in "java.io.PrintBuffer" "println()" methods at times;
                         now Boolean *must* be either the Rexx values "0" or "1" and if
                         a character is tried, then the Rexx string must consist of exactly one character;
                         should that (very unlikely) sometimes yield a wrong method to be picked, then you
                         need to use the "bsf.invokeStrict" subfunction or BSF method, which allows you to
                         exactly indicate the type of the argument for the method to be picked

     2006-02-24, ---rgf, added "addEventListenerReturningEventInfos"
     2006-02-15, ---rgf, fixed the Java access error for method invocation, if method is public, but
                         object reference is from a non-public class (like an inner class, eg. Enumeration)
     2006-02-07, ---rgf, fixed an error in "invoke:" (eternal loop, if method not found)
     2006-02-05, ---rgf, not using BSF anymore for invoking methods; reason: if Class' methods need to be
                         invoked via class objects, this is not possible with BSF; changing BSF itself may
                         not be acceptable for others, so adding logic here
     2006-01-05, ---rgf, added subfunction "loadClass" which uses Thread.currentThread().getSystemContextLoader()
                         to load Java classes by name (hint from Rick, thanks an awful lot!!); pre-registering
                         java.lang.Thread in the registry
     2005-11-28, ---rgf, changed license to Apache 2.0
     2005-11-06, ---rgf, added a TypeConvertor to indicate that converstion from "java.lang.String"
                         "java.lang.Object" is o.k. for Rexx (otherwise an error occurs)
     2005-07-30, ---rgf, added method pp() - "pretty print for debug-output"
     2005-07-07, ---rgf, fixed bug in coercing boolean/Boolean values
     2005-06-18, ---rgf, fixed bug if interface is sought and type is String
     2005-06-10, ---rgf, sending attribute message to interface class now works
     2005-05-06, ---rgf, added GET_FIELD_STRICT
 */


class RexxAndJava {

// TODO: rgf, 2016-12-03, remove after debugging
final static boolean bDebugRefCounter=false; // true; // false;
final static public ConcurrentHashMap<String,RAJBean> rajRegistryToWatch=new ConcurrentHashMap<String,RAJBean>();

// TODO: rgf, 20180106, remove after debugging RexxReflect
final static boolean bRgfUseNewRexxReflect=false; // true;        // if true, use new RexxReflect class


    /* Allows to refer to the RexxResolve class that gets employed (depends on the version of Java).
    *
    * @since BSF4ooRexx 6.0 (2018-01-05)
    */
    static RexxReflectInterface rxReflector=null;

    /** Stores the Java runtime version as an int, where Java 1.6 gets stored as 6 and so on.
    *
    * @since BSF4ooRexx 6.0 (2018-01-05)
    */
    static final int javaRuntimeVersion;

    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static final public String version = "600.20180108";

    static final private String pkgName = "org.rexxla.bsf.engines.rexx";

    /** If set to <code>.false</code>, then subfunction &quot;EXIT&quot; is not carried out.
        This may be necessary in rare cases, e.g. if BSF4ooRexx programs using <code>bsf.exit</code>
        are run via a BSF4ooRexx application like &quot;ooRexxTry.rxj&quot;, which would be
        shut down together with Java, no questions asked, which may surprise the user.
     * @since 2011-02-09
    */
    static public boolean carryOutSystemExit = true;


    /** Defines the version string. */
    static final private String RAJ_VERSION = version + " " + pkgName + " (org.apache.bsf)";

    /** Defines the name of DLL to be used to resolve the native functions. */
    static final protected String DLL_NAME = "BSF4ooRexx";

    /** Defines the string to indicate an 'eventText' is passed in: &quot;&lt;eventText&gt;&quot;. */
    static final protected String EVENT_TEXT = "<eventText>";

    /** Defines the string that leads in the BSFRegistry key to the Object array containing the
     *  event object and additional information. */
    static final protected String EVENT_LISTENER_ARGUMENTS_LEADIN  = "/*BSFBEAN/*";

    /** Defines the string that leads out the BSFRegistry key to the Object array containing the
     *  event object and additional information. This concludes a sequence which represents
     *  at the same time a nested Rexx comment.
     */
    static final protected String EVENT_LISTENER_ARGUMENTS_LEADOUT = "*/*/";


    /** Defines the string which represents <code>null</code>, by default
        <code>&quot;.NIL&quot;</code>. Arguments given by Rexx can in addition
        be indicated to be <code>null</code> by merely omitting them on the Rexx side.
      */
    static protected String null4Rexx = ".NIL";


    /** Determines whether the return value to Rexx will prepend the string &quot;OBJECT&quot;
     * or &quot;STRING&quot; with a trailing tabulator character (&quot;09&quot;x in Rexx or
     * the '\t' character in Java). By default this is set to <code>false</code> to remain
     * backwardly compatible with older BSF4ooRexx programs.
     */
    // static protected boolean bsfPrefixReturnValue = false; // 2008-06-22
    protected boolean bsfPrefixReturnValue = false; // 2008-06-22
        // the following strings must not be changed as BSF.CLS' BSF.WRAP() routine and
        // other methods and routines there depend on the value *and* length (must be 3)!
    static final protected String OBJECT_INDICATOR_PREFIX="<O>"; // "OBJECT ";
    static final protected String STRING_INDICATOR_PREFIX="<S>"; // "STRING ";


    /** Allows Synchronized access to the vector storing the
      * text sent by the eventListeners.
      */
    protected SyncPutGet eventTextList=new SyncPutGet() ;


    // ---rgf, 2006-11-19
    /** Stores the &quot;netrexx.lang.Rexx&quot; class object, if available.
     *  Cf. <a href="http://www2.hursley.ibm.com/netrexx/">http://www2.hursley.ibm.com/netrexx/</a>.
     */
    static protected Class nlR_class =null; // to store the class object for 'netrexx.lang.Rexx'


    /** Stores the BSF manager. */
    BSFManager mgr;

    /** Stores the Rexx engine. */
    BSFEngine  rexxEngine;

    /** Getter method for field {@link #rexxEngine}. */
    protected BSFEngine getRexxEngine()     // accessor
    {
        return rexxEngine;
    }


    /** Field used, if System.identityHashCode() returns duplicate values in method
     *  {@link #makeBeanName(Object o)}, to allow adding a unique number to make the
     *  resulting identifying string unique.
     *
     * @since 2012-11-01
     * @author Rony G. Flatscher
     */
    static int spilloverCounter=0;



    /**
      * To store the default TypeConvertorRegistry, if needed.
      */
// rgf, 2016-11-27: moved to static
    static TypeConvertorRegistry glob_tcr = new TypeConvertorRegistry();  // will include default set of type convertors


    /** Contains the RexxInterpreter instance ID this engine is associated with.
     */
    private String rii_ID=null;


    /** Getter method.
     *  @return the RexxInterpreter instance ID
     */
    // protected String get_rii_ID(); the BSF.CLS prolog code makes sure that this gets invoked
    public String get_rii_ID()
    {
        return rii_ID;
    }

    /** Setter method. Only meant to be used by Java4Rexx.java and RexxAndJava instance that has noe
     *  peer Rexx interpreter instance yet. If already set an exception gets raised.
     *
     */
    protected void set_rii_ID(String new_rii_ID) throws BSFException
    {
        final boolean bDebug= // true;
                              false;

if (bDebug) System.err.println(this+".set_rii_ID(new_rii_ID=["+new_rii_ID+"])");

        if (rii_ID!=null)   // already created for this RAJO!
        {
            throw new BSFException("A Rexx interpreter instance has been created already having the ID ["+rii_ID+"]");
        }

        this.rii_ID=new_rii_ID;
    }


    /** If terminate() was invoked, then this instance's BSFManager and BSFEngine must not be used anymore.
     */
    protected boolean bTerminated=false;


    /** This fields serves as a central registry for all Rexx interpreter instances.
     */
    static private ConcurrentHashMap<String,RAJBean> rajRegistry=new ConcurrentHashMap<String,RAJBean>();;

    /** Lock used when adding, removing Java objects or changing their reference counters. */
    final static private Object lockRajRegistry=new Object();



    /** Load the DLL, which allows interfacing Java with Rexx or Object Rexx, determines
     *  whether setting accessibility rights is supported.
     * <p>
     *
     * Preregisters the most important class objects including primitive and their wrapper classes.
     * Referring to them is possible by using the following strings from Rexx (here the case *is* significant!):
     *
     *  <ul>
     *      <li> &quot;Class.class&quot;,
     *      <li> &quot;Object.class&quot;,
     *      <li> &quot;Method.class&quot;,
     *
     *      <li> &quot;Array.class&quot;,
     *      <li> &quot;String.class&quot;,
     *      <li> &quot;System.class&quot;,
     *      <li> &quot;Thread.class&quot;,
     *
     *      <li> &quot;Boolean.class&quot;,
     *      <li> &quot;boolean.class&quot;,
     *      <li> &quot;Byte.class&quot;,
     *      <li> &quot;byte.class&quot;,
     *      <li> &quot;Character.class&quot;,
     *      <li> &quot;char.class&quot;,
     *      <li> &quot;Double.class&quot;,
     *      <li> &quot;double.class&quot;,
     *      <li> &quot;Integer.class&quot;,
     *      <li> &quot;int.class&quot;,
     *      <li> &quot;Long.class&quot;,
     *      <li> &quot;long.class&quot;,
     *      <li> &quot;Float.class&quot;,
     *      <li> &quot;float.class&quot;,
     *      <li> &quot;Short.class&quot;,
     *      <li> &quot;short.class&quot;,
     *      <li> &quot;Void.class&quot;,
     *      <li> &quot;void.class&quot;.
     *  </ul>
     */
    static
    {

// TODO: rgf, 2016-12-19: one may remove the beanName from RAJBean after debugging
            // preregister important classes with a predefined bean name in the new central registry
        rajRegistry.put("Class.class",    new RAJBean("Class.class",    Class.class    ));
        rajRegistry.put("Object.class",   new RAJBean("Object.class",   Object.class   ));
        rajRegistry.put("Method.class",   new RAJBean("Method.class",   Method.class   ));

        rajRegistry.put("Array.class",    new RAJBean("Array.class",    Array.class    ));
        rajRegistry.put("String.class",   new RAJBean("String.class",   String.class   ));
        rajRegistry.put("System.class",   new RAJBean("System.class",   System.class   ));
        rajRegistry.put("Thread.class",   new RAJBean("Thread.class",   Thread.class   ));

        rajRegistry.put("Boolean.class",  new RAJBean("Boolean.class",  Boolean.class  ));
        rajRegistry.put("boolean.class",  new RAJBean("boolean.class",  boolean.class  ));
        rajRegistry.put("Byte.class",     new RAJBean("Byte.class",     Byte.class     ));
        rajRegistry.put("byte.class",     new RAJBean("byte.class",     byte.class     ));
        rajRegistry.put("char.class",     new RAJBean("char.class",     char.class     ));
        rajRegistry.put("Character.class",new RAJBean("Character.class",Character.class));
        rajRegistry.put("Double.class",   new RAJBean("Double.class",   Double.class   ));
        rajRegistry.put("double.class",   new RAJBean("double.class",   double.class   ));
        rajRegistry.put("Float.class",    new RAJBean("Float.class",    Float.class    ));
        rajRegistry.put("float.class",    new RAJBean("float.class",    float.class    ));
        rajRegistry.put("int.class",      new RAJBean("int.class",      int.class      ));
        rajRegistry.put("Integer.class",  new RAJBean("Integer.class",  Integer.class  ));
        rajRegistry.put("Long.class",     new RAJBean("Long.class",     Long.class     ));
        rajRegistry.put("long.class",     new RAJBean("long.class",     long.class     ));
        rajRegistry.put("Short.class",    new RAJBean("Short.class",    Short.class    ));
        rajRegistry.put("short.class",    new RAJBean("short.class",    short.class    ));
        rajRegistry.put("Void.class",     new RAJBean("Void.class",     Void.class     ));
        rajRegistry.put("void.class",     new RAJBean("void.class",     void.class     ));



        // ---rgf, 2006-11-19, try to load class "netrexx.lang.Rexx", if available to the contextClassLoader
        try
        {
            nlR_class = Thread.currentThread().getContextClassLoader().loadClass("netrexx.lang.Rexx");
        }
        catch (Exception e)
        {
            try {
                // uses defined class loader of current class
                nlR_class = Class.forName("netrexx.lang.Rexx");
            }
            catch (Exception e0){
                ;
            }
        }

        // 2012-03-12, rgf: move loading the BSF4ooRexx library *after* loading the NetRexx class, this
        //                  way it is clear whether netrexx.lang.Rexx is available for BSF4ooRexx
        System.loadLibrary( DLL_NAME );


        // for conversions of java.lang.String -> java.lang.Object, RexxAndJava.java needs an explicit
        // type convertor; rgf, 2005-11-06
        {
// rgf, 2016-11-27: moved to static
                // register conversion of java.lang.String->java.lang.Object here, no need seen
                // to enclose it into the standard type convertors in class TypeConvertorRegistry.java
             TypeConvertor tc = new TypeConvertor () {
              public Object convert (Class from, Class to, Object obj) {
                  return obj;
              }

              public String getCodeGenString() {
                    return "(Class from, Class to, Object obj) {\n" +
                               "return obj;\n" +
                               "}";
              }
            };
                 // register this type-convertor
            glob_tcr.register (String.class, Object.class, tc); // rgf, 2005-07-30
        }


            // 20180105, rgf: determine Java runtime version in use
        int jVersion=6;     // default to Java 6
        try {
            String[] st=System.getProperty("java.specification.version").split("\\.");
            jVersion=Integer.valueOf((st[0].equals("1") ? st[1] : st[0]));   // extract and assign Java version
        }
        catch (Throwable t) {}
        javaRuntimeVersion=jVersion;    // assign version to static final field

        switch (javaRuntimeVersion)     // set RexxReflect class object to use
        {
            case 8: // TODO: handle default and static Interface methods

            case 9: // TODO: only use classes, if their module is open to the Java object's class for which reflection takes place
                     rxReflector=new RexxReflectJava9();
                     break;

                     // TODO: test whether using the 9-version compiled to 1.6 can be run on 1.6; if so
                     //       rename RexxReflectJava9.java to RexxReflect.java
            default: rxReflector=new RexxReflectJava9() ;        // default to Java 6 related reflection
        }

// System.err.println(RexxAndJava.class+" (static block) javaRuntimeVersion="+javaRuntimeVersion);
// System.err.println(RexxAndJava.class+" (static block) rxReflector       ="+rxReflector);
// try { System.err.println(rxReflector.reflectMethod()); } catch (Throwable t) {}

    }   // end of static block


    /** Returns an unmodifiable version of the {@link rajRegistry} Map (for debugging purposes).
    */
// TODO: rgf, 20161219, remove after debugging
    protected static Map getRajRegistry()
    {
        return Collections.unmodifiableMap(rajRegistry);
    }




    /** Constructor to receive and store the BSF manager and the Rexx engine.
     */
    RexxAndJava (BSFManager mgr, BSFEngine rengine)
    {
        this.mgr        = mgr;     //the current BSFManager
        this.rexxEngine = rengine; //the current RexxEngine
        jniSetupNativeJVMPointer();     // make sure the JNI library can initialize the Java related datastructures
    }



    /** Finalize method that makes sure that terminate() on Rexx engine gets invoked.
     *
     *  @since 2015-08-03
     */
    @SuppressWarnings( "deprecation" ) public void finalize ()      // method needed to maintain native Rexx side
    {
        final boolean bDebug= false;
                              // true;
        if (bDebug)
        {
            System.err.println(this+": in finalize(), this=["+this+"], rii_ID=["+rii_ID+"]...");
System.err.println(this+": *** - FINALIZE -  *** *** - FINALIZE -  *** *** - FINALIZE -  *** *** - FINALIZE -  *** *** - FINALIZE -  *** *** - FINALIZE -  *** ");
        }
        terminate();        // make sure we terminate and carry out any clean-up
    }






    /** This native method makes sure that the C++ library gets a hold to
     *  this JVM in case it does not already have a pointer to it.
     */
    protected native void jniSetupNativeJVMPointer ();     // changed 2012-02-06, rgf



    /** This native method Calls the Rexx interpreter,
      * having it execute the passed in
      * <em>script</em> with the given array of arguments.
      *
      * @param scriptSourceLocation a String denoting the location from where the
      *               script code comes from (usually a fully qualified file name)
      *               or <code>null</code>, if no information is available
      *
      * @param script a String consisting of Rexx statements separated by
      *               by line-end characters (';' or CR-LF).
      *
      * @param args an array of Strings representing the arguments
      *               or <code>null</code>, if no arguments are available.
      *
      * @return a String containing the result or <code>null</code>, if
      *         no result is returned or the Rexx interpreter stopped
      *         execution due to a runtime error.
      */

// rgf, 2009-05-24: no need anymore; also this was restricted to accepting arguments in String[] form
//                  and returning a String only, whereas the replacements jniRexxRunProgram() and
//                  jniRexxSendMessageToRexxObject() accept Object[] arguments and return Object
//
//    public native String jniRexxStart (String scriptSourceLocation, String script, String[] args);



    /** This native method waits for the Object Rexx interpreter to terminate and waits until
      * all of the cleanup (e.g. of Object Rexx) is done. This is necessary for ooRexx 3.x.
      *
      * @return returns always 0. (Just there to allow future return codes, if necessary.)
    @deprecated
      */
    @Deprecated protected native int jniRexxWaitForTermination ( );

    /** This native method indicates whether the Object Rexx interpreter is still running or not.
      * This is necessary for ooRexx 3.x.
      *
      * @return returns always 0. (Just there to allow future return codes, if necessary.)
    @deprecated
      */
    @Deprecated protected native int jniRexxDidRexxTerminate ( );


    /** This native method returns the current thread id as seen by
     *  BSF4ooRexx encoded as a decimal value into a String. This value
     *  can be used as an argument for {@link #jniRexxSetHalt(String tid)}.
      *
      * @return returns the decimal value of the thread id (TID)
      */
    protected native String jniRexxGetTID ( );



    /** This native method halts a Rexx interpreter instance or individual
     *  Rexx threads in the current process. It uses the Rexx function
     * &quot;RexxSetHalt()&quot; (cf. the documentation in the &quot;rexxpg.pdf&quot;
     * book of Object REXX or ooRexx.
     *
     * @param tid a string containing either the thread id as returned by
     *            {@link #jniRexxGetTID ( )} or the string <code>;&quot;0&quot;</code>,
     *            which causes the Rexx interpreter instance to halt (terminate) all
     *            its Rexx threads by raising the Rexx halt condition.
      *
      * @return returns the return code of the Rexx function (from the ooRexx
     *          documentation, rev. 4334):
     *           <table>
     *              <tr>
     *                 <td><code>0</code>
     *                 <td><code>RXARI_OK</code>
     *                 <td>The function completed successfully.
     *
     *              <tr>
     *                 <td><code>1</code>
     *                 <td><code>RXARI_NOT_FOUND</code>
     *                 <td>The target Rexx procedure was not found.
     *
     *              <tr>
     *                 <td><code>2</code>
     *                 <td><code>RXARI_PROCESSING_ERROR</code>
     *                 <td>A failure in Rexx processing occurred.
     *
     *           </table>
      */
    // used by RexxEngine
    protected native int jniRexxSetHalt (String tid);


    // rgf, 2009-05-03, exploiting new ooRexx 4.0 APIs ----------------------------------------
    /** Creates a new Rexx interpreter instance using the ooRexx 4.0 API <code>RexxCreateInterpreter()</code>.
     *
     * @return a String representing the created Rexx interpreter instance ID or <code>null</code>, if
     *         no Rexx interpreter instance could be created
     *
     * @since May 2009
     */
    protected native String jniRexxCreateInterpreterInstance (Object [] options);           // RexxCreateInterpreter(), returns rii-id



    // rgf, 2009-05-26, exploiting new ooRexx 4.0 APIs ----------------------------------------
    /** Returns the Rexx interpreter instance serving as the root of the linked list of Rexx interpreter using the ooRexx 4.0 API <code>RexxCreateInterpreter()</code>.
     * This will be needed in the context where Rexx loads Java, and the Java side needs to set up the BSF infrastructure
     * accordingly.
     *
     * @return a String representing the root Rexx interpreter instance ID or <code>null</code>, if
     *         no Rexx interpreter instance is available at all
     *
     * @since May 2009
     */
    protected native String jniGetRexxInterpreterInstanceRoot ();           // RexxCreateInterpreter(), returns rii-id



        // execute piece of code or dispatch method on ooRexx object
    /** Uses the ooRexx 4.0 API to execute a Rexx program or to execute a callback (sending a message to a given object).
     *
     * @param rii_ID a String representing the Rexx interpreter instance as returned by {@link #jniRexxCreateInterpreterInstance ()}
     * @param invocationType determines the desired invocation
     *                  <dl>
     *                       <dt>1 <dd> execute Rexx program from file, use the 4.0 API <code>CallProgram</code>
     *                       <dt>2 <dd> execute Rexx program from buffer, uses the 4.0 API <code>NewRoutine</code> and <code>CallRoutine</code>
     *                       <dt>3 <dd> load Rexx package (and run prolog) from file, use the 4.0 API <code>LoadPackage</code>
     *                       <dt>4 <dd> load Rexx package (and run prolog) from buffer, use the 4.0 API <code>LoadPackageFromData</code>
     *                  </dl>
     *
     * @param fileName  name (path) to file that contains the program to execute
     * @param programData Rexx code to execute
     * @param args an array of arguments to pass on
     *
     * @throws a {@link RexxException}, if the argument does not denote an existing Rexx interpreter instance
     * @return a return value from running the Rexx program or <code>null</code>
     *
     * @since May 2009
     */
    protected  native Object jniRexxRunProgram (
                    String  rii_ID,                 // RexxInstance instance ID
                    int     invocationType,         // determines whether "CallProgram", "LoadPackage" or "LoadPackageFromData" is to be used
                    String  fileName,               // filename which contains the program to run
                    String  programData,            // Rexx code to execute; if given "fileName" denotes a (symbolic) name for this code
                    Object[]args                    // arguments
                                           );
                    // args: of type String (keys to registry or pure values) or
                    //       RexxProxy (JNI needs to fetch and use it)


    /** Uses the ooRexx 4.0 API to execute a Rexx program or to execute a callback (sending a message to a given object).
     *
     * @param rii_ID a String representing the Rexx interpreter instance as returned by {@link #jniRexxCreateInterpreterInstance ()}
     * @param obj_ID the object id that is used as a key on the JNI side, {@link org.rexxla.bsf.engines.rexx.RexxProxy#getRexxObjectID ()}
     * @param userData_ID optional RexxProxy, supplied either with the <code>BsfCreateRexxProxy()</code> as second argument or later
     *                    set by programs
     *
     * @param javaObjectBean  the Java object that invoked this very callback
     * @param methodObjectBean  <code>java.lang.reflect.Method</code> object that was invoked in the Java proxy
     * @param messageName the name of the message to send to the Rexx object (the Rexx object will look for a method
     *               named like the message and invoke it)
     * @param methodDescription string formated as internal Java string that describes a method's arguments and return value
     * @param args an array of arguments to pass on
     * @param returnType the primitive type signature (for primitive types or matching wrapper classes) or <code>null</code> for any object
     * @throws a {@link RexxException}, if the argument does not denote an existing Rexx interpreter instance
     *
     * @return <code>0</code>
     *
     * @since May 2009
     */
    protected native Object jniRexxSendMessageToRexxObject (
                    String  rii_ID,             // RexxInstance instance ID
                    String  obj_ID,             // RexxProxy objectID
                    String  userData_ID,        // RexxProxy userData_ID
//                    String  creationTID,      // the BsfGetTID() value of the thread in which the proxy got created
                    String  javaObjectBean,     // javaObjectBean (a String)
                    String  methodObjectBean,   // methodObjectBean (a String)
                    String  messageName,        // message name (determines name of method to invoke)
                    String  methodDescription,  // messageDesscription, internal string describing arguments and return value
                    Object[]args,               // arguments, edited for native layer
                    String  returnType          // primitive type signature or null
                                                           );
                    // args: of type String (keys to registry or pure values) or
                    //       RexxProxy (JNI needs to fetch and use it)



    /** Unregisters the Rexx object denoted by 'obj_ID' from the JNI registry.
     *
     * @param obj_ID the object id that is used as a key on the JNI side
     *
     * @return number of references to the Rexx object in the JNI registry left; a
     *         number of '0' indicates that no references are left, a return value of '-1'
     *         indicates that the Rexx object was not registered (could not be found)
     */
    // used by RexxProxy
    protected native int jniUnregisterRexxObject(String obj_ID);


    /** Uses the ooRexx 4.0 API <code>Halt()</code> to raise the <code>HALT</code> condition in all Rexx threads
     *  belonging to the given Rexx interpreter instance.
     *
     * @param rii_ID a String representing the Rexx interpreter instance as returned by {@link #jniRexxCreateInterpreterInstance ()}
     * @throws a {@link RexxException}, if the argument does not denote an existing Rexx interpreter instance
     * @return <code>0</code>
     *
     * @since May 2009
     */
    // used by RexxEngine
    protected native int    jniRexxHaltInterpreterInstance (String rii_ID); // use Halt()


    /** Uses the ooRexx 4.0 API <code>Terminate()</code> to wait on all Rexx threads
     *  belonging to the given Rexx interpreter instance to terminate.
     *
     * @param rii_ID a String representing the Rexx interpreter instance as returned by {@link #jniRexxCreateInterpreterInstance ()}
     * @throws a {@link RexxException}, if the argument does not denote an existing Rexx interpreter instance
     * @return <code>0</code>
     *
     * @since May 2009
     */
    // used by RexxEngine and Java4Rexx
    protected native int    jniRexxTerminateInterpreterInstance (String rii_ID); // use Terminate(), but *must* be issued from the original thread context


     // ------------------ JNI functions meant to be accessed by Java exit handlers only!
    //                     (these jni functions need a properly set up opaque slot argument!)

     //  rgf, 2012-02-08, used for allowing exit and command handlers to interact with their RexxExitContext
     //                   for raising conditions and querying/setting variables.
     //
     protected native void    jniRaiseCondition    (Object[] slot, String conditionName, String description, Object[] additional, Object result);
     protected native void    jniRaiseException    (Object[] slot, long definedErrorNumber, Object[] substitutions);
     protected native void    jniRaiseException0   (Object[] slot, long definedErrorNumber);
     protected native void    jniRaiseException1   (Object[] slot, long definedErrorNumber, String substitution1);
     protected native void    jniRaiseException2   (Object[] slot, long definedErrorNumber, String substitution1, String substitution2);
     protected native Object  jniGetContextVariable(Object[] slot, String variableName);
     protected native void    jniSetContextVariable(Object[] slot, String variableName, Object variableValue);

     // rgf, 2012-02-11: for RexxHandler access only (these jni functions need a properly set up opaque slot argument!)
     protected native boolean jniCheckCondition    (Object[] slot);
     protected native void    jniClearCondition    (Object[] slot);
     protected native Object  jniGetConditionInfo  (Object[] slot);

     protected native void    jniSetThreadTrace    (Object[] slot, boolean state);
     protected native void    jniHaltThread        (Object[] slot);

     // rgf, 2012-02-12: for RexxHandler access only (these jni functions need a properly set up opaque slot argument!)
     protected native void    jniDropContextVariable(Object[] slot, String variableName);

     // rgf, 2012-02-18: for RexxHandler access only (these jni functions need a properly set up opaque slot argument!)
     protected native void    jniSetContextVariableToNil(Object[] slot, String variableName);

     // rgf, 2012-02-19: for RexxHandler access only (these jni functions need a properly set up opaque slot argument!)
     protected native Object  jniGetCallerContext   (Object[] slot);
     protected native Object  jniGetAllContextVariables(Object[] slot);

     // rgf, 2012-02-24: for RexxHandler access only (these jni functions need a properly set up opaque slot argument!)
     protected native Object  jniGetLocalEnvironment(Object[] slot);
     protected native Object  jniGetGlobalEnvironment(Object[] slot);
     // rgf, 2012-02-25: for RexxHandler access only (these jni functions need a properly set up opaque slot argument!)
     protected native Object  jniNil(Object[] slot);
     protected native long    jniInterpreterVersion(Object[] slot);
     protected native long    jniLanguageLevel     (Object[] slot);

     // rgf, 2014-03-29
     protected native String  jniProcEnvironment(int function, String envName, String envValue);


     // ------------------ allow fetching a few important Rexx objects resp. Rexx interpreter related information
     // rgf, 2015-05-09
     protected native Object  jniGetLocalEnvironment0 (String rii_ID);
     protected native Object  jniGetGlobalEnvironment0(String rii_ID);
     protected native Object  jniNil0                 (String rii_ID);
     protected native long    jniInterpreterVersion0  (String rii_ID);
     protected native long    jniLanguageLevel0       (String rii_ID);





    /**  rgf, 2003-01-15, used for allowing the BSF4ooRexx-DLL to get at *env, *obj and then to mid etc.
     * */
    protected native int jniInitialize4Rexx() ;


/* ---> rgf, 20180108
        // define the constants for the desired function
static final int           ADD_EVENT_LISTENER          =  1  ;
static final int           ARRAY_AT                    =  2  ;
static final int           ARRAY_LENGTH                =  3  ;
static final int           ARRAY_PUT                   =  4  ;
static final int           ARRAY_PUT_STRICT            =  5  ;
static final int           CREATE_ARRAY_OBJECT         =  6  ;
static final int           EXIT                        =  7  ;
static final int           GET_FIELD_VALUE             =  8  ;
static final int           GET_FIELD_VALUE_STRICT      =  9  ;    // 2005-05-06, ---rgf
static final int           GET_PROPERTY_VALUE          =  10 ;
static final int           GET_STATIC_VALUE            =  11 ;
static final int           GET_STATIC_VALUE_STRICT     =  12 ;
static final int           GET_VERSION                 =  13 ;
static final int           INVOKE                      =  14 ;
static final int           INVOKE_STRICT               =  15 ;
static final int           LOOKUP_BEAN                 =  16 ;
static final int           POLL_EVENT_TEXT             =  17 ;
static final int           POST_EVENT_TEXT             =  18 ;
static final int           REGISTER_BEAN               =  19 ;
static final int           NEW                         =  19 ;    // 2005-06-17, ---rgf: synonym for REGISTER_BEAN, hence same value
static final int           REGISTER_BEAN_STRICT        =  20 ;
static final int           NEW_STRICT                  =  20 ;    // 2005-06-17, ---rgf: synonym for REGISTER_BEAN_STRICT, hence same value
static final int           SET_FIELD_VALUE             =  21 ;
static final int           SET_FIELD_VALUE_STRICT      =  22 ;
static final int           SET_PROPERTY_VALUE          =  23 ;
static final int           SET_PROPERTY_VALUE_STRICT   =  24 ;
static final int           SET_REXX_NULL_STRING        =  25 ;
static final int           SLEEP                       =  26 ;
static final int           UNREGISTER_BEAN             =  27 ;
static final int           WRAP_ARRAY_OBJECT           =  28 ;
static final int           WRAP_ENUMERATION_OBJECT     =  29 ;
static final int           LOAD_CLASS                  =  30 ;   // 2006-01-05, ---rgf: new subfunction to get a thread-context loaded class

// removed: 2009-06-07: use BsfCreateRexxProxy() instead
// static final int           ADD_EVENT_LISTENER_2        =  31 ;   // 2006-02-24, ---rgf: allows for retrieving event objects

static final int           GET_BSF_MANAGER             =  32 ;   // 2006-12-08, ---rgf
static final int           BSF_PREFIX_RETURN_VALUE     =  33 ;   // 2008-06-22, ---rgf
static final int           RAW_REGISTER_BEAN           =  34 ;   // 2008-08-28, ---rgf
static final int           REXX_HALT                   =  35 ;   // 2009-04-02, ---rgf
static final int           CREATE_ARRAY_OBJECT_OF      =  36 ;   // 2009-07-30, ---rgf
static final int           GET_REXX_AND_JAVA           =  37 ;   // 2009-10-02, ---rgf
static final int           GET_REXX_ENGINE             =  38 ;   // 2009-10-07, ---rgf

static final int           KUDOS                       =  39 ;   // 2009-10-17, ---rgf

static final int           IS_INSTANCE                 =  40 ;   // 2010-02-20, ---rgf

static final int           CARRY_OUT_SYSTEM_EXIT       =  41 ;   // 2011-02-23, ---rgf

static final int           ENV_GET                     =  42;    // 2014-03-29, ---rgf
static final int           ENV_SET                     =  43;    // 2014-03-29, ---rgf

static final int           INVOKE_JSO                  =  44;    // 2014-10-02, ---rgf
static final int           INVOKE_STRICT_JSO           =  45;    // 2014-10-04, ---rgf
static final int           GET_FIELD_VALUE_JSO         =  46;    // 2014-10-04, ---rgf
static final int           GET_FIELD_VALUE_STRICT_JSO  =  47;    // 2014-10-04, ---rgf
static final int           GET_PROPERTY_VALUE_JSO      =  48;    // 2014-10-04, ---rgf

static final int           GET_STATIC_VALUE_JSO        =  49 ;   // 2014-10-04, ---rgf
static final int           GET_STATIC_VALUE_STRICT_JSO =  50 ;   // 2014-10-04, ---rgf

static final int           GET_RII_ID                  =  51 ;   // 2015-08-01, ---rgf
static final int           INCREASE_REF_COUNTER        =  52 ;   // 2015-08-04, ---rgf


        // define the constant strings for the function names passed from Rexx
private static final String STRING_ADD_EVENT_LISTENER             = "addEventListener"                     ;
// private static final String STRING_ADD_EVENT_LISTENER_2           = "addEventListenerReturningEventInfos"  ; // rgf, 2006-02-24
private static final String STRING_ARRAY_AT                       = "arrayAt"                              ;
private static final String STRING_ARRAY_LENGTH                   = "arrayLength"                          ;
private static final String STRING_ARRAY_PUT                      = "arrayPut"                             ;
private static final String STRING_ARRAY_PUT_STRICT               = "arrayPutStrict"                       ;
private static final String STRING_CREATE_ARRAY_OBJECT            = "createArray"                          ; // rgf, 2003-02-17
private static final String STRING_EXIT                           = "exit"                                 ;
private static final String STRING_GET_BSF_MANAGER                = "getBSFManager"                        ; // rgf, 2006-12-08
private static final String STRING_GET_FIELD_VALUE                = "getFieldValue"                        ;
private static final String STRING_GET_FIELD_VALUE_JSO            = "getFieldValueJSO"                     ; // rgf, 2014-10-04
private static final String STRING_GET_FIELD_VALUE_STRICT         = "getFieldValueStrict"                  ; // rgf, 2005-05-06
private static final String STRING_GET_FIELD_VALUE_STRICT_JSO     = "getFieldValueStrictJSO"               ; // rgf, 2014-10-04
private static final String STRING_GET_PROPERTY_VALUE             = "getPropertyValue"                     ;
private static final String STRING_GET_PROPERTY_VALUE_JSO         = "getPropertyValueJSO"                  ; // rgf, 2014-10-04
private static final String STRING_GET_STATIC_VALUE               = "getStaticValue"                       ;
private static final String STRING_GET_STATIC_VALUE_JSO           = "getStaticValueJSO"                    ; // rgf, 2014-10-04
private static final String STRING_GET_STATIC_VALUE_STRICT        = "getStaticValueStrict"                 ; // rgf, 2005-05-06
private static final String STRING_GET_STATIC_VALUE_STRICT_JSO    = "getStaticValueStrictJSO"              ; // rgf, 2014-10-04
private static final String STRING_GET_VERSION                    = "version"                              ; // rgf, 2003-03-10
private static final String STRING_INVOKE                         = "invoke"                               ;
private static final String STRING_INVOKE_JSO                     = "invokeJSO"                            ; // rgf, 2014-10-02
private static final String STRING_INVOKE_STRICT                  = "invokeStrict"                         ; // rgf, 2003-05-07
private static final String STRING_INVOKE_STRICT_JSO              = "invokeStrictJSO"                      ; // rgf, 2014-10-04
private static final String STRING_LOOKUP_BEAN                    = "lookupBean"                           ;
private static final String STRING_POLL_EVENT_TEXT                = "pollEventText"                        ;
private static final String STRING_POST_EVENT_TEXT                = "postEventText"                        ;
private static final String STRING_REGISTER_BEAN                  = "registerBean"                         ;
private static final String STRING_NEW                            = "new"                                  ; // rgf, 2005-06-17
private static final String STRING_REGISTER_BEAN_STRICT           = "registerBeanStrict"                   ; // rgf, 2003-05-08
private static final String STRING_NEW_STRICT                     = "newStrict"                            ; // rgf, 2005-06-17
private static final String STRING_SET_FIELD_VALUE                = "setFieldValue"                        ;
private static final String STRING_SET_FIELD_VALUE_STRICT         = "setFieldValueStrict"                  ; // rgf, 2003-05-10
private static final String STRING_SET_PROPERTY_VALUE             = "setPropertyValue"                     ;
private static final String STRING_SET_PROPERTY_VALUE_STRICT      = "setPropertyValueStrict"               ; // rgf, 2003-05-10
private static final String STRING_SET_REXX_NULL_STRING           = "setRexxNullString"                    ;
private static final String STRING_SLEEP                          = "sleep"                                ;
private static final String STRING_UNREGISTER_BEAN                = "unregisterBean"                       ;
private static final String STRING_WRAP_ARRAY_OBJECT              = "wrapArray"                            ; // rgf, 2003-02-09
private static final String STRING_WRAP_ENUMERATION_OBJECT        = "wrapEnumeration"                      ; // rgf, 2003-02-12
private static final String STRING_LOAD_CLASS                     = "loadClass"                            ; // rgf, 2006-01-05
private static final String STRING_BSF_PREFIX_RETURN_VALUE        = "bsfPrefixReturnValue"                 ; // rgf, 2008-06-22
private static final String STRING_RAW_REGISTER_BEAN              = "rawRegisterBean"                      ;
private static final String STRING_REXX_HALT                      = "haltThread"                           ; // rgf, 2009-04-02
private static final String STRING_CREATE_ARRAY_OBJECT_OF         = "createArrayOf"                        ; // rgf, 2009-07-30
private static final String STRING_CREATE_JAVA_ARRAY_OBJECT_OF    = "createJavaArrayOf"                    ; // rgf, 2009-09-10
private static final String STRING_CREATE_JAVA_ARRAY_OBJECT       = "createJavaArray"                      ; // rgf, 2009-09-10
private static final String STRING_GET_REXX_AND_JAVA              = "getRexxAndJava"                       ; // rgf, 2009-10-02
private static final String STRING_GET_REXX_ENGINE                = "getRexxEngine"                        ; // rgf, 2009-10-07
private static final String STRING_GET_RAJO                       = "getRAJO"                              ; // rgf, 2009-10-12

private static final String STRING_KUDOS                          = "kudos"                                ; // rgf, 2009-10-17

// private static final String STRING_IS_INSTANCE                    = "isInstance"                           ; // rgf, 2010-02-20
private static final String STRING_IS_A                           = "isA"                                  ; // rgf, 2010-02-20

private static final String STRING_CARRY_OUT_SYSTEM_EXIT          =  "carryOutSystemExit";   // 2011-02-23, ---rgf


private static final String STRING_ENV_GET                        =  "getEnv";               // 2014-03-29, ---rgf
private static final String STRING_ENV_SET                        =  "setEnv";               // 2014-03-29, ---rgf

private static final String STRING_GET_RII_ID                     =  "get_RII_id";           // 2015-08-01, ---rgf
private static final String STRING_INCREASE_REF_COUNTER           =  "increaseRefCounter";   // 2015-08-04, ---rgf

<--- */


    /** Entry point for Rexx calls into Java. This method carries out
      * all the necessary type conversions and finally calls/invokes the
      * indicated Java method.
     *
      * <p>
     *
     * <hr width="50%">
     *
      <em>&quot;typeIndicator&quot;</em>: If a call needs to explicitly indicate the type of Rexx arguments meant
      for invoking a Java
      * method (indicated by a trailing "Strict" in the subfunction name),
      * than the following strings/characters must be used for defining
      * the type of the argument immediately preceding the respective
        argument (minimum letters needed are uppercased and printed in bold):
      *
      *<p>

        <table border="1" cellpadding="3" cellspacing="0" align="center">
        <tr class="TableHeadingColor">
        <td> <strong> TypeIndicator</strong>
        <td> <strong> Following Argument is of Type:
             </strong>

        <tr class="TableRowColor"><!-- table row -->
        <td> <code> &quot;<strong>BO</strong>olean&quot; </code>
        <td> <code> &quot;boolean&quot;</code> i.e. the value <code>0</code> (false) or <code>1</code> (true)

        <tr class="TableRowColor"><!-- table row -->
        <td> <code> &quot;<strong>BY</strong>te&quot; </code>
        <td> <code> &quot;byte&quot;</code> a byte value

        <tr class="TableRowColor"><!-- table row -->
        <td> <code> &quot;<strong>C</strong>har&quot; </code>
        <td> <code> &quot;char&quot;</code> i.e. a single (UTF8) character

        <tr class="TableRowColor"><!-- table row -->
        <td> <code> &quot;<strong>D</strong>ouble&quot; </code>
        <td> <code> &quot;double&quot;</code> a double value

        <tr class="TableRowColor"><!-- table row -->
        <td> <code> &quot;<strong>F</strong>loat&quot; </code>
        <td> <code> &quot;float&quot;</code> a float value

        <tr class="TableRowColor"><!-- table row -->
        <td> <code> &quot;<strong>I</strong>nt&quot; </code>
        <td> <code> &quot;int&quot;</code> an integer value

        <tr class="TableRowColor"><!-- table row -->
        <td> <code> &quot;<strong>L</strong>ong&quot; </code>
        <td> <code> &quot;long&quot;</code> a long value

        <tr class="TableRowColor"><!-- table row -->
        <td> <code> &quot;<strong>O</strong>bject&quot; </code>
        <td> &quot;Object&quot; an Object which must be registered already

        <tr class="TableRowColor"><!-- table row -->
        <td> <code> &quot;<strong>SH</strong>ort&quot; </code>
        <td> <code> &quot;short&quot;</code> a short value

        <tr class="TableRowColor"><!-- table row -->
        <td> <code> &quot;<strong>ST</strong>ring&quot; </code>
        <td> <code> &quot;String&quot;</code> a string value (UTF8)


        </table>
        <p>

      * Hence, for the "...Strict"-subfunctions the Rexx supplied arguments
        must be given
      * in pairs by Rexx: first the type argument (from the above table)
       and then the argument itself.
     * <hr width="50%">
      *
      *  <p>The call syntax from Rexx looks like, e.g.:
      *
      * <pre>
      *    call BSF "SubFunctionName", "argument1", "argument2", "", ...
      * or:
      *    a=BSF("SubFunctionName", "argument1", "argument2", ...)
      * </pre>
      *
      *
      * <p>The following BSF methods are made available to Rexx and
      * can therefore be indicated as the first argument (the 'subfunction') to the 'BSF'
      * call from Rexx:
      * <p>
      *
     *
     *
     *
     *

        <table border="1" cellpadding="3" cellspacing="0">
        <tr class="TableHeadingColor">
        <td> <strong>SubFunctionName </strong>
        <td> <strong> Argument     </strong>
        <td> <strong> Description  </strong>


        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code> &quot;addEventListener&quot; </code>
        <td colspan="2">Adds an event listener to a bean and defines the <code>eventText</code>
                        to be generated. These generated texts can be retrieved with the
                        function <code>pollEventText</code>.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Mandatory, name of a registered bean which should get monitored.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>eventSetName </code>
        <td> Mandatory, name of the set the event belongs to. This name is used
             to create an event adapter at runtime.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>filter </code>
        <td> Mandatory, name of the actual event which should cause a notification.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>eventText </code>
        <td> Mandatory, text which should be sent by this event listener, each
             time the event occurs.




        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code> &quot;arrayAt&quot; </code>
        <td colspan="2">Allows accessing a Java array element.
                        <br>Returns the Java object at the indicated array element,
                            or <code>null</code>, if no object is stored there.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>arrayObject </code>
        <td> Mandatory, name of a registered arrayObject (received by Java).

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>idx1 </code>
        <td> Mandatory, integer indicating the position in dimension 1.
             <p>
             <em>Alternatively,</em> a sole Java int-array containing the 0-based index-values.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>idx2 </code>
        <td> Integer indicating the position in dimension 2 ...



        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code> &quot;arrayLength&quot; </code>
        <td colspan="2">Returns the value of the 'length' field of the <code>arrayObject</code>.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>arrayObject </code>
        <td> Mandatory, name of a registered arrayObject (received by Java).





        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code> &quot;arrayPut&quot; </code>
        <td colspan="2">Allows to put a new value into a Java array element at the
                        indicated position.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>arrayObject </code>
        <td> Mandatory, name of a registered arrayObject (received by Java).


        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>newValue </code>
        <td> The new value to be set at the indicated position in the array.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>idx1 </code>
        <td> Mandatory, integer indicating the position in dimension 1.
             <p>
             <em>Alternatively,</em> a sole Java int-array containing the 0-based index-values.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>...idxn </code>
        <td> Integer indicating the position in dimension n ...

     *
     *

        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code> &quot;arrayPutStrict&quot; </code>
        <td colspan="2">Allows to put a new value into a Java array element at the
                        indicated position.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>arrayObject </code>
        <td> Mandatory, name of a registered arrayObject (received by Java).


        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>typeIndicator </code>
        <td> Type indicator for <code>newValue</code>

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>newValue </code>
        <td> The new value to be set at the indicated position in the array.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>idx1 </code>
        <td> Mandatory, integer indicating the position in dimension 1.
             <p>
             <em>Alternatively,</em> a sole Java int-array containing the 0-based index-values.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>... idxn </code>
        <td> Integer indicating the position in dimension n ...

     *
     *


        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code> &quot;bsfPrefixReturnValue&quot; </code>
        <td colspan="2">Allows to globally set RexxAndJava to prepend a type hint
                        to the string that gets returned ('&lt;O&gt;' for a Java
                        object in the BSFRegistry where the following string denotes
                        the index value; '&lt;S&gt;' indicates that the remainder is
                        to be interpreted as a plain string)

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>newSetting</code>
        <td> Optional. If omitted the current setting will be returned (&quot;1&quot;
                       or quot;0&quot;).
                       If given, then it must be a boolean value, i.e.  &quot;1&quot; (true)
                       or quot;0&quot; (false).
     *
     *

        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code> &quot;carryOutSystemExit&quot; </code>
        <td colspan="2">Allows to globally set RexxAndJava to not carry out
     *                  the subfunction <code>&quot;exit&quot;</code>. This
     *                  allows e.g. <code>&quot;ooRexxTry.rxj&quot;</code> to
     *                  run BSF4ooRexx programs that excercise that subfunction.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>newSetting</code>
        <td> Optional. If omitted the current setting will be returned (&quot;1&quot;
                       or quot;0&quot;).
                       If given, then it must be a boolean value, i.e.  &quot;1&quot; (true)
                       or quot;0&quot; (false).
     *


        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code> &quot;createJavaArray&quot; </code> <br>
     *       <code> &quot;createArray&quot; </code> (deprecated)
        <td colspan="2">Creates a Java array of the given <code>componentType</code> and number of
                       dimensions.
                        <br>Returns the created Java array object.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>componentType</code>
        <td> Mandatory, name of a registered Java class object or a string fully qualifying a Java class.
             For convenience, you may use any of the pre-registered beans named (note case of first letter!):<br>

                 &quot;<code>Class.class</code>&quot;,
                 &quot;<code>Object.class</code>&quot;,
                 &quot;<code>Method.class</code>&quot;,

                 &quot;<code>Array.class</code>&quot;,
                 &quot;<code>String.class</code>&quot;,
                 &quot;<code>System.class</code>&quot;,

                 &quot;<code>Boolean.class</code>&quot;,
                 &quot;<code>boolean.class</code>&quot; (primitive data type),
                 &quot;<code>Byte.class</code>&quot;,
                 &quot;<code>byte.class</code>&quot; (primitive data type),
                 &quot;<code>Character.class</code>&quot;,
                 &quot;<code>char.class</code>&quot; (primitive data type),
                 &quot;<code>Double.class</code>&quot;,
                 &quot;<code>double.class</code>&quot; (primitive data type),
                 &quot;<code>Integer.class</code>&quot;,
                 &quot;<code>int.class</code>&quot; (primitive data type),
                 &quot;<code>Long.class</code>&quot;,
                 &quot;<code>long.class</code>&quot; (primitive data type),
                 &quot;<code>Float.class</code>&quot;,
                 &quot;<code>float.class</code>&quot; (primitive data type),
                 &quot;<code>Short.class</code>&quot;,
                 &quot;<code>short.class</code>&quot; (primitive data type).

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>capacity 1</code>
        <td> Mandatory, integer indicating the number of elements in dimension 1.
             <p>
             <em>Alternatively,</em> a sole Java int-array containing the capacity per dimension.
             The first dimension is stored with index 0, the second with index 1, ...

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>capacity 2</code>
        <td> Integer indicating the number of elements in dimension 2.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> ... <code>capacity n </code>
        <td> Integer indicating the number of elements in dimension n.
     *
     *
     *


        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;createJavaArrayOf&quot; </code><br>
     *       <code>&quot;createArrayOf&quot; </code> (deprecated)
     *
        <td colspan="2">Creates a single dimensioned Java array of the given <code>componentType</code> and
     *                  adds the supplied arguments to it. The length (size) is determined by the
     *                  added arguments and can be <code>0</code>, if only the <code>componentType</code>
     *                  was given (an empty Java array object).
                        <br>Returns the created Java array object.

     *
        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>componentType</code>
        <td> Mandatory, name of a registered Java class object or a string fully qualifying a Java class.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>arg # 1</code>
        <td> Argument # 1 (if given must be of the given <code>componentType</code>).

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>... arg # n</code>
        <td> Argument # n (if given must be of the given <code>componentType</code>).





        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code> &quot;wrapArray&quot; </code>
        <td colspan="2">Wraps the given array up in a {@link ArrayWrapper}-object, allowing
                        easy access to information about the array like dimensions, size and
                        also getting and setting values in an easy manner.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>arrayObject </code>
        <td> Mandatory, name of a registered arrayObject (received by Java).




        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;exit&quot; </code>
        <td colspan="2">Terminates the Java Virtual Machine. Allows Rexx
                        to shut down the JVM (and thereby Rexx as well).
                        In order for Rexx being able to shutdown properly
                        itself, the termination of the JVM is delayed by
                        <code>time2wait</code>.

                        <br>Returns the string
                        <code>&quot;SHUTDOWN,&nbsp;REXX&nbsp;!&quot;</code>.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>retVal</code>
        <td> Optional integer return value. If omitted, then &quot;0&quot;
             will be used as argument for the <code>System.exit(n)</code> call.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>time2wait</code>
        <td> Optional number of milliseconds (msecs) to wait until the JVM will
             be terminated. This allows Rexx to shutdown (clean up) its side as
             well. Default value, if omitted: <code>100</code>, i.e. 100 msecs,
             i.e. 1/10th second.


        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;getBSFManager&quot; </code>
        <td colspan="2">Returns the BSFManager object that dispatches this
                        Rexx engine instance. This way one can get at the
                        version of BSF in use, but also one gains the ability
                        to dispatch scripts in any of the supported BSF languages
                        sharing the BSF registry with this Rexx engine instance!

     *
        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;getFieldValue&quot; </code>
        <td colspan="2">Looks up a <em>public</em> field of the registered
                        object. The case of the characters of the field name does not matter.
                        <br>
                        <em>Hint:</em> If the field cannot be found, then this subfunction assumes that
                        a getter method (field name prepended by &quot;get&quot;) exists for the field and tries to invoke one. If
                        you do not want this behaviour use subfunciton
                        <code>&quot;getFieldValueStrict&quot;</code> instead.


                        <br>Returns the value of the field.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Mandatory, name of the object to be looked up.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>fieldName </code>
        <td> Mandatory, name of the <em>public</em> field of the object.



        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;getFieldValueStrict&quot; </code>
        <td colspan="2">Looks up a <em>public</em> field of the registered
                        object. The case of the characters of the field name must match exactly.
                        <br>Returns the value of the field.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Mandatory, name of the object to be looked up.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>fieldName </code>
        <td> Mandatory, <em>case sensitive</em> name of the <em>public</em> field of the object.






        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;getPropertyValue&quot; </code> <strong>(untested!)</strong>
        <td colspan="2">Looks up a <em>property</em> of the registered
                        <em>Java Bean</em> object.
                        <br>Returns the value of the property.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Mandatory, name of the Java Bean object to be looked up.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>propertyName </code>
        <td> Mandatory, name of the Java Bean <em>property</em>.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>index </code>
        <td> Mandatory, <code>null</code>, if Java Bean property is not indexed,
             a valid integer number else.

     *

        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;getRexxAndJava&quot; </code>, synonym: <code>&quote;getRAJO&quote;</code>
        <td colspan="2">Returns the RexxAndJava object to which this Rexx program
     *                  interfaces.

        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;getRexxEngine&quot; </code>
        <td colspan="2">Returns the RexxEngine object, that is used to run the
     *                  Rexx program.

     *

        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;get_RII_ID&quot; </code>
        <td colspan="2">Returns the &quot;Rexx Interpreter Instance (RII) ID&quot; used to attach
     *                  to the corresponding Rexx interpreter instance


        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;getStaticValue&quot; </code>
        <td colspan="2">Looks up a <em>public static</em> field of the
                        given class or interface. This function does <em>not instantiate</em>
                        the class (would not be possible for abstract classes
                        or interfaces). Case of <code>fieldName</code> is <em>not</em> relevant!
                        <br>Returns the value of the public static field.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>className </code>
        <td> Mandatory, name of the class, abstract class or interface to be looked up.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>fieldName </code>
        <td> Mandatory, name of the <em>public static</em> field of the object.


        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;getStaticValueStrict&quot; </code>
        <td colspan="2">Looks up a <em>public static</em> field of the
                        given class or interface. This function does <em>not instantiate</em>
                        the class (would not be possible for abstract classes
                        or interfaces). Case of <code>fieldName</code> is relevant!
                        <br>Returns the value of the public static field.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>className </code>
        <td> Mandatory, name of the class, abstract class or interface to be looked up.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>fieldName </code>
        <td> Mandatory, <em>case sensitive</em> name of the <em>public static</em> field of the object.


        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;haltThread&quot; </code>
        <td colspan="2">Halts (terminates) the ooRexx thread with the given
                        <code>tid</code>. A <code>tid</code> value of <code>0</code>
                        will cause all ooRexx threads to be halted.

                        <br>Returns the return value of <code>RexxSetHalt(...)</code>:
                            <code>0</code> (o.k.), <code>1</code> (not found), and
                            <code>2</code> (failure).

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>tid </code>
        <td> Mandatory, the thread id as returned e.g. by <code>BsfGetTID()</code> or
              <code>0</code> to halt (terminate) all ooRexx threads.





        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;invoke&quot; </code>
        <td colspan="2">Invokes (calls) the method with the given arguments
                        on the bean.
                        <br>

                        <em>Hint:</em> if the method could not be found and has no arguments,
                        this subfunction assumes that the value of a field is sought. Therefore,
                        it employs the subfunction <code>&quot;getFieldValue&quot;</code> using
                        the <code>methodName</code> as the <code>fieldName</code>.

                        If you do not want to use this
                        feature, then employ the subfunction <code>&quot;invokeStrict&quot;</code> instead.

                        <br>Returns the result of the method invocation or
                            <code>null</code>.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Mandatory, name of a registered bean on which the method (function)
             should get invoked.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>methodName </code>
        <td> Mandatory, name of the method which should get invoked. <code>methodName</code> is <em>not</em>
             case sensitive!

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>arg # 1</code>
        <td> Argument # 1 (if given).

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>... arg # n</code>
        <td> Argument # n (if given).






        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;invokeStrict&quot; </code>
        <td colspan="2">Invokes (calls) the method with the given arguments
                        on the bean.
                        The case of the characters of the method name must match exactly.
                        Each Rexx argument <em>must</em> be preceeded by its Java type.

                        <br>Returns the result of the method invocation or
                            <code>null</code>.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Mandatory, name of a registered bean on which the method (function)
             should get invoked.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>methodName </code>
        <td> Mandatory, <em>case sensitive</em> name of the method which should get invoked.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>typeIndicator # 1</code>
        <td> Type indicator for argument # 1 (if given).

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>arg # 1</code>
        <td> The argument # 1 itself (if given).

        <tr class="TableRowColor"><!-- table row -->
        <td >
        <td colspan="2"> <em>Any additional arguments must be given in pairs of
                       &quot;typeIndicator&quot; and &quot;arg&quot; as well.</em>




        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;isA&quot; </code>
        <td colspan="2">Tests whether <code>javaObject</code> is an instance
                        of <code>javaClass</code> (equivalent to Java's <code>instanceof</code> operator).

                        <br>returns <code>.true</code> (<code>1</code>) or
                                    <code>.false</code> (<code>0</code>), respectively

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>javaObject </code>
        <td> Mandatory, Java object to test.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>javaClass </code>
        <td> Mandatory, Java class object or a string fully denoting a Java class.



        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;loadClass&quot; </code>
        <td colspan="2">Loads a Java class with the given name.
            <br>Returns the object or <code>null</code>, if not found.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Mandatory, fully qualified name of the Java class to be looked.
             Returns the class object rendered to a string or <code>null</code>, if not found.




        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;lookupBean&quot; </code>
        <td colspan="2">Looks up an object in the BSF
            manager object registry. (Allows for retrieving pre-<em>declared
            beans</em> as well.)
            <br>Returns the object or <code>null</code>, if not found.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Mandatory, name of the object to be looked up. Returns
             the object rendered to a string or <code>null</code>, if not found.



        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;new&quot;</code> or<br>
             <code>&quot;registerBean&quot;</code> (a synonym)
        <td colspan="2">Creates a new object (instantiating the given Java class
                        with the given arguments) and registers it using the
                        given <em>beanName</em>.
                        <br>Returns <em>beanName</em>
                        (always use this returned beanName for looking up the
                        object in the registry).


        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Name of the object to be created and registered.
             if no name is given (empty string or <code>null</code>)
             then a unique one will be created and returned;
             this is done in method {@link #makeBeanName(Object o)}.


        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanType</code>
        <td> Mandatory, type (Java class name) of the bean (Java object) to be
             created and registered.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>arg # 1</code>
        <td> The argument # 1 (if given).

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>... arg # n</code>
        <td> The argument # n (if given).





        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;newStrict&quot; </code>  or<br>
             <code>&quot;registerBeanStrict&quot;</code> (a synonym)
        <td colspan="2">Creates a new object (instantiating the given Java class
                        with the given arguments) and registers it using the
                        given <em>beanName</em>.
                        <br>Returns <em>beanName</em>
                        (always use this returned beanName for looking up the
                        object in the registry).


        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Name of the object to be created and registered.
             if no name is given (empty string or <code>null</code>)
             then a unique one will be created and returned;
             this is done in method {@link #makeBeanName(Object o)}.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanType</code>
        <td> Mandatory, type (Java class name) of the bean (Java object) to be
             created and registered.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>typeIndicator # 1</code>
        <td> Type indicator for argument # 1 (if given).

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>arg # 1</code>
        <td> The argument # 1 (if given).

        <tr class="TableRowColor"><!-- table row -->
        <td >
        <td colspan="2"> <em>Any additional arguments must be given in pairs of
                       &quot;typeIndicator&quot; and &quot;arg&quot; as well.</em>







        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;pollEventText&quot; </code>
        <td colspan="2">Polls for the eventText defined in the above subfunction
                        &quot;<code>addEventListener</code>&quot; placed
                        into a queue by the appropriate event listeners,
                        or posted via &quot;<code>postEventText</code>&quot; below.
                        <br>

                        If no timeout value is supplied, &quot;0&quot;
                        will be used by default, which blocks the return of this function
                        (forever) until some text is available for return.

                        <br>Returns available <code>eventText</code>, or if a
                            timeout occurred while waiting <code>null</code>.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>timeOut </code>
        <td> Optional, timeout in milliseconds, i.e. time to wait for
             an <code>eventText</code> to appear, before returning.






        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code> &quot;postEventText&quot; </code>
        <td colspan="2">Adds <code>eventText</code> to the <code>eventText</code> vector (list),
                        can be retrieved via <code>pollEventText</code>.
                       <br>Returns the received <code>eventText</code>

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>eventText </code>
        <td> Mandatory, text which should be put into the <code>eventText</code> vector.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>priority </code>
        <td> Optional, priority level of <code>eventText</code>, can be one of:
          *        <p><ul>
                   <li>&quot;<code>2</code>&quot;: high ('alarm level') priority.
                   <li>&quot;<code>1</code>&quot;: normal (<strong>default</strong>) priority, and
                   <li>&quot;<code>0</code>&quot;: low ('batch level') priority,
                   </ul>



        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;rawRegisterBean&quot; </code> or<br>
             <code>&quot;new&quot;</code> (a synonym)
        <td colspan="2">Uses the supplied <em>beanName</em> to store the supplied
                        <em>object</em> in the BSFRegistry.

        Creates a new object (instantiating the given Java class
                        with the given arguments) and registers it using the
                        given <em>beanName</em>.
                        <br>Returns <em>beanName</em>
                        (always use this returned beanName for looking up the
                        object in the registry).


        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Name of the object to be created and registered.
             if no name is given (empty string or <code>null</code>)
             then a unique one will be created and returned;
             this is done in method {@link #makeBeanName(Object o)}.


        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanType</code>
        <td> Mandatory, type (Java class name) of the bean (Java object) to be
             created and registered.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>arg # 1</code>
        <td> The argument # 1 (if given).

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>... arg # n</code>
        <td> The argument # n (if given).






        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;setFieldValue&quot; </code>
        <td colspan="2">Looks up a <em>public</em> field of the registered
                        object and sets it to the argument's value.
                        <br>

                        <em>Hint:</em> if the field could not be found, this subfunction
                        automatically attempts to employ a setter method (<code>fieldName</code>
                        prepended with the string &quot;set&quot;). If you do not want to use this
                        feature, then employ the subfunction <code>&quot;setFieldValueStrict&quot;</code> instead.

                        <br>Returns the new value of the field.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Mandatory, name of the object to be looked up.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>fieldName </code>
        <td> Mandatory, name of the <em>public</em> field of the object.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>newValue </code>
        <td> The new value the field gets set to.




        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;setFieldValueStrict&quot; </code>
        <td colspan="2">Looks up a <em>public</em> field of the registered
                        object and sets it to the argument's value.
                        <br>Returns the new value of the field.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Mandatory, name of the object to be looked up.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>fieldName </code>
        <td> Mandatory, <em>case sensitive</em> name of the <em>public</em> field of the object.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>typeIndicator </code>
        <td> <em>optional</em> type indicator for the immediately following argument
             (<code>newValue</code>) or <code>newValue</code>.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>newValue </code>
        <td> The new value the field gets set to. (If this argument is given, then the
             previous argument is interpreted as a <code>typeIndicator </code>.)






        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;setPropertyValue&quot; </code> <strong>(untested!)</strong>
        <td colspan="2">Looks up a <em>property</em> of the registered
                        <em>Java Bean</em> object and sets it to the argument's value.
                        <br>Returns the new value of the property.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Mandatory, name of the Java Bean object to be looked up.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>propertyName </code>
        <td> Mandatory, name of the Java Bean <em>property</em>.


        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>index </code>
        <td> Mandatory, <code>null</code>, if Java Bean property is not indexed,
             a valid integer number else.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>newValue </code>
        <td> The new value the property gets set to.






        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;setPropertyValueStrict&quot; </code> <strong>(untested!)</strong>
        <td colspan="2">Looks up a <em>property</em> of the registered
                        <em>Java Bean</em> object and sets it to the argument's value.
                        <br>Returns the new value of the property.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Mandatory, name of the Java Bean object to be looked up.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>propertyName </code>
        <td> Mandatory, name of the Java Bean <em>property</em>.


        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>index </code>
        <td> Mandatory, <code>null</code>, if Java Bean property is not indexed,
             a valid integer number else.

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>typeIndicator </code>
        <td> Type indicator for the immediately following argument
             (<code>newValue</code>).

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>newValue </code>
        <td> The new value the property gets set to.






        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;setRexxNullString&quot;</code>
        <td colspan="2">Sets the field {@link #null4Rexx} to the string which serves
                        as the representation for <code>null</code>.
                        <ul>
                        <li>The Java side will return this value to Rexx to indicate a Java
                            <code>null</code> (&quot;no value&quot;). This conversion is
                            done in method {@link #makeString4Rexx(Object o)}.

                        <li>The Rexx side can use this string for
                            an argument for which no value is supplied.
                            This option is available in addition to the standard Rexx option
                            of just leaving the missing argument empty. The conversion of
                            Rexx arguments to Java is done in
                            {@link #convFromRexx(String type, String value)}.
                        </ul>


        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>newString </code>
        <td> Mandatory, the new string to be used to indicate a Java
            <code>null</code>.





        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;sleep&quot;</code>

        <td colspan="2">Sleeps <code>time2sleep</code> seconds before returning.


        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code> time2sleep </code>
        <td align="left"> Mandatory, indicates seconds to sleep before returning. <code>time2sleep</code>.
                        The sleeping occurs with an attempted resolution of milliseconds,
                        being calculated like:
                        <code> (long) ((double)time2sleep*1000+0.5d)</code>, e.g. <br>
                   <ul>
                   <li><code>3</code> (three seconds, 3000 msec),
                   <li><code>600</code> (600 seconds, i.e. 10 minutes, 600000 msec),
                   <li><code>0.5</code> (5/10 second, i.e. half a second, 500 msec),
                   <li><code>0.01</code> (1/100 second, 10 msec),
                   <li><code>0.001</code> (1/1000 second,  1 msec),
                   <li><code>0.0005</code> (rounded up, ie. 1/1000 second, 1 msec),
                   <li><code>0.0004</code> (rounded down, ie. 0/1000 second, 0 msec).
                   </ul>

                   <p><em>Hint:</em> If available, use Rexx' <code>SysSleep()</code> function
                   instead (it is usually more exact).




        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;unregisterBean&quot; </code>
        <td colspan="2">Unregisters an object from the BSF manager's
                        object registry.
                        <br>Returns the former beanName of the just unregistered object.
     *                  <br>If the object could not be unregistered, because the reference
     *                      counter did not drop to 0, then <code>.NIL</code> is returned.


        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>beanName </code>
        <td> Mandatory, name of bean (Java object) to be unregistered.



        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code>&quot;version&quot; </code>
        <td colspan="2">Returns the version of this program in the form of
              &quot;<code>version.YYYYMMMDDD package-name-of-engine info</code>&quot;, e.g.
              &quot;<code>272.20061210 org.rexxla.bsf.engines.rexx (org.apache.bsf)</code>&quot;
              for Apache's BSF, if no argument is given. Otherwise it returns the
              version of <code>BSFManager</code> or of the indicated class in
              the <code>org.rexxla.bsf.engines.rexx</code> package.


        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>classIndicator</code>
        <td> Optional. If no argument is given the version of this program is returned in
     *       the long format: &quot;<code>version.YYYYMMMDDD package-name-of-engine info</code>&quot;.
     *       <br>
     *       In the following list of arguments only the first uppercase characters need to
     *       be given:

     *       <table>
     *       <tr><td>&quot;<code>ALl<code>&quot;
     *           <td>Returns a blank delimited string with pairs of fully qualified class names
     *               and their version number in the form of <code>version.YYYYMMDD</code>.

     *       <tr><td>&quot;<code>ARrayWrapper<code>&quot;
     *           <td>Returns the version number of the <code>org.rexxla.bsf.engines.rexx.ArrayWrapper</code> class.

     *       <tr><td>&quot;<code>BSFManager<code>&quot;
     *           <td>Returns the version number of the <code>org.apache.bsf.BSFManager</code> class.

     *       <tr><td>&quot;<code>EnumerationWrapper<code>&quot;
     *           <td>Returns the version number of the <code>org.rexxla.bsf.engines.rexx.EnumerationWrapper</code> class.

     *       <tr><td>&quot;<code>Java4Rexx<code>&quot;
     *           <td>Returns the version number of the <code>org.rexxla.bsf.engines.rexx.Java4Rexx</code> class.

     *       <tr><td>&quot;<code>REXXAndJava<code>&quot;
     *           <td>Returns the version number of the <code>org.rexxla.bsf.engines.rexx.RexxAndJava</code> class.

     *       <tr><td>&quot;<code>REXXEngine<code>&quot;
     *           <td>Returns the version number of the <code>org.rexxla.bsf.engines.rexx.RexxEngine</code> class.

     *       <tr><td>&quot;<code>Supplier<code>&quot;
     *           <td>Returns the version number of the <code>org.rexxla.bsf.engines.rexx.Supplier</code> class.

     *       </table>




        <!-- -------------------------------------------------------- -->
        <tr class="TableHeadingColor"><!-- table row -->
        <td> <code> &quot;wrapEnumeration&quot; </code>
        <td colspan="2">Wraps the given object up in a {@link EnumerationWrapper}-object, allowing
                        Java 1.1 programs to use the methods of the Enumeration interface
                        (the problem: Java 1.1 raises an IllegalAccessException, if accessing
                        methods of inner classes; starting with 1.2 one can come by this bug
                        under program control).

        <tr class="TableRowColor"><!-- table row -->
        <td>
        <td align="right"> <code>enumerationObject </code>
        <td> Mandatory, name of a registered enumerationObject (received by Java).





        </table>
        <p>
      *
      *  @return A Rexx string or <code>null</code>.
      *
      */

    public String javaCallBSF (String args[])  throws BSFException
    {

/* // rgf, 2016-11-30: even if a Rexx engine got terminated it may be the case that UNINITs cause this method
   //                  to be invoked; as unregisterBean does not use the Rexx engine, but merely reduces the
   //                  refCounter for the Java object in the registry, we allow for "unregisterBean" operation
   //                  to proceed

*/

        InvocationType choice=getInvocationType(args[0]);   // get invocation type

        // RexxAndJava got terminated, cannot use it anymore, do not serve it anymore
        if (bTerminated==true &&
              ( args==null ? true :              // args is null: allow ignoring this invocation
                    ( args.length==0 ? true :    // no args:  allow ignoring this invocation
                      ( ! (choice==UNREGISTER_BEAN) )     // if not "unregisterBean": allow ignoring this invocaton
        //              ( ! args[0].equalsIgnoreCase( STRING_UNREGISTER_BEAN))     // if not "unregisterBean": allow ignoring this invocaton
                    )
               )
            )
        {
            throw new RexxException("RexxAndJava "+this+" got terminated, cannot be used anymore.");
        }

        {
            final boolean bDebug= // true;  // if true, show debug infos
                                  false;
            if (bDebug)
            {
                System.err.print(this+".javaCallBSF(...)"
                                   +"/rii_ID=["+this.rii_ID+"]"
                                   +", mgr=["+mgr+"]"
                                   +                 ", args[0]=["+args[0]+"]"
                                   +(args.length>1 ? ", args[1]=["+args[1]+"]" : "")
                                   +(args.length>2 ? ", args[2]=["+args[2]+"]" : "")
                                   +(args.length>3 ? ", args[3]=["+args[3]+"]" : "")
                                   +(args.length>4 ? ", args[4]=["+args[4]+"]" : "")
                                   +(args.length>5 ? ", args[5]=["+args[5]+"]" : "")
                                   +(args.length>6 ? ", args[6]=["+args[6]+"]" : "")
                                   );
                System.err.print("\n\t---> RexxAndJava.javaCallBSF(), args[0]=["+args[0]+"], nr args=["+args.length+"]: ");
                for (int i=1; i<args.length;i++)
                {
                    System.err.print(" "+i+"=["+args[i]+"]");
                }
                System.err.println();
            }
        }

       // determine which function to invoke

       boolean bStrongTyping=false; // set to true, if Java types preceed the individual Rexx arguments



/* ---->  rgf, 20180108
       int choice = 0;

            if (args[0].equalsIgnoreCase( STRING_INVOKE                         )) choice = INVOKE                       ;
       else if (args[0].equalsIgnoreCase( STRING_INVOKE_JSO                     )) choice = INVOKE_JSO                   ; // rgf, 2010-10-02
       else if (args[0].equalsIgnoreCase( STRING_INVOKE_STRICT                  )) choice = INVOKE_STRICT                ;
       else if (args[0].equalsIgnoreCase( STRING_INVOKE_STRICT_JSO              )) choice = INVOKE_STRICT_JSO            ; // rgf, 2014-10-04

       else if (args[0].equalsIgnoreCase( STRING_REGISTER_BEAN                  )) choice = REGISTER_BEAN                ;
       else if (args[0].equalsIgnoreCase( STRING_NEW                            )) choice = NEW                          ;
       else if (args[0].equalsIgnoreCase( STRING_REGISTER_BEAN_STRICT           )) choice = REGISTER_BEAN_STRICT         ;
       else if (args[0].equalsIgnoreCase( STRING_NEW_STRICT                     )) choice = NEW_STRICT                   ;



       else if (args[0].equalsIgnoreCase( STRING_LOOKUP_BEAN                    )) choice = LOOKUP_BEAN                  ;
       else if (args[0].equalsIgnoreCase( STRING_UNREGISTER_BEAN                )) choice = UNREGISTER_BEAN              ;

       else if (args[0].equalsIgnoreCase( STRING_ADD_EVENT_LISTENER             )) choice = ADD_EVENT_LISTENER           ;
//       else if (args[0].equalsIgnoreCase( STRING_ADD_EVENT_LISTENER_2           )) choice = ADD_EVENT_LISTENER_2         ;
       else if (args[0].equalsIgnoreCase( STRING_POLL_EVENT_TEXT                )) choice = POLL_EVENT_TEXT              ;
       else if (args[0].equalsIgnoreCase( STRING_POST_EVENT_TEXT                )) choice = POST_EVENT_TEXT              ;

       else if (args[0].equalsIgnoreCase( STRING_GET_FIELD_VALUE                )) choice = GET_FIELD_VALUE              ;
       else if (args[0].equalsIgnoreCase( STRING_GET_FIELD_VALUE_JSO            )) choice = GET_FIELD_VALUE_JSO          ; // rgf, 2014-10-04
       else if (args[0].equalsIgnoreCase( STRING_GET_FIELD_VALUE_STRICT         )) choice = GET_FIELD_VALUE_STRICT       ; // rgf, 2005-05-06
       else if (args[0].equalsIgnoreCase( STRING_GET_FIELD_VALUE_STRICT_JSO     )) choice = GET_FIELD_VALUE_STRICT_JSO   ; // rgf, 2014-10-04
       else if (args[0].equalsIgnoreCase( STRING_GET_STATIC_VALUE               )) choice = GET_STATIC_VALUE             ;
       else if (args[0].equalsIgnoreCase( STRING_GET_STATIC_VALUE_JSO           )) choice = GET_STATIC_VALUE_JSO         ; // rgf, 2014-10-04
       else if (args[0].equalsIgnoreCase( STRING_GET_STATIC_VALUE_STRICT        )) choice = GET_STATIC_VALUE_STRICT      ;
       else if (args[0].equalsIgnoreCase( STRING_GET_STATIC_VALUE_STRICT_JSO    )) choice = GET_STATIC_VALUE_STRICT_JSO  ; // rgf, 2014-10-04

       else if (args[0].equalsIgnoreCase( STRING_EXIT                           )) choice = EXIT                         ;

       else if (args[0].equalsIgnoreCase( STRING_SET_FIELD_VALUE                )) choice = SET_FIELD_VALUE              ;
       else if (args[0].equalsIgnoreCase( STRING_SET_FIELD_VALUE_STRICT         )) choice = SET_FIELD_VALUE_STRICT       ;

       else if (args[0].equalsIgnoreCase( STRING_ARRAY_AT                       )) choice = ARRAY_AT                     ;
       else if (args[0].equalsIgnoreCase( STRING_ARRAY_LENGTH                   )) choice = ARRAY_LENGTH                 ;
       else if (args[0].equalsIgnoreCase( STRING_ARRAY_PUT                      )) choice = ARRAY_PUT                    ;
       else if (args[0].equalsIgnoreCase( STRING_ARRAY_PUT_STRICT               )) choice = ARRAY_PUT_STRICT             ;
       else if (args[0].equalsIgnoreCase( STRING_CREATE_ARRAY_OBJECT_OF         )) choice = CREATE_ARRAY_OBJECT_OF       ; // 20090730, ---rgf
       else if (args[0].equalsIgnoreCase( STRING_CREATE_JAVA_ARRAY_OBJECT_OF    )) choice = CREATE_ARRAY_OBJECT_OF       ; // 20090910, ---rgf, a synonym
       else if (args[0].equalsIgnoreCase( STRING_CREATE_ARRAY_OBJECT            )) choice = CREATE_ARRAY_OBJECT          ;
       else if (args[0].equalsIgnoreCase( STRING_CREATE_JAVA_ARRAY_OBJECT       )) choice = CREATE_ARRAY_OBJECT          ; // 20090910, ---rgf, a synonym

       else if (args[0].equalsIgnoreCase( STRING_GET_PROPERTY_VALUE             )) choice = GET_PROPERTY_VALUE           ;
       else if (args[0].equalsIgnoreCase( STRING_GET_PROPERTY_VALUE_JSO         )) choice = GET_PROPERTY_VALUE_JSO       ; // 20141004, ---rgf
       else if (args[0].equalsIgnoreCase( STRING_SET_PROPERTY_VALUE             )) choice = SET_PROPERTY_VALUE_STRICT    ;
       else if (args[0].equalsIgnoreCase( STRING_SET_PROPERTY_VALUE_STRICT      )) choice = SET_PROPERTY_VALUE_STRICT    ;

       else if (args[0].equalsIgnoreCase( STRING_SET_REXX_NULL_STRING           )) choice = SET_REXX_NULL_STRING         ;
       else if (args[0].equalsIgnoreCase( STRING_SLEEP                          )) choice = SLEEP                        ;

       else if (args[0].equalsIgnoreCase( STRING_WRAP_ARRAY_OBJECT              )) choice = WRAP_ARRAY_OBJECT            ;
       else if (args[0].equalsIgnoreCase( STRING_WRAP_ENUMERATION_OBJECT        )) choice = WRAP_ENUMERATION_OBJECT      ;

       else if (args[0].equalsIgnoreCase( STRING_GET_VERSION                    )) choice = GET_VERSION                  ;

       else if (args[0].equalsIgnoreCase( STRING_LOAD_CLASS                     )) choice = LOAD_CLASS                   ;
       else if (args[0].equalsIgnoreCase( STRING_GET_BSF_MANAGER                )) choice = GET_BSF_MANAGER              ;
       else if (args[0].equalsIgnoreCase( STRING_GET_REXX_AND_JAVA              )) choice = GET_REXX_AND_JAVA            ;
       else if (args[0].equalsIgnoreCase( STRING_GET_RAJO                       )) choice = GET_REXX_AND_JAVA            ;  // 2009-10-12, rgf
       else if (args[0].equalsIgnoreCase( STRING_GET_REXX_ENGINE                )) choice = GET_REXX_ENGINE              ;

       else if (args[0].equalsIgnoreCase( STRING_BSF_PREFIX_RETURN_VALUE        )) choice = BSF_PREFIX_RETURN_VALUE      ;

       else if (args[0].equalsIgnoreCase( STRING_RAW_REGISTER_BEAN              )) choice = RAW_REGISTER_BEAN            ;  // 20080828, rgf

       else if (args[0].equalsIgnoreCase( STRING_REXX_HALT                      )) choice = REXX_HALT                    ;  // 20090402, rgf

       else if (args[0].equalsIgnoreCase( STRING_KUDOS                          )) choice = KUDOS                        ;  // 20091017, rgf

       else if (args[0].equalsIgnoreCase( STRING_IS_A                           )) choice = IS_INSTANCE                  ;  // 20100220, rgf
//       else if (args[0].equalsIgnoreCase( STRING_IS_INSTANCE                    )) choice = IS_INSTANCE                  ;  // 20100220, rgf

       else if (args[0].equalsIgnoreCase( STRING_CARRY_OUT_SYSTEM_EXIT          )) choice = CARRY_OUT_SYSTEM_EXIT        ;  // 20110223, rgf

       else if (args[0].equalsIgnoreCase( STRING_ENV_GET                        )) choice = ENV_GET                      ;  // 20140329, rgf
       else if (args[0].equalsIgnoreCase( STRING_ENV_SET                        )) choice = ENV_SET                      ;  // 20140329, rgf

       else if (args[0].equalsIgnoreCase( STRING_GET_RII_ID                     )) choice = GET_RII_ID                   ;  // 20150801, rgf
       else if (args[0].equalsIgnoreCase( STRING_INCREASE_REF_COUNTER           )) choice = INCREASE_REF_COUNTER         ;  // 20150804, rgf

---> */

        // ==================================================================
        // ==================================================================

       switch (choice)
       {
          case ADD_EVENT_LISTENER :
                  {
                  /* ********************************************************
                   * call BSF 'addEventListener', beanName, eventSetName, filter, script
                   *
                   * - beanName: the bean that fires the event
                   * - eventSetName: the set the event belongs to
                   * - filter: the actual event
                   * - script: the script to be run, if the event fires
                   *********************************************************/
                      if(args.length!=5)
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" invalid # of args; usage: call BSF " +
                              "'"+args[0]+"', beanName, eventSetName, filter, script)");
                      }

                      Object bean = null;
                      bean=lookupBean(this, args[1]);

                      if(bean==null)
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": object '"+args[1]+"' not registered");
                      }

                      args[3] = args[3].equals("") ? null : args[3];
                      try
                      {
// RgfMsgBox("BSF4ooRexx subfunction "+'"'+args[0]+'"'+" before invoking EngineUtils.addEventListener(...).",'1');
                          EngineUtils.addEventListener(bean, args[2], args[3],
                                                       rexxEngine, mgr, EVENT_TEXT,
                                                       0, 0, args[4]);
                          // return "1";   // return .true
// RgfMsgBox("BSF4ooRexx subfunction "+'"'+args[0]+'"'+" everything o.k., about to return .true.", '1');
                          return makeString4Rexx(this, "1");   // return .true
                      }
                      catch (BSFException e)
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   // "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": could not add 'eventText', got BSF exception: [" + e.getMessage ()+"]");
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": could not add 'eventText', got exception: [" + e.toString ()+"]");
                      }
                  }
        // ==================================================================


    // ==================================================================
       case CREATE_ARRAY_OBJECT_OF:     // rgf, 2009-07-30
           /*******^**************************************************
            * value = BSF( 'createArrayOf', componentType, [val1] ...)
            *
            * - componentType: class object or a string with a fully qualified Java class name
            * - val1 ...: optional additional argument(s), each representing a value of componentType;
            *             if no values are given, then an array object will get created of size 0!
             *********************************************************/
           {
               if (args.length<2)
               {
                   throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                      "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": too few arguments, you must supply at least the \"componentType\" argument!");
               }

               Object o_componentType=null;
               try
               {
                   o_componentType=convFromRexx("O", args[1]);
               }
               catch (BSFException be)      // bean not registered exception ?
               {
                   // 2009-06-25, rgf: if argument is a string, then try to dynamically load the Java class and use that instead
                   try
                   {
                       o_componentType=EngineUtils.loadClass(mgr, args[1]);
                       if (o_componentType==null)   // zut alors! (not found, hence throw appropriate exception
                       {
                           throw (new ClassNotFoundException (args[1]));
                       }
                   }
                   catch (Exception e)
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                          "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" componentType ["+args[1]+"]"+
                          " is neither an instance of 'java.lang.Class', nor could a Java class by that name be dynamically loaded!"
                          , e  // rgf, 2011-06-04: supply Exception object
                          );
                   }
               }

               // ---------------->
               // throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
               //     "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" componentType ["+args[1]+"] is not an instance of 'Class'!");


               Class componentType=(Class) o_componentType;
               int   length=args.length-2;  // caclulate number of array elements
               Object arr=Array.newInstance( componentType, length); // create the array object of the desired length

               // determine number of element, create array object of given type, then assign the values to the array object
               for (int i=2; i<args.length; i++)    // iterate over arguments, convert and save them ...
               {
                   try
                   {
                       Object o_nv=convFromRexx(args[i]);

                       if (o_nv!=null && componentType.isPrimitive() )  // if not null, determine component type of array
                       {
                           TypeConvertor tc = glob_tcr.lookup(String.class, componentType);
                           o_nv=tc.convert(String.class, componentType, o_nv);
                       }

                       // assign value to array; if wrapper type and componentType is primitive, unwrapping/unboxing occurs automatically by Array.set(...)
                       Array.set(arr, i-2, o_nv);
                   }
                   catch (Exception e)
                   {
                       Throwable t=e.getCause();
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                              "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": processing argument with index # "+i+
                              " (value=\""+args[i]+"\") caused an exception"+
                              ( t==null ? "" : "(\""+t+"\")" )+
                              ", must be of componentType \""+componentType+"\""
                               , e  // rgf, 2011-06-04: supply Exception object
                          );
                   }
               }

               return makeString4Rexx(this, arr);     // return the single dimensioned array object
           }


     // ==================================================================

       case CREATE_ARRAY_OBJECT:
           /*******^**************************************************
            * value = BSF( 'createArray', componentType, idx1 [, idx2 [, idx3 [, idx4 [, idx5]...]]] )
            *
            * - componentType: class object or a string with a fully qualified Java class name
            * - idx1 ... idx5: maximum entries per indicated dimension (idx1...idx5...)
            *   alternatively: idx1 can be a Java array of int
             *********************************************************/
           {
               Object o_componentType=null;

               try
               {
                   o_componentType=convFromRexx("O", args[1]);
               }
               catch (BSFException be)      // bean not registered exception ?
               {}

               if (! (o_componentType instanceof Class))    // not a class object in hand ?
               {
                   // 2009-06-25, rgf: if argument is a string, then try to dynamically load the Java class and use that instead
                   try
                   {
                       o_componentType=EngineUtils.loadClass(mgr, args[1]);
                       if (o_componentType==null)   // zut alors! (not found, hence throw appropriate exception
                       {
                           throw (new ClassNotFoundException (args[1]));
                       }
                   }
                   catch (Exception e)
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                          "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" componentType ["+args[1]+"]"+
                          " is neither an instance of 'java.lang.Class', nor could a Java class by that name be dynamically loaded!"
                          , e  // rgf, 2011-06-04: supply Exception object
                          );
                   }

                   // ---------------->
                   // throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                   //     "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" componentType ["+args[1]+"] is not an instance of 'Class'!");
               }

               Class componentType=(Class) o_componentType;

               int [] indices=null;     // define a variable to hold an int-array for the indices
               int firstIdx=2;

               if (args[firstIdx].charAt(0) == '[')         // an array in hand !
               {
                   Object o_idx=convFromRexx("O", args[firstIdx]);

                   // ArrayWrapper aw=ArrayWrapper.getArrayWrapper(o_idx);  // get
                   // rgf, 2009-09-10
                   ArrayWrapper aw=null;

                   try {
                       aw=new ArrayWrapper(o_idx);  // get
                   }
                   catch (ClassNotFoundException cnfe)
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                            "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" index array object, class object of 'componentType' not found!", cnfe);
                   }


                   if (aw.componentType != int.class)       // check array of indices for correct class
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                            "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" index array object ["+args[firstIdx]+"] not of type 'int'!");
                   }
                   indices=(int []) o_idx;                  // o.k., assign to int-array
               }
               else // individual indices, create an array of int's for it
               {
                   indices=new int [ args.length-firstIdx ];  // calculate & reserve needed array-size
                   int i=0, j=firstIdx;
                   for ( ; j<args.length; i++, j++)
                   {
                       indices[i]=Integer.parseInt(args[j]);
                   }
               }

               Object arr=Array.newInstance( componentType, indices); // create the array object
               return makeString4Rexx(this, arr);     // return the array object
           }


       // ==================================================================

       case ARRAY_AT:
          /*********************************************************
           * value = BSF( 'arrayAt', arrayObject, idx1 [, idx2 [, idx3 [, idx4 [, idx5]...]]] )
           *
           * - arrayObject:   array to be worked upon
           * - idx1 ... idx5 ...: index values for arrays
           *   alternatively: idx1 can be a Java array of int
            *********************************************************/
       case ARRAY_PUT:
           /*********************************************************
            * value = BSF( 'arrayPut', arrayObject, "newValue", idx1 [, idx2 [, idx3 [, idx4 [, idx5]...]]] )
            *
            * - arrayObject:   array to be worked upon
            * - newValue:      new value to set array element to
            * - idx1 ... idx5 ...: index values for arrays
            *   alternatively: idx1 can be a Java array of int
            *********************************************************/

       case ARRAY_PUT_STRICT :
           /*********************************************************
            * value = BSF( 'arrayPut', arrayObject, "argType", "newValue", idx1 [, idx2 [, idx3 [, idx4 [, idx5]...]]] )
            *
            * - arrayObject:   array to be worked upon
            * - argType:       type of "newValue"
            * - newValue:      new value to set array element to
            * - idx1 ... idx5 ...: index values for arrays
            *   alternatively: idx1 can be a Java array of int
            *********************************************************/

       case WRAP_ARRAY_OBJECT:
           /*********************************************************
            * value = BSF( 'arrayWrap', arrayObject)
            *
            * - arrayObject:   array to be worked upon
            *********************************************************/

           {
               final boolean bDebug= // true;    // if true, show debug infos
                                     false;

               if (args[1].charAt(0) != '[')    // not an array object!
               {
                   throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                            "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" array object ["+args[1]+"] is not an array!");
               }

if (bDebug)
{
    System.err.println(this+": *ARRAY*:  args[0]=["+args[0]+"]");
    for (int i=0; i<args.length; i++)
    {
        System.err.println("\targs["+i+"]=["+args[i]+"]");
    }

}


               Object arrayObj = null;
               arrayObj=lookupBean(this, args[1]);

               if (arrayObj == null)
               {
                   throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                            "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" array object ["+args[1]+"] not registered!");
               }


               if (choice==WRAP_ARRAY_OBJECT)     // create and return the ArrayWrapper
               {
                   // return makeString4Rexx( new ArrayWrapper(arrayObj) ); // get the element and return it to Rexx

                   // rgf, 2009-09-10
                   ArrayWrapper aw=null;

                   try {
                       aw=new ArrayWrapper(arrayObj);  // get
                   }
                   catch (ClassNotFoundException cnfe)
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                            "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" class object of 'componentType' not found!", cnfe);
                   }
                   return makeString4Rexx(this, aw);  // return ArrayWrapper instance
               }


               if (args.length< (choice==ARRAY_AT ? 3 : 5) )   // not enough arguments?
               {
                   String errMsg = "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" invalid # of args; usage: value = BSF( " +
                                    "'"+args[0]+"', arrayObject,"+
                                                   (choice==ARRAY_AT ? "" : " argType, newValue,") +
                                    " indexArray of int |or comma-separated indices| idx1 [, idx2]...";
               }

               Object o=null, o_nv=null;

               // ArrayWrapper aw=ArrayWrapper.getArrayWrapper(arrayObj); // get wrapper, so we know which component type it has
               // ArrayWrapper aw=new ArrayWrapper(arrayObj); // get wrapper, so we know which component type it has
               // rgf, 2009-09-10
               ArrayWrapper aw=null;

               try {
                   aw=new ArrayWrapper(arrayObj);  // get
               }
               catch (ClassNotFoundException cnfe)
               {
                   throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                        "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" class object of 'componentType' not found!", cnfe);
               }

               int firstIdx=2;          // first argument with index-information, default to arrayAt()

               if (choice==ARRAY_PUT_STRICT)
               {
                   o_nv=convFromRexx( args[2], args[3] );     // get the Java object
if (bDebug) System.err.println("ARRAY_PUT_STRICT:  args[2]=["+args[2]+"], "+"args[3]=["+args[3]+"]: o_nv=["+o_nv+"]");
                   firstIdx=4;          // first argument with index-information for arrayPut()
               }
               else if (choice==ARRAY_PUT)      // convert Rexx argument to be stored in array to the appropriate type
               {
                   o_nv=convFromRexx(args[2]);  // get Java null or Java object from BSF registry, String else

                   Class cl=arrayObj.getClass(),
                         ct=aw.componentType;
                   // rgf, 2018-01-01: only use a type convertor if convFromRexx returned a string
                   if (o_nv!=null && o_nv instanceof String && ct.isPrimitive() )      // if not null, determine component type of array
                   {
                       // rgf, 20180104: special case Boolean values: behave like SET_FIELD, where only
                       if ( (ct==boolean.class || ct==Boolean.class) && ((String) o_nv).equals("1") )
                       {
                           o_nv=Boolean.TRUE;
                       }
                       else     // use corresponding type convertor
                       {
                           TypeConvertor tc = glob_tcr.lookup(String.class, ct);
                           o_nv=tc.convert(String.class, ct, o_nv);
                       }

                   }

                   firstIdx=3;          // first argument with index-information for arrayPut()
               }

                    // check index-information: was an array passed in or individual index-values ?
               int [] indices=null;     // define a variable to hold an int-array for the indices

               if (args[firstIdx].charAt(0) == '[')         // an array in hand !
               {
                   Object o_idx=convFromRexx("O", args[firstIdx]);

                    // get and test component type
                   // if (ArrayWrapper.getArrayWrapper(o_idx).componentType != int.class)       // check array of indices for correct class

                   // rgf, 2009-09-10
                   ArrayWrapper aw2=null;

                   try {
                       aw2=new ArrayWrapper(o_idx);  // get
                   }
                   catch (ClassNotFoundException cnfe)
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                            "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" class object of 'componentType' not found!", cnfe);
                   }

                   // if (new ArrayWrapper(o_idx).componentType != int.class)       // check array of indices for correct class
                   if (aw2.componentType != int.class)       // check array of indices for correct class
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                            "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" index array object ["+args[firstIdx]+"] not of type 'int'!");
                   }
                   indices=(int []) o_idx;                  // o.k., assign to int-array
               }
               else // individual indices, create an array of int's for it
               {
                   indices=new int [ args.length-firstIdx ];  // calculate & reserve needed array-size
                   int i=0, j=firstIdx;
                   for ( ; j<args.length; i++, j++)
                   {
                       indices[i]=Integer.parseInt(args[j]);
                   }
               }

               // aw=ArrayWrapper.getArrayWrapper(arrayObj);  // get
               // aw=new ArrayWrapper(arrayObj);  // get

               // rgf, 2009-09-10
               try {
                   aw=new ArrayWrapper(arrayObj);  // get
               }
               catch (ClassNotFoundException cnfe)
               {
                   throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                        "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" class object of 'componentType' not found!", cnfe);
               }


               if (choice==ARRAY_AT)
               {
if (bDebug)  System.err.println("ARRAY_AT(): aw.get(indices)=["+aw.get(indices)+"]...");
                   return makeString4Rexx(this, aw.get(indices)); // get the element and return it to Rexx
               }
               else
               {
                   aw.put(o_nv, indices);           // put the element into the array
               }
           }
           break;



          case ARRAY_LENGTH       :
                  {
                  /* ********************************************************
                   * value = BSF( 'arrayLength', arrayObject )
                   * returns the value of the array field 'length'
                   *
                   * - arrayObject:   array to be worked upon
                   *
                   *********************************************************/
                      if (args.length!=2)
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" invalid # of args; usage: value = BSF( " +
                                  "'"+args[0]+"', arrayObject )");
                      }
                      Object arrayObj = null;

                      arrayObj=lookupBean(this, args[1]);

                      int    num      = Array.getLength( arrayObj ) ;
                      // return makeString4Rexx ( this, new Integer(num) );
                      return makeString4Rexx ( this, Integer.valueOf(num) );
                  }
        // ==================================================================



          case EXIT               :
                  {
                  /* ********************************************************
                   * call BSF 'exit' [, return_value [, time2sleep] ]
                   * terminates the JVM; returns the string "SHUTDOWN, REXX !"
                   * - retVal : optional return value
                   * - msecs  : optional msecs to sleep before terminating the JVM
                   *********************************************************/
                      int  rc=0;                  // return code
                      long msecs2sleep=100l;      // msecs to sleep (default: 1/10 sec)

                      if (args.length > 1)
                      {
                         rc=Integer.parseInt(args[1]);    // get return code, if supplied
                                                          // get milliseconds to sleep
                         if (args.length > 2) msecs2sleep = Long.parseLong(args[2]);
                      }

                      // rgf, 2011-02-09: controls whether System.exit is carried out or not
                      if (carryOutSystemExit==true)
                      {
                              // rgf, 2012-02-06: make sure  no callbacks from Java to Rexx occur anymore
                          ((RexxEngine)rexxEngine).bTerminated=true;

                              // create and start the delayed exit thread
                          new Thread( new ExitDelayed(rc, msecs2sleep) ) .start();

                      }
                      // return "SHUTDOWN, REXX !";  // indicate to Rexx what to do ... ;)
                      return makeString4Rexx(this,"SHUTDOWN, REXX !");
                  }
        // ==================================================================

       case GET_BSF_MANAGER    :
               {
               /* ********************************************************
                * call BSF 'getBSFManager'
                * returns the current BSFMananger instance
                *********************************************************/

                   if (args.length > 1)
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" does not expect any argument!");
                   }
                   return makeString4Rexx(this,this.mgr);    // return the BSFManager instance
               }

      // ==================================================================

       case GET_REXX_AND_JAVA  :
               {
               /* ********************************************************
                * call BSF 'getRexxAndJava'
                * returns the this RexxAndJava instance
                *********************************************************/

                   if (args.length > 1)
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" does not expect any argument!");
                   }
                   return makeString4Rexx(this,this);    // return this instance
               }

      // ==================================================================

       case GET_REXX_ENGINE  :
               {
               /* ********************************************************
                * call BSF 'getRexxEngine'
                * returns the current BSFMananger instance
                *********************************************************/

                   if (args.length > 1)
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" does not expect any argument!");
                   }
                   return makeString4Rexx(this, this.getRexxEngine());    // return this instance
               }

         // ==================================================================

          case GET_FIELD_VALUE           :
          case GET_FIELD_VALUE_JSO       : // rgf, 2014-10-04
          case GET_FIELD_VALUE_STRICT    : // rgf, 2005-05-06
          case GET_FIELD_VALUE_STRICT_JSO: // rgf, 2014-10-04

          case SET_FIELD_VALUE           :
          case SET_FIELD_VALUE_STRICT    :

          case GET_PROPERTY_VALUE        :
           case GET_PROPERTY_VALUE_JSO    : // rgf, 2014-10-05

          case SET_PROPERTY_VALUE        :
          case SET_PROPERTY_VALUE_STRICT :
                  {

                      final boolean bDebug= // true;   // if true, show debug infos
                                            false;
                      final int iDebugLevel=0; // 9; // 0;

if (iDebugLevel>0)
{
    String tmp1="";
    tmp1=tmp1+"//// FIELD \\\\: args.length=["+args.length+"], [0]=["+args[0]+"]: [1]=["+args[1]+"]";
    if (args.length >= 3) tmp1=tmp1+"[2]=["+args[2]+"] ";
    if (args.length >= 4) tmp1=tmp1+"[3]=["+args[3]+"] ";
    if (args.length >= 5) tmp1=tmp1+"[4]=["+args[4]+"] ";
    if (args.length >= 6) tmp1=tmp1+"[5]=["+args[5]+"] ";

    System.err.println(tmp1);
}



                     /* ********************************************************
                      * object = BSF( 'getFieldValue',          beanName, fieldName )
                      * object = BSF( 'getFieldValueStrict',    beanName, fieldName ) // case sensitive fieldName
                      * object = BSF( 'setFieldValueStrict',    beanName, fieldName, argType, newValue )
                      * object = BSF( 'setFieldValue',          beanName, fieldName, newValue )
                      * object = BSF( 'getPropertyValue',       beanName, fieldName, index )
                      * object = BSF( 'setPropertyValueStrict', beanName, fieldName, index, argType, newValue )
                      * object = BSF( 'setPropertyValue',       beanName, fieldName, index, newValue )
                      *
                      * - beanName: the bean to lookup
                      * - fieldName: the bean to lookup
                      * - returns:  the String rendering of the bean or null, if not found
                      *********************************************************/

                     String tmp = null;        // new 2001-04-25, edited: 2003-05-10
                     String fieldName=args[2];    // store received fieldName

                          if ( (choice==GET_FIELD_VALUE        || choice==GET_FIELD_VALUE_JSO ||
                                choice==GET_FIELD_VALUE_STRICT || choice==GET_FIELD_VALUE_STRICT_JSO)
                             && args.length != 3
                             ) tmp=    "fieldName";

                     else if (choice==SET_FIELD_VALUE           && args.length != 4) tmp=    "fieldName, newValue";
                     else if (choice==SET_FIELD_VALUE_STRICT    && (args.length<4 || args.length>5))   tmp= "fieldName, [newValueType,] newValue";

                     else if (choice==GET_PROPERTY_VALUE        && (args.length!=3 && args.length!=4)) tmp= "propertyName[, index]";
                     else if (choice==SET_PROPERTY_VALUE        && args.length != 5) tmp= "propertyName, index, newValue";
                     else if (choice==SET_PROPERTY_VALUE_STRICT && args.length != 6) tmp= "propertyName, index, newValueType, newValue";

                     if (tmp != null)        // wrong number of arguments
                     {
                         throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                  "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": wrong number of arguments ("+args.length+");"
                                     +" usage: value=BSF("+args[0]+", beanName, " +tmp+" ).");
                     }

                     Object o = null;
                     o=lookupBean(this, args[1]);

                     if (o==null)
                     {
                         throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                  "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": bean '"+args[1]+"' not registered.");
                     }

                     try
                     {

                         // rgf, 20180106
                         if (bRgfUseNewRexxReflect &&
                               ( choice==GET_FIELD_VALUE     || choice==GET_FIELD_VALUE_STRICT     ||
                                 choice==GET_FIELD_VALUE_JSO || choice==GET_FIELD_VALUE_STRICT_JSO ||
                                 choice==SET_FIELD_VALUE     || choice==SET_FIELD_VALUE_STRICT
                               )
                         )
                         {

                             Object result=null;

                             // determine whether a STRICT variant is desired
                             boolean bStrictInvocation=(choice==GET_FIELD_VALUE_STRICT     ||
                                                       choice==GET_FIELD_VALUE_STRICT_JSO ||
                                                       choice==SET_FIELD_VALUE_STRICT     );

                             switch (choice)
                             {
                                 case GET_FIELD_VALUE           :
                                 case GET_FIELD_VALUE_JSO       :
                                 case GET_FIELD_VALUE_STRICT    :
                                 case GET_FIELD_VALUE_STRICT_JSO:
                                     result=rxReflector.reflectField(this, choice, bStrictInvocation, o, fieldName, null, false);

                                     switch (choice)
                                     {
                                         case GET_FIELD_VALUE_JSO:
                                         case GET_FIELD_VALUE_STRICT_JSO:
                                             // special processing: return JSO (Java String Object), not the String value:
                                              String beanName = makeBeanName(this, result);     // create a Rexx name
                                              return registerBean(this, beanName, result);      // register JSO and return beanName
                                     }
                                     break;

                                 case SET_FIELD_VALUE           :
                                     result=rxReflector.reflectField(this, choice, bStrictInvocation, o, fieldName, args[3], true); // coerce args[3] (a Rexx string)
                                     break;

                                 case SET_FIELD_VALUE_STRICT    :
                                     Object newValue=args[3];  // assume we only have the value (which is a string value at this stage)
                                     if (args.length==5)
                                     {
                                         newValue=convFromRexx(args[3], args[4]);  // convert using the supplied type indicator!
                                     }

                                            // indicate if value type was not supplied by Rexx by supplying 'null'
                                     result= (args.length==4 ? rxReflector.reflectField(this, choice, bStrictInvocation, o, fieldName, newValue, true )
                                                             : rxReflector.reflectField(this, choice, bStrictInvocation, o, fieldName, newValue, false)
                                           );
                                     return makeString4Rexx(this, result);
                             }
                             return makeString4Rexx(this, result);
                         }



                         Bean    bean  = null;
                         Integer index = null;

                         if (choice==GET_FIELD_VALUE            ||
                             choice==GET_FIELD_VALUE_JSO        ||  // rgf, 2014-10-04
                             choice==GET_FIELD_VALUE_STRICT     ||  // rgf, 2005-05-06
                             choice==GET_FIELD_VALUE_STRICT_JSO ||  // rgf, 2014-10-04

                             choice==SET_FIELD_VALUE            ||
                             choice==SET_FIELD_VALUE_STRICT     )
                         {           // get a field
                                // try to find any field matching the name (not case sensitive!)
                             if (choice==GET_FIELD_VALUE || choice==GET_FIELD_VALUE_JSO || choice==SET_FIELD_VALUE)
                             {
//????
if (iDebugLevel>1) System.err.println("---> searching for fieldName (not case sensitively...)");

                                 Object tmpBean = null;
                                 tmpBean=lookupBean(this, args[1]);

                                 if(tmpBean==null)
                                 {
                                     // 2003-06-11, ---rgf
                                     if (args[1]==null || args[1].equals(null4Rexx))
                                     {
                                         throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                                  "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": object '"+args[1]+"' not registered");
                                     }

                                     tmpBean=args[1]; // 2003-06-11, ---rgf: not in BSF-registry, assuming a String object method

if (bDebug) { System.err.println("get|set field|property(): bean not in registry, assuming a String object operation is desired for ["+bean+"]"); }
                                 }


                                 // find correct cased field name
                                 Class c = tmpBean.getClass();    // class object for reflection
                                 if (c == Class.class)         // if already a class object, use all of its methods
                                 {
                                     c=(Class) tmpBean;
                                 }

                                       // determine correct case of method name
                                 Field[] f = c.getFields();
                                 for (int i=0; i<f.length; i++)
                                 {
                                     if(fieldName.equalsIgnoreCase(f[i].getName()))
                                     {
                                         fieldName=f[i].getName();   // remember method name

if (iDebugLevel>1) System.err.println("---> fieldName=[" + fieldName + "]");

                                         args[2]=fieldName;         // rgf, 2005-05-06: make sure right case is now used for fieldName
                                         i=f.length;               // leave for-loop
                                     }
                                 }
//????
                             }

if (bDebug) System.err.println("--> FIELD <--(): args[0]=[" + args[0] + "] args[1]=["+args[1]+"], fieldName=["+fieldName+"]");

                             // rgf, 2005-05-07
                             // "try" the following, if not successful, try
                             // a method named "set"+fieldName with value as argument
                             // (take care of set...strict case!
                             try
                             {
                                 bean=ReflectionUtils.getField(o, fieldName);       // try to get the value
                             }
                             catch (Exception e)
                             {
                                 if (choice!=SET_FIELD_VALUE && choice!=GET_FIELD_VALUE && choice!=GET_FIELD_VALUE_JSO)   // rgf,20141004
                                 {
                                    throw (e);      // strict versions, hence re-throwing the exception
                                 }

                                    // try to use a setter-method instead (fieldName led in by "set")
                                 try
                                 {
                                     if (choice==SET_FIELD_VALUE)
                                     {
if (bDebug) System.err.println("Trying 'SET"+args[2]+"' ? : " + ! (args[2].toUpperCase()).startsWith("SET"));
                                            // maybe trying a method named "get"+methName, if not already led-in with "get" ?
                                         if (! (args[2].toUpperCase()).startsWith("SET") )
                                         {
                                             String tmpArg[]={
                                                 INVOKE.upperCase, // STRING_INVOKE , // subfunction "INVOKE"
                                                 args[1],        // beanName
                                                 "SET"+args[2],  // methName ==> fieldName ?
                                                 args[3]         // argument
                                                 } ;
                                             return javaCallBSF( tmpArg ); // rgf, 2008-06-23
                                         }
                                     }
                                     else // choice==GET_FIELD_VALUE
                                     {
if (bDebug) System.err.println("Trying 'GET"+args[2]+"' ? : " + ! (args[2].toUpperCase()).startsWith("GET"));
                                         // maybe trying a method named "get"+methName, if not already led-in with "get" ?
                                         if (! (args[2].toUpperCase()).startsWith("GET") )
                                         {
                                             String tmpArg[]={
                                                 INVOKE.upperCase, // STRING_INVOKE , // subfunction "INVOKE"
                                                 args[1],        // beanName
                                                 "GET"+args[2]   // methName ==> fieldName ?
                                                 } ;
                                             return javaCallBSF( tmpArg ); // rgf, 2008-06-23
                                         }
                                     }

                                 }
                                 catch (Exception e1) { };
                             }
                         }
                         else        // get a property
                         {
                            if ( args.length>=4 && args[3]!=null && (!args[3].equals("")) )
                                     index=Integer.parseInt(args[3]);            // get Integer from String

                            bean=ReflectionUtils.getProperty(o, args[2], index);
                         }

if (bDebug)
{
    System.err.println("result of tests:");
    System.err.println("\t\tJSO?\t#1:"+(choice==GET_FIELD_VALUE_JSO ||
                                    choice==GET_FIELD_VALUE_STRICT_JSO ||
                                    choice==GET_PROPERTY_VALUE_JSO)
                       );
    System.err.println("\t\tb.v?\t#2:"+(bean.value != null)+", bean.value.getClass():"+bean.value.getClass() );
    System.err.println("\t\tb.v?\t#3:"+(bean.value instanceof String ||
                                    (nlR_class!=null && bean.value.getClass()==nlR_class)
                        )
                      );
}

                         if (choice==GET_FIELD_VALUE            ||
                             choice==GET_FIELD_VALUE_STRICT     ||
                             choice==GET_PROPERTY_VALUE         ||
                             choice==GET_FIELD_VALUE_JSO        ||
                             choice==GET_FIELD_VALUE_STRICT_JSO ||
                             choice==GET_PROPERTY_VALUE_JSO         )
                         {

// rgf, 2014-10.04: temporatily force java.lang.String
// bean.value=bean.value.toString();

                             if ( ( choice==GET_FIELD_VALUE_JSO        ||
                                  choice==GET_FIELD_VALUE_STRICT_JSO ||
                                  choice==GET_PROPERTY_VALUE_JSO
                                 ) &&
                                 bean.value != null &&
                                 ( bean.value instanceof String || (nlR_class!=null && bean.value.getClass()==nlR_class) )
                                )
                             {
                                 // register result as a bean and return it
                                  String beanName = makeBeanName(this, bean.value);
                                  return registerBean(this, beanName, bean.value);
                             }

                             // not a String nor NetRexx object to be returned
                             return makeString4Rexx(this, bean.value);             // return whatever we got
                         }


                         // this section sets fields/properties
                         try         // now set the field/property to the new value
                         {
                             // rgf, 20180106: glob_tcr will be initialized at all times (moved to static in 2016)
                             // if (glob_tcr == null) // no TypeConvertorRegistry instance yet?
                             // {
                             //   glob_tcr=new TypeConvertorRegistry();        // create an instance
                             // }

                             if (choice==SET_FIELD_VALUE || choice==SET_FIELD_VALUE_STRICT)    // normal field
                             {
                                 if      (args.length==4 && args[3]==null) bean.value=null;  // SET_FIELD_VALUE to null
                                 else if (args.length==5 && args[4]==null) bean.value=null;  // SET_FIELD_VALUE_STRICT to null

                                 else if (choice==SET_FIELD_VALUE || args.length==4) // rgf, 2005-05-07
                                 {
                                     Object tmpValue=convFromRexx(args[3]);    // relaxed version, need to determine type

if (bDebug) System.err.println("--> --> about to set field's value: bean.type=["+bean.type+"]"+", tmpValue=["+tmpValue+"]");
// rgf, 2010-04-17
                                     if (tmpValue != null && tmpValue instanceof String && (bean.type).isPrimitive())
                                     {
                                         // rgf, 2010-04-17: special case of Boolean, where true must be "true", no other value
                                         if ( (bean.type==boolean.class || bean.type==Boolean.class) &&
                                              ((String)tmpValue).compareTo("1")==0 )
                                         {
                                             tmpValue=Boolean.TRUE;
                                         }
                                         else
                                         {
                                             // convert Rexx argument to Java
                                             TypeConvertor tc=glob_tcr.lookup(String.class, bean.type);
                                             tmpValue=tc.convert(String.class, bean.type, tmpValue);
                                         }


if (bDebug) System.err.println("--> --> TYPE-CONVERTOR (String.class!?!): now tmpValue=["+tmpValue+"]");
                                     }
                                     bean.value=tmpValue;
                                 }
                                    // strict (type explicitly stated):
                                 else bean.value=convFromRexx(args[3], args[4]);

if (bDebug)
{
    System.err.println("--> ==> now setting the value: bean.type=["+bean.type+"], bean.value=["+bean.value+"]");
    System.err.println("--> ==> now setting the value: bean.type.getClass=["+bean.type.getClass()+"], bean.value.getClass()=["+bean.value.getClass()+"]");
    System.err.println("--> ==> now setting the value: o=["+o+"], args[2]="+args[2]+", bean=["+bean+"], glob_tcr=["+glob_tcr+"]");
}

                                 ReflectionUtils.setField(o, args[2], bean, glob_tcr);
                             }
                             else
                             {
                                 if      (args.length==5 && args[4]==null) bean.value=null; // SET_PROPERTY_VALUE to null
                                 else if (args.length==6 && args[5]==null) bean.value=null; // SET_PROPERTY_VALUE_STRICT to null

                                 else if (choice==SET_PROPERTY_VALUE)
                                 {
                                     // relaxed version, need to determine type
                                     Object tmpValue=convFromRexx(args[4]);
                                     if (tmpValue != null && (bean.type).isPrimitive())
                                     {
                                         // convert Rexx argument to Java
                                         TypeConvertor tc=glob_tcr.lookup(String.class, bean.type);
                                         tmpValue=tc.convert(String.class, bean.type, tmpValue);
                                     }
                                     bean.value=tmpValue;
                                 }
                                 // strict (type explicitly stated):
                                 else bean.value=convFromRexx(args[4], args[5]);

                                 ReflectionUtils.setProperty(o, args[2], index, bean.value, bean.type, glob_tcr);
                             }

                             // return received value-string (don't know why, but keeping it for back-compatibility, --rgf, 2003-05-10
                             // return args[args.length-1];
                             return makeString4Rexx(this, args[args.length-1]);
                         }
                         catch (Exception e)
                         {
// e.printStackTrace(System.err);

                            throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": exception while setting "
                                      +(choice==SET_FIELD_VALUE ? "field" : "property")
                                      +" ["+ args[2]+"] in object ["+args[1]+"] to ["+ (args.length>4 ? args[4] : args[3]) +"]"
                                      +"\n, exception thrown: ["+e.toString()+"]"     // rgf, 2008-07-09
                                      , e  // rgf, 2011-06-04: supply Exception object
                                      );
                         }

                     }
                     catch (Exception e)
                     {
                        throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
                               "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": exception while accessing "
                                  +( (choice==GET_FIELD_VALUE            ||
                                      choice==GET_FIELD_VALUE_JSO        ||     // rgf, 20141004
                                      choice==SET_FIELD_VALUE            ||
                                      choice==GET_FIELD_VALUE_STRICT     ||
                                      choice==GET_FIELD_VALUE_STRICT_JSO ||     // rgf, 20141004
                                      choice==SET_FIELD_VALUE_STRICT)
                                      ? "field" : "property" )
                                  +" ["+args[2]+"] in object ["+args[1]+"]"
                                  +"\n, exception thrown: ["+e.toString()+"]"     // rgf, 2008-07-09
                                  , e  // rgf, 2011-06-04: supply Exception object
                                  );
                     }
                  }
        // ==================================================================

          case GET_STATIC_VALUE             :
          case GET_STATIC_VALUE_JSO         :   // rgf, 2014-10-04
          case GET_STATIC_VALUE_STRICT      :   // rgf, 2005-05-06
          case GET_STATIC_VALUE_STRICT_JSO :   // rgf, 2014-10-04
                  {
                  /* ********************************************************
                   * call BSF 'getStaticValue', className, fieldName
                   *
                   * gets the value of a static field of the given class or interface;
                   * does *not* instantiate the class (would not be possible for
                   * abstract classes and interfaces)
                   *
                   * className: name of class or interface to lookup
                   * fieldName: name of a public static field to lookup
                   *********************************************************/
                      if(args.length!=3)
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": invalid # of args; usage: value = BSF( " +
                              "'"+args[0]+"', className, fieldName )");
                      }

                      boolean bError=false;
                      String  errTxt="",
                              fieldName=args[2]; // assign field Name


                      Class cl=null;
                      Field f=null;

                        // rgf, 2005-05-06 begin
                      Object tmpBean = null;
                      tmpBean=lookupBean(this, args[1]);

                      if(tmpBean==null)
                      {

                          // cl=EngineUtils.loadClass(mgr, args[1]); // ---rgf, 2003-05-10
                          try
                          {
                              // cl=Thread.currentThread().getContextClassLoader().loadClass(args[1]); // ---rgf, 2007-01-28
                              // rgf, 20070918: now EngineUtils applies new class loader sequence
                              cl=EngineUtils.loadClass(mgr, args[1]); // ---rgf, 20070918
                              if (cl==null) // rgf, 2008-06-05
                              {
                                  // throw (new ClassNotFoundException ("class ["+args[1]+"] not found"));
                                  throw (new ClassNotFoundException (args[1]));
                                  // throw (new ClassNotFoundExceptio^n ());
                              }
                          }
                          catch (Exception e)
                          {
                              throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                              //        "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": class '"+args[1]+"', threw exception: ["+e.getLocalizedMessage()+"]");
                                 "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": class '"+args[1]+"', threw exception: ["+e.toString()+"]"
                                 , e  // rgf, 2011-06-04: supply Exception object
                                 );
                              // "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": class ["+args[1]+"]', threw exception: ["+e.getLocalizedMessage()+"]");
                          }
                      }
                      else
                      {
                          // find correct cased field name
                          cl = tmpBean.getClass();    // class object for reflection
                          if (cl == Class.class)         // if already a class object, use all of its methods
                          {
                              cl=(Class) tmpBean;
                          }
                      }
                        // rgf, 2005-05-06 end

                          // rgf, 2005-05-06
                      if (choice==GET_STATIC_VALUE || choice==GET_STATIC_VALUE_JSO) // try to get field from case-INsensitive name
                      {
                                // determine correct case of method name
                          Field[] fa = cl.getFields();
                          for (int i=0; i<fa.length; i++)
                          {
                              if(fieldName.equalsIgnoreCase(fa[i].getName()))
                              {
                                  fieldName=fa[i].getName();   // remember method name in case sensitiveness
                                  i=fa.length;               // leave for-loop
                              }
                          }
                      }


                      if (!bError)
                      {
                         try { f = cl.getField(fieldName); }// get the field object
                         catch (NoSuchFieldException e) {
                             bError=true;
                             errTxt="class '"+args[1]+"', static field '"+args[2]+"' not found";
                         }
                      }

                      if (!bError)        // got a field object, now check whether public & static
                      {
                         int modifiers=f.getModifiers();          // get the modifiers

                         if ( (modifiers & Modifier.STATIC) == 0 )
                         {  bError=true;
                            errTxt="class '"+args[1]+"', static field '"+args[2]+"' not STATIC";
                         }
                         else             // o.k., now what?
                         {                // get the value as an Object using the class object as object


                             Object result=null;
                             try { result=f.get( cl ) ; }
                             catch (IllegalAccessException e) {
//                                  e.printStackTrace(System.err);
                             }

                             // ---rgf, 2014-10-02: return reference, if a String or Netrexx object
                             if (choice==INVOKE_JSO && result != null &&
                                   (result instanceof String ||
                                    (nlR_class!=null && result.getClass()==nlR_class)
                                   )
                                 )
                             {
                                 // register result as a bean and return it
                                  String beanName = makeBeanName(this, result);
                                  return registerBean(this, beanName, result);
                             }


                             if ( ( choice==GET_STATIC_VALUE_JSO        ||
                                  choice==GET_STATIC_VALUE_STRICT_JSO
                                 ) &&
                                 result != null &&
                                 ( result instanceof String || (nlR_class!=null && result.getClass()==nlR_class) )
                                )
                             {
                                 // register result as a bean and return it
                                  String beanName = makeBeanName(this, result);
                                  return registerBean(this, beanName, result);
                             }


                             return makeString4Rexx(this, result);
                         }
                      }

                          // o.k., if arriving here, we're in trouble, i.e. an error occured !
                      throw new BSFException (BSFException.REASON_EXECUTION_ERROR, "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": "+errTxt+"!");
                  }
        // ==================================================================

           case INVOKE_JSO :   // new as of 2014-10-02: allow version that returns a java.lang.String as an object-reference
                               //                       such that Rexx coders can employ all Java String methods directly (especially codepage related)
                  // ---rgf, 2014-05-14: use Class.getMethods() instead of Class.getDeclaredMethods();
                  //                     getMethods() returns all public methods (including the Java 8 default methods)

                  // ---rgf TODO: add superclass resolution using getDelcaredMethods() seeking matching
                  //              protected methods, cf. <http://docs.oracle.com/javase/tutorial/java/javaOO/accesscontrol.html>

           case INVOKE :   // new as of 2014-05-14: use Class.getMethods() which returns all public methods (in Java 8 also default methods)
                      {
                      /* ********************************************************
                       * value = BSF( 'invoke', [beanName], function [, arg]... )
                       *
                       * - beanName: the bean whose method is to be invoked
                       * - function: the function's name
                       * - arg: the argument's value
                       *********************************************************/


                          final boolean bDebug= false;
                                                // true;

    if (bDebug)
    {
        System.err.print("\n==> INVOKE <===");
        System.err.println("invoke: beanName=["+args[1]+"], function: ["+args[2]+"] args.length="+args.length);
    }

                          int nrArgs=args.length-3; // contains nr of arguments for method
                          // Object coercedArgs[]=new Object[nrArgs];   // this array will contain the arguments co-erced to the appropriate typ
                          Object [] coercedArgs = null; // array containing the coerced arguments


                          if (args.length<3)
                          {
                              throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                       "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" invalid # of args; usage: value = BSF( " +
                                      "'"+args[0]+"', beanName, function [, arg]... )");
                          }

                          Object bean = null;
                          bean=lookupBean(this, args[1]);

                          if(bean==null)
                          {
                              // 2003-06-11, ---rgf
                              if (args[1]==null || args[1].equals(null4Rexx))
                              {
                              throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                       "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": object '"+args[1]+"' not registered");
                              }
                              bean=args[1]; // 2003-06-11, ---rgf: not in BSF-registry, assuming a String object method
    if (bDebug) { System.err.println("invoke(): bean not found, assuming a String object operation is desired for ["+args[1]+"]"); }

                          }
    if (bDebug) System.err.println("bean: ["+bean+"], bean.getClass()=["+bean.getClass()+"]");
                          //convert function-arguments to the correct type
                          Object funcArgs[] = new Object[(args.length-3)];
                          if (nrArgs>0)
                          {
                              // convert arguments to Java type, if possible
                              for (int i=3; i<args.length; i++ )
                              {
                                  // funcArgs[j] = convFromRexx( args[i]);
                                  funcArgs[i-3] = convFromRexx(args[i]);
                              }
                          }


                          // find correct method name
                            // get class object, if necessary

                          Class c = null;
                            // rgf, 2009-07-13, if a class object in hand, then in the first round look for
                            //                  static methods only
                          boolean lookForStaticMethodsOnly= (bean instanceof Class);

                          c = (lookForStaticMethodsOnly ? (Class) bean : bean.getClass() );

    if (bDebug)
    {
        System.err.println("\tnrArgs=["+nrArgs+"], lookForStaticMethodsOnly=["+lookForStaticMethodsOnly+"], c=["+c+"]");
        System.err.println("\tStarting class to look for method: ["+c+"]");
    }

                            // now find a method with the needed amount of arguments
                            // start out with present class and work up to root class
// --> 20140514
// <--- 20140514
                          // boolean bFound=false;
                          boolean bLooking=true;

                          int k=0;
                          // rgf, 2010-04-20: implement same logic as in INVOKE; i.e. resolve the entire class tree even for class object
                          for (k=0; k<2; k++)   // iterate twice at the most (2nd time using bean's class object)
                          {
                                    // determine correct case of method name
                              Method[] m = c.getMethods();
                              for (int i=0; i<m.length; i++)
                              {
                                  if (lookForStaticMethodsOnly)    // if a static object, look for static methods only!
                                  {
                                      if (!Modifier.isStatic(m[i].getModifiers()))
                                          continue;
                                  }

                                  // rgf, 2005-05-06: do an exact match
                                  if(args[2].equalsIgnoreCase(m[i].getName()))
                                  {
                                      Class paramTypes[]=m[i].getParameterTypes();
if (bDebug) System.err.println("\t\t\tmethName=["+m[i].getName()+"] paramTypes.length="+paramTypes.length+" | nrArgs="+nrArgs);

                                      if (paramTypes.length != nrArgs)  // wrong nr of arguments
                                          continue;

                                        // only access method if it is defined to be public
                                      if (!Modifier.isPublic(m[i].getModifiers()))
                                          continue ;

                                        // right number of arguments, can we coerce to them?
                                      if (nrArgs==0)     // no argument, hence found
                                      {
                                          // FOUND !
                                          bLooking=false;       // don't look any further

if (bDebug) System.err.println("---> NO ARGS! Hence, FOUND !");
                                      }
                                      else
                                      {
                                          coercedArgs=this.coerceArgs( funcArgs, paramTypes);
                                          bLooking=(coercedArgs==null); // if null, keep on searching
if (bDebug)
{
    if (bLooking)
    {
if (bDebug) System.err.println("---> coercedArgs() did *NOT* find matching arguments, hence looking further...");
    }
    else
    {
if (bDebug) System.err.println("---> coercedArgs() found matching, about to invoke via method!!");
    }
}


                                      }
if (bDebug) System.err.println("\t\tmethod was public, nrArgs matched, bLooking="+bLooking);

                                      if (bLooking==false)    // not looking anymore, found!
                                      {
if (bDebug) System.err.println("                   bean=["+bean+"], coercedArgs["+coercedArgs+"]");
                                          try
                                          {
                                              // Object result=m[i].invoke(bean , coercedArgs);
                                              Object result=invokeMethod(m[i], bean, coercedArgs);

if (bDebug) // ---rgf, 2006-07-29
{
    System.err.println("                   result=["+result+"]#");
    if (result!=null)
    {
        if (result.getClass().isArray())
        {
            try
            {
                Object aha[]=(Object[]) result;
                System.out.println("\taha.length=["+aha.length+"]");

                for (int i1=0; i1<aha.length; i1++)
                {
                    System.out.print  ("\t\tiaha["+i1+"]=["            );
                    System.out.print  (                   aha[i1]+"]");
                    System.out.println(", hashCode="+(aha[i1]==null? "<null>" : Integer.toHexString( aha[i1].hashCode())) );
                }
            }
            catch (Exception e)
            {
                System.err.println(" -->-->-->-->-->   exception while trying to debug-dump:");
                e.printStackTrace();
            }
        }
        System.err.println("result=["+result+"], result.getClass()=["+result.getClass()+"], about to return makeString4Rexx(...)");

    }

}
                                              // ---rgf, 2014-10-02: return reference, if a String or Netrexx object
                                              if (choice==INVOKE_JSO && result != null &&
                                                    (result instanceof String ||
                                                     (nlR_class!=null && result.getClass()==nlR_class)
                                                    )
                                                  )
                                              {
// System.err.println("RAJ: TSK # 1");
                                                  // register result as a bean and return it
                                                   String beanName = makeBeanName(this, result);
                                                   return registerBean(this, beanName, result);
                                              }

                                              return makeString4Rexx(this, result);
                                          }
                                          catch (Exception e)
                                          {

if (bDebug)
{
    System.err.println("                   BRRR, e.toString()=["+e.toString()+"].");
    System.err.print  ("                   BRRR, e.printStackTrace()=\n["); e.printStackTrace();System.err.println("].");
    System.err.println("                   BRRR, e.getMessage()=["+e.getMessage()+"].");
    System.err.println("                   BRRR, e.getClass().getName()=["+e.getClass().getName()+"]");

    e.printStackTrace();
    System.err.println("--- --- --- --- ---");
}

                                                  // search a BSFException, if found, throw that instead
                                              Throwable originalCause=null;
                                              for (Throwable t=e; t!=null; t=t.getCause())
                                              {
                                                  if (t instanceof BSFException)      // BSFException found, throw it now
                                                  {
                                                      originalCause=t;
                                                  }
                                              }
                                              if (originalCause!=null)
                                              {
                                                  throw (BSFException) originalCause;
                                              }

                                              throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
                                                                      "/// Java-exception (RexxAndJava) occurred: ["+e.toString()+"]" +
                                                                      getCauseAsString(", getCause(): ", e)+" \\\\\\\n" +
//                                                          "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": object '"+args[1]+"' - method ["+args[2]+"], method not found or error (exception) executing method!"
                                                                      "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": object '"+args[1]+"' - method ["+args[2]+"]: error (exception) while executing Java method!"
                                                          , e  // rgf, 2011-06-04: supply Exception object
                                                          );
                                          }
                                      }
                                  }



//===================================================
/* --->
                                  {

                                      Class paramTypes[]=m[i].getParameterTypes();
                      // if (bDebug)
                      System.err.println("\t\t\tmethName=["+m[i].getName()+"] paramTypes.length="+paramTypes.length+", nrArgs="+nrArgs);
                                      if (paramTypes.length != nrArgs)  // wrong nr of arguments
                                          continue;

                                        // only access method if it is defined to be public
                                      if (!Modifier.isPublic(m[i].getModifiers()))
                                          continue ;

                                        // right number of arguments, can we coerce to them?
                                      if (nrArgs==0)     // no argument, hence found
                                      {
                                          // FOUND !
                                          bFound=true;   // don't look any further

                      if (bDebug) System.err.println("---> NO ARGS! Hence, FOUND !                                   ");
                                      }
                                      else
                                      {
                                          int j=0;

                                          bFound=true;  // default to found
                                          for (j=0; j<nrArgs && bFound==true; j++)
                                          {
                                              // bFound=(funcArgs[j]==paramTypes[j]);
                                              if  (funcArgs[j]==null) continue;

                      if (bDebug) System.err.println("paramTypes[j].isPrimitive()=["+paramTypes[j].isPrimitive()+"], paramTypes[j]=["+paramTypes[j]+"], funcArgs[j].getClass()=["+funcArgs[j].getClass()+"]");
                                              if (paramTypes[j].isPrimitive())
                                              {
                                                  Class pCl=paramTypes[j],
                                                        fCl=funcArgs[j].getClass();

System.err.println("\t\t\ti="+i+", pCl=["+pCl+"], fCl=["+fCl+"]");

                                                       if (pCl==boolean.class) { bFound=( Boolean.class.isAssignableFrom(fCl)) ;}
                                                  else if (pCl==int.class )    { bFound=( Integer.class.isAssignableFrom(fCl)) ;}
                                                  else if (pCl==char.class)    { bFound=( Character.class.isAssignableFrom(fCl)) ;}
                                                  else if (pCl==byte.class)    { bFound=( Byte.class.isAssignableFrom(fCl)) ;}
                                                  else if (pCl==short.class)   { bFound=( Short.class.isAssignableFrom(fCl)) ;}
                                                  else if (pCl==long.class)    { bFound=( Long.class.isAssignableFrom(fCl)) ;}
                                                  else if (pCl==float.class)   { bFound=( Float.class.isAssignableFrom(fCl)) ;}
                                                  else if (pCl==double.class)  { bFound=( Double.class.isAssignableFrom(fCl)) ;}
                                              }
                                              else
                                              {
                                                  bFound=(paramTypes[j].isAssignableFrom(funcArgs[j].getClass()));
                                              }

                                          }
                                      }


                                      if (bFound)   // o.k. method found, invoke it
                                      {
                      if (bDebug) System.err.println("BINGO !  BINGO !   bean=["+bean+"]");
                                              try
                                              {
                                                  // Object result=m[i].invoke(bean , funcArgs);
                                                  Object result = invokeMethod(m[i], bean, funcArgs);

                      if (bDebug) System.err.println("                   result=["+result+"]");
                                                  return makeString4Rexx(result);
                                              }
                                              catch (Exception e)
                                              {
                      if (bDebug)
                      {
                      System.err.println("                   BRRR, e.getMessage()=["+e.getMessage()+"].");
                      System.err.println("                   BRRR, e.getCause()=["+e.getCause()+"], e.getCause().getMessage()=["+e.getCause().getMessage()+"]");
                      System.err.println("                   BRRR, e.getClass().getName()=["+e.getClass().getName()+"]");
                      e.printStackTrace();
                      System.err.println("--- --- --- --- ---");
                      }

                                                  Throwable originalCause=null;
                                                  for (Throwable t=e; t!=null; t=t.getCause())
                                                  {
                      if (bDebug) System.err.println("        --> RexxAndJava.java(): looping # 1, t=["+t+"] ...");
                                                      if (t instanceof BSFException)      // BSFException found, throw it now
                                                      {
                      if (bDebug) System.err.println("    ==> ==> RexxAndJava.java(): BINGO, throwing t=["+t+"].");
                                                          originalCause=t;
                                                      }
                                                  }
                      if (bDebug) System.err.println("    ==> ==> RexxAndJava.java(): NO BSFexception FOUND, proceeding as normal...");
                                                  if (originalCause!=null)
                                                  {
                      if (bDebug) System.err.println("    ==> ==> RexxAndJava.java(): BINGO, throwing originalCause=["+originalCause+"].");
                                                      throw (BSFException) originalCause;
                                                  }


                      if (bDebug) System.err.println("==> ==> ==> RexxAndJava(), throwing # 1...");
                                                  throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
                                                              // "/// Java-exception (RexxAndJava) occurred: ["+eMsg+"] \\\\\\\n" +
                                                                          "/// Java-exception (RexxAndJava) occurred: ["+e.toString()+"]" +
                                                                          getCauseAsString(", getCause(): ", e)+" \\\\\\\n" +
                                                              "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": object '"+args[1]+"' - method ["+args[2]+"], method not found or error (exception) executing method!"
                                                              , e  // rgf, 2011-06-04: supply Exception object
                                                              );
                                              }
                                      }
                                  }
<-- */
//===================================


                              }


                              if (bean instanceof Class)    // maybe a method of java.lang.Class ?
                              {
                                  c=bean.getClass();        // repeat search in bean's class object (java.lang.Class)
                                  lookForStaticMethodsOnly=false;   // from now on look for all kind of methods
                              }
                              else
                              {
                                  break;
                              }

                          }


                          if (args.length==3)    // rgf, 2005-05-06, no argument given, maybe a getField intended?
                          {
// ??????
                              // if not a getter method, try to access a field by the name of the method
                              if (! (args[2].toUpperCase()).startsWith("GET") )
                              {
                                  try
                                  {

                                      String tmpArg[]={
                                          choice==INVOKE_JSO ?
                                                 GET_FIELD_VALUE_JSO.upperCase : // STRING_GET_FIELD_VALUE_JSO :
                                                 GET_FIELD_VALUE.upperCase     , // STRING_GET_FIELD_VALUE ,
                                          args[1],        // beanName
                                          args[2]         // methName ==> fieldName ?
                                          } ;
if (bDebug) System.err.println("'invoke': trying '"+GET_FIELD_VALUE.mixedCase+"'...");

// System.err.println("RAJ: INVOKE_JSO? args[0]=["+args[0]+"], but not found, now trying to find field using ["+tmpArg[0]+","+tmpArg[1]+","+tmpArg[2]);

                                      return javaCallBSF( tmpArg ); // rgf, 2008-06-23
                                  }
                                  catch (Exception e) { };

                              }

/* */

if (bDebug) System.err.println("'invoke': AFTER, AFTER, trying '"+GET_FIELD_VALUE.mixedCase+"', args[2]=["+args[2]+"]...");

                              // 2016-11-06, rgf: add MAKEARRAY and SUPPLIER support; if bean has one of the
                              //             interfaces java.util.Enumeration, java.lang.Iterable, java.util.Iterator,
                              //             java.util.Collection, java.util.Map implemented, then create objects
                              //             that ooRexx expects; hint: MAKEARRAY may be implicitly requested
                              //             when ooRexx is about to execute a DO...OVER statement
                              //
                              //             use RexxProxy to create genuine ooRexx .Array and .Supplier objects to return
                              if (args[2].equalsIgnoreCase("MAKEARRAY") || args[2].equalsIgnoreCase("SUPPLIER"))
                              {
                                  boolean bMakeArray=args[2].equalsIgnoreCase("MAKEARRAY"); // MAKEARRAY or SUPPLIER ?
                                  bean=lookupBean(this, args[1]);

                                  Object result=null;
                                  String rexxCreateArray   ="return .array~new                          \n" +
                                                            "::requires BSF.CLS                           ";

                                  String rexxCreateSupplier="use arg itemArray, indexArray              \n" +
                                                            "return .supplier~new(itemArray,indexArray) \n" +
                                                            "::requires BSF.CLS                           ";


                                  if (bean instanceof Class)    // if a java.lang.Enum class in hand, iterate over all enum values
                                  {
                                      Class beanClz=(Class) bean;
                                      if (beanClz.isEnum())     // iterate over all values()
                                      {
                                          Enum values[]=(Enum[])beanClz.getEnumConstants(); // get all Enum values, they come as an array already
                                          if (bMakeArray)   // MAKEARRAY message
                                          {
                                                // create an ooRexx array object, return it
                                              RexxProxy rpArr1=(RexxProxy) rexxEngine.apply("InvokeFrom_RexxAndJava_Enum_01.rex", 0, 0, rexxCreateArray, null, null);
                                                // populate Rexx array
                                              for (int i=0; i<values.length;i++)
                                              {
                                                  rpArr1.sendMessage1("APPEND",values[i]);
                                              }
                                              result=rpArr1;
                                          }
                                          else  // SUPPLIER message: create index array using the Enum ordinal values
                                          {     // do as if a Map ordinal->value was in hand, i.e. use the ordinal values as index values for the Rexx supplier
                                              RexxProxy rpItemArray =(RexxProxy) rexxEngine.apply("InvokeFrom_RexxAndJava_Enum_02.rex", 0, 0, rexxCreateArray, null, null);
                                              RexxProxy rpIndexArray=(RexxProxy) rexxEngine.apply("InvokeFrom_RexxAndJava_Enum_03.rex", 0, 0, rexxCreateArray, null, null);

                                              for (int i=0; i<values.length; i++)
                                              {
                                                  rpItemArray .sendMessage1("APPEND",values[i]);
                                                  rpIndexArray.sendMessage1("APPEND",values[i].ordinal());
                                              }
                                              Vector<RexxProxy> vArgs=new Vector<RexxProxy>();
                                              vArgs.add(rpItemArray);
                                              vArgs.add(rpIndexArray);
                                              result=rexxEngine.apply("InvokeFrom_RexxAndJava_Enum_04.rex", 0, 0, rexxCreateSupplier, null, vArgs);
                                          }
                                      }
                                  }
                                  else  // ? instance of java.lang.Iterable, java.util.Enumeration, java.util.Collection, java.util.Iterator, java.util.Map ?
                                  {
                                      boolean isMap=Map.class.isInstance(bean);

                                      if ( isMap                              ||
                                           Iterable.class   .isInstance(bean) ||
                                           Iterator.class   .isInstance(bean) ||
                                           Collection.class .isInstance(bean) ||
                                           Enumeration.class.isInstance(bean)
                                         )
                                      {
                                          Iterator it=null;
                                            // try to create iterators
                                          if (isMap)    // get the keyset and create an iterator for it
                                          {
                                              it= ((Map) bean).keySet().iterator();

if (bDebug) System.err.println("in ... -- /// isMap, setting \"it\"...");
                                          }

                                          else if (Iterable.class.isInstance(bean))
                                          {
                                              it= ((Iterable) bean).iterator();
if (bDebug) System.err.println("in ... -- /// isIterable, setting \"it\"...");
                                          }

                                          else if (Iterator.class.isInstance(bean) )
                                          {
                                              it= (Iterator) bean;
if (bDebug) System.err.println("in ... -- /// isIterator, setting \"it\"...");
                                          }
                                          else if (Collection.class.isInstance(bean) )
                                          {
                                              it= ((Collection) bean).iterator();
if (bDebug) System.err.println("in ... -- /// isCollection, setting \"it\"...");
                                          }


                                            // create an ooRexx array object, return it
                                          RexxProxy rpArr1=(RexxProxy) rexxEngine.apply("InvokeFrom_RexxAndJava_Other_01.rex", 0, 0, rexxCreateArray, null, null);

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
                                          else if (Enumeration.class.isInstance(bean) )
                                          {
if (bDebug) System.err.println("in ... --> isEnumeration, MAKEARRAY \"it\"...");
                                              Enumeration en=(Enumeration) bean;
                                              while (en.hasMoreElements())
                                              {
                                                  rpArr1.sendMessage1("APPEND",en.nextElement());
                                              }
                                          }

                                          if (bMakeArray)   // MAKEARRAY message
                                          {
                                              result=rpArr1;
                                          }
                                          else      // SUPPLIER message: all but Map get a Rexx like integer array
                                          {
if (bDebug)  System.err.println("in ... # i: SUPPLIER branch...");
                                              if (isMap)    // it has the keys already
                                              {
                                                  Map mBean=(Map) bean;
                                                    // create an ooRexx array object, return it
                                                  RexxProxy rpArr2=(RexxProxy) rexxEngine.apply("InvokeFrom_RexxAndJava_Other_02.rex", 0, 0, rexxCreateArray, null, null);
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
                                                  result=rexxEngine.apply("InvokeFrom_RexxAndJava_Other_03.rex", 0, 0, rexxCreateSupplier, null, vArgs);
                                              }
                                              else
                                              {
                                                    // return a supplier as if from a bag, i.e. item and index array are the same
                                                  Vector<RexxProxy> vArgs=new Vector<RexxProxy>();
                                                  vArgs.add(rpArr1);
                                                  vArgs.add(rpArr1);
                                                  result=rexxEngine.apply("InvokeFrom_RexxAndJava_Other_04.rex", 0, 0, rexxCreateSupplier, null, vArgs);
                                              }

                                          }
                                      }
                                  }

                                  if (result!=null)     // bingo, register and return
                                  {
                                      // register result as a bean and return it
                                       String beanName = makeBeanName(this, result);
                                       return registerBean(this, beanName, result);
                                  }
                              }
/* */
// ??????
                          }



                            // one error could be: using a value which is a key into the BSF registry, hence give
                          {
                              StringBuffer dump=new StringBuffer();       // programmer as much information as possible to find out about this
                              int i=0;
if (bDebug) {System.err.println("-> args.length=["+args.length+"]");}
                              for(i=3; i<args.length; i++)
                              {

if (bDebug) {System.err.println("-> -> error-loop, i=["+i+"]");}

                                  dump.append("\n\t\targ # "+(i-2)+
                                            ", supplied argument=["+truncString(args[i],25)+
                                            "] --> value=["+ (truncString(""+funcArgs[i-3], 45)) +"]"
                                            +" type=<"+(funcArgs[i-3]==null? "nulltype>" :
                                                                           funcArgs[i-3].getClass().getName())+">"
                                            );
                              }
                              if (i>3)
                              {
                                  dump.append("\n");
                              }

                              throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                       "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": "+
                                       "\n\tbean:   ["+bean+"]"+
                                       " type: ["+bean.getClass().toString()+"]"+  // rgf, 2009-04-13
                                       "\n\tmethod: ["+args[2]+"] not found!" +
                                       (dump.length()==0 ? ""
                                                         : "\n\n\tcheck method name or arguments (number and types):\n"+dump)
                                       );
                          }

                      }

            // ==================================================================


       case INVOKE_STRICT_JSO  :            // 2014-10-04: ---rgf
       case INVOKE_STRICT      :
                  bStrongTyping=true;       // indicate that type information is passed in by Rexx
                  {
                      boolean bDebug=false;      // if true, show debug infos

                  /* ********************************************************
                   * value = BSF( 'invokeStrict', [beanName], function [, argType, arg]... )
                   *
                   * - beanName: the bean whose method is to be invoked
                   * - function: the function's name
                   * - argType: the type of an argument
                   * - arg: the argument's value
                   *********************************************************/

if (bDebug) System.err.println("invokeStrict: beanName=["+args[1]+"], function: ["+args[2]+"] args.length="+args.length);

                      if((args.length<3) || ((args.length-3)%2!=0))
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" invalid # of args; usage: value = BSF( " +
                                  "'"+args[0]+"', beanName, function [, argType, arg]... )");
                      }
                      Object bean = null;
                      bean=lookupBean(this, args[1]);

                      if(bean==null)
                      {
                          // 2003-06-11, ---rgf
                          if (args[1]==null || args[1].equals(null4Rexx))
                          {
                              throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                       "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": object '"+args[1]+"' not registered");
                          }

                          bean=args[1]; // 2003-06-11, ---rgf: not in BSF-registry, assuming a String object method
if (bDebug) { System.err.println("invoke_strict(): bean not found, assuming a String object operation is desired for ["+bean+"]"); }
                      }


                      //convert function-arguments to the correct type
                      Object funcArgs[] = new Object[(args.length-3)/2];
                      boolean funcArgsIsPrimitive[] = new boolean [(args.length-3)/2];
                      int nrArgs=funcArgs.length;

                      if (args.length>3)
                      {
                          for(int i=3, j=0; i<args.length; i+=2, j++)
                          {
                                  // convert arguments to Java type
                              String args_i=args[i];
                              funcArgs[j] = convFromRexx( args_i, args[i+1]);
                                // remember whether we have a primitive datatype
                              funcArgsIsPrimitive[j] = false;

                              if (args_i != null && args_i.length()>0)  // only use if at least one character is there
                              {
                                    // is it a primitive type ?
                                  args_i=args_i.toUpperCase();
                                  funcArgsIsPrimitive[j]=!(args_i.startsWith("O") || args_i.startsWith("ST"));
                                  // (Character.toUpperCase(args_i.charAt(0)) != 'O');
                              }

                          }
                      }


                      // find correct method name

                      Class c = null;
                        // rgf, 2009-07-13, if a class object in hand, then in the first round look for
                        //                  static methods only
                      boolean lookForStaticMethodsOnly= (bean instanceof Class);
                      c = (lookForStaticMethodsOnly ? (Class) bean : bean.getClass() );

                      boolean bFound=false;

                      int k=0;
                      // rgf, 2010-04-20: implement same logic as in INVOKE; i.e. resolve the entire class tree even for class object
                      for (k=0; k<2; k++)   // iterate twice at the most (2nd time using bean's class object)
                      {
                                // determine correct case of method name
                          Method[] m = c.getMethods();
                          for (int i=0; i<m.length; i++)
                          {
                              if (lookForStaticMethodsOnly)    // if a static object, look for static methods only!
                              {
                                  if (!Modifier.isStatic(m[i].getModifiers()))
                                      continue;
                              }

                              // rgf, 2005-05-06: do an exact match
                              if(args[2].equals(m[i].getName()))
                              {
     if (bDebug) System.err.println("\t\t->\t methName=["+m[i].toString()+"]");
                                  Class paramTypes[]=m[i].getParameterTypes();
     if (bDebug) System.err.println("\t\t\tmethName=["+m[i].getName()+"] paramTypes.length="+paramTypes.length);
                                  if (paramTypes.length != nrArgs)  // wrong nr of arguments
                                      continue;

                                    // only access method if it is defined to be public
                                  if (!Modifier.isPublic(m[i].getModifiers()))
                                      continue ;

                                    // right number of arguments, can we coerce to them?
                                  if (nrArgs==0)     // no argument, hence found
                                  {
                                      // FOUND !
                                      bFound=true;   // don't look any further

     if (bDebug) System.err.println("---> NO ARGS! Hence, FOUND !                                   ");
                                  }
                                  else
                                  {
                                      int j=0;

                                      bFound=true;  // default to found
                                      for (j=0; j<nrArgs && bFound==true; j++)
                                      {
                                          // bFound=(funcArgs[j]==paramTypes[j]);
                                          if  (funcArgs[j]==null) continue;

if (bDebug) System.err.println("\t\t\t\ta) paramTypes["+j+"].isPrimitive()=["+paramTypes[j].isPrimitive()+"], paramTypes[j]=["+paramTypes[j]+"], funcArgsIsPrimitive[j]=["+funcArgsIsPrimitive[j]+"] \n\t\t\t\t\t| funcArgs[j].getClass()=["+funcArgs[j].getClass()+"]");
                                          if (funcArgsIsPrimitive[j])   // a primitive value at this position?
                                          {
if (bDebug) System.err.println("\t\t\t\tab) paramTypes["+j+"].isPrimitive()=["+paramTypes[j].isPrimitive()+"], in \"funcArgsIsPrimitive\" branch...");
                                              if (paramTypes[j].isPrimitive())
                                              {
if (bDebug) System.err.println("\t\t\t\tabc1)paramTypes["+j+"].isPrimitive()=["+paramTypes[j].isPrimitive()+"], in \"paramTypes.isPrimitive()\" branch...");
                                                  Class pCl=paramTypes[j],
                                                        fCl=funcArgs[j].getClass();

                                                       if (pCl==boolean.class) { bFound=( Boolean.class.isAssignableFrom(fCl)) ;}
                                                  else if (pCl==int.class )    { bFound=( Integer.class.isAssignableFrom(fCl)) ;}
                                                  else if (pCl==char.class)    { bFound=( Character.class.isAssignableFrom(fCl)) ;}
                                                  else if (pCl==byte.class)    { bFound=( Byte.class.isAssignableFrom(fCl)) ;}
                                                  else if (pCl==short.class)   { bFound=( Short.class.isAssignableFrom(fCl)) ;}
                                                  else if (pCl==long.class)    { bFound=( Long.class.isAssignableFrom(fCl)) ;}
                                                  else if (pCl==float.class)   { bFound=( Float.class.isAssignableFrom(fCl)) ;}
                                                  else if (pCl==double.class)  { bFound=( Double.class.isAssignableFrom(fCl)) ;}
                                              }
                                              else  // this is not the method we search for strictly!
                                              {
if (bDebug) System.err.println("\t\t\t\tabc2)paramTypes["+j+"].isPrimitive()=["+paramTypes[j].isPrimitive()+"], in \"paramTypes.isPrimitive()\", method not found, check next one!");
                                                  bFound=false;
                                              }
                                          }

                                          else  // only check, if the Rexx strict type is of "Object"
                                          {
if (bDebug) System.err.println("\t\t\t\tB) paramTypes["+j+"].isPrimitive()=["+paramTypes[j].isPrimitive()+"], funcArgsIsPrimitive[j]=["+funcArgsIsPrimitive[j]+"] in \"Object\" branch...");
                                              bFound=(((Class<?>)paramTypes[j]).isAssignableFrom(funcArgs[j].getClass()));
                                          }

                                      }
                                  }


                                  if (bFound)   // o.k. method found, invoke it
                                  {
if (bDebug) System.err.println("BINGO !  BINGO !   bean=["+bean+"]");
                                          try
                                          {
                                              // Object result=m[i].invoke(bean , funcArgs);
                                              Object result = invokeMethod(m[i], bean, funcArgs);

if (bDebug) System.err.println("                   result=["+result+"]");
                                              // ---rgf, 2014-10-04: return reference, if a String or Netrexx object
                                              if (choice==INVOKE_STRICT_JSO && result != null &&
                                                    (result instanceof String ||
                                                     (nlR_class!=null && result.getClass()==nlR_class)
                                                    )
                                                  )
                                              {
                                                  // register result as a bean and return it
                                                   String beanName = makeBeanName(this, result);
                                                   return registerBean(this, beanName, result);
                                              }

                                              return makeString4Rexx(this, result);
                                          }
                                          catch (Exception e)
                                          {
if (bDebug)
{
    System.err.println("                   BRRR, e.getMessage()=["+e.getMessage()+"].");
    System.err.println("                   BRRR, e.getCause()=["+e.getCause()+"], e.getCause().getMessage()=["+e.getCause().getMessage()+"]");
    System.err.println("                   BRRR, e.getClass().getName()=["+e.getClass().getName()+"]");
    e.printStackTrace();
    System.err.println("--- --- --- --- ---");
}

                                              Throwable originalCause=null;
                                              for (Throwable t=e; t!=null; t=t.getCause())
                                              {
if (bDebug) System.err.println("        --> RexxAndJava.java(): looping # 1, t=["+t+"] ...");
                                                  if (t instanceof BSFException)      // BSFException found, throw it now
                                                  {
if (bDebug) System.err.println("    ==> ==> RexxAndJava.java(): BINGO, throwing t=["+t+"].");
                                                      originalCause=t;
                                                  }
                                              }
if (bDebug) System.err.println("    ==> ==> RexxAndJava.java(): NO BSFexception FOUND, proceeding as normal...");
                                              if (originalCause!=null)
                                              {
if (bDebug) System.err.println("    ==> ==> RexxAndJava.java(): BINGO, throwing originalCause=["+originalCause+"].");
                                                  throw (BSFException) originalCause;
                                              }


if (bDebug) System.err.println("==> ==> ==> RexxAndJava(), throwing # 1...");
                                              throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
                                                          // "/// Java-exception (RexxAndJava) occurred: ["+eMsg+"] \\\\\\\n" +
                                                                      "/// Java-exception (RexxAndJava) occurred: ["+e.toString()+"]" +
                                                                      getCauseAsString(", getCause(): ", e)+" \\\\\\\n" +
//                                                          "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": object '"+args[1]+"' - method ["+args[2]+"], method not found or error (exception) executing method!"
                                                                      "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": object '"+args[1]+"' - method ["+args[2]+"]: error (exception) while executing Java method!"
                                                          , e  // rgf, 2011-06-04: supply Exception object
                                                          );
                                          }
                                  }
                              }
                          }


                          if (bean instanceof Class)    // maybe a method of java.lang.Class ?
                          {
                              c=bean.getClass();        // repeat search in bean's class object (java.lang.Class)
                              lookForStaticMethodsOnly=false;   // from now on look for all kind of methods
                          }
                          else
                          {
                              break;
                          }

                      }

                        // one error could be: using a value which is a key into the BSF registry, hence give
                      {
                          StringBuffer dump=new StringBuffer();       // programmer as much information as possible to find out about this
                          int i=0, j=0;
                          for(i=3, j=0; i<args.length; i+=2, j++)
                          {
                              dump.append("\n\t\targ # "+(j+1)+": typeIndicator=["+args[i]+"]"+
                                        ", supplied argument=["+truncString(args[i+1],25)+
    //                                    "] --> ["+ (funcArgs[j]) +"]");
                                        "] --> value=["+ (truncString(""+funcArgs[j], 45)) +"]"
                                        +" type=<"+(funcArgs[j]==null? "nulltype>" :
                                                                       funcArgs[j].getClass().getName())+">"
                                        );
                          }
                          if (j>0)
                          {
                              dump.append("\n");
                          }

                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": "+
                                   "\n\tbean:   ["+bean+"]"+
                                   " type: ["+bean.getClass().toString()+"]"+  // rgf, 2009-04-13
                                   "\n\tmethod: ["+args[2]+"] not found!" +
                                   (dump.length()==0 ? ""
                                                     : "\n\n\tcheck method name or arguments (number and types):\n"+dump)
                                   );
                      }
                  }

        // ==================================================================
/* ---> 20140514
       case INVOKE :
                  {
                  / * ********************************************************
                   * value = BSF( 'invoke', [beanName], function [, arg]... )
                   *
                   * - beanName: the bean whose method is to be invoked
                   * - function: the function's name
                   * - arg: the argument's value
                   ********************************************************* /



                      final boolean bDebug=
                                      false;
                                      // true;

if (bDebug)
{
    System.err.println("\n==> INVOKE <===");
    System.err.println("invoke: beanName=["+args[1]+"], function: ["+args[2]+"] args.length="+args.length);
}

                      int nrArgs=args.length-3; // contains nr of arguments for method
                      // Object coercedArgs[]=new Object[nrArgs];   // this array will contain the arguments co-erced to the appropriate typ
                      Object [] coercedArgs = null; // array containing the coerced arguments


                      if (args.length<3)
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" invalid # of args; usage: value = BSF( " +
                                  "'"+args[0]+"', beanName, function [, arg]... )");
                      }

                      Object bean = mgr.lookupBean(args[1]);
                      if(bean==null)
                      {
                          // 2003-06-11, ---rgf
                          if (args[1]==null || args[1].equals(null4Rexx))
                          {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": object '"+args[1]+"' not registered");
                          }
                          bean=args[1]; // 2003-06-11, ---rgf: not in BSF-registry, assuming a String object method
if (bDebug) { System.err.println("invoke(): bean not found, assuming a String object operation is desired for ["+args[1]+"]"); }

                      }
if (bDebug) System.err.println("bean: ["+bean+"], bean.getClass()=["+bean.getClass()+"]");
                      //convert function-arguments to the correct type
                      Object funcArgs[] = new Object[(args.length-3)];
                      if (nrArgs>0)
                      {
                          // convert arguments to Java type, if possible
                          for (int i=3; i<args.length; i++ )
                          {
                              // funcArgs[j] = convFromRexx( args[i]);
                              funcArgs[i-3] = convFromRexx(args[i]);
                          }
                      }


                      // find correct method name
                        // get class object, if necessary

                      Class c = null;
                        // rgf, 2009-07-13, if a class object in hand, then in the first round look for
                        //                  static methods only
                      boolean lookForStaticMethodsOnly= (bean instanceof Class);

                      c = (lookForStaticMethodsOnly ? (Class) bean : bean.getClass() );

if (bDebug)
{
    System.err.println("\tnrArgs=["+nrArgs+"], lookForStaticMethodsOnly=["+lookForStaticMethodsOnly+"], c=["+c+"]");
    System.err.println("\tStarting class to look for method: ["+c+"]");
}

                        // now find a method with the needed amount of arguments
                        // start out with present class and work up to root class
                      {
                          String methNameArg=args[2],   // name as passed in (all in upper case)
                                 methName="";           // name in correct casing

                          boolean bLooking=true,
                                  bStopLooking=true;
                          Class   tmpClass=c,
                                  startClass=c,
                                  tmpSuperClass;
                          while ( bLooking )
                          {
if (bDebug)
{
    System.err.println("\n--> bLooking am Kopf: methNameArg=["+methNameArg+"]");
    System.err.println("\t\t===> tmpClass: ["+tmpClass+"], lookForStaticMethodsOnly=["+lookForStaticMethodsOnly+"] ...");
}
                              Method[] m=tmpClass.getDeclaredMethods();

                              int i;
                              for (i=0; i<m.length && bLooking; i++)    // check out the methods
                              {
                                  if (lookForStaticMethodsOnly)    // if a static object, look for static methods only!
                                  {
if (bDebug) System.err.println("\t\t\t method to analyze: ["+m[i].getName()+"] isStatic? "+Modifier.isStatic(m[i].getModifiers()));

                                      if (!Modifier.isStatic(m[i].getModifiers()))
                                          continue;
                                  }
if (bDebug) System.err.println("\t\t\t\t method to analyze: ["+m[i]+"] ...");

                                  methName=m[i].getName();

                                  if(methNameArg.equalsIgnoreCase(methName)) // method of this name found ?
                                  {
                                      Class paramTypes[]=m[i].getParameterTypes();
if (bDebug) System.err.println("\t\t\tmethName=["+methName+"] paramTypes.length="+paramTypes.length+" | nrArgs="+nrArgs);

                                      if (paramTypes.length != nrArgs)  // wrong nr of arguments
                                          continue;

                                        // only access method if it is defined to be public
                                      if (!Modifier.isPublic(m[i].getModifiers()))
                                          continue ;

                                        // right number of arguments, can we coerce to them?
                                      if (nrArgs==0)     // no argument, hence found
                                      {
                                          // FOUND !
                                          bLooking=false;       // don't look any further

if (bDebug) System.err.println("---> NO ARGS! Hence, FOUND !");
                                      }
                                      else
                                      {
                                          coercedArgs=this.coerceArgs( funcArgs, paramTypes);
                                          bLooking=(coercedArgs==null); // if null, keep on searching
if (bDebug)
{
    if (bLooking)
    {
if (bDebug) System.err.println("---> coercedArgs() did *NOT* find matching arguments, hence looking further...");
    }
    else
    {
if (bDebug) System.err.println("---> coercedArgs() found matching, about to invoke via method!!");
    }
}


                                      }
if (bDebug) System.err.println("\t\tmethod was public, nrArgs matched, bLooking="+bLooking);

                                      if (bLooking==false)    // not looking anymore, found!
                                      {
if (bDebug) System.err.println("                   bean=["+bean+"], coercedArgs["+coercedArgs+"]");
                                          try
                                          {
                                              // Object result=m[i].invoke(bean , coercedArgs);
                                              Object result=invokeMethod(m[i], bean, coercedArgs);

if (bDebug) // ---rgf, 2006-07-29
{
    System.err.println("                   result=["+result+"]#");
    if (result!=null)
    {
        if (result.getClass().isArray())
        {
            try
            {
                Object aha[]=(Object[]) result;
                System.out.println("\taha.length=["+aha.length+"]");

                for (int i1=0; i1<aha.length; i1++)
                {
                    System.out.print  ("\t\tiaha["+i1+"]=["            );
                    System.out.print  (                   aha[i1]+"]");
                    System.out.println(", hashCode="+(aha[i1]==null? "<null>" : Integer.toHexString( aha[i1].hashCode())) );
                }
            }
            catch (Exception e)
            {
                System.err.println(" -->-->-->-->-->   exception while trying to debug-dump:");
                e.printStackTrace();
            }
        }
        System.err.println("result=["+result+"], result.getClass()=["+result.getClass()+"], about to return makeString4Rexx(...)");

    }

}
                                              // ---rgf, 2014-10-02: return reference, if a String or Netrexx object
                                              if (choice==INVOKE_JSO && result != null &&
                                                    (result instanceof String ||
                                                     (nlR_class!=null && result.getClass()==nlR_class)
                                                    )
                                                  )
                                              {
                                                  // register result as a bean and return it
                                                   String beanName = makeBeanName(result);
                                                   return registerBean(beanName, result);
                                              }

                                              return makeString4Rexx(result);
                                          }
                                          catch (Exception e)
                                          {

if (bDebug)
{
    System.err.println("                   BRRR, e.toString()=["+e.toString()+"].");
    System.err.print  ("                   BRRR, e.printStackTrace()=\n["); e.printStackTrace();System.err.println("].");
    System.err.println("                   BRRR, e.getMessage()=["+e.getMessage()+"].");
    System.err.println("                   BRRR, e.getClass().getName()=["+e.getClass().getName()+"]");

    e.printStackTrace();
    System.err.println("--- --- --- --- ---");
}

                                                  // search a BSFException, if found, throw that instead
                                              Throwable originalCause=null;
                                              for (Throwable t=e; t!=null; t=t.getCause())
                                              {
                                                  if (t instanceof BSFException)      // BSFException found, throw it now
                                                  {
                                                      originalCause=t;
                                                  }
                                              }
                                              if (originalCause!=null)
                                              {
                                                  throw (BSFException) originalCause;
                                              }

                                              throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
                                                                      "/// Java-exception (RexxAndJava) occurred: ["+e.toString()+"]" +
                                                                      getCauseAsString(", getCause(): ", e)+" \\\\\\\n" +
                                                          "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": object '"+args[1]+"' - method ["+args[2]+"], method not found or error (exception) executing method!"
                                                          , e  // rgf, 2011-06-04: supply Exception object
                                                          );
                                          }
                                      }
                                  }
                              }


                              tmpSuperClass=tmpClass.getSuperclass(); // get superclass, e.g. null, if an interface

// ---> rgf, 2010-04-20
                              if (lookForStaticMethodsOnly)
                              {
                                  lookForStaticMethodsOnly = (tmpSuperClass!=Object.class );
                              }
// <---

if (bDebug) System.err.println("\t\ttmpClass=["+tmpClass+"] tmpSuperClass: ["+tmpSuperClass+"] bLooking=["+bLooking+"] args.length=["+args.length+"]...");

                                // an interface class has null as its root class
                              bStopLooking=(bLooking && (tmpClass==Object.class || tmpSuperClass==null));

                              if (bStopLooking)   // root class, method not found
                              {
if (bDebug) System.err.println("---> bStopLooking==true!!");
                                  if (args.length==3)    // rgf, 2005-05-06, no argument given, maybe a getField intended?
                                  {
// ??????
                                      // if not a getter method, try to access a field by the name of the method
                                      if (! (args[2].toUpperCase()).startsWith("GET") )
                                      {
                                          try
                                          {
                                              String tmpArg[]={
                                                  STRING_GET_FIELD_VALUE ,
                                                  args[1],        // beanName
                                                  args[2]         // methName ==> fieldName ?
                                                  } ;
if (bDebug) System.err.println("'invoke': trying '"+STRING_GET_FIELD_VALUE+"'...");

                                              return javaCallBSF( tmpArg ); // rgf, 2008-06-23
                                          }
                                          catch (Exception e) { };

                                      }
// ??????
                                  }

                                    // ---rgf, 2006-02-05: need to look for method in java.lang.Class ?
                                  if ( (startClass instanceof Class) && (startClass!=Class.class
                                       || (tmpSuperClass==null && lookForStaticMethodsOnly==true))  // an interface class in hand?
                                     )
                                  {
if (bDebug) System.err.println("---> ---> looking in class-object of bean! startClass=["+startClass+"], startClass.getClass()=["+startClass.getClass()+"]");
                                        // an interface class, now look for its instance methods
                                      if (tmpSuperClass==null && lookForStaticMethodsOnly==true)
                                      {
                                          lookForStaticMethodsOnly=false;
                                      }

                                      bLooking=true;
                                      tmpClass=startClass.getClass();
                                      startClass=tmpClass;
                                      tmpSuperClass=null;
if (bDebug) System.err.println("\t --> --> 'invoke': trying to look for a method of java.lang.Class, before 'continue'...");
                                      continue;
                                  }


                                    // one error could be: using a value which is a key into the BSF registry, hence give
                                  StringBuffer dump=new StringBuffer();     // programmer as much information as possible to find out about this
                                  for (i=0; i<funcArgs.length; i++)
                                  {
                                      Object tmpArg=funcArgs[i];    // could be 'null' !
                                      String tmpStr=truncString(""+tmpArg, 30);      // make sure argument is turned into a String

                                      // dump.append("\n\t\targ # "+(i+1)+": ["+tmpStr+"] --> of type/class:  [");
                                      dump.append("\n\t\targ # "+(i+1)+": ["+tmpStr+"] --> <");
                                      if (tmpArg==null)
                                      {
                                          dump.append("nulltype>");
                                      }
                                      else   // either already a String, otherwise get a String, make sure it is not shown in full...
                                      {
                                          dump.append(tmpArg.getClass().getName()+">");
                                      }
                                  }

                                  if (i>0)
                                  {
                                      dump.append("\n");    // insert empty line to make reading easier
                                  }

if (bDebug) System.out.println("we're in troubles! thread=["+Thread.currentThread().getName()+"], BSFManager=["+mgr+"]\n");

                                  throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                           "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": "+
                                           "\n\tbean:   ["+bean+"]"+
                                           " type: ["+bean.getClass().toString()+"]"+  // rgf, 2009-04-13
                                           "\n\tmethod: ["+args[2]+"] not found!" +
                                           (dump.length()==0 ? ""
                                                             : "\n\n\tcheck method name or arguments (number and types):\n"+dump)

                                           );
                              }
                              tmpClass=tmpSuperClass;   // try the new superclass

                          }
                      }

                        // one error could be: using a value which is a key into the BSF registry, hence give
                      String dump="";       // programmer as much information as possible to find out about this
                      for (int i=0; i<funcArgs.length; i++)
                      {
                          dump=dump+"\n\t\targ # "+(i+1)+": ["+args[i+3]+
                                    "] --> ["+funcArgs[i]+"]";
                      }
                      throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                               "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": "+
                               "\n\tbean:   ["+bean+"]"+
                               " type: ["+bean.getClass().toString()+"]"+  // rgf, 2009-04-13
                               "\n\tmethod: ["+args[2]+"] not found!" +
                               (dump.length()==0 ? ""
                                                 : "\n\n\tcheck method name or arguments (number and types):\n"+dump)
                               );
                  }
<-- 20140514 */

        // ==================================================================

          case IS_INSTANCE :
                  {
                  /* ********************************************************
                   * call BSF 'isA', javaObject , javaClass
                   *
                   * javaObject:    test if instance of javaClass
                   * javaClass:     a class object or a string denoting a class/interface
                   *********************************************************/
                      if(args.length!=3)
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": invalid # of args; usage: value = BSF( " +
                              "'"+args[0]+"', javaObject, javaClass)");
                      }

                      Object javaObject=null;
                      javaObject=lookupBean(this, args[1]);

                      if (javaObject==null)     // not in BSFRegistry, use string object directly
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                             "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": javaObject ["+args[1]+"]"+
                             " cannot be retrieved from the BSFRegistry!"
                             );
                      }

                      Object javaClass = null;
                      javaClass=lookupBean(this, args[2]);

                      if(javaClass==null)       // not in BSFRegistry, try to laod class
                      {
                          try
                          {
                              javaClass=EngineUtils.loadClass(mgr, args[2]);    // try to load class
                              if (javaClass==null)         // not found
                              {
                                  throw (new ClassNotFoundException (args[2]));
                              }
                          }
                          catch (Exception e)
                          {
                              throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                 "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": javaClass ["+args[2]+"]"+
                                 " is neither an instance of 'java.lang.Class', nor could a Java class by that name be dynamically loaded!"
                                 , e  // rgf, 2011-06-04: supply Exception object
                                 );
                          }
                      }
                        // carry out the test and return .true or .false accordingly
                      return makeString4Rexx(this, ((Class)javaClass).isInstance(javaObject) ? "1" : "0" );
                  }



        // ==================================================================

          case GET_VERSION        :
                  {
                  /* ********************************************************
                   * object = BSF( 'version' )
                   *
                   * - returns:  the String with the version information:
                   *             package_name package_version version_number.dateOfVersion
                   *********************************************************/

                      if (args.length>2)
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" invalid # of args;\nusage: value = BSF( " +
                                  "'"+args[0]+"' [, ALl|ARrayWrapper|BsfManager|EnumerationWrapper|Java4Rexx|REXXAndJava|REXXEngine|Supplier] )");
                      }

                      if (args.length==1)   // no argument given, return
                      {
                          return makeString4Rexx(this, RAJ_VERSION );
                      }

                        // get argument in uppercase
                      String uCase= (args[1]==null ? "" : args[1].toUpperCase());
                      if (uCase.startsWith("AL"))           // ALl
                      {
                          String conc=" ";
                          return makeString4Rexx(this,
                              pkgName         +".ArrayWrapper "      +ArrayWrapper.version       +conc+
                              "org.apache.bsf"+".BSFManager "        +mgr.getVersion()           +conc+
                              pkgName         +".EnumerationWrapper "+EnumerationWrapper.version +conc+
                              pkgName         +".Java4Rexx "         +Java4Rexx.version          +conc+
                              pkgName         +".RexxAndJava "       +RexxAndJava.version        +conc+
                              pkgName         +".RexxEngine "        +RexxEngine.version         +conc+
                              pkgName         +".Supplier "          +Supplier.version
                          );
                      }
                      else if (uCase.startsWith("AR"))
                      {
                          return makeString4Rexx(this, pkgName+".ArrayWrapper "+ArrayWrapper.version);
                      }
                      else if (uCase.startsWith("B"))
                      {
                          return makeString4Rexx(this, "org.apache.bsf"+".BSFManager "+mgr.getVersion());
                      }
                      else if (uCase.startsWith("E"))
                      {
                          return makeString4Rexx(this, pkgName+".EnumerationWrapper "+EnumerationWrapper.version);
                      }
                      else if (uCase.startsWith("J"))
                      {
                          return makeString4Rexx(this, pkgName+".Java4Rexx "+Java4Rexx.version);
                      }
                      else if (uCase.startsWith("REXXA"))
                      {
                          return makeString4Rexx(this, pkgName+".RexxAndJava "+RexxAndJava.version);
                      }
                      else if (uCase.startsWith("REXXE"))
                      {
                          return makeString4Rexx(this, pkgName+".RexxEngine "+RexxEngine.version);
                      }
                      else if (uCase.startsWith("S"))
                      {
                          return makeString4Rexx(this, pkgName+".Supplier "  +Supplier.version);
                      }

                      throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                               "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" invalid argument ["+args[1]+"];\nusage: value = BSF( " +
                               "'"+args[0]+"' [, ALl|ARrayWrapper|BsfManager|EnumerationWrapper|Java4Rexx|REXXAndJava|REXXEngine|Supplier] )");
                      // return makeString4Rexx( RAJ_VERSION );
                  }
        // ==================================================================




       case LOOKUP_BEAN        :
               {
               /* ********************************************************
                * object = BSF( 'lookup', beanName )
                *
                * - beanName: the bean to lookup
                * - returns:  the String rendering of the bean or null, if not found
                *********************************************************/

                   if (args.length!=2)
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" invalid # of args; usage: value = BSF( " +
                                "'"+args[0]+"', beanName )");
                   }

                   Object tmp = null;
                   tmp=lookupBean(this, args[1]);

                   if (tmp!=null)
                   {
                      return makeString4Rexx(this, tmp);
                   }
               }
          break;
     // ==================================================================


           case LOAD_CLASS         :
                   {
                   /* ********************************************************
                    * object = BSF( 'loadClass', JavaClassName )
                    *
                    * - JavaClassName: the fully qualified name of the Java class to load
                    * - returns:       the String rendering of the Java class object or null, if not found
                    *********************************************************/

                       if (args.length!=2)
                       {
                           throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                    "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" invalid # of args; usage: value = BSF( " +
                                    "'"+args[0]+"', JavaClassName )");
                       }

                       Object tmp=null;

                       try
                       {
                           // tmp = Thread.currentThread().getContextClassLoader().loadClass(args[1]);
                           // rgf 20070918: EngineUtils now applies new class loader lookup
                           tmp=EngineUtils.loadClass(mgr, args[1]); // ---rgf, 20070918
                           if (tmp==null)  // rgf, 2008-06-05
                           {
                               // throw (new ClassNotFoundException ("class ["+args[1]+"] not found"));
                               // throw (new ClassNotFoundException ("java.lang.ClassNotFoundException: "+args[1]));
                               throw (new ClassNotFoundException (args[1]));
                               // throw (new ClassNotFoundException ());
                           }
                       }
                       // catch (ClassNotFoundException e)
                       // {
                       //     throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                       //              "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": class '"+args[1]+"' not found!");
                       // }
                       catch (Exception e)
                       {
                           throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                 // "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": class '"+args[1]+"', threw exception: ["+e.getLocalizedMessage()+"]");
                                 "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": class '"+args[1]+"', threw exception: ["+e.toString()+"]"
                                 , e  // rgf, 2011-06-04: supply Exception object
                                 );
                           // "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": class ["+args[1]+"], threw exception: ["+e.getLocalizedMessage()+"]");
                       }

                       if (tmp!=null)
                       {
                          return makeString4Rexx(this, tmp);
                       }
                   }
              break;
         // ==================================================================



           case RAW_REGISTER_BEAN  :     // 2008-08-28, ---rgf: pass-through for BSF manager's register Bean
                   {
                   /* ********************************************************
                    * object = BSF( 'rawRegisterBean', beanName, object )
                    *
                    * - beanName    - a string determining the index value to be used
                    *
                    * - object      - the object to be stored in the BSF registry
                    *
                    * - returns: beanName,
                    *
                    * Remarks: this subfunction will replace (!) any existing object in BSF
                    *          registry with the passed in one!
                    *
                    *********************************************************/
                       final boolean bDebug=false;

                       if (args.length!=3)
                       {
                           throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                    "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" invalid # of args; usage: value = BSF( " +
                                    "'"+args[0]+"', beanName, object )");
                       }

                       // get Java value for object, register this with the BSF manager,
                       // register it with our reference count mechanism and return it

                       Object oJava=convFromRexx(args[2]);  // get Java null or Java object from BSF registry, String else

if (bDebug)
{
    System.err.println("---RexxAndJava, subfunction: args[0]=["+args[0]+"]: args[1]=["+args[1]+"]"+
                       ", args[2]=["+args[2]+"]<==>oJava=["+oJava+"]/oJava.getClass()=["+(oJava==null ? null : oJava.getClass())+"]");
}
//                       return RAJReg.rawRegisterBean(args[1], oJava, bsfPrefixReturnValue, rii_ID);
                       return rawRegisterBean(args[1], oJava);

                   }

         // ==================================================================




          case POLL_EVENT_TEXT    :
                  {
                  /* ********************************************************
                   * call BSF 'pollEventText' [timeout]
                   *
                   * timeout: time in msec to wait for receiving an eventText
                   *          before returning to Rexx (this way using Java's
                   *          locking mechanism and waiting without a busy loop)
                   *********************************************************/
                      if(args.length>2) // 2009-06-20, rgf: added as Mr. Michael Lex (student@WU Vienna) found this as a bug in his work on TestUnits for BSF4ooRexx
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": invalid # of args; usage: value = BSF( " +
                              "'"+args[0]+"'[, timeout])");
                      }

                      long maxWait=0;           // wait forever
                      if (args.length>1)
                      {
                          maxWait=Long.parseLong(args[1]); // get maxWaitTime

                          if (maxWait<0L)       // illegal value ?
                          {
                              throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                       "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": 'timeout' argument value ["+maxWait+"] must not be negative");
                          }
                      }

                      try
                         {
                            return makeString4Rexx(this, eventTextList.get(maxWait));
                         }
                      catch (java.lang.InterruptedException e)
                         {
//                            e.printStackTrace(System.err);
                           throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
                                    "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" threw ["+e.toString()+"], hence aborting..."
                                    , e  // rgf, 2011-06-04: supply Exception object
                                    );
                         }
                  }
        // ==================================================================

          case POST_EVENT_TEXT    :
                  {
                  /* ********************************************************
                   * call BSF 'postEventText', eventText [, priority]
                   *
                   * eventText: eventText to be added to the "eventTextList"
                   * priority:  priority level, 0=low, 1=normal (default), 2=high ("alarm level")
                   *********************************************************/
                      if(args.length>3)
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": invalid # of args; usage: value = BSF( " +
                              "'"+args[0]+"', eventText [, priority])");
                      }

                      int priority=1;     // default to normal priority

                      if (args.length==3) // priority given ?
                      {
                         priority=Integer.parseInt( args[2] );
                         if (priority<0 || priority>2) priority = 1;
                      }
                      eventTextList.put((Object) args[1], priority);
                      // return args[1];
                      return makeString4Rexx(this, args[1]);
                  }
        // ==================================================================

          case REGISTER_BEAN_STRICT      :
                  bStrongTyping=true;       // indicate that type information is passed in by Rexx
                  {
                  /* ********************************************************
                   * beanName = BSF( 'registerBean', beanName, beanType [, argType, arg]... )
                   *
                   * - beanName: the bean's name, optional
                   * - beanType: the bean's type
                   * - argType: the type of a constructor argument
                   * - arg: the argument's value
                   *********************************************************/

                      if((args.length<3) || ((args.length-3)%2!=0))
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": invalid # of args; usage: beanName=BSF( " +
                                  "'"+args[0]+"', [beanName], beanType [, argType, arg]... )");
                      }

                      //convert arguments to the correct type
                      Object funcArgs[] = new Object[(args.length-3)/2];
                      if(args.length>3)
                      {
                          char type;
                          for(int i=3, j=0; i<args.length; i+=2, j++)
                          {
                                  // convert arguments to Java type
                              funcArgs[j] = convFromRexx( args[i], args[i+1]);
                          }
                      }

                      // create and register the object
                      try {

                          Object bean     = EngineUtils.createBean(args[2],funcArgs);

                          String beanName = (String) convFromRexx("STring", args[1]);

                          if (beanName==null || beanName.equals(""))
                          {  // need to create a unique beanName
                             // beanName=""+ bean.getClass().getName() + '@' + Integer.toHexString( bean.hashCode() );
                             beanName=makeBeanName(this, bean);
                          }

                          return registerBean(this, beanName, bean);   // will use <O> indicator

                      }
                      catch (BSFException e)
                      {
                          throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": could not create a bean from instantiating '"+
                                   args[2]+"', exception thrown: ["+e.toString()+"]"
                                   , e  // rgf, 2011-06-04: supply Exception object
                                   );
                      }
                  }
        // ==================================================================

        // ==================================================================
          case REGISTER_BEAN      :
                  {
                  /* ********************************************************
                   * beanName = BSF( 'registerBean', beanName, beanType [, arg]... )
                   *
                   * - beanName: the bean's name, optional
                   * - beanType: the bean's type
                   * - arg: the argument's value
                   *********************************************************/

                      final boolean bDebug=  // true;  // if true, show debug infos
                                            false;

                      if (args.length<3)
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": invalid # of args; usage: beanName=BSF( " +
                                  "'"+args[0]+"', [beanName], beanType [, argType, arg]... )");
                      }


                      // Class cl=EngineUtils.loadClass(mgr, args[2]);

                      // 2010-04-18, rgf

if (bDebug)
{
    {
System.err.println("REGISTER_BEAN: args[2]=["+args[2]+"], r.lB["+lookupBean(this, args[2])+"], cFR=["+convFromRexx(args[2])+"]");
    }
}

                      Class cl=null;

                      try {
                          cl=(Class) convFromRexx(args[2]);    // null;
                      }
                      catch (java.lang.ClassCastException cce)
                      {}

                      if (cl==null)
                      {
                          try
                          {
                              // cl=Thread.currentThread().getContextClassLoader().loadClass(args[2]); // ---rgf, 2007-01-28
                              // rgf 20070918: EngineUtils now does new class loader lookup
                              cl=EngineUtils.loadClass(mgr, args[2]); // ---rgf, 20070918

                              if (cl==null)  // rgf, 2008-06-05
                              {
                                  // throw (new ClassNotFoundException ("class ["+args[2]+"] not found"));
                                  // throw (new ClassNotFoundException ("java.lang.ClassNotFoundException: "+args[2]));
                                  throw (new ClassNotFoundException (args[2]));
                                  // throw (new ClassNotFoundException ());
                              }
                          }
                          // catch (ClassNotFoundException e)
                          // {
                          //     throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                          //              "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": class '"+args[2]+" (argument 'beanType') not found!");
                          // }
                          catch (Exception e)
                          {
                              throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                       // "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": class '"+args[2]+"', threw exception: ["+e.getLocalizedMessage()+"]");
                                       "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": class '"+args[2]+"', threw exception: ["+e.toString()+"]"
                                       , e  // rgf, 2011-06-04: supply Exception object
                                       );
                              // "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": class ["+args[2]+"], threw exception: ["+e.getLocalizedMessage()+"]");
                          }
                      }

                      Constructor [] constr=cl.getConstructors();

                      if (constr.length==0)
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": no constructors available for Java class ["+args[2]+"], cannot create an instance...\n");
                      }


                      int nrArgs=args.length-3; // arguments given for creating object
                      Object funcArgs[] = new Object[nrArgs] ;
                      Object [] coercedArgs = null;

                      // check Rexx arguments, replace nulls with Java nulls and
                      // references to the BSF registry with the Java object
                      if(args.length>3)
                      {
                          for(int i=3; i<args.length; i++)
                          {
                                  // convert nulls and beans to Java null and Java object
                              funcArgs[i-3] = convFromRexx( args[i] );
                          }
                      }

                      boolean bFound=false;

                        // find matching constructor
                      int i=0;
                      for (i=0; i<constr.length; i++)
                      {
                          if (!Modifier.isPublic(constr[i].getModifiers()))  // not a public constructor
                              continue ;

                          Class paramTypes[]=constr[i].getParameterTypes();
                          if (paramTypes.length != nrArgs) continue;    // nr of arguments does not match

                          if (nrArgs>0)
                             coercedArgs=this.coerceArgs( funcArgs, paramTypes);

                          if (nrArgs==0 || coercedArgs != null)
                          {
                              bFound=true;
                              break;
                          }
                      }

                      if (!bFound)  // no matching constructor found
                      {

                            // one error could be: using a value which is a key into the BSF registry, hence give
                          String dump="";       // programmer as much information as possible to find out about this
                          for (i=0; i<funcArgs.length; i++)
                          {
                              dump=dump+"\n\t\targ # "+(i+1)+": ["+args[i+3]+
                                        "] in BSFRegistry? --> ["+funcArgs[i]+"] --> type [" +
                                        (funcArgs[i]==null ? "(null)]" : funcArgs[i].getClass()+"]")
                                        ;
                          }

                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": no public constructor matching given arguments found for Java class ["+
                                   args[2]+"], cannot create an instance...\n"+
                                   (dump.length()==0 ? ""
                                                     : "\n\n\tcheck method name or arguments (number and types):\n"+dump)
                                   );
                      }


                      Object bean=null;
                      // create and register the object
                      try {
                          // Object bean     = EngineUtils.createBean(args[2],funcArgs);
                          bean     = constr[i].newInstance(coercedArgs);
                      }
                      catch (Exception e)   // 2003-05-09, --rgf, maybe an IllegalAccessException?
                      {
                          boolean bRaise=true;      // by default raise exception

                          if (e instanceof IllegalAccessException &&
                              // bMethodHasSetAccessible &&
                              Modifier.isPublic(constr[i].getModifiers())   )   // if a public method allow access to it
                          {
                              constr[i].setAccessible(true);    // allow unconditional access
                              bRaise=false;     // assume everything goes o.k.

                              try
                              {
                                  bean = constr[i].newInstance( coercedArgs );  // create instance
                              }
                              catch (Exception e2)  // puh, this is a tough one !
                              {
                                  bRaise=true;  // now, raise exception
                                  e=e2;         // give information about this one
                              }
                          }

                          if (bRaise)   // do we have to raise an exception ?
                          {
                              // re-throw the exception
                              throw new BSFException (BSFException.REASON_OTHER_ERROR,
                                                      "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": could not create a Java object from instantiating '"+
                                                      args[2]+"' with the given arguments.\n"+
                                                      "\t\tusing constructor: ["+constr[i]+"]\n"+
                                                      "\t\t raised exception: ["+e.toString()+"]"+
                                                      getCauseAsString(" -> ", e)  // rgf, 2008-06-08
                                                      , e  // rgf, 2011-06-04: supply Exception object
                                                      );
                          }
                      }


                        // creating went well, now registering new Java object
                      String beanName = ( convFromRexx(args[1])==null? null : args[1]);


                      if (beanName==null || beanName.equals(""))
                      {  // need to create a unique beanName
                         // beanName=""+ bean.getClass().getName() + '@' + Integer.toHexString( bean.hashCode() );
                         beanName=makeBeanName(this, bean);
                      }

                      return registerBean(this, beanName, bean);   // will use <O> indicator
                  }
        // ==================================================================

          case UNREGISTER_BEAN    :
                  {
                  /* ********************************************************
                   * call BSF 'unregisterBean', beanName
                   *
                   * - beanName: the bean to unregister
                   *
                   *********************************************************/
                      {
                          final boolean bDebug= // true;    // if true, show debug infos
                                                 false;

                            // 2015-08-03, rgf
                          if (args.length!=2)
                          {
                              throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                       "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": invalid # of args; usage: call BSF " +
                                  "'"+args[0]+"', beanName");
                          }


if (bDebug) System.err.println("RexxAndJava()::UNREGISTER_BEAN(): about to unregister=["+args[1]+"]");
                          if (unregisterBean(args[1]))  // bean got unregistered (removed)!
                          {
if (bDebug) System.err.println("RexxAndJava()::UNREGISTER_BEAN(): REMOVED FROM REGISTRY! ["+args[1]+"]");
                                // return beanName (index value), indicate that this is now a mere string
                              return (bsfPrefixReturnValue ? STRING_INDICATOR_PREFIX : "")+args[1];
                          }
                          return makeString4Rexx(this, null);     // return .NIL
                      }
                  }
        // ==================================================================


          case SET_REXX_NULL_STRING :
                  {
                  /* ********************************************************
                   * call BSF 'setRexxNullString', newString
                   *
                   * - newString: string to use for indicating "null" to/by Rexx
                   *              (default is the string ".NIL" in field "null4Rexx"
                   *********************************************************/
                      if(args.length!=2 || args[1]==null)
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": invalid arguments; usage: call BSF " +
                              "'"+args[0]+"', newString");
                      }
                      null4Rexx=args[1];        // set new value
                      // return args[1];
                      return makeString4Rexx(this, args[1]);
                  }
        // ==================================================================


           case BSF_PREFIX_RETURN_VALUE :
                   {
                   /* ********************************************************
                    * object = BSF( 'bsfPrefixReturnValue'[, 0|1] )
                    *
                    * "0" or "1" - if false, do not prefix the returned strings with
                    *              type indicators; else prepend them
                    *
                    * - returns: - the actual value of this setting
                    *********************************************************/

                       if (args.length>2)
                       {
                           throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                    "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" invalid # of args;\nusage: value = BSF( " +
                                   "'"+args[0]+"' [, 0 | 1 ] )");
                       }

                       if (args.length==2)   // no argument given, return current setting
                       {
                           try  // convert to short, if not "0", then true, false else
                           {
                               bsfPrefixReturnValue=((Short.decode(args[1].trim()).shortValue())!=0);
                           }
                           catch (Exception e)
                           {
                               throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                        "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" invalid argument ["+args[1]+"];\nusage: value = BSF( " +
                                        "'"+args[0]+"' [, 0 | 1] ), exception thrown: ["+e.toString()+"]"
                                        , e  // rgf, 2011-06-04: supply Exception object
                                        );
                           }
                       }

                       // return makeString4Rexx( this, new Boolean(bsfPrefixReturnValue) );
                       return makeString4Rexx( this, (bsfPrefixReturnValue ? Boolean.TRUE : Boolean.FALSE) );

                   }
        // ==================================================================
          case CARRY_OUT_SYSTEM_EXIT:
              {
              /* ********************************************************
               * object = BSF( 'carryOutSystemExit'[, 0|1] )
               *
               * "0" or "1" - if false, then the "exit" subfunction is not carried out
               *
               * - returns: - the actual value of this setting
               *********************************************************/

                  if (args.length>2)
                  {
                      throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                               "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" invalid # of args;\nusage: value = BSF( " +
                              "'"+args[0]+"' [, 0 | 1 ] )");
                  }

                  if (args.length==2)   // no argument given, return current setting
                  {
                      try  // convert to short, if not "0", then true, false else
                      {
                          carryOutSystemExit=((Short.decode(args[1].trim()).shortValue())!=0);
                      }
                      catch (Exception e)
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" invalid argument ["+args[1]+"];\nusage: value = BSF( " +
                                   "'"+args[0]+"' [, 0 | 1] ), exception thrown: ["+e.toString()+"]"
                                   , e  // rgf, 2011-06-04: supply Exception object
                                   );
                      }
                  }

                  // return makeString4Rexx( this, new Boolean(carryOutSystemExit) );
                  return makeString4Rexx( this, (carryOutSystemExit ? Boolean.TRUE : Boolean.FALSE) );

              }
   // ==================================================================


          case SLEEP :  // meant for Regina and other Rexxes which don't have a SysSleep() by default
                  {
                  /* ********************************************************
                   * call BSF 'sleep', time2sleep
                   *
                   * - time2sleep: decimal number of seconds to sleep, e.g. '10' (ten seconds),
                   *               '0.5' (half a second), '0.01' (1/100 second), '600' (10 minutes)
                   *********************************************************/

                      if(args.length!=2 || args[1]==null)
                      {
                          throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": invalid arguments; usage: call BSF " +
                              "'"+args[0]+"', time2sleep");
                      }

                      long sleepTime = 0l;

                      try {   // convert received string into a long (rounded up)
                              sleepTime = (long) ((new java.math.BigDecimal(args[1])).doubleValue()*1000+0.5d);
                          }
                      catch (Exception e)
                          {
                              throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                   "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": argument 'time2sleep', value '"+args[1]+"' not numeric, exception thrown: ["+e.toString()+"]"
                                   , e  // rgf, 2011-06-04: supply Exception object
                                   );
                          }

                      Thread thisThread = Thread.currentThread(); // get current Thread
                      try
                      {
                         thisThread.sleep( sleepTime );   // sleep the indicated time
                      }
                      catch (Exception e)
                      {
                         e.printStackTrace();
                      }

                      break;
                  }




        // ==================================================================

           case WRAP_ENUMERATION_OBJECT:
               /*********************************************************
                * value = BSF( 'wrapEnumeration', arrayObject)
                *
                * - arrayObject:   array to be worked upon
                *********************************************************/
               {

                   Object enumObj = null;
                   enumObj=lookupBean(this, args[1]);

                   if (enumObj == null)
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" object ["+args[1]+"] not registered!");
                   }

                   if (!(enumObj instanceof Enumeration))
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" object ["+args[1]+"] does not have the 'Enumeration' interface implemented!");
                   }
                   return makeString4Rexx(this, new EnumerationWrapper( (Enumeration) enumObj) ); // wrap the object and return it to Rexx
               }




               // ==================================================================

           case REXX_HALT :
                   {
                   /* ********************************************************
                    * call BSF 'haltThread', tid
                    *
                    * - tid: BSFGetTID()-value; if "0" all Rexx thread instances
                    *        are halted/terminated
                    *********************************************************/
                       if(args.length!=2 || args[1]==null)
                       {
                           throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                    "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": invalid arguments; usage: call BSF " +
                               "'"+args[0]+"', tid");
                       }

                        // now halt given thread
                       return makeString4Rexx(this, ""+jniRexxSetHalt(args[1]));
                   }


        // ==================================================================
        // ---rgf, 2014-03-29

           case ENV_GET:
           case ENV_SET:
               /*********************************************************
                * value = BSF( 'getEnv', envName)
                * value = BSF( 'setEnv', envName, newValue )
                        -- remove environment variable:
                * value = BSF( 'setEnv', envName) or
                * value = BSF( 'setEnv', envName, null)
                *
                * - arrayObject:   array to be worked upon
                *********************************************************/
               {
                   final int GET_ENVIRONMENT_VARIABLE=1;
                   final int SET_ENVIRONMENT_VARIABLE=2;

                   if ( choice==ENV_GET && (args.length!=2 || args[1]==null ) )
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": invalid arguments; usage: call BSF " +
                           "'"+args[0]+"', envName");
                   }
                   else if (args.length<2 || args.length>3 || args[1]==null )
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": invalid arguments; usage: call BSF " +
                           "'"+args[0]+"', envName [, newValue=.nil]");
                   }

                   String res=null;

                   if (choice==ENV_GET)
                   {
                       res=jniProcEnvironment(GET_ENVIRONMENT_VARIABLE, args[1], null);    // get current value for environment variable
                   }

                   if (choice==ENV_SET)     // set or unset
                   {
                       res=jniProcEnvironment(SET_ENVIRONMENT_VARIABLE, args[1], args.length==2 ? null : args[2]);
                   }

                   return makeString4Rexx( this, res );           // wrap the object and return it to Rexx
               }



               // ==================================================================

                                // 2009-10-17, ---rgf
           case KUDOS     :     // "Easter-egg": return a string of people who have helped the cause of "BSF4ooRexx" one way or the other
                   {
                   /* ********************************************************
                    * call BSF 'kudos'
                    *
                    *********************************************************/
                       if(args.length!=1)
                       {
                           throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                    "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": invalid number of arguments");
                       }

                       String kudos=
                           "Ahammer, Andreas"                       + "\n" +
                           "Augustin, Walter"                       + "\n" +
                           "Baginski, Adrian"                       + "\n" +    // 2016-08-14, rgf: .NET/CLR nutshell examples, 2017: JavaFX, ooRexxTryFX.rxj
                           "Barmwater, Gil"                         + "\n" +
                           "Beck, Andreas"                          + "\n" +
                           "Burger, Martin"                         + "\n" +
                           "Cowlishaw, Mike F."                     + "\n" +
                           "Davis, Chip"                            + "\n" +
                           "Dunkley, Paul"                          + "\n" +    // 2016-08-14, rgf: created s390x version of BSF4ooRexx.cc
                           "Dujis, Erik"                            + "\n" +
                           "Egli, R\u00e4tus"                       + "\n" +
                           "Faucher, Jean-Louis"                    + "\n" +
                           "Flatscher, Alice R."                    + "\n" +
                           "Flatscher, Daniel A."                   + "\n" +
                           "Flatscher, Michael B."                  + "\n" +
                           "Fosdick, Howard"                        + "\n" +
                           "Frysak, Josef"                          + "\n" +
                           "Gerger, Eva"                            + "\n" +    // 2016-08-24, rgf: tester (samples)
                           "Gmeiner, Michael"                       + "\n" +
                           "Gundacker, Dominik"                     + "\n" +
                           "G\u00f6rlich, Gerhard"                  + "\n" +
                           "Gro\u00dfe-Coosmann, Florian"           + "\n" +
                           "Hahnekamp, Rainer"                      + "\n" +
                           "Heinisch, Florian"                      + "\n" +
                           "Hesse, J\u00fcrgen"                     + "\n" +      // 2011-01-24, rgf
                           "Hessling, Mark"                         + "\n" +
                           "Hinz, Michael"                          + "\n" +
                           "Hoisl, Bernhard"                        + "\n" +
                           "Jansen, Ren\u00e9"                      + "\n" +
                           "Kalender, Peter"                        + "\n" +
                           "Kauril, Michael"                        + "\n" +
                           "Ke\u00dfler, Matthias"                  + "\n" +
                           "Lex, Michael"                           + "\n" +
                           "M\u00e4rker, Kurt"                      + "\n" +
                           "Marks, Brian"                           + "\n" +
                           "Matthiesen, Faber"                      + "\n" +
                           "McGuire, Rick"                          + "\n" +
                           "Miesfeld, Mark"                         + "\n" +
                           "M\u00fcller, Franz"                     + "\n" +
                           "Moldaschl, Markus"                      + "\n" +
                           "Nash, Simon"                            + "\n" +
                           "Paar, Manuel"                           + "\n" +      // 2011-01-24, rgf
                           "Pachl, Walter"                          + "\n" +
                           "Peedin, Lee"                            + "\n" +
                           "Pezerovic, Edin"                        + "\n" +
                           "Prem, Matthias"                         + "\n" +
                           "Raffel, Manuel"                         + "\n" +    // 2016-08-24, rgf: original author of .NET-support with CLR.CLS, oorexx.net.*
                           "Ratliff, Matt"                          + "\n" +
                           "Realfsen, \u00c5smund"                  + "\n" +
                           "Reindorf, Julian"                       + "\n" +    // 2016-08-24, rgf: testers (samples)
                           "Richard, Pierre G."                     + "\n" +
                           "Ruggles, David"                         + "\n" +
                           "Sauer, Ralph"                           + "\n" +
                           "Schmid, Stefan"                         + "\n" +
                           "Scholz, Nicole"                         + "\n" +
                           "Small, John"                            + "\n" +
                           "Sonnemans, Freddy"                      + "\n" +
                           "Spanberger, David"                      + "\n" +
                           "Specht, Tobias"                         + "\n" +
                           "St\u00f6hr, Dennis R."                  + "\n" + // 2011-01-24, rgf
                           "Taylor, Pam"                            + "\n" +
                           "Turner, Bill"                           + "\n" + // 2011-04-10, rgf
                           "Waglechner, Christoph"                  + "\n" +
                           "Wilson, Gray (artwork: graphics, icons)"+ "\n" +
                           "Woehr, Jack"                            + "\n" +
                           "Wolfers, Jon (Sahananda)"
                           ;

                        // now halt given thread
                       return makeString4Rexx(this, ""+kudos);
                   }

      // ==================================================================

       case GET_RII_ID  :
               {
               /* ********************************************************
                * call BSF 'get_rii_id'
                * returns the this RII_ID value ("Rexx Interpreter Instance", a string)
                * rgf, 2015-08-03
                *********************************************************/

                   if (args.length > 1)
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                "BSF4ooRexx subfunction "+'"'+args[0]+'"'+" does not expect any argument!");
                   }

                   return makeString4Rexx(this, get_rii_ID());    // return this instance
               }


      // ==================================================================

       case INCREASE_REF_COUNTER :
               {
               /* ********************************************************
                * call BSF 'increaseRefCounter'*, 'beanName'
                *
                *- beanName: the bean's name, mandatory, must exist
                * returns the new refcount
                * rgf, 2015-08-04
                *********************************************************/

                   final boolean bDebug= // true;    // if true, show debug infos
                                         false;

                   if (args.length<2)
                   {
                       throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": invalid # of args; usage: newRefCount=BSF( " +
                               "'"+args[0]+"', beanName");
                   }

                   String beanName=args[1];

                   String str="";

                   synchronized (lockRajRegistry)
                   {
                       RAJBean bean=rajRegistry.get(beanName);   // get bean
                       {
                           if (bean==null)       // no bean stored by that name
                           {
                               throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                        "BSF4ooRexx subfunction "+'"'+args[0]+'"'+": beanName ["+beanName+"] not in registry, cannot increase refCounter!");
                           }

                           bean.refCount+=1;                      // increase refcount
                           str= makeString4Rexx(this, bean.refCount);   // return new refcount for the bean

if (bDebugRefCounter)
{
     if (bean.refCount>3 || rajRegistryToWatch.get(beanName)!=null)
     {
System.err.println("//++// increaseRefCounter/(registerBean), RAJBean: "+bean+" \\\\++\\\\");

        if (rajRegistryToWatch.get(beanName)==null)
        {
            rajRegistryToWatch.put(beanName, bean);
        }
     }
}
                       }

                   }

                   return str;
                   // return makeString4Rexx(bean.refCount);   // return new refcount for the bean

               }




        // ==================================================================
          default:
                  throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
                           "'"+args[0]+"': unknown function name (no such function implemented).");
       }
        // ==================================================================

        return makeString4Rexx(this, null);
    }



     // rgf, 2005-05-07
    /** Returns <code>printStackTrace()</code> as a <code>String</code>.
     *
      * @param e an Exception object.
      * @return  a String
    */
    private String getPrintStackAsString(Exception e)
    {
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       PrintStream           ps   = new PrintStream(baos);
       e.printStackTrace(ps);
       return baos.toString();
    }


    // rgf, 2006-02-15
    /** Invokes method and makes sure that any public method will get successfully invoked.
     *
     * @param m method object to use for invocation
     * @param o object for which the method should get invoked
     * @param params Object[] with parameters to supply
     * @return the result of the method invocation
     */
    Object invokeMethod (Method m, Object o, Object [] params) throws IllegalAccessException, InvocationTargetException
    {
        try
        {

// System.err.println("[[[ ---> RAJ.invokeMethod: o=["+o+"], o.toString()=["+o.toString()+"], method="+m.toString()+"] <--- ]]]");

            return m.invoke(o, params);
        }
        catch (IllegalAccessException e)    // maybe an IllegalAccessException?
        {
            // if ( // bMethodHasSetAccessible &&
            //     Modifier.isPublic(m.getModifiers()) )   // if a public method allow access to it
            // {
                m.setAccessible(true);      // allow unconditional access
                return m.invoke(o, params);
            // }
            // throw e;    // re-throw exception
        }
    }






    /** Converts the string from Rexx to the indicated type. If
      * a type 'O' is indicated and the value for it is an empty string,
      * then <code>null</code> is returned for it.

      * @param type one of
                    &quot;<strong>BO</strong>olean&quot; (i.e. '0' or '1'),
                    &quot;<strong>BY</strong>te&quot;,
                    &quot;<strong>C</strong>har&quot;,
                    &quot;<strong>D</strong>ouble&quot;,
                    &quot;<strong>F</strong>loat&quot;,
                    &quot;<strong>I</strong>nt&quot;,
                    &quot;<strong>L</strong>ong&quot;,
                    &quot;<strong>O</strong>bject&quot; (Object, i.e. a registered bean),
                    &quot;<strong>SH</strong>ort&quot;, and
                    &quot;<strong>ST</strong>ring&quot;.

      * @param value the value from Rexx (a string) to be converted to the
               Java type.
      *
      * @return  the converted value.
      */
    private Object convFromRexx(String type, String value) throws BSFException
    {
        final boolean bDebug=  // true;   // if true, show debug infos
                               false;

if (bDebug) System.err.println("RexxAndJava.convFromRexx(): type=["+type+"], value=["+value+"] value==null: ["+(value==null)+"] null4Rexx=["+null4Rexx+"]");
        if (value==null) return null;

         if (value.equals(null4Rexx))   // Rexx-String indicating "null" ?
            return null;


         if (type.length()>0)
         {
             switch ( Character.toUpperCase(type.charAt(0)) )
             {

                 case 'S':
                            if (type.length() == 1) break;    // leave switch
                            switch (Character.toUpperCase(type.charAt(1)))
                            {
                               case 'T':    // String
                                         return value;

                               case 'H':    // Short
                                         // return new Short( value );
                                         return Short.valueOf( value );

                               default:  break;     // unknown type
                            }

                 case 'O':  // no object indicated, hence return null
                            if (value.equals("")) return null;

                            Object tmp;
                            tmp=lookupBean(this, value);

                            if (tmp==null){
                                throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                                    "Bean ["+value+"] not registered.");
                            }

                            return tmp;

                 // case 'I':  return new Integer      (value);
                 case 'I':  return Integer.valueOf(value);
                 case 'C':
                            // char/Character - deal with the special case that an empty string is supplied, probably
                            //                  because programmer supplied 0x00 on the Rexx side which gets transformed
                            //                  to an empty string in JNI's NewStringUTF()

                            if (value==null || value.length()==0)  // 0x00 from the Rexx side or empty string: deal with as 0x00
                            {

    if (bDebug)
    {
        // Character res=new Character((char)0);
        Character res=Character.valueOf((char)0);
        System.out.println("RexxAndJava.convFromRexx(): new Character: res=["+res+"], Integer.toHexString()=["+Integer.toHexString(res.charValue())+"]");
    }
                                // return new Character((char)0);       // create 0x00 char
                                return Character.valueOf((char)0);       // create 0x00 char
                            }

                            // return new Character    (value.charAt(0));
                            return Character.valueOf(value.charAt(0));

                 case 'B':  // need second character
                            if (type.length() == 1) break;    // leave switch
                            switch (Character.toUpperCase(type.charAt(1)))
                            {
                               case 'O':    // Boolean
                                         // return new Boolean( value.equals("1") || value.equalsIgnoreCase("true") );
                                         if (value.equals("1") || value.equalsIgnoreCase("true"))
                                         {
                                             return Boolean.TRUE;
                                         }
                                         return Boolean.FALSE;

                               case 'Y':    // Byte
                                         // return new Byte( value );
                                         return Byte.valueOf( value );

                               default:  break;     // unknown type
                            }


                 // case 'F':  return new Float        (value);
                 case 'F':  return Float.valueOf (value);
                 // case 'L':  return new Long         (value);
                 case 'L':  return Long.valueOf  (value);
                 // case 'D':  return new Double       (value);
                 case 'D':  return Double.valueOf(value);

             }
         }

            // if we arrive here, we have a problem ...
         throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
             "unknown argType ["+type+"] or incompatible value ["+value+"].");
    }


    /** Converts the string from the JNI to the indicated primitive/wrapper type, using
     *  Java's type indicators.

      * @param type one of
                    &quot;<strong>Z</strong>&quot; Boolean (i.e. '0' or '1'),
                    &quot;<strong>B</strong>te&quot;,
                    &quot;<strong>C</strong>har(acter)&quot;,
                    &quot;<strong>D</strong>ouble&quot;,
                    &quot;<strong>F</strong>loat&quot;,
                    &quot;<strong>I</strong>nt&quot;,
                    &quot;<strong>J</strong>&quot; Long, and
                    &quot;<strong>S</strong>hort&quot;.

      * @param value the value from Rexx (a string) to be converted to the
               Java type.
      *
      * @return  the converted value.
      */
    private Object convFromJNI(String type, String value) throws BSFException
    {
// System.err.println("convFromJNI(type=["+type+"], value=["+value+"])");
        if (value==null || value.length()==0) return null;

         switch (type.charAt(0))    // turn type into a char
         {

             case 'S':  // return new Short( value );   // Short
                        return Short.valueOf( value );  // Short

             // case 'I':  return new Integer      (value);
             case 'I':  return Integer.valueOf(value);
             // case 'C':  return new Character    (value.charAt(0));
             case 'C':  return Character.valueOf(value.charAt(0));

             case 'Z':    // Boolean
                        // return new Boolean( value.equals("1") || value.equalsIgnoreCase("true") );
                        if (value.equals("1") || value.equalsIgnoreCase("true"))
                        {
                            return Boolean.TRUE;
                        }
                        return Boolean.FALSE;

             // case 'B':  return new Byte( value );   // Byte
             case 'B':  return Byte.valueOf (value);   // Byte

             // case 'F':  return new Float        (value);
             case 'F':  return Float.valueOf(value);
             // case 'J':  return new Long         (value);
             case 'J':  return Long.valueOf (value);
             // case 'D':  return new Double       (value);
             case 'D':  return Double.valueOf(value);
         }
         throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
             "unknown return type ["+type+"], with value ["+value+"] (expecting the internal Java type indicator for primitives)");
    }




    /** Replaces a reference to a Java object with the Java object.
     *
     * @param value this String is used to look up the BSF registry. If an entry is found (a
     * Java object) it will get returned, if not (or the value is <code>null</code>) then
     * <code>null</code> is returned.
     *
     * @return the Java <code>object</code> stored in the BSF registry, <code>null</code> else
     **/
    // 20090514, ---rgf, turn to package scope
    Object convFromRexx(String value) throws BSFException
    {
         if (value==null) return null;

         if (value.equals(null4Rexx))   // Rexx-String indicating "null" ?
            return null;

         // Is this a reference to a Java object in the BSF registry?
         // If so, return Java object
         Object tmp=null;
         tmp=lookupBean(this, value);

         if (tmp==null) return value;

         return tmp;
    }


    /** Coerces the arguments from Rexx to the appropriate Java types.
     * @param funcArgs an <code>Object</code> array (actually the Rexx arguments of type
     *        <code>String</code>)
     * @param paramTypes an <code>Class</code> array, containing the Java class objects to which
     *        the Rexx arguments have to be converted to
     * <!-- (rgf, 2003-05-08) -->
     *  @return an array with the coerced arguments, if successful, <code>null</code> else.
     */
    static Object [] coerceArgs(Object [] funcArgs, Class [] paramTypes)
    {
        int k,
            nrArgs=funcArgs.length;

        final boolean bDebug=  // true;
                              false;

if (bDebug) System.err.println("\n---   ---   ---   ---   ---");

        Object [] coercedArgs = new Object [nrArgs];

        for (k=0; k<nrArgs;k++)
        {
if (bDebug) System.err.println("\tcoerceArgs() ---> k="+k+" (funcArgs.length="+funcArgs.length+
                                              ", paramTypes.length="+paramTypes.length+")");

if (bDebug) System.err.println("\t\tfuncArgs[k]=["+funcArgs[k]+"] ---> ["+paramTypes[k]+"]");

            if (funcArgs[k]==null)    // assuming explicit null is o.k. in any case
            {
if (bDebug) System.err.println("\t           ) ---> k=["+k+"]: null!");
                coercedArgs[k]=null;
            }

                // rgf, 2005-06-18, special case found: an interface is given, that is available for String as well!
            else if ( paramTypes[k].isInterface()  || (!(funcArgs[k] instanceof String)))
            {

if (bDebug) System.err.print("\t           ) ---> k="+k+": an Object not a String in hand: funcArgs ["+
                                          funcArgs[k]+"] to paramTypes ["+paramTypes[k]+"], assignable?");
// rgf, 2005-10-32                if (!(paramTypes[k].isAssignableFrom (funcArgs[k].getClass()))) // not compatible, break
if (bDebug)
{
    if (paramTypes[k].isArray()) {
        System.out.println("\n\t\t==> ==> ["+paramTypes[k]+"]: paramTypes[k].isArray=true, class=["+paramTypes[k].getClass()+"]");

        // System.out.println("\t\tgetLength(): "+((Array)paramTypes[k]).getLength());
        System.out.println("\t\t==> ==> ["+funcArgs[k]+"]: funcArgs[k].getClass()="+funcArgs[k].getClass());


System.out.println("paramTypes[k] instanceof (String): "+paramTypes[k] instanceof String);
System.out.println("funcArgs[k] instanceof (String):   "+funcArgs[k] instanceof String);
try{
    ArrayWrapper paw=new ArrayWrapper(paramTypes[k]);
    ArrayWrapper faw=new ArrayWrapper(funcArgs[k]);

    System.out.println("paw=["+paw+"], dimensions="+paw.dimensions+", componentType=["+paw.componentType+"]");
    System.out.println("faw=["+faw+"], dimensions="+faw.dimensions+", componentType=["+faw.componentType+"]");
}
catch (Exception e)
{}

    }
}

                if (!( ( (Class<?>)paramTypes[k]).isAssignableFrom (funcArgs[k].getClass()))) // not compatible, break
                {
                    // if (paramTypes[k].isPrimitive())
                    // rgf, 20100519 all primitive->wrappers get intercepted here: this allows to interchange primitive with matching wrapper types
                    if (
                          (paramTypes[k]==boolean.class  && funcArgs[k].getClass()==Boolean.class  ) ||
                          (paramTypes[k]==byte.class     && funcArgs[k].getClass()==Byte.class     ) ||
                          (paramTypes[k]==char.class     && funcArgs[k].getClass()==Character.class) ||
                          (paramTypes[k]==short.class    && funcArgs[k].getClass()==Short.class    ) ||
                          (paramTypes[k]==int.class      && funcArgs[k].getClass()==Integer.class  ) ||
                          (paramTypes[k]==long.class     && funcArgs[k].getClass()==Long.class     ) ||
                          (paramTypes[k]==float.class    && funcArgs[k].getClass()==Float.class    ) ||
                          (paramTypes[k]==double.class   && funcArgs[k].getClass()==Double.class   )
                       )
                    {
                        coercedArgs[k]=funcArgs[k];
            if (bDebug) System.err.println("\t           ) ---> k="+k+": successful coercion.");
                        continue;
                    }
if (bDebug) System.err.println(" NO ! ");

                    break;
                }
if (bDebug) System.err.println(" yes. ");

                coercedArgs[k]=funcArgs[k];
if (bDebug) System.err.println("\t           ) ---> k="+k+": successful coercion.");
                continue;
            }
            else
            {
                String tmpArg=(String) funcArgs[k];  // ---rgf, for {B|b}oolean coercion

                  // do we need a String, if so, we have it already!
                if (paramTypes[k]==String.class) {
                    coercedArgs[k]=funcArgs[k];
if (bDebug) System.err.println("\t           ) ---> k="+k+": successful coercion to String.");
                    continue;
                }

// ---> NetRexx coercion, if NetRexx.class is available <---
                 // do we need a netrexx.lang.Rexx instance?
                if (nlR_class!=null && paramTypes[k]==nlR_class) {
                    // try to create an instance of netrexx.lang.Rexx
                    try {
                        Class argClass[]={funcArgs[k].getClass()};  // determine class of argument
                        Constructor constr=((Class<?>)paramTypes[k]).getConstructor( argClass );

                        Object argRgf[]={funcArgs[k]};              // create argument array
                        coercedArgs[k]=constr.newInstance(argRgf);  // assign coerced object

if (bDebug) System.err.println("\t           ) ---> k="+k+": successful coercion to netrexx.lang.Rexx.");
                        continue;
                    }
                    catch (Exception e)
                    {
                        ;
                    }
                }

                  // try to co-erce argument
                TypeConvertor cvtor = glob_tcr.lookup (String.class, paramTypes[k]);
                if (cvtor==null)   // no convertor found, break; look for another method
                {
if (bDebug) System.err.println("\t           ) ---> k="+k+": no cvtor found! paramTypes[k]: ["+paramTypes[k]+")");
                    break;
                }


                    // BSF uses Boolean.valueOf() which is far too unrestricted !
                if (paramTypes[k]==boolean.class || paramTypes[k]==java.lang.Boolean.class)   // ---rgf, 2005-07-07: make sure Boolean are converted correctly
                {
if (bDebug) System.err.println("\t  >>> >>>  checking for {B|b}oolean value!");
                    // ---rgf, 2006-05-29: be stricter than Java, only accept "0", "1", "false", "true", ".false" and ".true" as Boolean's
                    // String s=(String)funcArgs[k];   // coerce to a string

                    // if (tmpArg.equals("0") || tmpArg.equalsIgnoreCase(".FALSE") || tmpArg.equalsIgnoreCase("FALSE"))
                    if (tmpArg.equals("0"))         // "false" in ooRexx
                    {
                        tmpArg="false";     // a Boolean representing false
if (bDebug) System.err.println("\t\t\t>>>  tmpArg=\"false\"!");
                    }

                    // else if (tmpArg.equals("1") || tmpArg.equalsIgnoreCase(".TRUE") || tmpArg.equalsIgnoreCase("TRUE"))
                    else if (tmpArg.equals("1"))    // "true" in Rexx
                    {
                        tmpArg="true";      // a Boolean representing true
if (bDebug) System.err.println("\t\t\t>>>  tmpArg=\"true\"!");
                    }
                    else                    // cannot be considered to be a Boolean
                    {
if (bDebug) System.err.println("\t\t\t>>>  Not a Boolean --> BREAK!");
                        break;
                    }
                }

                // BSF uses str.charAt(0), accepting any string, which is far too unrestricted !
                // a char/Character is only acceptable if the (Rexx) string consists of exactly one char
                if (paramTypes[k]==char.class || paramTypes[k]==java.lang.Character.class)   // ---rgf, 2005-07-07: make sure a Character is converted correctly
                {
if (bDebug) System.err.println("\t  >>> >>>  checking for {char|Character} value!");
                    // ---rgf, 2006-05-29: be stricter than Java, only accept "0", "1", "false", "true", ".false" and ".true" as Character's

                    if (tmpArg.length()!=1)     // a char or Character must be exactly of size 1 !
                    {
if (bDebug) System.err.println("\t\t\t>>>  Not a Character --> BREAK!");
                        break;
                    }
                }

                    // 2003-09-10, make sure to intercept exceptions while converting
                    // Object o= cvtor.convert( String.class, paramTypes[k], funcArgs[k]);
                Object o=null;
                try
                {
                    // o=cvtor.convert( String.class, paramTypes[k], funcArgs[k]);
                    o=cvtor.convert( String.class, paramTypes[k], tmpArg);
if (bDebug) System.err.println("\t           coercion succssful, o=["+o+"], type: ["+o.getClass()+"]. <--- <--- <---");

                }
                catch (Exception e)
                {
                   o=null;
                }


                if (o==null) // co-ercion failed: this is the wrong method, hence break!
                {
if (bDebug) System.err.println("\t           ) ---> k="+k+": coercion yielded 'null', breaking!!");
                   break;
                }
                coercedArgs[k]=o; // save coerced object instead
            }
        }

if (bDebug) System.err.println("\tcoerceArgs() ---> k="+k+" nrArgs="+nrArgs);

        if (k==nrArgs)    // no break, hence all arguments co-erced successfully!
        {
if (bDebug) System.err.println("\tcoerceArgs(): SUCCESSFUL! :-)");
            return coercedArgs;

        }

if (bDebug) System.err.println("\tcoerceArgs(): UNsuccessful :-( ---> returning 'null'!");
        return null;        // coercion was *not* successful, hence return nothing
    }

    /** Allow lookup of BSFRegistry entries.
     *
     * @param beanName string denoting the index into the BSFRegistry
     * @return the Java object matching <code>beanName</code>, if not found <code>null</code>
     *
     * @since 2012-02-19
     */
    static protected Object lookupBean(RexxAndJava rajo, String beanName)
    {
        final boolean bDebug=false;

if (bDebug) System.err.println(rajo+".lookupBean():     beanName=["+beanName+"]");

        RAJBean bean=rajRegistry.get(beanName);

if (bDebug && bean==null)
{
System.err.println("//*...*// attempt #1: lookupBean(beanName=["+beanName+"]): NOT FOUND! \\\\...\\\\");
}
            // bean not found, maybe still has prefix ?
        if (bean==null && rajo.bsfPrefixReturnValue && beanName.startsWith(OBJECT_INDICATOR_PREFIX))
        {
            String name=beanName.substring(3);  // remove prefix from bean
            bean=rajRegistry.get(name);         // try to get now

if (bDebug && bean==null)
{
    System.err.println("//*...*// attempt #2: lookupBean(beanName=["+beanName+"],boolean): bean=["+bean+"] \\\\...\\\\");
}

        }

if (bDebug==true)
{
    System.err.println("RAJ: lookupBean(\""+beanName+"\"): bean=["+bean+"], bean.javaObject.getClass()=["+
                       (bean==null ? null :
                                    (bean.javaObject==null ? null : bean.javaObject.getClass())
                       )
                       +"]");
}



        return (bean==null ? null : bean.javaObject);
    }



    /** Allow JNI to easily lookup the BSFRegistry.
     *
     * @param beanName string denoting the index into the BSFRegistry
     * @return the Java object matching <code>beanName</code>, if not found <code>null</code>
     */
    private Object lookupBean4JNI(String beanName)
    {
        final boolean bDebug=false;

if (bDebug) System.err.println(this+".lookupBean4JNI(): beanName=["+beanName+"], bTerminated=["+bTerminated+"]");

        if (bTerminated==true)
        {
            // return null;
            return (bsfPrefixReturnValue ? OBJECT_INDICATOR_PREFIX : "")+null4Rexx;                     // return the Rexx-String representing "null"
        }

        return lookupBean(this, beanName);
    }



    /** Allow JNI to easily register a bean.
     *
     * @param object a Java object which should be registered
     * @return beanName used to store and needed for retrieving the object in/from the BSFRegistry
     */
    private String registerBean4JNI(Object object)
    {
// rgf, 201507
if (bDebugRefCounter==true)
System.err.println(this+":  registerBean4JNI(object=["+object+"]): object.getClass()->["+(object==null? null : object.getClass())+"]");

        if (bTerminated==true)
        {
            return (bsfPrefixReturnValue ? OBJECT_INDICATOR_PREFIX : "")+null4Rexx;                     // return the Rexx-String representing "null"
        }
// rgf, 201507
if (bDebugRefCounter==true)
System.err.println(this+":  registerBean4JNI(object=["+object+"]): object.getClass()->["+(object==null? null : object.getClass())+"]");

        return registerBean(this, makeBeanName(this, object), object);
    }



    /** This method registers an <em>object</em> with the given <em>beanName</em> with
     *  the BSF registry, if not registered yet. In addition a reference counting is
     *  carried out.
     *
      *  @param beanName name of the object to be registered
      *
      *  @param object to be registered
      *
      *  @return returns the beanName
      */
    static protected String registerBean(RexxAndJava rajo, String beanName, Object object)
    {

        if (rajo.bTerminated==true)
        {
            return (rajo.bsfPrefixReturnValue ? OBJECT_INDICATOR_PREFIX : "")+null4Rexx;                     // return the Rexx-String representing "null"
        }

final boolean bDebug=false;
if (bDebug)
{
    System.err.print(rajo+".registerBean(beanName=["+beanName+"], object=["+object+"]: ");
}

        synchronized (lockRajRegistry)
        {
            RAJBean bean = rajRegistry.get( beanName );  // already registered?
            if (bean==null)                  // we need to create the bean
            {
                bean=new RAJBean(beanName, object);
                rajRegistry.put(beanName, bean);
if (bDebug) System.err.print("did not exist, CREATED!");
            }
            else    // exists already, increase refCounter
            {
                bean.refCount+=1;
if (bDebug) System.err.print("existed alread, refCount increased");
            }

if (bDebug) System.err.println(" -> bean.refCount=["+bean.refCount+"]");


if (bDebugRefCounter)
{

    if (object instanceof String)
    {
        String str=(String) object;
        int len=str.length();
        System.err.println("/-->/-->/storing a STRING /-->/["+str.substring(0, (len<51 ? len-1 : 50) ) +"] \\<--\\<--\\");
    }
    if (bean.refCount>3 || rajRegistryToWatch.get(beanName)!=null)
    {
        System.err.println("//++// registerBean, RAJBean: "+bean+" \\\\++\\\\ (AFTER increasing refCount=["+bean.refCount+"])");
        if (rajRegistryToWatch.get(beanName)==null)
        {
            rajRegistryToWatch.put(beanName, bean);
        }
    }
}

        }
        return (rajo.bsfPrefixReturnValue ? OBJECT_INDICATOR_PREFIX : "")+beanName;   // if so, return beanName

    }



    /** This method replaces an already registered Java <em>object</em> with the one supplied as the argument.
     *
      *  @param beanName name of the object to be replaced
      *  @param object that replaces the current
      *
      *  @return returns the beanName
     * @since 2016-12-19
      */
    protected String rawRegisterBean(String beanName, Object object)
    {
final boolean bDebug=false; // true;
if (bDebug)
{
    System.err.println("RAJ.-RAW-rawRegisterBean(beanName=["+beanName+"], object=["+object+"]: ");
}
        RAJBean bean = null;
        synchronized (lockRajRegistry)
        {
            bean=rajRegistry.get( beanName );  // already registered?
            if (bean==null)                  // we need to create the bean
            {
                bean=new RAJBean(beanName, object);
                rajRegistry.put(beanName, bean);
            }
            else    // exists already, replace Java object in bean with the supplied one, increase refCounter
            {
                bean.javaObject=object;
                bean.refCount+=1;
            }
        }


if (bDebugRefCounter) // && bean.refCount>3)
{
    System.err.println("//++// rawRegisterBean, RAJBean: "+bean+" \\\\++\\\\ (AFTER increasing refCount)");
}

        return (bsfPrefixReturnValue ? OBJECT_INDICATOR_PREFIX : "")+beanName;   // if so, return beanName
    }


    /** This method allows for unregistering Beans, taking into account reference counters.
      * Only if the reference counter is 0, will the given Bean be unregistered. (2003-01-06, ---rgf)
      *
      *  @param beanName name of the Bean to be unregistered
      *
      *  @return returns <code>true</code>, if the Bean exists, <code>false</code> else.
      */
    protected boolean unregisterBean(String beanName )
    {

        final boolean bDebug= // true;
                              false;

if (bDebug==true)
System.err.println("RAJ:   ===> RexxAndJava.unregisterBean("+beanName+"): bTerminated=["+bTerminated+"] ...");


/* // rgf, 2016-11-30: even if the Rexx engine is terminated, unregisterBean remains valid, reducing the refCount
   // of the registered Java object
        if (bTerminated==true)
        {
            return false;
        }
*/

// FIXME: remove output after debugging
if (bDebug==true && bTerminated==true)
{
    System.err.println("RAJ:   ===> RexxAndJava.unregisterBean(): ***************** ***************** bTerminated=true !!!! beanName=["+beanName+"] ***************** *****************");
}


if (bDebug) // testing; TODO: remove
{
    System.err.println("RAJ:   ===> RexxAndJava.unregisterBean(beanName="+beanName+")");
}


            synchronized (lockRajRegistry)
            {
                RAJBean bean = rajRegistry.get( beanName );
// if (bDebugRefCounter && (bean.refCount>3 || rajRegistryToWatch.get(beanName)!=null))
if (bDebugRefCounter && rajRegistryToWatch.get(beanName)!=null)
System.err.println("RAJ:   ===> RexxAndJava.unregisterBean("+beanName+"), bean.refCount=["+bean.refCount+"]  ...");

                // bean not found, maybe still has prefix ?
                if (bean==null && bsfPrefixReturnValue && beanName.startsWith(OBJECT_INDICATOR_PREFIX))
                {
                    beanName=beanName.substring(3);  // remove prefix from bean
                    bean=rajRegistry.get(beanName);
                }

                if (bean==null)
                {
if (bDebugRefCounter) System.err.println("RAJ:   ===> RexxAndJava.unregisterBean("+beanName+"), not in registry anymore!");

                    return false;
                }

if (bDebugRefCounter)
{
    // if (bean.refCount>3 || rajRegistryToWatch.get(beanName)!=null)
    if (rajRegistryToWatch.get(beanName)!=null)
    {
    System.err.println("//--// RAJ.unregisterBean(), decreasing refCount! bean=["+bean+"], new refCount=["+(bean.refCount-1)+"] \\\\--\\\\");
    }
}
                bean.refCount-=1;       // reduce counter
                if (bean.refCount==0)   // dropped to 0, remove bean
                {

if (bDebugRefCounter)
{
    if (rajRegistryToWatch.get(beanName)!=null)
    {
        String str=" ";

        if (bean.javaObject instanceof RexxProxy)
        {
            try {
                str="["+((RexxProxy) bean.javaObject).sendMessage0("STRING")+"] ";
            }
            catch (Exception exc)
            {}
        }
        System.err.println("//--// RAJ.unregisterBean(), REMOVING FROM REGISTRY! beanName=["+beanName+"], bean.refCount=["+bean.refCount+"] "+str+"\\\\--\\\\");
        rajRegistryToWatch.remove(beanName);
    }
    else    // TODO: 20161220, rgf: temporaryly show all removals!
    {
        System.err.println("//--// RAJ.unregisterBean(), REMOVING FROM REGISTRY! beanName=["+beanName+"], bean.refCount=["+bean.refCount+"] \\\\--\\\\");
    }
}

                    rajRegistry.remove(beanName);
                }
            }

            return true;
    }



    /** This method determines what String content to return; if a non-primitive Java object
     *  a unique beanName is created and used to save the Java object in the BSFRegistry,
     *  returning the beanName.
      *
      *  @param o the object which should get returned as a string to Rexx.
      *
      *  @return  a String rendering of the primitive types and String.
      *           Otherwise create a unique bean name, register the object
      *           under it and return the bean name. If <code>null</code>,
      *           then the string of {@link #null4Rexx} is returned to Rexx.
      */
    static protected String makeString4Rexx(RexxAndJava rajo, Object o)  // changed 2003-01-06 from "makeResultString()" to "makeString4Rexx()"
    {
        final boolean bDebug=  // true;     // if true, show debug infos
                               false;

if (bDebug)
{
    System.err.println("makeString4Rexx() 1a: o=["+o+"]:");
    System.err.println("makeString4Rexx() 1b: o.getClass()=\""+(o==null?null:o.getClass())+"\", o=["+(o==null ? null : o.toString())+"]...");
}

       if (o==null)
       {
          return (rajo.bsfPrefixReturnValue ? OBJECT_INDICATOR_PREFIX : "")+null4Rexx;                     // return the Rexx-String representing "null"
       }

       Class cl = o.getClass();

if (bDebug)
{
    if (cl==Character.class) {System.err.println("makeString4Rexx(): charValue="+((Character) o).charValue());}
}

       if (cl==String.class)
       {

if (bDebug) System.err.println("makeString4Rexx(): TSK # 1");
          return (rajo.bsfPrefixReturnValue ? STRING_INDICATOR_PREFIX : "")+((String) o);
       }

       else if (cl==Boolean.class )
       {
          return (rajo.bsfPrefixReturnValue ? STRING_INDICATOR_PREFIX : "")+( ((Boolean) o).booleanValue() ? "1" : "0");
       }

       else if (    cl.isPrimitive() ||
                cl==Integer.class    || cl==Character.class  ||
                cl==Long.class       ||
                cl==Float.class      || cl==Double.class     ||
                cl==Byte.class       || cl==Short.class
               )
       {
          return (rajo.bsfPrefixReturnValue ? STRING_INDICATOR_PREFIX : "")+o.toString();  // o.k.
       }

         // are we returning a netrexx.lang.Rexx instance, if so, use its String value
        if (nlR_class!=null && cl==nlR_class) {
            return (rajo.bsfPrefixReturnValue ? STRING_INDICATOR_PREFIX : "")+o.toString();
        }

                // an Object, register it as a bean, so it becomes
                // possible to refer to from Rexx as well

       String beanName = makeBeanName(rajo, o);
       return registerBean(rajo, beanName, o);
    }



    // rgf, 2018-01-05: consequence of making utility methods static
    /** This method will be fetched and excercised from native code.
     */
    private String makeString4Rexx(Object o)
    {
        return makeString4Rexx(this,o);
    }



    /** This method creates an identifer (unique) string value for the passed in object.
      *
      *  @param o the object for which a unique string value should be created
      *
      *  @return  <code>null</code>, if the argument was <code>null</code>,
     *            else a String, identifying (uniquely) the object for use with the BSF registry
      */
    static public String makeBeanName(RexxAndJava rajo, Object o)
    {
        final boolean bDebug=  // true;   // if true, show debug infos
                              false;

        if (o==null)
        {
            return null;
        }


            // rgf, 2009-07-13: all method objects of the same name have the same hashCode() (does
            //                  not apply to constructor objects only!), hence need for using
            //                  System.identityHashCode() instead
        String beanName="" + o.getClass().getName() + '@' + Integer.toHexString( System.identityHashCode(o) );

            // rgf, 2012-11-01: handle case where System.identityHashCode() returns duplicates
        Object olb=null;
        olb=lookupBean(rajo, beanName);

        if (olb!=null && o!=olb)   // oops, we got a duplicate identityHashCode for the same class!
        {   // make it into an identifier (unique) string
            beanName=beanName+"_"+Integer.toHexString(++spilloverCounter);
if (bDebug)  System.err.println("makeBeanName(): duplicate identityHashCode, spilloverCounter used, now beanName=["+beanName+"]");
        }

        return beanName;
    }


    /** Called by {@link RexxEngine.terminate()}, Will free the references to {@link BSFManager} and {@link RexxEngine}.
     *
     *  {@link public boolean isTerminated()} to test whether the Rexx interpreter instance of
     *  this {@link RexxEngine} got terminated.
     *
     * To halt all Rexx threads of this engine's Rexx interpreter instance peer use
     * {@link #halt()}.
     *
     * @since 2013-06-25
    */
    synchronized protected void terminate()
    {
        final boolean bDebug = // true;
                               false;

if (bDebug) System.err.println("*** TERMINATE *** bTerminated=["+bTerminated+"] *** rii_ID=["+rii_ID+"] *** TERMINATE *** *** TERMINATE *** *** TERMINATE *** *** TERMINATE *** *** TERMINATE *** *** TERMINATE ***");
        if (bTerminated==false) // if we did not run yet (eg. invoked by finalize() )
        {
            bTerminated=true;       // from now on do not use BSFManager and RexxEngine related methods
            rexxEngine =null;       // nullify, release reference
            mgr        =null;       // nullify, release reference
        }
    }

    /** Getter method for field <code>bTerminated</code>.
     *
     * @return <code>true</code>, if Rexx engine got successfully terminated, <code>false</else>
     *
     * @since 2013-06-25
     */
    public boolean isTerminated()
    {
        return bTerminated;
    }



/* up to 2009-07-13
    public String makeBeanName(Object o)
    {
        if (o==null)
        {
            return null;
        }

        String beanName = "" + o.getClass().getName() + '@' + Integer.toHexString( o.hashCode() );

        // java.lang.reflect.Constructor returns the same hashCode()-value for all of its instances!
        if (o instanceof Constructor)
        {
            // add the hashCode()-value from the string value to create a unique bean name
            beanName=beanName+"_"+Integer.toHexString(o.toString().hashCode());
        }
        return beanName;
    }
*/



   /** Class to exit the JVM, will be executed concurrently, sleeps and then
     * <code>calls System.exit(exit_number)</code>. This allows the Rexx side
       to gain time to shutdown properly.
     */
   class ExitDelayed implements Runnable
   {
         /** Holds the return value to be returned.             */
         int exitCode=0;

         /** Holds the time in milliseconds to wait before terminating the virtual machine.
             This should allow Rexx time to shutdown properly.  */
         long sleepTime=100l;

         /** Saves the <code>exitCode</code>. */
         ExitDelayed (int exitCode, long sleepTime)
         {
            this.exitCode  = exitCode;  // save exitCode
            this.sleepTime = sleepTime; // save time to sleep before terminating the JVM
         }

         /** This makes this thread runnable. Will wait {@link #sleepTime} and
           * terminates the Java virtual machine with {@link #exitCode}.
           */
         public void run() // throws InterruptedException
         {
            Thread thisThread = Thread.currentThread(); // get current Thread
            try
            {
               mgr.terminate();         // let the BSF manager "terminate gracefully"
               thisThread.sleep( sleepTime );   // sleep the indicated time
            }
            catch (InterruptedException e)
            {
               e.printStackTrace();
            }
            finally                     // in any case, kill the JVM!
            {
              System.exit(exitCode);    // now kill JVM
            }
         }
   }




    /** Inner class to implement a Synchronized ReaderWriter, where a
      * read on an empty Vector blocks the read, until a write occurs (with optional timeout).
      */
    protected class SyncPutGet
    {
        /** Vector to store the text sent by the eventListeners. */
        private Vector<Object> eventTextVector     = new Vector<Object>();      // normal priority
        private Vector<Object> eventTextVectorLow  = new Vector<Object>();      // low ("batch") priority
        private Vector<Object> eventTextVectorHigh = new Vector<Object>();      // high ("alarm") priority


        /** Get an object from the vector. If empty, block for the
          * given time.
          *
          * @param millis time to wait in milliseconds, if 0 waits until an object becomes available.
          *
          * @return the first object from the vector or <code>null</code>, if
          *         empty.
          */
        synchronized Object get (long millis) throws java.lang.InterruptedException
        {
            if ( eventTextVector    .isEmpty() &&
                 eventTextVectorLow .isEmpty() &&
                 eventTextVectorHigh.isEmpty()    )     // empty, block until filled
            {
               wait(millis);
            }

            if (eventTextVector    .isEmpty() &&
                eventTextVectorLow .isEmpty() &&
                eventTextVectorHigh.isEmpty()    )     // empty, return null
            {
// System.err.println("RexxAndJava.SyncPutGet.get(), about to return, nothing in any queue.");
                return null;    // awakened due to time-out?
            }

            Object o;
                // return the eventText in the order: High-Normal-Batch
            if ( ! eventTextVectorHigh.isEmpty())       // Highest priority!
            {
                // for supporting JRE 1.1.8, ---rgf, 2001-05-26
               o=eventTextVectorHigh.firstElement();
                 eventTextVectorHigh.removeElementAt(0);
            }

            else if ( ! eventTextVector.isEmpty() )     // Normal priority
            {
                // for supporting JRE 1.1.8, ---rgf, 2001-05-26
               o=eventTextVector.firstElement();
                 eventTextVector.removeElementAt(0);
            }

            else   // default: now lowest priority's turn
            {
                // for supporting JRE 1.1.8, ---rgf, 2001-05-26
               o=eventTextVectorLow.firstElement();
                 eventTextVectorLow.removeElementAt(0);
            }

            return o;
        }

        /** Adds an object to the vector and makes sure that waiting threads
          * are notified.
          *
          * @param o object to add to the vector.
          *
          * @param priority an integer value indicating the priority of the posted
          *        event, one of:

          *        <p><ul>
                   <li>&quot;<code>2</code>&quot;: high ('alarm level') priority.
                   <li>&quot;<code>1</code>&quot;: normal (<strong>default</strong>) priority, and
                   <li>&quot;<code>0</code>&quot;: low ('batch level') priority,
                   </ul>
          *
          */
        synchronized void put (Object o, int priority)
        {
            switch (priority)
            {
               case  2:         // add the element to the high priority vector
                           eventTextVectorHigh.addElement(o);   // changed to addElement() for JRE 1.1.8, ---rgf
                  break;

               case  0:         // add the element to the low priority vector
                           eventTextVectorLow.addElement(o);    // changed to addElement() for JRE 1.1.8, ---rgf
                  break;

               case  1:         // add the element to the normal priority vector
               default:
                           eventTextVector.addElement(o);       // changed to addElement() for JRE 1.1.8, ---rgf
                  break;
            }

            notifyAll();        // now notify all
        }
    }

    /** 'Pretty print': returns string value of supplied object enclosed in square brackets. */
    String pp(Object o)
    {
        if (o==null) return "<null>";
        return "["+o+"]";
    }

    // 2009-05-01
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


    static private String getCauseAsString (String title, Exception e)
    {
        final boolean bDebug= // true;    // if true, show debug infos
                              false;

        Throwable c=null;

        if (c instanceof InvocationTargetException) // rgf, 2010-03-27
        {
            c=((InvocationTargetException)e).getTargetException();   // unwrap and show exception the invoked method has thrown

if (bDebug) System.err.println("***debug RexxAndJava.getCauseAsString(): InvocationTargetException=["+e+"]\n"+
                   "\t--> getTargetException()=["+c+"]\n"+
                   "\t--> getCause()          =["+e.getCause()+"]");
        }

        if (c==null)
        {
            c=e.getCause();
        }

        if (c==null)
        {
            return "";
        }
        return title+"["+c.toString()+"]";
    }


    static private void RgfMsgBox (String message, char kind) {

        final JFrame frame = new JFrame("RexxAndJava - RgfMsgBox");

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        frame.addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing(WindowEvent e) {
                    // System.exit(0);     // kill JVM ???
                    frame.dispose();
                    return;
                }
            }
        );

        if (kind=='1')  // information
        {
            JOptionPane.showMessageDialog(frame, message,"information", JOptionPane.INFORMATION_MESSAGE);
        }
        else
        {
            JOptionPane.showMessageDialog(frame, message,"error"      , JOptionPane.ERROR_MESSAGE      );
        }

        frame.dispose();
    }




    /** Creates a new Rexx interpreter instance using the ooRexx 4.0 API <code>RexxCreateInterpreter()</code>.
     *  This method honors optional Rexx interpreter instance settings, such that a default
     *        command environment can be defined, as well as Rexx command handlers
     *        (and eventually Rexx exit handlers) implemented in Java.
     *
     * @return a String representing the created Rexx interpreter instance ID or <code>null</code>, if
     *         no Rexx interpreter instance could be created
     *
     * @since 2012-02-04
    */
    protected String createRexxInterpreterInstance(RexxConfiguration rc) throws BSFException
    {
final boolean bDebug= // true;
                      false;

        if (rii_ID!=null)   // already created for this RAJO!
        {
            throw new BSFException("A Rexx interpreter instance has been created already having the ID ["+rii_ID+"]");
        }

        // --rgf, 2014-04-02: now initialization takes place in constructor, such that calling jniProcEnvironment() can be carried out without a physical Rexx interpreter instance
        // jniSetupNativeJVMPointer();         // make sure C++ side gets a JVM pointer

        Object options[]=null;
        if (rc!=null)
        {
            rc.modifiable=false;            // from now on do not allow any changes to the structure of options
            options=rc.getArguments4JNI();  // get arguments for native code to configure the Rexx instance
        }

        String tmp_rii_ID=jniRexxCreateInterpreterInstance(options);  // create interpreter instance
        set_rii_ID(tmp_rii_ID);             // set the returned rii_ID


if (bDebug) {System.err.println(this+".createRexxInterpreterInstance(...), returning rii_ID=["+tmp_rii_ID+"]"); }

        return tmp_rii_ID;
    }



//
    /** This class maintains the name, the Java object denoted by it and the reference count of the Java object.
     *  The name will be used as the key.
     *
     * @since 2016-11-27
      */
    public static class RAJBean    // "RexxAndJava"-Bean
    {
            // package private
        /** The name used to store the bean in the registry. */
        String name=null;

        /** The Java object (&quot;Java bean&quot;).
         */
        Object javaObject=null; // the Java object

        /** Serves as the reference count field for maintenance purposes of the {@link rajRegistry}.
         */
        int refCount=1;         // allows to maintain the reference count value for this particular bean

        /* No default constructor for this class. */
        private RAJBean()
        {}

        /** Constructor.
         *
         * @param name the name used for the Java object
         * @param javaObject the Java object
         */
        public RAJBean(String name, Object javaObject)
        {
            this.javaObject=javaObject;
            this.name=name;
        }

        public Object getJavaObject()
        {
            return javaObject;
        }
        public int getRefCount()
        {
            return refCount;
        }
        public String toString()
        {
            return "RAJBean@"+Integer.toHexString( System.identityHashCode(this) )+"[name="+name+",javaObject="+javaObject+",refCount="+refCount+"]";
        }

/*
public void finalize()
{
    System.err.println("RAJBean.finalize(): name=["+name+"], refCount=["+refCount+"], javaObject=["+javaObject+"]");
}
*/

    }

}
/*
javah -jni  org.rexxla.bsf.engines.rexx.RexxAndJava
javap -s -p org.rexxla.bsf.engines.rexx.RexxAndJava
*/
