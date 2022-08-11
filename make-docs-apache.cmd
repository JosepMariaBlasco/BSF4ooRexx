rem 2006-08-09, 2010-12-27 ---rgf
rem javadoc -package -d ./docs-apache/bsf -use -version -author -subpackages org.apache
rem javadoc -package -d ./docs-apache/rexxla -use -version -author -subpackages org.rexxla

rem 2019-08-13: turn off doclint from Java 8 on (cf. https://blog.joda.org/2014/02/turning-off-doclint-in-jdk-8-javadoc.html
rem             linked to from https://stackoverflow.com/questions/22528767/how-to-work-around-the-stricter-java-8-javadoc-when-using-maven
rem             Oracle javado7 tutorial: https://www.oracle.com/technetwork/articles/javase/index-137868.html
rem             https://docs.oracle.com/javase/6/docs/technotes/tools/windows/javadoc.html#@link

set docs_bsf4rexx_dir=./docs.bsf4oorexx
set docs_apache_dir=./docs.apache.bsf


set javadoc_scope=-package
rem set javadoc_scope=-private

set doc_date=%runDate%

set doc_release_version=%releaseVersion%
if not "%doc_release_version%"=="" goto set_doc_date
set doc_release_version=v850

:set_doc_date
if not "%doc_date%"=="" goto continue
set doc_date=20220810

:continue
set docs_header=-header "%doc_date%, rgf"

rd /s /q %docs_apache_dir%
javadoc  -Xdoclint:none %javadoc_scope% -d %docs_apache_dir% -use -version -author %docs_header% -subpackages org.apache


rd /s /q %docs_bsf4rexx_dir%
set wtitle="BSF4ooRexx %doc_release_version% with Add-ons, %doc_date%, Rony G. Flatscher"
javadoc  -Xdoclint:none %javadoc_scope% -d %docs_bsf4rexx_dir% -windowtitle %wtitle% -use -version -author %docs_header% -subpackages org.oorexx -subpackages org.rexxla -subpackages com.sun.star.script.framework.provider.oorexx

