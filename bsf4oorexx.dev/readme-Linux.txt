BSF4ooRexx - Installation Directions for Linux                  4 Dec 2010
==============================================

BSF4ooRexx adds support to the ooRexx scripting language to camouflage
Java objects as Rexx objects.

It lets ooRexx programmers take full advantage of Java objects, but with
the simplicity of ooRexx (e.g., no typing or casting necessary, sending
ooRexx messages to Java objects)!

All such ooRexx programs work on all operating systems supporting ooRexx!


Prerequisites
=============

[1] Java Runtime Environment (JRE) 1.4 or higher installed on your computer.
    Usually the JRE gets installed with your web browser to allow for running
    Java applets from the Internet.
    Sometimes it is possible to run Java from the commandline. To check, open
    a command line window and enter: "java -version"

[2] A version newer than ooRexx 4.0.1 as a bug-fix (rev. 6133) is needed for the
    OpenOffice support. You can easily fetch the latest successful build of ooRexx
    from <http://build.oorexx.org> (or try to find a matching Linux-ooRexx in the
    subdirectory "linux-oorexx-6135").

[3] optional: OpenOffice.org from <http://download.openoffice.org/index.html>.

    Please note: on Linux it is possible that your distribution has an altered
    version of OpenOffice installed, which may not be compatible in its Java
    interfaces with the "genuine OpenOffice" package. In that case uninstall
    your distribution's OOo and get the "genuine OpenOffice" from the above URL.

[4] Uninstall any previously installed version of BSF4[oo]Rexx, if any.

    Choose the menu "Applications -> BSF4ooRexx ->  Installation -> uninstall"

    [If you have an old installation of BSF4ooRexx (installed before July 2010) then
     run the     scripts created in the old installation folder in the following
     sequence:

        Linux                   Comment
        -------                 -------
        uninstallOOo.sh         run, if the OOo support was installed

        uninstallBSF.sh         uninstalls the old BSF4[oo]Rexx support]


Installation
============


Step [1] Download the archive "BSF4ooRexx_install.zip" and unzip it

Step [2] Open the folder "bsf4oorexx/installation/linux"

Step [3] Double-click on or execute the program "install.sh"

That's it.

Have fun!

Rony G. Flatscher, Vienna, Austria, Europe


P.S.: Support is given either via the newsgroup <news:comp.lang.rexx> or the
      private e-mail list of the non-profit Rexx Language Association
      <http://www.rexxla.org> where you meet a good many helping experts!

