package org.rexxla.bsf.engines.rexx;   // belongs to that fine package ;) ...

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
 *  calls into the BSF4ooRexx library.
 *
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2001-2022 Rony G. Flatscher
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
  * @version 4.5.2
 */

/* changes:
    - 2012-02-04, rgf: cater for new (deferred) logic of creating a Rexx interpreter instance
    - 2012-02-06, rgf: supply new (default) RexxConfiguration object for creating Rexx interpreter instance
    - 2012-02-09, rgf: need to use try-block for createRexxInterpreterInstance() as it now throws a BSFException
    - 2012-04-17, rgf: change logic as there is no need for creating and terminating another Rexx
                       interpreter instance anymore (there was a bug too, that caused Terminate()
                       to not be invoked at all)
    - 2015-08-04, rgf: split logic to allow Java4Rexx to sucessfully set the Rexx interpreter ids (rii_IDs) in
                       RexxEngine and its companion RexxAndJava instance
    - 2022-01-29, rgf: in anticipation that eventually access to private members from JNI will get
                       prohibited; package private is safe enough anyway
    - 2022-08-01, rgf: logic in RexxEngine.initializer(...) got changed such that the ooRexx version
                       strings gets fetched and Termination() related defaults set accordingly using
                       apply which will cause the primodal Rexx interpreter to be created; as a
                       result rii_ID for the peer RAJ and RE get set such that Java4Rexx must not
                       set them again (which would cause an exception in RAJ)
*/

class Java4Rexx {
    /** Version string indicating version of this class. */
    static public String version = "641.20220801";

    final static boolean bDebug= // true;
                                 false;


  /**  BSF-manager to be used to get (instantiate) the Rexx engine. */
  static BSFManager bsfMgr=null;

  /** The Rexx engine loaded via {@link #bsfMgr}. */
  // static private BSFEngine  rxEngine=null;
  static RexxEngine  rxEngine=null;

  /** This method allows Rexx to load Java, then initialize
   *  the BSF4ooRexx infrastructure from the Java side, and
   *  thereafter use all of the infrastructure from Rexx.
   *
   * <p> Hint: JNI usually is able to get to private/protected members of a class, therefore
   *           this non-public method can be invoked by JNI.
   *
   */
  static void createInterface4Rexx() throws IOException {

    try {
        bsfMgr  = new BSFManager ();   // create and initialize a BSFManager
        // this will load BSF4ooRexx.dll from the Java side and initialize
        rxEngine =  (RexxEngine) bsfMgr.loadScriptingEngine("rexx");   // now the Rexx Engine and its supporting class is available
    }
    catch (BSFException e)
    {
      e.printStackTrace();
      // throw new RuntimeError("Java4Rexx.createInterface4Rexx(): cannot load the scripting engine \"rexx\"");
    }


    // for BSF4ooRexx using the 4.0 APIs it is important to save the Rexx interpreter instance ID which
    // loaded Java and thereby invoking this method (which initializeds the BSF4ooRexx infrastructure on
    // the Java side

        // now call into the DLL ("BSF4ooRexx.dll") in order to initialize; will set JVM in BSF4ooRexx.cc
    RexxAndJava raj=rxEngine.getRexxInterface();    // get the RexxAndJava (i.e. interface) instance
    raj.jniInitialize4Rexx();           // initialize BSF4ooRexx (will cache interface instance)

if (bDebug) System.err.println("///\\\\\\ Java4Rexx.createInterface4Rexx() ///\\\\\\  raj=["+raj+"], rxEngine=["+rxEngine+"], bsfMgr=["+bsfMgr+"] ///\\\\\\ ");

  }

  /** After {@link createInterfaceRexx} runs, the root Rexx interpreter instance (RII) will be
   *  saved in the native code. This static method allows the native code to initialize the rii_ID fields
   *  in RexxEngine and its RexxAndJava peer instance to be set.
   *
   * @since 2015-08-04
   */
  static void set_rii_ids ()
  {
          // now call into the DLL ("BSF4ooRexx.dll") in order to initialize; will set JVM in BSF4ooRexx.cc
      RexxAndJava raj=null;
      String root_rii_ID=null;
      try {
          raj=rxEngine.getRexxInterface();    // get the RexxAndJava (i.e. interface) instance
          root_rii_ID=raj.jniGetRexxInterpreterInstanceRoot(); // get reference to Rexx interpreter instance
        /* 2022-08-01: RexxEngine.initialize(...) now will use the primodal instance for
                       retrieving the ooRexx version string information and determining
                       the default values for Termination() depending on the ooRexx version;
                       it uses apply() which will create and set the rii_ID for the root
                       instance in which case set_rii_ID() must not be called
        */
          String raj_rii_ID=raj.get_rii_ID();
          if (raj_rii_ID==null)
          {
              raj.set_rii_ID(root_rii_ID);        // tell this RAJ instance to use root instance
          }
          else if (! raj_rii_ID.equals(root_rii_ID))    // rii_ID already set, but not the primodal one?
          {
              throw new BSFException(BSFException.REASON_EXECUTION_ERROR,
                                     "Java4Rexx: SEVERE ERROR! Root instance (root_rii_ID="+root_rii_ID+
                                     ") is not equal to already set raj_rii_ID ("+raj_rii_ID+"), "+
                                     "rxEngine.get_rii_ID()=["+rxEngine.get_rii_ID()+"]!");
          }

          String re_rii_ID=rxEngine.get_rii_ID();

          if (re_rii_ID==null)                    // tell this RE instance to use root instance
          {
              rxEngine.set_rii_ID(root_rii_ID);   // tell RE instance to use root instance
          }
          else if (! re_rii_ID.equals(root_rii_ID))     // rii_ID already set, but not the primodal one?
          {
              throw new BSFException(BSFException.REASON_EXECUTION_ERROR,
                                     "Java4Rexx: SEVERE ERROR! rxEngine.rii_ID ("+re_rii_ID+
                                     ") is not set to root rii_ID ("+root_rii_ID+")!");
          }
      }
      catch (BSFException e)
      {
        e.printStackTrace();
        // throw new RuntimeError("Java4Rexx.set_rii_ids(): exception while trying to set the RII IDs");
      }

if (bDebug)   System.err.println("///\\\\\\ Java4Rexx.set_rii_ids()  ///\\\\\\  raj=["+raj+"], rxEngine=["+rxEngine+"], bsfMgr=["+bsfMgr+"] root_rii_ID=["+root_rii_ID+"] ///\\\\\\ ");
  }

}

