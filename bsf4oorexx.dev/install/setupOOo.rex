#!/usr/bin/rexx
/*
   Name:    setupOOo.rex
   Purpose: create a shell script which sets the environment variable CLASSPATH
            to point to the Java archives needed to drive/automate OpenOffice.org
            resp. LibreOffice
   Author:  Rony G. Flatscher
   Version: 1.18
   Date:    2006-01-04
   Changed:
            2006-01-16, ---rgf, make sure CLASSPATH is used in any case
            2006-01-22, ---rgf, on Linux hint the user how to enter "setEnvironment4BSF4Rexx.sh"
            2006-01-24, ---rgf, now OOo-Reflection support unbundled, needs to be copied to
                                "OOo/program/classes" and then put into CLASSPATH
            2006-02-25, ---rgf, removed a quirk in a Unix string (delimiter part of string!),
                                "setEnvironment4OOo.*" is now copied to a PATH directory
            2007-07-07, ---rgf, changed hint-text for pointing to the OO directory 2.2
            2007-09-16, ---rgf, changed hint-text for pointing to the OO directory 2.3
            2008-05-17, ---rgf, now only runs on ooRexx due to usage of directives and OLEObject;
                                adds path to OOo-program-directory and jars to registry; uses
                                hive into which OOo got installed to (HKCU=user or HKLM=system);
                                supports OOo 2.x only
            2008-07-06, ---rgf, added path to soffice to CLASSPATH on Unix (now needed for OOo 2.4
                                e.g. on Ubuntu; fixed a typo in chmod; corrected an error in the
                                Unix-branch where home of soffice is looked for, if no argument
                                is given
            2008-07-12, ---rgf, changed sequence of OOo-jars to match what "unoinfo" of OOo 3.x shows,
                                added the routine "getClassPath4OOo()" which uses JavaInfo4BSF4RexxInstallation.java
                                to query and edit the necessary CLASSPATH string for interacting
                                with OOo
            2008-08-14, ---rgf, using USERID()-BIF
            2008-02-22, ---rgf, added "UNO_API_info.rex", "UNO_ReflectionUtil.cls" to the list of
                                programs to be copied to OOo/program
            2008-08-10, ---rgf, changed cmd.eCopy from "-plfv" to "-fv" to allow successful
                                execution from Ubuntu on a Windows NTFS-system;
                                on Unix: adding full path to setEnvironmentOOO.sh
            2008-08-20, ---rgf, changed PATH such that OOO-dir is now appended
            2009-03-26, ---rgf, moved "java" into quotes, such that it works on Unix-systems as well
            2009-10-31, ---rgf, - changed "BSF4Rexx" to "BSF4ooRexx"
            2010-03-07, ---rgf, - adding batch file for Vista/W7 to run the created
                                  installation files as local Administrator; doing same
                                  for Unix with 'sudo' (in case one wants to work via
                                  Explorer only as well)
                                - Linux: experimenting with 'ldconfig', such that OOo can find that
                                         shared library
            2010-03-20, ---rgf, - Unix: installing systemwide into /etc/profile, uninstall
                                        now uses sed to remove those entries with uninstall;
                                        /etc/profile seems to be only re-read if a logoff and
                                        logon occurs
            2010-05-27, ---rgf, - renamed extensions from ".rex" to ".rxo" to match renamed files
            2010-06-05, ---rgf, - added running "installOOoRexxMacros.rxo"
            2010-07-05, ---rgf, - renamed "add_ooRexxPackage.rex" to "add_ooRexxPackage.rxo"
            2010-07-27, ---rgf, - Windows: add file associations
            2010-07-31, ---rgf, - Linux: add file associations
            2010-08-10, ---rgf, - Linux: now adding setE* to /etc/profile and $HOME/.bashrc
            2010-08-15, ---rgf, - Linux: now supplying full path to "unopkg" to add_ooRexxPackage.rxo
            2010-08-17, ---rgf, - Linux: don't copy ooRexx-OOo-support files to OOo/program dir (not needed due to copying them tu /usr/bin)
            2010-08-24, ---rgf, - Windows: assume CLASSPATH is always set !
            2011-01-25/26, ---rgf, - Windows: if CLASSPATH set in HKCU, then make sure we refer to system's set value of CLASSPATH as well;
                                  escape "%" with "[25x]" in path
            2011-02-25, ---rgf (on the way to Tokyo) - add support for MacOSX
            2011-02-27, ---rgf, - fixed bug in determining MacOSX
                                - MacOSX: fixed bug in determining whether OOo is installed or not
                                - MacOSX: not setting up xdg-menu
            2011-03-01, ---rgf, - MacOSX: adjusted 'sed' arguments
            2011-03-02, ---rgf, - MacOSX: do not generate MacOSX home directory in setEnvironment4OOo.sh
            2011-04-10, ---rgf, - added support for LibreOffice (tested with LO 3.3.2) on
                                  Windows, MacOSX (Linux-Version was generic enough already)
            2011-05-28, ---rgf, - Unix: make sure newly created files are assigned to "admin" group and
                                  fully writable for "admin" members
            2011-05-31, ---rgf, - Unix: look for LibreOffice first in "/opt/libreoffice/program" (default installation
                                        location for genuine LO from www.LibreOffice.org)
            2011-06-08, ---rgf, - Unix: do not change group to "admin", e.g. Fedora Core may not have it
            2013-07-14, ---rgf, - Make output more verbose, such that logfile reflects what was found about OOo/LO
                                  (displays values for cmd.eSetPath and cmd.eSetCP)
            2013-07-15, ---rgf, - output additional log messages to ease debugging
                                - Unix: make sure to look for all kind of "libreoffice" variants

   last svn check-in: $Revision: 916 $ $Author: Administrator $ $Date: 2010-08-24 16:30:15 +0200 (Tue, 24 Aug 2010) $

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2006-2013 Rony G. Flatscher

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

/*

Ubuntu 6.10 (as of 2007-02-03):

OOO_pgms=/usr/lib/openoffice/program

# händisch:
export LD_LIBRARY_PATH=$OOO_pgms:$LD_LIBRARY_PATH

OOO_jars=/usr/share/java/openoffice

*/

cmd.=""                          /* default value for stem        */
parse upper source s +1          /* get first character of operating system in uppercase */
cmd.eIsUnix=(pos(s, "WO")=0)     /* neither Windows nor OS/2, hence Unix */

parse upper source s +1  -- get operating system name in uppercase
cmd.eIsMacOSX=(s="M")    -- determine whether running on "MACOSX"


-- cmd.eIsUnix=1     /* for debugging: force Unix-mode */


if cmd.eIsUnix=0 then
do
   cmd.eIsUnix=0                 /* Windows/OS2 style       */
   cmd.eFile.sep="\"
   cmd.ePath.sep=";"
end
else
do
   cmd.eIsUnix=1                 /* Unix style              */
   cmd.eFile.sep="/"
   cmd.ePath.sep=":"
end

                                 /* paths to Java and desired bin-dir given?  */
parse arg path2OOo               /* get path to OpenOffice.org/LibreOffice */
hive=""                    -- relevant for Windows registry

-- say "path2OOo=["path2OOo"]"

if path2OOo="" then        -- no path to OOo given
do
      -- cf.  <http://wiki.services.openoffice.org/wiki/Documentation/DevGuide/ProUNO/Java/Transparent_Use_of_Office_UNO_Components>
    if cmd.eIsUnix=1 then  -- Unix
    do
       if cmd.eIsMacOSX then
          cmd.ePath2OOoBase=getOOoHomeDirMacOSX() -- try to get OOo's home directory
       else
          cmd.ePath2OOoBase=getOOoHomeDirUnix() -- try to get OOo's home directory (works for OOo 2.x)
    end
    else                   -- Windows: get path to OOo from registry, use that hive to add PATH and CLASSPATH additions
    do
       parse value getOOoHomeDirWindows() with hive cmd.ePath2OOoBase
    end

    if \cmd.eIsMacOSX then	-- there is no trailing "/program" directory !
    do
         -- remove trailing "program"-directory
       lpos=lastPos(cmd.eFile.sep,cmd.ePath2OOoBase) -- get last occurrence of file separator
       if lpos>1 then
          cmd.ePath2OOoBase=substr(cmd.ePath2OOoBase, 1, lpos-1)  -- remove last directory ("program")
    end
end
else  -- path was given: needs to point to OOo base directory from which "program/soffice" can be derived
do
   /* check, whether correct directory given: */
   tmpPath=path2OOo~strip('Both')~strip("Both",'"') -- remove white space, quotes

      -- stream() returns canonical path
   if cmd.eIsUnix then
      tmpPath=stream(tmpPath || cmd.eFile.sep || "program" || cmd.eFile.sep || "soffice"    , "C", "QUERY EXISTS")
   else                 -- Windows
      tmpPath=stream(tmpPath || cmd.eFile.sep || "program" || cmd.eFile.sep || "soffice.exe", "C", "QUERY EXISTS")

   if tmpPath<>"" then           -- office executable found, extract path to it from stream()'s canonical path
   do
      lpos=lastPos(cmd.eFile.sep,tmpPath) -- get last occurrence of file separator
      if lpos>1 then
         lpos=lastPos(cmd.eFile.sep,tmpPath, lpos-1) -- get the second to last occurrence of the file separator

      if \cmd.eIsMacOSX then
      do
          if lpos>1 then             -- Bingo! Remove trailing "/program" on non MacOSX-systems
            cmd.ePath2OOoBase=substr(tmpPath,1,lpos-1)
      end
   end
end
cmd.ePath2OOoBase=canonizePath(cmd.ePath2OOoBase)

say "cmd.ePath2OOoBase:     ["cmd.ePath2OOoBase"]"


/*
oldDir=directory()               /* memorize actual directory                 */
cmd.ePath2OOoBase=directory(strip(path2OOo, 'B', '"'))   -- remove quotes, if any; change to directory
call directory oldDir            /* reset directory                           */
*/

-- temp
-- cmd.ePath2OOoBase="/opt/openoffice.org2.2

if cmd.ePath2OOoBase="" then         /* not found, give a message and abort ...   */
do
   say "OpenOffice./LibreOffice not installed?"
   say
   say "path to OpenOffice/LibreOffice directory" pp(path2OOo) "could not be found!"
   say
   say "     usage: rexx setupOOo [path-to-OpenOffice/LibreOffice-directory]"
   say

   if cmd.eIsUnix=0 then
   do
      say '   example: rexx setupOOo d:\Programme\OpenOffice.org 3'
   end
   else
   do
      say '   example: rexx setupOOo /opt/openoffice.org.3'
      say '            rexx setupOOo /usr/lib/openoffice'
   end
   exit -1
end

   -- on Windows: determine hive to use for "un/installOOo.cmd":
if cmd.eIsUnix=.false then    -- o.k. Windows, no hive given, determine hive to install to
do
/*
      -- remove trailing "\program" directory
    lpos=path2OOo~lastpos("\")         -- find last backslash
    if lpos>0 then
       path2OOo=path2OOo~left(lpos-1)  -- extract home-dir
*/

    if hive="HKLM" then    -- ooRexx in system hive (HKLM: local machine)
    do
       cmd.eRegistryPath     ="HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment\Path"
       cmd.eRegistryClassPath="HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment\CLASSPATH"
    end
    else                   -- ooRexx in user hive (HKCU: current user)
    do
       cmd.eRegistryPath     ="HKCU\Environment\Path"
       cmd.eRegistryClassPath="HKCU\Environment\CLASSPATH"
    end
end


if cmd.eIsMacOSX then
    cmd.ePath2OOoPrograms=cmd.ePath2OOoBase
else
    cmd.ePath2OOoPrograms=cmd.ePath2OOoBase     || cmd.eFile.sep || "program"

if \cmd.eIsMacOSX then  -- on MacOSX depend on "unoinfo"
   cmd.ePath2OOoJars    =cmd.ePath2OOoPrograms || cmd.eFile.sep || "classes"

cmd.eThisDirectory=directory() || cmd.eFile.sep -- save this directory

say "cmd.ePath2OOoPrograms: ["cmd.ePath2OOoPrograms"]"
say "cmd.ePath2OOoJars:     ["cmd.ePath2OOoJars"]"

/* for forcing testing Unix on Windows
j.cmd.eFile.separator="/" -- rgf, debug
j.cmd.ePath.separator=":" -- rgf, debug
*/

   /* parse path, put directories into the stem variable */
path.=""
call parsePath

cp=value("CLASSPATH", , "ENVIRONMENT") /* try to get the CLASSPATH value   */
/* cmd.eRgfReflectUNO="oorexx-uno.jar"  ---rgf, 2007-02-02: not needed anymore as part of "bsf-rexx-engine.jar" */
cmd.eSupportInfos=qc("Support: Internet newsgroup <news:comp.lang.rexx>, <http://www.OpenOffice.org> or via <http://www.RexxLA.org>")

-- 2010-03-19: create a unique needle (e.g. to allow deleting lines with sed from profiles later)
cmd.eInstallationNeedle="BSF4ooRexxNeedle - OpenOffice support" -- .dateTime~new~string

-- cmd.eIsUnix=.true -- rgf-DEBUG

if cmd.eIsUnix then
do
      -- get user's name
   tmpVal=value("SUDO_USER",,"ENVIRONMENT")  -- running as root via sudo ?
   if tmpVal<>"" then cmd.eUser=tmpVal       -- o.k., use this user
                 else cmd.eUser=value("USER",,"ENVIRONMENT")

   -- get and save logName and sudoUser-Name
   cmd.eLogName  = value('LOGNAME',    ,'ENVIRONMENT')  -- get current logname, could be 'root' if sudo'ing
   cmd.eSudoUser = value('SUDO_USER',  ,'ENVIRONMENT')  -- maybe sudo'ing, then who is the sudo user

   if cmd.eLogName="root", cmd.eSudoUser<>"" then
      cmd.eRealUserName=cmd.eSudoUser
   else
      cmd.eRealUserName=cmd.eLogName

   cmd.eSetOwner="chown" cmd.eRealUserName":admin"   -- newly created files, also assigned to "admin" group


   nl="0a"x                      /* Unix new-line character sequence (\n)  */
   cmd.eNullDevice = "/dev/null" /* null device  */

   cmd.eLeadIn="#!/bin/sh"nl     /* command to execute this file           */
   cmd.eEcho="echo"
   -- cmd.eCopy="cp -plfv"          /* preserve, hard-link, force, verbose: source target(dir)  */
   cmd.eCopy="cp -fv"            /* force, verbose: source target(dir) [works on FAT32/NTFS systems from Linux as well] */
   cmd.eDelete="rm -fv"
   cmd.eSetOOoHome = 'OOO_pgms='cmd.ePath2OOoPrograms
   cmd.eSetOOoJars = 'OOO_jars=$OOO_pgms' || cmd.eFile.sep || "classes"
   cmd.eJarVar = cmd.ePath.sep"$OOO_jars"cmd.eFile.sep   /* variable to use                        */

   if cmd.eIsMacOSX then
      cmd.eDLL="libBSF4ooRexx.dylib"  /* define name of dynamic link library    */
   else
      cmd.eDLL="libBSF4ooRexx.so"     /* define name of dynamic link library    */

--rgf,20100320   cmd.eSetOOoPgms = "OOoPgm=" || cmd.ePath2OOoPrograms

/*
   if cp="" then
      cmd.eSetCP  = "CLASSPATH=."            /* make sure local dir is searched!       */
   else
      cmd.eSetCP  = "CLASSPATH=$CLASSPATH"
*/


   -- rgf, 2010-03-20: only use $CLASSPATH, if set
   -- cmd.eSetCP= '[ "$CLASSPATH" != "" ] && TMPCP=.:$CLASSPATH || TMPCP=.:' || .endOfLine || -

   cmd.eSetCP= '[ "$CLASSPATH" != "" ] && TMPCP=$CLASSPATH: || TMPCP=' || .endOfLine || -
               'export CLASSPATH=$TMPCP'

   -- cmd.eSetCP2 = "export CLASSPATH"

   cmd.eSetPath="export PATH="   || "$PATH" || cmd.ePath.sep || "$OOO_pgms"

   cmd.eInstallScriptName="installOOo.sh"
   cmd.eUninstallScriptName="uninstallOOo.sh"

   cmd.eRunAsAdmin="sudo"        /* command to run as super user  */
   cmd.eInstallScriptName.RunAsAdmin="installOOo_sudo.sh"
   cmd.eUninstallScriptName.RunAsAdmin="uninstallOOo_sudo.sh"

   if cmd.eBinDir="" then
      cmd.eBinDir="/usr/bin"     /* directory where executables reside     */

   -- cmd.eSetEnvironmentScriptName="setEnvironment4OOo.sh"
   cmd.eSetEnvironmentScriptName=cmd.eThisDirectory || "setEnvironment4OOo.sh"
   cmd.eExecuteEnvironmentScript=". "cmd.eSetEnvironmentScriptName

   -- cmd.eSetEnvironment4BSFScriptName=cmd.eInstallDirectory || "setEnvironment4BSF.sh"
   cmd.eSetEnvironment4BSFScriptName=cmd.eThisDirectory || "setEnvironment4BSF.sh"

   cmd.eRunRexxScriptJava = cmd.eThisDirectory || "runRexxScriptJava.sh"
   cmd.eRunRexxScriptOOo  = cmd.eThisDirectory || "runRexxScriptOOo.sh"

   cmd.eSetExecutable    ="chmod 775"   /* set executable for all (user, group, other)  */
   cmd.eLineComment="#"

   cmd.eInstallSystemWide=.true           /* install into /etc/profile which supposedly works on all systems
                                             (hence not fiddling with /etc/profile.d which may or may not be
                                             supported by the Linux distribution;
                                             if .false, install into user's profile ~/.bashrc else */

   cmd.eAdd2BashRc   = "$HOME/.bashrc"    /* user's bash profile to add entry to       */
   cmd.eAdd2Profile  = "/etc/profile"     /* system wide setting                       */

      /* prolog and epilog used for ".bashrc" to make sure that CLASSPATH gets only extended, if
         "/etc/profile" has not run yet (and set CLASSPATH already to our needs) */
   cmd.eAdd2BashRcProlog = "if [ -z `echo $CLASSPATH | grep ""jurt.jar""` ] ; then" "#" cmd.eInstallationNeedle
   cmd.eAdd2BashRcEpilog = "fi" "#" cmd.eInstallationNeedle

   cmd.eAdd2BashRc.0 = 2
   -- cmd.eAdd2BashRc.1 = "    # allow BSF4ooRexx to find OOo and the OOo jars"
   -- cmd.eAdd2BashRc.2 = "    ." cmd.eSetEnvironmentScriptName ">/dev/null"
   -- cmd.eAdd2BashRc.1 = "    #" cmd.eInstallationNeedle "(created by" j.user.name")"
   cmd.eAdd2BashRc.1 = "    #" cmd.eInstallationNeedle "(created by" cmd.eUser || ")"

   cmd.eAdd2BashRc.2="if [ -f" cmd.eSetEnvironmentScriptName "];" -
                     "then ." cmd.eSetEnvironmentScriptName "; fi #" cmd.eInstallationNeedle
   if cmd.eIsMacOSX then --rgf, 2011-03-01: sed on MacOSX works differently, cf. <http://blog.mpdaugherty.com/2010/05/27/difference-with-sed-in-place-editing-on-mac-os-x-vs-linux/>
      cmd.e_sed_delete ="sed -i '' '/"cmd.eInstallationNeedle"/d'"
   else
      cmd.e_sed_delete ="sed -i '/"cmd.eInstallationNeedle"/d'"   -- 2010-03-19: sed command to delete entries from profile(s)

end
else  /* Windows, OS/2 (eCS), ... */
do
   cmd.eUser=value("USERNAME",,"ENVIRONMENT")

   nl="0d0a"x                    /* Windows/OS2 new-line character sequence (\n) */
   cmd.eNullDevice = "nul"       /* null device  */

   cmd.eEcho="@echo"
   cmd.eEchoOff="@echo off"
   cmd.eCopy="copy /y"
   cmd.eDelete="del /f"
   cmd.eSetOOoHome = 'set OOO_pgms='cmd.ePath2OOoPrograms
   cmd.eSetOOoJars = 'set OOO_jars=%OOO_pgms%'   || cmd.eFile.sep || "classes"

   cmd.eJarVar = cmd.ePath.sep"%OOO_Jars%"cmd.eFile.sep  /* variable to use                        */

   cmd.eDLL="BSF4ooRexx.dll"       /* define name of dynamic link library    */

--rgf,20100320   cmd.eSetOOoPgms = "set OOoPgm=" || cmd.ePath2OOoPrograms

/*
   if cp="" then
      cmd.eSetCP  = "set CLASSPATH=."        /* make sure local dir is searched!       */
   else
*/
   cmd.eSetCP  = "set CLASSPATH=.;%CLASSPATH%" -- rgf, 20100823: always assume CLASSPATH (e.g. BSF) exists already!

   cmd.eSetPath="set PATH="      || "%PATH%" || cmd.ePath.sep || "%OOO_pgms%"

   cmd.eInstallScriptName="installOOo.cmd"
   cmd.eUninstallScriptName="uninstallOOo.cmd"

   cmd.eRunAsAdmin=getRunAsCmd() /* command to run as local Administrator  */
   cmd.eInstallScriptName.RunAsAdmin="installOOo_runAsAdministrator.cmd"
   cmd.eUninstallScriptName.RunAsAdmin="uninstallOOo_runAsAdministrator.cmd"

   if cmd.eBinDir="" then
      cmd.eBinDir=getRexxDir()   /* directory where executables reside     */

   cmd.eSetEnvironmentScriptName="setEnvironment4OOo.cmd"
   cmd.eExecuteEnvironmentScript="  " cmd.eSetEnvironmentScriptName
   cmd.eSetEnvironment4BSFScriptName=cmd.eInstallDirectory || "setEnvironment4BSF.cmd"
   cmd.eLineComment="@REM "
end


   /* create necessary CLASSPATH    */

-- rgf, 2008-07-12: check whether "unoinfo" executable exists (starting with OOo 3.x), if so, use it
if cmd.eIsUnix then
do
   tmpPath=cmd.ePath2OOoPrograms || cmd.eFile.sep || "unoinfo"
   -- tmpPath=stream(cmd.ePath2OOoPrograms || cmd.eFile.sep || "unoinfo", "C", "QUERY EXISTS")
end
else                 -- Windows
do
   tmpPath=cmd.ePath2OOoPrograms || cmd.eFile.sep || "unoinfo.exe"
   -- tmpPath=stream(cmd.ePath2OOoPrograms || cmd.eFile.sep || "unoinfo.exe", "C", "QUERY EXISTS")
end

say "a) checking existence of ["tmpPath"]..."

tmpPath=stream(tmpPath, "C", "QUERY EXISTS")
say "b) query exists returns: ["tmpPath"]"

if tmpPath<>"" then
do
   cmd.eSetCPWithFullPath=getClassPath4OOo(tmpPath)
-- say "debug: cmd.eSetCPWithFullPath=["cmd.eSetCPWithFullPath"] <-- 000 <---"
end
else  -- assume pre OOo 3.0 setup configuration, create CLASSPATH string accordingly
do
   -- jars="jurt.jar unoil.jar ridl.jar juh.jar" cmd.eRgfReflectUNO, ---rgf, 2007-02-03: not needed anymore
   -- jars="jurt.jar unoil.jar ridl.jar juh.jar"
   -- use sequence of OOo 3.*
   jars="juh.jar ridl.jar jurt.jar unoil.jar"
        -- "sandbox.jar"               /* only needed for OOo < 2.0              */

   cmd.eSetCPWithFullPath=""
   do i=1 to words(jars)            /* loop over words                        */
      jar=word(jars, i)             /* extract word */
-- say "debug: jar=["jar"] <-"
--      cmd.eSetCP=cmd.eSetCP || cmd.eJarVar || jar  /* append it */

      if cmd.eSetCPWithFullPath<>"" then
         cmd.eSetCPWithFullPath=cmd.eSetCPWithFullPath || cmd.ePath.sep

      cmd.eSetCPWithFullPath=cmd.eSetCPWithFullPath || qualify(cmd.ePath2OOoJars || cmd.eFile.sep || jar)
   end

      -- make sure path to executable is also given in CLASSPATH (e.g. needed for OOo 2.4 on Unix)
   cmd.eSetCPWithFullPath=cmd.eSetCPWithFullPath || cmd.ePath.sep || cmd.ePath2OOoPrograms
end

/*
if cmd.eIsUnix=1 then
   cmd.eSetCP=cmd.eSetCP || cmd.ePath.sep || "$OOO_pgms"
else
   cmd.eSetCP=cmd.eSetCP || cmd.ePath.sep || "%OOO_pgms%"
*/


/* make sure that CLASSPATH is used in case this script runs at a time, when other programs have set
   the classpath environment variable
*/
/* rgf, 20100320
if cp="" then
do

--  call lineout fn, cmd.eSetCP || cmd.ePath.Sep || cmd.eSetCPWithFullPath   /* set CLASSPATH        */

  if cmd.eIsUnix=1 then
     cmd.eSetCP=cmd.eSetCP || "$OOO_pgms"  || cmd.ePath.sep

/*
  else -- rgf, 20100320
     cmd.eSetCP=cmd.eSetCP || cmd.ePath.sep || "%CLASSPATH%"
*/

end
-- */

-- say "debug: cmd.eSetCP            =["cmd.eSetCP"] <--"
-- say "debug: cmd.eSetCPWithFullPath=["cmd.eSetCPWithFullPath"] <--"
  -- rgf, 20100320: complete CLASSPATH definition
cmd.eSetCP=cmd.eSetCP || cmd.ePath.sep || cmd.eSetCPWithFullPath


/* determine files to copy to "OOo/program": needed for macros, we can point PATH there too!  */
if cmd.eIsMacOSX then  -- rgf, 2011-02-28, initially for MacOSX: do not copy dylib
   cmd.e4OOoPrograms="BSF.CLS UNO.CLS UNO_XINTERFACES.RXO UNO_CREATE_INTERFACE_LIST.RXO UNO_API_info.rxo UNO_ReflectionUtil.cls"
else
   cmd.e4OOoPrograms=cmd.eDLL "BSF.CLS UNO.CLS UNO_XINTERFACES.RXO UNO_CREATE_INTERFACE_LIST.RXO UNO_API_info.rxo UNO_ReflectionUtil.cls"


   /* create the scripts   */
call createEnvironmentScript

call createInstallScript

call createRunAsScripts       /* 2010-03-07: for Vista and higher */

call info         /* tell the user what to do with it */

exit


/* create the install script: this will copy the BSF4Rexx jar-file and dynamic link library
   to the appropriate Java extension directory
*/
createInstallScript: procedure expose cmd. path.
  fn=cmd.eInstallScriptName            /* name of install script              */
  ui=cmd.eUninstallScriptName          /* name of uninstall script            */

   /* copying setEnvironment4* to a PATH-directory */
  tgtDir=cmd.eBinDir       /* Windows: Rexx-home directory, Unix: /usr/bin  */

  call stream fn, "c", "open replace"
  call stream ui, "c", "open replace"

  -- hintString="created:" q(date("S") time()) "by user" q(value("USER",,"ENVIRONMENT"))
  hintString="created:" q(date("S") time()) "by user" q(userid())

  call lineout fn, cmd.eEcho qe("created:" q(date("S") time()))
  call lineout fn, cmd.eEcho qe("installing BSF4ooRexx ""OpenOffice/LibreOffice"" support...")
  call lineout fn, cmd.eEcho qe(".")
  call lineout fn, cmd.eEchoOff
  call lineout fn, ""

     /* uninstall: */
  call lineout ui, cmd.eEcho qe("created:" q(date("S") time()))
  call lineout ui, cmd.eEchoOff
  call lineout ui, cmd.eEcho qe("uninstalling BSF4ooRexx ""OpenOffice/LibreOffice"" support...")
  call lineout ui, cmd.eEcho qe(".")
  call lineout ui, ""
  call lineout ui, cmd.eEcho qe("(make sure that no instances of ""OpenOffice/LibreOffice"" are running!)")
  call lineout ui, ""

  -- uninstall Rexx macros, before we uninstall the rest of the pack ... ???
  call lineout ui, cmd.eEcho qe("uninstalling ooRexx OOo macros from OpenOffice/LibreOffice:")
  if cmd.eIsUnix=1 then
  do
     -- tmpCmd= "." cmd.eSetEnvironment4BSFScriptName  ">/dev/null;" "." cmd.eSetEnvironmentScriptName ">/dev/null;"
     -- rgf, 2010-08-21
     -- tmpCmd=cmd.eRunRexxScriptOOo "installOOoRexxMacros.rxo uninstall"
     -- call lineout ui, tmpCmd
     call lineout ui, cmd.eRunRexxScriptOOo "installOOoRexxMacros.rxo uninstall"
  end
  else
  do
     -- tmpCmd="call" cmd.eSetEnvironment4BSFScriptName  ">nul &&" -
     --               cmd.eSetEnvironmentScriptName      ">nul &&" -
     --               "rexx installOOoRexxMacros.rxo uninstall"

     -- on uninstall not needed as environment is already set o.k.
     -- call lineout ui,  cmd.eSetEnvironment4BSFScriptName  ">nul"
     -- call lineout ui,  cmd.eSetEnvironmentScriptName      ">nul"
     call lineout ui, "rexx installOOoRexxMacros.rxo uninstall"
  end

  -- call lineout ui, tmpCmd "rexx installOOoRexxMacros.rxo uninstall"
  call lineout ui, cmd.eEcho qe(" ")
  call lineout ui, ""

  /* rgf, 2006-07-11: add package to OOo via Rexx and the now established OOo-support */
  call lineout ui, cmd.eEcho qe("Removing package ""ScriptProviderForooRexx.oxt"" from OpenOffice/LibreOffice:")
  if cmd.eIsUnix=1 then
  do
--     call lineout ui, "."    cmd.eSetEnvironmentScriptName ">/dev/null;rexx add_ooRexxPackage.rxo remove 2>/dev/null"
     -- tmpCmd="rexx add_ooRexxPackage.rxo remove /unopkg:"cmd.ePath2OOoPrograms"/unopkg"
     -- call lineout ui, "."    cmd.eSetEnvironmentScriptName ">/dev/null;"tmpCmd "2>/dev/null"
     tmpCmd=cmd.eRunRexxScriptOOo "add_ooRexxPackage.rxo remove /unopkg:"cmd.ePath2OOoPrograms"/unopkg"
     call lineout ui, tmpCmd "2>/dev/null"
  end
  else
  do
     -- call lineout ui, "call" cmd.eSetEnvironmentScriptName ">nul && rexx add_ooRexxPackage.rxo remove 2>nul"
     -- tmpCmd="call" cmd.eSetEnvironment4BSFScriptName  ">nul &&" -
     --               cmd.eSetEnvironmentScriptName      ">nul &&" -
     --               "rexx  add_ooRexxPackage.rxo remove 2>nul"
     -- call lineout ui, tmpCmd

     call lineout ui, "rexx  add_ooRexxPackage.rxo remove 2>nul"
  end

  call lineout ui, cmd.eEcho qe(" ")
  call lineout ui, ""

/*  ---rgf, 2007-02-03: no need to copy as it is now part of "bsf-rexx-engine.jar"
     /* copying jar-file to OOo classes-directory */
  tgtDir=cmd.ePath2OOoJars
  tgtFile=stream(cmd.eRgfReflectUNO, "C","Query EXISTS")

  call lineout fn, cmd.eEcho qe("copying UNO reflection jar" q(cmd.eRgfReflectUNO) "to" q(tgtDir) "...")
  call lineout fn, cmd.eCopy qw(cmd.eRgfReflectUNO) qw(tgtDir)
  call lineout fn, ""

  call lineout ui, cmd.eEcho qe("deleting" q(cmd.eRgfReflectUNO)  "from directory" q(tgtDir) "...")
  call lineout ui, cmd.eDelete qw(tgtDir || cmd.eFile.sep || cmd.eRgfReflectUNO)
  call lineout ui, ""
*/

   /* 2007-02-03, ---rgf: copy OOo-Files to OOo directory; this way macros work without any
                          global changes to the environment */
  iw=words(cmd.e4OOoPrograms)
  if iw>0 then       /* if programs to copy to the appropriate OOo directory */
  do
     if cmd.eIsUnix=.true then
     do
        tmpTargetDir=q("/usr/bin")
        tmpTitleCopy="copying OOo-support Rexx programs to" tmpTargetDir "..."
        tmpTitleDel ="removing OOo-support Rexx programs from" tmpTargetDir "..."
     end
     else
     do
        tmpTargetDir=q(cmd.ePath2OOoPrograms)
        tmpTitleCopy="copying OOo-support Rexx programs to the OOo program directory:" tmpTargetDir "..."
        tmpTitleDel ="removing OOo-support Rexx programs from the OOo program directory:" tmpTargetDir "..."
     end

     call lineout fn, cmd.eEcho qe(tmpTitleCopy)
     call lineout ui, cmd.eEcho qe(tmpTitleDel)

     /* call lineout fn, cmd.eCopy qw(cmd.eRgfReflectUNO) qw(tgtDir) -- 2007-02-03, rgf: not needed anymore, part of "bsf-rexx-engine.jar" */

     do i=1 to iw
        pgm=word(cmd.e4OOoPrograms, i)
        pgmWithSourcePath=".."|| cmd.eFile.sep|| pgm -- word(cmd.e4OOoPrograms, i)

        call lineout fn, cmd.eEcho qe("copying" q(pgmWithSourcePath) "to" tmpTargetDir "...")
        call lineout ui, cmd.eEcho qe("deleting" q(pgm)  "from directory" tmpTargetDir "...")

        if cmd.eIsUnix=.false then  -- in Windows: copy to OOo/program; new strategy for Linux since 20100815, rgf
        do
           call lineout fn, cmd.eCopy qw(pgmWithSourcePath) qw(cmd.ePath2OOoPrograms)
           call lineout ui, cmd.eDelete qw(cmd.ePath2OOoPrograms || cmd.eFile.sep || pgm)
        end
        else   -- in Unix: copy to /usr/bin, ignore lib´BSF4ooRexx.so, which gets linked to its appropriate lib directory
        do
           if pgm~left(3)<>"lib" then
           do
              call lineout fn, cmd.eCopy qw(pgmWithSourcePath) qw("/usr/bin")
              call lineout ui, cmd.eDelete qw("/usr/bin/" || cmd.eFile.sep || pgm)
           end
        end

        call lineout fn, ""
        call lineout ui, ""
     end
  end

/*
   /* tgtFile=stream(cmd.eSetEnvironmentScriptName, "C","Query EXISTS") */
  tgtFile=cmd.eSetEnvironmentScriptName
  call lineout fn, cmd.eEcho qe("copying"  q(tgtFile) "to" q(tgtDir) "...")
  call lineout fn, cmd.eCopy qw(tgtFile) qw(tgtDir)
  call lineout fn, ""

  call lineout ui, cmd.eEcho qe("deleting" q(tgtFile)  "from directory" q(tgtDir) "...")
  call lineout ui, cmd.eDelete qw(tgtDir || cmd.eFile.sep || cmd.eSetEnvironmentScriptName)
  call lineout ui, ""
*/

   -- 2008-05-17, ---rgf: now add necessary path and classpath values to registry
  call lineout ui, ""
  call lineout fn, ""

  if cmd.eIsUnix=1 then          /* Unix only, append ". setEnvironment..." to .bashrc  */
  do
     bWriteProlog=.false  -- for "/etc/profile" no epilog/prolog, for .bashrc: yes!

     do tmpTgt over .array~of(cmd.eAdd2Profile, cmd.eAdd2BashRc)
        if cmd.eIsMacOSX, bWriteProlog then     -- on MacOSX do not create .bashrc
           leave

        appendTo=">>" tmpTgt

        call lineout fn, ""
        call lineout fn, cmd.eEcho qc("adding entries to the end of" tmpTgt "...")

-- rgf, 2010-08-14: do not write empty line anymore
--        call lineout fn, cmd.eEcho qc(" ") appendTo

        if bWriteProlog then
           call lineout fn, cmd.eEcho qc(cmd.eAdd2BashRcProlog) appendTo

        do i=1 to cmd.eAdd2BashRc.0
           call lineout fn, cmd.eEcho qc(cmd.eAdd2BashRc.i) appendTo
        end

        if bWriteProlog then
           call lineout fn, cmd.eEcho qc(cmd.eAdd2BashRcEpilog) appendTo

        call lineout fn, ""

        call lineout ui, cmd.eEcho qe("removing BSF4ooRexx' OpenOffice/LibreOffice entries from" tmpTgt "...")
        call lineout ui, cmd.e_sed_delete tmpTgt
        call lineout ui, ""

        bWriteProlog=.true
     end
---------------------
/* rgf, 2010-08-10
     if cmd.eInstallSystemWide=.true then -- install systemwide?
        tmpTgt=cmd.eAdd2Profile
     else
        tmpTgt=cmd.eAdd2BashRc

     call lineout fn, ""
     -- call lineout fn, cmd.eEcho qc("adding entries to the end of" cmd.eAdd2BashRc "...")
     call lineout fn, cmd.eEcho qc("adding entries to the end of" tmpTgt "...")

     -- appendTo=">>" cmd.eAdd2BashRc
     appendTo=">>" tmpTgt


     call lineout fn, cmd.eEcho qc(" ") appendTo
     -- call lineout fn, cmd.eEcho qc("#" hintString":") appendTo

     do i=1 to cmd.eAdd2BashRc.0
        call lineout fn, cmd.eEcho qc(cmd.eAdd2BashRc.i) appendTo
     end
     call lineout fn, ""

     call lineout ui, cmd.eEcho qe("removing BSF4ooRexx' OpenOffice.org entries from" tmpTgt "...")
     call lineout ui, cmd.e_sed_delete tmpTgt
     call lineout ui, ""
*/
----<----

      if \cmd.eIsMacOSX then	-- do not create file assosiations on MacOSX
      do
               -- 2010-07-27, create/remove fileAssocations
         call lineout fn, "./fileAssociations_for_rxo_add.sh"
         call lineout fn, ""

         call lineout ui, "./fileAssociations_for_rxo_remove.sh"
         call lineout ui, ""
      end

  end
  else      -- add statements to incorporate paths into registry
  do
      cmd="call rexx orx2reg.rex"      -- invoke utility to interact with Windows registry

      hint="Creating a backup of PATH and CLASSPATH registry values..."
      call lineout fn, cmd.eEcho qc(hint)
      call lineout ui, cmd.eEcho qc(hint)

      bkpFile="registryValues-"||date("S")"T"time("N")~changestr(":","")
      call lineout fn, cmd ">" bkpFile"-beforeAddOOoPathValues.bat"
      call lineout ui, cmd ">" bkpFile"-beforeDelOOoPathValues.bat"

      call lineout ui, ""
      call lineout fn, ""

         -- add OOo path to PATH
      arg="/key:"q(cmd.eRegistryPath) "/path:"q(cmd.ePath2OOoPrograms)
      tmpStr1=cmd "/add"    arg
      tmpStr2=cmd "/remove" arg
         -- inform user
      -- call lineout fn, cmd.eEcho qc(tmpStr1)
      -- call lineout ui, cmd.eEcho qc(tmpStr2)
      call lineout fn, cmd.eEcho qc("add OpenOffice/LibreOffice-paths to environment variable 'Path' via registry ...")
      call lineout ui, cmd.eEcho qc("remove OpenOffice/LibreOffice-paths from environment variable 'Path' via registry ...")

         -- carry out operation
      call lineout fn, tmpStr1
      call lineout ui, tmpStr2

      call lineout ui, ""
      call lineout fn, ""

         -- add bsf-jars to CLASSPATH

         -- make sure the current directory is searched as well
      if cmd.eRegistryClassPath~left(5)="HKCU\" then  -- rgf, 2011-01-25: make sure that system set CLASSPATH is honored !
         arg="/key:"q(cmd.eRegistryClassPath) "/path:"q("%CLASSPATH%;"cmd.eSetCPWithFullPath)
      else
         arg="/key:"q(cmd.eRegistryClassPath) "/path:"q(cmd.eSetCPWithFullPath)

         arg=arg~changestr("%", "[25x]") -- make sure that cmd.exe does not expand

      tmpStr1=cmd "/add"    arg
      tmpStr2=cmd "/remove" arg
         -- inform user
      -- call lineout fn, cmd.eEcho qc(tmpStr1)
      -- call lineout ui, cmd.eEcho qc(tmpStr2)
      call lineout fn, cmd.eEcho qc("add OpenOffice/LibreOffice-jars to environment variable 'CLASSPATH' via registry ...")
      call lineout ui, cmd.eEcho qc("remove OpenOffice/LibreOffice-jars from environment variable 'CLASSPATH' via registry ...")

         -- carry out operation
      call lineout fn, tmpStr1
      call lineout ui, tmpStr2

      call lineout ui, ""
      call lineout fn, ""

        -- 2010-07-27, rgf: create/remove fileAssocations
      call lineout fn, "regedit /s fileAssociations_for_RXO_add.reg"
      call lineout fn, "associationChanged.exe"
      call lineout fn, ""

      call lineout ui, "regedit /s fileAssociations_for_RXO_remove.reg"
      call lineout ui, "associationChanged.exe"
      call lineout ui, ""

  end


/*
  call lineout fn, cmd.eEcho qe("If addressing OOo from the command line, then setup the environment beforehand using:")
  call lineout fn, ""
  call lineout fn, cmd.eEcho qe("   " cmd.eExecuteEnvironmentScript)
  call lineout fn, ""
  call lineout fn, cmd.eEcho qe("done.")
  call lineout fn, ""
*/

   /* rgf, 2006-07-11: add package to OOo via Rexx and the now established OOo-support */
  call lineout fn, cmd.eEcho qe("adding package ""ScriptProviderForooRexx.oxt"" to OpenOffice/LibreOffice:")
  if cmd.eIsUnix=1 then
  do
--     call lineout fn, "."    cmd.eSetEnvironmentScriptName ">/dev/null;rexx add_ooRexxPackage.rxo"
     -- tmpCmd="rexx add_ooRexxPackage.rxo /unopkg:"cmd.ePath2OOoPrograms"/unopkg"
     -- call lineout fn, "."    cmd.eSetEnvironmentScriptName ">/dev/null;"tmpCmd "2>/dev/null"

     tmpCmd=cmd.eRunRexxScriptOOo "add_ooRexxPackage.rxo /unopkg:"cmd.ePath2OOoPrograms"/unopkg"
     call lineout fn, tmpCmd "2>/dev/null"

  end
  else
  do
     -- call lineout fn, "call" cmd.eSetEnvironmentScriptName ">nul && rexx add_ooRexxPackage.rxo"

     -- tmpCmd="call" cmd.eSetEnvironment4BSFScriptName  ">nul &&"  -
     --               cmd.eSetEnvironmentScriptName      ">nul &&"  -
     --               "rexx add_ooRexxPackage.rxo"
     -- call lineout fn, tmpCmd

     call lineout fn, "call" cmd.eSetEnvironment4BSFScriptName  ">nul"
     call lineout fn, "call" cmd.eSetEnvironmentScriptName      ">nul"
     call lineout fn, "rexx add_ooRexxPackage.rxo"

  end

   /* rgf, 2010-06-05: add OOo macros to OOo using Rexx and the now established OOo-support */
  call lineout fn, ""
  call lineout ui, ""

  call lineout fn, cmd.eEcho qe("adding ooRexx OOo macros to OpenOffice/LibreOffice:")

  if cmd.eIsUnix=1 then
  do
      -- rgf, 20100814
     -- tmpCmd= "." cmd.eSetEnvironmentScriptName ">/dev/null;"
     -- tmpCmd= "." cmd.eSetEnvironment4BSFScriptName  ">/dev/null;" "." cmd.eSetEnvironmentScriptName ">/dev/null;"
     -- rgf, 2010-08-21:
     tmpCmd=cmd.eRunRexxScriptOOo "installOOoRexxMacros.rxo install"


/*
     call lineout fn, "."    cmd.eSetEnvironmentScriptName ">/dev/null;rexx installOOoRexxMacros.rxo install"
     call lineout ui, "."    cmd.eSetEnvironmentScriptName ">/dev/null;rexx installOOoRexxMacros.rxo uninstall"
*/
  end
  else
  do
     -- tmpCmd="call" cmd.eSetEnvironment4BSFScriptName  ">nul &&"  -
     --               cmd.eSetEnvironmentScriptName      ">nul &&"  -
     --               "rexx installOOoRexxMacros.rxo install"

     tmpCmd="rexx installOOoRexxMacros.rxo install"
/*
     call lineout fn, "call" cmd.eSetEnvironmentScriptName ">nul && rexx installOOoRexxMacros.rxo install"
     call lineout ui, "call" cmd.eSetEnvironmentScriptName ">nul && rexx installOOoRexxMacros.rxo uninstall"
*/
  end

  -- call lineout fn, tmpCmd "rexx installOOoRexxMacros.rxo install"
  call lineout fn, tmpCmd

  lastMessage="done."
  if cmd.eInstallSystemWide=.true then -- installed systemwide?
     lastMessage=lastMessage "PLEASE logoff and logon for the changes to take affect!"

  call lineout fn, cmd.eEcho qe(lastMessage)

  call lineout fn, ""
  call lineout fn, cmd.eEcho qe("/// Please close ALL instances of OpenOffice/LibreOffice and the quickstarter.")
  call lineout fn, cmd.eEcho qe("(This will allow OpenOffice/LibreOffice to fully initialize and as a result find ooRexx!)")
  call lineout fn, ""

  call lineout fn, ""
  call lineout fn, cmd.eEcho cmd.eSupportInfos
  call lineout fn, ""
/*
  if cmd.eIsUnix=.false then
  do
     call lineout fn, "@pause"
     call lineout fn, ""
  end
*/

  call lineout ui, cmd.eEcho qe("done.")
  call lineout ui, ""
  call lineout ui, cmd.eEcho cmd.eSupportInfos
  call lineout ui, ""
/*
  if cmd.eIsUnix=.false then
  do
     call lineout ui, "@pause"
     call lineout ui, ""
  end
*/


  call stream fn, "c", "close"
  call stream ui, "c", "close"

  if cmd.eSetExecutable<>"" then       /* set file executable (Unix)    */
  do
     cmd.eSetExecutable cmd.eInstallScriptName

-- rgf,2011-06-08: FC has no "admin" group!
--     cmd.eSetOwner      cmd.eInstallScriptName

     cmd.eSetExecutable cmd.eUninstallScriptName

-- rgf,2011-06-08: FC has no "admin" group!
--     cmd.eSetOwner      cmd.eUninstallScriptName
  end
  return



/* Windows only: create runAs batch file for Vista and higher, rgf 2010-03-07 */
createRunAsScripts: procedure expose cmd. path.

  -- hintString="created:" q(date("S") time()) "by user" q(j.user.name)
  hintString="created:" q(date("S") time()) "by user" q(cmd.eUser)

  runAsCmd=cmd.eRunAsAdmin
  arr=.array~of(cmd.eInstallScriptName,   cmd.eInstallScriptName.RunAsAdmin, -
                cmd.eUninstallScriptName, cmd.eUninstallScriptName.RunAsAdmin)

  do i=1 to arr~items by 2
     script=arr[i]
     fn    =arr[i+1]
     call stream fn, "c", "open replace"
     -- header
     call lineout fn, cmd.eEcho qe(hintString)
     call lineout fn, cmd.eEcho qe("running" pp(script) "script as local Administrator ...")
     call lineout fn, ""
     tmpCmd=runAsCmd qw(script)
     call lineout fn, cmd.eEcho qe(tmpCmd "...")
     call lineout fn, tmpCmd
     call stream fn, "c", "close"
  end

  return




/* create the test script which sets up all needed parameters to run Rexx programs from
   this directory */
createEnvironmentScript: procedure expose cmd. path.
  fn=cmd.eSetEnvironmentScriptName

  call stream fn, "c", "open replace"
  call lineout fn, cmd.eEchoOff
  call lineout fn, cmd.eLineComment "created:" q(date("S") time())
  call lineout fn, cmd.eLineComment "(allows running OOo scripts from the commandline)"

  if cmd.eIsUnix=.true then   -- don't echo in Unix as this script may be executed on every new shell
     call lineout fn, cmd.eLineComment "setting the CLASSPATH environment variable to point to the OpenOffice/LibreOffice jar files"
  else
     call lineout fn, cmd.eEcho qe("setting the CLASSPATH environment variable to point to the OpenOffice/LibreOffice jar files")

--  call lineout fn, cmd.eEcho qe("(make sure that the environment is set for BSF4ooRexx as well)...")
  call lineout fn, ""

--  call lineout fn, cmd.eLineComment "make sure we use the same OOo-BSF4Rexx programs the OOo macros do (hence" q("OOo_pgms") "listed first)"
  if \cmd.eIsMacOSX then
  do
     call lineout fn, cmd.eLineComment "define and lookup OOo's program directory first"
     call lineout fn, cmd.eSetOOoHome
  end

-- call lineout fn, cmd.eSetOOoJars
--  call lineout fn, ""

--   call lineout fn, cmd.eLineComment "make sure only BSF4Rexx files from the installation are used, hence listing first"

  if cmd.eIsUnix=.false then  -- 2010-08-14: on Linux the needed Rexx scripts are copied to '/usr/bin' already, hence no need anymore
  do
     call lineout fn, cmd.eSetPath        /* set PATH        */
     call lineout fn, ""
  end

/*
  call lineout fn, cmd.eLineComment "copy UNO-reflection jar to OOo/SO program/classes directory..."
  call lineout fn, cmd.eCopy qw(stream(cmd.eRgfReflectUNO, "C","Query EXISTS")) qw(cmd.ePath2OOoJars)
  call lineout fn, ""
*/

  call lineout fn, cmd.eLineComment "set CLASSPATH environment variable ..."
--  call lineout fn, cmd.eLineComment "please note: if using OOo/SO version 2.0 or greater, you do not need ""sandbox.jar"""

--   call lineout fn, cmd.eSetCP          /* set CLASSPATH        */
  -- rgf, 20100320: call lineout fn, cmd.eSetCP || cmd.ePath.Sep || cmd.eSetCPWithFullPath   /* set CLASSPATH        */
  call lineout fn, cmd.eSetCP /* set CLASSPATH        */

--  call lineout fn, cmd.eSetCP2         /* export it on Unix    */
  call lineout fn, ""

  if cmd.eIsUnix=.false then  -- don't echo in Unix as this script may be executed on every new shell
     call lineout fn, cmd.eEcho qe(".")

--  call lineout fn, cmd.eEcho qe("It is advised to add these environment settings to your systems profile!")
--  call lineout fn, cmd.eEcho qe("(This way it is always assured, that Rexx can find the OOo Java classes.)")

/*
  if cmd.eIsUnix=1 then
  do
     call lineout fn, cmd.eEcho qe("E.g. add the following entries to your '~/.bashrc':")
     call lineout fn, cmd.eEcho qe(" ")
     call lineout fn, cmd.eEcho qe("   # allow using OpenOffice.org via its Java interface classes")
     call lineout fn, cmd.eEcho qe("   ." cmd.eSetEnvironmentScriptName ">/dev/null")
  end
*/

  call lineout fn, ""

  call stream fn, "c", "close"

  if cmd.eSetExecutable<>"" then       /* set file executable (Unix)    */
  do
     cmd.eSetExecutable cmd.eSetEnvironmentScriptName

-- rgf,2011-06-08: FC has no "admin" group!
--     cmd.eSetOwner      cmd.eSetEnvironmentScriptName
  end

  return



pp: procedure                    /* poor man's "pretty" print     */
  return "[" || arg(1) || "]"


q: procedure                     /* enclose string into apostrophes  */
  return """" || arg(1) || """"

   /* "qe": quote echo string: enquote entire string on Unix only */
qe: procedure expose cmd. path.
  if  cmd.eIsUnix=1 then return "'" || arg(1) || "'"
                    else return arg(1)


   /* "qe": quote fully-qualified paths in Windows as they may contain a blank */
qw: procedure expose cmd. path.
  if  cmd.eIsUnix=0 then return '"' || arg(1) || '"'
                    else return arg(1)


   /* "qc": quote string in apostrophes, if Unix, in quotes, if Windows (for support infos that contain <, >) */
qc: procedure expose cmd. path.
  if  cmd.eIsUnix=0 then return '"' || arg(1) || '"'     /* Windows  */
                    else return "'"  || arg(1) || "'"    /* Unix     */


iif: procedure                   /* VBasic-like function          */
  if arg(1)=1 then return arg(2)
  return arg(3)


info:                            /* tell user what has happened   */
  say "setupOOo.rex - info():"
  say
  say "cmd.eSetPath:"
  say cmd.eSetPath
  say "---"
  say "cmd.eSetCP:"
  say cmd.eSetCP
  say "---"
  say
  say
  say "the following scripts have been created:"
  say
  say " " pp(cmd.eInstallScriptName)": run to install ooRexx-UNO support for OpenOffice/LibreOffice"
/*
  if cmd.eIsUnix=0 then
     say "       usage:" cmd.eInstallScriptName
  else
     say "       usage:" q("./"cmd.eInstallScriptName)
  say
*/
  if cmd.eIsUnix=0 then
     say "       to install as Administrator run:" cmd.eInstallScriptName.RunAsAdmin
  else
     say "       to install as root run:" q("./"cmd.eInstallScriptName.RunAsAdmin)
  say
  say

  say " " pp(cmd.eUninstallScriptName)": run to uninstall ooRexx-UNO support for OpenOffice/LibreOffice"
/*
  if cmd.eIsUnix=0 then
     say "       usage:" cmd.eUninstallScriptName
  else
     say "       usage:" q("./"cmd.eUninstallScriptName)
  say
*/
  if cmd.eIsUnix=0 then
     say "       to uninstall as Administrator run:" cmd.eUnInstallScriptName.RunAsAdmin
  else
     say "       to uninstall as root run:" q("./"cmd.eUnInstallScriptName.RunAsAdmin)
  say

  -- say " " pp(cmd.eSetEnvironmentScriptName)": helper script for setting up the environment"
  say " " pp(filespec("Name",cmd.eSetEnvironmentScriptName))": helper script for setting up the environment"
  say "              for running OpenOffice.org (OOo)/LibreOffice (LO) ooRexx programs"
  say
/*
  if cmd.eIsUnix=0 then
     say "       usage:" cmd.eSetEnvironmentScriptName
  else
     say "       usage:" q(". ./"cmd.eSetEnvironmentScriptName)
  say
*/

/*
  say
  say "     remarks: - After running this script you may invoke any BSF4ooRexx-program"
  say "                automating OOo/SO via BSF4ooRexx', e.g. (try it!)"
  say
  if cmd.eIsUnix=0 then
     say "                  " cmd.eSetEnvironmentScriptName
  else
     say "                  " q(". ./"cmd.eSetEnvironmentScriptName)

  say "                  " "rexx testOOo.rex"
  say
  say "Good luck! :-)  [You may also want to take a look at this generated script.]"
*/
  say "Have fun and enjoy! --rony"
  exit 0

/* Get path to Rexx or Regina from PATH environment variable and return it;
   Windows/OS2
*/
getRexxDir: procedure expose cmd. path.
     /* use this precedence order to locate a path where to copy Rexx-files to  */
  dir=findPath("\ooREXX \regina rexx")
  if dir<>"" then return dir

  eyeCatcher="?!no-rexx-path-found!?"
  say "PROBLEM: could not find explicit path to a Rexx-directory!"
  say "         Please adjust the created scripts to point to any one directory listed"
  say "         with the environment variable ""PATH"", ie. replace" pp(eyecatcher) "with"
  say "         any of the listed PATH directory, and then run the scripts."
  return eyecatcher              /* indicate that not found       */



/* parse path into stem path. */
parsePath: procedure expose cmd. path.
/*
say "... j.path.separator=["j.path.separator"]"
say "... j.file.separator=["j.file.separator"]"
*/

  p=value("PATH", , "ENVIRONMENT")
  i=0
  do while p<>""
     parse var p chunk (cmd.ePath.sep) p
     i=i+1

     if cmd.eIsUnix=1 then
        path.i=chunk
     else
        path.i=changestr('"', chunk, "")  /* remove any quotes from any part of the Windows path */

     path.i.upperCase=translate(chunk)  /* uppercase version */
/*
say "... i="i "chunk="pp(chunk)
*/

     path.0=i
  end

  return

/* find needle in path and return it   */
findPath: procedure expose cmd. path.
   parse upper arg needles    /* blank delimited, translated into uppercase   */
   do i=1 to words(needles)   /* iterate over needles                         */
      n=word(needles, i)
      do k=1 to path.0        /* iterate over paths      */
         if pos(word(needles,i), path.k.upperCase)>0 then
            return path.k
      end
   end
   return ""



/* Returns the received path in canonized form, i.e. removes "." and "..", wherever possible */
canonizePath: procedure expose cmd. path.
   parse arg p

   sep=cmd.eFile.sep             -- get file separator
   tmpArr=p~makeArray(sep)       -- split string
   newArr=.array~new

   k=0      -- index into new array
   do i=1 to tmpArr~items
      w=tmpArr[i]
      if w="." then iterate      -- no need to keep "current directory"
      if w="..", k>0 then        -- can we remove super directory (not if it is ".." already!)
      do
         if newArr[k]<>".." then -- a directory, removing it would be o.k.
         do
               -- on Windows make sure first word is not a drive letter! (leave that untouched)
            if k=1, cmd.eIsUnix=.false, length(newArr[k]=2), substr(newArr[k],2,1)=":" then
               nop
            else
            do
               newArr~remove(k)  -- remove directory
               k=k-1             -- adjust k
               iterate           -- process next chunk
            end

         end
      end
      k=k+1                         -- increment index
      newArr[k]=w                   -- save
   end

   return newArr~makeString("LINE",sep)




/*  2008-05-17, ---rgf
    where is OOo-installed in HKCU or HKLM (we install into the same hive) ?

    returns HKCU or HKLM as first word delimited with a blank from the path
    to OOo's program directory
*/
::routine getOOoHomeDirWindows
  regpaths=.array~of("Software\OpenOffice.org\UNO\InstallPath\", "Software\LibreOffice\UNO\InstallPath\")
  wsh=.OleObject~new("WScript.Shell")

  do key over regpaths
     do hive over .array~of("HKCU", "HKLM")  -- current user/local machine
        val=readOOoRegEntry(wsh, hive, key)  -- try to fetch a value
        if val<>"" then return val
     end
  end
  return ""                               -- no entry for OOo/SO/LO found !

::routine readOOoRegEntry   -- read key from hive, return hive and value back
  use arg wsh, hive, key
  signal on syntax
  return hive wsh~regRead(hive"\"key)      -- gets triggered, if key does not exist in registry
syntax:
  return ""



::routine getOOoHomeDirWindows_Original
  key="Software\OpenOffice.org\UNO\InstallPath\"
  wsh=.OleObject~new("WScript.Shell")

  signal on syntax name syntax1           -- gets triggered if key does not exist in registry
  return "HKCU" wsh~regRead("HKCU\"key)   -- stored in HKCU (user) ?

syntax1:
  signal on syntax name syntax2
  return "HKLM" wsh~regRead("HKLM\"key)   -- stored in HKLM (system) ?

syntax2:                   -- no OOo-entry
  return ""



/* Look for "soffice" on environment variable PATH. If a link, find link's target.
*/
::routine getOOoHomeDirUnix

/*
    -- rgf, 2011-05-31: check for "/opt/libreoffice/soffice" first (default installation location for http://www.LibreOffice.org)
  path="/opt/libreoffice/program"
  if sysFileExists(path"/soffice") then
     return path
*/

   -- cater for new name variants of "libreoffice"
  do needle over .list~of("/opt/*openoffice*", "/opt/libreoffice*")
     -- call sysFileTree "/opt/libreoffice*", "dirs.", "DO"
     call sysFileTree needle, "dirs.", "DO"
     do i=1 to dirs.0
        if sysFileExists(dirs.i"/program/soffice") then
        do
           say "getOOoHomeDirUnix(): returning ["dirs.i"/program" || "]"
           return dirs.i"/program"     -- supply subdirectory "/program" as that will be cut off by caller
        end
     end
  end


  val=value("PATH", , "environment")   -- get PATH environment variable's value
  paths=val~makeArray(":")       -- create array from string, use Unix path delimiter (":")

  do p over paths                -- iterate over individual paths
     fn= p"/soffice"
     call sysFileTree fn, "files.", "F"

     if files.0>0 then           -- found!
     do
        parse var files.1 . "->" +3 fullPath    -- try to parse target, if a link in hand

        if fullPath="" then      -- nope, seems to be not a link
        do
           "ls -al" fn "| rxqueue"   -- address shell, let output be queued to us
           if queued()>0 then    -- found, try to get linked path
              parse pull . '->' +3 fullPath

           if fullPath<>"" then
           do
              if left(fullPath,1)="." then	-- path relative, if so prepend original path
                 fullPath=p||'/'||fullPath  -- prepend
           end
           else  -- no link available, use fn
              fullPath=fn
        end

            -- remove trailing "/soffice"
        lpos=fullPath~lastPos("/")           -- get last "/"
        fullPath=fullPath~left(lpos-1)     -- remove trailing "/soffice"

        return fullPath          -- return path to OOo
     end
  end
  return ""                      -- not found


/* rgf, 2011-02-25, try to find OOo's home dir on MacOSX, 2011-04-10: add search for LibreOffice */
::routine getOOoHomeDirMacOSX
-- well defined home directory
  paths=.array~of("/Applications/OpenOffice.org.app/Contents/MacOS", "/Applications/LibreOffice.app/Contents/MacOS")

  do path over paths
     soffice=path"/soffice"
     if SysFileExists(soffice) then return path
  end

  return ""  -- not found (not installed)



   -- queries needed OOo configuration using "unoinfo" (available starting with OOo 3.x)
::routine getClassPath4OOo
   parse arg path2unoinfo

      -- if path given, and has a blank, make sure it is enquoted so to not have the shell break it up!
   if path2unoinfo<>"", pos(" ",path2unoinfo)>0, path2unoinfo~left(1)<>'"' then
      path2unoinfo='"'path2unoinfo'"'

   say "getClassPath4OOo() - directory():  ["directory()"]"
   say "getClassPath4OOo() - path2unoinfo: ["path2unoinfo"]"

   cmd="java -cp . JavaInfo4BSF4RexxInstallation getClassPath4OOo" path2unoinfo "| rxqueue"
   say "getClassPath4OOo() - cmd:          ["cmd"]"
   cmd

   parse pull "CLASSPATH " classPath2OOo  -- parse classpath string from return value

   do while queued()>0           -- make sure queue is empty (in case of an error)
      parse pull .
   end

   say "getClassPath4OOo() returns:        ["classPath2OOo"]"
   return classPath2OOo



-- return "runas" command for this PC's local Administrator, rgf, 2010-03-07
::routine getRunAsCmd public
   wnw = .OLEObject~New("WScript.Network")

   computerName=wnw~ComputerName
   -- userDomain  =wnw~UserDomain
   -- userName    =wnw~UserName

   return "runas /env /user:"wnw~ComputerName"\Administrator"

