package org.rexxla.bsf.engines.rexx.jsr223;

import java.util.Arrays;
import java.util.Vector;
import javax.script.*;

import org.apache.bsf.BSFException;             // BSF support

import org.rexxla.bsf.engines.rexx.RexxEngine;
import org.rexxla.bsf.engines.rexx.RexxException;
import org.rexxla.bsf.engines.rexx.RexxProxy;


// TODO: check and correct visibility of new fields and methods

/*
------------------------ Apache Version 2.0 license -------------------------
   Copyright 2015-2018 Rony G. Flatscher

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-----------------------------------------------------------------------------

Changes:
    2016-10-26: ---rgf, added a few additional debug statements (debugging instrumentation by JavaFX),
                        in addition also conditionally allowing to display the Rexx source;
                        this way Rexx programmers become able to unambiguously identify whether the last
                        argument is a slot argument

    2016-11-04: ---rgf, rebasing on Java 1.6/6.0; generics related warning cannot be circumvented
                        (cf. comments in getInterface(Class<T> clasz) method)

    2016-11-08: ---rgf, add RexxCompiledScript object to slotDir, if available; supplied as new
                        fourth argument to static method createArgV(...).

    2016-11-27: ---rgf, created public getter method getRexxScriptEngine()

    2016-12-20: ---rgf, supply RexxScriptEngine instance via slotDir argument as well

    2017-04-15: ---rgf, use new .Slot.Argument (a subclass of .Directory) instead of a regular .directory,
                        suggested by Jon Wolfers at the 2017 International Rexx symposium
*/


/** Class that extends the <code>javax.script.CompiledScript</code> class and implements
 *  the <code>javax.script.Invocable</code> interface as this only makes sense in this
 *  class for {@link public <T> T getInterface(Class<T> clasz)}. Therefore the author
 *  (Rony G. Flatscher) who served on the JSR-223 specification committee) thinkgs that the documentation
 *  in <code>javax.script.Invocable</code> is false, as it states that <code>Invocable</code> gets implemented by a
 *  <code>ScriptEngine</code>, somehow indicating it has to operate on some &quot;state&quot; of the script engine.
 *  <br>Please note: the JSR-223 specification does not imply that ScriptEngine should implement this
 *  interface!
 * <br>The [oo]Rexx implementation of JSR-223 tries to be as flexible as possible and also implements
 * this interface in its implementation of <code>ScriptEngine</code> sharing as much code between
 * the tow (and caching compiled scripts for invoking the aforementioned method in a sensible way.
 *
 * @author Rony G. Flatscher
 * @version 100.20170415
 * @since  2015-05-11
 *
 */
public class RexxCompiledScript extends CompiledScript implements Invocable
{

    private static final boolean bDebug      = false; // true; // false;
    private static final boolean bShowSource = true; // false;

    /** Field that refers to the RexxScriptEngine instance which created this RexxCompiledScript. */
    RexxScriptEngine rse;

    /** Getter to retrieve the RexxScriptEngine used to create this compiled script.
     *  @return the RexxScriptEngine that created this RexxCompiledScript object
     */
    public RexxScriptEngine getRexxScriptEngine()
    {
        return this.rse;
    }


    /** The value of this field controls whether {@link createScriptException(Exception exc)} supplies
     *  the <code>BSFException</code> object or not.
     *  If set to <code>true</code> the <code>BSFException</code> (may contain a
     *  <code>RexxException</code> as cause) will be used to create and throw the <code>ScriptException</code>.
     * If set to <code>false</code> the (Rexx formatted) message and the line number, if available, are used
     * to create and throw the <code>ScriptException</code>.
     */
    boolean bSupplyBSFExceptionObject=false;

    /** Getter for the Rexx script which got compiled (tokenized).
     *
     *  @return the current value of {@link bSupplyBSFExceptionObject}
    */
    public boolean getSupplyBSFExceptionObject()
    {
        return bSupplyBSFExceptionObject;
    }

    /** Setter for the Rexx script which got compiled (tokenized).
     *
     *  @param the value to be used for the field {@link bSupplyBSFExceptionObject}
    */
    public void setSupplyBSFExceptionObject(boolean newValue)
    {
        bSupplyBSFExceptionObject=newValue;
    }



    /** The filename used for evaluating (executing) the script or invoking functions or methods from the script */
    String filename;

    /** Getter method to return the current value of {@link filename}.
     *
     *  @return the current value of {@link filename}
     */
    public String getFilename()
    {
        return filename;
    }


    /** The source code of the Rexx script that was compiled (tokenized). */
    String scriptSource;

    /** Getter for the Rexx script source code. If there are no Rexx script annotations present,
     *  then this code got compiled (tokenized), else the edited code stored in the field
     *  {@link editedScriptSource}.
     .

     *  @return the string representing the Rexx script program
    */
    public String getScriptSource()
    {
        return scriptSource;
    }

    /** The source code of the Rexx script that was compiled (tokenized). */
    String editedScriptSource=null;

    /** Getter for the Rexx edited script source code which got compiled (tokenized), it will have
     *  injected code, if any of the Rexx script annotations @GET, @GET("attribute list") or
     *  @SET("attribute list") was found in {@link scriptSource}. If this version exists, it got
     *  compiled instead of {@link scriptSource}.

     *  @return the string representing of the edited Rexx script program
    */
    public String getEditedScriptSource()
    {
        return editedScriptSource;
    }

    /** The <code>RexxProxy</code> representing the compiled (tokenized) script (a Rexx routine object). */
    RexxProxy rexxScriptObject;

    /** Getter for the <code>RexxProxy</code> representing the Rexx routine object the script got compiled (tokenized) to.
     *
     * @return a <code>RexxProxy</code> representing the Rexx routine object
     */
    public RexxProxy getScriptObject()
    {
        return rexxScriptObject;
    }


    /** A <code>RexxProxy</code> representing the script's package (a Rexx package object). */
    RexxProxy rexxPackageObject;

    /** Getter for the <code>RexxProxy</code> representing the Rexx routine object the script got compiled (tokenized) into.

     * @return a <code>RexxProxy</code> representing the Rexx package object of this compiled script
     */
    public RexxProxy getPackageObject()
    {
        return rexxPackageObject;
    }


    /** Constructor for a compiled (tokenized) Rexx script.
     *
     * @param scriptSource - The string representing the Rexx source code.
     * @@param filename - The filename to use when creating and executing the Rexx script.
     * @param bSupplyBSFExceptionObject - The value that controls whether a <BSFException> should be supplied
     *                    as a cause of a <code>ScriptException</code>.
     * @param scriptRexxProxy - The <code>RexxProxy</code>  representing the Rexx routine object.
     * @param scriptRexxPackage - The <code>RexxProxy</code> representing the Rexx package object from
     *                            the <code>scriptRexxProxy</code>.
     */
    RexxCompiledScript(RexxScriptEngine scriptEngine,
                       String    scriptSource,
                       String    filename,
                       boolean   bSupplyBSFExceptionObject,
                       RexxProxy scriptRexxProxy,
                       RexxProxy scriptRexxPackage)
    {
        super();
        this.rse                      =scriptEngine;
        this.scriptSource             =scriptSource;
        this.filename                 =filename;
        this.bSupplyBSFExceptionObject=bSupplyBSFExceptionObject;
        this.rexxScriptObject         =scriptRexxProxy;
        this.rexxPackageObject        =scriptRexxPackage;
if (bDebug)
{
    System.err.println("---> RexxCompiledScript ---> constructor ---> init()");
    if (bShowSource)
    {
        System.err.println("---> scriptSource: --->\n"+scriptSource+"\n<--- end of scriptSource\n");
    }

}

    }

    /** Evaluates (executes, runs) the Rexx program stored in this <code>RexxCompiledScript</code> object. The <code>sc</code>
     *  parameter will always be passed on to the Rexx routine by storing it in a <code>RexxDirectory</code> under
     *  the index <code>&quot;SCRIPTCONTEXT&quot;</code> and appending it as the last argument to the Rexx script.
     *  This way any Rexx routine is able to access and manipulate the <code>ScriptContext</code>.
     *
     *  @param sc - A <code>ScriptContext</code> object, which will be supplied to the Rexx routine in a
     *              <code>RexxDirectory</code> under the index <code>&quot;SCRIPTCONTEXT&quot;</code>, by appending
     *              it to the Rexx argument list. (Hint: one can always access this object from Rexx code
     *              with the statement <code>scriptcontext=arg('A')~lastItem~scriptContext</code>).)
     *
     *              <p>If this parameter contains a <code>ScriptEngine.ARGV</code> (the String
     *              <code>&quot;javax.script.argv&quot;</code>) entry (a Java <code>Object</code> array),
     *              then these objects are used as arguments for the Rexx routine in the supplied order.
     *              Java <code>null</code> values will be represented as ooRexx <code>.nil</code> values.
     *
     *              <br>Note: If any argument is of type <code>RexxProxy</code>, then the Rexx routine will
     *              receive the contained Rexx object instead.
     *
     * <p>Note # 2: This method will append an additional Rexx directory object argument with an entry named
     *
     *              <p>If this parameter contains a <code>ScriptEngine.FILENAME</code> (the String
     *              <code>&quot;javax.script.filename&quot;</code>) entry (a string), then this is used
     *              as the filename for the Rexx routine to execute and can be fetched from Rexx with
     *              the statement <code>parse source . . thisFilename</code>.
     *
     * @return the object the Rexx routine returns. In the case that a Rexx object gets returned it will
     *         be wrapped (boxed) in a <code>RexxProxy</code>. Supplying a <code>RexxProxy</code> as an
     *         argument to a Rexx routine will cause the contained Rexx object to be used in Rexx instead.
     *
     *         <br>Hint: one can interact with the contained Rexx object of a <code>RexxProxy</code> by sending
     *         it messages from Java using its <code>sendMessage</code> methods.
     *
     */
    public Object eval(ScriptContext sc) throws ScriptException
    {
if (bDebug==true)
{
    System.err.println("---> RexxCompiledScript ---> eval(), sc=["+sc+"]");
    if (bShowSource)
    {
        System.err.println("---> scriptSource: --->\n"+scriptSource+"\n<--- end of scriptSource\n");
    }
}
        if (sc==null)
        {
            throw new NullPointerException("Received \"ScriptContext sc\" argument null");
        }

        Object arrArgV[];
        try
        {

// TODO: rgf, 2017-10-09: after BetaTesting make sure that neither the RexxScriptEngine, nor the RexxCompiledScript get supplied to the Rexx script!

            arrArgV=createArgV((Object[])sc.getAttribute(ScriptEngine.ARGV), sc, rse, this);
        }
        catch (BSFException exc)
        {
            throw createScriptException(exc);
        }

        String filename=(String) sc.getAttribute(ScriptEngine.FILENAME);
        if (filename==null)     // no filename given
        {
            filename="filename_created_by_"+this.toString().replace('.','_').replace('@','-')+"_at_"+RexxScriptEngine.getGMTFormattedDate4Filename();
        }

        try
        {
            rse.updateRexxEngine(sc);   // make sure BSF.CLS support in JNI available; .input, .output, .error set accordingly

//            return rexxScriptObject.sendMessage1("CALLWITH", arrArgV);  // execute routine object

            return rexxScriptObject.sendMessage("CALLWITH", arrArgV);  // execute routine object
/*
// rgf, 2018-02-12: for debugging
System.err.println("RexxCompiledScript.eval(): sendMessage(\"CALLWITH\", arrArgV="+Arrays.deepToString(arrArgV)+")");
            return rexxScriptObject.sendMessage("CALLWITH", arrArgV);  // execute routine object
*/
        }
        catch (BSFException exc)
        {
            throw createScriptException(exc);
        }
    }



    /** Returns the <code>ScriptEngine</code> whose compile method created this CompiledScript.
     */
    public RexxScriptEngine getEngine()
    {
if (bDebug==true)
{
    System.err.println("---> RexxCompiledScript ---> getEngine(), returns=["+rse+"]");
    if (bShowSource)
    {
        System.err.println("---> scriptSource: --->\n"+scriptSource+"\n<--- end of scriptSource\n");
    }
}
        return rse;
    }


// -----------------------------------------------------------------------------------------------------
// Invocable methods
    /** Returns an implementation of a Java interface where the Java method invocations will be carried
     *  out by the public Rexx routines in the script. Therefore there must be a public Rexx routine defined
     *  for each Java interface method.
     *
     *
     *  <p>Hint: when an interface method gets invoked, the Rexx routine gets all its arguments in the
     *           same order supplied to, plus an appended <code>RexxDirectory</code> &quot;slot directory</code>
     *           object, which contains information about this particular method invocation. This slot directory
     *           will also contain an entry, named <code>&quot;SCRIPTCONTEXT&quot;</code>, which allows the
     *           Rexx routine to fully interact with the <code>ScriptContext</code> used for its invocation.
     *           <br>(Hint: one can always access this object from Rexx code with the statement
     *           <code>scriptcontext=arg('A')~lastItem~scriptContext</code>).)
     *
     *  @param clasz - The Java interface class object <code>&lt;T&gt;</code>, whose methods are implemented as public Rexx routines.
     *
     *  @return a Java object of type <code>&lt;T&gt;</code>
     */
    @SuppressWarnings("unchecked") // RexxProxy.newJavaProxyInstance(interfaces) returns a java.lang.reflect.Proxy that gets cast to T
    public <T> T getInterface(Class<T> clasz)
    {
if (bDebug==true)
{
    System.err.println("---> RexxCompiledScript ---> getInterface(clasz=["+clasz+"])");
    if (bShowSource)
    {
        System.err.println("---> scriptSource: --->\n"+scriptSource+"\n<--- end of scriptSource\n");
    }
}

        if (clasz==null)
        {
            throw new IllegalArgumentException("Received 'clasz' argument is null");
        }
        if (! (clasz.isInterface()) )
        {
            throw new IllegalArgumentException("Received 'clasz' argument is not an interface class");
        }

        ScriptContext sc=rse.getContext();  // get default ScriptContext
        RexxProxy rp;
        try {
                // make sure BSF.CLS support in JNI available; .input, .output, .error set accordingly
            rse.updateRexxEngine(sc);

                // create and get the ooRexx dispatching object, working on the available public routines
            rp=(RexxProxy) rse.getRoutineWrapper().sendMessage3("CALL", getScriptObject(), getPackageObject(), sc);
        }
        catch (Exception exc)        // if a BSFException, return null
        {
                // cannot throw a ScriptException because it would violate the Interface definition of this method;
                // try to inform the user anyway and give as much useful information as possible about the exception
            System.err.println("Exception ["+exc+"] raised while trying to create dispatching object, cause: ["+exc.getCause()+"]");
            System.err.println("Exception ["+exc+"], stack trace:");
            exc.printStackTrace();
            System.err.println("Exception ["+exc+"], returning null to indicate failure");
            return null;
        }

        //  now create a JavaProxy where the Interface method invocations will forward a message by
        //  the name of the invoked method to the contained Rexx object
        Object[] interfaces = new Object[1];
        interfaces[0]=clasz;

        // rgf, 2016-11-04:
        //          implements <T>getInterface(java.lang.Class<T>) in javax.script.Invocable; return type requires unchecked conversion
        // if doing an unchecked cast, "-Xlint:checked" will warn about this; still the following cast is o.k.
        return (T) rp.newJavaProxyInstance(interfaces);     // create and return the Java proxy for the interface
    }


    /** Returns an implementation of a Java interface where the Java method invocations will be implemented
     *  by the methods of the supplied Rexx object (a <code>RexxProxy</code>.
     *
     *  <p>Note: Uses the implementation in {@link RexxScriptEngine.getInterface(Object thiz, Class<T> clasz)}.
     *
     *  <p>Hint #1: the external <code>BSF4ooRexx</code> external Rexx function <code>BSFCreateRexxProxy(...)</code>
     *           is much more versatile and among other things allows for implementing multiple Java interface
     *           classes in a single Rexx class.
     *
     *  <p>Hint #2: when an interface method gets invoked, the Rexx method routine gets all its arguments in the
     *           same order supplied to, plus an appended <code>RexxDirectory</code> &quot;slot directory</code>
     *           object, which contains information about this particular method invocation. This slot directory
     *           will also contain an entry, named <code>&quot;SCRIPTCONTEXT&quot;</code>, which allows the
     *           Rexx routine to fully interact with the <code>ScriptContext</code> used for its invocation.
     *           <br>(Hint: one can always access this object from Rexx code with the statement
     *           <code>scriptcontext=arg('A')~lastItem~scriptContext</code>).)
     *
     *  @param thiz - The Java object (must be a <ocde>RexxProxy</code>) that implements the Java interface methods
     *
     *  @param clasz - The Java interface class object <code>&lt;T&gt;</code>, whose methods are implemented in the
     *                 <code>thiz</code> object
     *
     *  @return a Java object of type <code>&lt;T&gt;</code>
     */
    public <T> T getInterface(Object thiz, Class<T> clasz)
    {
if (bDebug==true)
{
    System.err.println("---> RexxCompiledScript ---> getInterface(thiz=["+thiz+"], clasz=["+clasz+"])");
    if (bShowSource)
    {
        System.err.println("---> scriptSource: --->\n"+scriptSource+"\n<--- end of scriptSource\n");
    }
}
            // use the implementation in RexxScriptEngine as this method is not CompiledScript related
        return rse.getInterface(thiz,clasz);
    }

    /** Invokes (calls) a public Rexx routine defined in this compiled script.
     *
     * @param name - The name of the public routine to invoke.
     *
     * @param args - The arguments (a Java array of type <code>Object</code>) for the routine.
     *
     *<p>Note # 1: If any argument is of type <code>RexxProxy</code>, then the Rexx routine will
     *              receive the contained Rexx object instead.
     *
     * <p>Note # 2: This method will append an additional Rexx directory object argument with an entry named
     *          <code>&quot;SCRIPTCONTEXT&quot;</code>, which allows the Rexx routine to fully interact
     *          with the <code>ScriptContext</code> used for its invocation.
     *           <br>(Hint: one can always access this object from Rexx code with the statement
     *           <code>scriptcontext=arg('A')~lastItem~scriptContext</code>).)
     *
     * @return the object the Rexx routine returns. In the case that a Rexx object gets returned it will
     *         be wrapped (boxed) in a <code>RexxProxy</code>. Supplying a <code>RexxProxy</code> as an
     *         argument to a Rexx routine will cause the contained Rexx object to be used in Rexx instead.
     *
     *         <br>Hint: one can interact with the contained Rexx object of a <code>RexxProxy</code> by sending
     *         it messages from Java using its <code>sendMessage</code> methods or the
     *         {@link invokeMethod(Object thiz, String name, Object... args} method.
     *
    */
    public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException
    {
if (bDebug==true)
{
    System.err.println("---> RexxCompiledScript ---> invokeFunction(name=["+name+"], args=["+(args==null ? null : args.length)+"])");
    if (bShowSource)
    {
        System.err.println("---> scriptSource: --->\n"+scriptSource+"\n<--- end of scriptSource\n");
    }
}
        if (name==null)
        {
            throw new NullPointerException("Received 'name' argument (function name) is null");
        }

        try {
            RexxProxy publicRoutines=(RexxProxy) rexxPackageObject.sendMessage0("PUBLICROUTINES");
            String result=(String) publicRoutines.sendMessage1("HASENTRY", name);

            if ( result.equals("0")==true)    // returned .false ?
            {
                throw new NoSuchMethodException("Received 'name' argument \""+name+"\" does not denote an existing, public Rexx routine");
            }

                // get the public routine, prepare arguments, invoke it
// TODO: rgf, 2017-10-09: after BetaTesting make sure that neither the RexxScriptEngine, nor the RexxCompiledScript get supplied to the Rexx script!
            Object arrArgV[]=createArgV(args, rse.getContext(), rse, this);    // supply default context

if (bDebug) System.err.println("RexxCompiledScript.invokeFunction(): sendMessage(\"CALLWITH\", arrArgV="+Arrays.deepToString(arrArgV)+")");

            RexxProxy routine=(RexxProxy) publicRoutines.sendMessage1("ENTRY",name);    // get routine

// rgf, 2018-02-12: war sendMessage1()! ?   -- fand SCRIPTCONTEXT nicht!
            return routine.sendMessage1("CALLWITH",arrArgV);    // the argument for a Rexx Routine object must be a single array object!
        }
        catch (Exception exc)
        {
if (bDebug)
{
    System.err.println(this+".invokeFunction(\""+name+"\", args=\""+Arrays.deepToString(args)+"]\", Exception: \""+exc+"\"");
    exc.printStackTrace();
}
            throw createScriptException(exc);       // create ScriptException and throw it
        }
    }


    /** Sends a Rexx message to the Rexx object contained in the addressed <code>RexxProxy</code> object.
     *
     *  <p>Note: Uses the implementation in {@link RexxScriptEngine.invokeMethod(Object thiz, String name, Object... args)}
     *           where this method belongs to.
     *
     *  @param thiz - The wrapped (as a <ocde>RexxProxy</code>) Rexx object to receive the message
     *
     *  @param name - The Rexx message (method) name.
     *
     *  @param args - The arguments (a Java array of type <code>Object</code>) for the Rexx method.
     *
     * <p>Note # 1: If any argument is of type <code>RexxProxy</code>, then the Rexx routine will
     *              receive the contained Rexx object instead.
     *
     * <p>Note # 2: This method will append an additional Rexx directory object argument with an entry named
     *          <code>&quot;SCRIPTCONTEXT&quot;</code>, which allows the Rexx routine to fully interact
     *          with the <code>ScriptContext</code> used for its invocation.
     *           <br>(Hint: one can always access this object from Rexx code with the statement
     *           <code>scriptcontext=arg('A')~lastItem~scriptContext</code>).)
     *
     * @return the object the Rexx routine returns. In the case that a Rexx object gets returned it will
     *         be wrapped (boxed) in a <code>RexxProxy</code>. Supplying a <code>RexxProxy</code> as an
     *         argument to a Rexx routine will cause the contained Rexx object to be used in Rexx instead.
     *
     *         <br>Hint: one can interact with the contained Rexx object of a <code>RexxProxy</code> by sending
     *         it messages from Java using its <code>sendMessage</code> methods or the
     *         {@link invokeMethod(Object thiz, String name, Object... args} method.
     *
    */
    public Object invokeMethod(Object thiz, String name, Object... args) throws ScriptException, NoSuchMethodException
    {
if (bDebug==true)
{
    System.err.println("---> RexxCompiledScript ---> invokeMethod(thiz=["+thiz+"], name=["+name+"], args=["+(args==null ? null : args.length)+"])");
    if (bShowSource)
    {
        System.err.println("---> scriptSource: --->\n"+scriptSource+"\n<--- end of scriptSource\n");
    }
}
            // use the implementation in RexxScriptEngine as this method is not CompiledScript related
        return rse.invokeMethod(thiz, name, args);
    }


    // --------------- utility methods

    /** Utility method that creates a <code>ScriptException</code> from the supplied <code>Exception</code>
     *  object.
     *
     * @param exc - The <code>Exception</code> object to process.
     *
     * @return a <code>ScriptException</code> object that either contains the supplied <code>Exception</code>
     *         object as its cause. If however {@link bSupplyBSFExceptionObject} is set to <code>false</code>
     *         then a Rexx like message with a line number, if available, gets created and used for the
     *         <code>ScriptException</code> object to be returned.
     */
    ScriptException createScriptException(Exception exc)
    {
if (bDebug==true)
{
    System.err.println("---> RexxCompiledScript ---> createScriptException(exc=["+exc+"])");
    if (bShowSource)
    {
        System.err.println("---> scriptSource: --->\n"+scriptSource+"\n<--- end of scriptSource\n");
    }
}
        if (bSupplyBSFExceptionObject==true )    // return a plain ScriptException supplying the causing exception
        {
            return new ScriptException(exc);
        }

            // just a plain BSFException or RexxException without the Rexx condition object: return causing exception
        if (! (exc instanceof RexxException) || (((RexxException)exc).getRexxConditionObject()==null) )
        {
            return new ScriptException(exc);
        }

        // o.k., we are supposed to fill in as much information as possible, instead of supplying the BSFException
        RexxProxy rco=((RexxException)exc).getRexxConditionObject();    // get Rexx condition object

        // TODO: create the values
        String message =createRexxLikeConditionMessage(rco);
        String filename="n/a";
        String position=null;
        try {
            filename=(String)rco.sendMessage0("PROGRAM");
            position=(String)rco.sendMessage0("POSITION");
        }
        catch (Exception bsfexc) {}

        int    line    =-1;      // indicate unknown line

        if (position instanceof String)
        {
            line= Integer.valueOf(position).intValue();
        }
        return new ScriptException(message, filename, line);
    }



    /** Utility method that creates the Rexx like condition message from the ooRexx condition object.
     *
     * ( Modelled after <code>BSF4ooRexx.cc</code>'s  <code>RgfCreateRexxlikeErrorInfo</code> function.)
     *
     *  @param rco a Rexx proxy allowing access to the Rexx condition object (a Rexx directory)
     *
     *  @return a Rexx like formatted error condition <code>String</code>
     */
    String createRexxLikeConditionMessage(RexxProxy rco)
    {
        StringBuffer sb=new StringBuffer();
        String conditionName=null;

        try
        {
            conditionName=(String) rco.sendMessage0("CONDITION");
            if (conditionName.equals("SYNTAX")) // format a syntax condition
            {
                // line # 1
                sb.append((String) ((RexxProxy) rco.sendMessage0("TRACEBACK")).sendMessage0("FIRSTITEM"));
                sb.append('\n');

                // line # 2
                sb.append(String.format("Error %s running %s line %s:  %s",
                                        (String) rco.sendMessage0("RC"),
                                        (String) rco.sendMessage0("PROGRAM"),
                                        (String) rco.sendMessage0("POSITION"),
                                        (String) rco.sendMessage0("ERRORTEXT")));
                sb.append('\n');
                sb.append(String.format("Error %s:  %s",
                                        (String) rco.sendMessage0("CODE"),
                                        (String) rco.sendMessage0("MESSAGE")));
            }

            else  if (conditionName.equals("NOVALUE"))  // format a novalue condition
            {
                // line # 1
                sb.append((String) ((RexxProxy) rco.sendMessage0("TRACEBACK")).sendMessage0("FIRSTITEM"));
                sb.append('\n');

                // line # 2
                sb.append(String.format("Rexx condition [%s] raised for variable [%s] running %s line: %s",
                                        conditionName,
                                        (String) rco.sendMessage0("DESCRIPTION"),
                                        (String) rco.sendMessage0("PROGRAM"),
                                        (String) rco.sendMessage0("POSITION")));
            }
                // as of ooRexx 4.2.0 for "failure", "error", "user": only entries CONDITION, RC (for failure and error) available
            else
            {
                // line # 1
                sb.append(String.format("Rexx condition [%s] raised running %s",
                                        conditionName, filename ));

                if (! (conditionName.startsWith("USER")))   // a failure or error condition has a RC value going with it
                {
                    sb.append(String.format(", return code [%s]",
                              (String) rco.sendMessage0("RC")));
                }

            }
        }
        catch (Exception exc)
        {
            throw new IllegalArgumentException("accessing argument \"rco\" with conditionname ["+conditionName+"] caused the exception: ["+exc+"]");
        }
        return sb.toString();
    }


    /** Creates and returns the argument array, append ScriptContext sc as last argument
     *  for the Rexx script to be able to fetch it. The arguments for Rexx will get a slotDir argument
     *  appended that contains an entry &quot;SCRIPTCONTEXT&quot; and either &quot;REXXCOMPILEDSCRIPT&quot;
     *  (if invoked by a <code>RexxCompiledScript</code>)
     *  or &quot;REXXSCRIPTENGINE&quot; (if invoked by a <code>RexxScriptEngine</code>).
     *
     *  <p>Note: As of ooRexx 4.2.0 the routine's <code>CALLWITH</code>
     *  method sends a <code>MAKEARRAY</code> message which will compact the Java array by removing <code>null</code>
     *  values (behaviour of {@link ArrayWrapper} to mimickry the ooRexx <code>Array MAKEARRAY</code> behaviour
     *  as close as possible
     *  (ooRexx arrays are capable of distinguishing whether an entry is empty (NULL) or is explicitly set
     *  to <code>.nil</code>). To keep the argument array intact when there are Java array entries with the
     *  value <code>null</code> a {@link RexxProxy} for the ooRexx <code>.nil</code> object is used for replacement.
     *  the Rexx code fetching such an argument needs to unwrap the contained ooRexx <code>.nil</code> object using
     *  the external BSF function <code>BsfRexxProxy(rexxProxy,"ooRexxObject")</code>.
     *
    *   @param args Object array with arguments to be supplied to the Rexx script
     *  @param sc ScriptContext to be appended to the arguments for the Rexx script
     *  @return an Object array containing all arguments to be passed on to the Rexx script
    */

// TODO: rgf, 2018-03-15, CALLWITH does not send MAKEARRAY message automatically, does it? If not, then supplying getNil() is not necessary!

    static Object[] createArgV(Object[] args, ScriptContext sc, RexxScriptEngine rse, RexxCompiledScript rcs) throws ScriptException, BSFException
    {

// TODO: rgf, 2017-10-09: after BetaTesting make sure that neither the RexxScriptEngine, nor the RexxCompiledScript get supplied to
//                        the Rexx script! (maybe make this controllable via the RexxScriptEngine for the Java host programmer ?)

        int argsLength= (args!=null ? args.length : 0);
        Object arrArgV[]=new Object[argsLength+1];
        int i=0;

        final boolean bDebug= false;
                              // true;
if (bDebug)
{
    System.err.println("/// in RexxCompiledScript.createArgV(): args["+args+"], sc=["+sc+"], rse=["+rse+"], rcs=["+rcs+"]");
}

        if (args!=null && args.length>0)    // if ARGV array is given and has entries, then create argument vector
        {
            // Object rexxNil=rse.getNil();
            for (i=0; i<args.length; i++)            // loop over arguments
            {
if (bDebug)
{
    System.err.println("\t===> createArgV(), args["+i+"]=["+args[i]+"]");
}
                // rgf, 2018-03-15: really necessary ?
                // Object o=args[i];
                // arrArgV[i]=( o==null ? rexxNil : o );    // maybe not really needed, think it over! ;)
                arrArgV[i]=o; // forgoes getNil(), so possible to remove it? ---rgf, 2018-03-15
            }
        }

            // add as last argument, so scripts can always fetch the current ScriptContext;
            // make sure last argument is a Rexx directory, with an entry "SCRIPTCONTEXT" storing the ScriptContext object
            // (this way Rexx routines - even those invoked from JavaProxy invocations - can rely that
            // the last argument is a directory containing an entry "SCRIPTCONTEXT")
        // arrArgV[i]=rse.getCreateDirectory().sendMessage2("CALL","SCRIPTCONTEXT",sc);
        RexxProxy rpDir=(RexxProxy) rse.getCreateSlotArgument().sendMessage2("CALL","SCRIPTCONTEXT",sc); // create .directory supply a first entry
        if (rcs!=null)
        {
            rpDir.sendMessage2("SETENTRY","REXXCOMPILEDSCRIPT",rcs);    // supply this RexxCompiledScript for reflective usage by Rexx program (e.g. for debugging)
        }

        // rgf, 2016-12-20: supply always the RESSCRIPTENGINE, if available // give the Rexx script the possibility to interact at leasat with the RexxScriptEngine (and get the currentScript from it
        if (rse!=null)
        {
            rpDir.sendMessage2("SETENTRY","REXXSCRIPTENGINE",rse);      // supply this RexxCompiledScript for reflective usage by Rexx program (e.g. for debugging)
        }
        arrArgV[i]=rpDir;
if (bDebug)
{
    System.err.println("\t===> createArgV(), after adding scriptcontext, returning arrArgV["+arrArgV+"].length=["+arrArgV.length+"]");
}
        return arrArgV;
    }

}
