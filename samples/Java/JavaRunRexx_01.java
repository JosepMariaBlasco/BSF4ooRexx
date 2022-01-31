/*
    Author:     Rony G. Flatscher
    Date:       2009-10-20
    Purpose:    Demonstrate how to invoke an ooRexx program and supply it
                a Java object to interact with pointing at a subtlety

                - as BSF.CLS has not been called prior to invoking the Rexx program, the
                  Java arguments are not prepended with the type indicator "<S>" for strings
                  and "<O>" for Java objects; therefore, if the Rexx program uses the routine
                  bsf.wrap() to turn a Java object reference into a BSF object, one must prepend
                  the beanName with the type indicator "<O>"

    Changed:    - 2013-06-13, demonstrate that one should use BSFManager's terminate() method,
                            if that particular Rexx interpreter instance is not needed anymore,
                            such that its reserved system resources can be reclaimed

                - 2022-01-29, rgf: expicitly do a System.exit(0)

    Needs:      ooRexx 4.1, BSF4ooRexx

    License:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2009-2022 Rony G. Flatscher

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

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.BSFEngine;
import java.util.Vector;

public class JavaRunRexx_01
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        BSFEngine  rexxEngine=mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // Rexx code to run
        String rexxCode= "parse arg beanName                        ;" +    // retrieve the beanName (index into BSFRegistry)
                         "say 'beanName        :' beanName          ;" +    // send Java object a message, show result
                         "javaObj=bsf.wrap('<O>' || beanName)       ;" +    // turn into an ooRexx BSF-object
                         "say 'javaObj~class   :' javaObj~class     ;" +    // send Java object a message, show result
                         "say 'javaObj~toString:' javaObj~toString  ;" +    // send Java object a message, show result

                         "::requires BSF.CLS                        ;" ;    // get ooRexx support (camouflage Java as ooRexx)

        // argument for Rexx
        Vector vArgs=new Vector();
        vArgs.addElement( System.getProperties() );     // get the current Java properties

        rexxEngine.apply ("InvokeFromJavaRunRexx_01.java", 0, 0, rexxCode, null, vArgs);

        mgr.terminate();    // make sure that the Rexx interpreter instance gets terminated!
        System.exit(0);
    }
}
