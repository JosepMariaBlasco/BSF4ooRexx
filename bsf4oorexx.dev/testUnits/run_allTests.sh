#! /bin/sh
# run the bsf4rexx-testGroups
# rexx testOORexx -R ../bsf4rexx -s
echo running all tests - rexx testOORexx -R `pwd`/bsf4rexx -s ...
rexx testOORexx -R `pwd`/bsf4rexx -s
