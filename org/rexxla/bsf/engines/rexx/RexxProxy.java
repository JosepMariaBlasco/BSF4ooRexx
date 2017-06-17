package org.rexxla.bsf.engines.rexx;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

import java.util.ArrayList;

import org.apache.bsf.BSFException;



/**
 * This class allows RexxProxy objects to be created from native (JNI) code and will be
 * registered in the BSFRegistry using the String retrievable by {@link #getBsfRegistryKey()}.
 * It allows ooRexx objects to be addressed by Java, as well as creating any number
 * of java.lang.reflect.Proxy objects from it(cf. {@link #newJavaProxyInstance(Object[] interfaces)})
 * which forward any method invocation to the proxied ooRexx object.
 * The class is public, such that any Java program can define variables of type RexxProxy. However, only
 * native code is capable of instantiating this class.
 *

 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2009-2017 Rony G. Flatscher
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
  * @version 1.0.2, 2009-05-03, 2009-06-27, 2009-07-11, 2009-07-31, 2010-04-16, 2016-11-04
  * @author Rony G. Flatscher (<a href="http://www.wu.ac.at">WU/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu.ac.at/english">http://www.wu.ac.at/english</a>)
  */

/*
    2016-11-05, rgf, - oops, overlooked that the test program did it from Rexx, now we make sure we
                       use Janino instead of ASM by setting to Janino in the static block

    2016-11-04, rgf, - Use Janino instead of ASM for extending abstract classes (would need to add
                       a default constructor in the case of an abstract class; however, Janino has
                       been maintained nicely and has become fast enought to replace ASM, so
                       contemplating of removing ASM for good eventually)
                     - cast "null" to "(java.lang.Class<?>[]) null" in Object.class.getMethod(...) for
                       "hashCode" and "toString" to get rid of the compiler warning
                     - rebasing on Java 1.6/6.0, get rid of generics related warnings where possible:

                           source_java>javac -Xlint:unchecked RexxProxy.java
                           RexxProxy.java:118: warning: [unchecked] unchecked call to getDeclaredMethod(java.lang.String,java.lang.Class<?>...) asa member of the raw type java.lang.Class
                                           mCreateProxiedJavaClass=clzProxiedJavaClassTool.getDeclaredMethod(
                                                                                                            ^
                           RexxProxy.java:898: warning: [unchecked] unchecked call to add(E) as a member of the raw type java.util.ArrayList
                                               al.add(clz);        // add interface class object
                                                     ^
                           RexxProxy.java:906: warning: [unchecked] unchecked call to add(E) as a member of the raw type java.util.ArrayList
                                                   al.add(ifs[k]);         // add interface class object
                                                         ^
                           RexxProxy.java:1054: warning: [unchecked] unchecked call to add(E) as a member of the raw type java.util.ArrayList
                                           al.add( raj.convFromRexx((String) o));  // string a key into BSFRegistry; if so return Java object
                                                 ^
                           RexxProxy.java:1059: warning: [unchecked] unchecked call to add(E) as a member of the raw type java.util.ArrayList
                                           al.add(o);
                                                 ^
                           RexxProxy.java:1064: warning: [unchecked] unchecked call to set(int,E) as a member of the raw type java.util.ArrayList
                                   al.set(0,clz);      // replace the very first element with the retrieved class object it stands for
                                         ^
                           RexxProxy.java:1178: warning: [unchecked] unchecked call to getMethod(java.lang.String,java.lang.Class<?>...) as a membe
                           r of the raw type java.lang.Class
                                           Method mSDH=extendedClz.getMethod("setDefaultHandler", new Class[]{org.rexxla.bsf.engines.rexx.RexxProxy.class});
                                                                            ^
                           RexxProxy.java:1262: warning: [unchecked] unchecked call to getMethod(java.lang.String,java.lang.Class<?>...) as a membe
                           r of the raw type java.lang.Class
                                       Method mSetDH=extendedClz.getMethod("setDefaultHandler", new Class[]{org.rexxla.bsf.engines.rexx.RexxProxy.class});
                                                                          ^


    2010-04-16/17, rgf, - added ability to switch to using the new Janino-based dynamic extension
                       of Java classes: takes at least about five to twenty times longer (e.g. 70,
                       15, 0 msec vs. 330msec), however allows for proxying methods of superclasses,
                       which in rare cases may be needed; timings will improve over time with
                       better hardware automatically;
                       actually, only the first invocation takes appr. 300msec on a 2GHz Pentium,
                       thereafter between 15mse and 40msec which is quite fair (though there are
                       cases where the time remains at appr. 240msec) !

    2009-09-22, rgf, - added the methods sendMessage(msgName,arg[]), sendMessage0(msgName),
                       sendMessage1(msgName,arg1), sendMessage2(msgName,arg1,arg2),
                       sendMessage3(msgName,arg1,arg2,arg3), sendMessage4(msgName,arg1,arg2,arg3,arg4),
                       sendMessage5(msgName,arg1,arg2,arg3,arg4,arg5) to ease sending messages
                       to Rexx objects a little bit

    2009-07-31, rgf, - added new method 'newExtendedProxyInstance(Object [])'
                     - renamed private method 'newJavaProxyInstanceFromRexx' to
                       'newJavaOrExtendedProxyInstanceFromRexx', which will invoke either
                       'newJavaProxyInstance' or 'newExtendedProxyInstance"

    2009-07-11, rgf, - added additional invoke() method which is crafted for allowing callbacks for
                       extended Java classes, accepting the javaObject that is the source of the callback,
                       as well as the internal Java method description (denotes argument and return values)

    2009-06-27, rgf, - added additional invoke() method which accepts a java.lang.reflect.Method object
                       instead of just a method name
*/

// TODO: 2016-11-04, rgf: tidy up code that uses reflection to see whether running on Java = 1.4

public class RexxProxy implements InvocationHandler
{

     /** Version information on this class. */
    static final public String version="102.20161105";


    // rgf, 2010-04-16 --->
    static final private String PROXIED_JAVA_CLASS_TOOL_NAME="org.rexxla.bsf.engines.rexx.ProxiedJavaClassTool";
    static final private String M_CREATE_PROXIED_JCLZ_NAME  ="createProxiedJavaClass";
    static       private Class<?>   clzProxiedJavaClassTool=null;   // ---rgf, 20161104, cf. <http://stackoverflow.com/questions/892725/proper-way-to-check-java-reflection-calls>
    static       private Method  mCreateProxiedJavaClass=null;
    /** If set to <code>true</code> the Janino based implementation is used to extend
     *  the given abstract or concrete class.
     */
    static private boolean extendUsingJanino=false;    // must be initially false, such that the setter can try to dynamically load the utility class

    /** Setter method for field <code>extendUsingJanino</code>. Controls whether to use the
     *  fast ASM solution for extending abstract classes, or the quick (but by no means as fast)
     *  Janino-based solution, which allows to proxy Java methods that are defined in superclasses
     *  (in this case the supplied method name may be blank-preceded by the superclass name)!
     *  If the field <code>extendUsingJanino</code> is <code>false</code> (default), then ASM is used, Janino else.
     */
    static public void setExtendUsingJanino(boolean b) throws BSFException
    {
        extendUsingJanino=b;    // save new value
            // try to load the Janino based utility class ?
        if (b==true && clzProxiedJavaClassTool==null)
        {
            try {
                clzProxiedJavaClassTool=RexxProxy.class.getClassLoader().loadClass(PROXIED_JAVA_CLASS_TOOL_NAME);
                mCreateProxiedJavaClass=clzProxiedJavaClassTool.getDeclaredMethod(
                             M_CREATE_PROXIED_JCLZ_NAME,
                             new Class [] { Class.class, String.class, String[].class, Class[].class }
                            );
            }
            catch (Exception e) // class not loadable, hence cannot be used
            {
                clzProxiedJavaClassTool=null;
                extendUsingJanino=false;    // reset change

                throw new BSFException (BSFException.REASON_OTHER_ERROR,
                        "Cannot load class ["+PROXIED_JAVA_CLASS_TOOL_NAME+"] or its method ["+
                                              M_CREATE_PROXIED_JCLZ_NAME+"]",
                        e   // supply causing Throwable
                        );
            }
        }
    }

    /** Getter method for field <code>extendUsingJanino</code>.
     */
    static public boolean getExtendUsingJanino()
    {
        return extendUsingJanino;
    }
    // <---



    /** If set, then output for debugging is created. */
    static boolean bDebug=  // true;
                            false;

    // cf. <http://java.sun.com/j2se/1.4.2/docs/guide/reflection/proxy.html>
      // preloaded Method objects for the methods in java.lang.Object
    private static Method hashCodeMethod;
    private static Method equalsMethod;
    private static Method toStringMethod;

    // -------------- static initializer ----------------------------------------------------------------
    static {
	try
        {
                // rgf, 2016-11-04: cast null to suppress warnings on the argumentless methods "hashCode" and "toString"
	    hashCodeMethod = Object.class.getMethod("hashCode", (java.lang.Class<?>[]) null);
	    equalsMethod   = Object.class.getMethod("equals",                          new Class[] { Object.class });
	    toStringMethod = Object.class.getMethod("toString", (java.lang.Class<?>[]) null);
        }
        catch (NoSuchMethodException e)
        {
	    throw new NoSuchMethodError(e.getMessage());
	}

        try // ---rgf, 2016-11-05: make sure we use Janino ("ProxiedJavaClassTool.java") instead of ASM ("RexxExtend*.java"), which still is a fallback
        {
            setExtendUsingJanino(true);
        }
        catch (Exception e) {}
    }

    // ------------------------- instance stuff ------------------------------

    protected Integer proxyHashCode(Object proxy)
    {
    	return new Integer(System.identityHashCode(proxy));
    }

    protected Boolean proxyEquals(Object proxy, Object other)
    {
        return (proxy == other ? Boolean.TRUE : Boolean.FALSE);
    }

    protected String proxyToString(Object proxy)
    {
    	return proxy.getClass().getName()
                                          + '['+Proxy.getInvocationHandler(proxy).toString()+']'
                                          + '@'
                                          + Integer.toHexString(proxy.hashCode())
                                          ;
    }



    /** The string value of the RexxInstance C++ pointer. The RexxProxy object can only be deployed via
     *  the RexxInstance in which it got created; this assures that the appropriate .local is referencable, and
     *  the RexxProxy's package object remains valid w.r.t. to its public routines and classes that may
     *  depend on entries in .local as well.
     */
    protected String rexxInterpreterID=null;

    /** Getter method.
     *
     * @return the ooRexx Interpreter ID
     */
    public String getRexxInterpreterID ()
    {
        return this.rexxInterpreterID;
    }


    /** The string value of the identity hash value of the ooRexx object, which is used as the
     *  key in the proxied ooRexx directory in the JNI code.
     */
    protected String rexxObjectID=null;

    /** Getter method.
     *
     * @return the ooRexx object ID
     */
    public String getRexxObjectID ()
    {
        return this.rexxObjectID;
    }


    /** The string value of the identity hash value of the optional &quot;userData&quot; ooRexx object,
     *  which - if it is not null - will be submitted as part of the callback directory argument to the
     *  Rexx object.
     */
    protected String rexxUserDataID=null;

    /** Getter method.
     *
     * @return the ooRexx object ID
     */
    public String getRexxUserDataID ()
    {
        return rexxUserDataID;
    }

    /** Setter method, meant to be used by the JNI layer only, which is able to even access private members.
     *  Do not change this as the ProxyRexx reference counting is done at the JNI layer only.
     *
     * @return the ooRexx object ID
     */
    private void setRexxUserDataID (String newValue)
    {
        rexxUserDataID=newValue;
    }


/*
    / ** Thread ID of thread in which this proxy got created (value of <code>BSFGetTID()</code>).
     *  If a userData value is available, then the callback will add a directory argument which
     *  will contain this value stored with the index value <code>&quot;CREATIONTID&quot;</code>.
     * /
    protected String creationTID=null;
    / ** Getter method.
     * @return thread ID of the thread in which this proxy got created (using value of <code>BSFGetTID()</code>)
    * /
    public String getCreationTID()
    {
        return creationTID;
    }
*/

    /** The string value that was used as the key for storing this object in the BSFRegistry. */
    protected String bsfRegistryKey=null;

    /** Setter method, meant to be used by the JNI layer only, hence private.
    */
    private void setBsfRegistryKey(String newValue)
    {
        bsfRegistryKey=newValue;
    }

    /** Getter method.
     * @return string value that was used as the key for storing this object in the BSFRegistry
    */
    public String getBsfRegistryKey()
    {
        return bsfRegistryKey;
    }


    /** The RexxEngine instance that creates this proxy and which will be used for callbacks.
     */
    protected RexxEngine rexxEngine=null;


    /** Getter method.
     *
     * @return the RexxEngine instance that gets used to dispatch methods to ooRexx objects
     */
    public RexxEngine getRexxEngineInstance ()
    {
        return this.rexxEngine;
    }


//    protected RexxAndJava rexxInterface=null;


    /** Make sure, no default constructor is available. */
    private RexxProxy() {}


    /** Create a proxy for the given ooRexx object, accessible only from JNI code, as JNI is able
     *  to access a private constructor.
     *
     * @param objectId a String representing the ooRexx object's identity hash value, which is used as the
     *                 key in the ooRexx registry at the JNI layer
     *
     * @param engine   the RexxEngine instance for which this proxy gets created
     */
    private RexxProxy(String rexxInterpreterId, String objectId, String userDataId, RexxEngine engine)
    {
        this.rexxInterpreterID=rexxInterpreterId;
        this.rexxObjectID     =objectId;
        this.rexxUserDataID   =userDataId;
        this.rexxEngine   =engine;
//        this.creationTID      =engine.getRexxInterface().jniRexxGetTID();

if (bDebug==true)
{
      System.err.println("RexxProxy-constructor --> ["+this+"]: riid=["+rexxInterpreterId+
                   "], objectId=["+objectId+"/"+Long.toHexString(Long.parseLong(objectId))+
                   "], userDataId=["+userDataId+"], engine=["+engine+"]");
}

    }




    /** Sends the given message with the given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   messageName name of the method to invoke
     * @param   args array of Objects serving as arguments
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage (String messageName, Object[] args) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage(m,args): m=["+messageName+"], args.length=["+(args==null ? null+"" : args.length+"")+"]...");

        return this.rexxEngine.call(this, messageName, args);
    }

    /** Sends the given message using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   messageName name of the method to invoke
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage0 (String messageName) throws BSFException
    {
// if (bDebug)
System.err.println("////// ---> RexxProxy.sendMessage0(m): m=["+messageName+"]...");

        return this.rexxEngine.call(this, messageName, null);
    }

    /** Sends the given message with the given argument using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   messageName name of the method to invoke
     * @param   arg1  single argument
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage1 (String messageName, Object arg1) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage1(m,arg1): m=["+messageName+"], arg1=["+arg1+"]...");

        return this.rexxEngine.call(this, messageName, new Object[]{arg1} );
    }


    /** Sends the given message with the two given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   messageName name of the method to invoke
     * @param   arg1  argument
     * @param   arg2  argument
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage2 (String messageName, Object arg1, Object arg2) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage2(m,arg1,arg2): m=["+messageName+"], arg1=["+arg1+"], arg2=["+arg2+"]...");

        return this.rexxEngine.call(this, messageName, new Object[]{arg1,arg2} );
    }

    /** Sends the given message with the three given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   messageName name of the method to invoke
     * @param   arg1  argument
     * @param   arg2  argument
     * @param   arg3  argument
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage3 (String messageName, Object arg1, Object arg2, Object arg3) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage3(m,arg1,arg2,arg3): m=["+messageName+"], arg1=["+arg1+"], arg2=["+arg2+"], arg3=["+arg3+"]...");

        return this.rexxEngine.call(this, messageName, new Object[]{arg1,arg2,arg3} );
    }


    /** Sends the given message with the four given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   messageName name of the method to invoke
     * @param   arg1  argument
     * @param   arg2  argument
     * @param   arg3  argument
     * @param   arg4  argument
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage4 (String messageName, Object arg1, Object arg2, Object arg3, Object arg4) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage4(m,arg1,arg2,arg3,arg4): m=["+messageName+"], arg1=["+arg1+"], arg2=["+arg2+"], arg3=["+arg3+"], arg4=["+arg4+"]...");

        return this.rexxEngine.call(this, messageName, new Object[]{arg1,arg2,arg3,arg4} );
    }


    /** Sends the given message with the five given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   messageName name of the method to invoke
     * @param   arg1  argument
     * @param   arg2  argument
     * @param   arg3  argument
     * @param   arg4  argument
     * @param   arg5  argument
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage5 (String messageName, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage5(m,arg1,arg2,arg3,arg4,arg5): m=["+messageName+"], arg1=["+arg1+"], arg2=["+arg2+"], arg3=["+arg3+"], arg4=["+arg4+"], arg5=["+arg5+"]...");

        return this.rexxEngine.call(this, messageName, new Object[]{arg1,arg2,arg3,arg4,arg5} );
    }




    /* ---------------------------------------------------------------------------- */


    /** Sends the given message with the given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   re RexxEngine object to use for the call, if null, using RexxEngine object that created this RexxProxy object
     * @param   messageName name of the method to invoke
     * @param   args array of Objects serving as arguments
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage (RexxEngine re, String messageName, Object[] args) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage(re,m,args): re=["+re+"], m=["+messageName+"], args.length=["+(args==null ? null+"" : args.length+"")+"]...");

        return (re==null ? this.rexxEngine : re).call(this, messageName, args);
    }

    /** Sends the given message using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   re RexxEngine object to use for the call, if null, using RexxEngine object that created this RexxProxy object
     * @param   messageName name of the method to invoke
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage0 (RexxEngine re, String messageName) throws BSFException
    {
// if (bDebug)
System.err.println("////// ---> RexxProxy.sendMessage0(re,m): re=["+re+"], m=["+messageName+"]...");

        return (re==null ? this.rexxEngine : re).call(this, messageName, null);
    }

    /** Sends the given message with the given argument using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   re RexxEngine object to use for the call, if null, using RexxEngine object that created this RexxProxy object
     * @param   messageName name of the method to invoke
     * @param   arg1  single argument
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage1 (RexxEngine re, String messageName, Object arg1) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage1(re,m,arg1): re=["+re+"], m=["+messageName+"], arg1=["+arg1+"]...");

        return (re==null ? this.rexxEngine : re).call(this, messageName, new Object[]{arg1} );
    }


    /** Sends the given message with the two given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   re RexxEngine object to use for the call, if null, using RexxEngine object that created this RexxProxy object
     * @param   messageName name of the method to invoke
     * @param   arg1  argument
     * @param   arg2  argument
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage2 (RexxEngine re, String messageName, Object arg1, Object arg2) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage2(re,m,arg1,arg2): re=["+re+"], m=["+messageName+"], arg1=["+arg1+"], arg2=["+arg2+"]...");

        return (re==null ? this.rexxEngine : re).call(this, messageName, new Object[]{arg1,arg2} );
    }

    /** Sends the given message with the three given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   re RexxEngine object to use for the call, if null, using RexxEngine object that created this RexxProxy object
     * @param   messageName name of the method to invoke
     * @param   arg1  argument
     * @param   arg2  argument
     * @param   arg3  argument
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage3 (RexxEngine re, String messageName, Object arg1, Object arg2, Object arg3) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage3(re,m,arg1,arg2,arg3): re=["+re+"], m=["+messageName+"], arg1=["+arg1+"], arg2=["+arg2+"], arg3=["+arg3+"]...");

        return (re==null ? this.rexxEngine : re).call(this, messageName, new Object[]{arg1,arg2,arg3} );
    }


    /** Sends the given message with the four given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   re RexxEngine object to use for the call, if null, using RexxEngine object that created this RexxProxy object
     * @param   messageName name of the method to invoke
     * @param   arg1  argument
     * @param   arg2  argument
     * @param   arg3  argument
     * @param   arg4  argument
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage4 (RexxEngine re, String messageName, Object arg1, Object arg2, Object arg3, Object arg4) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage4(re,m,arg1,arg2,arg3,arg4): re=["+re+"], m=["+messageName+"], arg1=["+arg1+"], arg2=["+arg2+"], arg3=["+arg3+"], arg4=["+arg4+"]...");

        return (re==null ? this.rexxEngine : re).call(this, messageName, new Object[]{arg1,arg2,arg3,arg4} );
    }


    /** Sends the given message with the five given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   re RexxEngine object to use for the call, if null, using RexxEngine object that created this RexxProxy object
     * @param   messageName name of the method to invoke
     * @param   arg1  argument
     * @param   arg2  argument
     * @param   arg3  argument
     * @param   arg4  argument
     * @param   arg5  argument
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage5 (RexxEngine re, String messageName, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage5(re,m,arg1,arg2,arg3,arg4,arg5): re=["+re+"], m=["+messageName+"], arg1=["+arg1+"], arg2=["+arg2+"], arg3=["+arg3+"], arg4=["+arg4+"], arg5=["+arg5+"]...");

        return (re==null ? this.rexxEngine : re).call(this, messageName, new Object[]{arg1,arg2,arg3,arg4,arg5} );
    }



    /* ---------------------------------------------------------------------------- */



    /* ---------------------------------------------------------------------------- */


    /** Sends the given message with the given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   raj RexxAndJava object which RexxEngine will be used for the call, if null, using RexxEngine object that created this RexxProxy object
     * @param   messageName name of the method to invoke
     * @param   args array of Objects serving as arguments
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage (RexxAndJava raj, String messageName, Object[] args) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage(raj,m, args): raj=["+raj+"], m=["+messageName+"], args.length=["+(args==null ? null+"" : args.length+"")+"]...");

        return (raj==null ? this.rexxEngine : raj.getRexxEngine()).call(this, messageName, args);
    }

    /** Sends the given message using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   raj RexxAndJava object which RexxEngine will be used for the call, if null, using RexxEngine object that created this RexxProxy object
     * @param   messageName name of the method to invoke
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage0 (RexxAndJava raj, String messageName) throws BSFException
    {
// TODO: analyze execution path if Java started from Rexx (RAJ gets registerBean() via registerBean4JNI
if (bDebug)
System.err.println("////// ---> RexxProxy.sendMessage0(raj,m): raj=["+raj+"], m=["+messageName+"]...");

        return (raj==null ? this.rexxEngine : raj.getRexxEngine()).call(this, messageName, null);
/*
        Object o= (raj==null ? this.rexxEngine : raj.getRexxEngine()).call(this, messageName, null);

System.err.println("////// <--- RexxProxy.sendMessage0(raj,m): o=["+o+"].");
return o;
*/
    }

    /** Sends the given message with the given argument using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   raj RexxAndJava object which RexxEngine will be used for the call, if null, using RexxEngine object that created this RexxProxy object
     * @param   messageName name of the method to invoke
     * @param   arg1  single argument
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage1 (RexxAndJava raj, String messageName, Object arg1) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage1(raj,m,arg1): raj=["+raj+"], m=["+messageName+"], arg1=["+arg1+"]...");

        return (raj==null ? this.rexxEngine : raj.getRexxEngine()).call(this, messageName, new Object[]{arg1} );
    }


    /** Sends the given message with the two given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   raj RexxAndJava object which RexxEngine will be used for the call, if null, using RexxEngine object that created this RexxProxy object
     * @param   messageName name of the method to invoke
     * @param   arg1  argument
     * @param   arg2  argument
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage2 (RexxAndJava raj, String messageName, Object arg1, Object arg2) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage2(raj,m,arg1,arg2): raj=["+raj+"], m=["+messageName+"], arg1=["+arg1+"], arg2=["+arg2+"]...");

        return (raj==null ? this.rexxEngine : raj.getRexxEngine()).call(this, messageName, new Object[]{arg1,arg2} );
    }

    /** Sends the given message with the three given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   raj RexxAndJava object which RexxEngine will be used for the call, if null, using RexxEngine object that created this RexxProxy object
     * @param   messageName name of the method to invoke
     * @param   arg1  argument
     * @param   arg2  argument
     * @param   arg3  argument
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage3 (RexxAndJava raj, String messageName, Object arg1, Object arg2, Object arg3) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage3(raj,m,arg1,arg2,arg3): raj=["+raj+"], m=["+messageName+"], arg1=["+arg1+"], arg2=["+arg2+"], arg3=["+arg3+"]...");

        return (raj==null ? this.rexxEngine : raj.getRexxEngine()).call(this, messageName, new Object[]{arg1,arg2,arg3} );
    }


    /** Sends the given message with the four given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   raj RexxAndJava object which RexxEngine will be used for the call, if null, using RexxEngine object that created this RexxProxy object
     * @param   messageName name of the method to invoke
     * @param   arg1  argument
     * @param   arg2  argument
     * @param   arg3  argument
     * @param   arg4  argument
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage4 (RexxAndJava raj, String messageName, Object arg1, Object arg2, Object arg3, Object arg4) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage4(raj,m,arg1,arg2,arg3,arg4): raj=["+raj+"], m=["+messageName+"], arg1=["+arg1+"], arg2=["+arg2+"], arg3=["+arg3+"], arg4=["+arg4+"]...");

        return (raj==null ? this.rexxEngine : raj.getRexxEngine()).call(this, messageName, new Object[]{arg1,arg2,arg3,arg4} );
    }


    /** Sends the given message with the five given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   raj RexxAndJava object which RexxEngine will be used for the call, if null, using RexxEngine object that created this RexxProxy object
     * @param   messageName name of the method to invoke
     * @param   arg1  argument
     * @param   arg2  argument
     * @param   arg3  argument
     * @param   arg4  argument
     * @param   arg5  argument
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object sendMessage5 (RexxAndJava raj, String messageName, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) throws BSFException
    {
if (bDebug) System.err.println("////// ---> RexxProxy.sendMessage5(raj,m,arg1,arg2,arg3,arg4,arg5): raj=["+raj+"], m=["+messageName+"], arg1=["+arg1+"], arg2=["+arg2+"], arg3=["+arg3+"], arg4=["+arg4+"], arg5=["+arg5+"]...");

        return (raj==null ? this.rexxEngine : raj.getRexxEngine()).call(this, messageName, new Object[]{arg1,arg2,arg3,arg4,arg5} );
    }




    /* ---------------------------------------------------------------------------- */



    /* ============================================================================ */

    /** Invokes the given method with the given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param   methodObject for which we invoke a Rexx message
     * @param   args array of Objects serving as arguments
     *
     * @return  value that the ooRexx method returns, if any
     */
    public Object invoke (Method methodObject, Object[] args) throws BSFException
    {
// if (bDebug) System.err.println("////// ---> RexxProxy.invoke(m, args): m=["+methodObject+"], TID=["+creationTID+"] args.length=["+(args==null ? null+"" : args.length+"")+"]...");
if (bDebug) System.err.println("////// ---> RexxProxy.invoke(m, args): m=["+methodObject+"], args.length=["+(args==null ? null+"" : args.length+"")+"]...");

        return this.rexxEngine.call(this, methodObject, args);
    }



    /** Invokes the given method with the given arguments using a callback to ooRexx using the RexxEngine instance.
     *
     * @param javaObject object that invoked this callback, such that the Rexx side can get access to it and send additional messages to it
     * @param methodName this name is used as the message to be sent to the Rexx object
     * @param methodDescriptor the Java internal representation of the method's arguments and
     *         return type, if any
     * @param   args array of Objects serving as arguments
     *
     * @return  value that the ooRexx method returns, if any
     *
     * @since 2009-07-11
     */
    public Object invoke (Object javaObject, String methodName, String methodDescriptor, Object[] args) throws BSFException
    {
// if (bDebug) System.err.println("////// ---> RexxProxy.invoke(m, args): m=["+methodObject+"], TID=["+creationTID+"] args.length=["+(args==null ? null+"" : args.length+"")+"]...");
if (bDebug) System.err.println("////// ---> RexxProxy.invoke(jO, mN, mD, args): jO=["+javaObject+
                          "], mName=["+methodName+"], mDescriptor=["+methodDescriptor+"], "+
                          "args.length=["+(args==null ? null+"" : args.length+"")+"]...");

        return this.rexxEngine.call(this, javaObject, methodName, methodDescriptor, args);
    }



    /** Finalize method will unregister Rexx object from the JNI registry.
     */
    public void finalize ()
    {
// boolean bDebug=true;
        if (bDebug)
        {
            System.err.println(this+": in finalize(), rexxObjectID=["+rexxObjectID+"]...");
        }

        // remove ooRexx entry in JNI registry

        int  nr=this.rexxEngine.getRexxInterface().jniUnregisterRexxObject(rexxObjectID);

        if (bDebug)
        {
            System.err.println(this+": finalize(), nr="+nr+" of references left...");
        }
    }




    /* ============================================================================ */

    /* <------------------ Java Proxy method ----------------> */

    /** Implements the {@link java.lang.reflect.InvocationHandler}'s method.
     *
     */
    public Object invoke(Object proxy, Method method, Object[] args) throws BSFException
    {

        // take over "equals", "hashCode", "toString", which are not available otherwise
        if (method.getDeclaringClass() == Object.class)
        {
            if (method.equals(hashCodeMethod))
            {
                return proxyHashCode(proxy);
            }
            else if (method.equals(equalsMethod))
            {
                return proxyEquals(proxy, args[0]);
            }
            else if (method.equals(toStringMethod))
            {
                return proxyToString(proxy);
            }
            else
            {
                throw new InternalError("RexxProxy.java: unexpected Object method dispatched: " + method);
            }
        }

        // let our RexxProxy handle the callback: get the RexxProxy object and invoke the method on it
        // return ((RexxProxy) Proxy.getInvocationHandler(proxy)).invoke(method.getName(), args);
        return ((RexxProxy) Proxy.getInvocationHandler(proxy)).invoke(method, args);
    }


    /** Creates an instance of java.lang.reflect.Proxy using the supplied list of interfaces; will use
     *  the RexxProxy as the target object.
     *
     * @param  interfaces array of interfaces the java.lang.reflect.Proxy object should implement.
     *                    Each element may be either a class object, a String which denotes a fully
     *                    qualified class name which should be loaded instead, or any other object, whose
     *                    implemented interfaces should be honored.
     *
     * @return java.lang.reflect.Proxy object which dispatches all messages to this RexxProxy instance, which
     *                    itself will dispatch (forward) it to ooRexx via a callback
     */
    // public Object newJavaProxyInstance(Object[] interfaces) throws IllegalArgumentException
    public Proxy newJavaProxyInstance(Object[] interfaces) throws IllegalArgumentException
    {
        ClassLoader cl=this.getClass().getClassLoader();

if (bDebug) System.err.println("newJavaProxyInstance(), interfaces.length="+interfaces.length);

        ArrayList<Class>   al  = new ArrayList<Class>();
        Class       clz = null;
        Object      o   = null;

        if (interfaces!=null )  // o.k. try to create a list of interface classes the proxy object can be used for
        {
            // int i=0;
            for (int i=0; i<interfaces.length; i++)
            {
if (bDebug) System.err.println("#0 interfaces[i]=["+interfaces[i]+"]");
                if (interfaces[i]==null)    // null given, iterate to next element
                {
                    continue;
                }

                o=interfaces[i];            // get object
                clz=o.getClass();           // get its class object

                if (clz==String.class)      // a string in hand? Assume: fully qualified name of a class, load it
                {
if (bDebug) System.err.println("\t#1 clz is String class, hence attempting to load Interface class...");
                    try
                    {
                        clz=cl.loadClass((String) o);   // try to load the denoted class
                    }
                    catch (Exception e)
                    {
                        // not available prior to 1.5!
                        // throw new IllegalArgumentException("Interface argument # "+(i+1)+" ["+o+"] could not be loaded! (Note: number is 1-based as in Rexx.)", e);
                        throw new IllegalArgumentException("Interface argument # "+(i+1)+" ["+o+"] could not be loaded! (Note: number is 1-based as in Rexx.)");
                    }
                }
                else if (clz==Class.class)  // was argument already a class object? If so, use it
                {
if (bDebug) System.err.println("\t#2 clz=["+clz+"], o=["+o+"]");
                    clz=(Class) o;          // assign class object
                }

if (bDebug) System.err.println("\t#3 now trying to get and process interfaces from class object, clz=["+clz+"] ...");

                // now process all implemented interfaces as reported by the
                if (clz.isInterface())  // already an interface class ?
                {
                    // ignore -Xlint:unchecked warning: no exception defined in the abstract AbstractList.add() can take place; hence ignoring warning (alternative: add a try block)
                    al.add(clz);        // add interface class object
                }
                else
                {
                    Class [] ifs=clz.getInterfaces();   // get implemented interfaces, if any
                    for (int k=0; k<ifs.length; k++)
                    {
if (bDebug) System.err.println("\t#4 adding ifs["+k+"]=["+ifs[k]+"] ...");
                        al.add(ifs[k]);         // add interface class object
                    }
                }
            }
        }

        if (al.size()==0)   // no interface classes, throw Exception
        {
            throw new IllegalArgumentException("Supplied list does not contain any interfaces!");
        }

        // Class []iclz=new Class()[al.size()];

        Object ao=Array.newInstance(Class.class, al.size());
        for (int i=0; i<al.size(); i++)
        {
            Array.set(ao, i, al.get(i));
        }

if (bDebug) System.err.println("#5 before newProxyInstance(...)");

        // return Proxy.newProxyInstance(cl, (Class[])(al.toArray()), this);
        // return Proxy.newProxyInstance(cl, (al.toArray()), this);

        Object obj=Proxy.newProxyInstance(cl, (Class[]) ao, this);

if (bDebug)
{
        System.err.println("---->");
        System.err.println("invocationHandler="+Proxy.getInvocationHandler(obj));
        System.err.println("getClass()       ="+obj.getClass());

        Class ifs[]=obj.getClass().getInterfaces();

        System.err.println("getClass().getInterfaces()="+ifs);

        for (int i=0;i<ifs.length;i++)
        {
            System.err.println("\tifs["+i+"]="+ifs[i]);
        }
        System.err.println("<----");
}

        // return obj;
        return (Proxy) obj;

        // return Proxy.newProxyInstance(cl, (Class[]) ao, this);

    }



    /** This method is meant to be invoked via JNI and pre-processes the argument-array
     *  to fill-in Java objects from the BSFRegistry, if supplied strings are keys into it.
     *
     * @param  interfaces array of interfaces the java.lang.reflect.Proxy object should implement.
     *                    Each element may be either a class object, a String which denotes a fully
     *                    qualified class name which should be loaded instead, or any other object, whose
     *                    implemented interfaces should be honored.
     *
     * @return java.lang.reflect.Proxy object which dispatches all messages to this RexxProxy instance, which
     *                    itself will dispatch (forward) it to ooRexx via a callback
     */
    private Object newJavaOrExtendedProxyInstanceFromRexx(Object[] arguments) throws IllegalArgumentException, BSFException, ClassNotFoundException, NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException
    {
// rgf, 2009-07-31
        RexxAndJava raj=this.rexxEngine.getRexxInterface();  // get RexxAndJava instance
        Class       clz=null;
        boolean     bFirstElementIsInterface=false;     // first entry in array determines (must be either "interface" or "abstract" class

        {
            if (arguments==null)
            {
                throw new IllegalArgumentException("Argument must not be null");
            }
            else if (arguments.length==0)
            {
                throw new IllegalArgumentException("Argument array object must contain elements");
            }

            Object o1=arguments[0];        // get first element of array

            if (o1 instanceof String)       // if string, could be a key into BSFRegistry
            {
                o1=raj.convFromRexx((String) o1);   // try to fetch Java object
            }

            if (o1 instanceof String)       // still a string, now try to load it as a class
            {
                ClassLoader cl=this.getClass().getClassLoader();
                try
                {
                    clz=cl.loadClass((String) o1);  // try to load the denoted class
                }
                catch (Exception e)
                {
                    // not available prior to 1.5:
                    // throw new IllegalArgumentException("First element (\""+arguments[0]+"\") in the supplied array does not represent a Java class or cannot be loaded/found", e);
                    throw new IllegalArgumentException("First element (\""+arguments[0]+"\") in the supplied array does not represent a Java class or cannot be loaded/found");
                }
            }
            else    // assume retrieved bean is a class object
            {
                try
                {
                    clz=(Class) o1;         // try to cast to Class
                }
                catch (Exception e)
                {
                    // not available prior to 1.5:
                    // throw new IllegalArgumentException("First element (\""+arguments[0]+"\") in the supplied array does not represent a Java class or cannot be loaded/found", e);
                    throw new IllegalArgumentException("First element (\""+arguments[0]+"\") in the supplied array does not represent a Java class or cannot be loaded/found");
                }
            }

            // now determine whether we deal with an "interface" or an "abstract" class
            if (clz.isInterface())
            {
                bFirstElementIsInterface=true;      // change from false to true
            }
            else    // an abstract class? If so, remaining arguments, if any, are regarded to be meant for a constructor
            {
                int modifiers=clz.getModifiers();   // get class' modifiers
                if (!Modifier.isAbstract(modifiers))
                {
                    throw new IllegalArgumentException("First element (\""+arguments[0]+"\") in the supplied array must denote either an interface or abstract class object");
                }
            }

        }


        // received argument array from Rexx, hence iterate through it and use the RexxAndJava functionality
        // to check and use
        // Object convFromRexx(String value) throws BSFException
        //
        ArrayList<Object> al=new ArrayList<Object>();

        // RexxAndJava raj=this.rexxEngine.getRexxInterface();  // get RexxAndJava instance

if (bDebug) System.err.println("newJavaProxyInstanceFromRexx(): arguments.length="+arguments.length);
        for (int i=0; i<arguments.length; i++)   // convert strings to Java objects (if used as key in BSFRegistry)
        {
            Object o=arguments[i];             // fetch object
if (bDebug) System.err.println("                                i="+i+" o=["+o+"]");

            if (o instanceof String)
            {
                al.add( raj.convFromRexx((String) o));  // string a key into BSFRegistry; if so return Java object
if (bDebug) System.err.println("     added: raj.convFromRexx((String) o)=["+raj.convFromRexx((String) o)+"]");
            }
            else
            {
                al.add(o);
if (bDebug) System.err.println("     added:                            o=["+o+"]");
            }
        }

        al.set(0,clz);      // replace the very first element with the retrieved class object it stands for

        if (bFirstElementIsInterface)
        {
            return newJavaProxyInstance(al.toArray());      // now go ahead and invoke the public method and return the JavaProxy object
        }
        else
        {
            return newExtendedProxyInstance(al.toArray());  // now extend the class, create an instance and return it
        }
    }



    /** Creates a new class from a supplied abstract class on the fly with all abstract
     *  methods being implemented such that they forward their invocations to a RexxProxy.
     *  This method then instantiates the extended class supplying itself as the RexxProxy
     *  object to be the target of the dispatch. If more than one element gets supplied
     *  in the argument array, then they are taken as values for the constructor to invoke.
     *
     *
     * @param  arguments  array of arguments, element at index <code>0</code> must be given and
     *                    denotes an abstract class whose abstract methods should be implemented;
     *                    remaining arguments are taken as arguments for the constructor of the
     *                    extended class
     *
     * @return object which dispatches all method invocations to this RexxProxy instance, which
     *                itself will dispatch (forward) it to ooRexx via a callback
     */
    public Object newExtendedProxyInstance(Object[] arguments) throws IllegalArgumentException, ClassNotFoundException, NoSuchFieldException, NoSuchMethodException, BSFException, IllegalAccessException, InvocationTargetException
    {

// rgf, 2009-07-31
        Class       clz=null;
        {
            if (arguments==null)
            {
                throw new IllegalArgumentException("Argument must not be null");
            }
            else if (arguments.length==0)
            {
                throw new IllegalArgumentException("Argument array object must contain elements");
            }

            Object o1=arguments[0];        // get first element of array

            if (o1 instanceof String)       // still a string, now try to load it as a class
            {
                ClassLoader cl=this.getClass().getClassLoader();
                try
                {
                    clz=cl.loadClass((String) o1);  // try to load the denoted class
                }
                catch (Exception e)
                {
                    // not available prior to 1.5
                    // throw new IllegalArgumentException("First element (\""+arguments[0]+"\") in the supplied array does not represent a Java class or cannot be loaded/found", e);
                    throw new IllegalArgumentException("First element (\""+arguments[0]+"\") in the supplied array does not represent a Java class or cannot be loaded/found");
                }
            }
            else    // assume retrieved bean is a class object
            {
                try
                {
                    clz=(Class) o1;         // try to cast to Class
                }
                catch (Exception e)
                {
                    // not available prior to 1.5
                    // throw new IllegalArgumentException("First element (\""+arguments[0]+"\") in the supplied array does not represent a Java class or cannot be loaded/found", e);
                    throw new IllegalArgumentException("First element (\""+arguments[0]+"\") in the supplied array does not represent a Java class or cannot be loaded/found");
                }
            }

            // test whether
            if (!Modifier.isAbstract(clz.getModifiers()))   // get class' modifiers and check whether class is abstract
            {
                throw new IllegalArgumentException("First element (\""+arguments[0]+"\") in the supplied array must denote an abstract class");
            }
        }

        // o.k. we now are sure we have an abstract class in hand, so let us create an extended version of it

        Class<?> extendedClz=null;

        if (extendUsingJanino)
        {
            if (Modifier.isAbstract(clz.getModifiers()))
            {
                // extendedClz=clzProxiedJavaClassTool(clz, null, null);   // no explicit name, no list of methods to proxy
                try {
                    extendedClz=(Class) mCreateProxiedJavaClass.invoke(null, new Object[] {clz, null, null, null} );
                }
                catch (InvocationTargetException ite)
                {
                    Throwable c=ite.getCause();
                    Throwable t=ite.getTargetException();
                    System.err.println("...RexxProxy.newExtendedProxyInstance(...) # 1: "+
                                       "cause=["+c.toString()+"]"+
                                       "\n\ttargetException=["+t+"]");

                    System.err.println("...RexxProxy.newExtendedProxyInstance(...) # 2: "+
                                       "class:  ["+clzProxiedJavaClassTool.toString()+"], "+
                                       "method: ["+mCreateProxiedJavaClass.toString()+"]");
                    throw (ite);    // rethrow
                }
            }
            else
            {
                // arguments array contains the method names to proxy
                arguments[0]=null;  // not a method name, hence nullify it (is originally the supplied class name to extend replaced by its class object)
                extendedClz=(Class) mCreateProxiedJavaClass.invoke(null, new Object[] {clz, null, (String []) arguments} );

                // get method object, set the default handler to this RexxProxy object
                Method mSDH=extendedClz.getMethod("setDefaultHandler", new Class[]{org.rexxla.bsf.engines.rexx.RexxProxy.class});

                mSDH.invoke(null, new Object[]{this});

                return extendedClz; // in this case return the class object which needs to get initialized separately
            }
        }
        else    // only able to handle an abstract class which gets initialized
        {
// long startLoadECL=0L, endLoadECL=0L, startGenerate=0L, endGenerate=0L;

// startLoadECL=System.currentTimeMillis();
            RexxExtendingClassLoader recl=new RexxExtendingClassLoader();
// endLoadECL=System.currentTimeMillis();

// startGenerate=System.currentTimeMillis();
            extendedClz=recl.generateAndLoadExtendedClass(clz.getName());
// endGenerate=System.currentTimeMillis();

// System.out.println("RexxProxy.newExtendedProxyInstance(): loading RexxExtendingClassLoader=["+(endLoadECL-startLoadECL)+"]");
// System.out.println("RexxProxy.newExtendedProxyInstance(): creating extended class         =["+(endGenerate-startGenerate)+"]");
// rgf, 2010-04-16, T7600 @ 2.33GHz: loading: 0|17msec, creating: 47|94,15|16,0
        }

        // now we need to get its constructors and inject this RexxProxy as the very first argument
        arguments[0]=this;      // o.k. now first argument is this RexxProxy

// \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

        RexxAndJava raj=this.rexxEngine.getRexxInterface();  // get RexxAndJava instance
        Constructor [] constr=extendedClz.getConstructors();

        if (constr.length==0)
        {
            throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                     "RexxPorxy.newExtendedProxyInstance( []Object ): no constructors available for dynamically extended Java class ["+extendedClz+"], cannot create an instance...\n");
        }


        int nrArgs=arguments.length;    // arguments given for creating object
        Object funcArgs[] = arguments;
        Object [] coercedArgs = null;

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
               coercedArgs=raj.coerceArgs( funcArgs, paramTypes);

            if (nrArgs==0 || coercedArgs != null)
            {
                bFound=true;
                break;
            }
        }

        if (!bFound)  // no matching constructor found
        {
for (int t=0; t<funcArgs.length; t++)
{
    System.err.println("---///RexxProxy.newExtendedProxyInstance(Object[]args]\\\\\\---> funcArgs["+i+"]=["+funcArgs[t]+"]");

}
            throw new BSFException (BSFException.REASON_INVALID_ARGUMENT,
                     "RexxProxy.newExtendedProxyInstance( []Object ): "+
                     "no constructor with number of needed arguments (\""+nrArgs+"\") found for dynamically extended Java class ["+extendedClz+"], cannot create an instance...\n");
        }

        Object bean=null;


        Field staticLockField=extendedClz.getField("classLock");

        synchronized (staticLockField)
        {
            // get method object, set the default handler to this RexxProxy object
            Method mSetDH=extendedClz.getMethod("setDefaultHandler", new Class[]{org.rexxla.bsf.engines.rexx.RexxProxy.class});

            try {
                mSetDH.invoke(null, new Object[]{this});
            }
            catch (InvocationTargetException ite)
            {
                Throwable c=ite.getCause();
                Throwable t=ite.getTargetException();
                System.err.println("...RexxProxy.newExtendedProxyInstance(...): "+
                                   "cause=["+c.toString()+"]"+
                                   "\n\ttargetException=["+t+"]");

                System.err.println("...RexxProxy.newExtendedProxyInstance(...): "+
                                   "class:  ["+extendedClz.toString()+"], "+
                                   "method: ["+mSetDH.toString()+"]");
                throw (ite);    // rethrow
            }

            // create and register the object
            try {
                // Object bean     = EngineUtils.createBean(args[2],funcArgs);
                bean     = constr[i].newInstance(coercedArgs);
            }
            catch (Exception e)   // 2003-05-09, --rgf, maybe an IllegalAccessException?
            {
                boolean bRaise=true;      // by default raise exception

                // maybe no rights to access ?
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
                            "RexxProxy.newExtendedProxyInstance( []Object ): "+
                            "creating an instance of the dynamically extended Java class ["+extendedClz+"] with the given arguments was not possible:\n"+
                            "\t\tusing constructor: ["+constr[i]+"]\n"+
                            "\t\t raised exception: ["+e.toString()+"]"+
                            (e.getCause()==null ? "" : "-> " + e.getCause()),
                            e   // supply causing Throwable
                            );
                }
            }
        }

        return bean;
    }

}


