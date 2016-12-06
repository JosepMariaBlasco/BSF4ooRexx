/*
   author:  Rony G. Flatscher
   date:    2016-12-06

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
num2 = sipClz~new(2)
sum=num1~add(num2)
say "'num1=1' (an IntegerProperty) and 'num2=2' (an IntegerProperty), 'sum' (a NumberBinding):" sum~getValue
num1~set(2)
say "after setting 'num1' to '2', sum:" sum~getValue
num2~set(3)
say "after setting 'num2' to '3', sum:" sum~getValue

::requires "BSF.CLS"	-- get Java support


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
