BSF4ooRexx - Installation Directions for MacOSX                 12 May 2015 (Beta)
===============================================

BSF4ooRexx adds support to the ooRexx scripting language to camouflage
Java objects as Rexx objects.

It lets ooRexx programmers take full advantage of Java objects, but with
the simplicity of ooRexx (e.g., no typing or casting necessary, sending
ooRexx messages to Java objects)!

All such ooRexx programs work on all operating systems supporting ooRexx!

NOTE: there are 32- and 64-bit native MacOSX installation packages that
install ooRexx 4.2.0 together with BSF4ooRexx. It is suggested to install
the 64-bit package.


Prerequisites
=============

[1] Java Runtime Environment (JRE) 1.4 or higher installed on your computer.

[2] Version 4.2.0 of ooRexx. This is part of the MacOSX BSF4ooRexx GUI
    installer from <http://sourceforge.net/projects/bsf4oorexx/files/>

[3] optional: OpenOffice.org (OOo) from <http://download.openoffice.org>

[4] Uninstall any previously installed version of BSF4[oo]Rexx, if any.



Installation
============

Either double-click on the GUI installer (see URL above).

Alternatively use the general command-line install:

Step [1] Download the archive "BSF4ooRexx_install.zip" and unzip it

Step [2] Open the folder "bsf4oorexx/installation/macosx"

Step [3] Double-click on or execute the program "install.sh"

That's it.

Have fun!

Rony G. Flatscher, Vienna, Austria, Europe


P.S.: Support is given either via the newsgroup <news:comp.lang.rexx> or the
      private e-mail list of the non-profit Rexx Language Association
      <http://www.rexxla.org> where you meet a good many helping experts!

      In addition check out the mailing lists and forums on the BSF4ooRexx
      home at <http://sourceforge.net/projects/bsf4oorexx/>.

P.P.S.: Restrictions on MacOSX:

        - Run awt/swing-applications with "rexxj.sh" only.

        - Using the executable "rexx" will load and start Java in "headless" mode,
          which means that no awt event dispatch thread can be created. This is
          due to some pecularities of MacOSX. (This restriction may be lifted in
          the future, when "rexx" will create a "CFRunLoop".)

