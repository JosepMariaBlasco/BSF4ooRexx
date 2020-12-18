BSF4ooRexx - Installation Directions for MacOS           17 December 2020 (Beta)
==============================================           =======================

Please note: there is a MacOS compatible GUI installation package that installs
             the latest version of ooRexx (currently 5.0 beta) together with
             the latest version of BSF4ooRexx which creates an entry "ooRexx"
             in the "Application" folder that supplies an Apple menu if opened.

BSF4ooRexx adds support to the ooRexx scripting language to camouflage
Java objects as Rexx objects.

It lets ooRexx programmers take full advantage of Java objects, but with
the simplicity of ooRexx (e.g., no typing or casting necessary, sending
ooRexx messages to Java objects)!

All such ooRexx programs work on all operating systems supporting ooRexx!


Prerequisites
=============

[1] Java Runtime Environment (JRE) 1.6 or higher installed on your computer.

[2] Version 4.2.0 of ooRexx. This is part of the MacOSX BSF4ooRexx GUI
    installer from <http://sourceforge.net/projects/bsf4oorexx/files/>

[3] optional: OpenOffice.org (OOo) from <http://download.openoffice.org> or its fork
              LibreOffice (LO) from <https://www.libreoffice.org/download/download/>

[4] Uninstall any previously installed version of BSF4ooRexx, if any:

    - cf. "readme-Linux.txt"

    - if the GUI installation package was used, then open "ooRexx" in the
      "Application" folder, pick "Installation -> Uninstall BSF4ooRexx"



Installation
============

Either double-click on the GUI installer (see URL above) and afterwards you are done!

Alternatively, use the general command-line install:

Step [1] Download the archive "BSF4ooRexx_install.zip" and unzip it

Step [2] Open a Terminal window and change the directory to
         "bsf4oorexx/install/macosx"

Step [3] Run the shell program "sh install.sh"

That's it.

Have fun!

Rony G. Flatscher, Vienna, Austria, Europe


P.S.: Support is given either via the newsgroup <news:comp.lang.rexx> or the
      private e-mail list of the non-profit Rexx Language Association
      <http://www.rexxla.org> where you meet a good many helping experts!

      In addition check out the mailing lists and forums on the BSF4ooRexx
      home at <http://sourceforge.net/projects/bsf4oorexx/>.

P.P.S.: Restrictions on MacOSX:

        - Run awt/swing/JavaFX-applications with "rexxj.sh" only.

        - Using the executable "rexx" will load and start Java in "headless" mode,
          which means that neither the awt nor the JavaFX event dispatch thread
          can be created.

