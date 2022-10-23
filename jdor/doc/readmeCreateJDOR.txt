rem 2022-10-03, rgf

rem Java 8 (Windows), assuming in "dev" directory:

   rd tmp /S /q
   md tmp
   md tmp\src
   md tmp\doc

   copy /y samples\*.r*    F:\work\svn\bsf4oorexx\branches\850\samples
   copy /y samples\*.cls   F:\work\svn\bsf4oorexx\branches\850\samples
   copy /Y doc\*html F:\work\svn\bsf4oorexx\branches\850\bsf4oorexx.dev\information\jdor
   copy /Y doc\*png  F:\work\svn\bsf4oorexx\branches\850\bsf4oorexx.dev\information\jdor

   copy /y doc\*html tmp\doc
   copy /y doc\*png tmp\doc
   copy /y readm*.txt doc
   copy /y readm*.txt tmp\doc

   javac JavaDrawingHandler.java -d tmp
   copy /y JavaDrawingHandler.java tmp\src
   copy /y JavaDrawingHandler.java F:\work\svn\bsf4oorexx\branches\850\bsf4oorexx.dev\source_java
   cd tmp
   jar -cvf JDOR_100_20221005.jar *
   copy /y *jar ..
   del *jar
   cd ..

