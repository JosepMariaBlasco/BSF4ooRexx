The xml-file "oorexx.net.proxygen.xml" contains the definition of CLR (Common
Language Runtime) classes for which Java proxies should be created with the
help of the jni4net tool "proxygen.exe". According to the xml-definitions the
created Java source files will be placed in the subdirectory named "java" and
the created Csharp source files will be placed in the subdirectory named "csharp".

All Java files need to be compiled and made available via CLASSPATH (this will be
done in the script "..\create_oorexx_net.cmd").

All Csharp files need to be compiled into a dll which needs to be signed, such
that a "strong-named" assembly gets created which can therefore be installed
into the global assembly cache (GAC). The script "..\create_oorexx_net.cmd"
will do that taking advantage of an installed MS Visual Studio, so you may
want to adapt that script to your configuration. MS Visual studio can be used
to load the project by loading the file "csharp\oorexx.net.sln".

---rgf, 2016-08-13
