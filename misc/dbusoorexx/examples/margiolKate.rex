#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (c) 2011
      name:       margiolKate.rex
      date:       2011-08-14
      changed: 2014-07-23, rgf: adapted logic to new infrastructure, hence a need to explicit stop
                                the message loop thread in order to allow ooRexx to stop
               2014-07-31, rgf: adjust to new DBusVersion() routine
      purpose:    demonstrate how to code the Sebastian Margiol examples for controlling "kate" and "klipper" with
                  the direct DBUS language binding for ooRexx

      needs:      "kate" (editor) and "klipper" (clipboard) installed

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

   signal on syntax              -- syntax condition handler
   signal on halt name syntax    -- CTL-C handler

   conn=.dbus~session            -- get the session connection

   kateProcId=getKateProcId()    -- get the procid of the newest Kate, create an instance if necessary
   if kateProcId="" then         -- no kate instance found (and could not be created)
   do
      say "no kate instance available, could not create one either, aborting ..."
      exit -1
   end
   busname="org.kde.kate-"kateProcID -- create the bus name kate listens to
   objPath="/Kate/Document/1"    -- define object path
   document=conn~getObject(busname,objPath)  -- get the kate document object

   klipper=conn~getObject("org.kde.klipper","/klipper")  -- get the klipper object

   struct=document~endOfLine(1)  -- structs are represented as Rexx array objects
   say "line" struct[2] "ends at position" struct[1]

   if document~isEmpty then      -- a new, empty document
   do
      say "Empty document, filling with computed text..."
      text=.array~of("               ____                    ", -
                     "  ___    ___  |  _ \   ___ __  ____  __", -
                     " / _  \ / _  \| |_) | / _ \\ \/ /\ \/ /", -
                     "| ( ) || ( ) ||  _ < | <_>/ >  <  >  < ", -
                     " \___/  \___/ |_| \_\ \__\ /_/\_\/_/\_\" )

      document~insertTextLines(.array~of(0,0), text, .false)   -- insert the text
   end
   else                          -- an existing document
   do
      say "Document not empty! (Found "document~totalCharacters "characters in" document~lines "line(s) in the document.)"
      say "- Text gets copied to the clipboard and document will be cleared. Press enter to continue ..."
      parse pull input

      klipper~setClipboardContents(document~text)
      document~clear
      say "- Text got copied to the clipboard and the document cleared."
      say "  (There are "document~totalCharacters "characters in" document~lines "line(s) in the document.)"
      say
      say "  Next step: paste text from clipboard into the document. Press enter to continue ..."
      parse pull input

      document~setText(klipper~getClipboardContents)
      say "- Text got pasted from the clipboard into the document."
   end
   say "There are "document~totalCharacters "characters in" document~lines "line(s) in the document."
   say "Done."
   conn~close                 -- closing the connections, stops the message loop thread
   exit

syntax:
   co=condition('object')     -- get condition object
   call dump2 co
   conn~close                 -- closing the connections, stops the message loop thread
   exit


::requires "dbus.cls"         -- get access to DBus
-- ::requires "rgf_util2.rex"    -- package installed with BSF4ooRexx, eases debugging due to 'pp'-formatting and dump2() routines


::routine getKateProcId       -- return process id of newest Kate instance of current user; if there is none, start an instance of Kate
  cmd='pgrep -n -x -u "$USER" kate | rxqueue'   -- get procid and queue it via the external Rexx queue

  proc=getProc(cmd)           -- get kate's proc id
  if proc="" then             -- found no kate instance for this user
  do
     "kate &"                 -- let the shell create a new instance of kate
     call syssleep .5         -- wait a little bit to give this kate instance time to request a busname
     proc=getProc(cmd)        -- get kate's proc id
  end
  return proc                 -- return the proc id

getProc: procedure            -- do the work: execute the command, parse its output
  parse arg cmd
  cmd                         -- let the shell execute the passed command
  proc=""
  do while queued()>0
     parse pull proc          -- pull the procid from the external Rexx queue, make sure its emptied
  end
  return proc

