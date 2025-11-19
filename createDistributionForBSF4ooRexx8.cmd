@echo off
@rem 2005-01-17, start,  Rony G. Flatscher
@rem 2009-10-20, overhauled for BSF4ooRexx
@rem 2010-07-24, added 'information' folder to regular installation with an intro to ooRexx, plus docs for rgf_util2.rex
@rem 2010-12-30/31, updated to new locations
@rem 2011-03-20, add MacOSX support
@rem 2014-05-25, create one jar only, containing both, bsf package et.al. and rexx engine
@rem 2015-05-12, adding JSR-223 support
@rem 2016-08-15, adding .NET/CLR support
@rem 2016-11-24, adding JavaFX support, basing on Java 1.6/6.0, hence version "600" (6.00)
@rem 2021-07-30, copy new NOTICE.txt to bsf4rexx
@rem 2022-09-29, TODO: add module-info.{java|class} to allow package to be used as a module
@rem 2024-06-06, rgf: add /y to xcopy to overwrite destination without prompting
@rem                   not called anymore: createScriptProviderForooRexx.cmd
@rem 2025-09-02, rgf: copy new rexxb4r.rex to bsf4oorexx directory

@rem @echo createDistribution.cmd (for BSF4ooRexx)
@echo %0 (for BSF4ooRexx)
@echo .
@rem works with Java 1.5 as well @echo You need Java 1.4 (for Javadoc because of switch "-subpackages") and
@echo CLASSPATH point to the OOo jars...
@echo .
@rem pause

setlocal
@echo setting environment to Java-based scripting languages ...
call e:\java\scriptJars\addEngines2cp.cmd

@REM  setup CLASSPATH environment variable (rgf, 20101231) ...
@echo adding junit and Xalan to CLASSPATH
set CLASSPATH=.;%CLASSPATH%;%cd%\jars\Xalan\junit.jar;%cd%\jars\Xalan\xalan.jar

@echo adding OOo to CLASSPATH
set ooo_classes=c:\Program Files (x86)\OpenOffice 4\program\classes
set CLASSPATH=%CLASSPATH%;%ooo_classes%\unoil.jar;%ooo_classes%\ridl.jar;%ooo_classes%\jurt.jar;%ooo_classes%\juh.jar;%ooo_classes%\ScriptFramework.jar


@echo adding javax.servlet.jsp and javax.servlet.http to CLASSPATH
set CLASSPATH=%CLASSPATH%;F:\download\java\java2s.com-javax.servlet.jsp\javax.servlet.jsp.jar;F:\download\java\java2s.com-javax.servlet.jsp\javax.servlet-3.0.0.v201112011016.jar

@rem do not forget to set the version date in bsf4oorexx.dev\bin\BSF.CLS accordingly as well!
set releaseDate=20251119
set runDate=20251119
set doc_date=20251119

@rem values to use for creating the BSF jarfile
@rem set bsfDate=20090910
@rem set bsfVersion=v400
@rem set releaseVersion=v452
@rem 6...baseline Java 1.6/6, ...41 ... baseline ooRexx 4.1
set releaseVersion=v850
set releaseName=bsf4ooRexx-%releaseVersion%-%releaseDate%
set bsfJarNameBin=%releaseName%-bin.jar
set bsfJarNameSource=%releaseName%-source.jar
set releaseInstallZip=BSF4ooRexx_install_%releaseVersion%-%releaseDate%

set oorexx-uno=oorexx-uno.jar

set baseDir=%cd%
set devName=bsf4oorexx.dev
set path2dev=%baseDir%\%devName%
echo baseDir=%baseDir%
echo path2dev=%path2dev%
pause


@rem -----------------------------------------------------------------------------------
@echo removing all Windows (security) attributes from the files; English Windows versions should use: cacls * /t /g everyone:f
goto continue
rem cacls *   /t /g everyone:f
cacls * /t /g jeder:f

@rem -----------------------------------------------------------------------------------
:continue
@echo deleting old jar-files...
del BSF4ooRexx-apache-bsf.jar BSF4ooRexx-apache-bsf-source.jar bsf-*.jar

@echo creating %bsfJarNameBin% ...
@rem jar cvMf %bsfJarNameBin% org/apache/bsf/
@rem rgf, 20090815 jar cvMf %bsfJarNameBin% org/apache/bsf org/apache/taglibs
jar cvMf %bsfJarNameBin% org/apache/bsf org/apache/taglibs org/codehaus/ org/rexxla/ org/oorexx/
jar uf   %bsfJarNameBin% META-INF/
@echo done - %bsfJarNameBin%
@rem pause

zip -d %bsfJarNameBin% "*.svn*" "*bkp*" "*tmp*" "*.idea*" "bin.iml"

rem rgf, 20070917, not needed, we have BSF4ooRexx-apache-bsf-source.jar instead
@rem @echo creating %bsfJarNameSource% ...
rem copy %bsfJarNameBin% %bsfJarNameSource%

@rem now remove sources from bin
zip -d  %bsfJarNameBin% "*.java" "*.txt" "*.cmd" "*~" "*bkp*" "*tmp*"

@rem add license files
zip -9  %bsfJarNameBin% Apache_LICENSE_2.0.txt
rem zip -9  %bsfJarNameBin% asm_LICENSE.TXT
zip -9  %bsfJarNameBin% janino_LICENSE.TXT
zip -9  %bsfJarNameBin% xdg-utils_freedesktop_org_LICENSE.txt
zip -9  %bsfJarNameBin% jni4net-MIT-LICENSE.txt
zip -9  %bsfJarNameBin%  NOTICE*

@echo deleted sources - done - %bsfJarNameBin%
pause

@rem remove compiled classes from source
rem zip -d %bsfJarNameSource% "*.class"  "*~"
rem zip -9 %bsfJarNameSource% ApacheLicense2.0.txt


@rem 2022-01-29, rgf: from now on do not create the script provider automatically
@rem call createScriptProviderForooRexx.cmd

@echo creating BSF Rexx engine jar file, containing the OOo support and datergf...
jar cvMf bsf-rexx-engine.jar org/rexxla/ org/oorexx/
jar uf   bsf-rexx-engine.jar META-INF/

@echo done - createScriptProviderForooRexx -  bsf-rexx-engine.jar
rem pause
rem zip -d bsf-rexx-engine.jar "*.svn*" "*.html" "*.css" "*.txt"
zip -d bsf-rexx-engine.jar "*.svn*"
zip -d bsf-rexx-engine.jar "*.idea*"
zip -d bsf-rexx-engine.jar "*.css"
zip -d bsf-rexx-engine.jar "*.html"
zip -d bsf-rexx-engine.jar "*.java"
zip -d bsf-rexx-engine.jar "*bkp*"
zip -d bsf-rexx-engine.jar "*tmp*"
zip -d bsf-rexx-engine.jar "*cmd"
zip -d bsf-rexx-engine.jar "*txt"
zip -d bsf-rexx-engine.jar "*zip"
zip -d bsf-rexx-engine.jar "*~"
zip -d bsf-rexx-engine.jar "/*cmd"
zip -d bsf-rexx-engine.jar "/*txt"
zip -d bsf-rexx-engine.jar "/*zip"
zip -d bsf-rexx-engine.jar "/*~"
rem make sure the ApacheLicense2.0.txt file is available
zip -9r bsf-rexx-engine.jar Apache_LICENSE_2.0.txt
rem zip -9r bsf-rexx-engine.jar asm_LICENSE.TXT
zip -9r bsf-rexx-engine.jar janino_LICENSE.TXT

@echo creating main-jar file...
jar cvMf BSF4ooRexx-apache-bsf.jar org/
@rem jar uf   BSF4ooRexx-apache-bsf.jar META-INF/
@rem pause

@echo removing unneeded entries
zip -d BSF4ooRexx-apache-bsf.jar org/apache/commons
zip -d BSF4ooRexx-apache-bsf.jar "*.svn*"
zip -d BSF4ooRexx-apache-bsf.jar "*.idea*"
zip -d BSF4ooRexx-apache-bsf.jar "*bkp*"
zip -d BSF4ooRexx-apache-bsf.jar "*tmp*"
zip -d BSF4ooRexx-apache-bsf.jar "*~"

rem make sure the ApacheLicense2.0.txt file is available
zip -9r BSF4ooRexx-apache-bsf.jar Apache_LICENSE_2.0.txt
@rem zip -9r BSF4ooRexx-apache-bsf.jar  asm_LICENSE.TXT
@rem pause

@echo creating the binary and source jar-files ...
copy BSF4ooRexx-apache-bsf.jar BSF4ooRexx-apache-bsf-source.jar

zip -9r BSF4ooRexx-apache-bsf-source.jar com/sun/

zip -d BSF4ooRexx-apache-bsf-source.jar "*.svn*"
zip -d BSF4ooRexx-apache-bsf-source.jar "*.idea*"
zip -d BSF4ooRexx-apache-bsf-source.jar "*bkp*"
zip -d BSF4ooRexx-apache-bsf-source.jar "*tmp*"
zip -d BSF4ooRexx-apache-bsf-source.jar "*~"

zip -d BSF4ooRexx-apache-bsf-source.jar "*.class"
zip -d BSF4ooRexx-apache-bsf-source.jar "*~"

rem 2007-01-31, rgf: not needed anymore!
rem zip -d BSF4ooRexx-apache-bsf.jar        "*.java"
rem zip -d BSF4ooRexx-apache-bsf.jar        "*.cmd"
rem zip -d BSF4ooRexx-apache-bsf.jar        "*.txt"
rem zip -d BSF4ooRexx-apache-bsf-source.jar "*.class"

del BSF4ooRexx-apache-bsf.jar

dir *.jar
@echo finished.
@rem pause


@rem -----------------------------------------------------------------------------------
@echo creating Javadocs ...
@echo [use at least Java 1.5 (5.0) and make OOo class files available via classpath!]
set DIR_DOCS_APACHE_BSF=docs.apache.bsf
set DIR_DOCS_BSF4OOREXX=docs.bsf4oorexx
set ZIP_DOCS_APACHE_BSF=docs_apache_bsf.zip
set ZIP_DOCS_BSF4OOREXX=docs_bsf4oorexx.zip
call make-docs-apache.cmd 2>javadoc_errors.txt
@echo deleting old zip-archives for Javadocs ...
del docs_apache.bsf.zip docs_bsf4oorexx.zip

@echo creating zip-archives for Javadocs
copy ApacheLicense2.0.txt %DIR_DOCS_APACHE_BSF%
zip -9r %ZIP_DOCS_APACHE_BSF% %DIR_DOCS_APACHE_BSF%/*
rem zip -9r %ZIP_DOCS_APACHE_BSF% ApacheLicense2.0.txt

copy ApacheLicense2.0.txt %DIR_DOCS_BSF4OOREXX%
zip -9r %ZIP_DOCS_BSF4OOREXX% %DIR_DOCS_BSF4OOREXX%/*
@echo finished.
@rem pause


@rem -----------------------------------------------------------------------------------
@echo deleting install directory
@rem rm -rf bsf4oorexx
rd /s /q bsf4oorexx

@echo creating install directory and populating it ...
md bsf4oorexx
@rem new 20221004
md bsf4oorexx\lib
md bsf4oorexx\install
md bsf4oorexx\install\linux
md bsf4oorexx\install\linux\resources
md bsf4oorexx\install\linux\resources\xdg-utils
md bsf4oorexx\install\linux\resources\menu
md bsf4oorexx\install\windows

md bsf4oorexx\install\images
md bsf4oorexx\install\images\windows
md bsf4oorexx\install\images\windows\windows6

md bsf4oorexx\install\images\linux
md bsf4oorexx\install\images\linux\Crystal
md bsf4oorexx\install\images\linux\gnome
md bsf4oorexx\install\images\linux\hicolor
md bsf4oorexx\install\images\linux\nuvola
md bsf4oorexx\install\images\linux\oxygen
md bsf4oorexx\install\images\linux\Tango

md bsf4oorexx\install\portable

md bsf4oorexx\information
md bsf4oorexx\Templates

@rem --------- rgf, 20160815, .NET/CLR support
md bsf4oorexx\clr
copy %path2dev%\oorexx.net\bin                     bsf4oorexx\clr
xcopy %path2dev%\oorexx.net\samples                bsf4oorexx\samples\ /s /y

@rem --------- rgf, 20220920, xcopy doc dirs directly to target information directory
xcopy %DIR_DOCS_APACHE_BSF%                        bsf4oorexx\information\%DIR_DOCS_APACHE_BSF%\ /S /y
@echo.
xcopy %DIR_DOCS_BSF4OOREXX%                        bsf4oorexx\information\%DIR_DOCS_BSF4OOREXX%\ /S /y
@echo.
@echo done copying %DIR_DOCS_BSF4OOREXX% to bsf4oorexx\information\%DIR_DOCS_BSF4OOREXX%\
@echo.

xcopy %path2dev%\oorexx.net\docs                   bsf4oorexx\information\ /s /y
@echo pause just for debugging .NET/CLR support so far

copy *LICENS*.txt                                  bsf4oorexx\
copy NOTICE.txt                                    bsf4oorexx\
@rem copy bsf-*.jar                                bsf4oorexx\
copy %bsfJarNameBin%                               bsf4oorexx\lib\
copy ScriptProviderForooRexx.oxt                   bsf4oorexx\install\
@rem copy ScriptProviderForooRexx.jar                   bsf4oorexx\install\
copy %path2dev%\bin\add*.r*                        bsf4oorexx\
copy %path2dev%\bin\BSF*                           bsf4oorexx\
copy %path2dev%\bin\rexxj2*                        bsf4oorexx\
copy %path2dev%\bin\rexxjh*                        bsf4oorexx\
copy %path2dev%\bin\rexxb4*                        bsf4oorexx\
copy %path2dev%\bin\rexxcUtil.rex                  bsf4oorexx\
copy %path2dev%\bin\rgf_util2.rex                  bsf4oorexx\
copy %path2dev%\bin\libBS*                         bsf4oorexx\
copy %path2dev%\bin\UNO*                           bsf4oorexx\

copy %path2dev%\images\windows                     bsf4oorexx\install\images\windows
copy %path2dev%\images\windows\windows6            bsf4oorexx\install\images\windows\windows6

copy %path2dev%\images\linux                       bsf4oorexx\install\images\linux
copy %path2dev%\images\linux\Crystal               bsf4oorexx\install\images\linux\Crystal
copy %path2dev%\images\linux\gnome                 bsf4oorexx\install\images\linux\gnome
copy %path2dev%\images\linux\hicolor               bsf4oorexx\install\images\linux\hicolor
copy %path2dev%\images\linux\nuvola                bsf4oorexx\install\images\linux\nuvola
copy %path2dev%\images\linux\oxygen                bsf4oorexx\install\images\linux\oxygen
copy %path2dev%\images\linux\Tango                 bsf4oorexx\install\images\linux\Tango


copy %path2dev%\Templates\*                        bsf4oorexx\Templates

xcopy %path2dev%\information\*                     bsf4oorexx\information\ /s /y

copy %path2dev%\install\*                          bsf4oorexx\install\
copy %path2dev%\install\linux\*                    bsf4oorexx\install\linux\
copy %path2dev%\install\linux\resources\*          bsf4oorexx\install\linux\resources\
copy %path2dev%\install\linux\resources\menu\*     bsf4oorexx\install\linux\resources\menu\
xcopy /s /y %path2dev%\install\linux\resources\xdg-utils\* bsf4oorexx\install\linux\resources\xdg-utils\
copy %path2dev%\install\windows\*                  bsf4oorexx\install\windows\

copy %path2dev%\install\portable\*                 bsf4oorexx\install\portable

del bsf4oorexx\install\*.html

md bsf4oorexx\install\OOo_ScriptLibraries
md bsf4oorexx\install\OOo_ScriptLibraries\oorexx
md bsf4oorexx\install\OOo_ScriptLibraries\javascript
md bsf4oorexx\install\OOo_ScriptLibraries\python

set tmpPath=install\OOo_ScriptLibraries\oorexx\ooRexx_examples
md  bsf4oorexx\%tmpPath%
copy %path2dev%\%tmpPath%\*                        bsf4oorexx\%tmpPath%\

set tmpPath=install\OOo_ScriptLibraries\javascript\HowtoCreateApiInfo
md  bsf4oorexx\%tmpPath%
copy %path2dev%\%tmpPath%\*                        bsf4oorexx\%tmpPath%\

set tmpPath=install\OOo_ScriptLibraries\python\pythonSamples
md  bsf4oorexx\%tmpPath%
copy %path2dev%\%tmpPath%\*                        bsf4oorexx\%tmpPath%\




copy /y %path2dev%\source_cc\associationChanged.exe     bsf4oorexx\install\

@rem since 20170109, rgf
md  bsf4oorexx\install\lib
copy /y %path2dev%\source_cc\lib\*                bsf4oorexx\install\lib\

md  bsf4oorexx\install\macosx
rem 20110320, rgf - universal install script, hence use the Linux versions
copy %path2dev%\install\linux\*                    bsf4oorexx\install\macosx\

copy "%path2dev%\ReferenceCards\BSF4ooRexx\*.pdf"  bsf4oorexx\
copy "%path2dev%\ReferenceCards\OOo\*.pdf"         bsf4oorexx\
copy /y "%path2dev%\ReferenceCards\BSF4ooRexx\*.pdf"  bsf4oorexx\information\
copy /y "%path2dev%\ReferenceCards\OOo\*.pdf"         bsf4oorexx\information\
copy /y bsf4oorexx\*pdf .


copy /y %path2dev%\chang*                          bsf4oorexx\install\
copy /y %path2dev%\chang*                          .

copy /y %path2dev%\readme-*                        bsf4oorexx\install\
rem copy /y %path2dev%\readme-*                    bsf4oorexx\
copy /y %path2dev%\readm*                          .

copy /y %path2dev%\credits.txt                     bsf4oorexx\install\
copy /y %path2dev%\credits.txt                     bsf4oorexx\
copy /y %path2dev%\credits.txt                     .


del bsf4oorexx\*bkp*
del bsf4oorexx\*diff


@echo creating utilities directory and populating it ...
md bsf4oorexx\utilities
md bsf4oorexx\utilities\ooRexxTryFX
md bsf4oorexx\utilities\OOo
md bsf4oorexx\utilities\OOo\UNO_API_info

xcopy /y %path2dev%\utilities\*                       bsf4oorexx\utilities\
xcopy    %path2dev%\utilities\ooRexxTryFX             bsf4oorexx\utilities\ooRexxTryFX\  /s/e/i/r/k/y
xcopy /y %path2dev%\utilities\OOo\*                   bsf4oorexx\utilities\OOo\
xcopy /y %path2dev%\utilities\OOo\UNO_API_info\*      bsf4oorexx\utilities\OOo\UNO_API_info\


@echo cleaning samples directories ...
del samples\OOo\*.jpg
del samples\OOo\*.pdf
xcopy samples\*                         bsf4oorexx\samples\   /s/e/i/r/k/y
@rem 2009-02-11, rgf

@rem 2023-07-18, rgf: make "jdor.cls" available via PATH (too many "escaped" JDOR-samples that use it)
@rem                  and remove it from installation's bsf4oorexx\samples
copy samples\jdor.cls                   bsf4oorexx
del  bsf4oorexx\samples\jdor.cls

del %releaseInstallZip%.zip
zip -9r %releaseInstallZip% bsf4oorexx/*
zip -d  %releaseInstallZip% "*.svn*" "*.idea*" "*bkp*" "*tmp*" "*cachedir*" "*~"  "*.idea*" "bin.iml"
zip -9r %releaseInstallZip% ApacheLicense2.0.txt
zip -9 %releaseInstallZip% NOTICE*

zip -d  %releaseInstallZip% "*samples/deprecated*" "*samples/LeePeedin/deprecated*"

@rem zip -d  BSF4ooRexx_install bsf4oorexx/samples/version4/

@echo finished.
@rem pause

@rem @rem -----------------------------------------------------------------------------------
@rem  as of 2006-01-17: part of the binary distribution
@rem @echo creating sample archive ...
@rem del BSF4ooRexx_samples.zip
@rem del samples\OOo\*.jpg
@rem del samples\OOo\*.pdf
@rem zip -9r BSF4ooRexx_samples samples/*
@rem zip -9r BSF4ooRexx_samples ApacheLicense2.0.txt
@rem zip -d BSF4ooRexx_samples "*.svn*" "*bkp*" "*tmp*" "*cachedir*" "*~"  "*.idea*" "bin.iml"
@rem @pause


@rem -----------------------------------------------------------------------------------
@echo creating test archive ...
del BSF4ooRexx_tests.zip
zip -9r BSF4ooRexx_tests test/*
zip -9r BSF4ooRexx_tests ApacheLicense2.0.txt
zip -d BSF4ooRexx_tests "*.svn*" "*.idea*" "*bkp*" "*_bkp*" "*tmp*"  "*~"  "*.idea*" "bin.iml"
@rem pause


@rem -----------------------------------------------------------------------------------
@echo creating testUnits archive ...
del BSF4ooRexx_testUnits.zip
zip -9r BSF4ooRexx_testUnits %devName%/testUnits/*
zip -9r BSF4ooRexx_testUnits ApacheLicense2.0.txt
zip -d  BSF4ooRexx_testUnits "*.svn*" "*.idea*" "*bkp*" "*_bkp*" "*tmp*"  "*~"  "*.idea*" "bin.iml"
@rem pause


@rem -----------------------------------------------------------------------------------
@echo creating source archive ...
del BSF4ooRexx_sources.zip
zip -9 BSF4ooRexx_sources ApacheLicense2.0.txt
zip -9 BSF4ooRexx_sources *LICENSE*
zip -9 BSF4ooRexx_sources NOTICE*

zip -9 BSF4ooRexx_sources *.txt
zip -9 BSF4ooRexx_sources *.cmd
zip -9 BSF4ooRexx_sources *.mf
zip -9r BSF4ooRexx_sources com/sun/*
zip -9r BSF4ooRexx_sources org/*
zip -9r BSF4ooRexx_sources %devName%/*
@rem 2022-01-29: add module-info.java to make it available
zip -9  BSF4ooRexx_sources module-info.java

zip -d BSF4ooRexx_sources "*.svn*" "*.idea*" "*bkp*" "*tmp*" "*~"   "*.idea*" "bin.iml"
zip -d BSF4ooRexx_sources "*class" "*dll" "*lib" "*pdf" "*exe"
zip -d BSF4ooRexx_sources "*zip" "*.o" "*obj" "*exp" "*swp" "*so"

@rem pause

dir *.jar *.zip











