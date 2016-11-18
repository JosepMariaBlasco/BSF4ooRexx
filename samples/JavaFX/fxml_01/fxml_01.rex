/* TODO: create another Rexx-Sample matching <http://docs.oracle.com/javafx/2/get_started/hello_world.htm>
*/

   -- create Rexx object that will control the FXML set up
rexxHandler=.rxDocHandler~new

   -- instantiate the abstract JavaFX class, the abstract "start" method will be served by rexxHandler
rxApp=BsfCreateRexxProxy(rexxHandler,,"javafx.application.Application")

   -- launch the application, invoke "start" and then stay up until the application closes
rxApp~launch(rxApp~getClass, .nil)     -- need to use this version of launch in order to work


::requires "BSF.CLS"    -- get Java support

/* implements the abstract method "start" for the Java class javafx.application.Application
   (BSF4ooRexx also supplies another (trailing) slotDir (a Rexx Directory) argument, as "start" is
   invoked from Java)
*/
::class RxDocHandler

   /* loads the fxml document defining the GUI elements, sets up a scene for it and shows it */
::method start    -- will be invoked by the "launch" method
  use arg stage   -- we get the stage to use for our UI

   -- create an URL for the FMXLDocument.fxml file (hence the protocol "file:")
  rootDocUrl=.bsf~new("java.net.URL", "file:FXML_01_Document.fxml")
  root=bsf.loadClass("javafx.fxml.FXMLLoader")~load( rootDocUrl  )   -- load the fxml document

  scene=.bsf~new("javafx.scene.Scene", root)    -- create a scene for our document
  stage~setScene(scene)                         -- set the stage to our scene
  stage~show                                    -- show the stage (and thereby our scene)
