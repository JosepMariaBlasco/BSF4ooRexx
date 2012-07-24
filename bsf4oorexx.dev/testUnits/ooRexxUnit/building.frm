#!/usr/bin/rexx
/*
  SVN Revision: $Rev: 546 $
  Change Date:  $Date: 2009-10-20 20:45:19 +0200 (Tue, 20 Oct 2009) $
*/
/*----------------------------------------------------------------------------*/
/*                                                                            */
/* Copyright (c) 2008-2008 Rexx Language Association. All rights reserved.    */
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

/** building.frm
 * A framework containing public classes, functions, etc., to aid in the
 * building of external binaries needed for testing ooRexx.  This is part of
 * the overall ooTest framework.
 */


::requires 'ooTest.frm'
::requires 'FileUtils.cls'


/** buildBinaries()
 */
::routine buildBinaries public
  use strict arg testResult, force

  parse source . . fileSpec
  os = .ooRexxUnit.OSName
  rrc = .ooTestConstants~UNEXPECTED_ERR_RC

  select
    when os == "WINDOWS" then do
      rrc = buildWindows(testResult, force, fileSpec)
    end

    when os == "LINUX" | os == "AIX"  then do
      rrc = buildUnix(testResult, force, fileSpec)
    end

    -- As an example of how to add support for a new OS.  Add a when section for
    -- the OS.  Then add / implement the code to build on that OS.
    when os == "MACOS" then do
      err = .ExceptionData~new(timeStamp(), fileSpec, .ExceptionData~UNEXPECTED)
      err~setLine(.line - 2)
      err~severity = "Unrecoverable"
      err~msg = "Building external binaries on MACOS is not implemented."
      testResult~addException(err)
    end

    otherwise  do
      err = .ExceptionData~new(timeStamp(), fileSpec, .ExceptionData~UNEXPECTED)
      err~setLine(.line - 2)
      err~severity = "Unrecoverable"
      err~msg = "Building external binaries on" os "is not implemented."
      testResult~addException(err)
    end
  end
  -- End select

return rrc
-- End buildBinaries()

::routine buildWindows public
  use strict arg testResult, force, fileSpec

  argTable = .table~new
  argTable["oldHomeVal"] = .nil
  argTable["oldBuildVal"] = .nil

  rCode = locateAPIDir(testResult, fileSpec, argTable)
  if rCode \== .ooTestConstants~SUCCESS_RC then return rCode

  oldHomeVal = argTable["oldHomeVal"]
  oldBuildVal = argTable["oldBuildVal"]
  currentDir = directory()

  makeDir = .ooTest.dir"\external\API"
  makeFile = "Makefile.windows"
  makeLocation = makeDir"\"makefile

  oldBinVal = replaceEnvValue("OOTEST_BIN_DIR", .ooTest.dir"\bin\WINDOWS")
  j = directory(makeDir)

  if force then do
    cmd = "nMake /nologo /F" makeFile "clean"
    rCode = doMake(testResult, cmd, makeLocation, "Issuing make clean", fileSpec)
  end

  if rCode == .ooTestConstants~SUCCESS_RC then do
    cmd = "nMake /nologo /F" makeFile
    rCode = doMake(testResult, cmd, makeLocation, "Issuing make", fileSpec)
  end

  if oldHomeVal \== .nil then j = replaceEnvValue("REXX_HOME", oldHomeVal)
  if oldBuildVal \ == .nil then j = replaceEnvValue("OOREXX_BUILD_HOME", oldBuildVal)
  j = directory(currentDir)

return rCode
-- End buildWindows()


::routine buildUnix public
  use strict arg testResult, force, fileSpec

  argTable = .table~new
  argTable["oldHomeVal"] = .nil
  argTable["oldBuildVal"] = .nil

  rCode = locateAPIDir(testResult, fileSpec, argTable)
  if rCode \== .ooTestConstants~SUCCESS_RC then return rCode

  oldHomeVal = argTable["oldHomeVal"]
  oldBuildVal = argTable["oldBuildVal"]
  currentDir = directory()
  os = .ooRexxUnit.OSName

  makeDir = .ooTest.dir"/external/API"
  makeFile = "Makefile." || os~lower
  makeLocation = makeDir"/"makefile

  oldBinVal = replaceEnvValue("OOTEST_BIN_DIR", .ooTest.dir"/bin/" || os)
  j = directory(makeDir)

  -- For now we have to use GNU make from the AIX Toolbox because the AIX
  -- make does not support "ifdef"
  if os == "AIX" then baseCmd = "/opt/freeware/bin/make -f" makeFile
  else baseCmd = "make -f" makeFile

  if force then do
    cmd = baseCmd "clean"
    rCode = doMake(testResult, cmd, makeLocation, "Issuing make clean", fileSpec)
  end

  if rCode == .ooTestConstants~SUCCESS_RC then do
    rCode = doMake(testResult, baseCmd, makeLocation, "Issuing make", fileSpec)
  end

  if oldHomeVal \== .nil then j = replaceEnvValue("REXX_HOME", oldHomeVal)
  if oldBuildVal \ == .nil then j = replaceEnvValue("OOREXX_BUILD_HOME", oldBuildVal)
  j = directory(currentDir)

return rCode
-- End buildUnix()

/** locateAPIDir()
 * A private helper function used to locate where the API include and library
 * files are.  It then sets the proper environment macros used by the platform
 * make files to construct the correct compile and link commands.
 */
::routine locateAPIDir
  use strict arg testResult, fileSpec, argTable

  rCode = .ooTestConstants~SUCCESS_RC

  dir = findInstallDir()
  if dir \== .nil then do
    argTable['oldHomeVal'] = replaceEnvValue("REXX_HOME", dir)
  end
  else do
    dir = findBuildDir()
    if dir \== .nil then do
      argTable['oldBuildVal'] = replaceEnvValue("OOREXX_BUILD_HOME", dir)
    end
    else do
      err = .ExceptionData~new(timeStamp(), fileSpec, .ExceptionData~UNEXPECTED)
      err~setLine(.line)
      err~severity = "Unrecoverable"
      err~msg = "Failed to build external binaries"
      err~additional = "Could not locate either the ooRexx install or ooRexx build directories"
      testResult~addException(err)
      rCode = .ooTestConstants~BUILD_FAILED_RC
    end
  end

return rCode
-- End locateAPIDir()


/** doMake()
 * A private helper routine to do the actual chore of issuing a make command
 * and collecting the results.
 */
::routine doMake
  use strict arg testResult, makeCmd, location, msg, fileSpec

  note = .Notification~new(timeStamp(), location, .NotificationTypes~LOG_TYPE)
  output = .array~new

  line = .line + 1
  ret = issueCmd(makeCmd, output)

  note~message = msg
  note~reason = ret
  note~additional = makeCmd
  note~additionalObject = output

  retObj = note

  if ret <> 0 then do
    err = .ExceptionData~new(timeStamp(), fileSpec, .ExceptionData~EXTERNAL)
    err~setLine(line)
    err~severity = "Severe"
    err~msg = "Failed to build external binaries"

    note~message = msg "failed"
    err~additionalObject = note

    testResult~addException(err)
    rCode = .ooTestConstants~BUILD_FAILED_RC
  end
  else do
    testResult~addNotification(note)
    rCode = .ooTestConstants~SUCCESS_RC
  end

return rCode
-- End doMake()

