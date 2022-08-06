/*
------------------------ Apache Version 2.0 license -------------------------
   Copyright 2022 Rony G. Flatscher

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

   date:    2022-02-06
   purpose: defines BSF4ooRexx as the module named "rexxla.oorexx" which provides
            the javax.script.ScriptEngineFactory service; once this gets part
            of the distribution it needs to be placed in the module's root directory

   log:     - rgf, 2022-01-27, re-experiment/refresh for creating module providing a
                               service eventually
            - rgf, 2022-01-29, test module with OpenJDK 11, run examples to find out
                               which modules are required and which ones need to be
                               exported
            - rgf, 2022-08-06, moved classes from org.rexxla.bsf.engines.rexx.jsr223
                               to org.rexxla.bsf.engines.rexx, adjust for it

   TODO/wished:
        - ASF's BSF: at ASF create and define module-info.java; then "use" it in here
        - Janino:    at Codehaus/git: create module-info.java and then "use" it in here

   to compile successfully use "-d .":

      javac -verbose -d . module-info.java
      javac -verbose -source 9 -target 9 -d . module-info.java

*/
module rexxla.oorexx {
    requires java.scripting;        // Java scripting module

    requires java.desktop;  // OpenJDK 11, because of: java.beans.PropertyChangeSupport (needed
                            // by org.apache.bsf.BSFManager)

        // service this module provides
    provides javax.script.ScriptEngineFactory with
             org.rexxla.bsf.engines.rexx.RexxScriptEngineFactory;

        // exported packages must contain at least a class (= "not empty")
    exports org.rexxla.bsf;         // allow access to ooRexx scripting support
    exports org.rexxla.bsf.engines.rexx;        // allow access to others

/* TODO: maybe third parties need access these as well ?
    exports org.oorexx.uno;         // UNO reflection for AOO/LO support
    exports org.oorexx.datergf;     // allow access to datergf demo classes (for samples)
    exports org.oorexx.misc;        // allow access e.g. to class OldGreekAlphabetEnum (for samples)
*/

        // problematic, cf. 'split package', however "requires"/"use"s it (also forces java.desktop to be required)
    exports org.apache.bsf;         // allow access to Apache BSF

}
