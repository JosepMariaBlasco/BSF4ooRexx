#!/usr/bin/rexx
/*
      author:     Rony G. Flatscher (c)
      date:       2011-08-04
      version:    0.1
      name:       dbusdoc.rex
      purpose:    documents given bus name in HTML form to allow the creation of API listings
      license:    AL 2.0
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
use arg conn=.nil
if conn<>.nil then call dumpConnectionStatistics conn

::requires "rgf_util2.rex"    -- get access to the rgf2 utilities (installed by BSF4ooRexx)

::routine dumpConnectionStatistics   public  -- dump connection's statistics
  use strict arg conn, now=(.dateTime~new)
  statistics=conn~statistics
  say "conn~collectStatistics:" pp2(conn~collectStatistics)
  call dump2 statistics, "statistics"
  say "dumping slotDirs of the last logged error, message or signal:"
  do e over .array~of("lastSentError","lastSentMessage", "lastSentSignal", "lastReceivedError", "lastReceivedMessage", "lastReceivedSignal")
      d=statistics~entry(e)
      if d=.nil then iterate
      say "   " pp2(e)":"
      call dump2 d, e"'s slotDir:"
  end

  started=statistics~started
  say "started: " pp2(started)
  say "now:     " pp2(now)
  say "duration:" pp2(now-started)
  say "-"~copies(50)

