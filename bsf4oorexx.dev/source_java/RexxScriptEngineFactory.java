package org.rexxla.bsf.engines.rexx;
import javax.script.*;

import java.util.Arrays;
import java.util.List;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.bsf.*;   // BSF support

import org.rexxla.bsf.engines.rexx.RexxEngine;

/*
------------------------ Apache Version 2.0 license -------------------------
   Copyright 2015-2025 Rony G. Flatscher

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

        - 2019-07-26, ---rgf: add "text/rexx", "text/oorexx", "application/rexx", "application/oorexx"

        - 2019-11-04, ---rgf: - add uppercase names to "SCRIPT_ENGINE_SHORT_NAMES"
                              - getParameter() now returns values for the keys (as Nahshorn):
                                ScriptEngine.NAME,
                                ScriptEngine.ENGINE, ScriptEngine.ENGINE_VERSION,
                                ScriptEngine.LANGUAGE, ScriptEngine.LANGUAGE_VERSION
        - 2019-11-07, ---rgf: - new static method "getStringLiteral()"
                              - getOutputStatement() now uses getStringLiteral() and returns
                                ".output~charOut(toDisplayAsRexxStringLiteral)"

        - 2020-01-20, ---rgf: - error fix: return THREADING constant value instead of "THREADING"

        - 2020-11-24, ---rgf: - getProgram(): make comment a little bit more terse

        - 2020-12-17, ---rgf: - introduced a boolean field that controls whether the
                                getOutputStatement(someString) uses "SAY" (true) or
                                ".output~charout" (false)

        - 2022-08-01, ---rgf: - make the following final fields protected to allow RexxScriptEngine
                                direct access: ENGINE_NAME, ENGINE_VERSION, EXTENSIONS, LANGUAGE_NAME,
                                SHORT_NAME, MIME_TYPES, SCRIPT_ENGINE_SHORT_NAMES, THREADING

        - 2022-08-02, ---rgf: - use a RexxEngine to fetch the LANGUAGE_VERSION value

        - 2022-08-06, ---rgf, - changed package name to 'org.rexxla.bsf.engines.rexx'
                              - now uses RexxEngine.getLanguageVersionString()

        - 2025-08-27, ---rgf, - reflect that RexxEngine got changed and now uses as 'id' value
                                the ooRexx interpreter instance identifier
*/


/** Class that implements the <code>javax.script.ScriptEngineFactory</code> interface for the ooRexx support.
 *
 * @author Rony G. Flatscher
 * @version 100.20220806
 * @since   2015-05-11
 *
 */
public class RexxScriptEngineFactory implements ScriptEngineFactory
{
    final private static boolean bDebug=false; // true;

    /** If a RexxScriptEngine gets created directly, allow it to fetch an instance of the RexxScriptEngineFactory. */
    final public static RexxScriptEngineFactory DEFAULT_REXX_SCRIPT_ENGINE_FACTORY=new RexxScriptEngineFactory();

    final protected static String       ENGINE_NAME      ="Open Object Rexx (ooRexx)";
    final protected static String       ENGINE_VERSION   ="101.20250827";
    final protected static List<String> EXTENSIONS       = Arrays.asList("rex", "rexx", "orx", "cls",
                                                                "rxj", "rexxj", "jrexx",
                                                                "rxo" );
    final protected static String       LANGUAGE_NAME    ="ooRexx";
    protected static String             LANGUAGE_VERSION =null; // let it be accessed by RexxScriptEngine

    final protected static String       SHORT_NAME       ="rexx";

    final protected static List<String> MIME_TYPES       = Arrays.asList(
                                                                       "text/rexx",
                                                                       "text/oorexx",
                                                                       "application/rexx",
                                                                       "application/oorexx",
                                                                       "text/x-rexx",
                                                                       "text/x-rexx-java",
                                                                       "text/x-rexx-java-ooo"
                                                                       );
    final protected static List<String> SCRIPT_ENGINE_SHORT_NAMES = Arrays.asList("rexx", "Rexx", "REXX",
                                                                       "oorexx", "ooRexx", "OOREXX",
                                                                       "orexx", "oRexx", "OREXX");
    final protected static String       THREADING        = "MULTITHREADED"; // "THREAD-ISOLATED";

    /** Field controls whether {@link #getOutputStatement(String toDisplay)} uses the
     *  Rexx <code>SAY</code> (always appends a line-feed character) keyword statement
     *  or the ooRexx <code>.output</code> monitor with the <code>charOut</code> method
     *  (will output the string as is without appending anything).
     *  This field is set to <code>true</code> by default causing the <code>SAY</code>
     *  keyword statement to be used.
    */
    public static boolean bSay4Output=true; // default: use SAY


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
if (bDebug) System.err.println("RexxScriptEngineFactory.getLanguageVersion() ...");

        // use RexxEngine to get the current ooRexx version string
        if (LANGUAGE_VERSION==null)
        {
            LANGUAGE_VERSION=RexxEngine.getLanguageVersionString();

        }
if (bDebug) System.err.println("RexxScriptEngineFactory.getLanguageVersion()=LANGUAGE_VERSION=["+LANGUAGE_VERSION+"]");
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
        the syntax of the supported scripting language. The boolean field {@link #bSay4Output}
        value controls whether <code>SAY</code> (<code>true</code>) or
        <code>.output~charOut</code> (<code>false</code>) gets employed.
    */
    public String getOutputStatement(String toDisplay)
    {
        if (bSay4Output)
        {
            return "SAY " + getStringLiteral(toDisplay);
        }
        return ".output~charOut(" + getStringLiteral(toDisplay) + ")";
    }


    /** Creates and returns a quoted Rexx string, with carriage return (CR) and
     *  line-feed (LF) characters escaped as concatenated Rexx hex strings.
     *
     * @param toDisplay the string to encode as a Rexx string literal, if null then ".nil" gets returned
     * @return a Rexx encoded string
     */

    static public String getStringLiteral(final String toDisplay)
    {
        if (toDisplay==null)        // .nil
        {
            return ".nil";
        }

        if (toDisplay.length()==0)  // empty string
        {
            return "\"\"";
        }


        StringBuffer sb=new StringBuffer();

        char    charArr[] = toDisplay.toCharArray();
        boolean bAddLeadingQuote = true;
        boolean bAddClosingQuote = true;

        for (int i=0; i<charArr.length;i++)
        {
            char c=charArr[i];

            if (c=='\r' || c=='\n')     // escape CR and/or LF
            {
                if (i==0)               //
                {
                    bAddLeadingQuote = false;
                    sb.append('"');     // close quote
                }
                else
                {
                    sb.append("\" || \"");      // close quote
                }
                if (c=='\r')
                {
                    sb.append("0d");
                    if ((i+1)<charArr.length)
                    {
                        if (charArr[i+1]=='\n') // \n in hand!
                        {
                           sb.append("0a");
                           i++;
                        }
                    }
                }
                else    // \n
                {
                    sb.append("0a");
                }
                sb.append("\"x");           // end hex-string

                if ((i+1)==charArr.length)  // no bytes left
                {
                    bAddClosingQuote = false;     // we are done
                }
                else                        // concatenate remaining characters of string
                {
                    sb.append(" || \"");
                }

            }
            else
            {
                if (bAddLeadingQuote==true)
                {
                    sb.append('\"');
                    bAddLeadingQuote=false;
                }

                if (c=='"')     // escape quote Rexx style
                {
                    sb.append("\"\"");
                }
                else
                {
                    sb.append(c);
                }
            }
        }

        if (bAddClosingQuote)
        {
            sb.append('"');
        }

        return sb.toString();
    }


    /** Returns the value of an attribute whose meaning may be implementation-specific. */
    public Object getParameter(final String key)
    {
// baseline is Java 6, hence cannot use a String value in a switch statement
/*
        switch (key)
        {
            case "THREADING":
                return THREADING;

            case ScriptEngine.NAME:
                return SHORT_NAME;

            case ScriptEngine.ENGINE:
                return ENGINE_NAME;

            case ScriptEngine.ENGINE_VERSION:
                return ENGINE_VERSION;

            case ScriptEngine.LANGUAGE:
                return LANGUAGE_NAME;

            case ScriptEngine.LANGUAGE_VERSION:
                 return LANGUAGE_VERSION==null ? getLanguageVersion() : LANGUAGE_VERSION;

            default:
                return null;
        }
*/
        if (key.equals("THREADING")) return THREADING;

        if (key.equals(ScriptEngine.NAME)            ) return SHORT_NAME;
        if (key.equals(ScriptEngine.ENGINE)          ) return ENGINE_NAME;
        if (key.equals(ScriptEngine.ENGINE_VERSION)  ) return ENGINE_VERSION;
        if (key.equals(ScriptEngine.LANGUAGE)        ) return LANGUAGE_NAME;
        if (key.equals(ScriptEngine.LANGUAGE_VERSION)) return LANGUAGE_VERSION==null ?
                                                                getLanguageVersion() :
                                                                LANGUAGE_VERSION ;
        return null;
    }


    /** Returns a valid scripting language executable program with given statements. */
    public String getProgram(String... statements)
    {
        StringBuilder sb=new StringBuilder();
        String strThis=this.toString();
        strThis=strThis.substring(strThis.lastIndexOf('.')); // extract unqualified name with hash

        sb.append("/* --> begin of "+strThis+" --> "+getGMTFormattedDateAsISO()+" --> */;\n");

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
        sb.append("/* <-- end   of "+strThis+" <-- "+getGMTFormattedDateAsISO()+" <-- */; \n");
        return sb.toString();
    }


    /** Creates and returns a new RexxScriptEngine instance. */
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
