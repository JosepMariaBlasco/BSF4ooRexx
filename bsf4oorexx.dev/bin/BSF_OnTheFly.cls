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

   version:    1.0.1
   date:       2009-06-08
   changed:    2010-05-11, rgf, now requires BSF.CLS
*/

parse source s

use arg code      -- can be a string or a method object, will be used as the code for an unknown method

if code~isA(.string) then
do
   -- unfortunately .message~new with a string argument unRexxingly raises a syntax error, if a CR or a LF is encountered!
   return .BSF.OnTheFly~new(code~changeStr("0d0a"x,";")~changeStr("0d"x,";")~changeStr("0a"x,";"))   -- create object and return it
end

if code~isA(.method) then     -- a method object supplied ?
do
   return .BSF.OnTheFly~new(code)   -- create object and return it
end

return .BSF.OnTheFly~new            -- return an object that will only do a "return" in its unknown method


::requires BSF.CLS               -- get access to public BSF routines and BSF classes


/* Stub class: an instance will be returned, the supplied code, if any, will be used as the unknown-method. */
::class "BSF.OnTheFly"

::method setMethod               -- make setMethod available to allow for setting a new method
  forward class (super)

::method unsetMethod             -- make unsetMethod available to allow for setting a new method
  forward class (super)

::method init                    -- constructor
  use arg code                   -- Rexx code (a string or a method object, cf. Object.setMethod() documentation

  if arg(1,"Exists"), code<>"" | code<>.nil then
     self~setMethod("unknown", code)
  else
     self~setMethod("unknown", "return .nil")   -- default: code just returns .nil (do nothing)



