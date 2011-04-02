# created: 'created: "2010-07-31T23-30-00.531356" by user "root"'
# script to run "BSF4ooRexx" programs without prior installation ...

# set BSF4REXX_HOME variable
BSF4REXX_HOME=/mnt/root_e/rony/dev/bsf/src/BSF4ooRexx.dev

# set CLASSPATH environment variable
[ "$CLASSPATH" != "" ] && TMPCP=:$CLASSPATH || TMPCP=
export CLASSPATH=.$TMPCP

# set LD_LIBRARY_PATH 
[ "$LD_LIBRARY_PATH" != "" ] && TMPLLP=$LD_LIBRARY_PATH: || TMPLLP=
export LD_LIBRARY_PATH=$TMPLLP$BSF4REXX_HOME:/usr/lib/jvm/java-6-openjdk/jre/lib/amd64/server

# Java start command, allows passing arguments to Rexx script
/usr/bin/java -Djava.library.path="$LD_LIBRARY_PATH" -classpath "$CLASSPATH" org.rexxla.bsf.RexxDispatcher $*
