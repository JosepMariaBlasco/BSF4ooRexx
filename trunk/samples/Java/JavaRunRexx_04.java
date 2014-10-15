/*
    Author:     Rony G. Flatscher
    Date:       2009-10-20
    Purpose:    Demonstrate how to fetch and interact with a Rexx condition object

                - first calls Rexx causing the requires-processing for BSF.CLS

                - then calls Rexx with a program that divides by 0, raising a condition in
                  Rexx, which is forwarded to Java by throwing a RexxException which contains
                  the Rexx condition object

                - then interacts with the Rexx condition object from Java to show some
                  of the condition information

    Changed:    2013-06-13, demonstrate that one should use BSFManager's terminate() method,
                            if that particular Rexx interpreter instance is not needed anymore,
                            such that its reserved system resources can be reclaimed

    Needs:      ooRexx 4.0, BSF4ooRexx

    License:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2009-2013 Rony G. Flatscher

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
import org.rexxla.bsf.engines.rexx.RexxException;

public class JavaRunRexx_04
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        BSFEngine  rexxEngine=mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        // Rexx code to run (just one line), triggering the requires-processing for "BSF.CLS"
        rexxEngine.apply ("InvokeFromJavaRunRexx_04.java", 0, 0, "::requires BSF.CLS", null, null);

        // Rexx code to run
        String rexxCode= "a=1                                         \n" +
                         "b=0                                         \n" +
                         "say a/b  -- cause a syntax error at runtime \n" + // divide by 0 condition

                         "::requires BSF.CLS                            " ; // get ooRexx support (camouflage Java as ooRexx)

        try
        {
            RexxProxy rp = (RexxProxy) rexxEngine.apply ("InvokeFromJavaRunRexx_04.java", 0, 0, rexxCode, null, null);
        }
        catch (RexxException re)
        {
            System.out.println("Rexx program threw an exception:\n");
            RexxProxy rp=re.getRexxConditionObject();       // get the Rexx condition object

            System.out.println("    rp.sendMessage0(\"condition\")       : ["+rp.sendMessage0("condition")+"]");
            System.out.println("    rp.sendMessage0(\"code\")            : ["+rp.sendMessage0("code")+"]");
            System.out.println("    rp.sendMessage0(\"message\")         : ["+rp.sendMessage0("message")+"]");
            System.out.println("    rp.sendMessage0(\"program\")         : ["+rp.sendMessage0("program")+"]\n");

            RexxProxy traceBack=(RexxProxy) rp.sendMessage0("traceback");      // get the traceback list
            System.out.println("    traceBack.sendMessage0(\"firstItem\"): \n\t["+traceBack.sendMessage0("firstItem")+"]");
        }

        mgr.terminate();    // make sure that the Rexx interpreter instance gets terminated!
    }
}

