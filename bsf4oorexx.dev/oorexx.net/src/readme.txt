This branch contains the sources of gacutilrgf.exe (csharp),
oorexx.net.jar (Java) and oorexx.net.dll (csharp).

For the csharp projects there are MS Visual Studio solution and project files
that can be used to inspect and (re-)create the dll and exe assemblies.

NOTE: for the assembly "oorexx.net.dll" it is mandatory that it gets signed
      in the process, otherwise jni4net will cause an error when trying to
      use this assembly (put into GAC e.g. with "gacutilrgf.exe" of this
      project)

To recreate the oorexx.net jar and assembly files, run

         create_oorexx_net.cmd

from this directory, which will use "javac" (on the PATH) and "msbuild.exe"
(adjust absolute path in the batch file) and copies the created jar, exe and
dll files to ..\bin.


---rgf, 2016-08-12



