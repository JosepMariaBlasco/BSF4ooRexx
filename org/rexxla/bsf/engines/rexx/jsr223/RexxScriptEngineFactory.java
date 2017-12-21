package org.rexxla.bsf.engines.rexx.jsr223;
import javax.script.*;

import java.util.Arrays;
import java.util.List;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;


import org.apache.bsf.*;   // BSF support

/*
------------------------ Apache Version 2.0 license -------------------------
   Copyright 2015-2017 Rony G. Flatscher

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
        - 2015-05-19, ---rgf: added a getName() method to return the explicit short name "Rexx"
                              (used for the ScriptEngine.NAME entry in the ENGINE_SCOPE)

        - 2016-12-21, ---rgf: getLanguageVersion() now gets the "parse version" string from the used
                              ooRexx interpreter, saves it in LANGUAGE_VERSION (now package private)
                              and returns that value

        - 2017-09-22, ---rgf: getLanguageVersion() directly employs a BSF Rexx engine, making sure
                              that BSF.CLS gets required

	- 2017-09-23, ---rgf: getLanguageVersion() explicitly terminates the Rexx engine after
			      retrieving the Rexx version
*/


/** Class that implements the <code>javax.script.ScriptEngineFactory</code> interface for the ooRexx support.
 *
 * @author Rony G. Flatscher
 * @version 100.20170922
 * @since   2015-05-11
 *
 */
public class RexxScriptEngineFactory implements ScriptEngineFactory
{
    final private static boolean bDebug=false; // true;

    /** If a RexxScriptEngine gets created directly, allow it to fetch an instance of the RexxScriptEngineFactory. */
    final public static RexxScriptEngineFactory DEFAULT_REXX_SCRIPT_ENGINE_FACTORY=new RexxScriptEngineFactory();

    final private static String       ENGINE_NAME      ="Open Object Rexx (ooRexx)";
    final private static String       ENGINE_VERSION   ="100.20170923";
    final private static List<String> EXTENSIONS       = Arrays.asList("rex", "rexx", "orx", "cls",
                                                                "rxj", "rexxj", "jrexx",
                                                                "rxo" );
    final private static String       LANGUAGE_NAME    ="ooRexx";
    // final private static String       LANGUAGE_VERSION ="4.1.0";
    static String                     LANGUAGE_VERSION=null;    // let it be accessed by RexxScriptEngine's constructor

    final private static String       SHORT_NAME       ="Rexx";

    final private static List<String> MIME_TYPES       = Arrays.asList("text/x-rexx",
                                                                       "text/x-rexx-java",
                                                                       "text/x-rexx-java-ooo" );
    final private static List<String> SCRIPT_ENGINE_SHORT_NAMES = Arrays.asList("rexx", "Rexx",
                                                                       "oorexx", "ooRexx", "orexx", "oRexx");
    final private static String       THREADING        = "MULTITHREADED"; // "THREAD-ISOLATED";


    /** Returns the full name of the ScriptEngine. */
    public String getEngineName()
    {
        return ENGINE_NAME;
    }

    /** Returns the version of the ScriptEngine. */
    public String getEngineVersion()
    {
        return ENGINE_VERSION;
    }

    /** Returns an immutable list of filename extensions, which generally identify scripts written
        in the language supported by this ScriptEngine. */
    public List<String> getExtensions()
    {
        return EXTENSIONS;
    }

    /** Returns the name of the scripting langauge supported by this ScriptEngine. */
    public String getLanguageName()
    {
        return LANGUAGE_NAME;
    }

    /** Returns the version of the scripting language supported by this ScriptEngine. */
    public String getLanguageVersion()
    {
        // ask the currently available Rexx interpreter about its version
        if (LANGUAGE_VERSION==null)
        {
            try
            {
                BSFManager bsfMgr = new BSFManager ();                // get an instance of BSFManager
                BSFEngine  bsfRexxEngine = bsfMgr.loadScriptingEngine("rexx");  // load the Rexx engine

                String rexxCode="parse version v \n return v \n ::requires BSF.CLS";    // get Rexx version
                LANGUAGE_VERSION=(String) bsfRexxEngine.apply("RexxScriptEngineFactory.java",
                                                              0, 0, rexxCode, null, null);
		bsfRexxEngine.terminate();   // do not use this Rexx interpreter instance anymore
            }
            catch (Exception exc)
            {
                System.err.println(exc.toString());
                exc.printStackTrace();  // show stack trace
            }

        }
        return LANGUAGE_VERSION;
    }

    /** Returns the short name of the scripting language supported by this ScriptEngine. */
    public String getName()
    {
        return SHORT_NAME;
    }

    /** Returns a String which can be used to invoke a method of a Java object using the
     *   syntax of the supported scripting language.
     *
     *    @param obj - The name representing the object whose method is to be invoked.
     *
     *    @param m - The name of the method to invoke.
     *
     *    @param args - names of the arguments in the method call.
     *
     */
    public String getMethodCallSyntax(String obj, String m, String... args)
    {
        StringBuilder sb=new StringBuilder();
            // append object name, message operator and message name
        sb.append(obj).append('~').append(m);

        if (args!=null && args.length>0)    // if we have arguments supply round brackets
        {
            sb.append('(');
            for (int i=0;i<args.length;i++) // append the argument
            {
                if (i>0)
                {
                    sb.append(',');         // append comma
                }
                String arg=args[i];
                if (arg==null)              // null (could be missing argument, but we do not know, hence pass value to ooRexx)
                {
                    sb.append(".nil");
                }
                else                        // append argument
                {
                    sb.append(arg);
                }
            }
            sb.append(')');
        }
        return sb.toString();
    }

    /** Returns an immutable list of mimetypes, associated with scripts that can be executed by the engine. */
    public List<String> getMimeTypes()
    {
        return MIME_TYPES;
    }

    /** Returns an immutable list of short names for the ScriptEngine, which may be used to identify
        the ScriptEngine by the ScriptEngineManager. */
    public List<String> getNames()
    {
        return SCRIPT_ENGINE_SHORT_NAMES;
    }

    /** Returns a String that can be used as a statement to display the specified String using
        the syntax of the supported scripting language. */
    public String getOutputStatement(String toDisplay)
    {
// TODO: decide whether using SAY instead (which appends a \n)

        if (toDisplay==null)
        {
            return "say";
        }
        return "say \""+toDisplay.replace("\"","\"\"")+"\"";    // escape quote in the Rexx language style

/*
        if (toDisplay==null)
        {
            return "";
        }
        return ".output~charout(\""+toDisplay.replace("\"","\"\"")+"\")";    // escape quote in the Rexx language style
*/

    }


    /** Returns the value of an attribute whose meaning may be implementation-specific. */
    public Object getParameter(String key)
    {
        if (key.equals("THREADING"))
        {
            return THREADING;
        }
        return null;
    }

    /** Returns a valid scripting language executable program with given statements. */
    public String getProgram(String... statements)
    {
        StringBuilder sb=new StringBuilder();
        String strThis=this.toString();

        sb.append("/* --> begin of "+strThis+".getProgram(String[]) --> "+getGMTFormattedDateAsISO()+" --> */;\n");

        if (statements==null)
        {
            sb.append("\t/* -- no statements supplied (received \"null\") */;\n");
        }
        else if (statements.length==0)
        {
            sb.append("\t/* -- no statements supplied (received an empty array) */; \n");
        }
        else
        {
            for (int i=0;i<statements.length;i++)
            {
                if (statements[i]==null)
                {
                    sb.append("\t-- 'null' received!;\n");
                }
                else
                {
                    sb.append("\t"+statements[i]+";\n");
                }
            }
        }
        sb.append("/* <-- end   of "+strThis+".getProgram(String[]) <-- "+getGMTFormattedDateAsISO()+" <-- */; \n");
        return sb.toString();
    }


    /** Creates and returns an instance of the ScriptEngine associated with this ScriptEngineFactory. */
    public ScriptEngine getScriptEngine()
    {
if (bDebug)
{ System.err.println("... arrived in "+this+".getScriptEngine()");}

        RexxScriptEngine rse=new RexxScriptEngine();
        return rse;
    }


    // -------------- utility
    /** Utility method that creates and returns a GMT-based ISO date string.
     *
     *  @return the current ISO date, edited to be usable as a filename
     */
    static public String getGMTFormattedDateAsISO()
    {
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(new Date());
    }

}
