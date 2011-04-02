#!/usr/bin/rexx
/*
   Name:    rexxpaws.rex
   Purpose: Linux replacement for Windows' "rexxpaws.exe" which pauses at the end of execution of a Rexx script
   Author:  Rony G. Flatscher
   Version: 1.00
   Date:    2010-08-09
   Changed: ---
   usage:   rexx rexxpaws.rex name-of-rexx-script [/args arg1 arg2 ... argn]

   last svn check-in: $Revision: 855 $ $Author: Administrator $ $Date: 2010-08-01 00:09:21 +0200 (Sun, 01. Aug 2010) $

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2010 Rony G. Flatscher

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

parse caseless arg script "/args" args
   -- remove leading and trailing spaces
script=strip(script)       -- strip spaces+tabs on both ends
args  =strip(args, "L")    -- strip leading spaces+tabs

parse source . invocationType thisFullPath
thisName=filespec("Name",thisFullPath)    -- extract filename only

-- say "invocationType=["invocationType"]" "arg(1)=["arg(1)"]" "script=["script"]" "args=["args"]"
signal on any

condObj=.nil
if args="" then   -- execute Rexx script
   call (script)  -- no args
else
   call (script) args   -- with supplied args

rc=0
signal end

any:              -- some condition got raised
   condObj=condition("Object")   -- get condition object
   call dumpConditionDir condObj -- dump condition object
   rc=condObj~rc

end:
   -- say thisName": Hit enter to end the" pp(script,0) "program ..."
   say "["thisFullPath"]: Hit enter to end the" pp(script,0) "program ..."
   parse pull input

   if condObj<>.nil then raise propagate
   exit rc


::routine dumpConditionDir
  use arg condObj

  .error~say("Condition object contains the following information:")
  .error~say
  indent1="    "
  indent2=" "~copies(18)
  indexArray=condObj~allIndexes~sort
  do idx over indexArray
     val=condObj[idx]
     .error~charout(indent1 pp(idx) pp(val))
     if val~isA(.collection) then   -- value is a collection, show items
     do
         .error~say(" "val~items "item(s):")
         valSupp=val~supplier       -- get a supplier object
         do while valSupp~available
            .error~say(indent2 ppIndex(valSupp~index, 6) pp(valSupp~item))
            valSupp~next
         end
     end
     else
        .error~say
  end


::routine pp      -- enclose string value in square brackets
  use arg pp, minWidth=13
  return ("["pp"]")~left(max(minWidth,length(pp)+2))

::routine ppIndex
  use arg pp, minWidth=4
  return "[" || pp~right(max(minWidth,length(pp))) || "]"

