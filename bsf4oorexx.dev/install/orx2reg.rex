#!/usr/bin/env rexx
/*
   last change: $Revision: 927 $ $Author: Administrator $ $Date: 2010-12-26 18:34:19 +0100 (Sun, 26 Dec 2010) $

   date:       2008-05-12, rgf

   version:    1.0.3

   authors:    Rony G. Flatscher

   purpose:    allows to add/remove paths from the registry; used in BSF4Rexx for permanently
               setting/removing paths for BSF4Rexx and/or OOo; the add-routine will remove
               duplicates paths before writing the new values

   usage:      orx2reg [/q[uery]  [/key:"keyValue"]]
                  ... returns the value of given key; if omitted the following values are
                      queried and returned:

                      "HKCU\Environment\CLASSPATH"
                      "HKCU\Environment\PATH"
                      "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment\CLASSPATH"
                      "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment\Path"

               orx2reg /a[dd]    /key:"keyValue" /path:"pathValue"
                  ... adds/changes given key with/to new path

               orx2reg /r[emove] /key:"keyValue" /path:"pathValue"
                  ... removes given path from given key; if no value left, then the
                      key is deleted/removed from the registry

   needs:      ooRexx 3.x

   changed: 2010-08-10, ---rgf: adding/removing "rexxpaws.exe" as SendTo-program
            2010-08-18/19, ---rgf: adding/removing Start-menu entries for menu node "BSF4ooRexx" to
                                ease reinstalling and uninstalling
            2011-01-26, ---rgf: cater for escaped "%" as "[25x]" on Windows
            2011-03-20, ---rgf: - added possibility to define an icon to a shortcut
                                - added menu-entry "ooRexxTry.rxj"
            2011-06-19, ---rgf: add the new icons from Graham Wilson for reinstall and uninstall



------------------------ Apache Version 2.0 license -------------------------
   Copyright 2008-2011 Rony G. Flatscher

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



------------------------ Common Public License 1.0 --------------------------
   Copyright 2008-2011 Rony G. Flatscher

   This program and the accompanying materials are made available under the
   terms of the Common Public License v1.0 which accompanies this distribution.

   It may also be viewed at: http://www.opensource.org/licenses/cpl1.0.php
-----------------------------------------------------------------------------

*/

parse version name rexxLevel date      -- parse the words
if rexxLevel<6 then
do
   say "Need RexxLevel 6.00 (IBM Object Rexx) or higher (ooRexx), aborting..."
   exit -1     -- wrong interpreter level
end


wsh=.OleObject~new("WScript.Shell")    -- Windows Script shell object

.local~pathDeli=";"                    -- path delimiter on Windows
.local~kind="REG_EXPAND_SZ"            -- kind (type) of registry entry

if arg(1)="" then       -- no command line switches given, do a default query
do
   call default_query wsh
   exit
end

parse caseless arg "/" +1 switch +1 '/key:"' key '"' 'path:"' path '"'
switch=switch~translate    -- put into uppercase
path=path~changestr("[25x]", "%")     -- unescape


if pos(switch, "ARQ")=0 then
do
   say "Switch character" pp(switch) "not one of 'A', 'Q', 'R'!" .endOfLine
   call usage
   exit -2     -- wrong usage
end

   -- process switches
if switch="A" then         -- add path to key
do
   if key="" then
   do
      "Key argument is empty or was not within mandatory quotes, aborting..."
      exit -3
   end
   if path="" then
   do
      "Path argument is empty or was not within mandatory quotes, aborting..."
      exit -4
   end

   call add2Registry wsh, key, path
   call RexxPaws wsh, "A"    -- add
   call startMenu wsh, "A"    -- add
end

else if switch="R" then    -- remove path from key
do
   if key="" then
   do
      "Key argument is empty or was not within mandatory quotes, aborting..."
      exit -3
   end
   if path="" then
   do
      "Path argument is empty or was not within mandatory quotes, aborting..."
      exit -4
   end

   call removeFromRegistry wsh, key, path
   call RexxPaws wsh, "R"    -- remove
   call startMenu wsh, "R"   -- remove
end

else                       -- query
do
   call query wsh, key
   exit
end

   -- starting with ooRexx 3.2 we can broadcast a settings change!
signal on syntax
call winsystm.cls          /* this will load the .WindowsManager class */
.WindowsManager~new~broadcastSettingChanged(1000)
-- say "New sessions will possess the new registry values."
exit

syntax:                    /* no .WindowsManager class or unknown "broadcastSettingChanged"
                              not known yet */
--   say "Logoff/logon or reboot in order to make the new registry values visible."



::routine usage      -- show usage section for user
  sl=sourceline()    -- get total number of sourcelines
  do i=1 to sl
     parse value sourceLine(i) with firstWord .
     if firstWord="usage:" then
     do
        say sourceLine(i)
        do k=i+1 to sl
           parse value sourceLine(k) with firstWord .
           if firstWord~right(1)=":" then return
           say sourceLine(k)
        end
     end
  end



   -- show HKCU (current user) and HKLM (local machine, system settings) PATH and CLASSPATH settings
::routine default_query
   use strict arg wsh

   keys=.list~of(                                                                         -
                "HKCU\Environment\Path"                                                      , -
                "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment\Path"     , -
                "HKCU\Environment\CLASSPATH"                                                 , -
                "HKLM\SYSTEM\CurrentControlSet\Control\Session Manager\Environment\CLASSPATH"  -
                )

   do k over keys       -- iterate over registry keys
      call query wsh, k -- show values
      say
   end


   -- show key value, list individual paths for easier debugging
::routine query
   use strict arg wsh, key

   tab1="09"x
   tab2="0909"x
   parse source . . thisFile
   fn=filespec("N", thisFile)

   say '@ECHO OFF'
   say 'REM' .DateTime~new
   say

   val=getVal(wsh,key)
   if val=.nil then
      say 'echo' '"'key'" ->' "["val"] (key does not exist)"

   else
   do
      say 'echo' '"'key'" ...'
      say
      say "rexx" fn "/add" '/key:"'key'"' '/path:"'val'"'
      say
      say tab1 'rem' "consisting of the following individual paths:"
      SAY

       paths=val~makeArray(.pathDeli)
       do p over paths
          say tab2 'rem' pp(p)         -- show individual path
       end
       say tab1 'rem' '-'~copies(78)
       say
   end


   -- show key value, list individual paths for easier debugging
::routine query_ori
   use strict arg wsh, key

   val=getVal(wsh,key)
   if val=.nil then
      say '"'key'" ->' "["val"] (key does not exist)"

   else
   do
      tab1="09"x
      tab2="0909"x
      say '"'key'" ->' "["val"]"
      say
      say tab1 "consisting of the following individual paths:"

       paths=val~makeArray(.pathDeli)
       do p over paths
          say tab2 pp(p)         -- show individual path
       end
   end



::routine pp
  return "["arg(1)"]"




::routine getVal           -- gets the value of the passed in key from the registry
  use arg wsh, key

  signal on syntax
  return wsh~regRead(key)

syntax: return .nil




/* Removes path(s) in "needle" from the given "key"'s value. */
::routine removeFromRegistry        -- allow needle to consist of multiple paths
   use arg wsh, key, needle


   newVal=""                        -- new value

   val=getVal(wsh, key)      /* get current value */
   if val=.nil then                 -- key does not exist, nothing to do
      return

     -- re-create the current value, remove duplicates
   valSet=.set~new                  -- set of values processed already
     -- remember needles that need to be removed
   needles=needle~makeArray(.pathDeli)
   do needle over needles
      uNeedle=needle~upper~strip    -- turn needle into uppercase
      valSet~put(uNeedle)           -- add to set
   end

   vals=val~makearray(.pathDeli)
   do v over vals                   -- create new value, remove duplicates
      uv=v~strip~upper
      if valSet~hasIndex(uv) then iterate -- duplicate or needle, skip
      valSet~put(uv)                -- add to set
      if newVal="" then newVal=v
                   else newVal=newVal||.pathDeli||v
   end

   if newVal<>val then              -- path changed, update key
   do
      if newVal="" then             -- remove key
         wsh~regDelete(key)
      else
         wsh~regWrite(key, newVal, .kind)
   end




/* Adds path(s) in "needle" to the given "key"'s value. In the process possible
   duplicate paths are removed. The sequence of paths is kept, the new paths are
   simply appended.
*/
::routine add2Registry        -- allow needle to consist of multiple paths
  use arg wsh, key, needle


  bAddNeedle=.true                      -- do we have to add the needle ?
  newVal=""                             -- new value

  val=getVal(wsh, key)      /* get current value */
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
     vals  =val~makearray(.pathDeli)
     do v over vals                     -- create new value, remove duplicates
        uv=v~strip~upper
        if valSet~hasIndex(uv) then iterate   -- duplicate, skip
        valSet~put(uv)                  -- add to set
        if newVal="" then newVal=v
                     else newVal=newVal||.pathDeli||v
     end

       -- deal with needles
     needles=needle~makeArray(.pathDeli)     -- maybe more than one path?

     do needle over needles
        uNeedle=needle~upper~strip      -- turn needle into uppercase
        if valSet~hasIndex(uNeedle) then iterate   -- already in new value, no dupes
        valSet~put(uNeedle)             -- remember

        bAddNeedle=.true                -- indicate we need to add the needle
        if newVal="" then newVal=needle -- add needle to value
                     else newVal=newVal||.pathDeli||needle
     end
  end

  if bAddNeedle=.true  then             -- we need to add the needle
     wsh~regWrite(key, newVal, .kind)



::routine RexxPaws            -- a[dd] a link to | r[emove] link from the SendTo special folder "SendTo"
  use strict arg wsh, mode="A"

  SendTo=wsh~SpecialFolders("SendTo")
  if SendTo="" then return   -- folder is not defined for this Windows system

  mode=mode~left(1)~upper

  rexxpaws=value("REXX_HOME",,"ENVIRONMENT") || "\rexxpaws.exe"

  path2ShortCut=SendTo || "\RexxPaws.lnk"
  if SysFileExists(path2ShortCut) then
     call SysFileDelete path2ShortCut     -- remove link

  if mode="A" then         -- add link
  do
     ShortCut = wsh~CreateShortcut(path2shortCut)

     ShortCut~TargetPath = rexxpaws
     ShortCut~Description="Executes a Rexx script, but waits upon termination for user to press the enter key." -
                          "(This way one can see the output of a Rexx script in a window, before the window gets closed.)"
     -- ShortCut~workingDirectory="."
     -- ShortCut~HotKey = "CTRL+SHIFT+R"
     Shortcut~save
  end


::routine StartMenu           -- a[dd=default] | r[emove] StartMenu entries for reinstall and uninstall
  use strict arg wsh, mode="A"

  mode=mode~left(1)~upper

  menuFolder=wsh~SpecialFolders("AllUsersPrograms")
  if menuFolder="" then        -- does not exist on this machine?
     menuFolder=wsh~SpecialFolders("Programs")   -- use the user's start menu

  if menuFolder="" then return   -- folder is not defined for this Windows system

  menuFolder=menuFolder"\BSF4ooRexx"
  menuFolderInstallation=menuFolder"\Installation"

  if SysFileExists(path2ShortCut) then
     call SysFileDelete path2ShortCut     -- remove link

  if mode="A" then         -- add menu
  do
     "md" qq(menuFolder)   -- create menu node (a folder)
     "md" qq(menuFolderInstallation)   -- create menu node for re- and uninstall scripts

     parse source . . s    -- get fully qualified path to this Rexx program

     location=filespec("Location", s)  -- get location of this directory (has a trailing '\')
     subLocation=location"windows"     -- define subfolder where the install scripts reside

     -- REINSTALL
     reinstallScript=subLocation"\reinstall.cmd"
     iconLocation=location"\images\windows\bsf4rexx-reinstall.ico, 0"
     call createMenu menuFolderInstallation, "Reinstall BSF4ooRexx.lnk", reinstallScript, "Reinstalls BSF4ooRexx.", subLocation, .nil, iconLocation
     -- call createMenu menuFolderInstallation, "Reinstall BSF4ooRexx.lnk", reinstallScript, "Reinstalls BSF4ooRexx.", subLocation

--     reinstallScript=subLocation"\reinstall_runas_Administrator.cmd"
--     call createMenu menuFolderInstallation, "Reinstall BSF4ooRexx (runas Administrator).lnk", reinstallScript, "Reinstalls BSF4ooRexx (running as Administrator).", subLocation

     -- UNINSTALL
     rmDir=qualify(location"..")    -- determine root of this installation directory
     args=""
     if rmDir~right(10)="BSF4ooRexx" then -- make sure we are deleting the BSF4ooRexx directory !
     do
         -- if testFile does not exist, uninstalling took place, but because of the tempDir the
         -- path ./BSF4ooRexx/install/windows may not have been successfully removed, hence:
         testFile=location"\uninstall.cmd"

/*
         --    cd to root dir, then remove the base dir again
         fragment= "( if not exist" qq(testFile) "( cd \ & rd /s/q" qq(rmDir) ") )"

            -- debug version (should not delete)
         fragment= "( if not exist" qq(testFile) "( cd \ & echo rd /s/q" qq(rmDir) " & pause ) )"
*/

         fragment = "cd \"    -- just switch to the root directory, such that the uninstall program
                              -- which is running in a different process can then remove the BSF4ooRexx subtree

         -- Shortcut's argument field seems to not be large enough, hence testing only this version:
         args = "/c" "&" fragment
     end

     uninstallScript=subLocation"\uninstall.cmd"
     iconLocation=location"\images\windows\bsf4rexx-uninstall.ico, 0"
     call createMenu menuFolderInstallation, "Uninstall BSF4ooRexx.lnk", uninstallScript, "Uninstalls BSF4ooRexx and removes it from the system.", subLocation, args, iconLocation
     -- call createMenu menuFolderInstallation, "Uninstall BSF4ooRexx.lnk", uninstallScript, "Uninstalls BSF4ooRexx and removes it from the system.", subLocation, args

--     uninstallScript=subLocation"\uninstall_runas_Administrator.cmd"
--     call createMenu menuFolderInstallation, "Uninstall BSF4ooRexx (runas Administrator).lnk", uninstallScript, "Uninstalls BSF4ooRexx and removes it from the system (running as Administrator).", subLocation, args


     -- LINKS TO FOLDERS/URL
--     webSite="http://wi.wu.ac.at/rgf/rexx/bsf4oorexx/current/"
     webSite="http://sourceforge.net/projects/bsf4oorexx/files/"
     call createMenu menuFolderInstallation, "BSF4ooRexx Download Page.lnk", webSite, "Opens the ""BSF4ooRexx"" download page on the Internet."

     InformationFolder=qualify(location"..\information") -- Folder that has additional infos, including ReleaseNotes.txt
     call createMenu menuFolder, "Information (ReleaseNotes, introductions to ooRexx, BSF4ooRexx overview).lnk", InformationFolder, "Opens the BSF4ooRexx ""information"" folder."

     InformationFolder=qualify(location"..\samples") -- Folder that has additional infos, including ReleaseNotes.txt
     call createMenu menuFolder, "Samples (simple to comprehensive BSF4ooRexx examples).lnk", InformationFolder, "Opens the BSF4ooRexx ""samples"" folder."

     UtilitiesFolder=qualify(location"..\utilities") -- Folder that has additional infos, including ReleaseNotes.txt
     call createMenu menuFolder, "Utilities (some platform-independent utility scripts).lnk", UtilitiesFolder, "Opens the BSF4ooRexx ""utilities"" folder."

      /* create menu entry for "ooRexxTry.rxj" */
     linkName   ="GUI RexxTry Program (ooRexxTry.rxj).lnk"
     targetPath =qualify(UtilitiesFolder"\ooRexxTry.rxj")
     description="Platform independent GUI (using Java awt and swing) that allows to interactively enter and execute Rexx code."
     workingDirectory=UtilitiesFolder
     -- no qualify(), leave leading backslash (yielding two consecutive backslashes): only then will WindowsXP use the icon!
     iconLocation=location"\images\windows\oorexxtry.ico, 0"
     -- iconLocation=qualify(location"\images\windows\oorexxtry.ico")

     call createMenu menuFolder, linkName, targetPath, description, workingDirectory, .nil, iconLocation
  end
  else   -- remove BSF4ooRexx menu entry
  do
      if SysFileExists(menuFolder) then
         "rd /s /q" qq(menuFolder)  -- remove menu node (a folder) with all of its contents
  end
  return


createMenu: procedure expose wsh
  use arg menuFolder, linkName, targetPath, description=.nil, workingDirectory=.nil, args=.nil, iconLocation=.nil

-- say "linkName=["linkName"], targetPath=["targetPath"]"
  shortCut=wsh~CreateShortcut(menuFolder"\"linkName)
  shortCut~targetPath=targetPath
  if description<>.nil      then shortCut~description=description
  if workingDirectory<>.nil then shortCut~workingDirectory=workingDirectory
  if args<>.nil & args<>""  then shortCut~arguments=args
  if iconLocation<>.nil     then shortCut~iconLocation=iconLocation
  shortCut~save
  return



::routine qq
  return '"' || arg(1) || '"'

::routine qqEsc
  return '\"' || arg(1) || '\"'  -- escapte the enclosing quotes

