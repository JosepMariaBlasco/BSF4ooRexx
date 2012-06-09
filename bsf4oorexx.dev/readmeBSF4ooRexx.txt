BSF4ooRexx, 4.10 9 June 2012, BETA
==================================

BSF4ooRexx, version 4.10: this version makes the configuration of individual
Rexx interpreter instances possible, as well implementing Rexx exit handlers
and Rexx command handlers in Java/NetRexx.

*******************************************************************************
* *ATTENTION*  Java's bitness must match the bitness of ooRexx ("rexx -v"):   *
*                                                                             *
*    - if ooRexx is 32-bit, then you must install the 32-bit version of Java, *
*    - if ooRexx is 64-bit, then you must install the 64-bit version of Java  *
*                                                                             *
*******************************************************************************

0) If there is a previous version of BSF4Rexx or BSF4ooRexx installed on your
   system, please deinstall it by executing "bsf4oorexx\install\uninstallBSF.cmd"
   (Windows) or "bsf4oorexx/install/uninstallBSF.sh" (Linux) or best: via the
   "uninstall" menu entry in the BSF4ooRexx installation menu.

1) unzip the installation archive "BSF4ooRexx_install-*.zip" (this will also unzip
   the OpenOffice/StarOffice-support files)

2) change into the subdirectory "bsf4oorexx/install/windows" or
   "bsf4oorexx/install/linux" or "bsf4oorexx/install/macosx", depending on
   your operating system and then

   - run the setup program named "install.cmd" (Windows) or "install.sh"
    (Linux, MacOSX), e.g. by double-clicking the installation script

Good luck and have fun!

Rony G. Flatscher
WU Wien
Vienna (Austria, Europe)


P.S.: Alternative installation instructions

Installation of BSF4ooRexx in a Nutshell
========================================

0) If there is a previous version of BSF4Rexx or BSF4ooRexx installed on your
   system, please deinstall it by executing "bsf4oorexx\install\uninstallBSF.cmd"
   (Windows) or "bsf4oorexx/install/uninstallBSF.sh" (Linux) or best: via the
   "uninstall" menu entry in the BSF4ooRexx installation menu.

1) unzip the installation archive "BSF4ooRexx_install-*.zip" (this will also unzip
   the OpenOffice/StarOffice-support files)

2) change into the subdirectory "bsf4oorexx/install"

   - run the Rexx setup program, e.g.

         rexx setupBSF.rex

         - this will create scripts customized matching your installation and
           operating system:

           - on Windows run

                installBSF_runAsAdministrator.cmd

                -> hint: on newer Windows systems the user "Administartor" may not
                         be active, which causes the installation to abort with an
                         error; in that case edit the generated runas-script
                         "installBSF_runAsAdministrator.cmd" and replace the user
                         "Administrator" with the name of a user that has administrative
                         rights (belongs to the "Administrator" group) on your computer

             on Linux run (and afterwards logoff and logon)

                ./installBSF_sudo.sh


3) done.


P.P.S.: Further information can be found in "readmeBSF4ooRexx_next.txt"
