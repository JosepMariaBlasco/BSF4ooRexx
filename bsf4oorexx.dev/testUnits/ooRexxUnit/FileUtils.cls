#!/usr/bin/rexx
/*
  SVN Revision: $Rev: 3047 $
  Change Date:  $Date: 2008-08-22 19:56:58 -0700 (Fri, 22 Aug 2008) $
*/
/*----------------------------------------------------------------------------*/
/*                                                                            */
/* Copyright (c) 2007-2008 Rexx Language Association. All rights reserved.    */
/*                                                                            */
/* This program and the accompanying materials are made available under       */
/* the terms of the Common Public License v1.0 which accompanies this         */
/* distribution. A copy is also available at the following address:           */
/* http://www.oorexx.org/license.html                                         */
/*                                                                            */
/* Redistribution and use in source and binary forms, with or                 */
/* without modification, are permitted provided that the following            */
/* conditions are met:                                                        */
/*                                                                            */
/* Redistributions of source code must retain the above copyright             */
/* notice, this list of conditions and the following disclaimer.              */
/* Redistributions in binary form must reproduce the above copyright          */
/* notice, this list of conditions and the following disclaimer in            */
/* the documentation and/or other materials provided with the distribution.   */
/*                                                                            */
/* Neither the name of Rexx Language Association nor the names                */
/* of its contributors may be used to endorse or promote products             */
/* derived from this software without specific prior written permission.      */
/*                                                                            */
/* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS        */
/* "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT          */
/* LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS          */
/* FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT   */
/* OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,      */
/* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED   */
/* TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,        */
/* OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY     */
/* OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    */
/* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS         */
/* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.               */
/*                                                                            */
/*----------------------------------------------------------------------------*/

/** FileUtils.cls
 *
 * Provides common public routines and utility classes to do file system related
 * tasks, to make writing test units easier.
 */


/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -*\
  Directives, Classes, or Routines.
\* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -*/
::requires 'OOREXXUNIT.CLS'


/** issueCmd()
 * Issues a command and returns its output and return code.
 *
 * @param cmd     REQUIRED
 *   The command to issue.  This should include any arguments to the command.
 *
 * @param output  REQUIRED [In/Out]
 *   An array.  The output produced by issuing the command is returned in this
 *   array.  The output is appended to the array.
 *
 * @return  Returns the return code produced by issuing the command.  However,
 *          9999 is returned as an error indication.
 */
::routine issueCmd public
  use strict arg cmd, output

  if \ output~isInstanceOf(.array) then return 9999

  -- This is only known to be good for AIX, Linux and Windows.  When issued under
  -- another OS, it is likely to fail.  The select is used so that the proper
  -- code can be placed here when it does fail.
  select
    when .ooRexxUnit.OSName == "WINDOWS" then stdErrToStdOut = '2>&1'
    when .ooRexxUnit.OSName == "LINUX" then stdErrToStdOut = '2>&1'
    when .ooRexxUnit.OSName == "AIX" then stdErrToStdOut = '2>&1'
    otherwise stdErrToStdOut = '2>&1'
  end

  tmpOutFile = 'tmpXXX_delete.me'

  cmd = cmd '>' tmpOutFile stdErrToStdOut
  cmd
  prgRet = RC

  fsObj = .stream~new(tmpOutFile)
  tmpArray = fsObj~arrayin
  do line over tmpArray
    output~append(line)
  end
  fsObj~close

  j = deleteFile(tmpOutFile)

return prgRet


/** findBuildDir()
 *
 * Determines the top-level directory in the ooRexx build source tree.
 *
 * This function works on the assumption that the test suite is being executed
 * from a developer's build directory.  There are a couple of variables that
 * can be used to over-ride that assumption.  But, if those variables are not
 * set then this function will only succeed when the assumption is true.
 */
::routine findBuildDir public

  -- When the -Dname=value arg is added to testOORexx then it will over-ride
  -- all else.  TODO check that this is the correct name.
  if .local~hasEntry("OOREXX_BUILD_HOME") then return .OOREXX_BUILD_HOME

  -- Setting the variable in the environment will be next in the order of
  -- precedence.  TODO change the REXX_BUILD_HOME var in the make files.
  dir = value("OOREXX_BUILD_HOME", , 'ENVIRONMENT')
  if dir \== "" then return dir

  -- Change directory to the directory the test suite was started from.  If we
  -- have an error, just return.  On error the current directory will not have
  -- been changed.
  currentDir = directory()
  if directory(.ooTest.originalWorkingDir) == "" then return .nil

  dir = .nil
  currentOS = .ooRexxUnit.OSName

  -- As new OSes are added to the list of OSes that the test suite runs on, this
  -- select will need to be expanded.
  select label FIND_BUILD_DIR_LABEL
    when currentOS == 'WINDOWS' then do
      -- On Windows check for SRC_DRV and SRC_DIR
      srcDrv = value("SRC_DRV", , 'ENVIRONMENT')
      srcDir = value("SRC_DIR", , 'ENVIRONMENT')
      if srcDrv \== "", srcDir \== "" then do
        tmpDir = srcDrv || srcDir
        if SysIsFileDirectory(tmpDir) then do
          dir = tmpDir
          leave FIND_BUILD_DIR_LABEL
        end
      end

      -- Assume
      tmp = directory("..\")
      if tmp \== "" then dir = directory()
    end

    when currentOS == 'LINUX' | currentOS = 'AIX' then do
      -- On AIX and Linux we could be in the root to begin with, or in .libs
      if .ooTest.originalWorkingDir~right(6) == "/.libs" then do
        tmp = directory("../")
        if tmp \== "" then dir = directory()
      end
      else do
        dir = directory()
      end
    end

    otherwise do
      nop
    end
  end FIND_BUILD_DIR_LABEL
  -- End select

  -- Restore the current directory.
  j = directory(currentDir)

return dir
-- End findBuildDir()

/** findInstallDir()
 *
 * Determines if ooRexx is installed on the current system and returns the root
 * directory of the installation.  Returns .nil if the install directory can not
 * be determined.
 *
 * @return  The fully-qualified path to the install directory on success,
 *          otherwise .nil
 *
 * @note    This is a 'best attempt' function.  If .nil is returned, it
 *          indicates ooRexx is probably not installed, not that ooRexx for
 *          sure is not installed.
 */
::routine findInstallDir public

  dir = .nil
  currentOS = .ooRexxUnit.OSName

  -- Notes for extending this on new platforms.  For unix-like platforms, if
  -- the generic findInstallDirOnUnix() can be slightly tweaked to work for the
  -- platform, then do that.  If the generic function is not sufficient, then
  -- add a platform specific function, like maybe findInstallDirOnBSD().  If
  -- the platform is not unix-like, then a new function will probably be needed.

  select
    when currentOS == "WINDOWS" then do
      -- On Windows, the install package sets REXX_HOME.
      home = value("REXX_HOME", , 'ENVIRONMENT')

      -- Verify (to some degree) that this is the correct install directory.
      if home \== "" then do
        if SysIsFileDirectory(home) then do
          if SysIsFile(home"\uninstall.exe") then dir = home
        end
      end
    end

    when currentOS == "LINUX" | currentOS == "AIX" then do
      dir = findInstallDirOnUnix()
    end

    otherwise nop
  end
  -- End select

return dir
-- End findInstallDir()

/** locateSamplePrg()
 * Locates a sample program normally shipped with the ooRexx distribution.
 *
 * The sample is searched for in the location(s) for samples programsin a normal
 * install.  If it is not found, an attempt to find it using the assumption that
 * the test is being executed from within a build directory.  This second search
 * allows the test suite to be run by a developer from his build directory
 * without having a regular install.
 *
 * @param   name REQUIRED
 *            The name of the sample program.
 *
 * @return  The complete path name of the sample if it is located, otherwise
 *          .nil.
 */
::routine locateSamplePrg public
  use strict arg name

  retObj = .nil

  currentOS = .ooRexxUnit.OSName
  installDir = findInstallDir()

  -- When the test suite begins running on a new OS, then another when clause
  -- will need to be added to the select for that OS.  If the new OS is unix-
  -- like, then the generic locateSampleOnUnix() function can most likely be
  -- used.  It that is not possible, then write a OS specific function, like
  -- maybe locateSampleOnBSD().

  select label FINDSAMPLE
    when currentOS == "WINDOWS" then do
      if installDir \== .nil then do
        j = SysFileTree(installDir'\samples\'name, f., 'FOS')
        if j == 0, f.0 == 1, SysIsFile(f.1) then do
          retObj = f.1
          leave FINDSAMPLE
        end
      end

      buildRoot = findBuildDir()

      if buildRoot \== .nil then do
        sampleDir = buildRoot || "\samples\"

        if SysIsFileDirectory(sampleDir) then do
          -- Okay, we are good (in all probability.)
          j = SysFileTree(sampleDir || name, f., 'FOS')
          if j == 0, f.0 == 1, SysIsFile(f.1) then do
            retObj = f.1
            leave FINDSAMPLE
          end
        end
      end
    end

    when currentOS == "LINUX" | currentOS == "AIX" then do
      retObj = locateSampleOnUnix(name, installDir)
    end

    otherwise do
      nop
    end
  end findSample -- End select

return retObj
-- End locateSamplePrg()

::routine locateSampleOnUnix
  use strict arg name, installDir

  retObj = .nil

  if installDir \== .nil then do
    j = SysFileTree(installDir'/share/ooRexx/'name, f., 'FOS')
    if j == 0, f.0 == 1, SysIsFile(f.1) then do
      retObj = f.1
    end
  end

  if retObj == .nil then do
    -- Not found, see if we can get a build directory
    buildRoot = findBuildDir()

    if buildRoot \== .nil then do
      sampleDir = buildRoot || "/samples/"

      if SysIsFileDirectory(sampleDir) then do
        -- Okay, we are good (in all probability.)
        j = SysFileTree(sampleDir || name, f., 'FOS')
        if j == 0, f.0 == 1, SysIsFile(f.1) then do
          retObj = f.1
        end
      end
    end
  end

return retObj
-- End locateSampleOnUnix()

/** findInstallDirOnUnix()
 * Tries to locate the install directory on a Unix-like system using what is
 * known about how ooRexx gets installed on those systems.
 *
 * @return   The fully qualified directory path to an installed ooRexx on
 *           success, otherwise .nil.
 */
::routine findInstallDirOnUnix

  dir = .nil

  -- See if we can locate rexx.img using a package manager.
  select
    when haveRpm() then do
      imageFile = checkRpmForInstalledFile("rexx.img")
    end

    when haveDpkg() then do
      imageFile = checkDpkgForInstalledFile("rexx.img")
    end

    when haveLpp() then do
      imageFile = checkLppForInstalledFile("rexx.img")
    end

    otherwise imageFile = .nil
  end
  -- End select

  -- If no luck, try the default install location
  if imageFile == .nil then do
    imageFile = "/opt/ooRexx/bin/rexx.img"
  end

  if SysIsFile(imageFile) then do
    p = imageFile~pos("/bin/rexx.img")
    if p <> 0 then do
      return imageFile~left(p - 1)
    end
  end

  -- Still didn't find it.  Try one more thing.  The problem with this, is that
  -- if we find a rexx, we need to be sure it is ooRexx.  And, it is assuming
  -- the install set up a soft link.
  softLink = ""
  cmdOut = .array~new
  ret = issueCmd('which rexx', cmdOut)
  if ret == 0, cmdOut~items == 1 then do
    rexxFile = cmdOut[1]
    cmdOut~empty
    ret = issueCmd('ls -la' rexxFile, cmdOut)
    if ret == 0, cmdOut~items == 1 then do
      if cmdOut[1]~left(1) == 'l' then softLink = cmdOut[1]
    end
  end

  if softLink \== "" then do
    parse var softLink discard "->" rexxFile
    rexxFile = rexxFile~strip
    if rexxFile \== "" then do
      binDir = rexxFile~left(rexxFile~pos("rexx") - 1)
      if SysIsFile(binDir"/rxftp.cls"), SysIsFile(binDir"/rxregexp.cls") then do
        p = rexxFile~pos("/bin/rexx")
        if p <> 0 then do
          tmp = rexxFile~left(p - 1)
          if SysIsFileDirectory(tmp) then dir = tmp
        end
      end
    end
  end

return dir
-- End findInstallDirOnUnix()

::routine haveRpm public
  cmdOut = .array~new
  ret = issueCmd('which rpm', cmdOut)
  if ret == 0 then return .true
  else return .false

::routine haveDpkg public
  cmdOut = .array~new
  ret = issueCmd('which dpkg', cmdOut)
  if ret == 0 then return .true
  else return .false

::routine haveLpp public
  cmdOut = .array~new
  ret = issueCmd('which lslpp', cmdOut)
  if ret == 0 then return .true
  else return .false


/** checkRpmForInstalledFile()
 * Uses rpm to check that: 1.) ooRexx is installed by rpm, 2.) the specified
 * file is installed, and 3.) what the fully qualified path name of the file is.
 *
 * @param   name  The file name to check for.
 *
 @ @return  The fully qualified file name if ooRexx is installed and the file is
 @          installed as part of the ooRexx package, otherwise .nil.
 */
::routine checkRpmForInstalledFile public
  use strict arg name

  fileName = .nil
  cmdOut = .array~new
  ret = issueCmd('rpm -q ooRexx', cmdOut)

  if ret == 0 then do
    cmdOut = .array~new

    -- Escape the dot(s) in the file name
    escName = escapeDots(name)

    ret = issueCmd('rpm -q ooRexx --list | grep' escName, cmdOut)
    if cmdOut~items == 1 then fileName = cmdOut[1]
  end
  return fileName

/** checkDpkgForInstalledFile()
 * Uses the debian package manager, dpkg, to check that: 1.) ooRexx is installed
 * by dpkg, 2.) the specified file is installed, and 3.) what the fully
 * qualified path name of the file is.
 *
 * @param   name  The file name to check for.
 *
 @ @return  The fully qualified file name if ooRexx is installed and the file is
 @          installed as part of the ooRexx package, otherwise .nil.
 */
::routine checkDpkgForInstalledFile public
  use strict arg name

  fileName = .nil
  cmdOut = .array~new
  ret = issueCmd('dpkg -s oorexx', cmdOut)

  if ret == 0 then do
    cmdOut = .array~new

    -- Escape the dot(s) in the name
    escName = escapeDots(name)

    ret = issueCmd('dpkg -L oorexx | grep' escName, cmdOut)
    if cmdOut~items == 1 then fileName = cmdOut[1]
  end
  return fileName

/** checkLppForInstalledFile()
 *
 * We use lslpp to get the list of installed files.
 *
 * @param   name  The file name to check for.
 *
 @ @return  The fully qualified file name if ooRexx is installed and the file is
 @          installed as part of the ooRexx package, otherwise .nil.
 */
::routine checkLppForInstalledFile public
  use strict arg name

  fileName = .nil

  cmdOut = .array~new
  ret = issueCmd('lslpp -L ooRexx.rte', cmdOut)

  if ret == 0 then do
    cmdOut = .array~new

    -- Escape the dot(s) in the name
    escName = escapeDots(name)

    ret = issueCmd('lslpp -f ooRexx.rte | grep' escName, cmdOut)
    if cmdOut~items == 1 then fileName = strip(cmdOut[1])
  end
  return fileName

::routine escapeDots
  use strict arg name

  escName = ""
  tmp = name
  do which tmp~pos('.') <> 0
    parse var tmp front '.' tmp
    escName = escName || front || '\.'
  end

  return escName || tmp


/* createFile( src, name ) - - - - - - - - - - - - - - - - - - - - - - - - - -*\

  Writes out a file using the supplied source.

  Input:
    src   REQUIRED
      An array containing the lines to be written to the file.

    name  REQUIRED
      The name of the file to be written.

  Returns:
    The fully qualified name of the file on succes.  Returns the empty string
    on error.
\* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -*/
::routine createFile public
  use strict arg src, name

  fileName = ""
  fsObj = .stream~new(name)
  state = fsObj~open("WRITE REPLACE")
  if state~abbrev("READY") then do
    fsObj~arrayout(src)
    fsObj~close
    fileName = fsObj~qualify
  end

return fileName
-- End createFile( src, name )

/* Convenience method.  Calls createFile() with .rex tacked onto basename. */
::routine createRexxPrgFile public
  use strict arg src, baseName
return createFile(src, baseName || '.rex')

/* addToFile( src, name )- - - - - - - - - - - - - - - - - - - - - - - - - - -*\

  Appends to an existing file using the supplied source.

  Input:
    src   REQUIRED
      An array containing the lines to be added to the file.

    name  REQUIRED
      The name of the file being appended.

  Returns:
    The fully qualified name of the file on succes.  Returns the empty string
    on error.
\* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -*/
::routine addToFile public
  use strict arg src, name

  fileName = ""
  fsObj = .stream~new(name)
  state = fsObj~open("WRITE APPEND")
  if state~abbrev("READY") then do
    do line over src
      fsObj~lineout(line)
    end
    fsObj~close
    fileName = fsObj~qualify
  end

return fileName
-- End addToFile( src, name )

/* deleteFile( fileName )- - - - - - - - - - - - - - - - - - - - - - - - - - -*\

  Provides a platform independent file delete.  On some platforms, SysFileDelete
  does not force a deletion.  This function will force the deletion where
  possible.

  Input:
    fileName REQUIRED
      The file to delete.

  Returns:
    The operating system return code when the delete is done.
\* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -*/
::routine deleteFile public
  use strict arg fileName

  -- On AIX (ksh), Linux (bash) and Windows the delete can be forced.  Not sure
  -- on other OSes.  Windows files need to be quoted, some test cases use file
  -- names with spaces in them.  TODO, need to check behavior on other OSes.
  select
    when .ooRexxUnit.OSName == "WINDOWS" then do
      'del /q /f'  .TestUtil~enQuote(fileName) '1>nul 2>&1'
      ret = RC
    end
    when .ooRexxUnit.OSName == "LINUX" then do
      'rm -f' .TestUtil~enQuote(fileName) '>/dev/null 2>&1'
      ret = RC
    end
    when .ooRexxUnit.OSName == "AIX" then do
      'rm -f' .TestUtil~enQuote(fileName) '>/dev/null 2>&1'
      ret = RC
    end
    when .ooRexxUnit.OSName == "MACOSX" then do
      'rm -f' .TestUtil~enQuote(fileName) '>/dev/null 2>&1'
      ret = RC
    end
    otherwise ret = SysFileDelete(fileName)
  end

return ret
-- End deleteFile( fileName )

/* execRexxPrgWithArgs( prgName, params, output )- - - - - - - - - - - - - - -*\

  Executes a Rexx program using a separate instance of the interpreter.  This
  function captures the output and the return code from the executed program and
  returns it to the caller.

  Input:
    prgName REQUIRED
      The Rexx program to execute.

    params  REQUIRED
      The arguments to the Rexx program.  If the Rexx program has no arguments,
      either use the empty string, or use the convenience function execRexxPrg

    output  REQUIRED  [In / Out]
      An array object in which the the executed program's output is returned.
      The output lines are appended to the array, so the array does not need to
      be empty.

  Returns:
    The return code produced by executing the program on success.  Returns 9999
    for an internal error.
\* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -*/
::routine execRexxPrgWithArgs public
  use strict arg prgName, params, output

  cmd = 'rexx' .TestUtil~enQuote(prgName) params
  prgRet = issueCmd(cmd, output)

return prgRet
-- End execRexxPrgWithArgs( prgName, params, output )

/**
 * This is a convenience method to execute a Rexx program with no arguments.  It
 * delegates to execRexxPrgWithArgs().
 */
::routine execRexxPrg public
  use strict arg prgName, output
return execRexxPrgWithArgs(prgName, "", output)


/* createOleObject( id ) - - - - - - - - - - - - - - - - - - - - - - - - - - -*\

  Creates an .OLEObject instance, a proxy for the specified COM object.  This
  routine is used to trap the REXX error that happens when the proxied COM
  object can not be created.

  Input:
    id          REQUIRED
      The string used to create the COM object.  I.e., the ProgID or CLSID.

    withEvents  OPTIONAL
      If true, create the OLE object with events, otherwise without events.  The
      default is false.

    beVerbose   OPTIONAL
      If true and the OleObject is not created, the error message is displayed.
      If false, the default, the message is not displayed.

      Use this option for test case development only.  Tests run for an
      automated test should not produce output.

  Returns:
    An instance of .OLEObject on success, .nil on failure.
\* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -*/
::routine createOleObject public
  use strict arg id, withEvents = .false, beVerbose = .false

  if \ isBoolean(withEvents) then
    raise syntax 88.900 array ("withEvents must be set to true or false; found:" withEvents)

  if \ isBoolean(beVerbose) then
    raise syntax 88.900 array ("beVerbose must be set to true or false; found:" beVerbose)

  signal on syntax name returnNil

  if withEvents then oleObject = .OLEObject~new(id, "WITHEVENTS")
  else oleObject = .OLEObject~new(id, "NOEVENTS")

  return oleObject

returnNil:
  if beVerbose then do
    cObj = condition("O")
    say "Error" rc":    " errortext(rc)
    say "Code " cObj~code":" cObj~message
  end

  return .nil
-- End createOleObject( id, verbose )


/** testForOleObject()
 * Provides a quick check to see if there would be a problem creating an
 * OLEObject on the current system.  (For example if the OLE Automation
 * application is not installed.)
 *
 * @param id  REQUIRED
 *   The string used to create the OLE Automation object.  I.e., the ProgID or
 *   CLSID.
 *
 * @return  Returns 0 if there are no problems, otherwise the syntax error
 *          code produced by the failure to create the OLE Automation object.
 */
::routine testForOleObject public
  use strict arg id

  signal on syntax name returnCode

  oleObject = .OLEObject~new(id, "NOEVENTS")
  drop oleObject
  return 0

returnCode:
  return condition('O')~code
