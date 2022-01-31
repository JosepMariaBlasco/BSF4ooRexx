/*
    Author:     Rony G. Flatscher
    Date:       2014-07-04, 2014-07-11
    Purpose:    Demonstrate how to run Rexx programs one after the other, using the
                package (public routines, public classes) for the next Rexx invocation;
                this allows one to run Rexx programs and aggregate all public routines
                and public classes

    Changed:    - 2014-07-11, ---rgf: demonstrate redirecting ooRexx standard streams to Java
                - 2018-01-03, ---rgf: make sure code argument is submitted as a Rexx array
                - 2022-01-29, rgf: expicitly do a System.exit(0)

    Needs:      ooRexx 4.0, BSF4ooRexx

    License:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2014-2022 Rony G. Flatscher

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
import org.rexxla.bsf.engines.rexx.RexxProxy;

public class JavaRunRexx_06
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        BSFEngine  rexxEngine=mgr.loadScriptingEngine("rexx");  // load the Rexx engine

            // create a Rexx routine (not using any other context)
        String createRoutine           ="return .routine~new(arg(1),arg(2)~makearray)";
            // create a Rexx routine, using context of third argument
        String createRoutineWithContext="return .routine~new(arg(1),arg(2)~makearray,arg(3))";

            // require BSF.CLS, which will cause an entry in .local named BSF, controlling argument conversion between Java and Rexx
        System.out.println("--- 1 (Java): require BSF.CLS and keep routine in order to be able to access its package,\n" +
                           "              redirect ooRexx standard streams to the standard Java streams...");
        String rexxCode="call bsf.redirectTo 'Java'     -- redirect ooRexx' standard streams to Java streams \n" +
                        "::requires BSF.CLS             -- get and activate the Java support                 \n";

        Vector    vArgs=new Vector();
        vArgs.addElement("rexxPackage1");    // routine/package name
        vArgs.addElement(rexxCode);          // Rexx code
        RexxProxy rp1 = (RexxProxy) rexxEngine.apply ("JavaRunRexx_Test_2-01", 0, 0, createRoutine, null, vArgs);
        rp1.sendMessage0("call");   // call (execute) the Rexx routine


        System.out.println("--- 2 (Java): define and run Rexx package2 which defines a public method 'test()'...");
            // Rexx code to run
        rexxCode= "parse source fn                                               ;\n" +
                  "say pp(fn) ;\n" +
                  "say pp(fn) 'test():' pp(test()) -- pp() is defined in BSF.CLS ;\n" +
                  "::routine test public                                         ;\n" +
                  "  return 'from routine test() at' .DateTime~new               ;" ;

        Vector    vArgs1=new Vector();
                  vArgs1.addElement("rexxPackage2");    // routine/package name
                  vArgs1.addElement(rexxCode);          // Rexx code
                  vArgs1.addElement(rp1);               // context to use
        RexxProxy rp2 = (RexxProxy) rexxEngine.apply ("JavaRunRexx_Test_2-02", 0, 0, createRoutineWithContext, null, vArgs1);
        rp2.sendMessage0("call");   // call (execute) the Rexx routine


        System.out.println("--- 3a (Java): run Rexx code with NO arguments that uses public method 'test()' defined for prior run...");
            // create a Rexx routine
        rexxCode= "parse source fn                                               ;\n" +
                  "say pp(fn) 'received' arg() 'argument(s)'                     ;\n" +
                  "say pp(fn) 'test():' pp(test()) -- pp() is defined in BSF.CLS ;\n" ;
        Vector    vArgs2=new Vector();
                  vArgs2.addElement("rexxPackage3");    // routine/package name
                  vArgs2.addElement(rexxCode);          // Rexx code for routine object
                  vArgs2.addElement(rp2);               // context to use
        RexxProxy rp3 = (RexxProxy) rexxEngine.apply ("JavaRunRexx_Test-2_03", 0, 0, createRoutineWithContext, null, vArgs2);
        rp3.sendMessage0("call");   // call (execute) the Rexx routine

        System.out.println("--- 3b (Java): run Rexx code WITH arguments that uses public method 'test()' defined for prior run...");
            // reuse routine/package
        rp3.sendMessage2("call", "argument from Java", new Object());  // call (execute) the Rexx routine with arguments

        mgr.terminate();    // make sure that the Rexx interpreter instance gets terminated!
        System.exit(0);
    }
}


