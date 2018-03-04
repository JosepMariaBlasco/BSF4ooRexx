#!/usr/bin/env rexx
/* called by FXMLLoader on the "FX Application Thread" */

/* initialize JavaFX objects, define public routines for event handling */
fxml=.my.app~fxml_pb.fxml  -- get the corresponding FXML Rexx directory

   -- clear label fields
fxml~idLabelCurrent ~text=""
fxml~idLabelStart   ~text=""
fxml~idLabelEnd     ~text=""
fxml~idLabelDuration~text=""

::routine onActionButtonStart public   -- toggle button, start
--  slotDir=arg(arg())     -- supplied by BSF4ooRexx, not needed, but available
  if .my.app~fxml_pb.fxml~idButtonStart~text="Start" then
     .action~setRunning
  else
     .action~setStop

::routine onActionButtonExit  public   -- exit the application
  bsf.loadClass("javafx.application.Platform")~exit


/* This class allows communication of state with the worker and
   updating the GUI. Therefore its methods must be invoked on
   the "JavaFX Application Thread".
*/
::class Action       public
::attribute state    class    -- states: "idle", "running", "stop"
::method init        class
  expose state
  state="idle"                -- initialize to "idle"

::method setRunning  class    -- invoked by pressing "Start" button, starts worker
  expose state
  if state<>"idle" then return   -- worker runs already

  fxml=.my.app~fxml_pb.fxml      -- get access to JavaFX controls
  fxml~idButtonStart~disable=.true  -- do not let user interact with this control
  state="running"

  fxml~idButtonExit~disable=.true
  fxml~idLabelEnd~text     =""
  fxml~idLabelDuration~text=""
  fxml~idLabelCurrent~text =""

  now=.dateTime~new
  .my.app~fxml_pb.fxml~startedAt=now   -- save Rexx object
  fxml~idLabelStart~text   = now "(started)"
  fxml~idButtonStart~text="Stop"

   -- start worker object, supply this class object
  .my.app~worker~go(self)           -- supply this class object
  fxml~idButtonStart~disable=.false -- allow interaction again


::method setStop  class       -- invoked by pressing "Stop" button, stops worker
  expose state
  if state<>'running' then return  -- not running, cannot stop

  fxml=.my.app~fxml_pb.fxml         -- get access to JavaFX controls
  fxml~idButtonStart~disable=.true  -- do not let user interact with this control

  state="stop"                      -- worker will stop and invoke "setIdle" method
  fxml~idButtonStart~text="Stopping..."
  now=.dateTime~new
  fxml~stoppedAt=now    -- save Rexx object


::method setIdle  class       -- invoked by worker on "JavaFX Application Thread"
  expose state
  if wordpos(state,'running stop')=0 then return   -- not running, nor stopping, ignore

  fxml=.my.app~fxml_pb.fxml         -- get access to JavaFX controls
  fxml~idButtonStart~disable=.true  -- do not let user interact with this control

  now=.dateTime~new
  fxml~stoppedAt=now -- save Rexx object

  now                 =.dateTime~new
  fxml~idLabelEnd~text=now "(ended)"
  duration            =now - .my.app~fxml_pb.fxml~startedAt
  fxml~idLabelDuration~text=duration "(duration)"
  if state='stop' then     -- indicate user stopped
  do
     current=fxml~idLabelCurrent~text
     fxml~idLabelCurrent~text=current "(interrupted!)"
  end

  state="idle"                      -- communicate we can be started again
  fxml~idButtonStart~text="Start"
  fxml~idButtonStart~disable=.false -- allow interaction again
  fxml~idButtonExit~disable=.false
