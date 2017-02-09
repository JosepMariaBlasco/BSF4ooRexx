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
filename="nutshell_03.rex"

b=sc~getBindings(sc~ENGINE_SCOPE)
if (b==.nil) then    -- if no ENGINE_SCOPE bindings available, create a Bindings
    b=rse~createBindings

b~put(rse~FILENAME,filename)

----------
    -- use public <T> T getInterface(Class<T> clasz)
.error~say("using ""<T> T getInterface(Class<T> clasz)"" using the <RexxScriptEngine>:")
.error~say("executing[" pp(filename) "fetching resulting ooRexx object:")
rp=rse~eval(.bsf~new("java.io.FileReader",filename), b)  -- now let us execute the Rexx script
.error~say("---")

clzRunnable=bsf.loadClass("java.lang.Runnable")

r=rse~getInterface(clzRunnable)  -- get an interface implementation, will use the latest CompiledRexxScript object
.error~say("creating new Thread supplying" pp(r)":")
th = .bsf~new("java.lang.Thread",r)
th~bsf.dispatch("start")   -- make sure the "start" message is sent to the Java side
th~sleep(100)  -- should be enough time for the thread to end (after running the Rexx code)
.error~say("---")


    -- use public <T> T getInterface(Object thiz, Class<T> clasz)
.error~say("using ""<T> T getInterface(Object thiz, Class<T> clasz)"" using the <RexxScriptEngine>:")
r=rse~getInterface(rp, clzRunnable)    -- get an interface implementation
.error~say("creating new Thread supplying[" pp(r)":")
th = .bsf~new("java.lang.Thread",r)
th~bsf.dispatch("start")   -- make sure the "start" message is sent to the Java side
th~sleep(100)  -- should be enough time for the thread to end (after running the Rexx code)
.error~say("---");


.error~say("--- now using the RexxCompiledScript methods instead ---")
.error~say("---")
rcs=rse~getCurrentScript   -- get current script

    -- use public <T> T getInterface(Class<T> clasz)
.error~say("using ""<T> T getInterface(Class<T> clasz)"" using the <RexxCompiledScript>:")
r=rcs~getInterface(clzRunnable)  -- get an interface implementation
.error~say("creating new Thread supplying" pp(r)":")
th = .bsf~new("java.lang.Thread",r)
th~bsf.dispatch("start")   -- make sure the "start" message is sent to the Java side
th~sleep(100)  -- should be enough time for the thread to end (after running the Rexx code)
.error~say("---");

    -- use public <T> T getInterface(Object thiz, Class<T> clasz)
.error~say("using ""<T> T getInterface(Object thiz, Class<T> clasz)"" using the <RexxCompiledScript>:")
r=rcs~getInterface(rp, clzRunnable) -- get an interface implementation
.error~say("creating new Thread supplying" pp(r)":");
r=rcs~getInterface(rp, clzRunnable) --get an interface implementation
th = .bsf~new("java.lang.Thread",r)
th~bsf.dispatch("start")   -- make sure the "start" message is sent to the Java side
th~sleep(100)  -- should be enough time for the thread to end (after running the Rexx code)

::requires "BSF.CLS"    -- get Java support

