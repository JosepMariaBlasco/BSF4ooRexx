parse arg rexxProgram
if rexxProgram="" then
do
  say "You must supply the name of a Rexx program to run."
  exit -1
end

mgr      = .bsf~new("org.apache.bsf.BSFManager")
re       = mgr~loadScriptingEngine("rexx")
rexxconf = re~getRexxConfiguration
say "default rexxconf="pp(rexxconf~toString)

rhOne    = .OneCommandHandler~new
jhOne    = BsfCreateRexxProxy(rhOne, , "org.rexxla.bsf.engines.rexx.AbstractDirectCommandHandler")
rexxconf~addCommandHandler("ONE", jhOne)

rhDeux    = .DeuxCommandHandler~new
jhDeux    = BsfCreateRexxProxy(rhDeux, , "org.rexxla.bsf.engines.rexx.AbstractRedirectingCommandHandler")
rexxconf~addCommandHandler("DEUX", jhDeux)
rexxconf~setInitialAddressEnvironment("ONE");
say
say "after configuration:"
say "edited  rexxconf="pp(rexxconf~toString)

say "now running the Rexx program you supplied in file """rexxProgram""":"
-- Rexx code to run
rexxCode= "call """rexxProgram"""    ;" -
          "::requires BSF.CLS        ;"    -- get ooRexx support (camouflage Java as ooRexx)
say "rexxCode="pp(rexxCode)

signal on syntax
-- invoke the interpreter and call the Rexx program in the supplied file name
re~apply(rexxProgram, 0, 0, rexxCode, .nil, .nil)
mgr~terminate        -- make sure that the Rexx interpreter instance gets terminated!
exit

syntax:
  co=condition('o')
  say ppJavaExceptionChain(co,.true)
  say "---"
  raise propagate


::requires BSF.CLS      -- get ooRexx support (camouflage Java as ooRexx)

::class OneCommandHandler

::method handleCommand
   use arg slot, address, command, slotDir

   say "[OneCommandHandler] address="pp(address) "command="pp(command)
   parse upper var command cmd .

      -- carrying out received commands, return values set the RC variable in Rexx
   select case cmd
        when "HELLO" then return "Hello, ooRexx! This is your Rexx (!) OneCommandHandler greeting you!"
        when "INFO"  then return slotDir~javaObject~toString(slot,address)
        when "PING"  then return "Pong!"
        otherwise   -- unknown command received create a failure
            jarr = bsf.createJavaArrayOf("java.lang.String", "Unknown command" pp(command) )
            slotDir~javaObject~raiseCondition(slot, "Failure", command, jarr, "-1")
            return .nil;
   end


::class DeuxCommandHandler

::method handleCommand
   use arg slot, address, command, slotDir

   say "[DeuxCommandHandler] address="pp(address) "command="pp(command)
   parse upper var command cmd .
      -- carrying out received commands, return values set the RC variable in Rexx
   select case cmd
        when "HELLO" then return "Hello, ooRexx! This is your Rexx (!) DeuxCommandHandler greeting you!"
        when "INFO"  then return slotDir~javaObject~toString(slot,address)
        when "PING"  then return "Pong!"
        otherwise   -- unknown command received create a failure
            jarr = bsf.createJavaArrayOf("java.lang.String", "Unknown command" pp(command) )
            slotDir~javaObject~raiseCondition(slot, "Failure", command, jarr, "-1")
            return .nil;
   end
