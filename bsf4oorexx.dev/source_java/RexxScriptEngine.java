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
   Copyright 2015-2016 Rony G. Flatscher

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

        2016-11-09: ---rgf, added static boolean field "bRedirectStandardFiles" that controls whether
                            the ScriptContext's Reader, Writer and ErrorWriter should replace the
                            monitored objects of .input (.debugInput), .output or .error (.traceOutput)

        2016-11-08: ---rgf, supply null as new fourth argument to RexxCompiledScript.createArgV(...)
                            as we have no RexxCompiledScript available in the case of invokeMethod()

        2016-11-07, rgf:   - updateRexxEngine() method should be synchronized to make sure that
                             re-assigning standard files does not get interrupted, if multithreaded
                             usages

        2016-11-05, rgf:   - basing on 1.6/6.0, get rid of generic warnings

        2016-10-26, ---rgf: adjust to observed usage by JavaFX
                      - added protected field "latestRexxPackage": stores latest created Rexx package
                        (from last compile()) to be added to the package of the next to be compiled
                        Rexx script
                      - added protected field "bAddLatestPackageDynamically" and public
                        methods setAddLatestPackageDynamically() and getAddLatestPackageDynamically():
                        if set to true will add dynamically the last package to a new compiled Rexx
                        script thereby allowing later Rexx scripts to access any public routines and
                        public classes of any prior compiled Rexx scripts (includes access to package
                        BSF.CLS)
                      - change: public static field "bPrependDynamicRequiresStatement" (to inject a dynamic
                        requires to package 'BSF.CLS') now honored only, if field "bAddLatestPackageDynamically" is false
                      - changed method updateRexxEngine(): the very first time 'BSF.CLS' is dynamically
                        required, the package of that Rexx code is stored in the protected field
                        "latestRexxPackage", such that it can be added to new compiled Rexx scripts

        2015-08-04, ---rgf: adjust to info that starting with ooRexx 5.0.0 the string
                            argument to .routine~new must not contain CR/LF chars; prepare
                            all Rexx ocde herein to allow replacing CR-LF with semi-colons
                            by changing line comments to block comments; make sure
                            that the Rexx code that creates .routine objects carries out
                            a makearray for the code-part (allowing CR-LF as stored on
                            file or in databases, but also giving much more meaningful
                            line-based error information; Rexx, after all is meant to be
                            an easy to use and to debug language!)

        2015-07-21, ---rgf: no need to do a BsfCreateRexxProxy(), this will now be done
                            by bsf.createArrayOf() (in BSF.CLS)

        2015-05-19, ---rgf: fill-in the engine related information into the ENGINE_SCOPE bindings

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
 * @version 100.20161105
 * @since  2015-05-11
 *
 */
public class RexxScriptEngine extends AbstractScriptEngine implements Compilable, Invocable
{

    /* if set to false the Java compiler will remove all bDebug-controlled statements */
    static private final boolean bDebug=false; // true;

    /** This controls whether ScriptExceptions should supply the <code>RexxException</code> object (extends <code>BSFException</code>) or fill in the ScriptException
     *  fields (message, line and column number, if present). Using <code>BSFException.getCause()</code> will return
     *  the <code>RexxException</code> object, that has a method <code>getRexxConditionObject</code> which is a <code>RexxProxy</code> for
     *  the Rexx directory (cf. the ooRexx condition object documentation for its entries).
     * <br>If set to <code>false</code>, then an attempt is made to supply as much information according to
     *  the <code>ScriptException</code> constructors, which might ease generic script editors that use JSR223 script engines.
     */
    public static boolean bSupplyBSFExceptionObject=true;

    /** Determines whether the ScriptContext's Reader, Writer and ErrorWriter should replace the Rexx script
     *  .input (and also .debugInput, if present), .output, .error (and also .traceOutput if present). There
     *  may be situations (like a GUI based interface to run Rexx scripts) where this automatic redirection
     *  should be inhibited.
     */
    public static boolean bRedirectStandardFiles=true;

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


    /** The Rexx code to inject for the Rexx script annotation <code>@GET</code>. Get the last
     *  argument (a Rexx Directory, dubbed "slotDir") and fetch the entry "ScriptContext" from it.
    */
    public static String strFormatGET=" ; call BSFContextVariables 'set', .jsr223~get(arg(arg())~scriptContext, %s) ; ";

    /** The Rexx code to inject for the Rexx script annotation <code>@SET</code>.
     *  Get the last argument (a Rexx Directory, dubbed "slotDir") and fetch the entry "ScriptContext" from it.
    */
    public static String strFormatSET=" ; .jsr223~set(arg(arg())~scriptContext, %s, BSFContextVariables('get')) ; ";

    /** Rexx code to dynamically require &quot;<code>BSF.CLS</code>&quot;. This makes sure that the
     *  native part of <code>BSF4ooRexx</code> is set to support the camouflaging support from the beginning.
     */
    public static final String dynamicallyRequireBSF=".context~package~addPackage(.package~new('BSF.CLS')); /* <-- injected by RexxScriptEngine.compile() */ ";

    //  - define a public static flag that determines whether we should add a prolog statement to make sure that BSF.CLS gets required (dynamically in this case)
    /** This controls whether the supplied Rexx scripts should get a dynamic requires statement prepended that
     *  makes sure that the Java support is present. Only honored, if the instance field {@link bAddLatestPackageDynamically}
     *  is set to <code>false</code>.
     */
    public static boolean bPrependDynamicRequiresStatement=true;


    /** This controls per RexxScriptEngine instance whether the Rexx package of a run script gets added
     *  to the Rexx package of the next compiled script. This makes all public routines and public
     *  classes of previous Rexx packages available for a new Rexx package.
     */
    boolean bAddLatestPackageDynamically=true;

    /** Allows to change the behaviour from adding the {@link latestRexxPackage} to new Rexx routines.
     *  Default is <code>true</code>, which makes sure that any new Rexx routine will get the latest
     *  Rexx package added to it, thereby gaining access to the package <code>BSF.CLS</code> and all
     *  previously defined public routines and public classes.
     *
     *  @param newValue <code>true</code> or <code>false</code>
     */
    public void setAddLatestPackageDynamically (boolean newValue)
    {
        if (bAddLatestPackageDynamically==newValue)
        {
            bAddLatestPackageDynamically=newValue;
        }
    }

    /** Returns the current setting of {@link bAddLatestPackageDynamically}.
     *
     *  @return current setting of {@link bAddLatestPackageDynamically}.
     */
    public boolean getAddLatestPackageDynamically ()
    {
        return bAddLatestPackageDynamically;
    }



    /** This controls whether the initial requires for 'BSF.CLS' has been carried out or not, which is necessary
     *  to allow the very first Rexx script to pass Java objects as arguments already.
     */
    boolean bInitialBSFRequiresDone=false;

    /** This field stores the Rexx package of the last Rexx script that got compiled and added the
     *  previous Rexx package stored in this field prior to it.
     *  This way each Rexx script that gets compiled will gain access to
     *  all previously defined public routines and public classes. This is the model e.g. JavaFX expects,
     *  each ScriptEngine acquires public members of previously executed code.
     */
    RexxProxy latestRexxPackage=null;


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


    // 2015-08-04, rgf: in upcoming ooRexx 4.0.0 no CR- or LF- chars are allowed!
    /** Rexx code to create and return a Rexx routine object from Rexx code. */
    String rexxCreateRoutineCode=
           "use strict arg name, code                      ;" +
               // make sure that we turn the string into a Rexx array,
               // because .routine will accept that in any case in all versions of ooRexx:
           "if code~isA(.array) then                       ;" +
           "   return .routine~new(name,code)              ;" +
           "else                                           ;" +
           "   return .routine~new(name,code~makeArray)    ;" +
           "::requires 'BSF.CLS'                           ;" ;

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
            if (bInitialBSFRequiresDone==false) updateRexxEngine(null);
            Vector<String> args=new Vector<String>();
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
        " use arg rexxObject, scriptContext                                                                             \n" +
        " /* say 'in rexxMonitorWrapperCode prolog...'                                                      */          \n" +
        "                                                                                                               \n" +
        "     /* create and return dispatcher object                                                        */          \n" +
        " return .DispatchWrapper~new(rexxObject, scriptContext)                                                        \n" +
        "                                                                                                               \n" +
        " ::requires BSF.CLS                              /* get Java camouflaging support                  */          \n" +
        "                                                                                                               \n" +
        "     /* dispatcher class for Invocable' <T> T getInterface(Object thiz, Class<T> clasz)            */          \n" +
        " ::class 'DispatchWrapper'                                                                                     \n" +
        "                                                                                                               \n" +
        " ::method init              /* constructor                                                         */          \n" +
        "   expose rexxScriptObject jsr223ScriptContext                                                                 \n" +
        "   use strict arg rexxScriptObject, jsr223ScriptContext                                                        \n" +
        "                                                                                                               \n" +
        "    /* add ScriptContext to slotDir (last) argument, forward method invocation                     */          \n" +
        " ::method unknown           /* dispatch public routines instead                                    */          \n" +
        "   expose rexxScriptObject jsr223ScriptContext                                                                 \n" +
        "   use strict arg methName, methArrArgv          /* unknown message name and its arguments         */          \n" +
        "                                                                                                               \n" +
        "   /* add ScriptContext to BSF4ooRexx' slot argument (a Rexx directory added as last argument on callbacks) */ \n" +
        "   methArrArgv~lastItem~setentry('SCRIPTCONTEXT', jsr223ScriptContext)      /* add ScriptContext   */          \n" +
        "                                                                                                               \n" +
        "   forward message (methName) to (rexxScriptObject) arguments (methArrArgv) /* forward message     */          \n" ;

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
            if (bInitialBSFRequiresDone==false) updateRexxEngine(null);
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
        "use arg rexxScript, rexxPackage, scriptContext                                                                 \n" +
        "                                                                                                               \n" +
        "/* say '/// --> /// in rexxRoutineWrapperCode - prolog... arg()='arg()                             */          \n" +
        "                                                                                                               \n" +
        "    /* create and return dispatcher object                                                         */          \n" +
        "return .DispatchToToPublicRoutines~new(rexxScript, rexxPackage, scriptContext)                                 \n" +
        "                                                                                                               \n" +
        "::requires BSF.CLS                              /* get Java camouflaging support                   */          \n" +
        "                                                                                                               \n" +
        "    /* dispatcher class for Invocable' <T> T getInterface(Class<T> clasz)                          */          \n" +
        "::class 'DispatchToToPublicRoutines'                                                                           \n" +
        "                                                                                                               \n" +
        "::method init              /* constructor                                                          */          \n" +
        "  expose rexxScriptObject rexxScriptPackage jsr223ScriptContext                                                \n" +
        "  use strict arg rexxScriptObject, rexxScriptPackage, jsr223ScriptContext                                      \n" +
        "                                                                                                               \n" +
        "::method unknown           /* dispatch public routines instead                                     */          \n" +
        "  expose rexxScriptObject rexxScriptPackage jsr223ScriptContext                                                \n" +
        "  use strict arg methName, methArrArgv          /* unknown message name and its arguments          */          \n" +
        "                                                                                                               \n" +
        "  pubRoutines=rexxScriptPackage~publicRoutines  /* get the public routines directory               */          \n" +
        "  routine=pubRoutines~entry(methName)                                                                          \n" +
        "  /*                                                                                                           \n" +
        "  -- if pubRoutines~hasentry(methName)=.false then -- if public routine not available raise error              \n" +
        "  */                                                                                                           \n" +
        "  if routine=.nil then /* if public routine not available raise error                              */          \n" +
        "     raise syntax 93.1 array (methName)         /* routine not found!                              */          \n" +
        "                                                                                                               \n" +
        "  /* add ScriptContext to BSF4ooRexx' slot argument (a Rexx directory added as last argument on callbacks) */  \n" +
        "  methArrArgv~lastItem~setentry('SCRIPTCONTEXT', jsr223ScriptContext) /* add ScriptContext         */          \n" +
        "                                                                                                               \n" +
        "  routine~callWith(methArrArgv)                 /* invoke routine                                  */          \n" +
        "  if var('RESULT') then return result           /* if a result object got returned, return it      */          \n" ;

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
            if (bInitialBSFRequiresDone==false) updateRexxEngine(null);
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
           "/* redirect .input, .output, .error       */  \n" +
           "call bsf.RedirectTo 'Java'                , - \n" +
           "     .bsf.inputStream~new(in)             , - \n" +
           "     .bsf.outputStream~new(out)           , - \n" +
           "     .bsf.outputStream~new(err)               \n" +
           "                                              \n" +
           "::requires BSF.CLS    /* get Java support */  \n" ;

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
            if (bInitialBSFRequiresDone==false) updateRexxEngine(null);
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
           "return dir          /* return Rexx directory object */          \n" +
           "                                                                \n" +
           "::requires 'BSF.CLS'    /* add Java support         */          \n" ;

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
            if (bInitialBSFRequiresDone==false) updateRexxEngine(null);
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
           "parse arg name, script                      /* fetch script code            */    ;" +
           "                                                                                  ;" +
           "if script~isA(.array) then                  /* argument already an array    */    ;" +
           "   r=.routine~new(name, script)             /* create routine object        */    ;" +
           "else                                                                              ;" +
           "   r=.routine~new(name, script~makeArray)   /* create routine object        */    ;" +
           "                                                                                  ;" +
           "p=r~package                                 /* get its package              */    ;" +
           "                                                                                  ;" +
           "   /* return Rexx objects in a Java array to ease access in the Java code   */    ;" +
             // note, no continuation char as everything is in the same single line
           "return bsf.createJavaArrayOf('java.lang.Object',                                   " +
           "                               r ,          /* Rexx routine object          */     " +
           "                               p )          /* Rexx package object          */    ;" +
           "                                                                                  ;" +
           "::requires BSF.CLS                                                                ;" ;

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
            if (bInitialBSFRequiresDone==false) updateRexxEngine(null);
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
    CompiledScript compile(final String script, String filename) throws ScriptException
    {
if (bDebug) {System.err.println("--> RexxScriptEngine().compile(String=[---script---], String=["+filename+"])...");}

        if (script==null)
        {
            throw new ScriptException( new NullPointerException("Received \"String script\" argument null"));
        }

        String     tmpScriptSource=script;  // the script that will get compiled (tokenized), maybe edited (code injected)
        boolean    bGotScriptEdited=false;  // true, if Rexx script got edited via injections


        RexxAnnotation ran=new RexxAnnotation(script, "showsource", 0); // try to get the Rexx script annotation "/* @showsource */"
        boolean bShowSource=(ran.posStartAnnotationChar>-1);    // determine, whether this Rexx script property is present
        if (bShowSource)    // show (for debugging purposes the supplied Rexx script)
        {
            System.err.println("Rexx script annotation \"/*@showsource*/\" present in file [" +filename+"], hence showing :"
                                +"\n\n----------- begin of Rexx script ----------- \n"
                                +script
                                +"\n----------- end of Rexx script (\"/*@showsource*/\" Rexx script annotation) -----------\n");
        }

        // - process @GET, @GET() and @SET() Rexx script annotations; if @SET() is empty, give a warning and ignore,
        //   save the edited script
        // - if Rexx script annotation SHOWSOURCE is given, show the edited script as well
        String rexxAnnos[] =new String[] { "GET", "SET" };  // Rexx script annotations to be processed

        // strFormatGET; use String.format(strFormatGET, ran.contentAsRexxString)
        // strFormatSET; use String.format(strFormatGET, ran.contentAsRexxString), // warning, if no attribute list is given, in which case do not inject code
        // TODO: make sure "script" String is copied and we work on the copy, after each rxAnno create the new "script" String off the StringBuffer
        for (String rxAnno : rexxAnnos)     // iterate over Rexx script annotations
        {
            StringBuilder  sbEditedScript=null; // StringBuilder
            int currPos=0;
            int prevPosStart=0;     // start of tmpScriptSource.substring() that determines which chunk of the script should be appended, if necessary
            int prevPosEnd=-1;      // end of tmpScriptSource.substring() that determines which chunk of the script should be appended, if necessary
            int tssLength=tmpScriptSource.length(); // get length of current temporary script source
                // get annotation, if any
            while ( currPos<tssLength       &&  // unprocessed chars left?
                    (ran=new RexxAnnotation(tmpScriptSource, rxAnno, currPos)).posStartAnnotationChar>=0 && // get and check whether the sought annotation was found
                     ran.isAnnotationWithinBlockCommentOnSameLine   // a valid annotation that has also an ending block comment?
                  )
            {
// System.err.println("--- 0 --- found a [@"+rxAnno+"], begin BComment: ["+ran.posStartBlockComment+"], '@'-pos: ["+ran.posStartAnnotationChar+"]...");
                    // allow one annotation per block comment only
                if (sbEditedScript==null)       // not yet created?
                {
                    sbEditedScript=new StringBuilder();
                }

                    // append script code before injection point
                prevPosEnd=ran.posStartBlockComment;    // everything up to, but not including open block comment

                if (prevPosEnd>prevPosStart)    // is there anything to be carried over from the original script ?
                {
// System.err.println("--- 1 --- adding script code before annotation, prevPosStart="+prevPosStart+", prevPosEnd="+prevPosEnd+"\n--->>>["+tmpScriptSource.substring(prevPosStart, prevPosEnd)+"]\n<<<---");

                    sbEditedScript.append( tmpScriptSource.substring(prevPosStart, prevPosEnd));
                    prevPosStart=prevPosEnd;
                }

// System.err.println("--- 2 --- RSE.compile(): now prevPosStart="+prevPosStart+", prevPosEnd="+prevPosEnd);

                    // inject code
                sbEditedScript.append( String.format( (rxAnno.startsWith("G") ? strFormatGET  : strFormatSET), ran.contentAsRexxString) );

                    // append entire Rexx script annotation (if multiple annotations of the same kind, the first one gets honored)
                prevPosEnd=ran.posEndBlockComment+2;

// System.err.println("--- 3 --- RSE.compile(): after appending code for ["+rxAnno+"] annotation, new prevPosEnd: ["+prevPosEnd+"]");
                // extract script section and append, if anything to append
                if (prevPosStart<prevPosEnd)
                {
// System.err.println("--- 4 --- RSE.compile(): add original script section - prevPosStart="+prevPosStart+", prevPosEnd="+prevPosEnd+", hence: ["+tmpScriptSource.substring(prevPosStart, prevPosEnd)+"]");
                    sbEditedScript.append( tmpScriptSource.substring(prevPosStart, prevPosEnd));
                    // set currPos and prevPosStart
                    prevPosStart=prevPosEnd;
// System.err.println("--- 4b --- RSE.compile(): now currPos=prevPosStart=prevPosEnd: ["+prevPosEnd+"]");
                }
                currPos=prevPosEnd;     // set currPos after current Rexx script annotation's end

// System.err.println("--- 5 --- RSE.compile(): processing of this annotation ended, next round: currPos="+currPos+", prevPosStart="+prevPosStart+", prevPosEnd="+prevPosEnd);
            }

                // did we edit the script source?
            if (sbEditedScript!=null)   // code got injected!
            {
                bGotScriptEdited=true;      // indicate that the script got edited
                    // add remaining script code, re-create script String value to make sure
                if (prevPosEnd<tssLength)   // do we have a section of the script left to be appended ?
                {
                    // sbEditedScript.append( tmpScriptSource.substring(prevPosStart, tssLength));
                    sbEditedScript.append( tmpScriptSource.substring(prevPosEnd, tssLength));
// System.err.println("--- 6 --- RSE.compile(): appending remaining script - prevPosEnd="+prevPosEnd+", tssLength="+tssLength+", appended from String:"+"\n--->>>["+tmpScriptSource.substring(prevPosEnd, tssLength)+"]\n<<<---");
                }
                tmpScriptSource=sbEditedScript.toString();  // create a String off it
                sbEditedScript=null;                        // reset StringBuilder variable to null

// System.err.println("--- 7 --- RSE.compile(): finished editing for ["+rxAnno+"] annotations, sbEditedScript.toString() --> ---> ===>["+tmpScriptSource+"] <=== <--- <--");
            }
        }

          // only necessary, if BSF-support is needed and no dynamic package additions take place (which would include the package BSF.CLS)
        if (bAddLatestPackageDynamically==false && bPrependDynamicRequiresStatement==true)     // inject dynamic requires for BSF.CLS
        {
            bGotScriptEdited=true;      // indicate that the script got edited
            // check for hash bang line [#!/usr/bin/rexx] to inject code appropriately!
            if (tmpScriptSource.charAt(0)=='#' && tmpScriptSource.charAt(1)=='!')     // starts with hash-bang line, inject after \n!
            {
                int idx=tmpScriptSource.indexOf('\n');

                // if no '\n' found then there is no Rexx code, hence do not inject at all
                if (idx>=0)      // found!
                {
                    String tmpStr=tmpScriptSource.substring(0,idx+1)+dynamicallyRequireBSF;
                    if (idx<tmpScriptSource.length())   // script code to be added
                    {
                        tmpStr=tmpStr+tmpScriptSource.substring(idx+1);
                    }
                    tmpScriptSource=tmpStr;
                }
            }
            else    // merely prepend the script source
            {
                tmpScriptSource=dynamicallyRequireBSF+tmpScriptSource;
            }
        }

        if (filename==null)     // no file name supplied, create an artificial one
        {
            filename="compiled_script_"+getGMTFormattedDate4Filename()+".rex";
        }

        Object[] rexxObjects;
        try {
            // make sure JNI is using BSF.CLS to fetch Java object arguments as Rexx proxies in Rexx programs
            if (bInitialBSFRequiresDone==false) updateRexxEngine(null);

            // now create the routine object, fetch it and its package object
            rexxObjects= (Object[]) getCreateRoutineAndPackage().sendMessage2("CALL", filename,tmpScriptSource);

        }
        catch (Exception exc)
        {
if (bDebug==true) {exc.printStackTrace();}
            throw new ScriptException(exc);     // re-throw, wrap cause in script
        }

        // rgf, 2016-10-26: add dynamically the latestRexxPackage to gain access to all its public routines and public classes
        if (bAddLatestPackageDynamically)  // add the latest package (from last compile)
        {
            try
            {
                RexxProxy rexxPackage=(RexxProxy) rexxObjects[1];
                rexxPackage.sendMessage1("ADDPACKAGE", latestRexxPackage);   // add the current latest RexxPackage to it
                latestRexxPackage = rexxPackage;    // this is from now on the latestRexxPackage to be used for the next script
            }
            catch (Exception exc)
            {
if (bDebug==true) {exc.printStackTrace();}
                throw new ScriptException(exc);     // re-throw, wrap cause in script
            }
        }

        currentScript=new RexxCompiledScript(this,
                                             script,
                                             filename,
                                             bSupplyBSFExceptionObject,     // controls creation of ScriptException
                                             (RexxProxy) rexxObjects[0],    // RexxProxy representing the script (routine)
                                             (RexxProxy) rexxObjects[1]);   // RexxProxy representing the package of the script (routine)

        if (bGotScriptEdited==true)     // save the edited script that was actually compiled (tokenized)
        {
            currentScript.editedScriptSource=tmpScriptSource;
            if (bShowSource)
            {
                System.err.println("Rexx script annotation \"/*@showsource*/\" present in file [" +filename+"], hence showing :"
                                    +"\n\n----------- begin of EDITED Rexx script ----------- \n"
                                    +tmpScriptSource
                                    +"\n----------- end of EDITED Rexx script (\"/*@showsource*/\" Rexx script annotation) -----------\n");
            }
        }

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
    @SuppressWarnings("unchecked") // RexxProxy.newJavaProxyInstance(interfaces) returns a java.lang.reflect.Proxy that gets cast to T
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
        RexxProxy rp=null;
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
            arrArgV=RexxCompiledScript.createArgV(args, sc, this, null);  // supply default context; no access to RexxCompiledScript
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

// TODO: 2016-11-07, rgf: synchronized method?
//          check whether current re-assignments are in place already or not
    synchronized void updateRexxEngine(ScriptContext sc) throws ScriptException, BSFException
    {
if (bDebug) {System.err.println("--> RexxScriptEngine().updateRexxEngine(), sc=["+sc+"] ...");}

            // make sure we have the native code set to support the camouflaging support from the beginning
        if (bInitialBSFRequiresDone==false) // make sure that JNI subsystem uses BSF.CLS for passing (Java object) arguments to Rexx scripts
        {
if (bDebug) {System.err.println("--> RexxScriptEngine().updateRexxEngine(), bInitialBSFRequiresDone==false !");}
            // the following statement makes sure, that the BSF infrastructure gets set up and is available
            // the package object will be returned by the Rexx code and can be used for adding it to
            // the next Rexx script that gets compiled
            latestRexxPackage=(RexxProxy) bsfRexxEngine.apply(this.toString()+"_require_bsf", 0, 0,
                                          dynamicallyRequireBSF +";return .context~package",
                                          null, null); // new Vector(), new Vector());
            bInitialBSFRequiresDone=true;
        }

        if (sc==null)   // if no ScriptContext supplied, we return here
        {
            return;
        }

        if (bRedirectStandardFiles==true)
        {
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
/*
System.err.println("... debug, add a few Bindings as scopes 13, 309, 155:");
try {
int scope=113;
System.err.println("... debug, scope: "+scope);
    sc.setBindings(new SimpleBindings(), scope);
scope=309;
System.err.println("... debug, scope: "+scope);
    sc.setBindings(new SimpleBindings(), scope);
scope=155;
System.err.println("... debug, scope: "+scope);
    sc.setBindings(new SimpleBindings(), scope);
}
catch (Exception e)
{
    System.err.println("... debug, OOPS exception, e="+e);
    e.printStackTrace();
}

System.err.println("... debug, iterate over ScriptContext.getScopes():");
for (Integer i: sc.getScopes())
{
    System.err.println("\tscope value i="+i);

}
*/


    }



    /** Inner class for determining a RexxScript annotation, if one exists, the annotation field will
     *  refer to the appropriate enum value and all fields are filled in to ease processing of the
     *  script's code by the caller.
     *  <p>
     *  A Rexx script annotation is a block comment at the very beginning or end of a Rexx statement,
     *  which starts with the at character (@) and followed by a name. Optionally the name may be followed
     *  by a pair of parenthesis that may contain a single (quoted) Rexx string, which gets interpreted
     *  according to the annotation name in hand.
     * <p>
     * Currently the following Rexx script annotations are defined:
     * <ul>
     * <li>@SHOWSOURCE
     * <li>@GET or @GET( blank delimited string of attribute names to be added as Rexx context variables  )
     * <li>@SET or @SET[ blank delimited string of attribute names to be updated by their Rexx context variable values )
     * </ul>
     *
     * @since 2016-11
     */
    public static class RexxAnnotation
    {
        /** The currently supported Rexx script annotation names need to be led in with an at (@) character,
         *  the name of the annotation and if values are supplied with it, then they must be given within
         *  parenthesis immediately following the annotation name.
         */
        public enum RexxAnnotationEnum
        {
            SHOWSOURCE,
            GET,
            SET
        };


        /** Stores the character position where a Rexx block comment starts. */
        int posStartBlockComment   =-1;
        /** Stores the character position where a Rexx block comment ends. */
        int posEndBlockComment     =-1;

        /** Stores the character position of the at (@) character. */
        int posStartAtChar         =-1;

        /** Stores the character position where the annotation name starts. */
        int posStartAnnotationChar =-1;
        /** Stores the character position where the annotation name ends. */
        int posEndAnnotationChar   =-1;

        /** Stores the character position of the open parenthesis character. */
        int posOpenParenthesis     =-1;
        /** Stores the character position of the close parenthesis character. */
        int posCloseParenthesis    =-1;

        /** Stores the first non-blank character after the open parenthesis character. */
        char contentFirstChar      =0x00;   //  first non-blank character after open parenthesis, maybe a charRexx quote, ie. ('), (")
        /** Stores the character position where the first non-blank character after the open parenthesis is located. */
        int  contentFirstCharPos   =-1;     //  position in script

        /** Stores the last non-blank character before the close parenthesis character. */
        char contentLastChar       =0x00;   //  last non-blank character before close parenthesis, maybe a charRexx quote, ie. ('), (")
        /** Stores the character position where the last non-blank character before the close parenthesis is located. */
        int  contentLastCharPos    =-1;     //  position in script

        /** Indicates whether the content represents a properly quoted Rexx string. */
        boolean isContentProperlyQuoted =false;  // is the content, if any, quoted properly for Rexx ?
        /** Stores the content as is. */
        String contentAsIs         =null;   // non-whitespace content between parenthesis, if any
        /** Stores the content as a properly quoted Rexx string. */
        String contentAsRexxString =null;   // non-whitespace content between parenthesis as a valid Rexx string

        /** Indicates whether the annotation is on the same line as the opening Rexx block comment and possesses
         *  a matching closing Rexx block comment.
         */
        boolean isAnnotationWithinBlockCommentOnSameLine=false;  // Rexx script annotation must be in same line as the beginning block comment

        /** Stores the Rexx script annotation's Enum value. */
        RexxAnnotationEnum annotation=null;


        /** The constructor parses the <code>script</code> argument for Rexx script
         *  annotations matching the <code>name</code> argument, starting with the
         *  <code>startPos</code> argument.
         *
         * @param script the Rexx script to parse
         * @param name the name of the Rexx script annotation, case does not matter
         * @param startPos the position in the script to start parsing
         */
        RexxAnnotation (String script, String name, int startPos)
        {
            final boolean bDebug=false; // true;

if (bDebug) System.err.println("---> arrived:\nRexxAnnotation(): startPos: "+startPos+", annotation name to seek: ["+name+"]");

            String ucName=name.toUpperCase();
            int scriptLength=-1;

            try // check whether we support that annotation
            {
                this.annotation=RexxAnnotationEnum.valueOf(ucName); // if not defined, an exception would be thrown
                scriptLength=script.length();   // get length of script (if null, an exception would be thrown)
            }
            catch (Exception e)
            {}


if (bDebug) System.err.println("RexxAnnotation(): ucName=["+ucName+"], this.annotation="+this.annotation);

            if (this.annotation==null || scriptLength==-1) return;  // unknown annotation or no script, do nothing

                // find annotation
            for (int i=startPos;i<scriptLength;i++)   // find the given Rexx annotation
            {
                    // get the next position of the at character that leads in a Rexx annotation
                this.posStartAtChar=script.indexOf('@',i);

if (bDebug) if (this.posStartAtChar>=0) System.err.println("RexxAnnotation(): FOUND! this.posStartAtChar="+this.posStartAtChar+", script.charAt(this.posStartAtChar="+this.posStartAtChar+")=["+script.charAt(this.posStartAtChar)+"]");

                if (this.posStartAtChar==-1) return;    // no (@) found, no further annotation present

                int begIdx=this.posStartAtChar+1;
                int endIdx=begIdx+ucName.length();

if (bDebug) System.err.println("RexxAnnotation(): begIdx="+begIdx+", endIdx="+endIdx+", script.substr()=["+script.substring(begIdx,endIdx)+"], ucName=["+ucName+"]");
                if (ucName.equalsIgnoreCase(script.substring(begIdx,endIdx)))  // found ?
                {
                    this.posStartAnnotationChar=begIdx;
                    this.posEndAnnotationChar  =endIdx-1;

if (bDebug) System.err.println("RexxAnnotation(): FOUND! final: this.posStartAnnotationChar="+this.posStartAnnotationChar
                  +", script.charAt("+posStartAnnotationChar+")=["+script.charAt(posStartAnnotationChar)+"]"
                  +", this.posEndAnnotationChar="+this.posEndAnnotationChar+", script.charAt("+posEndAnnotationChar+")=["
                  +script.charAt(posEndAnnotationChar)+"]");

                    break;
                }
                i=this.posStartAtChar;    // reposition (skip to current found '@')
            }

if (bDebug) System.err.println("RexxAnnotation(): this.posStartAnnotationChar="+this.posStartAnnotationChar+", this.posEndAnnotationChar="+this.posEndAnnotationChar);

                // find previous begin of block comment
            for (int i=posStartAnnotationChar-1; i>0; i--)
            {
                if (script.charAt(i)=='*' && script.charAt(i-1)=='/')
                {
                    posStartBlockComment=i-1;
                    break;
                }
            }

                // find following end of block comment
            for (int i=posEndAnnotationChar; i<(scriptLength-1); i++)
            {
                if (script.charAt(i)=='*' && script.charAt(i+1)=='/')
                {
                    posEndBlockComment=i;
                    break;
                }
            }

if (bDebug) System.err.println("RexxAnnotation(): block comments: "+ (posStartBlockComment>-1 ? "FOUND!" : "not found :-(" )
                   + " posStartBlockComment: script.charAt("+posStartBlockComment+")=["+script.charAt(posStartBlockComment)+"]"
                   + ", posEndBlockComment: script.charAt("+posEndBlockComment+")=["+script.charAt(posEndBlockComment)+"]");


                // find parentheses within block comment, if any
            if (posEndBlockComment>-1)      // an ending block comment found (otherwise it would be an error in the Rexx script)
            {
if (bDebug) System.err.println("RexxAnnotation(): seeking parentheses, posEndAnnotationChar+1="+(posEndAnnotationChar+1)+
                   "char=["+script.charAt(posEndAnnotationChar+1)+"]");
                for (int i=posEndAnnotationChar+1; i<(posEndBlockComment-1); i++)
                {
if (bDebug) System.err.println("... script.charAt(i="+i+")=["+script.charAt(i)+"], seeking: (");
                    if (script.charAt(i)=='(')
                    {
                        this.posOpenParenthesis=i;
                        // now try to find the closing parenthesis
                        for (int k=this.posOpenParenthesis+1; k<this.posEndBlockComment; k++)
                        {
if (bDebug) System.err.println("... script.charAt(k="+k+")=["+script.charAt(k)+"], seeking: )");
                            if (script.charAt(k)==')')
                            {
                                this.posCloseParenthesis=k;
                                break;
                            }
                        }
                        break;
                    }
                }
            }

if (bDebug) System.err.println("RexxAnnotation(): this.posOpenParenthesis="+this.posOpenParenthesis+", this.posCloseParenthesisr="+this.posCloseParenthesis+", scriptLength="+scriptLength);
            // if parenthesis, check whether content is quoted properly for Rexx (') or (") on either side, escaped quotes within
            if (this.posOpenParenthesis>=0 && this.posCloseParenthesis>=0)
            {
                // contentFirstChar, contentLastChar
                    // find first Rexx non-whitespace char
                for (int i=this.posOpenParenthesis+1; i<this.posCloseParenthesis; i++)
                {
                    char c= (char) script.charAt(i);    // get char
                    if (c==' ' || c=='\t' ) continue;   // skip ooRexx whitespace characters
                    this.contentFirstChar=c;         // save character found
                    this.contentFirstCharPos=i;      // save position
                    break;
                }
if (bDebug) System.err.println("RexxAnnotation(): contentFirstChar="+this.contentFirstChar+", contentFirstCharPos="+this.contentFirstCharPos);

                // find last Rexx non-whitespace char
                for (int i=this.posCloseParenthesis-1; i>this.posOpenParenthesis; i--)
                {
                    char c=(char) script.charAt(i);    // get char
                    if (c==' ' || c=='\t' ) continue;   // skip ooRexx whitespace characters
                    this.contentLastChar=c;         // save character found
                    this.contentLastCharPos=i;      // save position
                    break;
                }
if (bDebug) System.err.println("RexxAnnotation(): 1) contentLastChar="+this.contentLastChar+", contentLastCharPos="+this.contentLastCharPos);

                    // adjust if only one non-whitespace character between parenthesis
                if (this.contentLastCharPos==-1)     // only one non-ws character in between parenthesis?
                {
                    this.contentLastChar   =this.contentFirstChar;
                    this.contentLastCharPos=this.contentFirstCharPos;
                }

if (bDebug) System.err.println("RexxAnnotation(): 2) contentLastChar="+this.contentLastChar+", contentLastCharPos="+this.contentLastCharPos);
if (bDebug) System.err.println("RexxAnnotation(): contentFirstChar="+this.contentFirstChar+", contentLastChar="+this.contentLastChar);
if (bDebug) System.err.println("RexxAnnotation(): contentFirstCharPos="+this.contentFirstCharPos+", contentLastCharPos="+this.contentLastCharPos+", scriptLength="+scriptLength);

                    // determine whether content is quored and if so, is it a proper Rexx string?
                if ( (contentFirstChar=='"' && contentLastChar=='"') || (contentFirstChar=='\'' && contentLastChar=='\'') )
                {
                    // looks like a Rexx string
                    int lastCharToCheckPos=contentLastCharPos-1;
                    for (int i=contentFirstCharPos+1; i<lastCharToCheckPos; i++)
                    {
                        isContentProperlyQuoted=true;   // assume, proper Rexx string
                        char c=(char) script.charAt(i);
                        if ( c==contentFirstChar && (i==lastCharToCheckPos || (i<lastCharToCheckPos && script.charAt(i+1)!=contentFirstChar)) ) // not a proper Rexx string!
                        {
                            isContentProperlyQuoted=false;  // not a proper Rexx string
                            break;
                        }
                    }
                }

                    // if content, make it a proper Rexx string if necessary
                if (contentFirstCharPos>=0)
                {
                    contentAsIs         =null;   // non-whitespace content between parenthesis, if any
                    contentAsRexxString =null;   // non-whitespace content between parenthesis as a valid Rexx string
                    char quote='"';     // use double quote as delimiting quote
if (bDebug) System.err.println("RexxAnnotation(): script.substring("+contentFirstCharPos+","+(contentLastCharPos+1)+")");
                    contentAsIs        =script.substring(contentFirstCharPos, contentLastCharPos+1);
if (bDebug) System.err.println("RexxAnnotation(): contentAsIs        =["+contentAsIs+"]");

                    contentAsRexxString=contentAsIs;
                    if (isContentProperlyQuoted==false) // escape and enquote string
                    {
                        contentAsRexxString= '"' + contentAsIs.replace("\"", "\"\"") + '"';
                    }
if (bDebug) System.err.println("RexxAnnotation(): contentAsRexxString=["+contentAsRexxString+"]");
                }
            }

                // determine whether Rexx script annotation is on the same line as the opening block content
                // (we do not allow them to be on separate lines)
            isAnnotationWithinBlockCommentOnSameLine=true;
            for (int i=this.posStartBlockComment+2; (i<this.posStartAtChar-1) && isAnnotationWithinBlockCommentOnSameLine==true; i++)
            {
                isAnnotationWithinBlockCommentOnSameLine= (script.charAt(i)!='\n');
            }

                // check that no end block comment comes before our script annotation
            for (int i=this.posStartBlockComment+2; (i<this.posStartAtChar-1) && isAnnotationWithinBlockCommentOnSameLine==true; i++)
            {
                isAnnotationWithinBlockCommentOnSameLine= script.charAt(i)!='*' && script.charAt(i+1)!='/';
            }


if (bDebug) System.err.println("RexxAnnotion() - end of constructor! :)\n<---");
        }




        /** Create a string containing all attributes and their values.
         *
         * @return a String in which all attributes with their values are documented
         */
        public String toString()
        {
            StringBuilder sb=new StringBuilder();
            sb.append(this.getClass().getName()+"@"+this.hashCode());
            sb.append("[\n\tannotation=")             .append(this.annotation);
            sb.append(",\n\tposStartBlockComment=")   .append(this.posStartBlockComment);
            sb.append(",\n\tposStartAtChar=")         .append(this.posStartAtChar);
            sb.append(",\n\tposStartAnnotationChar=") .append(this.posStartAnnotationChar);
            sb.append(",\n\tposEndAnnotationChar=")   .append(this.posEndAnnotationChar);
            sb.append(",\n\tposOpenParenthesis=")     .append(this.posOpenParenthesis);
            sb.append(",\n\tcontentFirstCharPos=")    .append(this.contentFirstCharPos);
            sb.append(",\n\tcontentFirstChar=")       .append(this.contentFirstChar);
            sb.append(",\n\tcontentAsIs=")            .append(this.contentAsIs);
            sb.append(",\n\tcontentAsRexxString=")    .append(this.contentAsRexxString);
            sb.append(",\n\tcontentLastCharPos=")     .append(this.contentLastCharPos);
            sb.append(",\n\tcontentLastChar=")        .append(this.contentLastChar);
            sb.append(",\n\tposCloseParenthesis=")    .append(this.posCloseParenthesis);
            sb.append(",\n\tposEndBlockComment=")     .append(this.posEndBlockComment);
            sb.append(",\n\tisContentProperlyQuoted=").append(this.isContentProperlyQuoted);
            sb.append(",\n\tisAnnotationWithinBlockCommentOnSameLine=").append(this.isAnnotationWithinBlockCommentOnSameLine);
            sb.append(']');
            return sb.toString();
        }
    }

}
