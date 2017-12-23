#! /bin/sh
# run the bsf4rexx-testGroups
# rexx testOORexx -R ../bsf4rexx/lex -s
# rexx testOORexx -R ../bsf4rexx/rgf -s

echo running all tests but multithreading - rexx testOORexx -R `pwd`/bsf4rexx -s ...

echo running \'lex\' tests - rexx testOORexx -R `pwd`/bsf4rexx/lex -s ...
rexx testOORexx -R `pwd`/bsf4rexx/lex -s

echo running \'rgf\' tests - rexx testOORexx -R `pwd`/bsf4rexx/rgf -s ...
rexx testOORexx -R `pwd`/bsf4rexx/rgf -s

