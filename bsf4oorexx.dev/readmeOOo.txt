The ooRexx OpenOffice.org Support (Using BSF4ooRexx)     January 2024 (Beta)
====================================================     ===================

This adds support for interfacing with OpenOffice/StarOffice/LibreOffice using
BSF4ooRexx850, which therefore must be installed already. Please note: by default the
BSF4ooRexx850 installer will install the OOo-support, if OOo or LO is installed on the
machine.

Please see also the text file "changesOOo.txt" which contains important
information about changes in the OpenOffice.org (OOo) /Staroffice (SO) /
LibreOffice (LO) support.

Installation of the OpenOffice support in a Nutshell
====================================================

1) If there is a previous version of the OpenOffice support installed on your
   system, please deinstall it by executing "uninstallOOo.cmd" (Windows) or
   "uninstallOOo.sh" (Linux).

2) Close all instances of OpenOffice/LibreOffice including the quickstarter!

3) change into the subdirectory "bsf4oorexx/install"

   - run the Rexx setup program, e.g.

         rexx setupOOo.rex

         - this will create scripts customized matching your installation and
           operating system:

           - on Windows run

                installOOo_runAsAdministrator.cmd

             on Linux run (and afterwards logoff and logon)

                ./installOOo_sudo.sh

4) done.


Good luck and have fun!

Rony G. Flatscher
WU Wien
Vienna (Austria, Europe)

P.S.: Further information can be found in "readmeOOo_next.txt"
