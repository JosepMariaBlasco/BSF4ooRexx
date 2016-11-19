/* TODO: create another Rexx-Sample matching <http://docs.oracle.com/javafx/2/get_started/hello_world.htm>
*/

/*
   purpose: demonstrate using a ResourceBundle (properties-files) for automatic translation of
            items with the "%"-notation; in addtion also making GUI elements draw their values from
            the ScriptContext' Bindings using the $notation and the ${notation} (constantly
            updating a GUI element); cf. comments in "FXML_02_Document.fxml"

   invoke:
            rexx fxml_02
            rexx fxml_02 en
               ... use English properties file
            rexx fxml_02 de
               ... use German properties file
*/

   /* only "de" has an effect and will use the German translation for the */
parse arg locale .

   -- create Rexx object that will control the FXML set up
if locale<>"" then rexxHandler=.rxDocHandler~new(locale)
              else rexxHandler=.rxDocHandler~new

   -- instantiate the abstract JavaFX class, the abstract "start" method will be served by rexxHandler
rxApp=BsfCreateRexxProxy(rexxHandler,,"javafx.application.Application")

   -- launch the application, invoke "start" and then stay up until the application closes
rxApp~launch(rxApp~getClass, .nil)     -- need to use this version of launch in order to work
say center(" after rxApp~launch ", 70, "-")

::requires "BSF.CLS"    -- get Java support

/* implements the abstract method "start" for the Java class javafx.application.Application
   (BSF4ooRexx also supplies another (trailing) slotDir (a Rexx Directory) argument, as "start" is
   invoked from Java)
*/
::class RxDocHandler

::method init  -- constructor to fetch a locale string ("de" for German, file "fxml_01_de.properties"), if any
  expose locale
  use strict arg locale="en"

   /* loads the fxml document defining the GUI elements, sets up a scene for it and shows it */
::method start    -- will be invoked by the "launch" method
  expose locale
  use arg stage   -- we get the stage to use for our UI

   -- create an URL for the FMXLDocument.fxml file (hence the protocol "file:")
  rootDocUrl=.bsf~new("java.net.URL", "file:FXML_02_Document.fxml")
  jLocale=.bsf~new("java.util.Locale", locale)  -- if no file available, will use the default "fxml_01.properties"
  jRB=bsf.importClass("java.util.ResourceBundle")~getBundle("FXML_02", jLocale)

  root=bsf.loadClass("javafx.fxml.FXMLLoader")~load( rootDocUrl, jRB)   -- load the fxml document

  scene=.bsf~new("javafx.scene.Scene", root)    -- create a scene for our document
  stage~setScene(scene)                         -- set the stage to our scene
  stage~show                                    -- show the stage (and thereby our scene)
