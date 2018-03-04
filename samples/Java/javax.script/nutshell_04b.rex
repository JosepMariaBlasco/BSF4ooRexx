#!/usr/bin/env rexx
/*
   Rexx companion script that gets loaded and evaluated by Nutshell_04_Eval.java:

   /** Demonstrate evaluating a Rexx script, creating Rexx variables from the values
    *  of the ScriptContext.
    *
    *  @author Rony G. Flatscher
    *  @since 2015-06-07
    */

    changed: 2016-10-27, ---rgf: use .jsr223~scriptContext2rexxDir instead of the deprecated
                         public routine bsf.scriptContext2rexx(); dots in attribute names
                         remain intact, however names get mangled, if necessary to get at
                         valid Rexx variable names
*/


parse source s
say "parse source:" pp(s)
   -- slotDir (a Rexx directory) is always appended and contains the invocation's ScriptContext
use arg slotDir
scriptContext=slotDir~scriptContext    -- get ScriptContext entry from slot directory
say "creating Rexx context variables from the 'ScriptContext' entries" pp(scriptContext) "..."
   -- all the ScriptContext entries will be made directly available as Rexx variables; note:
call bsfContextVariables "set", .jsr223~scriptContext2rexxDir(scriptContext)
say "Java host started set-up at:" pp(started_at)

say "calculating aggregate width and height, returning it to Java"
aggWidth  =d1~width +d2~width +d3~width +d4~width +d5~width +d6~width +d7~width +d8~width
aggHeight =d1~height+d2~height+d3~height+d4~height+d5~height+d6~height+d7~height+d8~height
say "aggWidth="pp(aggWidth) "aggHeight="pp(aggHeight)
return .bsf~new("java.awt.Dimension", aggWidth, aggHeight)

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
