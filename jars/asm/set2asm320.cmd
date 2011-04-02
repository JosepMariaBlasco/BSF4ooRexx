rem rgf, 2009-06-27, 2010-12-30
rem set asm_home=F:\download\java\asm\asm-3.2\lib
rem set asm_home=%cd%\..\jars\asm
set asm_home=%cd%

set asm_bare=%asm_home%\asm-3.2.jar;%asm_home%\asm-util-3.2.jar
set asm_all=%asm_bare%;%asm_home%\asm-analysis-3.2.jar;%asm_home%\asm-commons-3.2.jar;%asm_home%\asm-tree-3.2.jar;%asm_home%\asm-xml-3.2.jar

rem set CLASSPATH=%asm_all%;e:\rony\dev\bsf\src;.;D:\Programme\Java\jre6\lib\ext\QTJava.zip
rem set CLASSPATH=%asm_bare%;e:\rony\dev\bsf\src;.;D:\Programme\Java\jre6\lib\ext\QTJava.zip
set CLASSPATH=%asm_all%;%CLASSPATH%

rem asm-3.2.jar
rem asm-analysis-3.2.jar
rem asm-commons-3.2.jar
rem asm-tree-3.2.jar
rem asm-util-3.2.jar
rem asm-xml-3.2.jar




