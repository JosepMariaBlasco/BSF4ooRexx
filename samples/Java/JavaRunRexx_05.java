/*
    Author:     Rony G. Flatscher
    Date:       2014-07-04
    Purpose:    Demonstrate how to setup a .host directory in Rexx from Java, which then
                receives Java objects for use in Rexx programs; in addition demonstrates
                turning all .host directory entries to environment symbols by merely putting
                its entries into .local; finally, a value is saved in .host by Rexx under
                the index "rexx.hey" that upon return is fetched from Java.

    Changed:    - 2022-01-29, rgf: expicitly do a System.exit(0)

    Needs:      ooRexx 4.1, BSF4ooRexx

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

import java.util.Vector;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.BSFEngine;
import org.rexxla.bsf.engines.rexx.RexxProxy;

public class JavaRunRexx_05
{
    public static void main (String args[]) throws BSFException
    {
        BSFManager mgr       =new BSFManager();     // create an instance of BSFManager
        BSFEngine  rexxEngine=mgr.loadScriptingEngine("rexx");  // load the Rexx engine

        System.out.println("--- 1 (Java): create Rexx directory object named 'HOST', save it in .LOCAL and return it to Java ...");
            // Rexx code to run
        String rexxCode= ".local~host=.directory~new    ;" + // create an ooRexx directory object
                         "return .host                  ;" + // return the Rexx directory object to Java
                         "::requires 'BSF.CLS'          ;" ; // get access to BSF4ooRexx support

        RexxProxy rexxDotHost = (RexxProxy) rexxEngine.apply ("InvokeFromJavaRunRexx_05.java-01", 0, 0, rexxCode, null, null);

        System.out.println("--- 2 (Java): add entries from Java to the received '.HOST' directory ...");
            // add values to the returned Rexx directory object
        rexxDotHost.sendMessage2("setEntry", "from.java",                "This is a value set by a Java program!");
        rexxDotHost.sendMessage2("setEntry", "FROM.JAVA.SYSTEM.CLASS",   System.class);
        rexxDotHost.sendMessage2("setEntry", "FrOm.JaVa.VeRsIoN.StRiNg", System.getProperty("java.version"));

        rexxCode="use arg val1, val2, val3                                                          ;" +
                 "say ' (ooRexx) val1='pp(val1) 'val2='pp(val2) 'val3='pp(val3) 'arg()='pp(arg())   ;" +
                 "say ' (ooRexx) .host~from.java               :' pp(.host~from.java               );" +
                 "say ' (ooRexx) .host~from.java.system.class  :' pp(.host~from.java.system.class  );" +
                 "say ' (ooRexx) .host~from.java.version.string:' pp(.host~from.java.version.string);" +
                 "say ' (ooRexx) Java version queried from Rexx:' pp(.host~from.java.system.class~getProperty('java.version')); " +
                 "::requires 'BSF.CLS'                                                             ;"  ; // get access to BSF4ooRexx support

        Vector vArgs = new Vector();    // Vector for arguments to be passed to Rexx
        vArgs.addElement(new java.util.GregorianCalendar());
        vArgs.addElement(null);
        rexxEngine.apply ("InvokeFromJavaRunRexx_05.java-02", 0, 0, rexxCode, null, vArgs);

        System.out.println("--- 3 (Java): access '.HOST' entry 'REXX.HEY' ...");
        System.out.println(" (Java) rexxDotHost.sendMessage0(\"rexx.hey\"): ["+
                             rexxDotHost.sendMessage0("rexx.hey")+"]");
        System.out.println(" (Java) rexxDotHost.sendMessage1(\"entry\", \"rexx.hey\"): ["+
                             rexxDotHost.sendMessage1("entry", "rexx.hey")+"]");

        mgr.terminate();    // make sure that the Rexx interpreter instance gets terminated!
        System.exit(0);
    }
}

