#!/usr/bin/rexx
/*
   name:             runTestUnits.rex
   author:           Rony G. Flatscher
   date:             2005-08-20
   version:          1.0.5

   changed:          2005-10-19, ---rgf, fixed a little bug: if "-r" is given without a directory,
                                         start with current directory (instead of root directory!)
                     2006-11-29, ---rgf, changed name to indicate what this program is meant for
                     2006-12-12, ---rgf, changed title for brief test results slightly
                     2006-12-13, ---rgf, added elapsed time information (for running all test units)
                     2006-12-14, ---rgf, added hashbang line
                     2007-08-17, ---rgf, using ooRexxUnit environment definitions
                     2007-08-18, ---mm, add formatter option, refactor code

   license:          CPL 1.0 (Common Public License v1.0, see below)

   languageLevel:    6.02
   purpose:          Load all ooRexx-base testUnits and run all the tests contained therein.

   remark:           Serves as an example of how to use and drive the ooRexxUnit framework

   category:
*/
/*----------------------------------------------------------------------------*/
/*                                                                            */
/* Copyright (c) 2005 Rexx Language Association. All rights reserved.         */
/*                                                                            */
/* This program and the accompanying materials are made available under       */
/* the terms of the Common Public License v1.0 which accompanies this         */
/* distribution. A copy is also available at the following address:           */
/* http://www.oorexx.org/license.html                                         */
/*                                                                            */
/* Redistribution and use in source and binary forms, with or                 */
/* without modification, are permitted provided that the following            */
/* conditions are met:                                                        */
/*                                                                            */
/* Redistributions of source code must retain the above copyright             */
/* notice, this list of conditions and the following disclaimer.              */
/* Redistributions in binary form must reproduce the above copyright          */
/* notice, this list of conditions and the following disclaimer in            */
/* the documentation and/or other materials provided with the distribution.   */
/*                                                                            */
/* Neither the name of Rexx Language Association nor the names                */
/* of its contributors may be used to endorse or promote products             */
/* derived from this software without specific prior written permission.      */
/*                                                                            */
/* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS        */
/* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT          */
/* LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS          */
/* FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT   */
/* OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,      */
/* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED   */
/* TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,        */
/* OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY     */
/* OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    */
/* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS         */
/* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.               */
/*                                                                            */
/*----------------------------------------------------------------------------*/
cmdLine = arg(1)

   if cmdLine~words > 3 then return doHelp()

   cmdOpts = .set~new
   if hasHelpOpt(cmdLine, cmdOpts) then return doHelp()

   switches = getSysFileTreeOpts(cmdOpts)
   formatter = getFormatterOpt(cmdOpts)

   -- If options are added, getSearchFile() has to remain last because it takes
   -- whatever is left.
   searchFile = getSearchFile(cmdOpts)
   if searchFile == "" then return doHelp()

   say "searchFile="pp(searchFile)", SysFileTree()-switches:" pp(switches) "formatter:" formatter "..."

      -- get all test unit files in current directory
   call sysFileTree searchFile, "tests.", switches

      -- create list of file names
   list=.list~new
   do i=1 to tests.0
      list~insert(tests.i)
   end

   ts=makeTestSuiteFromFileList(list)  -- create the testSuite object
   call time "Reset"    -- start timer
   tr=ts~execute   -- run all the tests in the testSuite, retrieve the testResult object
   e=time("Elapsed")

               -- show brief results
   parse source . . s                  -- get full path to this program
   this=filespec("Name", s)            -- extract file name

   select
      when formatter == 1 then do
         call simpleDumpTestResults tr, pp(this) "processed:" pp(searchFile) "with" pp(switches),
                                        "found" pp(tests.0) "file(s), elapsed time:" pp(e) "sec(s)"
      end
      when formatter == 2 then do
         z = simpleFormatTestResults(tr, pp(this) "processed:" pp(searchFile) "with" pp(switches) -
                                        "found" pp(tests.0) "file(s), elapsed time:" pp(e) "sec(s)")
      end
      otherwise do
        -- Should never really happen.
         call simpleDumpTestResults tr, pp(this) "processed:" pp(searchFile) "with" pp(switches),
                                        "found" pp(tests.0) "file(s), elapsed time:" pp(e) "sec(s)"
      end
   end
   -- End select

return 0

::requires ooRexxUnit.cls

::routine getSearchFile
   use arg cmdOpts
   if cmdOpts~items > 1 then return ""

   if cmdOpts~items == 0 then directory = "."
   else directory = cmdOpts~makearray~at(1)

   if directory~right(1) \== .ooRexxUnit.directory.separator then
      directory = directory || .ooRexxUnit.directory.separator

return directory || "*.testUnit"

::routine getFormatterOpt
   use arg cmdOpts
   opt = 1

   -- Look for a formatter arg in the cmdOpts set.  If we find it, remove it
   -- from the set and return the appropriate number (only 1 - 2 currently
   -- supported).  If we don't find it, remove nothing from the set and return
   -- the default.
   itr = cmdOpts~supplier
   do while itr~available
      w = itr~item
      if w~length == 2 & w~left(1) == '-' & w~right(1)~datatype('W') then do
         if w~right(1) > 0 & w~right(1) < 3 then do
            cmdOpts~remove(w)
            opt = w~right(1)
            leave
         end
      end
      itr~next
   end

return opt

::routine getSysFileTreeOpts
   use arg cmdOpts
   switches = "FO"  -- SysFileTree(): "F"iles only, "O"nly fully qualified name
   opt = cmdOpts~remove("-r")
   if .nil <> opt then switches = switches||"S" -- add "S" switch for SysFileTree()
return switches

::routine hasHelpOpt
   use arg cmdLine, cmdOpts

   -- Common args asking for help:
   h = .set~of("?", "/?", "-?", "/H", "/h", "-H", "-h", "help", "/help", "--help")

   do i = 1 to cmdLine~words
     cmdOpts~put(cmdLine~word(i))
   end

return h~intersection(cmdOpts)~items <> 0

::routine doHelp
   parse source . . thisFile
   parse value filespec("N", thisFile) with thisName "." .
   say "rexx" thisName "[-opts] path2directory"
   say
   say '  Arguments are not required and can be in any order.  path2directory'
   say '  defaults to the current directory.  -opts consists of zero or more'
   say '  of the following, separated by spaces:'
   say
   say "  -r      look in subdirectories recursively"
   say '  -[1-9]  select formatter for printing results'
   say '            currently valid values are:'
   say '            -1 == simpleDumpTestResults()'
   say '            -2 == simpleFormatTestResults()'
   say '            default is -1'
   say
   say '  path2directory  path to directory to search for "*.testUnit" files'

return 0
