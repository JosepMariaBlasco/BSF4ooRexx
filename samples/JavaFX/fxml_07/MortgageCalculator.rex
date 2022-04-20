#!/usr/bin/env rexx
/*
   author:     Rony G. Flatscher

   date:       2019-02-17

   changed:    --
   changed:    2020-08-12, rgf: - changing ppCondition2() to the new BSF.CLS routine ppJavaExceptionChain()
                                - removing dependency on "rgf_util2.rex"
               2022-04-20, rgf: - Morgage.fxml - call to MortageController.rex moved before
                                  Button definition such that button's event code can get
                                  to see its public routines

   purpose:    - demonstrates event filtering for assuring that numeric text fields
                 only contain valid numbers (otherwise an Alert error window pups
                 up and the wrong entered value gets replaced with the current valid
                 one, including the caret position) and synchronizing slider and
                 corresponding text fields;
                 in addition demonstrates the usage of java.text.Format to format
                 numbers (using the default Locale)

   needs:      -

   usage:      rexx MortgageCalculator.rex or rexxj.sh|rexxj.cmd MortgageCalculator.rex or double-clicking ;)

   license:    - Apache License 2.0 (AL 2.0)
*/
rexxVersion=.bsf4rexx~rexx.version
if rexxVersion<5 then
do
   say "This program cannot run with your version of ooRexx" pp(rexxVersion)", because"
   say "it uses the package local environment ('.context~package~local') introduced"
   say "with ooRexx 5.0. Besides ooRexx 5.0 may be needed anyway, because of its much"
   say "improved multi-threading support."
   say
   say "Hit enter to continue ..."
   bsf.loadClass("java.awt.Toolkit")~getDefaultToolkit~beep -- cross-platform beep
   parse pull x
   exit -1
end


   -- make sure we change to the directory this program resides, such that all resources are in the local directory
parse source  . . pgm
call directory filespec('L', pgm)   -- change to the directory where the program resides

signal on syntax     -- in case something deep in Java caused an exception,

call bsf.loadClass "java.text.NumberFormat", "NumberFormat"
call bsf.loadClass "java.math.RoundingMode", "RoundingMode"
call bsf.loadClass "java.lang.Math",         "Math"

rxApp=.rexxApplication~new
   -- instantiate the abstract JavaFX class, abstract "start" method implemented in Rexx
jrxApp=BsfCreateRexxProxy(rxApp,,"javafx.application.Application")
   -- launch the application, which will invoke the methdos "init" followed by "start"
jrxApp~launch(jrxApp~getClass, .nil)   -- need to use this version of launch in order to work
call sysSleep 0.01                     -- let ooRexx clean up
exit

syntax:
  co=condition("object")
  say ppJavaExceptionChain(co)   -- display Java exception chain
  exit -1

::requires "BSF.CLS"       -- get Java support

/* implements the abstract method "start" of javafx.application.Application */
::class RexxApplication

   /* loads the FXML file (doing translations), sets up a scene for it and shows it */
::method start  -- implementation in Rexx
  use arg primaryStage -- we get the primaryStage to use for our UI

  primaryStage~title="JavaFX Mortgage Calculator Demonstration (ooRexx)"
   -- create a file URL for fxml_02.fxml file (hence the protocol "file:")
  fxmlUrl=.bsf~new("java.net.URL", "file:Mortgage.fxml")
  rootNode=bsf.loadClass("javafx.fxml.FXMLLoader")~load(fxmlUrl)
  scene=.bsf~new("javafx.scene.Scene", rootNode)    -- create a scene from the tree
  primaryStage~setScene(scene)     -- set our scene on primaryStage
  primaryStage~setResizable(.false)-- do not allow the stage (window) to be resized
  primaryStage~show                -- show the primaryStage with the scene

/*
      ------------------------ Apache Version 2.0 license -------------------------
         Copyright 2019-2020 Rony G. Flatscher

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
