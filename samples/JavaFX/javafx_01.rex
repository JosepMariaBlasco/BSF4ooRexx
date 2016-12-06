/*
   author:  Rony G. Flatscher
   date:    2016-12-06

   purpose: this is the standalone (FXML-less) version of the Rexx GUI application (taking advantage of JavaFX)
            in the fxml_01/ directory, i.e. the GUI elements are created in Rexx code only

   license: Apache License 2.0 (see at bottom)

   invoke:

         rexx javafx_01.rex
         rexxj.{cmd|sh} javafx_01.rex

   needs: ooRexx 4.1 or higher, and BSF4ooRexx 6.00 or highter

*/

   -- create Rexx object that will control the FXML set up
rexxHandler=.RexxAppHandler~new

   -- instantiate the abstract JavaFX class, the abstract "start" method will be served by rexxHandler
rxApp=BSFCreateRexxProxy(rexxHandler, ,"javafx.application.Application")

   -- launch the application, invoke "start" and then stay up until the application closes
rxApp~launch(rxApp~getClass, .nil)  -- cf. <http://www.dreamchain.com/javafx-and-groovy-exception/> or <http://stackoverflow.com/questions/16636883/javafx-anonymous-application-class>, etc.


::requires "BSF.CLS"    -- get Java support

::class RexxAppHandler  -- the Rexx handler for javafx.application.Application

::method start          -- will be called by JavaFX, allows to setup everything
  use arg primaryStage

  primaryStage~setTitle("Hello JavaFX from ooRexx!")  -- we could use primaryStage~title="..." instead!

  colorClz=bsf.loadClass("javafx.scene.paint.Color")           -- get access to the JavaFX colors
  cdClz=bsf.loadClass("javafx.scene.control.ContentDisplay")   -- get access to ContentDisplay constants
  alClz=bsf.loadClass("javafx.geometry.Pos")                   -- get access to alignment constants (an Enum class)


  root=.bsf~new("javafx.scene.layout.AnchorPane")   -- create the root node
  root~prefHeight=200
  root~prefWidth=400

      -- define the Label
  lbl=.bsf~new("javafx.scene.control.Label")
  lbl~textFill=colorClz~BLUE
  lbl~setLayoutX(76)
  lbl~setLayoutY(138)
  lbl~prefHeight="16.0"
  lbl~prefWidth="248.0"
  lbl~contentDisplay=cdClz~CENTER         -- center ContentDisplay
  lbl~alignment=alClz~valueOf("CENTER")   -- center align

      -- define and add the Button
  btn=.bsf~new("javafx.scene.control.Button")
  btn~textFill=colorClz~BLUE
  btn~layoutX=170       -- assign as if it was a Rexx attribute
  btn~layoutY=89        -- assign as if it was a Rexx attribute
  btn~text="Click Me!"  -- assign as if it was a Rexx attribute
      -- create a RexxButtonHandler, wrap it up as a Java RexxProxy implementing all methods of "javafx.event.EventHandler":
  bh=BSFCreateRexxProxy(.RexxButtonHandler~new(lbl), ,"javafx.event.EventHandler")
  btn~setOnAction(bh)  -- let this instance's Java RexxProxy handle the event

      -- add the button to
  root~getChildren~~add(btn)~~add(lbl)

      -- put the scene on the stage (using AnchorPane's preferred height and width)
  primaryStage~setScene(.bsf~new("javafx.scene.Scene", root))
  primaryStage~show

   -- Rexx class which handles the button presses
::class RexxButtonHandler
::method init
  expose label       -- define an attribute
  use arg label      -- save reference to javafx.scene.control.Label

::method handle      -- will be invoked by the Java side
  expose label
  say .dateTime~new": arrived in code defined for Button's setOnAction method, i.e. the 'handle' method"

  say '... current value of 'pp(label)': label~getText='pp(label~text)  ;
  label~text="Clicked at:" .dateTime~new  -- set the label

  say .dateTime~new': returning from the event handler' ;
  say



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
