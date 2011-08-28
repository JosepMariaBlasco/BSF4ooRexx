#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (c) 2011
      name:       demoSignalListener.rex
      usage:      rexx demoHelloListener.rex
      purpose:    - demonstrate how to listen for any dbus signal with a Rexx object
      date:       2011-07-29 (2011-07-17)
      license:    AL 2.0 (Apache License 2.0)

            ------------------------ Apache Version 2.0 license -------------------------
               Copyright 2011 Rony G. Flatscher

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

signal on halt          -- intercept ctl-c (jump to label 'halt:' below)

conn=.dbus~session      -- get the "session" connection
conn~listener("add", .rexxSignalListener~new)   -- add the Rexx listener object
conn~match("add", "type='signal'", .true) -- ask for any signal on the bus, blocking mode

say "Hit enter to stop listener..."
parse pull answer

halt:                         -- a ctl-c causes a jump to this label
   say "stopping message loop."
   conn~messageLoop("stop")-- stop message loop



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
  use arg name, oldOwner, newOwner, slotDir
  say "==> NameOwnerChanged:" "Name:" pp2(name)", OldOwner:" pp2(oldOwner)", NewOwner:" pp2(newOwner)

