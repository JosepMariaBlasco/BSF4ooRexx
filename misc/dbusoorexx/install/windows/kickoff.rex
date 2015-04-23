/*
   Name:    kickoff.rex
   Author:  Rony G. Flatscher
   Date:    2015-04-22

   Usage:   rexx kickoff install|reinstall|uninstall
            ... will try to run "..\install_dbusoorexx.rex" elevated, if this causes
                an error, just runs it without; this way starting with Windows2000/XP
                elevation is attempted, the fallback is to run re/un/installation as is

   Version: 200.20150422
   Changes: - 2014-04-26, rgf: use "elevate{32|64}.exe" on Windows Vista and higher
                         to run the [re|un]install elevated
            - 2015-04-22, rgf: adapted for dbusoorexx installation (from BSF4ooRexx installation)
            - 2015-04-23, rgf: make sure on WinXP that the installation script runs once only

   Info-URLs:  <http://technet.microsoft.com/en-us/magazine/2007.06.utilityspotlight.aspx>
               <http://technet.microsoft.com/en-us/magazine/2008.06.elevation.aspx>
               <http://forums.techarena.in/vista-administration/977482.htm>
               <http://blogs.technet.com/b/heyscriptingguy/archive/2005/10/31/how-can-i-start-an-application-from-an-hta.aspx>

               ad elevate-tool, cf: <http://jpassing.com/2007/12/08/launch-elevated-processes-from-the-command-line/>,
               licensed under the MIT-license (see file "license_mit.txt" or
               <http://opensource.org/licenses/mit-license.php>)


   Last svn check-in: $Revision: 912 $ $Author: Administrator $ $Date: 2010-08-23 16:49:55 +0200 (Mon, 23 Aug 2010) $

   License:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2015 Rony G. Flatscher

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
.local~kickoff.bDebug=.false        -- debug switch

parse source . . s

parse upper arg args .
if args="" | wordPos(args, "INSTALL REINSTALL UNINSTALL")=0 then
do
   parse source . . s
   .error~say(s": no argument or unknown argument (one of 'install', 'reinstall', or 'uninstall') given, aborting ...")
   exit -1
end

select
   when args="INSTALL"   then args="-i"
   when args="REINSTALL" then args="-r"
   when args="UNINSTALL" then args="-u"
end

pgm2exec="rexx.exe"
workDir =directory()

/* Get Windows version:
      use "runas" via WShell:
                        XP                   = 5.1 (ooRexx returns 5.01)
                        Server 2003          = 5.2

      from here on use Elevate32.exe/Elevate64.exe:
                        Vista & Server 2008  = 6.0
                        W7 & Server 2008 R2  = 6.1
                        W8 & RT & Server 2012= 6.2
                        W8.1 & Server 2012 R2= 6.3
*/
parse value sysVersion() with . winVersion

if .kickoff.bDebug then say "pgm2exec=["pgm2exec"] args=["args"] winVersion=["winVersion"]..."   -- debug output

signal on any        -- if something goes wrong with wscript, then address the plain command

if winVersion<6 then    -- up to and including XP, Server 2003
do
   mode="sh~ShellExecute(...)"
   -- args ="..\install_dbusoorexx.rex" args  -- build argument to be passed to Rexx

   args = '"'qualify("..\install_dbusoorexx.rex")'"' args -- fully qualify script
say "---> using 'Shell.Application'"
   if .kickoff.bDebug<>.true then
   do
      sh=.OleObject~new("Shell.Application")
-- say "---> using 'Shell.Application', BEFORE runas, pgm2exec="pp(pgm2exec) "args="pp(args) "workDir="pp(workDir)
      sh~ShellExecute(pgm2exec, args, workDir, "runas")
-- say "---> using 'Shell.Application', after  runas, pgm2exec="pp(pgm2exec) "args="pp(args) "workDir="pp(workDir)
   end
   else  -- debug mode
   do
      say "sh~ShellExecute(...) worked..."
      say "now raising exception..."
      signal 99.901
      parse pull answer
   end
end
else  -- Vista and up
do
say "===>"
--   rexxExe     = quote( qualify( value("REXX_HOME",,"ENVIRONMENT")"\rexx.exe" ) )
   rexxExe     = "rexx.exe"   -- will be found along the path
--   setupScript = quote( qualify(workDir"\..\setupAllAndRun.rex") )   -- fully qualify script
--   setupScript = "..\install_dbusoorexx.rex"  -- fully qualify script
   setupScript = '"'qualify("..\install_dbusoorexx.rex")'"'  -- fully qualify script
   rexxBits=getRexxBitness()
   elevateExe = "Elevate"rexxBits".exe"
   mode="Elevate"rexxBits".exe"
   cmd= elevateExe "-wait" "-k" rexxExe setupScript args

   if .kickoff.bDebug<>.true then
   do
      say "elevation mode, command: ["cmd"]..."
      say "waiting on elevated command line window to be closed (enter 'exit' there) ..."
      cmd               -- let subcommand handler process the command
   end
   else  -- debug mode
   do
      say "elevation mode, command: ["cmd"]..."
      say "now raising exception..."
      signal 99.901
      parse pull answer
   end
end
exit 0

any:                 -- something went wrong, try to invoke without elevation

   co=condition(o)
   say "kickoff.rex: condition occurred:"
   do idx over co~allindexes~sort
      say "   "idx":" pp(co[idx])
   end
   say
   say "Hit enter to end (in syntax condition handler) ..."
   parse pull .


   say quote(mode) "did NOT work, trying to invoke without elevation at all: ["pgm2exec args"]..."
   if .kickoff.bDebug=.true then
   do
      parse pull answer -- wait for user to hit enter
   end
   else                 -- try to execute without elevation
   do
      pgm2exec args
   end

   exit 1


-- return bitness of Rexx interpreter (e.g. 32, 64, etc.)
::routine getRexxBitness
   -- starting with ooRexx 4.2.0 we can get the bitness information out of the version string
  parse version '_'version'(' '_' bitness '-'
  if version>="4.2", bitness<>"" then
  do
     if .kickoff..bDebug=.true then say "kickoff.rex/getRexxBitness, >= 4.2, bitness="||bitness
     return bitness
  end

  -- do not use ADDRESS CMD as on Linux this causes an error :(
  "rexx -v 2>&1 | rxqueue"
  bitness=.nil                  -- default value
  do while queued()>0           -- make sure we process entire queue (to empty it)
     parse pull hint ":" val .
     if hint="Addressing Mode" then bitness=val
  end

  if .kickoff..bDebug=.true then say "kickoff.rex/getRexxBitness, < 4.2, bitness="||bitness
  return bitness

::routine quote
  return '"' || arg(1) || '"'

::routine pp               -- "pretty-print"
  return "["arg(1)"]"
