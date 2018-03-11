#!/usr/bin/env rexx
/*  author:     Gil Barmwater
    name:       elevate.rex
    purpose:    allow a command to be run elevated under Windows XP or higher
    date:       2015-08-24
    usage:      elevate [-Wait] command
    return:     0 if the Wait option is specified and the command completes
                within the time out period or if Wait option is not specified;
                -1 if the Wait time expires before the command completes

    license:

    * ------------------------ Apache Version 2.0 license -------------------------
    *    Copyright (C) 2015 Gil Barmwater
    *
    *    Licensed under the Apache License, Version 2.0 (the "License");
    *    you may not use this file except in compliance with the License.
    *    You may obtain a copy of the License at
    *
    *        http://www.apache.org/licenses/LICENSE-2.0
    *
    *    Unless required by applicable law or agreed to in writing, software
    *    distributed under the License is distributed on an "AS IS" BASIS,
    *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    *    See the License for the specific language governing permissions and
    *    limitations under the License.
    * -----------------------------------------------------------------------------
    */
    parse arg theArgs
    if theArgs = "" then do
        say "Usage: specify the command line to be run as Administrator"
        say "       as the arguments to this script.  An optional -w may"
        say "       be specified prior to the command line data to cause"
        say "       the script to wait for the elevated prompt to end"
        say "       before terminating. The default wait time is 2 min. but"
        say "       can be overriden by specifying the number of sec."
        say "       immediately following the -w.  E.g. -w5 waits 5 seconds."
        exit
    end

    wait? = .false  -- default to .false
    if theArgs~word(1)~left(2)~upper = "-W" then do
        wait? = .true

        time2wait = 1200    -- default wait: 1200 tenths of a second (2 minutes)
        -- check for trailing time to wait (seconds)
        firstArg = theArgs~word(1)
        firstNumber = firstArg~verify("0123456789", "Match")
        if firstNumber > 0 then do
            tmpTime2wait = firstArg~substr(firstNumber)
            if tmpTime2wait~datatype("Whole") then
               time2wait = tmpTime2wait * 10    -- convert seconds to tenths
        end

        theArgs = theArgs~subword(2)   -- remove wait flag
    end
    done? = \wait?  -- if waiting, then not done, and vice versa

    -- build argument string for cmd.exe: first change directory to the current
    --  one, and then, if that was successful, run the specified target command
    cmdArgs = "/c cd /d" directory() "&&" theArgs
    if wait? then do
        -- create an external data queue for communication
        q_name = "cmd_running"
        call rxqueue C, q_name
        --  and make it active
        curr_q = rxqueue(S, q_name)
        -- add a command to the argument string which is executed regardless of
        --  the return code from the command that adds an entry to the queue
        cmdArgs ||= ' & rexx -e "call rxqueue s,'q_name';push ok"'
    end

    -- If elevation might not be required, the following comments show how to
    --  easily allow it to be overridden by setting an environment variable
/*  -- check for override to not use elevated prompt
    noUAC = (value("rexx_noUAC", , "ENVIRONMENT") = 1)
    verb = "runas open"~word(1 + noUAC) */

    verb = "runas"
    ShellObj = .OLEObject~New("Shell.Application")
    ShellObj~ShellExecute("cmd.exe", cmdArgs, , verb)

    if wait? then do
        say "waiting" time2wait/10 "second(s)..."
        -- loop until entry added to queue by elevated prompt or timeout
        loop i = time2wait to 1 by -1
            select
                when i // 10 = 0 then
                    .output~charout("/"i/10)    -- show seconds left
                when i // 5 = 0 then
                    .output~charout("+")        -- tick half a second
                otherwise
                    .output~charout(".")        -- tick 0.1 of a second
            end

            call sysSleep .1      -- sleep 1/10 of a second
            if queued() > 0 then do
                done? = .true
                pull .                  -- remove entry
                call rxqueue S, curr_q  -- reset active queue
                call rxqueue D, q_name  --  and delete communication queue
                leave
            end
        end
        if done? then
            say "done."
        else
            say " wait time ("time2wait/10" seconds) exceeded; wait terminated!"
    end
    return done? - 1