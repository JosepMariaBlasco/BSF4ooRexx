/*
   Name:    kickoff.rex
   Author:  Rony G. Flatscher
   Date:    2010-08-30

   Usage:   rexx kickoff install|reinstall|uninstall
            ... will try to run "..\setupAllAndRun.rex" elevated, if this causes
                an error, just runs it without; this way starting with Windows2000/XP
                elevation is attempted, the fallback is to run re/un/installation as is

   Version: 1.00
   Changes: ---

   Info-URLs:  <http://technet.microsoft.com/en-us/magazine/2007.06.utilityspotlight.aspx>
               <http://technet.microsoft.com/en-us/magazine/2008.06.elevation.aspx>
               <http://forums.techarena.in/vista-administration/977482.htm>
               <http://blogs.technet.com/b/heyscriptingguy/archive/2005/10/31/how-can-i-start-an-application-from-an-hta.aspx>

   Last svn check-in: $Revision: 912 $ $Author: Administrator $ $Date: 2010-08-23 16:49:55 +0200 (Mon, 23 Aug 2010) $

   License:

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
bDebug=.false        -- debug switch

parse source . . s

parse arg args .
if args="" | wordPos(args~upper, "INSTALL REINSTALL UNINSTALL")=0 then
do
   parse source . . s
   .error~say(s": no argument or unknown argument (one of 'install', 'reinstall', or 'uninstall') given, aborting ...")
   exit -1
end

args ="..\setupAllAndRun.rex" args  -- build argument to be passed to Rexx
pgm2exec="rexx.exe"
workDir =directory()

if bDebug then say "pgm2exec=["pgm2exec"] args=["args"] ..."   -- debug output

signal on any        -- if something goes wrong with wscript, then address the plain command
sh=.OleObject~new("Shell.Application")
if bDebug<>.true then
   sh~ShellExecute(pgm2exec, args, workDir, "runas")
else  -- debug mode
do
   say "sh~ShellExecute(...) worked..."
   say "now raising exception..."
   signal 99.901
   parse pull answer
end
exit 0

any:                 -- something went wrong, try to invoke without elevation
if bDebug<>.true then pgm2exec args

if bDebug then
do
   say "sh~ShellExecute(...) did NOT work, invoking without elevation: ["pgm2exec args"]..."
   parse pull answer
end
exit 1

