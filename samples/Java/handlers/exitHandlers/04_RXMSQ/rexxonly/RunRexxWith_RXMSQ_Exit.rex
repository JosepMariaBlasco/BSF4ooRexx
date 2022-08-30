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
 * purpose: demonstrate how to implement an ooRexx handler in ooRexx that does exactly
 *          the same as the Java handler in the super directory
 *
 * @author Rony G. Flatscher
 * @since  2022-08-30
 */
mgr      = .bsf~new("org.apache.bsf.BSFManager")
re       = mgr~loadScriptingEngine("rexx")
rexxconf = re~getRexxConfiguration
say "default rexxconf="pp(rexxconf~toString)

clzRexxExitHandler=bsf.loadClass("org.rexxla.bsf.engines.rexx.RexxExitHandler")
reh      = .RXMSQ_Exit~new    -- create an instance of the Rexx exit handler
jeh      = BsfCreateRexxProxy(reh, , "org.rexxla.bsf.engines.rexx.AbstractExitHandler")
rexxconf~addExitHandler(clzRexxExitHandler~RXMSQ, jeh)

say
say "after configuration:"
say "edited  rexxconf="pp(rexxconf~toString)

-- Rexx code to run
rexxCode= "call 'testRexxWith_RXMSQ_Exit.rxj'        ;" -
          "::requires BSF.CLS                        ;"    -- get ooRexx support (camouflage Java as ooRexx)

signal on syntax
-- invoke the interpreter and call the Rexx program in the supplied file name
re~apply('testRexxWith_RXMSQ_Exit.rxj', 0, 0, rexxCode, .nil, .nil)
mgr~terminate        -- make sure that the Rexx interpreter instance gets terminated!
exit

syntax:
  co=condition('o')
  say ppJavaExceptionChain(co,.true)
  say "---"
  raise propagate


::requires BSF.CLS      -- get ooRexx support (camouflage Java as ooRexx)

::class RXMSQ_Exit
::method init
  expose counter
  counter = 0           -- count # of invocations

::method handleExit
  expose counter
  use arg slot, exitNumber, subFunction, parmBlock, slotDir

  jSelf=slotDir~javaObject    -- get Java instance to send it directly messages from this method

  counter+=1
  .error~charout("(RXMSQ_Exit-side) [RXMSQ_exit] exitNumber=["exitNumber"] subFunction=["subFunction"], counter: "counter)

  select
     when subFunction=jSelf~RXMSQPLL then       -- 1 - pull string from external queue
     do
         parmBlock[1]="<value from REXX (!) exit handler, pretend this was pulled from an external queue, exit invocation counter=["counter"]>"
         .error~say(", RXMSQPLL returns:" pp(parmBlock[1]))
         return jSelf~RXEXIT_HANDLED
     end

     when subFunction=jSelf~RXMSQPSH then       -- 2 - push string to external queue
     do
         flag=parmBlock[1]       -- fetch flag array (boolean array)
         .error~say(", RXMSQPSH received:" pp(parmBlock[2])", LIFO="pp(flag[1]))
         return jSelf~RXEXIT_HANDLED
     end

     when subFunction=jSelf~RXMSQSIZ then       -- 3 - return number of items in external queue
     do
         parmBlock[1]="123"      -- return value
         .error~say(", RXMSQSIZ returns:" pp(parmBlock[1]))
         return jSelf~RXEXIT_HANDLED
     end

     when subFunction=jSelf~RXMSQNAM then       -- 4 - set new external queue name
     do
         .error~say(", RXMSQNAM received:" pp(parmBlock[1]))
         return jSelf~RXEXIT_HANDLED
     end

     otherwise
            -- future safe should there ever be a new subfunction added after 2012-02-20/2022-08-22
        return jSelf~RXEXIT_NOT_HANDLED
  end

