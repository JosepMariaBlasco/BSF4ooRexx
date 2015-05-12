/*
   Rexx companion script that gets loaded and evaluated by Nutshell_01_Eval.java:

   /** Demonstrate evaluating a Rexx script, once without supplying arguements and once supplying
    *  arguments.
    *
    *  @author Rony G. Flatscher
    *  @since 2015-05-12
    */

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

parse source s
say "(Rexx) parse source:" s
say
say "(Rexx) received" arg() "arguments:"
do i=1 to arg()
   say "(Rexx)   arg("i")="pp(arg(i)) pp(arg(i)~class)
   if i=arg() then      -- last argument is a directory containing "SCRIPTCONTEXT"
   do
      say "(Rexx) the last argument is a Rexx directory containing the ScriptContext of this invocation:"
      sc=arg(i)~scriptContext      -- fetch script context
      jclz=bsf.importClass("javax.script.ScriptEngine")
      fn=jclz~FILENAME
      say "(Rexx)           FILENAME ->" pp(sc~getAttributesScope(fn))  pp(fn)    pp(sc~getAttribute(fn))
      argv=jclz~ARGV
      say "(Rexx)           ARGV     ->" pp(sc~getAttributesScope(argv)) pp(argv) pp(sc~getAttribute(argv))
   end
end

::requires bsf.cls   -- get Java support
