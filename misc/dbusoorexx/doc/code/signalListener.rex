signal on halt          -- intercept ctl-c (jump to label 'halt:' below)

conn=.dbus~session      -- get the "session" connection
conn~listener("add", .rexxSignalListener~new)   -- add the Rexx listener object
conn~match("add", "type='signal'", .true)       -- ask for any signal message

say "Hit enter to stop listener..."
parse pull answer

halt:                   -- a ctl-c causes a jump to this label
   say "closing connection."
   conn~close           -- close connection, stops message loop

::requires "dbus.cls"      -- get dbus support for ooRexx

::class RexxSignalListener -- just dump all signals/events we receive
::method unknown           -- this method will service all invocations
  use arg methName, methArgs

  slotDir=methArgs[methArgs~items]     -- last argument is slotDir
  say "-->" pp(slotDir~messageTypeName) pp(slotDir~interface) -
            pp(slotDir~member)", nrArgs="methArgs~items-1

  if methArgs~items>1 then
  do
     do i=1 to methArgs~items-1
        say "    argument #" i":" pp(methArgs[i])
     end
  end
  say "-"~copies(79)

::method NameOwnerChanged  -- demo how to directly intercept a signal
  use arg name, old, new, slotDir
  say "==> NameOwnerChanged:" "Name:" pp(name)", OldOwner:" pp(old)", NewOwner:" pp(new)
  say "-"~copies(79)

::routine pp      -- "pretty print": enclose string value with square brackets
  parse arg value
  return "["value"]"
