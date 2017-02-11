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
filename="nutshell_02.rex"

b=sc~getBindings(sc~ENGINE_SCOPE)
if (b==.nil) then    -- if no ENGINE_SCOPE bindings available, create a Bindings
    b=rse~createBindings

b~put(rse~FILENAME,filename)

.error~say("(Host) executing" pp(filename) "fetching resulting ooRexx object:")
rp=rse~eval(.bsf~new("java.io.FileReader", filename), b) -- now let us execute the Rexx script

   -- use invokeMethod(...)
.error~~say("(Host) using invokeMethod() using the <RexxScriptEngine>:")~~say
.error ~say("(Host) invokeMethod('one'):" rse~invokeMethod(rp, "ONE", .nil))
args=bsf.createJavaArrayOf("java.lang.Object", "un", "deux", "trois")
.error ~say("(Host) invokeMethod('two'):" rse~invokeMethod(rp, "two", args)) -- "un", "deux", "trois"))
.error ~say("(Host) invokeMethod('now'):" rse~invokeMethod(rp, "now", .nil))
.error~~say("---")~~say

   -- use invokeFunction(...)
.error~~say("(Host) using invokeFunction() using the <RexxScriptEngine>:")~~say
.error ~say("(Host) invokeFunction('routine_eins'):" rse~invokeFunction("ROUTINE_EINS", .nil))
args=bsf.createJavaArrayOf("java.lang.Object", "eins", "zwei", "drei")
.error ~say("(Host) invokeFunction('routine_zwei'):" rse~invokeFunction("routine_zwei", args))
.error~~say("---")~~say

rcs=rse~getCurrentScript   -- get current script

    -- use invokeMethod(...)
.error~~say("(Host) using invokeMethod() using the the <RexxCompiledScript> directly:")~~say
.error ~say("(Host) invokeMethod('one'):" rcs~invokeMethod(rp, "ONE", .nil))
args=bsf.createJavaArrayOf("java.lang.Object", "un", "deux", "trois")
.error ~say("(Host) invokeMethod('two'):" rcs~invokeMethod(rp, "two", args))
.error ~say("(Host) invokeMethod('now'):" rcs~invokeMethod(rp, "now", .nil))
.error~~say("---")~~say

    -- use invokeFunction(...)
.error~~say("(Host) using invokeFunction() using the the <RexxCompiledScript> Rexx script> directly:")~~say
.error ~say("(Host) invokeFunction('routine_eins'):" rcs~invokeFunction("ROUTINE_EINS", .nil))
args=bsf.createJavaArrayOf("java.lang.Object", "eins", "zwei", "drei")
.error ~say("(Host) invokeFunction('routine_zwei'):" rcs~invokeFunction("routine_zwei", args))
.error~~say("---")~~say

::requires "BSF.CLS"    -- get Java support

