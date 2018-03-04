#!/usr/bin/env rexx
/* this is the Rexx program that does the work and interacts
   with GUI elements using using .FxGUIThread~runLater() to
   make sure that the "JavaFX Application Thread" gets used;

*/

::requires "BSF.CLS"

::class Worker public

::method go
  use arg clzAction  -- get class object

  reply     -- return to caller, keep working on a separate thread
  fxml=.my.app~fxml_pb.fxml  -- get the corresponding FXML Rexx directory
  pb        =fxml~idProgressBar
  lblCurrent=fxml~idLabelCurrent

  do i=1 to 100 while clzAction~state="running"
      -- update GUI controls on the "JavaFX Application Thread"
     .FXGuiThread~runLaterLatest(pb, "setProgress", "individual", box("Double",i/100))
     .FXGuiThread~runLaterLatest(lblCurrent, "setText", "i", i "%")

      -- instead of sleeping, do the real work here!  <-- <-- <--
     call SysSleep 0.01    -- sleep 1/100 of a second
  end
   -- we need to send the message on the "JavaFX Application Thread"
  msg=.FXGuiThread~runLater(clzAction, "setIdle")
  res=msg~result  -- this blocks until message was executed
  return

