javac -cp ".;%CLASSPATH%" --module-path out --add-modules mod_A,mod_B,mod_C TestUse.java

java  -cp ".;%CLASSPATH%" --module-path out --add-modules mod_A,mod_B,mod_C TestUse
