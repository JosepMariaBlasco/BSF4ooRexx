# ---rgf, 2007-02-17; script to execute
# adjust the following path to your installation
# OOoBase=/opt/openOffice.org
OOoBase=/usr/lib/openoffice

export PATH=$PATH:$OOoBase/program
echo PATH=$PATH

# export CLASSPATH="$CLASSPATH:$OOoBase/program/classes/jurt.jar:$OOoBase/program/classes/unoil.jar:$OOoBase/program/classes/ridl.jar:$OOoBase/program/classes/juh.jar:."
export CLASSPATH="/usr/lib/openoffice/program/classes/jurt.jar:/usr/lib/openoffice/program/classes/unoil.jar:/usr/lib/openoffice/program/classes/ridl.jar:/usr/lib/openoffice/program/classes/juh.jar:.:$CLASSPATH"
echo CLASSPATH=$CLASSPATH

export OFFICE_HOME=$OOoBase
echo OFFICE_HOME=$OFFICE_HOME

# export UNO_PATH=$OOoBase/program
echo UNO_PATH=$UNO_PATH

java CreateTextDocument

