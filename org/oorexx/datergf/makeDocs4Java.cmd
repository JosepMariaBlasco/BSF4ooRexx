set path2datergf=org\oorexx\datergf
set overviewFile=%path2datergf%\datergf_overview.html

set docDirName=%path2datergf%\datergf_docs
md  %docDirName%

set author=Rony G. Flatscher (c) 2001-2006 (r)
set name=datergf - Date/Time simple manipulations, arithmetics, formatting
set version=v0.92, 2001-04-07 (2006-01-01)

set targetfiles=%path2datergf%\*java

javadoc -author -version -overview %overviewFile% -public -d %docDirName% -use -windowtitle "%author%, %name%, %version%" -header "<em>%author%, %name%, %version% <strong>(Java)</strong></em>" %targetfiles%
rem javadoc -author -version -overview %overviewFile% -private -d %docDirName% -use -windowtitle "%author%, %name%, %version%" -header "<em>%author%, %name%, %version% <strong>(Java)</strong></em>" %targetfiles%

rem javadoc -author -version -package -d ..\docs -use -windowtitle "Date/Time RGF stuff, 2001-02...04" *.java
