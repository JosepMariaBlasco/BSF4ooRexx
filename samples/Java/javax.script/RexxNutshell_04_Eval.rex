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

timeStamp = .bsf~new("java.text.SimpleDateFormat", "yyyy.MM.dd.HH.mm.ss.SSSSSS")~format(.bsf~new("java.util.Date"))
sc~setAttribute("started_at", timeStamp, sc~GLOBAL_SCOPE)

    -- add the fileName to the ENGINE_SCOPE bindings
filename="nutshell_04a.rex"
sc~setAttribute(rse~FILENAME, filename, sc~ENGINE_SCOPE)

clzDim=bsf.import("java.awt.Dimension")
d1=clzDim~new( 25, 30)
d2=clzDim~new( 50, 30)
d3=clzDim~new( 75, 60)
d4=clzDim~new(100, 60)
d5=clzDim~new(125, 90)
d6=clzDim~new(150, 90)
d7=clzDim~new(175,120)
d8=clzDim~new(200,120)

dimArr=bsf.createJavaArrayOf(clzDim,d1,d2,d3,d4,d5,d6,d7,d8)

    -- define four Dimension objects that Rexx should use to add up the width and the height and return a new Dimension object
sc~setAttribute("d1", d1, sc~ENGINE_SCOPE)
sc~setAttribute("d2", d2, sc~ENGINE_SCOPE)
sc~setAttribute("d3", d3, sc~ENGINE_SCOPE)
sc~setAttribute("d4", d4, sc~ENGINE_SCOPE)
sc~setAttribute("d5", d5, sc~ENGINE_SCOPE)
sc~setAttribute("d6", d6, sc~ENGINE_SCOPE)
sc~setAttribute("d7", d7, sc~ENGINE_SCOPE)
sc~setAttribute("d8", d8, sc~ENGINE_SCOPE)

.error~say("executing" pp(filename) "without arguments:")
obj=rse~eval(.bsf~new("java.io.FileReader",filename), sc)   -- now let us execute the Rexx script
.output~say("Evaluated Rexx script" pp(filename) "returned:" pp(obj~toString))
.output~say("--------------------")

   -- now evaluate "nutshell_04b.rex"!
filename="nutshell_04b.rex"
sc~setAttribute(rse~FILENAME, filename, sc~ENGINE_SCOPE)
.error~say("(Java) executing" pp(filename) "without arguments:")
obj=rse~eval(.bsf~new("java.io.FileReader",filename), sc)   -- now let us execute the Rexx script
.output~say("(Java) Evaluated Rexx script" pp(filename) "returned:" pp(obj~toString))
.output~say("--------------------")

   -- now evaluate "nutshell_04c.rex"!
filename="nutshell_04c.rex"
sc~setAttribute(rse~FILENAME, filename, sc~ENGINE_SCOPE)
.error~say("executing" pp(filename) "(Rexx program contains the Rexx script annotation /* @get(d1 d2 d3 d4 d5 d6 d7 d8 started_at) */ to fetch all attributes from the ScriptContext Bindings as Rexx variables):")
obj=rse~eval(.bsf~new("java.io.FileReader",filename), sc)   -- now let us execute the Rexx script
.output~say("Evaluated Rexx script" pp(filename) "returned:" pp(obj~toString))
.output~say("--------------------")


   -- now evaluate "nutshell_04d.rex"!
filename="nutshell_04d.rex"
sc~setAttribute(rse~FILENAME, filename, sc~ENGINE_SCOPE);
-- add the ARGV entry which will cause the RexxScriptEngine to supply the array elements as
-- individual arguments; it then will append another Rexx directory with an entry "ScriptContext"
sc~setAttribute(rse~ARGV,     dimArr,   sc~ENGINE_SCOPE)
.error~say("executing" pp(filename) "WITH arguments (ScriptEngine.ARGV):")
obj=rse~eval(.bsf~new("java.io.FileReader",filename), sc)   -- now let us execute the Rexx script
.output~say("Evaluated Rexx script" pp(filename) "returned:" pp(obj~toString))

::requires "BSF.CLS"    -- get Java support
