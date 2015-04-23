#!/usr/bin/rexx
/*
   Name:    install_ooRexx_dbus.rex
   Purpose: copy the package data to /opt/home (Unix) or the apppropriate Windows program directory,
            link the binaries and dynamic libraries to /usr/bin and /usr/lib (Unix) or %REXX_HOME%
   Author:  Rony G. Flatscher (c) 2011
   Date:    2015-04-22
   Version: 102.20150422
   Changed: - 2015-04-22, ---rgf: all new, replaces old installation program
            - 2015-04-23, ---rgf: - on Windows: if PATH is changed in registry, then broadcast setting
                                    change to all toplevel windows
                                  - intercept show and pause if a condition occurred
                                  - Windows: now install into %REXX_HOME% (no need to manipulate registry)
   Remark:  one needs to run this script as super user (sudo on Linux) or in an Administrator
            shell (Windows);
            alternatively, use the dbusoorexx/install/{linux|macosx}/{*install.sh} resp.
            dbusoorexx\install\windows\{*install.cmd} scripts; these scripts will try to switch
            to super user mode for installing, reinstalling and uninstalling purposes

   Usage:   rexx install_ooRexx_dbus.rex -i | -r | -u
            switch:
                  -i ... install
                  -r ... reinstall (overwrite installation)
                  -u ... uninstall

   license:

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
cmd.=""     -- define stem variable, set default value to empty string

-- for debugging only (sort of "dry-run"):
cmd.bDebug=.true              -- debug mode (shows commands as well)
cmd.bRunCommands=.true -- .false   -- do not execute commands

--------------------------------------------

signal on any name syntax

      -- get source information about this script
parse source opsys . fpath
thisName=fileSpec("name",fpath)

-- make sure current directory is set to this directory in order to allow relative paths
oldDir=directory(fileSpec("location",fpath))

opsys1=opsys~left(1)~upper    -- get first letter in uppercase
if pos(opsys1, "LMW")=0 then  -- no target operating system that we serve?
do
   say thisName": installation supported for Linux, MacOSX, Windows, however found: ["opsys"], aborting."
   exit -1
end

   -- check switch: I[nstall], U[ninstall]
parse upper arg "-" +1 switch +1 .
if pos(switch,"IRU")=0 then
do
   say thisName": wrong switch ["switch"] found, use: '-i' for install, '-r' for replace or '-u' for uninstall, aborting."
   exit -2
end

   -- check minimum Rexx version
parse version '-' rexx '_' 1 '_' major '.' minor '.' ')_' bitness '-bit'
-- say rexx major'.'minor bitness

if major'.'minor < 4.2 then
do
   say thisName": need at least ooRexx 4.2, currently running:" major'.'minor"; aborting."
   exit -3
end

-- for debugging only (sort of "dry-run"):
/*
   bitness=32
   bitness=64
   opsys1="L"     -- Linux
   opsys1="M"     -- MacOSX
*/

cmd.eBitness   = bitness
cmd.eSwitch    = switch
cmd.eOpsys     = opsys
cmd.eOpSys1    = opsys1    -- first letter of operating system in uppercase
cmd.eThisName  = thisName
cmd.eTargetDirName="dbusoorexx"

   -- the following files will be linked to /usr/bin (Unix) or copied to REXX_HOME (Windows)
cmd.eFilesToLink=.array~of("dbus.cls", "dbusdoc.rex", "dbusdoc.css", "dbusListObjectPaths.rex")

if cmd.bDebug=.true then
do
   .error~say("cmd.eBitness      =" pp(cmd.eBitness)      )
   .error~say("cmd.eSwitch       =" pp(cmd.eSwitch)       )
   .error~say("cmd.eOpsys        =" pp(cmd.eOpsys)        )
   .error~say("cmd.eOpSys1       =" pp(cmd.eOpSys1)       )
   .error~say("cmd.eThisName     =" pp(cmd.eThisName)     )
   .error~say("current directory =" pp(directory())       )
   .error~say("cmd.eTargetDirName=" pp(cmd.eTargetDirName))
end

   -- define targets
cmd.eSourceBinDir=qualify("..")
cmd.eTargetDirPath=determineHomeOnTarget(cmd.)  -- get and save target dir (without trailing [back-]slash)

if opsys1="W" then
do
   -- if .false: *rex, *cls, *dll gets copied to/removed from REXX_HOME which should be on PATH, no registry manipulation needed
   -- if .true:  PATH gets set to/unset from cmd.eTargetDirPath (interacting with registry)
   cmd.bAddPathToRegistry=.false -- .true

   cmd.eSep   ="\"
   cmd.eDelete="del"
   cmd.eRmDir ="rd /s /q"
   cmd.eXCopy ="xcopy /s" enquote(cmd.eSourceBinDir) enquote(cmd.eTargetDirPath)
   cmd.eCopy  ="copy /y"
   cmd.eLink  =cmd.eCopy

   cmd.eDll   ="dbusoorexx.dll"
   cmd.eDll.32="install\libs\dbusoorexx-32.dll"
   cmd.eDll.64="install\libs\dbusoorexx-64.dll"


   cmd.eLinkDir=value("REXX_HOME",,"environment")  -- home directory of ooRexx to which PATH always points
   if cmd.eLinkDir="" then cmd.eLinkDir=cmd.eTargetDirPath~string -- no rexx_home, default to new directory
   cmd.eLinkDir.Dll=cmd.eLinkDir    -- on Windows PATH will find dlls and binaries
   cmd.eLinkDir.Bin=cmd.eLinkDir    -- on Windows PATH will find dlls and binaries
end
else  -- Unix: Linux or MacOSX
do
   cmd.ePrefix= "sudo"       -- use sudo for un/re/installing
   cmd.eSep   ="/"
   cmd.eDelete="rm -fv"
   cmd.eRmDir ="rm -fvr"

   -- cmd.eMkDir755 = "mkdir -v -m u=rwx,go=rx"    /* 755, create directories if needed   */
   cmd.eChmod755= "chmod -v 755"

   cmd.eLink="ln -fsv"     /* force, verbose: source target(dir); (source link-to-target) */

   cmd.eTargetDirPath="/opt/"cmd.eTargetDirName

   cmd.eCopy="cp -pfv"    /* preserve, force, verbose */

   if opsys1="M" then       -- MacOSX
   do
       -- on MacOSX make sure that target includes the target directory name ("dbusoorexx")
      cmd.eXCopy="cp -pfvR" enquote(cmd.eSourceBinDir||cmd.eSep)  enquote(cmd.eTargetDirPath)  /* force, verbose, recurse; "R" */
      cmd.eDll="libdbusoorexx.dylib"
      cmd.eDll.32="install/libs/libdbusoorexx.dylib"
      cmd.eDll.64="install/libs/libdbusoorexx.dylib"
      cmd.eLS="ls -alT"
      cmd.eLS.ignoreCols=9    -- then "fully_qualified_path_canBeSymbolicLink [-> path-2-original]
      cmd.eLinkDir.Dll="/usr/lib"
      cmd.eLinkDir.Bin="/usr/bin"
   end
   else -- Linux
   do
       -- on Ubuntu make sure that target does NOT include the target directory name ("dbusoorexx")
     cmd.eXCopy="cp -pfvR" enquote(cmd.eSourceBinDir||cmd.eSep)  enquote("/opt/")  /* force, verbose, recurse; "R" */

--   TODO: Assuming that even on 64 bit Linux a 32 bit .so gets installed into /usr/lib.
--         If not true for all Linux distribution, one could look where ooRexx installs
--         oorexxtry.rex and liboorexx.so and use those directories
      -- determine LinkDirs
      cmd.eDll="libdbusoorexx.so"
      cmd.eDll.32="install/libs/libdbusoorexx32.so"
      cmd.eDll.64="install/libs/libdbusoorexx64.so"
      cmd.eLS="ls --full-time -al"
         -- on Ubuntu same for 32- and 64-bit ! what about RH and the like?
      cmd.eLinkDir.Dll="/usr/lib"
      cmd.eLinkDir.Bin="/usr/bin"
   end
end

fileTargetDir=.File~new(cmd.eTargetDirPath)     -- create file object
if cmd.bDebug=.true then
   .error~say("fileTargetDir:" pp(fileTargetDir) "exists:" pp(fileTargetDir~exists))

oldDir=directory(cmd.eSep)    -- switch current directory to root directory (all commands should be path independent)

if cmd.bDebug=.true then
   .error~say("New current directory:" pp(directory())       )

------------------------------ install/replace -----------------------------
if pos(cmd.eSwitch,"IR")>0 then    -- install or replace
do
   if cmd.bDebug=.true then .error~say("in INSTALL/REPLACE branch, cmd.eSwitch=["cmd.eSwitch"]")
   if cmd.eSwitch="I", fileTargetDir~exists=.true then     -- cannot install, target dir exists already
   do
      .error~say(thisName": cannot install, target directory" pp(fileTargetDir) "exists already")
      .error~say(thisName": uninstall first or use replace instead, aborting")

      if cmd.bRunCommands=.true then
         exit -10
   end
   else if cmd.eSwitch="R", fileTargetDir~exists=.false then   -- cannot replace, before install
   do
      .error~say(thisName": cannot reinstall, target directory" pp(fileTargetDir) "does not exist")
      .error~say(thisName": install (no need to reinstall), aborting")

      if cmd.bRunCommands=.true then
         exit -11
   end

   if cmd.bRunCommands=.true, fileTargetDir~makeDirs=.false then
   do
      .error~say(thisName": could not create target directory" pp(fileTargetDir)", maybe missing rights, aborting...")
      .error~say(thisName": if missing rights, re-run this script as Administrator/sudo user, aborting")
      exit -15
   end

   cmd=(cmd.ePrefix cmd.eXCopy)~strip     -- copy recursively
   if cmd.bDebug=.true then .error~say("cmd:" pp(cmd))
   if cmd.bRunCommands=.true then cmd         -- run command?

   if cmd.eOpSys1<>"W" then   -- Unix: set Unix rights
       call setUnixRights cmd.

   -- now create appropriate DLL/so link
   if cmd.bAddPathToRegistry=.true then   -- if we are on Windows, this might be set to true
   do
      -- copy appropriate DLL into cmd.eTargetDirPath
      srcDll=cmd.eDll.bitness
      sourceFname=enquote(cmd.eTargetDirPath||cmd.eSep||srcDLL)   -- source-DLL/so-name
      targetFname=enquote(cmd.eTargetDirPath||cmd.eSep||cmd.eDll) -- target-DLL/so-name
      cmd=(cmd.ePrefix cmd.eLink sourceFname targetFname)~strip

      if cmd.bDebug=.true then .error~say("cmd:" pp(cmd))
      if cmd.bRunCommands=.true then cmd         -- run command?

      if cmd.bDebug=.true then .error~say("Windows: about to add the 'dbusoorexx' target directory to Windows registry's PATH")

      call windowsAdd2Registry cmd.
      call winSystm.cls       -- get access to .WindowManagerClass
      .WindowsManager~new~broadcastSettingChanged  -- inform top-level windows to reread settings
   end
   else
   do
      -- create links into appropriate directories
      do fname over cmd.eFilesToLink
         sourceFname=enquote(cmd.eTargetDirPath||cmd.eSep||fname) -- source-file-name
         targetFname=enquote(cmd.eLinkDir.Bin  ||cmd.eSep||fname) -- target-file-name
         cmd=(cmd.ePrefix cmd.eLink sourceFname targetFname)~strip

         if cmd.bDebug=.true then .error~say("cmd:" pp(cmd))
         if cmd.bRunCommands=.true then cmd         -- run command?
      end

   -- now create appropriate DLL/so link
      srcDll=cmd.eDll.bitness
      sourceFname=enquote(cmd.eTargetDirPath||cmd.eSep||srcDLL) -- source-DLL/so-name
      targetFname=enquote(cmd.eLinkDir.Dll ||cmd.eSep||cmd.eDll)      -- target-DLL/so-name
      cmd=(cmd.ePrefix cmd.eLink sourceFname targetFname)~strip

      if cmd.bDebug=.true then .error~say("cmd:" pp(cmd))
      if cmd.bRunCommands=.true then cmd         -- run command?
   end



end

else  ------------------------------ uninstall -----------------------------
do
   if cmd.bDebug=.true then .error~say("in UNINSTALL branch, cmd.eSwitch=["cmd.eSwitch"]")
   if fileTargetDir~exists=.false then    -- cannot uninstall, target dir does not exist
   do
      say thisName": cannot uninstall target directory" pp(fileTargetDir) "does not exist, aborting..."
      if cmd.bRunCommands=.true then
         exit -50
   end

   -- remove from Windows PATH
   if cmd.bAddPathToRegistry=.true then   -- if we are on Windows, this might be set to true
   do
      if cmd.bDebug=.true then .error~say("Windows: about to remove the 'dbusoorexx' target directory from Windows registry's PATH")
      call windowsRemoveFromRegistry cmd.
      call winSystm.cls       -- get access to .WindowManagerClass
      .WindowsManager~new~broadcastSettingChanged  -- inform top-level windows to reread settings
   end
   else
   do
      -- remove linked executables
      do fname over cmd.eFilesToLink
         targetFname=enquote(cmd.eLinkDir.Bin||cmd.eSep||fname) -- target-file-name
         cmd=(cmd.ePrefix cmd.eDelete targetFname)~strip

         if cmd.bDebug=.true then .error~say("cmd:" pp(cmd))
         if cmd.bRunCommands=.true then cmd         -- run command?
      end

      -- now delete appropriate DLL/so link
      targetFname=enquote(cmd.eLinkDir.Dll||cmd.eSep||cmd.eDll)      -- target-DLL/so-name
      cmd=(cmd.ePrefix cmd.eDelete targetFname)~strip

      if cmd.bDebug=.true then .error~say("cmd:" pp(cmd))
      if cmd.bRunCommands=.true then cmd         -- run command?
   end

      -- let the removal be the last action as on WinXP the start of uninstall.cmd will inhibit
      -- the successful removal, which may cause the program to stop
   cmd=(cmd.ePrefix cmd.eRmDir enquote(fileTargetDir))~strip  -- delete directory recursively
   if cmd.bDebug=.true then .error~say("cmd:" pp(cmd))
   if cmd.bRunCommands=.true then cmd         -- run command?

   if fileTargetDir~exists=.true then
   do
      say thisName": could not delete target directory" pp(fileTargetDir)", aborting..."
      exit -51
   end
end

if cmd.bDebug=.true then
do
   say "Hit enter to end ..."
   parse pull .
end

exit

syntax:
   co=condition('o')
   say "install_dbusoorexx.rex: condition occurred:"
   do idx over co~allindexes~sort
      say "   "idx":" pp(co[idx])
   end
   say
   say "Hit enter to end (in syntax condition handler) ..."
   parse pull .



::routine pp
  return "["arg(1)"]"


/* As development mainly occurs on Windows, make sure that the Unix rights are appropriately set */
::routine setUnixRights
   use arg cmd.

   cmd=(cmd.ePrefix cmd.eChmod755 cmd.eTargetDirPath)~strip
   if cmd.bDebug=.true then .error~say("setUnixRights() - cmd:" pp(cmd))
   if cmd.bRunCommands=.true then cmd         -- run command?

   do strWork over .array~of("-type d -exec chmod 775 '{}' \;", -  -- set directories to: rwx-rwxr-x
                             "-type f -exec chmod 664 '{}' \;")    -- make all files: rw-rw-r--
      cmd=(cmd.ePrefix "find" cmd.eTargetDirPath strWork)~strip
      if cmd.bDebug=.true then .error~say("setUnixRights() - cmd:" pp(cmd))
      if cmd.bRunCommands=.true then cmd         -- run command?
   end

   -- set executable bit:
   chunk1="find" cmd.eTargetDirPath -- command and directory to start out
   chunk2="-type f -execdir chmod ugo+x '{}' \;"   /* make sure executable gets set for all */
   tmpStr=""
   do ext over .array~of("sh", "rex", "cls", "so", "dylib",  "dll")
      if tmpStr="" then tmpStr =           '-iname "*'ext'"'
                   else tmpStr = tmpStr '-o -iname "*'ext'"'
   end
   cmd = (cmd.ePrefix chunk1 "\(" tmpStr "\)" chunk2)~strip   -- build command
   if cmd.bDebug=.true then .error~say("setUnixRights() - cmd:" pp(cmd))
   if cmd.bRunCommands=.true then cmd         -- run command?
   return


/** Determine home directory to copy installation package to.
* @return fully qualified path to new home without trailing [back-]slash
*/
::routine determineHomeOnTarget
   use arg cmd.

   if cmd.eOpSys1='W' then
   do
      homedir=value("programfiles",,"environment") -- ok for 32-on-32 or 64-on-64 (ooRexx-on-Windows)
      if cmd.eBitness=32 then    -- 32-bit ooRexx: on 32-bit or 64-bit Windows ?
      do
         tmp=value("ProgramFiles(x86)",,"environment")   -- 32-bit ooRexx on 64-bit Windows?
         if tmp<>"" then
            homedir=tmp
      end
      homedir=homedir"\"cmd.eTargetDirName   -- build fully qualified css-file-name
   end
   else  -- assuming a Unix operating system
   do
      homedir="/opt/"cmd.eTargetDirName
   end
   cmd.eHomeDir=homedir       -- set entry in stem
if cmd.bDebug=.true then .error~say("determineHomeOnTarget() :" pp(homedir))
   return homedir


/** Windows registry support. */
/** Removes path(s) in "needle" from the given "key"'s value.

   From the BSF4ooRexx installation script (orx2reg.rex)
*/
::routine windowsRemoveFromRegistry        -- allow needle to consist of multiple paths
   use arg cmd.
  -- use arg wsh, key, needle

   wsh=.OleObject~new("WScript.Shell")    -- Windows Script shell object
   key="HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment\Path"
   needle=cmd.eTargetDirPath     -- let PATH point to this

   pathDeli=";"                    -- path delimiter on Windows
   newVal=""                        -- new value

   val=getVal(wsh, key)      /* get current value */
if cmd.bDebug=.true then
do
   .error~say("windowsRemoveFromRegistry():    getVal="pp(val))
   .error~say("windowsRemoveFromRegistry():    needle="pp(needle))
end

   if val=.nil then                 -- key does not exist, nothing to do
      return

     -- re-create the current value, remove duplicates
   valSet=.set~new                  -- set of values processed already
     -- remember needles that need to be removed
   needles=needle~makeArray(pathDeli)
   do needle over needles
      uNeedle=needle~upper~strip    -- turn needle into uppercase
      valSet~put(uNeedle)           -- add to set
   end

   vals=val~makearray(pathDeli)
   do v over vals                   -- create new value, remove duplicates
      uv=v~strip~upper
      if valSet~hasIndex(uv) then
      do
if cmd.bDebug=.true then .error~say("windowsRemoveFromRegistry():    chunk ignored (needle|dupe)="pp(v) pp(uv))
         iterate -- duplicate or needle, skip
      end
      valSet~put(uv)                -- add to set
      if newVal="" then newVal=v
                   else newVal=newVal||pathDeli||v
   end

if cmd.bDebug=.true then
do
   .error~say("windowsRemoveFromRegistry():      val="pp(val))
   .error~say("windowsRemoveFromRegistry():   newVal="pp(newVal))
end

   if newVal<>val then              -- path changed, update key
   do
if cmd.bDebug=.true then
do
   .error~say("windowsRemoveFromRegistry():   cmd.bRunCommands="pp(cmd.bRunCommands))
end

      if newVal="" then             -- remove key
         NOP -- never delete PATH! wsh~regDelete(key)
      else
      do
         if cmd.bRunCommands=.true then
            wsh~regWrite(key, newVal, "REG_EXPAND_SZ")
      end
   end

if cmd.bDebug then .error~say("windowsRemoveFromRegistry():    getVal="pp(getVal(wsh,key)))
   return




/** Adds path(s) in "needle" to the given "key"'s value. In the process possible
   duplicate paths are removed. The sequence of paths is kept, the new paths are
   simply appended.

   From the BSF4ooRexx installation script (orx2reg.rex)
*/
::routine windowsAdd2Registry        -- allow needle to consist of multiple paths
  use arg cmd.
  -- use arg wsh, key, needle
                                                                       wsh=.OleObject~new("WScript.Shell")    -- Windows Script shell object
  wsh=.OleObject~new("WScript.Shell")    -- Windows Script shell object
  key="HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment\Path"
  needle=cmd.eTargetDirPath     -- let PATH point to this

  pathDeli=";"                  -- path delimiter on Windows

  bAddNeedle=.true                      -- do we have to add the needle ?
  newVal=""                             -- new value

  val=getVal(wsh, key)      /* get current value */

  if cmd.bDebug=.true then .error~say("windowsAdd2Registry():    geVal="pp(val))

  if val=.nil then                      -- no key present as of yet
  do
     newVal=needle
     bAddNeedle=.true                   -- indicate we need to add the needle
  end
  else
  do
     bAddNeedle=.false                  -- do we have to add the needle ?
       -- re-create the current value, remove duplicates
     valSet=.set~new                    -- set of values processed already
     vals  =val~makearray(pathDeli)
     do v over vals                     -- create new value, remove duplicates
        uv=v~strip~upper
        if valSet~hasIndex(uv) then iterate   -- duplicate, skip
        valSet~put(uv)                  -- add to set
        if newVal="" then newVal=v
                     else newVal=newVal||pathDeli||v
     end

       -- deal with needles
     needles=needle~makeArray(pathDeli)     -- maybe more than one path?

     do needle over needles
        uNeedle=needle~upper~strip      -- turn needle into uppercase
        if valSet~hasIndex(uNeedle) then iterate   -- already in new value, no dupes
        valSet~put(uNeedle)             -- remember

        bAddNeedle=.true                -- indicate we need to add the needle
        if newVal="" then newVal=needle -- add needle to value
                     else newVal=newVal||pathDeli||needle
     end
  end

if cmd.bDebug=.true then .error~say("windowsAdd2Registry(): bAddNeedle="pp(bAddNeedle)", cmd.bRunCommands="pp(cmd.bRunCommands))
  if bAddNeedle=.true, cmd.bRunCommands=.true  then   -- we need to write the new value
  do
if cmd.bDebug=.true then .error~say("windowsAdd2Registry(): newValue="pp(newVal))

     wsh~regWrite(key, newVal, "REG_EXPAND_SZ")

if cmd.bDebug=.true then .error~say("windowsAdd2Registry():   getVal="pp(getVal(wsh,key)))
  end


::routine getVal           -- gets the value of the passed in key from the registry
  use arg wsh, key

  signal on syntax
  return wsh~regRead(key)

syntax: return .nil

   -- enquote path/file names in case they contain blanks
::routine enquote
  parse arg val
  return '"' || val~changeStr('"','""') || '"'

