@rem 2007-01-31, 2007-02-02, 2007-10-17, 2008-07-12, 2010-07-25, 2014-05-25 ---rgf
setlocal
set tmprgf=ScriptProviderForooRexx

rem create OOo-Macro-Package which will contain everything, including BSF itself
@echo creating %tmprgf%.jar

@del  %tmprgf%.jar

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
zip -d %tmprgf%.jar "*tmp*"
@rem zip -d %tmprgf%.jar "*txt"
zip -d %tmprgf%.jar com/sun/star/script/framework/provider/oorexx/*.txt
@rem now re-add the deleted 'template.rxo'
zip %tmprgf%.jar com/sun/star/script/framework/provider/oorexx/template.rxo
zip -d %tmprgf%.jar "*tmp*"
zip -d %tmprgf%.jar "*work.tmp*"
zip -d %tmprgf%.jar "*patch*"
zip -d %tmprgf%.jar "*/bkp*"
zip -d %tmprgf%.jar "*/bkp/*"
zip -d %tmprgf%.jar "*bkp*"
zip -d %tmprgf%.jar "*.java"
zip -d %tmprgf%.jar "*~"

rem create source package
del %tmprgf%-source.jar
zip %tmprgf%-source.jar ScriptProviderForooRexxManifest.mf
zip %tmprgf%-source.jar com/sun/star/script/framework/provider/oorexx/*
zip -d %tmprgf%-source.jar "*.class"
zip -d %tmprgf%-source.jar "*bkp*"
zip -d %tmprgf%-source.jar "*~"

rem 20100725, rgf: use the now official OOo file extension ".oxt" instead of ".jar"
copy /y %tmprgf%.jar %tmprgf%.oxt
@rem @del  %tmprgf%.jar


@rem now create package
del %tmprgf%.oxt
jar vcf %tmprgf%.oxt %tmprgf%.jar

setlocal
cd bsf4oorexx.dev\ScriptProviderForooRexxPackage
jar vuf ../../%tmprgf%.oxt *
@rem pause
zip -d ../../%tmprgf%.oxt "*.svn*"
@rem zip ../../%tmprgf%.oxt META-INF/*
endlocal


rem to compile:
rem    add path to "ScriptFramework.jar", eg.
rem    set CLASSPATH=%CLASSPATH%;D:\Programme\OpenOffice.org 3\Basis\program\classes\ScriptFramework.jar
rem    set CLASSPATH=%CLASSpATH%;D:\Programme\OpenOffice 4\program\classes\ScriptFramework.jar

rem    compile using Java 1.4 class file format
rem    javac -source 1.4 -target 1.4 *.java

rem    if using bDebug, then generics are needed, hence use:
rem    javac -source 1.5 -target 1.5 *.java

