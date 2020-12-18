BSF4ooRexx - Installation Directions for Linux           17 December 2020 (Beta)
==============================================           =======================
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

    Home: <http://www.java.com>

[2] ooRexx 4.2.0 or higher from its home: <http://www.ooRexx.org>. Please note:
    it is highly advised to use the latest ooRexx 5.0 beta, which is stabler and
    has many great new features!

[3] optional: Apache OpenOffice.org (OOo) from <http://download.openoffice.org>

    Please note: on Linux it is possible that LibreOffice (LO), a fork
    of OpenOffice, is installed already. It can be downloaded as well
    from <https://www.libreoffice.org/download/download/>.

[4] Uninstall any previously installed version of BSF4[oo]Rexx, if any.

    Choose the menu "Applications -> BSF4ooRexx ->  Installation -> Uninstall BSF4ooRexx"

    [To manually uninstall, try to locate the following two scripts and execute
     them in the following sequence:

        Linux                   Comment
        -------                 -------
        uninstallOOo.sh         run, if the OOo support was installed

        uninstallBSF.sh         uninstalls the old BSF4[oo]Rexx support]


Installation
============

Either get the GUI-installer for your platform (deb, rpm) from
<http://sourceforge.net/projects/bsf4oorexx/files/> and double-click on
them.

Alternatively use the command-line version:

Step [1] Download the archive "BSF4ooRexx_install.zip" and unzip it

Step [2] Open a terminal window and change the directory to
         "bsf4oorexx/install/linux"

Step [3] Run the shell program "sh install.sh"

That's it.

Have fun!

Rony G. Flatscher, Vienna, Austria, Europe


P.S.: Support is given either via the newsgroup <news:comp.lang.rexx> or the
      private e-mail list of the non-profit Rexx Language Association
      <http://www.rexxla.org> where you meet a good many helping experts!

      In addition check out the mailing lists and forums on the BSF4ooRexx
      home at <http://sourceforge.net/projects/bsf4oorexx/>.

