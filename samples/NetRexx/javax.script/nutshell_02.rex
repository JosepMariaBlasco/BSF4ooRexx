/*
   Rexx companion script that gets loaded and evaluated by Nutshell_02_Invoke.java:

   /** Demonstrate the usage of the <code>Invocable</code> methods, namely <code>invokeMethod(...)</code>
    *  and <code>invokeFunction(...)</code>.
    *
    *  @author Rony G. Flatscher
    *  @since 2015-05-12
    */

In order to allow the Java program to send messages to a Rexx object, we return a Rexx object when this
Rexx script runs.
*/


return .test~new        -- return a Rexx object

::class test            -- a Rexx class
::method one            -- a Rexx method
  return 'one (from Rexx)'

::method two            -- a Rexx method
  use arg a, b, c, d       -- get (Java) arguments
  say "'d~scriptContext' is the ScriptContext of this invocation:" pp(d~scriptContext)

  return '->' a '->' b '->' c '!'  "(from Rexx)"   -- return the concatenated values

::method now            -- a Rexx method
  return  .DateTime~new~string "(from Rexx)"       -- return the string value

::routine routine_eins public -- a public routine
  return "return value from the public Rexx 'routine_eins'"

::routine routine_zwei public -- a public routine
  use arg a, b, c
  say "'arg(4)~scriptContext' is the ScriptContext of this invocation:" pp(arg(4)~scriptContext)
  return '->' a '->' b '->' c '!'  "(from Rexx)"   -- return the concatenated values

::routine routine_drei private   -- a private routine
  return "return value from the public Rexx 'routine_drei'"

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
