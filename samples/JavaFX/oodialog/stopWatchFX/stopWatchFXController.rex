#!/usr/bin/env rexx
/* Controller for stopWatchFX.fxml. */

/*
-- list Fonts available to JavaFX
do counter i f over bsf.loadClass("javafx.scene.text.Font")~getFamilies
   say i~right(3)":" pp(f)
end
*/

::requires BSF.CLS      -- get ooRexx-Java bridge

::routine menuCopy public     -- copy current elapsed time to clipboard
  use arg slotDir
  /* @get(lblElapsedTime) */
  clipboard=bsf.import("javafx.scene.input.Clipboard")~getSystemClipboard
  content  =.bsf~new("javafx.scene.input.ClipboardContent")
  content~putString(lblElapsedTime~text)
  clipboard~setContent(content)


::routine btnStart public     -- react upon button press
  use arg slotDir
  /* @get(btnStart btnExit lblElapsedTime) */
  currText=btnStart~text
  if currText="Start" then    -- set up
  do
     .ElapseTime~startDT=.dateTime~new -- record start time
     btnStart~text="Stop"
     btnExit~text="Split"
     .ElapseTime~startUpdating(lblElapsedTime)
  end
  else if currText="Resume" then
  do
     btnStart~text="Stop"
     .ElapseTime~startUpdating(lblElapsedTime)
  end
  else   -- "Stop": stop updating elapsed time
  do
     .ElapseTime~stopUpdating
     btnStart~text="Start"
     btnExit~text="Exit"
  end


::routine btnExit public      -- react upon button press
  use arg slotDir
  /* @get(btnStart btnExit lblElapsedTime) */
  if btnExit~text="Exit" then
     call exitApp
  else   -- "Split"
  do
     .ElapseTime~stopUpdating
     elapsed=(.dateTime~new-.elapseTime~startDT)~string  -- turn TimeSpan to string
     .FxGuiThread~runLaterLatest(lblElapsedTime, "setText", "i", elapsed~left(elapsed~length-3))
     btnStart~text="Resume"
  end


::routine exitApp public
  .elapseTime~stopUpdating    -- stop update thread if any
  bsf.loadClass('javafx.application.Platform')~exit   -- exit JavaFX application


/* Class that updates the elapsed time label. */
::class ElapseTime
::attribute startDT        class unguarded   -- start DateTime object
::method startUpdating     class unguarded   -- updates GUI
  expose  bUpdate startDT
  use arg lblElapsedTime
  bUpdate=.true            -- update label
  reply
  do while bUpdate=.true
     elapsed=(.dateTime~new-startDT)~string  -- turn TimeSpan to string
     .FxGuiThread~runLaterLatest(lblElapsedTime, "setText", "i", elapsed~left(elapsed~length-3))
     call syssleep 0.001   -- sleep 1/1000 second
  end

::method stopUpdating      class unguarded
  expose  bUpdate
  bUpdate=.false           -- stop updating
