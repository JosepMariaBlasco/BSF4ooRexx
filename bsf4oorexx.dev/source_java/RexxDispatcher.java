package org.rexxla.bsf;
import org.rexxla.bsf.engines.rexx.RexxException;
import org.rexxla.bsf.engines.rexx.RexxEngine;

import java.io.*;

/** Invokes given Rexx or ooRexx program and supplies the commandline arguments.
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2006-2011 Rony G. Flatscher
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
 * @author   Rony G. Flatscher
 * @version  402.20120606
 */

 /*
     last change: $Revision: 546 $ $Author: rony $ $Date: 2009-10-20 20:45:19 +0200 (Tue, 20 Oct 2009) $

     2012-02-06, ---rgf, - terminate() Rexx engine to inhibit callbacks from Java to Rexx

     2011-02-15, ---rgf, - adding support for MacOSX: Rexx' filename will be used in application title,
                           which gets set to "BSF4ooRexx [rexx-filename]"

     2009-10-10, ---rgf, - removed rexxengine.halt(), which raises the HALT condition in all Rexx threads

     2009-06-08, ---rgf, - added explicit rexxengine.terminate()
                         - checking, if file exists, aborting with a meaningful error message, if file
                           does not exist

     2009-05-27, ---rgf, using the new ooRexx 4.0 APIs in the BSF4Rexx 4.0 support

     2008-06-14, ---rgf, supply fully qualified (canonical) path of script to execute

     2006-01-28, ---rgf, fixed bug (arguments not passed on to Rexx), reporting the Rexx execution
                         error)

     2006-01-07, ---rgf, removed ability to look for com.ibm.bsf, if org.apache.bsf is not
                         found (to reduce confusion on error reports)
 */
public class RexxDispatcher {
        /** Version string indicating version of this class (majorVersion*100+minorVersion
         *  concatenated with a dot and the sorted date of last change.
         */
        static final public String version = "402.20120606";

        /** Key for storing the received args-String array in the BSF registry for Rexx to
         *  get at it.
         */
        static final private String keyName="allCommandLineArguments";

        private RexxDispatcher() {};   // do not allow to create an instance of this class

	/**
	 * Dispatches the Rexx/ooRexx program in args[0] and supplies it with the argument,
         * assembled by concatenating all remaining arguments with a blank. In addition the
         * String array <code>args[]</code> is saved in the BSF registry under the name
         * &quot;allCommandLineArguments&quot;.
	 *
	 * @param args arguments
	 *
	 */
	public static void main(String[] args)
        {
            if (args.length == 0)
            {
                    System.err.println("usage:\n\tjava RexxDispatcher rexx_file_name [arguments]");
                    System.exit(1);
            }

            // rgf, 2011-02-20: if running under MacOSX set Apple-Java-related properties
            if (System.getProperty("os.name").startsWith("Mac"))
            {
                    System.setProperty("apple.laf.useScreenMenuBar", "true");
                    // display supplied file name of Rexx script that gets executed
                    String name=args[0];
                    int lastIndex=name.lastIndexOf("/");

                    if (lastIndex >= 0 && lastIndex+1 < name.length())
                    {
                        name=name.substring(lastIndex+1);
                    }
                    System.setProperty("com.apple.mrj.application.apple.menu.about.name","BSF4ooRexx ["+name+"]");
            }

            String [] args4rexx    = null;
            String    argline      = null;
            java.util.Vector vArgs = null;

            Object scriptString    = null;
            org.apache.bsf.BSFManager ap_mgr  = null;   // Apache's bsf, Java >= 1.3

            // org.apache.bsf.BSFEngine  ap_rexx = null;   // Apache's bsf, Java >= 1.3
            org.rexxla.bsf.engines.rexx.RexxEngine ap_rexx = null;   // Apache's bsf, Java >= 1.3

            try     // access to Apache's BSF there ?
            {
                ap_mgr  = new org.apache.bsf.BSFManager ();
                ap_mgr.declareBean(keyName, args, args.getClass());    // save args[] in registry
                ap_rexx = (RexxEngine) ap_mgr.loadScriptingEngine("rexx");
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(1);
            }


                // if additional arguments, pass them as a blank delimited string to Rexx
            if (args.length>1)
            {
                vArgs=new java.util.Vector();
                argline="";
                boolean bEnquote=false;

                for (int i=1; i<args.length; i++)
                {
                    argline=argline + (argline.length()>0 ? " " : "");  // append a blank, if not first argument

                    // if an argument has a space re-enquote it!
                    bEnquote=false;
                    for (int k=0; k<args[i].length() && bEnquote==false; k++)
                    {
                        bEnquote=(args[i].charAt(k)==' ');
                    }
                    argline=argline + (bEnquote==true ? "\"" : "") + args[i] + (bEnquote==true ? "\"" : "");
                }
                vArgs.addElement(argline);      // add concatenated String
            }

            try
            {
                java.io.File file=new java.io.File(args[0]);    // create fileObject

                if (!file.exists())
                {
                    System.err.println("RexxDispatcher.java: file ["+args[0]+"] does not exist, aborting...");
                    System.exit(-1);
                }
                    // execute Rexx script
                Object obj =ap_rexx.apply( file.getCanonicalPath(),
                                        0,
                                        0,
                                        (Object) org.apache.bsf.util.IOUtils.getStringFromReader(new FileReader(file)),
                                        null,
                                        vArgs
                                      );

            }
            catch (Throwable t)
            {
                System.err.println("RexxDispatcher.java: Throwable of type '"+t.getClass().getName()+"' thrown while invoking Rexx:\ngetLocalizedMessage(): ["+t.getLocalizedMessage()+"]");
                try
                {
                    ap_rexx.terminate();    // try to terminate the Rexx instance
                }
                catch (Throwable t0) {}
                System.exit(-1);
            }
            finally
            {
                try
                {
                    // it may be the case that at this point in time there are still Rexx threads
                    // executing; once could sleep to allow them to end, but this is not acceptable
                    // for a general purpose dispatcher; a sleep statement may look like:
                    // try { Thread.sleep(750); } catch (Exception e) {}
                    ap_rexx.terminate();    // wait for termination of the Rexx instance
                }
                catch (Throwable t2)
                {
                    if (! (t2 instanceof RexxException))    // ignore RexxException at terminate()
                    {
                        System.err.println("\nRexxDispatcher.java: a '"+t2.getClass().getName()+"' thrown while terminating Rexx instance: ["+t2.getLocalizedMessage()+"]");
                        t2.printStackTrace();
                    }
                }
            }

            System.gc();    // run the garbage collector, such that Java objects with finalizers get a chance to run
            System.runFinalization();   // run outstanding finalizers (e.g. from RexxProxy objects), if any
            ap_rexx.terminate(); // terminate Rexx engine, will inhibit callbacks from Java to Rexx
            System.exit(0); // make sure JVM does not crash (there was a bug, if using JNI)
	}
}



