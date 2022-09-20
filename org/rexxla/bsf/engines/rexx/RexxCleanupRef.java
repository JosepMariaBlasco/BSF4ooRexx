package org.rexxla.bsf.engines.rexx;
// need to run javah to get the C-header include file for the native functions in this Java class

import java.lang.ref.PhantomReference ;
import java.lang.ref.ReferenceQueue ;
import java.util.IdentityHashMap ;
import java.util.Calendar;
/*
    rexxtry.rex
    call bsf.cls

    clzRef=bsf.loadclass("org.rexxla.bsf.engines.rexx.RexxCleanupRef")
    say clzRef~getStatistics

    ro=BsfCreateRexxProxy("oha",,"rexxobject")  -- do not use string as code, embed it for Java
    say "ro:" ro "ro~sendMessage0(...):" ro~sendMessage0("string")
    say clzRef~getStatistics
    drop ro; call gc            -- drop & let Rexx garbage collect
    say clzRef~getStatistics
    .java.lang.System~gc        -- let Java garbage collect
    say clzRef~getStatistics

*/


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
 * </ol>
 *
 * <p>To not block termination of Rexx the cleanup thread does not use the blocking <code>remove()</code>
 * method on the reference queue, but rather <code>remove(timeout)</code> and terminates when the
 * queue is empty. To make sure that the cleanup gets carried out eventually, each time a new
 * instance of this class gets created the @link{RexxCleanupRef#startCleanerThread()} method gets invoked, if
 * it is not running. From time to time the static method @link{RexxCleanupRef#startCleanerThread()} gets invoked
 * by BSF4ooRexx directly in the <code>RexxAndJava</code> class.
 *
 * <pre>------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2021-2022 Rony G. Flatscher
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
 * @version  100.20220802
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

    - 20210808, rgf: - clone in synchronized block
                     - change format patterns to static

    - 20220304, rgf: - cater for the eventual removal of System.runFinalization():
                       catch Throwable and report it

    - 20220714, rgf: - added new kind REXX_SCRIPT_ENGINE

    - 20220715, rgf: - changed logic to also cater for RexxScriptEngine (if freed,
                       RexxEngine.terminate() needs to be invoked which will cause
                       termination of the RII via jni), hence not invoking
                       jniRexxTerminateInterpreterInstance(RII) anymore

                     - before terminating "call bsf.redirectTo ooRexx" gets invoked to
                       make sure that potentially redirected standard files & redirected
                       ooRexx traceoutput and debuginput get redirected to the standard
                       ooRexx streams

                     - needed to change element from <String> to Object

    - 20220716, rgf: - if there are more than 100 unfinalized RexxEngine, then
                       System.gc() gets run on a separate thread to help clean
                       up all BSF4ooRexx related objects (especially RexxProxy
                       objects that may pin ooRexx objects)

    - 20220731, rgf: - tidying up, now that termination is done via RexxEngine.terminate() only

    - 20220802, rgf: - tidying up, refactoring names to make them self documentary
*/

public class RexxCleanupRef extends PhantomReference
{
    /** If set to false the compiler should eradicate all dependent code. */
    private static final boolean bDebug=false; // true;

    /** Controls whether debug output gets generated in the context of terminating
     *  a Rexx interpreter instance (RII).
     */
    private static boolean bDebugRII=false; // true; // false; // show terminating RII (Rexx interpreter instance)

    /** Returns the current setting of the private boolean field <cdoe>bDebugRII</code> which controls debug output
     *  in the context of terminating a Rexx interpreter instance (RII).
     * @return returns current setting (if <code>true</code> then showing debug message when terminating Rexx interpreter instance)
     */
    public static boolean getDebugRII()
    {
        return bDebugRII;
    }

    /** Allows changing Setter for <code>bDebugRII</code> (Rexx interpreter instance related).
     * @param newDebugState if <code>true</code> then showing a debug message when terminating a Rexx interpreter instance
     */
    public static void setDebugRII(boolean newDebugState)
    {
        bDebugRII=newDebugState;
    }

    private static boolean bDebugRexxProxy=false; // true; // false;  // show dereferencing RexxObject and remaining references
    /** Getter for <code>bDebugRexxProxy</code>.
     * @return returns current setting (if <code>true</code> then showing debug message when dereferencing a RexxProxy)
     */
    public static boolean getDebugRexxProxy()
    {
        return bDebugRexxProxy;
    }

    /** Setter for <code>bDebugRexxProxy</code>.
     * @param newDebugState if <code>true</code> then showing a debug message when dereferencing a RexxProxy
     */
    public static void setDebugRexxProxy(boolean newDebugState)
    {
        bDebugRexxProxy=newDebugState;
    }


    /** Version string indicating version of this class (majorVersion*100+minorVersion
     *  concatenated with a dot and the sorted date of last change.
     */
    static final public String version = "100.20220802";

    /** Counts number of times the startCleanerThread() thread got started so far. */
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


    /** Learned that <code>PhantomReferences</code> must be refenced themselves
     *  otherwise they may never be put on the reference queue, so storing them
     *  in this <code>IdentityHashMap</code> until the reference is being put on
     *  the reference queue for processing, at which time the reference will be
     *  removed from the <code>IdentityHashMap</code>. The <code>IdentityHashMap</code>
     *  gets used as <code>IdentityHashSet</code>, i.e. the value part will always
     *  be </code>null</code>.
     */
    static public final IdentityHashMap<RexxCleanupRef,Object> pinnedReferences = new IdentityHashMap<RexxCleanupRef,Object>();


    /** Defines the valid kinds of Rexx related resources to finalize. */
    static public enum RefKind { TEST, REXX_ENGINE, REXX_PROXY, REXX_SCRIPT_ENGINE } ;

    /** Stores the enum objects in an arary in ordinal order. */
    static private RefKind refkinds[] = RefKind.class.getEnumConstants();

    /** Getter that returns a clone (copy) of the enum constants of the @link{RefKind} enum class.
     *
     * @return array of enum constants
     */
    static public RefKind[] getRefkinds()
    {
        return refkinds;
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
        synchronized (counters)
        {
            return counters.clone();
        }
    }

    /** Minimum threshold of unfinalized RexxEngines, causing a System.gc() to be called. */
    private static int     gcThresholdMin=1;        // minimum unfinalized RexxEngines (every new RexxEngine will cause a gc())
    /** Return gcThresholdMin attribute.
     *  @return gcThresholdMin value */
    public static  int     getGcThresholdMin()
    {
        return gcThresholdMin;
    }
    /** Set gcThresholdMin attribute: <code>newValue&gt;=1 && newValue&lt;=gcThresholdMax</code>.
     *  @param newValue */
    public static void setGcThresholdMin(int newValue)
    {
        if (newValue>=1 && newValue<=gcThresholdMax)
        {
            gcThresholdMin=newValue;
        }
    }

    /** Maximum threshold of unfinalized RexxEngines, causing a System.gc() to be called. */
    // private static int     gcThresholdMax=500;      // maximum unfinalized RexxEngines, before running System.gc()
    private static int     gcThresholdMax=100;      // maximum unfinalized RexxEngines, before running System.gc()
    // private static int     gcThresholdMax=50;      // maximum unfinalized RexxEngines, before running System.gc()
    /** Return gcThresholdMax attribute.
     *  @return gcThresholdMax value */
    public static  int     getGcThresholdMax()
    {
        return gcThresholdMax;
    }
    /** Set gcThresholdMin attribute: <code>newValue&gt;=gcThresholdMin</code>.
     *  @param newValue */
    public static void  setGcThresholdMax(int newValue)
    {
        if (newValue >= gcThresholdMin)
        {
            gcThresholdMax=newValue;
        }
    }

    private static boolean gcTriggered=false;       // indicates whether gcThreshold+1 encountered
    private static int     gcThresholdIgnoreFor=0;  // in case gcTriggered, ignore for gcThresholdIgnoreFor

    /** Threshold of unfinalized RexxEngines, causing a System.gc() to be called. */
    private static int     gcThreshold=100;         // if >= gcThreshold (unfinalized RexxEngines) then run System.gc()

    /** Threshold of unfinalized RexxEngines which will cause a System.gc() to be called.
     *
     * @return current threshold value that triggers System.gc()
     */
    public static int getGcThreshold()
    {
        return gcThreshold;
    }

    /** Set threshold of unfinalized RexxEngines which will cause a System.gc() to
     *  be called.
     *
     * @param newValue number of unfinalized RexxEngines that will cause an out of bound
     *        System.gc() to be triggered (to reclaim unused BSF4ooRexx resources)
     */
    public static void setGcThreshold(int newValue)
    {
        if (newValue>=gcThresholdMin && newValue<= gcThresholdMax)
        {
            gcThreshold=newValue;
            gcThresholdIgnoreFor=gcThreshold;
        }
    }


    /** Lock controlling whether a clean runnable needs to be dispatched on a new thread. */
    private static boolean cleanerThreadRunning=false;

    /** The Runnable that uses remove(timeout) to fetch references from the reference queue.
     *  This Runnable will exit the thread once no references can be fetched from the queue.
     *  <br>Note: using poll() will behave the same, but will start more threads, remove()
     *  without timeout would block and hinder the Rexx interpreter to end.
    */
    static Runnable runnableCleaner=new Runnable()
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
                catch (Throwable t)
                {
                    System.err.println("RexxCleanupRef.runnableCleaner(thread="+Thread.currentThread().getName()+") threw throwable: "+t);
                }
                if (bDebug==true) System.err.println("RexxCleanupRef.runnableCleaner: about to finish (thread="+Thread.currentThread().getName()+"), resetting 'cleanerThreadRunning' to false.");
                cleanerThreadRunning=false;    // reset lock to allow to start a new thread next time a new instance gets created
            }
        } ;

    /** The kind of PhantomReference. */
    final private RefKind refKind;

    /** The string representing the Rexx interpreter instance or Rexx object in the native layer
     *  or the RexxEngine instance peer for the RexxScriptEngine instance.
     */
    final private Object obj_id;

    // we must not refer to referent, otherwise it never can be reclaimed and this will not work
    /** Constructor. Whenever an instance gets created the static method @link{#clean} gets invoked
     *  to make sure that that the reference queue gets processed from time to time.
     *
     * @param referent the RexxEngine or RexxProxy object to watch
     * @param refKind RexxCleanupRef.
     * @param obj_id the string id maintained in native code or RexxEngine of finalized RexxScriptEngine
     *               or the RexxEngine instance peer of the RexxScriptEngine instance
     */
    public RexxCleanupRef (final Object referent, final RefKind refKind, final Object obj_id)
    {
        super(referent, refQueue);
        this.refKind=refKind;
        this.obj_id =obj_id;

        boolean bRunGC=false;       // if true runs java.lang.System.gc() to reclaim BSF4ooRexx objects

        synchronized (pinnedReferences)
        {
            pinnedReferences.put(this,null);
        }

        int ordinal=refKind.ordinal();
        synchronized (counters)
        {
            counters[ordinal][0]++;
        }

        if (refKind==RefKind.REXX_ENGINE)   // determine whether we want to run System.gc()
        {
            // if gcThreshold or more RexxEngines, run the Java garbage collector
            // in order to not peg the system with gc() calls when encountering unfinalized
            // RexxEngines >= gcThreshold after a gc() call we skip the next gcThreshold/3
            // creations of RexxEngines before checking for a gc() again
            int unfinalizedRE = (int) (counters[ordinal][0] - counters[ordinal][1]);
            bRunGC=(unfinalizedRE >= gcThreshold);

            if (bRunGC)
            {
                if (gcThresholdIgnoreFor>0 || gcTriggered)  // skip System.gc() this time
                {
                    gcTriggered=false;                      // reset
                    if (--gcThresholdIgnoreFor<=0)
                    {
                        gcThresholdIgnoreFor=gcThreshold/3; // reset
                        if (bDebug==true) System.err.println("\n... *** RexxCleanupRef(): so far createdRE=["+counters[ordinal][0]+"] | unfinalizedRE: ["+unfinalizedRE+"] >= gcThreshold ["+gcThreshold+"] | gcTriggered=[true] -> gcThresholdIgnoreFor: ["+gcThresholdIgnoreFor+"] ...");
                    }
                }
                else
                {
                    gcTriggered=true;                       // indicate we just ran System.gc()
                    gcThresholdIgnoreFor=0;                 // reset
                    if (bDebug==true) System.err.println("\n... *** RexxCleanupRef(): so far createdRE=["+counters[ordinal][0]+"] | unfinalizedRE: ["+unfinalizedRE+"] >= gcThreshold ["+gcThreshold+"] -> running System.gc() ...");
                    System.gc();        // have the Java garbage collector run
                }
            }
        }

        if (cleanerThreadRunning==false)   // cleaner thread not running, start it
        {
            startCleanerThread();    // simple strategy, whenever a new RexxCleanupRef gets created run the clean method
        }
    }

// deprecated: as of July 2022 termination occurs via RexxEngine.terminate() (RexxAndJava
//             also using RexxEngine.terminate()) only!
//             Leaving just in case so not to have the c++ include file and BSF4ooRexx.cc
//             regenerated if a need arises in the future
    /* * Uses the ooRexx 4.0 API <code>Terminate()</code> to wait on all Rexx threads
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
     *  <li>REXX_PROXY:         proxy: unregister the Rexx proxy, if reference counter drops to 0 the
     *                          cached Rexx object will be removed from the ooRexx registry
     *  <li>REXX_ENGINE:        NOP, just there for debugging
     *  <li>REXX_SCRIPT_ENGINE: use supplied RexxEngine object to invoke terminate()
     * </ul>
     */
    private void finalizeRexxObject ()
    {
        if (bDebug==true) System.err.println("*** RexxCleanupRef.finalizeRexxObject(): refKind=["+this.refKind+"], obj_id=["+this.obj_id+"]");

        if (refKind==RefKind.REXX_ENGINE)   // nothing to do (cf. REXX_SCRIPT_ENGINE)
        {
            if (bDebugRII || bDebug==true)
            {
                System.err.println("RexxCleanupRef.REXX_ENGINE: -> RII=["+obj_id+"]");
            }
        }

        else if (refKind==RefKind.REXX_PROXY)
        {
            if (bDebugRexxProxy || bDebug==true)
            {
                System.err.println("RexxCleanupRef.REXX_PROXY, RexxProxy: --> before jniUnregisterRexxObject("+obj_id+")");
            }
            int res=jniUnregisterRexxObject((String) obj_id);    // returns number of references left on the Rexx object

            if (bDebugRexxProxy || bDebug==true)
            {
                System.err.println("RexxCleanupRef.REXX_PROXY, RexxProxy: --> AFTER  jniUnregisterRexxObject("+obj_id+") remaining references="+res
                                   + (res<=0 ? " <-- removing" : "")
                                  );
            }

            if (bDebug==true) System.err.println("*** RexxCleanupRef.REXX_PROXY: refKind=["+this.refKind+"], obj_id=["+this.obj_id+"], res=["+res+"]=jniUnregisterRexxObject(...), (thread="+Thread.currentThread().getName()+")");
        }

        else if (refKind==RefKind.REXX_SCRIPT_ENGINE)   // on the clean thread
        {
            RexxEngine re = (RexxEngine) obj_id;
            if (bDebugRII || bDebug==true)
            {
                System.err.println("RexxCleanupRef.REXX_SCRIPT_ENGINE: re=["+obj_id+"] ==> re.get_rii_ID=["+re.get_rii_ID()+"] [RE_"+re.id+"]");
            }
if (bDebug) System.err.println("RexxCleanupRef: REXX_SCRIPT_ENGINE --> before re.terminate() for [RE_"+re.id+"]");
            re.terminate();     // this will terminate the RII via jni
if (bDebug) System.err.println("RexxCleanupRef: REXX_SCRIPT_ENGINE <-- after  re.terminate() for [RE_"+re.id+"]");
        }
    }

    /** This method checks whether a clean thread is running and if not, will create
     *  a new clean thread that processes the @link{PhantomReference}s on the reference
     *  queue, if any.
     */
    synchronized static private void startCleanerThread()
    {
        if (cleanerThreadRunning==false)   // only create cleaner thread, if not yet present
        {
            cleanerThreadRunning=true;
            String threadName="RexxCleanupRef.runnableCleaner_"+(++cleanInvocationCounter);

            if (bDebug==true) System.err.println("RexxCleanupRef.startCleanerThread(): about to run on a thread named: "+threadName);

            new Thread (runnableCleaner, threadName).start();  // create and start the thread
        }
    }

    /** Main method to test briefly garbage collection with TestObjects using <code>RexxCleanupRef</code>. */
    static int testObjectCounter=0;
    public static void main (String args[])
    {
        class TestObject
        {
            TestObject()
            {
                new RexxCleanupRef(this, RexxCleanupRef.RefKind.TEST, "clean infos for TestObject # "+(++testObjectCounter) );
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

        try {
            System.runFinalization();
            System.err.println(getStatistics("after runFinalization()"));
        }
        catch (Throwable t) {
            System.err.println("exception while trying to run 'System.runFinalization()': "+t);
        }

        System.gc();
        System.err.println(getStatistics("after gc()"));

        int sleepTime=500;
        System.err.println("main(): sleeping "+sleepTime+" ms");
        try { Thread.sleep(sleepTime); } catch (Throwable t) {}
        System.err.println(getStatistics("after sleeping "+sleepTime+" ms"));

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

    static final private String strFormatHeader_1  = "RexxCleanupRef [%1$tY-%1$tm-%1$td %1$tT.%1$tN]%n";
    static final private String strFormatHeader_2  = "RexxCleanupRef [%1$tY-%1$tm-%1$td %1$tT.%1$tN] [%2$s]%n";
    static final private String strFormatHeader_3  = String.format("%21s  %17s  %17s %18s%n", "RefKind:", "Instances:", "Finalized:", "Not Yet Finalized:");
    static final private String strFormatLineHeader= "%-20s";
    static final private String strFormatNumbers   = ": [%,16d] [%,16d] [%,16d]%n";
    static final private String strDivider         = String.format("%-78s%n","").replace(' ','-');

    /** Creates a string with formatted instances, finalizes and not yet finalized objects grouped by
     *  @link {RexxCleanupRef.RefKind}.
     *
     * @param title to be included in the header line at the top
     * @return formatted String (including newline at the end of each line)
     */
    static public String getStatistics(final String title)
    {
        String strRes,
               strLineHeader,
               strNumbers;

        long totalInstances=0;
        long totalFinalizes=0;

            // header line
        if (title==null || title.isEmpty())
        {
            strRes=String.format(strFormatHeader_1, Calendar.getInstance());
        }
        else
        {
            strRes=String.format(strFormatHeader_2, Calendar.getInstance(), title);
        }

        strRes+=strFormatHeader_3 + strDivider;

        synchronized (counters)
        {
            for (RefKind en : refkinds)
            {
                strLineHeader=String.format(strFormatLineHeader,'['+en.name()+']').replace(' ','.');
                int ordinal=en.ordinal();
                strNumbers=String.format(strFormatNumbers, counters[ordinal][0], counters[ordinal][1],
                                                 counters[ordinal][0]-counters[ordinal][1]);
                strRes+=strLineHeader+strNumbers;

                totalInstances+=counters[ordinal][0];
                totalFinalizes+=counters[ordinal][1];
            }

            strLineHeader =String.format(strFormatLineHeader,"Totals").replace(' ','.');
            strNumbers=String.format(strFormatNumbers, totalInstances, totalFinalizes,
                                                       totalInstances-totalFinalizes);
            strRes+=strDivider+strLineHeader+strNumbers;
        }
        return strRes;
    }

}
