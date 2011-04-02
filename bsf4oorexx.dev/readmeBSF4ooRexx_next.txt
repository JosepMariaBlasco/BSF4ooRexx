Additional information on BSF4ooRexx that may be interesting
============================================================

BSF4ooRexx is an extension to Rexx and ooRexx, consisting of an external Rexx
function package ("BSF4ooRexx.dll", "libBSF4ooRexx") and a set of Java classes
stored in a Java archive.

BSF allows Java programs to call (invoke) script programs written in languages
other than Java.  Such script programs may also be able to call back into Java
and use all of Java's classes and objects.  (cf. <http://jakarta.apache.org/bsf>)

- With "BSF4ooRexx" any Java application can use Open Object Rexx (ooRexx) as
  a scripting language.

- Open Object Rexx (ooRexx) can use Java as a huge, operating system independent
  function library.  The special ooRexx support enabled by requiring "BSF.CLS"
  makes Java appear like a huge Object Rexx class library!

Preliminary versions of "The Vienna Version of BSF4ooRexx" package were partly
introduced in April 2005 and April 2006 to the Rexx world at the International
Rexx Symposium organized by the Rexx Language Association (cf.
<http://www.RexxLA.org>) where it was used for enabling ooRexx to automate
(remote control) OpenOffice.org in a platform independent manner.

An overview article (dated: 2003-06-01) with short examples can be found at:

   <http://wi.wu-wien.ac.at/rgf/rexx/orx14/orx14_bsf4rexx-av.pdf>

(and for the forerunner at:

  <http://wi.wu-wien.ac.at/rgf/rexx/orx12/JavaBeanScriptingWithRexx_orx12.pdf>).


Another (newer) document on BSF4ooRexx can be found at:

  <http://wi.wu-wien.ac.at/rgf/rexx/orx15/2004_orx15_bsf-orx-layer.pdf>


The "Vienna version of BSF4ooRexx" adds additional functionality, the most notable
addition allows ooRexx programs to address Java fields as if they were Object
Rexx attributes, as well as supplying the functions box() and unbox() to wrap
and unwrap primitive datatypes into/from their corresponding Java class (e.g.
needed for setting property values in OpenOffice.org - OOo).

At the International Rexx Symposium in May 2009 an outlining talk was given about
"BSF4Rexx 4.0" which now has been renamed to "BSF4ooRexx 4.0".

All Rexx programs using BSF4ooRexx are per se fully portable, ie.  to any system
which possesses Java and a Rexx interpreter, provided that the JNI DLL/shared
library is ported to that platform (the source file to be ported/compiled is
"BSF4ooRexx.cc").  Out of the box the following operating systems are supported:
Linux (Intel) and Windows, with the Mac OSX distribution of ooRexx incorporating
BSF4Rexx.



Files and Archives
==================

By convention BSF4ooRexx scripts carry the extension ".rxj" (Rexx-Java),
scripts that drive OpenOffice.org carry the extension ".rxo" (Rexx-OpenOffice).


readmeBSF4ooRexx.txt ... text file, outlining the installation of BSF4ooRexx

readmeBSF4ooRexx_next.txt ... this text file, explaining BSF4ooRexx and its installation

readmeOOo.txt        ... text file, outlining the installation of the OpenOffice support

readmeOOo_next.txt   ... text file, explaining the OpenOffice.org/Staroffice
                         support and its installation

BSF4ooRexx_install.zip ... Archive that contains the binary distribution of, the
                         sources of and the samples for BSF4ooRexx (including
                         OpenOffice.org samples), the setup scripts for Linux
                         and Windows


changesBSF4ooRexx.txt  ... text file containing a record of the most important changes

changesOOo.txt       ... text file containing a record of the most important
                         changes since the 2005 International Rexx symposium

ApacheLicense2.0.txt ... Apache license 2.0 text


BSF4ooRexx_sources.zip ... Archive that contains the C++ and Java sources

docs_bsf4rexx.zip    ... Archive that contains the documentation of the BSF4ooRexx
                         Java programs, including add-ons

docs_apache_bsf.zip  ... Archive that contains the documentation of Apache's BSF


Installation
============


!!!  IF YOU HAVE ALREADY AN OLDER VERSION OF BSF4Rexx OR BSF4ooRexx INSTALLED, PLEASE
UNINSTALL AND REMOVE IT FROM YOUR SYSTEM BEFORE INSTALLING THIS VERSION  !!!!


Archive 'BSF4ooRexx_install.zip'
==============================

   - after running "setupBSF.rex" the following files get created, tailored
     to your installation; the file names differ in their file extension:
     ".cmd" (Windows) and ".sh" (Linux):

        rexxj                   ... run ooRexx programs without installing BSF4ooRexx

        setEnvironment4BSF      ... set environment to allow running BSF4ooRexx programs
                                    from rexx.exe

        installBSF              ... install BSF4ooRexx systemwide
        uninstallBSF            ... uninstall BSF4ooRexx systemwide


     Installing as Administrator on Windows:

        installBSF_runAsAdministrator.cmd       ... will run installBSF.cmd as Administrator
        uninstallBSF_runAsAdministrator.cmd     ... will run uninstallBSF.cmd as Administrator

     Installing as root on Linux:

        installBSF_sudo.sh      ... will run installBSF.sh as root
        uninstallBSF_sudo.sh    ... will run uninstallBSF.sh as root




   Problems FAQ:


     - If the script does not run, because the binary java[.exe] is not on
       the path, then invoke the script and supply explicitly the path:

          rexx setupBSF.rex path-to-java[.exe]



Testing your BSF4ooRexx installation
====================================

   a) Issue one of the following commands from the command line in the "install"
      directory (this will load Rexx via Java):

        Windows:

                 rexxj infoBSF.rxj
              or
                 rexxj infoBSF-oo.rxj

        Unix:

                 rexxj.sh infoBSF.rxj
              or
                 rexxj.sh infoBSF-oo.rxj



   b) Issue one of the following commands from the command line in the "install"
      (this will cause Rexx to load Java, the 'setEnvironment4BSF' settings must
      be in effect for these tests to succed):

         rexx infoBSF.rxj
      or
         rexx infoBSF-oo.rxj

      If this does does not work, because Java cannot be found, please execute
      the script "setEnvironment4BSF.cmd" (Windows) or
      ". ./setEnvironment4BSF.sh" (Linux with bash shell). Then rerun the
      scripts above.



Next steps
==========

 - Run all the sample programs in "bsf4oorexx/samples/" and study them.

 - To study the new features introduced with "BSF4ooRexx 4.0" run and study all
   the programs in "bsf4oorexx/samples/version4" and its subdirectories.

 - Study the programs in "bsf4oorexx/utilities/".

 - If interested in scripting OpenOffice.org (the examples can be found in
   "bsf4oorexx/samples/OOo" and its subdirectories), then please follow the
   instructions in 'readmeOOo.txt' to install the specific OpenOffice support
   which makes it very easy to program/script OOo with ooRexx in an openplatform
   fashion.


Support, questions and discussion
=================================

   Please use the main Rexx newsgroup to post questions, example code etc.:

        <news:comp.lang.rexx>

   or Google's interface to this netnews group:

        <http://groups.google.at/group/comp.lang.rexx/topics>

   Alternatively, enroll as a member to the non-profit organization "Rexx Language
   Asssociation" (<http://www.RexxLA.org>) and use the members only listserver list
   there.

   OpenOffice-only related questions (other than the Rexx support itself) should be
   posted on the respective listserver lists at <http://www.OpenOffice.org> (e.g.
   "dev", "udk", "framework").


Good luck and have fun!

... and please tell us what you are using it for such that others may learn of
interesting problem solving applications with this technology.



Rony G. Flatscher
WU Wien
Vienna (Austria, Europe), 2010-12-04



---   ---   ---   ---   ---   ---   ---   ---   ---   ---   ---   ---   ---

Some hints ad Java:
-------------------

- In order to use Java as a huge external [oo]Rexx function library you
  merely need to have the "Java runtime environment (JRE)" installed on your
  machine. Probably it is installed already, due to support Java applets via
  your WWW-browser. Otherwise you can download and install Java e.g. from:

  <http://java.sun.com>.

- You can download the entire Java API documentation from Sun's Java site. The
  documentation is merely a set of plain HTML documents that are linked with
  each other. Alternatively, it is possible to point your WWW browser directly
  to Sun's Java WWW pages offering the very same documentation:

  <http://java.sun.com/docs/index.html>
     ... Overview page which allows you to choose which Java version's docs you
     want to browse, e.g. as of this writing the following "J2SE API Docs" were
     accessible:

        <http://java.sun.com/j2se/1.4.2/docs/api/>            ... Java 1.4.2
        or overview: <http://java.sun.com/j2se/1.4.2/docs/>

        <http://java.sun.com/j2se/1.5.0/docs/api/>            ... Java 1.5 aka
        or overview: <http://java.sun.com/j2se/1.5.0/docs/>       Java 5.0

        <http://java.sun.com/javase/6/docs/api/>              ... Java 1.6 aka
        or overview: <http://java.sun.com/javase/6/docs/>         Java 6.0

     The overview URL also contains plenty of links to tutorials and other
     valuable Java info material.



a) Installing Java, if not yet installed
========================================

   Open a command window and enter:

        java -version

   If "java" is not found or not executing Java (but a stub program as is
   sometimes the case on Linux) or the Java version is smaller than 1.4, then
   get a new Java from the Internet for your operating system and install it on
   your machine.  One free possibility is to download Java from Sun, the
   "father" of Java:

        http://java.sun.com

   It is sufficient to download the runtime version (JRE - Java runtime
   environment), but you can download the Java developer version (JDK - Java
   development kit) as well.

   To get free access at the full documentation of all Java classes, you may
   want to point your browser to:

        http://java.sun.com/docs/index.html

   Remark on Linux:
   ----------------

   On Linux, if you cannot execute "java -version" successfully, then study your
   Linux installation directions to make your installed Java available via the
   command line.

   Alternatively, you can run the supplied script (*Linux* only, part of the
   archive 'BSF4ooRexx_install.zip'!):

      rexx setupJava.rex

   which will try to locate possible installed versions of Java and will create
   symbolic links to point to your
   Java installation.

   As a result you can invoke Java from any process and the command line.



Installing ooRexx
=================

   Go and get the opensource and free "Open Object Rexx" (ooRexx) version 4.0 or
   higher interpreter from:

        <http://www.ooRexx.org/download.html>
        <http://sourceforge.net/project/showfiles.php?group_id=119701>

   Install ooRexx as per the instructions.


Further information on BSF4ooRexx
=================================

        - International Rexx Symposia, cf. <http://www.RexxLA.org>, look for
          "BSF" (should yield presentations and articles), especially:

             <http://wi.wu.ac.at/rgf/rexx/orx20/2009_orx20_BSF4Rexx-20091031-article.pdf>

        - Students' works at the "WU" (Wirtschaftsuniversitaet Wien, Austria),
          look for "BSF", cf.

             <http://wi.wu.ac.at/rgf/diplomarbeiten/>

        - <news:comp.lang.rexx>


