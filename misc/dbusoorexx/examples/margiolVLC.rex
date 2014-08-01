#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (c) 2011
      name:       margiolVLC.rex
      date:       2011-08-14
      changed: 2014-07-23, rgf: adapted logic to new infrastructure, hence a need to explicit stop
                                the message loop thread in order to allow ooRexx to stop
               2014-07-31, rgf: adjust to new DBusVersion() routine
      purpose:    demonstrate how to code the Sebastian Margiol examples for controlling "vlc" with the direct
                  DBUS language binding for ooRexx

      needs:      get and install "vlc" (http://www.videolan.org), then go to "Tools -> Preferences",
                  "Show settings" to "All", then open "Interfaces" in the left frame and mark
                  "Control interfaces" and check "D-Bus control interface"

      version:    1.0

      license:    AL 2.0

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

      dbus-specifications: <http://dbus.freedesktop.org/doc/dbus-specification.html>
*/
   say "DBusVersion():" DBusVersion()

   signal on syntax
   signal on halt name syntax

   conn=.dbus~session            -- get the session connection
   listener=.SignalHandler~new   -- create an instance of our signal handler
   conn~listener("add",listener) -- add the listener to the connection

      -- tell DBus to forward us the signals matching the following rule/filter (cf. <http://dbus.freedesktop.org/doc/dbus-specification.html>)
   matchRule="type='signal',interface='org.freedesktop.MediaPlayer'"
      -- if third argument is given and set to .true, then the message may block, but also returns
      -- error information, if the match rule is misformed (so this is recommended for debugging filters)
   conn~match("add",matchRule,.true)   -- tell DBus what to dispatch to our connection

   mp=conn~getObject("org.mpris.vlc","/Player")  -- get the VLC player object
   say "mp~pause..."
   mp~pause                   -- pause the player (status gets changed)
   say "paused position:" mp~PositionGet
   -- call dump2 mp~getMetaData, "MetaData of current track"
   wait=10
   say "sleeping" wait "seconds ..."
   call sysSleep wait

   say "current volume: " mp~VolumeGet", setting to '40'..."
   mp~volumeSet(40)
   say "current volume: " mp~VolumeGet
   mp~pause                   -- let it play again ... (status change)
   say "sleeping" wait "seconds ..."
   call sysSleep wait
   say "-"~copies(50)

   say "now changing tracks a bit, mp~PositionGet:" mp~PositionGet
   say "next, wait for" wait "seconds"
   mp~next
   say "now position:" mp~PositionGet
   mp~PositionSet(60000)      -- position at the one minute mark in the track
   say "now position:" mp~PositionGet
   call sysSleep wait

   say "next, wait for" wait "seconds"
   mp~next
   say "now position:" mp~PositionGet
   call sysSleep wait

   say "prev, wait for" wait "seconds"
   mp~~prev~~prev
   say "now position:" mp~PositionGet
   call sysSleep wait
   say "stopping the media player."
   mp~stop                    -- stop media player
   say "--- The End. ---"
   conn~close                 -- closing the connections, stops the message loop thread
   exit

syntax:
   co=condition('object')     -- get condition object
   call dump2 co
   conn~close                 -- closing the connections, stops the message loop thread
   exit


::requires "dbus.cls"         -- get access to DBus
::requires "rgf_util2.rex"    -- package installed with BSF4ooRexx, eases debugging due to 'pp'-formatting and dump2() routines


   /* only define methods by the name of the signal to intercept  */
::class SignalHandler
::method CapsChange
  use arg i, slotDir
  say "===> CapsChange-signal:" pp2(i) "received at:" slotDir~dateTime

::method StatusChange
  use arg ai, slotDir
  say "===> StautsChange-signal:" "received at:" slotDir~dateTime
  call dump2 ai, "statusChange data"

::method TrackChange
  use arg dir, slotDir
  say "===> TrackChange-signal:" "received at:" slotDir~dateTime
  call dump2 dir, "TrackChange submitted directory"

::method unknown
  use arg methName, methArgs
  slotDir=methArgs[methArgs~last]   -- last argument is the 'slotDir' directory
  say "===> unknown signal: name="pp2(methName) "received at:" slotDir~dateTime

