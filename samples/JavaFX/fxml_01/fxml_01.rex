/*
   author:  Rony G. Flatscher
   date:    2016-12-06

   purpose: this is a simple the Rexx GUI application (taking advantage of JavaFX)
            where the GUI elements are defined in a FXML file ("FXML_01_Document.fxml"),
            which also defines running the Rexx program ("fxml_01_controller.rex")
            and invoking a public routine ("klickButtonAction") therein, if the user
            presses the push button in the GUI

   license: Apache License 2.0 (see at bottom)

   invoke:

         rexx fxml_01.rex
         rexxj.{cmd|sh} fxml_01.rex

   needs: ooRexx 4.1 or higher, and BSF4ooRexx 6.00 or highter

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

  stage~setTitle("Hello JavaFX from ooRexx!")   -- we could use stage~title="..." instead!

   -- create an URL for the FMXLDocument.fxml file (hence the protocol "file:")
  rootDocUrl=.bsf~new("java.net.URL", "file:FXML_01_Document.fxml")
  root=bsf.loadClass("javafx.fxml.FXMLLoader")~load( rootDocUrl  )   -- load the fxml document

  scene=.bsf~new("javafx.scene.Scene", root)    -- create a scene for our document
  stage~setScene(scene)                         -- set the stage to our scene
  stage~show                                    -- show the stage (and thereby our scene)


/*
      ------------------------ Apache Version 2.0 license -------------------------
         Copyright 2016 Rony G. Flatscher

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
