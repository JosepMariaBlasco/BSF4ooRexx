Additional information on the OpenOffice.org support that may be interesting 2012-02-25 BETA
============================================================================================

The ooRexx OpenOffice.org (OOo) support is based on BSF4ooRexx as it uses
the Java interfaces to and from OOo.  After installation of the OOo support
the branches from the OOo-menu "Tools -> Macros" will contain an entry
"ooRexx" indicating that it is now possible to create and run OOo macros
implemented in ooRexx.

The component technology OOo uses is built with the programming language
independent "Universal Network Objects (UNO)", which can be used on its
own for creating cross-platform applications.

The ooRexx support is created at the UNO-level and can therefore be used for
interfacing with any UNO component technology, not only OOo!  The following
ooRexx programs constitute the UNO support with specific OOo support built on
top of it:

   UNO.CLS                       ... an ooRexx module defining UNO proxies and
                                 making it easy to query interface objects from
                                 service objects by merely sending the
                                 (unqualified) interface name to the UNO proxy;

   UNO_XINTERFACES.REX           ... an ooRexx module defining the names of all
                                 OOo interface classes as of OOo version 2.1.0
                                 (January 2007) which increases performance;
                                 since July 2006 UNO.CLS has a fall-back
                                 solution in place, which uses the UNO
                                 reflection mechanism at runtime

   UNO_CREATE_INTERFACE_LIST.REX ... not needed, but could be used to create
                                 the above ooRexx program "UNO_XINTERFACES.REX";
                                 it is part of the distribution for completeness
                                 only

The following Java archive enables OOo to use ooRexx as a macro language:

   ScriptProviderForooRexx.jar   ... Java archive containing the glue code
                                 between OOo's Java based scripting framework
                                 and ooRexx, employing the Jakarta BSF framework
                                 (it would be very easy to use BSF to add
                                 additional languages as macro languages to OOo)


The OOo ("scripting") support allows to create (commandline) ooRexx openplatform
scripts that can interface and remote control OOo on the one hand.  On the other
hand OOo itself can invoke ooRexx macros.

BSF allows Java programs to call (invoke) script programs written in languages
other than Java.  Such script programs may also be able to call back into Java
and use all of Java's classes and objects.

- With "BSF4ooRExx" any Java application can use (Object) Rexx as a scripting
  language

- Open Object Rexx can use Java as a huge, operating system independent function
  library.  (The Object Rexx support enabled with "BSF.CLS" makes Java look like
  a huge Object Rexx class library!)



Files and Archives
==================

readmeBSF4ooRexx.txt ... text file, outlining the installation of BSF4ooRexx

readmeBSF4ooRexx_next.txt ... this text file, explaining BSF4ooRexx and its installation

readmeOOo.txt        ... text file, outlining the installation of the OpenOffice support

readmeOOo_next.txt   ... text file, explaining the OpenOffice.org/Staroffice
                         support and its installation

BSF4ooRexx_install.zip ... Archive that contains the binary distribution of, the
                         sources of and the samples for BSF4ooRexx (including
                         OpenOffice.org samples), the setup scripts for Unix
                         and Windows


changesBSF4ooRexx.txt  ... text file containing a record of the most important
                         changes

changesOOo.txt       ... text file containing a record of the most important
                         changes since the 2005 International Rexx symposium

ApacheLicense2.0.txt ... Apache license 2.0 text

BSF4ooRexx_sources.zip ... Archive that contains the C++ and Java sources

docs_bsf4rexx.zip    ... Archive that contains the documentation of the BSF4ooRexx
                         Java programs, including add-ons

docs_apache_bsf.zip  ... Archive that contains the documentation of Apache's BSF



The following Java archive enables OOo to use ooRexx as a macro language:

ScriptProviderForooRexx.jar ...  Java archive containing the glue code between
                         OOo's Java based scripting framework and ooRexx,
                         employing the Jakarta BSF framework (it would be very
                         easy to use BSF to add additional languages as macro
                         languages to OOo)



Installation
============

Install BSF4ooRexx
==================

   Cf. "readmeBSF4ooRexx.txt" for instructions.


Get and install OpenOffice.org (OOo) / StarOffice (SO)
======================================================

   You need to have OpenOffice.org installed before being able to install the specific
   ooRexx support for it.

   * ATTENTION *  The bitness of OpenOffice.org must match the bitness of ooRexx ("rexx -v"):

        - if ooRexx is 32-bit, then you must install the 32-bit version of OpenOffice.org,
        - if ooRexx is 64-bit, then you must install the 64-bit version of OpenOffice.org


   Go and get OpenOffice.org from <http://www.OpenOffice.org>: it is free, opensource,
   small in size, multiplatform and is able to read and write Microsoft Office documents!

   Starting with version 2.0 the open-standard OASIS document format is used!

   Installation is done within a minute or two and off you go...

   Note (especially for Linux users): if your installed version of OpenOffice.org does not
   include the Java support, then ooRexx cannot interact with that version.
   (Some Linux distributions compile OpenOffice.org without the Java support!)
   In such a case please download the "genuine" OpenOffice.org package and install that.


   Make sure that Java is enabled for OpenOffice.org
   -------------------------------------------------

   - start e.g. OOo Writer, choose the menu option 'Tools -> Options... ->
     OpenOffice.org -> Java": make sure that the check-box is set next to "Use a
     Java runtime-environment"

   - If you had to change the OOo Java configuration then you need to close
     *all* running instances of OOo; do not forget to close the quick-launcher
     as well!

     This will make sure that on the next start of any OOo component this new
     configuration takes in effect as OOo has to re-read the configuration
     from scratch.



Install the OOo support
=======================
  - * ATTENTION LINUX USERS *     * ATTENTION LINUX USERS *   * ATTENTION LINUX USERS *

    Quite a few Linux distributions preinstall OpenOffice.org in a configuration that
    does not have the standardized Java-based scripting framework. Therefore, if you
    are not able to install and utilize the OOo support of BSF4ooRexx, then please
    use the official Linux-Distribution of OpenOffice by downloading it from

        http://www.OpenOffice.org

    After the installation of the official distribution of OpenOffice.org everything
    should work as explained in this readme.


  - Run "rexx setupOOo.rex path-to-OOo-directory"

    - e.g. "rexx setupOOo.rex d:\Programme\OpenOffice.org 2.3"

      This will create the following scripts:

        - "installOOo.cmd" (Windows) or
          "installOOo.sh"  (Unix)

            --> this is the installer which is tailored to your specific environment

            --> run this generated script to install the ooRexx OpenOffice.org support,
                such that ooRexx can be used as a macro language (you must close all
                running instances of OpenOffice including the quickstarter, and then
                restart OpenOffice to get the support activated)


        - "uninstallOOo.cmd" (Windows) or
          "uninstallOOo.sh"  (Unix)

            --> this is the uninstaller which matches the tailored installation script above;

            --> run it to uninstall the ooRexx OpenOffice.org support

        - "setEnvironment4OOo.cmd" (Windows) or
          "setEnvironment4OOo.sh"  (Unix)

            --> this a script that sets the environment variables to point to OpenOffice.org
                Java archive files, such that you can run ooRexx scripts from the command line;
                run it, before invoking any ooRexx script that drives OpenOffice.org

            --> hint: you can put those environment variable definitions into
                your logon-script or into your machine's set of pre-set
                environment variables); then start your ooRexx-OOo-scripts by
                issuing "rexx my-ooRexx-scriptfile.rex"


Testing the installation
========================

      - After running "installOOo.{cmd|sh} you must close *all* running instances of OOo;
        do not forget to close the quick-launcher as well!

        This will make sure that on the next start of any OOo component this new
        configuration takes in effect as OOo has to re-read the configuration from
        scratch.

        This will make sure that the following test-scripts can run successfully, if the
        installation was successfull!


      - To test your ooRexx-OOo installation open a new command line window, change into
        the "install" directory and issue the following commands (file extension ".cmd"
        for Windows, ".sh" for Unix):

                Windows:
                          setEnvironment4OOo.cmd
                          rexxj.cmd testOOo.rex
                          rexxj.cmd testOOo2.rex
                          rexx testOOo.rex
                          rexx testOOo2.rex

                Unix:

                          . ./setEnvironment4OOo.sh
                          ./rexxj.sh testOOo.rex
                          ./rexxj.sh testOOo2.rex
                          rexx testOOo.rex
                          rexx testOOo2.rex



Further information/documentation on Automating OOo/SO with BSF4ooRexx
======================================================================

   - Information resources at the WU (Wirtschaftsuniversitaet Wien): point your browser
     to <http://wi.wu-wien.ac.at/rgf/diplomarbeiten/> and locate all entries having
     the name "OOo" or "OpenOffice" in the title. Starting with fall 2005 the explanations
     and ooRexx nutshell examples should work with the current UNO.CLS ooRexx module (earlier
     work was dependent on the outdated ooRexx module OOO.CLS).

     The nutshell examples from the following two papers are part of the BSF4ooRexx
     distribution and stored in subdirectories of "samples/OOo":

     Andreas Ahammer: "OpenOffice.org Automation: Object Model, Scripting Languages,
                       'Nutshell'-Examples"
          <http://wi.wu-wien.ac.at/rgf/diplomarbeiten/index.html#bakk_07>

     Martin Burger: "OpenOffice.org Automatisation with Object Rexx"
          <http://wi.wu-wien.ac.at/rgf/diplomarbeiten/index.html#bakk_200601>

     Goerlich, Gerhard; Realfsen, Asmund; Spanberger, David:
      "BSF4Rexx and OpenOffice.org Nutshell-Examples"
          <http://wi.wu-wien.ac.at/rgf/diplomarbeiten/Seminararbeiten/2006s_wu/20060628_BSF4RexxSnippets_version_4.pdf>

     In general you may look <http://wi.wu-wien.ac.at/rgf/diplomarbeiten/> for student's
     work relating to OpenOffice.org.

     All the nutshell examples of this work should run out of the box on your machine under
     Windows and Unix, and help you jump-start controlling OOo from Rexx! All of the
     nutshell examples are distributed with this package under "samples/OOo" in a slightly
     edited form.

   - The following pointers may be of help as well:

          <http://wi.wu-wien.ac.at/rgf/rexx/orx16/2005_orx16_Gluing2ooRexx_OOo.pdf>

          <http://wi.wu-wien.ac.at/rgf/rexx/orx16/2005_orx16_NutShell_OOo.pdf>
          Please note: the file 'OOO.CLS' has been replaced/superceded by 'UNO.CLS'.

   - The OOo-homepage with links to the *excellent* DeveloperGuide (a *must* !):

     OOo homepage:

          <http://www.openOffice.org>

     *Excellent* "OOo Developer's Guide" homepage (there exists also a PDF version):

          <http://api.openoffice.org/DevelopersGuide/DevelopersGuide.html>

     *Excellent* Snippets homepage (featuring ooRexx samples since 2006-07 !):

          <http://codesnippets.services.openoffice.org/>


  Good luck and have fun !

---

Rony G. Flatscher
WU Wien
Vienna (Austria, Europe)



---   ---   ---   ---   ---   ---   ---   ---   ---   ---   ---   ---   ---

De-/Install the ooRexx Macro Support Within OOo Manually
========================================================

The install script "installOOo.{cmd|sh}" adds the package
'ScriptProviderForooRexx.jar' to OpenOffice.org, which makes it possible to
invoke ooRexx macros from within OOo.  This way wherever a (Basic, Python)
script can be defined within OOo one can now define an ooRexx script as well.

One can remove and then add that package from the commandline by issuing either

    Windows:
       unopkg %OOoHOME%\program\unokpg remove -shared ScriptProviderForooRexx.jar

    Unix:
       unopkg $OOoHOME/program/unokpg remove -shared ScriptProviderForooRexx.jar

or (to add the removed package later on):

    Windows:
       unopkg %OOoHOME%\program\unokpg add -shared ScriptProviderForooRexx.jar

    Unix:
       unopkg $OOoHOME/program/unokpg add -shared ScriptProviderForooRexx.jar


Where "%OOoHOME%" or "$OOoHOME" should be replaced by the fully qualified path
pointing to the OpenOffice root directory.

After removing or adding that package, you must close all instances of OOo,
including the quick starter.  This forces the OOo modules to read the (new)
configuration from scratch, the next time one gets invoked.




---   ---   ---   ---   ---   ---   ---   ---   ---   ---   ---   ---   ---

Further information on the BSF4ooRexx support for OpenOffice.org
================================================================

        - International Rexx Symposia, cf. <http://www.RexxLA.org>, look for
          "OpenOffice" or "OOo" (should yield presentations and articles)

        - Students' works at the "WU" (Wirtschaftsuniversitaet Wien, Austria),
          look for "OpenOffice" or "OOo", cf.

             <http://wi.wu.ac.at/rgf/diplomarbeiten/>

        - <news:comp.lang.rexx>

        - Snippet homepage of OpenOffice.org, choose "ooRexx" in the subpages
          of: <http://codesnippets.services.openoffice.org/index.xml>


