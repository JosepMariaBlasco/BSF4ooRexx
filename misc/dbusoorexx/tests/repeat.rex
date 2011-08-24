#!/usr/bin/rexx
/*
      author:  Rony G. Flatscher, (c) 2011
      name:    testPrivateClientPing.rex
      date:    2011-08-24
      license: AL 2.0, CPL 1.0
      purpose: - allows to repeat a command the number of given times

      usage:   rexx repeat.rex nrOfRepetions command

      needs:   testPrivatePongServer.rex running and listening on the address this
               clients attaches to

      hint:    derived from testRawServerPong.rex, merely replaced "session" with the address
               where the server is listening for clients


      dbus-specifications: <http://dbus.freedesktop.org/doc/dbus-specification.html> (as of 2011-07-14)

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

parse arg repetitions cmd

say "repetitions: ["repetitions"]"
say "cmd:         ["cmd"]"

if datatype(repetitions,"Whole")=.false then
   signal usage

started=.dateTime~new
last=started
do i=1 to repetitions
   say "run #" (i~right(7))"/"repetitions":" cmd
   cmd
   now=.dateTime~new
   info="run #" (i~right(7))"/"repetitions "lasted:" now-last
   say left(info, 79, "-")
   last=now
end
ended=.dateTime~new

say "started:" started", ended:" ended", duration:" ended-started
exit 0

usage:
  say "rexx repeat.rex NrOfRepetitions command"
  exit 0
