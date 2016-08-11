The sdk of jni4net comes with a tool named "proxygen.exe" which is used to process
the custom defined xml-file "oorexx.net.proxygen.xml". It will create a Java and
a matching Csharp file, which need to be compiled.

- The resulting Java file should be compiled and put into a jar named "oorexx.net.jar"
  and made available to Java (e.g. by adding it to the CLASSPATH).

- The Csharp file should be compiled into a "oorexx.net.dll" assembly which needs to
  be registered in the GAC (global assembly cache) using "gacutilrgf.exe" or the
  like.

---rgf, 2016-08-11
