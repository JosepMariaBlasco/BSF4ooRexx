#!/usr/bin/rexx
/*
   Name:    setupBSF.rex
   Purpose: set up BSF4Rexx such, that it can be used easily (attempt to serve users
            without any knowledge about Java installation issues)
   Author:  Rony G. Flatscher
   Date:    2006-01-07
   Version: 1.31
   Changed:
            2015-05-27, ---rgf, - fixed (unexplainable, probably change mistake) mix-up in the code
                                  section that tries to determine the 32-bit java.exe on a 64-bit Windows;
                                  added further code to test that 32-bit java.exe path on a 64-bit Windows
                                  indeed is pointing to %ProgramFiles(x86)%
            2015-05-12, ---rgf, - trying to find a 32-bit "java.exe", if ooRexx is 32-bit but runs on a
                                  64-bit Windows and Java cannot be found by the usual means (via PATH,
                                  nor via registry); now looks in the 32- and/or 64-bit "ProgramFiles"\Java
                                  directory or subdirectories for "java.exe" using SysFileTree(); if multiple
                                  found then return the one with the highest index, assuming that that is
                                  the latest Java (i.e. Java subdirectories get listed ascendingly by their
                                  contained version number)
                                - make sure that we check for the minium ooRexx version 4.1.0
            2014-09-15, ---rgf, - create_REXXJ_Script() now honors environment variable"BSF4Rexx_JavaStartupOptions"
            2014-05-25, ---rgf, - getRexxBitness(): use "parse version" starting with ooRexx 4.2 (encodes bitness in version string)
                                - use elevation starting with Windows Vista (Windows version 6.0)
            2013-07-14, ---rgf, - Make output more verbose, such that logfile reflects environment at installation time
                                  (displays values for cmd.eSetBSF4REXX_HOME, cmd.eSetPath and cmd.eSetCP), will
                                  allow to analyze Java at hand
                                - add log message, if environment variable 'processor_architeW6432' is present
            2012-02-25, ---rgf, - Unix: remove "-n" flag from "ldconfig"
                                - Unix: detect target so-dir by looking for the lib-dir librexx.so got installed to
            2011-06-08, ---rgf, - Unix: do not change group to "admin", e.g. Fedora Core may not have it
            2011-05-28, ---rgf, - Unix: if run on Unix make sure all dirs/files are set to owner "user:admin",
                                         chmod to 775 for dirs and 664 on files, such that "admin" users can
                                         remove dirs and files as well
            2011-05-24, ---rgf, - general: changed logic to determine path to java binary as early as possible
                                - Windows: use registry's JRE entry, if java.exe cannot be found
            2011-04-21, ---rgf, - Windows: adjust for 64-bit behaviour, if Rexx is 32-bit (needing 32-bit Java):
                                           generated batch files need to refer to %windir%\SysWOW64\java.exe
            2011-03-27, ---rgf, - Linux: now executing separate 'xdg_icons_menu_add-remove.sh'
            2011-03-26, ---rgf, - Linux: do not call xdg_menu_*.sh as menus are handled in the fileAssociation scripts
            2011-03-21, ---rgf, - Linux: link "libjvm.so" symbolically to "/opt/BSF4ooRexx", such
                                         that Java can find its home
            2011-03-07, ---rgf, - MacOSX: do not use fat binary anymore, only support 32- and 64-bit in install (ppc-support would have to be added explicitly)
            2011-03-02, ---rgf, - Unixes: link "rexxj.sh" to "/usr/bin" as well
                                - MacOSX: copy/link "libBSF4ooRexx.dylib" also to "/usr/lib/java"
            2011-03-01, ---rgf, - macosx: adjusted 'sed' arguments
            2011-02-27, ---rgf, - fixed bug in determining macosx
                                - macosx: do not create file associations and menu options
                                - macosx: make sure to use dyld_library_path everywhere
            2011-02-25, ---rgf (on the way to tokyo) - add support for macosx
            2011-01-25/26, ---rgf, - windows: if classpath set in hkcu, then make sure we refer to system's set value of classpath as well;
                                  escape "%" with "[25x]" in path
            2011-01-03, ---rgf, - fixed bug in parsing the output of a "ls -al", thanks to uli zinngrebe for reporting on 2010-12-26!
            2010-08-11, ---rgf, - added "cacao" subdir to potential jvm locations on unix
            2010-08-10, ---rgf, - linux: now adding sete* to /etc/profile and $home/.bashrc
            2010-08-07, ---rgf, - linux: remove chmod logic, is now done in "setupallandrun.rex"
            2010-08-01, ---rgf, - linux: change modification bits
                                - correcting 'rexxj.sh': export path as well
                                - now linking 'libjvm.so', if necessary; allows to forgo setting
                                  ld_library_path which is unfortunately tricky (one needs to use
                                  to set ~/.bashrc individually for every user as /etc/profile
                                  will runn before setuid's screen which wipes out a set ld_library_path)

            2010-07-31, ---rgf, - linux: add file associations

            2010-07-27, ---rgf, - windows: add file associations

            2010-03-28, ---rgf, - windows: fixed bug that inserted leading blanks into path

            2010-03-21, ---rgf, - unix: make sure libbsf4oorexx.so is copied into /usr/lib and
                                        run ldconfig
            2010-03-19, ---rgf, - unix: installing systemwide into /etc/profile, uninstall
                                        now uses sed to remove those entries with uninstall;
                                        /etc/profile seems to be only re-read if a logoff and
                                        logon occurs

            2010-03-18, ---rgf, - changing logic for "setEnvironment4BSF.*", removing superfluous
                                  comments which only irritate passers by and end-users

            2010-03-07, ---rgf, - adding batch file for Vista/W7 to run the created
                                  installation files as local Administrator; doing same
                                  for Unix with 'sudo' (in case one wants to work via
                                  Explorer only as well)
            2009-10-31, ---rgf, - changed "BSF4Rexx" to "BSF4ooRexx" in informative help text
            2009-10-21, ---rgf, - using now j.file.separator in new copy command
                                - add definition of LD_LIBRARY_PATH for Unix "rexxj.sh"

            2009-10-20, ---rgf, adjust for new name ("BSF4ooRexx") and supported architectures
                                (32, 64), make sure we run on ooRexx 4.0 or higher !

            2008-08-10, ---rgf, changed cmd.eCopy from "-plfv" to "-fv" to allow successful
                                execution from Ubuntu on a Windows NTFS-system;
                                on Unix: adding full path to setEnvironmentBSF.sh
            2008-07-14, ---rgf, added "canonizePath()"; changed logic for "setEnvironemnt4BSF"
                                on Unix; will now set LD_LIBRARY_PATH to point to a library
                                that has the dynamic link library included; lists it last on
                                LD_LIBRARY_PATH
            2008-05-17, ---rgf, now only runs on ooRexx due to usage of directives and OLEObject;
                                creates an install and uninstall script for BSF (adding paths to
                                PATH, CLASSPATH; if rexx.exe is put into user's PATH then HKCU is
                                used, otherwise HKLM)

            2007-02-03, ---rgf, changed logic of installation script to create a "rexxj.{cmd|sh},
                                which sets all needed environment values to run the Rexx scripts
                                from everywhere; this way it is not necessary (and not advised!)
                                to install BSF4Rexx as a Java extension (one JDBC-related error,
                                which I have not been able to trace down yet);
            2007-01-31, ---rgf, made "bsf4rexx.{cmd|sh}" simpler, hence easier to read and to
                                maintain; JavaInfo4BSF4RexxInstallation.java now also returns
                                information about BSFManager.java and RexxEngine.java, if present
                                on the system, e.g.
                                    org.apache.commons.logging.LogFactory=[1]
                                    org.apache.bsf.BSFManager.available=[1]
                                    org.apache.bsf.BSFManager.version=[242.20070128]
                                    org.rexxla.bsf.engines.rexx.RexxEngine.available=[1]
                                    org.rexxla.bsf.engines.rexx.RexxEngine.version=[208.20070128]
            2006-12-17, ---rgf, Java 6.0 introduces a second directory in "java.ext.dirs",
                                using first one ("jre/lib/ext"; script had assumed only one
                                directory)
            2006-12-08, ---rgf, since BSF 2.4.1 (20061208), commons-logging is not needed
                                anymore, hence adopting install script
            2006-01-07, ---rgf, incorporated Mark Miesfeld fix (wording, parseArgs)
                                fixed bug, if "." was missing in set CLASSPATH
            2006-01-15, ---rgf, on Windows make sure that path chunks get quotes removed
            2006-01-17, ---rgf, split jar-file into bsf-v???-yyyymmdd.jar and bsf-rexx-engine.jar,
                                take that into account for copying and removing
            2006-01-22, ---rgf, on Linux hint the user how to enter "setEnvironment4BSF4Rexx.sh"
            2006-01-24, ---rgf, make sure that support infos are quoted with apostrophes on Unix,
                                with quotes on Windows, added qc() for that purpose
            2006-02-06, ---rgf, Unix version now prepared to add setEnvironment4BSF4Rexx.sh to .bashrc
                                automatically
            2006-02-25, ---rgf, "setEnvironment4BSF4Rexx.*" is now copied to a PATH directory,
                                corrected quoting for Unix echo-outputs
            2006-08-01, ---rgf, fixed a bug which prevented from "setEnvironment4BSF4Rexx.*" to be
                                deleted upon uninstall; corrected grammar error; takes care to
                                copy "commons-logging-1.1.jar", if necessary; fixed a bug, now
                                creates working "bsf4rexx.{cmd|sh}"
            2006-09-07, ---rgf, added the directory "jrockit" ("BEA Java"), kudos for this go to
                                Mette (mettemusens .. hotmail.com)

   last svn check-in: $Revision: 925 $ $Author: Administrator $ $Date: 2010-08-31 14:14:13 +0200 (Tue, 31 Aug 2010) $

   Usage:   rexx setupBSF [path2java.exe [, path2binDir-to-install-Rexx-scripts-to]]
            ... "path2java.exe": optional path to java executable
            ... "path2binDir-to-install-Rexx-scripts-to": optional path to directory
                into which the supporting rexx-scripts should be copied to; defaults
                to "/usr/bin" on Linux and to the ooRexx-homedirectory on Windows

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2006-2014 Rony G. Flatscher

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

-- TODO: for Linux change "chmod" logics: set all files to 644, directories and executables to 755

-- parse version "exx_" val "(" . level .

   -- 20150527: make sure we get the full version and the full source information logged
parse version v
say "parse version v: v="pp(v)
parse source  s
say "parse source  s: s="pp(s)

parse version '-' name '_' val '(' . level .

minVersion="4.1.0"

if val<minVersion | level<6.03 then    -- make sure we use ooRexx 4.1.0 or higher !
do
   say "PANIC: need ooRexx ["minVersion"] or higher, found only version ["val"]!"
   exit -1
end

cmd.=""                          /* default value for stem        */
j.=""                            /* default value for stem        */

parse upper source cmd.eOperatingSystem . /* get name of operating system     */
   -- set most important flags
cmd.eIsWindows=(cmd.eOperatingSystem~left(1)="W")
cmd.eIsUnix   =\cmd.eIsWindows   /* if not Windows, it's a Unix dialect       */

cmd.eBitness=getRexxBitness()    -- can be "0" if not determinable, else "32" or "64"

if cmd.eIsWindows=.true then
do
   j.file.separator="\"
   j.path.separator=";"
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
   parse value sysVersion() with . cmd.eWinVersion
   cmd.eElevation.exe="windows\Elevate"cmd.eBitness".exe"   -- define elevate exe to use
end
else
do
   j.file.separator="/"
   j.path.separator=":"
end

   /* parse path, put directories into the stem variable */
path.=""
call parsePath

cmd.eDateTime=.dateTime~new~string
cmd.eDateTimeEdited=cmd.eDateTime~changeStr(":","-")
cmd.eArgSeparator="09"x          /* separator for delimiting Rexx arguments   */
                                 /* paths to Java and desired bin-dir given?  */
parse value parseArgs(arg(1)) with cmd.eJavaBinary (cmd.eArgSeparator) cmd.eBinDir
if cmd.eJavaBinary="" then       /* no path to java.exe given, determine it   */
do
   say "setupBSF.rex: no path to Java's binary supplied, using determinePath2javaBinary()..."
   call determinePath2javaBinary
end
else
do
   say "setupBSF.rex: path to Java's binary supplied, using: ["cmd.eJavaBinary"]"
end


call getJavaProps                /* get the properties of the installed Java  */


say 'setupBSF.rex - dumpJavaProps():'
call dumpJavaProps               /* dump the Java properties in sorted order  */

/* for forcing testing Unix on Windows
j.file.separator="/" -- rgf, debug
j.path.separator=":" -- rgf, debug
-- */




if j.path.separator="" | j.file.separator="" then
do
   say "PANIC: could not use 'java' to get at its properties!"
   say
   say "Please make sure that you have Java installed! You can run 'setupBSF.rex'"
   say "with an argument being the fully qualified path to the 'java' executable"
   say "for which you wish to create the 'un/install' and 'bsf4rexx' scripts."
   say
   call usage
end

      /* determine commands depending on Unix or Windows (OS2)    */
cmd.eIsUnix=(j.file.separator="/")

parse upper source s +1  -- get operating system name in uppercase
cmd.eIsMacOSX=(s="M")    -- determine whether running on "MACOSX"

-- cmd.eIsUnix=.true -- for debugging only !!

      /* DLL/SO-extension library usually named "i386" on Intel, and "solaris" for Sun's Java   */
cmd.eExt.dir.bin=j.java.home || j.file.separator || "lib" || j.file.separator || ,
                                iif(pos("86", j.os.arch)>0, "i386", j.os.arch)



-- rgf, 2010-03-07, 2010-03-18:
cmd.eSuperDir = qualify(directory() || j.file.separator || "..")  -- get path to super directory
cmd.eSuperDir2=cmd.eSuperDir || j.file.separator   -- supply a trailing separator

/* 2007-01-28, ---rgf */

   /* set BSF4REXX_HOME */
cmd.eSetBSF4REXX_HOME=iif(cmd.eIsUnix=1, "", "set ") || "BSF4REXX_HOME="cmd.eSuperDir
   -- 2013-07-14, ---rgf: ooRexx 4.1.3 introduced an error if using a compound symbol ending in a dot
-- call SysFileTree cmd.eSuperDir2||"bsf*.jar", "cmd.eJarBaseNames.", "FO" /* query for the jars we need */
call SysFileTree cmd.eSuperDir2||"bsf*.jar", "files.", "FO" /* query for the jars we need */
cmd.eJarBaseNames.0=files.0
do i=1 to files.0             -- now assign elements from files. to cmd.eJarBaseNames.
   cmd.eJarBaseNames.i=files.i
end


-- rgf, 2010-03-18
if cmd.eIsUnix=1 then
   cmd.eSetCP="export CLASSPATH="
else
   cmd.eSetCP="set CLASSPATH="


do i=1 to cmd.eJarBaseNames.0    /* iterate over all found jar-files    */
   cmd.eSetCP=cmd.eSetCP || iif(i=1, "", j.path.separator) || -
                            iif(cmd.eIsUnix=1, "$BSF4REXX_HOME", "%BSF4REXX_HOME%") || -
                            j.file.separator || filespec("Name", cmd.eJarBaseNames.i)
end

/* 2006-08-02, ---rgf */
     /* get and set the commons-logging jar-file name: */
   cmd.eCommonsLoggingJarName=""    /* default to empty  */
   call sysFileTree "commons-logging-*.jar", "clname.", "FO"   /* get fully qualified file name */
   if clname.0>0 then
   do
      cmd.eCommonsLoggingJarName=clname.1    /* save very first (should be only) filename */

         /* extend classpath  */
      if right(cmd.eSetCP,1)="=" then
         cmd.eSetCP=cmd.eSetCP || cmd.eCommonsLoggingJarName
      else
         cmd.eSetCP=cmd.eSetCP || j.path.separator || cmd.eCommonsLoggingJarName

         /* add to jar-stem   */
      cmd.eJarBaseNames.0=i         /* i got already increased by 1, so no need to do it again */
      cmd.eJarBaseNames.i=cmd.eCommonsLoggingJarName
   end
/* 2006-08-02, ---rgf */

-- rgf, 2010-03-18
if cmd.eIsUnix=.true then
do
   if right(cmd.eSetCP,1)="=" then
      cmd.eSetCP=cmd.eSetCP || "."  || "$TMPCP"
   else
      cmd.eSetCP=cmd.eSetCP || ":." || "$TMPCP"
end
else
do
   if right(cmd.eSetCP,1)="=" then
      cmd.eSetCP=cmd.eSetCP                     || "." || j.path.separator || "%CLASSPATH%"
   else
      cmd.eSetCP=cmd.eSetCP || j.path.separator || "." || j.path.separator || "%CLASSPATH%"
end


   /* define path to "jvm.dll/libjvm.so" ("javai.dll/libjavai.so" for Java 1.1.8)   */
if cmd.eIsMacOSX then
   cmd.ePath2JavaDLL=getPath2JavaOnMacOSX()
else
   cmd.ePath2JavaDLL= getPath2JavaDll()

cmd.eInstallDirectory=directory() || j.file.separator -- save this directory

cmd.eSupportInfos=qc("Support: Internet newsgroup <news:comp.lang.rexx> or via <http://www.RexxLA.org>")


-- cmd.eIsUnix=.true -- for debugging only

-- 2010-03-19: create a unique needle (e.g. to allow deleting lines with sed from profiles later)
cmd.eInstallationNeedle="BSF4ooRexxNeedle"   -- .dateTime~new~string

cmd.eHintString="created:" q(cmd.eDateTimeEdited) "by user" q(j.["USER.NAME"]) -- q(j.user.name)

if cmd.eIsUnix then
do
   suname=value("SUDO_USER",,"ENVIRONMENT") -- fetch environment value, if available
   if suname<>"" then
      cmd.eHintString="created:" q(cmd.eDateTimeEdited) "by sudo user" q(suname)

   -- get and save logName and sudoUser-Name
   cmd.eLogName  = value('LOGNAME',    ,'ENVIRONMENT')  -- get current logname, could be 'root' if sudo'ing
   cmd.eSudoUser = value('SUDO_USER',  ,'ENVIRONMENT')  -- maybe sudo'ing, then who is the sudo user

   if cmd.eLogName="root", cmd.eSudoUser<>"" then
      cmd.eRealUserName=cmd.eSudoUser
   else
      cmd.eRealUserName=cmd.eLogName

   call UnixDetermineTargetLibDirs cmd.   -- determine where to copy/link libraries to

   nl="0a"x                      /* Unix new-line character sequence (\n)     */
   cmd.eNullDevice = "/dev/null" /* null device  */

   cmd.eLeadIn="#!/bin/sh"nl     /* command to execute this file              */
   cmd.eEcho="echo"
   -- cmd.eCopy="cp -plfv"          /* preserve, hard-link, force, verbose: source target(dir)  */
   cmd.eCopy="cp -pfv"            /* force, verbose: source target(dir) [works on FAT32/NTFS systems from Linux as well] */
--   cmd.eCopy="cp -f"            /* force, verbose: source target(dir) [works on FAT32/NTFS systems from Linux as well] */
   cmd.eLink="ln -fv"            /* force, verbose: source target(dir)  */
--   cmd.eLink="ln -f"            /* force, verbose: source target(dir)  */
   cmd.eDelete="rm -fv"
--   cmd.eDelete="rm -f"
   -- cmd.eSetPath="export PATH=" || "$PATH" || j.path.separator || "$BSF4REXX_HOME"
   cmd.eSetPath="export PATH=$BSF4REXX_HOME:$PATH"
   /*
       No need to list the Java-known library paths (JVM will find it on its own)
   */
   cmd.eSetCP= '[ "$CLASSPATH" != "" ] && TMPCP=:$CLASSPATH || TMPCP=' || .endOfLine || cmd.eSetCP

   -- if LD_LIBRARY_PATH is set, use it
   -- rgf, 20100321: note: if used in /etc/profile will get totally ignored ! (works for ~/.bashrc)
   if cmd.eIsMacOSX then
   do
      cmd.eSetDLLPath = '[ "$DYLD_LIBRARY_PATH" != "" ] && TMPLLP=$DYLD_LIBRARY_PATH: || TMPLLP=' || .endOfLine || -
                         "export DYLD_LIBRARY_PATH=$BSF4REXX_HOME:" || cmd.ePath2JavaDLL  || ":" || ":" || "$TMPLLP"
--                         "export DYLD_LIBRARY_PATH=$BSF4REXX_HOME:" || cmd.ePath2JavaDLL  || ":" || "$BSF4REXX_HOME/install/"cmd.eBitness || ":" || "$TMPLLP"
--                         "export DYLD_LIBRARY_PATH=$BSF4REXX_HOME:" || cmd.ePath2JavaDLL  || ":" || "$BSF4REXX_HOME/install/macosx/resources" || ":" || "$TMPLLP"
--                         "export DYLD_LIBRARY_PATH=$BSF4REXX_HOME:" || cmd.ePath2JavaDLL  || ":" || "`pwd`/macosx/resources" || ":" || "$TMPLLP"
   end
   else
   do
      cmd.eSetDLLPath = '[ "$LD_LIBRARY_PATH" != "" ] && TMPLLP=$LD_LIBRARY_PATH: || TMPLLP=' || .endOfLine || -
                         "export LD_LIBRARY_PATH=$BSF4REXX_HOME:" || cmd.ePath2JavaDLL || ":" || "$TMPLLP"
   end

                      -- "export LD_LIBRARY_PATH=$TMPLLP" || "$BSF4REXX_HOME" || j.path.separator  || cmd.ePath2JavaDLL

   cmd.eInstallScriptName="installBSF.sh"
   cmd.eUninstallScriptName="uninstallBSF.sh"

   cmd.eRunAsAdmin="sudo"        /* command to run as super user  */
   cmd.eInstallScriptName.RunAsAdmin="installBSF_sudo.sh"
   cmd.eUninstallScriptName.RunAsAdmin="uninstallBSF_sudo.sh"

   cmd.eTestScriptName   = "rexxj.sh"  /* "testBSF4Rexx.sh"             */
   -- cmd.eSetEnvironmentScriptName="setEnvironment4BSF.sh"
   cmd.eSetEnvironmentScriptName=cmd.eInstallDirectory || "setEnvironment4BSF.sh"

   cmd.eInstallSystemWide=.true           /* install into /etc/profile which supposedly works on all systems
                                             (hence not fiddling with /etc/profile.d which may or may not be
                                             supported by the Linux distribution;
                                             if .false, install into user's profile ~/.bashrc else */

   cmd.eAdd2BashRc   = "$HOME/.bashrc"    /* logon user's bash profile to add entry to       */
   cmd.eAdd2Profile  = "/etc/profile"     /* system wide setting                       */

      /* prolog and epilog used for ".bashrc" to make sure that CLASSPATH gets only extended, if
         "/etc/profile" has not run yet (and set CLASSPATH already to our needs) */
   cmd.eAdd2BashRcProlog = "if [ -z `echo $CLASSPATH | grep ""bsf-rexx-engine.jar""` ] ; then" "#" cmd.eInstallationNeedle
   cmd.eAdd2BashRcEpilog = "fi" "#" cmd.eInstallationNeedle

   cmd.eAdd2BashRc.0 = 2
   cmd.eAdd2BashRc.1 = "    #" cmd.eInstallationNeedle "("cmd.eHintString")"
   -- cmd.eAdd2BashRc.2 = "    ." cmd.eSetEnvironmentScriptName ">/dev/null"  "#" cmd.eInstallationNeedle
   cmd.eAdd2BashRc.2="if [ -f" cmd.eSetEnvironmentScriptName "];" -
                     "then ." cmd.eSetEnvironmentScriptName "; fi #" cmd.eInstallationNeedle

   if cmd.eIsMacOSX then --rgf, 2011-03-01: sed on MacOSX works differently, cf. <http://blog.mpdaugherty.com/2010/05/27/difference-with-sed-in-place-editing-on-mac-os-x-vs-linux/>
      cmd.e_sed_delete_profile ="sed -i '' '/"cmd.eInstallationNeedle"/d'"
   else
      cmd.e_sed_delete_profile ="sed -i '/"cmd.eInstallationNeedle"/d'"   -- 2010-03-19: sed command to delete entries from profile(s)

/* -- rgf, 20100321: not using /etc/ld.so.conf as ldconfig may not be present on all Unixes
   --                copying to /usr/lib instead (running ldconfig on Linuxes)
   cmd.eLdConfigFile        ="/etc/ld.so.conf"     /* config file for 'ldconfig' */
   cmd.e_sed_delete_ld      ="sed -i '\"(cmd.eSuperDir~changeStr("/","\/"))"\d"  -- escape slashes
-- */

   cmd.eSetExecutable    ="chmod 755"     /* set executable for all (user, group, other)  */
   cmd.eLineComment="#"
   if cmd.eIsMacOSX then
      cmd.eDLL="libBSF4ooRexx.dylib"
   else
      cmd.eDLL="libBSF4ooRexx.so"   /* define name of dynamic link library    */

   if cmd.eIsMacOSX then
      cmd.eDLLAllBaseNames="libBSF4ooRexx*.dylib"
   else
      cmd.eDLLAllBaseNames="libBSF4ooRexx*.so"

   if cmd.eBinDir="" then
      cmd.eBinDir="/usr/bin"     /* directory where executables reside     */

   cmd.eLibDir="/usr/lib"
   /*  cmd.eQuote=''                 /* no quoting of file names for Unix      */ */
   cmd.eQuote='"'                /* ---rgf, 2007-01-28: quoting of file names for Unix too! */
   cmd.eAllArguments='$*'
   if cmd.eJavaBinary="" then
      call determinePath2javaBinary
end
else  /* Windows  */
do
   nl="0d0a"x                    /* Windows/OS2 new-line character sequence (\n) */
   cmd.eNullDevice = "nul"       /* null device  */

   cmd.eSetLocal="setlocal"      /* Windows command to set a local environment*/
   cmd.eEcho="@echo"
   cmd.eEchoOff="@echo off"
   cmd.eCopy="copy /y"
   cmd.eLink=cmd.eCopy           /* no ln available, hence copy   */
   cmd.eDelete="del /f"

   --   cmd.eSetPath=   "set PATH=" || "%PATH%" || j.path.separator || "%BSF4REXX_HOME%"
   cmd.eSetPath=   "set PATH=%BSF4REXX_HOME%;%PATH%"
   -- cmd.eSetDLLPath="set PATH=" || "%PATH%" || j.path.separator || cmd.ePath2JavaDLL
   cmd.eSetDLLPath="set PATH=%BSF4REXX_HOME%;" || cmd.ePath2JavaDLL || ";%PATH%"

   cmd.eInstallScriptName="installBSF.cmd"
   cmd.eUninstallScriptName="uninstallBSF.cmd"

   cmd.eRunAsAdmin=getRunAsCmd(cmd.) /* command to run as local Administrator  */
   cmd.eInstallScriptName.RunAsAdmin="installBSF_runAsAdministrator.cmd"
   cmd.eUninstallScriptName.RunAsAdmin="uninstallBSF_runAsAdministrator.cmd"

   cmd.eTestScriptName   = "rexxj.cmd" /* "testBSF4Rexx.cmd"            */
   cmd.eSetEnvironmentScriptName="setEnvironment4BSF.cmd"
   cmd.eLineComment="REM "
   cmd.eDLL="BSF4ooRexx.dll"     /* define name of dynamic link library    */
   if cmd.eBinDir="" then
      cmd.eBinDir=getRexxDir()   /* directory where executables reside     */

   cmd.eLibDir=cmd.eBinDir
   cmd.eQuote='"'                /* always quote file names for Windows    */
   cmd.eAllArguments='%*'

   if cmd.eJavaBinary="" then
      call determinePath2javaBinary

   if isRexxInstalledInHKCU() then  -- ooRexx in user hive (HKCU: current user)
   do
      cmd.eRegistryPath     ="HKCU\Environment\Path"
      cmd.eRegistryClassPath="HKCU\Environment\CLASSPATH"
   end
   else                             -- ooRexx in system hive (HKLM: local machine)
   do
      cmd.eRegistryPath     ="HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment\Path"
      cmd.eRegistryClassPath="HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment\CLASSPATH"
   end
end

if cmd.eJavaBinary="" | cmd.eJavaBinary='""' then
do
   javaName="'java"iif(cmd.eIsUnix,"",".exe")"'"
   say "PANIC: no executable" javaName "found !"
   say
   say "Please make sure that you have Java installed! You can run 'setupBSF.rex'"
   say "with an argument being the fully qualified path to the '"javaName"' executable"
   say "for which you wish to create the 'un/install' and 'bsf4rexx' scripts."
   say
   call usage
end

cmd.eRexxDispatch= "org.rexxla.bsf.RexxDispatcher" cmd.eAllArguments

if cmd.eBitness>0 then  -- copy the appropriate DLL/shared library into super directory
   cmd.eCopy cmd.eBitness || j.file.separator || cmd.eDLL ".." ">" cmd.eNullDevice


   /* create the scripts   */
call create_REXXJ_Script

/*
if cmd.eIsUnix=.true then     /* set ag+rx   */
   "chmod 755" cmd.eTestScriptName
*/

cmd.eCopy cmd.eTestScriptName ".." ">" cmd.eNullDevice   /* copy rexxj.{sh|cm} to superdirectory   */


call createEnvironmentScript  /* Windows probably needs this script        */

-- TODO: ---rgf: 20140921: define a switch (in .local?) that determines whether to install into extension
--                         (java.ext.dirs)
-- call createInstallScript4javaExtension   /* also creates the matching uninstall script      */
-- or:
call createInstallScript      /* new on 2008-05-17, ---rgf
                                 Windows: will add environment values to registry,
                                 Linux:   will add the execution of setEnvironment4BSF4Rexx.sh to ~/bash.rc
                              */

call createRunAsScripts       /* 2010-03-07: for Vista and higher */

/**/
if cmd.eIsUnix=1 then         /* make sure rights are not fully available to the world */
   call setChmod cmd.

/* ---> 2010-08-07, done by controlling script "setupAllAndRun.rex"
if cmd.eIsUnix=1 then         /* make sure rights are not fully available to the world */
do
      -- rgf, 2010-07-31, new logic
   "chmod 755 ../.."                                     /* 755 on "bsf4oorexx"-directory */
   "find .. -type f -exec chmod 644 '{}' \;"
   "find .. -type d -exec chmod 755 '{}' \;"  /* 755 on all directories, starting out in parent direcotry */
   'find .. \( -iname "*.txt" -o -iname "*.pdf" -o -iname "*.oxt" \) -type f -execdir chmod 644 ''{}'' \;'

   chunk1="find .."   -- leadin, start from parent directory
   chunk2="-type f -exec chmod ugo+x '{}' \;"  /* make sure they are executable */
   tmpStr=""
   do ext over .array~of("sh", "so", "rex", "rxj", "rxo", "rexxj", "jrexx", "cls", "orx", "testUnits", "exe", "class", "cmd", "py", "js", "nrx", "jacl")
      if tmpStr="" then tmpStr = '-iname "*'ext'"'
                   else tmpStr = tmpStr '-o -iname "*'ext'"'
   end
   cmd = chunk1 "\(" tmpStr "\)" chunk2   -- build command
   cmd                                    -- let the shell run the command
end
<--- */


/* 2007-02-03, ---rgf: do not create the script to install BSF4Rexx as a Java extension (yet)
                       - there is a problem with JDBC-driver-loading and the ClassLoader being used;
                         needs to be debugged (could not do it to the gory details, need more time

call createInstallScript4javaExtension   /* also creates the matching uninstall script      */
*/

call info                  /* tell user what one can do now                   */

exit


/* create the install script: this will copy the BSF4Rexx jar-file and dynamic link library
   to the appropriate Java extension directory; in effect using BSF4Rexx as a genuine extension
   to Java;
   shortcomings as of 2007-02-03:
       - problems with JDBC samples that do not work, because drivers are loaded wrongly!
       - this script needs to be created and run for every Java possessing its own directory
         structure, which may be rather error prone
*/
createInstallScript: procedure expose j. cmd. path.
  fn=cmd.eInstallScriptName            /* name of install script              */
  ui=cmd.eUninstallScriptName          /* name of uninstall script            */


   -- 2010-07-27, rgf: create fileAssocations
  if \cmd.eIsMacOSX then		-- do not setup file associations and menus on MacOSX
     call "setupFileAssociations.rex"       /* create the file association files   */

  call stream fn, "c", "open replace"
  call stream ui, "c", "open replace"

  -- hintString="created:" q(date("S") time()) "by user" q(j.user.name)
  -- hintString="created:" q(cmd.eDateTimeEdited) "by user" q(j.user.name)

  /* call lineout fn, cmd.eLeadIn */
  call lineout fn, cmd.eEcho qe(cmd.eHintString)
  call lineout fn, cmd.eEcho qe("installing ""BSF4ooRexx""...")
  call lineout fn, ""

     /* uninstall: */
  /* call lineout ui, cmd.eLeadIn */
  call lineout ui, cmd.eEcho qe(cmd.eHintString)
  call lineout ui, cmd.eEcho qe("uninstalling ""BSF4ooRexx""...")
  call lineout ui, ""


/* TODO: rgf, 2010-08-07

---
   -> $HOME/.bashrc
         -- instead of ". /etc/profile" invoke directly the generated script(s)
            ". /opt/bsf4oorexx/install/setEnvironment4BSF.sh"
            ". /opt/bsf4oorexx/install/setEnvironment4OOo.sh"

            [ `echo $CLASSPATH | grep "some-string-needle" | wc -l` -eq 0 ]; then . /etc/profile ; fi;

      even better:
            [ -z `echo $CLASSPATH | grep "some-string-needle"' ]; then . /etc/profile; fi;

  TODO: take care that "~/.bashrc" is actually "$HOME/.bashrc" !!


*/

  if cmd.eIsUnix=1 then
  do
/* ---> 2010-08-07, ---rgf
     "chmod u+rwx,go+rx *sh"     -- make sure the newly created shell scripts are executable
<--- */
     if cmd.eIsMacOSX then
        tmpJvmLibName="libjvm.dylib"
     else
        tmpJvmLibName="libjvm.so"

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

        call lineout ui, cmd.eEcho qe("removing BSF4ooRexx entries from" tmpTgt "...")
        call lineout ui, cmd.e_sed_delete_profile tmpTgt

        if bWriteProlog then
           call lineout fn, cmd.eEcho qc(cmd.eAdd2BashRcEpilog) appendTo

        call lineout fn, ""
        call lineout ui, ""

        bWriteProlog=.true
     end

     if cmd.eIsMacOSX then    -- copy libBSF4ooRexx.dylib from and to correct location, no support for ppc as of 2011-03-07
     do
           path2lib=cmd.eBitness || "/"

           tmpCmd=cmd.eCopy path2lib || cmd.eDLL ".." -- make sure it's in BSFHOME for setEnvironment4BSF.sh
-- say "tmpCmd=["tmpCmd"]"
           call lineout fn, cmd.eEcho qc(tmpCmd)
           call lineout fn, tmpCmd
           call lineout fn, ""


           tmpCmd=cmd.eCopy path2lib || cmd.eDLL cmd.eLibDir
-- say "tmpCmd=["tmpCmd"]"
           call lineout fn, cmd.eEcho qc(tmpCmd)
           call lineout fn, tmpCmd
           call lineout fn, ""

           tmpCmd=cmd.eDelete cmd.eLibDir || "/" || cmd.eDLL
           call lineout ui, cmd.eEcho qc(tmpCmd)
           call lineout ui, tmpCmd
           call lineout ui, ""

         -- on MacOSX also copy/link to "/usr/lib/java"
           tmpJavaLibDir="/usr/lib/java"
           tmpCmd=cmd.eCopy path2lib || cmd.eDLL tmpJavaLibDir
-- say "tmpCmd=["tmpCmd"]"
           call lineout fn, cmd.eEcho qc(tmpCmd)
           call lineout fn, tmpCmd
           call lineout fn, ""

           tmpCmd=cmd.eDelete tmpJavaLibDir || "/" || cmd.eDLL
           call lineout ui, cmd.eEcho qc(tmpCmd)
           call lineout ui, tmpCmd
           call lineout ui, ""

     end
     else   -- plain Unix/Linux
     do
-- TODO: rgf, 2011-03-07: check whether we need to copy lib to BSF4REXX-HOME in order for SetEnvironment4BSF.sh to work
        -- rgf, 2010-08-11: cf. <http://wiki.winehq.org/WineOn64bit>, Fedora/SuSE vs. Debian-based

        if cmd.eTgtLib32<>.nil then
        do
           tmpCmd=cmd.eCopy "32/" || cmd.eDLL cmd.eTgtLib32
-- say "tmpCmd=["tmpCmd"]"
           call lineout fn, cmd.eEcho qc(tmpCmd)
           call lineout fn, tmpCmd
           call lineout fn, ""
        end

        if cmd.eTgtLib64<>.nil then   -- rgf, 2011-03-07
        do
           tmpCmd=cmd.eCopy "64/" || cmd.eDLL cmd.eTgtLib64
-- say "tmpCmd=["tmpCmd"]"
           call lineout fn, cmd.eEcho qc(tmpCmd)
           call lineout fn, tmpCmd
           call lineout fn, ""
        end

     -- rgf, 20100815: delete libBSF4ooRexx*.so in /usr/lib, /usr/lib32, /usr/lib64
     --                ldconfig may create symbolic links named libBSF4ooRexx-32-so and libBSF4ooRexx-64.so
        do tmpTgtLib over .array~of("/usr/lib", "/usr/lib32", "/user/lib64")
           if SysFileExists(tmpTgtLib) then
           do
              tmpCmd=cmd.eDelete tmpTgtLib || "/" || cmd.eDLLAllBaseNames
              call lineout ui, cmd.eEcho qc(tmpCmd)
              call lineout ui, tmpCmd
              call lineout ui, ""
           end
        end
     end

     -- rgf, 20100814
     -- tmpName1="/opt/BSF4ooRexx/libjvm.so"  -- default location to link to
     tmpPath2libjvm=cmd.ePath2JavaDLL || j.file.separator || tmpJvmLibName -- "libjvm.so"

     -- tmpCmd="ln -fsv" tmpPath2libjvm "/opt/BSF4ooRexx/libjvm.so"
     -- tmpCmd="ln -fsv" tmpPath2libjvm "/opt/BSF4ooRexx/"tmpJvmLibName
     -- tmpCmd=cmd.eLink tmpPath2libjvm "/opt/BSF4ooRexx/"tmpJvmLibName -- rgf, 2011-03-06

      -- on Linux this needs to be a symbolic link, such that the JVM can find its Java home
     tmpCmd="ln -fsv" tmpPath2libjvm "/opt/BSF4ooRexx/"tmpJvmLibName -- rgf, 2011-03-21

     call lineout fn, cmd.eEcho qc(tmpCmd)
     call lineout fn, tmpCmd
     call lineout fn, ""

      -- copying Rexx scripts to /usr/bin
     call lineout fn, cmd.eEcho qc("copying BSF4ooRexx scripts to /usr/bin ...")
     call lineout ui, cmd.eEcho qc("removing BSF4ooRexx scripts from /usr/bin ...")
     files2userBin=.array~of("../BSF.CLS", "../BSF_OnTheFly.cls", "../rexxj.sh", "../rexxj2.sh", "../rgf_util2.rex", "../rexxj2.sh", "rexxpaws.rex")
     do binFile over files2userBin
         tmpCmd=cmd.eCopy binFile "/usr/bin"
         call lineout fn, cmd.eEcho qc(tmpCmd)
         call lineout fn, tmpCmd

         tmpCmd="rm -fv" "/usr/bin/"filespec("Name", binFile)
         call lineout ui, cmd.eEcho qc(tmpCmd)
         call lineout ui, tmpCmd
     end
     call lineout fn, ""
     call lineout ui, ""


     if \cmd.eIsMacOSX then
     do
        -- run ldconfig: rgf, 2010-03-21: not available on Solaris or Mac !
-- say "cmd.eOperatingSystem=["cmd.eOperatingSystem"]"
        -- if cmd.eOperatingSystem="LINUX" then    -- update /etc/ld.so.cache

         -- tmpCmd="ldconfig"
         -- tmpCmd="ldconfig -n /usr/lib /usr/lib32 /usr/lib64 /opt/BSF4ooRexx"   -- rgf, 2010-08-14
         tmpCmd="ldconfig /usr/lib /usr/lib32 /usr/lib64 /opt/BSF4ooRexx"   -- rgf, 2012-02-25
         call lineout fn, cmd.eEcho qc(tmpCmd)
         call lineout fn, tmpCmd
         call lineout fn, ""
         call lineout ui, cmd.eEcho qc(tmpCmd)
         call lineout ui, tmpCmd
         call lineout ui, ""

        -- copy *bins to /usr/bin

      -- create icons and menu (2010-08-20, 2011-03-27)
         call lineout fn, "# add icons and menu"
         call lineout fn, "./xdg_icons_menu_add.sh"
         call lineout fn, ""

         call lineout ui, "# remove icons and menu"
         call lineout ui, "./xdg_icons_menu_remove.sh"
         call lineout ui, ""

           -- create fileAssocations
         call lineout fn, "# add mimetype (file associations)"
         call lineout fn, "./fileAssociations_for_rex_add.sh"
         call lineout fn, "./fileAssociations_for_rxj_add.sh"
         call lineout fn, ""

         call lineout ui, "# remove mimetype (file associations)"
         call lineout ui, "./fileAssociations_for_rex_remove.sh"
         call lineout ui, "./fileAssociations_for_rxj_remove.sh"
         call lineout ui, ""

     end

  end
  else      -- add statements to incorporate paths into registry
  do
      -- j.user.dir == %BSF4REXX_HOME%
      cmd="call rexx orx2reg.rex"      -- invoke utility to interact with Windows registry

      hint="Creating a backup of PATH and CLASSPATH registry values..."
      call lineout fn, cmd.eEcho qc(hint)
      call lineout ui, cmd.eEcho qc(hint)

      -- bkpFile="registryValues-"||date("S")"T"time("N")~changestr(":","")
      bkpFile="registryValues-"||cmd.eDateTimeEdited
      call lineout fn, cmd ">" bkpFile"-beforeAddBSF4RexxPathValues.bat"
      call lineout ui, cmd ">" bkpFile"-beforeDelBSF4RexxPathValues.bat"

      call lineout ui, ""
      call lineout fn, ""

         -- add BSF4ooRexx home directory to PATH
      -- paths=j.user.dir||j.path.separator||cmd.ePath2JavaDLL
      -- rgf, 2010-03-07, cater for new directory structure
      paths=cmd.eSuperdir||j.path.separator||cmd.ePath2JavaDLL

      arg="/key:"q(cmd.eRegistryPath) "/path:"q(paths)
      tmpStr1=cmd "/add"    arg
      tmpStr2=cmd "/remove" arg
         -- inform user
      -- call lineout fn, cmd.eEcho qc(tmpStr1)
      -- call lineout ui, cmd.eEcho qc(tmpStr2)

      call lineout fn, cmd.eEcho qc("add BSF4ooRexx-paths to environment variable 'Path' via registry ...")
      call lineout ui, cmd.eEcho qc("remove BSF4ooRexx-paths from environment variable 'Path' via registry ...")

         -- carry out operation
      call lineout fn, tmpStr1
      call lineout ui, tmpStr2

      call lineout ui, ""
      call lineout fn, ""

         -- add bsf-jars to CLASSPATH
      tmpArgs=""
      do i=1 to cmd.eJarBaseNames.0
         if tmpArgs<>"" then tmpArgs=tmpArgs || j.path.separator
         tmpArgs=tmpArgs || cmd.eJarBaseNames.i
      end

      if tmpArgs<>"" then
      do
         tmpArgs=tmpArgs || j.path.separator || "."   -- add current directory (for datergf-BSF4Rexx-samples to work)

            -- make sure the current directory is searched as well
         if cmd.eRegistryClassPath~left(5)="HKCU\" then  -- rgf, 2011-01-25: make sure that system set CLASSPATH is honored !
            arg="/key:"q(cmd.eRegistryClassPath) "/path:"q("%CLASSPATH%;"tmpArgs)
         else
            arg="/key:"q(cmd.eRegistryClassPath) "/path:"q(tmpArgs)

         arg=arg~changestr("%", "[25x]") -- make sure that cmd.exe does not expand

         tmpStr1=cmd "/add"    arg
         tmpStr2=cmd "/remove" arg
            -- inform user
         -- call lineout fn, cmd.eEcho qc(tmpStr1)
         -- call lineout ui, cmd.eEcho qc(tmpStr2)

         call lineout fn, cmd.eEcho qc("add BSF4ooRexx-jars to environment variable 'CLASSPATH' via registry ...")
         call lineout ui, cmd.eEcho qc("remove BSF4ooRexx-jars from environment variable 'CLASSPATH' via registry ...")

            -- carry out operation
         call lineout fn, tmpStr1
         call lineout ui, tmpStr2

           -- 2010-07-27, rgf: create/remove fileAssocations
         call lineout fn, ""
         call lineout fn, "regedit /s fileAssociations_for_REX_add.reg"
         call lineout fn, "regedit /s fileAssociations_for_RXJ_add.reg"
         call lineout fn, "associationChanged.exe"

         call lineout ui, ""
         call lineout ui, "regedit /s fileAssociations_for_REX_remove.reg"
         call lineout ui, "regedit /s fileAssociations_for_RXJ_remove.reg"
         call lineout ui, "associationChanged.exe"
      end
  end

/* rgf, 2010-03-19: always copied while running setupBSF.rex, no need to delete it
  -- rgf, 2010-03-08: copy "rexxj.*" to ..
  call lineout fn, ""
  call lineout fn, cmd.eEcho qe("copying" q(cmd.eTestScriptName) "to" q("..") "...")
  call lineout fn, cmd.eCopy qw(cmd.eTestScriptName) qw("..")
  -- rgf, 2010-03-08: delete "../rexxj.*" ..
  call lineout ui, ""
  call lineout ui, cmd.eEcho qe("deleting" q(cmd.eTestScriptName)  "from directory" q("..") "...")
  call lineout ui, cmd.eDelete qw(".." || j.file.separator || cmd.eTestScriptName)
  -- rgf, 2010-03-08: the end.
*/

  lastMessage="done."
  if cmd.eInstallSystemWide=.true then -- installed systemwide?
     lastMessage=lastMessage "PLEASE logoff and logon for the changes to take affect!"

  call lineout fn, ""
  call lineout fn, cmd.eEcho qe(lastMessage)
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

  call lineout ui, ""
  call lineout ui, cmd.eEcho qe(lastMessage)
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
     cmd.eSetExecutable cmd.eUninstallScriptName
  end
  return


/* Windows only: create runAs batch file for Vista and higher, rgf 2010-03-07 */
createRunAsScripts: procedure expose j. cmd. path.

  -- hintString="created:" q(date("S") time()) "by user" q(j.user.name)
  -- hintString="created:" q(cmd.eDateTimeEdited) "by user" q(j.user.name)

  runAsCmd=cmd.eRunAsAdmin
  arr=.array~of(cmd.eInstallScriptName,   cmd.eInstallScriptName.RunAsAdmin, -
                cmd.eUninstallScriptName, cmd.eUninstallScriptName.RunAsAdmin)

  do i=1 to arr~items by 2
     script=arr[i]
     fn    =arr[i+1]
     call stream fn, "c", "open replace"
     -- header
     call lineout fn, cmd.eEcho qe(cmd.eHintString)
     call lineout fn, cmd.eEcho qe("running" pp(script) "script as local Administrator ...")
     call lineout fn, ""
     if cmd.eIsUnix=.true then
        tmpCmd=runAsCmd qw("./"script)
     else
        tmpCmd=runAsCmd qw(script)
     call lineout fn, cmd.eEcho qe(tmpCmd "...")
     call lineout fn, tmpCmd
     call stream fn, "c", "close"
  end

  return



/* create the install script: this will copy the BSF4Rexx jar-file and dynamic link library
   to the appropriate Java extension directory; in effect using BSF4Rexx as a genuine extension
   to Java;
   shortcomings as of 2007-02-03:
       - problems with JDBC samples that do not work, because drivers are loaded wrongly!
       - this script needs to be created and run for every Java possessing its own directory
         structure, which may be rather error prone
   ---> not used anymore (2007-02-03)
*/
createInstallScript4javaExtension: procedure expose j. cmd. path.
  fn=cmd.eInstallScriptName            /* name of install script              */
  ui=cmd.eUninstallScriptName          /* name of uninstall script            */

  call stream fn, "c", "open replace"
  call stream ui, "c", "open replace"

  -- hintString="created:" q(date("S") time()) "by user" q(j.user.name)
  -- hintString="created:" q(cmd.eDateTimeEdited) "by user" q(j.user.name)

  /* call lineout fn, cmd.eLeadIn */
  call lineout fn, cmd.eEcho qe(cmd.eHintString)
  call lineout fn, cmd.eEcho qe("installing optional Java extension ""BSF4ooRexx""...")
  call lineout fn, ""
/* ---> 2010-08-07, ---rgf
  if cmd.eIsUnix=1 then                /* make sure rights are not fully available to the world */
  do
     call lineout fn, cmd.eLineComment "make sure rights are not fully available to the world"
     call lineout fn, "chmod 755 *.sh *.so *.CLS *.rex *.rxj *.rxo *.dll *.class *.REX"   /* set rwxr-xr-x */
     call lineout fn, "chmod 644 *.java"                                      /* set rw-r--r-- */
     call lineout fn, ""
  end
<--- */

     /* uninstall: */
  /* call lineout ui, cmd.eLeadIn */
  -- call lineout ui, cmd.eEcho qe("created:" q(date("S") time()) "by user" q(j.user.name))
  call lineout ui, cmd.eEcho qe(cmd.eHintString)
  call lineout ui, cmd.eEcho qe("uninstalling optional Java extension ""BSF4ooRexx""...")
  call lineout ui, ""


  /* tgtDir=j.java.ext.dirs */
  tgtDir=getDir(j.java.ext.dirs, 1) /* since Java 6: 1st directory "jre/lib/ext", 2nd directory: system-wide */
  call lineout fn, cmd.eEcho qe("copying the BSF4ooRexx jar file(s) to the Java extension directory" q(tgtDir) "...")
  call lineout ui, cmd.eEcho qe("deleting the BSF4ooRexx jar file(s) from the Java extension directory" q(tgtDir) "...")

  /* call lineout fn, cmd.eCopy qw(cmd.eJar) qw(tgtDir) */
  /* call lineout ui, cmd.eDelete qw(tgtDir || j.file.separator || cmd.eJarBaseName) */
  do i=1 to cmd.eJarBaseNames.0    /* iterate over all found jar-files    */
     call lineout fn, cmd.eCopy   qw(cmd.eJarBaseNames.i) qw(tgtDir)
     call lineout ui, cmd.edelete qw(tgtDir || j.file.separator || filespec("Name", cmd.eJarBaseNames.i) )
  end

  call lineout fn, ""
  call lineout ui, ""

  /* do we need to supply the commons.logging jar ?   */
  if j.org.apache.commons.logging.LogFactory=0 then
  do
     call lineout fn, cmd.eEcho qe("/// trying to install optional org.apache.commons.logging ...")
/*
     call lineout fn, cmd.eEcho qe("NEED for org.apache.commons.logging, hence copying the file ...")
     shortName=filespec("Name", cmd.eCommonsLoggingJarName)

     call lineout fn, cmd.eEcho qe("copying the commons-logging jar" q(shortName)  "to the Java extension directory" q(tgtDir) "...")
     call lineout ui, cmd.eEcho qe("deleting the commons-logging jar" q(shortName) "from the Java extension directory" q(tgtDir) "...")
*/

     if cmd.eCommonsLoggingJarName="" then   /* hmm, not found in current directory!   */
     do
        msg="/// COULD NOT FIND the OPTIONAL jar-file for the package org.apache.commons.logging AT INSTALL TIME !"
        call lineout fn, cmd.eEcho qe(msg)
        call lineout fn, cmd.eEcho qe("///  Look for the commons logging jar-file at http://jakarta.apache.org/commons")
        call lineout fn, cmd.eEcho qe("///  and copy the jar-file to" qw(tgtDir) "yourself, if you want")
        call lineout ui, cmd.eEcho qe(msg)
     end
/*
     else   /* create the copy/delete statement */
     do
        call lineout fn, cmd.eCopy   qw(cmd.eCommonsLoggingJarName) qw(tgtDir)
        call lineout ui, cmd.edelete qw(tgtDir || j.file.separator || filespec("Name", cmd.eCommonsLoggingJarName) )
     end
*/
     call lineout fn, ""
     call lineout ui, ""
  end



  tgtDir=cmd.eExt.dir.bin
  call lineout fn, cmd.eEcho qe("copying the BSF4ooRexx dynamic link library file to its Java extension directory" q(tgtDir) "...")
  call lineout fn, cmd.eCopy qw(cmd.eDLL) qw(tgtDir)
  call lineout fn, ""
     /* uninstall: */
  call lineout ui, cmd.eEcho qe("delete the BSF4ooRexx dynamic link library file from its Java extension directory" q(tgtDir) "...")
  call lineout ui, cmd.eDelete qw(tgtDir || j.file.separator || cmd.eDLL)
  call lineout ui, ""


  tgtDir=cmd.eLibDir
  call lineout fn, cmd.eEcho qe("copying the BSF4ooRexx dynamic link library file to the lib directory" q(tgtDir) "...")
  call lineout fn, cmd.eCopy qw(cmd.eDLL) qw(tgtDir)
  call lineout fn, ""
     /* uninstall: */
  call lineout ui, cmd.eEcho qe("delete the BSF4Rooexx dynamic link library file from the lib directory" q(tgtDir) "...")
  call lineout ui, cmd.eDelete qw(tgtDir || j.file.separator || cmd.eDLL)
  call lineout ui, ""


  tgtDir=cmd.eBinDir
  call lineout fn, cmd.eEcho qe("copying the Rexx scripts and ooRexx class files to" q(tgtDir) "...")
     /* uninstall: */
  call lineout ui, cmd.eEcho qe("deleting the Rexx scripts and ooRexx class files from" q(tgtDir) "...")
  list="*.rex *.REX *.cls *.CLS *.sh *.cmd"     /* list of files to copy/uninstall  */
  processed.=""
  do i=1 to words(list)
     call sysFileTree word(list, i), "files.", "FO"    /* get fully qualified files only  */
     do k=1 to files.0           /* now copy/delete file-by-file     */
        if cmd.eIsUnix=0 then    /* if Windows, then case-insensitive file-names, do not process them twice */
        do
           w=translate(files.k)  /* turn into uppercase              */
           if processed.w<>"" then iterate   /* already processed    */
           processed.w=files.k   /* remember that we processed this  */
        end

        call lineout fn, cmd.eCopy   qw(files.k) qw(tgtDir)   /* install    */
        call lineout ui, cmd.eDelete qw(makeFileName(tgtDir, j.file.separator, files.k))  /* uninstall  */
     end
  end

  call lineout fn, ""
  call lineout fn, cmd.eEcho qe("done.")
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

  call lineout ui, ""
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
     cmd.eSetExecutable cmd.eUninstallScriptName
  end
  return




/* create the "rexxj.{cmd|sh}script which sets up all needed parameters to run Rexx programs from
   this directory */
create_REXXJ_Script: procedure expose j. cmd. path.
  fn=cmd.eTestScriptName

  call stream fn, "c", "open replace"

  if cmd.eIsUnix=.false then
     call lineout fn, cmd.eEchoOff

  /* -- call lineout fn, cmd.eLeadIn */
  -- call lineout fn, cmd.eLineComment "created:" q(date("S") time()) "by user" q(j.user.name)
  call lineout fn, cmd.eLineComment "created:" qe(cmd.eHintString)
  call lineout fn, cmd.eLineComment "script to run ""BSF4ooRexx"" programs without prior installation ..."
  call lineout fn, ""

  if cmd.eIsUnix=.false then
  do
     call lineout fn, cmd.eSetLocal    /* on Windows: create a local environment */
     call lineout fn, ""
  end

  call lineout fn, cmd.eLineComment "set BSF4REXX_HOME variable"
  call lineout fn, cmd.eSetBSF4REXX_HOME  /* set BSF4REXX_HOME */
  call lineout fn, ""

  -- rgf, 2010-08-01
  call lineout fn, cmd.eLineComment "set PATH environment variable"
  call lineout fn, cmd.eSetPath        /* set PATH */
  call lineout fn, ""

  call lineout fn, cmd.eLineComment "set CLASSPATH environment variable"
  call lineout fn, cmd.eSetCP          /* set CLASSPATH        */
  call lineout fn, ""

  if cmd.eIsUnix=1 then    -- rgf, 2009-10-21
  do
     if cmd.eIsMacOSX then
         call lineout fn, cmd.eLineComment "set DYLD_LIBRARY_PATH "
     else
         call lineout fn, cmd.eLineComment "set LD_LIBRARY_PATH "

     -- call lineout fn, subword(cmd.eSetDLLPath, 2)  -- skip first word ("export")
     call lineout fn, cmd.eSetDLLPath
     call lineout fn, ""
  end

  call lineout fn, cmd.eLineComment "Java start command, allows passing arguments to Rexx script"
  --  tmpStr1="-Djava.library.path=" || cmd.eQuote
  -- rgf, 2010-03-08
  tmpStr1="-Djava.library.path=" || cmd.eQuote

  if cmd.eIsUnix=1 then
  do
  --   tmpStr1=tmpStr1 || "$BSF4REXX_HOME"  || j.path.separator || "$LD_LIBRARY_PATH" || cmd.eQuote
     if cmd.eIsMacOSX then
        tmpStr1=tmpStr1 || "$DYLD_LIBRARY_PATH" || cmd.eQuote
     else
        tmpStr1=tmpStr1 || "$LD_LIBRARY_PATH" || cmd.eQuote
  end
  else
  do
     /*
     tmpStr1=tmpStr1 || "%BSF4REXX_HOME%" || j.path.separator || "%PATH%"           || cmd.eQuote
     */
     tmpStr1=tmpStr1 || "%BSF4REXX_HOME%"  || cmd.eQuote
  end

  tmpStr2="-classpath" cmd.eQuote || iif(cmd.eIsUnix=1, "$CLASSPATH", "%CLASSPATH%") || cmd.eQuote

  tmpStr3= iif(cmd.eIsUnix=1, "$BSF4Rexx_JavaStartupOptions", "%BSF4Rexx_JavaStartupOptions%")

  -- rgf, 2011-02-27: if a Java 64-bit then explicitly start Java in Rexx-bitness (32 or 64 bit) mode
  tmpStr0=""
  if cmd.eBitness=32, j.sun.arch.data.model=64 then
     tmpStr0="-d32"


  call lineout fn, cmd.eJavaBinary tmpStr0 tmpStr1 tmpStr2 tmpStr3 cmd.eRexxDispatch

  call stream fn, "c", "close"

  if cmd.eSetExecutable<>"" then       /* set file executable (Unix)    */
     cmd.eSetExecutable cmd.eTestScriptName

  return



/* creates the script that sets up the environment for Rexx to find the Java-DLL for
   demand loading it. */
createEnvironmentScript: procedure expose j. cmd. path.
  fn=cmd.eSetEnvironmentScriptName

  call stream fn, "c", "open replace"
  /* call lineout fn, cmd.eLeadIn */
  /* call lineout fn, "" */
  -- call lineout fn, cmd.eLineComment "created:" q(date("S") time())  "by user" q(j.user.name)
  call lineout fn, cmd.eLineComment "created:" qe(cmd.eHintString)
  call lineout fn, cmd.eLineComment "run this script before invoking a BSF4ooRexx program that loads Java on its own."
  call lineout fn, ""

  call lineout fn, cmd.eSetBSF4REXX_HOME
  call lineout fn, ""

  if cmd.eIsUnix=1 then -- don't echo in Unix as this script may be executed on every new shell
     call lineout fn, cmd.eLineComment "setting the CLASSPATH environment variable..."
  else
     call lineout fn, cmd.eEcho qe("setting the CLASSPATH environment variable...")

  call lineout fn, cmd.eSetCP
  call lineout fn, ""

/*
  -- rgf, 2010-03-08: add Path to BSF4ooRexx with the correct bitness
  lpos=lastPos("=",cmd.eSetDLLPath) -- find location in string, insert thereafter
  dllPath=insert(cmd.eInstallDirectory || cmd.eBitness || j.path.separator, cmd.eSetDLLPath, lpos)
*/

  if cmd.eIsUnix=1 then
  do
/* rgf, 2008-07-14: don't link anymore, could be that users do not want this at all

     call lineout fn, "if ! [ -f /usr/lib/libjvm.so ] "
     call lineout fn, "then"
     call lineout fn, "   ln -sf" cmd.ePath2JavaDLL || "/libjvm.so"  "/usr/lib/libjvm.so"
     call lineout fn, "  " cmd.eEcho qe( q(cmd.ePath2JavaDLL || "/libjvm.so") "linked symbolically to" q("/usr/lib/libjvm.so") )
     call lineout fn, "else "
     call lineout fn, "  " cmd.eEcho qe( q("libjvm.so") "already available in" q("/usr/lib")", creation of a symbolic link not carried out!" )
     call lineout fn, "  " cmd.eEcho qe("to force new links to a specific Java version use:" q("rexx setupJava.rex"))
     call lineout fn, "fi   "
*/

/*
     call lineout fn, cmd.eEcho qe(" ")
     call lineout fn, cmd.eEcho qe("For non-Java programs to be able to successfully invoke Java via the")
     call lineout fn, cmd.eEcho qe( q('libjvm') "dynamic link library the following paths to Java directories")
     call lineout fn, cmd.eEcho qe("must be set (Java property" q('java.library.path') "on this system to:")
     call lineout fn, cmd.eEcho qe("   " pp(j.java.library.path))
     call lineout fn, cmd.eEcho qe(" ")

     call lineout fn, cmd.eEcho qe("Use the following commands on the commandline:")
     call lineout fn, cmd.eEcho qe(" ")
*/


/* 20100801, rgf: either /usr/lib/jvm exists or we link it symbolically, hence we can forgo
                  setting LD_LIBRARY_PATH which only would be reliably possible, if doing it via ~/bashrc
                  which might not be what the user has; the PATH and CLASSPATH settings will be carried
                  out through /etc/profile (which runs before "setuid screen" which resets LD_LIBRARY_PATH!)
     call lineout fn, cmd.eLineComment "set LD_LIBRARY_PATH to point to the" q('libjvm.so') "and" q('libBSF4ooRexx.so') "dynamic library"

     call lineout fn, cmd.eEcho qe("setting the LD_LIBRARY_PATH environment variable ...")
     call lineout fn, cmd.eSetDLLPath
     call lineout fn, ""
*/


/* rgf, 20100814: new strategy, scripts copied to /usr/bin, hence PATH remains unchanged
     call lineout fn, cmd.eEcho qe("setting the PATH environment variable ...")
     call lineout fn, cmd.eSetPath        /* set PATH             */
     call lineout fn, ""

     call lineout fn, cmd.eEcho qe(" ")
*/
  end
  else      /* non-Unix */
  do
     call lineout fn, ""

     call lineout fn, cmd.eLineComment "setting the PATH environment variable..."
     call lineout fn, cmd.eSetPath        /* set PATH             */

     call lineout fn, ""
     call lineout fn, cmd.eSetDLLPath
  end


  if cmd.eIsUnix=.false then  -- don't echo in Unix as this script may be executed on every new shell
  do
     call lineout fn, ""
     call lineout fn, cmd.eEcho qe(".")
     call lineout fn, cmd.eEcho cmd.eSupportInfos
     call lineout fn, ""
  end

  call stream fn, "c", "close"

  if cmd.eSetExecutable<>"" then       /* set file executable (Unix)    */
     cmd.eSetExecutable cmd.eSetEnvironmentScriptName

  return


/* create stem variable "sort." from "j." and sort it by property keys ascendingly */
dumpJavaProps: procedure expose j. cmd. path. sort.
   sort.=""
   max=0
   do i=1 to j.0
      sort.i=j.i.eName j.i.eValue
      max=max(max, length(j.i.eName))
   end
   max=max+2                     /* account for square brackets   */

   sort.0=j.0                    /* save total items in stem      */

   call sysStemSort "sort."      /* sort the stem                 */
   do i=1 to sort.0
      parse var sort.i n1 v1
      say right(i, 2) "-" left(pp(n1), max)":" pp(v1)
   end
   return


/* execute the Java program "JavaInfo4BSF4RexxInstallation.class" to queue all Java properties to
   the system Rexx queue and make them available via the stem variable "j."
*/
getJavaProps: procedure expose j. cmd. path.
   signal on any name usage                           /* trap to usage label     */
         /* get the Java properties    */
   if cmd.eJavaBinary="" then java="java"             /* default     */
                         else java=cmd.eJavaBinary    /* use command-line value  */

   java "-cp . JavaInfo4BSF4RexxInstallation | rxqueue"

   i=0
   max=0
   -- j.=""
   sort.=""
   if queued()=0 then
   do
      say "PANIC: nothing in external Rexx queue, cannot get at the Java properties, aborting..."
      say
      call usage
   end
   else
   do i=1 to 99999 while queued()<>0
      parse pull pName "=[" pVal "]"
      if pname="" then iterate      /* blank line (the last line?)   */
      max=max(max, length(pName))   /* memorize longest name         */

      parse var pVal v1 "0x" v2     /* hex-encoded string ?          */
      j.i.eName=pName               /* use a number based index      */
      j.i.eValue=v1||x2c(v2)

      j.pName=j.i.eValue            /* save in mixed case            */

      uName=translate(pName)        /* save in mixed case            */
      j.uName=j.i.eValue
      j.0=i                         /* save maximum number of entries*/
   end
   return



/* Get the fully qualified path to "jvm.dll", if multiple places, then
   prefer "client", "hotspot", "classic" (seems to be the only one), "server".
*/
getPath2JavaDll: procedure expose j. cmd. path.
   tmpStr=j.java.home

   parts="client hotspot classic server jrockit cacao"  /* look in these directories  */

      /* removed search for [lib]javai.{so|dll}, as Java 1.1.8 is not supported anymore */
   if cmd.eIsUnix=1 then            /* Unix           */
   do
      if cmd.eIsMacOSX then
         dlls="libjvm.dylib"
      else
         dlls="libjvm.so"
   end
   else                             /* Windows, OS2   */
      dlls ="jvm.dll"

   countWords=words(dlls)
   do i=1 to countWords
      tmpFileName=j.java.home || j.file.separator || word(dlls, i)
      say "Attempt #" i "of" countWords "searching file: ["word(dlls, i)"] in ["j.java.home || j.file.separator || -
          "] or in one of its subdirectories..."
      call sysFileTree tmpFileName, "files.", "FOS"

      if files.0>0 then             /* files found?   */
      do
         do k=1 to words(parts)
            p=j.file.separator || word(parts, k) || j.file.separator    /* get word       */
            do m=1 to files.0       /* iterate over files found   */
               if pos(p, files.m)>0 then
               do
                  return substr(files.m, 1, lastpos(j.file.separator, files.m)-1)
               end
            end
         end
      end
   end

      /* default, even if directory does not exist to hint the user where to look for  */
   return tmpStr || j.file.separator || "bin" || j.file.separator || "client"


/* rgf, 2011-02-25: try to locate correct libjvm.dylib on MacOSX
*/
getPath2JavaOnMacOSX: procedure
   call SysFileTree "/System/Library/Java/JavaVirtualMachines/libjvm.dylib", "files.", "FOS"
   if files.0=0 then return ""

   location=filespec("Loction", files.[files.0])   -- get path to newest Java in case there are multiply dylibs
   return location~left(max(1,location~length-1))  -- remove trailing file separator

/* Parse commandline arguments, if any.
*/
parseArgs: procedure expose j. cmd. path.
   parse arg tmpStr
   if tmpStr="" then return ""      /* no arguments available        */
   tmpStr=strip(tmpStr)             /* remove leading & trailing ws  */
   if left(tmpStr,1)='"' then       /* argument enquoted             */
   do
      parse var tmpStr '"' path2javaExe '"' tmpStr
      path2javaExe='"' || path2javaExe || '"'
   end
   else
     parse var tmpStr path2javaExe tmpStr

   path2binDir=strip(strip(tmpStr), 'B', '"')   /* could be quoted on Windows (but not "apostrophed"!)   */
   binDir=path2binDir

   curDir=directory( )              /* save current directory        */
   newDir=directory( binDir )       /* See if directory exists       */
   if newDir \== binDir then
     signal usage
   z = directory( curDir )          /* restore current directory     */

   return path2javaExe || cmd.eArgSeparator || path2binDir


/* Get path to Rexx or Regina from PATH environment variable and return it;
   Windows/OS2
*/
getRexxDir: procedure expose j. cmd. path.
     /* use this precedence order to locate a path where to copy Rexx-files to  */
  dir=findPath("\ooREXX \regina rexx")
  if dir<>"" then return dir

  eyeCatcher="?!no-rexx-path-found!?"
  say "PROBLEM: could not find explicit path to a Rexx-directory!"
  say "         Please adjust the created scripts to point to any one directory listed"
  say "         with the environment variable ""PATH"", ie. replace" pp(eyecatcher) "with"
  say "         any of the listed PATH directory, and then run the scripts."
  return eyecatcher              /* indicate that not found       */


/* Look for filename in path, if found return fully qualified path.
*/
getFromPath: procedure expose j. cmd. path.
  parse arg filename

  do i=0 to path.0      /* iterate over path directories          */
     fn=stream(path.i || j.file.separator || filename, "C", "QUERY EXISTS")

     if fn<>"" then return fn /* found, return it                 */
  end
  return filename



/* Extract filename from fully qualified path in "file" and add it to "tgtDir".
*/
makeFileName: procedure
  parse arg tgtDir, sep, file
  lpos=lastpos(sep, file)
  if lpos>0 then file=substr(file, lpos+1)
  return tgtDir || sep || file

pp: procedure                    /* poor man's "pretty" print     */
  return "[" || arg(1) || "]"


q: procedure                     /* enclose string into apostrophes  */
  return '"' || arg(1) || '"'


   /* "qe": quote echo string: enquote entire string on Unix only */
qe: procedure expose j. cmd. path.
  if  cmd.eIsUnix=1 then return "'" || arg(1) || "'"  /* use apostrophes only, rgf 2006-01-18   */
                    else return arg(1)


   /* "qe": quote fully-qualified paths in Windows as they may contain a blank */
qw: procedure expose j. cmd. path.
  if  cmd.eIsUnix=0 then return """" || arg(1) || """"
                    else return arg(1)

   /* "qc": quote string in apostrophes, if Unix, in quotes, if Windows (for support infos that contain <, >) */
qc: procedure expose j. cmd. path.
  if  cmd.eIsUnix=0 then return '"' || arg(1) || '"'     /* Windows  */
                    else return "'"  || arg(1) || "'"    /* Unix     */


iif: procedure                   /* VBasic-like function          */
  if arg(1)=1 then return arg(2)
  return arg(3)

info:                            /* tell user what has happened   */
  say "setupBSF.rex - info():"
  say
  say "cmd.eSetBSF4REXX_HOME: ["cmd.eSetBSF4REXX_HOME"]"
  say "cmd.eSetPath:          ["cmd.eSetPath"]"
  say "cmd.eSetCP:            ["cmd.eSetCP"]"
  say
  say "---"
  say
  say "the following scripts have been created:"
  say
  say " " pp(cmd.eTestScriptName)": allows to run ""BSF4ooRexx"" programs without installation, e.g. to test the setup/installation:"
  say
  if cmd.eIsUnix=0 then
  do
  --   say "       usage:" cmd.eTestScriptName "rexx-file optional-arguments"
     say "                    " cmd.eTestScriptName "infoBSF.rex"
  end
  else
  do
     say "       usage:" q("./"cmd.eTestScriptName "rexx-file optional-arguments")
     say "     example:" q("./"cmd.eTestScriptName "infoBSF.rex")
  end

  say
  say
  -- say " " pp(cmd.eInstallScriptName)": install" q("BSF4ooRexx") "and set environment variables permanently"
  say " " pp(cmd.eInstallScriptName)": run to install" q("BSF4ooRexx")
  say
  if cmd.eIsUnix=0 then
     say "       to install as Administrator run:" cmd.eInstallScriptName.RunAsAdmin
  else
     say "       to install as root run:" q("./"cmd.eInstallScriptName.RunAsAdmin)
  say
  say

/*
  say
  say "     remarks: - You may need to run as user ""root"" (Linux) or ""Administrator"" (Windows)."
  say "                Under Vista, you may need to run the un/install scripts with elevated privileges."
  say
  say "              - After installation you may start any Rexx-program by issuing:"
  say
  if cmd.eIsUnix=0 then
     say "                  " cmd.eSetEnvironmentScriptName

  say "                   rexx rexx-file optional-arguments"
  say
  say "                or (note the trailing ""j"", which means via ""Java""):"
  say

  if cmd.eIsUnix=0 then
     say "                   rexxj rexx-file optional-arguments"
  else
     say "                   rexxj.sh rexx-file optional-arguments"

*/

  -- say " " pp(cmd.eUninstallScriptName)": use to uninstall" q("BSF4ooRexx") "(removes BSF4ooRexx-paths from Windows registry)"
  say " " pp(cmd.eUninstallScriptName)": run to uninstall" q("BSF4ooRexx")
  say
  if cmd.eIsUnix=0 then
     say "       to uninstall as Administrator run:" cmd.eUnInstallScriptName.RunAsAdmin
  else
     say "       to uninstall as root run:" q("./"cmd.eUnInstallScriptName.RunAsAdmin)
  say



  say
  -- say " " pp(cmd.eSetEnvironmentScriptName)": allows Rexx to find Java ("q("jvm.dll")") on its own"
  say " " pp(cmd.eSetEnvironmentScriptName)": helper script for setting up the environment"
  say "              for running BSF4ooRexx rograms"
  say


/*
  if cmd.eIsUnix=1 then
     say "                it is advised to run this script right after installation of ""BSF4ooRexx"""
  else
     say "                it is advised to add this environment setting to your system's profile"
  say


  say
  say "     remarks: - You may need to run as user ""root"" (Linux) or ""Administrator"" (Windows)."
  say "                Under Vista, you may need to run the un/install scripts with elevated privileges."
  say
*/
  say "Good luck! :-)  [You may also want to take a look at these generated scripts.]"
  exit 0



/* give user feedback and show usage of this script */
usage:
  say
  say "This program creates scripts to test, install and uninstall ""BSF4ooRexx""."
  say
  say "Usage:     rexx setupBSF [fully-qualified-file-name-of-java.exe [dir-to-copy-scripts-to]]"
  say "  (dir-to-copy-scripts-to MUST exist)"
  say
  say "Examples:  rexx setupBSF"

  -- if cmd.eIsUnix=.false then
  if cmd.eIsWindows=.true then
  do
     say "           - will use any ""java.exe"" found in one of the directories of the"
     say "             environment variable ""PATH"""
  end
  else
  do
     say "           - will use /usr/bin/java"
     say "             if no java binary is found, then execute ""rexx setupJava.rex"", which"
     say "             will create the necessary symbolic links to an installed Java."
     say "             (will also link 'libjvm.so' to '/opt/BSF4ooRexx')"
  end
  say

  -- if cmd.eIsUnix=.false then
  if cmd.eIsWindows=.true then
  do
     say "           rexx setupBSF E:\jdk1.6\jre\bin\java.exe D:\windows\system32"
     say "           - will use ""java.exe"" from the given first argument"
     say "           - will copy the BSF4ooRexx-support-files to the Windows system directory"
  end
  else
  do
--     say "           rexx setupBSF /usr/java/j2sdk1.4.2_10/jre/bin/java /usr/bin"
     say "           rexx setupBSF /usr/lib/jvm/java-6-openjdk/jre/bin /usr/bin"
     say "           - will use the binary ""java"" from the given first argument"
     say "           - will copy the BSF4ooRexx-support-files to /usr/bin"
  end

  exit -1


/* parse path into stem path. */
parsePath: procedure expose j. cmd. path.
  p=value("PATH", , "ENVIRONMENT")
  i=0
  do while p<>""
     parse var p chunk (j.path.separator) p
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
findPath: procedure expose j. cmd. path.
   parse upper arg needles    /* blank delimited, translated into uppercase   */
   do i=1 to words(needles)   /* iterate over needles                         */
      n=word(needles, i)
      do k=1 to path.0        /* iterate over paths      */
         if pos(word(needles,i), path.k.upperCase)>0 then
            return path.k
      end
   end
   return ""


/* Returns the directory as indicated by the second, optional argument (if omitted,
   very first directory is assumed); if given number is higher than available directories,
   then the last directory is returned */
getDir: procedure expose j. cmd. path.
   parse arg dirs, number
   if number="" then number=1    /* default to very first listed directory */
   tmpStr=""
   do i=1 to number while dirs<>""
      parse var dirs tmpStr (j.path.separator) dirs
   end
   return tmpStr



/* Returns the received path in canonized form, i.e. removes "." and "..", wherever possible */
canonizePath: procedure expose j. cmd. path.
   parse arg p

   tmpArr=p~makeArray(j.file.separator)
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

   return newArr~makeString("LINE",j.file.separator)


/* Determine fully qualified path to the "java" binary (Windows: "java.exe")  */
determinePath2javaBinary: procedure expose cmd. j. path. -- rgf, 2011-05-24: determine path to java binary
   if cmd.eIsWindows=.false then
   do
      cmd.eJavaBinary=getFromPath("java") /* get fully qualified path   */
      say "determinePath2javaBinary(), getFromPath() returned: ["cmd.eJavaBinary"]"
      return
   end

   wProgramFiles  =value("ProgramFiles",,"environment")
   wProgramFiles86=value("ProgramFiles(x86)",,"environment")   -- 32-bit folder, available on 64-bit Windows only
   wProgramW6432  =value("ProgramW6432",,"environment")        -- WOW (Windows-on-Windows), available on 64-bit Windows only

   /* check whether running 32-bit on a 64-bit Windows; if so adapt accordingly */
   if wProgramW6432<>"", cmd.eBitness=32 then
   do

      windir=value("windir",,"environment")  -- get Windows home installation directory
      wow64 =windir"\SysWOW64\java.exe"
      win32 =windir"\system32\java.exe"

      if sysfileexists(wow64) then           -- this should be seen by 32-bit
          cmd.eJavaBinary=qw(wow64)
      else if sysfileexists(win32) then
          cmd.eJavaBinary=qw(win32)

      say "determinePath2javaBinary(), 'ProgramW6432':" pp(wProgramW6432)", cmd.eJavaBinary="pp(cmd.eJavaBinary)
   end

   if cmd.eJavaBinary="" then       -- not yet determined
   do
      javaExe="java.exe"                 -- executable's name
      tmpJavaBinary=getFromPath(javaExe) -- on path ?
      say "determinePath2javaBinary(), getFromPath() returned: ["tmpJavaBinary"]"

         -- 32-bit Rexx on 64-bit Windows, but java.exe not in "ProgramFiles(x86)", then we point to 64-bit Java, reset
      if cmd.eBitness=32, wProgramW6432<>"", tmpJavaBinary~caselessAbbrev(wProgramFiles86)=.false then
      do
          say "determinePath2javaBinary(), 32-bit Rexx on 64-bit Windows, but java.exe seems to be in a 64-bit directory, reset & continue search"
          tmpJavaBinary=javaExe        --  reset, make sure search continues
      end

      if tmpJavaBinary=javaExe then    -- not found on path, try JRE registry entry
      do
         say "determinePath2javaBinary(), Java binary not found, consulting registry ..."
         wsh  = .OleObject~new("WScript.Shell")
         baseKey = "HKLM\Software\JavaSoft\Java Runtime Environment"

         version=wsh~regRead(basekey"\CurrentVersion")
         say "... ["||basekey"\CurrentVersion]:" version

         javaHome=wsh~regRead(basekey"\"version"\JavaHome")
         say "... ["||basekey"\"version"\JavaHome}: ["javaHome"]"
         binJava=javaHome"\bin\java.exe"

            -- 32-bit Rexx on 64-bit Windows, but java.exe not in "ProgramFiles(x86)", then we point to 64-bit Java, reset
         if cmd.eBitness=32, wProgramW6432<>"", javaHome~caselessAbbrev(wProgramFiles86)=.false then
         do
             say "determinePath2javaBinary(), 32-bit Rexx on 64-bit Windows, but java.exe seems to be in a 64-bit directory, reset & continue search"
             binJava=""                   --  reset, make sure search continues
         end

         if sysFileExists(binJava) then   -- o.k. finally found !
            cmd.eJavaBinary=qw(binJava)
         else   -- not yet found try looking in the file system, ---rgf, 20150512
         do
            pf=wProgramFiles           -- value("ProgramFiles",,"environment")
            if cmd.eBitness=32 then    -- Rexx is 32-bit
            do
                pf=wProgramFiles86     -- value("ProgramFiles(x86)",,"environment")   -- on 64-bit Windows?

                if pf="" then -- on 32-bit Windows
                   pf=wProgramFiles    -- value("ProgramFiles",,"environment")
            end
            searchPattern=pf"\Java\java.exe"
            say "determinePath2javaBinary(), java.exe not found yet, looking in ["searchPattern"] and its subdirectories ..."
              -- try to locate java.exe, use the last one (assuming it is the newest)
            call SysFileTree searchPattern, "files.", "FSO"
            count=files.0
            if count>0 then
            do
                cmd.eJavaBinary=qw(files.count)
                say "determinePath2javaBinary(), java.exe found in" count " places using ["searchPattern"], returning ["files.count"]."
            end
         end
      end
      else  -- found on path, use it
      do
         cmd.eJavaBinary=qw(tmpJavaBinary)
      end
   end
   say "determinePath2javaBinary():" cmd.eJavaBinary
   return


/*  2008-05-17, ---rgf
    returns .true, if path to Rexx is installed in HKCU (user settings), .false else;
    using a routine to deal with a raised syntax condition (if key is not available in registry)
*/
::routine isRexxInstalledInHKCU
  signal on syntax               -- gets triggered if key does not exist in registry
  val=.OleObject~new("WScript.Shell")~regRead("HKCU\Environment\Path")
  paths=val~makeArray(";")       -- create array from string, use path delimiter (";")

  do p over paths                -- iterate over individual paths
      -- if "rexx.exe" is contained in a directory, then return .true
     if stream(p~strip"\rexx.exe", "C", "QUERY EXISTS")<>"" then
        return .true
  end
  return .false                  -- not found

syntax:
  return .false            -- HKCU-PATH does not exist at all !


-- return bitness of Rexx interpreter (e.g. 32, 64, etc.)
::routine getRexxBitness
   -- starting with ooRexx 4.2.0 we can get the bitness information out of the version string
  parse version '_'version'(' '_' bitness '-'
  if version>="4.2", bitness<>"" then
  do
     return bitness
  end

  -- do not use ADDRESS CMD as on Linux this causes an error :(
  "rexx -v 2>&1 | rxqueue"
   bitness=0
  do while queued()>0           -- make sure we process entire queue (to empty it)
     parse pull hint ":" val .
     if hint="Addressing Mode" then bitness=val
  end

  return bitness




-- return "runas" command for this PC's local Administrator, rgf, 2010-03-07, 2014-05-25
::routine getRunAsCmd public
   use arg cmd.

   if cmd.eWinVersion>= 6 then
   do
      return cmd.eElevation.Exe "-wait -k"
   end
   else     -- use runas
   do
      wnw = .OLEObject~New("WScript.Network")

      computerName=wnw~ComputerName
      -- userDomain  =wnw~UserDomain
      -- userName    =wnw~UserName

      return "runas /env /user:"wnw~ComputerName"\Administrator"
   end



-- 2012-02-12, rgf: new logic - use cmd.eBitness and then look for "librexx.so" and
--                  copy appropriate "libBSF4ooRexx" to the same location
::routine unixDetermineTargetLibDirs
  use arg cmd.

  cmd.eTgtLib32=.nil                -- no target for 32-bit as of yet
  cmd.eTgtLib64=.nil                -- no target for 64-bit as of yet

  librexx="librexx.so"

  if cmd.eBitness=32 then        -- 32-bit Rexx
  do
      if SysFileExists("/usr/lib/"librexx) then
         cmd.eTgtLib32="/usr/lib"
      else if SysFileExists("/usr/lib32/"librexx) then
         cmd.eTgtLib32="/usr/lib32"
  end
  else if cmd.eBitness=64 then   -- 64-bit rexx
  do
      if SysFileExists("/usr/lib/"librexx) then
         cmd.eTgtLib64="/usr/lib"
      else if SysFileExists("/usr/lib64/"librexx) then
         cmd.eTgtLib64="/usr/lib64"
  end
  return


::routine unixDetermineTargetLibDirsOriginal      -- determine which /usr/lib* to use for the different architectures and distributions
  use arg cmd.

  "uname -m | rxqueue"     -- get machine architecture, e.g. "x86_64"
  do while queued()>0
     parse pull tmp
     if tmp<>"" then machine=tmp
  end

  cmd.eUnixArchitecture=32 -- default to 32-bit architecture
  if pos("64", machine)>0 then
     cmd.eUnixArchitecture=64


  "ls -ald /usr/lib* | rxqueue"     -- get libs in /usr, check whether /usr/lib64 is a link (to /usr/lib) on Debian based systems
  libs=.array~new
  do while queued()>0
     parse pull lib
     libs~append(lib)
  end

-- e.g.:
-- drwxr-xr-x 220 root root 77824 2011-03-27 23:46 /usr/lib
-- drwxr-xr-x  40 root root 36864 2011-03-27 23:46 /usr/lib32
-- lrwxrwxrwx   1 root root     3 2011-01-27 09:00 /usr/lib64 -> lib


  lib32=.false
  lib64=.false
  lib64isLink=.false
  do val over libs
     -- parse value val with flags . . . . . . lib . target
     parse value val with _mode _inode _owner _group _size _date _time lib . target

say "unixDetermineLibs:" _mode"|"lib"|"target "..."

     if      lib="/usr/lib32" then lib32=.true
     else if lib="/usr/lib64" then
     do
        lib64=.true
        if target<>'' then lib64isLink=.true
     end
  end

  -- now determine library directories we need to use
  cmd.eTgtLib32=.nil                -- no target for 32-bit as of yet
  cmd.eTgtLib64=.nil                -- no target for 64-bit as of yet

  if cmd.eUnixArchitecture=32 then  -- a 32-bit Linux
  do
     cmd.eTgtLib32="/usr/lib"       -- 32-bit Linux go into /usr/lib
  end
  else                              -- a 64-bit Linux
  do
     if lib32=.true then            -- Ubuntu-like?
        cmd.eTgtLib32="/usr/lib32"
     else
     do
        if lib64isLink=.false then  -- Fedora, SuSE ?
           cmd.eTgtLib32="/usr/lib"
     end

     if lib64=.true then
     do
        if lib64isLink=.true then   -- Debian based (symbolic link /usr/lib64 to /usr/lib)
          cmd.eTgtLib64="/usr/lib"
        else                        -- Fedora, SuSE ?
          cmd.eTgtLib64="/usr/lib64"
     end
  end
  return


::routine setChmod   -- on Unix systems make sure that we set the file flags accordingly
   use strict arg cmd., bAllFiles=.true

   if cmd.eBDebug=.true then
   do
      say cmd.eThisName': setChmod, bAllFiles=[' || bAllFiles || '], ...'
      return
   end

      -- make sure that "admin" group members have full access to directories and files
   if bAllFiles=.true then
   do
      "chmod 775 .."                            /* 755 on "bsf4oorexx"-directory                      */

-- rgf,2011-06-08: FC has no "admin" group!
--      "chown" "-R" cmd.eRealUserName":admin ../*"

      "find .. -type d -exec chmod 775 '{}' \;" /* 755 make all directories traversable; rwx-rwxr-x   */
      "find .. -type f -exec chmod 664 '{}' \;" /* make all files rw-rw-r--                           */

            /* make all executable files executable: */
      -- 'find .. \( -iname "*.txt" -o -iname "*.pdf" -o -iname "*.oxt" -o -iname "*.htm*" \) -type f -execdir chmod 644 ''{}'' \;'
      chunk1="find .."   -- leadin, start from parent directory
      chunk2="-type f -execdir chmod ugo+x '{}' \;"   /* make sure executable gets set for all */
      tmpStr=""
      do ext over .array~of("sh", "so", "rex", "rxj", "rxo", "rexxj", "jrexx", "cls", "orx", "testGroup", "testUnit", "exe", "class", "cmd", "py", "js", "nrx", "jacl")
         if tmpStr="" then tmpStr =           '-iname "*'ext'"'
                      else tmpStr = tmpStr '-o -iname "*'ext'"'
      end
      cmd = chunk1 "\(" tmpStr "\)" chunk2   -- build command
      cmd                                    -- let the shell run the command
   end
   else  /* just make sure that all (possibly newly created) .sh-files in the current directory get set to: 775 */
   do

-- rgf,2011-06-08: FC has no "admin" group!
--       'find . -iname "*.sh" -type f -exec chown' cmd.eRealUserName':admin  ''{}'' \;'

       'find . -iname "*.sh" -type f -exec chmod 775 ''{}'' \;'
   end
   return



/*
---- for testing canonizePath():
l=.list~of( "c:\eins\zwei\drei\vier", -
            "c:\eins\zwei\drei\.."  , -
            "c:\eins\zwei\..\.."    , -
            "c:\eins\zwei\..\..\a"  , -
            "c:\eins\..\..\.."      , -
            "c:\..\..\..\.."        , -
            "--------------"        , -
              "\eins\zwei\drei\.."  , -
              "\eins\zwei\..\.."    , -
              "\eins\zwei\..\..\a"  , -
              "\eins\..\..\.."      , -
              "\..\..\..\.."        , -
            "--------------"        , -
               "eins\zwei\drei\.."  , -
               "eins\zwei\..\.."    , -
               "eins\zwei\..\..\a"  , -
               "eins\..\..\.."      , -
               "..\..\..\.."        , -
            "--------------"        , -
            "c:\eins\zwei\drei\."  , -
            "c:\eins\zwei\.\."    , -
            "c:\eins\zwei\.\.\a"    , -
            "c:\eins\.\.\."      , -
            "c:\.\.\.\."        , -
            "--------------"        , -
              "\eins\zwei\drei\."  , -
              "\eins\zwei\.\."    , -
              "\eins\zwei\.\.\a"    , -
              "\eins\.\.\."      , -
              "\.\.\.\."        , -
            "--------------"        , -
               "eins\zwei\drei\."  , -
               "eins\zwei\.\.\a"  , -
               "eins\zwei\.\."    , -
               "eins\.\.\."      , -
               ".\.\.\."              )

do it over l
   say (it)~left(22) "->" canonizePath(it)
end
exit
----
*/


/*

on UNIX the extension for dynamic link libraries can be:

.so      ibm-aix solaris sparc linux freebsd netbsd openbsd dec-osf nto-qnx qnx beos unknown
.dylib   apple-darwin
.sl      hp-hpux

on Windows:

.dll     windows cygnus


looking for Unix:    libjvm.so | libjvm.dylib | libjvm.sl
            Windows: jvm.dll

*/


