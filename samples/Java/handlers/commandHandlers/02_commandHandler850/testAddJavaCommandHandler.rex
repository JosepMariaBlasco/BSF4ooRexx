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
environment="EPOQUE" -- define environment name (can be any name in this case!)
say "   getting and adding a direct command handler for an environment named:" pp(environment)
commandHandler=.bsf~new("DirectCommandHandler")    -- create the Java Rexx command handler
-- add the command handler to ooRexx defining the environment it serves
call BsfCommandHandler 'add', environment, commandHandler
say

say "   list of currently defined Java command handlers:"
do counter c item over bsfCommandHandler('List')
   say "       " c":" item
end
say

say "   setting default environment to:" pp(environment)
address (environment) -- define default environment for commands consisting of a string only
say

say "2) default environment:" pp(address())  -- query and display current environment
say
say "about to use the" pp(environment) "environment for our commands in a loop:"
say

do i=1 to 15
   "helloWorld #" i     -- define and send a command to the default environment
   say "round #" right(i,2)": handler returned RC="pp(rc) "rc~class="pp(rc~class)
   say
end

::requires "BSF.CLS"    -- get ooRexx-Java bridge