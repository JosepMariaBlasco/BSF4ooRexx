package org.rexxla.bsf.engines.rexx.jsr223;
import javax.script.*;

import java.util.Vector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import java.util.List;
import java.util.Vector;

import org.apache.bsf.BSFManager;           // BSF support
import org.apache.bsf.BSFException;         // BSF support

import org.rexxla.bsf.engines.rexx.RexxEngine;
import org.rexxla.bsf.engines.rexx.RexxProxy;
import org.rexxla.bsf.engines.rexx.RexxException;

/*
------------------------ Apache Version 2.0 license -------------------------
   Copyright 2015 Rony G. Flatscher

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
        - 2015-05-19, ---rgf: fill-in the engine related information into the ENGINE_SCOPE bindings

*/


/** Class that extends extends the <code>javax.script.ScriptEngine</code> class and implements the
 *  <code>javax.script.Compilalbe</code> and *  the <code>javax.script.Invocable</code> interface,
 *  although the latter one would only make sense in {@link RexxCompiledScript} class, because of the
 *  {@link public <T> T getInterface(Class<T> clasz)} method. Therefore the author (Rony
 *  G. Flatscher who served on the JSR-223 specification committee) thinks that the documentation
 *  in <code>javax.script.Invocable</code> is false, as it states that <code>Invocable</code> gets implemented by a
 *  <code>ScriptEngine</code>, somehow indicating it has to operate on some &quot;state&quot; of the script engine.
 *  <br>Please note: the JSR-223 specification does not imply that ScriptEngine should implement this
 *  interface!
 * <br>The [oo]Rexx implementation of JSR-223 tries to be as flexible as possible and also implements
 * this interface in its implementation of <code>ScriptEngine</code> sharing as much code between
 * the tow (and caching compiled scripts for invoking the aforementioned method in a sensible way.
 *
 * @author Rony G. Flatscher
 * @since  2015-05-11
 *
 */
public class RexxScriptEngine extends AbstractScriptEngine implements Compilable, Invocable
{

    /* if set to false the Java compiler will remove all bDebug-controlled statements */
    static private final boolean bDebug=false; // true; // false; // true;

    /** This controls whether ScriptExceptions should supply the <code>RexxException</code> object (extends <code>BSFException</code>) or fill in the ScriptException
     *  fields (message, line and column number, if present). Using <code>BSFException.getCause()</code> will return
     *  the <code>RexxException</code> object, that has a method <code>getRexxConditionObject</code> which is a <code>RexxProxy</code> for
     *  the Rexx directory (cf. the ooRexx condition object documentation for its entries).
     * <br>If set to <code>false</code>, then an attempt is made to supply as much information according to
     *  the <code>ScriptException</code> constructors, which might ease generic script editors that use JSR223 script engines.
     */
    public static boolean bSupplyBSFExceptionObject=true;

    /** Caches the latest reader fetched from the script context. ooRexx will store this in <code>.input</code>, such that
     *  it is not necessary to set this value each time a script gets invoked or evaluated.
     */
    Reader             cachedReader;

    /** Caches the latest writer fetched from the script context. ooRexx will store this in <code>.output</code>, such that
     *  it is not necessary to set this value each time a script gets invoked or evaluated.
     */
    Writer             cachedWriter;

    /** Caches the latest writer fetched from the script context. ooRexx will store this in <code>.error</code>, such that
     *  it is not necessary to set this value each time a script gets invoked or evaluated.
     */
    Writer             cachedErrorWriter;

    /** Caches the last compiled script. This is necessary in the case that <code>Invocation</code> methods
     *  get invoked (namely &quot;<code>&lt;T&gt; T getInterface(Class;&lt;T&gt; clasz)/code>&quot;)
     *  via the ScriptEngine as the <code>javax.script.Invocable</code> documentation (in
     *  the author's (Rony G. Flatscher) opinion, who also has served on the JSR-223 specification
     *  team for javax.script) erroneously states that <code>Invocable</code> should be implemented
     *  by a <code>ScriptEngine</code> rather than a CompiledScript</code>.
     */
    RexxCompiledScript currentScript;

    /** Getter method for &quot;current&quot; (the latest evaluated) script. Due to the ooRexx architecture
     *  each script gets compiled (actually tokenized and its package set up) and will be stored in a field.
     *  Each script that gets evaluated will replace the {@link currentScript} object.
     *  Fetching this {@link RexxCompiledScript} allows one to execute it as many times as needed, but also
     *  use all of the implemented {@link Invocable} methods.
     *
     *  @return the current (latest) {@link RexxCompiledScript} object or <code>null</code> if no script has
     *          been evaluated or compiled yet
     */
    public RexxCompiledScript getCurrentScript()
    {
        return currentScript;
    }



    /** Rexx code to dynamically require &quot;<code>BSF.CLS</code>&quot;. This makes sure that the
     *  native part of <code>BSF4ooRexx</code> is set to support the camouflaging support from the beginning.
     */
    public static final String dynamicallyRequireBSF=".context~package~addPackage(.package~new('BSF.CLS')); /* <-- injected by RexxScriptEngine.compile() */ ";

    //  - define a public static flag that determines whether we should add a prolog statement to make sure that BSF.CLS gets required (dynamically in this case)
    /** This controls whether the supplied Rexx scripts should get a dynamic requires statement prepended that
     *  makes sure that the Java support is present. This may cause runtime errors, if the first statement in the
     *  Rexx script is a hash-bang line (if present it must be the very first line, because the ooRexx interpreter
     *  removes it for execution).
     */
    public static boolean bPrependDynamicRequiresStatement=true;


    /** This controls whether the initial requires for 'BSF.CLS' has been carried out or not, which is necessary
     *  to allow the very first Rexx script to pass Java objects as arguments already.
     */
    boolean bInitialBSFRequiresDone=false;


    /** Stores the BSF Rexx engine which gets used to invoke the scripts */
    RexxEngine bsfRexxEngine;

    /** Getter method that returns the BSF scripting engine in use.
     *  @return the BSF Rexx engine used for this RexxScriptEngine
     */
    public RexxEngine getBsfRexxEngine()
    {
        return bsfRexxEngine;
    }

    private Object nil=null;

    /** Getter method that returns the ooRexx <code>.nil</code> object.
     *  @return a <code>RexxProxy</code> referring to the ooRexx <code>.nil</code> object
     */
    public Object getNil()
    {
        if (nil==null)
        {
            nil=bsfRexxEngine.getNil();
        }
        return nil;
    }


    /** Stores the BSF Rexx manager that is used for loading the BSF Rexx engine. */
    BSFManager bsfMgr;

    /** Getter method that returns the BSF manager used for managing this BSF scripting engine.
     *  @return the BSF manager used for managing this RexxScriptEngine
     */
    public BSFManager getBsfManager()
    {
        return bsfMgr;
    }


    /** Rexx code to create and return a Rexx routine object from Rexx code. */
    String rexxCreateRoutineCode=
           "use strict arg name, code                   \n" +
           "return .routine~new(name,code)              \n" +
           "::requires 'BSF.CLS'                        \n" ;

    /** <code>RexxProxy</code> for the Rexx routine created by the Rexx code stored in {@link rexxCreateRoutineCodeCode}. */
    private RexxProxy rexxCreateRoutine=null;

    /** Getter method to return {@link rexxCreateRoutineCode}. If {@link rexxCreateRoutineCode} is <code>null</code>
     *  the Rexx routine object will get created, stored and returned.
     *  @return the <code>RexxProxy</code> for the Rexx routine object
     *          a single entry)
     */
    RexxProxy getCreateRoutine()  throws ScriptException
    {
if (bDebug==true){ System.err.println("arrived: getCreateRoutine(), rexxCreateRoutine="+rexxCreateRoutine);}
        if (rexxCreateRoutine!=null)
        {
            return rexxCreateRoutine;
        }

        String filename="RexxScriptEngine_rexxCreateRoutineCode.rex";
        try
        {
            updateRexxEngine(null);
            Vector args=new Vector();
            args.add("CreateRoutineCode");
            args.add(rexxCreateRoutineCode);

            rexxCreateRoutine=(RexxProxy) bsfRexxEngine.apply(filename, 0, 0, rexxCreateRoutineCode, null, args);
        }
        catch (Exception exc)
        {
if (bDebug==true) {exc.printStackTrace(); }
            throw new ScriptException(exc);     // re-throw, wrap cause in script
        }
        return rexxCreateRoutine;
    }


    /** Rexx code to create and return a Rexx routine object from Rexx code. */
    String rexxMonitorWrapperCode=
        " use arg rexxObject, scriptContext                                                                          \n" +
        " -- say 'in rexxMonitorWrapperCode prolog...'                                                               \n" +
        "                                                                                                            \n" +
        "     -- create and return dispatcher object                                                                 \n" +
        " return .DispatchWrapper~new(rexxObject, scriptContext)                                                     \n" +
        "                                                                                                            \n" +
        " ::requires BSF.CLS                              -- get Java camouflaging support                           \n" +
        "                                                                                                            \n" +
        "     -- dispatcher class for Invocable' <T> T getInterface(Object thiz, Class<T> clasz)                     \n" +
        " ::class 'DispatchWrapper'                                                                                  \n" +
        "                                                                                                            \n" +
        " ::method init              -- constructor                                                                  \n" +
        "   expose rexxScriptObject jsr223ScriptContext                                                              \n" +
        "   use strict arg rexxScriptObject, jsr223ScriptContext                                                     \n" +
        "                                                                                                            \n" +
        "    -- add ScriptContext to slotDir (last) argument, forward method invocation                              \n" +
        " ::method unknown           -- dispatch public routines instead                                             \n" +
        "   expose rexxScriptObject jsr223ScriptContext                                                              \n" +
        "   use strict arg methName, methArrArgv          -- unknown message name and its arguments                  \n" +
        "                                                                                                            \n" +
        "   -- add ScriptContext to BSF4ooRexx' slot argument (a Rexx directory added as last argument on callbacks) \n" +
        "   methArrArgv~lastItem~setentry('SCRIPTCONTEXT', jsr223ScriptContext) -- add ScriptContext                 \n" +
        "                                                                                                            \n" +
        "   forward message (methName) to (rexxScriptObject) arguments (methArrArgv) -- forward message              \n" ;

    /** <code>RexxProxy</code> for using an in-between (monitor) class for adding the <code>ScriptContext</code>
     *  to the slotDir directory added by BSF4ooRexx in Java call-back scenarios.
     */
    private RexxProxy rexxMonitorWrapper=null;

    /** Getter method to return the ooRexx routine. If {@link rexxMonitorWrapper} is <code>null</code>
     *  the Rexx routine object will get created, stored and returned.
     *  @return the <code>RexxProxy</code> for the Rexx routine object
     */
    RexxProxy getMonitorWrapper()  throws ScriptException
    {
if (bDebug==true){ System.err.println("arrived: getMonitorWrapper(), rexxMonitorWrapper="+rexxMonitorWrapper);}
        if (rexxMonitorWrapper!=null)
        {
            return rexxMonitorWrapper;
        }

        try
        {
            updateRexxEngine(null);
            rexxMonitorWrapper=(RexxProxy) getCreateRoutine().sendMessage2("CALL", "MonitorWrapperCode", rexxMonitorWrapperCode);
        }
        catch (Exception exc)
        {
if (bDebug==true) {exc.printStackTrace(); }
            throw new ScriptException(exc);     // re-throw, wrap cause in script
        }
        return rexxMonitorWrapper;
    }

    /** Rexx code to create and return a Rexx routine object from Rexx code. */
    String rexxRoutineWrapperCode=
        "use arg rexxScript, rexxPackage, scriptContext                                                             \n" +
        "                                                                                                           \n" +
        "-- say '/// --> /// in rexxRoutineWrapperCode - prolog... arg()='arg()                                     \n" +
        "                                                                                                           \n" +
        "    -- create and return dispatcher object                                                                 \n" +
        "return .DispatchToToPublicRoutines~new(rexxScript, rexxPackage, scriptContext)                             \n" +
        "                                                                                                           \n" +
        "::requires BSF.CLS                              -- get Java camouflaging support                           \n" +
        "                                                                                                           \n" +
        "    -- dispatcher class for Invocable' <T> T getInterface(Class<T> clasz)                                  \n" +
        "::class 'DispatchToToPublicRoutines'                                                                       \n" +
        "                                                                                                           \n" +
        "::method init              -- constructor                                                                  \n" +
        "  expose rexxScriptObject rexxScriptPackage jsr223ScriptContext                                            \n" +
        "  use strict arg rexxScriptObject, rexxScriptPackage, jsr223ScriptContext                                  \n" +
        "                                                                                                           \n" +
        "::method unknown           -- dispatch public routines instead                                             \n" +
        "  expose rexxScriptObject rexxScriptPackage jsr223ScriptContext                                            \n" +
        "  use strict arg methName, methArrArgv          -- unknown message name and its arguments                  \n" +
        "                                                                                                           \n" +
        "  pubRoutines=rexxScriptPackage~publicRoutines  -- get the public routines directory                       \n" +
        "  routine=pubRoutines~entry(methName)                                                                      \n" +
        "                                                                                                           \n" +
        "  -- if pubRoutines~hasentry(methName)=.false then -- if public routine not available raise error          \n" +
        "  if routine=.nil then -- if public routine not available raise error                                      \n" +
        "     raise syntax 93.1 array (methName)         -- routine not found!                                      \n" +
        "                                                                                                           \n" +
        "  -- add ScriptContext to BSF4ooRexx' slot argument (a Rexx directory added as last argument on callbacks) \n" +
        "  methArrArgv~lastItem~setentry('SCRIPTCONTEXT', jsr223ScriptContext) -- add ScriptContext                 \n" +
        "                                                                                                           \n" +
        "  routine~callWith(methArrArgv)                 -- invoke routine                                          \n" +
        "  if var('RESULT') then return result           -- if a result object got returned, return it              \n" ;

    /** <code>RexxProxy</code> for using an in-between (monitor) class for adding the <code>ScriptContext</code>
     *  to the slotDir directory added by BSF4ooRexx in Java call-back scenarios.
     */
    private RexxProxy rexxRoutineWrapper=null;

    /** Getter method to return the ooRexx routine. If {@link rexxRoutineWrapper} is <code>null</code>
     *  the Rexx routine object will get created, stored and returned.
     *  @return the <code>RexxProxy</code> for the Rexx routine object
     */
    RexxProxy getRoutineWrapper()  throws ScriptException
    {
if (bDebug==true){ System.err.println("arrived: getRoutineWrapper(), rexxRoutineWrapper="+rexxRoutineWrapper);}
        if (rexxRoutineWrapper!=null)
        {
            return rexxRoutineWrapper;
        }

        try
        {
            updateRexxEngine(null);
            rexxRoutineWrapper=(RexxProxy) getCreateRoutine().sendMessage2("CALL", "RoutineWrapperCode", rexxRoutineWrapperCode);
        }
        catch (Exception exc)
        {
if (bDebug==true) {exc.printStackTrace(); }
            throw new ScriptException(exc);     // re-throw, wrap cause in script
        }
        return rexxRoutineWrapper;
    }



    /** Rexx code to create and return a Rexx directory, optionally allowing for one entry-name and entry-value.
     *
     * @param entryName optional, a String denoting the entry name (will be uppercased by Rexx, if necessary)
     * @param entryValue optional, any object to be stored with <code>entryName</code>
     * @return a newly created Rexx directory object, with the supplied <code>entryName</code> and
     *         <code>entryValue</code>
     */

    String rexxRedirectStandardFilesCode=   // redirect ooRexx standard streams to the ScriptContext supplied ones
           "use arg in, out, err                          \n" +
           "-- redirect .input, .output, .error           \n" +
           "call bsf.RedirectTo 'Java'                , - \n" +
           "     .bsf.inputStream~new(in)             , - \n" +
           "     .bsf.outputStream~new(out)           , - \n" +
           "     .bsf.outputStream~new(err)               \n" +
           "                                              \n" +
           "::requires BSF.CLS    -- get Java support     \n" ;

    /** <code>RexxProxy</code> for the Rexx routine created by the Rexx code stored in {@link rexxRedirectStandardFilesCode}. */
    private RexxProxy rexxRedirectStandardFiles=null;

    /** Getter method to return {@link rexxRedirectStandardFiles}. If {@link rexxRedirectStandardFiles} is <code>null</code>
     *  the Rexx routine object will get created, stored and returned.
     *
     *  @return the <code>RexxProxy</code> for the Rexx routine object
     */
    RexxProxy getRedirectStandardFiles()  throws ScriptException
    {
if (bDebug==true){ System.err.println("arrived: getRedirectStandardFiles(), rexxgetRedirectStandardFiles="+rexxRedirectStandardFiles);}
        if (rexxRedirectStandardFiles!=null)
        {
            return rexxRedirectStandardFiles;
        }

        String filename="RexxScriptEngine_rexxRedirectStandardFilesCode.rex";
        try
        {
            updateRexxEngine(null);
            rexxRedirectStandardFiles=(RexxProxy) getCreateRoutine().sendMessage2("CALL", "RedirectStandardFiles", rexxRedirectStandardFilesCode);
        }
        catch (Exception exc)
        {
if (bDebug==true) {exc.printStackTrace();}
            throw new ScriptException(exc);     // re-throw, wrap cause in script
        }
        return rexxRedirectStandardFiles;
    }




    /** Rexx code to create and return a Rexx directory, optionally allowing for one entry-name and entry-value.
     *
     * @param entryName optional, a String denoting the entry name (will be uppercased by Rexx, if necessary)
     * @param entryValue optional, any object to be stored with <code>entryName</code>
     * @return a newly created Rexx directory object, with the supplied <code>entryName</code> and
     *         <code>entryValue</code>
     */
    String rexxCreateDirectoryCode=
           "use strict arg entryName=.nil, entryValue=.nil                  \n" +
           "dir=.directory~new                                              \n" +
           "if entryName<>.nil then                                         \n" +
           "   dir~setEntry(entryName,entryValue)                           \n" +
           "return dir          -- return Rexx directory object             \n" +
           "                                                                \n" +
           "::requires 'BSF.CLS'    -- add Java support                     \n" ;

    /** <code>RexxProxy</code> for the Rexx routine created by the Rexx code stored in {@link rexxCreateDirectoryCode}. */
    private RexxProxy rexxCreateDirectory=null;

    /** Getter method to return {@link rexxCreateDirectory}. If {@link rexxCreateDirectory} is <code>null</code>
     *  the Rexx routine object will get created, stored and returned.
     *  @return the <code>RexxProxy</code> for the Rexx routine to create a Rexx directory (and optionally supply
     *          a single entry)
     */
    RexxProxy getCreateDirectory()  throws ScriptException
    {
if (bDebug==true){ System.err.println("arrived: getCreateDirectory(), rexxCreateDirectory="+rexxCreateDirectory);}
        if (rexxCreateDirectory!=null)
        {
            return rexxCreateDirectory;
        }

        String filename="RexxScriptEngine_rexxCreateDirectoryCode.rex";
        try
        {
            updateRexxEngine(null);
/*
            Vector args=new Vector();
            args.add("createDirectory");
            args.add(rexxCreateDirectoryCode);

            rexxCreateDirectory=(RexxProxy) bsfRexxEngine.apply(filename, 0, 0, rexxCreateRoutineCode, null, args);
*/

            rexxCreateDirectory=(RexxProxy) getCreateRoutine().sendMessage2("CALL", "createDirectory", rexxCreateDirectoryCode);
        }
        catch (Exception exc)
        {
if (bDebug==true) {exc.printStackTrace();}
            throw new ScriptException(exc);     // re-throw, wrap cause in script
        }
        return rexxCreateDirectory;
    }



    /** Rexx code to create and return a Rexx RoutineAndPackage, optionally allowing for one entry-name and entry-value.
     *
     * @param name the name of the routine
     * @param script the code (a String) of the routine (a Rexx program)
     * @return a newly created Rexx RoutineAndPackage object, with the supplied <code>entryName</code> and
     *         <code>entryValue</code>
     */
    String rexxCreateRoutineAndPackageCode=
           "parse arg name, script         -- fetch script code                       \n" +
           "r=.routine~new(name, script)   -- create routine object                   \n" +
           "p=r~package                    -- get its package                         \n" +
           "                                                                          \n" +
           "   -- return Rexx objects in a Java array to ease access in the Java code \n" +
           "return bsf.createJavaArrayOf('java.lang.Object', -                        \n" +
           "            BsfCreateRexxProxy(r),  -     -- box it into a Java object    \n" +
           "            BsfCreateRexxProxy(p))        -- box it into a Java object    \n" +
           "                                                                          \n" +
           "::requires BSF.CLS                                                        \n" ;

    /** <code>RexxProxy</code> for the Rexx routine created by the Rexx code stored in {@link rexxCreateRoutineAndPackageCode}. */
    private RexxProxy rexxCreateRoutineAndPackage=null;

    /** Getter method to return {@link rexxCreateRoutineAndPackage}. If {@link rexxCreateRoutineAndPackage} is <code>null</code>
     *  the Rexx routine object will get created, stored and returned.
     *  @return the <code>RexxProxy</code> for the Rexx routine to create a Rexx RoutineAndPackage (and optionally supply
     *          a single entry)
     */
    RexxProxy getCreateRoutineAndPackage()  throws ScriptException
    {
if (bDebug==true){ System.err.println("arrived: getCreateRoutineAndPackage(), rexxCreateRoutineAndPackage="+rexxCreateRoutineAndPackage);}

        if (rexxCreateRoutineAndPackage!=null)
        {
            return rexxCreateRoutineAndPackage;
        }

        String filename="RexxScriptEngine_rexxCreateRoutineAndPackageCode.rex";
        try
        {
            updateRexxEngine(null);
/*
            Vector args=new Vector();
            args.add("createRoutineAndPackage");
            args.add(rexxCreateRoutineAndPackageCode);

            rexxCreateRoutineAndPackage=(RexxProxy) bsfRexxEngine.apply(filename, 0, 0, rexxCreateRoutineCode, null, args);
*/

            rexxCreateRoutineAndPackage=(RexxProxy) getCreateRoutine().sendMessage2("CALL", "createRoutineAndPackage", rexxCreateRoutineAndPackageCode);
        }
        catch (Exception exc)
        {
            // in this very initial phase of compilation (tokenization) of the Rexx script always supply the cause exception
            throw new ScriptException(exc);     // re-throw, wrap cause in script
        }
        return rexxCreateRoutineAndPackage;
    }



    /** Default constructor which creates the peer BSF Rexx engine instance. */
    public RexxScriptEngine()
    {
        super();
if (bDebug==true) { System.out.println("--> RexxScriptEngine() -->: "+this+" (CONSTRUCTOR!)"); }
        bsfMgr = new BSFManager ();                // get an instance of BSFManager
        try
        {
           bsfRexxEngine = (RexxEngine) bsfMgr.loadScriptingEngine("rexx");  // load the Rexx engine

            // get the default ScriptContext, fill-in engine related information using the official names
           ScriptContext sc=getContext(); // get default ScriptContext
           RexxScriptEngineFactory ref=RexxScriptEngineFactory.DEFAULT_REXX_SCRIPT_ENGINE_FACTORY;
           sc.setAttribute(ENGINE          , ref.getEngineName(),      ScriptContext.ENGINE_SCOPE);
           sc.setAttribute(ENGINE_VERSION  , ref.getEngineVersion(),   ScriptContext.ENGINE_SCOPE);
           sc.setAttribute(LANGUAGE        , ref.getLanguageName(),    ScriptContext.ENGINE_SCOPE);
           sc.setAttribute(LANGUAGE_VERSION, ref.getLanguageVersion(), ScriptContext.ENGINE_SCOPE);
           sc.setAttribute(NAME            , ref.getName(),            ScriptContext.ENGINE_SCOPE);
        }
        catch (Exception exc)
        {
            System.err.println(this+".RexxScriptEngine(), exception occured while attempting to load the Rexx-engine: "+exc);
            exc.printStackTrace();
            throw new RuntimeException(exc);
        }
     }


     /** Returns the default RexxScriptEngineFactory instance for the Rexx scripting language.
      *
      * @return the default RexxScriptEngine Factory (RexxScriptEngineFactory.DEFAULT_REXX_SCRIPT_ENGINE_FACTORY)
      */
    public ScriptEngineFactory getFactory()
    {
        return RexxScriptEngineFactory.DEFAULT_REXX_SCRIPT_ENGINE_FACTORY;
    }


    /** Creates and returns a SimpleBindings instance.
     * @return a new SimpleBindings instance
    */
    public Bindings createBindings()
    {
if (bDebug) {System.err.println("--> RexxScriptEngine() --> create.Bindings: "+this+".createBindings()");}

        return new SimpleBindings();
    }



    /** Creates the script from the supplied <code>Reader</code> and uses {@link Object eval (String script, ScriptContext sc)}
     *  to carry out the operation.
     *
     *  @param r - A <code>Reader</code> supplying the Rexx code to evaluate.
     *  @param sc - A <cdoe>ScriptContext</code> to use for evaluating the script
     *
     * @return the object the Rexx routine returns. In the case that a Rexx object gets returned it will
     *         be wrapped (boxed) in a <code>RexxProxy</code>. Supplying a <code>RexxProxy</code> as an
     *         argument to a Rexx routine will cause the contained Rexx object to be used in Rexx instead.
     *
     *         <br>Hint: one can interact with the contained Rexx object of a <code>RexxProxy</code> by sending
     *         it messages from Java using its <code>sendMessage</code> methods.
     *
     */
    public Object eval (Reader r, ScriptContext sc) throws ScriptException
    {
if (bDebug) {System.err.println("--> RexxScriptEngine().eval(Reader=["+r+"],ScriptContext=["+sc+"])..."); }
        if (r==null)        // first argument must be supplied!
        {
            throw new NullPointerException("Received \"Reader r\" argument null");
        }

        return this.eval(getReaderAsString(r),sc);     // let our core method execute the script
    }




    /** Creates a {@link RexxCompiledScript} object for the supplied Rexx script and then uses
     *  {@link RexxCompiledScript.eval(ScriptContext sc)} to carry out the operation.
     *
     * @param script - The Rexx script to evaluate.
     *
     *  @param sc - A <code>ScriptContext</code> object supplying information for evaluating the Rexx script
     *
     * @return the object the Rexx routine returns. In the case that a Rexx object gets returned it will
     *         be wrapped (boxed) in a <code>RexxProxy</code>. Supplying a <code>RexxProxy</code> as an
     *         argument to a Rexx routine will cause the contained Rexx object to be used in Rexx instead.
     *
     *         <br>Hint: one can interact with the contained Rexx object of a <code>RexxProxy</code> by sending
     *         it messages from Java using its <code>sendMessage</code> methods.
     *
     */
    public Object eval (String script, ScriptContext sc) throws ScriptException
    {
if (bDebug)
{
    System.err.println("--> RexxScriptEngine().eval(String=[---script---],ScriptContext=["+sc+"])...");
    dumpScriptContext(sc);
}

        if (script==null)
        {
            throw new NullPointerException("Received \"(String) script\" argument null");
        }

        if (sc==null)
        {
            throw new NullPointerException("Received \"(ScriptContext) sc\" argument null");
        }

        // same script as currentScript, if so, use that to eval()uate it
        if (currentScript!=null && script.equals(currentScript.scriptSource)==true)
        {
            return currentScript.eval(sc);      // evaluate the code with the given ScriptContext
        }
        else    // the script has to be processed/tokenized/compiled
        {
                // try to fetch supplied filename, if necessary, create artificial filename (for Rexx' parse source s)
            String filename=(String) sc.getAttribute(ScriptEngine.FILENAME);
            if (filename==null)     // no filename given
            {
                filename="filename_created_by_"+this.toString().replace('.','_').replace('@','-')+"_at_"+getGMTFormattedDate4Filename()+".rex";
            }

            CompiledScript rcs=compile(script, filename);   // create and get the compiled (tokenized) form
            return rcs.eval(sc);        // now evaluate the code with the given ScriptContext
        }
    }


// Compilable methods ------------------------------------------------------------------------
    /** Compiles (tokenizes) the script (source read from Reader) for later execution.
     *  The actual compilation (tokenization) will be carried out by {@link compile(String script)}.
     *
     *  @param script a Reader that supplies the characters of the script
     *
     *  @return a CompiledScript that can be repeatedly evaluate()'d or used to invoke functions
     *          of the script or methods in an object
    */
    public CompiledScript compile(Reader script) throws ScriptException
    {
if (bDebug) {System.err.println("--> RexxScriptEngine().compile(Reader=["+script+"])...");}
        if (script==null)        // first argument must be supplied!
        {
            throw new ScriptException(new NullPointerException("Received \"Reader r\" argument null"));
        }

        return compile(getReaderAsString(script));      // let our core method execute the script
    }

    /** Compiles (tokenizes) the script (source given as a String) for later execution.
     *  The actual compilation (tokenization) will be carried out by
     *  {@link compile(String script, String filename)}.
     *
     * @param script - The Rexx script to be compiled (tokenized).
     *
     *  @return a CompiledScript that can be repeatedly evaluate()'d or used to invoke functions
     *          of the script or methods in an object
     *
    */
    public CompiledScript compile(String script) throws ScriptException
    {
if (bDebug) {System.err.println("--> RexxScriptEngine().compile(String=[---script---])...");}
        if (script==null)        // first argument must be supplied!
        {
            throw new ScriptException(new NullPointerException("Received \"String script\" argument null"));
        }

        return compile(script,null);       // let our core method execute the script
    }

    /** Compiles the script (source represented as a String) for later execution.
     *
     * @param script - The Rexx script to be compiled (tokenized).
     *
     * @param filename - The filename to be used for evaluating the Rexx script. If this value is <code>null</code>
     *                   then a filename gets generated, which will include the time of generation in its name.
     * <p>Hint: this filename can be fetched from Rexx using the <ocde>PARSE SOURCE</code> keyword instruction.
     *
     *  @return a CompiledScript that can be repeatedly evaluate()'d or used to invoke functions
     *          of the script or methods in an object
    */
    CompiledScript compile(String script, String filename) throws ScriptException
    {
if (bDebug) {System.err.println("--> RexxScriptEngine().compile(String=[---script---], String=["+filename+"])...");}


        if (script==null)
        {
            throw new ScriptException( new NullPointerException("Received \"String script\" argument null"));
        }

        String scriptSource;
        if (bPrependDynamicRequiresStatement==true)     // inject dynamic requires for BSF.CLS
        {
            // check for hash bang line [#!/usr/bin/rexx] to inject code appropriately!
            if (script.charAt(0)=='#' && script.charAt(1)=='!')     // if hash-bang line, inject after \n!
            {
                int idx=script.indexOf('\n');
                // if no '\n' found then there is no Rexx code, hence do not inject at all
                if (idx>=0)      // found!
                {
                    scriptSource=script.substring(0,idx+1)+dynamicallyRequireBSF;
                    if (script.length()-1 > idx)
                    {
                        scriptSource=scriptSource+script.substring(idx+1);
                    }
                }
                else
                {
                    scriptSource=script;
                }
            }
            else
            {
                scriptSource=dynamicallyRequireBSF+script;
            }
        }
        else
        {
            scriptSource=script;
        }
if (bDebug==true)
{
    System.err.println("script -->\n"+script+"\n<--");
    System.err.println("scriptSource -->\n"+scriptSource+"\n<--");
}

        if (filename==null)     // no file name supplied, create an artificial one
        {
            filename="compiled_script_"+getGMTFormattedDate4Filename()+".rex";
        }

        Object[] rexxObjects;
        try {
            // make sure JNI is using BSF.CLS to fetch Java object arguments as Rexx proxies in Rexx programs
            updateRexxEngine(null);

            // now create the routine object, fetch it and its package object
            rexxObjects= (Object[]) getCreateRoutineAndPackage().sendMessage2("CALL", filename,scriptSource);

        }
        catch (Exception exc)
        {
if (bDebug==true) {exc.printStackTrace();}
            throw new ScriptException(exc);     // re-throw, wrap cause in script
        }

        currentScript=new RexxCompiledScript(this,
                                             script,
                                             filename,
                                             bSupplyBSFExceptionObject,     // controls creation of ScriptException
                                             (RexxProxy) rexxObjects[0],    // RexxProxy representing the script (routine)
                                             (RexxProxy) rexxObjects[1]);   // RexxProxy representing the package of the script (routine)

        return currentScript;            // save in cache, from now on all Invocable methods can be carried out via this ScriptEngine as well
    }

// -----------------------------------------------------------------------------------------------------
// Invocable methods
    /** Returns an implementation of an interface using functions compiled in the interpreter.
     *
     *  <p>Note: Uses the implementation in {@link RexxCompiledScript.getInterface(Class<T> clasz)}.
     *
     *  @param clasz - The Java interface class object <code>&lt;T&gt;</code>, whose methods are implemented as public Rexx routines.
     *
     *  @return a Java object of type <code>&lt;T&gt;</code>
     */
    public <T> T getInterface(Class<T> clasz)
    {
        if (currentScript==null)
        {
            throw new NullPointerException("there is no currentScript in cache to carry out the method, you need to eval() or compile a script before using this method");
        }
        // use the implementation in RexxCompiledScript as this method is not RexxScriptEngine related
        return currentScript.getInterface(clasz);
    }


    /** Returns an implementation of a Java interface where the Java method invocations will be implemented
     *  by the methods of the supplied Rexx object (a <code>RexxProxy</code>.
     *
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
        if (thiz==null)
        {
            throw new IllegalArgumentException("Received 'thiz' argument is null");

        }
        if (!(thiz instanceof RexxProxy))
        {
            throw new IllegalArgumentException("Received 'thiz' argument is not a Rexx script object (of type RexxProxy)");
        }

        if (clasz==null)
        {
            throw new IllegalArgumentException("Received 'clasz' argument is null");
        }
        if (! (clasz.isInterface()) )
        {
            throw new IllegalArgumentException("Received 'clasz' argument is not an interface class");
        }

        ScriptContext sc=getContext();  // get default ScriptContext
        RexxProxy rp;
        try {
                // make sure BSF.CLS support in JNI available; .input, .output, .error set accordingly
            updateRexxEngine(sc);

                // create and get the ooRexx dispatching object, working on the available public routines
            rp=(RexxProxy) getMonitorWrapper().sendMessage2("CALL", thiz, sc);
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

        // return (T) ((RexxProxy) thiz).newJavaProxyInstance(interfaces); // create and return the Java proxy for the interface
        return (T) rp.newJavaProxyInstance(interfaces); // create and return the Java proxy for the interface
    }



    /** Invokes (calls) a public Rexx routine defined in this compiled script.
     *
     *  <p>Note: Uses the implementation in {@link RexxCompiledScript} where this method belongs to.
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
    */
    public Object invokeFunction(String name, Object... args) throws ScriptException, NoSuchMethodException
    {
        if (currentScript==null)
        {
            throw new NullPointerException("there is no RexxCompiledScript in cache to carry out the method, you need to compile a script before using this method");
        }
        // use the implementation in RexxCompiledScript as this method is not RexxScriptEngine related
        return currentScript.invokeFunction(name,args);
    }


    /** Sends a Rexx message to the Rexx object contained in the addressed <code>RexxProxy</code> object.
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
        if (thiz==null)
        {
            throw new IllegalArgumentException("Received 'thiz' argument (script object to send message to) is null");
        }
        if (!(thiz instanceof RexxProxy))
        {
            throw new IllegalArgumentException("Received 'thiz' argument (script object to send message to) is not a RexxProxy received from Rexx");
        }
        if (name==null)
        {
            throw new NullPointerException("Received 'name' argument (method name) is null");
        }

        ScriptContext sc=getContext();      // get default ScriptContext

        Object arrArgV[];
        try
        {
            arrArgV=RexxCompiledScript.createArgV(args, sc, this);  // supply default context
        }
        catch (BSFException exc)
        {
            throw new ScriptException(exc);
        }

        try {
                // make sure BSF.CLS support in JNI available; .input, .output, .error set accordingly
            updateRexxEngine(sc);
            return ((RexxProxy) thiz).sendMessage(name,arrArgV);
        }
        catch (Exception exc)
        {
            throw new ScriptException (exc);
        }
    }



// utility methods ---------------------------------------------------------------------------

    /** Utility method to dump the content of the supplied <code>ScriptContext</code>.
     */
    public static void dumpScriptContext(ScriptContext sc)
    {
        List<Integer> scopes=sc.getScopes();
        System.err.println("scopes: "+scopes);

        for (int scope: scopes )
        {
            System.out.print("\tscope: "+scope);
                 if (scope==ScriptContext.GLOBAL_SCOPE) { System.err.println(" --> GLOBAL_SCOPE"); }
            else if (scope==ScriptContext.ENGINE_SCOPE) { System.err.println(" --> ENGINE_SCOPE"); }
            else
            {
                System.err.println();
            }

            Bindings bind=sc.getBindings(scope);
            System.err.println("\t\tscope "+scope+": ["+(bind==null ? null : bind.entrySet().toString())+"]");
        }
    }


    /** Utility method to format a <code>SimpleDateFormat</code> such that it can be used as part of a generated filename.
     *
     * @return a string indicating the current date and time suitable to be used as part of a filename
     */
    static public String getGMTFormattedDate4Filename()
    {
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy_MM_dd'T'HH_mm_ss_S'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }


    /** Utility method to read all characters of a <code>Reader</code> using a <code>BufferedReader</code>
     *  if necessary.
     *
     * @param r - The <code>Reader</code> object to read from.
     * @return a <code>String</code> representing built from the characters or null, if the
     *         supplied <code>Reader</code> was <code>null</code>
     */
    static String getReaderAsString(Reader r) throws ScriptException
    {
        if (r==null)        // no Reader supplied
        {
            return null;
        }

        if (! (r instanceof BufferedReader) )   // make sure we use a BufferedReader
        {
            r=new BufferedReader(r);
        }

        StringBuffer sb=new StringBuffer();
        try
        {
            int c=r.read();
            while(c != -1)
            {
                sb.append((char) c);
                c= r.read();
            }
        }
        catch (IOException ioe)
        {
            throw new ScriptException(ioe);
        }
        return sb.toString();
    }


    /** Utility method that makes sure that the native <code>BSF4ooRexx</code> interface for this Rexx
     *  interpreter instance is using <code>BSF.CLS</code> for making exchange of Java objects in
     *  arguments transparent to the Rexx program. In addition it will redirect the Rexx script's
     *  standard monitors <code>.input</code>, <code>.output</code> and <code>.error</code> to those
     *  supplied by the <code>ScriptContext</code>, if supplied at all.
     *
     * @param sc - The <code>ScriptContext</code> the Rexx script should use or <code>null</code>.
     *             If supplied, then the <code>ScriptContext</code>'s defined <code>Reader</code>,
     *             <code>Writer</code> and <code>ErrorWriter</code> get used to redirect the Rexx
     *             script's standard monitors <code>.input</code>, <code>.output</code> and
     *             <code>.error</code>.
     */
    void updateRexxEngine(ScriptContext sc) throws ScriptException, BSFException
    {
if (bDebug) {System.err.println("--> RexxScriptEngine().updateRexxEngine(), sc=["+sc+"] ...");}

            // make sure we have the native code set to support the camouflaging support from the beginning
        if (bInitialBSFRequiresDone==false) // make sure that JNI subsystem uses BSF.CLS for passing (Java object) arguments to Rexx scripts
        {
if (bDebug) {System.err.println("--> RexxScriptEngine().updateRexxEngine(), bInitialBSFRequiresDone==false !");}
            // the following statement makes sure, that the BSF infrastructure gets set up and is available
            bsfRexxEngine.apply(this.toString()+"_require_bsf", 0, 0, dynamicallyRequireBSF, null, null); // new Vector(), new Vector());
            bInitialBSFRequiresDone=true;
        }

        if (sc==null)   // if no ScriptContext supplied, we return here
        {
            return;
        }

if (bDebug) {System.err.println("    RexxScriptEngine(), getting and setting reader and writers ...");}

            // determine whether we need to change this Rexx engines .input, .output and/or .error monitors
        Reader tmpReader     =(Reader) sc.getReader();
        Writer tmpWriter     =(Writer) sc.getWriter();
        Writer tmpErrorWriter=(Writer) sc.getErrorWriter();

            // has there been a change in any of the reader or writers, if so reflect them!
        if ( (cachedReader!=tmpReader) || (cachedWriter!=tmpWriter) || (cachedErrorWriter!=tmpErrorWriter) )
        {
                // cache current values
            cachedReader     =tmpReader;
            cachedWriter     =tmpWriter;
            cachedErrorWriter=tmpErrorWriter;
            try
            {
                getRedirectStandardFiles().sendMessage3("CALL", tmpReader, tmpWriter, tmpErrorWriter);
            }
            catch (BSFException exc)
            {
if (bDebug==true) {exc.printStackTrace();}
                throw new ScriptException(exc);
            }
        }
    }

}
