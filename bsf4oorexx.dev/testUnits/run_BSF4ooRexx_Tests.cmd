@echo off
rem run the bsf4rexx-testGroups
rem rexx testOORexx -R ..\bsf4rexx\rgf -s

echo all tests from %cd%\bsf4rexx\rgf ...
rexx testOORexx -R %cd%\bsf4rexx\rgf -s

