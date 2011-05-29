#!/usr/bin/rexx
/*
   author:  Rony G. Flatscher
   date:    2010-08-05,14,18
   version: 1.03
   name:    setupAllAndRun.rex
   purpose: installer to install or reinstall the BSF4ooRexx package

   usage:  setupAllAndRun.rex [install [targetDir] [/@q:]] | reinstall [/@q:] | uninstall [/keep] [/@q:]
           ... targetDir:    directory to install to, optional
                              ... if targetDir ".", then install from current directory (don't
                                  copy package to default directory)
                              ... if targetDir given, copy to it and install from there

                              ... if an installBSF.{cmd|sh} exists, creates installOOo.{cmd|sh}, if necessary
                              ... if no generated install scripts exist, then first the distribution
                                  is copied to "/opt/BSF4ooRexx" (Linux) or
                                  "%ProgramFiles%\BSF4ooRexx" (Windows) and then installed from there

                "r[einstall]" ... runs uninstall scripts, deletes all install and uninstall scripts,
                                  generates them from scratch and runs the install scripts

   needs:   to be run as sudo or "runas" Administrator or a user with administrative rights

   changed: 2010-08-28, rgf, - on install and reinstall show a BSF4ooRexx dialog about success
                             - make sure that script starts out of its own homedirectory (to allow
                               catering for some Windows 7 new "security" behaviour regarding setting
                               the homedirectory of cmd.exe scripts; so now elevating rexx.exe instead)
            2011-01-26, -rgf, - correct BSF-message popup text to not claim that no errors occurred during install and reinstall
            2011-02-25, -rgf (on the way to Tokyo) - add support for MacOSX
            2011-05-25, -rgf  - add an option "/@q:" (quiet) which will inhibit dialogs, such that
                                this installer can be used by other installers in "silent"
                                (unattended) mode

   remark:  ---

   license:

    ------------------------ Apache Version 2.0 license -------------------------
       Copyright (C) 2010-2011 Rony G. Flatscher

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

bPauseAtEnd=.true                -- pause at end, such that user can see the output in the console

cmd.eBDebug=.false               -- if set to .true, then debug mode
cmd.eBInteractive=.true          -- if set to .true, then Rexx-"pause" are carried out
cmd.eBQuiet=.false               -- if set to .true, then no dialogs are presented

parse source . . s
cmd.eThisLocation=filespec("Location", s) -- get location (including drive letter, if any)
cmd.eThisName=filespec("Name", S)         -- get unqualified name of this program

   /* set this script's directory as working directory, no matter from where it got invoked! */
call directory cmd.eThisLocation

parse version '-' name '_' v '(' . level .

minVersion="4.0.1"
if v<minVersion | level<6.03 then
do
   say cmd.eThisName": this package needs ooRexx version" minVersion "or higher, you are running:" name v", aborting..."
   if cmd.eBInteractive=.true then
   do
      say cmd.eThisName": about to exit program, please hit enter to continue ..."
      parse pull input
   end
   exit -1
end


-- process arguments: setupAllAndRun.rex [install [targetDir]] | reinstall | uninstall [keep]
parse arg runTypeArg targetDir "/@"quietArg":"  -- try to match "/@q:"

if quietArg<>"" then
do
   if quietArg~upper<>"Q" then      -- must be in the form of "/@qr:"
   do
      say cmd.eThisName", usage:  setupAllAndRun.rex [install [targetDir] [/@q:]] | reinstall [/@q:] | uninstall [keep] [/@q:]"
      if cmd.eBInteractive=.true then
      do
         say cmd.eThisName": about to exit program, please hit enter to continue ..."
         parse pull input
      end
      exit -40
   end

   cmd.eBQuiet=.true          -- if set to .true, then no dialogs are presented
   bPauseAtEnd=.false         -- do not wait on user input at end
   cmd.eBInteractive=.false   -- turn off interactivity
end


if runTypeArg="" then
do
   say cmd.eThisName", usage:  setupAllAndRun.rex [install [targetDir] [/@q:]] | reinstall [/@q:] | uninstall [keep] [/@q:]"
   if cmd.eBInteractive=.true then
   do
      say cmd.eThisName": about to exit program, please hit enter to continue ..."
      parse pull input
   end
   exit -99
end
else
do
   runType=runTypeArg~left(1)~upper
   if pos(runType, "IRU")=0 then
   do
       say cmd.eThisName': unknown run mode ['runTypeArg'], must be one of "I[install]", "R[einstall]", or "U[ninstall]", aborting...'

       if cmd.eBInteractive=.true then
       do
          say cmd.eThisName": about to exit program, please hit enter to continue ..."
          parse pull input
       end
       exit -2
   end
end


cmd.eInstallDirName="BSF4ooRexx"             -- name of the directory to copy to
cmd.eSetupBSF="setupBSF.rex"
cmd.eSetupOOo="setupOOo.rex"

   -- start of filenames of generated scripts (to ease deletion)
cmd.eGeneratedScripts.BSF=.array~of("installBSF", "uninstallBSF", "fileAssociations_for_")
cmd.eGeneratedScripts.OOo=.array~of("installOOo", "uninstallOOo", "fileAssociations_for_RXO")

cmd.eOpSysFull=sysVersion()
cmd.eOpSys=cmd.eOpsysFull~left(1)~upper

parse upper source s +1  -- get operating system name in uppercase
cmd.eIsMacOSX=(s="M")    -- determine whether running on "MACOSX"

-- if sysVersion()~left(1)~upper="W" then       -- determine opsys and default target installation directory
if cmd.eOpSys="W" then
do
--   cmd.eOpSys="W"
   cmd.eFile.sep="\"
   cmd.eDefaultTargetDir=value("ProgramFiles", ,"ENVIRONMENT")"\"cmd.eInstallDirName
   cmd.eInstallScriptNameBSF  ="installBSF.cmd"
   cmd.eUninstallScriptNameBSF="uninstallBSF.cmd"
   cmd.eInstallScriptNameOOo  ="installOOo.cmd"
   cmd.eUninstallScriptNameOOo="uninstallOOo.cmd"
--   cmd.eSudo='runas /env /user:Administrator "%cmd%"' -- replace %cmd% with shell-script
   cmd.eRM="del /q"              -- remove command
   cmd.eScriptExtension=".cmd"
   cmd.eRexxJ="rexxj.cmd"

   -- xcopy:   xcopy /s /r /k /y test1\* test2\

   -- del:     del test2 /s /f /q
   -- rmdir:   rd dirName /s /q     ... dirName<>"." because of lock an error would occur
end
else   -- Unix-like system, including MacOSX
do
   cmd.eOpSys="U"
   cmd.eFile.sep="/"
   cmd.eDefaultTargetDir="/opt/"cmd.eInstallDirName
   cmd.eInstallScriptNameBSF  ="./installBSF.sh"
   cmd.eUninstallScriptNameBSF="./uninstallBSF.sh"
   cmd.eInstallScriptNameOOo  ="./installOOo.sh"
   cmd.eUninstallScriptNameOOo="./uninstallOOo.sh"
--   cmd.eSudo='sudo ./%cmd%'      -- replace %cmd% with shell-script
   cmd.eRM="rm -fv"              -- remove command
   cmd.eScriptExtension=".sh"
   cmd.eRexxJ="./rexxj.sh"

   cmd.eMkDir755 = "mkdir -v -m u=rwx,go=rx"   /* verbose, created directories have umask 755 */

/* ---> new strategy: symbolically link matchitng "libjvm.so" to "/opt/BSF4ooRexx" where it will
                      be looked for fist, then "libjvm.so" is searched without path
      -- make sure we have Java setup and '/usr/lib/libjvm.so' available, unless we are uninstalling
   if SysFileExists("/usr/lib/libjvm.so")=.false, runType<>'U' then -- no Java setup as of yet?
   do
      say cmd.eThisName': "/usr/lib/libjvm.so" is missing, hence it needs to get set up, thereofre running "setupJava.rex" ...'
      call "setupJava.rex"    -- all Rexx program
      if result<>0 then       -- oops, there is a problem, so quit this run
      do
         if cmd.eBInteractive=.true then
         do
            say cmd.eThisName": about to exit program, please hit enter to continue ..."
            parse pull input
         end
         exit result
      end
      say cmd.eThisName': if you wish to change the Java version BSF4ooRexx programs pick up, just re-run "setupJava.rex" ...'
   end
<--- */

-- cp -rfv * /opt/BSF4ooRexx

-- rm -rfv /opt/BSF4ooRexx
-- rmdir --ignore-fail-on-non-empty /etc/skel/Templates

-- 'id rony | rxqueue' ; parse pull 'gid=' . '(' gname ')';


end
cmd.eBInstallBSF=.true    -- start out to install BSF
cmd.eBInstallOOo=.true    -- start out to install OOo

if cmd.eBDebug=.true then
do
   say pp("cmd.eOpSys", cmd.eOpSys)
   say pp("cmd.eOpSysFull", cmd.eOpSysFull)
   say pp("cmd.eDefaultTargetDir", cmd.eDefaultTargetDir)
end

cmd.eThisDir=directory()   -- get current directory from which this script got started

-- process arguments: setupAllAndRun.rex [install [targetDir]] | reinstall | uninstall [keep]
targetDir=strip(targetDir)
if runType='I', targetDir="" then   -- second argument is optional target dir
do
   targetDir=cmd.eDefaultTargetDir         -- assign default target directory

   if SysFileExists(targetDir) then
   do
       say cmd.eThisName': target directory ['targetDir'] exists already, aborting...'
       if cmd.eBInteractive=.true then
       do
          say cmd.eThisName": about to exit program, please hit enter to continue ..."
          parse pull input
       end
       exit -3
   end
end
else if runType='U' then
do
    cmd.eSwitch=targetDir~left(1)~upper   -- get first letter in uppercase
    if cmd.eSwitch="" then                -- default to "R[emove]"
       cmd.eSwitch="R"
    else if pos(cmd.eSwitch, "KR")=0 then
    do
       say cmd.eThisName': unknown reinstall switch ['targetDir'], must be "K[eep]" or "R[emove]" (default) or empty, aborting...'

       if cmd.eBInteractive=.true then
       do
          say cmd.eThisName": about to exit program, please hit enter to continue ..."
          parse pull input
       end
       exit -4
    end
end


                        -- determine whether installation scripts exist already
cmd.eBInstallBSF=\SysFileExists(cmd.eThisDir || cmd.eFile.sep || cmd.eInstallScriptNameBSF)
cmd.eBInstallOOo=\SysFileExists(cmd.eThisDir || cmd.eFile.sep || cmd.eInstallScriptNameOOo)

bUninstallBSFExists=SysFileExists(cmd.eThisDir || cmd.eFile.sep || cmd.eUninstallScriptNameBSF)
bUninstallOOoExists=SysFileExists(cmd.eThisDir || cmd.eFile.sep || cmd.eUninstallScriptNameOOo)

   -- installation ------------------------------------------------------------------------
if runType="I" then     -- control installation, rc-values -10...-19
do
   if cmd.eBDebug=.true then say cmd.eThisName': debug mode, install branch with targetDir=['targetDir'] ...'

   if cmd.eBInstallBSF=.false, cmd.eBInstallOOo=.false then  -- already installed, don't do anything
   do
       say cmd.eThisName' (install-mode): installation scripts exist already, aborting... (you may run this program as root/Administrator with the "reinstall" switch)'

       if cmd.eBInteractive=.true then
       do
          say cmd.eThisName": about to exit program, please hit enter to continue ..."
          parse pull input
       end
       exit -11
   end

   if cmd.eBInstallBSF=.true then   -- o.k. we need to install from scratch
   do
/* -- try to install OOo-support even for non-genuine versions of OOo
       if cmd.eOpSys="U" then
       do
          -- check whether genuine OOo installation present
          call SysFileTree "/opt/openoffice*", "oooDirs.", "DO" /* query whether openoffice-directories exist in "opt" */
          if oooDirs.0=0 then
          do
             cmd.eBInstallOOo=.false      -- do not automatically install OOo support
             say cmd.eThisName": Please note: rerun ""install.sh"", if you want to install the special BSF4ooRexx OpenOffice.org support"
             say cmd.eThisName"  (make sure that you have the original version from 'http://www.OpenOffice.org' installed on your system!)"
             say
          end
       end
       else             -- Windows: usually only the official version from OOo installed, hence go ahead
*/
       cmd.eBInstallOOo=.true       -- try to install OOo support afterwards

       if cmd.eOpSys="U" then
          call setChmod cmd., .true    -- .true: entire installation package, .false: *.sh in current directory only

       if targetDir<>"." then    -- copy entire installation package to target Dir
       do
          newInstallDir=copyPackage(cmd., targetDir)
          if datatype(newInstallDir, "Whole") then -- was a return code returned instead of the new installation dir ?
          do

             if cmd.eBInteractive=.true then
             do
                say cmd.eThisName": about to exit program, please hit enter to continue ..."
                parse pull input
             end
             exit newInstallDir  -- abort installation with the received return code
          end

          oldDir=directory(newInstallDir) -- change to new install dir

          if cmd.eOpSys="U" then    -- make sure the flags are setup the way we need them
          do
             call setChmod cmd., .true -- .true: entire installation package, .false: *.sh in current directory only
          end
          else    -- Windows, make sure we switch to correct drive letter, if that has changed
          do
              if oldDir~left(2)~upper<>newInstallDir~left(2)~upper, newInstallDir~substr(2,1)=":" then
              do
                 cmd=newInstallDir~left(2)
                 if cmd.eBDebug=.true then
                    say pp("about to change drive to:", cmd)
                 else
                    cmd             -- let shell execute command
              end
          end
       end
   end

   if cmd.eBInstallBSF=.true then   -- create and run BSF install scripts
   do
      res=createAndRunInstallationScripts(cmd., "install", cmd.eSetupBSF, cmd.eInstallScriptNameBSF, -12, -13)
      if res<>0 then
      do
         if cmd.eBInteractive=.true then
         do
            say cmd.eThisName": about to exit program, please hit enter to continue ..."
            parse pull input
         end
         exit res
      end
   end

   if cmd.eBInstallOOo=.true then   -- create and run OOo install scripts
   do
      res=createAndRunInstallationScripts(cmd., "install", cmd.eSetupOOo, cmd.eInstallScriptNameOOo, -14, -15)
      if res<>0 then
      do
         if cmd.eBInteractive=.true then
         do
            say cmd.eThisName": about to exit program, please hit enter to continue ..."
            parse pull input
         end
         exit res
      end
   end

   title="BSF4ooRexx - Install"
   msg="Install was carried out (this dialog uses BSF4ooRexx) !"
   lf="0a"x

   if cmd.eOpSys<>"W", \cmd.eIsMacOSX then   -- neither under Windows, nor Mac
   do
      msg=msg lf -
              lf"Please logoff/logon to make sure that the BSF4ooRexx application menu gets rebuilt." -
              lf -
              lf"(Otherwise look for the BSF4ooRexx menu options in the 'Applications' menu 'Other' and"             -
              lf"run the BSF4ooRexx programs by right-clicking and picking the appropriate launcher.)"
   end
   msg=msg lf lf"(This very dialog is written in Rexx using Java for an operating system independent GUI dialog!)"
   msg=msg~changeStr(lf,"/LF/")

   if cmd.eBQuiet=.false then
   do
      res=showMessageBoxViaBSF(cmd., msg, title, "information")   -- try to bring up a dialog to inform user
      if res=.true then bPauseAtEnd=.false
   end
end


   -- uninstallation ----------------------------------------------------------------------
else if runType="U" then   -- control uninstallation, rc-values -20...-29
do
   if cmd.eBDebug=.true then say cmd.eThisName': debug mode, un-install branch with switch=['cmd.eSwitch'] ...'

   bProceed=.true          -- default: proceed with uninstalling

   if cmd.eBQuiet=.false then
   do
      if cmd.eSwitch="R" then -- attention: remove (delete) from system is chosen!
      do
         say "This will uninstall and remove (delete!) ""BSF4ooRexx"" from your system."
         say "Do you wish to uninstall and delete ? (yes/NO=default)"
         parse upper pull answer .     -- get user's answer
         bProceed=(answer~left(1)="Y")
         if bProceed=.false then -- do not uninstall !
         do
            say cmd.eThisName": you do not want to uninstall and delete ""BSF4ooRexx"", hit enter to return ..."
            parse pull answer
            exit -20
         end
      end
   end

   if cmd.eBDebug<>.true, bProceed=.true then -- not in debug mode, execute generated uninstall script
   do
      if SysFileExists(cmd.eUninstallScriptNameOOo)=.true then -- uninstall OOo support, if present
      do
         if cmd.eBDebug=.true then
            say cmd.eThisName': debug mode, would run ['cmd.eUninstallScriptNameOOo'] ...'
         else
            cmd.eUninstallScriptNameOOo   -- let the shell execute the script
      end

      if SysFileExists(cmd.eUninstallScriptNameBSF)=.true then -- uninstall OOo support, if present
      do
         if cmd.eBDebug=.true then
            say cmd.eThisName': debug mode, would run ['cmd.eUninstallScriptNameBSF'] ...'
         else
            cmd.eUninstallScriptNameBSF   -- let the shell execute the script
      end

      if cmd.eSwitch="R" then          -- remove all BSF4ooRexx package files
      do
         if cmd.eBDebug=.true then
            say cmd.eThisName': debug mode, would run [removeInstallationDir(cmd.)] ...'
         else
         do
            res=removeInstallationDir(cmd.)
            if res<>0 then
            do
               if cmd.eBInteractive=.true then
               do
                  say cmd.eThisName": about to exit program, please hit enter to continue ..."
                  parse pull input
               end
               exit res
            end
         end
      end
   end
end

   -- reinstallation ----------------------------------------------------------------------
else -- runType="R"     -- control re-installation, rc-values -30...-39
do
   if cmd.eBDebug=.true then say cmd.eThisName': debug mode, re-install branch ...'
      -- remove currently installed packages
   if SysFileExists(cmd.eUninstallScriptNameOOo)=.true then -- uninstall OOo support, if present
   do
      if cmd.eBDebug<>.true then
         cmd.eUninstallScriptNameOOo   -- let the shell execute the script
      call removeInstallationScripts cmd., "OOo"
   end

   if SysFileExists(cmd.eUninstallScriptNameBSF)=.true then -- uninstall OOo support, if present
   do
      if cmd.eBDebug<>.true then
         cmd.eUninstallScriptNameBSF   -- let the shell execute the script
      call removeInstallationScripts cmd., "BSF"
   end

      -- create and run install scripts
   res=createAndRunInstallationScripts(cmd., "reinstall", cmd.eSetupBSF, cmd.eInstallScriptNameBSF, -32, -33)
   if res<>0 then
   do
      if cmd.eBInteractive=.true then
      do
         say cmd.eThisName": about to exit program, please hit enter to continue ..."
         parse pull input
      end
      exit res
   end

   res=createAndRunInstallationScripts(cmd., "reinstall", cmd.eSetupOOo, cmd.eInstallScriptNameOOo, -34, -35)
   if res<>0 then
   do
      if cmd.eBInteractive=.true then
      do
         say cmd.eThisName": about to exit program, please hit enter to continue ..."
         parse pull input
      end
      exit res
   end

   title="BSF4ooRexx - Reinstall"
   msg="Reinstall was carried out (this dialog uses BSF4ooRexx) !"
   lf="0a"x
   if cmd.eOpSys<>"W", \cmd.eIsMacOSX then   -- neither under Windows, nor Mac
   do
      msg=msg lf -
              lf"Please logoff/logon to make sure that the BSF4ooRexx application menu gets rebuilt." -
              lf -
              lf"(Otherwise look for the BSF4ooRexx menu options in the 'Applications' menu 'Other' and"             -
              lf"run the BSF4ooRexx programs by right-clicking and picking the appropriate launcher.)"
   end
   msg=msg lf lf"(This very dialog is written in Rexx using Java for an operating system independent GUI dialog!)"
   msg=msg~changeStr(lf,"/LF/")

   if cmd.eBQuiet=.false then
   do
      res=showMessageBoxViaBSF(cmd., msg, title, "information")   -- try to bring up a dialog to inform user
      if res=.true then bPauseAtEnd=.false
   end
end

if bPauseAtEnd=.true then    -- pause at end, such that user can
do
   say cmd.eThisName": about to close program, please hit enter to continue ..."
   parse pull input
end

exit 0

::routine removeInstallationScripts    -- remove generated (un-)installation scripts (for regenerating them)
  use arg cmd., type

  if cmd.eBDebug<>.true then
     say "*** (debug) removeInstallationScripts, type=["type"]..."

  type=type~upper                   -- make sure type is in uppercase
  do name over cmd.eGeneratedScripts.type -- fetch matching array
     cmd=cmd.eRm name || "*" || cmd.eScriptExtension
     if cmd.eBDebug=.true then      -- display generated command
        say "*** (debug), cmd=["cmd"]"
     else
        cmd                         -- execute command
  end
  return


::routine createAndRunInstallationScripts -- routine to create and run BSF or OOo install scripts
   use strict arg cmd., mode, createInstallScript, runInstallScript, rc1, rc2

   if cmd.eBDebug=.true then
   do
      say cmd.eThisName": (debug) createAndRunInstallationScripts, mode=["mode"], createInstallScript=["createInstallScript"], runInstallScript=["runInstallScript"] ..."
      return 0
   end

say cmd.eThisName": (debug) createAndRunInstallationScripts, mode=["mode"], createInstallScript=["createInstallScript"], runInstallScript=["runInstallScript"] ..."

   call (createInstallScript)
   if result<>"RESULT", result<>0 then       -- something went wrong, abort
   do
       say cmd.eThisName' ('mode'-mode): running ['createInstallScript'] caused return code ['result'], aborting...'
       exit rc1
   end

   if cmd.eOpSys="U" then     -- on Unix set execution flags for freshly generated .sh scripts
      call setChmod cmd., .false   -- .false: *.sh in current directory only

   runInstallScript        -- let the shell execute the generated script
   if rc<>"RC", rc<>0 then
   do
      say cmd.eThisName' ('mode'-mode): running ['runInstallScript'] caused return code ['result'], aborting...'
      exit rc2
   end

   return 0       -- indicate everything went o.k.



::routine copyPackage   -- will copy the BSF4ooRexx package to the given location, returns new install dir name
  use strict arg cmd., targetDir

  if cmd.eOpSys='W' then   -- Windows
  do
      if targetDir~right(1)<>'\' then
         targetDir=targetDir'\'
      cmd='xcopy /s /r /k /y ..\*' enQQ(targetDir)
  end
  else      -- Unix
  do
      if targetDir~right(1)<>'/' then
         targetDir=targetDir'/'

      -- make sure directories exist, before copying
      tmpDir=targetDir
      newDir=""
      do while tmpDir<>""
         parse var tmpDir dirName "/" tmpDir    -- first run dirName="" as nothing before first "/"
         if newDir="" then newDir="/"
                      else newDir=newDir"/"dirName

         if cmd.eBDebug=.true then say "*** SysFileExists("newDir")=" SysFileExists(newDir)

         if SysFileExists(newDir)=.false then  -- dreate directory, if necessary
         do
            cmd = cmd.eMkDir755 newDir
            say cmd.eThisName" (install-mode): about to run '"cmd"' ..."
            cmd
         end
      end

      cmd='cp -rfv ../*' enQQ(targetDir)
  end

  say cmd.eThisName" (install-mode): about to run '"cmd"' ..."
  if cmd.eBDebug<>.true then
  do
     cmd
     if rc<>0 then
     do
        say cmd.eThisName" (install-mode): something went wrong, rc='"rc"', aborting ..."
        exit rc
     end
  end

  return targetDir || "install"  -- return new install dir name


::routine removeInstallationDir  -- remove the installation directory, i.e. parent directory plus all subdirectories
   use strict arg cmd.

   if cmd.eBDebug=.true then
      say cmd.eThisName': debug mode, in [removeInstallationDir(cmd., 'cmd.eThisDir')] ...'

   if cmd.eThisDir~right(7)<>'install' then    -- we must be started from the 'install' directory, otherwise stop for security reasons
   do
      say cmd.eThisName' (uninstall-mode): panic - current directory ['cmd.eThisDir'] is not spelled "install", aborting...'
      exit -31
   end

   parentDir=directory("..")  -- go up one level
   if SysFileExists("BSF.CLS")=.false then
   do
      say cmd.eThisName' (uninstall-mode): panic wrong directory ? - current directory ['parentDir'] does not contain the file "BSF.CLS", aborting...'
      exit -32
   end

   if parentDir<>cmd.eDefaultTargetDir then
   do
      say cmd.eThisName' (uninstall-mode): panic wrong directory ? - current directory ['parentDir'] is not in expected place (['cmd.eDefaultTargetDir']), not allowed to remove it via script, aborting...'
      exit -33
   end

   upperDir=directory("..")   -- go up one more level, then remove
   if cmd.eOpSys='W' then     -- Windows ?
       cmd="rd /s /q" enQQ(cmd.eDefaultTargetDir)  -- delete subdirectory recursively
   else
       cmd="rm -rfv" cmd.eDefaultTargetDir   -- delete subdirectory recursively

   say cmd.eThisName' (uninstall-mode): about to run "'cmd'" ...'
   if cmd.eBDebug<>.true then
   do
      cmd                     -- let the shell execute
      if rc<>0 then exit rc
   end
   return 0



::routine pp
  parse arg description, val
  return description~left(25,'.') "["val"]"


-- return bitness of Rexx interpreter (e.g. 32, 64, etc.)
::routine getRexxBitness

  -- do not use ADDRESS CMD as on Linux this causes an error :(
   "rexx -v 2>&1 | rxqueue"
   bitness=.nil                  -- default value
   do while queued()>0           -- make sure we process entire queue (to empty it)
      parse pull hint ":" val .
      if hint="Addressing Mode" then bitness=val
   end
   return bitness


::routine setChmod   -- on Unix systems make sure that we set the file flags accordingly
   use strict arg cmd., bAllFiles=.true

   if cmd.eBDebug=.true then
   do
      say cmd.eThisName': setChmod, bAllFiles=[' || bAllFiles || '], ...'
      return
   end

   if bAllFiles=.true then
   do
      "chmod 755 ../.."                         /* 755 on "bsf4oorexx"-directory                      */
      "find .. -type f -exec chmod 644 '{}' \;" /* make all files rw-r--r--                           */
      "find .. -type d -exec chmod 755 '{}' \;" /* 755 make all directories traversable; rwx-r-xr-x   */

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
   else  /* just make sure that all (possibly newly created) .sh-files in the current directory get set to: 755 */
   do
       'find . -iname "*.sh" -type f -exec chmod 755 ''{}'' \;'
   end
   return


::routine enQQ       -- enclose string in double quotes
  parse arg str
  return '"' || str || '"'


::routine showMessageBoxViaBSF
  use strict arg cmd., msg, title, type="information"

  signal on any
  deli=enQQ("/=/")     -- define delimiter
  cmd=cmd.eRexxJ "msgBox.rxj" enQQ(msg || deli || title || deli || type)
  cmd                -- let environment carry out the command
  return .true       -- indicate success

any:                 -- something went wrong while executing the program
  return .false      -- indicate error
