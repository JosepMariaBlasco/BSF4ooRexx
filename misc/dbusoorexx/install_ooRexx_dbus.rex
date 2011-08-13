#!/usr/bin/rexx
/*
   Name:    install_ooRexx_dbus.rex
   Purpose: link the necessary files to their target locations, such that liboorexx.dbus is operational
   Author:  Rony G. Flatscher (c) 2011
   Date:    2011-07-19
   Version: 1.0
   Changed: --

   Usage:   rexx install_ooRexx_dbus.rex [-u]
            ... no switch   -> install
            ... swithc "-u" -> uninstall

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2011 Rony G. Flatscher

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
parse source opsys . fpath
thisName=fileSpec("name",fpath)

if opsys<>"LINUX" then
do
   say thisName": operating system must be LINUX, found ["opsys"], aborting ..."
   exit -1
end

parse arg "-" switch .
if switch<>"", switch<>"u" then
do
   say thisName": wrong switch ["switch"] found, either no switch (for install) or '-u' for uninstall, aborting ..."
   exit -2
end

cmd.=""                          /* default value for stem        */
cmd.eBitness=getRexxBitness()        -- can be "0" if not determinable, else "32" or "64"
call unixDetermineTargetLibDirs cmd. -- find target libs to link to

-- bDebug=.true                     /* if bDebug is set to .true, then debug-mode   */

if bDebug=.true then
do
   say "***debug: cmd.eBitness ="cmd.eBitness
   say "          cmd.eTgtLib32="cmd.eTgtLib32
   say "          cmd.eTgtLib64="cmd.eTgtLib64
end

cmd.eDelete="rm -fv"
cmd.eCopy="cp -pfv"            /* force, verbose: source target(dir) [works on FAT32/NTFS systems from Linux as well] */
cmd.eLink="ln -fsv"            /* force, verbose: source target(dir); (source link-to-target) */

   -- defining dirs to work for ldconfig
cmd.eLdConfig="ldconfig -n /usr/lib"
   -- adding other "/usr/lib" directories as well ?
lib="/usr/lib32"
if sysFileExists(lib) then cmd.eLdConfig=cmd.eLdConfig lib
lib="/usr/lib64"
if sysFileExists(lib) then cmd.eLdConfig=cmd.eLdConfig lib

   -- now build the commands and execute them
width=34
         -- link Rexx package
if switch="" then    -- install
do
   fn="dbus.cls"
   tgt="/usr/bin"
   cmd=cmd.eCopy fn tgt
   say "copying DBUS Rexx package:"~left(width) cmd
end
else                 -- remove
do
   cmd=cmd.eDelete "/usr/bin/dbus.cls"
   say "removing DBUS Rexx package:"~left(width) cmd
end
if bDebug<>.true then
   cmd   -- carry out the command via the shell

         -- link Rexx shared object (DLL)
tgtLibName="libdbusoorexx.so"
if switch="" then    --install
do
   if cmd.eBitness=32 then
      cmd=cmd.eCopy "libdbusoorexx32.so" cmd.eTgtLib32"/"tgtLibName
   else if cmd.eBitness=64 then
      cmd=cmd.eCopy "libdbusoorexx64.so" cmd.eTgtLib64"/"tgtLibName

   say "copying DBUS Rexx shared library:"~left(width) cmd
end
else
do
   if cmd.eBitness=32 then
      cmd=cmd.eDelete cmd.eTgtLib32"/"tgtLibName
   else if cmd.eBitness=64 then
      cmd=cmd.eDelete  cmd.eTgtLib64"/"tgtLibName

   say "removing DBUS Rexx shared library:"~left(width) cmd
end
if bDebug<>.true then
   cmd   -- carry out the command via the shell

cmd=cmd.eLdConfig
say "updating shared library cache:"~left(width) cmd
if bDebug<>.true then
   cmd   -- carry out the command via the shell

say "done."

   -- from BSF4ooRexx package:
::routine unixDetermineTargetLibDirs      -- determine which /usr/lib* to use for the different architectures and distributions
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

-- say "unixDetermineLibs:" _mode"|"lib"|"target "..."

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


-- return bitness of Rexx interpreter (e.g. 32, 64, etc.) - from BSF4ooRexx package
::routine getRexxBitness

  -- do not use ADDRESS CMD as on Linux this causes an error :(
   "rexx -v 2>&1 | rxqueue"
   bitness=0
   do while queued()>0
      parse pull hint ":" val .
      if hint="Addressing Mode" then bitness=val
   end
   return bitness


