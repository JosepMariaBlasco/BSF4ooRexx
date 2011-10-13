#!/usr/bin/rexx
/*
   Name:    elevate.rex
   Author:  Rony G. Flatscher
   Date:    2010-08-27/28

   Usage:   rexx elevate.rex program [arguments]
            ... will "runas" given 'program', supplying 'arguments', if any
                program ends with ".cmd", then "cmd.exe" will be "runas" launched
                program ends with ".rex", then "rexx.exe" will be "runas" launched
                program ends with a WSH file extension, then "wscript.exe" will be "runas" launched

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

parse arg pgmName args
tmpName=fileSpec("Name",pgmName)    -- extract name only, important e.g. for "..\some.rex" or the like
parse upper var tmpName name"."ext

rexxExtensions=.set~of("REX", "REXX", "ORX", "CLS", "TESTGROUP", "TESTUNIT", -
                       "RXO", "RXJ", "JREXX", "REXXJ")

wscriptExtensions=.set~of("JSE", "JS", "VBE", "VBS", "WSF", "WSH", "RXS")

if ext="CMD" then       -- elevate cmd.exe
do
   pgm2exec="cmd.exe"
   args="/c" arg(1)
end
else if rexxExtensions~hasIndex(ext) then -- elevate rexx.exe
do
   pgm2exec="rexx.exe"
   args=arg(1)
end
else if wscriptExtensions~hasIndex(ext) then -- elevate wscript.exe
do
   pgm2exec="wscript.exe"
   args=arg(1)
end
else                          -- elevate program supplied as first argument
do
   pgm2exec=pgmName
end

if bDebug then say "pgm2exec=["pgm2exec"] args=["args"] ..."   -- debug output

sh=.OleObject~new("Shell.Application")
sh~ShellExecute(pgm2exec, args, directory(), "runas")


