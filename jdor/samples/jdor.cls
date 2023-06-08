#!/usr/bin/env rexx
/*
   name:       jdor.cls
   purpose:    make it easy for classic Rexx programmers to take advantage of
               the new dynamic loadable Rexx command handlers of ooRexx 5.0 made
               available by BSF4ooRexx850. They need to call/require this package
               and thereafter can use the public routine "addJdorHandler([environmentName])"
               to create a JDOR handler and register it with ooRexx using "environmentName",
               if omitted, then "JDOR" will be used for "environmentName".

               "JDOR" stands for "Java2D for ooRexx"

   author:     Rony G. Flatscher
   date:       2022-09-03
   license:    Apache License 2.0
 * ------------------------ Apache Version 2.0 license -------------------------
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
*/

/** Creates a new command handler that will serve the environment by the name
 *  of the optional environmentName argument.
 *  @param environmentName the address environment name to use, if omitted defaults to "JDOR"
*/
::routine addJdorHandler   public
   use strict arg environmentName="JDOR"

   call BsfCommandHandler "add", environmentName, .bsf~new("org.oorexx.handlers.jdor.JavaDrawingHandler")

::requires "BSF.CLS"    -- get ooRexx-Java bridge
