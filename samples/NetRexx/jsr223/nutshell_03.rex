/*
   Rexx companion script that gets loaded and evaluated by Nutshell_03_Interface.java:

   /** Demonstrate the usage of the <code>Invocable</code> methods, this time using Java proxies
    *  for executing Java interface methods either with Rexx routines or with Rexx objects.
    *
    *  @author Rony G. Flatscher
    *  @since 2015-05-12
    */

In order to allow the Java program to use a Rexx object for implementing Java interface methods,
we return a Rexx object when this Rexx script runs.

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

parse source . . thisFile
say "(from Rexx) source:" pp(thisFile)", prolog code, tid="pp(BsfGetTid())
return .test~new     -- return Rexx object

::requires BSF.CLS

::routine run public -- a public routine to be used for the java.lang.Runnable implementation
  use arg slotDir
  say "(from Rexx) public routine 'RUN', tid="pp(BsfGetTid()) || -
                   ", slotDir~scriptContext:" pp(slotDir~scriptContext)


::class test         -- a Rexx class implementing the method of the Java interface class
::method run         -- a method routine to be used for the java.lang.Runnable implementation
  use arg slotDir
  say "(from Rexx)" pp(self) "method 'RUN', tid="pp(BsfGetTid()) || -
                    ", slotDir~scriptContext:" pp(slotDir~scriptContext)

