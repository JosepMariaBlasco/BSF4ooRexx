/*
   name:       BSF.OnTheFly.cls
   author:     Rony G. Flatscher
   license:    ASF 2.0, LGPL 3.0
   purpose:    wrapper class for BSF4Rexx to use, if only a method object or a string object
               with Rexx code is supplied for creating a RexxProxy; that code will be added
               to the method unknown via setMethod();
               having this template code externally as a Rexx program allows easier
               maintenance and addition for BSF4Rexx users, if special needs arise
               (e.g. timestamps, monitoring, debugging)

   version:    1.0.2
   date:       2009-06-08
   changed:    2010-05-11, rgf, now requires BSF.CLS
               2017-07-25, rgf, - added .array as an argument, if code is a string use "makeArray" to
                                  create an .array (stabler as changing CR-LF to ';' may be fragile
                                  in the case that a line comment occurs in the code)
                                - removed forward capabilities for setmethod and unsetmethod (they are private)
                                - removed superfluous "parse source s" statement
*/

use arg code      -- can be a string or a method object, will be used as the code for an unknown method

if code~isA(.string) then     -- Rexx code supplied as a string, can have CR-LF embedded (which would cause an ooRexx exception)
   return .BSF.OnTheFly~new(code~makeArray)  -- create object and return it

if code~isA(.array) then      -- Rexx code supplied as an array
   return .BSF.OnTheFly~new(code)   -- create object and return it

if code~isA(.method) then     -- Rexx code supplied as an array
   return .BSF.OnTheFly~new(code)   -- create object and return it

return .BSF.OnTheFly~new      -- return an object that will only do a "return .nil" in its unknown method


::requires BSF.CLS            -- get access to public BSF routines and BSF classes


/* Stub class: an instance will be returned, the supplied code, if any, will be used as the unknown-method. */
::class "BSF.OnTheFly"

::method init                    -- constructor
  use arg code                   -- Rexx code (a string or a method object, cf. Object.setMethod() documentation

  if arg(1,"Exists"), code<>"" | code<>.nil then
     self~setMethod("unknown", code)
  else
     self~setMethod("unknown", "return .nil")   -- default: code just returns .nil (do nothing)



