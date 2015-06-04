import java.io.File;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;


import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

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
*/


/** A command line Java utility to query, test and evaluate {@link javax.script} scripting engines.
 *  <p>Usage: <code>java RunScript [{-i | -t | -e enginename filename [args...] | filename [args...]}]</code>
 * <br>
 * <ul>
 * <li> ... no arguments: lists the currently available <code>javax.script</code> engines
 *
 * <li> <code>-h</code> ... 'help', lists and briefly explains the command line options
 *
 * <li> <code>-i</code> ... lists the available <code>javax.script</code> engines and informs about their properties
 *
 * <li> <code>-t</code> ... lists the available <code>javax.script</code> engines and tests evaluating their factory's <code>&quot;getOutputStatement(String)&quot;</code> method
 *
 * <li> <code>-e enginename filename [args...]</code> ... uses <code>&quot;enginename&quot;</code> to evaluate <code>&quot;filename&quot;</code> supplying the arguments <code>&quot;args...&quot;</code>
 *
 * <li> <code>filename [args...]</code> ... the <code>&quot;filename&quot;</code> extension determines the script engine to evaluate it, supplying the arguments <code>&quot;args...&quot;</code>
 * </ul>
 *
 *
 *  @author Rony G. Flatscher
 *  @since 2015-05-20
 */
public class RunScript
{
    public static void main(String args[])
    {

        ScriptEngineManager manager = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = manager.getEngineFactories();
        HashMap<String,ScriptEngineFactory> n2sef=new HashMap<String,ScriptEngineFactory> ();
        HashMap<String,ScriptEngine>        n2se =new HashMap<String,ScriptEngine> ();

            // create the Maps
        for (ScriptEngineFactory factory : factories)
        {
            String name=factory.getLanguageName();
            // String name=factory.getEngineName();
            n2sef.put(name, factory);
            n2se .put(name, factory.getScriptEngine());
        }

            // create a sorted set of key names
        SortedSet<String> keys = new TreeSet<String>(n2sef.keySet());

        int argsStartAt=-1;     // arguments - if any - for filenmae to execute start at this position
        ScriptEngine scriptEngineToUse=null;
        String filename=null;

        if (args.length==0)     // list all available javax.script engines
        {
            for (String key : keys)
            {
                System.out.println(key);
            }
            return;
        }
        else    // process the first argument to decide what we need to do
        {
            String firstWord=args[0];       // get first value

            if (firstWord.startsWith("-")==true)    // a switch at hand!
            {
                if (firstWord.length()!=2)    // oops an error, we need excactly two chars (-i, -t, -e)
                {
                    throw new IllegalArgumentException("switch '-' must be followed by one of the letters 'i', 't', or 'e'");
                }

                String uSwitch=firstWord.substring(1).toUpperCase(); // get switch in uppercase
                if (uSwitch.equals("I"))       // list all engines, list all their standard properties
                {
                    for (String key : keys)         // list all engines in order{
                    {
                        showEngineProperties(key, n2sef.get(key));
                    }
                    return;
                }

                else if (uSwitch.equals("T"))  // list all engines, test them
                {
                    for (String key : keys)         // list all engines in order{
                    {
                        testEngine(key, n2sef.get(key),  n2se.get(key));
                    }
                    return;
                }

                else if (uSwitch.equals("H"))   // -h ... help text
                {
                    System.out.println("usage: java RunScript [{-i | -t | -e enginename filename [args...] | filename [args...]}]\n");
                    System.out.println("          ... no arguments lists the available javax.script engines");
                    System.out.println("       -h ... 'help', gives this usage text");
                    System.out.println("       -i ... lists the available javax.script engines and informs about their properties");
                    System.out.println("       -t ... lists the availabel javax.script engines and tests evaluating their \"getOutputStatement(String)\" method");
                    System.out.println("       -e  enginename filename [args...] ... uses \"enginename\" to evaluate \"filename\" supplying the arguments \"args...\"");
                    System.out.println("       filename [args...] ... the \"filename\" extension determines the script engine to evaluate it, supplying the arguments \"args...\"");
                    return;
                }

                else if (uSwitch.equals("E"))   // -e engineName fileName [arg... ]
                {
                    if (args.length<3)
                    {
                        throw new IllegalArgumentException("too few command line arguments ("+args.length+"), minimum needed is 3: \"-e enginename filename\"");
                    }

                    // check whether engine is available, if so, then load it
                    filename=args[2];           // assign filename
                    String errMsg="no \""+args[1]+"\" javax.script engine available";
                    try
                    {
                        scriptEngineToUse=manager.getEngineByName(args[1]);     // fetch script engine
                    }
                    catch (Exception exc)
                    {
                        throw new IllegalArgumentException(errMsg);
                    }
                    if (scriptEngineToUse==null)
                    {
                        throw new IllegalArgumentException(errMsg);
                    }
                    argsStartAt=3;      // fourth argument!
                }

                else    // unknown switch
                {
                    throw new IllegalArgumentException("unknown switch '"+firstWord+"', must be followed by one of '-h', '-i', '-t', or '-e'");
                }
            }
            else    // a filename in hand, check whether we have a suitable engine available
            {
                //        - check whether suitable engine is available, if not raise an exception
                filename=args[0];           // assign filename
                int lastIdx=filename.lastIndexOf('.');

                if (lastIdx==0)
                {
                    throw new IllegalArgumentException("filename \"filename\" does not have an extension, cannot determine scripting engine");
                }

                String errMsg="cannot determine scripting engine from the filename extension in \""+filename+"\"";

                try
                {
                    String extension=filename.substring(lastIdx+1);   // get extension
                    scriptEngineToUse=manager.getEngineByExtension(extension);     // fetch script engine
                }
                catch (Exception exc)       // if substring() causes an exception
                {
                    throw new IllegalArgumentException(errMsg);
                }

                if (scriptEngineToUse==null)    // no scripting engine found
                {
                    throw new IllegalArgumentException(errMsg);
                }

                argsStartAt=1;      // second argument!
            }
        }

        // now setup the remainder and eval() the "filename" with the script engine
        //        - check whether file exists, if not raise an exception
        File f=null;
        try
        {
            f=new File (filename);
        }
        catch (Exception exc)
        {
            throw new IllegalArgumentException(exc);
        }

        if (f.exists()==false)
        {
            throw new IllegalArgumentException("filename \""+filename+"\" does not exist");
        }

        //        - supply filename
        ScriptContext sc=scriptEngineToUse.getContext();    // get the default context
        sc.setAttribute(ScriptEngine.FILENAME, filename, ScriptContext.ENGINE_SCOPE);

        //        - if arguments, setup ARGV in ENGINE_SCOPE
System.err.println("args.length="+args.length+", argsStartAt="+argsStartAt+", > ? "+ (args.length>argsStartAt));
        if (args.length>argsStartAt)    // do we have command line arguments to supply?
        {
            String argArr []=new String [args.length-argsStartAt];    // determine array size
            int k=0;
            for (int i=argsStartAt; i<args.length; i++, k++)
            {
                argArr[k]=args[i];
            }
            sc.setAttribute(ScriptEngine.ARGV, argArr, ScriptContext.ENGINE_SCOPE);
        }
System.err.println("sc.getAttribute(ScriptEngine.ARGV)="+sc.getAttribute(ScriptEngine.ARGV));

        //        - eval the script
        try
        {
            scriptEngineToUse.eval(new FileReader(f));
        }
        catch (Exception exc)
        {
            throw new IllegalArgumentException(exc);
        }
    }


    // show information about the script engine
    static void showEngineProperties(String name, ScriptEngineFactory sef)
    {
        System.out.println(name);

        System.out.print  ("\tgetEngineName()          : ");
        try { System.out.println(sef.getEngineName()     ); } catch (Exception e) { System.out.println("--> FAILED!"); }

        System.out.print  ("\tgetEngineVersion()       : ");
        try { System.out.println(sef.getEngineVersion()  ); } catch (Exception e) { System.out.println("--> FAILED!"); }

        System.out.print  ("\tgetExtensions()          : ");
        try { System.out.println(sef.getExtensions     ()); } catch (Exception e) { System.out.println("--> FAILED!"); }

        System.out.print  ("\tgetLanguageName()        : ");
        try { System.out.println(sef.getLanguageName ()  ); } catch (Exception e) { System.out.println("--> FAILED!"); }

        System.out.print  ("\tgetLanguageVersion()     : ");
        try { System.out.println(sef.getLanguageVersion()); } catch (Exception e) { System.out.println("--> FAILED!"); }

        System.out.print  ("\tgetMimeTypes()           : ");
        try { System.out.println(sef.getMimeTypes      ()); } catch (Exception e) { System.out.println("--> FAILED!"); }

        System.out.print  ("\tgetNames()               : ");
        try { System.out.println(sef.getNames()          ); } catch (Exception e) { System.out.println("--> FAILED!"); }

        System.out.print  ("\tgetParameter(\"THREADING\"): ");
        try { System.out.println(sef.getParameter("THREADING")); } catch (Exception e) { System.out.println("--> FAILED!"); }
    }


    // create an output statement and execute output statement for each available script engine
    static void testEngine(String name, ScriptEngineFactory sef, ScriptEngine se)
    {
        String text="hello world, this is \""+name+"\" speaking! ";
        // String code=sef.getProgram(sef.getOutputStatement(text));
        String code=sef.getOutputStatement(text);
        System.out.println("---> language: "+name+"\n");
        System.out.println("\toutput statement to process: \n\n\t\t"+text+"\n");
        System.out.println("\tgetOutputStatement(text) returned: \n\n\t\t"+code+"\n");
        System.out.println("\tnow running \"eval(code)\" using the scripting engine: \n");
        try
        {
            se.eval(code);
            System.out.println("\n");
        }
        catch (ScriptException sexc)
        {
            System.err.println(sexc+" while eval() the code: "+code);
        }
    }
}


