#!/usr/bin/rexx
/*
      author:  Rony G. Flatscher, (c) 2011
      name:    testPingProperties.rex
      date:    2011-07-25
      changes:    2014-07-24, rgf: explicitly close connection to stop message loop thread
      license: AL 2.0
      purpose: - test the special Rexx support for org.freedesktop.DBus.Properties Get() and Set():
                 makes properties available as Rexx attributes (to get the value, just send the
                 name of the property as the message; to set the value either with the
                 'p~propName=newValue' or the special support as 'p~PropName(newValue)'

      usage:   rexx testPingProperties
      needs:   the server program 'testPongProperties' must be running

      dbus-specifications: <http://dbus.freedesktop.org/doc/dbus-specification.html> (as of 2011-07-14)
*/

say "DbusVersion():" dbusVersion()

objectPath="/org/rexxla/oorexx/dbus/test/Pong"           -- path to remote object
busName   ="org.rexxla.oorexx.dbus.test.pong.properties" -- bus to use

conn=.dbus~session      -- get the session connection

proxy=conn~getObject(busName,objectPath)        -- get access to the remote object
say "proxy="pp2(proxy)
call dump2 proxy~proxy.methods, "proxy~proxy.methods" -- show methods of the remote object
call IDBus.dumpIDBus proxy~proxy.introspectRootNode   -- show methods of the remobe object
say

say "now querying property values of the remote object:"

say "rony1RO:" pp2(proxy~Rony1ro)
say "rony3RW:" pp2(proxy~rony3rw)
say

say "Rony1RO:" proxy~Rony1ro
say "... setting 'Rony2wo', new value in parenthesis 'proxy~Rony2wo(newValue)'"
proxy~Rony2wo("Rony2wo changed from client at:" .dateTime~new)

say "... setting 'Rony2wo', new value in attribute notation: 'proxy~Rony2wo=newValue'"
proxy~Rony2wo="Rony2wo changed from client at:" .dateTime~new
say

say "... setting 'rony3RW', new value in parenthesis 'proxy~Rony3rw(newValue)':"
proxy~Rony3rw("Rony3rw changed from client at:" .dateTime~new)
say "rony3RW now:" pp2(proxy~rony3rw)

say "... setting 'rony3RW', new value in attribute notation: 'proxy~Rony3rw=newValue':"
proxy~Rony3rw="Rony3rw changed from client at:" .dateTime~new
say "rony3RW now:" pp2(proxy~rony3rw)
say

say "DbusVersion():" dbusVersion()

call dumpConnectionStatistics conn
conn~close
exit

halt:    -- stop the message loop thread
   say " ... ctl-c was pressed, stopping messageLoop thread..."
   call dumpConnectionStatistics conn
   conn~close
   exit -1

syntax:
   cond=condition('o')
   say "exception occurred, showing dumpCondition2(condition('o'))..."
   say ppCondition2(cond)
   say
   arr=cond~additional
   call dump2 arr, "additional"
   say
   call dump2 arr[arr~last], "additional[last]"  -- error slotDir
   call dumpConnectionStatistics conn
   conn~close
   exit -2



::requires "dbus.cls"         -- get DBus support for ooRexx
::requires "rgf_util2.rex"    -- get access to the rgf2 utilities (installed by BSF4ooRexx)
::requires "testDumpStats.rex"
