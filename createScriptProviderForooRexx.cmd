@echo off
@rem 2007-01-31, 2007-02-02, 2007-10-17, 2008-07-12, 2010-07-25, 2014-05-25 ---rgf
setlocal
set tmprgf=ScriptProviderForooRexx

@echo adding OOo to CLASSPATH

:aoo
if not exist "c:\Program Files (x86)\OpenOffice 4\program\classes" goto libreoffice5
set ooo_classes=c:\Program Files (x86)\OpenOffice 4\program\classes
echo using AOO jar files ...
goto goahead

:libreoffice5
if not exist "C:\Program Files\LibreOffice 5\program\classes" goto libreoffice
set ooo_classes=C:\Program Files\LibreOffice 5\program\classes
echo using LibreOffice 5 jar files ...
goto goahead

rem Libreoffice 6 does not have version indicator in directory name
:libreoffice
set ooo_classes=C:\Program Files\LibreOffice\program\classes
echo using LibreOffice jar files ...

:goahead

echo rgf ooo_classes=%ooo_classes%

rem set CLASSPATH=%CLASSPATH%;%ooo_classes%\unoil.jar;%ooo_classes%\ridl.jar;%ooo_classes%\jurt.jar;%ooo_classes%\juh.jar;%ooo_classes%\ScriptFramework.jar

set CLASSPATH=.;%ooo_classes%\unoil.jar;%ooo_classes%\ridl.jar;%ooo_classes%\jurt.jar;%ooo_classes%\juh.jar;%ooo_classes%\ScriptFramework.jar

echo rgf CLASSPATH=%CLASSPATH%

@echo compiling ScriptProvider4ooRexx
javap com.sun.star.script.framework.provider.ClassLoaderFactory
@echo .
javap com.sun.star.script.framework.container.ScriptMetaData
@echo ...
javac -source 1.6 -target 1.6 com/sun/star/script/framework/provider/oorexx/*.java
@rem
pause


rem create OOo-Macro-Package which will contain everything, including BSF itself
@echo creating %tmprgf%.jar

@del  %tmprgf%.jar

rem echo jar cvmf %tmprgf%Manifest.mf %tmprgf%.jar Apache* com/sun/star/script/ org/apache/bsf/ org/oorexx/ org/rexxla/ org/apache/taglibs org/codehaus/
rem pause

jar cvmf %tmprgf%Manifest.mf %tmprgf%.jar Apache* com/sun/star/script/ org/apache/bsf/ org/oorexx/ org/rexxla/ org/apache/taglibs org/objectweb/ org/codehaus/
@ rem jar cvf %tmprgf%.jar Apache* com/sun/star/script/ org/apache/bsf/ org/oorexx/ org/rexxla/

rem jar cvmf %tmprgf%Manifest.mf %tmprgf%.jar Apache* com/sun/star/script/


zip -d %tmprgf%.jar "*.svn*"
zip -d %tmprgf%.jar "*.rex"
zip -d %tmprgf%.jar "*.rxo"
zip -d %tmprgf%.jar "*.cmd"
@rem zip -d %tmprgf%.jar "*.html"
zip -d %tmprgf%.jar "*zip"
zip -d %tmprgf%.jar "*diff*"
zip -d %tmprgf%.jar "*bkp*"
zip -d %tmprgf%.jar "*test*"
zip -d %tmprgf%.jar "*temp*"
zip %tmprgf%.jar com/sun/star/script/framework/provider/oorexx/template.rxo

zip -d %tmprgf%.jar "*tmp*"
@rem zip -d %tmprgf%.jar "*txt"
zip -d %tmprgf%.jar "com/sun/star/script/framework/provider/oorexx/*.txt"
zip -d %tmprgf%.jar "com/sun/star/script/framework/provider/oorexx/beanshell/*"
zip -d %tmprgf%.jar "com/sun/star/script/framework/provider/oorexx/experimental/*"
@rem now re-add the deleted 'template.rxo'
zip -d %tmprgf%.jar "*tmp*"
zip -d %tmprgf%.jar "*work.tmp*"
zip -d %tmprgf%.jar "*patch*"
zip -d %tmprgf%.jar "*/bkp*"
zip -d %tmprgf%.jar "*/bkp/*"
zip -d %tmprgf%.jar "*bkp*"
zip -d %tmprgf%.jar "*.java"
zip -d %tmprgf%.jar "*~"
zip -d %tmprgf%.jar "*_.swo"
zip -d %tmprgf%.jar "*.swp"

rem create source package
del %tmprgf%-source.jar
zip %tmprgf%-source.jar ScriptProviderForooRexxManifest.mf
zip %tmprgf%-source.jar com/sun/star/script/framework/provider/oorexx/*
zip -d %tmprgf%-source.jar "*.class"
zip -d %tmprgf%-source.jar "*bkp*"
zip -d %tmprgf%-source.jar "*~"
zip -d %tmprgf%-source.jar "com/sun/star/script/framework/provider/oorexx/*.txt"
zip -d %tmprgf%-source.jar "com/sun/star/script/framework/provider/oorexx/beanshell/*"
zip -d %tmprgf%-source.jar "com/sun/star/script/framework/provider/oorexx/experimental/*"
zip -d %tmprgf%-source.jar "*.swp"

rem 20100725, rgf: use the now official OOo file extension ".oxt" instead of ".jar"
copy /y %tmprgf%.jar %tmprgf%.oxt
@rem @del  %tmprgf%.jar


@rem now create package
@rem rgf 20161027 removed this block as it does not make sense to first create the oxt file and then delete and recreate it
@rem del %tmprgf%.oxt
@rem jar vcf %tmprgf%.oxt %tmprgf%.jar

setlocal
cd bsf4oorexx.dev\ScriptProviderForooRexxPackage
jar vuf ../../%tmprgf%.oxt *
@rem pause
zip -d ../../%tmprgf%.oxt "*.svn*"
zip -d ../../%tmprgf%.oxt "*.swp"
@rem zip ../../%tmprgf%.oxt META-INF/*
endlocal


rem to compile:
rem    add path to "ScriptFramework.jar", eg.

rem    if using bDebug, then generics are needed, hence use:
rem    javac -source 1.5 -target 1.5 *.java

rem since 2017-12, baseline is Java 1.6/6
rem    add path to "ScriptFramework.jar", eg.
rem    set CLASSPATH=%CLASSPATH%;c:\Program Files (x86)\OpenOffice 4\program\classes\ScriptFramework.jar
rem    javac -source 1.6 -target 1.6 *.java

rem 2018-03-12, rgf: it seems that the oxt must contain the jar as jar and not exploded! :(

zip -d %tmprgf%.oxt "org*"
zip -d %tmprgf%.oxt "com*"
@rem add jar file
jar vuf %tmprgf%.oxt %tmprgf%.jar



