rem 2006-08-09, 2010-12-27 ---rgf
rem javadoc -package -d ./docs-apache/bsf -use -version -author -subpackages org.apache
rem javadoc -package -d ./docs-apache/rexxla -use -version -author -subpackages org.rexxla

set docs_bsf4rexx_dir=./docs.bsf4oorexx
set docs_apache_dir=./docs.apache.bsf

set doc_date=%runDate%

if not "%doc_date%"=="" goto continue
set doc_date=2012-02-24

:continue
set docs_header=-header "%doc_date%, rgf"

rm -rf %docs_apache_dir%
javadoc -package -d ./docs.apache.bsf                     -use -version -author %docs_header% -subpackages org.apache


rm -rf %docs_bsf4rexx_dir%
set wtitle="BSF4ooRexx 4.0 with Add-ons, %doc_date%, Rony G. Flatscher"
javadoc -package -d %docs_bsf4rexx_dir% -windowtitle %wtitle% -use -version -author %docs_header% -subpackages org.oorexx -subpackages org.rexxla -subpackages com.sun.star.script.framework.provider.oorexx

