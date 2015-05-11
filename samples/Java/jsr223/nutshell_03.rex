parse source . . thisFile
say "(from Rexx) source:" pp(thisFile)", prolog code, tid="pp(BsfGetTid())
return .test~new     -- return Rexx object

::requires BSF.CLS

::routine run public -- a public routine to be used for the java.lang.Runnable implementation
  use arg slotDir
  say "(from Rexx) public routine 'RUN', tid="pp(BsfGetTid()) || -
                   ", slotDir~scriptContext:" pp(slotDir~scriptContext)


::class test         -- a Rexx class implementing the method of the Java interface class
::method run         -- a method routine to be used for the java.lang.Runnable implementation
  use arg slotDir
  say "(from Rexx)" pp(self) "method 'RUN', tid="pp(BsfGetTid()) || -
                    ", slotDir~scriptContext:" pp(slotDir~scriptContext)

