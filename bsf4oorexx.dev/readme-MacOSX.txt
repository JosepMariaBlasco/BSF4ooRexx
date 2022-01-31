BSF4ooRexx - Installation Directions for MacOS           31 January 2022 (GA)
==============================================           ====================
Home: <http://sourceforge.net/projects/bsf4oorexx/>

BSF4ooRexx adds support to the ooRexx scripting language to camouflage
Java objects as Rexx objects.

It lets ooRexx programmers take full advantage of Java objects, but with
the simplicity of ooRexx (e.g., no typing or casting necessary, sending
ooRexx messages to Java objects)!

All such ooRexx programs work on all operating systems supporting ooRexx!


Prerequisites
=============

[1] Java Runtime Environment (JRE) 1.6 or higher installed on your computer.
    Usually the JRE gets installed with your web browser to allow for running
    Java applets from the Internet.
    Sometimes it is possible to run Java from the commandline. To check, open
    a command line window and enter: "java -version"

    Home:
         - azul.com <https://www.azul.com/downloads/?package=jdk#download-openjdk>

                - On the download page choose either "JDK FX" or "JRE FX" to
                  get the Java version with the great JavaFX support

         - bellsoft <https://bell-sw.com/pages/downloads/>

                - On the download page choose "Full JDK" or "Full JRE" to
                  get the Java version with the great JavaFX support

         - Oracle <http://www.java.com> (note the license!)


[2] ooRexx 5.0.0 or newer from its home: <https://sourceforge.net/projects/oorexx/files/oorexx/>.
    (The current BSF4ooRexx baseline for ooRexx is version 4.1.0, so this
    version of ooRexx and later versions are supported.)

[3] optional: Apache OpenOffice.org (OOo/AOO) from <http://download.openoffice.org> or
              LibreOffice (LO) from <https://www.libreoffice.org/download/download/>

[4] Uninstall any previously installed version of BSF4[oo]Rexx, if any.

    Prerequisite: close all instances of OpenOffice/LibreOffice, also the quickstarter!

    Choose the menu "Applications -> BSF4ooRexx ->  Installation -> Uninstall BSF4ooRexx"

    [To manually uninstall, try to locate the following two scripts and execute
     them in the following sequence:

        MacOSX                                  Comment
        ------                                  -------
        /opt/BSF4ooRexx/install/uninstallOOo.sh run, if the OOo support was installed

        /opt/BSF4ooRexx/install/uninstallBSF.sh uninstalls the previous BSF4[oo]Rexx support]


Installation
============

        Step [1] Close all instances of OpenOffice/LibreOffice including the quickstarter

        Step [2] Download the archive "BSF4ooRexx_install_v641-YYYYMMDD.zip" and unzip it

        Step [3] Open a terminal window and change into the unzipped directory into
                 "bsf4oorexx/install/macosx"

        Step [4] Run the shell program "sh install.sh" which will copy
                 everything to "/opt/BSF4ooRexx", create the installation
                 and uninstall scripts in "/opt/BSF4ooRexx/install" and
                 runs the install scripts

        Step [5] Open the app "/Applications/ooRexx.app" and from the menu
                 bar open "GUI Rexxtry Program (ooRexxTry.rxj)" to test the
                 installation (you can enter Rexx statements and execute them
                 in this window)

That's it.

Have fun!

        Important Note for MacOSX
        =========================

        - Run GUI applications (using awt/swing/JavaFX) with "rexxj.sh" instead
          of "rexx"

        - Using the executable "rexx" will load and start Java in "headless" mode,
          such that GUI applications (using awt/swing/JavaFX) cannot be created.


Rony G. Flatscher, Vienna, Austria, Europe


P.S.: Support is given either via the newsgroup <news:comp.lang.rexx> or the
      private e-mail list of the non-profit Rexx Language Association
      <http://www.rexxla.org> where you meet a good many helping experts!

      In addition check out the mailing lists and forums on the BSF4ooRexx
      home at <http://sourceforge.net/projects/bsf4oorexx/>.



