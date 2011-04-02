// rgf, 2009-09-10, 2009-10-16: test execution of Rexx scripts by concurrently
//                  creating and using Rexx engines

package rgf.F_multiThreading;

import org.rexxla.bsf.engines.rexx.RexxEngine;
import org.rexxla.bsf.engines.rexx.RexxProxy;

import org.apache.bsf.*;    // BSF support
import java.io.IOException; // exception handling
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

/** Test Java program which demonstrates how easy it is to invoke Rexx via BSF.
 * @author   Rony G. Flatscher, 2001-04-27, 2007-09-10, 2009-09-11
 */
public class TestNestedEngines_80 {
    static BSFManager  bm=null;             // BSFManager of the Rexx instance that invoked this Java program
    static RexxProxy   testUnit=null;       // to allow assertions
    static int         nrJavaThreads=0;     // determine number of Java threads
    static int         nrRexxThreads=0;     // determine number of Rexx threads
    static String      rexxCode=null;       // Rexx code to run
    static RexxProxy   rpArray=null;        // RexxProxy to allow storing TID in Rexx array object

    static final boolean bDebug=false;


    public TestNestedEngines_80(BSFManager bm, RexxProxy testUnit, int nrJavaThreads, int nrRexxThreads, String rexxCode, RexxProxy rpArray)
    {
            // save arguments in attributes
        TestNestedEngines_80.bm            =bm;
        TestNestedEngines_80.testUnit      =testUnit;
        TestNestedEngines_80.nrJavaThreads =nrJavaThreads;
        TestNestedEngines_80.nrRexxThreads =nrRexxThreads;
        TestNestedEngines_80.rexxCode      =rexxCode;
        TestNestedEngines_80.rpArray       =rpArray;
    }


  /** Creates number of desired threads, runs them. */
  public void dispatchRexxScripts() throws Exception
  {

      String      tmpResult="";
      Throwable   tmpTh=null;
      String      tmpthreadName=null;



      BSFEngine  rexx = bm.loadScriptingEngine("rexx");
        // create arrays of needed size
      Thread               t[]=new Thread[nrJavaThreads];
      ThreadedJNIRexxStart80 r[]=new ThreadedJNIRexxStart80[nrJavaThreads];

      for (int i=0;i<nrJavaThreads; i++)
      {
          r[i]=new ThreadedJNIRexxStart80();
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
    static private class ThreadedJNIRexxStart80 implements Runnable
    {
            // define thread local variables
        private static ThreadLocal tlThrowable = new ThreadLocal();
        private static ThreadLocal tlResult    = new ThreadLocal();
        private static ThreadLocal tlThreadName= new ThreadLocal();


        public void run ()  // invoke ooRexx on its own Java thread
        {


            String threadName=Thread.currentThread().getName();
            setThreadName(threadName);                        // save thread name
            try {
                rpArray.sendMessage1("APPEND", threadName);  // save this thread name in supplied RexxProxy array

                // define arguments for Rexx script
                Vector args = new Vector(4);
                args.add(TestNestedEngines_80.testUnit);
                args.add(rpArray);
                args.add(new Integer(nrRexxThreads));
                args.add(threadName);

                // create a new BSFManager, which will use a separate Rexx interpreter instance
                BSFManager bmNew=new BSFManager();
                // load the Rexx engine for this BSFManager instance
                // BSFEngine  rexx = bm.loadScriptingEngine("rexx");
                BSFEngine  rexx = bmNew.loadScriptingEngine("rexx");

                // execute the Rexx script
                Object result=rexx.apply ("rexx", 0, 0,
                                          "::requires BSF.CLS",  // make sure BSF.CLS is loaded for processing arguments in next invocation
                                          null,
                                          null);

                result=rexx.apply ("rexx", 0, 0,
                                   rexxCode.replaceAll("@threadName", (""+threadName)),
                                   null,
                                   args);

                tlResult.set(result);   // save result
            }
            catch (Throwable th) {
                // this.th=th;         // save Throwable
                tlThrowable.set(th);   // save Throwable
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
