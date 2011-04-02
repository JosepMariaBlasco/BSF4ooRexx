package org.rexxla.bsf.engines.rexx;   // belongs to that fine package ...

import org.apache.bsf.*;   // BSF support
import java.io.*;       // exception handling

/** Java class to allow Rexx to invoke <code>BSF4ooRexx</code> via JNI, after the JVM was created.
 *  <p>
 *  <code>&quot;BSF4ooRexx.BsfLoadJava()&quot;</code> will create a JVM, load this class and
 *  then invoke the static method {@link #createInterface4Rexx()}. This method creates an instance
 *  of {@link BSFManager} and uses it to load an instance of the Rexx engine, which allows
 *  to initialize the <code>&quot;BSF4ooRexx&quot;</code> link library.
 *
 *  <p>
 *  To make sure that
 *  upon return from the static method {@link #createInterface4Rexx()} the reference
 *  to the Rexx engine object received via {@link RexxEngine#getRexxInterface()
 *  RexxEngine.getRexxInterface()}, a
 *  global reference is created via JNI while executing the method
 *  {@link RexxAndJava#jniInitialize4Rexx() RexxAndJava.jniInitialize4Rexx()}, which
 *  calls into the BSF4ooRexx-DLL.
 *
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2001-2009 Rony G. Flatscher
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ----------------------------------------------------------------------------- </pre>
 *
 *
 * @author Rony G. Flatscher (<a href="http://www.wu-wien.ac.at">WU-Wien/Wirtschaftsuniversit&auml;t Wien</a>, <a href="http://www.wu-wien.ac.at/english">http://www.wu-wien.ac.at/english</a>)
  * @version 4.0 (needs new APIs), 2009-05-31
 */
class Java4Rexx {
    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static public String version = "400.20091019";


  /**  BSF-manager to be used to get (instantiate) the Rexx engine. */
  static private BSFManager bsfMgr=null;

  /** The Rexx engine loaded via {@link #bsfMgr}. */
  // static private BSFEngine  rxEngine=null;
  static private RexxEngine  rxEngine=null;

  /** This method allows Rexx to load Java, then initialize
   *  the BSF4ooRexx infrastructure from the Java side, and
   *  thereafter use all of the infrastructure from Rexx.
   *
   * <p> Hint: JNI is able to get to private/protected members of a class! This way this method
   * can <em>only</em> be invoked by JNI.
   *
    */
  private static void createInterface4Rexx() throws IOException {

    try {
        bsfMgr  = new BSFManager ();   // create and initialize a BSFManager
        // this will load BSF4ooRexx.dll from the Java side and initialize
        // rxEngine = bsfMgr.loadScriptingEngine("rexx");   // now the Rexx Engine and its supporting class is available
        rxEngine =  (RexxEngine) bsfMgr.loadScriptingEngine("rexx");   // now the Rexx Engine and its supporting class is available
    }
    catch (BSFException e)
    {
      e.printStackTrace();
    }

    // for BSF4ooRexx using the 4.0 APIs it is important to save the Rexx interpreter instance ID which
    // loaded Java and thereby invoking this method (which initializeds the BSF4ooRexx infrastructure on
    // the Java side

        // now call into the DLL ("BSF4ooRexx.dll") in order to initialize; will set JVM in BSF4ooRexx.cc
    RexxAndJava raj=rxEngine.getRexxInterface();    // get the interface instance
    raj.jniInitialize4Rexx();                       // initialize Rexx, creates another Interpreter instance

    String root_rii_ID=raj.jniGetRexxInterpreterInstanceRoot(); // get root of interpreter instances (defined in BsfLoader())
    String rii_ID=rxEngine.get_rii_ID();

// System.err.println("Java4Rexx.java: BSFGetTID()=["+raj.jniRexxGetTID()+"]");


    raj.jniRexxTerminateInterpreterInstance(rii_ID); // terminate spurious Rexx interpreter instance

    rxEngine.set_rii_ID(root_rii_ID);   // now tell engine instance which Rexx interpreter instance to use

  }
}

