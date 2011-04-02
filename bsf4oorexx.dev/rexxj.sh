# created: 'created: "2011-03-01T09-05-51.528636" by user "rony"'
# script to run "BSF4ooRexx" programs without prior installation ...

# set BSF4REXX_HOME variable
BSF4REXX_HOME=/Users/rony/bsf4oorexx.201012/root201012/trunk/bsf4oorexx.dev

# set PATH environment variable
export PATH=$BSF4REXX_HOME:$PATH

# set CLASSPATH environment variable
[ "$CLASSPATH" != "" ] && TMPCP=:$CLASSPATH || TMPCP=
export CLASSPATH=.$TMPCP

# set DYLD_LIBRARY_PATH
[ "$DYLD_LIBRARY_PATH" != "" ] && TMPLLP=$DYLD_LIBRARY_PATH: || TMPLLP=
export DYLD_LIBRARY_PATH=$BSF4REXX_HOME:/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Libraries:$BSF4REXX_HOME/install/macosx/resources:$TMPLLP

# Java start command, allows passing arguments to Rexx script
/usr/bin/java -d32 -Djava.library.path="$DYLD_LIBRARY_PATH" -classpath "$CLASSPATH" org.rexxla.bsf.RexxDispatcher $*
