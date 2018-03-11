#!/usr/bin/env rexx
/*
-- 2017-02-11, Rony G. Flatscher: this is the Rexx equivilent fo the Java program "RunScript.java"

------------------------ Apache Version 2.0 license -------------------------
   Copyright 2017 Rony G. Flatscher

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


/** A command line Rexx utility to query, test and evaluate {@link javax.script} scripting engines that
 *  gets distributed via the BSF4ooRexx package from Sourceforge.
 *
 *  <p>Usage: <code>RexxRunScript [{-i | -t | -e enginename filename [args...] | filename [args...]}]</code>
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
 *  @since 2017-02-11
 */

parse arg args

manager   = .bsf~new("javax.script.ScriptEngineManager")
factories = manager~getEngineFactories
n2sef=.bsf~new("java.util.HashMap") -- ScriptEngineFactory
n2se =.bsf~new("java.util.HashMap") -- ScriptEngine

   -- create the Maps
do factory over factories
   name=factory~getLanguageName
   -- String name=factory.getEngineName
   n2sef~put(name, factory)
   n2se ~put(name, factory~getScriptEngine)
end

   -- create a sorted set of key names
keys = .bsf~new("java.util.TreeSet", n2sef~keySet)

argsStartAt      =-1;   -- arguments - if any - for filenmae to execute start at this position
scriptEngineToUse=.nil
filename         =.nil

if args="" then      -- list all available javax.script engines
do
   say "The following javax.script engines are currently available:"; say
   do key over keys
       say "09"x || key
   end
   say
   call showHelp
   exit
end

else    -- process the first argument to decide what we need to do
do
   parse arg firstWord .    -- get first value

   if firstWord~left(1)="-" then    -- a switch at hand!
   do
       if firstWord~length<>2 then  -- oops an error, we need excactly two chars (-i, -t, -e)
       do
           .error~say("switch '-' must be followed by one of the letters 'i', 't', or 'e'")
           exit -1
       end

       uSwitch=firstWord~substr(2,1)~upper -- get switch in uppercase
       select
       when uSwitch="I" then     -- list all engines, list all their standard properties
             do
                 do key over keys      -- list all engines in order{
                     call showEngineProperties key, n2sef~get(key)
                 end
                 exit
             end

       when uSwitch="T" then     -- list all engines, test them
             do
                 do key over keys      -- list all engines in order
                     call testEngine key, n2sef~get(key),  n2se~get(key)
                 end
                 exit
             end

       when uSwitch="H" then     -- -h ... help text
             do
                 call showHelp
                 exit
             end

       when uSwitch="E" then     -- -e engineName fileName [arg... ]
             do
                 if words(args)<3 then
                 do
                     .error~say("too few command line arguments ("words(args)"), minimum needed is 3: ""-e enginename filename""")
                     exit -2
                 end

                 -- check whether engine is available, if so, then load it
                 filename=word(args,3)      -- assign filename
                 errMsg="no """word(args,1)""" javax.script engine available"
                 scriptEngineToUse=manager~getEngineByName(word(args,2))   -- fetch script engine
                 if scriptEngineToUse=.nil then
                 do
                     .error~say(errMsg)
                     exit -3
                 end
                 argsStartAt=4      -- fourth argument!
             end

       otherwise  -- unknown switch
             do
                 .error~say("unknown switch '"firstWord"', must be followed by one of '-h', '-i', '-t', or '-e'")
                 exit -4
             end
       end  -- end select
   end
   else    -- a filename in hand, check whether we have a suitable engine available
   do
       --        - check whether suitable engine is available, if not raise an exception
       filename=word(args,1)           -- assign filename
       lastIdx=filename~lastPos('.')

       if lastIdx=0 then
       do
           .error~say("filename ""filename"" does not have an extension, cannot determine scripting engine")
           exit -5
       end

       errMsg="cannot determine scripting engine from the filename extension in """filename""""

       extension=filename~substr(lastIdx+1)  -- get extension
       scriptEngineToUse=manager~getEngineByExtension(extension)  -- fetch script engine

       if scriptEngineToUse=.nil then  -- no scripting engine found
       do
           .error~say(errMsg)
           exit -6
       end

       argsStartAt=2      -- second argument!
   end
end


-- now setup the remainder and eval() the "filename" with the script engine
--        - check whether file exists, if not raise an exception
f=.bsf~new("java.io.File", filename)

if f~exists=.false then
do
   .error~say("filename """filename""" does not exist")
   exit -7
end

--        - supply filename
sc=scriptEngineToUse~getContext  -- get the default context
sc~setAttribute(scriptEngineToUse~FILENAME, filename, sc~ENGINE_SCOPE)

--        - if arguments, setup ARGV in ENGINE_SCOPE
if words(args)>argsStartAt then  -- do we have command line arguments to supply?
do
   size=words(args)-argsStartAt+1  -- determine array size
   if size>0 then
   do
      argArr=bsf.createJavaArray("java.lang.Object", size)
      do i=argsStartAt to words(args)
          argArr[i-argsStartAt+1]=word(args,i)
      end
   end
   else
      argArr=.nil

   sc~setAttribute(scriptEnginetoUse~ARGV, argArr, sc~ENGINE_SCOPE)
end

--        - eval the script
scriptEngineToUse~eval(.bsf~new("java.io.FileReader", f))


::requires "BSF.CLS"    -- get Java support

-- show information about the script engine
::routine showEngineProperties
   use arg name, sef

  say name
  say "09"x || "getEngineName          :" get_engine_property(sef, "getEngineName")
  say "09"x || "getEngineVersion       :" get_engine_property(sef, "getEngineVersion")
  say "09"x || "getExtensions          :" get_engine_property(sef, "getExtensions")
  say "09"x || "getLanguageName        :" get_engine_property(sef, "getLanguageName")
  say "09"x || "getLanguageVersion     :" get_engine_property(sef, "getLanguageVersion")
  say "09"x || "getMimeTypes           :" get_engine_property(sef, "getMimeTypes")
  say "09"x || "getNames               :" get_engine_property(sef, "getNames")
  say "09"x || "getParameter(THREADING):" get_engine_property(sef, "getParameter", "THREADING")

::routine get_engine_property
  use arg sef, msg, arg=.nil
  signal on syntax
  if arg=.nil then
     str=sef~send(msg)
  else
     str=sef~sendWith(msg, .array~of(arg))

  if str~isA(.bsf) then -- if a Java object, send it "toString"
    str=str~toString

  return str
syntax:
  return "--> FAILED!"

 -- create an output statement and execute output statement for each available script engine
::routine testEngine
   use arg name, sef, se

   tab="09"x      -- define TAB character
   say "---> language" pp(name)": ---------------------------------------->"; say
   text="hello world, this is """name""" speaking! "
   code=sef~getOutputStatement(text)
   say tab"1) output statement to process:" text; say

   .output~charout(tab"--> testing getOutputStatement(String)")
   say ", produced the following" pp(name) "output statement)"; say; say; say code; say
   say tab"... now running ""eval(code)"" using the scripting engine" pp(name)":"; say; say
--   se~eval(code)
   call test_engine_eval se, code
   say; say

   .output~charout(tab"2) testing getMethodCallSyntax(obj, meth, arg...)")
   args=bsf.createJavaArrayOf("java.lang.String", "arg1", "arg2")
   methCode=sef~getMethodCallSyntax("object", "method", args)
   say ", produced the following" pp(name) "method call statement:"; say; say; say methCode; say

   .output~charout(tab"3) testing getProgram(String...)")
   stmts=bsf.createJavaArrayOf("java.lang.String", code, methCode)
   programCode=sef~getProgram(stmts)
   say " produced the following" pp(name) "program:"; say; say; say programCode; say

   say "<--- end of testing language" pp(name)". <----------------------------------------"; say

::routine test_engine_eval -- evaluate, in case of an error show error message
   use arg se, code
   signal on syntax
   se~eval(code)     -- evaluate the code
   return

syntax:
   -- say condition("object")~message  -- just show error message
   return

-- allow us to call this from different parts
::routine showHelp
   say "usage: RexxRunScript [{-i | -t | -e enginename filename [args...] | filename [args...]}]"; say
   say "          ... no arguments lists the available javax.script engines"
   say "       -h ... 'help', gives this usage text"
   say "       -i ... lists the available javax.script engines and informs about their properties"
   say "       -t ... lists the available javax.script engines and tests evaluating their ""getOutputStatement(String)"" method"
   say "       -e  enginename filename [args...] ... uses ""enginename"" to evaluate ""filename"" supplying the arguments ""args..."""
   say "       filename [args...] ... the ""filename"" extension determines the script engine to evaluate it, supplying the arguments ""args..."""
