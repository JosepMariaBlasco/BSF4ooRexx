/*
    Author:     Rony G. Flatscher
    Date:       2009-10-20
    Purpose:    Demonstrate how to invoke an ooRexx program and supply it
                a Java object to interact with pointing at a subtlety

                - this version first calls Rexx causing the requires-processing
                  for BSF.CLS, which has two consequences:

                  - each subsequent invocation of a Rexx program will therefore have
                    immediate access to all of BSF.CLS features

                  - the native layer will be able to automatically turn Java objects
                    supplied as arguments into ooRexx BSF objects, allowing the Rexx
                    code to become simpler

    Needs:      ooRexx 4.0, BSF4ooRexx

    License:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2009 Rony G. Flatscher

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

public class JavaRunRexx_02
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        BSFEngine  rexxEngine=mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // Rexx code to run (just one line), triggering the requires-processing for "BSF.CLS"
        rexxEngine.apply ("InvokeFromJavaRunRexx_02.java", 0, 0, "::requires BSF.CLS", null, null);

        // Rexx code to run
        String rexxCode= "use arg javaObj                           ;" +    // retrieve the beanName (index into BSFRegistry)
                         "say 'javaObj~class   :' javaObj~class     ;" +    // send Java object a message, show result
                         "say 'javaObj~toString:' javaObj~toString  ;" +    // send Java object a message, show result

                         "::requires BSF.CLS                        ;" ;    // get ooRexx support (camouflage Java as ooRexx)

        // argument for Rexx
        Vector vArgs=new Vector();
        vArgs.addElement( System.getProperties() );     // get the current Java properties

        rexxEngine.apply ("InvokeFromJavaRunRexx_02.java", 0, 0, rexxCode, null, vArgs);
    }
}
