#!/usr/bin/env rexx
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

   version:    103.20171221
   date:       2009-06-08
   changed:    2010-05-11, rgf, now requires BSF.CLS
               2017-07-25, rgf, - added .array as an argument, if code is a string use "makeArray" to
                                  create an .array (stabler as changing CR-LF to ';' may be fragile
                                  in the case that a line comment occurs in the code)
                                - removed forward capabilities for setmethod and unsetmethod (they are private)
                                - removed superfluous "parse source s" statement
               2017-12-21, rgf, - corrected comment, changed test for empty or .nil code, changed version style
               2018-01-13, rgf, - changed the code to make it even easier for Rexx programmers to implement Rexx lambda
                                  functions: BsfCretatRexxProxy now allows as the first argument a String, an array of
                                  Strings, a Method object or a Routine object that implements the single function;
                                  the Rexx code in all of these variants will receive the Java argument(s), if any,
                                  in the same order as Java has sent them; it is possible to learn more about the
                                  Java invocation by inspecting the trailing slot.argument (a Rexx directory)
*/

use arg code   -- can be a string, an array (of statements), a method or a routine object

if arg(1,'Omitted') then
   return .BSF.onTheFly~new("INITFROMREXX")

return .BSF.onTheFly~new("INITFROMREXX", code)


::requires BSF.CLS            -- get access to public BSF routines and BSF classes


::method invokeMethod         -- method version: invokes the supplied unknown method
  expose executable
  use arg msgName, msgArgs
  self~run(executable,"Array",msgArgs)      -- run the method, supply arguments
  if VAR('RESULT') then return result
  return

::method invokeRoutine        -- routine version: invokes the supplied unknown method
  expose executable
  use arg msgName, msgArgs
  executable~callWith(msgArgs)
  if VAR('RESULT') then return result
  return


/* Stub class: an instance will be returned, the supplied code, if any, will be used as the unknown-method. */
::class "BSF.OnTheFly"

::method init                    -- constructor
  expose executable kind

  use arg arg1, arg2             -- Rexx code (a string or a method object, cf. Object.setMethod() documentation)
  if arg1="INITFROMREXX" then    -- message not from Java, it is from this Rexx package!
  do
     select
         when arg(2,'Omitted') then    -- no executable code supplied, make sure we do nothing, but returning .nil
              do
                 executable=.method~new('unknown', 'return .nil')   -- just return .nil
                 kind="METHOD"
                 self~setMethod('unknown', executable)  -- create a method with a single line of Rexx code
                 return
              end

         when arg2~isA(.string) then   -- string gets the arguments in the Rexx UNKNOWN format!
              do
                 executable=.method~new('unknown', arg(2)~makeArray) -- make sure we do not have CR/LF in supplied string
                 kind="METHOD"
                 self~setMethod('unknown', .methods~invokeMethod, 'Object')   -- use the method-version
              end

         when arg2~isA(.array) then    -- array gets the arguments in the Rexx UNKNOWN format!
              do
                 executable=.method~new('unknown', arg2)    -- make sure we do not have CR/LF in supplied string
                 kind="METHOD"
                 self~setMethod('unknown', .methods~invokeMethod, 'Object')   -- use the method-version
              end

         when arg2~isA(.method)  then  -- method object fetches Java arguments as sent by Java (not in the Rexx UNKNOWN format!)
              do
                 executable=arg2
                 kind="METHOD"
                 self~setMethod('unknown', .methods~invokeMethod, 'Object')   -- use the method-version
              end

         when arg2~isA(.routine) then  -- method object string fetches Java arguments as sent by Java (not in the Rexx UNKNOWN format!)
              do
                 executable=arg2
                 kind="ROUTINE"
                 self~setMethod('unknown', .methods~invokeRoutine, 'Object')  -- use the routine-version
              end
     end
     return    -- we are done setting up this instance
  end

   -- Java sent the message "INIT"!
  if kind="METHOD" then -- run the method, supply all received arguments
      self~run(executable, "array", arg(1,"Array"))
  else                  -- run the routine
      executable~callWith(arg(1,"Array"))

  if var('result') then -- if a result was returned, return it to Java
     return result

  return