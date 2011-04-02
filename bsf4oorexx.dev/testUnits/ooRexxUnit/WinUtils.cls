#!/usr/bin/rexx
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
/*
   name:             WinUtils.cls
   authors:          Mark Miesfeld
   last update:      2007-11-24

   purpose:          Provides public routines and utility classes to aid in the
                     creation of test units that are Windows specific.  (The
                     test cases are for Windows specific portions of the ooRexx
                     package.)

   remarks:          This file fits into the ooRexxUnit framework as part of the
                     framwork's primary use by the ooRexx development team to
                     test the ooRexx interpretor.  It may be of limited use
                     outside of that role.

   category0:        ooRexxUnit
   category1:        framework
*/


/* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -*\
  Directives, Classes, or Routines.
\* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -*/
::requires "winsystm.cls"

/* class: ProgramLauncher- - - - - - - - - - - - - - - - - - - - - - - - - - -*\
    A class used to start an ooRexx program in a separate thread.
\* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -*/
::class 'ProgramLauncher' public

  -- Attribute: prgResultCode
  -- Holds the return code when the program ends
  ::method  prgResultCode attribute

  ::method init
    expose prgName params finished
    use arg prgName, arguments
    self~init:super
    finished = .false

    -- Stupid Windows and its stupid spaces in path names.
    prgName = prgName~strip
    if prgName~pos(' ') <> 0, prgName~left(1) \== '"' then prgName = .TestUtil~enQuote(prgName)

    if arg(2, 'O') then params = ""
    else params = arg(2)

  /** getResult()
   * A blocking method that waits until the launcher program finishes and then
   * returns the program's result code.  (The return code from the launched
   * program.)
   */
  ::method getResult unguarded
    expose finished
    guard on when finished
    return self~prgResultCode

  ::method startProgram
    expose prgName params finished

    reply 0

    'rexx' prgName params
    self~prgResultCode = RC
    finished = .true

::class 'WindowFinder' public
  ::attribute repetitions get
  ::attribute repetitions set private

  ::attribute sleepTime get
  ::attribute sleepTime set private

  ::method init
    expose windowMgr

    windowMgr = .WindowsManager~new
    self~repetitions = 20
    self~sleepTime   = .1

  ::method getWindow
    expose windowMgr
    use arg title, loops, pause

    if arg(2, 'O') then loops = self~repetitions
    if arg(3, 'O') then pause = self~sleepTime

    do loops
      j = SysSleep(pause)
      wnd = windowMgr~find(title)
      if wnd~class == .WindowObject then leave
    end

  return wnd

  /** setPause()
   * Sets the default pause for this window finder to that specified
   */
  ::method setPause
    use strict arg pause  -- Need to add arg checking
    self~sleepTime = pause

  /** setRepitions()
   * Sets the default repetitions for this window finder to that specified
   */
  ::method setRepetitions
    use strict arg loop  -- Need to add arg checking
    self~repetitions = loop


/* displayWindowChildren( windowObj )- - - - - - - - - - - - - - - - - - - - -*\

  Utility routine to display information about the child windows of the
  specified window.  This is useful for getting the needed information about
  windows, such as buttons, so that they can be manipulated.

  Note:
    This function would not, normally, be used in a test unit.  It is a utility
    to aid in the development of test units where the Windows GUI is involved.

  Input:
    windowObj REQUIRED
      A WindowObject instance that represents the parent window of the children
      windows.

  Returns:
    -1 if windowObject is not the proper class, otherwise the count of children
    windows.
\* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -*/
::routine  displayWindowChildren public
  use strict arg windowObj

  if windowObj~class <> .WindowObject then do
    say "Error not a WindowObject.  Can not display children."
    return -1
  end

  enum. = windowObj~enumerateChildren
  say 'The' windowObj~title "window has" enum.0 'children'

  do i = 1 to enum.0
    say 'Child number:' i
    say '  Title:          ' enum.i.!title
    say '  Window Class    ' enum.i.!class
    say '  Window ID       ' enum.i.!id
    say '  Window State:   ' enum.i.!state
    say '  Count children: ' enum.i.!children
    say '  Window handle:  ' enum.i.!handle
    say '  Window position:' enum.i.!coordinates
    say
  end

return enum.0;
-- End displayWindowChildren( windowObj )
