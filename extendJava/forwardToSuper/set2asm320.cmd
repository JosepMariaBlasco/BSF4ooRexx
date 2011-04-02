@echo off
rem rgf, 2009-06-27
set asm_home=F:\download\java\asm\asm-3.2\lib

set asm_bare=%asm_home%\asm-3.2.jar;%asm_home%\asm-util-3.2.jar
set asm_all=%asm_bare%;%asm_home%\asm-analysis-3.2.jar;%asm_home%\asm-commons-3.2.jar;%asm_home%\asm-tree-3.2.jar;%asm_home%\asm-xml-3.2.jar

set CLASSPATH=%asm_all%;e:\rony\dev\bsf\src;.;D:\Programme\Java\jre6\lib\ext\QTJava.zip
rem set CLASSPATH=%asm_bare%;e:\rony\dev\bsf\src;.;D:\Programme\Java\jre6\lib\ext\QTJava.zip

rem asm-3.2.jar
rem asm-analysis-3.2.jar
rem asm-commons-3.2.jar
rem asm-tree-3.2.jar
rem asm-util-3.2.jar
rem asm-xml-3.2.jar




