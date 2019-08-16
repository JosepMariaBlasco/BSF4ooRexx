BSF4ooRexx, 6.41                                         3 February 2019 (Beta)
================                                         ======================

This is a package that creates a bridge between ooRexx and Java. For that
reason Java must be installed on your machine as well, which is easy and
a one click away e.g. from <http://www.java.com>. See box below for an
important information.

PLEASE NOTE: this version of BSF4ooRexx is for Java 1.6/6, 1.7/7, 1.8/8
             and Java 9.0 (introduced the module system) and newer


*IMPORTANT* !

    *AFTER DOWNLOADING* the installation zip-archive (e.g.  BSF4ooRexx_install_*zip)
    but *BEFORE UNZIPPING* make sure that no "security" attributes are attached to it.

    On *WINDOWS*: right mouse click on the zip-file and choose "Unblock" from the
                  Property menu, *then* unzip. Here are directions by Microsoft:
                  <https://blogs.msdn.microsoft.com/delay/p/unblockingdownloadedfile/>

                  [If you do not do this, unexpected/unexplainable errors may
                  occur, when using the new .NET support, as the Windows
                  unzip utility would mark all unzipped files as a security risk.]



*******************************************************************************
* *ATTENTION*  Java's bitness must match the bitness of ooRexx ("rexx -v"):   *
*                                                                             *
*    - if ooRexx is 32-bit, then you must install the 32-bit version of Java, *
*    - if ooRexx is 64-bit, then you must install the 64-bit version of Java  *
*                                                                             *
*      -> consult after unzipping:  bsf4oorexx/information/0_startHere.html   *
*                                                                             *
*******************************************************************************


===============================================================================

HINT: after unzipping the zip-archive you can get additional installation information
      beyond this readme file by placing your browser to the file:

         bsf4oorexx/information/0_startHere.html



0) If there is a previous version of BSF4Rexx or BSF4ooRexx installed on your
   system, please deinstall it first!

   Best use the menu "BSF4ooRexx -> Installation -> Uninstall BSF4ooRexx".

           [A fallback, manual deinstallation, would be to locate the path to the
           installation directory "bsf4oorexx", named "path2-bsf4oorexx" in the
           following commands, and then run (e.g. by double-clicking) the
           appropriate uninstall script:
           - "path2-bsf4oorexx\install\uninstallBSF.cmd" (Windows) or
           - "sudo path2-bsf4oorexx/install/uninstallBSF.sh" (Linux)
           ]

1) Unzip the downloaded installation archive "BSF4ooRexx_install-*.zip" (this
   will also unzip the Apache OpenOffice scripting-support for ooRexx)

2) change into the unzipped subdirectory "bsf4oorexx/install/windows" or
   "bsf4oorexx/install/linux" or "bsf4oorexx/install/macosx", depending on
   your operating system and then run as Administrator/superuser/sudo

   - the setup program named "install.cmd" (Windows) or "install.sh"
    (Linux, MacOSX), e.g. by double-clicking the installation scripts
    or opening a console/terminal changing to the above location and
    enter "install.cmd" or "sh install.sh" manually which will cause
    elevation to Administrator/uperuser/sudo

    (you may need to run from the console/terminal as lately some installations
    inhibit running files downloaded from the Internet from a GUI interface)

Good luck and have fun!

Rony G. Flatscher
WU Wien
Vienna (Austria, Europe)

