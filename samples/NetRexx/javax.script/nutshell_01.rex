#!/usr/bin/env rexx
/*
   Rexx companion script that gets loaded and evaluated by Nutshell_01_Eval.java:

   /** Demonstrate evaluating a Rexx script, once without supplying arguements and once supplying
    *  arguments.
    *
    *  @author Rony G. Flatscher
    *  @since 2015-05-12
    */
*/

parse source s
say "parse source:" s
say
say "received" arg() "arguments:"
do i=1 to arg()
   say "         arg("i")="pp(arg(i)) pp(arg(i)~class)
   if i=arg() then      -- last argument is a directory containing "SCRIPTCONTEXT"
   do
      say "the last argument is a Rexx directory containing the ScriptContext of this invocation:"
      sc=arg(i)~scriptContext      -- fetch script context
         -- get the constant field values for FILENAME, ARGV from class javax.script.ScriptEngine
      jclz=bsf.importClass("javax.script.ScriptEngine")  -- import the Java class
      fn=jclz~FILENAME     -- get FILENAME constant value
      argv=jclz~ARGV       -- get ARGV constant value
      say "                 FILENAME ->" pp(sc~getAttributesScope(fn))  pp(fn)    pp(sc~getAttribute(fn))
      say "                 ARGV     ->" pp(sc~getAttributesScope(argv)) pp(argv) pp(sc~getAttribute(argv))
   end
end

::requires bsf.cls   -- get Java support

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

   2016-11-08, ---rgf: removed "(Rexx)" from SAY statements as a proper Rexx-identifying prefix gets
                       now supplied automatically
*/
