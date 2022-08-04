#!/usr/bin/env rexx
/*
   Name:    createPortableB4R.rex
   Purpose: create a bare-bone "_portable_" version of BSF4ooRexx with the directories
               - bin: PATH needs to point to this, on Windows also the dll goes here
               - doc: content of "bsf4oorexx/information" folder
               - jar: all Java jar/zip files that need to be placed on CLASSPATH (use '*' to include all)
               - lib:   the shared object/dynamic link library (gets copied from "bsf4oorexx/install/lib")
               - legal: the notcies and license files
               - samples: all samples, i.e. "bsf4oorexx/information"
   Usage:   createPortableB4R op_sys bitness [machine]
            ... op_sys L(inux), M(ac) or D(arwin), W(indows)
            ... bitness: 32 or 64
            ... machine: optional, x (default: Intel), amd (Intel) arm, s390x
   Date:    2022-02-07
   Needs:   unzipped bsf4oorexx-zip-archive, i.e. "bsf4oorexx" directory as source
   Hints:   creates temporary directory "_portable_", takes BSF4ooRexx version from
            bsf4oorexx/BSF.CLS (needle: .bsf4rexx~version="641.20220131" -- set version (date distribution got created)),
            ignores directories 'clr', 'Templates', 'utilities', 'install' for portable version
   license:
    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2022 Rony G. Flatscher

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

.context~package~local~bDry  =.false -- .true      -- if .true then commands are shown only, otherwise they get executed
.context~package~local~bDebug=.false -- .true      -- if .true show debug output
.context~package~local~bUNO  =.true  -- .false     -- if .true keeps UNO support, i.e. AOO/LO support

call getBaseName   -- creates base name, includes version number from BSF.CLS

parse arg op_sys bitness machine
if .bDebug then say .line": debug - op_sys:" pp(op_sys)  "bitness:" pp(bitness) "machine:" pp(machine)

parse source s +1    -- get current operating system: W[indows], L[inux], D[arwin]
if op_sys="" then op_sys=s    -- default to current operating system
             else op_sys=op_sys~strip~left(1)~strip~upper

if s<>"W" then call set_unix_attributes   -- on Unix make sure unzipped archive files get appropriate bits set

if pos(op_sys,"LMDW")=0 then
do
   say "arg1 'op_sys' must be one of 'L'(inux), 'M'(acOS)/'D'(arwin) or 'W'(indows), found:" pp(op_sys)
   exit -1
end
if op_sys='M' then op_sys='D' -- standardize on 'D'arwin

if bitness="" then bitness=.rexxinfo~architecture  -- default to current ooRexx bitness
if wordpos(bitness, "32 64")=0 then
do
   say "arg2 'bitness' must be one of '32' or '64', found:" pp(bitness)
   exit -2
end

machine=machine~strip~lower
if machine="" then
do
    if op_sys<>'D' then machine='x' -- default to Intel, on Apple use universal
end
else if wordpos(machine, "x amd arm s390x")=0 then
do
   say "arg3 'machine' is optional, must be one of 'x', 'amd', 'arm' or 's390x', found:" pp(machine)
   exit -3
end

select case op_sys   -- operating system this script executes
   when 'W' then  -- Windows
            do
               tgtLibName="BSF4ooRexx.dll"
               if bitness=32 then
               do
                  srcLibName=tgtLibName"-32-x86"
                  tgtOpsysName="Windows-x86"
               end
               else
               do
                  srcLibName=tgtLibName"-64-amd64"
                  tgtOpsysName="Windows-amd64"
               end
            end

   when 'D' then  -- Apple
            do
              tgtLibName="libBSF4ooRexx.dylib"
              select case machine
                 when "arm"      then
                     do
                        srcLibName=tgtLibName"-64-arm64"
                        tgtOpsysName="Darwin-arm64"
                     end
                 when "x", "amd" then
                     do
                        srcLibName=tgtLibName"-64-x86_64"
                        tgtOpsysName="Darwin-x86_64"
                     end
                 otherwise
                    srcLibName=tgtLibName"-universal"
                    tgtOpsysName="Darwin-universal"
              end
            end

   otherwise   -- Linux
   do
       tgtLibName="libBSF4ooRexx.so"
       select case bitness
           when 32 then
                  if machine="arm" then
                  do
                     srcLibName=tgtLibName"-32-arm32"
                     tgtOpsysName="Linux-arm32"
                  end
                  else
                  do
                     srcLibName=tgtLibName"-32-x86"
                     tgtOpsysName="Linux-x86"
                  end

           when 64 then
              select case machine
                 when 'arm'   then
                     do
                        srcLibName=tgtLibName"-64-arm64"
                        tgtOpsysName="Linux-arm64"
                     end

                 when 's390x' then
                     do
                        srcLibName=tgtLibName"-64-"machine
                        tgtOpsysName="Linux-s390x"
                     end

                 otherwise
                     srcLibName=tgtLibName"-64-amd64"
                     tgtOpsysName="Linux-amd64"
              end
       end
   end
end

/* --------------------------------------------------------------- */

fileSep=.file~separator          -- \ (Windows), / (Unix)
pathSep=.file~pathSeparator      -- ; (Windows), : (Unix)

   -- on which platform are we?
parse source s +1 . . fullpath   -- W[indows], L[inux], D[arwin]; path to this script
_location=filespec("location",fullpath)

-- commands: cmd., default to the Unix commands
cmd.=.nil

if s="W" then     -- Windows
do
   cmd.eIsWindows=.true
   cmd.eIsMacOS  =.false
   cmd.eIsUnix   =.false
end
else              -- Unix
do
   cmd.eIsWindows=.false
   cmd.eIsMacOS=(s="D" | s="M")    -- determine whether running on "MacOS" replaced by "Darwin"
   cmd.eIsUnix=.true
end

if op_sys="W" then
do
   cmd.eExt=".cmd"
   cmd.eUnixSource=""
end
else
do
   cmd.eExt=".sh"
   cmd.eUnixSource=". "
end


-- ignoring directories 'clr', 'Templates', 'utilities', 'install'
srcDir    ="bsf4oorexx"          -- root of freshly unzipped bsf4oorexx.zip archive
-- ignore! srcClrDir =srcDir || fileSep || 'clr'   -- on Windows copy to 'bin', ignore on Unix
srcInfo   =srcDir || fileSep || 'information'   -- copy to 'doc'
srcSamples=srcDir || fileSep || 'samples'
srcInstall=srcDir || fileSep || 'install' || fileSep
srcLib    =srcInstall || 'lib'

if \SysFileExists(srcDir) | \SysFileExists(srcInfo) | \SysFileExists(srcSamples) | \SysFileExists(srcInstall) | \SysFileExists(srcLib) then
do
   say "directory structure of" pp(srcDir) "is incorrect, aborting ..."
   exit -4
end


tgtTmpDirName="_portable_"

tgtDirName   =getBaseName(srcDir, tgtOpsysName)
tgtDir       =tgtTmpDirName || fileSep || tgtDirName || fileSep
tgtBin       =tgtDir || 'bin'
tgtDoc       =tgtDir || 'doc'
if op_sys='W' then tgtLib=tgtDir || 'bin'
              else tgtLib=tgtDir || 'lib'
tgtJars      =tgtDir || 'jars'  -- all jar-files go there
tgtLegal     =tgtDir || 'legal'
tgtSamples   =tgtDir || 'samples'

runtimeTgtDirs=(tgtDir, tgtBin, tgtLib, tgtJars, tgtLegal)
additionalTgtDirs=(tgtDoc, tgtSamples)


if .bDebug then
do
   say "srcDir            :" pp(srcDir            )
   say "srcInfo           :" pp(srcInfo           )
   say "srcLib            :" pp(srcLib            )
   say "srcSamples        :" pp(srcSamples        )
   say "tgtTmpDirName     :" pp(tgtTmpDirName     )
   say "tgtDirName        :" pp(tgtDirName        )
   say "tgtDir            :" pp(tgtDir            )
   say "tgtBin            :" pp(tgtBin            )
   say "tgtDoc            :" pp(tgtDoc            )
   say "tgtJars           :" pp(tgtJars           )
   say "tgtLib            :" pp(tgtLib            )
   say "tgtSamples        :" pp(tgtSamples        )
   say
end

if cmd.eIsWindows then
do
   cmd.eEcho="@echo"
   cmd.eCopyRecursive="xcopy /s"
   cmd.eCopy="copy"
   cmd.eDelete="del /f"
   cmd.eDeleteWithSub="rd /s /q"
   cmd.eMkDir="md"
   cmd.eMove ="move"
end
else  -- Unix
do
   cmd.eEcho="echo"
   cmd.eCopy="cp -pfv"
   cmd.eCopyRecursive="cp -prfv"    /* recursively, force, verbose: source target(dir); note: if target
                              dir does not exist
   [works on FAT32/NTFS systems from Linux as well] */

   cmd.eDelete="rm -fv"
   cmd.eDeleteWithSub="rm -rfv"
   cmd.eMkDir="mkdir -pvm 755"
   cmd.eMove ="mv"
end

if .bDebug then
do
   say "cmd.eIsWindows    :" pp(cmd.eIsWindows    )
   say "cmd.eIsUnix       :" pp(cmd.eIsUnix       )
   say "cmd.eIsMacOS     :" pp(cmd.eIsMacOS     )
   say "cmd.eEcho         :" pp(cmd.eEcho         )
   say "cmd.eCopy         :" pp(cmd.eCopy         )
   say "cmd.eDelete       :" pp(cmd.eDelete       )
   say "cmd.eDeleteWithSub:" pp(cmd.eDeleteWithSub)
   say "cmd.eMkDir        :" pp(cmd.eMkDir        )
   say
end


if sysFileExists(tgtTmpDirName) then
do
   say "target directory" pp(tgtTmpDirName) "exists already, removing it ..."
   cmd=cmd.eDeleteWithSub tgtTmpDirName
   if .bDry=.false then address system cmd
                   else say "command (dry):" pp(cmd)
end

say "creating target directories ..."
   -- create runtime target directories
do tmpDir over runtimeTgtDirs
   cmd=cmd.eMkDir tmpDir
   if .bDry=.false then address system cmd
                   else say "command (dry):" pp(cmd)
end

cmds=.array~new

specs=fileSep'*'
cmds~append(cmd.eCopy          srcDir     || specs tgtBin)

srcSpec=tgtBin || fileSep || '*LIC*'   -- case sensitive!
cmds~append(cmd.eCopy   srcSpec tgtLegal)
cmds~append(cmd.eDelete srcSpec)

srcSpec=tgtBin || fileSep || '*.txt'   -- for case sensitive systems
cmds~append(cmd.eCopy   srcSpec tgtLegal)
cmds~append(cmd.eDelete srcSpec)

srcSpec=tgtBin || fileSep || '*.jar'
cmds~append(cmd.eCopy   srcSpec tgtJars)
cmds~append(cmd.eDelete srcSpec)

srcSpec=tgtBin || fileSep || '*.pdf'      -- reference cards are already in doc
cmds~append(cmd.eDelete srcSpec)

srcSpec=tgtBin || fileSep || 'rexxj2-*'   -- we do not assume configurable JVM anymore
cmds~append(cmd.eDelete srcSpec)

if op_sys="W" then srcSpec = tgtBin || fileSep || '*.sh'   -- on Windows delete Unix scripts
              else srcSpec = tgtBin || fileSep || '*.cmd'  -- on Unix delete Windows scripts
cmds~append(cmd.eDelete srcSpec)

srcSpec=tgtBin || fileSep || 'addLinks2OOoSnippets.rxo'  -- remove this utility, not needed
cmds~append(cmd.eDelete srcSpec)

-- remove clr/.net related: (as of 2022-02-02: available only on Windows with Java 8 in full installation)
--      legal/jni4net-MIT-LICENSE.txt,
say "removing CLR/.NET support ..."
srcSpec=tgtLegal || fileSep || "jni4net-MIT-LICENSE.txt"
cmds~append(cmd.eDelete srcSpec)

-- remove UNO support from 'bin'
if .bUNO=.false then
do
   say "removing UNO (AOO/LO) support ..."
   srcSpec=tgtBin || fileSep || 'UNO*'       -- remove AOO/LO support
   cmds~append(cmd.eDelete srcSpec)
end
else  -- for getting proper UNO CLASSPATH setupbsf4oorexx.rex uses 'install/JavaInfo4BSF4RexxInstallation.class'
do
   srcSpec=srcInstall || 'JavaInfo4BSF4RexxInstallation.class'
   cmds~append(cmd.eCopy srcSpec tgtDir)                    -- copy Java class
   cmds~append(cmd.eCopy _location || "testuno.rex" tgtDir) -- copy UNO test script
end
cmds~append(cmd.eCopy _location || "setupbsf4oorexx.rex" tgtDir)  -- copy setup script
cmds~append(cmd.eCopy _location || "testbsf4oorexx.rex" tgtDir)   -- copy test script

   -- on Unix do a chmod 775 on these:
copiedRexxScripts=.array~of(tgtDir"testuno.rex", tgtDir"setupbsf4oorexx.rex", tgtDir"testbsf4oorexx.rex")

srcLibPath=srcLib || fileSep || srcLibName
tgtLibPath=tgtLib || fileSep || tgtLibName
cmds~append(cmd.eCopy srcLibPath tgtLibPath)

do cmd over cmds
   if .bDry=.false then address system cmd
                   else say "command (dry):" pp(cmd)
end

if .bDry<>.true, cmd.eIsUnix=.true then   -- make sure the rexx-scripts get set to 775
do f over copiedRexxScripts
   address system "chmod 775" f
end

-- create readme.txt file in tgtDir, on Unix set to 664
readme_txt=.resources~readme_main
if op_sys="D" then readme_txt~appendAll(.resources~readme_mac)

if .bUno=.true then readme_txt~appendAll(.resources~readme_uno)
readme_txt=readme_txt~makeString
readme_txt=readme_txt~changeStr("@REXXJ@",      "rexxj"cmd.eExt)
readme_txt=readme_txt~changeStr("@SETENV2B4R@", "setenv2bsf4oorexx"cmd.eExt)
readme_txt=readme_txt~changeStr("@UNIX_SOURCE@", cmd.eUnixSource)

readme_fn=tgtDir"readme.txt"
s=.stream~new(readme_fn)~~open("write replace") -- remove existing
s~charout(readme_txt)      -- write readme.txt
s~close
if .bIsUnix=.true then address system "chmod 664" readme_fn

unoHint=""
if .bUNO=.true then unoHint="-UNO"
call createZipArchive tgtTmpDirName, tgtDirName"-portable"unoHint"-runtime.zip", cmd.eMove
say
-- say 'hit enter to continue ...'; parse pull .

   -- now add doc and samples
say "creating target 'doc' and 'samples' directories ..."
   -- create 'doc' and 'samples' target directories
do tmpDir over additionalTgtDirs
   cmd=cmd.eMkDir tmpDir
   if .bDry=.false then address system cmd
                   else say "command (dry):" pp(cmd)
end

cmds=.array~new
cmds~append(cmd.eCopyRecursive srcInfo    || specs tgtDoc)
cmds~append(cmd.eCopyRecursive srcSamples || specs tgtSamples)

-- remove clr/.net related: (as of 2022-02-02: available only on Windows with Java 8 in full installation)
say "removing CLR/.NET support ..."
names=.array~of("clr-oorexxdoc.xml", "20150810_Raffel-Manuel-ooRexx-Net-architecture.pdf", -
                "20160723_Baginski-Adrian-ooRexx-Net-Cookbook.pdf")
do n over names
   cmds~append(cmd.eDelete tgtDoc || fileSep || n)
end
cmds~append(cmd.eDeleteWithSub tgtDoc || fileSep || oorexxdoc)

-- remove clr/.net and outdated: samples/clr/, samples/oorexx.net/, samples/deprecated/
   -- remove clr/.net support (needs to be fully installed, use normal package instead)
dirs=.array~of("clr", "oorexx.net", "deprecated")
do d over dirs
   cmds~append(cmd.eDeleteWithSub tgtSamples || fileSep || d)
end

if .bUNO=.false then    -- remove UNO (AOO/LO)support
do
   say "removing UNO (AOO/LO) support ..."
   --  doc/refcardOOo.pdf, samples/OOo
   cmds~append(cmd.eDelete        tgtDoc     || fileSep || "refcardOOo.pdf" )
   cmds~append(cmd.eDeleteWithSub tgtSamples || fileSep || "OOo" )
end

do cmd over cmds
   if .bDry=.false then address system cmd
                   else say "command (dry):" pp(cmd)
end

call createZipArchive tgtTmpDirName, tgtDirName"-portable"unoHint".zip", cmd.eMove



/** Creates the zip archive in the given working directory and moves it with the supplied
    move command.
   @param workDir working directory
   @param zipname the zip archive's name
   @param mvCommand the move command for moving the created zip archive
*/
::routine createZipArchive
  use strict arg workDir, zipname, mvCommand

  currDir=directory()         -- save current directory
  call directory workDir      -- change to working directory
  if .bDebug then say "... createZipArchive, current directory now:" pp(directory())

  cmd="zip -9r" zipname "*"   -- zip entire subtree
  if .bDry=.false then address system cmd
                  else say "command (dry):" pp(cmd)

  cmd=mvCommand zipname '..'
  if .bDry=.false then address system cmd
                  else say "command (dry):" pp(cmd)

  call directory currDir      -- go back
  if .bDebug then say "... createZipArchive, current directory now:" pp(directory())


/** Enquotes the string value in square brackets and returns it.
  @return argument enclosed in square brackets
*/
::routine pp
  return "["arg(1)"]"

/** Gets the BSF.CLS version (from its content), creates the base file name for the
    portable BSF4ooRexx zip archives and returns.
    @return base name for portable BSF4ooRexx zip archives
*/
   -- fetch BSF4ooRexx version from BSF.CLS, create directory name with it by extending received directory name
::routine getBaseName
  baseName="bsf4oorexx"
  use strict arg dirname=(projectName), tgtOpsysName=""

  fn=baseName || .file~separator || "BSF.CLS"
  s=.stream~new(fn)~~open("read shared")
  data=s~charin(1,s~chars) -- read all characters
  s~close
  parse var data '.bsf4rexx~version="' version '"'
  suffix=""
  if tgtOpsysName<>"" then suffix="-"tgtOpsysName
  return dirname"_v"version || suffix

/** Makes sure that the file and directory attributes are defined appropriately
    for Unix systems in the exploded bsf4oorexx-installation package directories.
*/
   -- make sure that directories are traversable, files readable, scripts executable
::routine set_unix_attributes
   use strict arg rootDir="bsf4oorexx"

   -- make sure exploded bsf4oorexx installation archive gets appropriate bits set
   cmds=.array~new
      /* make directories traversable, all files rw-rw-r */
   cmds~append("chmod 775" rootDir)                               /* 775 on "bsf4oorexx"-directory                      */
   cmds~append("find" rootDir "-type d -exec chmod 775 '{}' \;") /* 775 make all directories traversable; rwx-rwxr-x   */
   cmds~append("find" rootDir "-type f -exec chmod 664 '{}' \;") /* make all files rw-rw-r--                           */

   exeTypes=.array~of("sh", "so", "rex", "rexx", "rxj", "rxo", "rexxj", "jrexx", "cls", -
                      "frm", "orx", "testGroup", "testUnit", "exe", "class", "cmd",     -
                      "py", "js", "nrx", "jacl" )
   do ext over exeTypes -- set executable bits to scripts/programs
      cmds~append('find' rootDir '-iname "*.'ext'" -type f -exec chmod 775 ''{}'' \;')
   end

   say .line "(".context~name"): setting appropriate Unix bits"
   do c over cmds
      if .bDry then say c
               else address system c
   end
   return

/* ========================================================================== */
::resource readme_main
-----------------------------------------------------------------------------------------
"Portable BSF4ooRexx"
-----------------------------------------------------------------------------------------

Run the script

   rexx setupbsf4oorexx.rex

        ... will create the script "@REXXJ@" which can then be used to run
            any BSF4-ooRexx program

        ... will create the script "@SETENV2B4R@" which can then be
            used to set the shell's environment to make BSF4ooRexx available


=========================================================================================
Portable Zip Archive ("Stick") Version for BSF4ooRexx (ooRexx)
=========================================================================================

BSF4ooRexx ("bean scripting framework" for "open object Rexx") is an ooRexx-Java
bridge.  It camouflages Java as the message based, caseless, dynamically typed
ooRexx, making it easy for Rexx programmers to exploit any Java class library on
any operating system.

"BSF4ooRexx" is an open-source project governed by the non-profit special
interest group "Rexx Language Association" (RexxLA).  Like ooRexx it is an easy
to learn, dynamically typed, caseless and powerful ooRexx-Java bridge.
BSF4ooRexx implements the message paradigm for interacting with Java classes and
Java objects that makes it as easy to interact with Java as it is with ooRexx.

This version allows you to test and use BSF4ooRexx without a need to install it
and to carry along with additional BSF4ooRexx versions for different operating
systems e.g.  on a single (USB) "stick", hence "portable".

Links:

   RexxLA Homepage: http://www.rexxla.org
   BSF4ooRexx Project:  https://sourceforge.net/projects/bsf4oorexx/


-----------------------------------------------------------------------------------------
Create the Scripts (setupbsf4oorexx.rex)
-----------------------------------------------------------------------------------------

First run

       rexx setupbsf4oorexx.rex

from this directory (usually the portable archive's unzipped directory)
using ooRexx 5 or newer.

Running "setupbsf4oorexx.rex" from the archive's unzipped directory will
generate the scripts "@REXXJ@" (sets environment and runs BSF4-ooRexx
programs via Java) and "@SETENV2B4R@" (set process/shell environment
to allow BSF4ooRexx to be used).

If the location of the script's home directory has been moved simply rerun
"setupbsf4oorexx.rex" from the new location.


-----------------------------------------------------------------------------------------
Purpose of "@REXXJ@"
-----------------------------------------------------------------------------------------

   This script will set up a local environment to this directory's BSF4ooRexx
   directories in the current process and expects a Rexx program with arguments, e.g.

       @REXXJ@ testbsf4oorexx.rex

   Upon return from the script the shell's environment is unchanged.


-----------------------------------------------------------------------------------------
Purpose of "@SETENV2B4R@" (Set Environment to portable BSF4ooRexx Environment)
-----------------------------------------------------------------------------------------

   This script allows to set up the environment to this directory's Rexx interpreter
   in the current shell:

       @UNIX_SOURCE@@SETENV2B4R@

   Upon return from the process the shell's environment is changed such that this
   portable BSF4ooRexx gets used. One can then run BSF4-ooRexx programs via "rexx",
   e.g.

       rexx testbsf4oorexx.rex

::END


/* ========================================================================== */
::resource readme_mac
   Please note: on MacOS (Darwin) it is mandatory to use the @REXXJ@
                script in the case that the BSF4-ooRexx program uses Java GUI
                classes, e.g.

       @REXXJ@ testbsf4oorexx.rex

::END


/* ========================================================================== */
::resource readme_uno

-----------------------------------------------------------------------------------------
Support for UNO (Universal Network Objects) if AOO or LO are present
-----------------------------------------------------------------------------------------

   If either AOO (Apache OpenOffice) or LO (LibreOffice) are found, then the
   appropriate UNO CLASSPATH definitions get added, such that ooRexx scripts/programs
   can interact via the Java UNO API (application program interface) if AOO/LO and
   ooRexx have the same bitness. To test you could issue e.g.

       @REXXJ@ testuno.rex

::END
