#! /bin/sh

# rem point to root of testing framework and to the framework subdir
# export PATH=$PATH:/mnt/root_e/rony/dev/bsf/src/source_cc_40/testUnits
export PATH=$PATH:`pwd`:`pwd`/ooRexxUnit

# rem point to the root test cases for "bsf4rexx", needed for Java to find all Java classes compiled therein
# export CLASSPATH=$CLASSPATH:/mnt/root_e/rony/dev/bsf/src/source_cc_40/testUnits/bsf4rexx
export CLASSPATH=$CLASSPATH:`pwd`/bsf4rexx

