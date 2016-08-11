The sdk of jni4net comes with a tool named "proxygen.exe" which is used to process
the custom defined xml-file "oorexx.net.proxygen.xml". It will create Java and
matching Csharp files, which need to be compiled.

- The resulting Java files in the subdirectory "java" need be compiled and put into a 
  jar named "oorexx.net.jar" made available to Java (e.g. by adding it to the CLASSPATH).

  This will be done by "...\create.cmd" script (in ..).


- The Csharp file should be compiled into a "oorexx.net.dll" assembly which needs to
  be registered in the GAC (global assembly cache) using "gacutilrgf.exe" or the
  like. 

  There is a MS Visual Studio 2015 solution file "csharp\oorexx.net.sln" defined. Just
  load it and do a build. Copy the resulting "csharp\bin\Release\oorexx.net.dll" to
  "..\..\bin", like

     copy csharp\bin\Release\oorexx.net.dll ..\..\bin
 

---rgf, 2016-08-11
