#!/usr/bin/env rexx
/*
   author:  Rony G. Flatscher
   date:    2016-12-06
   changed: 2017-11-09, rgf: adapted to Rexx symposium 2017 article

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


rxApp=.RexxApplication~new -- create Rexx object that will control the FXML set up
jrxApp=BSFCreateRexxProxy(rxApp, ,"javafx.application.Application")
jrxApp~launch(jrxApp~getClass, .nil)    -- launch the application, invokes "start"

::requires "BSF.CLS"    -- get Java support

-- Rexx class defines "javafx.application.Application" abstract method "start"
::class RexxApplication -- implements the abstract class "javafx.application.Application"

::method start          -- Rexx method "start" implements the abstract method
  use arg primaryStage  -- fetch the primary stage (window)
  primaryStage~setTitle("Hello JavaFX from ooRexx! (Green Version)")

   -- create an URL for the FMXLDocument.fxml file (hence the protocol "file:")
  fxmlUrl=.bsf~new("java.net.URL", "file:fxml_01.fxml")
   -- use FXMLLoader to load the FXML and create the GUI graph from its definitions:
  rootNode=bsf.loadClass("javafx.fxml.FXMLLoader")~load(fxmlUrl)

  scene=.bsf~new("javafx.scene.Scene", rootNode)    -- create a scene for our document
  primaryStage~setScene(scene)  -- set the stage to our scene
  primaryStage~show             -- show the stage (and thereby our scene)


/*
      ------------------------ Apache Version 2.0 license -------------------------
         Copyright 2016-2017 Rony G. Flatscher

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