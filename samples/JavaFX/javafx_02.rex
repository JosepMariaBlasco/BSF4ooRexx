#!/usr/bin/env rexx
/*
   author:  Rony G. Flatscher
   date:    2016-12-06

   changed: 2018-02-03, rgf: the JavaFX Builder infrastructure got deprecated in JavaFX 8 and its classes
                             (like LabelBuilder) removed in JavaFX 9 (cf.<http://mail.openjdk.java.net/pipermail/openjfx-dev/2013-March/006725.html>);
                             using ooRexx cascading messages instead, which is always available :)

   purpose: this is the standalone (FXML-less) version of the Rexx GUI application (taking advantage of JavaFX)
            in the fxml_02/ directory, i.e. the GUI elements are created in Rexx code only

   license: Apache License 2.0 (see at bottom)

   invoke:

         rexx javafx_02.rex
         rexxj.{cmd|sh} javafx_02.rex

   needs: ooRexx 4.1 or higher, and BSF4ooRexx 6.00 or highter

*/

.local~my.fx=.directory~new   -- to contain objects for easying the coupling of the different parts of this application
.my.fx~rexxStarted=.dateTime~new~string   -- save the date and time this program started

resources="fxml_02"    -- location of our resources (css, properties files, images)
   -- forward slashes ("WWW convention") suffice for both, Unix and Windows systems
.my.fx~properties=resources || "/FXML_02"         -- Properties base file name
.my.fx~appIcon   =resources || "/oorexx_032.png"  -- application icon
.my.fx~bkgPicture=resources || "/bsf4oorexx_032.png" -- background picture for AnchorPane
.my.fx~styleSheet=resources || "/fxml_02.css"     -- stylesheet to use

parse arg locale                 -- parse command line arguments, if any
if locale="" then locale="en"    -- default to English
jLocale=.bsf~new("java.util.Locale", locale) -- get Locale object
   -- let ResourceBundle choose which properties file to load and use:
.my.fx~resourceBundle=bsf.importClass("java.util.ResourceBundle")~getBundle(resources"/FXML_02", jLocale)

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
  cdClz=bsf.loadClass("javafx.scene.control.ContentDisplay")   -- get access to ContentDisplay constants (an Enum class)
  alClz=bsf.loadClass("javafx.geometry.Pos")                   -- get access to alignment constants (an Enum class)
  lblClz=bsf.import("javafx.scene.control.Label", "FX.Label")   -- import class under the name ".FX.Label"

   -- a Rexx directory which contains all data needed in our event handler (cf. RexxButtonHandler Rexx class below)
  userData=.directory~new
  userData~count=1            -- each Button click will show and then increase this value
  userData~title="--> -> >"   -- a free form hint string

/* AnchorPane definition in FXML, cf. "fxml_2/FXML_02_Document.fxml":

    <AnchorPane id="AnchorPane" fx:id="root"
         prefHeight="240.0" prefWidth="480.0"
         styleClass="root"
         stylesheets="@fxml_02.css"
         xmlns="http://javafx.com/javafx/8.0.65" xmlns:fx="http://javafx.com/fxml/1">
*/
  root=.bsf~new("javafx.scene.layout.AnchorPane")   -- create the root node
  root~id="root"        -- treat Java field/property as if an ooRexx attribute
  root~prefHeight=240   -- treat Java field/property as if an ooRexx attribute
  root~prefWidth=480    -- treat Java field/property as if an ooRexx attribute
  root~getStyleSheets~add(.my.fx~styleSheet)
  rootChildren=root~getChildren  -- collection of javafx controls to manage

-------------------------------------------------------------------
/* Label definition in FXML, cf. "fxml_2/FXML_02_Document.fxml":

   <Label fx:id="labelRexxStarted" alignment="CENTER" layoutX="50.0" layoutY="26.0"
        minHeight="16" minWidth="69" prefHeight="16.0" prefWidth="380.0"
        styleClass="rexxInfo" stylesheets="@fxml_02.css"
        text="$rexxStarted" />
*/
      -- define the first Label
  lbl=.fx.label~new
  lbl~setId("rexxStarted")
  lbl~setAlignment(alClz~valueOf("CENTER"))  -- center align, alternatively: "alClz~CENTER"
  lbl~setLayoutX(50)
  lbl~setLayoutY(26)
  lbl~setMinHeight(16)
  lbl~setMinWidth(69)
  lbl~setPrefHeight(16)
  lbl~setPrefWidth(380)
  lbl~getStyleClass~add("rexxStarted") -- add the CSS style named ".rexxStarted" to this node
  lbl~setText(.my.fx~rexxStarted)
  rootChildren~add(lbl)       -- add this Label control

-------------------------------------------------------------------
/* Label definition in FXML, cf. "fxml_2/FXML_02_Document.fxml", code goes to RexxButtonHandler's 'handle':

        <Button fx:id="button" layoutX="210.0" layoutY="137.0" onAction=
           "
                   say ' ==&gt; ---&gt; arrived in button''s onAction-code ...'  ;
                   /*@showsource*/ /* the previous Rexx script annotation will cause this code to be shown in original and edited state */ ;
                   use arg slotDir /* this argument is always sent by BSF4ooRexx */ ;
                   /*@get(label)*/ /* the previous Rexx script annotation will incorporate the attribute 'label' as a local Rexx variable */ ;
                   say ' ... label~getText='pp(label~getText)  ;
                   say ' ... changing text in label to current date and time ...' ;
                   label~text=.dateTime~new~string             ;
                   say ' ... label~getText='pp(label~getText)  ;
                   say ' ... now invoking the public Rexx routine ''klickButtonAction''' ;
                   call klickButtonAction slotDir /* now process the event */;
           "
           text="%clickMe"
        />
*/
      -- define and add the Button
  btn=.bsf~new("javafx.scene.control.Button")
  btn~id="button"
  btn~layoutX=210       -- assign as if it was a Rexx attribute
  btn~layoutY=137       -- assign as if it was a Rexx attribute
  btn~text=.my.fx~resourceBundle~getString("clickMe") -- assign as if it was a Rexx attribute
      -- create a RexxButtonHandler, wrap it up as a Java RexxProxy implementing all methods of "javafx.event.EventHandler";
      -- supply the RexxDirectory "userData" as second argument, such that it gets returned via the appended slotDir argument
  bh=BSFCreateRexxProxy(.RexxButtonHandler~new, userData,"javafx.event.EventHandler")
  btn~setOnAction(bh)   -- let this instance's Java RexxProxy handle the event
  rootChildren~add(btn) -- add this Button control

-------------------------------------------------------------------
/* Label definition in FXML, cf. "fxml_2/FXML_02_Document.fxml":

        <Label fx:id="year" layoutX="50.0" layoutY="175.0" minHeight="16" minWidth="20" style="-fx-background-color: palegoldenrod;" text="%year" />
*/
   -- now using cascading messages
  lbl=.fx.label~new~~setId("year")                                     -
                   ~~setTextFill(colorClz~BLUE)                        -
                   ~~setLayoutX(50)                                    -
                   ~~setLayoutY(175)                                   -
                   ~~setMinHeight(16)                                  -
                   ~~setMinWidth(20)                                   -
                   ~~setStyle("-fx-background-color: palegoldenrod;")  -
                   ~~setText(.my.fx~resourceBundle~getString("year"))
  rootChildren~add(lbl)       -- add this Label control

-------------------------------------------------------------------
/* Label definition in FXML, cf. "fxml_2/FXML_02_Document.fxml":

        <Label fx:id="label" layoutX="95.0" layoutY="175.0" minHeight="16" minWidth="49" prefHeight="16.0" prefWidth="335.0" style="-fx-background-color: honeydew;" />
*/
  lbl=.fx.label~new~~setId("label")                                    -
                   ~~setTextFill(colorClz~BLUE)                        -
                   ~~setLayoutX(95)                                    -
                   ~~setLayoutY(175)                                   -
                   ~~setMinHeight(16)                                  -
                   ~~setMinWidth(49)                                   -
                   ~~setPrefHeight(16)                                 -
                   ~~setPrefWidth(335)                                 -
                   ~~setStyle("-fx-background-color: honeydew;")       -
                   ~~setText("")

  rootChildren~add(lbl)       -- add this Label control
  userData~label=lbl    -- put it into our userData Rexx directory



-------------------------------------------------------------------
/* Label definition in FXML, cf. "fxml_2/FXML_02_Document.fxml":

        <Label fx:id="labelRexxInfo" alignment="CENTER" layoutX="50.0" layoutY="200.0" minHeight="16.0" minWidth="49.0" prefHeight="16.0" prefWidth="380.0" style="-fx-background-color: skyblue; -fx-cursor: wait; -fx-font-family: serif; -fx-font-weight: lighter;" text="${rexxInfo}" />
*/
  lbl=.fx.label~new~~setId("labelRexxInfo")                            -
                   ~~setTextFill(colorClz~BLUE)                        -
                   ~~setAlignment(alClz~CENTER)                        -
                   ~~setLayoutX(50)                                    -
                   ~~setLayoutY(200)                                   -
                   ~~setMinHeight(16)                                  -
                   ~~setMinWidth(49)                                   -
                   ~~setPrefHeight(16)                                 -
                   ~~setPrefWidth(380)                                 -
                   ~~setStyle("-fx-background-color: skyblue; -fx-cursor: wait; -fx-font-family: serif; -fx-font-weight: lighter;")

  rootChildren~add(lbl)       -- add this Label control
  userData~rexxInfo=lbl -- put it into our userData Rexx directory

-------------------------------------------------------------------
      -- put the scene on the stage (using AnchorPane's preferred height and width)
  primaryStage~setScene(.bsf~new("javafx.scene.Scene", root))
  img=.bsf~new("javafx.scene.image.Image", .my.fx~appIcon)
  primaryStage~getIcons~add(img)                       -- set application icon
  primaryStage~show


   -- Rexx class which handles the button presses
::class RexxButtonHandler

::method handle      -- will be invoked by the Java side
  use arg event, slotDir   -- the last argument will always be added by BSF4ooRexx
  say " ==> ---> arrived in button's RexxButtonHandler's 'handle' method ..."
  slotDir=arg(arg())          -- get last argument, will always be added by BSF4ooRexx
  userData=slotDir~userData   -- fetch the Rexx directory we supplied at button event handler creation time
   -- get values we want to work with
  rexxInfo=userData~rexxInfo  -- get the rexxInfo Label    ^
  count   =userData~count     -- get the current value of 'count'
  title   =userData~title     -- get the value of 'title'
  label   =userData~label     -- get the Label with the id "label"

  say ' ... label~getText='pp(label~getText)
  say ' ... changing text in label to current date and time ...'
  label~text=.dateTime~new~string
  say ' ... label~getText='pp(label~getText)

   -- as this version does not use the javax.script infrastructure there is
   -- no "ScriptContext" from javax.script available to us; "fxml_02" by
   -- contrast will access it and display its content on each invocation

   -- using the FXMLLoader we could have it update this Label
  rexxInfo~text="Updated from RexxButtonHandler's 'handle' method."
  if count//2=0 then rexxInfo~text=rexxInfo~text~reverse -- if even, reverse the current text
  userData~count+=1           -- increment value of 'count'

    -- access the "label" JavaFX Label, change its text
  label~setText(title .dateTime~new~string "(count #" count")")

  say " <== <--- returning from RexxButtonHandler's 'handle' method."
  say



/*
      ------------------------ Apache Version 2.0 license -------------------------
         Copyright 2016-2018 Rony G. Flatscher

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