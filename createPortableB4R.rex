#!/usr/bin/env rexx
/*
   Name:    createPortableB4R.rex

   Purpose: create a bare-bone "_portable_" version of BSF4ooRexx with the directories
               - bin: PATH needs to point to this, on Windows also the dll goes here
               - doc: content of "information" folder
               - jar: all Java jar/zip files that need to be placed on CLASSPATH (use '*' to include all)
               - lib:   the shared object/dynamic link library
               - legal: the notcies and license files
               - samples: all samples

            note: AOO/UNO support will be removed (use regular installation to get it installed)

   Usage:   createPortableB4R op_sys bitness [machine]
            ... op_sys L(inux), M(ac) or D(arwin), W(indows)
            ... bitness: 32 or 64
            ... machine: optional, x (default: Intel), amd (Intel) arm, s390x

   Date:    2022-02-01


   Needs:   unzipped bsf4oorexx-zip-archive, i.e. "bsf4oorexx" directory as source, tmpdir "_portable_"
            take BSF4ooRexx version from BSF.CLS, needle:
               .bsf4rexx~version="641.20220131" -- set version (date distribution got created)

   Hints:   ignores directories 'clr', 'Templates', 'utilities', 'install' for portable version

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

.context~package~local~bDry  =.false -- .true        -- if .true then commands are shown only, otherwise they get executed
.context~package~local~bDebug=.false -- .true      -- if .true show debug output

call getBaseName

parse arg op_sys bitness machine
say "debug - op_sys:" pp(op_sys)  "bitness:" pp(bitness) "machine:" pp(machine)
op_sys=op_sys~strip~left(1)~strip~upper

if pos(op_sys,"LMDW")=0 then
do
   say "arg1 'op_sys' must be one of 'L'(inux), 'M'(acOS)/'D'(arwin) or 'W'(indows), found:" pp(op_sys)
   exit -1
end
if op_sys='M' then op_sys='D' -- standardize on 'D'arwin

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
else if machine<>"", wordpos(machine, "x amd arm s390x")=0 then
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
parse source s +1    -- W[indows], L[inux], D[arwin]

-- commands: cmd., default to the Unix commands
cmd.=.nil

cmd.eIsWindows=(s="W")
cmd.eIsMacOSX=(s="D" | s="M")    -- determine whether running on "MACOSX" replaced by "Darwin"
cmd.eIsUnix   =\cmd.eIsWindows   /* if not Windows, it's a Unix dialect       */


-- ignoring directories 'clr', 'Templates', 'utilities', 'install'
srcDir    ="bsf4oorexx"          -- root of freshly unzipped bsf4oorexx.zip archive
-- ignore! srcClrDir =srcDir || fileSep || 'clr'   -- on Windows copy to 'bin', ignore on Unix
srcInfo   =srcDir || fileSep || 'information'   -- copy to 'doc'
srcSamples=srcDir || fileSep || 'samples'
srcLib    =srcDir || fileSep || 'install' || fileSep || 'lib'

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

-- TODO: rexxj.{sh|cmd} mit lokalem Bezug, i.e. "setupbsf4oorexx.{sh|cmd}" -> "rexxj.{sh|cmd}" --> tgtDir


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
   say "cmd.eIsMacOSX     :" pp(cmd.eIsMacOSX     )
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

srcSpec=tgtBin || fileSep || '*.txt'
cmds~append(cmd.eCopy   srcSpec tgtLegal)
cmds~append(cmd.eDelete srcSpec)

srcSpec=tgtBin || fileSep || '*.jar'
cmds~append(cmd.eCopy   srcSpec tgtJars)
cmds~append(cmd.eDelete srcSpec)

srcSpec=tgtBin || fileSep || '*.pdf'      -- reference cards are already in doc
cmds~append(cmd.eDelete srcSpec)

srcSpec=tgtBin || fileSep || 'rexxj2-*'   -- we do not assume configurable JVM anymore
cmds~append(cmd.eDelete srcSpec)

srcSpec=tgtBin || fileSep || 'addLinks2OOoSnippets.rxo'  -- remove AOO/LO support
cmds~append(cmd.eDelete srcSpec)

srcSpec=tgtBin || fileSep || 'UNO*'       -- remove AOO/LO support
cmds~append(cmd.eDelete srcSpec)

srcLibPath=srcLib || fileSep || srcLibName
tgtLibPath=tgtLib || fileSep || tgtLibName
cmds~append(cmd.eCopy srcLibPath tgtLibPath)

do cmd over cmds
   if .bDry=.false then address system cmd
                   else say "command (dry):" pp(cmd)
end

call createZipArchive tgtTmpDirName, tgtDirName"-portable-runtime.zip", cmd.eMove

   -- now add doc and samples

-- say 'hit enter to continue ...'; parse pull .

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

do cmd over cmds
   if .bDry=.false then address system cmd
                   else say "command (dry):" pp(cmd)
end
call createZipArchive tgtTmpDirName, tgtDirName"-portable.zip", cmd.eMove

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

::routine pp
  return "["arg(1)"]"

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


::routine quote   -- quote received string (e.g. blanks in dirs or file names)
  return '"' || arg(1) || '"'
