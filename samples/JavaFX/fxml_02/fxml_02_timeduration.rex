#!/usr/bin/env rexx
   /* usage: fxml_02_timeduration.rex [de] ... "de" will cause fxml_02_de.properties to be used */
/*
   author:  Rony G. Flatscher
   date:    2016-12-06
   changed: 2017-11-09, rgf: adapted to Rexx symposium 2017 article
            2018-03-24, rgf: make sure that we change into the directory of this program
            2018-06-25, rgf: demonstrate how to intercept the "stop" method
            2020-08-12, rgf: - changing ppCondition2() to the new BSF.CLS routine ppJavaExceptionChain()
                             - removing dependency on "rgf_util2.rex"

   purpose: this is a simple the Rexx GUI application (taking advantage of JavaFX)
            where the GUI elements are defined in a FXML file ("FXML_02_Document.fxml"),
            which also defines running the Rexx program ("fxml_02_controller.rex")
            and invoking a public routine ("klickButtonAction") therein, if the user
            presses the push button in the GUI;

            demonstrate using a ResourceBundle (properties-files) for automatic translation of
            items with the "%"-notation; in addition also making GUI elements draw their values from
            the ScriptContext' Bindings using the $notation and the ${notation} (constantly
            updating a GUI element); cf. comments in "FXML_02_Document.fxml"

            demonstrate using the routine bsf.createProxyClass() to create a Proxy class that
            allows one to intercept not only the abstract Java method "start" but also the
            concrete Java methods like "stop" in the abstract class "javafx.application.Application"

   license: Apache License 2.0 (see at bottom)

   invoke:

         rexx test_fxml_02_timeduration.rex [en|de]
         rexxj.{cmd|sh} test_fxml_02_timeduration.rex [en|de]

   needs: ooRexx 4.1 or higher, and BSF4ooRexx 6.00 or highter

*/

   -- change directory to program location such that relatively addressed resources can be found
parse source  . . pgm
call directory filespec('L', pgm)   -- change to the directory where the program resides

   /* only "de" has an effect and will use the German translation for the */
parse arg locale .

   -- create Rexx object that will control the FXML set up with or without local
if locale<>"" then rxApp=.rexxApplication~new(locale)
              else rxApp=.rexxApplication~new

   -- create a subclass, make sure invocations of methods "start" and "stop" get forwarded
   -- to the Rexx object "rxApp"
tmpClz=bsf.createProxyClass("javafx.application.Application", , "start", "stop")
   -- instantiate the dynamically created class, supply Rexx object that implements the methods
jrxApp=tmpClz~new(rxApp)

   -- launch the application, which will invoke the methdos "init" followed by "start"
signal on syntax              -- if an error occurs, the Java exception may not be helpful, so do it Rexx-like
jrxApp~launch(jrxApp~getClass, .nil)    -- need to use this version of launch in order to work
say center(" after jrxApp~launch ", 70, "-")
call sysSleep 0.01            -- let ooRexx clean up
exit

syntax:                       -- internal routine: eases identification of the problem
  co=condition("object")      -- get condition directory
  say ppJavaExceptionChain(co)   -- display Java exception chain
  exit -1

::requires "BSF.CLS"          -- get Java support

/* implements the abstract method "start" of javafx.application.Application */
::class RexxApplication

::method init   -- constructor to fetch the locale ("de": "fxml_01_de.properties")
  expose locale -- get direct access to attribute
  use strict arg locale="en"    -- if omitted use "fxml_01_en.properties"

   /* loads the FXML file (doing translations), sets up a scene for it and shows it */
::method start  -- implementation in Rexx
  expose locale startDateTime -- get direct access to attribute
  startDateTime=.dateTime~new -- save timestamp
  use arg stage -- we get the stage to use for our UI

   -- create a file URL for fxml_02.fxml file (hence the protocol "file:")
  fxmlUrl=.bsf~new("java.net.URL", "file:fxml_02.fxml")
  jLocale=.bsf~new("java.util.Locale", locale)  -- get the desired Locale
  jRB=bsf.loadClass("java.util.ResourceBundle")~getBundle("fxml_02", jLocale)

  rootNode=bsf.loadClass("javafx.fxml.FXMLLoader")~load(fxmlUrl, jRB)
  scene=.bsf~new("javafx.scene.Scene", rootNode)    -- create a scene from the tree
  stage~setScene(scene)     -- set our scene on stage
  stage~title="A Crazy FXML Rexx Application"   -- set the title for the stage
  img=.bsf~new("javafx.scene.image.Image", "oorexx_032.png")    -- create Image
  stage~getIcons~add(img)   -- use image as the application icon
  stage~show                -- show the stage with the scene

::method stop  -- this method will be called when the JavaFX applications is terminated
  expose startDateTime        -- get access to start timestamp
  endDateTime=.dateTime~new   -- get timestamp
  width=26
  say
  say "---> Application started at:" pp(startDateTime~string~right(width))
  say "     Application ended   at:" pp(  endDateTime~string~right(width))
  say "     Application lasted:    " pp((endDateTime-startDateTime)~string~right(width))


/*
      ------------------------ Apache Version 2.0 license -------------------------
         Copyright 2016-2020 Rony G. Flatscher

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