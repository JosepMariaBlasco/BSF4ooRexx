@echo off
if not exist bsf goto nobsf
echo bsf exists.
goto test2
:nobsf
  echo no bsf !

:test2
  if not exist xyz goto noxyz
  echo xyz exists.
  goto end

:noxyz
  echo no xyz !

:end
