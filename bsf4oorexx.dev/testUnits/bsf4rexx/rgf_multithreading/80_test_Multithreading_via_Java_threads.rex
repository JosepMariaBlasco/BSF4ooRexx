#!/usr/bin/env rexx
/* author:     Rony G. Flatscher
   date:       2009-09-10, 2009-10-10, 2009-10-11, 2009-10-16
   license:    ASF 2.0 (Apache) or CPL 1.0

   purpose:    controller of the multithreaded execution of Rexx threads
*/

use arg rpTestUnit, rpArray, nrThreads, javaThreadName

    tid=bsfGetTID()              -- get this TID

          -- save this thread's TID
    argArray=bsf.createJavaArrayOf("java.lang.Object", tid)
    -- rpArray~sendMessage("APPEND", argArray)
    -- rpArray~"APPEND"(argArray)
    rpArray~APPEND(argArray)

    rexxArray=.array~new         -- create a new rexxArray for this controller's threads

    counter=.counter~new         -- create a counter object

    do i=1 to nrThreads
       counter~increase                   -- increase the counter
          -- thread will decrease counter before returning
       .ThreadClz_using_RexxProxy_Array~new~work(rpTestUnit, tid, i, counter, rpArray, rexxArray, javaThreadName)
    end

    maxTimeToWait=1+nrThreads*1.5/10
    call time "Reset"            -- reset timer
    loop while counter~value>0, time("elapsed")<maxTimeToWait  -- until no more threads, or over 10seconds
       call sysSleep .00001              -- yield
    end

    rpTestUnit~assertEquals(0, counter~value, "active thread counter in Java thread:" javaThreadName)

      -- this controller created 'nrThreads' threads, check it
    rpTestUnit~assertEquals(nrThreads, rexxArray~items, "total number of threads:" javaThreadName)
    return


::requires BSF.CLS


/* ==================================================================================== */
-- implement a counter that one can increase/decrease
::class counter
::method init           -- constructor
  expose value
  value=0               -- set attribute
  forward to (super)    -- let superclass initialize

::attribute value       -- allow interfacing with the attribute

::method increase       -- increase value by 1
  expose value
  value+=1
  return value

::method decrease       -- decrease value by 1
  expose value
  value-=1
  return value



/* ==================================================================================== */
-- implement a class with a threaded worker method
::class "ThreadClz_using_RexxProxy_Array"

::method work
  use arg testUnit, TID, nr, counter, rpArray, rexxArray, javaThreadName
  reply                 -- return & create a new thread

  thisTID=BsfGetTID()
  testUnit~assertTrue(BsfAttachToTID(TID), "attaching to main TID:" TID", thread #" nr", thisTID="thisTID "running in Java thread named:" javaThreadName)

   -- have the RexxProxy send the following message to its proxied Rexx object
  rpArray  ~append(thisTID)
  rexxArray~append(thisTID)

  testUnit~assertTrue(BsfDetach(), "detaching from main TID:" TID", thread #" nr", thisTID="thisTID "running in Java thread named:" javaThreadName)
  counter~decrease
  return