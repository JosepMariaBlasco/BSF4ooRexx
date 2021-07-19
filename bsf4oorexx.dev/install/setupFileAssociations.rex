#!/usr/bin/env rexx
/*
   Name:    setupFileAssociations.rex
   Purpose: creates files to set up and remove file associations
   Author:  Rony G. Flatscher
   Date:    2010-07-26/27/30/31, 2010-08-20
   Version: 1.02
   Changes:
            2010-07-26, ---rgf, plain
            2010-12-08, ---rgf, changing application/x-rexx* -> text/x-rexx*
            2011-03-21, ---rgf, add "rgf-bsf4oorexx_oorexxtry.desktop" to Linux
            2011-03-26, ---rgf, reorganize xdg-install in linux, remove all dashes from name and use "--novendor"
            2011-03-27, ---rgf, - Linux: now creating separate 'xdg_icons_menu_add-remove.sh'
            2011-05-28, ---rgf, - Unix: make sure group of new files is set to "admin" and "admin" users
                                        have full write access to these files as well
            2011-06-08, ---rgf, - Unix: do not change group to "admin", e.g. Fedora Core may not have it
            2017-01-22, ---rgf, - add file extension ".frm" as a Rexx type
            2018-12-21, ---rgf, - SysVersion() now returns the Windows version number with multiple dots, hence
                                  getting integer value using '.' in the parse template rather than via an
                                  arithmetic operation
            2021-07-13, ---rgf, - Unix
                                  - add "cmd.eSudoUser", "cmd.eChown", cmd.eSetExecutable
                                  - make real user the owner of the created scripts
                                  - on Unix add "cmd.eChownUserGroup", new routine "determineGroupName(defGroup)"
            2021-07-14, rgf   - on Unix: new routine "determineHomeDir()", new "cmd.eHomeDir"
            2021-07-17, rgf   - little cleanup of code

   last svn check-in: $Revision: 640 $ $Author: rony $ $Date: 2010-04-17 22:25:38 +0200 (Sat, 17 Apr 2010) $

   Usage:   rexx setupFileAssocitions.rex

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2010-2021 Rony G. Flatscher

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



dir=.directory~new   -- will contain setup relevant information
dir~runtime=.dateTime~new~string
dir~rexx_home = value("REXX_HOME", , "ENVIRONMENT")   -- fetch value of environment variable "REXX_HOME"

parse source opsys +1                  -- determine operating system
parse upper arg switch val             -- determine command line switch (DEBUG {W|})
switch=switch~left(1)

if switch="D" then      -- "debug" argument given as first word ?
do
   dir~debug=.true                     -- remember debug mode is active
   dir~operating.system=opsys          -- remember opsys we are running under
   if val~left(1)="W" then opsys="W"   -- run Windows file association part
                      else opsys="U"   -- run Linux/Unix file association part
   dir~debug.operating.system=opsys    -- remember target opsys to generate for
end


parse version "REXX-" rexxProcessor "_" version "("
dir~rexx.processor = rexxProcessor
dir~rexx.version   = version

if opsys='W' then    -- Windows
do
   dir~opsys="W"              -- operating system: Windows
   file.sep="\"               -- file separator
   dir~rexx_bin = "rexx.exe"                          -- name of executable
   dir~path.sep       = ";"   -- path separator
   parse value SysVersion() with opSysName opSysVersion "." -- extract integer part of Windows' version number
   dir~win.version=opSysVersion     -- 5=WinXP, 6=Vista, 7=Win7 ...
end
else                 -- Unix based
do
   dir~opsys="U"              -- operating system: Unix-like
   file.sep="/"               -- file separator
   dir~rexx_bin = "rexx"                              -- name of executable
   dir~path.sep       = ":"   -- path separator
end
dir~file.sep       = file.sep    -- save file separator as well

dir~full_path_to_rexx=dir~rexx_home || file.sep || dir~rexx_bin -- fully qualified path

   /* determine location of "images" */
dir~path_to_images="images"
if directory()~caselessPos("BSF4ooRexx.dev")>0 then -- o.k. running in the development environment
   dir~path_to_images=".."file.sep"images"

   /* define (base) names of image files */
dir~rxj.baseName="bsf4oorexx"
dir~rex.baseName="oorexx"
dir~rxo.baseName="oorexx4ooo"

   /* template files (containing template code, if new file gets created via Explorer) */
dir~rex.template ="RexxScript (Rexx script) executed by ooRexx.rex"
dir~rxj.template ="RexxJavaScript (Rexx script using Java) executed by ooRexx.rxj"
dir~rxo.template ="RexxOOoScript (Rexx script using OpenOffice) executed by ooRexx.rxo"

   /* define available pixel sizes of the PNG versions (for Unix) */
dir~png.imageSizes=.array~of(16,32,48,64,128,256)


dir ~ rex.Name = "RexxScript"       -- already in use on Windows for ooRexx ! (do not change!)
dir ~ rxj.Name = "RexxJavaScript"
dir ~ rxo.Name = "RexxOOoScript"

dir ~ rex.mimetype = "text/x-rexx"
dir ~ rxj.mimetype = "text/x-rexx-java"
dir ~ rxo.mimetype = "text/x-rexx-java-ooo"

   -- on Windows ".rex" is already set up
dir ~ rex.extensions = .array~of( ".cls",   ".orx",  ".frm", ".rex", ".rexx", ".testGroup", ".testUnit" )

if opsys<>'W' then -- add '.rex' filetype for Unix
   dir ~ rex.extensions ~ append(".rex")

dir ~ rxj.extensions = .array~of( ".jrexx", ".rexxj", ".rxj" )
dir ~ rxo.extensions = .array~of( ".rxo" )

info = "executed by" dir~rexx.processor -- "version" dir~rexx.version
dir ~rex.comment = dir~rex.name "(Rexx script)" info
dir ~rxj.comment = dir~rxj.name "(Rexx script using Java)" info
dir ~rxo.comment = dir~rxo.name "(Rexx script using OpenOffice)" info

if switch="D" then      /* debug switch ? */
do
   call showDir dir, file.sep -- show current content of "dir"
end

if opsys='W' then call create4windows dir
             else call create4linux_xdg dir



/* ================================================================================= */
/* -------------------- Windows part -------------------- */
/*
   Maybe useful URLs (as of 2010-07-27):

   - Regedit Command Line Options Syntax
      <http://techsupt.winbatch.com/ts/T000001029F18.html>
   - Regedit command-line switches
      <http://www.eolsoft.com/freeware/registry_jumper/regedit_switches.htm>
   - WindowsScript Host ("WScript.Shell")
      <http://msdn.microsoft.com/en-us/library/98591fh7%28v=VS.85%29.aspx>
   - Windows® XP File Association Fixes
      <http://www.dougknox.com/xp/file_assoc.htm>
   - Customizing the Windows XP New Menu
      <http://windows-xp-dox.net/MS.Press-Insider.Power.Techniq/6843final/LiB0043.html>
   - How to add, modify, or delete registry subkeys and values by using a registration entries (.reg) file
      <http://support.microsoft.com/kb/310516>

*/
::routine create4windows
  use arg dir

  wsh  = .OleObject~new("WScript.Shell")
  hkcr = "HKEY_CLASSES_ROOT"
  file.sep = dir~file.sep
  fragmentName = "fileAssociations_for_"

  call updateRexxAssociation dir, hkcr, wsh, fragmentName
  call createNewAssociations dir, hkcr, wsh, fragmentName


  exit

   ----- change the .REX definition ------------------------------------------------
updateRexxAssociation: procedure
  use arg dir, hkcr, wsh, fragmentName

  regFile="rex"
  uRegFile=regFile~upper

   -- write prolog
  ifs = .stream~new(fragmentName || uRegfile"_add.reg")~~open("replace")
  ufs = .stream~new(fragmentName || uRegfile"_remove.reg")~~open("replace")

  ifs~~say("Windows Registry Editor Version 5.00")~~say
  ufs~~say("Windows Registry Editor Version 5.00")~~say

  ifs~~say("; Created by ""setupFileAssociations.rex"" (from BSF4ooRexx) on:" dir~runtime)~~say
  ufs~~say("; Created by ""setupFileAssociations.rex"" (from BSF4ooRexx) on:" dir~runtime)~~say

   -- add file-extension entries: do not change "HKCR\.REX" definitions
  bCreatedREXentry=.false     -- set to true, if .REX entry had to be created
  do extension over dir~rex.extensions
     if extension='.rex' then    -- this extension should exist already
     do
        key=hkcr || "\" || extension~upper || "\"
        keyVal=queryWinRegistry(wsh, key)
        if keyVal<>.nil then           -- if set up, don't change it
        do
           ifs~~say("; skipping existing key:" pp(hkcr || "\" || extension~upper)) ~~say   -- add/update key
           iterate
        end
        bCreatedREXentry=.true
     end

     ifs~say(pp(hkcr || "\" || extension~upper))    -- add/update key
     ifs~say('@="REXXScript"')
     ifs~say('"Content Type"=' || enQQ(dir~rex.mimetype) )
     ifs~say('"PerceivedType"="text"')

     ufs~say(pp('-' || hkcr || "\" || extension~upper)) -- remove entire key

     ifs~say
     ufs~say
  end

   -- be nice and also add the new definitions (researched for this program) to "HKCR/.REX", if not present

  if bCreatedREXEntry=.false then   /* .REX key existed already */
  do
     bAdded_REX_Key_Entry=.false    /* indicate whether .REX key entry got created */
         -- set mimetype information with filetype
     contentTypeKey = hkcr || "\.REX\Content Type"
     contentTypeVal = queryWinRegistry(wsh, contentTypeKey)
     if contentTypeVal=.nil then
     do
         -- add mimetype information
        ifs~say(pp(hkcr || "\.REX"))    -- process key
        ufs~say(pp(hkcr || "\.REX"))
        bAdded_REX_Key_Entry=.true

        ifs~say('"Content Type"=' || enQQ(dir~rex.mimetype))   -- add/update value
        ufs~say('"Content Type"=-')     -- remove value
     end
     else
     do
        ifs~say(";" pp(hkcr || "\.REX"))    -- add/update key
        ifs~say("; mimetype entry exists already:" pp(contentTypeKey)"="pp(contentTypeVal) )    -- add/update key
     end

         -- set PerceivedType information with filetype
     perceivedTypeKey = hkcr || "\.REX\PerceivedType"
     perceivedTypeVal = queryWinRegistry(wsh, perceivedTypeKey)
     if perceivedTypeVal=.nil then
     do
         -- add mimetype information
        if bAdded_REX_Key_Entry=.false then
        do
           ifs~say(pp(hkcr || "\.REX"))    -- add/update key
           ufs~say(pp(hkcr || "\.REX"))
           bAdded_REX_Key_Entry=.true
        end

        ifs~say('"PerceivedType"="text"')
        ufs~say('"PerceivedType"=-')    -- remove value
     end
     else
     do
        ifs~say(";" pp(hkcr || "\.REX"))    -- add/update key
        ifs~say("; PerceivedType entry exists already:" pp(perceivedTypeKey)"="pp(perceivedTypeVal) )    -- add/update key
     end

     ifs~say      -- end of entries under the key ".REX"
     ufs~say      -- end of entries under the key ".REX"

         -- new key! set template file name for ShellNew
     fname=stream("..\Templates\"dir~rex.template, "C", "query exists") /* from current directory */
     if fname<>"" then
     do
        shellNewKey = hkcr || "\.REX\ShellNew\"
        shellNewVal = queryWinRegistry(wsh, shellNewKey)
        if shellNewVal=.nil then
        do
            -- add mimetype information
           ifs~say(pp(hkcr || "\.REX\ShellNew"))   -- add/update key
           ifs~say('"FileName"=' || quote4reg(fname) )

           ufs~say(pp("-" ||  hkcr || "\.REX\ShellNew")) -- remove this particular key
           ufs~say
        end
        else
        do
           ifs~say(";" pp(hkcr || "\.REX\ShellNew"))    -- add/update key
           ifs~say("; key exists already:" pp(shellNewKey) )    -- add/update key
        end
        ifs~say
     end

  end

   -- add file-extension to MIME DataBase
  mimeTypeKey = hkcr || "\MIME\DataBase\Content Type\" || dir~rex.mimetype
  mimeTypeVal = queryWinRegistry(wsh, mimeTypeKey)
  if mimeTypeVal=.nil then
  do
     ifs~say(pp(hkcr || "\MIME\DataBase\Content Type\" || dir~rex.mimetype))  -- add/update key
     ifs~say('"Extension"=' || enQQ(".rex") )

     ufs~say(pp('-' || hkcr || "\MIME\DataBase\Content Type\" || dir~rex.mimetype))  -- remove key
     ufs~say
  end
  else
  do
     ifs~say(";" pp(mimeTypeKey) "exists already, value:" pp(mimeTypeVal))
  end
  ifs~say

   --------------------------------------
   -- edit key "REXXScript"
   -- change description, if necessary


   keyValue=hkcr || "\REXXScript"
   oldValue = queryWinRegistry(wsh, keyValue"\") -- get default value, if key exists
   if oldValue=.nil then   /* No key "HKCR\REXXScript" defined ! */
   do
      call createTypeNameEntry dir, ifs, ufs /* procedure will default to "REX" */
   end
   else  /* RexxScript-key exists, just check individual items to replace */
   do
      -- add typeName definitions
      keyValue=hkcr || "\REXXScript"
      ifs~say(pp(keyValue))    -- define key to work with
      ufs~say(pp(keyValue))

      oldValue = queryWinRegistry(wsh, keyValue"\") -- get default value
      ifs~say('@=' || enQQ(dir~rex.comment))  -- description/comment of entry
      ufs~say('@=' || enQQ(oldValue))         -- description/comment of entry

      ifs~say
      ufs~say

       -- change Rexx icon
      iconKey = "HKEY_CLASSES_ROOT\REXXScript\DefaultIcon"
      iconVal = queryWinRegistry(wsh, iconKey"\")   -- try to get current default value

      ifs~say(pp(iconKey))     -- udpate key
      fname=""
      if dir~win.version>5 then  -- Vista, Win7 or higher?
         fname=stream(dir~path_to_images || "\windows\windows6\" || dir~rex.baseName || ".ico", "C", "query exists")

      if fname="" then        -- get the default icon library for Windows
         fname=stream(dir~path_to_images || "\windows\" || dir~rex.baseName || ".ico", "C", "query exists")

      ifs~say('@='quote4reg(fname",0"))

      ufs~say(pp(iconKey))     -- udpate key
      if iconVal=.nil then     -- no DefaultIcon key as of yet!
      do
         fname=stream(dir~rexx_home || "\" || "rexx.ico", "C", "query exists")
         ufs~say('@='quote4reg(fname",0"))
      end
      else   -- use existing value for restoring
      do
         ufs~say('@='quote4reg(iconVal))      -- restore value that was found
      end
   end


   --- done
  ifs~close
  ufs~close

  return


   ----- add the .RXJ and .RXO definitions ------------------------------------------------
createNewAssociations: procedure
  use arg dir, hkcr, wsh, fragmentName

  -- create separate files for each file association type
  do regfile over .list~of("rxj", "rxo")
     uRegFile=regfile~upper
       -- open/create files
     ifs = .stream~new(fragmentName || uRegfile"_add.reg")~~open("replace")
     ufs = .stream~new(fragmentName || uRegfile"_remove.reg")~~open("replace")

         -- write prolog
     ifs~~say("Windows Registry Editor Version 5.00")~~say
     ufs~~say("Windows Registry Editor Version 5.00")~~say

     ifs~~say("; Created by ""setupFileAssociations.rex"" (from BSF4ooRexx) on:" dir~runtime)~~say
     ufs~~say("; Created by ""setupFileAssociations.rex"" (from BSF4ooRexx) on:" dir~runtime)~~say

     typeName=dir~entry(regfile".Name")
     mimetype=dir~entry(regfile".MimeType")

      -- add file-extension entries
     do extension over dir~entry( uRegFile".extensions")

        key=hkcr || "\" || extension~upper || "\"
        keyVal=queryWinRegistry(wsh, key)
        if keyVal<>.nil then           -- if set up, don't change it
        do
           ifs~~say("; skipping existing key:" pp(hkcr || "\" || extension~upper)) ~~say   -- add/update key
           iterate
        end

        ifs~say(pp(hkcr || "\" || extension~upper))    -- add/update key
        ifs~say('@=' || enQQ(typeName))
        ifs~say('"Content Type"=' || enQQ(mimetype) )
        ifs~say('"PerceivedType"="text"')

        ufs~say(pp('-' || hkcr || "\" || extension~upper)) -- remove entire key

        ifs~say
        ufs~say
     end


         -- set template file name for ShellNew
     fname=stream("..\Templates\"dir~entry(regfile".template"), "C", "query exists") /* from current directory */
     if fname<>"" then
     do
        shellNewKey = hkcr || "\."uRegFile"\ShellNew\"
        shellNewVal = queryWinRegistry(wsh, shellNewKey)
        if shellNewVal=.nil then
        do
            -- add mimetype information
           ifs~say(pp(hkcr || "\."uRegFile"\ShellNew"))   -- add/update key
           ifs~say('"FileName"=' || quote4reg(fname) )

           ufs~say(pp("-" ||  hkcr || "\."uRegFile"\ShellNew")) -- remove this particular key
           ufs~say
        end
        else
        do
           ifs~say(";" pp(hkcr || "\."uRegFile"\ShellNew"))    -- add/update key
           ifs~say("; key exists already:" pp(shellNewKey))    -- add/update key
        end
        ifs~say
     end


      -- add file-extension to MIME DataBase
     ifs~say(pp(hkcr || "\MIME\DataBase\Content Type\" || mimetype))  -- add/update key
     ifs~say('"Extension"=' || enQQ("."regFile) )
     ifs~say

     ufs~say(pp('-' || hkcr || "\MIME\DataBase\Content Type\" || mimetype))  -- remove key
     ufs~say

     call createTypeNameEntry dir, ifs, ufs, hkcr, typename, regFile

      -- done
     ifs~close
     ufs~close
  end
  return

createTypeNameEntry: procedure
  use strict arg dir, ifs, ufs, hkcr="HKEY_CLASSES_ROOT", typename=(dir~rex.name), regFile="rex"

  -- add typeName definitions
  ifs~say
  ifs~say(pp(hkcr || "\" || typename))    -- add/update key
  ufs~say(pp('-' || hkcr || "\" || typename))    -- remove key

  ifs~say('@=' || enQQ(dir~entry(regFile".comment")))  -- description/comment of entry
  ifs~say

         -- define icon
  ifs~say(pp(hkcr || "\" || typename || "\DefaultIcon"))
  fname=""
  if regFile="rex", dir~win.version>5 then   -- Vista, Win7 or higher?
     fname=stream(dir~path_to_images || "\windows\windows6\" || dir~entry(regFile".baseName") || ".ico", "C", "query exists")

  if fname="" then
     fname=stream(dir~path_to_images || "\windows\" || dir~entry(regFile".baseName") || ".ico", "C", "query exists")

-- TODO: check whether we need to enquote the file-name part (if blanks are in the path)
  ifs~~say('@='quote4reg(fname",0"))~~say

         -- define shell
  ifs~say(pp(hkcr || "\" || typename || "\shell"))
  ifs~~say('@="open"')~~say

         -- define shell edit
  ifs~say(pp(hkcr || "\" || typename || "\shell\edit"))
  ifs~~say('@="Edit"')~~say

         -- define shell edit command
  ifs~say(pp(hkcr || "\" || typename || "\shell\edit\command"))
  ifs~~say('@=' || quote4reg('notepad.exe "%1"'))~~say


         -- define shell open
  ifs~say(pp(hkcr || "\" || typename || "\shell\open"))
  ifs~~say('@="Run"')~~say

         -- define shell open command
  ifs~say(pp(hkcr || "\" || typename || "\shell\open\command"))
  ifs~say('@=' || quote4reg(enQQ(dir~full_path_to_rexx) '"%1" %*'))

  return




/* ================================================================================= */
/* -------------------- Linux part ---------------------- */
/* rgf, 2011-03-24, replacement, now using XdgUtils exclusively, cf. <http://portland.freedesktop.org>,
   using "--mode system" in the following sequence:

   - xdg-icon-resource  -- install/delete icons
   - xdg-desktop-menu   -- install/delete .directory and .desktop files
   - xdg-mime           -- install/delete
*/
::routine create4linux_xdg
  use arg dir

  parse source opsys +1
  cmd.eIsMacOSX = (opsys='D' | opsys='M') -- "Darwin" (or old: "Macosx")

  baseDir      = "/usr/share"       -- install into "/usr/share" branch
  skeletonDir  = "/etc/skel"        -- skeleton dir to create "Templates", will be used for each new created user
  -- TemplatesDir = "$HOME/Templates"  -- template dir (user-related); maybe we need to check "~/.config/user-dirs.dirs"
  cmd.eInstallDir    =directory()   -- should be "./install"
  cmd.ePath2images   =cmd.eInstallDir"/images/linux/"
  cmd.ePath2resources=cmd.eInstallDir"/linux/resources/"
  cmd.eMode          ="system"      -- install system-wide
  cmd.eXdgMergedMenus="/etc/xdg/menus/applications-merged/"    -- location where the xdg-menu file needs to go to

  cmd.eShHashBang    = "#!/bin/sh"
  cmd.eXdgPathProlog = "export PATH=$PATH:"cmd.ePath2resources"xdg-utils/scripts"

   -- get and save logName and sudoUser-Name
   cmd.eLogName  = value('LOGNAME',    ,'ENVIRONMENT')  -- get current logname, could be 'root' if sudo'ing
   cmd.eSudoUser = value('SUDO_USER',  ,'ENVIRONMENT')  -- maybe sudo'ing, then who is the sudo user

   if cmd.eLogName="root", cmd.eSudoUser<>"" then
      cmd.eRealUserName=cmd.eSudoUser
   else
      cmd.eRealUserName=cmd.eLogName

  cmd.eChownUserGroup = cmd.eRealUserName":"determineGroupName(cmd.eRealUserName)
  cmd.eHomeDir=determineHomeDir(cmd.eRealUserName, cmd.eIsMacOSX)

  cmd.eEcho     = "echo"
  cmd.eCopy     = "cp -pfv"         /* preserve, force, verbose: source target(dir) [works on FAT32/NTFS systems from Linux as well] */
  cmd.eLink     = "ln -fsv"         /* force, symbolic, verbose: source target(dir/)  */
  cmd.eMkDir755 = "mkdir -v -m u=rwx,go=rx"   /* verbose, created directories have umask 755 */
  cmd.eMkDir775 = "mkdir -v -m ug=rwx,o=rx"   /* verbose, created directories have umask 775 */
  cmd.eRm       = "rm -fv"          /* force, verbose */
  cmd.eRmDir    = "rmdir --ignore-fail-on-non-empty -v"  /* no force! verbose, but do not fail */
  cmd.eTouch    = "touch"

  cmd.eChown="chown" cmd.eChownUserGroup   -- command to change the owner of the generated un/install scripts
  cmd.eSetExecutable = "chmod 755"  /* set executable for all (user, group, other)  */

  cmd.eNullDevice = "/dev/null" /* null device  */

  call ctlLinuxIconsMenus cmd., dir   -- create icons and menus

  -- create separate files for each file association type;
  fragmentName = "fileAssociations_for_"

  -- use the .rex file to register icons and setting up the menu
  mimeDir=.directory~new
  mimeDir~rex="oorexx"
  mimeDir~rxj="bsf4oorexx"
  mimeDir~rxo="oorexx4ooo"
  do regfile over .list~of("rex", "rxj", "rxo")
     baseName = dir~entry(regfile".baseName")
     desktopName = baseDir"/applications/"||baseName".desktop"

       -- open/create files
     ifs = .stream~new(fragmentName || regfile"_add.sh")~~open("replace")
     ufs = .stream~new(fragmentName || regfile"_remove.sh")~~open("replace")

         -- write prolog
     ifs~~say(cmd.eShHashBang)~~say~~say(cmd.eXdgPathProlog)~~say
     ufs~~say(cmd.eShHashBang)~~say~~say(cmd.eXdgPathProlog)~~say

     ifs~~say("# Created by ""setupFileAssociations.rex"" (from BSF4ooRexx) on:" dir~runtime)~~say
     ufs~~say("# Created by ""setupFileAssociations.rex"" (from BSF4ooRexx) on:" dir~runtime)~~say

         --------------------- define mime types
     ifs~~say~~say("# install mimetypes")~~say
     ufs~~say~~say("# uninstall mimetypes")~~say
     file=cmd.ePath2resources || mimeDir~entry(regFile)".xml"
     name=fileSpec("Name", file)    -- get name
     ifs~say("xdg-mime   install --novendor --mode" cmd.eMode file)     -- add
     ufs~say("xdg-mime uninstall --novendor --mode" cmd.eMode name)        -- remove
     ifs~say; ufs~say

     ---------------------------------------
     -- copy respective template file [create Templates directory in "$HOME/Templates" and "/etc/skel/Templates"]
     call linuxTemplates dir, ifs, ufs, regFile, cmd.

      -- done
     ifs~close
     ufs~close
  end

   -- make executable, change owner
   cmd.eSetExecutable fragmentName"*.sh"
   cmd.eChown         fragmentName"*.sh"
  return


---------------------------------------------------------------------------------------
::routine ctlLinuxIconsMenus  -- control routine for defining icons and menus in own file
  use arg cmd., dir

  fragmentName="xdg_icons_menu_"
  ifs=.stream~new(fragmentName"add.sh")~~open("replace")
  ufs=.stream~new(fragmentName"remove.sh")~~open("replace")

    -- write prolog
  ifs~~say(cmd.eShHashBang)~~say~~say(cmd.eXdgPathProlog)~~say
  ufs~~say(cmd.eShHashBang)~~say~~say(cmd.eXdgPathProlog)~~say

  ifs~~say("# Created by ""setupFileAssociations.rex"" (from BSF4ooRexx) on:" dir~runtime)~~say
  ufs~~say("# Created by ""setupFileAssociations.rex"" (from BSF4ooRexx) on:" dir~runtime)~~say

  call linuxXdgIcons cmd., ifs, ufs   -- add icons to system  -- rgf, 2011-03-25
  call linuxXdgMenu  cmd., ifs, ufs   -- setup menu

  ifs~close
  ufs~close

   -- make executable, change owner
  cmd.eSetExecutable fragmentName"*.sh"
  cmd.eChown         fragmentName"*.sh"


::routine linuxXdgMenu     -- install/uninstall menu
  use arg cmd., ifs, ufs

   -- install menu related files
  ifs~~say~~say("# install desktop and menu files")~~say
  ufs~~say~~say("# uninstall desktop and menu files")~~say

      ---------------------
  ifs~say("# installing desktop files to respective Categories menu (also used for mime associations)")
  ufs~say("# uninstalling desktop files from respective Categories menu (also used for mime associations)")
  -- first install the desktop files needed for handling mime filetypes by Categories
  call sysFileTree cmd.ePath2resources"*.desktop", "files.", "FO"
  do i=1 to files.0
      ifs~say("xdg-desktop-menu   install --noupdate --novendor --mode" cmd.eMode files.i)
      ufs~say("xdg-desktop-menu uninstall --noupdate --novendor --mode" cmd.eMode filespec("Name",files.i))
  end
  ifs~say; ufs~say

      --------------------- desktop files for BSF4ooRexx merged menu
  ifs~say("# installing desktop files used in merged BSF4ooRexx menu")
  ufs~say("# uninstalling desktop files used in merged BSF4ooRexx menu")

  dirFileBSF4ooRexx  =cmd.ePath2resources"menu/bsf4oorexx_menu.directory"
  dirFileInstallation=cmd.ePath2resources"menu/bsf4oorexx_submenu_installation.directory"

      -- create the BSF4ooRexx-menu entries
  do file over .array~of("bsf4oorexx_information.desktop", "bsf4oorexx_oorexxtry.desktop", -
                         "bsf4oorexx_samples.desktop",     "bsf4oorexx_utilities.desktop" )
     path2file=cmd.ePath2resources"menu/"file
     ifs~say("xdg-desktop-menu   install --noupdate --novendor --mode" cmd.eMode dirFileBSF4ooRexx path2file)
     ufs~say("xdg-desktop-menu uninstall --noupdate --novendor --mode" cmd.eMode dirFileBSF4ooRexx file)
  end

      -- create the Installation-submenu entries
  do file over .array~of("bsf4oorexx_home.desktop",      "bsf4oorexx_reinstall.desktop", -
                         "bsf4oorexx_setupjava.desktop", "bsf4oorexx_uninstall.desktop")

     path2file=cmd.ePath2resources"menu/"file
     ifs~say("xdg-desktop-menu   install --noupdate --novendor --mode" cmd.eMode dirFileBSF4ooRexx dirFileInstallation path2file)
     ufs~say("xdg-desktop-menu uninstall --noupdate --novendor --mode" cmd.eMode dirFileBSF4ooRexx dirFileInstallation file)
  end

  ifs~~say~~say("xdg-desktop-menu   forceupdate --mode" cmd.eMode)~~say
  ufs~~say~~say("xdg-desktop-menu   forceupdate --mode" cmd.eMode)~~say

  return


---------------------------------------------------------------------------------------
-- cf. <http://portland.freedesktop.org/xdg-utils-1.0/xdg-icon-resource.html>
::routine linuxXdgIcons    -- install/uninstall icons to/from system
  use arg cmd., ifs, ufs

  ifs~~say~~say("# install icons")~~say
  ufs~~say~~say("# uninstall icons")~~say

  call sysfiletree cmd.ePath2images"/*.png", "files.", "FSO"   -- search for png-icon files in all subdirectories
  theme=""
  do i=1 to files.0  -- loop over found icon files, e.g. .../images/linux/Crystal/oorexx_032.png"
     theme=linuxAddIconsDoTheWork(cmd., ifs, ufs, theme, files.i, "")
  end
  ifs~say; ufs~say   -- insert empty line for better ledgibility

   -- now define the x-mime icons
   logos=.array~of("oorexx_", "bsf4oorexx_",  "oorexx4ooo_")
   mimes=.array~of("application-x-rexx", "application-x-rexx-java", "application-x-rexx-java-ooo")
   theme=""
   do i=1 to logos~items
      call sysFileTree cmd.ePath2images"/"logos[i]"*.png", "files.", "FOS"
      do k=1 to files.0
        if pos("_logo", files.0)>0 then iterate -- do not use the logo icon as mimetype icon (testing)
        theme=linuxAddIconsDoTheWork(cmd., ifs, ufs, theme, files.k, mimes[i])
     end
   end

   -- now let the system update the icon file cache
   ifs~~say("xdg-icon-resource forceupdate --mode" cmd.eMode)~~say
   ufs~~say("xdg-icon-resource forceupdate --mode" cmd.eMode)~~say
   ifs~say; ufs~say   -- insert empty line for better ledgibility
   return

   -- create the appropriate install/uninstall statements in the install/uninstall file
linuxAddIconsDoTheWork: procedure
  use arg cmd., ifs, ufs, oldTheme, path2file, xMimeName=""

  lpos=lastPos("/", path2file)
  lpos=lastPos("/", path2file, lpos-1)   -- find theme's name
  -- parse var path2file +(lpos) theme "/" name "_" size "." ext

  parse var path2file +(lpos) theme "/" name "." ext
  lpos=lastPos("_", name)     -- make sure we only deal with the last underscore, which must be followed by a number
  size=substr(name,lpos+1)    -- take remainder
  name=substr(name,1,lpos-1)  -- extract name part

      -- define icon file descriptor
  if xMimeName<>"" then
  do
     context="mimetypes"
     name=xMimeName
  end
  else   -- regular application icon
  do
     context="apps"
  end
  params="--noupdate --novendor --mode" cmd.eMode "--context" context "--theme" theme "--size" (size+0)
  stderr2null="2>"cmd.eNullDevice   -- don't show user errors (e.g. copying to nonexistent theme directories)

  ifs~say("xdg-icon-resource   install" params path2file name stderr2null)
  ufs~say("xdg-icon-resource uninstall" params name stderr2null)
  if oldTheme<>"", oldTheme<>theme then
  do
     ifs~say; ufs~say   -- insert empty line for better ledgibility
  end
  return theme

----------------------------- end of (new) xdg usage ----------------------------------
---------------------------------------------------------------------------------------


::routine linuxTemplates      -- create Templates infrastructure, if necessary; supply rex/rxj/rxo templates
   use strict arg dir, ifs, ufs, regFile, cmd.

   ifs~~say~~say("# install template")~~say
   ufs~~say~~say("# uninstall template")~~say

   skelHome = "/etc/skel"                       -- skeleton home

   if dir~debug=.true then
   do
      say "-> linuxTemplates(), regFile="pp(regFile)
      say "-> linuxTemplates(), logName="pp(cmd.eLogName) "cmd.eHomeDir="pp(cmd.eHomeDir) "skelHome="pp(skelHome)
   end

   templateName = dir~entry(regFile".template") -- get template's name

   if dir~debug=.true then
      say "-> linuxTemplates(), templateName="pp(templateName)

   if templateName=.nil then return             -- none given

   path2srcTemplate=stream("../Templates/"templateName, "C", "query exists") /* from current directory */

   if dir~debug=.true then
      say "-> linuxTemplates(), path2srcTemplate="pp(path2srcTemplate)

   if path2srcTemplate="" then return        -- corresponding template file does not exist

   defaultTemplateName=getTemplateDirName()  -- get default name for "Templates" directory
   if dir~debug=.true then
      say "-> linuxTemplates(), defaultTemplateName="pp(defaultTemplateName)

   userTemplatesDirName=getTemplateHomeDirName(defaultTemplateName, cmd.)
   if dir~debug=.true then
      say "-> linuxTemplates(), userTemplatesDirName="pp(userTemplatesDirName)

   if SysFileExists(userTemplatesDirName)=.false then -- Templates folder does not exist !
   do
       ifs~say("# Templates directory will be created, but never removed")
       ifs~~say(cmd.eMkDir775 enQQ(userTemplatesDirName))~~say -- create Templates directory
            -- change owner/group to real user
       ifs~~say(cmd.eChown enQQ(userTemplatesDirName))
       ifs~say
   end

   if SysFileExists(skelHome"/Templates")=.false then -- Templates folder does not exist !
   do
       ifs~say("# Templates directory will be created, but never removed")
       ifs~~say(cmd.eMkDir755 skelHome"/Templates")~~say -- create Templates directory
   end

   ifs~say(cmd.eCopy enQQ(path2srcTemplate) enQQ(userTemplatesDirName))
   ufs~say(cmd.eRm   enQQ(userTemplatesDirName"/"templateName))

   ifs~say(cmd.eCopy enQQ(path2srcTemplate) enQQ(skelHome"/Templates"))
   ufs~say(cmd.eRm   enQQ(skelHome"/Templates/"templateName))

   ifs~say
   ufs~say
   return



getTemplateDirName: procedure    -- get the default template name
   fn="/etc/xdg/user-dirs.defaults"
   templateName=""

   if SysFileExists(fn)=.true then
   do
      fi=.stream~new(fn)~~open("READ")
      do while fi~chars>0
         line=fi~linein
         parse var line "TEMPLATES=" templateName
         if templateName<>"" then
            leave
      end
      fi~close
   end

   if templateName="" then    -- in need of a default name?
      templateName="Templates"

   return templateName


getTemplateHomeDirName: procedure      -- get the user's name for the Templates folder (could be translated)
   use arg defaultTemplateName, cmd.

   homeTemplateDir=cmd.eHomeDir"/"defaultTemplateName

   fn=cmd.eHomeDir"/.config/user-dirs.dirs"
   if SysFileExists(fn)=.true then
   do
      fi=.stream~new(fn)~~open("READ")
      content=fi~arrayin         -- get file's content as an array
      fi~close

      do i=1 to content~items
         if pos('XDG_TEMPLATES_DIR="', content[i])>0 then leave
      end

      if i<=content~items then   -- o.k. we are within the array limits, hence found
      do
          parse value content[i] with '="' templateName '"' -- get path

          if templateName="$HOME" | templateName="$HOME/" then  -- we need to correct this and set & create the Templates dir
          do
             templateName="$HOME/"defaultTemplateName    -- define new templateName
             content[i]='XDG_TEMPLATES="'templateName'"' -- replace entry with new value

             cmd.eCopy fn fn || "." || (.datetime~new~string~changestr(":","_")) || ".bkp" -- create a backup-copy
             fo=.stream~new(fn)~~open("replace")
             fo~arrayout(content)         -- replace definition file
             fo~close
          end

          if templateName~left(6)="$HOME/" then    -- we need to replace $HOME with physical path
              homeTemplateDir=templateName~changeStr("$HOME", cmd.eHomeDir)
          else
              homeTemplateDir=templateName
      end
   end

   return homeTemplateDir


/* ================================================================================= */
/* -------------------- Miscellaneous ------------------- */
::routine pp
  return "["arg(1)"]"

::routine quote4reg  -- qote a string (e.g. fully qualified path) according to the Windows registry rules
  parse arg str

  newStr=str~changeStr("\", "\\")      -- escape regular backslashes
  newStr=newStr~changeStr('"', '\"')   -- escape double-quotes
  return enQQ(newStr)

::routine enQQ       -- enclose string in double quotes
  parse arg str
  return '"' || str || '"'

::routine enQS       -- enclose string in single quotes, meant for Unix' echo command
  parse arg str
  return "'" || str || "'"

::routine queryWinRegistry    -- reads the given key from the registry
  use arg wsh, key
  signal on syntax            -- will be raised as OLE error, if key cannot be read/opened

  return wsh~regRead(key)     -- return value stored with key

syntax:                       -- key could not be read/opened
  return .nil                 -- return .nil


::routine showDir    -- show current content of dir
  use strict arg dir, file.sep
  signal on syntax name syntax1  -- in case "rgf_util2.rex" cannot be found (path not set up yet), just return gracefully
  call "rgf_util2.rex"
  call dump2 dir
  return

syntax1:
  signal on syntax name syntax2  -- in case "rgf_util2.rex" cannot be found (path not set up yet), just return gracefully
  fn=".."file.sep"rgf_util2.rex"  -- now try to call it from the parent directory
  call (fn)  -- now try to call it from the parent directory
  call dump2 dir
  return

syntax2:
  signal on syntax name syntax3  -- in case "rgf_util2.rex" cannot be found (path not set up yet), just return gracefully
  fn=".."file.sep"bin"file.sep"rgf_util2.rex"
  call (fn)  -- now try to call it from the parent directory
  call dump2 dir
  return

syntax3:
  say "showDir(): syntax error, cannot dump 'dir' (probably path to 'rgf_util2.rex' not set up yet) ..."
  return

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

