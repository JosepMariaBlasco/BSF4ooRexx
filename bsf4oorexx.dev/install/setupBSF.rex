#!/usr/bin/env rexx
/*
   Name:    setupBSF.rex
   Purpose: set up BSF4Rexx such, that it can be used easily (attempt to serve users
            without any knowledge about Java installation issues)
   Author:  Rony G. Flatscher
   Date:    2006-01-07
   Version: 1.42
   Changed:
            2022-11-03, - add cmd.eHomeDir"/BSF4ooRexx/lib/" || "*" to classpath to pick up all jars there

            2022-10-16, - copy BSF4ooRexx850/clr's dll and jar files into BSF4ooRexx850/lib
                        - leave BSF4ooRexx850/clr's gacutilrgf.exe in BSF4ooRexx850/clr and use it from there
                        - copy BSF4ooRexx850/clr's CLR.CLS to BSF4ooRexx850 (BSF4ooRexx850 directory is on PATH)

            2022-10-05, - corrected creation of rexxj.{sh|cmd} to point to new $BSF4OOREXX_HOME/lib directory

            2022-10-04, - changed CLASSPATH to BSF4OOREXX_HOME/lib/_* as all BSF4ooRexx jars
                          will be copied there; has also the benefit to allow users to add
                          jar-files for their apps and have BSF4ooRexx find the classes therein

            2022-08-29, - changed BSF4ooRexx to BSF4ooRexx850
            2021-11-16, - Linux arm32 name was wrong ("libBSF4ooRexx.so-32-arm" instead of "libBSF4ooRexx.so-32-arm32")

            2021-08-29, - make sure on Unix that "/usr/local/bin" and "/usr/local/lib"
                          exist and if not create them before placing links there

            2021-08-26, - 64-bit arm test against different machine names ("arm", "aarch64",
                          "arm64") and use either "lib/libBSF4ooRexx.so-64-aarch64" or if not
                          present "lib/libBSF4ooRexx.so-64-aarch64"

            2021-08-14, - MacOSX: add 'aarch64' (reported by Java as such) as another test,
                                  default to 'universal' in case the name 'x86_64' is not
                                  reported by Java

            2021-07-25 - 2021-07-28 - changes for rexxj.{cm|sh}:
                        - fetches BSF4OOREXX_HOME's "bsf4ooRexx*bin.jar" as absolute path and prepends it
                          to the CLASSPATH

                        - Unix: after checking JAVA_HOME trying the Java version that was used to create
                                the rexxj.sh script before falling back to "java"

            2021-07-24, ---rgf, - changes for rexxj.{cm|sh}:

                                  - BSF4OOREXX_HOME gets defined to point to the directory where setupBSF.rex
                                    resides; will be placed in front of java.library.path: this makes sure
                                    that Java will find the dynamic link library [lib]BSF4ooRexx.{dll|so|dylib}

                                  - OOREXX_LIB_HOME environment variable gets set, if ooRexx 5.0 or higher;
                                    will be placed after BSF4OOREXX_HOME, but before java.library.path: this
                                    makes sure that Java loads this Rexx interpreter

                                  - if PREPEND_JAVA_LIBRARY environment variable is set, it will
                                    be put in front of all of the java.library.path: this allows on MacOS
                                    to be used in place of DYLD_LIBRARY_PATH/LD_LIBRARY_PATH which might
                                    get wiped out by SIP (note: if a script defines DYLD_LIBRARY_PATH
                                    then this will be honored if running programs from the same session)

                                  - if JAVA_HOME environment variable is set, use it, otherwise
                                    fallback to the exact Java version that was employed when the script got
                                    created; if that path does not exist anymore, fall back to "java" and
                                    have the operating system find and run it



            2021-07-17, ---rgf, - on Unix: - added cmd.eUseSymbolicLink?, cmd.eSymbolicLink, cmd.eChownOfSymbolicLink
                                           - now using symbolic links for /usr/local/{bin|lib}
                                             - apply owner:group
                                             - making sure symbolic links get owner:group set (must use "-h")
                                           - instead of copying the files physically, now only
                                             symbolic links get created; to change this behaviour back to copying
                                             change: "cmd.eUseSymbolicLink?=.false" below
                                           - little cleanup of code

            2021-07-14, ---rgf, - on Unix: - if new profile/bashrc/zshenv gets created change owner to realUserName afterwards
            2021-07-13, ---rgf, - on Unix: - added: add/remove to/from $HOME/.zshenv
                                           - correct needle test for conditionally setting environment from ~/.bashrc
                                           - make real user the owner of the created un/install scripts
                                           - added "cmd.eChown"
                                           - on Unix add "cmd.eChownUserGroup", new routine "determineGroupName(defGroup)";
                                             make sure libjvm.so link in /opt/BSF4ooRexx gets owner:group set;
                                             new routine "determineHomeDir(userName, isMacOS)", result stored
                                             in new "cmd.eHomeDir"

            2020-11-20, ---rgf, - make sure the 64-bit arm library can be also "aarch64" and use
                                  the suffix "-aarch64" for it from now on
            2020-08-17, ---rgf, - add 64-bit arm library
            2018-10-11, ---rgf, - adjust installation for now supporting ARM-32 (Raspberry Pi)
            2018-06-13, ---rgf, - create_RexxJ_Script: the CLASSPATH may get too large for
                                  the (Windows) command line, and Jvaa honors the CLASSPATH
                                  environment variable, so no need to explicitly state it
            2017-08-13, ---rgf, - corrected determining MacOSX
            2017-05-09, ---rgf, - add cmd.ePath2JavaDLL to info-output if set or on Windows or MacOSX
            2017-02-14, ---rgf, - Linux: carry out a separate ldconfig with the directory to which
                                  /opt/BSF4ooRexx/libjvm.so points to
            2017-02-12, ---rgf, - add quoted cmd.ePath2JavaDLL to "ldconfig" command; it seems that some
                                  Linuxes (possibly with multiple Java installations) otherwise do not
                                  find the libjvm.so that gets loaded with dlopen() by BSF4ooRexx
            2017-02-03, ---rgf, - adjust test for MacOSX for new operating system name "Darwin" (used to be "MacOSX")
                                - if Java cannot be found, abort installation with appropriate message and usage
                                - make sure that the libBSF4ooRexx* shared libraries get the x-bit set (despite new naming)
                                - use "/sbin/ldconfig" instead of "ldconfig" to make sure that it is being found, if available

            2017-01-09, ---rgf, - adopting the installation of the lib/dll file to their new names and
                                  locations
                                - on Unix: do not create a link to the libjvm.{so|dylib} to serve as
                                  the Java to use anymore; leaving the capability in BSF4ooRexx.cc
            2017-01-05, ---rgf, resolving <https://sourceforge.net/p/bsf4oorexx/bugs/26/>
                                - getJavaProps(): now returns stem with Java properties
                                - determinePath2JavaBinary(): on Windows will proceed searching, if
                                  a found java.exe does not match the bitness of ooRexx
            2016-08-15, ---rgf, - MacOSX: adjust for El Capitan, target dirs now "/usr/local/bin" and "/usr/local/lib"
                                - Windows: change elevation to use Gil Barmwater's "elevate.rex" script instead (bitness agnostic)
                                - Windows: add .NET/CLR (common language runtime) support
                                - added routine "enquote" to enquote paths in double-quotes
            2016-04-17, ---rgf, - fixed a bug uncovered by Paul Dunkley: getJavaProps() does not work
                                  on Linux on z (s390x) as the character sequence indicating an escaped
                                  hexadecimal value ("0x") may occur in paths on ("s390x")
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
                                  (displays values for cmd.eSetBSF4OOREXX_HOME, cmd.eSetPath and cmd.eSetCP), will
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
            2011-03-02, ---rgf, - Unixes: link "rexxj.sh" to "/usr/bin" as well; since 2020-08-18 to "/usr/local/bin"
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

            2010-03-21, 2020-08-18, ---rgf, - unix: make sure libbsf4oorexx.so is copied into /usr/local/lib
                                        and run ldconfig
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
                to "/usr/local/bin" on Linux and to the %ProgramFiles[ (x86)] directory on
                Windows, which gets put on the PATH and CLASSPATH

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2006-2022 Rony G. Flatscher

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

cmd.eRexxVersion=v               /* save version string           */
parse var v "_" cmd.eRexxMajorVersion "."    /* if on ooRexx 5.0 or higher fetch and use lib-location */

parse upper source cmd.eOperatingSystem . /* get name of operating system     */
   -- set most important flags
cmd.eIsWindows=(cmd.eOperatingSystem~left(1)="W")
cmd.eIsUnix   =\cmd.eIsWindows   /* if not Windows, it's a Unix dialect       */

cmd.eBitness=getRexxBitness()    -- can be "0" if not determinable, else "32" or "64"

   -- setting file and path separators manually
if cmd.eIsWindows=.true then
do
   j.file.separator="\"
   j.path.separator=";"
   /* Get Windows version:
         use "runas" via WShell:
                           XP                   = 5.1 (ooRexx returns 5.01)
                           Server 2003          = 5.2

         from here on use Elevate32.exe/Elevate64.exe, since 2016-08-24, use Gil Barmwater's elevate.rex:
                           Vista & Server 2008  = 6.0
                           W7 & Server 2008 R2  = 6.1
                           W8 & RT & Server 2012= 6.2
                           W8.1 & Server 2012 R2= 6.3
   */
   parse value sysVersion() with . cmd.eWinVersion
   cmd.eElevation.exe="windows\elevate.rex"  -- 2015-08-24: Gil Barmwater's elevate.rex, works on all Windows versions so far
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
   call determinePath2javaBinary -- will do getJavaProps() and set j.
end
else
do
   say "setupBSF.rex: path to Java's binary supplied, using: ["cmd.eJavaBinary"]"
   j.=getJavaProps()    /* returns stem object that contains the Java properties */
end

if cmd.eJavaBinary="" then -- rgf, 2017-02-03, no Java binary found, abort installation !
do
   say "setupBSF.rex: could not find Java, please install or supply path to Java home directory, aborting ... (line #" .line")"
   call usage  -- will exit -1
end

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
   call usage  -- will exit -1
end

if j.sun.arch.data.model<>cmd.eBitness then  -- found Java not the same bitness as Rexx ?
do
   say "PANIC: Java's bitness ("j.sun.arch.data.model") does not match Rexx' bitness ("pp(cmd.eBitness)")!"
   say
   say "Please make sure that you have Java and ooRexx installed with the same bitness (addressing mode)!"
   say
   say "   ooRexx:" cmd.eBitness"-bit, Java:" j.sun.arch.data.model"-bit"
   say
   call usage  -- will exit -1
end


parse upper source s +1  -- get operating system name in uppercase
cmd.eIsMacOSX=(s="D" | s="M")    -- determine whether running on "MACOSX" replaced by "Darwin"

if cmd.eIsUnix then  -- get home directory
do
   cmd.eHomeDir      = determineHomeDir(cmd.eRealUserName, cmd.eIsMacOSX)  -- find for Unix (Darwin, Linux, etc.)
end
else
do
   cmd.eHomeDir=value("USERPROFILE",,"environment")
end

-- cmd.eIsUnix=.true -- for debugging only !!

cmd.eSuperDir = qualify(directory() || j.file.separator || "..")  -- get path to super directory
cmd.eSuperDir2=cmd.eSuperDir || j.file.separator   -- supply a trailing separator

   /* set BSF4OOREXX_HOME */
   -- if BSF4OOREXX_HOME not defined, assign default value
if cmd.eIsWindows then
   cmd.eSetBSF4OOREXX_HOME='if "%BSF4OOREXX_HOME%"=="" set BSF4OOREXX_HOME='cmd.eSuperDir
else
   cmd.eSetBSF4OOREXX_HOME='[ "$BSF4OOREXX_HOME" = "" ] && BSF4OOREXX_HOME='cmd.eSuperDir

if cmd.eRexxMajorVersion>= 5 then   /* .RexxInfo available, use itfor rexxj.{cmd|sh}   */
do
   -- if OOREXX_LIB_HOME is not defined, assign default value
   if cmd.eIsWindows then
      cmd.eSetooRexxLibHome='if "%OOREXX_LIB_HOME%"=="" set OOREXX_LIB_HOME='.rexxInfo~libraryPath
   else
      cmd.eSetooRexxLibHome='[ "$OOREXX_LIB_HOME" = "" ] && OOREXX_LIB_HOME='.rexxInfo~libraryPath

   -- if cmd.eRexxMajorVersion>= 5 then   /* .RexxInfo available, use itfor rexxj.{cmd|sh}   */
   --    cmd.eSetooRexxLibHome=iif(cmd.eIsUnix=1, "", "set ") || "OOREXX_LIB_HOME=".rexxInfo~libraryPath
end

   -- 2013-07-14, ---rgf: ooRexx 4.1.3 introduced an error if using a compound symbol ending in a dot
-- rgf, 20221004: replace individual jars with lib/ *
cmd.eJarBaseNames.0=0
/* --
call SysFileTree cmd.eSuperDir2||"bsf*.jar", "files.", "FO" /* query for the jars we need */
cmd.eJarBaseNames.0=files.0
do i=1 to files.0             -- now assign elements from files. to cmd.eJarBaseNames.
   cmd.eJarBaseNames.i=files.i
end
-- */

if cmd.eIsUnix=1 then
   cmd.eSetCP="export CLASSPATH="
else
   cmd.eSetCP="set CLASSPATH="

-- rgf, 20221004: replace individual jars with lib/ *
/* --
do i=1 to cmd.eJarBaseNames.0    /* iterate over all found jar-files    */
   cmd.eSetCP=cmd.eSetCP || iif(i=1, "", j.path.separator) || -
                            iif(cmd.eIsUnix=1, "$BSF4OOREXX_HOME", "%BSF4OOREXX_HOME%") || -
                            j.file.separator || filespec("Name", cmd.eJarBaseNames.i)
end
-- */
-- watch out for Rexx comment, hence concatenating asterisk
cmd.eSetCP=cmd.eSetCP || iif(cmd.eIsUnix=1, "$BSF4OOREXX_HOME/lib/" || "*", "%BSF4OOREXX_HOME%\lib\*")

-- 2016-08-15, rgf: define .NET/CLR
if cmd.eIsUnix<>1 then
do
   cmd.eCLRDir="..\clr" -- should .NET/CLR (common language runtime) support be put into bsf4oorexx-bin-dir?
   cmd.eCLR.GacUtilRgf="gacutilrgf.exe"   -- install/uninstall assemblies into/from GAC (global assembly cache)
   cmd.eCLR.eAssemblies=.array~of("jni4net.n-0.8.8.0.dll", "oorexx.net.dll")  -- assemblies to put into/remove from GAC
   cmd.eCLR.eJars      =.array~of("jni4net.j-0.8.8.0.jar", "oorexx.net.jar")  -- assemblies to put into/remove from GAC

/* -- rgf, 20221004: no need to list jar files individually anymore
   do jar over cmd.eCLR.eJars -- cmd.eSetCP will be used for setting environment variables in scripts
      cmd.eSetCP=cmd.eSetCP || iif(i=1, "", j.path.separator) || -
                               iif(cmd.eIsUnix=1, "$BSF4OOREXX_HOME", "%BSF4OOREXX_HOME%") || -
                               j.file.separator || jar
   end
-- */
end

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
   i=cmd.eJarBaseNames.0+1
   cmd.eJarBaseNames.0=i         /* i got already increased by 1, so no need to do it again */
   cmd.eJarBaseNames.i=cmd.eCommonsLoggingJarName
end

   -- add current directory (new: 2022-11-03) followed by "homedir/lib/*" to pick up all jars there
if cmd.eIsUnix=.true then
do
   tmpHomeDirLib=":"cmd.eHomeDir"/BSF4ooRexx/lib/" || "*"   -- 2022-11-03: allow user's lib to be used as jar collection

   if right(cmd.eSetCP,1)="=" then
      cmd.eSetCP=cmd.eSetCP || "."  || tmpHomeDirLib || "$TMPCP"
   else
      cmd.eSetCP=cmd.eSetCP || ":." || tmpHomeDirLib || "$TMPCP"
end
else
do
   tmpHomeDirLib=";"cmd.eHomeDir"\BSF4ooRexx\lib\*"   -- 2022-11-03: allow user's lib to be used as jar collection
   if right(cmd.eSetCP,1)="=" then
      cmd.eSetCP=cmd.eSetCP                     || "." || tmpHomeDirLib || ";%CLASSPATH%"
   else
      cmd.eSetCP=cmd.eSetCP || j.path.separator || "." || tmpHomeDirLib || ";%CLASSPATH%"
end

   /* define path to "jvm.dll/libjvm.so" ("javai.dll/libjavai.so" for Java 1.1.8)   */
if cmd.eIsMacOSX then
   cmd.ePath2JavaDLL=getPath2JavaOnMacOSX(j.)
else
   cmd.ePath2JavaDLL= getPath2JavaDll()

cmd.eInstallDirectory=directory() || j.file.separator -- save this directory

cmd.eSupportInfos=qc("Support: Internet newsgroup <news:comp.lang.rexx> or via <http://www.RexxLA.org>")

-- cmd.eIsUnix=.true -- for debugging only
-- 2010-03-19: create a unique needle (e.g. to allow deleting lines with sed from profiles later)
cmd.eInstallationNeedle="BSF4ooRexx850Needle"   -- .dateTime~new~string
cmd.eHintString="created:" q(cmd.eDateTimeEdited) "by user" q(j.["USER.NAME"]) -- q(j.user.name)

if cmd.eIsUnix then
do
   -- controls whether a link to the current libjvm.{so|dylib} should be created in the BSF4ooRexx directory
   -- by default set to .true as on systems with multiple versions of Java installed this link
   -- will control which libjvm.so to use
   cmd.bCreateLinkInB4RHome?=.true  -- create a link to libjvm.so from install time to /opt/BSF4ooRexx from where the JVM gets picked up
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

   cmd.eChownUserGroup = cmd.eRealUserName":"determineGroupName(cmd.eRealUserName)

   call UnixDetermineTargetLibDirs cmd.   -- determine where to copy/link libraries to

   cmd.eLineComment="#"

   nl="0a"x                      /* Unix new-line character sequence (\n)     */
   cmd.eNullDevice = "/dev/null" /* null device  */

   cmd.eLeadIn="#!/bin/sh"nl     /* command to execute this file              */
   cmd.eEcho="echo"
   cmd.eCopy="cp -pfv"           /* force, verbose: source target(dir) [works on FAT32/NTFS systems from Linux as well] */
   cmd.eLink="ln -fv"            /* force, verbose: source target(dir)  */

   cmd.eUseSymbolicLink? = .true /* 2021-07-17, rgf: controls whether to copy or to create a symbolic link to/in /usr/local/{bin|lib}*/
   cmd.eSymbolicLink="ln -sfv"   /* symbolic, force, verbose: sourceFilePath targetFilePath (= symbolic link)  */
   cmd.eDelete="rm -fv"
   cmd.eChown="chown" cmd.eChownUserGroup   -- command to change the owner of the generated un/install scripts
   cmd.eChownOfSymbolicLink="chown -h" cmd.eChownUserGroup -- command to change the owner of the generated un/install scripts

   cmd.eSetPath="export PATH=$BSF4OOREXX_HOME:$PATH"
   cmd.eSetCP= '[ "$CLASSPATH" != "" ] && TMPCP=:$CLASSPATH || TMPCP=' || .endOfLine || cmd.eSetCP

   -- if LD_LIBRARY_PATH is set, use it
   if cmd.eIsMacOSX then cmd.eLDlibPath="DYLD_LIBRARY_PATH"   -- MacOSX
                    else cmd.eLDlibPath="LD_LIBRARY_PATH"     -- Unix (Linux)

   -- define [DY]LD_LIBRARY_PATH
/*
   if cmd.eSetooRexxLibHome<>"" then      -- we get run by ooRexx >= 5
      tmpLibPath='TMP_LIB_PATH=$BSF4OOREXX_HOME:$OOREXX_LIB_HOME'
   else
      tmpLibPath='TMP_LIB_PATH=$BSF4OOREXX_HOME'

*/
   tmpLibPath='[ "$OOREXX_LIB_HOME" = "" ] && TMP_LIB_PATH=$BSF4OOREXX_HOME  || TMP_LIB_PATH=$BSF4OOREXX_HOME:$OOREXX_LIB_HOME' || .endofLine || -
              'TMP_LIB_PATH=$TMP_LIB_PATH:'j.java.library.path

   -- tmpLibPath=tmpLibPath":"j.java.library.path  -- append the existing Java library path

   tmpArr=.array~new
      -- if [DY]LD_LIBRARY_PATH exists, PREpend it (the user expects LD_LIBRARY_PATH to be used first)
   tmpArr~append( tmpLibPath )
   tmpArr~append( '[ "$'cmd.eLDlibPath'" != "" ] &&' cmd.eLDlibPath'=$'cmd.eLDlibPath':$TMP_LIB_PATH'  -
                                                '||' cmd.eLDlibPath'=$TMP_LIB_PATH')

   tmpArr~append("")

   if cmd.eIsMacOSX then   -- prcoess PREPEND_LIBRARY_PATH as Apple's SIP wipes out DYLD_LIBRARY_PATH and LD_LIBRARY_PATH
   do -- place a hint for Apple users
      tmpArr~append( cmd.eLineComment "as of 2021-07-24 Apple's SIP (macOS Big Sur 11.4) clears DYLD_LIBRARY_PATH and even LD_LIBRARY_PATH environment variables from new shell sessions, use PREPEND_LIBRARY_PATH instead")
   end
   tmpArr~append( cmd.eLineComment "PREPEND_LIBRARY_PATH optional environment variable, will be put at head of library path")
   tmpArr~append( '[ "$PREPEND_LIBRARY_PATH" != "" ] &&' cmd.eLDlibPath'=$PREPEND_LIBRARY_PATH:$'cmd.eLDlibPath )
   tmpArr~append("")

   tmpArr~append( "export" cmd.eLDlibPath )
   cmd.eSetDLLPath = tmpArr~toString('L',.endOfLine)

   cmd.eInstallScriptName="installBSF.sh"
   cmd.eUninstallScriptName="uninstallBSF.sh"

   cmd.eRunAsAdmin="sudo"        /* command to run as super user  */
   cmd.eInstallScriptName.RunAsAdmin="installBSF_sudo.sh"
   cmd.eUninstallScriptName.RunAsAdmin="uninstallBSF_sudo.sh"

   cmd.eTestScriptName   = "rexxj.sh"  /* "testBSF4Rexx.sh"             */
   cmd.eSetEnvironmentScriptName=cmd.eInstallDirectory || "setEnvironment4BSF.sh"

   cmd.eInstallSystemWide=.true           /* install into /etc/profile which supposedly works on all systems
                                             (hence not fiddling with /etc/profile.d which may or may not be
                                             supported by the Linux distribution;
                                             if .false, install into user's profile ~/.bashrc else */

   -- TODO: remove done above: cmd.eHomeDir      = determineHomeDir(cmd.eRealUserName, cmd.eIsMacOSX)  -- find for Unix (Darwin, Linux, etc.)

   cmd.eAdd2BashRc   = cmd.eHomeDir"/.bashrc"   /* logon user's bash profile to add entry to       */
   cmd.eAdd2Profile  = "/etc/profile"     /* system wide setting                       */
   cmd.eAdd2Zshenv   = cmd.eHomeDir"/.zshenv"   /* user's zsh environment for all sessions   */



      /* prolog and epilog used for ".bashrc" to make sure that CLASSPATH gets only extended, if
         "/etc/profile" has not run yet (and set CLASSPATH already to our needs) */
   cmd.eAdd2BashRcProlog = "if [ -z `echo $CLASSPATH | grep ""bsf4ooRexx-.*-bin\.jar""` ] ; then" "#" cmd.eInstallationNeedle
   cmd.eAdd2BashRcEpilog = "fi" "#" cmd.eInstallationNeedle

   cmd.eAdd2setEnvironment.0 = 2
   cmd.eAdd2setEnvironment.1 = "    #" cmd.eInstallationNeedle "("cmd.eHintString")"
   cmd.eAdd2setEnvironment.2="if [ -f" cmd.eSetEnvironmentScriptName "];" -
                             "then ." cmd.eSetEnvironmentScriptName "; fi #" cmd.eInstallationNeedle

   if cmd.eIsMacOSX then --rgf, 2011-03-01: sed on MacOSX works differently, cf. <http://blog.mpdaugherty.com/2010/05/27/difference-with-sed-in-place-editing-on-mac-os-x-vs-linux/>
      cmd.e_sed_delete_profile ="sed -i '' '/"cmd.eInstallationNeedle"/d'"
   else
      cmd.e_sed_delete_profile ="sed -i '/"cmd.eInstallationNeedle"/d'"   -- 2010-03-19: sed command to delete entries from profile(s)

   cmd.eSetExecutable    ="chmod 755"     /* set executable for all (user, group, other)  */
   if cmd.eIsMacOSX then
   do
      cmd.eDLL="libBSF4ooRexx850.dylib"   /* define name of dynamic link library    */
      cmd.eDLLAllBaseNames="libBSF4ooRexx850*.dylib"  -- pattern to delete
      cmd.eLibDir="/usr/local/lib"  -- new target library dir since 2015 (ElCapitan on MacOSX forces this)
      if cmd.eBinDir="" then
         cmd.eBinDir="/usr/local/bin"  /* directory where executables reside     */

      --   when cmd.eBitness=32 then cmd.eDll.sourcePath="lib/libBSF4ooRexx.dylib-32-x86"    -- no 32-bit support anymore!
      if cmd.eBitness=64 then
      do
          if j.os.arch='arm' | j.os.arch='arm64' | j.os.arch='aarch64' then
              cmd.eDll.sourcePath="lib/libBSF4ooRexx850.dylib-64-arm64"
          else if j.os.arch='x86_64' then
              cmd.eDll.sourcePath="lib/libBSF4ooRexx850.dylib-64-x86_64"
          else -- default to universal which includes arm64 and x86_64
              cmd.eDll.sourcePath="lib/libBSF4ooRexx850.dylib-universal"
      end
      else cmd.eDll.sourcePath="lib/panic-unknown-bitness-"cmd.eBitness
   end
   else
   do
      cmd.eDLL="libBSF4ooRexx850.so"      /* define name of dynamic link library    */
      cmd.eDLLAllBaseNames="libBSF4ooRexx850*.so"     -- pattern to delete
      cmd.eLibDir="/usr/local/lib"        -- target library dir
      if cmd.eBinDir="" then
         cmd.eBinDir="/usr/local/bin"        /* directory where executables reside     */

      select      -- determine location and name of library to use
         when cmd.eBitness=32   then
         do
            if j.os.arch='arm'  then cmd.eDll.sourcePath="lib/libBSF4ooRexx850.so-32-arm32"
                                else cmd.eDll.sourcePath="lib/libBSF4ooRexx850.so-32-x86"
         end
         when cmd.eBitness=64    then
         do
            if j.os.arch='s390x'    then cmd.eDll.sourcePath="lib/libBSF4ooRexx850.so-64-s390x"
            else if j.os.arch='arm' | j.os.arch='aarch64' | j.os.arch='arm64' then    -- Linux
                                    do
                                       if sysFileExists("lib/libBSF4ooRexx850.so-64-aarch64") then
                                          cmd.eDll.sourcePath="lib/libBSF4ooRexx850.so-64-aarch64"
                                       else
                                          cmd.eDll.sourcePath="lib/libBSF4ooRexx850.so-64-arm64"
                                    end
                                    else -- Intel/AMD
                                       cmd.eDll.sourcePath="lib/libBSF4ooRexx850.so-64-amd64"
         end

         when j.os.arch~caselessAbbrev('aix') then
                                     cmd.eDll.sourcePath="lib/libBSF4ooRexx850.so-64-aix"

         otherwise                   cmd.eDll.sourcePath="lib/panic-unknown-bitness-"cmd.eBitness"-arch-"j.os.arch
      end
   end

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

   cmd.eSetLocal="setlocal enableDelayedExpansion"    /* Windows command to set a local environment and allow for delayed expansion! */
   cmd.eEcho="@echo"
   cmd.eEchoOff="@echo off"
   cmd.eCopy="copy /y"
   cmd.eLink=cmd.eCopy           /* no ln available, hence copy   */
   cmd.eDelete="del /f"

   cmd.eLineComment="REM "

   -- 2021-07-23, rgf: cmd.ePath2JavaDLL not needed on PATH as Java runs already
   --   cmd.eSetPath=   "set PATH=" || "%PATH%" || j.path.separator || "%BSF4OOREXX_HOME%"
   cmd.eSetPath=   "set PATH=%BSF4OOREXX_HOME%;%PATH%"
   if cmd.eSetooRexxLibHome<>"" then      -- we get run by ooRexx >= 5 ?
   do
      tmpArr~append( cmd.eSetooRexxLibHome )
      cmd.eSetDLLPath="set PATH=%BSF4OOREXX_HOME%;%OOREXX_LIB_HOME%;%PATH%"
   end
   else
   do
      cmd.eSetDLLPath="set PATH=%BSF4OOREXX_HOME%;%PATH%"
   end

   cmd.eInstallScriptName="installBSF.cmd"
   cmd.eUninstallScriptName="uninstallBSF.cmd"

   cmd.eRunAsAdmin=getRunAsCmd(cmd.) /* command to run as local Administrator  */
   cmd.eInstallScriptName.RunAsAdmin="installBSF_runAsAdministrator.cmd"
   cmd.eUninstallScriptName.RunAsAdmin="uninstallBSF_runAsAdministrator.cmd"

   cmd.eTestScriptName   = "rexxj.cmd" /* "testBSF4Rexx.cmd"            */
   cmd.eSetEnvironmentScriptName="setEnvironment4BSF.cmd"
   cmd.eDLL="BSF4ooRexx850.dll"     /* define name of dynamic link library    */

   select
      when cmd.eBitness=32 then cmd.eDll.sourcePath="lib\BSF4ooRexx850.dll-32-x86"
      when cmd.eBitness=64 then cmd.eDll.sourcePath="lib\BSF4ooRexx850.dll-64-amd64"
      otherwise                 cmd.eDll.sourcePath="lib\panic-unknown-bitness-"cmd.eBitness
   end

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
do
   if cmd.eIsWindows then
   do
      cmd.eCopy cmd.eDll.sourcePath ".." || j.file.separator || cmd.eDLL  ">" cmd.eNullDevice
   end
   else  -- Unix
   do
      tmpTgt=qualify("../"cmd.eDLL)
      srcFn =qualify(cmd.eDll.sourcePath)

      if cmd.eUseSymbolicLink?=.true then
         tmpCmd=cmd.eSymbolicLink srcFn tmpTgt
      else
         tmpCmd=cmd.eCopy         srcFn tmpTgt

      cmd.eEcho qc(tmpCmd)
      tmpCmd

      if cmd.eUseSymbolicLink?=.true then
         cmd.eChownOfSymbolicLink tmpTgt
      else
         cmd.eChown tmpTgt
   end
end

if cmd.eCLRDir<>"" then -- .NET/CLR (common language runtime) support be put into eBinDir?
do
-- rgf, 20221004: copy jar-files to BSF4OOREXX_HOME/lib/ * !
   tmpCmd=cmd.eCopy cmd.eCLRDir"\*.jar" "..\lib" ">" cmd.eNullDevice
say   tmpCmd
   tmpCmd

-- rgf, 20221016: also the CLR dll go to ../lib
   tmpCmd=cmd.eCopy cmd.eCLRDir"\*.dll" "..\lib" ">" cmd.eNullDevice
say   tmpCmd
   tmpCmd

   tmpCmd=cmd.eCopy cmd.eCLRDir"\CLR.CLS" ".." ">" cmd.eNullDevice
say   tmpCmd
   tmpCmd

/* -- rgf, 20221004: CLASSPATH will show to "BSF4OOREXX_HOME/lib/ *" so no need to put explicitly on CLASSPATH anymore
   -- now determine CLR-related jars
   oldMax=cmd.eJarBaseNames.0
   do jarFname over cmd.eClr.eJars
      fullPath=qualify(".." || j.file.separator || jarFname)
      oldMax+=1
      cmd.eJarBaseNames.oldMax=fullPath   -- add jar file
      cmd.eJarBaseNames.0=oldMax          -- adjust number of elements
   end
-- */
end

/*
say "... debugging CLR additions, check whether copying took place correctly, here the list of jars to set:"
do i=1 to cmd.eJarBaseNames.0
say "    ... cmd.eJarBaseNames."i"=["cmd.eJarBaseNames.i"]"
end
exit
*/

   /* create the scripts   */
call create_REXXJ_Script
cmd.eCopy cmd.eTestScriptName ".." ">" cmd.eNullDevice   /* copy rexxj.{sh|cm} to superdirectory   */
call createEnvironmentScript  /* Windows probably needs this script        */
call createInstallScript      /* new on 2008-05-17, ---rgf
                                 Windows: will add environment values to registry,
                                 Linux:   will add the execution of setEnvironment4BSF4Rexx.sh to ~/bash.rc
                              */

call createRunAsScripts       /* 2010-03-07: for Vista and higher */

/**/
if cmd.eIsUnix=1 then         /* make sure rights are not fully available to the world */
   call setChmod cmd.

call info                  /* tell user what one can do now                   */
exit


/* create the install script: this will copy the BSF4Rexx jar-file and dynamic link library
   to the appropriate Java extension directory; in effect using BSF4Rexx as a genuine extension
   to Java;
*/
createInstallScript: procedure expose j. cmd. path.
  fn=cmd.eInstallScriptName            /* name of install script              */
  ui=cmd.eUninstallScriptName          /* name of uninstall script            */

  if \cmd.eIsMacOSX then		-- do not setup file associations and menus on MacOSX
     call "setupFileAssociations.rex"       /* create the file association files   */

  call stream fn, "c", "open replace"
  call stream ui, "c", "open replace"

  /* call lineout fn, cmd.eLeadIn */
  call lineout fn, cmd.eEcho qe(cmd.eHintString)
  call lineout fn, cmd.eEcho qe("installing ""BSF4ooRexx850""...")
  call lineout fn, ""

     /* uninstall: */
  /* call lineout ui, cmd.eLeadIn */
  call lineout ui, cmd.eEcho qe(cmd.eHintString)
  call lineout ui, cmd.eEcho qe("uninstalling ""BSF4ooRexx850""...")
  call lineout ui, ""

  if cmd.eIsUnix=1 then
  do
     if cmd.eIsMacOSX then
        tmpJvmLibName="libjvm.dylib"
     else
        tmpJvmLibName="libjvm.so"

     bWriteProlog=.false  -- for "/etc/profile" no epilog/prolog, for .bashrc: yes!

     do tmpTgt over .array~of(cmd.eAdd2Profile, cmd.eAdd2BashRc, cmd.eAdd2Zshenv)
        -- if cmd.eIsMacOSX, tmpTgt=cmd.eAdd2BashRc then     -- on MacOSX do not create .bashrc
        --   iterate

        bWriteProlog=(tmpTgt=cmd.eAdd2BashRc)   -- write pro/epilog if .bashrc

        appendTo=">>" tmpTgt

        call lineout fn, ""
        call lineout fn, cmd.eEcho qc("adding entries to the end of" tmpTgt "...")

        if bWriteProlog then
           call lineout fn, cmd.eEcho qc(cmd.eAdd2BashRcProlog) appendTo

        do i=1 to cmd.eAdd2setEnvironment.0
           call lineout fn, cmd.eEcho qc(cmd.eAdd2setEnvironment.i) appendTo
        end

        call lineout ui, cmd.eEcho qe("removing BSF4ooRexx850 entries from" tmpTgt "...")
        call lineout ui, cmd.e_sed_delete_profile tmpTgt

        if bWriteProlog then
           call lineout fn, cmd.eEcho qc(cmd.eAdd2BashRcEpilog) appendTo

        call lineout fn, ""
        call lineout ui, ""

        if tmpTgt<>cmd.eAdd2Profile then  -- let /etc/config remain root/root, change the others
        do
           call lineout fn, cmd.eEcho qc("make sure ownership and group is set to '"cmd.eRealUserName"' using command:" cmd.eChown tmpTgt "...")
           call lineout fn, cmd.eChown tmpTgt
           call lineout fn, ""
        end
     end

     tmpTgt=cmd.eLibDir || "/" || cmd.eDLL
     srcFn = qualify(directory()"/"||cmd.eDLL.sourcePath)   -- get fully qualified path
     if cmd.eUseSymbolicLink?=.true then
        tmpCmd=cmd.eSymbolicLink srcFn tmpTgt
     else
        tmpCmd=cmd.eCopy cmd.eDLL.sourcePath tmpTgt

     call lineout fn, cmd.eEcho qc(tmpCmd)
     call lineout fn, tmpCmd

     if cmd.eUseSymbolicLink?=.true then
        call lineout fn, cmd.eChownOfSymbolicLink tmpTgt
     else
        call lineout fn, cmd.eChown tmpTgt

     call lineout fn, ""

     tmpCmd=cmd.eDelete tmpTgt
     call lineout ui, cmd.eEcho qc(tmpCmd)
     call lineout ui, tmpCmd
     call lineout ui, ""

     -- rgf, 2021-08-29: create "/usr/local/bin" and "/usr/local/lib" if they do not exist already, never remove them
     localDirs=.array~of("/usr/local/bin", "/usr/local/lib")
     do dir over localDirs
        call lineout fn, "if ! [ -d" dir "] ; then "
        call lineout fn, "   " cmd.eEcho qc("creating" dir "...")
        call lineout fn, "    mkdir -pvm 755" dir
        call lineout fn, "fi"
     end
     call lineout fn, ""


     if cmd.eIsMacOSX<>1 then    -- on Linux also copy to lib32 or lib64, if they exist
     do
        -- rgf, 2010-08-11: cf. <http://wiki.winehq.org/WineOn64bit>, Fedora/SuSE vs. Debian-based
        if cmd.eTgtLib32<>.nil then
        do
           tmpTgt=cmd.eTgtLib32 || "/" || cmd.eDLL
           if cmd.eUseSymbolicLink?=.true then
              tmpCmd=cmd.eSymbolicLink srcFn tmpTgt
           else
              tmpCmd=cmd.eCopy cmd.eDLL.sourcePath tmpTgt

           call lineout fn, cmd.eEcho qc(tmpCmd)
           call lineout fn, tmpCmd
           if cmd.eUseSymbolicLink?=.true then
              call lineout fn, cmd.eChownOfSymbolicLink tmpTgt
           else
              call lineout fn, cmd.eChown tmpTgt
           call lineout fn, ""
        end

        if cmd.eTgtLib64<>.nil then   -- rgf, 2011-03-07
        do
           tmpTgt=cmd.eTgtLib64 || "/" || cmd.eDLL
           if cmd.eUseSymbolicLink?=.true then
              tmpCmd=cmd.eSymbolicLink srcFn tmpTgt
           else
              tmpCmd=cmd.eCopy cmd.eDLL.sourcePath tmpTgt

           call lineout fn, cmd.eEcho qc(tmpCmd)
           call lineout fn, tmpCmd
           if cmd.eUseSymbolicLink?=.true then
              call lineout fn, cmd.eChownOfSymbolicLink tmpTgt
           else
              call lineout fn, cmd.eChown tmpTgt
           call lineout fn, ""
        end
     end

     -- create delete statements, now for both, MacOS and Unix
     -- rgf, 2020-08-18: new logic, delete from general lib-directory and from respective bitness lib
     libs2look=.array~of("/usr/local/lib", "/usr/lib")
     do tmpLibDir over libs2look    -- for Linux, also check bitness lib directories
        libs2look~~append(tmpLibDir||cmd.eBitness) -- either append 32 or 64
     end

     do tmpTgtLib over libs2look
        if SysFileExists(tmpTgtLib) then
        do
           tmpCmd=cmd.eDelete tmpTgtLib || "/" || cmd.eDLLAllBaseNames
           call lineout ui, cmd.eEcho qc(tmpCmd)
           call lineout ui, tmpCmd
           call lineout ui, ""
        end
     end

     if cmd.bCreateLinkInB4RHome?=.true then -- rgf, 20170109
     do
        tmpPath2libjvm=cmd.ePath2JavaDLL || j.file.separator || tmpJvmLibName -- "libjvm.so"
        tmpTgt="/opt/BSF4ooRexx850/"tmpJvmLibName
        -- on Linux this needs to be a symbolic link, such that the JVM can find its Java home
        tmpCmd=cmd.eSymbolicLink tmpPath2libjvm tmpTgt

        call lineout fn, cmd.eEcho qc(tmpCmd)
        call lineout fn, tmpCmd
        call lineout fn, cmd.eChownOfSymbolicLink  tmpTgt

        call lineout fn, ""
     end

      -- copying Rexx scripts to cmd.eBinDir (/usr/local/bin)
     call lineout fn, cmd.eEcho qc("adding BSF4ooRexx850 scripts to" cmd.eBinDir "...")
     call lineout ui, cmd.eEcho qc("removing BSF4ooRexx850 scripts from" cmd.eBinDir "...")

     files2userBin=.array~of("../BSF.CLS", "../BSF_OnTheFly.cls", "../rexxj.sh", "../rexxj2.sh", "../rgf_util2.rex", "../rexxj2.sh", "rexxpaws.rex")
     do binFile over files2userBin
         tmpTgt=cmd.eBinDir || "/" || filespec('name',binFile)
         srcFn = qualify(directory()"/"||binFile)  -- get fully qualified path
         call lineout fn, cmd.eEcho qc("# // srcFn:" srcFn "// tmpTgt:" tmpTgt "...")

         if cmd.eUseSymbolicLink?=.true then
            tmpCmd=cmd.eSymbolicLink srcFn tmpTgt
         else
            tmpCmd=cmd.eCopy srcFn tmpTgt

         call lineout fn, cmd.eEcho qc(tmpCmd)
         call lineout fn, tmpCmd

         if cmd.eUseSymbolicLink?=.true then
            call lineout fn, cmd.eChownOfSymbolicLink tmpTgt
         else
            call lineout fn, cmd.eChown tmpTgt

         tmpCmd="rm -fv" cmd.eBinDir"/"filespec("Name", binFile)
         call lineout ui, cmd.eEcho qc(tmpCmd)
         call lineout ui, tmpCmd
     end
     call lineout fn, ""
     call lineout ui, ""


     if \cmd.eIsMacOSX then
     do
         if sysFileExists("/sbin/ldconfig") then   -- supply fully qualified name
            cmdLDconfig="/sbin/ldconfig"
         else                 -- fallback that may yield a runtime error "command not found"
            cmdLDconfig="ldconfig"
            -- only work on existing lib directories
         tmpCmd=cmdLDconfig
         do libDir over .array~of("/usr/local/lib", "/usr/local/lib32",  "/usr/local/lib64", "/opt/BSF4ooRexx850", "/usr/lib", "/usr/lib32", "/usr/lib64")
            if sysFileExists(libDir) then tmpCmd = tmpCmd libDir  -- lib directory exists, add it
         end

            -- a separate ldconfig targeting the libjvm.so directory we link to /opt/BSF4ooRexx
         tmpCmd2 = cmdLDconfig enquote(cmd.ePath2JavaDLL)

         call lineout fn, cmd.eEcho qc(tmpCmd)
         call lineout fn, tmpCmd
         call lineout fn, tmpCmd2
         call lineout fn, ""
         call lineout ui, cmd.eEcho qc(tmpCmd)
         call lineout ui, tmpCmd
         call lineout ui, tmpCmd2
         call lineout ui, ""

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
  else      -- non-Unix (Windows) branch: add statements to incorporate paths into registry
  do
      if cmd.eClrDir<>"" then    -- 2016-08-15, adding .NET/CLR support
      do
          call lineout fn, cmd.eEcho qc("Installing .Net/CLR assemblies into GAC (global assembly cache) ...")
          call lineout ui, cmd.eEcho qc("Uninstalling .Net/CLR assemblies from GAC (global assembly cache) ...")

          -- tmpSuper=".." || j.file.separator
          tmpSuper="..\lib\"  -- 2022-10-16: new target for CLR
          tmpGacUtil=enquote(qualify(tmpSuper"\..\clr\" || cmd.eCLR.GacUtilRgf))

          do assembly over cmd.eCLR.eAssemblies
             parse var assembly displayName ".dll"
             call lineout fn, tmpGacUtil "-i" enquote(qualify(tmpSuper || assembly))
             call lineout fn, tmpGacUtil "-l" displayName

             call lineout ui, tmpGacUtil "-u" enquote(qualify(tmpSuper || assembly))
             call lineout ui, tmpGacUtil "-l" displayName
          end

          call lineout ui, ""
          call lineout fn, ""
      end

      cmd="call rexx orx2reg.rex"      -- invoke utility to interact with Windows registry

      hint="Creating a backup of PATH and CLASSPATH registry values..."
      call lineout fn, cmd.eEcho qc(hint)
      call lineout ui, cmd.eEcho qc(hint)

      bkpFile="registryValues-"||cmd.eDateTimeEdited
      call lineout fn, cmd ">" bkpFile"-beforeAddBSF4RexxPathValues.bat"
      call lineout ui, cmd ">" bkpFile"-beforeDelBSF4RexxPathValues.bat"

      call lineout ui, ""
      call lineout fn, ""

         -- add BSF4ooRexx home directory to PATH
      paths=cmd.eSuperdir||j.path.separator||cmd.ePath2JavaDLL

      arg="/key:"q(cmd.eRegistryPath) "/path:"q(paths)
      tmpStr1=cmd "/add"    arg
      tmpStr2=cmd "/remove" arg
         -- inform user

      call lineout fn, cmd.eEcho qc("add BSF4ooRexx850-paths to environment variable 'Path' via registry ...")
      call lineout ui, cmd.eEcho qc("remove BSF4ooRexx850-paths from environment variable 'Path' via registry ...")

         -- carry out operation
      call lineout fn, tmpStr1
      call lineout ui, tmpStr2

      call lineout ui, ""
      call lineout fn, ""

         -- add bsf-jars to CLASSPATH
      tmpArgs=cmd.eSuperdir"\lib\*"    -- rgf, 20221004 instead of ""
      do i=1 to cmd.eJarBaseNames.0
         if tmpArgs<>"" then tmpArgs=tmpArgs || j.path.separator
         tmpArgs=tmpArgs || cmd.eJarBaseNames.i
      end

      if tmpArgs<>"" then
      do
         -- add current directory followed by user's homedir "lib" to pick up all jars there, if any
         tmpArgs=tmpArgs || ";.;" || cmd.eHomeDir"\BSF4ooRexx\lib\" || "*"

            -- make sure the current directory is searched as well
         if cmd.eRegistryClassPath~left(5)="HKCU\" then  -- rgf, 2011-01-25: make sure that system set CLASSPATH is honored !
            arg="/key:"q(cmd.eRegistryClassPath) "/path:"q("%CLASSPATH%;"tmpArgs)
         else
            arg="/key:"q(cmd.eRegistryClassPath) "/path:"q(tmpArgs)

         arg=arg~changestr("%", "[25x]") -- make sure that cmd.exe does not expand

         tmpStr1=cmd "/add"    arg
         tmpStr2=cmd "/remove" arg
            -- inform user
         call lineout fn, cmd.eEcho qc("add BSF4ooRexx850-jars to environment variable 'CLASSPATH' via registry ...")
         call lineout ui, cmd.eEcho qc("remove BSF4ooRexx850-jars from environment variable 'CLASSPATH' via registry ...")

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

  lastMessage="done."
  if cmd.eInstallSystemWide=.true then -- installed systemwide?
     lastMessage=lastMessage "PLEASE logoff and logon for the changes to take affect!"

  call lineout fn, ""
  call lineout fn, cmd.eEcho qe(lastMessage)
  call lineout fn, ""

  call lineout fn, cmd.eEcho cmd.eSupportInfos
  call lineout fn, ""

  call lineout ui, ""
  call lineout ui, cmd.eEcho qe(lastMessage)
  call lineout ui, ""
  call lineout ui, cmd.eEcho cmd.eSupportInfos
  call lineout ui, ""

  call stream fn, "c", "close"
  call stream ui, "c", "close"

  if cmd.eSetExecutable<>"" then       /* set file executable (Unix)    */
  do
     cmd.eSetExecutable cmd.eInstallScriptName
     cmd.eChown         cmd.eInstallScriptName

     cmd.eSetExecutable cmd.eUninstallScriptName
     cmd.eChown         cmd.eUninstallScriptName
  end
  return


/* Windows only: create runAs batch file for Vista and higher, rgf 2010-03-07 */
createRunAsScripts: procedure expose j. cmd. path.
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

     if cmd.eIsUnix=.true then
        cmd.eChown fn      -- change ownership
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

  /* call lineout fn, cmd.eLeadIn */
  call lineout fn, cmd.eEcho qe(cmd.eHintString)
  call lineout fn, cmd.eEcho qe("installing optional Java extension ""BSF4ooRexx850""...")
  call lineout fn, ""

     /* uninstall: */
  call lineout ui, cmd.eEcho qe(cmd.eHintString)
  call lineout ui, cmd.eEcho qe("uninstalling optional Java extension ""BSF4ooRexx850""...")
  call lineout ui, ""

  tgtDir=getDir(j.java.ext.dirs, 1) /* since Java 6: 1st directory "jre/lib/ext", 2nd directory: system-wide */
  call lineout fn, cmd.eEcho qe("adding the BSF4ooRexx850 jar file(s) to the Java extension directory" q(tgtDir) "...")
  call lineout ui, cmd.eEcho qe("deleting the BSF4ooRexx850 jar file(s) from the Java extension directory" q(tgtDir) "...")

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

     if cmd.eCommonsLoggingJarName="" then   /* hmm, not found in current directory!   */
     do
        msg="/// COULD NOT FIND the OPTIONAL jar-file for the package org.apache.commons.logging AT INSTALL TIME !"
        call lineout fn, cmd.eEcho qe(msg)
        call lineout fn, cmd.eEcho qe("///  Look for the commons logging jar-file at http://jakarta.apache.org/commons")
        call lineout fn, cmd.eEcho qe("///  and copy the jar-file to" qw(tgtDir) "yourself, if you want")
        call lineout ui, cmd.eEcho qe(msg)
     end

     call lineout fn, ""
     call lineout ui, ""
  end

  tgtDir=cmd.eLibDir
  call lineout fn, cmd.eEcho qe("adding the BSF4ooRexx850 dynamic link library file to the lib directory" q(tgtDir) "...")
  call lineout fn, cmd.eCopy qw(cmd.eDLL) qw(tgtDir)
  call lineout fn, ""
     /* uninstall: */
  call lineout ui, cmd.eEcho qe("delete the BSF4Rooexx dynamic link library file from the lib directory" q(tgtDir) "...")
  call lineout ui, cmd.eDelete qw(tgtDir || j.file.separator || cmd.eDLL)
  call lineout ui, ""

  tgtDir=cmd.eBinDir
  call lineout fn, cmd.eEcho qe("adding the Rexx scripts and ooRexx class files to" q(tgtDir) "...")
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

  call lineout ui, ""
  call lineout ui, cmd.eEcho qe("done.")
  call lineout ui, ""
  call lineout ui, cmd.eEcho cmd.eSupportInfos
  call lineout ui, ""

  call stream fn, "c", "close"
  call stream ui, "c", "close"

  if cmd.eSetExecutable<>"" then       /* set file executable (Unix)    */
  do
     cmd.eSetExecutable cmd.eInstallScriptName
     cmd.eChown         cmd.eInstallScriptName

     cmd.eSetExecutable cmd.eUninstallScriptName
     cmd.eChown         cmd.eUninstallScriptName
  end
  return



/* create the "rexxj.{cmd|sh}script which sets up all needed parameters to run Rexx programs from
   this directory */
create_REXXJ_Script: procedure expose j. cmd. path.
  fn=cmd.eTestScriptName


  call stream fn, "c", "open replace"

  if cmd.eIsUnix=.false then
     call lineout fn, cmd.eEchoOff

  call lineout fn, cmd.eLineComment "created:" qe(cmd.eHintString)
  if cmd.eRexxMajorVersion<5 then
     call lineout fn, cmd.eLineComment "Rexx version used to run this script:" qe(cmd.eRexxVersion)
  else
     call lineout fn, cmd.eLineComment "Rexx version used to run this script:" qe(cmd.eRexxVersion)", located in:" qe(.rexxInfo~executable)

  call lineout fn, cmd.eLineComment "Java version used to run this script:" qe(j.java.version)", located in:" qe(j.java.home)
  call lineout fn, cmd.eLineComment "script to run ""BSF4ooRexx850"" programs without prior installation ..."
  call lineout fn, ""

  if cmd.eIsUnix=.false then
  do
     call lineout fn, cmd.eSetLocal    /* on Windows: create a local environment */
     call lineout fn, ""
  end

  call lineout fn, cmd.eLineComment "set BSF4OOREXX_HOME variable"
  call lineout fn, cmd.eSetBSF4OOREXX_HOME  /* set BSF4OOREXX_HOME */
  call lineout fn, ""

  call lineout fn, cmd.eLineComment "get fully qualified path for BSF4OOREXX_HOME/lib"
  if cmd.eIsUnix=1 then                /* find bsf4ooRexx*bin.jar */
  do
     call lineout fn, "TMP_BSF4OOREXX_JAR=$BSF4OOREXX_HOME/lib/*" -- jar files go there since 850
  end
  else   -- Windows
  do
     call lineout fn, "echo bsf4rexx_home: %BSF4OOREXX_HOME%"
     -- need to change directory as tests showed wrong absolute path otherwise (takes current dir :( )!
     call lineout fn, 'for /f "usebackq tokens=*" %%a in (`cd`) do set CURR_DIR=%%a'
     call lineout fn, "cd /d %BSF4OOREXX_HOME%"      -- also change drive letter

     call lineout fn, "for /F ""delims="" %%I in ('""dir /B %BSF4OOREXX_HOME%\lib 2>null""') do set TMP_BSF4OOREXX_JAR=%%~fI"
     call lineout fn, "cd /d %CURR_DIR%"           -- also change drive letter
  end
  call lineout fn, ""

  if cmd.eSetooRexxLibHome<>"" then         -- use .RexxInfo to get library path
  do
     call lineout fn, cmd.eLineComment "set OOREXX_LIB_HOME variable"
     call lineout fn, cmd.eSetooRexxLibHome
     call lineout fn, ""
  end

  -- rgf, 2010-08-01
  call lineout fn, cmd.eLineComment "set PATH environment variable"
  call lineout fn, cmd.eSetPath        /* set PATH */
  call lineout fn, ""

  call lineout fn, cmd.eLineComment "set CLASSPATH environment variable"

  tmpArr=.array~new

  if cmd.eIsWindows=1 then
  do
     parse var cmd.eSetCP prefix '=' rest
      -- within an if statement '%' must be replaced by '!'
     tmpArr~append('if "%TMP_BSF4OOREXX_JAR%" == "" ('           )
     tmpArr~append('   'cmd.eSetCP~changestr('%','!')                                )
     tmpArr~append(')  else ( 'prefix'=!TMP_BSF4OOREXX_JAR!;'rest~changestr('%','!') )
     tmpArr~append(') '                                          )
  end
  else   -- Unix
  do
     -- tmpArr~append(cmd.eSetCP)   -- set CLASSPATH on Unix
     tmpArr~append('[ "$TMP_BSF4OOREXX_JAR" != "" ] && export CLASSPATH=$TMP_BSF4OOREXX_JAR:$CLASSPATH')
  end
  call lineout fn, tmpArr~toString('l',.endofline)
  call lineout fn, ""

  if cmd.eIsUnix=1 then                /* ??LD_LIBRARY_PATH    */
  do
     call lineout fn, cmd.eLineComment "set" cmd.eLDlibPath
     call lineout fn, cmd.eSetDLLPath
     call lineout fn, ""
  end

  call lineout fn, cmd.eLineComment "Java start command, allows passing arguments to Rexx script"
  tmpStr1="-Djava.library.path=" || cmd.eQuote

  if cmd.eIsUnix=1 then
  do
     tmpStr1=tmpStr1 || "$"cmd.eLDlibPath || cmd.eQuote
  end
  else   -- windows
  do
     tmpArr=.array~of( -
         'if not "%PREPEND_LIBRARY_PATH%"=="" ( set TMP_LIB_PATH=!PREPEND_LIBRARY_PATH!;!BSF4OOREXX_HOME!' , -
         '                             ) else ( set TMP_LIB_PATH=!BSF4OOREXX_HOME!)                      ' )

     tmpArr~append('if not "!OOREXX_LIB_HOME!"=="" (set TMP_LIB_PATH=!TMP_LIB_PATH!;!OOREXX_LIB_HOME!)' )
     tmpArr~append('set TMP_LIB_PATH=!TMP_LIB_PATH!;'j.java.library.path )

     call lineout fn, ""
     call lineout fn, tmpArr~toString('L',.endOfLine)
     call lineout fn, ""

     tmpStr1 = tmpStr1 || "%TMP_LIB_PATH%" || cmd.eQuote
  end

  -- rgf, 20180614: the CLASSPATH may get too large for the (Windows) command line, and Java
  --                honors the CLASSPATH environment variable, so no need to explicitly state it
  tmpStr2=""      -- do not supply CLASSSPATH via the command line (may break on Windows)
  tmpStr3= iif(cmd.eIsUnix=1, "$BSF4Rexx_JavaStartupOptions", "%BSF4Rexx_JavaStartupOptions%")

  -- rgf, 2011-02-27: if a Java 64-bit then explicitly start Java in Rexx-bitness (32 or 64 bit) mode
  tmpStr0=""
  if cmd.eBitness=32, j.sun.arch.data.model=64 then
     tmpStr0="-d32"

  /* 2021-07-22, rgf: new strategy:
      1. if JAVA_HOME set, search and if found, use its jvm.dll/libjvm.{so|dylib}
      2. use the Java as located and used by setupBSF.rex
      3. use 'java' such that the operating system gets used to find and run it
  */

  call lineout fn, cmd.eLineComment "if JAVA_HOME defined, use its java binary"
  if cmd.eIsWindows=1 then
  do
     tmpArr=.array~of( -
     'set JAVA_BINARY='strip(cmd.eJavaBinary,"both",'"')                                            , -
     '@rem enables referring via exclamation mark within blocks; from now on must not use endlocal' , -
     'if not "%JAVA_HOME%" == "" (                                                                ' , -
     '   set jexe=!JAVA_HOME!\bin\java.exe                                                        ' , -
     '   if exist "!jexe!"  (                                                                     ' , -
     '     set java_binary=!jexe!                                                                 ' , -
     '   ) else (                                                                                 ' , -
     '      set jexe=!JAVA_HOME!\jre\bin\java.exe                                                 ' , -
     '      if exist "!jexe!" (                                                                   ' , -
     '        set java_binary=!jexe!                                                              ' , -
     '      )                                                                                     ' , -
     '   )                                                                                        ' , -
     ')                                                                                           ' , -
     '@rem if original java.exe is not present anymore use just java as a last resort             ' , -
     'if not exist "%JAVA_BINARY%" set JAVA_BINARY=java                                           ' )
  end
  else   -- Unix
  do
     tmpArr=.array~new
     tmpArr~append( 'if [ -e "${JAVA_HOME}/bin/java" ] ; then JAVA_BINARY=${JAVA_HOME}/bin/java ;')
     tmpArr~append( 'elif [ -e "${JAVA_HOME}/jre/bin/java"  ] ; then JAVA_BINARY=${JAVA_HOME}/jre/bin/java ;' )

            -- try to get the java binary that is being used for the creation step;
     if j.java.home<>"" then
     do
        call sysFileTree j.java.home"/java", "files.", "FOS"   -- get all "java" binaries
        binJava=""
        do i=1 to files.0
           if files.i~pos("/server/")>0 then binJava=files.i   -- use the "server"
        end
        if files.0>0 then
        do
           if binJava="" then binJava=files.1   -- use the first java binary (maybe the only one
           tmpArr~append( 'elif [ -e "'binJava'" ] ; then JAVA_BINARY=' || binJava ';' )
        end
     end

         -- that's the default Java, if JAVA_HOME was not set, as it got used for the setupBSF.rex run
     tmpArr~append( 'elif [ -e "'cmd.eJavaBinary'" ] ; then JAVA_BINARY='cmd.eJavaBinary ';' )
     tmpArr~append( 'else JAVA_BINARY=java' )   -- last resort: let opsys locate java: currently in /usr/bin/java, in the future in /usr/local/bin/java?
     tmpArr~append( 'fi' )
  end

  call lineout fn, tmpArr~toString('l',.endofline) -- write string to file
  call lineout fn, ""

  if cmd.eIsWindows=1 then
  do
     call lineout fn, enquote("%JAVA_BINARY%") tmpStr0 tmpStr1 tmpStr2 tmpStr3 cmd.eRexxDispatch
  end
  else   -- Unix
  do
     if cmd.eIsMacOSX=1 then
     do
        call lineout fn, cmd.eLineComment "export DYLD_PRINT_LIBRARIES=1"
        call lineout fn, cmd.eLineComment "export DYLD_PRINT_LIBRARIES_POST_LAUNCH=1"
        call lineout fn, cmd.eLineComment "export DYLD_PRINT_RPATHS=1"
        call lineout fn, cmd.eLineComment "export DYLD_PRINT_STATISTICS=1"
     end
     else   -- Linux
     do   -- cf. e.g. <http://www.bnikolic.co.uk/blog/linux-ld-debug.html> (20210727)
        call lineout fn, cmd.eLineComment "export LD_DEBUG=libs reloc statistics"
     end

     call lineout fn, ""
     call lineout fn, enquote("$JAVA_BINARY") tmpStr0 tmpStr1 tmpStr2 tmpStr3 cmd.eRexxDispatch
  end

  call stream fn, "c", "close"

  if cmd.eSetExecutable<>"" then       /* set file executable (Unix)    */
  do
     cmd.eSetExecutable cmd.eTestScriptName
     cmd.eChown         cmd.eTestScriptName
  end

  return



/* creates the script that sets up the environment for Rexx to find the Java-DLL for
   demand loading it. */
createEnvironmentScript: procedure expose j. cmd. path.
  fn=cmd.eSetEnvironmentScriptName

  call stream fn, "c", "open replace"
  call lineout fn, cmd.eLineComment "created:" qe(cmd.eHintString)
  call lineout fn, cmd.eLineComment "run this script before invoking a BSF4ooRexx850 program that loads Java on its own."
  call lineout fn, ""

  call lineout fn, cmd.eSetBSF4OOREXX_HOME
  call lineout fn, ""

  if cmd.eIsUnix=1 then -- don't echo in Unix as this script may be executed on every new shell
     call lineout fn, cmd.eLineComment "setting the CLASSPATH environment variable..."
  else
     call lineout fn, cmd.eEcho qe("setting the CLASSPATH environment variable...")

  call lineout fn, cmd.eSetCP
  call lineout fn, ""

  if cmd.eIsUnix<>.true then
  do
     call lineout fn, ""

     call lineout fn, cmd.eLineComment "setting the PATH environment variable..."
     call lineout fn, cmd.eSetPath        /* set PATH             */

     call lineout fn, ""
     call lineout fn, cmd.eSetDLLPath

     call lineout fn, ""
     call lineout fn, cmd.eEcho qe(".")
     call lineout fn, cmd.eEcho cmd.eSupportInfos
     call lineout fn, ""
  end

  call stream fn, "c", "close"

  if cmd.eSetExecutable<>"" then       /* set file executable (Unix)    */
  do
     cmd.eSetExecutable cmd.eSetEnvironmentScriptName
     cmd.eChown         cmd.eSetEnvironmentScriptName
  end

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
getJavaProps: procedure expose cmd. path.
   signal on any name usage                           /* trap to usage label     */
   parse arg altJavaBinary

         /* get the Java properties    */
   if cmd.eJavaBinary="" then
   do
       if altJavaBinary<>"" then -- as of 20170104: supplied only on Windows from determinePath2javaBinary
          java=altJavaBinary     -- use fully qualified, alternate Java binary
       else
          java="java"             /* default     */
   end
   else
       java=cmd.eJavaBinary    /* use command-line value  */

   j.=""
      -- execute the Java program in current directory (bsf4oorexx/install)
   command=java "-cp . JavaInfo4BSF4RexxInstallation | rxqueue"
   command  -- execute the command

   i=0
   max=0
   -- j.=""
   sort.=""
   if queued()=0 then
   do
      say "PANIC (getJavaProps) after executing:" pp(command)
      say "PANIC (getJavaProps): nothing in external Rexx queue, cannot get at the Java properties, aborting..."
      say
      call usage
   end
   else
   do i=1 to 99999 while queued()<>0
      parse pull pName "=" "[" >1 pVal "]" <1   -- remove leading "[" and trailing "]"
      if pname="" then iterate      /* blank line (the last line?)   */

      max=max(max, length(pName))   /* memorize longest name         */

      j.i.eName=pName               /* use a number based index      */
      if pVal~abbrev("0x") then     -- an escaped (hex-encoded) string?
         pVal=substr(pVal,3)~x2c    -- recreate original value as supplied by Java

      j.i.eValue=pVal               -- assign Java value
      j.pName=j.i.eValue            /* save in mixed case            */

      uName=translate(pName)        /* save in mixed case            */
      j.uName=j.i.eValue
      j.0=i                         /* save maximum number of entries*/
   end
   return j.



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


/*
   rgf, 2021-07-17: adjust search to new realities as there is no "/System/Library/Java/JavaVirtualMachines" anymore on MacOS
*/
getPath2JavaOnMacOSX: procedure
   use strict arg j.
   call SysFileTree j.java.home"/libjvm.dylib", "files.", "FOS"
   if files.0=0 then return "" -- not found

   location=filespec("Location", files.[files.0])  -- get location of last listed libjvm.dylib
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
  -- return filename
  return ""    -- indicate not found by returning empty string



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
  say "cmd.eSetBSF4OOREXX_HOME: ["cmd.eSetBSF4OOREXX_HOME"]"
  say "cmd.eSetPath:          ["cmd.eSetPath"]"
  say "cmd.eSetCP:            ["cmd.eSetCP"]"
  if cmd.ePath2JavaDll<>"" | cmd.eIsUnix=0 | cmd.eIsMacOSX then -- rgf, 2017-05-09
     say "cmd.ePath2JavaDll:     ["cmd.ePath2JavaDll"]"
  say
  say "---"
  say
  say "the following scripts have been created:"
  say
  say " " pp(cmd.eTestScriptName)": allows to run ""BSF4ooRexx850"" programs without installation, e.g. to test the setup/installation:"
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
  say " " pp(cmd.eInstallScriptName)": run to install" q("BSF4ooRexx850")
  say
  if cmd.eIsUnix=0 then
     say "       to install as Administrator run:" cmd.eInstallScriptName.RunAsAdmin
  else
     say "       to install as root run:" q("./"cmd.eInstallScriptName.RunAsAdmin)
  say
  say

  say " " pp(cmd.eUninstallScriptName)": run to uninstall" q("BSF4ooRexx850")
  say
  if cmd.eIsUnix=0 then
     say "       to uninstall as Administrator run:" cmd.eUnInstallScriptName.RunAsAdmin
  else
     say "       to uninstall as root run:" q("./"cmd.eUnInstallScriptName.RunAsAdmin)
  say

  say
  say " " pp(cmd.eSetEnvironmentScriptName)": helper script for setting up the environment"
  say "              for running BSF4ooRexx850 rograms"
  say

  say "Good luck! :-)  [You may also want to take a look at these generated scripts.]"
  exit 0



/* give user feedback and show usage of this script */
usage:
  say
  say "This program creates scripts to test, install and uninstall ""BSF4ooRexx850""."
  say
  say "Usage:     rexx setupBSF [fully-qualified-file-name-of-java.exe [dir-to-copy-scripts-to]]"
  say "  (dir-to-copy-scripts-to MUST exist)"
  say
  say "Examples:  rexx setupBSF"

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
     say "             (will also link 'libjvm.so' to '/opt/BSF4ooRexx850')"
  end
  say

  if cmd.eIsWindows=.true then
  do
     say "           rexx setupBSF E:\jdk1.6\jre\bin\java.exe D:\windows\system32"
     say "           - will use ""java.exe"" from the given first argument"
     say "           - will copy the BSF4ooRexx850-support-files to the Windows system directory"
  end
  else
  do
     say "           rexx setupBSF /usr/lib/jvm/java-6-openjdk/jre/bin /usr/local/bin"
     say "           - will use the binary ""java"" from the given first argument"
     say "           - will copy the BSF4ooRexx850-support-files to /usr/local/bin"
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
   say "determinePath2javaBinary(), starting ..." "line #" pp(.line)

   if cmd.eIsWindows=.false then
   do
      cmd.eJavaBinary=getFromPath("java") /* get fully qualified path   */
      say "determinePath2javaBinary(), getFromPath() returned: ["cmd.eJavaBinary"]" "line #" pp(.line)
      if cmd.eJavaBinary<>"" then
      do
          j.=getJavaProps(cmd.eJavaBinary)   -- get and assign Java properties; bitness test done in caller part
          say "determinePath2javaBinary(), found! Java binary:" pp(cmd.eJavaBinary)", returning." "line #" pp(.line)
      end
      return
   end

   ----------------------------------------------------------------------------
   -- from here on it is Windows only!
      -- try to find java.exe in %windir% directory
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

      say "determinePath2javaBinary(), 'ProgramW6432':" pp(wProgramW6432)", cmd.eJavaBinary="pp(cmd.eJavaBinary) "line #" pp(.line)
   end

   if cmd.eJavaBinary<>"" then   -- 20170104: test for matching bitness
   do
      tmpJ.=getJavaProps(cmd.eJavaBinary)
         -- if Java and ooRexx do not match in bitness, look for another Java
      if tmpJ.sun.arch.data.model<>cmd.eBitness then
      do
         say "determinePath2javaBinary(), Java bitness does not match ooRexx bitness, Java tested:" pp(cmd.eJavaBinary)", continuing search ..." "line #" pp(.line)
         cmd.eJavaBinary=""
      end
      else  -- found, assign Java property stem, return
      do
          j.=tmpJ.
          say "determinePath2javaBinary(), found! Java binary:" pp(cmd.eJavaBinary)", returning." "line #" pp(.line)
          return
      end
   end

   ----------------------------------------------------------------------------
      -- try to find java.exe on PATH
   javaExe="java.exe"                 -- executable's name
   tmpJavaBinary=getFromPath(javaExe) -- on path ?
   say "determinePath2javaBinary(), getFromPath() returned: ["tmpJavaBinary"]" "line #" pp(.line)

      -- 32-bit Rexx on 64-bit Windows, but java.exe not in "ProgramFiles(x86)", then we point to 64-bit Java, reset
   if cmd.eBitness=32, wProgramW6432<>"", tmpJavaBinary~caselessAbbrev(wProgramFiles86)=.false then
   do
       say "determinePath2javaBinary(), 32-bit Rexx on 64-bit Windows, but java.exe seems to be in a 64-bit directory, reset & continue search" "line #" pp(.line)
       tmpJavaBinary=javaExe        --  reset, make sure search continues
   end
   else  -- check whether bitness really matches (it should, but who knows what a user did! )
   do
      tmpJavaBinary=qw(tmpJavaBinary)
      tmpJ.=getJavaProps(tmpJavaBinary)
         -- if Java and ooRexx do not match in bitness, look for another Java
      if tmpJ.sun.arch.data.model<>cmd.eBitness then
      do
         say "determinePath2javaBinary(), Java bitness does not match ooRexx bitness, Java tested:" pp(tmpJavaBinary)", continuing search ..." "line #" pp(.line)
         tmpJavaBinary=javaExe        --  reset, make sure search continues
      end
      else  -- found, save cmd.eJavaBinary assign Java property stem, return
      do
         cmd.eJavaBinary=tmpJavaBinary
         j.=tmpJ.
          say "determinePath2javaBinary(), found! Java binary:" pp(cmd.eJavaBinary)", returning." "line #" pp(.line)
         return
      end
   end

   ----------------------------------------------------------------------------
      -- try to find java.exe via Windows registry
   say "determinePath2javaBinary(), Java binary not found, consulting registry ..." "line #" pp(.line)
   wsh  = .OleObject~new("WScript.Shell")
   baseKey = "HKLM\Software\JavaSoft\Java Runtime Environment"

   version=wsh~regRead(basekey"\CurrentVersion")
   say "... ["||basekey"\CurrentVersion]:" version

   javaHome=wsh~regRead(basekey"\"version"\JavaHome")
   say "... ["||basekey"\"version"\JavaHome}: ["javaHome"]"
   tmpJavaBinary=javaHome"\bin\java.exe"

      -- 32-bit Rexx on 64-bit Windows, but java.exe not in "ProgramFiles(x86)", then we point to 64-bit Java, reset
   if cmd.eBitness=32, wProgramW6432<>"", javaHome~caselessAbbrev(wProgramFiles86)=.false then
   do
       say "determinePath2javaBinary(), 32-bit Rexx on 64-bit Windows, but java.exe seems to be in a 64-bit directory, reset & continue search" "line #" pp(.line)
       tmpJavaBinary=""                   --  reset, make sure search continues
   end

   if sysFileExists(tmpJavaBinary) then   -- o.k. finally found !?
   do
      tmpJavaBinary=qw(tmpJavaBinary)
      tmpJ.=getJavaProps(tmpJavaBinary)
         -- if Java and ooRexx do not match in bitness, look for another Java
      if tmpJ.sun.arch.data.model<>cmd.eBitness then
      do
         say "determinePath2javaBinary(), Java bitness does not match ooRexx bitness, Java tested:" pp(tmpJavaBinary)", continuing search ..." "line #" pp(.line)
         tmpJavaBinary=javaExe        --  reset, make sure search continues
      end
      else  -- found, save cmd.eJavaBinary assign Java property stem, return
      do
         cmd.eJavaBinary=tmpJavaBinary
         j.=tmpJ.
          say "determinePath2javaBinary(), found! Java binary:" pp(cmd.eJavaBinary)", returning." "line #" pp(.line)
         return
      end
   end


   ----------------------------------------------------------------------------
      -- not yet found try looking in the file system, ---rgf, 20150512
   say "determinePath2javaBinary(), Java binary not found, explicitly looking in standard program file directories ..." "line #" pp(.line)
   pf=wProgramFiles           -- value("ProgramFiles",,"environment")
   if cmd.eBitness=32 then    -- Rexx is 32-bit
   do
       pf=wProgramFiles86     -- value("ProgramFiles(x86)",,"environment")   -- on 64-bit Windows?

       if pf="" then -- on 32-bit Windows
          pf=wProgramFiles    -- value("ProgramFiles",,"environment")
   end
   searchPattern=pf"\Java\java.exe"
   say "determinePath2javaBinary(), java.exe not found yet, looking in ["searchPattern"] and its subdirectories ..." "line #" pp(.line)
     -- try to locate java.exe, use the last one (assuming it is the newest)
   call SysFileTree searchPattern, "files.", "FSO"
   count=files.0
   if count>0 then
   do
       cmd.eJavaBinary=qw(files.count)
       say "determinePath2javaBinary(), java.exe found in" count " places using ["searchPattern"], returning ["files.count"]." "line #" pp(.line)
   end

   do i=1 to count.0
       cmd.eJavaBinary=qw(files.count)
       say "determinePath2javaBinary(), checking files."i"/"count.0" ["files.count"]." "line #" pp(.line)
       tmpJ.=getJavaProps(cmd.eJavaBinary)
          -- if Java and ooRexx do not match in bitness, look for another Java
       if tmpJ.sun.arch.data.model<>cmd.eBitness then
       do
          say "determinePath2javaBinary(), Java bitness does not match ooRexx bitness, Java tested:" pp(cmd.eJavaBinary)", continuing search ..." "line #" pp(.line)
       end
       else  -- found, save cmd.eJavaBinary assign Java property stem, return
       do
          j.=tmpJ.
          say "determinePath2javaBinary(), found! Java binary:" pp(cmd.eJavaBinary)", returning." "line #" pp(.line)
          return
       end
   end

   cmd.eJavaBinary=""      --
   say "determinePath2javaBinary(): Java (with matching bitness) NOT found!" "line #" pp(.line)
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


/* 2016-08-15, ---rgf
   enquote in double quotes (for paths containing non-standard characters or white space)
*/
::routine enquote
  return '"' || arg(1) || '"'



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
      -- return cmd.eElevation.Exe "-wait -k"
      return cmd.eElevation.Exe  -- 2016-08-15, rgf: adjust argument for elevate.rex
   end
   else     -- use runas
   do
      wnw = .OLEObject~New("WScript.Network")

      computerName=wnw~ComputerName
      -- userDomain  =wnw~UserDomain
      -- userName    =wnw~UserName

      return "runas /env /user:"wnw~ComputerName"\Administrator"
   end


-- 2020-08-18, rgf: determine bitness related library directories
::routine unixDetermineTargetLibDirs
  use arg cmd.

  cmd.eTgtLib32=.nil                -- make sure we start out with no target for 32-bit
  cmd.eTgtLib64=.nil                -- make sure we start out with no target for 64-bit

  tmpLibDir=cmd.eLibDir||cmd.eBitness  -- create bitness specific library directory name
  if SysFileExists(tmpLibDir) then
  do
           if cmd.eBitness=32 then cmd.eTgtLib32=tmpLibDir
      else if cmd.eBitness=64 then cmd.eTgtLib64=tmpLibDir
  end



-- 2012-02-12, rgf: new logic - use cmd.eBitness and then look for "librexx.so" and
--                  copy appropriate "libBSF4ooRexx" to the same location
::routine unixDetermineTargetLibDirs_from_20120212
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
      "find .. -type d -exec chmod 775 '{}' \;" /* 755 make all directories traversable; rwx-rwxr-x   */
      "find .. -type f -exec chmod 664 '{}' \;" /* make all files rw-rw-r--                           */

            /* make all executable files executable: */
      chunk1="find .."   -- leadin, start from parent directory
      chunk2="-type f -execdir chmod ugo+x '{}' \;"   /* make sure executable gets set for all */
      tmpStr=""
      do ext over .array~of("sh", "so", "rex", "rexx", "rxj", "rxo", "rexxj", "jrexx", "cls", "frm", "orx", "testGroup", "testUnit", "exe", "class", "cmd", "py", "js", "nrx", "jacl")
         if tmpStr="" then tmpStr =           '-iname "*'ext'"'
                      else tmpStr = tmpStr '-o -iname "*'ext'"'
      end

      -- rgf, 2017-02-03: as the libBSF4ooRexx files changed (bitness and architecture appended) make sure they get ther x-bit set
      tmpStr = tmpStr '-o -iname "libBSF4ooRexx850*"'
      cmd = chunk1 "\(" tmpStr "\)" chunk2   -- build command
      say "setupBSF.rex: line #" pp(.line) cmd  -- show command (for logging)
      cmd                                    -- let the shell run the command
   end
   else  /* just make sure that all (possibly newly created) .sh-files in the current directory get set to: 775 */
   do
       'find . -iname "*.sh" -type f -exec chmod 775 ''{}'' \;'
   end
   return


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

::routine pp   -- rgf, 2017-02-04
  return "["arg(1)"]"

/* Unix: returns the group name to be used in 'chown' command:
   - if "staff" present in "/etc/group" use it (Linux: full rights to "/usr/local")
   - else use real user name as the group name
*/
::routine determineGroupName
  use strict arg groupName

  s=.stream~new("/etc/group")~~open("read")
  arr=s~arrayin
  s~close
  bFound=.false      -- needle "staff:" found?
  do entry over arr while bFound=.false
     bFound=entry~abbrev("staff:")  -- allow this to work prior to ooRexx 5.0 which has "startsWith"
  end
  if bFound then return "staff"     -- o.k. found, use group name "staff"
  return groupName


/* Unix: if MacOS then use "/Users/<userid>", if "root", then "/var/root"

   TODO: Android "/data/media/<userid>", cf. <https://en.wikipedia.org/wiki/Home_directory>
*/
::routine determineHomeDir
  use strict arg userName, isMacOS=.false

  if isMacOS=.true then
  do
     if userName="root" then homedir="/var/root"
                        else homedir="/Users/"userName
     if \sysFileExists(homedir) then homedir="$HOME"  -- fall back to environment variable
     return homedir
  end

  s=.stream~new("/etc/passwd")~~open("read") -- read passwd file
  arr=s~arrayin
  s~close
  bFound=.false      -- needle "staff:" found?
  needle=userName":"
  do entry over arr until bFound=.true
     bFound=entry~abbrev(needle) -- allow this to work prior to ooRexx 5.0 which has "startsWith"
  end
  if bFound=.false then return "$HOME"    -- not found, fall back to environment variable

  parse var entry uname ":" pwd ":" uid ":" gid ":" gecos ":" homeDir ":" shell
  if sysFileExists(homeDir) then return homeDir -- done

  return "$HOME"     -- not found, fall back to environment variable
