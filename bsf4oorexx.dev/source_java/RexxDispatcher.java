package org.rexxla.bsf;
import org.rexxla.bsf.engines.rexx.RexxException;
import org.rexxla.bsf.engines.rexx.RexxEngine;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/** Invokes given Rexx or ooRexx program and supplies the commandline arguments.
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2006-2026 Rony G. Flatscher
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
 * @version  405.20260114
 */

 /*
     last change: $Revision: 546 $ $Author: rony $ $Date: 2009-10-20 20:45:19 +0200 (Tue, 20 Oct 2009) $

     2026-01-14, ---rgf: - if the Rexx script is not found, then try with appended ".rex",
                           ".rexx", ".rxj", ".rxo", ".cls"
                         - improve error message to be more comprehensible/legible
                         - make sure that we only check, if full path was not yet tested to
                           prevent loops if environment has recursive directories

     2024-07-07, ---rgf: - if the Rexx script is not found in the current directory, then use
                           java.library.path to locate it and if found there use its canonical path
                           representation

     2023-10-31, ---rgf: - do not do a gc() and runFinalization() which may become obsolete
                           in the future

     2021-08-04, ---rgf: - Object.finalize() is slated to be removed from a future version of Java,
                           possibly also System.runFinalization(), cater for it

     2016-11-05, ---rgf: - basing on 1.6/6.0, get rid of generic warnings

     2013-01-07, ---rgf, - somehow I mangled a local copy of this file, introducing an error,
                           reordering Rexx termination sequences

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
        static final public String version = "405.20260114";

        /** Determine whether debug output should be given.
         */
        static final private boolean bDebug = false;    // true: debug output; false: no debug output

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
            java.util.Vector<String> vArgs = null;

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
                vArgs=new java.util.Vector<String>();
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
                String rexxProgram = findFile(args[0]);

                if (rexxProgram==null)
                {
                    System.err.println("RexxDispatcher.java: the supplied Rexx program \""+args[0]+"\" can neither be found in the current directory (\".\", ie., \""
                                       + System.getProperty("user.dir") + "\") "
                                       + "nor in 'java.library.path':\n---> \""+System.getProperty("java.library.path")+"\" <---, aborting...");
                    System.exit(-1);
                }
                    // execute Rexx script
// System.err.println("*** RexxDispatcher: # 161, before apply() of ["+file.getCanonicalPath()+"] ...");
                Object obj =ap_rexx.apply( rexxProgram,
                                        0,
                                        0,
                                        (Object) org.apache.bsf.util.IOUtils.getStringFromReader(new FileReader(rexxProgram)),
                                        null,
                                        vArgs
                                      );

// System.err.println("*** RexxDispatcher: # 170, after apply() ...");
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
// System.err.println("*** RexxDispatcher: # 202, before ap_rexx.terminate() ...");
                    ap_rexx.terminate();    // wait for termination of the Rexx instance
// System.err.println("*** RexxDispatcher: # 204, after ap_rexx.terminate() ...");
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

// System.err.println("*** RexxDispatcher: # 215, before exit() ...");
            System.exit(0); // make sure JVM does not crash (there was a bug, if using JNI)
	}



        /** This method tries to locate the supplied Rexx program in the
         *  following locations: current directory and then in each directory
         *  listed with "java.library.path"; if found it returns the canonical
         *  path.
         *
         *  @param fileName the Rexx program to find
         *  @return the canonical path of the Rexx program if found on java.library.path, if not found return null
         */
        static String findFile(String fileName)     // find and return file
        {
            File tmpFile=new File(fileName);
            try
            {
                if (tmpFile.exists())
                {
                    return tmpFile.getCanonicalPath();
                }
            }
            catch (IOException ioe) {}

                // not found, search along java.library.path
            String fileSeparator = System.getProperty("file.separator");
            String arrPaths[] = System.getProperty("java.library.path").
                                       split(System.getProperty("path.separator"));

            // .rxj Rexx program needs Java; .rxo: Rexx program needs Java and OpenOffice/LibreOffice
            String suffixes[] = { "", ".rex", ".rexx", ".rxj", ".rxo", ".cls" };

            // remember tested paths
            HashSet<String> seenPaths = new HashSet<>();

            for (String suff : suffixes)
            {
                for (String dir : arrPaths) // iterate over each path
                {
                    String fullPath=dir+fileSeparator+fileName+suff; // create fully qualified name

                    if (seenPaths.add(fullPath))    // if not yet tested
                    {
                        if (bDebug)
                        {
                            System.err.println("\t checking ---> ["+fullPath+"]...");
                        }
                        tmpFile=new File(fullPath);
                        if (tmpFile.exists())
                        {
                            try
                            {
                                if (bDebug)
                                {
                                    System.err.println("\t BINGO!!! fullPath=["+fullPath+"],"
                                                      +"\t         returning ["+tmpFile.getCanonicalPath()+"]");
                                }
                                return tmpFile.getCanonicalPath();  // return canonical path
                            }
                            catch (IOException ioe) {}
                        }
                    }
                }
            }
            return null;    // not found
        }
}



