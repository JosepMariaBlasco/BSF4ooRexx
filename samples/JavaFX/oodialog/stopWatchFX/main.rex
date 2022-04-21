/*
   A JavaFX implementation of "ooRexx\samples\oodialog\examples\stopWatch.rex"
   (Windows only) that runs on Windows, but also on Linux and MacOS.

   ---rgf, 2022-04-14
*/

   -- change directory to program location such that relatively addressed resources can be found
parse source  . . pgm
call directory filespec('L', pgm)   -- change to the directory where the program resides

signal on syntax
rxApp=.RexxApplication~new -- create Rexx object that will control the FXML set up
jrxApp=BSFCreateRexxProxy(rxApp, ,"javafx.application.Application")
jrxApp~launch(jrxApp~getClass, .nil)    -- launch the application, invokes "start"
call sysSleep .1        -- sleep a bit to let ooRexx on Windows do some housekeeping :)

::requires "BSF.CLS"    -- get Java support

-- Rexx class implements the "javafx.application.Application" abstract method "start"
::class RexxApplication -- implements the abstract class "javafx.application.Application"

::method start          -- Rexx method "start" implements the abstract Java method
  use arg primaryStage  -- fetch the primary stage (window)

  signal on syntax      -- if something goes wrong, let us report it
  primaryStage~setTitle("StopWatchFX")

   -- create an URL for the FMXLDocument.fxml file (hence the protocol "file:")
  fxmlUrl=.bsf~new("java.net.URL", "file:stopWatchFX.fxml")
   -- use FXMLLoader to load the FXML and create the GUI graph from its definitions:
  rootNode=bsf.loadClass("javafx.fxml.FXMLLoader")~load(fxmlUrl)
  scene=.bsf~new("javafx.scene.Scene", rootNode)    -- create a scene for our document
  primaryStage~setScene(scene)  -- set the stage to our scene
  primaryStage~show             -- show the stage (and thereby our scene)
  return

syntax:                 -- if something goes wrong, let us report it
  co=condition('o')     -- get all condition information from ooRexx
  say ppJavaExceptionChain(co,.true)   -- show all Java exceptions leading to this one
  say "---"
  raise propagate       -- now let ooRexx report the syntax condition

