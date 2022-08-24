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
 * @since  2022-08-24
 */

say "1) default environment:" pp(address())
say
environment="TEST"   -- define environment name (can be any name in this case!)
say "   getting and adding a direct command handler for an environment named:" pp(environment)
commandHandler=.bsf~new("RedirectingCommandHandler")  -- create the Java Rexx command handler
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

say "command: 'address (environment) ""ping""'"
address (environment) "ping"
say "     RC:" pp(rc) ".rs=".rs
say

/*
say "   setting default environment to:" pp(environment)
address (environment) -- define default environment for commands consisting of a string only
say
-- */

line="-"~copies(79)

say "command: address test ""ping"""
address test "ping"
say "     RC:" pp(rc) ".rs=".rs
say line
say

say "command: 'hello'"
address test "hello"
say "     RC:" pp(rc) ".rs=".rs
say line
say

say "command: 'rot13 abc'"
signal on any name myLabel_1
address test "rot13 abc"    -- a condition will be raised
myLabel_1:
co=condition('o') -- a condition raised?
if \co~isNil then say ppCondition2(condition('o'))
say "     RC:" pp(rc) ".rs=".rs
say line
say

signal off any
signal on  any name myLabel_1bb
say "command: address test ""ping"""
address test "ping"
myLabel_1bb:
co=condition('o') -- a condition raised?
if \co~isNil then say ppCondition2(condition('o'))
say "     RC:" pp(rc) ".rs=".rs
say line
say



in =.array~of("hello, my beloved world!")
out=.array~new
err=.array~new

say "command: 'rot13' with input using (in) output using (out) error using (err)"
signal on any name myLabel_2
address test "rot13 abc" with input using (in) output using (out) error using (err)
myLabel_2:
co=condition('o') -- a condition raised?
if \co~isNil then say ppCondition2(condition('o'))
say "     RC:" pp(rc) ".rs=".rs
say "     in:" pp(in~makestring) "out:" pp(out~makestring) "err:" pp(err~makestring)
say line
say

say "command: 'rot13' with output using (out) error using (err)"
signal off any
signal on syntax
signal on any name myLabel_3
out~empty
err~empty
co=.nil
address test "rot13 abc" with output using (out) error using (err)
say ".//\\."
myLabel_3:
co=condition('o') -- a condition raised?
if \co~isNil then say ppCondition2(condition('o'))
say "     RC:" pp(rc) ".rs=".rs
say "     in:" pp(in~makestring) "out:" pp(out~makestring) "err:" pp(err~makestring)
say line
say

/*
say "command: 'rot13 abc' with output using (out)"
address (environment) "rot13 abc" with output using (out)
say "     RC:" pp(rc) ".rs=".rs "out->"pp(out~makeString)
say
*/

exit

syntax:
   co=condition('o')
   say ppJavaExceptionChain(co,.true)
   say "---"
   raise propagate





::requires "BSF.CLS"       -- get ooRexx-Java bridge
::requires "rgf_util2.rex" -- get access to ppCondtion2()
