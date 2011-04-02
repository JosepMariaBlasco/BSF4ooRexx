BSF4ooRexx, 4.05 Dec 2010, GA (general availability)
====================================================

BSF4ooRexx, version 4.05: this version exploits the new APIs of ooRexx 4.0.1 and
as such can only be used with ooRexx 4.0.1 or newer, hence the renaming of the
package from "BSF4Rexx" to "BSF4ooRexx".

The files "changesBSF4ooRexx.txt" and "changesOOo.txt" give brief information
about the changes.

"readmeOOo.txt" gives instruction on creating the OpenOffice/StarOffice support
for your system.


*******************************************************************************
* *ATTENTION*  Java's bitness must match the bitness of ooRexx ("rexx -v"):   *
*                                                                             *
*    - if ooRexx is 32-bit, then you must install the 32-bit version of Java, *
*    - if ooRexx is 64-bit, then you must install the 64-bit version of Java  *
*                                                                             *
*******************************************************************************


Installation of BSF4ooRexx in a Nutshell
========================================

0) If there is a previous version of BSF4Rexx or BSF4ooRexx installed on your
   system, please deinstall it by executing "uninstallBSF.cmd" (Windows) or
   "uninstallBSF.sh" (Linux).

1) unzip the archive "BSF4ooRexx_install.zip" into the target directory
   (this will also unzip the OpenOffice/StarOffice-support files)

   - Linux-hint: you may want to unzip the archive into the "/opt" directory


2) change into the subdirectory "bsf4oorexx/install"

   - run the Rexx setup program, e.g.

         rexx setupBSF.rex

         - this will create scripts customized matching your installation and
           operating system:

           - on Windows run

                installBSF_runAsAdministrator.cmd

             on Linux run (and afterwards logoff and logon)

                ./installBSF_sudo.sh


3) done.


Good luck and have fun!

Rony G. Flatscher
WU Wien
Vienna (Austria, Europe), 2010-12-04

P.S.: Further information can be found in "readmeBSF4ooRexx_next.txt"


