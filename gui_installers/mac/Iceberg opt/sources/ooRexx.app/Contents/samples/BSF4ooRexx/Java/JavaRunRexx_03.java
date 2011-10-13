/*
    Author:     Rony G. Flatscher
    Date:       2009-10-20
    Purpose:    Demonstrate how to interact with a Rexx object from Java

                - first calls Rexx causing the requires-processing for BSF.CLS

                - then calls Rexx and have it return a Rexx object (a directory)

                - then sends messages to the Rexx object from Java

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
import org.rexxla.bsf.engines.rexx.RexxProxy;

public class JavaRunRexx_03
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        BSFEngine  rexxEngine=mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // Rexx code to run (just one line), triggering the requires-processing for "BSF.CLS"
        rexxEngine.apply ("InvokeFromJavaRunRexx_03.java", 0, 0, "::requires BSF.CLS", null, null);

        // Rexx code to run
        String rexxCode= "d=.directory~new                                             ;" + // create an ooRexx directory object
                         "d~ooRexx='Open Object Rexx'                                  ;" + // add an entry
                         "d~BSF4ooRexx='Bean Scripting Framework for Open Object Rexx' ;" + // add another entry
                         "return d                                                     ;" + // return the Rexx object to Java

                         "::requires BSF.CLS                        ;" ;    // get ooRexx support (camouflage Java as ooRexx)

        RexxProxy rp = (RexxProxy) rexxEngine.apply ("InvokeFromJavaRunRexx_03.java", 0, 0, rexxCode, null, null);

        // send messages to the returned ooRexx object (a directory)
            // message without no argument
        System.out.println("rp~ooRexx              = ["+rp.sendMessage0("ooRexx")+"]");

            // messages with one argument
        System.out.println("rp~entry(\"ooRexx\")     = ["+rp.sendMessage1("entry", "ooRexx")+"]");
        System.out.println("rp[\"OOREXX\"]           = ["+rp.sendMessage1("[]",    "OOREXX")+"]\n");

        // message without no argument
        System.out.println("rp~BSF4ooRexx          = ["+rp.sendMessage0("Bsf4ooRexx")+"]");

        // messages with one argument
        System.out.println("rp~entry(\"BSF4ooRexx\") = ["+rp.sendMessage1("entry", "BSF4ooRexx")+"]");
        System.out.println("rp[\"BSF4OOREXX\"]       = ["+rp.sendMessage1("[]", "BSF4OOREXX")+"]");
    }
}

