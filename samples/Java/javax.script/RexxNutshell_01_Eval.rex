#!/usr/bin/env rexx
/*
-- 2017-02-09, Rony G. Flatscher: this is the Rexx equivilent fo the Java program "Nutshell_02_Eval.java"
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

-- Using a ScriptManager to fetch the Rexx script enginge
manager = .bsf~new("javax.script.ScriptEngineManager")
rse     = manager~getEngineByName("Rexx")

sc=rse~getContext -- get the default ScriptContext
    -- add the fileName to the ENGINE_SCOPE bindings
filename="nutshell_01.rex"
sc~setAttribute(rse~FILENAME,filename,sc~ENGINE_SCOPE)

.error~say("(Host) executing" pp(filename) "without arguments:")
rse~eval(.bsf~new("java.io.FileReader", filename), sc)   -- now let us execute the Rexx script

.error~say("-------------------------")
.error~say("(Host) executing" pp(filename)", now with four arguments, the third one being 'null':")
    -- add arguments for the script to the ENGINE_SCOPE bindings
args=bsf.createJavaArrayOf("java.lang.Object", "one", "zwei", .nil, "quatre")
sc~setAttribute(rse~ARGV, args, sc~ENGINE_SCOPE)
    -- the RexxScriptEngine always compiles the last script and makes it available with the getCurrentScript() method
rse~getCurrentScript~eval  -- now let us re-execute the Rexx script

::requires "BSF.CLS"    -- get Java support
