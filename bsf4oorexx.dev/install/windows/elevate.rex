/*  author:    Gil Barmwater
    name:      elevate.rex
    purpose:   allow a command to be run elevated under Windows XP or higher
    date:      2015-08-06
    usage:     elevate [-Wait] command

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
        say "       before terminating."
        exit
    end

    bWait=.false     -- default to .false
    if theArgs~word(1)~left(2)~upper = "-W" then do
        bWait = .true

        time2wait=1200           -- default wait 1200 tenth of a second (120 seconds, 2 minutes)
        firstArg=theArgs~word(1) -- check whether flag has trailing time to wait in seconds
        numbers="0123456789"
        firstNumber=verify(firstArg, numbers, "Match")
        if firstNumber>0 then
        do
            tmpTime2wait=substr(firstArg,firstNumber)
            if datatype(tmpTime2wait, "Whole") then
               time2wait=tmpTime2wait*10  -- tickers are in 1/10 of a second, hence multiply by 10
        end

        theArgs = theArgs~subword(2)   -- remove wait flag
    end

         -- Windows: double "&&" - the following command will only be executed, if the first one was successful (rc=0)
    cmdArgs = "/c cd /d" quote(directory()) "&&" theArgs
    dummyFile="cmd.running"
    if bWait then do
        call lineout dummyFile -- create zero-length file

         -- Windows: single '&' - the following command will always be executed after the previous
        cmdArgs ||= " & del" quote(qualify(dummyFile))
    end
/*  -- check for override to not use elevated prompt
    noUAC = (value("rexx_noUAC", , "ENVIRONMENT") = 1)
    verb = "runas open"~word(1 + noUAC)
*/  verb = "runas"
    ShellObj = .OLEObject~New("Shell.Application")
    ShellObj~ShellExecute("cmd.exe", cmdArgs, , verb)

    if bWait=.true then
    do
       say "waiting" time2wait/10 "second(s)..."
       loop i=time2wait to 1 by -1
                if i//10=0 then .output~charout("/"i/10) -- show seconds left
           else if i// 5=0 then .output~charout("+")     -- tick half a second
           else                 .output~charout(".")     -- tick one tenth of a second

           call sysSleep .1      -- sleep 1/10 of a second
           if \SysFileExists(dummyFile) then
              leave
       end
       if SysFileExists(dummyFile) then
       do
          say " waiting time ("time2wait/10" seconds) exceeded, prematurely ending wait!"
          exit -1       -- return non-zero return code to indicate this condition
       end
       say "done."
    end

::routine quote
  return '"' || arg(1) || '"'

::routine pp
  return "["arg(1)"]"
