package org.rexxla.bsf.engines.rexx;
// need to run javah to get the C-header include file for the native functions in this Java class

import java.lang.ref.PhantomReference ;
import java.lang.ref.ReferenceQueue ;
import java.util.IdentityHashMap ;
import java.util.Calendar;


/** With Java 9 Object.finalize() gets deprecated, therefore we use <code>PhantomReferences</code>
 *  for cleanup actions in these two cases:
 *
 * <ol>
 * <li>RexxAndJava.java: when the RexxEngine gets terminated we make sure that the
 *     peer RexxInterpreter gets terminated and removed from the list of available
 *     RexxInterpreter instances.
 *
 * <li>RexxProxy.java: whenever a RexxProxy object gets out of scope its reference
 *     count needs to be decreased such that eventually ooRexx can claim the peer
 *     Rexx object.
 *
 * </ol>
 *
 * <p>To not block termination of Rexx the cleanup thread does not use the blocking <code>remove()</code>
 * method on the reference queue, but rather <code>remove(timeout)</code> and terminates when the
 * queue is empty. To make sure that the cleanup gets carried out eventually, each time a new
 * instance of this class gets created the @link{RexxCleanupRef#clean()} method gets invoked, if
 * it is not running. From time to time the static method @link{RexxCleanupRef#clean()} gets invoked
 * by BSF4ooRexx directly in the <code>RexxAndJava</code> class.
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2021 Rony G. Flatscher
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
 * @version  100.20210805
*/

/*
    - 20210806, rgf: - make KindRef enum public

                     - add two-dimensional long array "counters" for counting instances,
                       finalizes grouped by enum values

                     - add public getter getCounters() to return a copy of "counters"

                     - added static main() method which tests RexxCleanupRef with
                       TestObjects, to run use the following comand:
                       java org.rexxla.bsf.engines.rexx.RexxCleanupRef

    - 20210807, rgf: - new public static methods "getStatistics()" and "getStatistics(title)"
                       which return a formatted string showing the current totals of instances,
                       finalizations, not yet finalized grouped by RefKind

                     - not needed anymore hence removed static countInstances and
                       countFinalizes with their getter methods

                     - new public static method getRefkinds which returns an array of the
                       current enum values of the RefKind enum class

*/

public class RexxCleanupRef extends PhantomReference
{
    /** If set to false the compiler should eradicate all dependent code. */
    private static final boolean bDebug=false; // true;


    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static final public String version = "100.20210807";

    /** Counts number of times the clean() thread got started so far. */
    private static int cleanInvocationCounter=0;

    /** Getter method.
     *  @return current number of clean threads started so far(depends on the implementation)
    */
    static public int getCleanInvocationCounter()
    {
        return cleanInvocationCounter;
    }

    /** This queue will get references enqueued, whenever the referred to Java object is not referenced anymore.
     */
    private final static ReferenceQueue refQueue=new ReferenceQueue();


    /** Learned that <code>PhantomReferences</code> must be rerenced themselves
     *  otherwise they may never be put on the reference queue, so storing them
     *  in this <code>IdentityHashMap</code> until the reference is being put on
     *  the reference queue for processing, at which time the reference will be
     *  removed from the <code>IdentityHashMap</code>. The <code>IdentityHashMap</code>
     *  gets used as <code>IdentityHashSet</code>, i.e. the value part will always
     *  be </code>null</code>.
     */
    static public final IdentityHashMap<RexxCleanupRef,Object> pinnedReferences = new IdentityHashMap<RexxCleanupRef,Object>();


    /** Defines the valid kinds of Rexx related resources to finalize. */
    static public enum RefKind { TEST, REXX_ENGINE, REXX_PROXY } ;

    static private RefKind refkinds[] = RefKind.class.getEnumConstants();

    /** Getter that returns a clone (copy) of the enum constants of the @link{RefKind} enum class.
     *
     * @return copy of the array of enum constants
     */
    static public RefKind[] getRefkinds()
    {
        return refkinds.clone();
    }


    /** Counter array indexed by ordinal value of RefKind, and by created instances and finalizations ran. */
    static private long counters[][] = new long[refkinds.length][2];

    /** Getter that returns a clone (copy) of the current counters array indexed by ordinal value of
     *  <code>RefKind</code> in first dimension, and by 0 (created instances) and 1 (finalizers ran).
     *
     * @return copy of array at time of invocation
     */
    static public long[][] getCounters()
    {
        return counters.clone();
    }


    /** Lock controlling whether a clean runnable needs to be dispatched on a new thread. */
    private static boolean cleanLock=false;

    /** The Runnable that uses remove(timeout) to fetch references from the reference queue.
     *  This Runnable will exit the thread once no references can be fetched from the queue.
     *  <br>Note: using poll() will behave the same, but will start more threads, remove()
     *  without timeout would block and hinder the Rexx interpreter to end.
    */
    static Runnable rRemove=new Runnable()
        {
            // times out, if nothing on the queue; returns immediately, if no references on queue after timeout
            // will save creation of Threads compared to rPolled, but seems to behave comparably otherwise
            @Override public void run()
            {
                RexxCleanupRef rcObj=null;
                try {
                    while ( (rcObj=(RexxCleanupRef) refQueue.remove()) != null )
                    {
                        rcObj.finalizeRexxObject();
                        rcObj.clear();          // clear PhantomReference
                        synchronized (pinnedReferences)
                        {
                            pinnedReferences.remove(rcObj);  // remove PhantomReference
                        }

                        synchronized (counters)
                        {
                            counters[rcObj.refKind.ordinal()][1]++;
                        }
                    }
                }
                catch (Exception e)
                {
                    System.err.println("RexxCleanupRef.clean(): Runnable (thread="+Thread.currentThread().getName()+") received exception: "+e);
                }
                if (bDebug==true) System.err.println("RexxCleanupRef.clean().rRemove: about to finish (thread="+Thread.currentThread().getName()+"), resetting 'cleanLock' to false.");
                cleanLock=false;    // reset lock to allow to start a new thread next time a new instance gets created
            }
        } ;

    /** The kind of PhantomReference. */
    final private RefKind refKind;

    /** The string representing the Rexx interpreter instance or Rexx object in the native layer. */
    final private String  str_id;

    // we must not refer to referent, otherwise it never can be reclaimed and this will not work
    /** Constructor. Whenever an instance gets created the static method @link{#clean} gets invoked
     *  to make sure that that the reference queue gets processed from time to time.
     *
     * @param referent the RexxEngine or RexxProxy object to watch
     * @param refKind RexxCleanupRef.
     * @param str_id the string id maintained in native code
     */
    RexxCleanupRef (final Object referent, final RefKind refKind, final String str_id)
    {
        super(referent, refQueue);
        this.refKind=refKind;
        this.str_id =str_id;

        synchronized (pinnedReferences)
        {
            pinnedReferences.put(this,null);
        }

        synchronized (counters)
        {
            counters[refKind.ordinal()][0]++;
        }

        if (cleanLock==false)   // cleaner thread not running, start it
        {
            clean();    // simple strategy, whenever a new RexxCleanupRef gets created run the clean method
        }
    }

    /** Uses the ooRexx 4.0 API <code>Halt()</code> to raise the <code>HALT</code> condition in all Rexx threads
     *  belonging to the given Rexx interpreter instance.
     *
     * @param rii_ID a String representing the Rexx interpreter instance as returned by {@link #jniRexxCreateInterpreterInstance ()}
     * @throws a {@link RexxException}, if the argument does not denote an existing Rexx interpreter instance
     * @return <code>0</code>
     *
     * @since May 2009
     */
    // RexxAndJava entry points used by RexxEngine
    protected native int    jniRexxHaltInterpreterInstance (String rii_ID); // use Halt()


    /** Uses the ooRexx 4.0 API <code>Terminate()</code> to wait on all Rexx threads
     *  belonging to the given Rexx interpreter instance to terminate.
     *
     * @param rii_ID a String representing the Rexx interpreter instance as returned by {@link #jniRexxCreateInterpreterInstance ()}
     * @throws a {@link RexxException}, if the argument does not denote an existing Rexx interpreter instance
     * @return <code>0</code>
     *
     * @since May 2009
     */
    // RexxAndJava entry points used by RexxEngine and Java4Rexx
    protected native int    jniRexxTerminateInterpreterInstance (String rii_ID); // use Terminate(), but *must* be issued from the original thread context

    /** Unregisters the Rexx object denoted by 'obj_ID' from the JNI registry.
     *
     * @param obj_ID the object id that is used as a key on the JNI side
     *
     * @return number of references to the Rexx object in the JNI registry left; a
     *         number of '0' indicates that no references are left, a return value of '-1'
     *         indicates that the Rexx object was not registered (could not be found)
     */
    // RexxAndJava entry points used by RexxProxy
    protected native int jniUnregisterRexxObject(String obj_ID);



    /** Depending on the kind of argument (Rexx interpreter or Rexx proxy) carry out the
     *  finalization action via JNI:
     * <ul>
     * <li>Rexx interpreter instance: terminate the Rexx interpreter instance on a separate
     *                                thread (the Rexx interpreter instance may block until all
     *                                its threads ended.
     *  <li> Rexx proxy: unregister the Rexx proxy, if reference counter drops to 0 the
     *                                cached Rexx object will be removed from the collection
     *                                such that the ooRexx garbage collector can destroy it
     * </ul>
     */
    private void finalizeRexxObject ()
    {
        if (bDebug==true) System.err.println("*** RexxCleanupRef.finalizeRexxObject(): refKind=["+this.refKind+"], str_id=["+this.str_id+"]");

        if (refKind==RefKind.REXX_ENGINE)
        {
            Runnable r=new Runnable ()
            {
                 public void run()
                 {
                     long startTime;
                     if (bDebug==true)
                     {
                         startTime=System.currentTimeMillis();
                         System.err.println("*** RexxCleanupRef.finalizeRexxObject(): refKind=["+refKind+"], str_id=["+str_id+"] ... now invoke _jniRexxTerminateInterpreterInstance(...), (thread="+Thread.currentThread().getName()+")");
                     }

                     // -> carry out termination on another thread as Terminate() waits until all threads of the Rexx interpreter instance ended
                     int res=0;  // result
                     try
                     {
                         // Not needed: halting all threads with jniRexxHaltInterpreterInstance() before terminating Rexx interpreter instance
                         res=jniRexxTerminateInterpreterInstance (str_id);
                     }
                     catch (Exception e)    // the interpreter instance could be already terminated by RexxEngine !
                     {
                         if (bDebug==true)
                         {
                             System.err.println("*** RexxCleanupRef.finalizeRexxObject(): refKind=["+refKind+"], str_id=["+str_id+"] -> exception: "+e);
                         }
                     }

                     if (bDebug==true)
                     {
                        System.err.println("*** RexxCleanupRef.finalizeRexxObject(): refKind=["+refKind+"], res=["+res+"]=jniRexxTerminateInterpreterInstance(...) lasted: ["
                             + ((float)(System.currentTimeMillis()-startTime)/1000 ) +"] seconds, (thread="+Thread.currentThread().getName()+")");
                     }
                 }
             };
            new Thread (r, refKind+"_Terminate_"+str_id).start();
            // <--------------
        }
        else if (refKind==RefKind.REXX_PROXY)
        {
            int res=jniUnregisterRexxObject(str_id);    // returns number of references left

            if (bDebug==true) System.err.println("*** RexxCleanupRef.finalizeRexxObject(): refKind=["+this.refKind+"], str_id=["+this.str_id+"], res=["+res+"]=jniUnregisterRexxObject(...), (thread="+Thread.currentThread().getName()+")");
        }
    }

    /** This method checks whether a clean thread is running and if not, will create
     *  a new clean thread that processes the @link{PhantomReference}s on the reference
     *  queue, if any.
     */
    synchronized static private void clean()
    {
        if (cleanLock==false)   // only create cleaner thread, if not yet present
        {
            cleanLock=true;
            String threadName="RexxCleanupRef.clean_"+(++cleanInvocationCounter);

            if (bDebug==true) System.err.println("RexxCleanupRef.clean(): about to run on a thread named: "+threadName);

            new Thread (rRemove, threadName).start();  // create and start the thread
        }
    }

    /** Main method to test briefly garbage collection with TestObjects using <code>RexxCleanupRef</code>. */
    static int testObjectCounter=0;
    public static void main (String args[])
    {
        class TestObject
        {
            int num=0;
            TestObject()
            {
                num=testObjectCounter++;
                new RexxCleanupRef(this, RexxCleanupRef.RefKind.TEST, this.toString());
            }

            public String toString()
            {
                return "TestObject_#_"+num;
            }
        }

        java.util.ArrayList<TestObject> al = new java.util.ArrayList<TestObject>();
        int nrObjects=123456;
        System.out.println("RexxCleanupRef.main(): creating ["+nrObjects+"] TestObjects and storing them in an ArrayList ...\n");

        for (int i=0; i<nrObjects; i++)
        {
            TestObject jobj=new TestObject();
            al.add(jobj);
        }

        System.err.println(getStatistics("main(): before nullifying"));
        System.out.println("... nullifying the ArrayList ...\n");
        al=null;

        System.gc();
        System.err.println(getStatistics("after gc()"));

        System.runFinalization();
        System.err.println(getStatistics("after runFinalization()"));

        System.gc();
        System.err.println(getStatistics("after gc()"));

        System.err.println("main(): sleeping 1000 ms");
        try { Thread.sleep(1000); } catch (Throwable t) {}
        System.err.println(getStatistics("after sleeping 1000 ms"));

        System.err.println("\nJava version used for this run: ["+System.getProperty("java.version")+"]");

        System.exit(0);     // force exit (there is a cleaner thread running)
    }


    /** Creates a string with formatted instances, finalizes and not yet finalized objects grouped by
     *  @link {RexxCleanupRef.RefKind}.
     *
     * @return formatted String (including newline at the end of each line)
     */
    static public String getStatistics()
    {
        return getStatistics(null);
    }

    /** Creates a string with formatted instances, finalizes and not yet finalized objects grouped by
     *  @link {RexxCleanupRef.RefKind}.
     *
     * @param title to be included in the header line at the top
     * @return formatted String (including newline at the end of each line)
     */
    static public String getStatistics(String title)
    {
        String strRes="",
               strHeader,
               strFormatHeader="%-13s",
               strNumbers,
               strFormatNumbers=": [%,16d] [%,16d] [%,16d]%n",
               strDivider=String.format("%-71s%n","").replace(' ','-');

        long totalInstances=0;
        long totalFinalizes=0;

            // header line
        if (title==null || title.isEmpty())
        {
            strRes=String.format("RexxCleanupRef [%1$tY-%1$tm-%1$td %1$tT.%1$tN]%n",
                                 Calendar.getInstance());
        }
        else
        {
            strRes=String.format("RexxCleanupRef [%1$tY-%1$tm-%1$td %1$tT.%1$tN] [%2$s]%n",
                                 Calendar.getInstance(), title);
        }


        strRes+=String.format("%14s  %17s  %17s %18s%n", "RefKind:", "Instances:", "Finalized:", "Not Yet Finalized:");
        strRes+=strDivider;

        synchronized (counters)
        {
            for (RefKind en : refkinds)
            {
                strHeader=String.format(strFormatHeader,'['+en.name()+']').replace(' ','.');
                int ordinal=en.ordinal();
                strNumbers=String.format(strFormatNumbers, counters[ordinal][0], counters[ordinal][1],
                                                 counters[ordinal][0]-counters[ordinal][1]);
                strRes+=strHeader+strNumbers;

                totalInstances+=counters[ordinal][0];
                totalFinalizes+=counters[ordinal][1];
            }


            strHeader =String.format(strFormatHeader,"Totals").replace(' ','.');
            strNumbers=String.format(strFormatNumbers, totalInstances, totalFinalizes,
                                                       totalInstances-totalFinalizes);
            strRes+=strDivider+strHeader+strNumbers;
        }
        return strRes;
    }

}
