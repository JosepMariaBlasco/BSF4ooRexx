package org.rexxla.bsf.engines.rexx;
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

import java.net.URL;         // rgf, 20171104: FXML-invocation has 'location' attribute of type URL

import org.apache.bsf.BSFManager;           // BSF support
import org.apache.bsf.BSFException;         // BSF support

import org.rexxla.bsf.engines.rexx.RexxEngine;
import org.rexxla.bsf.engines.rexx.RexxProxy;
import org.rexxla.bsf.engines.rexx.RexxException;

import org.rexxla.bsf.engines.rexx.RexxCleanupRef;

/*
------------------------ Apache Version 2.0 license -------------------------
   Copyright 2015-2022 Rony G. Flatscher

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

        2022-08-06, ---rgf, - changed package name to 'org.rexxla.bsf.engines.rexx'

        2022-08-01, ---rgf, - instead of using public getters, directly access Rexx engine
                              related static info, now that these fiels are made protected
                              in RexxScriptEngineFactory

        2022-07-14, ---rgf, - removed field bsfMgr and public routine getBsfManager(): not
                              needed in the context of this class

        2021-09-14, ---rgf, - bug fix in "public void setAddLatestPackageDynamically (boolean newValue)" (reported by Michael Hall)

        2021-07-05, ---rgf, - in order to make J2EE's JavaScriptTaglibs library Rexx agnostic:
                              added logic to cater for being used for a JSP (Java server page); if
                              'out' and ScriptContext.getWriter() are the same object, it is assumed
                              that the RexxScriptEngine gets employed for a JSP in which case no
                              prefix is injected, no automatic flushing carried out and the linend
                              is set to the sequence "0d0a"x to match HTML and XML linened conventions


        2020-09-22, ---rgf, - make sure that redirected streams get only replaced when
                              the ScriptContext's in, out or err are not yet redirected
                              to monitored .bsf.inputStream or .bsf.outputStream objects;
                              to reset the redirection use "call bsf.redirectTo 'Rexx'"

        2020-09-13, ---rgf, - let bsf.redirectTo do the wrapping

        2020-08-27, ---rgf, - make sure that compiled scripts do not get rewritten (no annotation
                              processing)
                            - if a compiled script is not encoded, then raise an appropraite ScriptException

        2020-08-25, ---rgf, - do not remove hashbang line as compiled+encoded Rexx programs rely on it

        2020-01-28, ---rgf, - when compiling and no filename is given, then try to use
                              the FILENAME value in the ENGINE_SCOPE Bindings, if any, otherwise
                              create an artificial filename that includes day and time of compiling;
                              note once compilation is done the filename cannot be changed anymore

        2020-01-27, ---rgf, - using createArgV(): only supply RexxScriptEngine instance if debugging

        2019-11-18, ---rgf, getReaderAsString(Reader r): read into a char[] rather than iterating
                            over single read characters

        2019-08-16, ---rgf, correct Javadocs

        2018-03-04, ---rgf, added method "removeHashBang(String script)" which blanks out the
                            first Rexx script line, if it starts with a Unix hashbang (#!)

        2017-11-04, ---rgf, if missing filename for eval(): check whether ScriptContext Bindings have
                            an entry "location" of type java.net.URL; if so, then assume that the
                            execution comes from a JavaFX FXML file so use that location file as part
                            of the artificially created filename to ease debugging

        2017-10-09, ---rgf, renamed "DispatchToToPublicRoutines" to "DispatchToPublicRoutines" (removing one "To")

        2017-10-08, ---rgf, make sure that the static method dumpScriptContext(sc) will supply
                            an appropriate header for non-standard scopes

        2017-09-22, ---rgf, make sure that in the constructor the RexxScriptEngine does not get
                            used to execute Rexx scripts, such that it is still possible to configure
                            the (to be created) Rexx interpreter instance

        2017-04-15, ---rgf, use .slot.argument class (created by BSF4ooRexx.cc as subclass of .directory
                            and stored in .environment ad load time of the library); this way
                             Rexx programmers become able to unambiguously identify whether the last
                             argument is a slot argument

        2017-04-03, ---rgf, correct parsing of Rexx script annotations

        2017-02-21, ---rgf, make sure that any NL character in a potential Rexx script annotation
                            (between "/ *" and "* /") causes the annotation to be ignored; this allows
                            for leaving them in the code, by merely inserting a NL character after
                            and/or before the block comment

        2017-02-12, ---rgf, use the Rexx annotation string as is: enclose in double-quotes after
                            escaping double-quotes (adhering to the Rexx rules)

        2016-12-21, ---rgf, if RexxScriptEngineFactory.LANGUAGE_VERSION is null in its constructor,
                            then the "parse version" string is queried from the installed ooRexx
                            interpreter and saved in that package private field, such that it gets
                            used for supplying it via the script context's language Bindings

        2016-12-07, ---rgf, adapted logic for the injection code in the case that the
                            Rexx script annotation does not contain a value (using explicitly empty string instead)

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
 *  {@link #getInterface(Class clasz)} method. Therefore the author (Rony
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
 * @version 103.20220806
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


    /** Caches the last compiled script. This is necessary in the case that {@link Invocable} methods
     *  get invoked (namely {@link #getInterface(Class clasz)})
     *  via the {@link ScriptEngine} as the {@link javax.script.Invocable} documentation (in
     *  the author's (Rony G. Flatscher) opinion, who also has served on the JSR-223 specification
     *  team for javax.script) erroneously states that {@link Invocable} should be implemented
     *  by a {@link ScriptEngine} rather than a {@link CompiledScript}.
     */
    RexxCompiledScript currentScript;

    /** Getter method for &quot;current&quot; (the latest evaluated) script. Due to the ooRexx architecture
     *  each script gets compiled (actually tokenized and its package set up) and will be stored in a field.
     *  Each script that gets evaluated will replace the {@link #currentScript} object.
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
     *  makes sure that the Java support is present. Only honored, if the instance field {@link #bAddLatestPackageDynamically}
     *  is set to <code>false</code>.
     */
    public static boolean bPrependDynamicRequiresStatement=true;


    /** This controls per RexxScriptEngine instance whether the Rexx package of a run script gets added
     *  to the Rexx package of the next compiled script. This makes all public routines and public
     *  classes of previous Rexx packages available for a new Rexx package.
     */
    boolean bAddLatestPackageDynamically=true;

    /** Allows to change the behaviour from adding the {@link #latestRexxPackage} to new Rexx routines.
     *  Default is <code>true</code>, which makes sure that any new Rexx routine will get the latest
     *  Rexx package added to it, thereby gaining access to the package <code>BSF.CLS</code> and all
     *  previously defined public routines and public classes.
     *
     *  @param newValue <code>true</code> or <code>false</code>
     */
    public void setAddLatestPackageDynamically (boolean newValue)
    {
        if (bAddLatestPackageDynamically!=newValue)
        {
            bAddLatestPackageDynamically=newValue;
        }
    }

    /** Returns the current setting of {@link #bAddLatestPackageDynamically}.
     *
     *  @return current setting of {@link #bAddLatestPackageDynamically}.
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
    // private Object getNil()
    {
        if (nil==null)
        {
            nil=bsfRexxEngine.getNil();
        }
        return nil;
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

    /** <code>RexxProxy</code> for the Rexx routine created by the Rexx code stored in {@link #rexxCreateRoutineCodeCode}. */
    private RexxProxy rexxCreateRoutine=null;

    /** Getter method to return {@link #rexxCreateRoutineCode}. If {@link #rexxCreateRoutineCode} is <code>null</code>
     *  the Rexx routine object will get created, stored and returned.
     *  @return the <code>RexxProxy</code> for the Rexx routine object
     *          a single entry)
     *
     *  @throws ScriptException if an exception occurs during routine creation
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
     *
     */
    private RexxProxy rexxMonitorWrapper=null;

    /** Getter method to return the ooRexx routine. If {@link #rexxMonitorWrapper} is <code>null</code>
     *  the Rexx routine object will get created, stored and returned.
     *  @return the <code>RexxProxy</code> for the Rexx routine object
     *
     *  @throws ScriptException if an exception occurs while wrapping up the monitor code
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
        "return .DispatchToPublicRoutines~new(rexxScript, rexxPackage, scriptContext)                                   \n" +
        "                                                                                                               \n" +
        "::requires BSF.CLS                              /* get Java camouflaging support                   */          \n" +
        "                                                                                                               \n" +
        "    /* dispatcher class for Invocable' <T> T getInterface(Class<T> clasz)                          */          \n" +
        "::class 'DispatchToPublicRoutines'                                                                             \n" +
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

    /** Getter method to return the ooRexx routine. If {@link #rexxRoutineWrapper} is <code>null</code>
     *  the Rexx routine object will get created, stored and returned.
     *  @return the <code>RexxProxy</code> for the Rexx routine object
     *
     *  @throws ScriptException if an exception occurs
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



    /** Rexx code that redirects the supplied standard files <code>stdin</code>, <code>stdout</code>
    *   and <code>stderr</code> to Java.
     * Its arguments are:
     * <ul>
     * <li> <code>in</code>: <code>stdin</code> gets redirected
     * <li> <code>out</code>: <code>stdout</code> gets redirected
     * <li> <code>err</code>: <code>stdout</code> gets redirected
     * </ul>
     *
     * The Rexx script returns a newly created Rexx directory object, with the supplied <code>entryName</code> and
     *         <code>entryValue</code>.
     *
     */

    String rexxRedirectStandardFilesCode_ori=   // redirect ooRexx standard streams to the ScriptContext supplied ones
           "use arg in, out, err                          \n" +
           "/* redirect .input, .output, .error       */  \n" +
           "call bsf.RedirectTo 'Java'                , - \n" +
           "     .bsf.inputStream~new(in)             , - \n" +
           "     .bsf.outputStream~new(out)           , - \n" +
           "     .bsf.outputStream~new(err)               \n" +
           "                                              \n" +
           "::requires BSF.CLS    /* get Java support */  \n" ;


    String rexxRedirectStandardFilesCode_debug = // _with_debug_statements =    // redirect ooRexx standard streams to the ScriptContext supplied ones
        "use arg in, out, err                                                            \n" +
        ".stdout~say('RSE.rex: // args ---> in='in', out='out', err='err)     \n" +
        "                                                                                \n" +
        "/* redirect .input, .output, .error       */                                    \n" +
        "   -- only replace, if not yet a .bsf.inputStream                               \n" +
        "currIn=.input~current                                                           \n" +
        "if \\.input~current~isA(.bsf.inputStream) then                                  \n" +
        "do                                                                              \n" +
        "   currIn=in                                                                    \n" +
        "   .error~say(.context~name': .error~say ... currIn ='pp(currIn))               \n" +
        "   .java.lang.system~err~println(.context~name': currIn ='pp(currIn))           \n" +
        "end                                                                             \n" +
        "call show 'in', in, .input, 'BEFORE redirect'                                   \n" +
        "                                                                                \n" +
        "   -- only replace, if not yet a .bsf.outputStream                              \n" +
        "currOut=.output~current                                                         \n" +
        "if \\.output~current~isA(.bsf.outputStream) then                                \n" +
        "do                                                                              \n" +
        "   currOut=out                                                                  \n" +
        "   .error~say(.context~name': .error~say ... currOut='pp(currOut))              \n" +
        "   .java.lang.system~err~println(.context~name': currOut='pp(currOut))          \n" +
        "end                                                                             \n" +
        "call show 'out', out, .output, 'BEFORE redirect'                                \n" +
        "                                                                                \n" +
        "   -- only replace, if not yet a .bsf.outputStream                              \n" +
        "currErr=.error ~current                                                         \n" +
        "if \\.error ~current~isA(.bsf.outputStream) then                                \n" +
        "do                                                                              \n" +
        "   currErr=err                                                                  \n" +
        "   .error~say(.context~name': .error~say ... currErr='pp(currErr))              \n" +
        "   .java.lang.system~err~println(.context~name': currErr='pp(currErr))          \n" +
        "end                                                                             \n" +
        "call show 'err', err, .error, 'BEFORE redirect'                                 \n" +
        "                                                                                \n" +
        "call bsf.RedirectTo 'Java', currIn, currOut, currErr                            \n" +
        "                                                                                \n" +
        "call show 'in', in, .input, 'after  redirect'                                   \n" +
        "call show 'out', out, .output, 'after  redirect'                                \n" +
        "call show 'err', err, .error, 'after  redirect'                                 \n" +
        "                                                                                \n" +
        "::requires BSF.CLS    /* get Java support */                                    \n" +
        "                                                                                \n" +
        " ::routine show                                                                 \n" +
        "    use arg title, stream, monitor, additional=''                               \n" +
        "                                                                                \n" +
        " s='Rexx-show():' title additional                                              \n" +
        " curr=monitor~current                                                           \n" +
        " s=s 'stream='pp(stream) 'monitor~current='pp(curr)                             \n" +
        " if (curr~isA(.bsf.inputStream)) |  curr~isA(.bsf.outputStream) then            \n" +
        " do                                                                             \n" +
        "   jo=curr~javaObject                                                           \n" +
        "   s=s '| javaobject='pp(jo) 'prefix='pp(curr~prefix)                           \n" +
        " end                                                                            \n" +
        "                                                                                \n" +
        " .stdout~say('rexxRedirectStandardFilesCode:' s)                                \n" +
        " .java.lang.system~err~println(s)                                               \n" +
        " .java.lang.system~out~println(s)                                               \n" ;


/* Note: this program will not replace monitored streams, if they are instances
         of .bsf.inputStream or .bsf.outputStream. This way the Rexx programmer
         can force his versions to remain in effect, rather than getting them
         replaced by this routine automatically. This may be helpful in situations
         where e.g. the prefixes are not needed (like in the J2EE taglib library
         where the "REXXout>" prefix must not be used and hence gets set to .nil).

         If in this case the Rexx programmer wishes to allow the default replacements
         to take effect again, then he needs to first issue a "call bsf.RedirectTo
         'Rexx'" (and then maybe followed by a "call bsf.RedirectTot 'Java' which
         then will fetch the System in, out, and err streams to be used for redirection.
*/
    String rexxRedirectStandardFilesCode =   // redirect ooRexx standard streams to the ScriptContext supplied ones
        "/* redirect .input, .output, .error       */                                    \n" +
        "use arg in, out, err                                                            \n" +
        "                                                                                \n" +
        "   -- only replace, if not yet a .bsf.inputStream                               \n" +
        "currIn=.input~current                                                           \n" +
        "                                                                                \n" +
        "if \\.input~current~isA(.bsf.inputStream) then                                  \n" +
        "   currIn=in                                                                    \n" +
        "                                                                                \n" +
        "   -- only replace, if not yet a .bsf.outputStream                              \n" +
        "currOut=.output~current                                                         \n" +
        "if \\.output~current~isA(.bsf.outputStream) then                                \n" +
        "   currOut=out                                                                  \n" +
        "                                                                                \n" +
        "   -- only replace, if not yet a .bsf.outputStream                              \n" +
        "currErr=.error ~current                                                         \n" +
        "if \\.error ~current~isA(.bsf.outputStream) then                                \n" +
        "   currErr=err                                                                  \n" +
        "                                                                                \n" +
        "call bsf.RedirectTo 'Java', currIn, currOut, currErr                            \n" +
        "                                                                                \n" +
        "::requires BSF.CLS    /* get Java support */                                    \n" ;


    /** <code>RexxProxy</code> for the Rexx routine created by the Rexx code stored in {@link #rexxRedirectStandardFilesCode}. */
    private RexxProxy rexxRedirectStandardFiles=null;

    /** Getter method to return {@link #rexxRedirectStandardFiles}. If {@link #rexxRedirectStandardFiles} is <code>null</code>
     *  the Rexx routine object will get created, stored and returned.
     *
     *  @return the <code>RexxProxy</code> for the Rexx routine object
     *
     *  @throws ScriptException if an exception occurs
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


    /** Rexx code to setup <code>.output</code> to be usable for creating HTML and XML text via a JspWriter. */
    String rexxRoutineSetupOut4JspCode =
        "use arg out                                                  \n" +
        "-- make sure no prefix gets inserted                         \n" +
        "outWrapped=.bsf.outputStream~new(out, .nil, '0d0a'x, .false) \n" +
        "prev=.output~current                                         \n" +
        " -- remove monitored object, if none left, add it back       \n" +
        "if .output~destination=.nil then .output~destination(prev)   \n" +
        ".output~destination(outWrapped)                              \n" +
        "                                                             \n" +
        "::requires BSF.CLS  /* get Java support */                   \n" ;


    /** <code>RexxProxy</code> for setting up <code>.output</code> for Jsp (<code>javax.servlet.jsp.JspWriter</code>),
     *  i.e., not prefixing anything and making sure that "0d0a"x gets appended as newline.
     */
    private RexxProxy rexxRoutineSetupOut4Jsp=null;

    /** Getter method to return the ooRexx routine. If {@link #rexxRoutineSetupOut4Jsp} is <code>null</code>
     *  the Rexx routine object will get created, stored and returned.
     *  @return the <code>RexxProxy</code> for the Rexx routine object
     *
     *  @throws ScriptException if an exception occurs
     */
    RexxProxy getRoutineSetupOut4Jsp()  throws ScriptException
    {
if (bDebug==true){ System.err.println("arrived: getRoutineSetupOut4Jsp(), rexxRoutineSetupOut4Jsp="+rexxRoutineSetupOut4Jsp);}
        if (rexxRoutineSetupOut4Jsp!=null)
        {
            return rexxRoutineSetupOut4Jsp;
        }

        try
        {
            if (bInitialBSFRequiresDone==false) updateRexxEngine(null);
            rexxRoutineSetupOut4Jsp=(RexxProxy) getCreateRoutine().sendMessage2("CALL", "RoutineSetupOut4Jsp", rexxRoutineSetupOut4JspCode);
        }
        catch (Exception exc)
        {
if (bDebug==true) {exc.printStackTrace(); }
            throw new ScriptException(exc);     // re-throw, wrap cause in ScriptException
        }
        return rexxRoutineSetupOut4Jsp;
    }




    /** Rexx code to create and return a subclass of the Rexx directory class, which carries the name
     *  &quot;Slot.Argument&quot;, optionally allowing for one entry-name and entry-value.
     *
     * <ul>
     * <li> <code>entryName</code>: optional, a String denoting the entry name (will be uppercased by Rexx, if necessary)
     * <li> <code>entryValue</code>: optional, any object to be stored with <code>entryName</code>
     * </ul>
     *
     * The Rexx script returns a newly created Rexx directory object, with the supplied <code>entryName</code> and
     *         <code>entryValue</code>.
     */
    String rexxCreateSlotArgumentCode=
           "use strict arg entryName=.nil, entryValue=.nil                  \n" +
           "dir=.slot.argument~new                                          \n" +
           "if entryName<>.nil then                                         \n" +
           "   dir~setEntry(entryName,entryValue)                           \n" +
           "return dir          /* return Rexx directory object */          \n" +
           "                                                                \n" +
           "::requires 'BSF.CLS'    /* add Java support         */          \n" ;

    /** <code>RexxProxy</code> for the Rexx routine created by the Rexx code stored in {@link #rexxCreateSlotArgumentCode}. */
    private RexxProxy rexxCreateSlotArgument=null;

    /** Getter method to return {@link #rexxCreateSlotArgument}. If {@link #rexxCreateSlotArgument} is <code>null</code>
     *  the Rexx routine object will get created, stored and returned.
     *
     *  @return the <code>RexxProxy</code> for the Rexx routine to create a Rexx directory (and optionally supply
     *          a single entry)
     *
     *  @throws ScriptException if an exception occurs
     */
    RexxProxy getCreateSlotArgument()  throws ScriptException
    {
if (bDebug==true){ System.err.println("arrived: getCreateSlotArgument(), rexxCreateSlotArgument="+rexxCreateSlotArgument);}
        if (rexxCreateSlotArgument!=null)
        {
            return rexxCreateSlotArgument;
        }

        String filename="RexxScriptEngine_rexxCreateSlotArgumentCode.rex";
        try
        {
            if (bInitialBSFRequiresDone==false) updateRexxEngine(null);
            rexxCreateSlotArgument=(RexxProxy) getCreateRoutine().sendMessage2("CALL", "createSlotArgument", rexxCreateSlotArgumentCode);
        }
        catch (Exception exc)
        {
if (bDebug==true) {exc.printStackTrace();}
            throw new ScriptException(exc);     // re-throw, wrap cause in script
        }
        return rexxCreateSlotArgument;
    }



    /** Rexx code to create and return a Rexx RoutineAndPackage, optionally allowing for one entry-name and entry-value.
     *
     * The Rexx script's arguments are:
     * <ul>
     * <li> <code>name</code>: the name of the routine
     * <li> <code>script</code>: the code (a String) of the routine (a Rexx program)
     * </ul>
     * It will return a newly created Rexx RoutineAndPackage object, with the supplied <code>entryName</code> and
     *         <code>entryValue</code>
     */
    String rexxCreateRoutineAndPackageCode=
           "parse arg name, script                      /* fetch script code            */    ; " +
           "                                                                                  ; " +
           "if script~isA(.array) then                  /* argument already an array    */    ; " +
           "   r=.routine~new(name, script)             /* create routine object        */    ; " +
           "else                                                                              ; " +
           "   r=.routine~new(name, script~makeArray)   /* create routine object        */    ; " +
           "                                                                                  ; " +
           "p=r~package                                 /* get its package              */    ; " +
           "   /* return Rexx objects in a Java array to ease access in the Java code   */    ; " +
           "return bsf.createJavaArrayOf('java.lang.Object', r, p)                            ; " +
           "                                                                                  ; " +
           "::requires BSF.CLS                                                                ; " ;

    String rexxCreateRoutineAndPackageCode_work1=
           "parse arg name, script                      /* fetch script code            */    ; " +
           "                                                                                  ; " +
           "say 'script~class='pp(script~class)                                               ; " +
           "if script~isA(.array) then                  /* argument already an array    */    ; " +
           "do                                                                                ; " +
           "   r=.routine~new(name, script)             /* create routine object        */    ; " +
           "end                                                                               ; " +
           "else                                                                              ; " +
           "do                                                                                ; " +
           "  say '... string!, script~left(40)    ='pp(script~left(40))                            ; " +
           "  say '             script~left(40)~c2x='pp(script~left(40)~c2x)                        ; " +
           "  strarr=script~makeArray                                                         ; " +
        "  str1=strarr~makeString('L','0a'x);strArr2=str1~makeArray;str2=strArr2~makeString('L','0a'x)         ; " +
        "  say '...script=str1?' pp(script=str1) '==' pp(script==str1) 'script~length='pp(script~length) 'str1~length='pp(str1~length)     ; " +
        "  say '...str1  =str2?' pp(str1  =str2) '==' pp(str1  ==str2) 'str1  ~length='pp(str1  ~length) 'str2~length='pp(str2~length)     ; " +
        "    .stream~new('scri.eencoded.rex')~~open('write replace')~~charout(script)~~close  ; " +
        "    .stream~new('str1.eencoded.rex')~~open('write replace')~~charout(str1)~~close  ; " +
        "    .stream~new('str2.eencoded.rex')~~open('write replace')~~charout(str2)~~close  ; " +
        "                                                                                  ; " +
        "                                                                                  ; " +
           "  say '    strArr[1]    ='pp(strarr[1])                                           ; " +
           "  say '    strArr[1]~c2x='pp(strarr[1]~c2x)                                       ; " +
           "  say '    strArr[2]    ='pp(strarr[2])                                           ; " +
           "  say '    strArr[2]~c2x='pp(strarr[2]~c2x)                                       ; " +
           "  say '    strArr[3]    ='pp(strarr[3])                                           ; " +
           "  say '    strArr[3]~c2x='pp(strarr[3]~c2x)                                       ; " +
           "  say '    strArr~items ='pp(strarr~items) '| script~length='pp(script~length)    ; " +
           "   r=.routine~new(name, strArr          )   /* create routine object        */    ; " +
           " /*  r=.routine~new(name, script~makeArray) */   /* create routine object        */    ; " +
           "   say '<--- routine successfully created!'                                        ; " +
        "  say                                                                             ; " +
           "end                                                                               ; " +
           "                                                                                  ; " +
           "p=r~package                                 /* get its package              */    ; " +
           "   /* return Rexx objects in a Java array to ease access in the Java code   */    ; " +
           "return bsf.createJavaArrayOf('java.lang.Object', r, p)                            ; " +
           "                                                                                  ; " +
           "::requires BSF.CLS                                                                ; " ;

    String rexxCreateRoutineAndPackageCode_nixi=
           "parse arg name, script                      /* fetch script code            */    ; " +
           "                                                                                  ; " +
           "say 'script~class='pp(script~class)                                               ; " +
           "if script~isA(.array) then                  /* argument already an array    */    ; " +
           "do                                                                                ; " +
           "   r=.routine~new(name, script)             /* create routine object        */    ; " +
           "end                                                                               ; " +
           "else                                                                              ; " +
           "do                                                                                ; " +
           "  say '... string!, script~left(40)    ='pp(script~left(40))                      ; " +
           "  say '             script~left(40)~c2x='pp(script~left(40)~c2x)                  ; " +
           "  say '             script~length      ='pp(script~length)                        ; " +
           "  say '             script~countStr(0A)='pp(script~countStr('0A'))                ; " +
           "  say '             script~countStr(''0A''x)='pp(script~countStr('0A'x))          ; " +
           "  strArr=script~makeArray                                                         ; " +
           "  say '             strArr~items='pp(strarr~items)                                ; " +
           "   r=.routine~new(name, strArr          )   /* create routine object        */    ; " +
           " /*  r=.routine~new(name, script~makeArray) */   /* create routine object        */    ; " +
           "   say '<--- routine successfully created!'                                        ; " +
           "  say                                                                             ; " +
           "end                                                                               ; " +
           "                                                                                  ; " +
           "p=r~package                                 /* get its package              */    ; " +
           "   /* return Rexx objects in a Java array to ease access in the Java code   */    ; " +
           "return bsf.createJavaArrayOf('java.lang.Object', r, p)                            ; " +
           "                                                                                  ; " +
           "::requires BSF.CLS                                                                ; " ;

    /** <code>RexxProxy</code> for the Rexx routine created by the Rexx code stored in {@link #rexxCreateRoutineAndPackageCode}. */
    private RexxProxy rexxCreateRoutineAndPackage=null;

    /** Getter method to return {@link #rexxCreateRoutineAndPackage}. If {@link #rexxCreateRoutineAndPackage} is <code>null</code>
     *  the Rexx routine object will get created, stored and returned.
     *  @return the <code>RexxProxy</code> for the Rexx routine to create a Rexx RoutineAndPackage (and optionally supply
     *          a single entry)
     *
     *  @throws ScriptException if an exception occurs while creating the routine and its package
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

if (bDebug==true)
{ System.out.println("--> RexxScriptEngine() -->: "+this+" (CONSTRUCTOR!)"); }

        BSFManager bsfMgr = new BSFManager ();                // get an instance of BSFManager
        try
        {
           bsfRexxEngine = (RexxEngine) bsfMgr.loadScriptingEngine("rexx");  // load the Rexx engine

           new RexxCleanupRef(this, RexxCleanupRef.RefKind.REXX_SCRIPT_ENGINE, bsfRexxEngine);

            // get the default ScriptContext, fill-in engine related information using the official names
           ScriptContext sc=getContext(); // get default ScriptContext
           RexxScriptEngineFactory ref=RexxScriptEngineFactory.DEFAULT_REXX_SCRIPT_ENGINE_FACTORY;
           sc.setAttribute(ENGINE          , ref.ENGINE_NAME        , ScriptContext.ENGINE_SCOPE);
           sc.setAttribute(ENGINE_VERSION  , ref.ENGINE_VERSION     , ScriptContext.ENGINE_SCOPE);
           sc.setAttribute(LANGUAGE        , ref.LANGUAGE_NAME      , ScriptContext.ENGINE_SCOPE);
           sc.setAttribute(LANGUAGE_VERSION,
                           ref.LANGUAGE_VERSION==null ? ref.getLanguageVersion()
                                                      : ref.LANGUAGE_VERSION
                                                                    , ScriptContext.ENGINE_SCOPE);
           sc.setAttribute(NAME            , ref.SHORT_NAME         , ScriptContext.ENGINE_SCOPE);
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



    /** Creates the script from the supplied <code>Reader</code> and uses {@link #eval (String script, ScriptContext sc)}
     *  to carry out the operation.
     *
     *  @param r - A <code>Reader</code> supplying the Rexx code to evaluate.
     *  @param sc - A <code>ScriptContext</code> to use for evaluating the script
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
     *  {@link RexxCompiledScript#eval(ScriptContext sc)} to carry out the operation.
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
                // rgf, 2017-11-04: maybe executed because defined in a FXML file (set up and executed by FXMLLoader),
                //                  if so "location" is a URL to the FXML file, use its name to indicate where the
                //                  invocation of the Rexx code comes from
                Object location=sc.getAttribute("location");
                if (location instanceof URL)
                {
                    filename="rexx_invoked_via_["+((URL) location).getFile()+"]_at_"+getGMTFormattedDate4Filename()+".rex";
                }
                else    // create a debuggable filename that includes this RexxScriptEngine instance and date/time information
                {
                    filename="filename_created_by_"+this.toString().replace('.','_').replace('@','-')+"_at_"+getGMTFormattedDate4Filename()+".rex";
                }
            }

            CompiledScript rcs=compile(script, filename);   // create and get the compiled (tokenized) form
            return rcs.eval(sc);        // now evaluate the code with the given ScriptContext
        }
    }


// Compilable methods ------------------------------------------------------------------------
    /** Compiles (tokenizes) the script (source read from Reader) for later execution.
     *  The actual compilation (tokenization) will be carried out by {@link #compile(String script)}.
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
     *  {@link #compile(String script, String filename)}.
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
     * <p>Hint: this filename can be fetched from Rexx using the <code>PARSE SOURCE</code> keyword instruction.
     *
     *  @return a CompiledScript that can be repeatedly evaluate()'d or used to invoke functions
     *          of the script or methods in an object
     *
     *  @throws ScriptException if an exception occurs
    */
    CompiledScript compile(final String script, String filename) throws ScriptException
    {
if (bDebug) {System.err.println("--> RexxScriptEngine().compile(String=[---script---], String=["+filename+"])...");}

        if (script==null)
        {
            throw new ScriptException( new NullPointerException("Received \"String script\" argument null"));
        }

        // String     tmpScriptSource=removeHashBang(script);  // the script that will get compiled (tokenized), maybe edited (code injected)
        String     tmpScriptSource=script;  // 2020-08-25: do not remove hashbang as compiled+encoded programs rely on it!
        boolean    bGotScriptEdited=false;  // true, if Rexx script got edited via injections

        // --> determine whether compiled script in hand, which must not be edited!
        boolean    isScriptCompiled=false;  // only if false, can the script be annotation processed
        if (script.startsWith("#!"))        // hashbang must be present
        {
            int len=script.length();
            int line2=script.indexOf(0x0a)+1;   // get position of character on second line
            if (line2>0 && line2<len)           // second line with characters on it
            {
                int needlePos=script.indexOf("/**/@REXX", line2);   // compiled="/**/@REXX", compiled+encoded: "/**/@REXX@"
                isScriptCompiled=(needlePos == line2);    // if second line starts with needle, it is a compiled Rexx script!
                    // if compiled, is it encoded, if not throw ScriptException
                if (isScriptCompiled && needlePos+10<len && script.charAt(needlePos+9)!='@')
                {
                    throw new ScriptException("Rexx script ["+filename+"] is compiled, but not encoded");
                }
            }
        }

        boolean bShowSource=false;
        if (isScriptCompiled==false)        // only process if source available
        {
            RexxAnnotation ran=new RexxAnnotation(script, "showsource", 0); // try to get the Rexx script annotation "/* @showsource */"
                // determine, whether this Rexx script property is present
            bShowSource=(ran.posStartAnnotationChar>-1 && ran.isAnnotationWithinBlockCommentOnSameLine);
            if (bShowSource)    // show (for debugging purposes the supplied Rexx script)
            {
                System.err.println(this+": show source of Rexx script, annotation \"/*@showsource*/\" present in file [" +filename+"]:"
                                    +"\n/// ----------------------------------------> begin of Rexx script ----------------------------------------> ///\n"
                                    +script
                                    +"\n\\\\\\ <----------------------------------------- end of Rexx script <----------------------------------------- \\\\\\");
            }

            // - process @GET, @GET() and @SET() Rexx script annotations; if @SET() is empty, give a warning and ignore,
            //   save the edited script
            // - if Rexx script annotation SHOWSOURCE is given, show the edited script as well
            String rexxAnnos[] =new String[] { "GET", "SET" };  // Rexx script annotations to be processed

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
    if (bDebug) System.err.println("--- 0 --- found a [@"+rxAnno+"], begin BComment: ["+ran.posStartBlockComment+"], '@'-pos: ["+ran.posStartAnnotationChar+"], ran.isAnnotationWithinBlockCommentOnSameLine=["+ran.isAnnotationWithinBlockCommentOnSameLine+"]...");
                        // allow one annotation per block comment only
                    if (sbEditedScript==null)       // not yet created?
                    {
                        sbEditedScript=new StringBuilder();
                    }

                        // append script code before injection point
                    prevPosEnd=ran.posStartBlockComment;    // everything up to, but not including open block comment

                    if (prevPosEnd>prevPosStart)    // is there anything to be carried over from the original script ?
                    {
    if (bDebug) System.err.println("--- 1 --- adding script code before annotation, prevPosStart="+prevPosStart+", prevPosEnd="+prevPosEnd+"\n--->>>["+tmpScriptSource.substring(prevPosStart, prevPosEnd)+"]\n<<<---");

                        sbEditedScript.append( tmpScriptSource.substring(prevPosStart, prevPosEnd));
                        prevPosStart=prevPosEnd;
                    }

    if (bDebug) System.err.println("--- 2 --- RSE.compile(): now prevPosStart="+prevPosStart+", prevPosEnd="+prevPosEnd);

                        // inject code
                    if (ran.contentAsRexxString==null)  // no content, then use empty string ("") instead to mean all atributes
                    {
                        sbEditedScript.append( String.format( (rxAnno.startsWith("G") ? strFormatGET  : strFormatSET), "\"\"") );
                    }
                    else    // submit the Rexx string
                    {
                        // submit annotation content as is, escape and enquote according to Rexx rules
                        String tmpString='"'+ran.contentAsIs.replace("\"", "\"\"")+'"';   // escape any quotes

                        sbEditedScript.append( String.format( (rxAnno.startsWith("G") ? strFormatGET  : strFormatSET), tmpString) );
                    }

                        // append entire Rexx script annotation (if multiple annotations of the same kind, the first one gets honored)
                    prevPosEnd=ran.posEndBlockComment+2;

    if (bDebug) System.err.println("--- 3 --- RSE.compile(): after appending code for ["+rxAnno+"] annotation, new prevPosEnd: ["+prevPosEnd+"]");
                    // extract script section and append, if anything to append
                    if (prevPosStart<prevPosEnd)
                    {
    if (bDebug) System.err.println("--- 4 --- RSE.compile(): add original script section - prevPosStart="+prevPosStart+", prevPosEnd="+prevPosEnd+", hence: ["+tmpScriptSource.substring(prevPosStart, prevPosEnd)+"]");
                        sbEditedScript.append( tmpScriptSource.substring(prevPosStart, prevPosEnd));
                        // set currPos and prevPosStart
                        prevPosStart=prevPosEnd;
    if (bDebug) System.err.println("--- 4b --- RSE.compile(): now currPos=prevPosStart=prevPosEnd: ["+prevPosEnd+"]");
                    }
                    currPos=prevPosEnd;     // set currPos after current Rexx script annotation's end

    if (bDebug) System.err.println("--- 5 --- RSE.compile(): processing of this annotation ended, next round: currPos="+currPos+", prevPosStart="+prevPosStart+", prevPosEnd="+prevPosEnd);
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
    if (bDebug) System.err.println("--- 6 --- RSE.compile(): appending remaining script - prevPosEnd="+prevPosEnd+", tssLength="+tssLength+", appended from String:"+"\n--->>>["+tmpScriptSource.substring(prevPosEnd, tssLength)+"]\n<<<---");
                    }
                    tmpScriptSource=sbEditedScript.toString();  // create a String off it
                    sbEditedScript=null;                        // reset StringBuilder variable to null

    if (bDebug) System.err.println("--- 7 --- RSE.compile(): finished editing for ["+rxAnno+"] annotations, sbEditedScript.toString() --> ---> ===>["+tmpScriptSource+"] <=== <--- <-- sbEditedScript!=NULL, hence bGotScriptEdited->[TRUE]!");
                }
            }


              // only necessary, if BSF-support is needed and no dynamic package additions take place (which would include the package BSF.CLS)
            if (bAddLatestPackageDynamically==false && bPrependDynamicRequiresStatement==true)     // inject dynamic requires for BSF.CLS
            {
    if (bDebug) System.err.println("--- 8 --- RSE.compile(): bAddLatestPackageDynamically==false && bPrependDynamicRequiresStatement==true, bGotScriptEdited -> [TRUE] !");
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
        }


        if (filename==null)     // no file name supplied
        {
            // try to fetch the name from the ENGINE_SCOPE Bindings
            ScriptContext sc=this.getContext();
            filename=(String) sc.getAttribute(this.FILENAME, sc.ENGINE_SCOPE);

            if (filename==null)     // create some artificial file name, indicating time of compiliation
            {
                filename="compiled_script_"+getGMTFormattedDate4Filename()+".rex";
            }
        }

        Object[] rexxObjects;
        try {
            // make sure JNI is using BSF.CLS to fetch Java object arguments as Rexx proxies in Rexx programs
            if (bInitialBSFRequiresDone==false) updateRexxEngine(null);

            // now create the routine object, fetch it and its package object
            rexxObjects= (Object[]) getCreateRoutineAndPackage().sendMessage2("CALL", filename,tmpScriptSource);

// rgf, 2022-04-20 --->
if (bDebug==true) {
    java.io.File file = new java.io.File(filename);
    java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
    java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(fos);
    try {
        //convert string to byte array
        byte[] bytes = tmpScriptSource.getBytes();
        //write byte array to file
        bos.write(bytes);
        System.out.println("\n*** Data written to file ["+filename+"] successfully. ***");
    } catch (java.io.IOException e) {
        System.err.println("\n*** Error writing to file ["+filename+"] ***.");
        e.printStackTrace();
    }
    finally {
        bos.close();
        fos.close();
    }
}
// rgf, 2022-04-20 <---

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
if (bDebug==true)   // rgf, 2022-04-20
{
    int length=tmpScriptSource.length();
    if (length>100)
    {
        length=100;
    }
    if (length>0)
    {
       System.out.println("*** RSE.compile(script,filename): ADDPACKAGE(...) for \n\t'file': "+filename+"\n\t'tmpScriptSource': ["+tmpScriptSource.substring(0,length)+"] ... ***");
    }
    System.out.println("*** RSE.compile(script,filename): ADDPACKAGE("+latestRexxPackage+") *** bsfRexxEngine="+bsfRexxEngine+" *** rii_ID="+bsfRexxEngine.get_rii_ID()+" ***");
}
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
                System.err.println(this+": show source of EDITED Rexx script, annotation \"/*@showsource*/\" present in [" +filename+"]:"
                                    +"\n/// ========================================> begin of EDITED Rexx script ========================================> ///\n"
                                    +tmpScriptSource
                                    +"\n\\\\\\ <========================================= end of EDITED Rexx script <========================================= \\\\\\");
            }
        }

        return currentScript;            // save in cache, from now on all Invocable methods can be carried out via this ScriptEngine as well
    }


// -----------------------------------------------------------------------------------------------------
// Invocable methods
    /** Returns an implementation of an interface using functions compiled in the interpreter.
     *
     *  <p>Note: Uses the implementation in {@link RexxCompiledScript#getInterface(Class clasz)}.
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
     *           same order supplied to, plus an appended <code>RexxSlotArgument</code> "slot directory"
     *           object, which contains information about this particular method invocation. This slot directory
     *           will also contain an entry, named <code>&quot;SCRIPTCONTEXT&quot;</code>, which allows the
     *           Rexx routine to fully interact with the <code>ScriptContext</code> used for its invocation.
     *           <br>(Hint: one can always access this object from Rexx code with the statement
     *           <code>scriptcontext=arg('A')~lastItem~scriptContext</code>).)
     *
     *  @param thiz - The Java object (must be a <code>RexxProxy</code>) that implements the Java interface methods
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
     *         {@link #invokeMethod(Object thiz, String name, Object... args)} method.
     *
     * @throws ScriptException depending on what went wrong in invocation
     * @throws NoSuchMethodException if method does not exist
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
     *  @param thiz - The wrapped (as a <code>RexxProxy</code>) Rexx object to receive the message
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
     *         {@link #invokeMethod(Object thiz, String name, Object... args)} method.
     *
     * @throws ScriptException depending on what went wrong in invocation
     * @throws NoSuchMethodException if method does not exist
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
            if (bDebug) // supply this instance in Slot.Argument to Rexx for debugging purposes only
            {
                arrArgV=RexxCompiledScript.createArgV(this, args, sc, this, null);  // supply default context; no access to RexxCompiledScript
            }
            else       // GA version: do not supply RexxScriptEngine instance to Rexx script
            {
                arrArgV=RexxCompiledScript.createArgV(this, args, sc, null, null);  // supply default context; no access to RexxCompiledScript
            }
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
     *
     * @param sc the ScriptContext object for which the Bindings of all scopes get dumped
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
                System.err.println(" --> Scope with number ["+scope+"] -->");
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
     *
     * @throws ScriptException if {@link IOException} occurs with the supplied Reader object
     */
    static String getReaderAsString(Reader reader) throws ScriptException
    {
        if (reader==null)        // no Reader supplied
        {
            return null;
        }

        if (! (reader instanceof BufferedReader) )   // make sure we use a BufferedReader
        {
            reader=new BufferedReader(reader);
        }

        StringBuffer sb=new StringBuffer();
        try
        {
            char[] charBuffer = new char[4096];
            int r = 0;

            while ((r = reader.read(charBuffer)) != -1)
            {
                sb.append(charBuffer, 0, r);
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
     *
     * @throws ScriptException depending on what went wrong in invocation
     * @throws BSFException if something went wrong while interacting with ooRexx
     */

//          check whether current re-assignments are in place already or not
    synchronized void updateRexxEngine(ScriptContext sc) throws ScriptException, BSFException
    {
        // boolean bDebug=false; // true;
if (bDebug)
{
    System.err.println(this+": --> RexxScriptEngine().updateRexxEngine(), sc=["+sc+"] ...");
    System.out.println("*** RSE.updateRexxEngine(): rse/this="+this+" / "+"bsfRexxEngine="+bsfRexxEngine+" *** "+bsfRexxEngine.get_rii_ID()+" ***");
}
            // make sure we have the native code set to support the camouflaging support from the beginning
        if (bInitialBSFRequiresDone==false) // make sure that JNI subsystem uses BSF.CLS for passing (Java object) arguments to Rexx scripts
        {
if (bDebug)
{
    System.out.println("*** RSE.updateRexxEngine(): bInitialBSFRequiresDone gets carried out ...");
    System.err.println(this+": --> RexxScriptEngine().updateRexxEngine(), bInitialBSFRequiresDone==false !");
}
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
if (bDebug) {System.err.println(this+":     RexxScriptEngine(), getting and setting reader and writers ...");}

                // determine whether we need to change this Rexx engines .input, .output and/or .error monitors
            Reader tmpReader     =(Reader) sc.getReader();
            Writer tmpWriter     =(Writer) sc.getWriter();
            Writer tmpErrorWriter=(Writer) sc.getErrorWriter();

if (bDebug==true) System.err.println(this+":     REXX -> RexxScriptEngine.update(...)");

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

if (bDebug==true) System.err.println("[RexxScriptEngine: after getRedirectStandardFiles(), tmpWriter: "+tmpWriter+"]\n");

                    // assumption: if 'out' the same as getWriter() we run for a JSP, so remove any prefix from .output
                    // (alternatively, check whether writer is a "javax.servlet.jsp.JspWriter" using Class.getName() and .getSuperclass())

if (bDebug==true){
    System.err.println("[RexxScriptEngine: before testing for"
     +" 'out'==tmp'Writer: \""+(sc.getAttribute("out",sc.ENGINE_SCOPE)==tmpWriter)
     +" 'out': \""+(sc.getAttribute("out",sc.ENGINE_SCOPE))
     +"\"]\n");
}
                    if (sc.getAttribute("out",sc.ENGINE_SCOPE)==tmpWriter) // 'out' present and same object?
                    {
                        getRoutineSetupOut4Jsp().sendMessage1("CALL",tmpWriter);    // adjust .output

if (bDebug==true) System.err.println("[RexxScriptEngine: after getRoutineSetupOut4Jsp()]\n");

                    }
                }
                catch (BSFException exc)
                {
if (bDebug==true) {exc.printStackTrace();}
                    throw new ScriptException(exc);
                }
            }
        }
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

if (bDebug) if (this.posStartAtChar>=0) System.err.println("RexxAnnotation(): '@' FOUND! this.posStartAtChar="+this.posStartAtChar+", script.charAt(this.posStartAtChar="+this.posStartAtChar+")=["+script.charAt(this.posStartAtChar)+"]");

                if (this.posStartAtChar==-1) return;    // no (@) found, no further annotation present

                int begIdx=this.posStartAtChar+1;

// 2017-04-02, rgf: check whether we have --->
                int k=0, m=0,                       // do not interfere with "i"
                    lengthUCName=ucName.length(),
                    endIdx=begIdx+lengthUCName
                    ;
                // determine whether we have enough characters left to test the needle against a substring
                for (k=begIdx; k<scriptLength && m<lengthUCName; k++, m++ )
                {}

// 2017-04-02, rgf: check whether we have <---
                // int endIdx=begIdx+ucName.length();


if (bDebug) System.err.println("RexxAnnotation(): begIdx="+begIdx+", endIdx="+endIdx+", script.substr()=["+script.substring(begIdx,endIdx)+"], ucName=["+ucName+"]");
                if (lengthUCName==m && ucName.equalsIgnoreCase(script.substring(begIdx,endIdx)))  // found ?
                {
                    this.posStartAnnotationChar=begIdx;
                    this.posEndAnnotationChar  =endIdx-1;

if (bDebug) System.err.println("RexxAnnotation(): FOUND! final: this.posStartAnnotationChar="+this.posStartAnnotationChar
                  +", script.charAt("+posStartAnnotationChar+")=["+script.charAt(posStartAnnotationChar)+"]"
                  +", this.posEndAnnotationChar="+this.posEndAnnotationChar+", script.charAt("+posEndAnnotationChar+")=["
                  +script.charAt(posEndAnnotationChar)+"]");

if (bDebug) System.err.println("===> RexxAnnotation(): before calling parseAnnotation(), this=["+this+"]");
                    parseAnnotation(script, name, scriptLength);
if (bDebug) System.err.println("<=== RexxAnnotation(): AFTER  calling parseAnnotation(), this=["+this+"]");

                    if (isAnnotationWithinBlockCommentOnSameLine==true) // we found one, return!
                    {
                        break;
                    }

                }
                i=this.posStartAtChar;    // reposition (skip to current found '@')
            }
        }


        /** Determine whether we have a RexxScriptAnnotation (@showSource, @get(...), @set(...)),
         *  and if so fill-in the correct values in our fields.
         *
         * @param script the Rexx script to parse
         * @param name the name of the Rexx script annotation, case does not matter
         * @param scriptLength the total length of the script to parse
         *
        */
        // boolean isRexxScriptAnnotation(String script, String name, int startPos, int scriptLength)
        void parseAnnotation(final String script, final String name, final int scriptLength)
        {
// boolean bDebug=true; // set temporaryly locally to true
if (bDebug) System.err.println("RexxAnnotation().parseAnnotation(): needle/name=["+name+"],  this.posStartAnnotationChar="+this.posStartAnnotationChar+", this.posEndAnnotationChar="+this.posEndAnnotationChar);

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

if (bDebug)
{
    System.err.println("RexxAnnotation(): 2) contentLastChar="+this.contentLastChar+", contentLastCharPos="+this.contentLastCharPos);
    System.err.println("RexxAnnotation(): contentFirstChar="+this.contentFirstChar+", contentLastChar="+this.contentLastChar);
    System.err.println("RexxAnnotation(): contentFirstCharPos="+this.contentFirstCharPos+", contentLastCharPos="+this.contentLastCharPos+", scriptLength="+scriptLength);
}

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

            for (int i=this.posStartBlockComment+2; i<this.posStartAtChar && isAnnotationWithinBlockCommentOnSameLine==true; i++)
            {
                char ch=script.charAt(i);
                isAnnotationWithinBlockCommentOnSameLine= ch!='\n' &&
                                                          ch!='*' && script.charAt(i+1)!='/';
            }

                // check that no newline comes before block end comment
            for (int i=this.posEndAnnotationChar; (i<this.posEndBlockComment) && isAnnotationWithinBlockCommentOnSameLine==true; i++)
            {
                // if a NL char, then we ignore the annotation
                char ch=script.charAt(i);
                isAnnotationWithinBlockCommentOnSameLine= ch!='\n' &&
                                                          ch!='*' && script.charAt(i+1)!='/';
            }

if (bDebug)
{
    System.err.println("RexxAnnotation().toString():\n["+this.toString()+"]\n");
    System.err.println("RexxAnnotation() - end of constructor! :)\n<---");
}

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
