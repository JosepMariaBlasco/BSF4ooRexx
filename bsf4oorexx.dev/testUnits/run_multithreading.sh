#! /bin/sh
# run the bsf4rexx-testGroups
# rexx testOORexx -R ../bsf4rexx/rgf_multithreading -s

echo running \'bsf4rexx/rgf_multithreading\' tests - rexx testOORexx -R `pwd`/bsf4rexx/rgf_multithreading -s ...
rexx testOORexx -R `pwd`/bsf4rexx/rgf_multithreading -s
