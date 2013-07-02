@rem 2005-01-17, start,  Rony G. Flatscher
@rem 2009-10-20, overhauled for BSF4ooRexx
@rem 2010-07-24, added 'information' folder to regular installation with an intro to ooRexx, plus docs for rgf_util2.rex
@rem 2010-12-30/31, updated to new locations
@rem 2011-03-20, add MacOSX support

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
set CLASSPATH=%CLASSPATH%;D:\Programme\OpenOffice.org 3\program\;D:\Programme\OpenOffice.org 3\Basis\program\classes\unoil.jar;D:\Programme\OpenOffice.org 3\URE\java\ridl.jar;D:\Programme\OpenOffice.org 3\URE\java\jurt.jar;D:\Programme\OpenOffice.org 3\URE\java\juh.jar;D:\Programme\OpenOffice.org 3\Basis\program\classes\ScriptFramework.jar



set runDate=20130629
set doc_date=2013-06-29

@rem values to use for creating the BSF jarfile
@rem set bsfDate=20090910
set bsfDate=20130629
@rem set bsfVersion=v400
set bsfVersion=v411
set bsfJarNameBin=bsf-%bsfVersion%-%bsfDate%.jar
set bsfJarNameSource=bsf-%bsfVersion%-%bsfDate%-source.jar

set oorexx-uno=oorexx-uno.jar

set baseDir=%cd%
set devName=bsf4oorexx.dev
set path2dev=%baseDir%\%devName%
echo baseDir=%baseDir%
echo path2dev=%path2dev%
pause


@rem -----------------------------------------------------------------------------------
@echo deleting old jar-files...
del BSF4ooRexx-apache-bsf.jar BSF4ooRexx-apache-bsf-source.jar bsf-*.jar

@echo creating %bsfJarNameBin% ...
@rem jar cvMf %bsfJarNameBin% org/apache/bsf/
@rem rgf, 20090815 jar cvMf %bsfJarNameBin% org/apache/bsf org/apache/taglibs
jar cvMf %bsfJarNameBin% org/apache/bsf org/apache/taglibs org/objectweb/ org/codehaus/
zip -d %bsfJarNameBin% "*.svn*" "*bkp*" "*tmp*"

rem rgf, 20070917, not needed, we have BSF4ooRexx-apache-bsf-source.jar instead
@rem @echo creating %bsfJarNameSource% ...
rem copy %bsfJarNameBin% %bsfJarNameSource%

@rem now remove sources from bin
zip -d %bsfJarNameBin% "*.java" "*.txt" "*.cmd" "*~" "*bkp*" "*tmp*"
zip -9  %bsfJarNameBin% ApacheLicense2.0.txt
zip -9  %bsfJarNameBin% asm_LICENSE.TXT
zip -9  %bsfJarNameBin% janino_LICENSE.TXT

@rem remove compiled classes from source
rem zip -d %bsfJarNameSource% "*.class"  "*~"
rem zip -9 %bsfJarNameSource% ApacheLicense2.0.txt


call createScriptProviderForooRexx.cmd

@echo creating BSF Rexx engine jar file, containing the OOo support and datergf...
jar cvMf bsf-rexx-engine.jar org/rexxla/ org/oorexx/
rem zip -d bsf-rexx-engine.jar "*.svn*" "*.html" "*.css" "*.txt"
zip -d bsf-rexx-engine.jar "*.svn*"
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
zip -9r bsf-rexx-engine.jar ApacheLicense2.0.txt
zip -9r bsf-rexx-engine.jar asm_LICENSE.TXT
zip -9r bsf-rexx-engine.jar janino_LICENSE.TXT

@echo creating main-jar file...
jar cvMf BSF4ooRexx-apache-bsf.jar org/

@echo removing unneeded entries
zip -d BSF4ooRexx-apache-bsf.jar org/apache/commons
zip -d BSF4ooRexx-apache-bsf.jar "*.svn*"
zip -d BSF4ooRexx-apache-bsf.jar "*bkp*"
zip -d BSF4ooRexx-apache-bsf.jar "*tmp*"
zip -d BSF4ooRexx-apache-bsf.jar "*~"

rem make sure the ApacheLicense2.0.txt file is available
zip -9r BSF4ooRexx-apache-bsf.jar ApacheLicense2.0.txt
zip -9r BSF4ooRexx-apache-bsf.jar  asm_LICENSE.TXT
zip -9r BSF4ooRexx-apache-bsf.jar  janino_LICENSE.TXT
@rem pause

@echo creating the binary and source jar-files ...
copy BSF4ooRexx-apache-bsf.jar BSF4ooRexx-apache-bsf-source.jar

zip -9r BSF4ooRexx-apache-bsf-source.jar com/sun/

zip -d BSF4ooRexx-apache-bsf-source.jar "*.svn*"
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
call make-docs-apache.cmd
@echo deleting old zip-archives for Javadocs ...
del docs_apache_bsf.zip docs_bsf4oorexx.zip

@echo creating zip-archives for Javadocs
zip -9r docs_apache_bsf docs.apache.bsf/*
zip -9r docs_apache_bsf ApacheLicense2.0.txt

zip -9r docs_bsf4oorexx docs.bsf4oorexx/*
zip -9r docs_bsf4oorexx ApacheLicense2.0.txt
@echo finished.
@rem pause


@rem -----------------------------------------------------------------------------------
@echo deleting install directory
rm -rf bsf4oorexx

@echo creating install directory and populating it ...
md bsf4oorexx
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

md bsf4oorexx\information
md bsf4oorexx\Templates


copy *licens*.txt                                  bsf4oorexx\
copy bsf-*.jar                                     bsf4oorexx\
copy ScriptProviderForooRexx.oxt                   bsf4oorexx\install\
@rem copy ScriptProviderForooRexx.jar                   bsf4oorexx\install\
copy %path2dev%\bin\add*.r*                        bsf4oorexx\
copy %path2dev%\bin\BSF*                           bsf4oorexx\
copy %path2dev%\bin\rexxj2*                        bsf4oorexx\
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

copy %path2dev%\information\*                      bsf4oorexx\information

copy %path2dev%\install\*                          bsf4oorexx\install\
copy %path2dev%\install\linux\*                    bsf4oorexx\install\linux\
copy %path2dev%\install\linux\resources\*          bsf4oorexx\install\linux\resources\
copy %path2dev%\install\linux\resources\menu\*     bsf4oorexx\install\linux\resources\menu\
xcopy /s %path2dev%\install\linux\resources\xdg-utils\* bsf4oorexx\install\linux\resources\xdg-utils\
copy %path2dev%\install\windows\*                  bsf4oorexx\install\windows\

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

md  bsf4oorexx\install\32
copy /y %path2dev%\source_cc\32\*.dll              bsf4oorexx\install\32\
copy /y %path2dev%\source_cc\32\lib*.so            bsf4oorexx\install\32\
copy /y %path2dev%\source_cc\32\lib*.dylib         bsf4oorexx\install\32\

md  bsf4oorexx\install\64
copy /y %path2dev%\source_cc\64\*.dll              bsf4oorexx\install\64\
copy /y %path2dev%\source_cc\64\lib*.so            bsf4oorexx\install\64\
copy /y %path2dev%\source_cc\64\lib*.dylib         bsf4oorexx\install\64\

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
md bsf4oorexx\utilities\OOo
md bsf4oorexx\utilities\OOo\UNO_API_info

xcopy %path2dev%\utilities\*                       bsf4oorexx\utilities\
xcopy %path2dev%\utilities\OOo\*                   bsf4oorexx\utilities\OOo\
xcopy %path2dev%\utilities\OOo\UNO_API_info\*      bsf4oorexx\utilities\OOo\UNO_API_info\


@echo cleaning samples directories ...
del samples\OOo\*.jpg
del samples\OOo\*.pdf
xcopy samples\*                         bsf4oorexx\samples\   /s/e/i/r/k/y
@rem 2009-02-11, rgf
rm -rf bsf4oorexx\samples\OOo\tmp

del BSF4ooRexx_install.zip
zip -9r BSF4ooRexx_install bsf4oorexx/*
zip -d  BSF4ooRexx_install "*.svn*" "*bkp*" "*tmp*" "*cachedir*" "*~"
zip -9r BSF4ooRexx_install ApacheLicense2.0.txt
zip -9r BSF4ooRexx_install asm_LICENSE.TXT
zip -9r BSF4ooRexx_install janino_LICENSE.TXT

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
@rem zip -d BSF4ooRexx_samples "*.svn*" "*bkp*" "*tmp*" "*cachedir*" "*~"
@rem @pause


@rem -----------------------------------------------------------------------------------
@echo creating test archive ...
del BSF4ooRexx_tests.zip
zip -9r BSF4ooRexx_tests test/*
zip -9r BSF4ooRexx_tests ApacheLicense2.0.txt
zip -d BSF4ooRexx_tests "*.svn*" "*bkp*" "*_bkp*" "*tmp*"  "*~"
@rem pause


@rem -----------------------------------------------------------------------------------
@echo creating testUnits archive ...
del BSF4ooRexx_testUnits.zip
zip -9r BSF4ooRexx_testUnits %devName%/testUnits/*
zip -9r BSF4ooRexx_testUnits ApacheLicense2.0.txt
zip -d  BSF4ooRexx_testUnits "*.svn*" "*bkp*" "*_bkp*" "*tmp*"  "*~"
@rem pause


@rem -----------------------------------------------------------------------------------
@echo creating source archive ...
del BSF4ooRexx_sources.zip
zip -9 BSF4ooRexx_sources ApacheLicense2.0.txt
zip -9 BSF4ooRexx_sources asm_LICENSE.TXT
zip -9 BSF4ooRexx_sources janino_LICENSE.TXT

zip -9 BSF4ooRexx_sources *.txt
zip -9 BSF4ooRexx_sources *.cmd
zip -9 BSF4ooRexx_sources *.mf
zip -9r BSF4ooRexx_sources com/sun/*
zip -9r BSF4ooRexx_sources org/*
zip -9r BSF4ooRexx_sources %devName%/*

zip -d BSF4ooRexx_sources "*.svn*"  "*bkp*" "*tmp*" "*~"
zip -d BSF4ooRexx_sources "*class" "*dll" "*lib" "*pdf" "*exe"
zip -d BSF4ooRexx_sources "*zip" "*.o" "*obj" "*exp" "*swp" "*so"

@rem pause

dir *.jar *.zip











