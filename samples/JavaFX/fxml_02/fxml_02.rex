/*
   author:  Rony G. Flatscher
   date:    2016-12-06

   purpose: this is a simple the Rexx GUI application (taking advantage of JavaFX)
            where the GUI elements are defined in a FXML file ("FXML_02_Document.fxml"),
            which also defines running the Rexx program ("fxml_02_controller.rex")
            and invoking a public routine ("klickButtonAction") therein, if the user
            presses the push button in the GUI;

            demonstrate using a ResourceBundle (properties-files) for automatic translation of
            items with the "%"-notation; in addition also making GUI elements draw their values from
            the ScriptContext' Bindings using the $notation and the ${notation} (constantly
            updating a GUI element); cf. comments in "FXML_02_Document.fxml"

   license: Apache License 2.0 (see at bottom)

   invoke:

         rexx fxml_02.rex [en|de]
         rexxj.{cmd|sh} fxml_02.rex [en|de]

   needs: ooRexx 4.1 or higher, and BSF4ooRexx 6.00 or highter

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
  jLocale=.bsf~new("java.util.Locale", locale)
  jRB=bsf.importClass("java.util.ResourceBundle")~getBundle("FXML_02", jLocale)

  root=bsf.loadClass("javafx.fxml.FXMLLoader")~load(rootDocUrl, jRB) -- load the fxml document

  scene=.bsf~new("javafx.scene.Scene", root)    -- create a scene for our document
  stage~setScene(scene)                         -- set the stage to our scene
  scene~getStyleSheets~add("fxml_02.css")       -- use this CSS to style the UI

  img=.bsf~new("javafx.scene.image.Image", "oorexx_032.png")
  stage~getIcons~add(img)                       -- set application icon
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
