// rgf, 2009-09-10, 2009-10-16: test execution of Rexx scripts by concurrently
//                  creating and using Rexx engines

package rgf.F_multiThreading;

import org.rexxla.bsf.engines.rexx.RexxProxy;

import org.apache.bsf.*;    // BSF support
import java.io.IOException; // exception handling
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

/** Test Java program which demonstrates how easy it is to invoke Rexx via BSF.
 * @author   Rony G. Flatscher, 2001-04-27, 2007-09-10, 2009-09-11, 2009-10-16
 */
public class TestNestedEngines {
    static BSFManager  bm=null;             // BSFManager of the Rexx instance that invoked this Java program
    static RexxProxy   testUnit=null;       // to allow assertions
    static int         nrJavaThreads=0;     // determine number of Java threads
    static int         nrRexxThreads=0;     // determine number of Rexx threads
    static String      rexxCode=null;       // Rexx code to run
    static RexxProxy   rpArray=null;        // RexxProxy to allow storing TID in Rexx array object


    public TestNestedEngines(BSFManager bm, RexxProxy testUnit, int nrJavaThreads, int nrRexxThreads, String rexxCode, RexxProxy rpArray)
    {
            // save arguments in attributes
        TestNestedEngines.bm            =bm;
        TestNestedEngines.testUnit      =testUnit;
        TestNestedEngines.nrJavaThreads =nrJavaThreads;
        TestNestedEngines.nrRexxThreads =nrRexxThreads;
        TestNestedEngines.rexxCode      =rexxCode;
        TestNestedEngines.rpArray       =rpArray;
    }


  /** Creates number of desired threads, runs them. */
  public void dispatchRexxScripts() throws Exception
  {

      String      tmpResult="";
      Throwable   tmpTh=null;
      String      tmpthreadName=null;

        // create arrays of needed size
      Thread               t[]=new Thread[nrJavaThreads];
      ThreadedJNIRexxStart r[]=new ThreadedJNIRexxStart[nrJavaThreads];

      for (int i=0;i<nrJavaThreads; i++)
      {
          r[i]=new ThreadedJNIRexxStart();
          t[i]=new Thread(r[i]);             // create thread
          r[i].setThreadName(t[i].getName());
          t[i].start();               // start execution on own thread
          // System.out.println("--> Java: just started thread ["+r[i].getThreadName()+"]...");System.out.flush();
      }
      // all got started

      for (int i=0;i<nrJavaThreads; i++)        // join all threads
      {
          t[i].join();
      }
      // all are finished
  }


        // Runnable inner class
    static private class ThreadedJNIRexxStart implements Runnable
    {
            // define thread local variables
        private static ThreadLocal tlThrowable = new ThreadLocal();
        private static ThreadLocal tlResult    = new ThreadLocal();
        private static ThreadLocal tlThreadName= new ThreadLocal();


        public void run ()  // invoke ooRexx on its own Java thread
        {
            try {
                String threadName=Thread.currentThread().getName();

                rpArray.sendMessage("APPEND", new Object [] {threadName} );  // save this thread name in supplied RexxProxy array

                // define arguments for Rexx script
                Vector args = new Vector(4);
                args.add(testUnit);
                args.add(rpArray);
                args.add(new Integer(nrRexxThreads));
                args.add(threadName);

                // load the Rexx engine for this BSFManager instance
                BSFEngine  rexx = bm.loadScriptingEngine("rexx");

                Object result=rexx.apply ("rexx", 0, 0,
                                          "::requires BSF.CLS",  // make sure BSF.CLS is loaded for processing arguments in next invocation
                                          null,
                                          args);

                // execute the Rexx script
                result=rexx.apply ("rexx", 0, 0,
                                   rexxCode.replaceAll("@threadName", (""+threadName)),
                                   null,
                                   args);

                tlResult.set(result);   // save result
            }
            catch (Throwable th) {
                // this.th=th;         // save Throwable
                tlThrowable.set(th);   // save Throwable
                System.err.println("TestNestedEngines: threadName=["+tlThreadName.get()+"], Throwable=["+th+"]");
                th.printStackTrace();
            }
        }

        public Object getResult() {         // getter
            return tlResult.get();
        }

        public Throwable getThrowable() {   // getter
            return (Throwable) tlThrowable.get();
        }

        public String getThreadName() {     // getter
            return (String) tlThreadName.get();
        }

        public void setThreadName(String threadName) {   // setter
            tlThreadName.set(threadName);
        }
    }
}
