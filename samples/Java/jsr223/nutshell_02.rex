return .test~new        -- return a Rexx object

::class test            -- a Rexx class
::method one            -- a Rexx method
  return 'one (from Rexx)'

::method two            -- a Rexx method
  use arg a, b, c, d       -- get (Java) arguments
  say "(from Rexx) 'd~scriptContext' is the ScriptContext of this invocation:" pp(d~scriptContext)

  return "(from Rexx)" '->' a '->' b '->' c '!' -- return the concatenated values

::method now            -- a Rexx method
  return "(from Rexx)" .DateTime~new~string -- return the string value

::routine routine_eins public -- a public routine
  return "(from Rexx) return value from the public 'routine_eins'"

::routine routine_zwei public -- a public routine
  use arg a, b, c
  say "(from Rexx) 'arg(4)~scriptContext' is the ScriptContext of this invocation:" pp(arg(4)~scriptContext)
  return "(from Rexx)" '->' a '->' b '->' c '!' -- return the concatenated values

::routine routine_drei private   -- a private routine
  return "(from Rexx) return value from the public 'routine_drei'"
