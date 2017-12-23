@echo off
rem run the bsf4rexx-testGroups
rem rexx testOORexx -R ..\bsf4rexx\lex -s

echo all tests from %cd%\bsf4rexx\lex ...
rexx testOORexx -R %cd%\bsf4rexx\lex -s
