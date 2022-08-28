#!/usr/bin/env rexx
/* ------------------------ Apache Version 2.0 license -------------------------
 *    Copyright (C) 2022 Rony G. Flatscher
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        <a href="http://www.apache.org/licenses/LICENSE-2.0">http://www.apache.org/licenses/LICENSE-2.0</a>
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * -----------------------------------------------------------------------------
 *
 * purpose: demonstrate the new BSF4ooRexx850 external function BsfCommandHandler() to
            add Rexx command handlers (implemented in Java) at runtime
 *
 * @author Rony G. Flatscher
 * @since  2022-08-25
 */

say "1) default environment:" pp(address())
say
environment="TEST"   -- define environment name (can be any name in this case!)
say "   getting and adding a direct command handler for an environment named:" pp(environment)
commandHandler=.bsf~new("NetRexxRedirectingCommandHandler")  -- create the Java Rexx command handler
-- add the command handler to ooRexx defining the environment it serves
call BsfCommandHandler 'add', environment, commandHandler
say

say "   list of currently defined Java command handlers:"
do counter c item over bsfCommandHandler('List')
   say "       " c":" item
end
say
say "2) default environment:" pp(address())  -- query and display current environment
say
say "about to use the" pp(environment) "environment for our commands in a loop:"
say

line="-"~copies(79)

-- the command 'ping' never raises a condition nor exception
say .line "command:" pp(sourceline(.line+1))
address test "ping"
say "     RC:" pp(rc) ".rs="pp(.rs) "| RESULT="pp(result)
say line
say

-- the command 'hello' never raises a condition nor exception
say .line "command:" pp(sourceline(.line+1))
address test "hello"
say "     RC:" pp(rc) ".rs="pp(.rs) "| RESULT="pp(result)
say line
say

-- the command 'hello' never raises a condition nor exception
say .line "command:" pp(sourceline(.line+1))
address test "info"
say "     RC:" pp(rc) ".rs="pp(.rs) "| RESULT="pp(result)
say line
say

say "*--* "~copies(20)
say

-- this will cause a failure condition to be raised in the command handler
-- because neither output nor error got redirected, but are needed to receive
-- the result
signal on any name myLabel_1  -- transfer control in case of a condition/exception
say .line "command:" pp(sourceline(.line+1)) "(missing redirections to output or error; creates FAILURE condition)"
address test "rot13 abc"      -- a condition will be raised
myLabel_1:
   if .rs<>0 then       -- signal transfer, condition/exception occurred
   do
      co=condition('o') -- a condition raised?
      if \co~isNil then say ppCondition2(condition('o'))
   end
say "     RC:" pp(rc) ".rs="pp(.rs) "| RESULT="pp(result)
say .line "***" "result:" pp(result~makeString)
say line
say

say "*--* "~copies(20)
say


-- in =.array~of("HELLO, My Beloved World!" || .endofline)
-- in =.array~of("HELLO, My Beloved World!".endOfLine"this is the second line!")
in =.array~of("HELLO, My Beloved World!")
out=.array~new
err=.array~new

signal on any name myLabel_2
say .line "command:" pp(sourceline(.line+1))
address test "rot13" with input using (in) output using (out) error using (err)
myLabel_2:
   if .rs<>0 then       -- signal transfer, condition/exception occurred
   do
      co=condition('o') -- a condition raised?
      if \co~isNil then say ppCondition2(condition('o'))
   end
say "     RC:" pp(rc) ".rs="pp(.rs) "| RESULT="pp(result)
say "     in:" pp(in~makestring) "out:" pp(out~makestring) "err:" pp(err~makestring)
say line
say


signal on any name myLabel_3
out~empty
err~empty
drop result             -- RESULT got set last by 'err~empty'
say .line "command:" pp(sourceline(.line+1))
address test "rot13 abc" with output using (out) error using (err)
myLabel_3:
   if .rs<>0 then       -- signal transfer, condition/exception occurred
   do
      co=condition('o') -- a condition raised?
      if \co~isNil then say ppCondition2(condition('o'))
   end
say "     RC:" pp(rc) ".rs="pp(.rs) "| RESULT="pp(result)
say "     in:" pp(in~makestring) "out:" pp(out~makestring) "err:" pp(err~makestring)
say line
say

signal on any name myLabel_6
out~empty
err~empty
drop result             -- RESULT got set last by 'err~empty'
say .line "command:" pp(sourceline(.line+1)) "(no input redirection nor supplied data: ERROR condition will get raised)"
address test "rot13" with output using (out) error using (err)
myLabel_6:
   if .rs<>0 then       -- signal transfer, condition/exception occurred
   do
      co=condition('o') -- a condition raised?
      if \co~isNil then say ppCondition2(condition('o'))
   end
say "     RC:" pp(rc) ".rs="pp(.rs) "| RESULT="pp(result)
say "     in:" pp(in~makestring) "out:" pp(out~makestring) "err:" pp(err~makestring)
say line
say

say "*--* "~copies(20)
say

out~empty
err~empty
drop result             -- RESULT got set last by 'err~empty'
say .line "command:" pp(sourceline(.line+1))
address test "uppercase" with input using (in) output using (out)
say "     RC:" pp(rc) ".rs="pp(.rs) "| RESULT="pp(result)
say "     in:" pp(in~makestring) "out:" pp(out~makestring) "err:" pp(err~makestring)
say line
say

out~empty
err~empty
drop result             -- RESULT got set last by 'err~empty'
say .line "command:" pp(sourceline(.line+1))
address test "uppercase This text is supplied with the command in ooRexx!" with output using (out)
say "     RC:" pp(rc) ".rs="pp(.rs) "| RESULT="pp(result)
say "     in:" pp(in~makestring) "out:" pp(out~makestring) "err:" pp(err~makestring)
say line
say

out~empty
err~empty
drop result             -- RESULT got set last by 'err~empty'
say .line "command:" pp(sourceline(.line+1))
address test "lowercase" with input using (in) error using (err)
say "     RC:" pp(rc) ".rs="pp(.rs) "| RESULT="pp(result)
say "     in:" pp(in~makestring) "out:" pp(out~makestring) "err:" pp(err~makestring)
say line
say

out~empty
err~empty
drop result             -- RESULT got set last by 'err~empty'
say .line "command:" pp(sourceline(.line+1))
address test "lowercase This TEXT is SUPPLIED with THE command IN ooRexx!" with error using (err)
say "     RC:" pp(rc) ".rs="pp(.rs) "| RESULT="pp(result)
say "     in:" pp(in~makestring) "out:" pp(out~makestring) "err:" pp(err~makestring)
say line
say

say "*--* "~copies(20)
say
out~empty
err~empty
drop result             -- RESULT got set last by 'err~empty'
signal on any name myLabel_7
say .line "command:" pp(sourceline(.line+1)) "(will create an error condition)"
address test "some unknown command" -- will raise an error condition
myLabel_7:
   if .rs<>0 then       -- signal transfer, condition/exception occurred
   do
      co=condition('o') -- a condition raised?
      if \co~isNil then say ppCondition2(condition('o'))
   end
say "     RC:" pp(rc) ".rs="pp(.rs) "| RESULT="pp(result)
say line
say

say "*--* "~copies(20)
say "Done."

::requires "BSF.CLS"       -- get ooRexx-Java bridge
::requires "rgf_util2.rex" -- get access to ppCondtion2()
