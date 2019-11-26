package org.rexxla.bsf.engines.rexx;

import java.util.*;
import java.io.*;
import org.apache.bsf.*;
import org.apache.bsf.util.BSFEngineImpl;
import org.rexxla.bsf.engines.rexx.RexxAndJava;

import java.lang.reflect.Method;

// TODO: rgf, 20191126  - process declaredBeans
//                      - RexxEngine local declaredBeans (as HashMap?)
//                      - change raj.lookupBean() accordingly


/**
  * This class manages the calls from Java via JNI to Open Object Rexx (ooRexx).
  *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2001-2019 Rony G. Flatscher
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
  * @version 6.0.0, 20191126
  * @author Rony G. Flatscher (<a href="http://www.wu-wien.ac.at">WU-Wien/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu-wien.ac.at/english">http://www.wu-wien.ac.at/english</a>)
  *
  */


/*
    last change: $Revision: 546 $ $Author: rony $ $Date: 2009-10-20 20:45:19 +0200 (Tue, 20 Oct 2009) $

    2019-11-26, ---rgf, added HashMap<String,BSFDeclaredBean> engineDeclaredBeans to maintain
                        BSFManager's declareBean(bean) and undeclareBean(name) invocations; will
                        be looked up by RexxAndJava.lookupBean(), if beanName not found in its
                        registry

    2019-11-25, ---rgf, the Apache BSF taglib uses BSFManager.declareBean() to register Java objects
                        for the scripts, therefore declareBean() and undeclareBean() must honor
                        their invocations in order to make those beans available to ooRexx
                        - declareBean(): now registers BSFDeclaredBean with RexxAndJava
                        - undeclareBean(): now unregisters the bean with RexxAndJava

    2019-08-13, ---rgf, - fix Javadoc warnings

    2018-02-23, ---rgf, - changed logic in unregisterBean() to adapt to change in RexxAndJava.unregisterBean return type (now int)

    2018-01-05, ---rgf, - adapt RexxAndJava method invocations where methods got turned into static ones (e.g. registerBean)

    2016-12-20, ---rgf, - methods that supply a Java methodObject or javaObject via jniRexxSendMessageToRexxObject,
                          will now increase the respective bean's refCount by one as the native C++ method
                          jniRexxSendMessageToRexxObject will use bsf.wrap to create a Rexx .BSF object which will
                          use unregisterBean upon running its uninit method

    2016-11-19, ---rgf, - updated major version number to 6 to indicate new base level Java 1.6/6

    2016-11-04, ---rgf, - basing on Java 1.6/6.0, getting rid of "-Xlint:unchecked" warnings:
                            RexxEngine.java:517: warning: [unchecked] unchecked call to addElement(E) as a member of the raw type java.util.Vector
                                                   beanVector.addElement(res[1]);        // remember beanName for later deregistering
                                                                        ^
                            RexxEngine.java:662: warning: [unchecked] unchecked call to addElement(E) as a member of the raw type java.util.Vector
                                                   beanVector.addElement(res[1]);       // remember beanName for later deregistering
                                                                        ^
                            RexxEngine.java:842: warning: [unchecked] unchecked call to addElement(E) as a member of the raw type java.util.Vector
                                                   beanVector.addElement(res[1]);       // remember beanName for later deregistering
                                                                        ^
                            RexxEngine.java:1087: warning: [unchecked] unchecked call to addElement(E) as a member of the raw type java.util.Vector
                                                      beanVector.addElement(res[1]);    // remember beanName for later deregistering

    2015-07-17, ---rgf, - fixed bug in call() where a dynamically created bean name containing the
                          object prefix was not removed for unregisterBean()

    2015-05-09, ---rgf, - added public methods
                            - getLocalEnvironment()   ... returns .local
                            - getPublicEnvironment()  ... returns .environment
                            - getNil()                ... returns .nil
                            - getInterpreterVersion() ... returns long, where the least significant three bytes contain the version encoded
                            - getLanguageVersion()    ... returns long, where the least significant two bytes contain the version encoded

    2014-07-03, ---rgf, - make sure that RexxProxy objects are always passed as Java objects in arguments;
                          adapt apply() as well to pass through RexxProxy objects in arguments

    2014-03-30, ---rgf, - add undocumented pass-thru method procEnvironment(...) to allow a last resort means
                          (allow Java to set/get process environment variables via JNI using RexxAndJava)

    2013-06-29, ---rgf, - get isTerminated() back into the code; was mistakingly deleted between rev 137 and 148

    2013-06-24, ---rgf, - terminate() now invokes the new RexxAndJava.terminate() in order to allow it
                          to release references to BSFManager and RexxEngine, afterwards releasing
                          the reference to its RexxAndJava peer;
                          experimental: if trying to terminate the primodal Rexx interpreter instance a
                          warning will get issued on System.err that informs the reader, that Java cannot
                          use the primodal Rexx interpreter instance anymore; in effect the primodal
                          Rexx interpreter instance does never get terminated, such that the native code
                          has it always available for its own purposes

                        - added bJNITerminated which is set to true, if a Rexx interpreter instance
                          got terminated via JNI; bTerminated logically terminates the usage of a
                          RexxEngine (usually changed by the peer RexxAndJava object's "exit" subfunction

    2013-06-10, ---rgf, - now terminating RexxAndJava peer using its new method terminate(),
                          then nullifying references to it

    2013-06-10, ---rgf, - added public getter method isTerminated()
                        - added "synchronized" keyword to terminate();
                          note: the primodal Rexx interpreter instance is never terminated, use isTerminated()
                          to check for that after terminating a Rexx engine

    2012-02-19, ---rgf, - added public methods registerBean(), unregisterBean(), lookupBean() as
                          pass-through methods for the protected RexxAndJava methods

    2012-02-06, ---rgf, - jniRegisterBSF() moved to RexxAndJava with the new name jniSetupNativeJVMPointer()
                        - private field "rexxConfig" and getter method "getRexxConfig()" added, supplying it
                          for creating a Rexx interpreter instance
                        - changed 'private' to 'protected' for field 'bTerminated' to allow RexxAndJava's [BSF.]EXIT
                          to block callbacks from Java to Rexx
    2012-02-04, ---rgf, - changed code to allow for delayed creation of a Rexx interpreter instance (RII),
                          which is meant to allow configuring a Rexx engine instance via the field
                          rexxInterface (a RexxAndJava object) for Java implemented Rexx exit and
                          command handlers;

     // =====================================
     2009-08-27, ---rgf, - added the argument "returnType" to rexxInterface.jniRexxSendMessageToRexxObject()
     2009-07-12, ---rgf, - renamed argument "methodDescription" to "methodDescriptor" for
                           jniRexxSendMessageToRexxObject()
     2009-07-11, ---rgf, - added arguments "javaObject" and "methodDescription" to
                           jniRexxSendMessageToRexxObject()
     2009-06-27, ---rgf, - added method call() expecting a method object, such that InvocationHandler usage
                           allows to process (and forward) the supplied method object as well
     2009-06-23, ---rgf, - removed passing "createdTID" to callback directory object
     2009-06-08, ---rgf, - adjusted to use the new BSF4Rexx 4.0 API based JNI functions
                         - removed ThreadedJNIRexxStart(): too complicated, can always be (easily) done
                           from Java
                         - folded apply's invocations of Rexx script (depending whether args were given or not)
                           into one common invocation

     2009-05-04, ---rgf, - added fields "bTerminated" (indicates whether the Rexx interpreter instance
                         - has been terminated and is not accessible anymore), "rii_ID" (the Rexx interpreter
                           instance pointer value as a string, as supplied by JNI)
                         - implementation of "call()" now allows to invoke any Rexx method with any arguments,
                           as long as a RexxProxy object is submitted; this implements the callback functionality
                           with the help of the new ooRexx 4.0 APIs


     // ===================================== pre-4.0 editions
     2009-04-02, ---rgf, add a terminate() method to halt/terminate the Rexx interpreter instance,
                         terminate(tid), terminateAllRexxThreads()
     2009-02-10, ---rgf, escaping strings which begin with OBJECT_INDICATOR_PREFIX with
                         STRING_INDICATOR_PREFIX, using RexxAndJava's makeString4Rexx()
                         (Java objects/beans get always processed using RexxAndJava.makeString4Rexx())

     2008-08-14, ---rgf, added ability to execute the Rexx interpreter on a separate Java thread;
                         this was an experiment for allowing the current BSF4Rexx.cc logic to
                         remain unchanged, when OpenOffice.org (OOo) 2.4.1 invokes another
                         Rexx script in the same thread another invocation of a Rexx script
                         has been running already; unfortunately OOo hangs if Rexx scripts are
                         dispatched on a separate Java thread;
                         additions left in code in case some use case mandates/eases the
                         deployment of Rexx scripts on their own Java thread (just use the
                         RexxEngine instance method "setThreadedRexxStart(true)"
     2008-06-14, ---rgf, passing "scriptSourceLocation" to jniRexxStart()
     2007-10-13, ---rgf, updated hint in RexxEngine.call() to point to ooRexx, instead of the frozen IBM interpreter
     2007-09-21, ---rgf, updated comments and version number
     2006-12-22, ---rgf, fixed a bug in apply() when source is null (reported by Markus Auchmann, Austria)

     2006-02-21, ---rgf, fixed a bug in the event text handling (index out of range); thanks to Lee Peedin
                         for pointing that out
     2005-12-28, ---rgf, changed license to Apache
 */
public class RexxEngine extends BSFEngineImpl {
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static final public String version = "600.20191126";

    /** If terminate() was invoked, then this instance must not be used anymore.
     */
    protected boolean bTerminated=false;

    /** Getter method for field <code>bTerminated</code>.
     *
     * @return <code>true</code>, if Rexx engine got successfully terminated, <code>false</else>
     */
    public boolean isTerminated()
    {
       return bTerminated;
    }

    /** Will be set to true, if terminate() used JNI to successfully terminate the Rexx interpreter
     *  instance.
     * @since 2013-06-25
     */
    protected boolean bJNITerminated=false;

    /** Contains the RexxInterpreter instance ID this engine is associated with.
     */
    private String rii_ID=null;

    /** Getter method.
     *  @return the RexxInterpreter instance ID
     */
    // protected String get_rii_ID()
    public String get_rii_ID()
    {
        return rii_ID;
    }

    /** Setter method. Only meant to be used by Java4Rexx.java.
     */
    protected void set_rii_ID(String rii_ID)
    {
        this.rii_ID=rii_ID;
    }

    /** Rexx configuration object, that determines the options with which a
     *  Rexx interpreter instance should get created.
     */
    private RexxConfiguration rexxConfig=new RexxConfiguration();

    /** Getter method.
     * @return the RexxConfiguration object of this Rexx engine
     */
    public RexxConfiguration getRexxConfiguration()
    {
        return rexxConfig;
    }


    private final boolean bDebug= false ; // 20040404, ---rgf
                                  // true;

    /** Allows using the Rexx interpreter from Java. */
    // private RexxAndJava   rexxInterface;

    private RexxAndJava   rexxInterface;  // allow access to package programs


    /** Returns the {@link RexxAndJava} instance that is used for interfacing with Rexx.
    */
    public RexxAndJava getRexxInterface()   // TODO: should it be set to protected instead?
    {
        return rexxInterface;
    }

    /** Stores the script code supplied with eval(), exec(), apply(), in order to be
     *  able to locate the code in call(). [Hint: Xalan as per 2002-09 uses BSF in
     *  its extension mechanism such, that it first calls exec() to have the script
     *  pre-compiled and thereafter uses call() to invoke the script.
     */
    private String lastScriptCode = null;


    /** Stores the script source information (e.g. fully qualified path to script)
     *  supplied with eval(), exec(), apply(), in order to be
     *  able to locate the code in call().
     */
    private String lastSourceLocation=null;


    /** Stores the declared beans supplied by BSFManager in the initialize() method.
     */
    HashMap<String,BSFDeclaredBean> engineDeclaredBeans = new HashMap();

    /** Initializes the Rexx engine and causes the external
      * functions 'BSF' and 'BSFPollEvents' to be registered with the
      * Rexx interpreter. Therefore Rexx programs invoked from Java
      * have these two functions available to them and are therefore
      * able to communicate with Java.
      */
    public void initialize (BSFManager mgr, String lang,
                            Vector declaredBeans) throws BSFException
    {
        super.initialize (mgr, lang, declaredBeans);
        rexxInterface = new RexxAndJava(mgr, this);

if (bDebug)
{
    Iterator it = declaredBeans.iterator();
    System.err.println("RexxEngine.initialize(), dumping supplied Vector 'declaredBeans':");
    while (it.hasNext())
    {
        Object el = it.next();
        System.err.println("\t... it.next()=["+el+"] .getClass().getName()=["+el.getClass().getName()+"]");
    }
}


        Iterator<BSFDeclaredBean> it = declaredBeans.iterator();
        while (it.hasNext())
        {
            this.declareBean(it.next());
        }


        // rexxInterface.jniRegisterBSF();

/*
if (bDebug) System.err.println("===> RexxEngine().initialize(): before ===> rexxInterface.jniRegisterBSF()");
    //    rexxInterface.jniRegisterBSF();
try
{
        rexxInterface.jniRegisterBSF();
}
catch (Exception e)
{
    System.err.println("...> RexxEngine().initialize(): exception occured=["+e+"]");
    e.printStackTrace();
}
*/

/* rgf, 2012-02-04
if (bDebug) System.err.println("===> RexxEngine().initialize(): before ===> rii_ID=["+rii_ID+"]");

        this.rii_ID=rexxInterface.jniRexxCreateInterpreterInstance();  // create interpreter instance
if (bDebug) System.err.println("===> RexxEngine().initialize(): after  ===> rii_ID=["+rii_ID+"]");
*/
    }



    /** Get the ooRexx <code>.local</code> directory.
     *
     *  @return a <code>RexxProxy</code> allowing access to the ooRexx <code>.local</code> directory
     *  @since 2015-05-09
     */
    public Object getLocalEnvironment()
    {

if (bDebug==true) System.err.println(this+".getLocalEnvrionment(), before doing a \"return rexxInterface.jniGetLocalEnvironment(rii_ID);\" ...");

        if (rii_ID==null)   // Rexx interpreter instance (RII) not yet created, create it!
        {
            throw new NullPointerException("\"rii_ID\" (Rexx interpereter instance ID) null, Rexx interpreter instance not yet created (just eval() or apply() a script))");
        }

        return rexxInterface.jniGetLocalEnvironment0(rii_ID);
    }

    /** Get the ooRexx <code>.environment</code> directory.
     *
     *  @return a <code>RexxProxy</code> allowing access to the ooRexx <code>.environment</code> directory
     *  @since 2015-05-09
     */
    public Object getGlobalEnvironment()
    {
        if (rii_ID==null)   // Rexx interpreter instance (RII) not yet created, create it!
        {
            throw new NullPointerException("\"rii_ID\" (Rexx interpereter instance ID) null, Rexx interpreter instance not yet created (just eval() or apply() a script))");
        }
        return rexxInterface.jniGetGlobalEnvironment0(rii_ID);
    }

    /** Get the ooRexx <code>.nil</code> object.
     *
     *  @return a <code>RexxProxy</code> representing the ooRexx <code>.nil</code> sentinel object
     *  @since 2015-05-09
     */
    public Object getNil()
    // Object getNil()
    {
        if (rii_ID==null)   // Rexx interpreter instance (RII) not yet created, create it!
        {
            throw new NullPointerException("\"rii_ID\" (Rexx interpereter instance ID) null, Rexx interpreter instance not yet created (just eval() or apply() a script))");
        }
        return rexxInterface.jniNil0(rii_ID);
    }


   /** (Copied from the ooRexx <code>rexxpg.pdf</code> documentation) &quot;Returns
    *  the version of the interpreter. The returned version is
    * encoded in the 3 least significant bytes of the returned value,
    * using 1 byte each for the interpreter version, release, and revision
    * values. For example, on a 32-bit platform, this value would be
    * <code>0x00040000</code> for version 4.0.0.&quot;
    *
    * @return the ooRexx interpreter version
     *  @since 2015-05-09
    */
    public long getInterpreterVersion()
    {
        if (rii_ID==null)   // Rexx interpreter instance (RII) not yet created, create it!
        {
            throw new NullPointerException("\"rii_ID\" (Rexx interpereter instance ID) null, Rexx interpreter instance not yet created (just eval() or apply() a script))");
        }
        return rexxInterface.jniInterpreterVersion0(rii_ID);
    }


   /** (Copied from the ooRexx <code>rexxpg.pdf</code> documentation) &quot;Returns the language level of
    *  the interpreter. The returned language level is encoded in the 2 least
    *  significant bytes of the returned value, using 1 byte each for the
    *  interpreter version, release, and revision values. For example, on a 32-bit
    *  platform, this value would be <code>0x00000603</code> for language level 6.03.&quot;
    *
    * @return the ooRexx language level
     *  @since 2015-05-09
    */
    public long getLanguageLevel()
    {
        if (rii_ID==null)   // Rexx interpreter instance (RII) not yet created, create it!
        {
            throw new NullPointerException("\"rii_ID\" (Rexx interpereter instance ID) null, Rexx interpreter instance not yet created (just eval() or apply() a script))");
        }
        return rexxInterface.jniLanguageLevel0(rii_ID);
    }





    /** Evaluates (executes) a piece of Rexx code
      * (can be a whole program as well)
      * <em>without</em> any Java-supplied arguments and returns the result.
      *
      * @param source if set to {@link RexxAndJava#EVENT_TEXT},
      *               then <code>oscript</code> is put into the event queue
      *               to be polled by Rexx. If this text starts with the
      *               string <code>&quot;&#47;*2*&#47;&quot;</code> it is
      *               put into the alarm (highest dispatch) level vector, if it starts with the
      *               string <code>&quot;&#47;*0*&#47;&quot;</code> it is
      *               put into the batch (lowest dispatch) level vector,
      *               else it is put into the normal level vector.
      *
      * @param lineNo not used.
      * @param columnNo not used.
      * @param oscript the String version of this object [using toString()]
      *                represents the Rexx code to be executed.
      *                Its statements are delimited with a ';' or a
      *                CR (carriage-return) and/or LF (line-feed).
      *
      * @return a String object with the result from Rexx or <code>null</code>.
      */
    public Object eval (String source, int lineNo, int columnNo, Object oscript)
                                        throws BSFException
   {
if (bDebug) System.err.println(this+" ===> ===> ===> RexxEngine.eval() - begin...");

        if (bTerminated==true)       // engine got terminated, cannot use it anymore
        {
            throw new RexxException("RexxEngine "+this+" got terminated, cannot be used anymore.");
        }

        // rgf, 2012-02-04
        if (rii_ID==null)   // Rexx interpreter instance (RII) not yet created, create it!
        {
if (bDebug) System.err.println("===> RexxEngine(), creating RII: before ===> rii_ID=["+rii_ID+"]");
            this.rii_ID=rexxInterface.createRexxInterpreterInstance(rexxConfig);
if (bDebug) System.err.println("===> RexxEngine(), creating RII: after  ===> rii_ID=["+rii_ID+"]");
        }


        String script = oscript.toString ();

        if(source!= null && source.equals(RexxAndJava.EVENT_TEXT))     // dispatching eventListener text
        {
            if (script.startsWith("/*2*/"))                    // alarm (highest) level
            {
                rexxInterface.eventTextList.put(script, 2);
            }
            else if (script.startsWith("/*0*/"))               // batch (lowest) level
            {
                rexxInterface.eventTextList.put(script, 0);
            }
            else                                      // normal level (default)
            {
                rexxInterface.eventTextList.put(script, 1);       // put text into normal level event text vector
            }

            return null;
        }
        else
        {
           lastScriptCode=script;       // save script's code, fall 2002, ---rgf: adjusts for some usage patterns
           lastSourceLocation=source;       // save script source info, 2008-06-14

           Object obj=null;

if (bDebug) System.err.println("===> RexxEngine.eval()-1 - before: rexxInterface.jniRexxRunProgram (...)");
           obj=rexxInterface.jniRexxRunProgram(
                   rii_ID,                 // RexxInstance instance ID
                   2,                      // invocationType,         // determines whether "CallProgram", "LoadPackage" or "LoadPackageFromData" is to be used
                   source,                 // String  fileName,               // filename which contains the program to run
                   script,                 // String  programData,            // Rexx code to execute; if given "fileName" denotes a (symbolic) name for this code
                   null                    // Object[]args                    // arguments
                   );
if (bDebug) System.err.println(this+" <=== <=== <=== RexxEngine.eval()-1 - AFTER: rexxInterface.jniRexxRunProgram (...), obj=["+obj+"]");

           return obj;
        }
    }




    /** Calls (executes) a Rexx program which code has been supplied before via an invocation of
     *  &quot;apply()&quot;,
     *  &quot;exec()&quot; or &quot;eval()&quot; with the Java-supplied arguments and returns
     *  the result.
     *  <p>[Hint: This follows after learning how Xalan is employing BSF: it first calls &quot;exec()&quot;
     *  and then &quot;call()&quot;, hence &quot;call()&quot; does <em>not</em> receive the source
     *  to be executed.]
      *
       <p>
     * As of 2009-05-17 using the new ooRexx 4.0 APIs the following fundamental change has
     * been implemented: if the argument <code>object</code> is an instance of {@link org.rexxla.bsf.engines.rexx.RexxProxy},
     * then a callback on the RexxProxy object is carried out, using <code>method</method> as the
     * ooRexx message name, supplying arguments, if any. Otherwise, the original behaviour takes place.
     *
     * <p><em>Note:</em> if the RexxProxy's <code>userDataID</code> field is not set (i.e. <code>null</code>),
     * then no slot argument will be supplied as the last argument to the Rexx method.
     *
      * @param object if an instance of RexxProxy, then a callback is carried out
      * @param name if given, name of the message to send to the ooRexx object
      * @param args arguments to be passed to Rexx, if any
      *
      * @return a String object with the result from Rexx or <code>null</code>.
      */
    public Object call (Object object, String name, Object[] args)
                        throws BSFException
    {

if (bDebug) System.err.println(this+" ===> ===> ===> RexxEngine.call(Object object, String name, Object[] args) - begin...");

        if (bTerminated==true)       // engine got terminated, cannot use it anymore
        {
            throw new RexxException("RexxEngine "+this+" got terminated, cannot be used anymore.");
        }

        // rgf, 2012-02-04
        if (rii_ID==null)   // Rexx interpreter instance (RII) not yet created, create it!
        {
if (bDebug) System.err.println(this+" ===> RexxEngine(), creating RII: before ===> rii_ID=["+rii_ID+"]");
            this.rii_ID=rexxInterface.createRexxInterpreterInstance(rexxConfig);
if (bDebug) System.err.println(this+" ===> RexxEngine(), creating RII: after  ===> rii_ID=["+rii_ID+"]");
        }

        // String editedArgs[] = new String [ (args!= null ? args.length : 0) ];
        Object editedArgs[] = null;

        if (args!=null)                 // if arguments given, create array with needed dimension
        {
            editedArgs= new Object[args.length];
        }

        // 2003-01-06, ---rgf, deal with arguments (non-primitive Java objects) which need to be turned into Beans
        // Vector beanVector = new Vector(args.length);   // maximum size
        // Vector beanVector = new Vector(editedArgs.length);   // maximum size
        Vector<Object> beanVector = new Vector<Object>();
        Object [] res;                  // to contain the results of args2RexxString()

            // if true, then pass RexxProxy objects as is (will be dealt with in JNI)
        boolean bIs40Callback= (object instanceof RexxProxy);


        for (int i=0 ; i < (args != null ? args.length : 0) ; i++)   // turn argument objects into string arguments for Rexx
        {
           Object argVal=args[i];
           if (argVal!=null)
           {
               // if (bIs40Callback && argVal instanceof RexxProxy)    // leave RexxProxy arguments unchanged
               if (argVal instanceof RexxProxy)    // leave RexxProxy arguments unchanged
               {
                   editedArgs[i]=argVal;
               }
               else
               {
                   res = args2RexxString( argVal );         // get String or Bean for this argument
                   if (((Boolean) res[0]).booleanValue())    // a Bean had to be created
                   {
if (bDebug) System.err.println(this+".call(): args2RexxString() created bean: ["+res[1]+"], have to unregister later ...");
                       beanVector.addElement(res[1]);        // remember beanName for later deregistering
                   }
                   editedArgs[i]=(String) res[1];             // String value for Rexx
               }
           }
           else editedArgs[i]=null;
        }

            // lastScriptCode only contains code, if apply(), eval() or exec() was called already
        Object o=null;

            // if (object instanceof RexxProxy)    // new 4.0 API behaviour
        if (bIs40Callback)                  // new 4.0 API behaviour
        {
if (bDebug) System.err.println(this+" RexxEngine.call(): jniRexxSendMessageToRexxObject() ...");
// System.err.println("RE.call(...) v# 1, name=["+name+"]");
            o=rexxInterface.jniRexxSendMessageToRexxObject (
                ((RexxProxy) object).rexxInterpreterID,   // RexxInstance instance ID
                ((RexxProxy) object).rexxObjectID,        // RexxProxy objectID
                ((RexxProxy) object).rexxUserDataID,      // RexxProxy userData_ID
//                ((RexxProxy) object).creationTID,         // RexxProxy creationTID
                null,                       // javaObjectBean (a String)
                null,                       // methodObjectBean (a String)
                name,                       // message name (determines name of method to invoke)
                null,                       // methodDescription (a String), not used here
                editedArgs,                 // arguments, edited for native layer
                null                        // unknown return type, hence any is o.k.
                );
        }
        else    // old, pre-4.0 behaviour
        {
if (bDebug) System.err.println(this+" RexxEngine.call(): jniRexxRunProgram() ...");
            o=rexxInterface.jniRexxRunProgram (
                            rii_ID,             // RexxInstance instance ID
                            2,                  // invocationType: call program from buffer
                            lastSourceLocation, // filename (source)
                            lastScriptCode,     // programData
                            editedArgs          // arguments
                            );
        }

        // now deregister Beans
        Enumeration e=beanVector.elements();    // get elements for Enumeration
        while ( e.hasMoreElements() )
        {
            String el=(String) e.nextElement();

if (bDebug) System.err.println(this+".call(o,n,a): unregisterBean 1: ["+el+"] ----> "+"rexxInterface.bsfPrefixReturnValue=["+rexxInterface.bsfPrefixReturnValue+"]");

            if (rexxInterface.bsfPrefixReturnValue==true)
            {
if (bDebug) System.err.println(this+".call(): unregisterBean 2a: ["+el.substring(3)+"] ----> "+"rexxInterface.bsfPrefixReturnValue=["+rexxInterface.bsfPrefixReturnValue+"]");
                rexxInterface.unregisterBean( el.substring(3));  // remove "<O>" or "<S>"
            }
            else
            {
if (bDebug) System.err.println(this+".call(): unregisterBean 2b: ["+el+"] ----> "+"rexxInterface.bsfPrefixReturnValue=["+rexxInterface.bsfPrefixReturnValue+"]");
                rexxInterface.unregisterBean( el);
            }
        }

if (bDebug) System.err.println(this+" <=== <=== <=== RexxEngine.call(Object object, String name, Object[] args) - end, returning ["+o+"]...");
        return o;
    }



    /** Allows forwarding a messages to a RexxProxy using the method name of the supplied
     *  <code>methodObject</code> object with the supplied <code>arguments</code>, if any. This
     *  method will be mostlikely invoked employing a {@link java.lang.reflect.Proxy} object.
      *
       <p>
     * As of 2009-05-17 using the new ooRexx 4.0 APIs the following fundamental change has
     * been implemented: if the argument <code>object</code> is an instance of {@link org.rexxla.bsf.engines.rexx.RexxProxy},
     * then a callback on the RexxProxy object is carried out, using <code>method</method> as the
     * ooRexx message name, supplying arguments, if any. Otherwise, the original behaviour takes place.
     *
      *
      * @param proxy a RexxProxy to which the callback is to be carried out
      * @param methodObject the java.lang.reflect.Method object that is supposed to be invoked originally
      * @param args arguments to be passed to Rexx, if any
      *
      * @return a String object with the result from Rexx or <code>null</code>.
      */
    public Object call (RexxProxy proxy, Method methodObject, Object[] args)
                        throws BSFException
    {
if (bDebug) System.err.println(this+" ===> ===> ===> RexxEngine.call(RexxProxy proxy, Method methodObject, Object[] args) - begin...");

        if (proxy==null)
        {
            throw new RexxException("'proxy' argument must not be null!");
        }

        if (methodObject==null)
        {
            throw new RexxException("'methodObject' argument must not be null!");
        }

        if (bTerminated==true)       // engine got terminated, cannot use it anymore
        {
            throw new RexxException("RexxEngine "+this+" got terminated, cannot be used anymore.");
        }

        // rgf, 2012-02-04
        if (rii_ID==null)   // Rexx interpreter instance (RII) not yet created, create it!
        {
if (bDebug) System.err.println(this+" ===> RexxEngine(), creating RII: before ===> rii_ID=["+rii_ID+"]");
            this.rii_ID=rexxInterface.createRexxInterpreterInstance(rexxConfig);
if (bDebug) System.err.println(this+" ===> RexxEngine(), creating RII: after  ===> rii_ID=["+rii_ID+"]");
        }

        String name=methodObject.getName();         // get method name (will be used as message)

        // String methodObjectBean=rexxInterface.makeString4Rexx(methodObject);    // create beanName
        String methodObjectBean=RexxAndJava.makeString4Rexx(rexxInterface, methodObject);    // create beanName
        // 20161220, rgf: increase refCount by one as the C++ method will use wrap.bsf() such that .bsf uninit will run and do a unregisterBean
        if (rexxInterface.bsfPrefixReturnValue==true)
        {
            // rexxInterface.registerBean(methodObjectBean.substring(3), methodObject);    // remove "<O>" prefix
            RexxAndJava.registerBean(rexxInterface, methodObjectBean.substring(3), methodObject);    // remove "<O>" prefix
        }
        else
        {
            // rexxInterface.registerBean(methodObjectBean, methodObject);
            RexxAndJava.registerBean(rexxInterface, methodObjectBean, methodObject);
        }


        // String editedArgs[] = new String [ (args!= null ? args.length : 0) ];
        Object editedArgs[] = null;

        if (args!=null)                 // if arguments given, create array with needed dimension
        {
            editedArgs= new Object[args.length];
        }

        // 2003-01-06, ---rgf, deal with arguments (non-primitive Java objects) which need to be turned into Beans
        // Vector beanVector = new Vector(args.length);   // maximum size
        // Vector beanVector = new Vector(editedArgs.length);   // maximum size
        Vector<Object> beanVector = new Vector<Object>();
        Object [] res;                  // to contain the results of args2RexxString()


        for (int i=0 ; i < (args != null ? args.length : 0) ; i++)   // turn argument objects into string arguments for Rexx
        {
           Object argVal=args[i];
           if (argVal!=null)
           {
               if (argVal instanceof RexxProxy)             // leave RexxProxy arguments unchanged
               {
                   editedArgs[i]=argVal;
               }
               else
               {
                   res = args2RexxString( argVal );         // get String or Bean for this argument
                   if (((Boolean) res[0]).booleanValue())   // a Bean had to be created
                   {
                       beanVector.addElement(res[1]);       // remember beanName for later deregistering
                   }
                   editedArgs[i]=(String) res[1];           // String value for Rexx
               }
           }
           else editedArgs[i]=null;
        }


            // determine result type, if primitive type or wrapper class
        String resultType=null;     // null indicates any object
        Class rt=methodObject.getReturnType();

        if (rt==boolean.class || rt==Boolean.class)
        {
            resultType="Z";
        }
        else if (rt==int.class || rt==Integer.class)
        {
            resultType="I";
        }
        else if (rt==long.class || rt==Long.class)
        {
            resultType="J";
        }
        else if (rt==byte.class || rt==Byte.class)
        {
            resultType="B";
        }
        else if (rt==char.class || rt==Character.class)
        {
            resultType="C";
        }
        else if (rt==short.class || rt==Short.class)
        {
            resultType="S";
        }
        else if (rt==float.class || rt==Float.class)
        {
            resultType="F";
        }
        else if (rt==double.class || rt==Double.class)
        {
            resultType="D";
        }


        Object o=null;      // returned object

        // if (object instanceof RexxProxy)    // new 4.0 API behaviour
if (bDebug) System.err.println(this+" RexxEngine.call(p,m,a): jniRexxSendMessageToRexxObject() ...");
// System.err.println("RE.call(...) v# 2, proxy=["+proxy+"], name=["+name+"]");
        o=rexxInterface.jniRexxSendMessageToRexxObject (
            proxy.rexxInterpreterID,   // RexxInstance instance ID
            proxy.rexxObjectID,        // RexxProxy objectID
            proxy.rexxUserDataID,      // RexxProxy userData_ID
//             proxy.creationTID,         // RexxProxy creationTID
            null,                       // javaObjectBean (a String)
            methodObjectBean,           // methodObjectBean (a String)
            name,                       // message name (determines name of method to invoke)
            null,                       // methodDescription (a String), not used here
            editedArgs,                 // arguments, edited for native layer
            resultType                  // the expected (primitive) return type, or null else
            );

        // now deregister Beans
        Enumeration e=beanVector.elements();    // get elements for Enumeration
        while ( e.hasMoreElements() )
        {
            // rexxInterface.unregisterBean( (String) e.nextElement());
            String el=(String) e.nextElement();

if (bDebug) System.err.println(this+".call(): unregistering: ["+el+"] ----> "+"rexxInterface.bsfPrefixReturnValue=["+rexxInterface.bsfPrefixReturnValue+"]");

            if (rexxInterface.bsfPrefixReturnValue==true)
            {
if (bDebug) System.err.println(this+".call(): unregistering: ["+el.substring(3)+"] ----> "+"rexxInterface.bsfPrefixReturnValue=["+rexxInterface.bsfPrefixReturnValue+"]");
                rexxInterface.unregisterBean( el.substring(3));  // remove "<O>" or "<S>"
            }
            else
            {
if (bDebug) System.err.println(this+".call(): unregistering: ["+el+"] ----> "+"rexxInterface.bsfPrefixReturnValue=["+rexxInterface.bsfPrefixReturnValue+"]");
                rexxInterface.unregisterBean( el);
            }
        }

        if (rexxInterface.bsfPrefixReturnValue==true)
        {
            rexxInterface.unregisterBean(methodObjectBean.substring(3));    // remove "<O>" prefix
        }
        else
        {
            rexxInterface.unregisterBean(methodObjectBean);
        }

if (bDebug) System.err.println(this+" <=== <=== <=== RexxEngine.call(RexxProxy proxy, Method methodObject, Object[] args) - end, returning ["+o+"]...");
       return o;
    }


    /** Allows forwarding a messages to a RexxProxy using the supplied <code>methodName</code>
        the supplied <code>arguments</code>, if any. This method will be mostlikely invoked
        employing a RexxProxy object in the context of a dynamically created (proxy) Java class
     * (e.g. an abstract class, where the abstract methods will forward their invocation to the
     * RexxProxy object).
      *
      * @param proxy a RexxProxy to which the callback is to be carried out
      * @param javaObject object that invoked this callback
      * @param methodName this name is used as the message to be sent to the Rexx object
      * @param methodDescriptor the Java internal representation of the method's arguments and
     *         return type, if any
      * @param args arguments to be passed to Rexx, if any
      *
      * @return a String object with the result from Rexx or <code>null</code>.
      */
    public Object call (RexxProxy proxy, Object javaObject, String methodName, String methodDescriptor, Object[] args)
                        throws BSFException
    {
if (bDebug) System.err.println(this+" ===> ===> ===> RexxEngine.call(RexxProxy proxy, Object javaObject, String methodName, String methodDescriptor, Object[] args) - begin...");

        if (proxy==null)
        {
            throw new RexxException("'proxy' argument must not be null!");
        }

        if (javaObject==null)
        {
            throw new RexxException("'javaObject' argument must not be null!");
        }

        if (methodName==null)
        {
            throw new RexxException("'methodName' argument must not be null!");
        }

        if (bTerminated==true)       // engine got terminated, cannot use it anymore
        {
            throw new RexxException("RexxEngine "+this+" got terminated, cannot be used anymore.");
        }

        // rgf, 2012-02-04
        if (rii_ID==null)   // Rexx interpreter instance (RII) not yet created, create it!
        {
if (bDebug) System.err.println(this+" ===> RexxEngine(), creating RII: before ===> rii_ID=["+rii_ID+"]");
            this.rii_ID=rexxInterface.createRexxInterpreterInstance(rexxConfig);
if (bDebug) System.err.println(this+" ===> RexxEngine(), creating RII: after  ===> rii_ID=["+rii_ID+"]");
        }


        // String javaObjectBean=rexxInterface.makeString4Rexx(javaObject);
        String javaObjectBean=RexxAndJava.makeString4Rexx(rexxInterface, javaObject);
        // 20161220, rgf: increase refCount by one as the C++ method will use wrap.bsf() such that .bsf uninit will run and do a unregisterBean
        if (rexxInterface.bsfPrefixReturnValue==true)
        {
            // rexxInterface.registerBean(javaObjectBean.substring(3), javaObject);    // remove "<O>" prefix
            RexxAndJava.registerBean(rexxInterface, javaObjectBean.substring(3), javaObject);    // remove "<O>" prefix
        }
        else
        {
            // rexxInterface.registerBean(javaObjectBean, javaObject);
            RexxAndJava.registerBean(rexxInterface, javaObjectBean, javaObject);
        }

        // String editedArgs[] = new String [ (args!= null ? args.length : 0) ];
        Object editedArgs[] = null;

        if (args!=null)                 // if arguments given, create array with needed dimension
        {
            editedArgs= new Object[args.length];
        }

        // 2003-01-06, ---rgf, deal with arguments (non-primitive Java objects) which need to be turned into Beans
        // Vector beanVector = new Vector(args.length);   // maximum size
        // Vector beanVector = new Vector(editedArgs.length);   // maximum size
        Vector<Object> beanVector = new Vector<Object>();
        Object [] res;                  // to contain the results of args2RexxString()


        for (int i=0 ; i < (args != null ? args.length : 0) ; i++)   // turn argument objects into string arguments for Rexx
        {
           Object argVal=args[i];
           if (argVal!=null)
           {
               if (argVal instanceof RexxProxy)             // leave RexxProxy arguments unchanged
               {
                   editedArgs[i]=argVal;
               }
               else
               {
                   res = args2RexxString( argVal );         // get String or Bean for this argument
                   if (((Boolean) res[0]).booleanValue())   // a Bean had to be created
                   {
                       beanVector.addElement(res[1]);       // remember beanName for later deregistering
                   }
                   editedArgs[i]=(String) res[1];           // String value for Rexx
               }
           }
           else editedArgs[i]=null;
        }

        String resultType=null;     // null indicates any object
        int    length=(methodDescriptor==null ? 0 : methodDescriptor.length()); // get length

        if (length>0)
        {
            // String chunks[]=methodDescriptor.split(" | ");  // split between internal and human readable form
            // extract internal descriptor
            String tmpMethodDescriptor=methodDescriptor.split(" | ")[0];

// System.err.println("...RexxEngine.call(): tmpMethodDescriptor=["+tmpMethodDescriptor+"]");

            int posReturnType=tmpMethodDescriptor.lastIndexOf(')');
            if (posReturnType>0 && posReturnType<length)
            {
                char firstChar=tmpMethodDescriptor.charAt(posReturnType+1);
                if (firstChar!='L')         // a primitive type ?
                {
                    if (firstChar!='V')     // not void ?
                    {
                        resultType=""+firstChar;    // turn char into a string
                    }
                }

                else    // check to see whether we have a wrapper class
                {
                    String returnTypeName=tmpMethodDescriptor.substring(posReturnType+1); // extract internal name of return type

                    if (returnTypeName.startsWith("Ljava/lang/"));
                    {
                            // extract last word including trailing semi-colon
                        returnTypeName=returnTypeName.substring(returnTypeName.lastIndexOf('/'));
// System.err.println("...RexxEngine.call(): remaining 'returnTypeName'=["+returnTypeName+"]");
                        if (returnTypeName.equals("/Boolean;"))
                        {
                            resultType="Z";
                        }
                        else if (returnTypeName.equals("/Byte;"))
                        {
                            resultType="B";
                        }
                        else if (returnTypeName.equals("/Character;"))
                        {
                            resultType="C";
                        }
                        else if (returnTypeName.equals("/Short;"))
                        {
                            resultType="S";
                        }
                        else if (returnTypeName.equals("/Integer;"))
                        {
                            resultType="I";
                        }
                        else if (returnTypeName.equals("/Long;"))
                        {
                            resultType="J";
                        }
                        else if (returnTypeName.equals("/Float;"))
                        {
                            resultType="F";
                        }
                        else if (returnTypeName.equals("/Double;"))
                        {
                            resultType="D";
                        }
                    }
                }
            }
        }

        Object o=null;      // returned object

        // if (object instanceof RexxProxy)    // new 4.0 API behaviour
if (bDebug) System.err.println(this+" RexxEngine.call(): jniRexxSendMessageToRexxObject() ...");
// System.err.println("RE.call(...) v# 3, proxy=["+proxy+"], methodName=["+methodName+"]");
        o=rexxInterface.jniRexxSendMessageToRexxObject (
            proxy.rexxInterpreterID,   // RexxInstance instance ID
            proxy.rexxObjectID,        // RexxProxy objectID
            proxy.rexxUserDataID,      // RexxProxy userData_ID
//             proxy.creationTID,         // RexxProxy creationTID
            javaObjectBean,             // javaObject bean
            null,                       // methodObjectBean (a String) not used in this version
            methodName,                 // message name (determines name of method to invoke)
            methodDescriptor,           // methodDescriptor (a String)
            editedArgs,                 // arguments, edited for native layer
            resultType                  // the expected (primitive) return type, or null else
            );

        // now deregister Beans
        Enumeration e=beanVector.elements();    // get elements for Enumeration
        while ( e.hasMoreElements() )
        {
            // rexxInterface.unregisterBean( (String) e.nextElement());
            String el=(String) e.nextElement();

if (bDebug) System.err.println(this+".call(p,j,m,m,a): unregistering: ["+el+"] ----> "+"rexxInterface.bsfPrefixReturnValue=["+rexxInterface.bsfPrefixReturnValue+"]");

            if (rexxInterface.bsfPrefixReturnValue==true)
            {
if (bDebug) System.err.println(this+".call(): unregistering: ["+el.substring(3)+"] ----> "+"rexxInterface.bsfPrefixReturnValue=["+rexxInterface.bsfPrefixReturnValue+"]");
                rexxInterface.unregisterBean( el.substring(3));  // remove "<O>" or "<S>"
            }
            else
            {
if (bDebug) System.err.println(this+".call(): unregistering: ["+el+"] ----> "+"rexxInterface.bsfPrefixReturnValue=["+rexxInterface.bsfPrefixReturnValue+"]");
                rexxInterface.unregisterBean( el);
            }
        }

        if (rexxInterface.bsfPrefixReturnValue==true)
        {
            rexxInterface.unregisterBean(javaObjectBean.substring(3));  // remove "<O>" prefix
        }
        else
        {
            rexxInterface.unregisterBean(javaObjectBean);
        }

if (bDebug) System.err.println(this+" <=== <=== <=== RexxEngine.call(RexxProxy proxy, Object javaObject, String methodName, String methodDescriptor, Object[] args) - end, returning ["+o+"]...");
       return o;
    }




    /** Applies (evaluates) a piece of Rexx code
      * (can be a whole program as well)
      * <em>with</em> Java-supplied arguments and returns the result.
      *
      * @param source name of source containing the script (e.g. fully qualified file name) or null
      * @param lineNo not used.
      * @param columnNo not used.
      * @param oscript the String version of this object [using toString()]
      *                represents the Rexx code to be executed.
      *                Its statements are delimited with a ';' or a
      *                an LF (line-feed, &quot;0x0a&quot;), preceeded by an
                       optional CR (carriage-return, &quot;0x0d&quot;).

      * @param vArgNames not used.
      * @param vArgs arguments to be passed to Rexx.
      *
      * @return a String object with the result from Rexx or <code>null</code>.
      */
    public Object apply (String source, int lineNo, int columnNo,
                         Object oscript,
                         java.util.Vector vArgNames,
                         java.util.Vector vArgs
                        )
                        throws BSFException
    {

if (bDebug) System.err.println(this+" ===> ===> ===> RexxEngine.apply() - just entered (...)");

        if (bTerminated==true)       // engine got terminated, cannot use it anymore
        {
            throw new RexxException("RexxEngine "+this+" got terminated, cannot be used anymore.");
        }

        // rgf, 2012-02-04
        if (rii_ID==null)   // Rexx interpreter instance (RII) not yet created, create it!
        {
if (bDebug) System.err.println(this+" ===> RexxEngine(), creating RII: before ===> rii_ID=["+rii_ID+"]");
            this.rii_ID=rexxInterface.createRexxInterpreterInstance(rexxConfig);
if (bDebug) System.err.println(this+" ===> RexxEngine(), creating RII: after  ===> rii_ID=["+rii_ID+"]");
        }

    // ---rgf, 2006-02-24
        String script = oscript.toString ();

            // an event text got posted, put it into the event text queue
        if(source!=null && source.equals(RexxAndJava.EVENT_TEXT))   // dispatching eventListener text?
        {
            int i=0, size=vArgs.size();             // get size of argument Vector
            Object params[]=new Object[size];
            for (i=0; i<size; i++)                  // assign arguments to Object array
            {
                params[i]=vArgs.elementAt(i);
            }

            String key=
                RexxAndJava.EVENT_LISTENER_ARGUMENTS_LEADIN +
                // rexxInterface.makeString4Rexx(params)         +
                RexxAndJava.makeString4Rexx(rexxInterface, params)         +
                RexxAndJava.EVENT_LISTENER_ARGUMENTS_LEADOUT;

// System.err.println("}-}-} RexxEngine.eval(), source=["+source+"]\n\tscript=["+script+"]");
            if (script.startsWith("/*2*/"))                    // alarm (highest) level
            {
                rexxInterface.eventTextList.put(key+script, 2);
            }
            else if (script.startsWith("/*0*/"))               // batch (lowest) level
            {
                rexxInterface.eventTextList.put(key+script, 0);
            }
            else                                      // normal level (default)
            {
                rexxInterface.eventTextList.put(key+script, 1);       // put text into normal level event text vector
            }

            return null;
        }

        lastScriptCode=script;   // save script's code, fall 2002, ---rgf
        lastSourceLocation=source;  // save script source info, 2008-06-14

        Vector<Object> beanVector  = null;  // bean Vector to build (for each argument one entry)

        Object objArg[] = null;     // args for Rexx
        // 2003-01-06, ---rgf, deal with arguments (non-primitive Java objects) which need to be turned into Beans
        Object [] res;                  // to contain the results of args2RexxString()

        if (vArgs!=null && vArgs.isEmpty())
        {
            vArgs=null;         // set variable to null, if no arguments available!
        }

        if (vArgs!=null)        // process arguments
        {
            int i=0;

            Enumeration e=vArgs.elements();
            // this works under JRE 1.1.8, i.e. under older versions of OS/2 Warp as well, 2001-05-23, ---rgf
            objArg = new Object [vArgs.size()];
            beanVector= new Vector<Object>(vArgs.size());

            for ( i=0; e.hasMoreElements() ; i++)
            {
               Object argVal=e.nextElement();
               if (argVal!=null)
               {
                   if (argVal instanceof RexxProxy)         // leave RexxProxy arguments unchanged
                   {
                       objArg[i]=argVal;
                   }
                   else
                   {
                       res = args2RexxString( argVal );     // get String or Bean for this argument
                       if (((Boolean) res[0]).booleanValue())   // a Bean had to be created
                       {
                          beanVector.addElement(res[1]);    // remember beanName for later deregistering
                       }
                       objArg[i]=(String) res[1];           // assign beanName (a String)
                   }
               }
               else
               {
                   objArg[i]=null;
               }
            }
        }



        Object obj=null;

if (bDebug) System.err.println(this+" ===> RexxEngine.apply() - before: rexxInterface.jniRexxRunProgram (...)");

        obj=rexxInterface.jniRexxRunProgram(
                rii_ID,                 // RexxInstance instance ID
                2,                      // invocationType,         // determines whether "CallProgram", "LoadPackage" or "LoadPackageFromData" is to be used
                lastSourceLocation,     // String  fileName,               // filename which contains the program to run
                lastScriptCode,         // String  programData,            // Rexx code to execute; if given "fileName" denotes a (symbolic) name for this code
                objArg               // Object[]args                    // arguments
                );

if (bDebug) System.err.println(this+" <=== RexxEngine.apply() - AFTER: rexxInterface.jniRexxRunProgram (...)");

        if (beanVector!=null)
        {
            // now deregister Beans
            Enumeration e=beanVector.elements();    // get elements for Enumeration

            while ( e.hasMoreElements() )
            {
                // rexxInterface.unregisterBean( (String) e.nextElement());
                String el=(String) e.nextElement();

    if (bDebug) System.err.println(this+".apply(): unregistering: ["+el+"] ----> "+"rexxInterface.bsfPrefixReturnValue=["+rexxInterface.bsfPrefixReturnValue+"]");

                if (rexxInterface.bsfPrefixReturnValue==true)
                {
    if (bDebug) System.err.println(this+".apply(): unregistering: ["+el.substring(3)+"] ----> "+"rexxInterface.bsfPrefixReturnValue=["+rexxInterface.bsfPrefixReturnValue+"]");
                    rexxInterface.unregisterBean( el.substring(3));  // remove "<O>" or "<S>"
                }
                else
                {
    if (bDebug) System.err.println(this+".apply(): unregistering: ["+el+"] ----> "+"rexxInterface.bsfPrefixReturnValue=["+rexxInterface.bsfPrefixReturnValue+"]");
                    rexxInterface.unregisterBean( el);
                }
            }
        }

if (bDebug) System.err.println(this+" <=== <=== <=== RexxEngine.apply() - before returning ["+obj+"] (...)");
        return obj;
    }




    /** Turns received object either into a String, if object is a primitive type
      * or a Java wrapper class for primitive types. Otherwise it makes sure that
      * a Bean will be created for it and returns the beanName instead (a String).
      * <code>null</code> will get returned as <code>null</null>, hence no String
      * representation is returned instead; this way the Rexx <code>ARG()</code>
      * function can be used to determine, if such an argument was omitted or not.
      * (2003-01-06, ---rgf).
      *
      * @param o object to be turned into a String or a Bean.
      * @return a two-dimensional array of Object. The element with index 0 indicates whether
      * a Bean had to be created (<code>Boolean.TRUE</code> or <code>Boolean.FALSE</code>),
      * the second element with index 1 represents the object as a String or <code>null</code>
      * if the received object is <code>null</code>.
      */
    private Object [] args2RexxString(Object o)
    {
        Object res [] = { Boolean.FALSE, // use Boolean object representing false
                          o };

        if (o==null) return res;    // leave it as is

        Class cl = o.getClass();    // get class object

        if (cl==String.class)
        {
            // a string in hand that has a prefix that would mislead to represent
            // the name of a registered bean; hence make sure it is prefixed with
            // the string indicator "<S>", if "bsfPrefixReturnValue" is set to "true"
            if (((String)o).startsWith(rexxInterface.OBJECT_INDICATOR_PREFIX))
            {
                // res[1] = rexxInterface.makeString4Rexx( o );
                res[1] = RexxAndJava.makeString4Rexx( rexxInterface, o );
            }
            return res;
        }

        else if (cl==Boolean.class )
        {
           res [1] = ( ((Boolean) o).booleanValue() ? "1" : "0");     // return boolean value
           return res;
        }


        else if ( cl.isPrimitive()     ||
                  cl==Integer.class    || cl==Character.class  ||
                  cl==Long.class       ||
                  cl==Float.class      || cl==Double.class     ||
                  cl==Byte.class       || cl==Short.class
                )
        {
            res[1]=o.toString();    // make sure a String object will get returned
            return res;
        }


        // not of a primitive type nor String, hence a bean will be created for it

if (bDebug)
{
    System.err.println("RexxEngine.args2RexxString(o), 1. o=["+o+"], rexxInterface=["+rexxInterface+"], .mgr=["+rexxInterface.mgr+"]");
}

        res[0] = Boolean.TRUE;          // a bean will be created for this
        // res[1] = rexxInterface.makeString4Rexx( o );  // create Bean, return beanName (proxy for received Java object)
        res[1] = RexxAndJava.makeString4Rexx( rexxInterface, o );  // create Bean, return beanName (proxy for received Java object)

if (bDebug)
{
    System.err.println("RexxEngine.args2RexxString(o), 2. o=["+o+"], rexxInterface=["+rexxInterface+"], .mgr=["+rexxInterface.mgr+"]");
}

        return res;
    }




    /** Saves the supplied bean in {@link engineDeclaredBeans} which will be looked up
     *  by RexxAndJava.lookupBean() if a bean with the supplied name is not present
     *  in RexxAndJava's registry.
      *
      * @param bean the bean to be declared
      */
    public void declareBean(BSFDeclaredBean bean)
                 throws BSFException
    {
        if (bTerminated==true)       // engine got terminated, cannot use it anymore
        {
            return;     // gracefully return
        }
if (bDebug) System.err.println("RexxEngine.declareBean(): name=["+bean.name+"], bean=["+bean+"], bean.bean=["+bean.bean+"]");

        engineDeclaredBeans.put(bean.name, bean);
    }

    /** Removes the supplied bean from {@link engineDeclaredBeans}.
     *
      *
      * @param bean the bean to be declared for the already started language.
      */

    public void undeclareBean(BSFDeclaredBean bean)
                       throws BSFException
    {
        if (bTerminated==true)       // engine got terminated, cannot use it anymore
        {
            return;     // gracefully return
        }
if (bDebug) System.err.println("RexxEngine.undeclareBean(): name=["+bean.name+"], bean=["+bean.bean+"]");

        engineDeclaredBeans.remove(bean.name);
    }


    /** Waits until all Rexx threads of this engine's Rexx interpreter instance peer have terminated
     *  (using the new ooRExx 4.0 APIs); this method <em>must be</em> called from the same thread
     *  that created the Rexx interpreter instance (i.e. the thread that created this instance of
     *  the RexxEngine). Terminating the primodal Rexx interpreter instance makes it unavailable for
     *  use by the Java side, but remains running for the native code. All other Rexx interpreter
     *  instances will be terminated for good.
     *  Use {@link #isTerminated()} to test whether the Rexx interpreter instance of
     *  this {@link RexxEngine} got terminated.
     *
     * To halt all Rexx threads of this engine's Rexx interpreter instance peer use
     * {@link #halt()}.
     *
    */
    synchronized public void terminate() {
final boolean bDebug= // true;
                      false;

        if (bJNITerminated==true)  // already terminated, return
        {
            return;
        }

        bTerminated=true;   // do not allow any invocations via this instance anymore

        if (rii_ID!=null)   // if RII got created
        {
            // this waits until all threads of the instance have finished
if (bDebug) System.err.println(this+".terminate(): before jniRexxTerminateInterpreterInstance("+rii_ID+")...");

            int res=rexxInterface.jniRexxTerminateInterpreterInstance(rii_ID);  // wait for termination of all Rexx threads of this interpreter instance

            if (res==-2)         // primodal RII will never get terminated, to allow JNI to use it from the Rexx side to maintain Rexx collections for its purposes
            {
                // System.err.println(this+": terminated primodal Rexx interpreter instance=["+rii_ID+"] (cannot be used from Java anymore)");
                return;
            }

if (bDebug) System.err.println(this+".terminate(): AFTER jniRexxTerminateInterpreterInstance("+rii_ID+")");

        }

        bJNITerminated=true;    // indicate that JNI successfully carried out Terminate() on the RII

        // super.terminate();          // now let the superclass do its work
        rexxInterface.terminate();  // let RexxAndJava peer terminate as well
        rexxInterface=null;         // remove reference to RexxAndJava peer
        super.terminate();          // now let the superclass do its work
    }



    /**
     * Halt (raise the <code>HALT</code> condition) all Rexx threads of this engine's peer
     * Rexx interperter instance (using the new ooRExx 4.0 APIs).
     *
    */
    public void halt() throws RexxException
    {
if (bDebug) System.err.println("RexxEngine().halt(): before jniRexxHaltInterpreterInstance("+rii_ID+")..");

        if (rii_ID!=null)   // if RII got created
        {
            rexxInterface.jniRexxHaltInterpreterInstance(rii_ID);  // halt interpreter instance
        }

if (bDebug) System.err.println("RexxEngine().halt(): AFTER jniRexxHaltInterpreterInstance("+rii_ID+")");

    }


    /** Allow halting an individual Rexx thread (raising the <code>HALT</code> condition) having
     *  the given thread ID.
     *
     * @param tid a string with the target thread ID, or
     *        the string <code>&quot;0&quot;</code> (raises the <code>HALT</code> condition
     *        in all ooRexx threads)
     *
     * @return return code from <code>RexxSetHalt(...)</code>: <code>0<code> (o.k.),
     *                    <code>1</code> (tid not found), or <code>2</code> (failure), or
     *                    <code>3</code> (Rexx interpreter instance was not yet created)
    */
    public int halt(String tid)
    {
        if (rii_ID!=null)   // if RII got created
        {
            return rexxInterface.jniRexxSetHalt(tid);   // halt/terminate Rexx interpreter instance of this thread
        }
        return 3;

    }


    // these methods are pass-through methods for the respective protected RexxAndJava methods

    /** This method registers an <em>object</em> with the given <em>beanName</em>
     *  that gets created automatically in the BSF registry, if not registered yet.
     *  In addition a reference counting is carried out.
     *
      *  @param object to be registered
      *
      *  @return returns the beanName
     *
     * @since 2012-02-19
      */
    public String  registerBean(Object object) throws RexxException
    {
        if (bTerminated==true)       // engine got terminated, cannot use it anymore
        {
            throw new RexxException("RexxEngine "+this+" got terminated, cannot be used anymore.");
        }
        // return rexxInterface.registerBean(rexxInterface.makeString4Rexx(object), object);
        return RexxAndJava.registerBean(rexxInterface, RexxAndJava.makeString4Rexx(rexxInterface, object), object);
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
     *
     * @since 2012-02-19
      */
    public String  registerBean(String beanName, Object object)  throws RexxException
    {
        if (bTerminated==true)       // engine got terminated, cannot use it anymore
        {
            throw new RexxException("RexxEngine "+this+" got terminated, cannot be used anymore.");
        }
        // return rexxInterface.registerBean(beanName, object);
        return RexxAndJava.registerBean(rexxInterface, beanName, object);
    }


    /** This method allows for unregistering Beans, taking into account reference counters.
      * Only if the reference counter drops to 0, will the given Bean be unregistered.
      *
      *  @param beanName name of the Bean to be unregistered
      *
      *  @return returns <code>true</code>, if the Bean exists, <code>false</code> else.
     *
     * @since 2012-02-19
      */
    public boolean unregisterBean(String beanName )  throws RexxException
    {

/* // rgf, 2016-11-30: even if the Rexx engine is terminated, unregisterBean remains valid, reducing the refCount
   // of the registered Java object

        if (bTerminated==true)       // engine got terminated, cannot use it anymore
        {
            throw new RexxException("RexxEngine "+this+" got terminated, cannot be used anymore.");
        }
*/
        return (rexxInterface.unregisterBean(beanName)>=0); // rgf, 2018-02-23: now returns remaining refCount or -1, if not found
    }





    /** Allow lookup of BSFRegistry entries.
     *
     * @param beanName string denoting the index into the BSFRegistry
     * @return the Java object matching <code>beanName</code>, if not found in BSF registry returns <code>null</code>
     *
     * @since 2012-02-19
     */
    public Object lookupBean(String beanName)  throws RexxException
    {
        if (bTerminated==true)       // engine got terminated, cannot use it anymore
        {
            throw new RexxException("RexxEngine "+this+" got terminated, cannot be used anymore.");
        }
        // return rexxInterface.lookupBean(beanName);
        return RexxAndJava.lookupBean(rexxInterface, beanName);
    }

    /* 2014-03-30: ---rgf, no documentation generated, a last resort means to get and set the process environment via Java using JNI.
     */
    public String procEnvironment(int t, String n, String v)  throws RexxException
    {
        if (bTerminated==true)       // engine got terminated, cannot use it anymore
        {
            throw new RexxException("RexxEngine "+this+" got terminated, cannot be used anymore.");
        }
        return rexxInterface.jniProcEnvironment(t, n, v);
    }



}


