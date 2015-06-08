/*
   Rexx companion script that gets loaded and evaluated by Nutshell_04_Eval.java:

   /** Demonstrate evaluating a Rexx script, fetching values to interact with from the supplied
    *  ScriptContext, rather than as arguments (preferred!).
    *
    *  @author Rony G. Flatscher
    *  @since 2015-06-07
    */
*/

parse source s
say "(Rexx) parse source:" pp(s)
   -- slotDir (a Rexx directory) is always appended and contains the invocation's ScriptContext
use arg slotDir
scriptContext=slotDir~scriptContext    -- get ScriptContext entry from slot directory
say "(Rexx) fetching entries from 'ScriptContext'" pp(scriptContext)
started_at=scriptContext~getAttribute("started_at")
say "(Rexx) Java host started set-up at:" pp(started_at)

d1=scriptContext~getAttribute("d1")
d2=scriptContext~getAttribute("d2")
d3=scriptContext~getAttribute("d3")
d4=scriptContext~getAttribute("d4")
d5=scriptContext~getAttribute("d5")
d6=scriptContext~getAttribute("d6")
d7=scriptContext~getAttribute("d7")
d8=scriptContext~getAttribute("d8")

say "(Rexx) calculating aggregate width and height, returning it to Java"
aggWidth  =d1~width +d2~width +d3~width +d4~width +d5~width +d6~width +d7~width +d8~width
aggHeight =d1~height+d2~height+d3~height+d4~height+d5~height+d6~height+d7~height+d8~height
say "(Rexx) aggWidth="pp(aggWidth) "aggHeight="pp(aggHeight)
return .bsf~new("java.awt.Dimension", aggWidth, aggHeight)

::requires bsf.cls   -- get Java support

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
