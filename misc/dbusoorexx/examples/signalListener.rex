#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (c) 2011
      name:       demoSignalListener.rex
      usage:      rexx demoHelloListener.rex
      purpose:    - demonstrate how to listen for any dbus signal with a Rexx object
      date:       2011-07-29 (2011-07-17)
      changes:    2014-07-23, rgf, adapt to new infrastructure logic, show DBusVersion,
                                   accept "session" and or "system" as the bus from the commandline
               2014-07-31, rgf: adjust to new DBusVersion() routine
      license:    AL 2.0 (Apache License 2.0)

            ------------------------ Apache Version 2.0 license -------------------------
               Copyright 2011-2014 Rony G. Flatscher

               Licensed under the Apache License, Version 2.0 (the "License");
               you may not use this file except in compliance with the License.
               You may obtain a copy of the License at

                   http://www.apache.org/licenses/LICENSE-2.0

               Unless required by applicable law or agreed to in writing, software
               distributed under the License is distributed on an "AS IS" BASIS,
               WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
               See the License for the specific language governing permissions and
               limitations under the License.
            -----------------------------------------------------------------------------
*/

say "DBusVersion():" DBusVersion()

signal on syntax name halt -- make sure message loop gets stopped
signal on halt             -- intercept ctl-c or closing terminal in which Rexx runs

parse lower arg busName .

-- timeout=180    -- listen for 180 seconds (3 minutes) for signals, then stop
timeout=60     -- listen for 60 seconds for signals, then stop

if busName="system" then
do
   conn=.dbus~system       -- get the "session" connection
   say "... listening for SIGNALs on the 'system' bus ..."
end
else
do
   conn=.dbus~session      -- get the "session" connection
   say "... listening for SIGNALs on the 'session' bus ..."
end

conn~listener("add", .rexxSignalListener~new)   -- add the Rexx listener object
conn~match("add", "type='signal'")  -- ask for any signal on the bus, non-blocking mode

say "listening" timeout "secs for signals ..."
call syssleep timeout

halt:
   say "closing the connection."
   conn~close                 -- close, thereby terminating message loop thread


::requires "dbus.cls"         -- get dbus support for ooRexx
::requires "rgf_util2.rex"    -- installed with the BSF4ooRexx package


::class RexxSignalListener    -- just dump all signals/events we receive
::method unknown              -- this method will service all invocations
  use arg methName, methArgs

  slotDir=methArgs[methArgs~items]     -- last argument is slotDir
  say "==>" pp2(slotDir~messageTypeName) pp2(slotDir~interface) pp2(slotDir~member) pp2(slotDir~dateTime)": nrArgs="methArgs~items-1

  call dump2 slotDir, "accompanying slotDir"
  if methArgs~items>1 then
  do
     call dump2 methArgs~section(1, methArgs~items-1), "signal's arguments"
  end
  say "-"~copies(79)


::method NameOwnerChanged  -- demo how to directly intercept a signal
  use arg one, two, three, slotDir
  say "==>" pp2(slotDir~messageTypeName) pp2(slotDir~interface) pp2(slotDir~member) pp2(slotDir~dateTime)":" "Name:" pp2(one)", OldOwner:" pp2(two)", NewOwner:" pp2(three)

   /* if there are signals that have the same name as methods of the Rexx class .Object, then
      one *must* implement these as individual methods */
::method Start             -- demo how to directly intercept a signal
  use arg one, slotDir
  say "==>" pp2(slotDir~messageTypeName) pp2(slotDir~interface) pp2(slotDir~member) pp2(slotDir~dateTime)":" pp2(one)

