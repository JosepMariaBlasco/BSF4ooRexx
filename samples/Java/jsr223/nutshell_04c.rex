/*
   Rexx companion script that gets loaded and evaluated by Nutshell_04_Eval.java:

   /** Demonstrate evaluating a Rexx script, creating Rexx variables from the values
    *  of the ScriptContext using the new Rexx script annotation "@get()" (embedded in
    *  a Rexx block comment).
    *
    *  @author Rony G. Flatscher
    *  @since 2016-12-07
    */

*/


parse source s
say "parse source:" pp(s)

say "creating Rexx context variables from all scriptContext Bindings using the Rexx script annotation ""/*@" || "get()*/"" ..."
/*@get()*/ /* from now on we can access *all* the ScriptScope attributes as local Rexx variables !
              Alternatively, one can denote the desired attributes by blank delimited names within the parentheses */

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
