#!/usr/bin/env rexx
/*
   author:  Rony G. Flatscher
   date:    2016-12-06
   changed: 2017-11-09, rgf: adapted to Rexx symposium 2017 article

   purpose: this is the ooRexx version of the pure Java tutorial on how to use JavaFX property
            binding (cf. http://docs.oracle.com/javase/8/javafx/properties-binding-tutorial/binding.htm)

   license: Apache License 2.0 (see at bottom)

   invoke:

         rexx javafx_property_binding.rex
         rexxj.{cmd|sh} javafx_property_binding.rex

   needs: ooRexx 4.1 or higher, and BSF4ooRexx 6.00 or highter

*/

  -- import the Java class, allow it to be used like an ooRexx class thereafter
sipClz=bsf.import("javafx.beans.property.SimpleIntegerProperty")
num1 = sipClz~new(1)
say "num1:" num1 "|" num1~toString "|" num1~getValue
num2 = sipClz~new(2)
say "num2:" num2 "|" num2~toString "|" num2~getValue
say
sum=num1~add(num2)
say "sum: " sum
say "sum: " sum~toString "|" sum~getValue "|" sum~toString
say "---"
say "num1:" num1~getValue "num2:" num2~getValue "-> sum:" sum~getValue
say "setting 'num1=2' ..."
num1~set(2)
say "num1:" num1~get      "num2:" num2~get      "-> sum:" sum~get
say "setting 'num2=3' ..."
num2~set(3)
say "num1:" num1~getValue "num2:" num2~getValue "-> sum:" sum~getValue

::requires "BSF.CLS"	-- get Java support


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